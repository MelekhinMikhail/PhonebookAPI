package com.mirea.kt.phonebookapp.models;

import com.mirea.kt.phonebookapp.models.enums.NumberType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "phone_number")
@Data
@NoArgsConstructor
public class PhoneNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "number_id")
    private int id;

    @Column(name = "number")
    private String number;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NumberType numberType;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;
}
