package com.test.service;

import com.shdwraze.annotation.stereotype.Component;

@Component
public class UserServiceImpl implements UserService {

    @Override
    public void helloWorld() {
        System.out.println("Hello world");
    }
}
