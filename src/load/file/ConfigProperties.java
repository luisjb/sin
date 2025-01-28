/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import com.ucod.lang.LocaleUtil;
import java.awt.Dialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Properties;

import static load.file.MainApp.INFO;
import static load.file.MainApp.cputil;
import static load.file.MainApp.isWindows;
import static load.file.MainApp.supportedLocales;

/**
 *
 * @author diego
 */
public class ConfigProperties {

    static InputStream inputStream;

    @SuppressWarnings("static-access")
    public static class ConfigFile {

        public static enum FileType {
            SYSTEL, // 0
            FIRES, // 1
            INTELLIBUILD, // 2
            COTO, // 3
            DIGI, // 4
            SDR, // 5
            IMP_DB, // 6
            BIZERBA, // 7
            SICAR, // 8
            ITENS_MGV, //9
            MT8450, // 10
            ITENS_ERPtoMGV, // 11
            SYSTELRDC429, // 12
            ANONIMA,//13
            ELEVENTAS,//14
            MY_BUSINESS_POS,//15
            ITENS_MGV_DEPTO, //16
            HANBAI_KATANA; //17

            /**
             *
             * @param value
             * @return
             */
            public static FileType fromInteger(int value) {
                switch (value) {
                    case 0:
                        return SYSTEL;
                    case 1:
                        return FIRES;
                    case 2:
                        return INTELLIBUILD;
                    case 3:
                        return COTO;
                    case 4:
                        return DIGI;
                    case 5:
                        return SDR;
                    case 6:
                        return IMP_DB;
                    case 7:
                        return BIZERBA;
                    case 8:
                        return SICAR;
                    case 9:
                        return ITENS_MGV;
                    case 10:
                        return MT8450;
                    case 11:
                        return ITENS_ERPtoMGV;
                    case 12:
                        return SYSTELRDC429;
                    case 13:
                        return ANONIMA;
                    case 14:
                        return ELEVENTAS;
                    case 15:
                        return MY_BUSINESS_POS;
                    case 16:
                        return ITENS_MGV_DEPTO;
                    case 17:
                        return HANBAI_KATANA;
                }
                return null;
            }

        }

        /*public static class FileType {

            public static final int SYSTEL = 0;
            public static final int FIRES = 1;
            public static final int INTELLIBUILD = 2;
            public static final int COTO = 3;
            public static final int DIGI = 4;
            public static final int SDR = 5;
            public static final int IMP_DB = 6;
        }*/
        public static class DBType {

            public static String POSTGRESQL = "0";
            public static String FIREBIRD = "1";
            public static String SQLSERVER = "2";
            public static String MARIADB = "3";
            public static String MySQL = "4";
            public static String SQLITE = "5";

        }

        private static String filePath;
        private static String fileLabelsPath;
        private static String filePathNovedades;
        private static String filePathRangos;
        private static String logpath;
        private static String psqlpath;
        private static String[] ipv4;
        private static String separator;
        private static String importmanual;
        private static String deleteFileInput;
        private static String showmessage;
        private static FileType filetype;
        private static Locale localeLang;

        // Import from DB.
        private static String importDBType;
        private static String importDBName;
        private static String importDBIP;
        private static String importDBPort;
        private static String importDBUser;
        private static String importDBPass;
        private static String importDBQuery;

        // Import from ItensMGV
        /*
         * FilePath will be the same as the General.
         * FilePathNovedades will be the same as the General.
         */
        private static String itensMGVReceingFilePath;
        private static String itensMGVNutInfoFilePath;
        private static String itensMGVExtra2FilePath;
        private static String itensMGVTaraFilePath;
        private static String itensMGVConservacionFilePath;
        private static String itensMGVIngredientsFilePath;
        private static String itensMGVIngredientsFilePath2;

        //Email settings:
        private static boolean sendEmail;
        private static String emailUser;
        private static String emailPass;
        private static String emailHost;
        private static String emailPort;
        private static String[] emailDestinatary;

        //Qendra Handler
        private static boolean qendraIsActive;
        private static boolean qendraDefPath;
        private static String qendraPath, eleventasPath;
        public static final String QENDRA_DEFAULT_PATH = "C:\\Program Files\\Qendra\\qendra.fdb";

