package com.test.service;

import com.shdwraze.annotation.stereotype.Component;

@Component
public class TestUserServiceImpl implements UserService {
    @Override
    public void helloWorld() {
        System.out.println("Bye-bye");
    }
}
