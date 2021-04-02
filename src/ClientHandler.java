// Project 105
// Rana Ihab Ahmed
// Alaa Mahmoud Ibrahim
// Kareem Mohamed Morsy
// Mirette Amin Danial
// Mariam Ihab Mohamed

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {
    Socket client;
    DataInputStream clientRead;
    DataOutputStream clientWrite;
    String name = null;
    boolean isUser = false; // to determine if the server is connected to a client or another server

    ClientHandler(Socket client) {
        try {
            this.client = client;
            clientRead = new DataInputStream(client.getInputStream());
            clientWrite = new DataOutputStream(client.getOutputStream());
            clientWrite.writeUTF("220 " + Server.serverName);
        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = clientRead.readUTF();

                if (message.equalsIgnoreCase("REGISTER") ||
                        message.equalsIgnoreCase("LOGIN") ||
                        message.startsWith("HELLO")) {
                    if (message.equalsIgnoreCase("REGISTER")) {
                        String user = clientRead.readUTF();
                        String password = clientRead.readUTF();

                        if (!user.endsWith("@" + Server.serverName)) {
                            // if the user entered the name of another server
                            clientWrite.writeUTF("535 Authentication credentials invalid - Incorrect email");
                        }
                        else if (Server.users.contains(user.substring(0, user.indexOf('@')))) {
                            // if the username entered is used by another user
                            clientWrite.writeUTF("535 Authentication credentials invalid - Email Exists");
                        }
                        else {
                            name = user.substring(0, user.indexOf('@'));
                            clientWrite.writeUTF("250 REGISTERED");
                            // add the user's name and password to the credential's file and the respective lists
                            Server.addUser(name, password);
                            // make a folder for the user in the server's folder
                            File userFolder = new File(Server.name + "/" + name);
                            userFolder.mkdir();
                            // make inbox file for the user in the user's folder
                            File userFile = new File(Server.name + "/" + name + "/inbox.txt");
                            userFile.createNewFile();
                        }
                    }
                    else if (message.equalsIgnoreCase("LOGIN")) {
                        String user = clientRead.readUTF();
                        String password = clientRead.readUTF();

                        if (!user.endsWith("@" + Server.serverName)) {
                            // if the user entered the name of another server
                            clientWrite.writeUTF("535 Authentication credentials invalid - Incorrect email");
                        }
                        else {
                            name = user.substring(0, user.indexOf('@'));
                            // search for the user in the list and get its index
                            int userId = Server.users.indexOf(name);
                            if (userId == -1) { // no user was found with this name
                                clientWrite.writeUTF("535 Authentication credentials invalid - Email Doesn't Exists");
                                // skip the rest of the while loop
                                // make the user choose from the beginning either to register, login or quit
                                continue;
                            } else {
                                // compare the entered password to the existing password
                                if (!password.equals(Server.passwords.get(userId))) { // if wrong password entered
                                    clientWrite.writeUTF("535 Authentication credentials invalid - Incorrect password");
                                    // skip the rest of the while loop
                                    // make the user choose from the beginning either to register, login or quit
                                    continue;
                                }
                            }
                            clientWrite.writeUTF("250 LOGIN"); // this would not be outputted at the user's console
                            // only sent because th client would be expecting a message

                        }
                    }
                    else if (message.startsWith("HELLO")) {
                        if (name != null) {
                            // User has connected
                            isUser = true;
                        }
                        else // else: Other server has connected
                            name = message.replaceFirst("HELLO ", ""); // get server's name

                        clientWrite.writeUTF("250 Hello " + name + ", pleased to meet you");
                        handleUser();
                        break;
                    }
                }
                else if (message.equalsIgnoreCase("QUIT")) {
                    clientWrite.writeUTF("221 " + Server.serverName + " closing connection");
                    clientWrite.close();
                    clientRead.close();
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            try {
                clientWrite.close();
            } catch (IOException ignored) {
            }
            try {
                clientRead.close();
            } catch (IOException ignored) {
            }
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleUser() throws IOException { // function to either quit or send email or receive email
        String from = "";
        String to = "";

        while (true) {
            StringBuilder data = new StringBuilder();
            String message = clientRead.readUTF();
            if (message.startsWith("MAIL FROM")) {
                // Replace first occurrence of "MAIL FROM" with empty string
                from = message.replaceFirst("MAIL FROM ", ""); // get the sending user's email
                clientWrite.writeUTF("250 " + from + "...Sender ok");
            }
            else if (message.equalsIgnoreCase("RCPT TO")) {
                to = clientRead.readUTF(); // get the email of the recipient
                String userName = to.substring(0, to.indexOf('@'));
                String otherServer = to.substring(to.indexOf("@") + 1); // get the server's name
                String otherName = otherServer.substring(0, otherServer.indexOf('.')); // remove .com
                otherName = otherName.replace(otherName.charAt(0), otherName.toUpperCase().charAt(0)); //make the first letter capital
                File portFile = new File(otherName + "/port.txt"); // get the port file of the recipient's server
                File userFile = new File(Server.name + "/" + userName + "/inbox.txt");
                if(otherServer.equals(Server.serverName) && !userFile.exists()){ // userFile does not exist which means user does not exist
                    clientWrite.writeUTF("550 invalid address");
                    continue;
                }
                else if(!portFile.exists()){ // portFile does not exist which means server does not exist
                    clientWrite.writeUTF("550 invalid address");
                    continue;
                }
                else if(!otherServer.equals(Server.serverName)) {
                    try { // see if the other server is online or offline
                        Scanner fileScanner = new Scanner(portFile);
                        int port = fileScanner.nextInt(); // get the port number of the other server
                        InetAddress ip = InetAddress.getLocalHost(); // get ip address
                        Socket server = new Socket(ip, port);

                        // We found out that this code will prevent sending to users from other servers
                        // As well as it will access other servers directly which is not how SMTP works

                        /*if(!userFile.exists()){
                        //    clientWrite.writeUTF("550 invalid address");
                        //    server.close();
                        //    continue;
                        }*/
                        server.close();
                    }catch (IOException e) {
                        clientWrite.writeUTF("550 invalid address");
                        continue;
                    }

                }
                clientWrite.writeUTF("250 " + to + "...Recipient ok");
            }
            else if (message.equalsIgnoreCase("DATA")) { // get the data of the email
                clientWrite.writeUTF("Please enter the body of your email ended by ‘&&&‘");
                while (true) {
                    message = clientRead.readUTF();
                    if (!message.equals("&&&")) // if the message did not end
                        data.append(message).append("\n");
                    else {
                        clientWrite.writeUTF("250 Message accepted for delivery");
                        break;
                    }
                }
                String messageData = data.toString();

                if (!isUser) //Server
                {
                    String userName = to.substring(0, to.indexOf('@')); // get the username, subtract the server's name
                    // get the inbox's file of the user
                    File userFile = new File(Server.name + "/" + userName + "/inbox.txt");
                    FileWriter writer = new FileWriter(userFile, true); // used to write in the inbox file

                    // write the message in the inbox
                    writer.write("FROM: " + from + '\n');
                    writer.write("TO: " + to + '\n');
                    writer.write('\n' + messageData);
                    writer.write("========================" + '\n'); // to separate this email from the next one
                    writer.close();
                }
                else {
                    // check whether the recipient is in the same server
                    if (to.endsWith("@" + Server.serverName)) {
                        String userName = to.substring(0, to.indexOf('@'));
                        File userFile = new File(Server.name + "/" + userName + "/inbox.txt");
                        FileWriter writer = new FileWriter(userFile, true); // used to write in the recipient's inbox

                        // write the message in the inbox
                        writer.write("FROM: " + from + '\n');
                        writer.write("TO: " + to + '\n');
                        writer.write('\n' + messageData + '\n');
                        writer.write("========================" + '\n'); // to separate this email from the next one
                        writer.close();
                    }
                    else { // the recipient is a user of another server
                        String otherServer = to.substring(to.indexOf("@") + 1); // get the server's name
                        String otherName = otherServer.substring(0, otherServer.indexOf('.')); // remove .com
                        otherName = otherName.replace(otherName.charAt(0), otherName.toUpperCase().charAt(0)); //make the first letter capital
                        File portFile = new File(otherName + "/port.txt"); // get the port file of the recipient's server
                        Scanner fileScanner = new Scanner(portFile);
                        int port = fileScanner.nextInt(); // get the port number of the other server

                        InetAddress ip = InetAddress.getLocalHost(); // get ip address

                        Socket server = new Socket(ip, port); // start a connection with the recipient's server
                        DataOutputStream serverWrite = new DataOutputStream(server.getOutputStream());
                        DataInputStream serverRead = new DataInputStream(server.getInputStream());
                        serverRead.readUTF();
                        serverWrite.writeUTF("HELLO " + Server.serverName);
                        serverRead.readUTF();
                        serverWrite.writeUTF("MAIL FROM " + from);
                        serverRead.readUTF();
                        serverWrite.writeUTF("RCPT TO");
                        serverWrite.writeUTF(to);
                        serverRead.readUTF();
                        serverWrite.writeUTF("DATA");
                        serverWrite.writeUTF(messageData); // the data is appended in messageData
                        serverWrite.writeUTF("&&&"); // so the other server could know that the message ended
                        serverRead.readUTF();
                        serverWrite.writeUTF("QUIT"); // to close the connection after sending the email
                        serverRead.readUTF();
                        serverRead.close();
                        serverWrite.close();
                        server.close();
                    }
                }
            }
            else if (message.equalsIgnoreCase("QUIT")) { // close the connection
                clientWrite.writeUTF("221 " + Server.serverName + " closing connection");
                clientWrite.close();
                clientRead.close();
                client.close();
                break;
            }
        }
    }

    public Socket getClient() {
        return client;
    }

    public DataInputStream getClientRead() {
        return clientRead;
    }

    public DataOutputStream getClientWrite() {
        return clientWrite;
    }

    public String getClientName() {
        return name;
    }

    public void setClientName(String name) {
        this.name = name;
    }
}