        public static boolean useQendraDefPath() {
            return qendraDefPath;
        }

        public static void setUseQendraDefPath(boolean qendraDefPath) {
            ConfigFile.qendraDefPath = qendraDefPath;
        }

        public static boolean useQendra() {
            return qendraIsActive;
        }

        public static void setQendraIsActive(boolean qendraIsActive) {
            ConfigFile.qendraIsActive = qendraIsActive;
        }

        public static String getQendraPath() {
            return qendraPath;
        }

        public static void setQendraPath(String qendraPath) {
            ConfigFile.qendraPath = qendraPath;
        }

        public static String getEleventasPath() {
            return eleventasPath;
        }

        public static void setEleventasPath(String eleventasPath) {
            ConfigFile.eleventasPath = eleventasPath;
        }

        public static String getImportDBType() {
            return importDBType;
        }

        public static void setImportDBType(String importDBType) {
            ConfigFile.importDBType = importDBType;
        }

        public static String getImportDBName() {
            return importDBName;
        }

        public static void setImportDBName(String importDBName) {
            ConfigFile.importDBName = importDBName;
        }

        public static String getImportDBIP() {
            return importDBIP;
        }

        public static void setImportDBIP(String importDBIP) {
            ConfigFile.importDBIP = importDBIP;
        }

        public static String getImportDBPort() {
            return importDBPort;
        }

        public static void setImportDBPort(String importDBPort) {
            ConfigFile.importDBPort = importDBPort;
        }

        public static String getImportDBUser() {
            return importDBUser;
        }

        public static void setImportDBUser(String importDBUser) {
            ConfigFile.importDBUser = importDBUser;
        }

        public static String getImportDBPass() {
            return importDBPass;
        }

        public static void setImportDBPass(String importDBPass) {
            ConfigFile.importDBPass = importDBPass;
        }

        public static String getImportDBQuery() {
            return importDBQuery;
        }

        public static void setImportDBQuery(String importDBQuery) {
            ConfigFile.importDBQuery = importDBQuery;
        }

        public static String getItensMGVReceingFilePath() {
            return itensMGVReceingFilePath;
        }

        public static void setItensMGVReceingFilePath(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.itensMGVReceingFilePath = filePath;
            } else {
                ConfigFile.itensMGVReceingFilePath = "";
            }
        }

        public static String getItensMGVNutInfoFilePath() {
            return itensMGVNutInfoFilePath;
        }

