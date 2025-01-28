/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import com.ucod.lang.LocaleUtil;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author diego
 */
public class FormatSelector implements Runnable {

    private final String ip;
    private JProgressBar pb;
    private JLabel lblTime;
    private boolean bImportOnlyPrices;
    private boolean bImportFileUpdate;
CustomLogger customLogger = CustomLogger.getInstance();
    FormatSelector(String ip, JProgressBar pb, JLabel lblTime, boolean bImportOnlyPrices, boolean bImportFileUpdate) {
        this.ip = ip;
        this.pb = pb;
        this.lblTime = lblTime;
        this.bImportOnlyPrices = bImportOnlyPrices;
        this.bImportFileUpdate = bImportFileUpdate;
        // this.pb.setValue(20);
    }

    // public static StringBuffer sbIP = new StringBuffer();
    /**
     * @param args the command line arguments
     */
//    public void main(String ip) {
//        // try {
//        FileLoader loader;
//        if (MainApp.ping.isReachable2(ip)) {
//            loader = null;
//            loader = new FileLoader(getCon(ip));
//            if (loader.loadCSVPostQendra(ConfigProperties.ConfigFile.getFilePath(), "FILE", false)) {
//                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip);
//            } else {
//                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
//            }
//        } else {
//            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("No importo el archivo a:") + ip);
//        }
////        } catch (Exception e) {
////            customLogger.writeLog(MainApp.ERROR, e.toString());
////        }
//    }
    private Connection getCon(String ip) {
        Connection connection = null;
        String driver = "org.postgresql.Driver"; // el nombre de nuestro driver Postgres.
        String connectString = "jdbc:postgresql://" + ip + ":5432/cuora"; // llamamos nuestra bd
        String user = "systel"; // usuario postgres
        String password = "Systel#4316"; // no tiene password nuestra bd.

        try {
            Class.forName(driver);
//Hacemos la coneccion.
            connection = DriverManager.getConnection(connectString, user, password);
//Si la conexion fue realizada con exito, muestra el sgte mensaje.
            customLogger.writeLog(MainApp.INFO, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("CONEXION A LA BASE DE DATOS EJEMPLO REALIZADA CON EXITO! "));
//Cerramos la conexion
//            connection.close();
        } //Si se produce una Excepcion y no nos podemos conectar, muestra el sgte. mensaje.
        catch (SQLException e) {
            customLogger.writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("SE HA PRODUCIDO UN ERROR EN LA CONEXION A LA BASE DE DATOS EJEMPLO! "));
        } catch (ClassNotFoundException ex) {
            customLogger.writeLog(MainApp.ERROR, ip, ex.toString());
        }

        return connection;
    }

    @Override
    public void run() {
        FileLoader loader;
        File fileNovedades = null;
        File filefull = null;

        if (ip.isEmpty()) {
            return;
        }

        if (MainApp.ping.isReachable2(ip)) {
            try {

                fileNovedades = new File(ConfigProperties.ConfigFile.getFilePathNovedades());
                if (!fileNovedades.exists()
                        && bImportFileUpdate
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_ERPtoMGV.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.SYSTEL.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ANONIMA.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ELEVENTAS.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.MY_BUSINESS_POS.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.HANBAI_KATANA.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.SYSTELRDC429.toString())) {
                    customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("ATENCIÓN... No se encuentra el archivo a importar"));
                    fileNovedades = null;
                    return;
                }
                filefull = new File(ConfigProperties.ConfigFile.getFilePath());
                if (!filefull.exists() && !bImportFileUpdate
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.IMP_DB.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV_DEPTO.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.SYSTELRDC429.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.SYSTEL.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ELEVENTAS.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.MY_BUSINESS_POS.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_ERPtoMGV.toString())
                        && !ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.HANBAI_KATANA.toString())) {
                    customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("ATENCIÓN... No se encuentra el archivo a importar"));
                    filefull = null;
                    return;
                }

                loader = null;
                loader = new FileLoader(getCon(ip), pb, lblTime);

