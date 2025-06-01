package com.jpmc;

import com.jpmc.dao.DaoClass;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        DaoClass daoClass = new DaoClass();
        daoClass.connectToDB();
    }
}