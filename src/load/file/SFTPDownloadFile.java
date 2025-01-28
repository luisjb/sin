/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import java.io.IOException;
import java.net.InetAddress;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 *
 * @author diego
 */
public class SFTPDownloadFile {

    //static Properties props;
    public static void main(String[] args) {

        SFTPDownloadFile getMyFiles = new SFTPDownloadFile();
        if (args.length < 1) {
            System.err.println("Usage: java " + getMyFiles.getClass().getName()
                    + " Properties_filename File_To_Download ");
            System.exit(1);
        }

        String propertiesFilename = args[0].trim();
        String fileToDownload = args[1].trim();
        getMyFiles.startSFTP(propertiesFilename, fileToDownload);

    }

    private SSHClient setupSshj() throws IOException {
        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect(InetAddress.getByName("192.168.100.18"), 22);
        client.authPassword("root", "Systel#4316");
        return client;
    }

    public boolean startSFTP(String propertiesFilename, String fileToDownload) {

        //props = new Properties();

        SSHClient sshClient = null;
        SFTPClient sftpClient = null;
        try {
            sshClient = setupSshj();
            sftpClient = sshClient.newSFTPClient();
            sftpClient.get("/home/root/app/diarco_systel.csv", "/home/diego/Descargas/ftp/" + "sshjFile.txt");

            System.out.println("File download successful");

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                sftpClient.close();
                sshClient.disconnect();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
