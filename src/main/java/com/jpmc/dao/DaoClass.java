package com.jpmc.dao;

import com.jpmc.bean.Customer;
import com.jpmc.bean.Login;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class DaoClass {

    Connection connection = new ConnectToDB().connectToDB();

    public boolean customerLogin(Login login) throws SQLException {
        Scanner sc = new Scanner(System.in);

        //Write a Query
        String query = "select * from login where AccountNumber=?";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1, login.getAccountNumber());

        //Execute the Query
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return true;
        } else {
            return false;
        }

    }


    public boolean createAccount(Customer customer) throws SQLException {
        Scanner sc = new Scanner(System.in);

        //Write a Query
        String query = "insert into customer(AccountNumber,CustomerName,Branch,Balance) values(?,?,?,?)";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1, customer.getAccountNumber());
        preparedStatement.setString(2, customer.getCustomerName());
        preparedStatement.setString(3, customer.getBranch());
        preparedStatement.setDouble(4, customer.getBalance());

        if (isLowBalance(customer.getBalance())) {
            System.out.println("Minimum Balance should be Rs. 1000 to Create a Account..");
            return false;
        }

        //Execute the Query
        if (preparedStatement.executeUpdate() > 0) {
            System.out.println("Customer Account Created Successfully..");
            generateLoginCredentials(connection, customer);
            return true;
//            System.out.println("Now Login..");
//            System.out.println("Enter your AccountNumber and Pin to Login: ");
//            String accNo = sc.nextLine();
//            String pin = sc.nextLine();
//            customerLogin(connection, new Login(accNo, pin));
        } else {
            return false;
        }

    }

    public void generateLoginCredentials(Connection connection, Customer customer) throws SQLException {
        //Write a Query
        String query = "insert into login(AccountNumber,Pin,Balance) values(?,?,?)";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1, customer.getAccountNumber());

        //generating login pin - 6 digits
        Random random = new Random();
        String pin = String.valueOf(100000 + random.nextInt(900000)); // Range: 100000 to 999999

        preparedStatement.setString(2, pin);
        preparedStatement.setDouble(3, customer.getBalance());

        //Execute the Query
        if (preparedStatement.executeUpdate() > 0) {
            System.out.println("Login Credentials Generated Successfully..");
            System.out.println("Your Username[AccountNumber]: " + customer.getAccountNumber());
            System.out.println("Your Pin: " + pin);
        } else {
            System.out.println("Process Failed..");
        }
    }

    public void deposit(Customer customer, Double amt) throws SQLException {
        //Write a Query
        String query1 = "select Balance from customer where AccountNumber=?";
        String query2 = "update customer set Balance=? where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement1
        PreparedStatement preparedStatement1 = connection.prepareStatement(query1); //select

        //Put the values of Placeholders - preparedStatement1
        preparedStatement1.setString(1, customer.getAccountNumber());

        //Execute the Query - preparedStatement1
        ResultSet resultSet = preparedStatement1.executeQuery();
        double balance;
        if (resultSet.next()) {
            balance = resultSet.getDouble("Balance");
        } else {
            System.out.println("Process Failed..");
            return;
        }


        //Get a reference to the PreparedStatement Object - preparedStatement2
        PreparedStatement preparedStatement2 = connection.prepareStatement(query2); //update

        //Put the values of Placeholders - preparedStatement2
        preparedStatement2.setDouble(1, balance + amt);
        preparedStatement2.setString(2, customer.getAccountNumber());

        //Execute the Query - preparedStatement2
        if (preparedStatement2.executeUpdate() > 0) {
            updateTransactions(connection, customer, customer, "deposit", amt);
            System.out.println(amt + " Amount Deposit and DataBase Updated..");
        } else {
            System.out.println("Process Failed..");
            return;
        }
    }

    public void withdraw(Customer customer, Double amt) throws SQLException {
        //Write a Query
        String query1 = "select Balance from customer where AccountNumber=?";
        String query2 = "update customer set Balance=? where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement1
        PreparedStatement preparedStatement1 = connection.prepareStatement(query1); //select

        //Put the values of Placeholders - preparedStatement1
        preparedStatement1.setString(1, customer.getAccountNumber());

        //Execute the Query - preparedStatement1
        ResultSet resultSet = preparedStatement1.executeQuery();
        double balance;
        if (resultSet.next()) {
            balance = resultSet.getDouble("Balance");
            if (isLowBalance(balance - amt)) {
                System.out.println("Process Failed..");
                System.out.println("Minimum Balance should be Rs. 1000 after the Withdraw..");
                return;
            }
        } else {
            System.out.println("Process Failed..");
            return;
        }

        //Get a reference to the PreparedStatement Object - preparedStatement2
        PreparedStatement preparedStatement2 = connection.prepareStatement(query2); //update

        //Put the values of Placeholders - preparedStatement2
        preparedStatement2.setDouble(1, balance - amt);
        preparedStatement2.setString(2, customer.getAccountNumber());

        //Execute the Query - preparedStatement2
        if (preparedStatement2.executeUpdate() > 0) {
            updateTransactions(connection, customer, customer, "withdraw", amt);
            System.out.println(amt + " Amount Withdraw and DataBase Updated..");
        } else {
            System.out.println("Process Failed..");
            return;
        }
    }

    public void fundTransfer(Customer customer1, Customer customer2, Double amt) throws SQLException {
        //Write a Query
        String query1 = "select Balance from customer where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement1
        PreparedStatement preparedStatement1 = connection.prepareStatement(query1); //select

        //Put the values of Placeholders - preparedStatement1
        preparedStatement1.setString(1, customer1.getAccountNumber());

        //Execute the Query - preparedStatement1
        ResultSet resultSet1 = preparedStatement1.executeQuery();
        double balance1;
        if (resultSet1.next()) {
            balance1 = resultSet1.getDouble("Balance");
            if (isLowBalance(balance1 - amt)) {
                System.out.println("Process Failed..");
                System.out.println("Minimum Balance should be Rs. 1000 after the Amount Debited..");
                return;
            }
        } else {
            System.out.println("Process Failed..");
            return;
        }

//        withdraw(connection,customer1,amt); //update
        //Updating Debited of customer1
        //Write a Query
        String query11 = "update customer set Balance=? where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement11
        PreparedStatement preparedStatement11 = connection.prepareStatement(query11); //update

        //Put the values of Placeholders - preparedStatement11
        preparedStatement11.setDouble(1, balance1 - amt);
        preparedStatement11.setString(2, customer1.getAccountNumber());

        //Execute the Query - preparedStatement2
        if (preparedStatement11.executeUpdate() > 0) {
            System.out.println(amt + " Amount Debited and DataBase Updated..");
        } else {
            System.out.println("Process Failed..");
            return;
        }


        //Write a Query
        String query2 = "select Balance from customer where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement2
        PreparedStatement preparedStatement2 = connection.prepareStatement(query2); //select

        //Put the values of Placeholders - preparedStatement2
        preparedStatement2.setString(1, customer2.getAccountNumber());

        //Execute the Query - preparedStatement2
        ResultSet resultSet2 = preparedStatement2.executeQuery();
        double balance2;
        if (resultSet2.next()) {
            balance2 = resultSet2.getDouble("Balance");
        } else {
            System.out.println("Process Failed..");
            return;
        }

//        deposit(connection,customer2,amt); //update
        //Updating Credited of customer2
        //Write a Query
        String query22 = "update customer set Balance=? where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement11
        PreparedStatement preparedStatement22 = connection.prepareStatement(query22); //update

        //Put the values of Placeholders - preparedStatement11
        preparedStatement22.setDouble(1, balance2 + amt);
        preparedStatement22.setString(2, customer2.getAccountNumber());

        //Execute the Query - preparedStatement2
        if (preparedStatement22.executeUpdate() > 0) {
            System.out.println(amt + " Amount Credited and DataBase Updated..");
        } else {
            System.out.println("Process Failed..");
            return;
        }

        updateTransactions(connection, customer1, customer2, "transfer", amt);
        System.out.println("Fund Transfer Successfully and DataBase Updated..");
    }

    public void updateTransactions(Connection connection, Customer customer1, Customer customer2, String transactionsType, Double amt) throws SQLException {
        //Write a Query
        String query = "insert into transactions(AccountNumber,ToAccountNumber,TransactionsType,Amount) values(?,?,?,?)";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1, customer1.getAccountNumber());
        preparedStatement.setString(2, customer2.getAccountNumber());
        preparedStatement.setString(3, transactionsType);
        preparedStatement.setDouble(4, amt);

        //Execute the Query
        if (preparedStatement.executeUpdate() > 0) {
            System.out.println("Transactions Table Updated Successfully..");
        } else {
            System.out.println("Process Failed..");
        }
    }

    public void printTransactions(Customer customer) throws SQLException {
        //Write a Query
        String query = "select * from transactions where AccountNumber=?";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1, customer.getAccountNumber());

        //Execute the Query
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.print("From Account Number: " + resultSet.getString("AccountNumber"));
            System.out.print(" Rs. " + resultSet.getString("Amount") + " Send");
            System.out.print(" To Account Number: " + resultSet.getString("ToAccountNumber"));
            System.out.print(" Transaction Type: " + resultSet.getString("TransactionsType"));
            System.out.println(" Transaction Date: " + resultSet.getString("TransactionsDate"));
        }
    }

    public boolean isLowBalance(double balance) {
        if (balance < 1000) return true;
        return false;
    }

}
