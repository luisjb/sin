/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import com.ucod.lang.LocaleChangeListener;
import com.ucod.lang.LocaleUtil;
import com.ucod.swingplus.ModalResult;
import dbupdater.UpToDate;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import static load.file.ChooseDevices.map_DeptoMGV;
import static load.file.Utils.getPwd;

import org.apache.commons.io.FileUtils;
import org.dsd.DiscoverClient;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import java.nio.file.*;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.systel.ui.components.Utils.ListItem;

/**
 *
 * @author diego
 */
public class MainApp extends javax.swing.JFrame implements LocaleChangeListener {

    public static Color COLOR_GREEN = new java.awt.Color(59, 190, 59);
    public static Color COLOR_RED = new java.awt.Color(215, 31, 31);

    //public static StringBuffer sbIP = new StringBuffer();
    public static List<String> sbIP = new ArrayList<String>();

    public static ConfigProperties prop = null;
    public static Ping ping = new Ping();

    public static Instant creationTime_first = null;
    private static final String LAST_CHECKED_FILE = "last_checked.properties";

    String RuntimeDirectory = System.getProperty("user.dir");

    MyBusinessPosSQLServerConnector x = new MyBusinessPosSQLServerConnector();
    private static Scheduler2 scheduler;

    static void FindScales() {
        sbIP.clear();

        if (ConfigProperties.ConfigFile.getImportmanual().equalsIgnoreCase("0")) {
            String msg[] = ConfigProperties.ConfigFile.getIpv4();
            if (msg[0].isEmpty()) {
                SCALES_FOUND = 0;
                return;
            }

            for (String msg1 : msg) {
                sbIP.add(msg1 + "|" + "SCALE_NUMBER" + "|" + "SCALE_CAPACITY" + "|" + "SCALE_UNI_MEDI" + "|" + "SCALE_NAME" + "|" + "SCALE_ICON" + "|" + "SCALE_TYPE");
            }
            SCALES_FOUND = sbIP.size();

        } else {
            DiscoverScales();
        }

    }
    // List<String> commands = new ArrayList<String>();

    public static ChooseDevices chooseDev;
    // look and feel
//    public static UpperTheme nt;
//    public static UpperEssentialLookAndFeel nf;

    public static com.ucod.util.Logger logger;
    public static JPanel container;

// persistencia de datos
    // look and feel
    public static Cursor cursor;

    public static File file, fileCopy, fileStartAsService;
    public static boolean bRunByCommandLine = false;
    private static boolean bImportOnlyPrices = false;
    private static boolean bImportUpdates = false;

    // COLOR DEFINE 
//    public static Color colorBackPanel = new Color(65, 79, 171);
    //public static Color colorBackButton = new Color(229, 241, 255);
    //  public static Color colorBackButton = new Color(255, 255, 255);
    //  public static Color colorForeButton = com.ucod.FlatLookAndFeel.UpperTheme.VDBLUE; // new Color(65, 79, 171);
    // COLOR DEFINE
    public static int POPUP_TIME = 1000; // milisegundos
    public static int INTERFERENCES = 5;
    public String NameFolderndFile = "";

    public static int SCALE_NUMBER = 1, SCALE_IP = 0, SCALE_ICON = 5, SCALE_CAPACITY = 2, SCALE_UNI_MEDI = 3, SCALE_NAME = 4, SCALES_FOUND = -1, SCALE_TYPE = 6, SCALE_VERSION = 7, SCALE_MAC_ETH = 8, SCALE_MAC_WIFI = 9;
    public static String ERROR = "ERROR: ", WARNING = "WARNING: ", INFO = "INFO: ", DEBUG = "DEBUG: ";
    public Date dNow = new Date();
    public SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
    public SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    public static Date dateLog = new Date();
    public static SimpleDateFormat ftLog = new SimpleDateFormat("HH:mm:ss");

    public static DateTime timeStart = null;
    private DateTime timeFinish = null;
    private Duration duration = null;
    // private String purge = sdf.format(timeStart.minusDays(30).toDate());

    public String sError = new String();

    //public static String[] sip;
    public static int index;
    //private final Object lock;

    public static final Locale[] supportedLocales = {
        new Locale("en", "US"),
        new Locale("es", "AR"),
        new Locale("ar", "DZ"),
        new Locale("pt", "BR")
    };

    public static CryptoUtil cputil = new CryptoUtil();
    public static String OS = System.getProperty("os.name").toLowerCase();
    public static String key_pwd = "SystelPass8520";
    public static String pwd = getPwd();
    public static String VersioneIP = java.util.ResourceBundle.getBundle("SystelSyncFile").getString("BUILD") + "-" + Ping.getIPv4Address();
    private static final ReentrantLock logLock = new ReentrantLock();

