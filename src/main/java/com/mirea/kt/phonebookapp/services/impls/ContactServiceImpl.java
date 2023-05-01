package com.mirea.kt.phonebookapp.services.impls;

import com.mirea.kt.phonebookapp.models.Contact;
import com.mirea.kt.phonebookapp.models.PhoneNumber;
import com.mirea.kt.phonebookapp.models.User;
import com.mirea.kt.phonebookapp.repositories.ContactRepository;
import com.mirea.kt.phonebookapp.repositories.UserRepository;
import com.mirea.kt.phonebookapp.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    @Autowired
    public ContactServiceImpl(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public boolean addContact(User user, Contact contact) {
        if (contact == null || user == null) {
            return false;
        }

        if (user.getContacts().contains(contact)) {
            return false;
        }

        if (!contact.getNumbers().isEmpty()) {
            for (PhoneNumber number : contact.getNumbers()) {
                number.setContact(contact);
            }
        }

        user.getContacts().add(contact);
        userRepository.save(user);
        return true;
    }

    @Transactional
    @Override
    public boolean updateContact(User user, Contact contact) {
        if (contact == null || user == null) {
            return false;
        }

        long count = user.getContacts().stream().filter(x -> x.getId() == contact.getId()).count();

        if (count == 0) {
            return false;
        }

        user.getContacts().removeIf(x -> x.getId() == contact.getId());
        user.getContacts().add(contact);
        userRepository.save(user);
        return true;
    }

    @Transactional
    @Override
    public boolean deleteContact(User user, int id) {
        if (user == null) {
            return false;
        }

        if (!user.getContacts().removeIf(x -> x.getId() == id)) {
            return false;
        }

        contactRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Contact> getContacts(User user) {
        return user.getContacts();
    }

    @Override
    public Contact getContactById(int id) {
        return contactRepository.findById(id).orElse(null);
    }
}
