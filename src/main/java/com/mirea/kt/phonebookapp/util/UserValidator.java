package com.mirea.kt.phonebookapp.util;

import com.mirea.kt.phonebookapp.models.User;
import com.mirea.kt.phonebookapp.services.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    private final UserDetailsService userDetailsService;

    @Autowired
    public UserValidator(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        try {
            userDetailsService.loadUserByUsername(user.getLogin());
        } catch (UsernameNotFoundException ignored) {
            return;
        }

        errors.rejectValue("login", "", "User with this login has already created");
    }
}
