package com.mirea.kt.phonebookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Сущность контакта")
public class ContactDTO {

    @NotEmpty
    @Size(min = 2, max = 100, message = "Name of contact should be between 2 and 100 characters")
    @Schema(description = "Имя контакта")
    private String name;

    @Schema(description = "Путь к файлу, где хранится картинка")
    private String imageName;

    @Schema(description = "Список из номеров телефонов (сущность numberDTO)")
    private List<PhoneNumberDTO> numbers;
}