        public static void setItensMGVNutInfoFilePath(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.itensMGVNutInfoFilePath = filePath;
            } else {
                ConfigFile.itensMGVNutInfoFilePath = "";
            }
        }

        public static String getItensMGVTaraFilePath() {
            return itensMGVTaraFilePath;
        }

        public static String getItensMGVExtra2FilePath() {
            return itensMGVExtra2FilePath;
        }

        public static void setItensMGVExtra2FilePath(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.itensMGVExtra2FilePath = filePath;
            } else {
                ConfigFile.itensMGVExtra2FilePath = "";
            }
        }

        /**
         * @return the itensMGVIngredientsFilePath
         */
        public static String getItensMGVIngredientsFilePath() {
            return itensMGVIngredientsFilePath;
        }

        public static void setItensMGVIngredientsFilePath(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.itensMGVIngredientsFilePath = filePath;
            } else {
                ConfigFile.itensMGVIngredientsFilePath = "";
            }
        }

        /**
         * @return the itensMGVIngredientsFilePath2
         */
        public static String getItensMGVIngredientsFilePath2() {
            return itensMGVIngredientsFilePath2;
        }

        public static void setItensMGVIngredientsFilePath2(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.itensMGVIngredientsFilePath2 = filePath;
            } else {
                ConfigFile.itensMGVIngredientsFilePath2 = "";
            }
        }

        public static void setItensMGVTaraFilePath(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.itensMGVTaraFilePath = filePath;
            } else {
                ConfigFile.itensMGVTaraFilePath = "";
            }
        }
        //Scheduler settings:
        private static String schedulerPeriod;
        private static String schedulerTimeValue;
        private static String[] schedulerHours;

        //import mode settings:
        private static String modeArchive; // 0 = novedad / 1 = complet0
        private static String modeImport; // 0 = sólo precios / 1 = todos los datos

        public static String[] getSchedulerHours() {
            return schedulerHours;
        }

        public static void setSchedulerHours(String[] schedulerHours) {
            ConfigFile.schedulerHours = schedulerHours;
        }

        public static String getSchedulerPeriod() {
            return schedulerPeriod;
        }

        public static void setSchedulerPeriod(String schedulerPeriod) {
            ConfigFile.schedulerPeriod = schedulerPeriod;
        }

        public static String getModeArchive() {
            return modeArchive;
        }

        public static void setModeArchive(String modeArchive) {
            ConfigFile.modeArchive = modeArchive;
        }

        public static String getModeImport() {
            return modeImport;
        }

        public static void setModeImport(String modeImport) {
            ConfigFile.modeImport = modeImport;
        }

        public static String getSchedulerTimeValue() {
            return schedulerTimeValue;
        }

        public static void setSchedulerTimeValue(String schedulerTimeValue) {
            ConfigFile.schedulerTimeValue = schedulerTimeValue;
        }

        public static String getLogPath() {
            return logpath;
        }

        public static void setLogPath(String logpath) {
            ConfigFile.logpath = logpath;
        }

        public static String getPsqlPath() {
            return psqlpath;
        }

        public static void setPsqlPath(String psqlpath) {
            ConfigFile.psqlpath = psqlpath;
        }

        public static String getShowmessage() {
            return showmessage;
        }

        public static void setShowmessage(String showmessage) {
            ConfigFile.showmessage = showmessage;
        }

        public static String getImportmanual() {
            return importmanual;
        }

        public static void setImportmanual(String importmanual) {
            ConfigFile.importmanual = importmanual;
        }

        public static String getDeleteFileInput() {
            return deleteFileInput;
        }

        public static void setDeleteFileInput(String deleteFileInput) {
            ConfigFile.deleteFileInput = deleteFileInput;
        }

        public static String[] getIpv4() {
            return ipv4;
        }

        public static void setIpv4(String ipv4) {

            ConfigFile.ipv4 = ipv4.split("\\;");
        }

        public static String getSeparator() {
            return separator;
        }

        public static void setSeparator(String separator) {
            ConfigFile.separator = separator;
        }

        public static String getFilePath() {
            return filePath;
        }

        public static void setFilePath(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.filePath = filePath;
            } else {
                ConfigFile.filePath = "";
            }
        }

        public static String getFileLabelsPath() {
            return fileLabelsPath;
        }

        public static void setFileLabelsPath(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.fileLabelsPath = filePath;
            } else {
                ConfigFile.fileLabelsPath = "";
            }
        }

        public static Locale getLocaleLang() {
            return localeLang;
        }

        public static void setLocaleLang(String loclang) {

            for (Locale sl : supportedLocales) {
                if ((sl.getLanguage() + "_" + sl.getCountry()).equalsIgnoreCase(loclang)) {
                    ConfigFile.localeLang = sl;
                    return;
                }
            }

            ConfigFile.localeLang = supportedLocales[3];

        }

        public static String getFilePathNovedades() {
            return filePathNovedades;
        }

        public static void setFilePathNovedades(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.filePathNovedades = filePath;
            } else {
                ConfigFile.filePathNovedades = "";
            }
        }

        public static String getItensMGVConservacionFilePath() {
            return itensMGVConservacionFilePath;
        }

        public static void setItensMGVConservacionFilePath(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.itensMGVConservacionFilePath = filePath;
            } else {
                ConfigFile.itensMGVConservacionFilePath = "";
            }
        }

        public static String getFilePathRangos() {
            return filePathRangos;
        }

        public static void setFilePathRangos(String filePath, boolean isVisible) {
            if (isVisible) {
                ConfigFile.filePathRangos = filePath;
            } else {
                ConfigFile.filePathRangos = "";
            }
        }

        public static FileType getFiletype() {
            return filetype;
        }

        public static void setFiletype(String filetype) {
            ConfigFile.filetype = ConfigFile.FileType.fromInteger(Integer.parseInt(filetype));
        }

        public static boolean SendEmail() {
            return sendEmail;
        }

        public static void setSendEmail(boolean sendEmail) {
            ConfigFile.sendEmail = sendEmail;
        }

        public static String getEmailUser() {
            return emailUser;
        }

        public static void setEmailUser(String emailUser) {
            ConfigFile.emailUser = emailUser;
        }

        public static String getEmailPass() {
            return emailPass;
        }

        public static void setEmailPass(String emailPass) {
            ConfigFile.emailPass = emailPass;
        }

        public static String getEmailHost() {
            return emailHost;
        }

        public static void setEmailHost(String emailHost) {
            ConfigFile.emailHost = emailHost;
        }

        public static String getEmailPort() {
            return emailPort;
        }

        public static void setEmailPort(String emailPort) {
            ConfigFile.emailPort = emailPort;
        }

        public static String[] getEmailDestinatary() {
            return emailDestinatary;
        }

        public static void setEmailDestinatary(String[] emailDestinatary) {
            ConfigFile.emailDestinatary = emailDestinatary;
        }

    }

    public static void getPropValues() throws IOException {
        File file = null;
        InputStreamReader reader = null;
        CustomLogger customLogger = CustomLogger.getInstance();
        try {
            Properties prop = new Properties();
            String propFileName = "config_loadfile.properties";

            LocaleUtil localeUtil = new LocaleUtil(supportedLocales, "load/file/Bundle");
            if (isWindows()) {
                file = new File("C:" + File.separator + "SYSTEL" + File.separator + "Importador" + File.separator + "config_loadfile.properties");
            } else {
                file = new File(File.separator + "tmp" + File.separator + "config_loadfile.properties");
            }

            if (!file.exists()) {
                // file.createNewFile(); // create your file on the file system
                setPropValuesDefault();

                if (isWindows()) {
                    file = new File("C:" + File.separator + "SYSTEL" + File.separator + "Importador" + File.separator + "config_loadfile.properties");
                } else {
                    file = new File(File.separator + "tmp" + File.separator + "config_loadfile.properties");
                }
            }
            
            inputStream = new FileInputStream(file);
            if (isWindows()) {
                reader = new InputStreamReader(inputStream, "ISO-8859-1");
            } else {
                reader = new InputStreamReader(inputStream, "UTF-8");
            }

            if (reader != null) {
                prop.load(reader);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            // get the property value and print it out
            ConfigFile.setLocaleLang((prop.getProperty("lang") != null) ? prop.getProperty("lang") : getPropValuesDefault("lang"));
            ConfigFile.setFilePath((prop.getProperty("importpath") != null) ? prop.getProperty("importpath") : getPropValuesDefault("importpath"), true);
            ConfigFile.setFileLabelsPath((prop.getProperty("importlabelspath") != null) ? prop.getProperty("importlabelspath") : getPropValuesDefault("importlabelspath"), true);
            ConfigFile.setFilePathNovedades((prop.getProperty("importpathnovedades") != null) ? prop.getProperty("importpathnovedades") : getPropValuesDefault("importpathnovedades"), true);
            ConfigFile.setFilePathRangos((prop.getProperty("importpathrangos") != null) ? prop.getProperty("importpathrangos") : getPropValuesDefault("importpathrangos"), true);
            ConfigFile.setLogPath((prop.getProperty("logpath") != null) ? prop.getProperty("logpath") : getPropValuesDefault("logpath"));
            ConfigFile.setPsqlPath((prop.getProperty("psqlpath") != null) ? prop.getProperty("psqlpath") : getPropValuesDefault("psqlpath"));
            ConfigFile.setSeparator((prop.getProperty("separator") != null) ? prop.getProperty("separator") : getPropValuesDefault("separator"));
            ConfigFile.setImportmanual((prop.getProperty("importmanual") != null) ? prop.getProperty("importmanual") : getPropValuesDefault("importmanual"));
            ConfigFile.setIpv4((prop.getProperty("ipv4") != null) ? prop.getProperty("ipv4") : getPropValuesDefault("ipv4"));
            ConfigFile.setShowmessage((prop.getProperty("showmessage") != null) ? prop.getProperty("showmessage") : getPropValuesDefault("showmessage"));
            ConfigFile.setFiletype((prop.getProperty("filetype") != null) ? prop.getProperty("filetype") : getPropValuesDefault("filetype"));
            ConfigFile.setSendEmail((prop.getProperty("sendEmail") != null) ? prop.getProperty("sendEmail").equals("1") : getPropValuesDefault("sendEmail").equals("0"));
            ConfigFile.setEmailUser((prop.getProperty("emailUser") != null) ? prop.getProperty("emailUser") : getPropValuesDefault("emailUser"));
            ConfigFile.setEmailPass((prop.getProperty("emailPass") != null) ? cputil.decrypt(prop.getProperty("emailPass")) : getPropValuesDefault("emailPass"));
            ConfigFile.setEmailHost((prop.getProperty("emailHost") != null) ? prop.getProperty("emailHost") : getPropValuesDefault("emailHost"));
            ConfigFile.setEmailPort((prop.getProperty("emailPort") != null) ? prop.getProperty("emailPort") : getPropValuesDefault("emailPort"));
            ConfigFile.setEmailDestinatary((prop.getProperty("emailDestinatary") != null) ? prop.getProperty("emailDestinatary").split("\\;") : getPropValuesDefault("emailDestinatary").split("\\;"));
            ConfigFile.setSchedulerPeriod((prop.getProperty("schedulerPeriod") != null) ? prop.getProperty("schedulerPeriod") : getPropValuesDefault("schedulerPeriod"));
            ConfigFile.setSchedulerTimeValue((prop.getProperty("schedulerTimeValue") != null) ? prop.getProperty("schedulerTimeValue") : getPropValuesDefault("schedulerTimeValue"));
            ConfigFile.setSchedulerHours((prop.getProperty("schedulerHours") != null) ? prop.getProperty("schedulerHours").split("\\;") : getPropValuesDefault("schedulerHours").split("\\;"));
            ConfigFile.setModeArchive((prop.getProperty("modeArchive") != null) ? prop.getProperty("modeArchive") : getPropValuesDefault("modeArchive"));
            ConfigFile.setModeImport((prop.getProperty("modeImport") != null) ? prop.getProperty("modeImport") : getPropValuesDefault("modeImport"));
            ConfigFile.setImportDBType((prop.getProperty("importDBType") != null) ? prop.getProperty("importDBType") : getPropValuesDefault("importDBType"));
            ConfigFile.setImportDBName((prop.getProperty("importDBName") != null) ? prop.getProperty("importDBName") : getPropValuesDefault("importDBName"));
            ConfigFile.setImportDBIP((prop.getProperty("importDBIP") != null) ? prop.getProperty("importDBIP") : getPropValuesDefault("importDBIP"));
            ConfigFile.setImportDBPort((prop.getProperty("importDBPort") != null) ? prop.getProperty("importDBPort") : getPropValuesDefault("importDBPort"));
            ConfigFile.setImportDBUser((prop.getProperty("importDBUser") != null) ? prop.getProperty("importDBUser") : getPropValuesDefault("importDBUser"));
            ConfigFile.setImportDBPass((prop.getProperty("importDBPass") != null) ? cputil.decrypt(prop.getProperty("importDBPass")) : getPropValuesDefault("importDBPass"));
            ConfigFile.setImportDBQuery((prop.getProperty("importDBQuery") != null) ? prop.getProperty("importDBQuery") : getPropValuesDefault("importDBQuery"));
            //Added by GCastillo
            ConfigFile.setQendraIsActive((prop.getProperty("qendraIsActive") != null) ? prop.getProperty("qendraIsActive").equals("1") : getPropValuesDefault("qendraIsActive").equals("0"));
            ConfigFile.setUseQendraDefPath((prop.getProperty("qendraUseDefaultPath") != null) ? prop.getProperty("qendraUseDefaultPath").equals("1") : getPropValuesDefault("qendraUseDefaultPath").equals("1"));
            ConfigFile.setQendraPath((prop.getProperty("qendraPath") != null) ? prop.getProperty("qendraPath") : getPropValuesDefault("qendraPath"));
            ConfigFile.setDeleteFileInput((prop.getProperty("deletefileinput") != null) ? prop.getProperty("deletefileinput") : getPropValuesDefault("deletefileinput"));
            ConfigFile.setItensMGVNutInfoFilePath((prop.getProperty("itensMGVNutInfoPath") != null) ? prop.getProperty("itensMGVNutInfoPath") : getPropValuesDefault("itensMGVNutInfoPath"), true);
            ConfigFile.setItensMGVReceingFilePath((prop.getProperty("itensMGVReceingPath") != null) ? prop.getProperty("itensMGVReceingPath") : getPropValuesDefault("itensMGVReceingPath"), true);
            ConfigFile.setItensMGVTaraFilePath((prop.getProperty("itensMGVTaraPath") != null) ? prop.getProperty("itensMGVTaraPath") : getPropValuesDefault("itensMGVTaraPath"), true);
            ConfigFile.setItensMGVConservacionFilePath((prop.getProperty("itensMGVConservacionPath") != null) ? prop.getProperty("itensMGVConservacionPath") : getPropValuesDefault("itensMGVConservacionPath"), true);
            ConfigFile.setItensMGVExtra2FilePath((prop.getProperty("ItensMGVExtra2FilePath") != null) ? prop.getProperty("ItensMGVExtra2FilePath") : getPropValuesDefault("ItensMGVExtra2FilePath"), true);
            ConfigFile.setItensMGVIngredientsFilePath((prop.getProperty("ItensMGVIngredientsFilePath") != null) ? prop.getProperty("ItensMGVIngredientsFilePath") : getPropValuesDefault("ItensMGVIngredientsFilePath"), true);
            ConfigFile.setItensMGVIngredientsFilePath2((prop.getProperty("ItensMGVIngredientsFilePath2") != null) ? prop.getProperty("ItensMGVIngredientsFilePath2") : getPropValuesDefault("ItensMGVIngredientsFilePath2"), true);
            //Added by dbelatti
            ConfigFile.setEleventasPath((prop.getProperty("eleventasPath") != null) ? prop.getProperty("eleventasPath") : getPropValuesDefault("eleventasPath"));

            if (ConfigFile.getLocaleLang() == null) {
                LocaleUtil.setCurrentLocaleByIndex(1);
            } else {
                LocaleUtil.setCurrentLocale(ConfigFile.getLocaleLang());
            }

//            String company1 = prop.getProperty("company1");
//            String company2 = prop.getProperty("company2");
//            String company3 = prop.getProperty("company3");
        } catch (Exception e) {

            setPropValuesDefault();
            if (!MainApp.bRunByCommandLine) {
                ShowConfigs sc = new ShowConfigs();

//            sc.setModal(true);
//            sc.setAlwaysOnTop(true);
                sc.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                sc.pack();
                sc.setVisible(true);
                sc.dispose();
                sc = null;
            }
            System.out.println("Exception: " + e);
        } finally {
            reader.close();
            inputStream.close();
            file = null;
        }
    }

    public void setPropValues() throws IOException {
        CustomLogger customLogger = CustomLogger.getInstance();
        File fconfigFile = null;
        if (isWindows()) {
            fconfigFile = new File("C:" + File.separator + "SYSTEL" + File.separator + "Importador" + File.separator + "config_loadfile.properties");
        } else {
            fconfigFile = new File(File.separator + "tmp" + File.separator + "config_loadfile.properties");
        }
        // File configFile = new File(System.getProperty("user.home") + File.separator + "config_loadfile.properties");

        System.out.println("setPropValues():" + fconfigFile.getAbsolutePath());
        customLogger.getInstance().writeLog(INFO, "", "setPropValues():" + fconfigFile.getAbsolutePath());

        try {
            Properties props = new Properties();
            props.setProperty("lang", ConfigFile.getLocaleLang().toString());
            props.setProperty("importpath", ConfigFile.getFilePath().trim());
            props.setProperty("importlabelspath", ConfigFile.getFileLabelsPath().trim());
            props.setProperty("importpathnovedades", ConfigFile.getFilePathNovedades().trim());
            props.setProperty("importpathrangos", ConfigFile.getFilePathRangos().trim());
            props.setProperty("logpath", ConfigFile.getLogPath().trim());
            props.setProperty("psqlpath", ConfigFile.getPsqlPath().trim());
            props.setProperty("separator", ConfigFile.getSeparator().trim());
            props.setProperty("importmanual", ConfigFile.getImportmanual().trim());
            props.setProperty("deletefileinput", ConfigFile.getDeleteFileInput().trim());
            props.setProperty("ipv4", String.join(";", ConfigFile.getIpv4()));
            props.setProperty("showmessage", ConfigFile.getShowmessage());
            props.setProperty("filetype", Integer.toString(ConfigFile.getFiletype().ordinal()));
            props.setProperty("sendEmail", ConfigFile.SendEmail() ? "1" : "0");
            props.setProperty("emailUser", ConfigFile.getEmailUser());
            props.setProperty("emailPass", cputil.encrypt(ConfigFile.getEmailPass()));
            props.setProperty("emailHost", ConfigFile.getEmailHost());
            props.setProperty("emailPort", ConfigFile.getEmailPort());
            props.setProperty("emailDestinatary", String.join(";", ConfigFile.getEmailDestinatary()));
            props.setProperty("schedulerPeriod", ConfigFile.getSchedulerPeriod());
            props.setProperty("schedulerTimeValue", ConfigFile.getSchedulerTimeValue());
            props.setProperty("schedulerHours", String.join(";", ConfigFile.getSchedulerHours()));
            props.setProperty("modeArchive", ChooseDevices.getSelectedOptArchivoCompleto() ? "1" : "0");
            props.setProperty("modeImport", ChooseDevices.getSelectedOptTodosLosDatos() ? "1" : "0");
            props.setProperty("importDBType", ConfigFile.getImportDBType());
            props.setProperty("importDBName", ConfigFile.getImportDBName());
            props.setProperty("importDBIP", ConfigFile.getImportDBIP());
            props.setProperty("importDBPort", ConfigFile.getImportDBPort());
            props.setProperty("importDBUser", ConfigFile.getImportDBUser());
            props.setProperty("importDBPass", cputil.encrypt(ConfigFile.getImportDBPass()));
            props.setProperty("importDBQuery", ConfigFile.getImportDBQuery());
            //Added by Gcastillo
            props.setProperty("qendraIsActive", ConfigFile.useQendra() ? "1" : "0");
            props.setProperty("qendraUseDefaultPath", ConfigFile.useQendraDefPath() ? "1" : "0");
            props.setProperty("qendraPath", ConfigFile.getQendraPath());
            props.setProperty("itensMGVReceingPath", ConfigFile.getItensMGVReceingFilePath());
            props.setProperty("itensMGVNutInfoPath", ConfigFile.getItensMGVNutInfoFilePath());
            props.setProperty("itensMGVTaraPath", ConfigFile.getItensMGVTaraFilePath());
            props.setProperty("itensMGVConservacionPath", ConfigFile.getItensMGVConservacionFilePath());
            props.setProperty("ItensMGVExtra2FilePath", ConfigFile.getItensMGVExtra2FilePath());
            props.setProperty("ItensMGVIngredientsFilePath", ConfigFile.getItensMGVIngredientsFilePath());
            props.setProperty("ItensMGVIngredientsFilePath2", ConfigFile.getItensMGVIngredientsFilePath2());
            //Added by dbelatti
            props.setProperty("eleventasPath", ConfigFile.getEleventasPath());

            FileWriter writer = new FileWriter(fconfigFile);
            props.store(writer, "save settings");
            writer.close();
            getPropValues();
        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
        } catch (Exception ex) {
            setPropValuesDefault();
            if (!MainApp.bRunByCommandLine) {
                ShowConfigs sc = new ShowConfigs();

                sc.setModal(true);
                sc.setAlwaysOnTop(true);
                sc.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                sc.pack();
                sc.setVisible(true);
                sc.dispose();
                sc = null;
            }
            System.out.println("Exception: " + ex);
        } finally {
            fconfigFile = null;
        }
    }

    public static void setPropValuesDefault() throws IOException {
        CustomLogger customLogger = CustomLogger.getInstance();
        //System.getProperty("user.home")
        try {
            File checkConfigFile = new File(System.getProperty("user.home") + File.separator + "config_loadfile.properties");

            if (checkConfigFile.exists()) {
                //move checkConfigFile
                //configFile.delete();
                if (isWindows()) {
                    Files.move(Paths.get(checkConfigFile.getAbsolutePath()), Paths.get("C:" + File.separator + "SYSTEL" + File.separator + "Importador" + File.separator + "config_loadfile.properties"), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.move(Paths.get(checkConfigFile.getAbsolutePath()), Paths.get(File.separator + "tmp" + File.separator + "config_loadfile.properties"), StandardCopyOption.REPLACE_EXISTING);
                }

            }
            checkConfigFile = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        File configFile = null;
        if (isWindows()) {
            configFile = new File("C:" + File.separator + "SYSTEL" + File.separator + "Importador" + File.separator + "config_loadfile.properties");
        } else {
            configFile = new File(File.separator + "tmp" + File.separator + "config_loadfile.properties");
        }
        System.out.println("setPropValuesDefault():" + configFile.getAbsolutePath());
        customLogger.getInstance().writeLog(INFO, "", "setPropValuesDefault():" + configFile.getAbsolutePath());

        if (!configFile.exists()) {

            configFile.createNewFile();
            try {
                Properties props = new Properties();
                props.setProperty("lang", "es_AR");
                props.setProperty("importpath", "");
                props.setProperty("importlabelspath", "");
                props.setProperty("importpathnovedades", "");
                props.setProperty("importpathrangos", "");
                props.setProperty("logpath", "");
                props.setProperty("psqlpath", "C:\\Program Files\\PostgreSQL\\9.4\\bin\\psql.exe");
                props.setProperty("separator", ";");
                props.setProperty("importmanual", "0");
                props.setProperty("deletefileinput", "0");
                props.setProperty("ipv4", "");
                props.setProperty("showmessage", "1");
                props.setProperty("filetype", Integer.toString(ConfigProperties.ConfigFile.FileType.SYSTEL.ordinal()));
                props.setProperty("sendEmail", "0");
                props.setProperty("emailUser", "");
                props.setProperty("emailPass", "");
                props.setProperty("emailHost", "");
                props.setProperty("emailPort", "");
                props.setProperty("emailDestinatary", "");
                props.setProperty("schedulerPeriod", "0");
                props.setProperty("schedulerTimeValue", "10");
                props.setProperty("schedulerHours", "");
                props.setProperty("modeArchive", "1");
                props.setProperty("modeImport", "1");
                props.setProperty("importDBType", ConfigProperties.ConfigFile.DBType.POSTGRESQL);
                props.setProperty("importDBName", "");
                props.setProperty("importDBIP", "");
                props.setProperty("importDBPort", "");
                props.setProperty("importDBUser", "");
                props.setProperty("importDBPass", "");
                props.setProperty("importDBQuery", "");
                props.setProperty("qendraIsActive", "0");
                props.setProperty("qendraUseDefaultPath", "1");
                props.setProperty("qendraPath", "C:\\Program Files\\Qendra\\qendra.fdb");
                props.setProperty("itensMGVReceingPath", "");
                props.setProperty("itensMGVNutInfoPath", "");
                props.setProperty("itensMGVTaraPath", "");
                props.setProperty("itensMGVConservacionPath", "");
                props.setProperty("ItensMGVExtra2FilePath", "");
                props.setProperty("ItensMGVIngredientsFilePath", "");
                props.setProperty("ItensMGVIngredientsFilePath2", "");
                props.setProperty("eleventasPath", "");

                FileWriter writer = new FileWriter(configFile);
                props.store(writer, "save settings");
                writer.close();

            } catch (FileNotFoundException ex) {
                // file does not exist
            } catch (IOException ex) {
                // I/O error
            } finally {
                configFile = null;
            }
        }
    }

    public static String getPropValuesDefault(String sProp) throws IOException {

        switch (sProp) {
            case "lang":
                return "es_AR";
            case "psqlpath":
                return "C:\\Program Files\\PostgreSQL\\9.4\\bin\\psql.exe";
            case "separator":
                return ";";
            case "importmanual":
                return "0";
            case "deletefileinput":
                return "0";
            case "showmessage":
                return "1";
            case "filetype":
                return Integer.toString(ConfigProperties.ConfigFile.FileType.SYSTEL.ordinal());
            case "sendEmail":
                return "0";
            case "schedulerPeriod":
                return "0";
            case "schedulerTimeValue":
                return "10";
            case "modeArchive":
                return "1";
            case "modeImport":
                return "1";
            case "importDBType":
                return ConfigProperties.ConfigFile.DBType.POSTGRESQL;
            case "qendraIsActive":
                return "0";
            case "qendraUseDefaultPath":
                return "1";
            default:
                return "";
        }

    }

}
