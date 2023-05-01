package com.mirea.kt.phonebookapp.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "contact")
@Data
@NoArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private int id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "contact", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PhoneNumber> numbers;

    @Column(name = "image_name")
    private String imageName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

//    {
//        "name": "Vasya123",
//            "imageName": "none",
//            "numbers": [
//        {
//            "number":"89999999",
//                "numberType": "HOME"
//        }
//    ]
//    }
}
