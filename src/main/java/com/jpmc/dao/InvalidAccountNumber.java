package com.jpmc.dao;

public class InvalidAccountNumber extends Throwable {
    public InvalidAccountNumber(String s) {
        super(s);
    }
}