    /**
     * Creates new form MainApp
     */
    public MainApp() {

        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        logger = new NeoLogger(java.util.logging.Logger.getLogger(MainApp.class.getName()));

        // System.out.println((Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval"));
//        try {
//            nf = new UpperEssentialLookAndFeel(false, "");
//            nt = new UpperTheme();
//            UpperEssentialLookAndFeel.setCurrentTheme(nt);
//            javax.swing.UIManager.setLookAndFeel(nf);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
 /*      try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
         if (java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NIMBUS").equals(info.getName())) {
         javax.swing.UIManager.setLookAndFeel(info.getClassName());
         break;
         }
         }
         } catch (ClassNotFoundException ex) {
         java.util.logging.Logger.getLogger(MainApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
         java.util.logging.Logger.getLogger(MainApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
         java.util.logging.Logger.getLogger(MainApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
         java.util.logging.Logger.getLogger(MainApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         */
        //</editor-fold>
//        System.setProperty("awt.useSystemAAFontSettings", "on");
//        System.setProperty("swing.aatext", "true");
        //</editor-fold>
        initComponents();
        btnConfig.setVisible(false);
        System.setProperty("file.encoding", "UTF-8");
        //setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/dsd/icons/import.png")));
        ImageIcon im = new ImageIcon(getClass().getResource("/org/dsd/icons/import.png"));
        this.setIconImage(im.getImage());

        this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

        // Set the size of the JFrame to the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width, screenSize.height);
        this.setBounds(0, 0, screenSize.width, screenSize.height);
        this.setPreferredSize(screenSize);
        // Maximize the JFrame
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        // this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        txtTitle.setText(txtTitle.getText().trim() + "    v" + java.util.ResourceBundle.getBundle("SystelSyncFile").getString("BUILD") + " - IP: " + Ping.getIPv4Address());

        container = new JPanel();
        container.setBorder(new EmptyBorder(2, 2, 2, 2));
        jPanel3.add(container, java.awt.BorderLayout.CENTER);
        this.pack();
        if (!bRunByCommandLine) {
            this.setVisible(true);
            this.repaint();
        }

        NameFolderndFile = ConfigProperties.ConfigFile.getLogPath() + ft.format(dNow.getTime());

        file = new File(NameFolderndFile + ".log");
        CustomLogger.setLogFilePath(NameFolderndFile + ".log");
        CustomLogger customLogger = CustomLogger.getInstance();

        displayValues();
        LocaleUtil.addLocaleChangeListener(this);

        try {
            new Thread(new Runnable() {
                JProgressBar pgbr = null;

                @Override
                public void run() {
                    //si las ip estan fijas (modo manual)
                    System.out.println("Version:" + java.util.ResourceBundle.getBundle("SystelSyncFile").getString("BUILD"));
                    System.out.println("Importmanual: " + ConfigProperties.ConfigFile.getImportmanual());

                    FindScales();
                    if (!bRunByCommandLine) {
                        chooseDev = new ChooseDevices(null, Dialog.ModalityType.DOCUMENT_MODAL, sbIP);

                        chooseDev.setModal(true);
                        chooseDev.setVisible(true);

                        if (chooseDev.getModalResult() == null || chooseDev.getModalResult() == ModalResult.CANCEL || chooseDev.getDevicesSelected().length < 1) {
                            dispose();
                            if (!bRunByCommandLine) {
                                customLogger.shutdown();
                            }
                            return;
                        }
                        displayValues();

                        SCALES_FOUND = chooseDev.getDevicesSelected().length;
                    } else {
                        scheduler.isOnExecution = true;
                        displayValues();

                        SCALES_FOUND = sbIP.size();
                    }

                    if (!sbIP.isEmpty()) {
                        //  synchronized (lock) {
                        index = 1;
                        timeStart = new DateTime();
                        customLogger.getInstance().writeLog(INFO, "", "");
                        customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Inicio de importación:") + index + LocaleUtil.getMessage("de") + SCALES_FOUND + LocaleUtil.getMessage("balanzas"));
                        customLogger.getInstance().writeLog(INFO, "", "Version:" + java.util.ResourceBundle.getBundle("SystelSyncFile").getString("BUILD"));

                        MemoryUsage.getRAM();

                        InetAddress inetAddress;
                        try {
                            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                            while (interfaces.hasMoreElements()) {
                                NetworkInterface networkInterface = interfaces.nextElement();
                                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                                while (addresses.hasMoreElements()) {
                                    inetAddress = addresses.nextElement();
                                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                                        System.out.println("Interfaz: " + networkInterface.getName() + " - IP SERVER: " + inetAddress.getHostAddress());
                                        customLogger.getInstance().writeLog(INFO, "", "");
                                        customLogger.getInstance().writeLog(INFO, "", "Interfaz: " + networkInterface.getName() + " - IP SERVER: " + inetAddress.getHostAddress());
                                        customLogger.getInstance().writeLog(INFO, "", "");
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        //ExecutorService pool = Executors.newFixedThreadPool(1);
                        ExecutorService pool = Executors.newScheduledThreadPool(SCALES_FOUND);

//SI ES POR UI
                        if (!bRunByCommandLine) {
                            if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV_DEPTO.toString())) {

                                loadMapDeptoMGV();
                                pgbr = new JProgressBar();
                                JLabel lblTime = new JLabel("");
                                panelStatusSlave.add(pgbr);
                                pgbr.setStringPainted(true);
                                pgbr.setMinimum(0);
                                pgbr.setMaximum(0);
                                pgbr.setValue(0);
                                Runnable worker = new FormatSelector("localhost", pgbr, new JLabel(""), chooseDev.getImportType(), chooseDev.getImportFile());
                                worker.run();
                            }

                            String format = "yyyy/MM/dd HH:mm:ss";
                            SimpleDateFormat sdf = new SimpleDateFormat(format);

                            //tomo los datos del archivo de configuración
                            try {
                                ConfigProperties.getPropValues();
                            } catch (IOException ex) {
                                Logger.getLogger(ShowConfigs.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            for (String ip : chooseDev.getDevicesSelected()) {
                                String[] sip = ip.split("\\|");

                                if (isWindows()) {

                                    if (ConfigProperties.ConfigFile.getPsqlPath().isEmpty() || !new File(ConfigProperties.ConfigFile.getPsqlPath()).exists()) {
                                        customLogger.getInstance().writeLog(ERROR, "", "");
                                        customLogger.getInstance().writeLog(ERROR, "", "****************************************************************");
                                        customLogger.getInstance().writeLog(ERROR, "", "****************************************************************");
                                        customLogger.getInstance().writeLog(ERROR, "", LocaleUtil.getMessage("SetPsqlPath"));
                                        customLogger.getInstance().writeLog(ERROR, "", "****************************************************************");
                                        customLogger.getInstance().writeLog(ERROR, "", "****************************************************************");
                                        return;
                                    }
                                }

                                try {
                                    if (Ping.isReachable2(sip[SCALE_IP].replace('/', ' ').trim())) {
                                        UpToDate.getInstance(sip[SCALE_IP].replace('/', ' ').trim(), ConfigProperties.ConfigFile.getPsqlPath(), MainApp.pwd);
                                    } else {
                                        customLogger.getInstance().writeLog(MainApp.WARNING, sip[SCALE_IP].replace('/', ' ').trim(), " UpToDate - NO SE PUEDE CONECTAR CON EL DISPOSITIVO");
                                        System.out.println("");
                                        System.out.println(sip[SCALE_IP].replace('/', ' ').trim() + " UpToDate - NO SE PUEDE CONECTAR CON EL DISPOSITIVO");
                                        System.out.println("");
                                    }

                                } catch (Exception ex) {
                                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                customLogger.getInstance().writeLog(INFO, "", "");
                                customLogger.getInstance().writeLog(INFO, "", "****************************************************************");
                                customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Balanza Número:") + index + LocaleUtil.getMessage("de") + SCALES_FOUND + LocaleUtil.getMessage("balanzas"));
                                customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Balanza IP:") + sip[SCALE_IP].replace('/', ' ').trim());

                                pgbr = new JProgressBar();
                                JLabel lblTime = new JLabel("");
                                panelStatusSlave.add(pgbr);
                                pgbr.setString(LocaleUtil.getMessage("Nro:") + sip[SCALE_NUMBER] + LocaleUtil.getMessage("Nom:") + sip[SCALE_NAME] + LocaleUtil.getMessage("IP:") + sip[SCALE_IP].replace('/', ' ').trim());
                                pgbr.setStringPainted(true);
                                pgbr.setMinimum(0);
                                pgbr.setMaximum(0);
                                pgbr.setValue(0);
                                panelStatusSlave.add(lblTime);
                                panelStatusSlave.add(new JSeparator(SwingConstants.HORIZONTAL));

                                //Runnable worker = new FormatSelector(sip[SCALE_IP].replace('/', ' ').trim(), pgbr, lblTime, bImportOnlyPrices);
                                // Runnable worker = new FormatSelector(sip[SCALE_IP].replace('/', ' ').trim(), pgbr, lblTime, chooseDev.getImportType(), chooseDev.getImportFile());
                                pool.execute(new FormatSelector(sip[SCALE_IP].replace('/', ' ').trim(), pgbr, lblTime, chooseDev.getImportType(), chooseDev.getImportFile()));

                                index++;
                            }

                            for (String ip : chooseDev.getDevicesSelected()) {
                                final String[] sip = ip.split("\\|");
                                try {
                                    if (Ping.isReachable2(sip[SCALE_IP].replace('/', ' ').trim())) {
                                        if (!sip[SCALE_IP].replace('/', ' ').trim().equalsIgnoreCase("localhost")
                                                || !sip[SCALE_IP].replace('/', ' ').trim().equalsIgnoreCase("127.0.0.1")) {
                                            SshConnection sshcon = new SshConnection();
                                            sshcon.sendCurrentDate(sip);
                                            customLogger.getInstance().writeLog(INFO, sip[SCALE_IP].replace('/', ' '), LocaleUtil.getMessage("Set Date:") + sdf.format(DateTime.now().toDate()).trim());
                                        } else {
                                            customLogger.getInstance().writeLog(INFO, sip[SCALE_IP].replace('/', ' '), LocaleUtil.getMessage("Ignore Date:"));
                                        }
                                    } else {
                                        customLogger.getInstance().writeLog(MainApp.ERROR, sip[SCALE_IP].replace('/', ' ').trim(), " SSH CONNECTION - NO SE PUEDE CONECTAR CON EL DISPOSITIVO");
                                        System.out.println("");
                                        System.out.println(sip[SCALE_IP].replace('/', ' ').trim() + " SSH CONNECTION - NO SE PUEDE CONECTAR CON EL DISPOSITIVO");
                                        System.out.println("");
                                    }
                                } catch (Exception ex) {
                                    customLogger.getInstance().writeLog(ERROR, sip[SCALE_IP].replace('/', ' '), "AL SETEAR FECHA");

                                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                                } finally {

                                }
                            }
                        } else {
// SI ES POR PROCESO    

//                            System.out.println("01");
                            //tomo los datos del archivo de configuración por si se hicieron cambios en caliente y no se reinició el servicio
                            JLabel lblTime = new JLabel("");
                            pgbr = new JProgressBar();
                            try {
                                ConfigProperties.getPropValues();
                                if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV_DEPTO.toString())) {
                                    System.out.println("loadMapDeptoMGV");
                                    customLogger.getInstance().writeLog(INFO, "", "loadMapDeptoMGV");
//MemoryUsage.getRAM();
                                    loadMapDeptoMGV();
//MemoryUsage.getRAM();
                                    System.out.println("");
                                    System.out.println("START Import file to localhost DB");
                                    System.out.println("");
//MemoryUsage.getRAM();
                                    customLogger.getInstance().writeLog(INFO, "", "");
                                    customLogger.getInstance().writeLog(INFO, "", "START Import file to localhost DB");
                                    customLogger.getInstance().writeLog(INFO, "", "");
//MemoryUsage.getRAM();

                                    Path filePath = null;
                                    try {
                                        if (!ConfigProperties.ConfigFile.getFilePathNovedades().trim().isEmpty()) {

                                            filePath = Paths.get(ConfigProperties.ConfigFile.getFilePathNovedades());
                                            BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);

                                            Instant modifiedTime = attributes.lastModifiedTime().toInstant();

                                            LocalDateTime modifiedDateTime = LocalDateTime.ofInstant(modifiedTime, ZoneId.systemDefault());

                                            LocalDateTime lastCheckedDateTime = getLastCheckedDateTime();

                                            System.out.println("");
                                            System.out.println("****************************************");
                                            System.out.println("modifiedDateTime: " + modifiedDateTime);
                                            System.out.println("lastCheckedDateTime: " + lastCheckedDateTime);
                                            System.out.println("****************************************");
                                            System.out.println("");

                                            customLogger.getInstance().writeLog(INFO, "", "");
                                            customLogger.getInstance().writeLog(INFO, "", "****************************************");
                                            customLogger.getInstance().writeLog(INFO, "", "modifiedDateTime: " + modifiedDateTime);
                                            customLogger.getInstance().writeLog(INFO, "", "lastCheckedDateTime: " + lastCheckedDateTime);
                                            customLogger.getInstance().writeLog(INFO, "", "****************************************");
                                            customLogger.getInstance().writeLog(INFO, "", "");

                                            if (lastCheckedDateTime == null || modifiedDateTime.isAfter(lastCheckedDateTime)) {
                                                Runnable worker = new FormatSelector("localhost", pgbr, lblTime, bImportOnlyPrices, bImportUpdates);
                                                worker.run();

                                                saveLastCheckedDateTime(modifiedDateTime);

                                            } else {
                                                System.out.println("");
                                                System.out.println("El archivo ya fue importado localmente, no se volverá a importar hasta que haya cambios");
                                                System.out.println("");
                                                customLogger.getInstance().writeLog(INFO, "", "");
                                                customLogger.getInstance().writeLog(INFO, "", "El archivo ya fue importado localmente, no se volverá a importar hasta que haya cambios");
                                                customLogger.getInstance().writeLog(INFO, "", "");
                                            }
                                        }
                                    } catch (Exception e) {
                                        // MainApp.customLogger.getInstance().writeLog(MainApp.ERROR, sip[SCALE_IP].replace('/', ' ').trim(), "Importando el archivo: " + filePath + ": " + e.toString());
                                        customLogger.getInstance().writeLog(MainApp.ERROR, "", "Importando el archivo: " + filePath + ": " + e.toString());
                                        ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                                    }
//MemoryUsage.getRAM();
                                    System.out.println("");
                                    System.out.println("FINISH Import file to localhost DB");
                                    System.out.println("");
                                    customLogger.getInstance().writeLog(INFO, "", "");
                                    customLogger.getInstance().writeLog(INFO, "", "FINISH Import file to localhost DB");
                                    customLogger.getInstance().writeLog(INFO, "", "");
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(ShowConfigs.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.out.println("PSQL PATH: " + ConfigProperties.ConfigFile.getPsqlPath());

                            // Iterar sobre todos los dispositivos
//                            System.out.println("");
//                            System.out.println("");
//                            System.out.println("");
//                            System.out.println("");
//                            System.out.println("");
//                            System.out.println("@@ sbIP:" + sbIP.toString());
//                            System.out.println("");
//                            System.out.println("");
//                            System.out.println("");
//                            System.out.println("");
                            for (String ip : sbIP) {

                                if (index > sbIP.size()) {
                                    break;
                                }

                                final String[] sip = ip.split("\\|");
                                try {
                                    if (Ping.isReachable2(sip[SCALE_IP].replace('/', ' ').trim())) {
                                        UpToDate.getInstance(sip[SCALE_IP].replace('/', ' ').trim(), ConfigProperties.ConfigFile.getPsqlPath(), MainApp.pwd);
                                    } else {
                                        customLogger.getInstance().writeLog(MainApp.ERROR, sip[SCALE_IP].replace('/', ' ').trim(), " UpToDate - NO SE PUEDE CONECTAR CON EL DISPOSITIVO");
                                        System.out.println("");
                                        System.out.println(sip[SCALE_IP].replace('/', ' ').trim() + " UpToDate - NO SE PUEDE CONECTAR CON EL DISPOSITIVO");
                                        System.out.println("");
                                    }

                                } catch (Exception ex) {
                                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                customLogger.getInstance().writeLog(INFO, "", "");
                                customLogger.getInstance().writeLog(INFO, "", "****************************************************************");
                                customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Balanza Número:") + index + LocaleUtil.getMessage("de") + sbIP.size() + LocaleUtil.getMessage("balanzas"));
                                customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Balanza IP:") + sip[SCALE_IP].replace('/', ' ').trim());
////                                System.out.println("03");
                                pgbr = new JProgressBar();
                                lblTime = new JLabel("");
                                panelStatusSlave.add(pgbr);
//                                System.out.println("04");
                                pgbr.setString(LocaleUtil.getMessage("Nro:") + sip[SCALE_NUMBER] + LocaleUtil.getMessage("Nom:") + sip[SCALE_NAME] + LocaleUtil.getMessage("IP:") + sip[SCALE_IP].replace('/', ' ').trim());
                                pgbr.setStringPainted(true);
                                pgbr.setMinimum(0);
                                pgbr.setMaximum(0);
                                pgbr.setValue(0);
//                                System.out.println("04");
                                panelStatusSlave.add(lblTime);
                                panelStatusSlave.add(new JSeparator(SwingConstants.HORIZONTAL));
                                //Runnable worker = new FormatSelector(sip[SCALE_IP].replace('/', ' ').trim(), pgbr, lblTime, bImportOnlyPrices);
//                                System.out.println("05");
                                if (Ping.isReachable2(sip[SCALE_IP].replace('/', ' ').trim())) {
                                    if (!sip[SCALE_IP].replace('/', ' ').trim().equalsIgnoreCase("localhost") || !sip[SCALE_IP].replace('/', ' ').trim().equalsIgnoreCase("127.0.0.1")) {

                                        SshConnection sshcon = new SshConnection();
                                        sshcon.sendCurrentDate(sip);

                                        customLogger.getInstance().writeLog(INFO, sip[SCALE_IP].replace('/', ' '), LocaleUtil.getMessage("Set Date:") + sdf.format(DateTime.now().toDate()).trim());
                                    } else {
                                        customLogger.getInstance().writeLog(INFO, sip[SCALE_IP].replace('/', ' '), LocaleUtil.getMessage("Ignore Date:"));
                                    }
                                } else {
                                    customLogger.getInstance().writeLog(MainApp.ERROR, sip[SCALE_IP].replace('/', ' ').trim(), " SSH CONNECTION - NO SE PUEDE CONECTAR CON EL DISPOSITIVO");
                                    System.out.println("");
                                    System.out.println(sip[SCALE_IP].replace('/', ' ').trim() + " SSH CONNECTION - NO SE PUEDE CONECTAR CON EL DISPOSITIVO");
                                    System.out.println("");
                                }

                                //Runnable worker = new FormatSelector(sip[SCALE_IP].replace('/', ' ').trim(), pgbr, lblTime, bImportOnlyPrices, bImportUpdates);
//                                chooseDev.getImportType() = bImportOnlyPrices
//                                , chooseDev.getImportFile() = bImportUpdates
//                                System.out.println("06");
                                pool.execute(new FormatSelector(sip[SCALE_IP].replace('/', ' ').trim(), pgbr, lblTime, bImportOnlyPrices, bImportUpdates));

//                                System.out.println("07");
                                index++;
                            }

                        }

                        pool.shutdown();

                        // Wait until all threads are finish
                        while (!pool.isTerminated()) {

                            // pb_1.setValue((int) System.currentTimeMillis());
                            // panelStatusMaster.revalidate();
                        }
                        //System.out.println("\nFinished all threads");

                        //                       MemoryUsage.getRAM();
//System.out.println("ram_FINISH POOL: "+ sip[SCALE_IP].replace('/', ' ').trim()); 
                        timeFinish = new DateTime();
                        duration = new Duration(timeStart, timeFinish);

                        customLogger.getInstance().writeLog(INFO, "", "");
//
////  System.out.print(duration.getStandardMinutes() % 60 + " minutes, ");
////  System.out.print(duration.getStandardSeconds() % 60 + " seconds.");

                        customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Duración de la tarea:") + duration.getStandardMinutes() + LocaleUtil.getMessage("minutos") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("segundos."));
                        customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Fin del proceso"));
                        txtStatus.setText(LocaleUtil.getMessage("Fin del proceso"));

                        // sError.append(ftLog.format(dateLog.getTime()) + ": " + INFO + LocaleUtil.getMessage("Fin del proceso") + "\n");
                        System.out.println("");
                        System.out.println("");
                        System.out.println("");
                        System.out.println(LocaleUtil.getMessage("Duración de la tarea:") + duration.getStandardMinutes() + LocaleUtil.getMessage("minutos") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("segundos."));
                        System.out.println(LocaleUtil.getMessage("Fin del proceso"));
                        System.out.println("");
                        System.out.println("");
                        System.out.println("");

                        if (ConfigProperties.ConfigFile.SendEmail()) {
                            EMail email = new EMail(ConfigProperties.ConfigFile.getEmailUser(),
                                    ConfigProperties.ConfigFile.getEmailPass(),
                                    ConfigProperties.ConfigFile.getEmailHost(),
                                    ConfigProperties.ConfigFile.getEmailPort(),
                                    "");
                            //sip[SCALE_IP].replace('/', ' ').trim());
                            for (String address : ConfigProperties.ConfigFile.getEmailDestinatary()) {
                                email.addAddress(address);
                            }

                            email.sendMail(customLogger.readLog(true) + "\n" + customLogger.readLogError(true));

                        }

                        //fileWriter(file, customLogger.readLog(true));
                        try {

                            sError = CustomLogger.getInstance().readLogError(true);
                            txtLog.setText(CustomLogger.getInstance().readLog(true));
                            customLogger.flushLogs();
                            Thread.sleep(1000);
                            MoveFileAndLog(NameFolderndFile);

                            PurgeLog(10);
                            if (ConfigProperties.ConfigFile.getShowmessage().equalsIgnoreCase("1") && !bRunByCommandLine) {
                                if (!bRunByCommandLine) {
                                    sError += "\n" + LocaleUtil.getMessage("Fin del proceso") + "\n";
                                    showMessage(LocaleUtil.getMessage("Atención."), sError.toString());
                                } else {
                                    System.out.println("Atención: " + sError.toString());
                                }
                            }

                            //this.finalize();
                        } catch (IOException ex) {
                            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Throwable ex) {
                            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);

                        } finally {
                            if (bRunByCommandLine) {
                                try {
                                    System.out.println("ExitCodes.ERROR_STATUS A:" + ExitCodes.ERROR_STATUS);
                                    //System.exit(ExitCodes.ERROR_STATUS);
                                } catch (Throwable ex) {
                                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        //sendMail();
                        //  }
                    } else {
                        customLogger.getInstance().writeLog(ERROR, "", LocaleUtil.getMessage("No se encontraron balanzas conectadas."));
                        ExitCodes.ERROR_STATUS = ExitCodes.SCALES_NOT_FOUNDS;

                        customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Fin del proceso"));
                        txtStatus.setText(LocaleUtil.getMessage("Fin del proceso"));

                        //sError.append(ftLog.format(dateLog.getTime()) + ": " + INFO + LocaleUtil.getMessage("Fin del proceso") + "\n");
                        if (ConfigProperties.ConfigFile.SendEmail()) {
                            EMail email = new EMail(ConfigProperties.ConfigFile.getEmailUser(),
                                    ConfigProperties.ConfigFile.getEmailPass(),
                                    ConfigProperties.ConfigFile.getEmailHost(),
                                    ConfigProperties.ConfigFile.getEmailPort(),
                                    "");
                            //sip[SCALE_IP].replace('/', ' ').trim());
                            for (String address : ConfigProperties.ConfigFile.getEmailDestinatary()) {
                                email.addAddress(address);
                            }

                            email.sendMail(customLogger.readLog(true) + "\n" + CustomLogger.getInstance().readLogError(true));
                        }

                        //fileWriter(file, customLogger.readLog(true));
                        try {

                            sError = CustomLogger.getInstance().readLogError(true);

                            customLogger.flushLogs();
                            Thread.sleep(1000);

                            MoveFileAndLog(NameFolderndFile);

                            PurgeLog(10);

                            if (ConfigProperties.ConfigFile.getShowmessage().equalsIgnoreCase("1") && !bRunByCommandLine) {
                                sError += "\n" + LocaleUtil.getMessage("Fin del proceso") + "\n";
                                showMessage(LocaleUtil.getMessage("Atención."), sError.toString());
                            }

                            //this.finalize();
                        } catch (IOException ex) {
                            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Throwable ex) {
                            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            if (bRunByCommandLine) {
                                try {
                                    System.out.println("ExitCodes.ERROR_STATUS B:" + ExitCodes.ERROR_STATUS);
                                    //System.exit(ExitCodes.ERROR_STATUS);
                                } catch (Throwable ex) {
                                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        //sendMail();
                    }
                    sError = "";
                    txtLog.setText("");
                    //customLogger.flushLogs();
                    if (!bRunByCommandLine) {
                        customLogger.shutdown();
                        System.exit(0);
                    }
//                    System.out.println("");
//                    System.out.println("");
//                    System.out.println("      CHANGE isOnExecution = false");
                    scheduler.isOnExecution = false;
                    //scheduler.stop();
                }
            }
            ).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }
    }

    private static boolean isProcessRunning(String processName) {
        ProcessBuilder processBuilder = new ProcessBuilder("SystelImportService");
        try {
            Process process = processBuilder.start();
            String output = process.getInputStream().toString();
            return output.contains(processName);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class ScaleType {

        public static String CUORA_NEO = "NEO";
        public static String CUORA_NEO2 = "NEO2";
        public static String CUORA_MAX = "MAX";
        public static String MODULAR = "MODULAR";
        public static String ROBUSTA = "ROBUSTA";
        public static String VERIF_PRECIO_VICL = "VICL";

    }

    private void MoveFileAndLog(String directoryName) throws IOException {
        File theDir = new File(directoryName);

// if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + directoryName);
            boolean result = false;

            try {
                result = theDir.mkdir();
            } catch (SecurityException se) {
                //handle it
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, se);
            } catch (Exception se) {
                //handle it
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, se);
            }

            //si se pudo crear el directorio
            if (result) {
                try {
                    // Copy a file
                    if (isWindows()) {
                        System.out.println("This is Windows");
                        //copio el log
                        fileCopy = FileUtils.getFile(directoryName + File.separator + file.getName());
                        FileUtils.moveFile(file, fileCopy);
                    } else { // (isUnix()) {
                        System.out.println("This is Unix or Linux");
                        //copio el log 
                        fileCopy = FileUtils.getFile(directoryName + File.separator + file.getName());
                        FileUtils.moveFile(file, fileCopy);
                    }
                } catch (IOException e) {
                    System.out.println("ERROR COPY FILE: " + e.getMessage());
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, e);
                } finally {
                    file = null;
                    fileCopy = null;
                }
                File fileInput = null;
                File fileInputCopy = null;
                try {
                    fileInput = new File(bImportUpdates ? ConfigProperties.ConfigFile.getFilePathNovedades() : ConfigProperties.ConfigFile.getFilePath());
                    fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                    if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV.toString())
                            || ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV_DEPTO.toString())) {

                        String filePathMGVDepto;
                        if (isWindows()) {
                            filePathMGVDepto = "C:" + File.separator + "SYSTEL" + File.separator + "Importador" + File.separator + "MGVDepto.properties";
                        } else {
                            filePathMGVDepto = File.separator + "tmp" + File.separator + "MGVDepto.properties";
                        }

                        fileInput = new File(filePathMGVDepto);
                        fileInputCopy = new File(directoryName + File.separator + fileInput.getName());

                        //al MGVDepto.properties no hay que eliminarlo
                        if (fileInput.exists()) {
                            FileUtils.copyFile(fileInput, fileInputCopy);
                        }

                        if (!ConfigProperties.ConfigFile.getFilePathNovedades().isEmpty()) {
                            fileInput = new File(ConfigProperties.ConfigFile.getFilePathNovedades());
                            fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                            //delete the original file
                            if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                                //FileUtils.forceDelete(file);
                                if (fileInput.exists()) {
                                    FileUtils.moveFile(fileInput, fileInputCopy);
                                }
                            } else {
                                if (fileInput.exists()) {
                                    FileUtils.copyFile(fileInput, fileInputCopy);
                                }
                            }
                        }
                        if (!ConfigProperties.ConfigFile.getItensMGVNutInfoFilePath().isEmpty()) {
                            fileInput = new File(ConfigProperties.ConfigFile.getItensMGVNutInfoFilePath());
                            fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                            //delete the original file
                            if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                                //FileUtils.forceDelete(file);
                                if (fileInput.exists()) {
                                    FileUtils.moveFile(fileInput, fileInputCopy);
                                }
                            } else {
                                if (fileInput.exists()) {
                                    FileUtils.copyFile(fileInput, fileInputCopy);
                                }
                            }
                        }
                        if (!ConfigProperties.ConfigFile.getItensMGVReceingFilePath().isEmpty()) {
                            fileInput = new File(ConfigProperties.ConfigFile.getItensMGVReceingFilePath());
                            fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                            //delete the original file
                            if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                                //FileUtils.forceDelete(file);
                                if (fileInput.exists()) {
                                    FileUtils.moveFile(fileInput, fileInputCopy);
                                }
                            } else {
                                if (fileInput.exists()) {
                                    FileUtils.copyFile(fileInput, fileInputCopy);
                                }
                            }
                        }
                        if (!ConfigProperties.ConfigFile.getItensMGVTaraFilePath().isEmpty()) {
                            fileInput = new File(ConfigProperties.ConfigFile.getItensMGVTaraFilePath());
                            fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                            //delete the original file
                            if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                                //FileUtils.forceDelete(file);
                                if (fileInput.exists()) {
                                    FileUtils.moveFile(fileInput, fileInputCopy);
                                }
                            } else {
                                if (fileInput.exists()) {
                                    FileUtils.copyFile(fileInput, fileInputCopy);
                                }
                            }
                        }
                        if (!ConfigProperties.ConfigFile.getItensMGVConservacionFilePath().isEmpty()) {
                            fileInput = new File(ConfigProperties.ConfigFile.getItensMGVConservacionFilePath());
                            fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                            //delete the original file
                            if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                                //FileUtils.forceDelete(file);
                                if (fileInput.exists()) {
                                    FileUtils.moveFile(fileInput, fileInputCopy);
                                }
                            } else {
                                if (fileInput.exists()) {
                                    FileUtils.copyFile(fileInput, fileInputCopy);
                                }
                            }
                        }
                        if (!ConfigProperties.ConfigFile.getItensMGVExtra2FilePath().isEmpty()) {
                            fileInput = new File(ConfigProperties.ConfigFile.getItensMGVExtra2FilePath());
                            fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                            //delete the original file
                            if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                                //FileUtils.forceDelete(file);
                                if (fileInput.exists()) {
                                    FileUtils.moveFile(fileInput, fileInputCopy);
                                }
                            } else {
                                if (fileInput.exists()) {
                                    FileUtils.copyFile(fileInput, fileInputCopy);
                                }
                            }
                        }
                        if (!ConfigProperties.ConfigFile.getItensMGVIngredientsFilePath().isEmpty()) {
                            fileInput = new File(ConfigProperties.ConfigFile.getItensMGVIngredientsFilePath());
                            fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                            //delete the original file
                            if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                                //FileUtils.forceDelete(file);
                                if (fileInput.exists()) {
                                    FileUtils.moveFile(fileInput, fileInputCopy);
                                }
                            } else {
                                if (fileInput.exists()) {
                                    FileUtils.copyFile(fileInput, fileInputCopy);
                                }
                            }
                        }
                        if (!ConfigProperties.ConfigFile.getItensMGVIngredientsFilePath2().isEmpty()) {
                            fileInput = new File(ConfigProperties.ConfigFile.getItensMGVIngredientsFilePath2());
                            fileInputCopy = new File(directoryName + File.separator + fileInput.getName());
                            //delete the original file
                            if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                                //FileUtils.forceDelete(file);
                                if (fileInput.exists()) {
                                    FileUtils.moveFile(fileInput, fileInputCopy);
                                }
                            } else {
                                if (fileInput.exists()) {
                                    FileUtils.copyFile(fileInput, fileInputCopy);
                                }
                            }
                        }
                    } else {
                        //delete the original file
                        if (ConfigProperties.ConfigFile.getDeleteFileInput().equals("1")) {
                            //FileUtils.forceDelete(file);
                            if (fileInput.exists()) {
                                FileUtils.moveFile(fileInput, fileInputCopy);
                            }
                        } else {
                            if (fileInput.exists()) {
                                FileUtils.copyFile(fileInput, fileInputCopy);
                            }
                        }
                    }

                } catch (IOException e) {
                    // customLogger.getInstance().writeLog(ERROR, "IOException", e.getMessage());
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, e);

                } catch (Exception e) {
                    //  customLogger.getInstance().writeLog(ERROR, "Exception", e.getMessage());
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, e);
                } finally {
                    fileInput = null;
                    fileInputCopy = null;
                    theDir = null;
                }
            }
        }
    }

    public static void deleteFolder(File folder, String purge) throws IOException {
        File[] files = folder.listFiles();
        long lPurge = Long.valueOf(purge);
        long lFolderPurge = 0;
        try {
            if (files != null) { //some JVMs return null for empty dirs
                for (File f : files) {
                    if (f.isDirectory()) {
                        lFolderPurge = Long.valueOf(f.getPath().substring((f.getPath().length() - 14), (f.getPath().length() - 6)));
                        if (lFolderPurge < lPurge) {
                            FileUtils.deleteDirectory(f);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            files = null;
        }

    }

//    public static void sendMail() {
//        final String username = "testingsystel@gmail.com";
//        final String password = "Systel#4316";
//
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.port", "587");
//
//        Session session = Session.getInstance(props,
//                new javax.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(username, password);
//            }
//        });
//
//        try {
//
//            // Define message
//            MimeMessage message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(username));
//            message.setSubject("Proceso de importación balanzas Systel");
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress("dbelatti@systel.com.ar"));
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress("gcastillo@systel.com.ar"));
//            message.setText(txtLog.getText());
//            // Envia el mensaje
//            Transport.send(message);
//        } catch (Exception e) {
//        }
//    }
    @Override
    public void localeChanged(Locale locale, Locale locale1) {
        displayValues();
    }

//    public static class LocaleSelected {
//
//        public static final String ESPANOL = "es-AR";
//        public static final String PORTUGUES = "pt-BR";
//        public static final String INGLES = "en-US";
//        public static final String ARABE = "ar-DZ";
//
//        public static final int i_ESPANOL = 1;
//        public static final int i_PORTUGUES = 2;
//        public static final int i_INGLES = 3;
//        public static final int i_ARABE = 4;
//
//        public static Locale portuguesLocale;
//        public static Locale espanolLocale;
//        public static Locale inglesLocale;
//        public static Locale arabeLocale;
//        public static Locale defaultLocale;
//
//        public static void SetLocaleDefault(Locale locale) {
//            Locale.setDefault(locale);
//            ResourceBundle.clearCache();
//        }
//
//        public static Locale GetLocale(String locale) {
//            switch (locale) {
//                case LocaleSelected.ESPANOL:
//                    return espanolLocale;
//
//                case LocaleSelected.PORTUGUES:
//                    return portuguesLocale;
//
//                case LocaleSelected.INGLES:
//                    return inglesLocale;
//
//                case LocaleSelected.ARABE:
//                    return arabeLocale;
//
//                default:
//                    return espanolLocale;
//            }
//        }
//
//    }
    public static void showMessage(String title, String message) {
        MessageDialog msgDlg = new MessageDialog((java.awt.Frame) null, title, true);
        msgDlg.setFirstButtonVisible(false);
        msgDlg.setSecondButtonVisible(false);
        msgDlg.setThirdButtonVisible(true);
        msgDlg.setThirdButtonText(LocaleUtil.getMessage("ACEPTAR"));
        msgDlg.setThirdButtonModalResult(ModalResult.OK);
        msgDlg.setMessage(message);
        msgDlg.setBounds(0, 0, 800, 600);
        msgDlg.setVisible(true);
        msgDlg.dispose();
        //System.gc();
        msgDlg = null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        desktopPane = new javax.swing.JDesktopPane();
        jPanel3 = new javax.swing.JPanel();
        txtTitle = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        panelStatusMaster = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        panelStatusSlave = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        txtStatus = new javax.swing.JLabel();
        btnConfig = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("load/file/Bundle"); // NOI18N
        setTitle(bundle.getString("MainApp.title")); // NOI18N
        setPreferredSize(new java.awt.Dimension(1080, 720));
        setSize(new java.awt.Dimension(1080, 720));

        desktopPane.setPreferredSize(new java.awt.Dimension(1440, 900));
        desktopPane.setLayout(new java.awt.BorderLayout());

        jPanel3.setPreferredSize(new java.awt.Dimension(10, 50));
        jPanel3.setLayout(new java.awt.BorderLayout());

        txtTitle.setFont(new java.awt.Font("Calibri", 1, 24)); // NOI18N
        txtTitle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/dsd/icons/SYSTEL.png"))); // NOI18N
        txtTitle.setText(bundle.getString("MainApp.txtTitle.text")); // NOI18N
        txtTitle.setDoubleBuffered(true);
        txtTitle.setIconTextGap(5);
        txtTitle.setMaximumSize(new java.awt.Dimension(313, 200));
        txtTitle.setMinimumSize(new java.awt.Dimension(313, 200));
        txtTitle.setPreferredSize(new java.awt.Dimension(312, 200));
        jPanel3.add(txtTitle, java.awt.BorderLayout.CENTER);

        desktopPane.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel4.setLayout(new java.awt.BorderLayout());

        panelStatusMaster.setLayout(new java.awt.BorderLayout());

        panelStatusSlave.setLayout(new java.awt.GridLayout(50, 0, 5, 3));
        jScrollPane2.setViewportView(panelStatusSlave);

        panelStatusMaster.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel4.add(panelStatusMaster, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setDoubleBuffered(true);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(23, 400));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(239, 150));

        txtLog.setEditable(false);
        txtLog.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        txtLog.setDoubleBuffered(true);
        jScrollPane1.setViewportView(txtLog);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel5, java.awt.BorderLayout.PAGE_END);

        desktopPane.add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel1.setPreferredSize(new java.awt.Dimension(10, 100));
        jPanel1.setLayout(new java.awt.BorderLayout());

        txtStatus.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        txtStatus.setText(bundle.getString("MainApp.txtStatus.text")); // NOI18N
        jPanel1.add(txtStatus, java.awt.BorderLayout.NORTH);

        btnConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/dsd/icons/settings21.png"))); // NOI18N
        btnConfig.setText(bundle.getString("MainApp.btnConfig.text")); // NOI18N
        btnConfig.setEnabled(false);
        btnConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigActionPerformed(evt);
            }
        });
        jPanel1.add(btnConfig, java.awt.BorderLayout.CENTER);

        desktopPane.add(jPanel1, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(desktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        desktopPane.getAccessibleContext().setAccessibleName(bundle.getString("MainApp.desktopPane.AccessibleContext.accessibleName")); // NOI18N
        desktopPane.getAccessibleContext().setAccessibleDescription(bundle.getString("MainApp.desktopPane.AccessibleContext.accessibleDescription")); // NOI18N

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigActionPerformed
        ShowConfigs sc = new ShowConfigs();

        sc.setModal(true);
        //sc.setAlwaysOnTop(true);
        sc.setModalityType(ModalityType.APPLICATION_MODAL);
        sc.pack();
        sc.setVisible(true);
        sc.dispose();
        sc = null;

    }//GEN-LAST:event_btnConfigActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if (java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NIMBUS").equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainApp.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainApp.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainApp.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainApp.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        try {
            prop = new ConfigProperties();

            prop.getPropValues();
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        Locale.setDefault(ConfigProperties.ConfigFile.getLocaleLang());
        ResourceBundle.clearCache();

        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
        int count = 0;
        try {
            for (String arg : args) {

                if (arg.contains("-")) {
                    bRunByCommandLine = true;
                } else {
                    bRunByCommandLine = false;
                }

                if (arg.equalsIgnoreCase("--help")) {//ayuda
                    System.out.println(LocaleUtil.getMessage("Modo de empleo: SystelSyncFile.jar -i -n|c -p|f"));
                    System.out.println("");
                    System.out.println(LocaleUtil.getMessage("En Windows puede intentar:"));
                    System.out.println("");
                    System.out.println(LocaleUtil.getMessage("Importación completa: SystelSyncFile.jar -i -c -f"));
                    System.out.println("");
                    System.out.println(LocaleUtil.getMessage("Importación novedades / sólo precios: SystelSyncFile.jar -i -n -p"));
                    System.out.println("");
                    System.out.println(LocaleUtil.getMessage("Los tres argumentos son obligatorios."));
                    System.out.println(LocaleUtil.getMessage("    -i, indica que iniciará en modo línea de comando."));
                    System.out.println(LocaleUtil.getMessage("    -c, usa el archivo completo."));
                    System.out.println(LocaleUtil.getMessage("    -n, usa el archivo de novedades."));
                    System.out.println(LocaleUtil.getMessage("    -p, importa sólo precios."));
                    System.out.println(LocaleUtil.getMessage("    -f, importación de datos completa."));
                    System.out.println(LocaleUtil.getMessage("    -scheduler, ejecutar en background con -i -c -f."));
                    System.out.println("");
                }

                /* Ejecución del scheduler */
                if (arg.equalsIgnoreCase("-scheduler")) {

                    bRunByCommandLine = true;
                    bImportOnlyPrices = ConfigProperties.ConfigFile.getModeImport().trim().equalsIgnoreCase("0");
                    bImportUpdates = false; //ConfigProperties.ConfigFile.getModeArchive().trim().equalsIgnoreCase("0");

                    Runnable myTask = () -> {
                        System.out.println(" ######## Running scheduled task at: " + LocalDateTime.now() + "  ##########");
                        new MainApp().setVisible(false);
                    };

                    //Scheduler.SCHEDULER_TYPE schedulerType = Scheduler.SCHEDULER_TYPE.MINUTES;
                    switch (Integer.parseInt(ConfigProperties.ConfigFile.getSchedulerPeriod())) {
                        case 0:
                            //schedulerType = Scheduler.SCHEDULER_TYPE.MINUTES;
                            scheduler = new Scheduler2(Scheduler2.SCHEDULER_TYPE.MINUTES, myTask);
                            scheduler.setIntervalMinutes(Integer.parseInt(ConfigProperties.ConfigFile.getSchedulerTimeValue())); // Ejecutar cada 35 minutos
                            break;
                        case 1:
                            //schedulerType = Scheduler.SCHEDULER_TYPE.DAYS;
                            scheduler = new Scheduler2(Scheduler2.SCHEDULER_TYPE.HOURS, myTask);
                            scheduler.setHourList(Arrays.asList(ConfigProperties.ConfigFile.getSchedulerHours()).stream().sorted().collect(Collectors.toList())); // Ejecutar cada 35 minutos
                            break;
                    }

                    scheduler.startExecution();
                    //Scheduler scheduler = new Scheduler(null, schedulerType, null);
                    //scheduler.startExecution();

                    //String format = "yyyyMMddHHmmss";
                    //SimpleDateFormat sdf = new SimpleDateFormat(format);
                    // Date gmtTime = new Date( sdf.format(DateTime.now().toDate()));
                    //Date dt = DateTime.now().toDate();
                    //String sdt = sdf.format(dt);
                    //CreateFile.FileInRuntimePath(sdt + ".log");
                    return;
                }

                if (arg.equalsIgnoreCase("-i")) { // si es aejecutado por commando
                    count++;
                    bRunByCommandLine = true;
//                        break;
                }

                if (arg.equalsIgnoreCase("-n")) { // novedades
                    count++;
                    bImportUpdates = true;
                } else if (arg.equalsIgnoreCase("-c")) { // completo
                    count++;
                    bImportUpdates = false;
                }

                if (arg.equalsIgnoreCase("-p")) { //solo precios
                    count++;
                    bImportOnlyPrices = true;
                } else if (arg.equalsIgnoreCase("-f")) {//full
                    count++;
                    bImportOnlyPrices = false;
                }

            }

            if (count < 3 && bRunByCommandLine) {

                System.out.println(LocaleUtil.getMessage("Use SystelSyncFile.jar --help para obtener ayuda"));

                try {
                    bRunByCommandLine = false;
                    ExitCodes.ERROR_STATUS = ExitCodes.INSUFFICIENT_PARAMS;
                    System.out.println("ExitCodes.ERROR_STATUS C:" + ExitCodes.ERROR_STATUS);
                    System.exit(ExitCodes.ERROR_STATUS);
                    //return;
                } catch (Throwable ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (!bRunByCommandLine) {
                new MainApp().setVisible(true);
            } else {
                new MainApp().setVisible(false);
            }

//                    nf = new UpperEssentialLookAndFeel(false, "");
//                    nt = new UpperTheme();
//                    UpperEssentialLookAndFeel.setCurrentTheme(nt);
//                    javax.swing.UIManager.setLookAndFeel(nf);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//                    System.out.println("");
//                    System.out.println("");
//                    System.out.println("      CHANGE 2 isOnExecution = false");
//                    Scheduler2.isOnExecution = false;
        }
//            }
//        });
    }

    public static class CreateFile {

        private static void FileInRuntimePath(String fileName) {
            File fileAux = null;
            try {

                String currentDirectory = System.getProperty("user.dir");
                fileAux = new File(currentDirectory + File.separator + fileName);
                if (fileAux.createNewFile()) {
                    System.out.println("Archivo creado en: " + fileAux.getAbsolutePath());
                } else {
                    System.out.println("El archivo ya existe en: " + fileAux.getAbsolutePath());
                }
            } catch (IOException e) {
                System.out.println("Error al crear el archivo: " + e.getMessage());
            } finally {
                fileAux = null;
            }

        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConfig;
    private javax.swing.JDesktopPane desktopPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelStatusMaster;
    private javax.swing.JPanel panelStatusSlave;
    public static javax.swing.JTextArea txtLog;
    public static javax.swing.JLabel txtStatus;
    private javax.swing.JLabel txtTitle;
    // End of variables declaration//GEN-END:variables

//    private void ImportFile(String ip) {
//        FormatSelector lf = new FormatSelector();
//        lf.main(ip);
//    }
    private static void DiscoverScales() {
        CustomLogger customLogger = CustomLogger.getInstance();
        sbIP.clear();
        txtStatus.setText("");
        txtLog.setText("");

        customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Msg.300"));
        customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("El proceso puede demorar unos minutos. Aguarde...."));

        DiscoverClient ClientUDP = new DiscoverClient();
        try {
            ClientUDP.main(new String[]{""});
            ClientUDP.main(new String[]{""});
            ClientUDP.main(new String[]{""});
        } catch (SocketException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        SCALES_FOUND = sbIP.size();
        if (!sbIP.isEmpty()) {

            customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Se encontraron") + SCALES_FOUND + LocaleUtil.getMessage("balanzas conectadas."));
            ListItem item = null;
            // Iterar sobre todos los dispositivos
            for (String ip : sbIP) {

                final String[] sip = ip.split("\\|");
                String strIP2 = sip[SCALE_IP].replace('/', ' ').trim();

                if (sip.length > 6) { // si trae el tipo de balanza
                    item = new ListItem(sip[SCALE_NUMBER].trim(), strIP2, sip[SCALE_ICON].trim(), ListItem.ON_LINE, sip[SCALE_CAPACITY].trim(), sip[SCALE_UNI_MEDI].trim(), sip[SCALE_NAME].trim(), sip[SCALE_TYPE].trim());
                } else {
                    item = new ListItem(sip[SCALE_NUMBER].trim(), strIP2, sip[SCALE_ICON].trim(), ListItem.ON_LINE, sip[SCALE_CAPACITY].trim(), sip[SCALE_UNI_MEDI].trim(), sip[SCALE_NAME].trim(), "");
                }

                String htmlButton = LocaleUtil.getMessage("Balanza Número:") + item.getScaleNumber()
                        + LocaleUtil.getMessage("IP") + item.getScaleIP().replace('/', ' ').trim()
                        + LocaleUtil.getMessage("Nombre") + item.getScaleName()
                        + LocaleUtil.getMessage("Capacidad") + item.getScaleCapacity() + " " + item.getScaleUniMedi() + "\n";

                customLogger.getInstance().writeLog(INFO, "", htmlButton);
            }
        } else {
            customLogger.getInstance().writeLog(WARNING, "", LocaleUtil.getMessage("No se encontraron balanzas conectadas."));
            customLogger.getInstance().writeLog(INFO, "", LocaleUtil.getMessage("Fin del proceso"));

            txtStatus.setText(LocaleUtil.getMessage("Fin del proceso"));

        }
        //jPanel3.add(container, java.awt.BorderLayout.CENTER);
        //jSplitPane1.setRightComponent(container);

        ClientUDP = null;
    }

//    public void fileWriter(File savePath, String textArea) {
//
//        try {
//            File tmp = new File(savePath.getAbsolutePath());
//            tmp.getParentFile().mkdirs();
//
//            FileWriter fw = new FileWriter(savePath, true);
//            fw.write(textArea);
//            fw.close();
//        } catch (IOException ex) {
//            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    public static void PurgeLog(int days) {
        /**
         * *****************************************************
         ***************** BORRAR LOGS VIEJOS ******************
         * ****************************************************
         */

        File logDirectory = new File(ConfigProperties.ConfigFile.getLogPath());
        FileFilter filter = null;

        //  System.out.println("ConfigProperties.ConfigFile.getLogPath():" + ConfigProperties.ConfigFile.getLogPath());
        // System.out.println("logDirectory: " + logDirectory.getAbsolutePath());
        try {

            if (logDirectory.exists() && logDirectory.isDirectory()) {
                filter = new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname == null || pathname.isFile() || !pathname.getName().startsWith("2")) {
                            return false;  // Solo necesito los directorios que contienen los logs.
                        }
                        /*
                    Date current = new Date(System.currentTimeMillis());
                    Date fileDate = new Date(file.lastModified());
                         */
                        //TimeUnit.DAYS.convert(System.currentTimeMillis()-file.lastModified(), TimeUnit.MILLISECONDS);

                        // System.out.println("pathname.getName():" + pathname.getName());                        
                        final LocalDate fileDate = LocalDate.parse(pathname.getName(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                        final LocalDate currentDate = LocalDate.now();
                        //return (TimeUnit.DAYS.convert(System.currentTimeMillis()- basic.modifiedTime().toMillis(), TimeUnit.MILLISECONDS)) >= 7;

                        return ChronoUnit.DAYS.between(fileDate, currentDate) >= days;

                    }

                };
                for (File f : logDirectory.listFiles(filter)) {
                    //System.out.println("delete:" + f.getAbsolutePath());
                    FileUtils.deleteQuietly(f);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            filter = null;
            logDirectory = null;
        }

    }

//    public static void customLogger.getInstance().writeLog(String type, String ip, String message) {
//        logLock.lock();
//        try {
//            dateLog = new Date();
//            txtLog.append(ftLog.format(dateLog.getTime()) + ": " + type + " - " + ip + " - " + message + "\n");
//            txtLog.setCaretPosition(txtLog.getDocument().getLength());
////        if (bRunByCommandLine) {
////            System.out.println(ftLog.format(dateLog.getTime()) + ": " + type + " - " + ip + " - " + message);
////        }
//            if (type.equalsIgnoreCase(ERROR)) {
//                ExitCodes.ERROR_STATUS = ExitCodes.GENERIC_ERROR;
//                sError.append(ftLog.format(dateLog.getTime()) + ": " + type + " - " + ip + " - " + message + "\n");
//                if (bRunByCommandLine) {
//                    System.out.println(ftLog.format(dateLog.getTime()) + ": " + type + " - " + ip + " - " + message);
//                }
//            }
//        } finally {
//            logLock.unlock();
//        }
//    }
    private void displayValues() {
        txtStatus.setText(LocaleUtil.getMessage("MainApp.txtStatus.text"));
//        openButton.setText(LocaleUtil.getMessage("ABRIR"));
//        saveButton.setText(LocaleUtil.getMessage("GUARDAR"));
//        sendButton.setText(LocaleUtil.getMessage("TRANSFERIR_A_EQUIPO"));
//        readButton.setText(LocaleUtil.getMessage("LEER_DESDE_EQUIPO"));
//        docPropsButton.setText(LocaleUtil.getMessage("PROPIEDADES"));
//        pageLabel.setText(LocaleUtil.getMessage("TemplateEditor.pageLabel.text"));
//        // código para que el jodido swing respete el tamaño minimo de los botones
//        // del toolbar
//        for (java.awt.Component comp : topToolBar.getComponents()) {
//            if (comp instanceof javax.swing.JButton) {
//                comp.setPreferredSize(null);
//                java.awt.Dimension dim;
//
//                if (comp.getPreferredSize().width < comp.getMinimumSize().width) {
//                    dim = new java.awt.Dimension(
//                            comp.getMinimumSize().width, comp.getPreferredSize().height);
//                } else {
//                    dim = comp.getPreferredSize();
//                }
//                comp.setPreferredSize(dim);
//                comp.setSize(dim);
//            }
//        }
//        this.setTitle(LocaleUtil.getMessage("NOMBRE_APLICACION"));
        pack();
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    public static boolean isSolaris() {
        return OS.contains("sunos");
    }

    public static String getOS() {
        if (isWindows()) {
            return "win";
        } else if (isMac()) {
            return "osx";
        } else if (isUnix()) {
            return "uni";
        } else if (isSolaris()) {
            return "sol";
        } else {
            return "err";
        }
    }

    public static void loadMapDeptoMGV() {
        CustomLogger customLogger = CustomLogger.getInstance();
        Properties properties = new Properties();
        String filePath;
        if (isWindows()) {
            filePath = "C:" + File.separator + "SYSTEL" + File.separator + "Importador" + File.separator + "MGVDepto.properties";
        } else {
            filePath = File.separator + "tmp" + File.separator + "MGVDepto.properties";
        }
        File MGVDepto = new File(filePath);
        if (!MGVDepto.exists()) {
            try {
                MGVDepto.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("loadMapDeptoMGV():" + filePath);
        customLogger.writeLog(INFO, "", "loadMapDeptoMGV():" + filePath);

        try (FileInputStream fis = new FileInputStream(filePath)) {
            try {
                properties.load(fis);
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 1) {
                    String key = parts[0];
                    List<String> values = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));
                    map_DeptoMGV.put(key, values);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static LocalDateTime getLastCheckedDateTime() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(LAST_CHECKED_FILE)) {
            properties.load(input);
            String lastChecked = properties.getProperty("lastChecked");
            if (lastChecked != null) {
                return LocalDateTime.parse(lastChecked);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveLastCheckedDateTime(LocalDateTime dateTime) {
        Properties properties = new Properties();
        properties.setProperty("lastChecked", dateTime.toString());
        try (OutputStream output = new FileOutputStream(LAST_CHECKED_FILE)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
