package com.mirea.kt.phonebookapp.controllers;

import com.mirea.kt.phonebookapp.dto.ContactDTO;
import com.mirea.kt.phonebookapp.dto.PhoneNumberDTO;
import com.mirea.kt.phonebookapp.models.Contact;
import com.mirea.kt.phonebookapp.models.PhoneNumber;
import com.mirea.kt.phonebookapp.models.User;
import com.mirea.kt.phonebookapp.security.UserDetails;
import com.mirea.kt.phonebookapp.services.ContactService;
import com.mirea.kt.phonebookapp.services.UserService;
import com.mirea.kt.phonebookapp.util.exceptions.InvalidParamsException;
import com.mirea.kt.phonebookapp.util.exceptions.UnauthorizedRequestException;
import com.mirea.kt.phonebookapp.util.exceptions.UserErrorResponse;
import com.mirea.kt.phonebookapp.util.exceptions.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contact")
@Tag(name = "Контакты", description = "CRUD-операции для работы с контактами пользователя")
public class ContactController {

    private final ContactService contactService;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public ContactController(ContactService contactService, UserService userService, ModelMapper modelMapper) {
        this.contactService = contactService;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Operation(
            summary = "Получить все контакты пользователя",
            description = "По токену определяется пользователь и возвращается JSON-map с контактами," +
                    "где ключ - айди контакта, значение - сам контакт"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно найден и возвращен JSON c контактами"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный JWT-токен"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизованный запрос (отсутствие JWT-токена в загаловках HTTP-запроса)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "JWT-токен корректный, но пользователь с таким токеном не найден " +
                            "(например, пользователь был удален)"
            )
    })
    @GetMapping
    public Map<Integer, ContactDTO> getContacts() {
        User user = authorizeUser();

        Map<Integer, ContactDTO> map = new HashMap<>();

        if (!user.getContacts().isEmpty()) {
            for (Contact contact : user.getContacts()) {
                map.put(contact.getId(), convertToContactDTO(contact));
            }
        }

        return map;
    }

    @Operation(
            summary = "Добавить контакт",
            description = "Принимает JSON-объект типа ContactDTO и добавляет его в контакты к пользователю"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контакт успешно добавлен"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Данные, которые были переданы в теле некорректны"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизованный запрос (отсутствие JWT-токена в загаловках HTTP-запроса)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "JWT-токен корректный, но пользователь с таким токеном не найден " +
                            "(например, пользователь был удален)"
            )
    })
    @PostMapping
    public ResponseEntity<HttpStatus> addContact(@RequestBody @Valid ContactDTO contactDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidParamsException();
        }

        User user = authorizeUser();

        Contact contact = convertToContact(contactDTO);
        contact.setOwner(user);
        contactService.addContact(user, contact);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Обновить контакт",
            description = "Принимает JSON-объект типа ContactDTO, ID контакта из URL и обновляет его в контактах пользователя"
    )
    @Parameters(value = {
            @Parameter(name = "id", description = "ID контакта")
    })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контакт успешно обновлен"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Данные, которые были переданы в теле некорректны"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизованный запрос (отсутствие JWT-токена в загаловках HTTP-запроса)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "У пользователя нет контакта с таким ID"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<HttpStatus> updateContact(@RequestBody @Valid ContactDTO contactDTO, BindingResult bindingResult,
                                                    @PathVariable int id) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        User user = authorizeUser();

        Contact contact = convertToContact(contactDTO);
        contact.setOwner(user);
        contact.setId(id);

        if (!contactService.updateContact(user, contact)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Удалить контакт",
            description = "Принимает ID контакта и удаляет его из контактов пользователя"
    )
    @Parameters(value = {
            @Parameter(name = "id", description = "ID контакта")
    })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контакт успешно удален"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Данные, которые были переданы некорректны"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неавторизованный запрос (отсутствие JWT-токена в загаловках HTTP-запроса)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "У пользователя нет контакта с таким ID"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteContact(@PathVariable int id) {
        User user = authorizeUser();

        if (!contactService.deleteContact(user, id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok().build();
    }



    private Contact convertToContact(ContactDTO contactDTO) {
        List<PhoneNumber> numbers = new ArrayList<>();

        if (!contactDTO.getNumbers().isEmpty()) {
            for (PhoneNumberDTO number : contactDTO.getNumbers()) {
                numbers.add(convertToPhoneNumber(number));
            }
        }

        Contact contact = modelMapper.map(contactDTO, Contact.class);
        contact.setNumbers(numbers);
        return contact;
    }

    private ContactDTO convertToContactDTO(Contact contact) {
        List<PhoneNumberDTO> numbersDTO = new ArrayList<>();

        if (!contact.getNumbers().isEmpty()) {
            for (PhoneNumber number : contact.getNumbers()) {
                numbersDTO.add(convertToPhoneNumberDTO(number));
            }
        }

        ContactDTO contactDTO = modelMapper.map(contact, ContactDTO.class);
        contactDTO.setNumbers(numbersDTO);
        return contactDTO;
    }

    private PhoneNumber convertToPhoneNumber(PhoneNumberDTO phoneNumberDTO) {
        return modelMapper.map(phoneNumberDTO, PhoneNumber.class);
    }

    private PhoneNumberDTO convertToPhoneNumberDTO(PhoneNumber phoneNumber) {
        return modelMapper.map(phoneNumber, PhoneNumberDTO.class);
    }

    private User authorizeUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();

        if (login.equals("anonymousUser")) {
            throw new UnauthorizedRequestException();
        }

        User user = userService.getUserByLogin(login);

        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(UnauthorizedRequestException e) {
        UserErrorResponse response = new UserErrorResponse(
                "Unauthorized request (use JWT-token)",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(UserNotFoundException e) {
        UserErrorResponse response = new UserErrorResponse(
                "Can not found user with such login",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handleException(InvalidParamsException e) {
        UserErrorResponse response = new UserErrorResponse(
                "Invalid params",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
