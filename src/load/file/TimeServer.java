/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 *
 * @author diego
 */
public class TimeServer {

    public static void main(String[] args) {

        ServerSocket serverSocket = null;

        /* section one */
        try {
            serverSocket = new ServerSocket();
            serverSocket.setSoTimeout(1000);
            serverSocket.bind(
                    new InetSocketAddress(
                            InetAddress.getLocalHost(),
                            9999));
        } catch (IOException ioe) {
            System.err.println(
                    "Could not bind a server socket to port 9999: " + ioe);
            return; // System.exit(1);
        }

        /* section two */
        System.out.println("Server is now taking connections...");
        Socket socket = new Socket();

        while (true) {
            try {
                socket.setSoTimeout(1000);
                socket = serverSocket.accept();
                System.out.println("Connection from: "
                        + socket.getInetAddress());
                OutputStreamWriter writer = new OutputStreamWriter(
                        socket.getOutputStream());
                writer.write(new Date().toString() + "\r\n");
                writer.flush();

            } catch (IOException ie) {
                System.err.println("Exception: " + ie);
            }
        }

    }

}
