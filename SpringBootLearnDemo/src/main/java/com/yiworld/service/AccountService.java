package com.yiworld.service;

import com.yiworld.dao.AccountMapper;
import com.yiworld.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private AccountMapper accountMapper;

    public int add(String name, double money) {
        return accountMapper.add(name, money);
    }

    public int update(String name, double money, int id) {
        return accountMapper.update(name, money, id);
    }

    public int delete(int id) {
        return accountMapper.delete(id);
    }

    public Account findAccount(int id) {
        return accountMapper.findAccount(id);
    }

    public List<Account> findAccountList() {
        return accountMapper.findAccountList();
    }

    @Transactional  /*开启事务*/
    public void transfer() throws RuntimeException{
        this.update("测试1",1,1);//用户1减10块 用户2加10块
        int i=1/0;
        this.update("测试2",1,2);
    }
}