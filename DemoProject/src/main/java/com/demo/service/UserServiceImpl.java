package com.demo.service;

import com.demo.model.User;
import com.shdwraze.annotation.stereotype.Component;

@Component
public class UserServiceImpl implements UserService {

    @Override
    public void getFullName(User user) {
        System.out.println("My name is " + user.getName() + " " + user.getSurname());
    }
}