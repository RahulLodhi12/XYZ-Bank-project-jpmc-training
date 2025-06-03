package com.jpmc.dao;

public class InvalidBalanceException extends Exception {
    public InvalidBalanceException(String s) {
        super(s);
    }
}
