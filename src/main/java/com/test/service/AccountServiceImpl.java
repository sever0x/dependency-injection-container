package com.test.service;

import com.shdwraze.annotation.Autowired;
import com.shdwraze.annotation.PostConstructor;
import com.shdwraze.annotation.Qualifier;
import com.shdwraze.annotation.stereotype.Component;
import com.test.model.Account;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountServiceImpl implements AccountService {

    private List<Account> accounts = new ArrayList<>();

    private UserService userService;

    @Autowired
    public AccountServiceImpl(@Qualifier("TestUserServiceImpl") UserService userService) {
        this.userService = userService;
    }

    @Override
    @PostConstructor
    public List<Account> getAllAccounts() {
        userService.helloWorld();
        return accounts;
    }

    public void test() {
        System.out.println("It's me");
    }

    @Override
    public void createAccount(Account account) {
        accounts.add(account);
    }

    @Override
    public void removeAccount(Account account) {
        accounts.remove(account);
    }

    @Override
    public Account getAccountById(int id) {
        for (Account account : accounts) {
            if (account.getId() == id) {
                return account;
            }
        }

        return null;
    }
}
