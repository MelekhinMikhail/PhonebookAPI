package com.mirea.kt.phonebookapp.repositories;

import com.mirea.kt.phonebookapp.models.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
}
