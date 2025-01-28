/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;

/**
 *
 * @author pllanos
 */
public class Utils {

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";

    private static final String VER_SOFT = "1.0.0";
    private static final String VER_MSP = "1.0.0";
//    private static final String VER_LINUX = "Linux cuora_neo 3.0.35+yocto #23 SMP PREEMPT Wed Oct 8 00:47:15 ART 2014 armv7l GNU/Linux";
    private static final String VER_DB = "1.0.0";
//    private static final String VER_POSTGRES = "9.3";
    /*
     static boolean getVersionDB(
     * Get the extension of a file.
     */
    private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean IsValidIP(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = Utils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    static String getVersionSoft() {
        return java.util.ResourceBundle.getBundle("SystelSyncFile").getString("BUILD");
        //return VER_SOFT;
    }

    static String getVersionMSP() {
        return VER_MSP;
    }

    static String getVersionLinux() {
        try {
            return execCmd("uname", "-a");
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public static String execCmd(String file, String cmd) throws java.io.IOException, InterruptedException {
//        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
//        return s.hasNext() ? s.next() : "";

        StringBuilder out = new StringBuilder();
        ProcessBuilder ps = new ProcessBuilder(file, cmd);

//From the DOC:  Initially, this property is false, meaning that the
//standard output and error output of a subprocess are sent to two
//separate streams
        ps.redirectErrorStream(true);

        Process pr = ps.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            //System.out.println(line);
            out.append(line + "\r\n");
        }
        pr.waitFor();
        //  System.out.println("ok!");

        in.close();
        return out.toString();
    }

    static String getVersionDB() {
        return VER_DB;
    }

    static String getVersionJava() {
        try {
            return execCmd("java", "-version");
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";

    }

    static String getVersionPostgres() {
        try {
            return execCmd("/usr/bin/postgres", "-V");
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public enum ModalResult {

        ok, cancel, yes, no;
    }
    /* Cuora Neo Special Keys */
    public static final int KEY_MENU = 121;
    public static final int KEY_TOUCHCALIB = 119;
    public static final int KEY_PREPACK = 120;
    public static final int KEY_TESTMENU = 123;
    public static final int KEY_KEYBOARD = 122;
    public static final int KEY_PAPERFEED = 114;
    public static final int KEY_PRINTTEST = 154;
    public static final int KEY_TARE = 113;
    public static final int KEY_ZERO = 115;
    public static final int KEY_DISCOUNT = 116;
    public static final int KEY_PRICE = 117;
    public static final int KEY_QUANTITY = 118;
    //public static final int KEY_POWER = 0;
    public static final int KEY_POWER = 124;
    public static final int KEY_CALIB = 179;
    public static final int KEY_ENTER = '\n';

    public static void executeCommand(String cmd) {
        Runtime run = Runtime.getRuntime();
        Process pr = null;
        try {
            pr = run.exec(cmd);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            pr.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String fill_with_z(String str, int len) {
        for (; str.length() < len;) {
            str = "0" + str;
        }
        return str;
    }

    public static boolean isNumeric(String str) {
        return str.matches("[+-]?\\d*(\\.\\d+)?");
    }

    public static class ComboItem {

        private String key;
        private String value;

        public ComboItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static class ComboItemPrice {

        private String key;
        private String value;
        private String show;
        private String plvName;

        public ComboItemPrice(String key, String value, String show, String plvName) {
            this.key = key;
            this.value = value;
            this.show = show;
            this.plvName = plvName;
        }

        @Override
        public String toString() {
            return key;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getShow() {
            return show;
        }

        public String getplvName() {
            return plvName;
        }

    }
    
    public static class ListItemDevices {

        private String key;
        private String value;

        public ListItemDevices(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }


    }

    public static final boolean isDouble(String item) {
        Pattern pattern = Pattern.compile("^[-+]?\\d+(\\.{0,1}(\\d+?))?$");
        Matcher matcher = pattern.matcher(item);
        boolean result = matcher.matches();
        return result;
    }

    public static Object GetDecimal(double valor, int decimales) {
        DecimalFormat f;
        switch (decimales) {
            case 0:
                f = new DecimalFormat("####0");
                break;
            case 1:
                f = new DecimalFormat("####0.0");
                break;
            case 2:
                f = new DecimalFormat("####0.00");
                break;
            case 3:
                f = new DecimalFormat("####0.000");
                break;
            case 4:
                f = new DecimalFormat("####0.0000");
                break;
            default:
                f = new DecimalFormat("####0.00");
        }

        return f.format(valor);

    }

    public static Double GetDecimalDouble(double valor, int decimales) {
        DecimalFormat f;
        switch (decimales) {
            case 0:
                f = new DecimalFormat("####0");
                break;
            case 1:
                f = new DecimalFormat("####0.0");
                break;
            case 2:
                f = new DecimalFormat("####0.00");
                break;
            case 3:
                f = new DecimalFormat("####0.000");
                break;
            case 4:
                f = new DecimalFormat("####0.0000");
                break;
            default:
                f = new DecimalFormat("####0.00");
        }
        return Double.parseDouble(f.format(valor).replace(',', '.'));

        //return Double.valueOf(f.format(valor));
    }

    public static boolean isNumericDot(CharSequence cs) {
        if (cs == null) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if ((!Character.isDigit(cs.charAt(i))) && (cs.charAt(i) != '.')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumericComma(CharSequence cs) {
        if (cs == null) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if ((!Character.isDigit(cs.charAt(i))) && (cs.charAt(i) != ',')) {
                return false;
            }
        }
        return true;
    }

    public static class ListItem {

        private String key;
        private String value;
        private String iconId;
        private int tktValue;

        public ListItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public ListItem(String key, String value, String iconId) {
            this.key = key;
            this.value = value;
            this.iconId = iconId;
        }

        public ListItem(String key, String value, String iconId, int tktValue) {
            this.key = key;
            this.value = value;
            this.iconId = iconId;
            this.tktValue = tktValue;
        }

        @Override
        public String toString() {
            return key;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getIconId() {
            return iconId;
        }

        public int getTktValue() {
            return tktValue;
        }

        public void setTktValue(int tktValue) {
            this.tktValue = tktValue;
        }

    }

    public static class FakeEvent {

        public static void fakeActionPerformed(Component c) {
            ActionEvent ae = new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "");
            fakeExecute(ae);
        }

        public static void fakeKeyPressed(Component c, int vkcode) {
            KeyEvent ke = new KeyEvent(c, KeyEvent.KEY_PRESSED,
                    0, // When timeStamp
                    0, // Modifier
                    vkcode, // Key Code
                    KeyEvent.CHAR_UNDEFINED);  // Key Char
            fakeExecute(ke);
        }

        public static void fakeKeyReleased(Component c, int vkcode) {
            KeyEvent ke = new KeyEvent(c, KeyEvent.KEY_RELEASED,
                    0, // When timeStamp
                    0, // Modifier
                    vkcode, // Key Code
                    KeyEvent.CHAR_UNDEFINED);  // Key Char
            fakeExecute(ke);
        }

        private static void fakeExecute(AWTEvent ae) {
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ae);
        }
    }

    public static int chmod(String filename, int mode) {
        try {
            Class<?> fspClass = Class.forName("java.util.prefs.FileSystemPreferences");
            Method chmodMethod = fspClass.getDeclaredMethod("chmod", String.class, Integer.TYPE);
            chmodMethod.setAccessible(true);
            return (Integer) chmodMethod.invoke(null, filename, mode);
        } catch (Throwable ex) {
            return -1;
        }
    }

    public static String getPwd() {
        //Systel#4316
        CryptoUtil cputil = new CryptoUtil();

        try {
             /*
            //tomo el dato de entrada MAC de SOM
            //String sMAC = " f8: DC :7a1406:eC";
            String sMAC = " f8:DC:7a1406:ec";
            //Formateo y corrijo un poco el dato de entrada
            sMAC = sMAC.replace(":", "").replace(" ", "").trim();
            sMAC ="F8DC7A3B9568";
            //verifico que la MAC tenga sus 12 caracteres
            if(sMAC.length() == 12){
                //codifico la MAC
                String sKey  = CuoraNeoApp.Hash.md5(sMAC.toUpperCase()); //hago el UPPER "F8DC7A1406EC"
                //sKey =  "43cf9334eb91730ec02c1b72f8439c39"
                //retorno los últimos 10 dígitos (reemplazo 1 x X - 5 x Y - 9 x Z)
                //return sKey.replace("1", "X").replace("5", "Y").replace("9", "Z").toUpperCase().substring(sKey.length()-10, sKey.length());
                //sKey =  "72F843ZC3Z" // así queda la clave FINAL respecto a la MAC de la SOM
                sKey = sKey.replace("1", "X").replace("5", "Y").replace("9", "Z").toUpperCase().substring(sKey.length()-10, sKey.length());
            }else{
                return "ERROR - Formato inválido";
            }
             */
            //String datos  = cputil.encrypt(CuoraNeoApp.key_pwd, String.valueOf(" b4: 2e:99:4b: fd:d0").toUpperCase().replace(":", "").replace(" ", "").trim());
            //String datos2  = cputil.encrypt(CuoraNeoApp.key_pwd, String.valueOf("b42e994bfdd0").toUpperCase() );

            // String datos  = cputil.encrypt(CuoraNeoApp.key_pwd, "/run/media/sda/");
            // String datos1  = cputil.encrypt(CuoraNeoApp.key_pwd, "tar xzf /run/media/sda/upd_neo.tar.gz -C /tmp/");
            // String datos3  = cputil.encrypt(CuoraNeoApp.key_pwd, "openssl enc -d -aes256 -md md5 -k l4p4ss#S1st3l -in /run/media/sda/upd_neo_crypt.tar.gz | tar xz -C /tmp/");
            return cputil.decrypt(MainApp.key_pwd, "HKPvb75pu9ArjdUKiXJ+Wg==");
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        } finally {
            cputil = null;
        }
    }
}
