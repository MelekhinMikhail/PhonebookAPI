package com.mirea.kt.phonebookapp.repositories;

import com.mirea.kt.phonebookapp.models.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Integer> {
}
