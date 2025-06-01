package com.jpmc.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private String AccountNumber;
    private String CustomerName;
    private String Branch;
    private Double Balance;

    public Customer(String accNo) {
        this.AccountNumber = accNo;
    }

    public Customer(String accNo1, String accNo2) {
    }
}
