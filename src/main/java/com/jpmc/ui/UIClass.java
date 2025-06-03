package com.jpmc.ui;

import com.jpmc.bean.Customer;
import com.jpmc.bean.Login;
import com.jpmc.dao.*;
import com.jpmc.service.ServiceClass;

import javax.naming.InvalidNameException;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class UIClass {
    ServiceClass serviceClass = new ServiceClass();

    public void bankOperations() throws SQLException {
        Scanner sc = new Scanner(System.in);

        System.out.println("1. Create Account");
        System.out.println("2. Login into your Account");

        int pick = sc.nextInt();

        if (pick == 1) {
            //createAccount() method
            System.out.println("Enter CustomerName, Branch, Balance: To Create Bank Account");

            //Generating Unique Random Account Number every-time.
            long timestamp = System.currentTimeMillis(); // 13-digit positive number
            int randomNum = 10000 + new Random().nextInt(90000); // 5-digit positive number
            String accNo = timestamp + "" + randomNum; // 13 + 5 = 18-digit positive number (as String)
            sc.nextLine();

            String name;
            while (true) {
                try {
                    System.out.println("Enter name:");
                    name = sc.nextLine();
                    if (name == null || name.length() == 0) {
                        throw new InvalidNameException("Name cannot be empty..");
                    }
                    if (!name.matches("[a-zA-Z ]+")) {
                        throw new InvalidNameException("Name must contain only alphabetic characters.");
                    }
                    break;
                } catch (InputMismatchException | InvalidNameException e) {
                    System.out.println(e.getMessage());
                }
            }

            String branch;
            while (true) {
                try {
                    System.out.println("Enter Branch:");
                    branch = sc.nextLine();
                    if (branch == null || branch.length() == 0) {
                        throw new InvalidBranchException("Branch cannot be empty..");
                    }
                    if (!branch.matches("[a-zA-Z ]+")) {
                        throw new InvalidBranchException("Name must contain only alphabetic characters.");
                    }
                    break;
                } catch (InputMismatchException | InvalidBranchException e) {
                    System.out.println(e.getMessage());
                }
            }

            double balance;
            while (true) {
                try {
                    System.out.println("Enter Balance:");
                    balance = sc.nextDouble();
                    if (balance < 1000) {
                        throw new InvalidBalanceException("Balance cannot be below Rs.1000 !");
                    }
                    break;
                } catch (InputMismatchException | InvalidBalanceException e) {
                    System.out.println(e);
                    sc.nextLine();
                }
            }

            if(serviceClass.createAccount(new Customer(accNo, name, branch, balance))){
                pick=2;
            } else{
                System.out.println("Process Failed..");
            }
        }

        if (pick == 2) {
            //customerLogin
            System.out.println("Login:");
            System.out.println("Enter your AccountNumber and Pin to Login: ");
            sc.nextLine();
            String accNo;
            while(true){
                System.out.println("Enter Account Number: ");
                try{
                    accNo = sc.nextLine();
                    if(!accNo.matches("[0-9]+")){
                        throw new InvalidAccountNumber("Invalid Account Number..");
                    }
                    break;
                }catch (InputMismatchException | InvalidAccountNumber e){
                    System.out.println(e.getMessage());
                }
            }

            String pin;
            while(true){
                try{
                    System.out.println("Enter Pin: ");
                    pin = sc.nextLine();
                    if(!pin.matches("[0-9]+")){
                        throw new InvalidPin("Pin Must be Numeric..");
                    }
                    break;
                } catch (InputMismatchException | InvalidPin e){
                    System.out.println(e.getMessage());
                }
            }

            if(serviceClass.customerLogin(new Login(accNo, pin))){
                System.out.println("Login Successfully..");

                while (true) {
                    System.out.println("1. Deposit");
                    System.out.println("2. Withdraw");
                    System.out.println("3. Fund Transfer");
                    System.out.println("4. Print Transactions");
                    System.out.println("5. Exit");

                    int choice;
                    while (true) {
                        try {
                            System.out.println("Pick your choice[1-5]: ");
                            choice = sc.nextInt();
                            if(choice>=6 || choice<1){
                                throw new InvalidChoice("Choice Must be b/w [1-5]..");
                            }
                            break;
                        } catch (InputMismatchException | InvalidChoice e) {
                            System.out.println(e.getMessage());
                        }
                    }

                    if (choice == 1) {
                        //deposit
                        Double amt;
                        while (true) {
                            try {
                                System.out.println("Enter Amount you want to Deposit: ");
                                amt = sc.nextDouble();
                                if (amt < 100) {
                                    throw new MinimumDeposit("Minimum amount to Deposit is Rs.100..");
                                }
                                break;
                            } catch (InputMismatchException | MinimumDeposit e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        serviceClass.deposit(new Customer(accNo), amt);
                    } else if (choice == 2) {
                        //withdraw
                        Double amt;
                        while (true) {
                            try {
                                System.out.println("Enter Amount you want to Withdraw: ");
                                amt = sc.nextDouble();
                                if (amt > 25000) {
                                    throw new MaximumWithdraw("Maximum Withdraw Amount in a single day is Rs. 25000!!");
                                }
                                break;
                            } catch (InputMismatchException | MaximumWithdraw e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        serviceClass.withdraw(new Customer(accNo), amt);
                    } else if (choice == 3) {
                        //fund transfer
                        Double amt;
                        while (true) {
                            try {
                                System.out.println("Enter Amount you want to Transfer: ");
                                amt = sc.nextDouble();
                                sc.nextLine();
                                if (amt > 25000) {
                                    throw new MaximumWithdraw("Maximum Fund Transfer Amount in a single day is Rs. 25000!!");
                                }
                                break;
                            } catch (InputMismatchException | MaximumWithdraw e) {
                                System.out.println(e.getMessage());
                            }
                        }

                        String accNo2;
                        while (true) {
                            try {
                                System.out.println("Enter the AccountNumber To which you want to Send Money: ");
                                accNo2 = sc.nextLine();
                                if (!accNo2.matches("[0-9]+")) {
                                    throw new InvalidAccountNumber("Invalid Account Number");
                                }
                                break;
                            } catch (InputMismatchException | InvalidAccountNumber e) {
                                System.out.println(e.getMessage());
                            }
                        }

                        serviceClass.fundTransfer(new Customer(accNo), new Customer(accNo2), amt);
                    } else if (choice == 4) {
                        //print transactions
                        System.out.println("Transaction History: ");
                        serviceClass.printTransactions(new Customer(accNo));
                    } else if (choice == 5) {
                        System.out.println("Exit..");
                        return;
                    }

                }
            }

        }
    }
}