//FIRES
                if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.FIRES.toString())) {

                    if (!ConfigProperties.ConfigFile.getFilePathNovedades().trim().isEmpty()) {
                        if (fileNovedades.exists() && bImportFileUpdate) {
                            //NOVEDADES
                            if (loader.loadCSVFires(ConfigProperties.ConfigFile.getFilePathNovedades(), "FILE", false, ip, bImportOnlyPrices)) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());

                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        } else {
                            //FULL
                            if (loader.loadCSVFires(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices)) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        }
                    } else {
                        //FULL
                        if (loader.loadCSVFires(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices)) {
                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }

//MT8450
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.MT8450.toString())) {
                    if (fileNovedades.exists() && bImportFileUpdate) {

                        //NOVEDADES
                        if (loader.loadCSVMT8450(ConfigProperties.ConfigFile.getFilePathNovedades(), "FILE", false, ip, bImportOnlyPrices)) {
                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    } else {
                        //FULL
                        if (loader.loadCSVMT8450(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices)) {
                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }
//INTELLIBUILD
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.INTELLIBUILD.toString())) {

                    if (!ConfigProperties.ConfigFile.getFilePathNovedades().trim().isEmpty()) {

                        if (fileNovedades.exists() && bImportFileUpdate) {

                            //NOVEDADES
                            if (loader.loadCSVIntellibuildNovedades(ConfigProperties.ConfigFile.getFilePathNovedades(), "FILE", false, ip, bImportOnlyPrices)) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        } else {
                            //FULL
                            if (loader.loadCSVIntellibuild(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices)) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        }
                    } else {
                        //FULL
                        if (loader.loadCSVIntellibuild(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices)) {
                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }
// MGV                  
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV.toString())) {

                    ChooseDevices.bImportUpdates = true;

                    if (!ConfigProperties.ConfigFile.getFilePathNovedades().trim().isEmpty()) {

                        if (fileNovedades.exists()) {
                            //NOVEDADES ITENSMGV
                            if (loader.loadCSVSDR(ConfigProperties.ConfigFile.getFilePathNovedades(), "FILE", false, ip, bImportOnlyPrices)) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        }
                    } else {
                        customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                    }
// MGB buy Depto
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV_DEPTO.toString())) {

                    ChooseDevices.bImportUpdates = true;

                    if (!ConfigProperties.ConfigFile.getFilePathNovedades().trim().isEmpty()) {

                        if (fileNovedades.exists()) {
                            if (ip.equalsIgnoreCase("localhost")) {
                                
                                if (loader.loadCSVSDR(ConfigProperties.ConfigFile.getFilePathNovedades(), "FILE", false, ip, bImportOnlyPrices)) {
                                    customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                    if (MainApp.bRunByCommandLine) {
                                        System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                    }
                                } else {
                                    customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                                }
                            } else {
//                                System.out.println("ram_START loadMGVDepto: "+ ip); 
//MemoryUsage.getRAM(); 
                                if (loader.loadMGVDepto(false, ip)) {
                                    customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                    if (MainApp.bRunByCommandLine) {
                                        System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                    }
                                } else {
                                    customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                                }
                                                                
//MemoryUsage.getRAM();
//System.out.println("ram_FINISH loadMGVDepto: "+ ip); 
                            }
                        }
                    } else {
                        customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                    }
// RETAGUARDA
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_ERPtoMGV.toString())) {

                    if (!ConfigProperties.ConfigFile.getFilePath().trim().isEmpty()) {

                        //FULL
                        if (loader.loadCSVIntellibuild(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices)) {
                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }

//
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.IMP_DB.toString())) {

                    if (!ConfigProperties.ConfigFile.getFilePathNovedades().trim().isEmpty()) {

                        if (fileNovedades.exists() && bImportFileUpdate) {

                            //NOVEDADES
                            if (loader.loadCSVPVMex(ConfigProperties.ConfigFile.getFilePathNovedades(), "FILE", false, ip, bImportOnlyPrices)) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        } else {
                            //FULL
                            if (loader.loadCSVPVMex(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices)) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        }
                    } else {
                        //FULL
                        if (loader.loadCSVPVMex(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices)) {
                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }
// SYSTEL RDC429 para reimportar tabla de info nutri de brasil
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.SYSTELRDC429.toString())) {

                    //FULL
                    if (loader.loadCSV(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices,
                            ConfigProperties.ConfigFile.getFiletype(), ConfigProperties.ConfigFile.getFilePathRangos())) {
                        customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                        if (MainApp.bRunByCommandLine) {
                            System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                        }
                    } else {
                        customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                    }

// La anonima
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ANONIMA.toString())) {

                    //FULL
                    if (loader.loadCSV(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices,
                            ConfigProperties.ConfigFile.getFiletype(), ConfigProperties.ConfigFile.getFilePathRangos())) {
                        customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                        if (MainApp.bRunByCommandLine) {
                            System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                        }
                    } else {
                        customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                    }

//ELEVENTAS
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ELEVENTAS.toString())) {

                    if (!ConfigProperties.ConfigFile.getImportDBType().trim().equalsIgnoreCase("1") // 1 = Firebird
                            && !ConfigProperties.ConfigFile.getImportDBIP().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBName().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBPass().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBPort().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBQuery().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBType().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBUser().trim().isEmpty()) {
                        //NO TENGO PATH SALIR
                        customLogger.writeLog(MainApp.ERROR, ip, "No hay una base Firebird configurada " + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());

                        return;
                    } else {
                        //PROCEDO
                        if (loader.loadEleventas(ConfigProperties.ConfigFile.getImportDBIP().trim(),
                                ConfigProperties.ConfigFile.getImportDBName().trim(),
                                ConfigProperties.ConfigFile.getImportDBPass().trim(),
                                ConfigProperties.ConfigFile.getImportDBPort().trim(),
                                ConfigProperties.ConfigFile.getImportDBQuery().trim(),
                                ConfigProperties.ConfigFile.getImportDBType().trim(),
                                ConfigProperties.ConfigFile.getImportDBUser().trim(),
                                false,
                                ip)) {
                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }
//MY BUSINESS POS
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.MY_BUSINESS_POS.toString())) {
                    /*
                    0    cbDBType.addItem("PostgresSQL");
                    1    cbDBType.addItem("Firebird");
                    2    cbDBType.addItem("Microsoft SQL Server");
                    3    cbDBType.addItem("MySQL");
                    4    cbDBType.addItem("SQLite");
                     */

                    if (!ConfigProperties.ConfigFile.getImportDBType().trim().equalsIgnoreCase("2") // 2 = Microsoft SQL Server
                            && !ConfigProperties.ConfigFile.getImportDBIP().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBName().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBPass().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBPort().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBQuery().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBType().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBUser().trim().isEmpty()) {

                        //NO TENGO PATH SALIR
                        customLogger.writeLog(MainApp.ERROR, ip, "No hay una base Microsoft SQL Server configurada " + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());

                        return;
                    } else {
                        //PROCEDO
                        if (loader.loadMyBusinessPos(ConfigProperties.ConfigFile.getImportDBIP(),
                                ConfigProperties.ConfigFile.getImportDBName(),
                                ConfigProperties.ConfigFile.getImportDBPass(),
                                ConfigProperties.ConfigFile.getImportDBPort(),
                                ConfigProperties.ConfigFile.getImportDBQuery(),
                                ConfigProperties.ConfigFile.getImportDBType(),
                                ConfigProperties.ConfigFile.getImportDBUser(),
                                false,
                                ip)) {

                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }
//HANBAI - KATANA
                } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.HANBAI_KATANA.toString())) {
                    /*
                    0    cbDBType.addItem("PostgresSQL");
                    1    cbDBType.addItem("Firebird");
                    2    cbDBType.addItem("Microsoft SQL Server");
                    3    cbDBType.addItem("MariaDB");
                    */

                    if (!ConfigProperties.ConfigFile.getImportDBType().trim().equalsIgnoreCase("3") // 3 = MariaDB
                            && !ConfigProperties.ConfigFile.getImportDBIP().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBName().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBPass().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBPort().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBQuery().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBType().trim().isEmpty()
                            && !ConfigProperties.ConfigFile.getImportDBUser().trim().isEmpty()) {

                        //NO TENGO PATH SALIR
                        customLogger.writeLog(MainApp.ERROR, ip, "No hay una base MariaDB configurada " + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());

                        return;
                    } else {
                        //PROCEDO
                        if (loader.loadHanbai(ConfigProperties.ConfigFile.getImportDBIP(),
                                ConfigProperties.ConfigFile.getImportDBName(),
                                ConfigProperties.ConfigFile.getImportDBPass(),
                                ConfigProperties.ConfigFile.getImportDBPort(),
                                ConfigProperties.ConfigFile.getImportDBQuery(),
                                ConfigProperties.ConfigFile.getImportDBType(),
                                ConfigProperties.ConfigFile.getImportDBUser(),
                                false,
                                ip)) {

                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }

//QENDRA - SYSTEL
                } else {

                    if (!ConfigProperties.ConfigFile.getFilePathNovedades().trim().isEmpty()) {

                        if (fileNovedades.exists() && bImportFileUpdate) {

                            //NOVEDADES
                            if (loader.loadCSV(ConfigProperties.ConfigFile.getFilePathNovedades(), "FILE", false, ip, bImportOnlyPrices,
                                    ConfigProperties.ConfigFile.getFiletype(), ConfigProperties.ConfigFile.getFilePathRangos())) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        } else {
                            //FULL
                            if (loader.loadCSV(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices,
                                    ConfigProperties.ConfigFile.getFiletype(), ConfigProperties.ConfigFile.getFilePathRangos())) {
                                customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                if (MainApp.bRunByCommandLine) {
                                    System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                }
                            } else {
                                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                            }
                        }
                    } else {
                        //FULL
                        if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.COTO.toString())) {
                            if (!ConfigProperties.ConfigFile.getFileLabelsPath().trim().isEmpty()) {
                                if (loader.loadLabelsData(ConfigProperties.ConfigFile.getFileLabelsPath(), ip)) {
                                    if (MainApp.bRunByCommandLine) {
                                        System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                                    }
                                } else {
                                    customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                                }
                            }
                        }
                        if (loader.loadCSV(ConfigProperties.ConfigFile.getFilePath(), "FILE", false, ip, bImportOnlyPrices,
                                ConfigProperties.ConfigFile.getFiletype(), ConfigProperties.ConfigFile.getFilePathRangos())) {
                            customLogger.writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            if (MainApp.bRunByCommandLine) {
                                System.out.print(LocaleUtil.getMessage("Se importó CORRECTAMENTE el archivo a:") + ip + LocaleUtil.getMessage("tiempo total:") + lblTime.getText());
                            }
                        } else {
                            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("AL importar el archivo a:") + ip);
                        }
                    }
                }
            } catch (Exception e) {
                customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("No importo el archivo a:") + ip);
            } finally {
                fileNovedades = null;
                filefull = null;
                loader = null;
            }
        } else {
            fileNovedades = null;
            filefull = null;
            loader = null;
            customLogger.writeLog(MainApp.ERROR, ip, LocaleUtil.getMessage("No importo el archivo a:") + ip);
        }
    }
}
