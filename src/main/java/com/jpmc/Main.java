package com.jpmc;

import com.jpmc.dao.DaoClass;
import com.jpmc.ui.UIClass;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        System.out.println("Hello world!");

        UIClass uiClass = new UIClass();
        uiClass.bankOperations();
    }
}