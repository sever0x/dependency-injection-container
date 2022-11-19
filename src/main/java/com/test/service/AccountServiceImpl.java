package com.test.service;

import com.shdwraze.annotation.Autowired;
import com.shdwraze.stereotype.Component;
import com.test.model.Account;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountServiceImpl implements AccountService {

    private List<Account> accounts = new ArrayList<>();

    private UserServiceImpl userService;

    @Autowired
    public AccountServiceImpl(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public List<Account> getAllAccounts() {
        userService.helloWorld();
        return accounts;
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
