package org.dsd;

import com.ucod.lang.LocaleUtil;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import load.file.MainApp;
import static load.file.MainApp.SCALE_CAPACITY;
import static load.file.MainApp.SCALE_ICON;
import static load.file.MainApp.SCALE_NAME;
import static load.file.MainApp.SCALE_NUMBER;
import static load.file.MainApp.SCALE_TYPE;
import static load.file.MainApp.SCALE_UNI_MEDI;
import static load.file.MainApp.sbIP;
import static load.file.MainApp.txtStatus;

public class DiscoverClient {

    private interface Scale {

        int Number = 1;
        int Capacity = 2;
        int Unit = 3;
        int Name = 4;
        int Icon = 5;
        int Type = 6;
        int Version = 7;
        int Maceth = 8;
        int Macwifi = 9;
    }

    private static final String requestMessagePattern = "DISCOVER_FUIFSERVER_REQUEST";
    // private static String requestMessagePatternDateTime = "";
    private static final String responseMessagePattern = "DISCOVER_FUIFSERVER_RESPONSE";
    private static final String responseMessagePatternSS = "DISCOVER_FUIFSERVER_SS_RESPONSE";
    // private static final String responseMessagePatternDateTime = "DATETIME_SET_OK";
    private static final String errorMessagePattern = "|--|--|--|ERROR DE CONEXION|cuora_neo_cm.jpg";

    public void main(String[] args) throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        byte[] sendData = requestMessagePattern.getBytes();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        // requestMessagePatternDateTime = "SET_DATETIME|" + dateFormat.format(date);

        // byte[] sendDataDateTime = requestMessagePatternDateTime.getBytes();
        txtStatus.setText(LocaleUtil.getMessage("Msg.300"));

