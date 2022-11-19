package com.test.service;

import com.shdwraze.stereotype.Component;

@Component
public class UserServiceImpl implements UserService {

    @Override
    public void helloWorld() {
        System.out.println("Hello world");
    }
}
