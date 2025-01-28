/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import com.jcraft.jsch.ChannelExec;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static load.file.MainApp.SCALE_IP;
import org.joda.time.DateTime;

/**
 *
 * @author gcastillo
 */
public class SshConnection {

    ConectarPostgreSQL cnndb = null;

    public SshConnection() {

    }
    //Date localTime = new DateTime.now().toDate();

    public void sendCurrentDate(String[] sip) {
        if (sip[SCALE_IP].replace('/', ' ').trim().equalsIgnoreCase("localhost") || sip[SCALE_IP].replace('/', ' ').trim().equalsIgnoreCase("127.0.0.1")) {
            return;
        }
        System.out.println("SSH - " + sip[SCALE_IP].replace('/', ' ').trim());
        String format = "yyyy/MM/dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        // Date gmtTime = new Date( sdf.format(DateTime.now().toDate()));
        Date dt = DateTime.now().toDate();
        String sdt = sdf.format(dt);

        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;

        try {

            session = jsch.getSession("root", sip[SCALE_IP].replace('/', ' ').trim(), 22);

            session.setPassword(MainApp.pwd);

            // Configurar los algoritmos de cifrado, KEX y MAC compatibles
            java.util.Properties config = new java.util.Properties();
            // Intercambio de claves (KEX)
            config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group14-sha256,"
                    + "diffie-hellman-group16-sha512,diffie-hellman-group18-sha512,diffie-hellman-group-exchange-sha1,"
                    + "diffie-hellman-group-exchange-sha256,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,"
                    + "curve25519-sha256,curve25519-sha256@libssh.org,sntrup761x25519-sha512@openssh.com");

            // Algoritmos de cifrado (Ciphers)
            config.put("cipher.s2c", "3des-cbc,aes128-cbc,aes192-cbc,aes256-cbc,aes128-ctr,aes192-ctr,aes256-ctr,"
                    + "aes128-gcm@openssh.com,aes256-gcm@openssh.com,chacha20-poly1305@openssh.com");
            config.put("cipher.c2s", "3des-cbc,aes128-cbc,aes192-cbc,aes256-cbc,aes128-ctr,aes192-ctr,aes256-ctr,"
                    + "aes128-gcm@openssh.com,aes256-gcm@openssh.com,chacha20-poly1305@openssh.com");

            // Algoritmos MAC (Message Authentication Code)
            config.put("mac.s2c", "hmac-md5,hmac-sha1,hmac-sha2-256,hmac-sha2-512,hmac-sha2-256-96,hmac-sha2-512-96");
            config.put("mac.c2s", "hmac-md5,hmac-sha1,hmac-sha2-256,hmac-sha2-512,hmac-sha2-256-96,hmac-sha2-512-96");

            // Deshabilitar la verificación de la clave del host para evitar problemas en entornos de prueba
            config.put("StrictHostKeyChecking", "no");

            // Evitar guardar las claves en known_hosts, equivalente a UserKnownHostsFile=/dev/null
            jsch.setKnownHosts("/dev/null");
            
            session.setConfig(config);

            session.setTimeout(2000); //de la conexion
            session.connect();
            session.setTimeout(2000); //de la transferencia de datos
            System.out.println("SSH SESSION CONNECTED - " + sip[SCALE_IP].replace('/', ' ').trim());

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("date --set " + '"' + sdt + '"' + "; hwclock -w " + '"' + "/home/root/app/rtc/set-date.out -D /dev/spidev0.1 -H" + '"' + "; sync;");
            channel.connect();
            System.out.println("SSH CHANNEL CONNECTED change date - " + sip[SCALE_IP].replace('/', ' ').trim());

            byte[] buffer = new byte[1024];
            InputStream in = channel.getInputStream();
            while (true) {
                while (in.available() > 0) {
                    int bytesRead = in.read(buffer, 0, 1024);
                    if (bytesRead < 0) {
                        break;
                    }
                    System.out.print(new String(buffer, 0, bytesRead));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }

            channel.disconnect();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            System.out.println("SSH CHANNEL DISCONNECT change date - " + sip[SCALE_IP].replace('/', ' ').trim());

            channel.setCommand("sudo -s; /sbin/hwclock -w " + '"' + "/home/root/app/rtc/set-date.out -D /dev/spidev0.1 -H" + '"');
            channel.connect();
            System.out.println("SSH CHANNEL CONNECTED change hwclock - " + sip[SCALE_IP].replace('/', ' ').trim());

            while (true) {
                while (in.available() > 0) {
                    int bytesRead = in.read(buffer, 0, 1024);
                    if (bytesRead < 0) {
                        break;
                    }
                    System.out.print(new String(buffer, 0, bytesRead));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }

            channel.disconnect();
            System.out.println("SSH CHANNEL DISCONNECT change hwclock - " + sip[SCALE_IP].replace('/', ' ').trim());

            session.disconnect();
            System.out.println("SSH DISCONNECT - " + sip[SCALE_IP].replace('/', ' ').trim());
            // } catch (IOException ex) {
            //     Logger.getLogger(SshConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.out.println("SSH ERROR - " + sip[SCALE_IP].replace('/', ' ').trim());
            Logger.getLogger(SshConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            session = null;
            channel = null;
            jsch = null;
            cnndb = null;
        }
    }

    private static class ConectarPostgreSQL {

        Connection con = null;
        Statement s = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        public void CloseDB() {
            try {
                if (rs != null) {
                    if (!rs.isClosed()) {
                        rs.close();
                    }
                }

                if (ps != null) {
                    if (!ps.isClosed()) {
                        ps.close();
                    }
                }
                if (s != null) {
                    if (!s.isClosed()) {
                        s.close();
                    }
                }

                con.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

        public void UpdateDB(String sql, String ip) {

            try {
                Class.forName("org.postgresql.Driver");

                con = DriverManager.getConnection("jdbc:postgresql://" + ip + ":5432/cuora", "systel", MainApp.pwd);
                Statement s = con.createStatement();

                s.executeUpdate(sql);

                s.close();
                con.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        public void StoreProcedure(String sql) {

            try {
                Class.forName("org.postgresql.Driver");

                con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/cuora", "systel", MainApp.pwd);
                Statement s = con.createStatement();

                s.execute(sql);

                s.close();
                con.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

}
