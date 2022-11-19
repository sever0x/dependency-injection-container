package com.test.service;

import com.test.model.Account;

import java.util.List;

public interface AccountService {
    List<Account> getAllAccounts();

    void createAccount(Account account);

    void removeAccount(Account account);

    Account getAccountById(int id);
}
