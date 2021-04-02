// Project 105
// Rana Ihab Ahmed
// Alaa Mahmoud Ibrahim
// Kareem Mohamed Morsy
// Mirette Amin Danial
// Mariam Ihab Mohamed

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server{
    public static List<String> users = new ArrayList<>();   //List of Users
    public static List<String> passwords = new ArrayList<>();   //List of Passwords
    public static String serverName;    //Server's Name
    public static String name; // server's name without .com
    public static File credentialsFile; //Credential's File


    public static void main(String[] args){

        Scanner input = new Scanner(System.in);
        System.out.println("Please enter the server name and port number.");
        serverName = input.nextLine();    //Take Server Name as an input from user
        int port = input.nextInt();     //Take Port num as an input from user
        name = serverName.substring(0, serverName.indexOf('.')); // remove .com
        name = name.replace(name.charAt(0), name.toUpperCase().charAt(0)); //make the first letter capital
        //Initialize file of the server with the server's name.
        File serverFolder = new File(name);
        serverFolder.mkdir();   //Create it

        //Create Credential's file which will be in the server's folder and the file is named credentials.txt
        credentialsFile = new File(name+"/credentials.txt");

        try{
            // make a file that will hold the port number of the server
            File portFile = new File(name +"/port.txt");
            portFile.createNewFile();   //Create port file
            FileWriter myWriter = new FileWriter(portFile,false);  // myWriter is used to write in the portFile
            myWriter.write(Integer.toString(port)); // write the port number of the server in the port file
            myWriter.close();

            //Create credential file if it does not exist
            // if the file exists we put the username of the existing users in users list and their passwords in the passwords list
            if (!credentialsFile.createNewFile()){
                Scanner fileScanner = new Scanner(credentialsFile); //create scanner to read from credential file
                while (fileScanner.hasNextLine()){
                    users.add(fileScanner.nextLine());
                    passwords.add(fileScanner.nextLine());
                }
            }
        }catch (IOException e){
            System.out.println("Credentials file is not created");
            return;
        }

        try{
            ServerSocket server = new ServerSocket(port);   //Create server Socket with the entered port
            System.out.println(name+" server with port number '"+port+ "' is booted up.");
            while (true){   //while there is client to be connected with (threads)
                Socket client = server.accept();
                ClientHandler clientHandler = new ClientHandler (client);
                clientHandler.start();
            }
        }catch(IOException e){
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    public static void addUser(String user, String password){   //function for adding users
        try{
            FileWriter myWriter = new FileWriter(credentialsFile,true); //To write inside the file
            myWriter.write(user+"\n");  //write user's email inside the file
            myWriter.write(password+"\n");  //write user's password inside the file
            myWriter.close();

            users.add(user);    //Add user to list of users
            passwords.add(password);    //Add password to list of Passwords
        }catch (IOException e){
            System.out.println("An error occurred when writing credentials.");
        }
    }
}