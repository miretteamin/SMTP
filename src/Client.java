// Project 105
// Rana Ihab Ahmed
// Alaa Mahmoud Ibrahim
// Kareem Mohamed Morsy
// Mirette Amin Danial
// Mariam Ihab Mohamed

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    static public boolean isOn = true;
    static Scanner input = new Scanner(System.in);
    static Socket server;
    static DataOutputStream serverWrite;
    static DataInputStream serverRead;
    static String user;

    public static void main(String[] args) {
        try {

            InetAddress ip = InetAddress.getLocalHost();    //To get the Ip address
            System.out.println("Please enter server port number."); //Ask user to enter port number of server
            int port = input.nextInt();
            // To clear remaining string in the line
            input.nextLine();
            server = new Socket(ip, port);  //Create socket with the server's port number and IP address

            serverWrite = new DataOutputStream(server.getOutputStream());
            serverRead = new DataInputStream(server.getInputStream());
            String message = serverRead.readUTF();  //read message from the server
            System.out.println(message);    //output server's message to user

            while (true) {
                System.out.println("Please choose ‘REGISTER’ or ‘LOGIN’ or ‘QUIT’.");
                String inputMessage = input.nextLine(); //Input the user's choice
                if (inputMessage.equalsIgnoreCase("QUIT")) {   //If user chooses to quit
                    serverWrite.writeUTF(inputMessage); //send user's choice to the server
                    message = serverRead.readUTF(); //get message from the server
                    System.out.println(message);    //print the server's message to user
                    serverWrite.close();
                    serverRead.close();
                    server.close();
                    break;
                }
                //If user chooses to register or login
                else if (inputMessage.equalsIgnoreCase("REGISTER") || inputMessage.equalsIgnoreCase("LOGIN")) {
                    System.out.println("Please enter an email and a password.");    //Ask the user to enter email and password
                    serverWrite.writeUTF(inputMessage); //send user's choice to the server
                    user = input.nextLine();    //Take email from the user
                    serverWrite.writeUTF(user); //send email to server
                    inputMessage = input.nextLine();    //Take password from the user
                    serverWrite.writeUTF(inputMessage); //send password to server

                    message = serverRead.readUTF(); //Take message from the server to know if the email is incorrect or taken
                    if (message.startsWith("250")) {
                        System.out.println("HELLO " + user);
                        serverWrite.writeUTF("HELLO " + user);  //Send this to the server
                        message = serverRead.readUTF(); //read message from server
                        System.out.println(message);    //output this message to the user
                        Send(); //Call send function
                        break;
                    }

                    System.out.println(message);    //output the server's message to user
                }
            }
        } catch (IOException e) {
            System.out.println("You have been disconnected!!");
            Client.isOn = false;
        }
    }

    public static void Send() throws IOException { // used to send message or quit

        while (true) {
            String Mess;
            System.out.println("Please choose ‘SEND’ or ‘QUIT’.");  //Asks the user to choose SEND or QUIT
            Mess = input.nextLine();    //Enter the choice
            if (Mess.equalsIgnoreCase("SEND")) {    //If the user chooses SEND
                serverWrite.writeUTF("MAIL FROM " + user);  // Send this to the server
                System.out.println("MAIL FROM " + user);
                Mess = serverRead.readUTF();    //read message from server
                System.out.println("Server: " + Mess);  //output this message to the user
                serverWrite.writeUTF("RCPT TO");    //Send (RCPT TO) to the server
                System.out.println("RCPT TO");
                Mess = input.nextLine();    //enter email
                serverWrite.writeUTF(Mess); //send email to the server
                Mess = serverRead.readUTF();    //read message from server
                if(Mess.startsWith("550")){ // if invalid recipient email address
                    System.out.println("Server: " + Mess);
                    continue;
                }
                System.out.println("Server: " + Mess);  //output this message to the user
                serverWrite.writeUTF("DATA");   //Send (DATA) to the server
                System.out.println("DATA");
                Mess = serverRead.readUTF();    //read message from server
                System.out.println("Server: " + Mess);  //output this message to the user
                while (true) {  //While the message does not equal &&&
                    Mess = input.nextLine();    //Input messages from user
                    serverWrite.writeUTF(Mess); //send this messages to the server
                    if (Mess.equalsIgnoreCase("&&&"))
                        break;
                }
                Mess = serverRead.readUTF();    //read message from the server
                System.out.println("Server: " + Mess);  //output this message to the user
            }
            else if (Mess.equalsIgnoreCase("QUIT")) { //If the user chooses QUIT
                serverWrite.writeUTF(Mess); //send user's choice to the server
                Mess = serverRead.readUTF();    //read message from server
                System.out.println("Server: " + Mess);  //output this message to the user
                serverWrite.close();
                serverRead.close();
                server.close();
                break;
            }
        }
    }
}