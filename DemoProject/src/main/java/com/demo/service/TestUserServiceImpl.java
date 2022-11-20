package com.demo.service;

import com.demo.model.User;
import com.shdwraze.annotation.stereotype.Component;

@Component
public class TestUserServiceImpl implements UserService {

    @Override
    public void getFullName(User user) {
        System.out.println("Full name " + user.getName() + " " + user.getSurname());
    }
}
