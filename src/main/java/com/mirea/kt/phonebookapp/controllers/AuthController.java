package com.mirea.kt.phonebookapp.controllers;

import com.mirea.kt.phonebookapp.dto.AuthenticationDTO;
import com.mirea.kt.phonebookapp.models.User;
import com.mirea.kt.phonebookapp.security.JWTUtil;
import com.mirea.kt.phonebookapp.services.UserService;
import com.mirea.kt.phonebookapp.util.UserValidator;
import com.mirea.kt.phonebookapp.util.exceptions.UserErrorResponse;
import com.mirea.kt.phonebookapp.util.exceptions.UserNotCreatedException;
import com.mirea.kt.phonebookapp.util.exceptions.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Аутентификация", description = "Авторизация/Регистрация с возможностью получить JWT-токен, " +
        "который будет действителен в течении часа")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final UserValidator userValidator;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil, ModelMapper modelMapper, UserValidator userValidator, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
        this.userService = userService;
    }

    @Operation(
            summary = "Авторизоваться",
            description = "Примнимает на вход JSON-объект с логином и паролем, в случае, если пользователь найден " +
                    "возвращает JWT-токен"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно найден и возвращен JSON c токеном"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат переданных данных"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь с таким логином и паролем не найден"
            )
    })
    @PostMapping("/login")
    public Map<String, String> performLogin(@RequestBody AuthenticationDTO authenticationDTO) {
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authenticationDTO.getLogin(), authenticationDTO.getPassword());

        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            throw new UserNotFoundException();
        }

        String token = jwtUtil.generateToken(authenticationDTO.getLogin());
        return Map.of("jwt-token", token);
    }

    @Operation(
            summary = "Зарегистрироваться",
            description = "Примнимает на вход JSON-объект с логином и паролем, " +
                    "регистрирует пользователя и возвращает JWT-токен"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно зарегистрирован и возвращен JSON c токеном"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат переданных данных или пользователь с таким логином уже есть"
            )
    })
    @PostMapping("/register")
    public Map<String, String> performRegistration(@RequestBody @Valid AuthenticationDTO authenticationDTO,
                                                   BindingResult bindingResult) {
        User user = convertToUser(authenticationDTO);

        userValidator.validate(user, bindingResult);

        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();

            List<FieldError> errors = bindingResult.getFieldErrors();
            for(FieldError error : errors) {
                errorMsg.append(error.getField())
                        .append(" - ").append(error.getDefaultMessage())
                        .append(";");
            }

            throw new UserNotCreatedException(errorMsg.toString());
        }

        userService.addUser(user);

        String token = jwtUtil.generateToken(user.getLogin());
        return Map.of("jwt-token", token);
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(UserNotCreatedException e) {
        UserErrorResponse response = new UserErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(UserNotFoundException e) {
        UserErrorResponse response = new UserErrorResponse(
                "Incorrect login or password",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    private User convertToUser(AuthenticationDTO authenticationDTO) {
        return modelMapper.map(authenticationDTO, User.class);
    }
}
