package com.familyfood.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginRequest {
    @NotBlank(message = "微信code不能为空")
    private String code;

    private String nickname;
    private String avatarUrl;
}
