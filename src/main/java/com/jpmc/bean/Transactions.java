package com.jpmc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transactions {
    private String AccountNumber;
    private String ToAccountNumber;
    private String TransactionsType;
    private Double Amount;
    private Timestamp TransactionsDate;


}
