package com.vdc.platform.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final com.vdc.platform.service.IEdgeBoxService edgeBoxService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String boxId = request.getHeader("X-Box-Id");
        String boxSecret = request.getHeader("X-Box-Secret");

        if (boxId == null || boxId.isBlank() || boxSecret == null || boxSecret.isBlank()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing X-Box-Id or X-Box-Secret");
            return;
        }

        var box = edgeBoxService.lambdaQuery().eq(com.vdc.platform.entity.EdgeBox::getBoxId, boxId).one();
        if (box == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid box id");
            return;
        }
        if (box.getSecretKey() == null || !box.getSecretKey().equals(boxSecret)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid box secret");
            return;
        }

        String body = request.getReader().lines().reduce("", (a, b) -> a + b);
        if (!body.isBlank()) {
            try {
                JsonNode json = objectMapper.readTree(body);
                if (json.has("timestamp")) {
                    long ts = json.get("timestamp").asLong();
                    long now = Instant.now().getEpochSecond();
                    if (Math.abs(now - ts) > 300) {
                        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Timestamp out of range");
                        return;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse request body for timestamp check", e);
            }
        }

        ReReadableRequestWrapper wrapper = new ReReadableRequestWrapper(request, body.getBytes(StandardCharsets.UTF_8));
        filterChain.doFilter(wrapper, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/gateway/");
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = "{\"code\":" + status + ",\"message\":\"" + message + "\"}";
        response.getWriter().write(json);
    }

    private static class ReReadableRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final byte[] body;

        ReReadableRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return bais.read();
                }

                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
