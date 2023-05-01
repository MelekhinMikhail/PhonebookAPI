package com.mirea.kt.phonebookapp.services;

import com.mirea.kt.phonebookapp.models.Contact;
import com.mirea.kt.phonebookapp.models.User;

import java.util.List;

public interface ContactService {

    boolean addContact(User user, Contact contact);

    boolean updateContact(User user, Contact contact);

    boolean deleteContact(User user, int id);

    List<Contact> getContacts(User user);

    Contact getContactById(int id);
}
