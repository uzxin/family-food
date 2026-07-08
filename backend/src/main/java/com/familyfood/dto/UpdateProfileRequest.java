package com.familyfood.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称最长64个字符")
    private String nickname;
}
