/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
 
/**
 *
 * @author diego
 */
public class FTPDownloadFile {
     public static void main(String[] args) {
        String server = "localhost";
        int port = 21;
      //  String user = "root";
      //  String pass = "Systel#4316";
        String user = "diego";
        String pass = "Systel.089";
 
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(server, port);
            //ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            // APPROACH #1: using retrieveFile(String, OutputStream)
           // String remoteFile1 = "/home/root/app/diarco_systel.csv";
            String remoteFile1 = "/home/diego/Descargas/diarco_systel.csv";
            File downloadFile1 = new File("/home/diego/Descargas/ftp/FTPFile2.txt");
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();
 
            if (success) {
                System.out.println("File #1 has been downloaded successfully.");
            }
 
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
