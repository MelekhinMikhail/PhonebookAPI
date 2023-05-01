package com.mirea.kt.phonebookapp.dto;

import com.mirea.kt.phonebookapp.models.enums.NumberType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@Schema(description = "Сущность номера телефона")
public class PhoneNumberDTO {

    @NotEmpty
    @Size(min = 11, max = 20, message = "Number should be between 11 and 20 characters")
    @Schema(description = "Номер телефона")
    private String number;

    @Schema(description = "Тип номера: мобильный, домашний, рабочий, не указан (CELLULAR, HOME, WORKER, NONE соответственно)")
    private NumberType numberType;
}
