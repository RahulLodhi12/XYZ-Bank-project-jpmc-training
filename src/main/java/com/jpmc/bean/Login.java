package com.jpmc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {
    private String AccountNumber;
    private String Pin;
    private Double Balance;


    public Login(String accNo, String pin) {
        this.AccountNumber = accNo;
        this.Pin = pin;
    }
}
