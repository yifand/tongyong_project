package com.vdc.platform.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private Long siteId;
    private List<String> permissions;
}
