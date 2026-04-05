package com.vdc.pdi.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 更新用户请求DTO
 */
public class UpdateUserRequest {

    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Size(max = 200, message = "头像URL长度不能超过200")
    private String avatar;

    private Long deptId;

    private Integer dataScope;

    private List<Long> roleIds;

    // Getters and Setters
    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getDataScope() {
        return dataScope;
    }

    public void setDataScope(Integer dataScope) {
        this.dataScope = dataScope;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
