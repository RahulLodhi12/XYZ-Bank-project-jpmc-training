package com.jpmc.dao;

public class InvalidChoice extends Throwable {
    public InvalidChoice(String s) {
        super(s);
    }
}
