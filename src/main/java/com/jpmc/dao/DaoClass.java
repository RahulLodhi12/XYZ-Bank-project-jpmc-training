package com.jpmc.dao;

import com.jpmc.bean.Customer;
import com.jpmc.bean.Login;
import com.jpmc.bean.Transactions;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class DaoClass {
    public static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String URL_TO_CONNECT = "jdbc:mysql://localhost:3306/jpmc";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "#RoopRaj89";

    public void connectToDB(){
        Scanner sc = new Scanner(System.in);
        try {
            //1. Load the Driver
            Class.forName(DRIVER_NAME);

            //2. Connect to the Database
            Connection connection = DriverManager.getConnection(URL_TO_CONNECT,DB_USERNAME,DB_PASSWORD);

            //3. Prepared Statement - inside these "methods"
            //createAccount() method
            System.out.println("Enter CustomerName, Branch, Balance: To Create Bank Account");

            //Generating Unique Random Account Number every-time.
            long timestamp = System.currentTimeMillis(); // 13-digit positive number
            int randomNum = 10000 + new Random().nextInt(90000); // 5-digit positive number
            String accNo = timestamp + "" + randomNum; // 13 + 5 = 18-digit positive number (as String)
            String name = sc.nextLine();
            String branch = sc.nextLine();
            Double balance = sc.nextDouble();

            createAccount(connection,new Customer(accNo,name,branch,balance));

            //4. Close connection
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void customerLogin(Connection connection, Login login) throws SQLException {
        Scanner sc = new Scanner(System.in);

        //Write a Query
        String query = "select * from login where AccountNumber=?";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1,login.getAccountNumber());

        //Execute the Query
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            System.out.println("Login Successfully..");

            while (true){
                System.out.println("1. Deposit");
                System.out.println("2. Withdraw");
                System.out.println("3. Fund Transfer");
                System.out.println("4. Print Transactions");
                System.out.println("5. Exit");
                System.out.println("Pick your choice[1-5]: ");
                int choice = sc.nextInt();

                if(choice==1){
                    //deposit
                    System.out.println("Enter Amount you want to Deposit: ");
                    Double amt = sc.nextDouble();
                    deposit(connection,new Customer(login.getAccountNumber()),amt);
                }
                else if(choice==2){
                    //withdraw
                    System.out.println("Enter Amount you want to Withdraw: ");
                    Double amt = sc.nextDouble();
                    withdraw(connection,new Customer(login.getAccountNumber()),amt);
                }
                else if(choice==3){
                    //fund transfer
                    System.out.println("Enter Amount you want to Transfer: ");
                    Double amt = sc.nextDouble(); sc.nextLine();
                    System.out.println("Enter the AccountNumber To which you want to Send Money: ");
                    String accNo2 = sc.nextLine();
                    fundTransfer(connection,new Customer(login.getAccountNumber()), new Customer(accNo2), amt);
                }
                else if(choice==4){
                    //print transactions
                    System.out.println("Transaction History: ");
                    printTransactions(connection,new Customer(login.getAccountNumber()));
                }
                else if(choice==5){
                    System.out.println("Exit..");
                    return;
                }

            }
        }
        else{
            System.out.println("Login Failed..");
        }

    }


    void createAccount(Connection connection, Customer customer) throws SQLException {
        Scanner sc = new Scanner(System.in);

        //Write a Query
        String query = "insert into customer(AccountNumber,CustomerName,Branch,Balance) values(?,?,?,?)";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1,customer.getAccountNumber());
        preparedStatement.setString(2,customer.getCustomerName());
        preparedStatement.setString(3,customer.getBranch());
        preparedStatement.setDouble(4,customer.getBalance());

        if(isLowBalance(customer.getBalance())){
            System.out.println("Minimum Balance should be Rs. 1000 to Create a Account..");
            return;
        }

        //Execute the Query
        if(preparedStatement.executeUpdate()>0){
            System.out.println("Customer Account Created Successfully..");
            generateLoginCredentials(connection, customer);
            System.out.println("Now Login..");
            System.out.println("Enter your AccountNumber and Pin to Login: ");
            String accNo = sc.nextLine();
            String pin = sc.nextLine();
            customerLogin(connection, new Login(accNo,pin));
        }
        else{
            System.out.println("Process Failed..");
        }

    }

    void generateLoginCredentials(Connection connection, Customer customer) throws SQLException {
        //Write a Query
        String query = "insert into login(AccountNumber,Pin,Balance) values(?,?,?)";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1,customer.getAccountNumber());

        //generating login pin - 6 digits
        Random random = new Random();
        String pin = String.valueOf(100000 + random.nextInt(900000)); // Range: 100000 to 999999

        preparedStatement.setString(2,pin);
        preparedStatement.setDouble(3,customer.getBalance());

        //Execute the Query
        if(preparedStatement.executeUpdate()>0){
            System.out.println("Login Credentials Generated Successfully..");
            System.out.println("Your Username[AccountNumber]: " + customer.getAccountNumber());
            System.out.println("Your Pin: " + pin);
        }
        else{
            System.out.println("Process Failed..");
        }
    }

    void deposit(Connection connection, Customer customer, Double amt) throws SQLException {
        //Write a Query
        String query1 = "select Balance from customer where AccountNumber=?";
        String query2 = "update customer set Balance=? where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement1
        PreparedStatement preparedStatement1 = connection.prepareStatement(query1); //select

        //Put the values of Placeholders - preparedStatement1
        preparedStatement1.setString(1,customer.getAccountNumber());

        //Execute the Query - preparedStatement1
        ResultSet resultSet = preparedStatement1.executeQuery();
        double balance;
        if(resultSet.next()){
            balance = resultSet.getDouble("Balance");
        }
        else{
            System.out.println("Process Failed..");
            return;
        }


        //Get a reference to the PreparedStatement Object - preparedStatement2
        PreparedStatement preparedStatement2 = connection.prepareStatement(query2); //update

        //Put the values of Placeholders - preparedStatement2
        preparedStatement2.setDouble(1,balance+amt);
        preparedStatement2.setString(2,customer.getAccountNumber());

        //Execute the Query - preparedStatement2
        if(preparedStatement2.executeUpdate()>0){
            updateTransactions(connection,customer,customer,"deposit",amt);
            System.out.println(amt+ " Amount Deposit and DataBase Updated..");
        }
        else{
            System.out.println("Process Failed..");
            return;
        }
    }

    void withdraw(Connection connection, Customer customer, Double amt) throws SQLException {
        //Write a Query
        String query1 = "select Balance from customer where AccountNumber=?";
        String query2 = "update customer set Balance=? where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement1
        PreparedStatement preparedStatement1 = connection.prepareStatement(query1); //select

        //Put the values of Placeholders - preparedStatement1
        preparedStatement1.setString(1,customer.getAccountNumber());

        //Execute the Query - preparedStatement1
        ResultSet resultSet = preparedStatement1.executeQuery();
        double balance;
        if(resultSet.next()){
            balance = resultSet.getDouble("Balance");
            if(isLowBalance(balance-amt)){
                System.out.println("Process Failed..");
                System.out.println("Minimum Balance should be Rs. 1000 after the Withdraw..");
                return;
            }
        }
        else{
            System.out.println("Process Failed..");
            return;
        }

        //Get a reference to the PreparedStatement Object - preparedStatement2
        PreparedStatement preparedStatement2 = connection.prepareStatement(query2); //update

        //Put the values of Placeholders - preparedStatement2
        preparedStatement2.setDouble(1,balance-amt);
        preparedStatement2.setString(2,customer.getAccountNumber());

        //Execute the Query - preparedStatement2
        if(preparedStatement2.executeUpdate()>0){
            updateTransactions(connection,customer,customer,"withdraw",amt);
            System.out.println(amt+ " Amount Withdraw and DataBase Updated..");
        }
        else{
            System.out.println("Process Failed..");
            return;
        }
    }

    void fundTransfer(Connection connection, Customer customer1, Customer customer2, Double amt) throws SQLException {
        //Write a Query
        String query1 = "select Balance from customer where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement1
        PreparedStatement preparedStatement1 = connection.prepareStatement(query1); //select

        //Put the values of Placeholders - preparedStatement1
        preparedStatement1.setString(1,customer1.getAccountNumber());

        //Execute the Query - preparedStatement1
        ResultSet resultSet1 = preparedStatement1.executeQuery();
        double balance1;
        if(resultSet1.next()){
            balance1 = resultSet1.getDouble("Balance");
            if(isLowBalance(balance1-amt)){
                System.out.println("Process Failed..");
                System.out.println("Minimum Balance should be Rs. 1000 after the Amount Debited..");
                return;
            }
        }
        else{
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
        preparedStatement11.setDouble(1,balance1-amt);
        preparedStatement11.setString(2,customer1.getAccountNumber());

        //Execute the Query - preparedStatement2
        if(preparedStatement11.executeUpdate()>0){
            System.out.println(amt+ " Amount Debited and DataBase Updated..");
        }
        else{
            System.out.println("Process Failed..");
            return;
        }


        //Write a Query
        String query2 = "select Balance from customer where AccountNumber=?";

        //Get a reference to the PreparedStatement Object - preparedStatement2
        PreparedStatement preparedStatement2 = connection.prepareStatement(query2); //select

        //Put the values of Placeholders - preparedStatement2
        preparedStatement2.setString(1,customer2.getAccountNumber());

        //Execute the Query - preparedStatement2
        ResultSet resultSet2 = preparedStatement2.executeQuery();
        double balance2;
        if(resultSet2.next()){
            balance2 = resultSet2.getDouble("Balance");
        }
        else{
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
        preparedStatement22.setDouble(1,balance2+amt);
        preparedStatement22.setString(2,customer2.getAccountNumber());

        //Execute the Query - preparedStatement2
        if(preparedStatement22.executeUpdate()>0){
            System.out.println(amt+ " Amount Credited and DataBase Updated..");
        }
        else{
            System.out.println("Process Failed..");
            return;
        }

        updateTransactions(connection,customer1,customer2,"transfer",amt);
        System.out.println("Fund Transfer Successfully and DataBase Updated..");
    }

    void updateTransactions(Connection connection, Customer customer1, Customer customer2, String transactionsType , Double amt) throws SQLException {
        //Write a Query
        String query = "insert into transactions(AccountNumber,ToAccountNumber,TransactionsType,Amount) values(?,?,?,?)";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1,customer1.getAccountNumber());
        preparedStatement.setString(2,customer2.getAccountNumber());
        preparedStatement.setString(3,transactionsType);
        preparedStatement.setDouble(4,amt);

        //Execute the Query
        if(preparedStatement.executeUpdate()>0){
            System.out.println("Transactions Table Updated Successfully..");
        }
        else{
            System.out.println("Process Failed..");
        }
    }

    void printTransactions(Connection connection, Customer customer) throws SQLException {
        //Write a Query
        String query = "select * from transactions where AccountNumber=?";

        //Get a reference to the PreparedStatement object
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        //Put the value of Placeholders
        preparedStatement.setString(1,customer.getAccountNumber());

        //Execute the Query
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            System.out.print("From Account Number: "+resultSet.getString("AccountNumber"));
            System.out.print(" Rs. "+resultSet.getString("Amount") + " Send");
            System.out.print(" To Account Number: "+resultSet.getString("ToAccountNumber"));
            System.out.print(" Transaction Type: "+resultSet.getString("TransactionsType"));
            System.out.println(" Transaction Date: "+resultSet.getString("TransactionsDate"));
        }
    }

    boolean isLowBalance(double balance){
        if(balance<1000) return true;
        return false;
    }

}
