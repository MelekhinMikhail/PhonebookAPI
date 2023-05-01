package com.mirea.kt.phonebookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@Schema(description = "Сущность авторизации")
public class AuthenticationDTO {

    @NotEmpty(message = "Login can not be empty")
    @Size(min = 2, max = 100, message = "Login should be between 2 and 100 characters")
    @Schema(description = "Логин пользователя")
    private String login;

    @NotEmpty(message = "Password can not be empty")
    @Schema(description = "Пароль пользователя")
    private String password;
}
