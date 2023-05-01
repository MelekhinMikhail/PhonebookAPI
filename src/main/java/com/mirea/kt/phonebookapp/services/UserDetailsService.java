package com.mirea.kt.phonebookapp.services;

import com.mirea.kt.phonebookapp.models.User;
import com.mirea.kt.phonebookapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByLogin(login);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found!");
        }
        return new com.mirea.kt.phonebookapp.security.UserDetails(user.get());
    }
}
