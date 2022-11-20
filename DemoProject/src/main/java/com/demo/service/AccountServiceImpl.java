package com.demo.service;

import com.demo.model.Account;
import com.demo.model.User;
import com.shdwraze.annotation.Autowired;
import com.shdwraze.annotation.PostConstructor;
import com.shdwraze.annotation.Qualifier;
import com.shdwraze.annotation.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountServiceImpl implements AccountService {

    private List<Account> accounts = new ArrayList<>();

    private UserService userService;

    @Autowired
    public AccountServiceImpl(@Qualifier("UserServiceImpl") UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<Account> getAllAccounts() {
        return accounts;
    }

    @PostConstructor
    public void demo() {
        User user = new User("Giovanni", "Giorgio");
        Account account = new Account(1, "giorgio", user);

        createAccount(account);
        List<Account> accounts = getAllAccounts();

        for (Account acc : accounts) {
            System.out.println(acc.toString());
        }

        Account accountById = getAccountById(1);
        System.out.println("Login: " + accountById.getLogin());
        userService.getFullName(user);

        removeAccount(accountById);
        System.out.println("All accounts have been deleted? " + getAllAccounts().isEmpty());
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