        // Broadcast the message over all the network interfaces
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue; // Don't want to broadcast to the loopback interface
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null) {
                    try {
                        broadcast = InetAddress.getByAddress(new byte[]{
                            (byte) 255, (byte) 255, (byte) 255, (byte) 255});

                    } catch (UnknownHostException ex) {
                        Logger.getLogger(DiscoverClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try {
                    if (interfaceAddress.getAddress().toString().contains(":")) {
                        continue; // Don't look this addresses                    
                    }
                    DatagramSocket datagramSocket = new DatagramSocket();
                    datagramSocket.setBroadcast(true);
                    datagramSocket.setSoTimeout(1500);
                    DatagramPacket requestPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);

                    byte[] responseBuffer = new byte[2048];
                    DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

                    datagramSocket.send(requestPacket);
                    System.out.println(getClass().getName() + ">>> Broadcast SEND: " + requestPacket.getAddress().getHostAddress());

                    while (true) { // This loop will die when IOExeption is catched from received packet.
                        String msg[] = null;
                        try {
                            responsePacket.setData(responseBuffer);
                            responsePacket.setLength(responseBuffer.length);
                            datagramSocket.receive(responsePacket);
                            //We have a response
                            System.out.println(getClass().getName() + ">>> Broadcast response from server: " + responsePacket.getAddress().getHostAddress());

                            String message = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength()); //new String(responsePacket.getData()).trim();
                            if (message.contains(responseMessagePattern) || message.contains(responseMessagePatternSS)) {
                                msg = message.split("\\|");                                System.out.println("Adding IP: " + responsePacket.getAddress().getHostAddress());

                                if (message.split("\\|").length > 7) { // si trae el tipo de balanza
                                    if (!sbIP.contains(responsePacket.getAddress().getHostAddress() + "|" + msg[SCALE_NUMBER] + "|" + msg[SCALE_CAPACITY] + "|" + msg[SCALE_UNI_MEDI] + "|" + msg[SCALE_NAME] + "|" + msg[SCALE_ICON] + "|" + msg[SCALE_TYPE] + "|" + msg[MainApp.SCALE_VERSION] + "|" + msg[MainApp.SCALE_MAC_ETH] + "|" + msg[MainApp.SCALE_MAC_WIFI])) {
                                        sbIP.add(responsePacket.getAddress().getHostAddress() + "|" + msg[SCALE_NUMBER] + "|" + msg[SCALE_CAPACITY] + "|" + msg[SCALE_UNI_MEDI] + "|" + msg[SCALE_NAME] + "|" + msg[SCALE_ICON] + "|" + msg[SCALE_TYPE] + "|" + msg[MainApp.SCALE_VERSION] + "|" + msg[MainApp.SCALE_MAC_ETH] + "|" + msg[MainApp.SCALE_MAC_WIFI]);
                                    }
                                } else if (message.split("\\|").length > 6 && message.split("\\|").length <= 7) { // si trae el tipo de balanza
                                    if (!sbIP.contains(responsePacket.getAddress().getHostAddress() + "|" + msg[SCALE_NUMBER] + "|" + msg[SCALE_CAPACITY] + "|" + msg[SCALE_UNI_MEDI] + "|" + msg[SCALE_NAME] + "|" + msg[SCALE_ICON] + "|" + msg[SCALE_TYPE])) {
                                        sbIP.add(responsePacket.getAddress().getHostAddress() + "|" + msg[SCALE_NUMBER] + "|" + msg[SCALE_CAPACITY] + "|" + msg[SCALE_UNI_MEDI] + "|" + msg[SCALE_NAME] + "|" + msg[SCALE_ICON] + "|" + msg[SCALE_TYPE]);
                                    }
                                } else {
                                    if (!sbIP.contains(responsePacket.getAddress().getHostAddress() + "|" + msg[SCALE_NUMBER] + "|" + msg[SCALE_CAPACITY] + "|" + msg[SCALE_UNI_MEDI] + "|" + msg[SCALE_NAME] + "|" + msg[SCALE_ICON] + "|" + "")) {
                                        sbIP.add(responsePacket.getAddress().getHostAddress() + "|" + msg[SCALE_NUMBER] + "|" + msg[SCALE_CAPACITY] + "|" + msg[SCALE_UNI_MEDI] + "|" + msg[SCALE_NAME] + "|" + msg[SCALE_ICON] + "|" + "");
                                    }
                                }
                            }
                        } catch (SocketTimeoutException ex) {
                            break;
                        } catch (Exception e) {
                            System.out.println("Exception: " + e);
                            if (!sbIP.contains(responsePacket.getAddress().getHostAddress() + "|" + msg[SCALE_NUMBER] + "|" + msg[SCALE_CAPACITY] + "|" + msg[SCALE_UNI_MEDI] + "|" + msg[SCALE_NAME] + "|" + msg[SCALE_ICON] + "|" + "")) {
                                sbIP.add(responsePacket.getAddress().getHostAddress() + "|" + msg[SCALE_NUMBER] + "|" + msg[SCALE_CAPACITY] + "|" + msg[SCALE_UNI_MEDI] + "|" + msg[SCALE_NAME] + "|" + msg[SCALE_ICON] + "|" + "");
                            }
                        }
                    }

//                    if (interfaceAddress.getAddress().toString().contains(":")) {
//                        continue; // Don't look this addresses                    
//                    }
//                    requestPacket = new DatagramPacket(sendDataDateTime, sendDataDateTime.length, broadcast, 8888);
//                    datagramSocket.send(requestPacket);
//                    System.out.println(getClass().getName() + ">>> Broadcast DateTime SEND: " + requestPacket.getAddress().getHostAddress());
//
//                    while (true) { // This loop will die when IOExeption is catched from received packet.
//                        try {
//                            responsePacket.setData(responseBuffer);
//                            responsePacket.setLength(responseBuffer.length);
//                            datagramSocket.receive(responsePacket);
//                            //We have a response
//                            System.out.println(getClass().getName() + ">>> Broadcast DateTime response from server: " + responsePacket.getAddress().getHostAddress());
//
//                            String message = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength()); //new String(responsePacket.getData()).trim();
//                            if (message.contains(responseMessagePatternDateTime)) {
//                                String msg[] = message.split("\\|");
//
//                                System.out.println("SET DATE TIME OK: " + responsePacket.getAddress().getHostAddress());
//                            } else {
//                                System.out.println("SET DATE TIME ERROR: " + responsePacket.getAddress().getHostAddress());
//                            }
//                        } catch (SocketTimeoutException ex) {
//                            break;
//                        }
//                    }
                } catch (UnknownHostException ex) {
                    //Logger.getLogger(DiscoverClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    //Logger.getLogger(DiscoverClient.class.getName()).log(Level.SEVERE, null, ex);

                } catch (Exception e) {
                    System.out.println("Exception: " + e);
                }

                System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
            }
        }

        System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");

    }

}
