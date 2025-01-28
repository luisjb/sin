/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.SystemUtils;

/**
 *
 * @author diego
 */
public class Ping {

//    public static void main(String args[])
//            throws IOException {
//        // create the ping command as a list of strings
//        Ping ping = new Ping();
//        List<String> commands = new ArrayList<String>();
//        commands.add("ping");
//        commands.add("-c");
//        commands.add("1");
//        commands.add("192.168.1.148");
//        ping.doCommand(commands);
//    }
    public void doCommand(List<String> command)
            throws IOException {
        String s = null;

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }

    /**
     * @param internetProtocolAddress The internet protocol address to ping
     * @return True if the address is responsive, false otherwise
     * @throws IOException
     */
    public static boolean isReachable(String internetProtocolAddress) throws IOException {
        List<String> command = new ArrayList<>();
        try {
            command.add("ping");

            if (SystemUtils.IS_OS_WINDOWS) {
                command.add("-n");
            } else if (SystemUtils.IS_OS_UNIX) {
                command.add("-c");
            } else {
                throw new UnsupportedOperationException(java.util.ResourceBundle.getBundle("load/file/Bundle").getString("UNSUPPORTED OPERATING SYSTEM"));
            }

            command.add("1");
            command.add(internetProtocolAddress);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            /*            process.waitFor();

             if (process.exitValue() == 0) {

             return true;
             } else {

             return false;
             }
             */
            BufferedReader standardOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String outputLine;

            while ((outputLine = standardOutput.readLine()) != null) {
                // Picks up Windows and Unix unreachable hosts
                if (outputLine.toLowerCase().contains("100% packet loss")) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {

            Logger.getLogger(Ping.class.getName()).log(Level.SEVERE, null, e);

            return false;
        }

    }

    public static boolean isReachable2(String ip) {
        CustomLogger customLogger = CustomLogger.getInstance();
        int timeout = 2000; // Tiempo de espera de 5 segundos

        try {
            InetAddress address = InetAddress.getByName(ip);

            // Verificación con ICMP (requiere permisos especiales en algunos sistemas)
            if (address.isReachable(timeout)) {
                customLogger.getInstance().writeLog(MainApp.INFO, ip, " is reachable");
                System.out.printf("%s is reachable%n", address);
                return true;
            } else {
                customLogger.getInstance().writeLog(MainApp.WARNING, ip, " could not be contacted via ICMP");
                System.out.printf("%s could not be contacted via ICMP%n", address);
            }

            // Si no se puede contactar via ICMP, intentemos con una conexión TCP en un puerto conocido
            try (Socket socket = new Socket()) {
                SocketAddress socketAddress = new InetSocketAddress(ip, 80);
                socket.connect(socketAddress, timeout);
                System.out.printf("%s is reachable via TCP port 80%n", ip);
                return true;
            } catch (IOException e) {
                customLogger.getInstance().writeLog(MainApp.WARNING, ip, " could not be contacted via TCP port 80");
                System.out.printf("%s could not be contacted via TCP port 80%n", ip);
            }

        } catch (Exception e) {
            Logger.getLogger(Ping.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;

    }

    public static String getIPv4Address() {
        String ipAddress = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(":") == -1) {
                        ipAddress = inetAddress.getHostAddress();
                        break;
                    }
                }
                if (ipAddress != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

}
