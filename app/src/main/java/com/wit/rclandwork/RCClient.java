package com.wit.rclandwork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class RCClient {

    public static final String SERVER_IP = "192.168.43.34";

    Socket socket;
    PrintWriter output;
    BufferedReader input;

    RCClient(){
        try {
            socket = new Socket(InetAddress.getByName(SERVER_IP), 4141);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void turn(int pulseWidth) {
        write(output, ""+pulseWidth);
        //write(output, "Axis Coordinates: x: "+(pulseWidth%100)+ "  y: "+(pulseWidth/100));
    }
    void write(PrintWriter output, String message) {
        System.out.println("Sending: " +message);
        output.println(message);
    }
    void closeUp() {
        try {
            String in = "";
            while ((in = input.readLine()) != null) {
                if (in.equals("FIN-ACK")){ // Final-Acknowledge
                    break;
                }
            }
            System.out.print("Closing socket.");
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
