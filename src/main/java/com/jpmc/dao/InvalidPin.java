package com.jpmc.dao;

public class InvalidPin extends Throwable {
    public InvalidPin(String s) {
        super(s);
    }
}
