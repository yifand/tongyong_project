package com.vdc.pdi.common.exception;

import com.vdc.pdi.common.dto.ApiResponse;
import com.vdc.pdi.common.enums.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========== 业务异常 ==========

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException e, HttpServletRequest request) {
        logger.warn("业务异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(e.getResultCode(), e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthException(AuthException e, HttpServletRequest request) {
        logger.warn("认证异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(e.getResultCode(), e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponse<Map<String, String>> handleValidationException(ValidationException e, HttpServletRequest request) {
        logger.warn("参数校验异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        ApiResponse<Map<String, String>> response = ApiResponse.error(ResultCode.VALIDATION_ERROR, e.getMessage());
        response.setData(e.getErrors());
        return response;
    }

    // ========== 参数校验异常（Spring Validation） ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponse<Map<String, String>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        logger.warn("参数校验失败 [{}]", request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ResultCode.VALIDATION_ERROR, "请求参数校验失败");
        response.setData(errors);
        return response;
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponse<Map<String, String>> handleBindException(
            BindException e, HttpServletRequest request) {
        logger.warn("参数绑定失败 [{}]", request.getRequestURI());

        Map<String, String> errors = e.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "参数错误",
                        (existing, replacement) -> existing
                ));

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ResultCode.VALIDATION_ERROR, "参数绑定失败");
        response.setData(errors);
        return response;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponse<Map<String, String>> handleConstraintViolation(
            ConstraintViolationException e, HttpServletRequest request) {
        logger.warn("约束校验失败 [{}]", request.getRequestURI());

        Map<String, String> errors = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ResultCode.VALIDATION_ERROR, "约束校验失败");
        response.setData(errors);
        return response;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        logger.warn("缺少请求参数 [{}]: {}", request.getRequestURI(), e.getParameterName());
        return ApiResponse.error(ResultCode.BAD_REQUEST,
                "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        logger.warn("请求体解析失败 [{}]", request.getRequestURI());
        return ApiResponse.error(ResultCode.BAD_REQUEST, "请求体格式错误");
    }

    // ========== 请求相关异常 ==========

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoHandlerFound(
            NoHandlerFoundException e, HttpServletRequest request) {
        logger.warn("资源不存在 [{}] {} {}",
                request.getRequestURI(), e.getHttpMethod(), e.getRequestURL());
        return ApiResponse.error(ResultCode.NOT_FOUND, "请求的资源不存在");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        logger.warn("请求方法不支持 [{}]: {}", request.getRequestURI(), e.getMethod());
        return ApiResponse.error(ResultCode.METHOD_NOT_ALLOWED,
                "不支持的请求方法: " + e.getMethod());
    }

    // ========== 系统异常 ==========

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return ApiResponse.error(ResultCode.INTERNAL_ERROR, "系统繁忙，请稍后重试");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return ApiResponse.error(ResultCode.INTERNAL_ERROR, "系统运行错误");
    }
}
