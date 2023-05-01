package com.mirea.kt.phonebookapp.services;

import com.mirea.kt.phonebookapp.models.User;

public interface UserService {

    void addUser(User user);

    void updateUser(User user);

    void deleteUser(User user);

    User getUserByLogin(String login);
}
