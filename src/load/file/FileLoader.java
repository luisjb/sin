/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import com.opencsv.*;
import com.ucod.lang.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.util.*;
import javax.swing.*;

import static load.file.CharsetDetector.*;

import load.file.ConfigProperties.ConfigFile;

import static load.file.ConfigProperties.ConfigFile.FileType.COTO;

import load.file.EleventasFirebirdDBConnector.EleventasProducto;
import load.file.HanbaiMariadbConnector.HanbaiProducto;
import load.file.MyBusinessPosSQLServerConnector.MyBusinessPosProducto;
import load.file.SystelDBConnector.SystelConserva;
import load.file.SystelDBConnector.SystelExtra1;
import load.file.SystelDBConnector.SystelExtra2;
import load.file.SystelDBConnector.SystelIngredient;
import load.file.SystelDBConnector.SystelNutInfo;
import load.file.SystelDBConnector.SystelProducto;
import load.file.SystelDBConnector.SystelTara;
import org.apache.commons.lang.StringUtils;
//import static load.file.ConfigProperties.ConfigFile.FileType.SYSTEL;
import org.joda.time.*;
import org.joda.time.Duration;

/**
 *
 * @author diego
 */
public class FileLoader {

    CustomLogger customLogger = CustomLogger.getInstance();
    //public static String ip;
//    private static final String SQL_INSERT = "INSERT INTO ${table}(${keys}) VALUES(${values})";
//    private static final String TABLE_REGEX = "\\$\\{table\\}";
//    private static final String KEYS_REGEX = "\\$\\{keys\\}";
    private static final String VALUES_REGEX = "\\$\\{values\\}";

    private static final String SQL_DEPARTMENT = "select merge_department(${values})";
    private static final String SQL_GROUP = "select merge_group(${values})";
    private static final String SQL_MGV_DEPARTMENT = "select merge_mgv_department(${values})";
    private static final String SQL_DEPARTMENT_EXIST = "select * from department where UPPER(name) = ${values}";
    private static final String SQL_GROUP_EXIST = "select * from main_group where UPPER(name) = ${values}";
    private static final String SQL_PRODUCT = "select merge_product(${values})";
    private static final String SQL_PRODUCT_COTO = "select merge_product_coto(${values})";
    private static final String SQL_PRODUCT_EXIST = "select 1 from product where product_id = ${values}";
    private static final String SQL_PRODUCT_PRICE = "select merge_product_price(${values})";
    private static final String SQL_NUT_INFO = "select merge_nut_info(${values})";
    private static final String SQL_RECEING = "UPDATE product SET extra_field1 = ? WHERE product_id = ?";
    private static final String SQL_TARA = "UPDATE product SET tare = ? WHERE product_id = ?";
    private static final String SQL_DISCOUNT = "select merge_discount(${values})";
    private static final String SQL_DISCOUNT_DELETE_ALL = "select delete_discount()";
    private static final String SQL_LABELS = "select merge_label_format(${values})";
    private static final String SQL_CONSERVACION = "UPDATE product SET preservation_info = ? WHERE product_id = ?";
    private static final String SQL_EXTRA2 = "UPDATE product SET extra_field2 = ? WHERE product_id = ?";
    private static final String SQL_INGREDIENTS = "UPDATE product SET ingredients = ? WHERE product_id = ?";
    private static final String SQL_INGREDIENTS_ADD = "UPDATE product SET ingredients = COALESCE(ingredients, '') || ? WHERE product_id = ?";

    private static final String SQL_DEPARTMENT_UNLOG_ON = "ALTER TABLE public.department SET UNLOGGED";
    private static final String SQL_PRODUCT_UNLOG_ON = "ALTER TABLE public.product SET UNLOGGED";
    private static final String SQL_PRODUCT_PRICE_UNLOG_ON = "ALTER TABLE public.productprice SET UNLOGGED";
    private static final String SQL_NUT_INFO_UNLOG_ON = "ALTER TABLE public.nut_info_el_instance SET UNLOGGED";

    //private static final String SQL_PRODUCT_FIRES = "select merge_product_fires(${values})";
    private static final int FILE_DEPARTMENT_ID = 0;      //    0 ID departamento
    private static final int FILE_DEPARTMENT_NAME = 1;    //    Almacen           ;     1 name departamento
    private static final int FILE_PLU_ID = 2;             //    1;                      2 PLU
    private static final int FILE_PLU_NAME = 3;           //    Activia Bebible Fr;     3 nombre
    private static final int FILE_PLU_ERP_CODE = 4;       //    00001;                  4 codigo_plu ERP
    private static final int FILE_PRICE_LIST_1 = 5;       //    5,00;                   5 lista 1
    private static final int FILE_PRICE_LIST_2 = 6;       //    5,00;                   6 lista 2
    private static final int FILE_PLU_TYPE = 7;           //    Peso/Unidad;            7 tipo producto 0 P - 1 U
    private static final int FILE_PLU_TARE = 8;           //    0;                      8 tara
    private static final int FILE_PLU_USE_BY_DATE = 9;    //    0000;                   9 dias vencimiento
    private static final int FILE_PLU_DESCRIPTION = 10;   //    ;                       10 otros datos
    private static final int FILE_LAST_MODIFICATION = 11; //    13/12/2015 18:37:34;    11 Ultima modificación
    private static final int FILE_USER_MODIFICATION = 12; //    Administrador           12 Modificado por usuario

    //--PARSE QENDRA CSV--//
    private static final int FILE_QENDRA_DEPARTMENT_NAME = 0;                //  Panaderia
    private static final int FILE_QENDRA_PLU_ERP_CODE = 1;                   //  1001
    private static final int FILE_QENDRA_PLU_NAME = 2;                       //  Factura Dulce Cong
    private static final int FILE_QENDRA_PLU_ID = 3;                         //  1001
    private static final int FILE_QENDRA_PRICE_LIST_1 = 4;                   //  0
    private static final int FILE_QENDRA_PRICE_LIST_2 = 5;                   //  0
    private static final int FILE_QENDRA_PLU_TYPE = 6;                       //  CONGELADO / PESO / UNIDAD
    private static final int FILE_QENDRA_PLU_USE_BY_DATE = 7;                //  0
    private static final int FILE_QENDRA_PLU_OTROS = 8;                      //  TEXTO EXTRA
    private static final int FILE_QENDRA_PLU_TARE = 9;                       //  0
    private static final int FILE_QENDRA_PLU_TARE_WATER = 10;                //  0
    private static final int FILE_QENDRA_PLU_ORIGEN_CODE = 11;               //  
    private static final int FILE_QENDRA_PLU_PRESERVATION_CODE = 12;         //  1
    private static final int FILE_QENDRA_PLU_INGREDIENTS_CODE = 13;          //  1
    private static final int FILE_QENDRA_PLU_HAVE_NUTRITIONAL_INFO = 14;     //  S / N
    private static final int FILE_QENDRA_NUTINF_PORTION = 15;                //  50g
    private static final int FILE_QENDRA_NUTINF_CALORIES_PORTION = 16;       //  8400
    private static final int FILE_QENDRA_NUTINF_CARBOHIDRATES = 17;          //  23
    private static final int FILE_QENDRA_NUTINF_PROTEINS = 18;               //  0
    private static final int FILE_QENDRA_NUTINF_FATS_TOTAL = 19;             //  7
    private static final int FILE_QENDRA_NUTINF_FATS_SATURADES = 20;         //  0
    private static final int FILE_QENDRA_NUTINF_FATS_TRANS = 21;             //  0
    private static final int FILE_QENDRA_NUTINF_FIBER = 22;                  //  1
    private static final int FILE_QENDRA_NUTINF_SODIUM = 23;                 //  155
    private static final int FILE_QENDRA_PLU_EAN_CONFIG = 24;                //  G / P / S (Genera / PLU / Sin)
    private static final int FILE_QENDRA_PLU_EAN_DESCRIPTION = 25;           //  AAABBBBCCCC (datos de como está conformado el EAN para el PLU) 
    private static final int FILE_QENDRA_DATE_LAST_MODIFICATION = 26;        //  10/10/2018 09:24:22 a.m.
    private static final int FILE_QENDRA_USER_MODIFICATION = 27;             //  ADMIN
    private long department_id = 0;

    private static final int[] FILE_FIRES_MOS = new int[]{1, 3};  //    "MOS","Mostrador",1,2,0                                         0 ID departamento & 1 name departamento
    private static final int[] FILE_FIRES_COD = new int[]{3, 9};  //    "COD","Código",3,8,0                                            4 codigo_plu ERP
    private static final int[] FILE_FIRES_PLU = new int[]{9, 13};  //    "PLU","PLU",9,12,0                                              2 PLU
    private static final int[] FILE_FIRES_TXT = new int[]{13, 38};  //    "TXT","Texto",13,37,0                                           3 nombre
    private static final int[] FILE_FIRES_PRC = new int[]{38, 45};  //    "PRC","PRECIO",38,44,0                                          5 lista 1 & 6 lista 2
    private static final int[] FILE_FIRES_WGH = new int[]{45, 46};  //    "WGH","Pesado / No Pesado",45,45,0                              7 tipo producto 0=P - 1=U
    private static final int[] FILE_FIRES_CBA = new int[]{46, 58};  //    "CBA","Código de barras",46,57,0                                13 código de barras
    private static final int[] FILE_FIRES_CAD = new int[]{58, 61};  //    "CAD","Caducidad",58,60,0                                       9 dias vencimiento
    private static final int[] FILE_FIRES_TAR = new int[]{61, 66};  //    "TAR","Tara",61,65,0                                            8 tara
    private static final int[] FILE_FIRES_JTEC = new int[]{66, 67}; //    "JTEC","Juego de Tecla en la sección",66,66,0                   N/A
    private static final int[] FILE_FIRES_TEC = new int[]{67, 70};  //    "TEC","Tecla",67,69,0                                           N/A
    private static final int[] FILE_FIRES_IVA = new int[]{70, 72};  //    "IVA","Tipo Iva",70,71,0                                        N/A
    private static final int[] FILE_FIRES_BAJA = new int[]{72, 73}; //    "BAJA","Distinto de 0 Indica si baja de artículo",72,72,0       14 activo 0=Y !0=N

    private Connection connection;
    private char seprator;

    private static ConfigFile.FileType ftype;

    public static ConfigFile.FileType getFtype() {
        return ftype;
    }

    public void setFtype(ConfigFile.FileType ftype) {
        this.ftype = ftype;
    }

    /**
     * Public constructor to build CSVLoader object with Connection details. The
     * connection is closed on success or failure.
     *
     * @param connection
     */
    private JProgressBar pb;
    private JLabel lblTime;

    public FileLoader(Connection connection, JProgressBar pb, JLabel lblTime) {
        this.connection = connection;
        //Set default separator
        this.seprator = ConfigProperties.ConfigFile.getSeparator().charAt(0);
        this.pb = pb;
        this.lblTime = lblTime;

    }

    public boolean loadCSVPostQendra(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices) {

        final String ip = ip2;
        boolean doImportOnlyPrices = bImportOnlyPrices;
        boolean result = true;
        CSVReader csvReader = null;
        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);
            csvReader = new CSVReader(new FileReader(csvFile), this.seprator);

        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

        String query_dpt, query_prod, query_prod_price, query_dep_exist = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
//        query = query.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
//        query = query.replaceFirst(VALUES_REGEX, questionmarks);

        // System.out.println("Query: " + query);
        String[] nextLine;
        Connection con = null;
        PreparedStatement psDpt = null, psDptMax = null, psPlu = null, psPrice = null, psDepExist = null;
        ResultSet rs = null, rsDptMax = null;
        int count = 0;

        con = this.connection;
        try {
            con.setAutoCommit(false);

//-----------------------SQL_DEPARTMENT--------------------
            query_dep_exist = SQL_DEPARTMENT_EXIST;

            questionmarks = "?";
            query_dep_exist = query_dep_exist.replaceFirst(VALUES_REGEX, questionmarks);
            psDepExist = con.prepareStatement(query_dep_exist);

            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

            this.pb.setMaximum((int) (Files.lines(Paths.get(csvFile), Charset.forName("ISO-8859-1")).count() - 1));

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            while ((nextLine = csvReader.readNext()) != null) {
                try {
                    duration = new Duration(timeStart, timeStart.now());

//                 1                 ;     0 ID departamento
//                 Almacen           ;     1 name departamento
//                 1;                      2 PLU
//                 Activia Bebible Fr;     3 nombre
//                 00001;                  4 codigo_plu ERP
//                 5,00;                   5 lista 1
//                 5,00;                   6 lista 2
//                 Peso/Unidad;            7 tipo producto 0 P - 1 U
//                 0;                      8 tara
//                 0000;                   9 dias vencimiento
//                 ;                       10 otros datos
//                 13/12/2015 18:37:34;    11 Ultima modificación
//                 Administrador           12 Modificado por usuario
                    if (null != nextLine && nextLine.length > 0) {
                        count++;

                        this.pb.setValue(count);
                        this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                        if (doImportOnlyPrices) {
//-------------------------SQL_PRODUCT_PRICE------------------
////                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
//                    query_prod_price = SQL_PRODUCT_PRICE;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?";
//                    query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPrice = con.prepareStatement(query_prod_price);
                            psPrice.setString(1, "lst1");
                            psPrice.setLong(2, Long.parseLong(nextLine[FILE_QENDRA_PLU_ID].trim()));
                            if (Double.parseDouble(nextLine[FILE_QENDRA_PRICE_LIST_1].trim().replace(',', '.')) > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 1 | " + nextLine[FILE_QENDRA_PRICE_LIST_1].trim().replace(',', '.') + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }

                            psPrice.setDouble(3, Double.parseDouble(nextLine[FILE_QENDRA_PRICE_LIST_1].trim().replace(',', '.')));
                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 1 | " + nextLine[FILE_QENDRA_PRICE_LIST_1].trim().replace(',', '.'));
                            psPrice.execute();

                            psPrice.setString(1, "lst2");
                            psPrice.setLong(2, Long.parseLong(nextLine[FILE_QENDRA_PLU_ID].trim()));

                            if (Double.parseDouble(nextLine[FILE_QENDRA_PRICE_LIST_2].trim().replace(',', '.')) > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 2 | " + nextLine[FILE_QENDRA_PRICE_LIST_2].trim() + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }
                            psPrice.setDouble(3, Double.parseDouble(nextLine[FILE_QENDRA_PRICE_LIST_2].trim().replace(',', '.')));

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 2 | " + nextLine[FILE_QENDRA_PRICE_LIST_2].trim().replace(',', '.'));
                            psPrice.execute();
                        } else {
//-----------------------SQL_DEPARTMENT--------------------
//                    query_dpt = SQL_DEPARTMENT;
//                    /*
//                     the_product_id BIGINT,
//                     the_name CHARACTER VARYING,
//                     */

//                    questionmarks = "?,?";
//                    query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
                            // psDpt = con.prepareStatement(query_dpt);
                            psDepExist.setString(1, nextLine[FILE_QENDRA_DEPARTMENT_NAME].toUpperCase().trim());
                            rs = psDepExist.executeQuery();
                            if (!rs.next()) {
                                //NO ESTA, creo el ID y lo inserto
                                //MAX DEL ID
                                psDptMax = con.prepareStatement("SELECT MAX(department_id) as depatment_id FROM department");
                                rsDptMax = psDptMax.executeQuery();
                                rsDptMax.next();
                                department_id = rsDptMax.getLong("depatment_id") + 1;

                                psDpt.setLong(1, department_id);
                                psDpt.setString(2, nextLine[FILE_QENDRA_DEPARTMENT_NAME].trim());
                                //psDpt.addBatch();
                                customLogger.getInstance().writeLog(MainApp.INFO, "", "");
                                customLogger.getInstance().writeLog(MainApp.INFO, ip, "Departamento: " + department_id + " | " + nextLine[FILE_QENDRA_DEPARTMENT_NAME].trim());
                                psDpt.execute();

                            } else {
                                //ESTÁ, tomo el ID para usarlo luego
                                department_id = rs.getLong("department_id");
                            }

                            rs = null;
                            rsDptMax = null;

//-------------------------SQL_PRODUCT------------------
//                    query_prod = SQL_PRODUCT;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?,?,?,?,?,?,?,?";
//                    query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPlu = con.prepareStatement(query_prod);
//                     the_product_id BIGINT,
                            psPlu.setLong(1, Math.abs(Long.parseLong(nextLine[FILE_QENDRA_PLU_ID].trim())));
//                     the_erp_code CHARACTER VARYING,
                            psPlu.setString(2, nextLine[FILE_QENDRA_PLU_ERP_CODE].trim());
//                     the_name CHARACTER VARYING,
                            if (nextLine[FILE_QENDRA_PLU_NAME].trim().isEmpty()) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "PLU: " + nextLine[FILE_QENDRA_PLU_ID].trim() + " | El PLU no puede tener el nombre vacío.");
                                int x = 1 / 0;
                            }

                            psPlu.setString(3, nextLine[FILE_QENDRA_PLU_NAME].trim());
//                     the_attribute INTEGER,
                            if (nextLine[FILE_QENDRA_PLU_TYPE].trim().equalsIgnoreCase("peso")) {
                                psPlu.setInt(4, 0);
                                psPlu.setString(9, "1");
                            } else if (nextLine[FILE_QENDRA_PLU_TYPE].trim().equalsIgnoreCase("unidad")) {
                                psPlu.setInt(4, 1);
                                psPlu.setString(9, "2");
                            } else if (nextLine[FILE_QENDRA_PLU_TYPE].trim().equalsIgnoreCase("congelado")) {
                                psPlu.setInt(4, 2);
                                psPlu.setString(9, "1");
                            } else if (nextLine[FILE_QENDRA_PLU_TYPE].trim().equalsIgnoreCase("escurrido")) {
                                psPlu.setInt(4, 3);
                                psPlu.setString(9, "1");
                            } else {
                                psPlu.setInt(4, 1);
                                psPlu.setString(9, "2");
                            }

//                     the_department_id BIGINT,
                            psPlu.setLong(5, department_id);

//                     the_description CHARACTER VARYING,
                            psPlu.setString(6, nextLine[FILE_QENDRA_PLU_NAME].trim());
                            try {
                                if (Integer.parseUnsignedInt(nextLine[FILE_QENDRA_PLU_USE_BY_DATE].trim()) > 0) {
//                     the_print_used_by_date CHARACTER,
                                    psPlu.setString(7, "Y");
//                     the_used_by_date NUMERIC
                                    psPlu.setInt(8, Integer.parseUnsignedInt(nextLine[FILE_QENDRA_PLU_USE_BY_DATE].trim()));
                                } else {
//                     the_print_used_by_date CHARACTER,
                                    psPlu.setString(7, "N");
//                     the_used_by_date NUMERIC
                                    psPlu.setInt(8, 0);
                                }
                            } catch (Exception e) {
                                //                     the_print_used_by_date CHARACTER,
                                psPlu.setString(7, "N");
//                     the_used_by_date NUMERIC
                                psPlu.setInt(8, 0);
                            }
                            psPlu.setDouble(10, 0);
                            // psPlu.setDouble(10, Double.parseDouble(nextLine[FILE_QENDRA_PLU_TARE].trim().replace(',', '.')));

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "PLU: " + nextLine[FILE_QENDRA_PLU_ID].trim() + " | " + nextLine[FILE_QENDRA_PLU_NAME].trim());

                            psPlu.execute();

//-------------------------SQL_PRODUCT_PRICE------------------
////                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
//                    query_prod_price = SQL_PRODUCT_PRICE;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?";
//                    query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPrice = con.prepareStatement(query_prod_price);
                            psPrice.setString(1, "lst1");
                            psPrice.setLong(2, Long.parseLong(nextLine[FILE_QENDRA_PLU_ID].trim()));
                            if (Double.parseDouble(nextLine[FILE_QENDRA_PRICE_LIST_1].trim().replace(',', '.')) > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 1 | " + nextLine[FILE_QENDRA_PRICE_LIST_1].trim().replace(',', '.') + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }

                            psPrice.setDouble(3, Double.parseDouble(nextLine[FILE_QENDRA_PRICE_LIST_1].trim().replace(',', '.')));
                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 1 | " + nextLine[FILE_QENDRA_PRICE_LIST_1].trim().replace(',', '.'));
                            psPrice.execute();

                            psPrice.setString(1, "lst2");
                            psPrice.setLong(2, Long.parseLong(nextLine[FILE_QENDRA_PLU_ID].trim()));

                            if (Double.parseDouble(nextLine[FILE_QENDRA_PRICE_LIST_2].trim().replace(',', '.')) > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 2 | " + nextLine[FILE_QENDRA_PRICE_LIST_2].trim() + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }
                            psPrice.setDouble(3, Double.parseDouble(nextLine[FILE_QENDRA_PRICE_LIST_2].trim().replace(',', '.')));

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 2 | " + nextLine[FILE_QENDRA_PRICE_LIST_2].trim().replace(',', '.'));
                            psPrice.execute();

                        }
                    }
                    con.commit();
                } catch (SQLException e) {
                    result = false;
                    con.rollback();
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
                } catch (Exception e) {
                    result = false;
                    con.rollback();
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
                }

            }

            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Registros procesados: " + count);
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");

            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Fin importando el archivo: " + csvFile);

        } catch (SQLException ex) {
            result = false;
            Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;

        } catch (IOException ex) {
            result = false;
            Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IO_EXCEPTION;
        } catch (Exception ex) {
            result = false;
            Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {

            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psDptMax) {
                    psDptMax.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != rs) {
                    rs.close();
                }
                csvReader.close();

                rs = null;
                psDpt = null;
                psDptMax = null;
                psPlu = null;
                psPrice = null;
                con = null;
                csvReader = null;
            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        }

        return result;
    }

    public boolean loadCSVAlmacor(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices) {

        final String ip = ip2;
        boolean doImportOnlyPrices = bImportOnlyPrices;
        boolean result = true;
        CSVReader csvReader = null;
        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);
            csvReader = new CSVReader(new FileReader(csvFile), this.seprator);

        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

        String query_dpt, query_prod, query_prod_price = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
//        query = query.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
//        query = query.replaceFirst(VALUES_REGEX, questionmarks);

        // System.out.println("Query: " + query);
        String[] nextLine;
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPrice = null;
        int count = 0;

        con = this.connection;
        try {
            con.setAutoCommit(false);

//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

            this.pb.setMaximum((int) (Files.lines(Paths.get(csvFile), Charset.forName("ISO-8859-1")).count() - 1));

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            while ((nextLine = csvReader.readNext()) != null) {
                try {
                    duration = new Duration(timeStart, timeStart.now());

//                 1                 ;     0 ID departamento
//                 Almacen           ;     1 name departamento
//                 1;                      2 PLU
//                 Activia Bebible Fr;     3 nombre
//                 00001;                  4 codigo_plu ERP
//                 5,00;                   5 lista 1
//                 5,00;                   6 lista 2
//                 Peso/Unidad;            7 tipo producto 0 P - 1 U
//                 0;                      8 tara
//                 0000;                   9 dias vencimiento
//                 ;                       10 otros datos
//                 13/12/2015 18:37:34;    11 Ultima modificación
//                 Administrador           12 Modificado por usuario
                    if (null != nextLine && nextLine.length > 0) {
                        count++;

                        this.pb.setValue(count);
                        this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                        if (doImportOnlyPrices) {
//-------------------------SQL_PRODUCT_PRICE------------------
////                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
//                    query_prod_price = SQL_PRODUCT_PRICE;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?";
//                    query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPrice = con.prepareStatement(query_prod_price);
                            psPrice.setString(1, "lst1");
                            psPrice.setLong(2, Long.parseLong(nextLine[FILE_PLU_ID].trim()));
                            if (Double.parseDouble(nextLine[FILE_PRICE_LIST_1].trim().replace(',', '.')) > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 1 | " + nextLine[FILE_PRICE_LIST_1].trim().replace(',', '.') + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }

                            psPrice.setDouble(3, Double.parseDouble(nextLine[FILE_PRICE_LIST_1].trim().replace(',', '.')));
                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 1 | " + nextLine[FILE_PRICE_LIST_1].trim().replace(',', '.'));
                            psPrice.execute();

                            psPrice.setString(1, "lst2");
                            psPrice.setLong(2, Long.parseLong(nextLine[FILE_PLU_ID].trim()));

                            if (Double.parseDouble(nextLine[FILE_PRICE_LIST_2].trim().replace(',', '.')) > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 2 | " + nextLine[FILE_PRICE_LIST_2].trim() + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }
                            psPrice.setDouble(3, Double.parseDouble(nextLine[FILE_PRICE_LIST_2].trim().replace(',', '.')));

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 2 | " + nextLine[FILE_PRICE_LIST_2].trim().replace(',', '.'));
                            psPrice.execute();
                        } else {
//-----------------------SQL_DEPARTMENT--------------------
//                    query_dpt = SQL_DEPARTMENT;
//                    /*
//                     the_product_id BIGINT,
//                     the_name CHARACTER VARYING,
//                     */

//                    questionmarks = "?,?";
//                    query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
                            // psDpt = con.prepareStatement(query_dpt);
                            psDpt.setLong(1, Long.parseLong(nextLine[FILE_DEPARTMENT_ID].trim()));
                            psDpt.setString(2, nextLine[FILE_DEPARTMENT_NAME].trim());
                            //psDpt.addBatch();
                            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Departamento: " + nextLine[FILE_DEPARTMENT_ID].trim() + " | " + nextLine[FILE_DEPARTMENT_NAME].trim());
                            psDpt.execute();

//-------------------------SQL_PRODUCT------------------
//                    query_prod = SQL_PRODUCT;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?,?,?,?,?,?,?,?";
//                    query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPlu = con.prepareStatement(query_prod);
//                     the_product_id BIGINT,
                            psPlu.setLong(1, Math.abs(Long.parseLong(nextLine[FILE_PLU_ID].trim())));
//                     the_erp_code CHARACTER VARYING,
                            psPlu.setString(2, nextLine[FILE_PLU_ERP_CODE].trim());
//                     the_name CHARACTER VARYING,
                            if (nextLine[FILE_PLU_NAME].trim().isEmpty()) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "PLU: " + nextLine[FILE_PLU_ID].trim() + " | El PLU no puede tener el nombre vacío.");
                                int x = 1 / 0;
                            }

                            psPlu.setString(3, nextLine[FILE_PLU_NAME].trim());
//                     the_attribute INTEGER,
                            if (nextLine[FILE_PLU_TYPE].trim().equalsIgnoreCase("Peso")) {
                                psPlu.setInt(4, 0);
                                psPlu.setString(9, "1");
                            } else {
                                psPlu.setInt(4, 1);
                                psPlu.setString(9, "2");
                            }
//                     the_department_id BIGINT,
                            psPlu.setLong(5, Long.parseLong(nextLine[FILE_DEPARTMENT_ID].trim()));

//                     the_description CHARACTER VARYING,
                            psPlu.setString(6, nextLine[FILE_PLU_DESCRIPTION].trim());
                            try {
                                if (Integer.parseUnsignedInt(nextLine[FILE_PLU_USE_BY_DATE].trim()) > 0) {
//                     the_print_used_by_date CHARACTER,
                                    psPlu.setString(7, "Y");
//                     the_used_by_date NUMERIC
                                    psPlu.setInt(8, Integer.parseUnsignedInt(nextLine[FILE_PLU_USE_BY_DATE].trim()));
                                } else {
//                     the_print_used_by_date CHARACTER,
                                    psPlu.setString(7, "N");
//                     the_used_by_date NUMERIC
                                    psPlu.setInt(8, 0);
                                }
                            } catch (Exception e) {
                                //                     the_print_used_by_date CHARACTER,
                                psPlu.setString(7, "N");
//                     the_used_by_date NUMERIC
                                psPlu.setInt(8, 0);
                            }
                            psPlu.setDouble(10, Double.parseDouble(nextLine[FILE_PLU_TARE].trim().replace(',', '.')));

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "PLU: " + nextLine[FILE_PLU_ID].trim() + " | " + nextLine[FILE_PLU_NAME].trim());

                            psPlu.execute();

//-------------------------SQL_PRODUCT_PRICE------------------
////                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
//                    query_prod_price = SQL_PRODUCT_PRICE;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?";
//                    query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPrice = con.prepareStatement(query_prod_price);
                            psPrice.setString(1, "lst1");
                            psPrice.setLong(2, Long.parseLong(nextLine[FILE_PLU_ID].trim()));
                            if (Double.parseDouble(nextLine[FILE_PRICE_LIST_1].trim().replace(',', '.')) > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 1 | " + nextLine[FILE_PRICE_LIST_1].trim().replace(',', '.') + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }

                            psPrice.setDouble(3, Double.parseDouble(nextLine[FILE_PRICE_LIST_1].trim().replace(',', '.')));
                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 1 | " + nextLine[FILE_PRICE_LIST_1].trim().replace(',', '.'));
                            psPrice.execute();

                            psPrice.setString(1, "lst2");
                            psPrice.setLong(2, Long.parseLong(nextLine[FILE_PLU_ID].trim()));

                            if (Double.parseDouble(nextLine[FILE_PRICE_LIST_2].trim().replace(',', '.')) > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 2 | " + nextLine[FILE_PRICE_LIST_2].trim() + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }
                            psPrice.setDouble(3, Double.parseDouble(nextLine[FILE_PRICE_LIST_2].trim().replace(',', '.')));

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 2 | " + nextLine[FILE_PRICE_LIST_2].trim().replace(',', '.'));
                            psPrice.execute();

                        }
                    }
                    con.commit();
                } catch (SQLException e) {
                    result = false;
                    con.rollback();
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
                } catch (Exception e) {
                    result = false;
                    con.rollback();
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
                }

            }

            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Registros procesados: " + count);
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");

            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Fin importando el archivo: " + csvFile);

        } catch (SQLException ex) {
            result = false;
            Logger.getLogger(FileLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (IOException ex) {
            result = false;
            Logger.getLogger(FileLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IO_EXCEPTION;
        } catch (Exception ex) {
            result = false;
            Logger.getLogger(FileLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != con) {
                    con.close();
                }
                csvReader.close();

                psDpt = null;
                psDpt = null;
                psPlu = null;
                psPrice = null;
                con = null;
                csvReader = null;
            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public boolean loadCSVFires(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices) {

        final String ip = ip2;
        boolean doImportOnlyPrices = bImportOnlyPrices;
        boolean result = true;
        CSVReader csvReader = null;
        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);

            //csvReader = new CSVReader(new FileReader(csvFile));
            csvReader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), "ISO-8859-1"));

        } catch (Exception e) {
            result = false;
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

        String query_dpt, query_prod, query_prod_price = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
//        query = query.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
//        query = query.replaceFirst(VALUES_REGEX, questionmarks);

        // System.out.println("Query: " + query);
        String[] nextLine;
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPrice = null;
        int count = 0;

        con = this.connection;
        try {
            con.setAutoCommit(false);

//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);
            this.pb.setMaximum((int) (Files.lines(Paths.get(csvFile), Charset.forName("ISO-8859-1")).count() - 1));

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            while ((nextLine = csvReader.readNext()) != null) {
                try {
                    duration = new Duration(timeStart, timeStart.now());

//                 1                 ;     0 ID departamento
//                 Almacen           ;     1 name departamento
//                 1;                      2 PLU
//                 Activia Bebible Fr;     3 nombre
//                 00001;                  4 codigo_plu ERP
//                 5,00;                   5 lista 1
//                 5,00;                   6 lista 2
//                 Peso/Unidad;            7 tipo producto 0 P - 1 U
//                 0;                      8 tara
//                 0000;                   9 dias vencimiento
//                 ;                       10 otros datos
//                 13/12/2015 18:37:34;    11 Ultima modificación
//                 Administrador           12 Modificado por usuario

                    /*
        0
        "MOS","Mostrador",1,2,0                                         0 ID departamento & 1 name departamento
        "COD","Código",3,8,0                                            4 codigo_plu ERP
        "PLU","PLU",9,12,0                                              2 PLU
        "TXT","Texto",13,37,0                                           3 nombre
        "PRC","PRECIO",38,44,0                                          5 lista 1 & 6 lista 2
        "WGH","Pesado / No Pesado",45,45,0                              7 tipo producto 0=P - 1=U
        "CBA","Código de barras",46,57,0                                13 código de barras
        "CAD","Caducidad",58,60,0                                       9 dias vencimiento
        "TAR","Tara",61,65,0                                            8 tara
        "JTEC","Juego de Tecla en la sección",66,66,0                   N/A
        "TEC","Tecla",67,69,0                                           N/A
        "IVA","Tipo Iva",70,71,0                                        N/A
        "BAJA","Distinto de 0 Indica si baja de artículo",72,72,0       14 activo 0=Y !0=N
                     */
                    if (null != nextLine && Arrays.toString(nextLine).trim().length() > 2) {
                        count++;
                        this.pb.setValue(count);
                        this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                        if (doImportOnlyPrices) {
//-------------------------SQL_PRODUCT_PRICE------------------
////                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
//                    query_prod_price = SQL_PRODUCT_PRICE;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?";
//                    query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPrice = con.prepareStatement(query_prod_price);
                            psPrice.setString(1, "lst1");
                            psPrice.setLong(2, Long.parseLong(Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim()));

                            if (Double.parseDouble(Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim().replace(".", "").replace(",", "")) / 100 > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 1 | " + Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim() + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }

                            psPrice.setDouble(3, Double.parseDouble(Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim().replace(".", "").replace(",", "")) / 100);
                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 1 | " + Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim());
                            psPrice.execute();

                            psPrice.setString(1, "lst2");
                            psPrice.setLong(2, Long.parseLong(Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim().replace(".", "").replace(",", "")));
                            psPrice.setDouble(3, Double.parseDouble(Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim()) / 100);

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 2 | " + Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim());
                            psPrice.execute();
                        } else {

//-----------------------SQL_DEPARTMENT--------------------
//                    query_dpt = SQL_DEPARTMENT;
//                    /*
//                     the_product_id BIGINT,
//                     the_name CHARACTER VARYING,
//                     */
//                    questionmarks = "?,?";
//                    query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
                            // psDpt = con.prepareStatement(query_dpt);
/*                        StringBuilder sb = new StringBuilder();

                        sb.append("FILE_FIRES_MOS |" + Arrays.toString(nextLine).substring(FILE_FIRES_MOS[0], FILE_FIRES_MOS[1]).trim() + "| ");
                        sb.append("FILE_FIRES_COD |" + Arrays.toString(nextLine).substring(FILE_FIRES_COD[0], FILE_FIRES_COD[1]).trim() + "| ");
                        sb.append("FILE_FIRES_PLU |" + Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim() + "| ");
                        sb.append("FILE_FIRES_TXT |" + Arrays.toString(nextLine).substring(FILE_FIRES_TXT[0], FILE_FIRES_TXT[1]).trim() + "| ");
                        sb.append("FILE_FIRES_PRC |" + Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim() + "| ");
                        sb.append("FILE_FIRES_WGH |" + Arrays.toString(nextLine).substring(FILE_FIRES_WGH[0], FILE_FIRES_WGH[1]).trim() + "| ");
                        sb.append("FILE_FIRES_CBA |" + Arrays.toString(nextLine).substring(FILE_FIRES_CBA[0], FILE_FIRES_CBA[1]).trim() + "| ");
                        sb.append("FILE_FIRES_CAD |" + Arrays.toString(nextLine).substring(FILE_FIRES_CAD[0], FILE_FIRES_CAD[1]).trim() + "| ");
                        sb.append("FILE_FIRES_TAR |" + Arrays.toString(nextLine).substring(FILE_FIRES_TAR[0], FILE_FIRES_TAR[1]).trim() + "| ");
                        sb.append("FILE_FIRES_JTEC |" + Arrays.toString(nextLine).substring(FILE_FIRES_JTEC[0], FILE_FIRES_JTEC[1]).trim() + "| ");
                        sb.append("FILE_FIRES_TEC |" + Arrays.toString(nextLine).substring(FILE_FIRES_TEC[0], FILE_FIRES_TEC[1]).trim() + "| ");
                        sb.append("FILE_FIRES_IVA |" + Arrays.toString(nextLine).substring(FILE_FIRES_IVA[0], FILE_FIRES_IVA[1]).trim() + "| ");
                        sb.append("FILE_FIRES_BAJA |" + Arrays.toString(nextLine).substring(FILE_FIRES_BAJA[0], FILE_FIRES_BAJA[1]).trim() + "| ");

                        customLogger.getInstance().writeLog("PARSE:", "", sb.toString());  
                             */
                            psDpt.setLong(1, Math.abs(Long.parseLong(Arrays.toString(nextLine).substring(FILE_FIRES_MOS[0], FILE_FIRES_MOS[1]).trim())));
                            psDpt.setString(2, Arrays.toString(nextLine).substring(FILE_FIRES_MOS[0], FILE_FIRES_MOS[1]).trim());
                            //psDpt.addBatch();
                            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Departamento: " + Arrays.toString(nextLine).substring(FILE_FIRES_MOS[0], FILE_FIRES_MOS[1]).trim() + " | " + Arrays.toString(nextLine).substring(FILE_FIRES_MOS[0], FILE_FIRES_MOS[1]).trim());
                            psDpt.execute();

//-------------------------SQL_PRODUCT------------------
//                    query_prod = SQL_PRODUCT;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?,?,?,?,?,?,?,?";
//                    query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPlu = con.prepareStatement(query_prod);
//                     the_product_id BIGINT,
                            psPlu.setLong(1, Math.abs(Long.parseLong(Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim()))); //nextLine[FILE_PLU_ID].trim()));
//                     the_erp_code CHARACTER VARYING,
                            psPlu.setString(2, Arrays.toString(nextLine).substring(FILE_FIRES_COD[0], FILE_FIRES_COD[1]).trim()); //nextLine[FILE_PLU_ERP_CODE].trim());
//                     the_name CHARACTER VARYING,
                            if (Arrays.toString(nextLine).substring(FILE_FIRES_TXT[0], FILE_FIRES_TXT[1]).trim().isEmpty()) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "PLU: " + Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim() + " | El PLU no puede tener el nombre vacío.");
                                int x = 1 / 0;
                            }
                            psPlu.setString(3, Arrays.toString(nextLine).substring(FILE_FIRES_TXT[0], FILE_FIRES_TXT[1]).trim()); //nextLine[FILE_PLU_NAME].trim());
                            //psPlu.setString(3, new String(Arrays.toString(nextLine).substring(FILE_FIRES_TXT[0], FILE_FIRES_TXT[1]).trim().getBytes("ISO-8859-1"),"UTF-8"));
//                     the_attribute INTEGER,

                            //P = PESABLE // U = UNITARIOS
                            if (Arrays.toString(nextLine).substring(FILE_FIRES_WGH[0], FILE_FIRES_WGH[1]).trim().equalsIgnoreCase("P")) {
                                psPlu.setInt(4, 0);
                                psPlu.setString(9, "1");
                            } else {
                                psPlu.setInt(4, 1);
                                psPlu.setString(9, "2");
                            }
//                     the_department_id BIGINT,
                            psPlu.setLong(5, Math.abs(Long.parseLong(Arrays.toString(nextLine).substring(FILE_FIRES_MOS[0], FILE_FIRES_MOS[1]).trim())));

//                     the_description CHARACTER VARYING,
                            psPlu.setString(6, Arrays.toString(nextLine).substring(FILE_FIRES_TXT[0], FILE_FIRES_TXT[1]).trim());
                            //psPlu.setString(6, new String(Arrays.toString(nextLine).substring(FILE_FIRES_TXT[0], FILE_FIRES_TXT[1]).trim().getBytes("ISO-8859-1"),"UTF-8"));
//new String(nextLine.toString() .getBytes("ISO-8859-1"),"UTF-8"));
                            try {
                                if (Integer.parseUnsignedInt(Arrays.toString(nextLine).substring(FILE_FIRES_CAD[0], FILE_FIRES_CAD[1]).trim()) > 0) {
//                     the_print_used_by_date CHARACTER,
                                    psPlu.setString(7, "Y");
//                     the_used_by_date NUMERIC
                                    psPlu.setInt(8, Integer.parseUnsignedInt(Arrays.toString(nextLine).substring(FILE_FIRES_CAD[0], FILE_FIRES_CAD[1]).trim()));
                                } else {
//                     the_print_used_by_date CHARACTER,
                                    psPlu.setString(7, "N");
//                     the_used_by_date NUMERIC
                                    psPlu.setInt(8, 0);
                                }
                            } catch (Exception e) {
                                //                     the_print_used_by_date CHARACTER,
                                psPlu.setString(7, "N");
//                     the_used_by_date NUMERIC
                                psPlu.setInt(8, 0);
                            }
                            psPlu.setDouble(10, Double.parseDouble(Arrays.toString(nextLine).substring(FILE_FIRES_TAR[0], FILE_FIRES_TAR[1]).trim()) / 1000);

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "PLU: " + Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim() + " | " + Arrays.toString(nextLine).substring(FILE_FIRES_TXT[0], FILE_FIRES_TXT[1]).trim());
                            // customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "PLU: " + Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim() + " | " + new String(Arrays.toString(nextLine).substring(FILE_FIRES_TXT[0], FILE_FIRES_TXT[1]).trim().getBytes("ISO-8859-1"),"UTF-8"));

                            psPlu.execute();

//-------------------------SQL_PRODUCT_PRICE------------------
////                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
//                    query_prod_price = SQL_PRODUCT_PRICE;
//                    /*
//                     the_product_id BIGINT,
//                     the_erp_code CHARACTER VARYING,
//                     the_name CHARACTER VARYING,
//                     the_attribute INTEGER,
//                     the_department_id BIGINT,
//                     the_description CHARACTER VARYING,
//                     the_print_used_by_date CHARACTER,
//                     the_used_by_date NUMERIC
//                     */
//                    questionmarks = "?,?,?";
//                    query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
//                    psPrice = con.prepareStatement(query_prod_price);
                            psPrice.setString(1, "lst1");
                            psPrice.setLong(2, Long.parseLong(Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim()));

                            if (Double.parseDouble(Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim().replace(".", "").replace(",", "")) / 100 > 9999.99) {
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Precios: lista 1 | " + Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim() + " El precio no debe ser mayor a 9999.99");
                                int x = 1 / 0;
                            }

                            psPrice.setDouble(3, Double.parseDouble(Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim().replace(".", "").replace(",", "")) / 100);
                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 1 | " + Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim());
                            psPrice.execute();

                            psPrice.setString(1, "lst2");
                            psPrice.setLong(2, Long.parseLong(Arrays.toString(nextLine).substring(FILE_FIRES_PLU[0], FILE_FIRES_PLU[1]).trim().replace(".", "").replace(",", "")));
                            psPrice.setDouble(3, Double.parseDouble(Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim()) / 100);

                            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Precios: lista 2 | " + Arrays.toString(nextLine).substring(FILE_FIRES_PRC[0], FILE_FIRES_PRC[1]).trim());
                            psPrice.execute();
                        }
                        con.commit();
                    }

                } catch (SQLException e) {
                    result = false;
                    con.rollback();
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
                } catch (Exception e) {
                    result = false;
                    con.rollback();
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
                }

            }

            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Registros procesados: " + count);
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");
            customLogger.getInstance().writeLog(MainApp.INFO, "", "");

            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Fin importando el archivo: " + csvFile);

        } catch (SQLException ex) {
            result = false;
            Logger.getLogger(FileLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (IOException ex) {
            result = false;
            Logger.getLogger(FileLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IO_EXCEPTION;
        } catch (Exception ex) {
            result = false;
            Logger.getLogger(FileLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "" + ex.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != con) {
                    con.close();
                }
                csvReader.close();

                psDpt = null;
                psPlu = null;
                psPrice = null;
                con = null;
                csvReader = null;
            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }


    public boolean loadMGVDepto(boolean b, String ip2) {
        final String ip = ip2;
        boolean result = true;

        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Not a valid connection.");
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
            return false;
        }

        SystelDBConnector sysdbc = new SystelDBConnector();
        List<String> depto = ChooseDevices.map_DeptoMGV.get(ip);

        if (depto == null || depto.isEmpty()) {
            for (SystelDBConnector.SystelDepto lstDepto : ChooseDevices.lstDeptos) {
                depto.add(lstDepto.getId() + "");
            }
        }

        PreparedStatement psDpt = null;
        PreparedStatement psPlu = null;
        PreparedStatement psPrice = null;
        PreparedStatement psNutInfo = null;
        PreparedStatement psReceing = null;
        PreparedStatement psTara = null;
        PreparedStatement psConservacion = null;
        PreparedStatement psExtra2 = null;
        PreparedStatement psIngredientes = null;
        IBPlu SysData = new IBPlu();
        ProgressCounter progressCounter = new ProgressCounter();

        try {
            // Fetch all required data
            List<SystelDBConnector.SystelProducto> products = sysdbc.getPLUs(depto);
            List<SystelDBConnector.SystelDepto> departamentos = sysdbc.getDeptos(depto);

            // Optional data
            List<SystelDBConnector.SystelNutInfo> nutInfo = !ConfigFile.getItensMGVNutInfoFilePath().isEmpty() ? 
                sysdbc.getNutInfo(depto) : null;
            List<SystelDBConnector.SystelExtra1> extra1 = !ConfigFile.getItensMGVReceingFilePath().isEmpty() ?
                sysdbc.getExtra1(depto) : null;
            List<SystelDBConnector.SystelTara> tara = !ConfigFile.getItensMGVTaraFilePath().isEmpty() ?
                sysdbc.getTara(depto) : null;
            List<SystelDBConnector.SystelConserva> conserva = !ConfigFile.getItensMGVConservacionFilePath().isEmpty() ?
                sysdbc.getConserva(depto) : null;
            List<SystelDBConnector.SystelExtra2> extra2 = !ConfigFile.getItensMGVExtra2FilePath().isEmpty() ?
                sysdbc.getExtra2(depto) : null;
            List<SystelDBConnector.SystelIngredient> ingredients = !ConfigFile.getItensMGVIngredientsFilePath().isEmpty() || 
                !ConfigFile.getItensMGVIngredientsFilePath2().isEmpty() ?
                sysdbc.getIngredients(depto) : null;

            if (products == null || products.isEmpty()) {
                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se obtuvieron registros del departamento: " + depto.toString());
                return false;
            }

            int totalSize = products.size();
            if (nutInfo != null) totalSize += nutInfo.size();
            if (extra1 != null) totalSize += extra1.size();
            if (tara != null) totalSize += tara.size();
            if (conserva != null) totalSize += conserva.size();
            if (extra2 != null) totalSize += extra2.size();
            if (ingredients != null) totalSize += ingredients.size();

            this.pb.setMaximum(totalSize);

            connection.setAutoCommit(false);

            psDpt = prepareStatement(SQL_DEPARTMENT, "?,?");
            psPlu = prepareStatement(SQL_PRODUCT, "?,?,?,?,?,?,?,?,?,?");
            psPrice = prepareStatement(SQL_PRODUCT_PRICE, "?,?,?,?");
            psReceing = connection.prepareStatement(SQL_RECEING);
            psTara = connection.prepareStatement(SQL_TARA);
            psConservacion = connection.prepareStatement(SQL_CONSERVACION);
            psExtra2 = connection.prepareStatement(SQL_EXTRA2);
            psIngredientes = connection.prepareStatement(SQL_INGREDIENTS);

            final DateTime timeStart = new DateTime();

            // Procesar departamentos
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Procesando departamentos");
            for (SystelDBConnector.SystelDepto dept : departamentos) {
                processDepartment(dept, psDpt, ip);
            }

            // Procesar productos y precios
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Procesando productos y precios");
            for (SystelProducto producto : products) {
                processProduct(producto, psPlu, psPrice, SysData, progressCounter, timeStart, ip);
            }

            // Procesar información nutricional
            if (nutInfo != null && !nutInfo.isEmpty()) {
                processNutritionalInfo(nutInfo, psNutInfo, progressCounter, timeStart, ip);
            }

            // Procesar recetas
            if (extra1 != null && !extra1.isEmpty()) {
                processExtra1(extra1, psReceing, progressCounter, timeStart, ip);
            }

            // Procesar taras
            if (tara != null && !tara.isEmpty()) {
                processTara(tara, psTara, progressCounter, timeStart, ip);
            }

            // Procesar conservación
            if (conserva != null && !conserva.isEmpty()) {
                processConservacion(conserva, psConservacion, progressCounter, timeStart, ip);
            }

            // Procesar extra2
            if (extra2 != null && !extra2.isEmpty()) {
                processExtra2(extra2, psExtra2, progressCounter, timeStart, ip);
            }

            if (ingredients != null && !ingredients.isEmpty()) {
                processIngredients(ingredients, psIngredientes, progressCounter, timeStart, ip);
            }

            return true;

        } catch (Exception e) {
            result = false;
            try {
                connection.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Error general: " + e.getMessage());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
            return false;
        } finally {
            closeResources(psDpt, psPlu, psPrice, psNutInfo, psReceing, psTara, 
                          psConservacion, psExtra2, psIngredientes);
            sysdbc.closeConection();
        }
    }

    private class ProgressCounter {
        private int count = 0;
        public int increment() {
            return ++count;
        }
        public int get() {
            return count;
        }
    }

    private void processDepartment(SystelDBConnector.SystelDepto dept, PreparedStatement psDpt, String ip) {
        try {
            psDpt.setLong(1, dept.getId());
            psDpt.setString(2, dept.getName());
            psDpt.execute();
        } catch (SQLException e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                "Error procesando departamento " + dept.getId() + ": " + e.getMessage());
        }
    }

    private void processProduct(SystelProducto producto, PreparedStatement psPlu, 
                              PreparedStatement psPrice, IBPlu SysData, 
                              ProgressCounter counter, DateTime timeStart, String ip) {
        try {
            Duration duration = new Duration(timeStart, timeStart.now());
            updateProgress(counter.increment(), duration);

            SysData.parseItemsSystelMGV(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio1(),
                producto.getPrecio2(),
                producto.getDepto(),
                producto.getTipo(),
                producto.getTara()
            );

            SysData.doProductsItensMGV(psPlu);
            SysData.doPrices(psPrice);
            connection.commit();
            SysData.vaciarElementos();
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                "Error procesando producto " + producto.getId() + ": " + e.getMessage());
        }
    }
    
    private void processNutritionalInfo(List<SystelDBConnector.SystelNutInfo> nutInfo, 
                                  PreparedStatement psNutInfo,
                                  ProgressCounter counter,
                                  DateTime timeStart,
                                  String ip) {
        if (!ConfigFile.getItensMGVNutInfoFilePath().isEmpty() && nutInfo != null && !nutInfo.isEmpty()) {
            PreparedStatement psUpdatePLUNutID = null;
            IBPlu SysData = new IBPlu();

            try {
                // Log inicio de proceso
                customLogger.getInstance().writeLog(MainApp.INFO, ip, "Inicio importación datos nutricionales");
                customLogger.getInstance().writeLog(MainApp.INFO, ip, "Datos a importar:" + nutInfo.size());

                System.out.println(ip + "  " + "Inicio importación datos nutricionales");
                System.out.println(ip + "  " + "Datos a importar:" + nutInfo.size());

                // Preparar statement para update
                psUpdatePLUNutID = connection.prepareStatement("UPDATE product SET nut_info_set_id = ? WHERE product_id=?");

                // Procesar cada producto
                for (SystelNutInfo producto : nutInfo) {
                    try {
                        // Actualizar contador y progreso
                        Duration duration = new Duration(timeStart, timeStart.now());
                        updateProgress(counter.increment(), duration);

                        // Procesar información nutricional
                        SysData.codigo = producto.getId();
                        SysData.parseInputNutInfoItensSystelMGV(producto);
                        SysData.doNutInfoRDC429(psNutInfo);

                        // Actualizar nut_info_set_id
                        psUpdatePLUNutID.setString(1, "2");
                        psUpdatePLUNutID.setLong(2, producto.getId());
                        psUpdatePLUNutID.execute();

                        // Limpiar datos
                        SysData.vaciarElementos();
                        connection.commit();

                    } catch (Exception e) {
                        customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                            "Error en el producto: " + producto.getId());
                        System.out.println(ip + "Error en el producto: " + producto.getId());
                    }
                }

            } catch (Exception e) {
                customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                    "Error general procesando información nutricional: " + e.getMessage());
            } finally {
                try {
                    if (psUpdatePLUNutID != null) psUpdatePLUNutID.close();
                } catch (SQLException e) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                        "Error cerrando statements: " + e.getMessage());
                }
            }
        }
    }

    private void updateProgress(int count, Duration duration) {
        this.pb.setValue(count);
        this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + 
            duration.getStandardMinutes() + LocaleUtil.getMessage("min") + 
            duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
    }

    
    private void processExtra1(List<SystelDBConnector.SystelExtra1> extra1, 
                             PreparedStatement psReceing,
                             ProgressCounter counter, 
                             DateTime timeStart, 
                             String ip) {
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Procesando recetas");
            for (SystelDBConnector.SystelExtra1 ex1 : extra1) {
                try {
                    Duration duration = new Duration(timeStart, timeStart.now());
                    updateProgress(counter.increment(), duration);

                    psReceing.setString(1, ex1.getExtra1());
                    psReceing.setLong(2, ex1.getId());
                    psReceing.execute();

                    connection.commit();
                } catch (Exception e) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                        "Error procesando receta para producto " + ex1.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                "Error general procesando recetas: " + e.getMessage());
        }
    }

    private void processTara(List<SystelDBConnector.SystelTara> tara, 
                            PreparedStatement psTara,
                            ProgressCounter counter, 
                            DateTime timeStart, 
                            String ip) {
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Procesando taras");
            for (SystelDBConnector.SystelTara t : tara) {
                try {
                    Duration duration = new Duration(timeStart, timeStart.now());
                    updateProgress(counter.increment(), duration);

                    psTara.setFloat(1, t.getTara());
                    psTara.setLong(2, t.getId());
                    psTara.execute();

                    connection.commit();
                } catch (Exception e) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                        "Error procesando tara para producto " + t.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                "Error general procesando taras: " + e.getMessage());
        }
    }

    private void processConservacion(List<SystelDBConnector.SystelConserva> conserva, 
                                   PreparedStatement psConservacion,
                                   ProgressCounter counter, 
                                   DateTime timeStart, 
                                   String ip) {
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Procesando conservación");
            for (SystelDBConnector.SystelConserva c : conserva) {
                try {
                    Duration duration = new Duration(timeStart, timeStart.now());
                    updateProgress(counter.increment(), duration);

                    psConservacion.setString(1, c.getConserva());
                    psConservacion.setLong(2, c.getId());
                    psConservacion.execute();

                    connection.commit();
                } catch (Exception e) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                        "Error procesando conservación para producto " + c.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                "Error general procesando conservación: " + e.getMessage());
        }
    }

    private void processExtra2(List<SystelDBConnector.SystelExtra2> extra2, 
                             PreparedStatement psExtra2,
                             ProgressCounter counter, 
                             DateTime timeStart, 
                             String ip) {
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Procesando campo extra 2");
            for (SystelDBConnector.SystelExtra2 ex2 : extra2) {
                try {
                    Duration duration = new Duration(timeStart, timeStart.now());
                    updateProgress(counter.increment(), duration);

                    psExtra2.setString(1, ex2.getExtra2());
                    psExtra2.setLong(2, ex2.getId());
                    psExtra2.execute();

                    connection.commit();
                } catch (Exception e) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                        "Error procesando extra2 para producto " + ex2.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                "Error general procesando extra2: " + e.getMessage());
        }
    }

    private void processIngredients(List<SystelDBConnector.SystelIngredient> ingredients, 
                                  PreparedStatement psIngredientes,
                                  ProgressCounter counter, 
                                  DateTime timeStart, 
                                  String ip) {
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "Procesando ingredientes");
            for (SystelDBConnector.SystelIngredient ing : ingredients) {
                try {
                    Duration duration = new Duration(timeStart, timeStart.now());
                    updateProgress(counter.increment(), duration);

                    psIngredientes.setString(1, ing.getIngredient());
                    psIngredientes.setLong(2, ing.getId());
                    psIngredientes.execute();

                    connection.commit();
                } catch (Exception e) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                        "Error procesando ingredientes para producto " + ing.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, 
                "Error general procesando ingredientes: " + e.getMessage());
        }
    }
    
    


    

    private PreparedStatement prepareStatement(String sql, String questionmarks) throws SQLException {
        return connection.prepareStatement(sql.replaceFirst(VALUES_REGEX, questionmarks));
    }

    private void closeResources(PreparedStatement... statements) {
        for (PreparedStatement stmt : statements) {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public boolean loadCSVIntellibuild(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices) {

        final String ip = ip2;
        boolean result = true;
        boolean doImportOnlyPrices = bImportOnlyPrices;
        CSVReader csvReader = null;
        ResultSet rs = null;
        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        try {

            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);
            csvReader = new CSVReader(
                    new InputStreamReader(new FileInputStream(csvFile), "ISO-8859-1"),
                    '¬', '\'', 0);
            // String[] line;
            //csvReader = new CSVReader(new FileReader(csvFile), this.seprator);
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }
        String questionmarks = "";
        String query_dpt, query_prod, query_prod_exist, query_prod_price, query_nut_info = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        String[] input;
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPluExist = null, psPrice = null, psNutInfo = null;
        int count = 0;
        IBPlu ibdata = new IBPlu();
        con = this.connection;

        // Added by GCastillo
        QendraHandler qendra_handler = new QendraHandler();
        if (ConfigFile.useQendra()) {
            if (!ConfigFile.useQendraDefPath()) {
                try {
                    qendra_handler.connectDB(ConfigFile.getQendraPath());
                } catch (ClassNotFoundException ex) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se encuentra el archivo: " + ConfigFile.getQendraPath() + "\n Con error: " + ex.toString());
                }
            } else {
                try {
                    qendra_handler.connectDB(ConfigFile.QENDRA_DEFAULT_PATH);
                } catch (ClassNotFoundException ex) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se encuentra el archivo: " + ConfigFile.QENDRA_DEFAULT_PATH + "\n Con error: " + ex.toString());
                }
            }
        }

        try {
            con.setAutoCommit(false);

//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            query_prod_exist = SQL_PRODUCT_EXIST;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC,
             the_extra_field1 CHARACTER VARYING,
             the_extra_field2 CHARACTER VARYING
             the_ingredients CHARACTER VARYING
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

            questionmarks = "?";
            query_prod_exist = query_prod_exist.replaceFirst(VALUES_REGEX, questionmarks);
            psPluExist = con.prepareStatement(query_prod_exist);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

//-------------------------SQL_NUT_INFO------------------
            query_nut_info = SQL_NUT_INFO;
            questionmarks = "?,?,?,?,?,?";
            query_nut_info = query_nut_info.replaceFirst(VALUES_REGEX, questionmarks);
            psNutInfo = con.prepareStatement(query_nut_info);

            this.pb.setMaximum((int) (Files.lines(Paths.get(csvFile), Charset.forName("ISO-8859-1")).count() - 1));

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            while ((input = csvReader.readNext()) != null) {
                //this.pb.revalidate();
                duration = new Duration(timeStart, timeStart.now());

                String entrada = "";
                for (String cell : input) {
                    entrada += cell;
                }
                count++;
                this.pb.setValue(count);
                this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                //this.pb.setString( duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                if (ibdata.parseInput(entrada)) { // Acá se hace el parseo de los datos y se guarda en la clase ibdata
                    if (doImportOnlyPrices) {
                        //si SOLO QUIERE PRECIOS
                        //-----------------------SQL_PLU_EXIST-------------------------
                        psPluExist.setLong(1, ibdata.codigo);
                        rs = psPluExist.executeQuery();
                        if (!rs.next()) {
                            //NO ESTA!!!!, importo todo
                            //-----------------------SQL_DEPARTMENT--------------------
                            ibdata.doDepartments(psDpt);
                            //-----------------------SQL_PRODUCT-----------------------
                            ibdata.doProducts(psPlu);
                            //-----------------------SQL_PRICE-------------------------
                            ibdata.doPrices(psPrice);
                            //-----------------------SQL_NUT_INFO-------------------------
                            ibdata.doNutInfo(psNutInfo);
                        } else {
                            // LO TENGO!!!!, sólo actualizo el precio
                            //-----------------------SQL_PRICE-------------------------
                            ibdata.doPrices(psPrice);
                        }
                    } else {
                        //VA IMPORTACION FULL
                        //-----------------------SQL_DEPARTMENT--------------------
                        ibdata.doDepartments(psDpt);
                        //-----------------------SQL_PRODUCT-----------------------
                        ibdata.doProducts(psPlu);
                        //-----------------------SQL_PRICE-------------------------
                        ibdata.doPrices(psPrice);
                        //-----------------------SQL_NUT_INFO-------------------------
                        ibdata.doNutInfo(psNutInfo);
                    }
                    con.commit();

                    // Added by GCastillo
                    if (ConfigFile.useQendra()) {
                        qendra_handler.InsertProduct(ibdata, ibdata.codigo);
                    }

                    ibdata.vaciarElementos();
                }
            }
        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {

            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPluExist) {
                    psPluExist.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != psNutInfo) {
                    psNutInfo.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != connection) {
                    connection.close();
                }
                csvReader.close();

                psDpt = null;
                psPlu = null;
                psPluExist = null;
                psNutInfo = null;
                psPrice = null;
                ibdata = null;
                qendra_handler = null;
                rs = null;
                con = null;
                connection = null;
                csvReader = null;
            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public boolean loadCSVPVMex(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices) {

        Connection jdbc_conn = null;
        final String ip = ip2;
        boolean result = true;
        boolean doImportOnlyPrices = bImportOnlyPrices;
        BufferedReader csvReader = null;
        ResultSet rs = null;
        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
        }
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);
            //csvReader = new BufferedReader(new FileReader(csvFile));

            /*jdbc_conn = DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/"+csvFile,
        "SYSDBA", "masterkey");*/
            String driver_conn_string = "";

            if (ConfigProperties.ConfigFile.getImportDBType().equals(ConfigProperties.ConfigFile.DBType.FIREBIRD)) {
                driver_conn_string = "jdbc:firebirdsql://" + ConfigProperties.ConfigFile.getImportDBIP()
                        + ":" + ConfigProperties.ConfigFile.getImportDBPort()
                        + "/" + csvFile;
                Class.forName("org.firebirdsql.jdbc.FBDriver");
            } else if (ConfigProperties.ConfigFile.getImportDBType().equals(ConfigProperties.ConfigFile.DBType.POSTGRESQL)) {
                driver_conn_string = "jdbc:postgresql://" + ConfigProperties.ConfigFile.getImportDBIP()
                        + ":" + ConfigProperties.ConfigFile.getImportDBPort()
                        + "/" + ConfigProperties.ConfigFile.getImportDBName();
                Class.forName("org.postgresql.Driver");
            } else if (ConfigProperties.ConfigFile.getImportDBType().equals(ConfigProperties.ConfigFile.DBType.SQLSERVER)) {
                driver_conn_string = "jdbc:sqlserver://" + ConfigProperties.ConfigFile.getImportDBIP()
                        + ":" + ConfigProperties.ConfigFile.getImportDBPort()
                        + ";";
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } else if (ConfigProperties.ConfigFile.getImportDBType().equals(ConfigProperties.ConfigFile.DBType.MySQL)) {
                driver_conn_string = "jdbc:mysql://" + ConfigProperties.ConfigFile.getImportDBIP()
                        + ":" + ConfigProperties.ConfigFile.getImportDBPort()
                        + "/" + ConfigProperties.ConfigFile.getImportDBName();
                Class.forName("com.mysql.jdbc.Driver");
            } else if (ConfigProperties.ConfigFile.getImportDBType().equals(ConfigProperties.ConfigFile.DBType.SQLITE)) {
                driver_conn_string = "jdbc:firebirdsql://" + csvFile;
                Class.forName("org.sqlite.jdbc");
            }
            java.sql.Driver driver = java.sql.DriverManager.getDriver(driver_conn_string);

            java.util.Properties connectionProperties = new java.util.Properties();
            connectionProperties.put("user", ConfigProperties.ConfigFile.getImportDBUser());
            connectionProperties.put("password", ConfigProperties.ConfigFile.getImportDBPass());

            jdbc_conn = driver.connect(driver_conn_string, connectionProperties);
            System.out.println("Connection established.");
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando desde la DB, IP: "
                    + ConfigProperties.ConfigFile.getImportDBIP() + ", Port: "
                    + ConfigProperties.ConfigFile.getImportDBPort() + ", DB: "
                    + ConfigProperties.ConfigFile.getImportDBName() + ", User: "
                    + ConfigProperties.ConfigFile.getImportDBUser()
                    + "....\r\n Con error: " + e.toString());
            return false;
        }
        String questionmarks = "";
        String query_dpt, query_prod, query_prod_exist, query_prod_price, query_nut_info, query_sin_nut_info = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        String input;
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPluExist = null, psPrice = null, psNutInfo = null, psSinNutInfo = null;
        int count = 0;
        GeneralPlu gral_data = new GeneralPlu();
        con = this.connection;
        try {
            con.setAutoCommit(false);

//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            query_prod_exist = SQL_PRODUCT_EXIST;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC,
             the_extra_field1 CHARACTER VARYING,
             the_extra_field2 CHARACTER VARYING
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

            questionmarks = "?";
            query_prod_exist = query_prod_exist.replaceFirst(VALUES_REGEX, questionmarks);
            psPluExist = con.prepareStatement(query_prod_exist);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

//-------------------------SQL_NUT_INFO------------------
            query_nut_info = SQL_NUT_INFO;
            questionmarks = "?,?,?,?,?,?";
            query_nut_info = query_nut_info.replaceFirst(VALUES_REGEX, questionmarks);
            psNutInfo = con.prepareStatement(query_nut_info);

            query_sin_nut_info = "UPDATE product SET nut_info_set_id=null WHERE product_id=?";
            psSinNutInfo = con.prepareStatement(query_sin_nut_info);

            if (jdbc_conn == null) // Loguear mensaje de error.
            {
                return false;
            }
            // Esto es para poder mover el cursor al final, obtener la cantidad de filas y arrancar al principio.
            java.sql.Statement query_st = jdbc_conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            java.sql.ResultSet query_rs = query_st.executeQuery(ConfigProperties.ConfigFile.getImportDBQuery());

            if (query_rs.last()) {
                this.pb.setMaximum(query_rs.getRow());
                query_rs.beforeFirst();
            } else {
                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se pudo obtener la cantidad productos a importar");
                return false;
            }

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());
            while (query_rs.next()) {
                //System.out.println (query_rs.getString ("full_name"));
                //this.pb.revalidate();
                duration = new Duration(timeStart, timeStart.now());

                String entrada = "";
                /* for (String cell : input) {
                    entrada += cell;
                }*/
                count++;
                this.pb.setValue(count);
                this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                //this.pb.setString( duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                if (gral_data.parseInputPVMex(query_rs)) { // Acá se hace el parseo de los datos y se guarda en la clase ibdata
                    if (doImportOnlyPrices) {
                        //si SOLO QUIERE PRECIOS
                        //-----------------------SQL_PLU_EXIST-------------------------
                        psPluExist.setLong(1, gral_data.codigo_plu);
                        rs = psPluExist.executeQuery();
                        if (!rs.next()) {
                            //NO ESTA!!!!, importo todo
                            //-----------------------SQL_DEPARTMENT--------------------
                            gral_data.doDepartments(psDpt);
                            //-----------------------SQL_PRODUCT-----------------------
                            gral_data.doProducts(psPlu, ip);
                            //-----------------------SQL_PRICE-------------------------
                            gral_data.doPrices(psPrice);
                            //-----------------------SQL_NUT_INFO-------------------------
                            gral_data.doNutInfo(psNutInfo);
                        } else {
                            // LO TENGO!!!!, sólo actualizo el precio
                            //-----------------------SQL_PRICE-------------------------
                            gral_data.doPrices(psPrice);
                        }
                    } else {
                        //VA IMPORTACION FULL
                        //-----------------------SQL_DEPARTMENT--------------------
                        gral_data.doDepartments(psDpt);
                        //-----------------------SQL_PRODUCT-----------------------
                        gral_data.doProducts(psPlu, ip);
                        //-----------------------SQL_PRICE-------------------------
                        gral_data.doPrices(psPrice);
                        //-----------------------SQL_NUT_INFO-------------------------
                        gral_data.doNutInfo(psNutInfo);
                    }
                    if (gral_data.cod_inf_nut == 0) {
                        psSinNutInfo.setLong(1, gral_data.codigo_plu);
                        psSinNutInfo.execute();
                    }
                    con.commit();
                    gral_data.vaciarElementos();
                } else {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Formato inesperado en la linea: " + count);
                    //customLogger.getInstance().writeLog(MainApp.ERROR, this.ip, input);
                }
            }
        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
        } finally {
            rs = null;
            csvReader = null;
            input = null;
            psDpt = null;
            psPlu = null;
            psPluExist = null;
            psPrice = null;
            psNutInfo = null;
            gral_data = null;

            try {
                con.close();
                con = null;
                connection.close();
                connection = null;

            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        return result;
    }

    /*
     * Entranda a la función de importación de los formatos de etiquetas Systel
     *  
     * @return si fue exitoso o no
     *
     */
    public static HashMap<String, String> map_label_format = new HashMap<String, String>();

    public static HashMap<String, String> getMap_label_format() {
        return map_label_format;
    }

    public boolean loadLabelsData(String csvFile, String ip2) {
        final String ip = ip2;
        BufferedReader csvReader = null;
        String sCharset = "UTF-8";

        try {
            sCharset = detectCharset(csvFile);
            if (sCharset != null) {
                System.out.println("Detected encoding: " + sCharset);
            } else {
                System.out.println("No encoding detected. Using default encoding.");
                sCharset = "UTF-8";
            }
        } catch (Exception e) {
            sCharset = "UTF-8";
            e.printStackTrace();
        }

        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
        }
        customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);
        try {
            csvReader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), Charset.forName(sCharset)));

        } catch (FileNotFoundException ex) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + ex.toString());
        }

        try {

            String input;
            Duration duration = null;
            DateTime timeStart = new DateTime();

            Connection con = this.connection;
            con.setAutoCommit(true);

            CallableStatement psLabel = null;
            String questionmarks = "?,?,?,?";

            String query_label = SQL_LABELS;

            String callFunction = "{ ? = call public.merge_label_format(?, ?, ?, ?) }";

            query_label = query_label.replaceFirst(VALUES_REGEX, questionmarks);
            psLabel = con.prepareCall(callFunction);
            psLabel.registerOutParameter(1, Types.VARCHAR);

            int count = 0;
            GeneralPlu gralPLU = new GeneralPlu();
            while ((input = csvReader.readLine()) != null) {
                duration = new Duration(timeStart, timeStart.now());

                count++;
                this.pb.setValue(count);

                this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                boolean parseResult = false;
                parseResult = gralPLU.parseInputLabelSystel(input);

                if (parseResult) {
                    psLabel.setString(2, gralPLU.label_ID);
                    psLabel.setString(3, gralPLU.label_name);
                    psLabel.setString(4, gralPLU.label_template);
                    psLabel.setInt(5, gralPLU.label_type);
                    psLabel.execute();

                    // Recibir el resultado
                    String resultId = psLabel.getString(1);
                    if (resultId.equalsIgnoreCase(gralPLU.label_ID)) {
                        if (!map_label_format.containsKey(gralPLU.label_name)) {
                            getMap_label_format().put(gralPLU.label_name, gralPLU.label_ID);
                        }
                    } else {
                        if (!map_label_format.containsKey(gralPLU.label_name)) {
                            getMap_label_format().put(gralPLU.label_name, resultId);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Entranda a la función de importación SYSTEL COTO DIGI SDR
     *
     * @param
     *
     * @return si fue exitoso o no
     *
     */
    public boolean loadCSV(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices,
            ConfigFile.FileType ftype, String csvFileRanges) {

        setFtype(ftype);
        final String ip = ip2;
        boolean result = true;
        boolean doImportOnlyPrices = bImportOnlyPrices;
        BufferedReader csvReader = null;
        BufferedReader csvReaderRanges = null;
        ResultSet rs = null;
        String sCharset = "UTF-8";
        int iFilesRangeCount = 0;
        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
        }

        try {
            sCharset = detectCharset(csvFile);
            if (sCharset != null) {
                System.out.println("Detected encoding: " + sCharset);
            } else {
                //sCharset = "UTF-8";
                sCharset = "ISO-8859-1";
                System.out.println("No encoding detected. Using default encoding: " + sCharset);
            }
        } catch (Exception e) {
            sCharset = "UTF-8";
            System.out.println("Error encoding detected. Using default encoding: " + sCharset);
            e.printStackTrace();
        }
        try {

            switch (ftype) {
                case SYSTEL:
                    if (!ConfigFile.getSeparator().equalsIgnoreCase("Ã½") && !ConfigFile.getSeparator().equalsIgnoreCase("ý")) {
                        //sCharset = "UTF-8";
                    } else {
                        ConfigFile.setSeparator("ý");
                    }
                    break;
                case ANONIMA:
                    sCharset = "UTF-8";
                    break;

                default:
                    //                   sCharset = "ISO-8859-1";
                    break;
            }
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);
            csvReader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), Charset.forName(sCharset)));

            try {

                if (!csvFileRanges.trim().isEmpty()) {
                    customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFileRanges);
                    csvReaderRanges = new BufferedReader(new FileReader(csvFileRanges));
                    iFilesRangeCount = (int) (Files.lines(Paths.get(csvFileRanges), Charset.forName(sCharset)).count() - 1);
                }
            } catch (Exception e) {
                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFileRanges + ": " + e.toString());
                csvReaderRanges = null;
            }
            // String[] line;
            //csvReader = new CSVReader(new FileReader(csvFile), this.seprator);
        } catch (Exception e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
        }
        String questionmarks = "";
        String query_dpt, query_dep_exist, query_group, query_group_exist, query_prod, query_prod_exist, query_prod_price, query_nut_info, query_sin_nut_info, query_con_nut_info, query_discount = "", query_discount_delete_all = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        String input;
        Connection con = null;
        PreparedStatement psDpt = null, psGrp = null, psPlu = null, psPluExist = null, psPrice = null, psNutInfo = null, psSinNutInfo = null, psConNutInfo = null, psDepExist = null, psGrpExist = null, psDiscount = null, psDiscountDelAll = null;
        int count = 0;
        //      switch (ftype) {
        //          case SYSTELRDC429:
        IBPlu IBPlu = new IBPlu();
        //          default:
        GeneralPlu gralPLU = new GeneralPlu();
        //      }

        con = this.connection;
        try {
            con.setAutoCommit(false);

//-----------------------SQL_DEPARTMENT--------------------
            query_dep_exist = SQL_DEPARTMENT_EXIST;
            questionmarks = "?";
            query_dep_exist = query_dep_exist.replaceFirst(VALUES_REGEX, questionmarks);
            psDepExist = con.prepareStatement(query_dep_exist);

            query_dpt = SQL_DEPARTMENT;

//-----------------------SQL_GROUP--------------------
            query_group_exist = SQL_GROUP_EXIST;
            questionmarks = "?";
            query_group_exist = query_group_exist.replaceFirst(VALUES_REGEX, questionmarks);
            psGrpExist = con.prepareStatement(query_group_exist);

            query_group = SQL_GROUP;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            switch (ftype) {
                case COTO:
                    questionmarks = "?,?";
                    break;
                case DIGI:
                    questionmarks = "?,?";
                    break;
                case SDR:
                    questionmarks = "?,?";
                    break;
                case SYSTEL:
                    questionmarks = "?";
                    break;
                case SYSTELRDC429:
                    questionmarks = "?";
                    break;
                case BIZERBA:
                    questionmarks = "?";
                    break;
                case SICAR:
                    questionmarks = "?";
                    break;
                case MT8450:
                    questionmarks = "?";
                    break;
                case ANONIMA:
                    questionmarks = "?,?";
                    break;
            }
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

            query_group = query_group.replaceFirst(VALUES_REGEX, questionmarks);
            psGrp = con.prepareStatement(query_group);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            if (ftype.equals(COTO)) {
                query_prod = SQL_PRODUCT_COTO;
            } else {
                query_prod = SQL_PRODUCT;
            }

            query_prod_exist = SQL_PRODUCT_EXIST;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC,
             the_extra_field1 CHARACTER VARYING,
             the_extra_field2 CHARACTER VARYING
             */
            if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.ANONIMA) {
                questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            } else if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.COTO) {
                questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            } else if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTEL) {
                questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            } else if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTELRDC429) {
                questionmarks = "?,?";
            } else if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.MT8450) {
                questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            } else {
                questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            }
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

            questionmarks = "?";
            query_prod_exist = query_prod_exist.replaceFirst(VALUES_REGEX, questionmarks);
            psPluExist = con.prepareStatement(query_prod_exist);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

//-------------------------SQL_NUT_INFO------------------
            query_nut_info = SQL_NUT_INFO;
//            if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTELRDC429) {
//                questionmarks = "?,?,?,?,?,?";
//            } else {
            questionmarks = "?,?,?,?,?,?";
//            }
            query_nut_info = query_nut_info.replaceFirst(VALUES_REGEX, questionmarks);
            psNutInfo = con.prepareStatement(query_nut_info);

            query_sin_nut_info = "UPDATE product SET nut_info_set_id=null WHERE product_id=?";
            psSinNutInfo = con.prepareStatement(query_sin_nut_info);

            query_con_nut_info = "UPDATE product SET nut_info_set_id=? WHERE product_id=?";
            psConNutInfo = con.prepareStatement(query_con_nut_info);

//-------------------------SQL_DISOUNT_SCHEMA------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_discount = SQL_DISCOUNT;
            questionmarks = "?,?,?";
            query_discount = query_discount.replaceFirst(VALUES_REGEX, questionmarks);
            psDiscount = con.prepareStatement(query_discount);

            query_discount_delete_all = SQL_DISCOUNT_DELETE_ALL;
            psDiscountDelAll = con.prepareStatement(query_discount_delete_all);

            int maxLines = ((int) (Files.lines(Paths.get(csvFile), Charset.forName(sCharset)).count() - 1)) + iFilesRangeCount;
            this.pb.setMaximum(maxLines);

            customLogger.getInstance().writeLog(MainApp.INFO, ip, "TOTAL de líneas a importar:" + maxLines);
            System.out.println("WORKING...    - " + ip + " - TOTAL de líneas a importar:" + maxLines);
            customLogger.getInstance().writeLog(MainApp.INFO, ip, "COMENZANDO A IMPORTAR AGUARDE... ");
            System.out.println(ip + " - COMENZANDO A IMPORTAR AGUARDE... ");
            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            while ((input = csvReader.readLine()) != null) {
                //this.pb.revalidate();
                duration = new Duration(timeStart, timeStart.now());

                /* for (String cell : input) {
                    entrada += cell;
                }*/
                count++;

                this.pb.setValue(count);

                //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "CANTIDAD IMPORTADA " + count);
                //System.out.println(this.pb.getValue()+" - "+this.pb.getMaximum() );
                this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                //this.pb.setString( duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                boolean parseResult = false;
                switch (ftype) {
                    case COTO:
                        parseResult = gralPLU.parseInputCoto(input);
                        break;
                    case DIGI:
                        parseResult = gralPLU.parseInputDigi(input);
                        break;
                    case SDR:
                        parseResult = gralPLU.parseInputSDR(input);
                        break;
                    case SYSTEL:
                        parseResult = gralPLU.parseInputSystel(input);

                        //si el formato agrega campos y usa otro store procedure, modificamos la cantidad a demanda, acá
                        String[] input_parsed = input.split("\\" + ConfigFile.getSeparator());
                        if (input_parsed.length < 32) {
                            questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
                        } else if (input_parsed.length == 32) {
                            questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
                        } else {
                            questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
                        }
                        query_prod = SQL_PRODUCT;
                        psPlu = null;
                        query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
                        psPlu = con.prepareStatement(query_prod);
                        break;
                    case SYSTELRDC429:
                        parseResult = IBPlu.parseInputSystelRDC429(input);
                        break;
                    case BIZERBA:
                        parseResult = gralPLU.parseInputBizerba(input);
                        break;
                    case SICAR:
                        parseResult = gralPLU.parseInputSicar(input);
                        break;
                    case MT8450:
                        parseResult = gralPLU.parseInputMT8450(input);
                        break;
                    case ANONIMA:
                        parseResult = gralPLU.parseInputAnonima(input);
                        break;
                }
                if (parseResult) { // Acá se hace el parseo de los datos y se guarda en la clase ibdata
                    if (doImportOnlyPrices) {
                        //si SOLO QUIERE PRECIOS
                        //-----------------------SQL_PLU_EXIST-------------------------
                        psPluExist.setLong(1, gralPLU.codigo_plu);
                        rs = psPluExist.executeQuery();
                        if (!rs.next()) {
                            //NO ESTA!!!!, importo todo
                            //-----------------------SQL_DEPARTMENT--------------------
                            gralPLU.doDepartments(psDpt);
                            switch (ftype) {
                                case SYSTEL:
                                    psDepExist.setString(1, gralPLU.seccion_nombre.toUpperCase().trim());
                                    rs = psDepExist.executeQuery();
                                    if (rs.next()) {
                                        //ESTÁ, tomo el ID para usarlo luego
                                        gralPLU.seccion = rs.getInt("department_id");
                                    }
                                    break;
                            }
                            //-----------------------SQL_GROUP--------------------
                            gralPLU.doGroups(psGrp);
                            switch (ftype) {
                                case SYSTEL:
                                    psGrpExist.setString(1, gralPLU.grupo_nombre.toUpperCase().trim());
                                    rs = psGrpExist.executeQuery();
                                    if (rs.next()) {
                                        //ESTÁ, tomo el ID para usarlo luego
                                        gralPLU.grupo = rs.getInt("group_id");
                                    } else {
                                        gralPLU.grupo = 0;
                                    }
                                    break;
                            }
                            //-----------------------SQL_PRODUCT-----------------------
                            gralPLU.doProducts(psPlu, ip);
                            //-----------------------SQL_PRICE-------------------------
                            gralPLU.doPrices(psPrice);
                            //-----------------------SQL_NUT_INFO-------------------------
                            gralPLU.doNutInfo(psNutInfo);
                        } else {
                            // LO TENGO!!!!, sólo actualizo el precio
                            //-----------------------SQL_PRICE-------------------------
                            gralPLU.doPrices(psPrice);
                        }
                    } else {
                        //VA IMPORTACION FULL
                        switch (ftype) {
                            case COTO:
                                //importación COTO
                                //-----------------------SQL_DEPARTMENT--------------------
                                gralPLU.doDepartments(psDpt);
                                //-----------------------SQL_PRODUCT--------------------
                                gralPLU.doProducts(psPlu, ip);
                                //-----------------------SQL_PRICE----------------------
                                gralPLU.doPrices(psPrice);
                                //-----------------------SQL_NUT_INFO-------------------
                                gralPLU.doNutInfo3(psNutInfo);
                                //-----------------------SQL_RANGO_PRECIO---------------
                                //gralPLU.doDiscounts(psDiscount);
                                if (gralPLU.cod_inf_nut == 0) {
                                    psSinNutInfo.setLong(1, gralPLU.codigo_plu);
                                    psSinNutInfo.execute();
                                } else {
                                    psConNutInfo.setString(1, gralPLU.info_set_id);
                                    psConNutInfo.setLong(2, gralPLU.codigo_plu);
                                    psConNutInfo.execute();
                                }
                                break;
                            case SYSTELRDC429:
                            //importación RDC429
                            try {
                                //-----------------------SQL_PRODUCT--------------------
                                IBPlu.doProductsRDC429(psPlu);

                                //-----------------------SQL_NUT_INFO-------------------
                                IBPlu.doNutInfo2(psNutInfo);
                            } catch (Exception e) {
                                try {
                                    con.rollback();
                                } catch (SQLException ex) {
                                    Logger.getLogger(FileLoader.class
                                            .getName()).log(Level.SEVERE, null, ex);
                                }
                                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
                            }
                            break;
                            default:
                                //importación "standart"
                                //-----------------------SQL_DEPARTMENT--------------------
                                gralPLU.doDepartments(psDpt);
                                switch (ftype) {
                                    case SYSTEL:
                                        psDepExist.setString(1, gralPLU.seccion_nombre.toUpperCase().trim());
                                        rs = psDepExist.executeQuery();
                                        if (rs.next()) {
                                            //ESTÁ, tomo el ID para usarlo luego
                                            gralPLU.seccion = rs.getInt("department_id");
                                        }
                                        break;
                                }

                                //-----------------------SQL_GROUP--------------------
                                gralPLU.doGroups(psGrp);
                                switch (ftype) {
                                    case SYSTEL:
                                        psGrpExist.setString(1, gralPLU.grupo_nombre.toUpperCase().trim());
                                        rs = psGrpExist.executeQuery();
                                        if (rs.next()) {
                                            //ESTÁ, tomo el ID para usarlo luego
                                            gralPLU.grupo = rs.getInt("group_id");

                                        } else {
                                            gralPLU.grupo = 0;
                                        }

                                        break;
                                }
                                //-----------------------SQL_PRODUCT--------------------
                                gralPLU.doProducts(psPlu, ip);
                                //-----------------------SQL_PRICE----------------------
                                gralPLU.doPrices(psPrice);
                                //-----------------------SQL_NUT_INFO-------------------

                                //-----------------------SQL_NUT_INFO-------------------
                                if (gralPLU.cod_inf_nut == 0) {
                                    psSinNutInfo.setLong(1, gralPLU.codigo_plu);
                                    psSinNutInfo.execute();
                                } else {
                                    psConNutInfo.setString(1, gralPLU.info_set_id);
                                    psConNutInfo.setLong(2, gralPLU.codigo_plu);
                                    psConNutInfo.execute();

                                    if (gralPLU.info_set_id.equalsIgnoreCase("3")) {
                                        gralPLU.doNutInfo3(psNutInfo);
                                    } else if (gralPLU.info_set_id.equalsIgnoreCase("1")) {
                                        gralPLU.doNutInfo(psNutInfo);
                                    }
                                }
                                //-----------------------SQL_RANGO_PRECIO---------------
                                gralPLU.doDiscounts(psDiscount);
                                break;
                        }
                    }
                    con.commit();

                    switch (ftype) {
                        case SYSTELRDC429:
                            IBPlu.vaciarElementos();
                            break;
                        default:
                            gralPLU.vaciarElementos();
                    }
                } else {
                    switch (ftype) {
                        case SYSTELRDC429:
                            break;
                        case ANONIMA:
                            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Formato inesperado en la linea: " + count);
                            break;
                        default:
                            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Formato inesperado en la linea: " + count);
                            customLogger.getInstance().writeLog(MainApp.ERROR, ip, input);
                            break;
                    }
                }

                System.out.println("WORKING...    - " + ip + " - Importando la línea:" + count);

            }

            /**
             * *********************************
             * IMPORTACION DE RANGOS DE PRECIOS *
             * *********************************
             */
            if (csvReaderRanges != null) {
                int count2 = 0;
                try {
                    customLogger.getInstance().writeLog(MainApp.INFO, ip, "IMPORTA RANGOS X PRECIOS");
                    customLogger.getInstance().writeLog(MainApp.INFO, ip, "Borramos todos los rangos x precio");
                    gralPLU.vaciarElementos();
                    gralPLU.doDeleteSchemaDiscount(psDiscountDelAll);
                    con.commit();

                    input = "";

                    while ((input = csvReaderRanges.readLine()) != null) {
                        duration = new Duration(timeStart, timeStart.now());

                        count2++;
                        this.pb.setValue(count + count2);
                        this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                        boolean parseResult = false;
                        switch (ftype) {
                            case COTO:
                                //parseResult = gralPLU.parseInputCoto(input);
                                break;
                            case DIGI:
                                //parseResult = gralPLU.parseInputDigi(input);
                                break;
                            case SDR:
                                //parseResult = gralPLU.parseInputSDR(input);
                                break;
                            case SYSTEL:
                                parseResult = gralPLU.parseInputSystelRangos(input);
                                break;
                        }
                        if (parseResult) { // Acá se hace el parseo de los datos y se guarda en la clase gralPLU
                            //-----------------------SQL_DISCOUNT_SCHEMA--------------------
                            gralPLU.doDiscounts(psDiscount);
                            con.commit();
                            gralPLU.vaciarElementos();
                        } else {
                            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "RANGOS X PRECIOS Formato inesperado en la linea: " + count2);
                            customLogger.getInstance().writeLog(MainApp.ERROR, ip, input);
                        }
                    }
                } catch (SQLException e) {
                    result = false;
                    try {
                        con.rollback();
                    } catch (SQLException ex) {
                        Logger.getLogger(FileLoader.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "RANGOS X PRECIOS  En la importación: " + e.toString());
                } catch (Exception e) {
                    result = false;
                    try {
                        con.rollback();
                    } catch (SQLException ex) {
                        Logger.getLogger(FileLoader.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "RANGOS X PRECIOS  Al leer la línea: " + count2 + " Mensaje: " + e.toString());
                }
            }
        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPluExist) {
                    psPluExist.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != psNutInfo) {
                    psNutInfo.close();
                }
                if (null != psSinNutInfo) {
                    psSinNutInfo.close();
                }
                if (null != psDepExist) {
                    psDepExist.close();
                }
                if (null != psDiscount) {
                    psDiscount.close();
                }
                if (null != psDiscountDelAll) {
                    psDiscountDelAll.close();
                }
                if (null != rs) {
                    rs.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != connection) {
                    connection.close();
                }
                csvReader.close();
                if (csvReaderRanges != null) {
                    csvReaderRanges.close();
                }

                rs = null;
                input = null;
                psDpt = null;
                psPlu = null;
                psPluExist = null;
                psPrice = null;
                psNutInfo = null;
                psSinNutInfo = null;
                psDepExist = null;
                psDiscount = null;
                psDiscountDelAll = null;
                con = null;
                connection = null;
                csvReader = null;
                csvReaderRanges = null;
                IBPlu = null;
                gralPLU = null;

            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public boolean loadCSVSDR(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices) {

        final String ip = ip2;
        boolean doImportOnlyPrices = bImportOnlyPrices;

        boolean result = true;
        Iterator<String> csvReader = null;
        Iterator<String> csv_nut_info = null;
        Iterator<String> csv_receing = null;
        Iterator<String> csv_tara = null;
        Iterator<String> csv_conservacion = null;
        Iterator<String> csv_extra2 = null;
        Iterator<String> csv_ingredients = null;
        Iterator<String> csv_ingredients2 = null;
        Path filePath = null;
        Stream<String> stream = null;

        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }

        if (ip.equalsIgnoreCase("localhost")) {
            try {
                Statement stmt = connection.createStatement();
                String importDBQuery = "select delete_all_PLU();";
                stmt.executeQuery(importDBQuery);
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);

            try {
                filePath = Paths.get(csvFile);
                stream = Files.lines(filePath, StandardCharsets.ISO_8859_1);
                csvReader = stream.iterator();
            } catch (Exception e) {
                result = false;
                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
                ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
            }

            if (!ConfigFile.getItensMGVNutInfoFilePath().isEmpty()) {
                try {
                    filePath = Paths.get(ConfigFile.getItensMGVNutInfoFilePath());
                    stream = Files.lines(filePath, StandardCharsets.ISO_8859_1);
                    csv_nut_info = stream.iterator();
                } catch (Exception e) {
                    result = false;
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + ConfigFile.getItensMGVNutInfoFilePath() + ": " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                }
            }
            if (!ConfigFile.getItensMGVReceingFilePath().isEmpty()) {
                try {
                    filePath = Paths.get(ConfigFile.getItensMGVReceingFilePath());
                    stream = Files.lines(filePath, StandardCharsets.ISO_8859_1);
                    csv_receing = stream.iterator();
                } catch (Exception e) {
                    result = false;
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + ConfigFile.getItensMGVReceingFilePath() + ": " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                }
            }
            if (!ConfigFile.getItensMGVTaraFilePath().isEmpty()) {
                try {
                    filePath = Paths.get(ConfigFile.getItensMGVTaraFilePath());
                    stream = Files.lines(filePath, StandardCharsets.ISO_8859_1);
                    csv_tara = stream.iterator();
                } catch (Exception e) {
                    result = false;
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + ConfigFile.getItensMGVTaraFilePath() + ": " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                }
            }
            if (!ConfigFile.getItensMGVConservacionFilePath().isEmpty()) {
                try {
                    filePath = Paths.get(ConfigFile.getItensMGVConservacionFilePath());
                    stream = Files.lines(filePath, StandardCharsets.ISO_8859_1);
                    csv_conservacion = stream.iterator();
                } catch (Exception e) {
                    result = false;
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + ConfigFile.getItensMGVConservacionFilePath() + ": " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                }
            }
            if (!ConfigFile.getItensMGVExtra2FilePath().isEmpty()) {
                try {
                    filePath = Paths.get(ConfigFile.getItensMGVExtra2FilePath());
                    stream = Files.lines(filePath, StandardCharsets.ISO_8859_1);
                    csv_extra2 = stream.iterator();
                } catch (Exception e) {
                    result = false;
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + ConfigFile.getItensMGVExtra2FilePath() + ": " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                }
            }
            if (!ConfigFile.getItensMGVIngredientsFilePath().isEmpty()) {
                try {
                    filePath = Paths.get(ConfigFile.getItensMGVIngredientsFilePath());
                    stream = Files.lines(filePath, StandardCharsets.ISO_8859_1);
                    csv_ingredients = stream.iterator();
                } catch (Exception e) {
                    result = false;
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + ConfigFile.getItensMGVIngredientsFilePath() + ": " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                }
            }
            if (!ConfigFile.getItensMGVIngredientsFilePath2().isEmpty()) {
                try {
                    filePath = Paths.get(ConfigFile.getItensMGVIngredientsFilePath2());
                    stream = Files.lines(filePath, StandardCharsets.ISO_8859_1);
                    csv_ingredients2 = stream.iterator();

                } catch (Exception e) {
                    result = false;
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + ConfigFile.getItensMGVIngredientsFilePath2() + ": " + e.toString());
                    ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                }
            }

        } catch (Exception e) {
            result = false;
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

//        String query_dpt, query_prod, query_prod_price = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
//        query = query.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
//        query = query.replaceFirst(VALUES_REGEX, questionmarks);
        // System.out.println("Query: " + query);
        String nextLine = null;
//        Connection con = null;
//        PreparedStatement psDpt = null, psPlu = null, psPrice = null;

        String query_dpt, query_prod, query_prod_exist, query_prod_price, query_nut_info, query_receing, query_tara, query_conservacion, query_extra2, query_ingredientes = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        String[] input;
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPluExist = null, psPrice = null, psNutInfo = null, psReceing = null, psTara = null, psConservacion = null, psExtra2 = null, psIngredientes = null;
        ResultSet rs = null;

        int count = 0;
        IBPlu ibdata = new IBPlu();
        con = this.connection;

        QendraHandler qendra_handler = new QendraHandler();
        if (ConfigFile.useQendra()) {
            if (!ConfigFile.useQendraDefPath()) {
                try {
                    qendra_handler.connectDB(ConfigFile.getQendraPath());
                } catch (ClassNotFoundException ex) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se encuentra el archivo: " + ConfigFile.getQendraPath() + "\n Con error: " + ex.toString());
                }
            } else {
                try {
                    qendra_handler.connectDB(ConfigFile.QENDRA_DEFAULT_PATH);
                } catch (ClassNotFoundException ex) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se encuentra el archivo: " + ConfigFile.QENDRA_DEFAULT_PATH + "\n Con error: " + ex.toString());
                }
            }
        }

        try {
            con.setAutoCommit(false);
//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            query_prod_exist = SQL_PRODUCT_EXIST;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

            this.pb.setMaximum((int) (Files.lines(Paths.get(csvFile), Charset.forName("ISO-8859-1")).count() - 1));
            if (csv_nut_info != null) {
                this.pb.setMaximum((int) (Files.lines(Paths.get(ConfigFile.getItensMGVNutInfoFilePath()), Charset.forName("ISO-8859-1")).count() - 1) + this.pb.getMaximum());
            }
            if (csv_receing != null) {
                this.pb.setMaximum((int) (Files.lines(Paths.get(ConfigFile.getItensMGVReceingFilePath()), Charset.forName("ISO-8859-1")).count() - 1) + this.pb.getMaximum());
            }
            if (csv_tara != null) {
                this.pb.setMaximum((int) (Files.lines(Paths.get(ConfigFile.getItensMGVTaraFilePath()), Charset.forName("ISO-8859-1")).count() - 1) + this.pb.getMaximum());
            }
            if (csv_conservacion != null) {
                this.pb.setMaximum((int) (Files.lines(Paths.get(ConfigFile.getItensMGVConservacionFilePath()), Charset.forName("ISO-8859-1")).count() - 1) + this.pb.getMaximum());
            }
            if (csv_extra2 != null) {
                this.pb.setMaximum((int) (Files.lines(Paths.get(ConfigFile.getItensMGVExtra2FilePath()), Charset.forName("ISO-8859-1")).count() - 1) + this.pb.getMaximum());
            }
            if (csv_ingredients != null) {
                this.pb.setMaximum((int) (Files.lines(Paths.get(ConfigFile.getItensMGVIngredientsFilePath()), Charset.forName("ISO-8859-1")).count() - 1) + this.pb.getMaximum());
            }
            if (csv_ingredients2 != null) {
                this.pb.setMaximum((int) (Files.lines(Paths.get(ConfigFile.getItensMGVIngredientsFilePath2()), Charset.forName("ISO-8859-1")).count() - 1) + this.pb.getMaximum());
            }

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            HashMap<Long, List<Long>> map_nut_info = new HashMap<Long, List<Long>>();
            HashMap<Long, List<Long>> map_receing = new HashMap<Long, List<Long>>();
            HashMap<Integer, List<Long>> map_tara = new HashMap<Integer, List<Long>>();
            HashMap<Integer, List<Long>> map_conservacion = new HashMap<Integer, List<Long>>();
            HashMap<Integer, List<Long>> map_extra2 = new HashMap<Integer, List<Long>>();
            HashMap<Integer, List<Long>> map_ingredietes = new HashMap<Integer, List<Long>>();
            HashMap<Integer, List<Long>> map_ingredietes2 = new HashMap<Integer, List<Long>>();
            boolean success = false;

            // ******* LECTURA ARCHIVO DE NOVEDADES ******* //
            while (csvReader.hasNext()) {
                try {
                    nextLine = csvReader.next();

                    duration = new Duration(timeStart, timeStart.now());

                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                    success = false;
                    if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.INTELLIBUILD.toString())) {
                        success = ibdata.parseInputSDR(nextLine);
                    } else if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV.toString())
                            || ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV_DEPTO.toString())) {
                        success = ibdata.parseInputItensMGV(nextLine, map_nut_info, map_receing, map_conservacion, map_extra2, map_ingredietes, map_tara, map_ingredietes2);
                    }

                    if (success) { // Acá se hace el parseo de los datos y se guarda en la clase ibdata

                        //VA IMPORTACION FULL
                        //-----------------------SQL_DEPARTMENT--------------------
                        if (!ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV_DEPTO.toString())) {
                            ibdata.doDepartments(psDpt);
                        }
                        //-----------------------SQL_PRODUCT-----------------------
                        ibdata.doProductsItensMGV(psPlu);
                        //-----------------------SQL_PRICE-------------------------
                        ibdata.doPrices(psPrice);
                        //-----------------------SQL_NUT_INFO-------------------------
                        //doNutInfo(psNutInfo, ibdata);

                        con.commit();

                        // Added by GCastillo
                        if (ConfigFile.useQendra()) {
                            qendra_handler.InsertProduct(ibdata, ibdata.codigo);
                        }
                        ibdata.vaciarElementos();
                    } else {
                        customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Formato inesperado en la linea: " + count);
                    }
                } catch (Exception e) {
                    System.err.println("");
                }

            }

            // ********* IMPORTACION OTROS DATOSITENSMGV ********* //}
            if (csv_nut_info != null) {
                customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo nutricional:") + ConfigFile.getItensMGVNutInfoFilePath());
                System.out.println(ip + "  " + LocaleUtil.getMessage("Inicio importando el archivo nutricional:") + ConfigFile.getItensMGVNutInfoFilePath());
                query_nut_info = SQL_NUT_INFO;
                questionmarks = "?,?,?,?,?,?";
                query_nut_info = query_nut_info.replaceFirst(VALUES_REGEX, questionmarks);
                psNutInfo = con.prepareStatement(query_nut_info);

                PreparedStatement psUpdatePLUNutID = con.prepareStatement("UPDATE product SET nut_info_set_id = ? WHERE product_id=?");
                // Importamos NUT INFO.
                while (csv_nut_info.hasNext()) {
                    nextLine = csv_nut_info.next();
                    duration = new Duration(timeStart, timeStart.now());
                    count++;

                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                    success = false;
                    if (ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV.toString())
                            || ConfigProperties.ConfigFile.getFiletype().toString().equalsIgnoreCase(ConfigProperties.ConfigFile.FileType.ITENS_MGV_DEPTO.toString())) {
                        success = ibdata.parseInputNutInfoItensMGV(nextLine);
                    } else {
                        System.err.println("");
                    }
                    if (success) {
                        if (!map_nut_info.containsKey(ibdata.cod_inf_nut)) {
                            ibdata.vaciarElementos();
                            continue;
                        }
                        for (long cod_plu : map_nut_info.get(ibdata.cod_inf_nut)) {
                            ibdata.codigo = cod_plu;
                            if (ibdata.info_set_id.equals("2")) {
                                //MGV7 nuevo formato
                                ibdata.doNutInfo2(psNutInfo);
                            } else {
                                ibdata.doNutInfo(psNutInfo);
                            }
                            psUpdatePLUNutID.setString(1, ibdata.info_set_id);
                            psUpdatePLUNutID.setLong(2, cod_plu);
                            psUpdatePLUNutID.execute();
                        }
                    }
                    ibdata.vaciarElementos();
                }
                con.commit();
            }

            if (csv_receing != null) {
                customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo de recetas:") + ConfigFile.getItensMGVReceingFilePath());
                System.out.println(ip + "  " + LocaleUtil.getMessage("Inicio importando el archivo de recetas:") + ConfigFile.getItensMGVReceingFilePath());

                query_receing = SQL_RECEING;
                psReceing = con.prepareStatement(query_receing);

                // Importamos RECETAS.  
                while (csv_receing.hasNext()) {
                    try {
                        nextLine = csv_receing.next();
                    } catch (Exception e) {
                        System.err.println("");
                    }

                    duration = new Duration(timeStart, timeStart.now());
                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                    if (ibdata.parseInputReceingItensMGV(nextLine)) {
                        if (!map_receing.containsKey(ibdata.cod_inf_extra)) {
                            ibdata.vaciarElementos();
                            continue;
                        }
                        for (long cod_plu : map_receing.get(ibdata.cod_inf_extra)) {

                            psReceing.setString(1, ibdata.desc_campo_extra1);
                            psReceing.setLong(2, cod_plu);
                            psReceing.execute();
                        }
                    }
                    ibdata.vaciarElementos();
                }

                con.commit();
            }

            if (csv_tara != null) {
                customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo de taras:") + ConfigFile.getItensMGVTaraFilePath());
                System.out.println(ip + "  " + LocaleUtil.getMessage("Inicio importando el archivo de taras:") + ConfigFile.getItensMGVTaraFilePath());

                query_tara = SQL_TARA;
                psTara = con.prepareStatement(query_tara);

                // Importamos RECETAS.
                while (csv_tara.hasNext()) {
                    nextLine = csv_tara.next();
                    duration = new Duration(timeStart, timeStart.now());
                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                    if (ibdata.parseInputTaraMGV(nextLine)) {
                        if (!map_tara.containsKey(ibdata.cod_tara)) {
                            ibdata.vaciarElementos();
                            continue;
                        }
                        for (long cod_plu : map_tara.get(ibdata.cod_tara)) {
                            psTara.setFloat(1, Float.parseFloat(ibdata.valor_tara));
                            psTara.setLong(2, cod_plu);
                            psTara.execute();
                        }
                    }
                    ibdata.vaciarElementos();
                }
                con.commit();
            }

            if (csv_conservacion != null) {
                customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo de conservación:") + ConfigFile.getItensMGVConservacionFilePath());
                System.out.println(ip + "  " + LocaleUtil.getMessage("Inicio importando el archivo de conservación:") + ConfigFile.getItensMGVConservacionFilePath());

                query_conservacion = SQL_CONSERVACION;
                psConservacion = con.prepareStatement(query_conservacion);

                // Importamos CONSERVACIONES.
                while (csv_conservacion.hasNext()) {
                    nextLine = csv_conservacion.next();
                    duration = new Duration(timeStart, timeStart.now());
                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                    if (ibdata.parseInputConservacionItensMGV(nextLine)) {
                        if (!map_conservacion.containsKey(ibdata.cod_conservacion)) {
                            ibdata.vaciarElementos();
                            continue;
                        }
                        for (long cod_plu : map_conservacion.get(ibdata.cod_conservacion)) {

                            psConservacion.setString(1, ibdata.desc_conservacion);
                            psConservacion.setLong(2, cod_plu);
                            psConservacion.execute();
                        }
                    }
                    ibdata.vaciarElementos();
                }
                con.commit();
            }

            if (csv_extra2 != null) {
                customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo de campo extra 2:") + ConfigFile.getItensMGVExtra2FilePath());
                System.out.println(ip + "  " + LocaleUtil.getMessage("Inicio importando el archivo de campo extra 2:") + ConfigFile.getItensMGVExtra2FilePath());

                query_extra2 = SQL_EXTRA2;
                psExtra2 = con.prepareStatement(query_extra2);

                // Importamos Extra 2.
                while (csv_extra2.hasNext()) {
                    nextLine = csv_extra2.next();
                    duration = new Duration(timeStart, timeStart.now());
                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                    if (ibdata.parseInputExtra2MGV(nextLine)) {
                        if (!map_extra2.containsKey(ibdata.cod_campo_extra2)) {
                            ibdata.vaciarElementos();
                            continue;
                        }
                        for (long cod_plu : map_extra2.get(ibdata.cod_campo_extra2)) {
                            psExtra2.setString(1, ibdata.desc_campo_extra2);
                            psExtra2.setLong(2, cod_plu);
                            psExtra2.execute();
                        }
                    }
                    ibdata.vaciarElementos();
                }
                con.commit();
            }

            if (csv_ingredients != null) {
                customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo de ingredientes:") + ConfigFile.getItensMGVIngredientsFilePath());
                System.out.println(ip + "  " + LocaleUtil.getMessage("Inicio importando el archivo de ingredientes:") + ConfigFile.getItensMGVIngredientsFilePath());

                query_ingredientes = SQL_INGREDIENTS;
                psIngredientes = con.prepareStatement(query_ingredientes);

                while (csv_ingredients.hasNext()) {
                    nextLine = csv_ingredients.next();
                    duration = new Duration(timeStart, timeStart.now());
                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                    if (ibdata.parseInputIngredientesMGV(nextLine)) {
                        if (!map_ingredietes.containsKey(ibdata.cod_ingredientes)) {
                            ibdata.vaciarElementos();
                            continue;
                        }
                        for (long cod_plu : map_ingredietes.get(ibdata.cod_ingredientes)) {
                            psIngredientes.setString(1, ibdata.desc_ingredientes);
                            psIngredientes.setLong(2, cod_plu);
                            psIngredientes.execute();
                        }
                    }
                    ibdata.vaciarElementos();
                }
                con.commit();
            }

            if (csv_ingredients2 != null) {
                customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo de ingredientes:") + ConfigFile.getItensMGVIngredientsFilePath2());
                System.out.println(ip + "  " + LocaleUtil.getMessage("Inicio importando el archivo de ingredientes:") + ConfigFile.getItensMGVIngredientsFilePath2());

                if (csv_ingredients == null) {
                    query_ingredientes = SQL_INGREDIENTS;
                } else {
                    query_ingredientes = SQL_INGREDIENTS_ADD;
                }

                psIngredientes = con.prepareStatement(query_ingredientes);

                while (csv_ingredients2.hasNext()) {
                    nextLine = csv_ingredients2.next();
                    duration = new Duration(timeStart, timeStart.now());
                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));
                    if (ibdata.parseInputIngredientesMGV(nextLine)) {
                        if (!map_ingredietes2.containsKey(ibdata.cod_ingredientes)) {
                            ibdata.vaciarElementos();
                            continue;
                        }
                        for (long cod_plu : map_ingredietes2.get(ibdata.cod_ingredientes)) {
                            psIngredientes.setString(1, ibdata.desc_ingredientes);
                            psIngredientes.setLong(2, cod_plu);
                            psIngredientes.execute();
                        }
                    }
                    ibdata.vaciarElementos();
                }
                con.commit();
            }

        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
            System.out.println(ip + "En la importación: " + e.toString());

            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
            System.out.println(ip + "Al leer la línea: " + count + " Mensaje: " + e.toString());

            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != psNutInfo) {
                    psNutInfo.close();
                }
                if (null != psExtra2) {
                    psExtra2.close();
                }
                if (null != psIngredientes) {
                    psIngredientes.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != connection) {
                    connection.close();
                }

                rs = null;
                csvReader = null;
                input = null;
                psDpt = null;
                psPlu = null;

                psPrice = null;
                psNutInfo = null;
                psExtra2 = null;
                psIngredientes = null;
                ibdata = null;
                qendra_handler = null;

                con.close();
                con = null;
                connection.close();
                connection = null;

                System.gc();

            } catch (Exception ex) {
                rs = null;
                csvReader = null;
                input = null;
                psDpt = null;
                psPlu = null;

                psPrice = null;
                psNutInfo = null;
                psExtra2 = null;
                psIngredientes = null;
                ibdata = null;
                qendra_handler = null;

                csvReader = null;
                csv_nut_info = null;
                csv_receing = null;
                csv_tara = null;
                csv_conservacion = null;
                csv_extra2 = null;
                csv_ingredients = null;
                csv_ingredients2 = null;
                filePath = null;
                stream = null;

                con = null;
                connection = null;
                //System.gc();

                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
                ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
            }
        }

        return result;
    }

    public boolean loadEleventas(String importDBIP, String importDBName, String importDBPass, String importDBPort, String importDBQuery, String importDBType, String importDBUser, boolean b, String ip2) {

        final String ip = ip2;

        boolean result = true;

        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        EleventasFirebirdDBConnector efdbc = new EleventasFirebirdDBConnector();
        ResultSet rs = null;
        List<EleventasProducto> productos = null;
        try {

            //productos = efdbc.getPLUs(dbPath);
            productos = efdbc.getPLUs(importDBIP, importDBName, importDBPass, importDBPort, importDBQuery, importDBType, importDBUser);
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + importDBName);
            if (productos == null) {
                result = false;
                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se obtuvieron registros de: " + importDBName);
                ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                return result;
            }

        } catch (Exception e) {
            result = false;
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + importDBName + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

        String query_dpt, query_prod, query_prod_price; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPrice = null;

        int count = 0;
        GeneralPlu elevData = new GeneralPlu();
        con = this.connection;

        try {
            con.setAutoCommit(false);
//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

            this.pb.setMaximum(productos.size() - 1);

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            // Procesar la lista de productos
            for (EleventasProducto producto : productos) {
                duration = new Duration(timeStart, timeStart.now());
                count++;
                this.pb.setValue(count);
                this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                customLogger.getInstance().writeLog(MainApp.INFO, ip, "ROW:" + count + " - " + producto.toString());

                if (elevData.parseEleventas(
                        producto.getId(),
                        producto.getCodigo(),
                        producto.getDescripcion(),
                        producto.getPrecio(),
                        producto.getDepto(),
                        producto.getDeptoNombre(),
                        producto.getTipo()
                )) {

                    //VA IMPORTACION FULL
                    //-----------------------SQL_DEPARTMENT--------------------
                    elevData.doDepartments(psDpt);
                    //-----------------------SQL_PRODUCT-----------------------
                    elevData.doProductsEleventas(psPlu);
                    //-----------------------SQL_PRICE-------------------------
                    elevData.doPricesEleventas(psPrice);
                    //-----------------------SQL_NUT_INFO-------------------------
                    //doNutInfo(psNutInfo, ibdata);
                    con.commit();

                } else {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "El producto: " + producto.getCodigo() + "  no pudo ser importador por un error en su estructura");
                }
                elevData.vaciarElementos();
            }

        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != connection) {
                    connection.close();
                }

                rs = null;
                psDpt = null;
                psPlu = null;
                psPrice = null;
                elevData = null;

                con.close();
                con = null;
                connection.close();
                connection = null;

            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
                ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
            }
        }

        return result;
    }

    public boolean loadMyBusinessPos(String importDBIP, String importDBName, String importDBPass, String importDBPort, String importDBQuery, String importDBType, String importDBUser, boolean b, String ip2) {

        final String ip = ip2;

        boolean result = true;

        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        MyBusinessPosSQLServerConnector mbpmssql = new MyBusinessPosSQLServerConnector();

        List<MyBusinessPosProducto> productos = null;
        try {

            //productos = efdbc.getPLUs(dbPath);
            productos = mbpmssql.getPLUs(importDBIP, importDBName, importDBPass, importDBPort, importDBQuery, importDBType, importDBUser);

            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + importDBName);

            if (productos == null) {
                result = false;
                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se obtuvieron registros de: " + importDBName);
                ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                return result;
            }

        } catch (Exception e) {
            result = false;
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + importDBName + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

        String query_dpt, query_prod, query_prod_price; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPrice = null;

        int count = 0;
        GeneralPlu mbpData = new GeneralPlu();
        con = this.connection;

        try {
            con.setAutoCommit(false);
//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

            this.pb.setMaximum(productos.size() - 1);

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            // Procesar la lista de productos
            for (MyBusinessPosProducto producto : productos) {
                if (!producto.getId().equalsIgnoreCase("SYS")) {
                    duration = new Duration(timeStart, timeStart.now());
                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                    customLogger.getInstance().writeLog(MainApp.INFO, ip, "ROW:" + count + " - " + producto.toString());

                    if (mbpData.parseMyBusinesPos(
                            producto.getId(),
                            producto.getCodigo(),
                            producto.getDescripcion(),
                            producto.getPrecio(),
                            producto.getPrecio2(),
                            producto.getDepto(),
                            producto.getDeptoNombre(),
                            producto.getTipo(),
                            producto.getCodebar()
                    )) {

                        //VA IMPORTACION FULL
                        //-----------------------SQL_DEPARTMENT--------------------
                        mbpData.doDepartments(psDpt);
                        //-----------------------SQL_PRODUCT-----------------------
                        mbpData.doProductsEleventas(psPlu);
                        //-----------------------SQL_PRICE-------------------------
                        mbpData.doPrices(psPrice);
                        //-----------------------SQL_NUT_INFO-------------------------
                        //doNutInfo(psNutInfo, ibdata);

                        con.commit();
                    } else {
                        customLogger.getInstance().writeLog(MainApp.ERROR, ip, "El producto: " + producto.getCodigo() + "  no pudo ser importador por un error en su estructura");
                    }
                }
                mbpData.vaciarElementos();
            }

        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != connection) {
                    connection.close();
                }

                psDpt = null;
                psPlu = null;
                psPrice = null;
                mbpData = null;

                con.close();
                con = null;
                connection.close();
                connection = null;

            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
                ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
            }
        }

        return result;
    }

    public boolean loadHanbai(String importDBIP, String importDBName, String importDBPass, String importDBPort, String importDBQuery, String importDBType, String importDBUser, boolean b, String ip2) {

        final String ip = ip2;

        boolean result = true;

        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        HanbaiMariadbConnector hanmdb = new HanbaiMariadbConnector();

        List<HanbaiProducto> productos = null;

        QendraHandlerHanbai qendra_handler = new QendraHandlerHanbai();
        if (ConfigFile.useQendra()) {
            if (!ConfigFile.useQendraDefPath()) {
                try {
                    qendra_handler.connectDB(ConfigFile.getQendraPath());
                } catch (ClassNotFoundException ex) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se encuentra el archivo: " + ConfigFile.getQendraPath() + "\n Con error: " + ex.toString());
                }
            } else {
                try {
                    qendra_handler.connectDB(ConfigFile.QENDRA_DEFAULT_PATH);
                } catch (ClassNotFoundException ex) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se encuentra el archivo: " + ConfigFile.QENDRA_DEFAULT_PATH + "\n Con error: " + ex.toString());
                }
            }
        }

        try {

            //productos = efdbc.getPLUs(dbPath);
            productos = hanmdb.getPLUs(importDBIP, importDBName, importDBPass, importDBPort, importDBQuery, importDBType, importDBUser);

            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + importDBName);

            if (productos == null) {
                result = false;
                customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se obtuvieron registros de: " + importDBName);
                ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
                return result;
            }

        } catch (Exception e) {
            result = false;
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + importDBName + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

        String query_dpt, query_prod, query_prod_price; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPrice = null;

        int count = 0;
        GeneralPlu hanData = new GeneralPlu();
        con = this.connection;

        try {
            con.setAutoCommit(false);
//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

            this.pb.setMaximum(productos.size() - 1);

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            // Procesar la lista de productos
            for (HanbaiProducto producto : productos) {
                if (!producto.getId().equalsIgnoreCase("SYS")) {
                    duration = new Duration(timeStart, timeStart.now());
                    count++;
                    this.pb.setValue(count);
                    this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                    customLogger.getInstance().writeLog(MainApp.INFO, ip, "ROW:" + count + " - " + producto.toString());

                    if (hanData.parseHanbai(
                            producto.getId(),
                            producto.getCodigo(),
                            producto.getDescripcion(),
                            producto.getPrecio(),
                            producto.getPrecio2(),
                            producto.getDepto(),
                            producto.getDeptoNombre(),
                            producto.getTipo(),
                            producto.getCodebar(),
                            producto.getCaducidad()
                    )) {

                        //VA IMPORTACION FULL
                        //-----------------------SQL_DEPARTMENT--------------------
                        hanData.doDepartments(psDpt);
                        //-----------------------SQL_PRODUCT-----------------------
                        hanData.doProductsEleventas(psPlu);
                        //-----------------------SQL_PRICE-------------------------
                        hanData.doPrices(psPrice);
                        //-----------------------SQL_NUT_INFO-------------------------
                        //doNutInfo(psNutInfo, ibdata);

                        con.commit();
                    } else {
                        customLogger.getInstance().writeLog(MainApp.ERROR, ip, "El producto: " + producto.getCodigo() + "  no pudo ser importador por un error en su estructura");
                    }
                }

                if (ConfigFile.useQendra()) {
                    qendra_handler.InsertProduct(hanData, hanData.codigo_plu);
                }

                hanData.vaciarElementos();
            }

        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != connection) {
                    connection.close();
                }

                psDpt = null;
                psPlu = null;
                psPrice = null;
                hanData = null;
                qendra_handler = null;

                con.close();
                con = null;
                connection.close();
                connection = null;

            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
                ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
            }
        }

        return result;
    }

    public boolean loadCSVIntellibuildNovedades(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices) {

        final String ip = ip2;
        boolean doImportOnlyPrices = bImportOnlyPrices;

        boolean result = true;
        CSVReader csvReader = null;
        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);

            //csvReader = new CSVReader(new FileReader(csvFile));
            csvReader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), "ISO-8859-1"));

        } catch (Exception e) {
            result = false;
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

//        String query_dpt, query_prod, query_prod_price = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
//        query = query.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
//        query = query.replaceFirst(VALUES_REGEX, questionmarks);
        // System.out.println("Query: " + query);
        String[] nextLine;
//        Connection con = null;
//        PreparedStatement psDpt = null, psPlu = null, psPrice = null;

        String query_dpt, query_prod, query_prod_exist, query_prod_price, query_nut_info, query_receing, query_tara = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        String[] input;
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPluExist = null, psPrice = null, psNutInfo = null, psReceing = null, psTara = null;
        ResultSet rs = null;

        int count = 0;
        IBPlu ibdata = new IBPlu();
        con = this.connection;
        QendraHandler qendra_handler = new QendraHandler();
        if (ConfigFile.useQendra()) {
            if (!ConfigFile.useQendraDefPath()) {
                try {
                    qendra_handler.connectDB(ConfigFile.getQendraPath());
                } catch (ClassNotFoundException ex) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se encuentra el archivo: " + ConfigFile.getQendraPath() + "\n Con error: " + ex.toString());
                }
            } else {
                try {
                    qendra_handler.connectDB(ConfigFile.QENDRA_DEFAULT_PATH);
                } catch (ClassNotFoundException ex) {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, "No se encuentra el archivo: " + ConfigFile.QENDRA_DEFAULT_PATH + "\n Con error: " + ex.toString());
                }
            }
        }

        try {
            con.setAutoCommit(false);

//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            query_prod_exist = SQL_PRODUCT_EXIST;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

            this.pb.setMaximum((int) (Files.lines(Paths.get(csvFile), Charset.forName("ISO-8859-1")).count() - 1));

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            boolean success = false;
            // ******* LECTURA ARCHIVO DE NOVEDADES ******* //
            while ((nextLine = csvReader.readNext()) != null) {

                duration = new Duration(timeStart, timeStart.now());

                count++;
                this.pb.setValue(count);
                this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                success = false;

                success = ibdata.parseInputSDR(Arrays.toString(nextLine));

                if (success) { // Acá se hace el parseo de los datos y se guarda en la clase ibdata

                    //VA IMPORTACION FULL
                    //-----------------------SQL_DEPARTMENT--------------------
                    ibdata.doDepartments(psDpt);
                    //-----------------------SQL_PRODUCT-----------------------
                    ibdata.doProductsItensMGV(psPlu);
                    //-----------------------SQL_PRICE-------------------------
                    ibdata.doPrices(psPrice);
                    //-----------------------SQL_NUT_INFO-------------------------
                    //doNutInfo(psNutInfo, ibdata);

                    con.commit();

                    // Added by GCastillo
                    if (ConfigFile.useQendra()) {
                        qendra_handler.InsertProduct(ibdata, ibdata.codigo);
                    }
                    ibdata.vaciarElementos();
                }
            }
        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }

                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != psNutInfo) {
                    psNutInfo.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != connection) {
                    connection.close();
                }
                csvReader.close();

                rs = null;
                csvReader = null;
                input = null;
                psDpt = null;
                psPlu = null;

                psPrice = null;
                psNutInfo = null;
                ibdata = null;
                qendra_handler = null;

                con.close();
                con = null;
                connection.close();
                connection = null;
            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
                ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
            }
        }
        return result;
    }

    public boolean loadCSVMT8450(String csvFile, String tableName,
            boolean truncateBeforeLoad, String ip2, boolean bImportOnlyPrices) {

        final String ip = ip2;
        boolean doImportOnlyPrices = bImportOnlyPrices;

        boolean result = true;
        CSVReader csvReader = null;

        if (null == this.connection) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("NOT A VALID CONNECTION."));
            ExitCodes.ERROR_STATUS = ExitCodes.NOT_VALID_DB_CONNECTION;
        }
        try {
            customLogger.getInstance().writeLog(MainApp.INFO, ip, LocaleUtil.getMessage("Inicio importando el archivo:") + csvFile);

            //csvReader = new CSVReader(new FileReader(csvFile));
            csvReader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), "ISO-8859-1"));

        } catch (Exception e) {
            result = false;
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Importando el archivo: " + csvFile + ": " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.IMPORT_FILE_ERROR;
        }

        String questionmarks = "";

//        String query_dpt, query_prod, query_prod_price = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
//        query = query.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
//        query = query.replaceFirst(VALUES_REGEX, questionmarks);
        // System.out.println("Query: " + query);
        String[] nextLine;
//        Connection con = null;
//        PreparedStatement psDpt = null, psPlu = null, psPrice = null;

        String query_dpt, query_prod, query_prod_exist, query_prod_price = ""; //SQL_PRODUCT; //SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        String[] input;
        Connection con = null;
        PreparedStatement psDpt = null, psPlu = null, psPluExist = null, psPrice = null;
        ResultSet rs = null;

        int count = 0;
        GeneralPlu ibdata = new GeneralPlu();
        con = this.connection;

        try {
            con.setAutoCommit(false);

//-----------------------SQL_DEPARTMENT--------------------
            query_dpt = SQL_DEPARTMENT;
            /*
             the_product_id BIGINT,
             the_name CHARACTER VARYING,
             */
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación departamentos");
            questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            psDpt = con.prepareStatement(query_dpt);

//-------------------------SQL_PRODUCT------------------
            //                   customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de productos");
            query_prod = SQL_PRODUCT;
            query_prod_exist = SQL_PRODUCT_EXIST;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?,?,?,?,?,?,?";
            query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);
            psPlu = con.prepareStatement(query_prod);

            questionmarks = "?";
            query_prod_exist = query_prod_exist.replaceFirst(VALUES_REGEX, questionmarks);
            psPluExist = con.prepareStatement(query_prod_exist);
//-------------------------SQL_PRODUCT_PRICE------------------
//                    customLogger.getInstance().writeLog(MainApp.INFO, "Inicio importación de precios");
            query_prod_price = SQL_PRODUCT_PRICE;
            /*
             the_product_id BIGINT,
             the_erp_code CHARACTER VARYING,
             the_name CHARACTER VARYING,
             the_attribute INTEGER,
             the_department_id BIGINT,
             the_description CHARACTER VARYING,
             the_print_used_by_date CHARACTER,
             the_used_by_date NUMERIC
             */
            questionmarks = "?,?,?,?";
            query_prod_price = query_prod_price.replaceFirst(VALUES_REGEX, questionmarks);
            psPrice = con.prepareStatement(query_prod_price);

            this.pb.setMaximum((int) (Files.lines(Paths.get(csvFile), Charset.forName("ISO-8859-1")).count() - 1));

            DateTime timeStart = new DateTime();
            Duration duration = null; //new Duration(timeStart, new DateTime());

            boolean success = false;
            // ******* LECTURA ARCHIVO DE NOVEDADES ******* //
            while ((nextLine = csvReader.readNext()) != null) {
                duration = new Duration(timeStart, timeStart.now());

                count++;
                this.pb.setValue(count);
                this.lblTime.setText(LocaleUtil.getMessage("Tiempo:") + duration.getStandardMinutes() + LocaleUtil.getMessage("min") + duration.getStandardSeconds() % 60 + LocaleUtil.getMessage("seg"));

                success = false;
                success = ibdata.parseInputMT8450(Arrays.toString(nextLine));

                if (success) { // Acá se hace el parseo de los datos y se guarda en la clase ibdata
                    if (doImportOnlyPrices) {
                        //si SOLO QUIERE PRECIOS
                        //-----------------------SQL_PLU_EXIST-------------------------
                        psPluExist.setLong(1, ibdata.codigo_plu);
                        rs = psPluExist.executeQuery();
                        if (!rs.next()) {
                            //NO ESTA!!!!, importo todo
                            //-----------------------SQL_DEPARTMENT--------------------
                            ibdata.doDepartments(psDpt);
                            //-----------------------SQL_PRODUCT-----------------------
                            ibdata.doProductsMT8450(psPlu);
                            //-----------------------SQL_PRICE-------------------------
                            ibdata.doPrices(psPrice);
                        } else {
                            // LO TENGO!!!!, sólo actualizo el precio
                            //-----------------------SQL_PRICE-------------------------
                            ibdata.doPrices(psPrice);
                        }
                    } else {
                        //VA IMPORTACION FULL
                        //-----------------------SQL_DEPARTMENT--------------------
                        ibdata.doDepartments(psDpt);
                        //-----------------------SQL_PRODUCT-----------------------
                        ibdata.doProductsMT8450(psPlu);
                        //-----------------------SQL_PRICE-------------------------
                        ibdata.doPrices(psPrice);
                    }
                    con.commit();

                    ibdata.vaciarElementos();
                }
            }
        } catch (SQLException e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "En la importación: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
        } catch (Exception e) {
            result = false;
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, "Al leer la línea: " + count + " Mensaje: " + e.toString());
            ExitCodes.ERROR_STATUS = ExitCodes.EXCEPTION;
        } finally {
            try {
                if (null != psDpt) {
                    psDpt.close();
                }
                if (null != psPluExist) {
                    psPluExist.close();
                }
                if (null != psPlu) {
                    psPlu.close();
                }
                if (null != psPrice) {
                    psPrice.close();
                }
                if (null != con) {
                    con.close();
                }
                if (null != connection) {
                    connection.close();
                }
                csvReader.close();

                rs = null;
                csvReader = null;
                input = null;
                psDpt = null;
                psPlu = null;
                psPluExist = null;
                psPrice = null;

                ibdata = null;

                con.close();
                con = null;
                connection.close();
                connection = null;

            } catch (Exception ex) {
                Logger.getLogger(FileLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
                ExitCodes.ERROR_STATUS = ExitCodes.SQL_EXCEPTION;
            }
        }

        return result;
    }

    public char getSeprator() {
        return seprator;
    }

    public void setSeprator(char seprator) {
        this.seprator = seprator;

    }

    public static Connection getCon(String ip) {
        CustomLogger customLogger = CustomLogger.getInstance();
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
            customLogger.getInstance().writeLog(MainApp.INFO, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("CONEXION A LA BASE DE DATOS EJEMPLO REALIZADA CON EXITO! "));
//Cerramos la conexion
//            connection.close();
        } //Si se produce una Excepcion y no nos podemos conectar, muestra el sgte. mensaje.
        catch (SQLException e) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("SE HA PRODUCIDO UN ERROR EN LA CONEXION A LA BASE DE DATOS EJEMPLO! "));
        } catch (ClassNotFoundException ex) {
            customLogger.getInstance().writeLog(MainApp.ERROR, ip, ex.toString());
        }

        return connection;
    }

    public static List<SystelDBConnector.SystelDepto> loadMGVDepto() {
        SystelDBConnector sysdbc = new SystelDBConnector();

        List<SystelDBConnector.SystelDepto> deptos = null;

        deptos = sysdbc.getAllDeptos();

        return deptos;
    }

    public static List<SystelDBConnector.SystelDepto> parseInputMGVDepto(String csvFile) {

        String sCharset = "UTF-8";

        try {
            sCharset = detectCharset(csvFile);
            if (sCharset != null) {
                System.out.println("Detected encoding: " + sCharset);
            } else {
                System.out.println("No encoding detected. Using default encoding.");
                sCharset = "ISO_8859_1";
            }
        } catch (Exception e) {
            sCharset = "UTF-8";
            e.printStackTrace();
        }
        Set<String> uniqueSubstrings = null;
        Path filePath = null;
        Stream<String> stream = null;
        try {
            filePath = Paths.get(csvFile);
            stream = Files.lines(filePath, Charset.forName(sCharset));
            uniqueSubstrings = stream
                    .map(s -> s.substring(0, Math.min(2, 2))) // Obtener los primeros dos caracteres a partir del segundo
                    .distinct() // Asegurarse de que sean únicos
                    .filter(s -> !s.trim().isEmpty()) // Filtrar líneas vacías
                    .sorted() // Ordeno los datos
                    .collect(Collectors.toCollection(TreeSet::new) // Recoger los resultados en un Set
                    );
        } catch (Exception e) {
            Logger.getLogger(ChooseDevices.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
            System.exit(1);
        } finally {
            filePath = null;
            stream = null;
        }

        IBPlu SysData = new IBPlu();
        Connection con = getCon("localhost");

        try {
            con.setAutoCommit(false);
//-----------------------SQL_DEPARTMENT--------------------
            String query_dpt = SQL_MGV_DEPARTMENT;
            /*
            the_product_id BIGINT,
            the_name CHARACTER VARYING,
             */
            String questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            PreparedStatement psDpt = con.prepareStatement(query_dpt);

            for (String dpto : uniqueSubstrings) {
                //-----------------------SQL_DEPARTMENT--------------------
                if (!dpto.trim().isEmpty()) {
                    SysData.doDepartments(psDpt, Long.parseLong(dpto));
                }
            }

        } catch (Exception e) {
            Logger.getLogger(ChooseDevices.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                con.commit();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            con = null;
            SysData.vaciarElementos();
            SysData = null;
        }

        return loadMGVDepto();
    }

    public static List<SystelDBConnector.SystelDepto> parseInputMGVDepto(List<SystelDBConnector.SystelDepto> deptos) {
        IBPlu SysData = new IBPlu();
        Connection con = getCon("localhost");

        try {
            con.setAutoCommit(false);
//-----------------------SQL_DEPARTMENT--------------------
            String query_dpt = SQL_DEPARTMENT;
            /*
            the_product_id BIGINT,
            the_name CHARACTER VARYING,
             */
            String questionmarks = "?,?";
            query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);
            PreparedStatement psDpt = con.prepareStatement(query_dpt);

            for (SystelDBConnector.SystelDepto dpto : deptos) {
                //-----------------------SQL_DEPARTMENT--------------------
                SysData.doDepartments(psDpt, dpto.getId(), dpto.getName());
            }

        } catch (Exception e) {
            Logger.getLogger(ChooseDevices.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                con.commit();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            con = null;
            SysData.vaciarElementos();
            SysData = null;
        }

        return loadMGVDepto();
    }

}

/**
 *
 * @author gcastillo
 *
 * Contendra las funciones y datos necesarios para el manejo del CSV para
 * intellibuild.
 *
 *
 */
class IBPlu {

    public long codigo = 0;
    public int seccion = 0;
    public char tipo = 'P';
    public String nombre = "";
    public double precio = 0;
    public double precio_2 = 0;
    public int vencimiento = 0;
    public boolean imp_embalage = false;
    public boolean imp_vencimiento = false;
    //---
    public long cod_inf_extra = 0;
    public String info_extra = "";
    //---
    public long cod_inf_nut = 0;
    public String info_set_id = "0"; // Se agrega para manejar diferentes formatos de tablas.
    public String porcion = ""; // Se agrega ademas la medida casera.
    public String porcion_embalage = ""; // Se agrega ademas la medida casera.
    public String medida_casera = "";
    public String valor_energetico = "";
    public String azucares_totales = "";
    public String azucares_adicionales = "";
    public String azucares_adicionales_pct = "";
    public String carbohidratos = "";
    public String proteinas = "";
    public String grasas_total = "";
    public String grasas_saturadas = "";
    public String grasas_trans = "";
    public String fibra = "";
    public String sodio = "";
    public String valor_energetico_pct = "";
    public String carbohidratos_pct = "";
    public String proteinas_pct = "";
    public String grasas_total_pct = "";
    public String grasas_saturadas_pct = "";
    public String grasas_trans_pct = "";
    public String fibra_pct = "";
    public String sodio_pct = "";
    //---
    public int cod_imagen = 0;
    public String path_imagen = "";
    //---
    public int cod_proveedor = 0;
    public String desc_proveedor = "";
    //---
    public int cod_tara = 0;
    public String valor_tara = "";
    //---
    public int cod_som = 0;
    public String desc_som = "";
    //---
    public int cod_fraccionador = 0;
    public String desc_fraccionador = "";
    //---
    public int cod_conservacion = 0;
    public String desc_conservacion = "";
    //---
    public int cod_campo_extra1 = 0;
    public String desc_campo_extra1 = "";
    //---
    public int cod_campo_extra2 = 0;
    public String desc_campo_extra2 = "";

    public String primary_barcode_flag = "N";
    public String primary_barcode_data = "";
    //---
    public int cod_origen = 0;
    public String desc_origen = "";
    //---
    public int cod_ingredientes = 0;
    public String desc_ingredientes = "";
    //---
    public int cod_ingredientes1 = 0;
    //-- FIM --

    //--versiones de MGV--//
    private static final int VER_MGV5_1 = 0;
    private static final int VER_MGV5_2 = 1;
    private static final int VER_MGV5_3 = 2;
    private static final int VER_MGV6 = 3;
    private static final int VER_MGV7 = 4;
    BufferedWriter writer;

    public IBPlu() {

    }

    public void vaciarElementos() {
        codigo = 0;
        seccion = 0;
        tipo = 'P';
        nombre = "";
        precio = 0;
        precio_2 = 0;
        vencimiento = 0;
        imp_embalage = false;
        imp_vencimiento = false;
        //---
        cod_inf_extra = 0;
        info_extra = "";
        //---
        cod_inf_nut = 0;
        info_set_id = "0";
        porcion = ""; // Se agrega ademas la medida casera.
        valor_energetico = "";
        carbohidratos = "";
        proteinas = "";
        grasas_total = "";
        grasas_saturadas = "";
        grasas_trans = "";
        fibra = "";
        sodio = "";
        valor_energetico_pct = "";
        carbohidratos_pct = "";
        proteinas_pct = "";
        grasas_total_pct = "";
        grasas_saturadas_pct = "";
        grasas_trans_pct = "";
        fibra_pct = "";
        sodio_pct = "";
        //---
        cod_imagen = 0;
        path_imagen = "";
        //---        cod_proveedor = 0;
        desc_proveedor = "";
        //---
        cod_tara = 0;
        valor_tara = "";
        //---
        cod_som = 0;
        desc_som = "";
        //---
        cod_fraccionador = 0;
        desc_fraccionador = "";
        //---
        cod_conservacion = 0;
        desc_conservacion = "";
        //---
        cod_campo_extra1 = 0;
        desc_campo_extra1 = "";
        //---
        cod_campo_extra2 = 0;
        desc_campo_extra2 = "";
        primary_barcode_flag = "N";
        primary_barcode_data = "";
        //---
        cod_origen = 0;
        desc_origen = "";
        //---
        cod_ingredientes = 0;
        desc_ingredientes = "";
        porcion_embalage = ""; // Se agrega ademas la medida casera.
        medida_casera = "";
        azucares_totales = "";
        azucares_adicionales = "";
        azucares_adicionales_pct = "";
    }

    public boolean parseInputIntellibuild(String input) {

        //se redefine esta variable para hacer dinámico al importador, en cuanto al largo del archivo
        FILE_SDR_TXT[1] = input.length() - 1;

        this.seccion = Integer.parseInt(input.substring(FILE_SDR_DEP[0], FILE_SDR_DEP[1]).trim());
//        ///= Arrays.toString(nextLine).substring(FILE_SDR_NO_USAR[0], FILE_SDR_NO_USAR[1]).trim();
        this.tipo = Integer.parseInt(input.substring(FILE_SDR_PLU_TYPE[0], FILE_SDR_PLU_TYPE[1]).trim()) == 0 ? 'P' : 'U';
        this.codigo = Integer.parseInt(input.substring(FILE_SDR_PLU_CODE[0], FILE_SDR_PLU_CODE[1]).trim());
        this.precio = Double.parseDouble(input.substring(FILE_SDR_PRC[0], FILE_SDR_PRC[1]).trim()) / 100;
        this.vencimiento = Integer.parseInt(input.substring(FILE_SDR_CAD[0], FILE_SDR_CAD[1]).trim());
        this.imp_vencimiento = this.vencimiento > 0;

        this.nombre = input.substring(FILE_SDR_TXT[0], FILE_SDR_TXT[1]).trim();

        return true;
    }

    /**
     *
     * @param input Linea a parsear.
     * @return True si llego al final del parseo de 1 PLU completo, false en
     * caso contrario.
     */
    public boolean parseInput(String input) {
        /*
        Esperamos un input de la forma:
            CMD : TEXT
        Por lo que haremos un split por los : luego veremos a que comando 
        corresponde, y luego procederemos a dejar el text donde corresponda.
         */
        //System.out.println(input);
        if (input.trim().equals("--")) {
            return false;
        } else if (input.trim().equals("-- FIM --")) {
            // TODO: debe ejecutar query o ver de retornar true y que se haga
            // afuera.
            //this.vaciarElementos();
            return true;
        } else if (input.trim().equals("")) {
            return false;
        }

        String[] input_parsed = input.split(":");

        if (input_parsed[0].contains("ITEM")) {
            this.codigo = Integer.parseInt(input_parsed[1].split(" ")[1]);
        } else if (input_parsed[0].contains("DEPARTAMENTO")) {
            this.seccion = Integer.parseInt(input_parsed[1].trim());
        } else if (input_parsed[0].contains("TIPO")) {
            this.tipo = input_parsed[1].trim().toCharArray()[0];
        } else if (input_parsed[0].contains("DESCRIÇÃO LINHA")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.nombre += input_parsed[i].trim();
            }
            if (this.nombre.length() >= 56) {
                this.nombre = this.nombre.substring(0, 56);
            }

        } else if (input_parsed[0].contains("PREÇO")) {
            this.precio = Double.parseDouble(input_parsed[1].trim().replace(",", "."));
        } else if (input_parsed[0].contains("IMP. VALIDADE")) {
            this.imp_vencimiento = 'S' == input_parsed[1].trim().toCharArray()[0];
        } else if (input_parsed[0].contains("VALIDADE")) {
            this.vencimiento = Integer.parseInt(input_parsed[1].trim());
        } else if (input_parsed[0].contains("EMBALAGEM")) {
            this.imp_embalage = 'S' == input_parsed[1].trim().toCharArray()[0];
        } else if (input_parsed[0].contains("COD. INF. EXTRA")) {
            this.cod_ingredientes = Integer.parseInt(input_parsed[1].split(" ")[1]);
        } else if (input_parsed[0].contains("DA IE")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.desc_ingredientes += input_parsed[i].trim() + " ";
            }
        } else if (input_parsed[0].contains("COD. INF. NUTRI")) {
            this.cod_inf_nut = Integer.parseInt(input_parsed[1].split(" ")[1]);
        } else if (input_parsed[0].contains("PORÇÃO") || input_parsed[0].contains("PORCAO")) {
            this.porcion += input_parsed[1].trim();
        } else if (input_parsed[0].contains("MEDIDA CASEIRA")) {
            this.porcion += " (" + input_parsed[1].trim() + ")";
        } else if (input_parsed[0].contains("VALOR ENERG")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.valor_energetico += input_parsed[i].trim();
            }
            if (this.valor_energetico.contains("VD(%)")) {
                this.valor_energetico = this.valor_energetico.substring(0, this.valor_energetico.length() - 8).trim();
            }
            if (this.valor_energetico.contains("QNS")) {
                this.valor_energetico = "QNS";
                this.valor_energetico_pct = "0%";
            } else {
                try {
                    this.valor_energetico_pct += Math.round((Double.parseDouble(this.valor_energetico.substring(0, 5).replace(",", ".").trim()) / 20)) + "%";
                } catch (Exception e) {
                    this.valor_energetico_pct = "0%";
                }
            }
        } else if (input_parsed[0].contains("CARBOIDRATOS")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.carbohidratos += input_parsed[i].trim();
            }
            if (this.carbohidratos.contains("VD(%)")) {
                this.carbohidratos = this.carbohidratos.substring(0, this.carbohidratos.length() - 8).trim();
            }

            if (this.carbohidratos.contains("QNS")) {
                this.carbohidratos = "QNS";
                this.carbohidratos_pct = "0%";
            } else {
                try {
                    this.carbohidratos_pct += Math.round((Double.parseDouble(this.carbohidratos.replace("(g)", "").replace("VD(%)", "").replace("(mg)", "").replace(",", ".").trim()) / 3)) + "%";
                } catch (Exception e) {
                    this.carbohidratos_pct = "0%";
                }
            }

        } else if (input_parsed[0].contains("PROTEINAS")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.proteinas += input_parsed[i].trim();
            }
            if (this.proteinas.contains("VD(%)")) {
                this.proteinas = this.proteinas.substring(0, this.proteinas.length() - 8).trim();
            }

            if (this.proteinas.contains("QNS")) {
                this.proteinas = "QNS";
                this.proteinas_pct = "0%";
            } else {
                try {
                    this.proteinas_pct += Math.round((Double.parseDouble(this.proteinas.replace("(g)", "").replace("VD(%)", "").replace("(mg)", "").replace(",", ".").trim()) / 0.75)) + "%";
                } catch (Exception e) {
                    this.proteinas_pct = "0%";
                }
            }
        } else if (input_parsed[0].contains("GORDURAS TOTAIS")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.grasas_total += input_parsed[i].trim();
            }
            if (this.grasas_total.contains("VD(%)")) {
                this.grasas_total = this.grasas_total.substring(0, this.grasas_total.length() - 8).trim();
            }
            if (this.grasas_total.contains("QNS")) {
                this.grasas_total = "QNS";
                this.grasas_total_pct = "0%";
            } else {
                try {
                    this.grasas_total_pct += Math.round((Double.parseDouble(this.grasas_total.replace("(g)", "").replace("VD(%)", "").replace("(mg)", "").replace(",", ".").trim()) / 0.55)) + "%";
                } catch (Exception e) {
                    this.grasas_total_pct = "0%";
                }
            }
        } else if (input_parsed[0].contains("SATURADAS")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.grasas_saturadas += input_parsed[i].trim();
            }
            if (this.grasas_saturadas.contains("VD(%)")) {
                this.grasas_saturadas = this.grasas_saturadas.substring(0, this.grasas_saturadas.length() - 8).trim();
            }
            if (this.grasas_saturadas.contains("QNS")) {
                this.grasas_saturadas = "QNS";
                this.grasas_saturadas_pct = "0%";
            } else {
                try {
                    this.grasas_saturadas_pct += Math.round((Double.parseDouble(this.grasas_saturadas.replace("(g)", "").replace("VD(%)", "").replace("(mg)", "").replace(",", ".").trim()) / 0.22)) + "%";
                } catch (Exception e) {
                    this.grasas_saturadas_pct = "0%";
                }
            }
        } else if (input_parsed[0].contains("GORDURA TRANS") || input_parsed[0].contains("TRANS")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.grasas_trans += input_parsed[i].trim();
            }
            if (this.grasas_trans.contains("VD(%)")) {
                this.grasas_trans = this.grasas_trans.substring(0, this.grasas_trans.length() - 8).trim();
            }
            if (this.grasas_trans.contains("QNS")) {
                this.grasas_trans = "QNS";
            }
        } else if (input_parsed[0].contains("FIBRA ALIMENTAR")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.fibra += input_parsed[i].trim();
            }
            if (this.fibra.contains("VD(%)")) {
                this.fibra = this.fibra.substring(0, this.fibra.length() - 8).trim();
            }
            if (this.fibra.contains("QNS")) {
                this.fibra = "QNS";
                this.fibra_pct = "0%";
            } else {
                try {
                    this.fibra_pct += Math.round((Double.parseDouble(this.fibra.replace("(g)", "").replace("VD(%)", "").replace("(mg)", "").replace(",", ".").trim()) / 0.25)) + "%";
                } catch (Exception e) {
                    this.fibra_pct = "0%";
                }
            }
        } else if (input_parsed[0].contains("SÓDIO") || input_parsed[0].contains("SODIO")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.sodio += input_parsed[i].trim();
            }
            if (this.sodio.contains("VD(%)")) {
                this.sodio = this.sodio.substring(0, this.sodio.length() - 8).trim();
            }
            if (this.sodio.contains("QNS")) {
                this.sodio = "QNS";
                this.sodio_pct = "0%";
            } else {
                try {
                    this.sodio_pct += Math.round((Double.parseDouble(this.sodio.replace("(g)", "").replace("VD(%)", "").replace("(mg)", "").replace(",", ".").trim()) / 24)) + "%";
                } catch (Exception e) {
                    this.sodio_pct = "0%";
                }
            }
        } else if (input_parsed[0].contains("COD. IMAGEM")) {
            this.cod_imagen = Integer.parseInt(input_parsed[1].trim());
        } else if (input_parsed[0].contains("CAMINHO DA IMAGEM")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.path_imagen += input_parsed[i].trim();
            }
        } else if (input_parsed[0].contains("COD. FORNECEDOR")) {
            this.cod_proveedor = Integer.parseInt(input_parsed[1].split(" ")[1]);
        } else if (input_parsed[0].contains("DO FORN.")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.desc_proveedor += input_parsed[i].trim();
            }
        } else if (input_parsed[0].contains("COD. TARA")) {
            this.cod_tara = Integer.parseInt(input_parsed[1].split(" ")[1]);
        } else if (input_parsed[0].contains("VALOR DA TARA")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.valor_tara += input_parsed[i].trim();
            }
        } else if (input_parsed[0].contains("COD. SOM")) {
            this.cod_som = Integer.parseInt(input_parsed[1].trim());
        } else if (input_parsed[0].contains("ARQUIVO DE SOM") || input_parsed[0].contains("CAMINHO DO SOM")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.desc_som += input_parsed[i].trim();
            }
        } else if (input_parsed[0].contains("COD. FRACIONADOR")) {
            this.cod_fraccionador = Integer.parseInt(input_parsed[1].split(" ")[1]);
        } else if (input_parsed[0].contains("DO FRAC.")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.desc_fraccionador += input_parsed[i].trim() + " ";
            }
        } else if (input_parsed[0].contains("COD. CONSERVACADO") || input_parsed[0].contains("COD. CONSERVACAO")) {
            this.cod_conservacion = Integer.parseInt(input_parsed[1].split(" ")[1]);
        } else if (input_parsed[0].contains("CONSERV.")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.desc_conservacion += input_parsed[i].trim() + " ";
            }
        } else if (input_parsed[0].contains("CAMPOEXTRA1")) {
            this.cod_campo_extra1 = Integer.parseInt(input_parsed[1].split(" ")[1]);
        } else if (input_parsed[0].contains("CE1")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.desc_campo_extra1 += input_parsed[i].trim() + " ";
            }
            this.desc_campo_extra1 = this.desc_campo_extra1.replace("      ", "") + " ";
        } else if (input_parsed[0].contains("CAMPOEXTRA2")) {
            this.cod_campo_extra2 = Integer.parseInt(input_parsed[1].trim());
        } else if (input_parsed[0].contains("CE2")) {
            for (int i = 1; i < input_parsed.length; i++) {
                this.desc_campo_extra2 += input_parsed[i].trim() + " ";
            }
            this.desc_campo_extra2 = this.desc_campo_extra2.replace("      ", "") + " ";

        }
        return false;
    }
    //01000000430002699000CARNE MOIDA ESPECIAL
    private static final int[] FILE_SDR_DEP = new int[]{1, 3};        //    "MOS","DEPTO",1,2,0                                         0 ID departamento & 1 name departamento
    private static final int[] FILE_SDR_NO_USAR = new int[]{3, 5};    //    "NO USAR",3,8,0                                            4 codigo_plu ERP
    private static final int[] FILE_SDR_PLU_TYPE = new int[]{5, 6};   //    "TYPE","TIPO PLU",9,12,0                                              2 PLU
    private static final int[] FILE_SDR_PLU_CODE = new int[]{6, 12};  //    "CODE","PLU CODIGO" ,13,37,0                                           3 nombre
    private static final int[] FILE_SDR_PRC = new int[]{12, 18};      //    "PRC","PRECIO",38,44,0                                          5 lista 1 & 6 lista 2
    private static final int[] FILE_SDR_CAD = new int[]{18, 21};      //    "CADUCIDAD  / No Pesado",45,45,0                              7 tipo producto 0=P - 1=U
    private static int[] FILE_SDR_TXT = new int[]{21, 71};      //    "TXT","NOMBRE PLU",46,57,0                                13 código de barras
    //private static int[] FILE_SDR_TXT ;

    /**
     *
     * @author diego Funciones MGV7
     *
     */
    public boolean parseInputItensMGV(String input, HashMap<Long, List<Long>> map_nut_info, HashMap<Long, List<Long>> map_receing, HashMap<Integer, List<Long>> map_conservacion, HashMap<Integer, List<Long>> map_extra2, HashMap<Integer, List<Long>> map_ingredietes, HashMap<Integer, List<Long>> map_tara, HashMap<Integer, List<Long>> map_ingredietes2) {
        /*
- ITENSMGV5 - VERSÃO 1-113 bytes
DD(2)T(1)CCCCCC(6)PPPPPP(6)VVV(3)D1(25)D2(25)RRRRRR(6)FFF(3)IIII(4)DV(1)DE(1)CF(4)L(12)G(11)Z(1)R(2)       
- ITENSMGV5 - VERSÃO 2-150 bytes
DD(2)T(1)CCCCCC(6)PPPPPP(6)VVV(3)D1(25)D2(25)RRRRRR(6)FFFF(4)IIIIII(6)DV(1)DE(1)CF(4)L(12)G(11)Z(1)CS(4)CT(4)FRAC(4)CE1(4)CE2(4)CONS(4)EAN(12)
- ITENSMGV5 - VERSÃO 3-156 bytes
DD(2)T(1)CCCCCC(6)PPPPPP(6)VVV(3)D1(25)D2(25)RRRRRR(6)FFFF(4)IIIIII(6)DV(1)DE(1)CF(4)L(12)G(11)Z(1)CS(4)CT(4)FRAC(4)CE1(4)CE2(4)CONS(4)EAN(12)GL(6) 

- ITENSMGV6 - VERSÃO- 247bytes
DD(2)T(1)CCCCCC(6)PPPPPP(6)VVV(3)D1(25)D2(25)RRRRRR(6)FFFF(4)IIIIII(6)DV(1)DE(1)CF(4)L(12)G(11)Z(1)CS(4)CT(4)FRAC(4)CE1(4)CE2(4)CONS(4)EAN(12)GL(6)|DA|D3(35)D4(35)CE3(6)CE4(6)MIDIA(6) (+CR+LF)

- ITENSMGV7 - VERSÃO- +271bytes
DD(2)T(1)CCCCCC(6)PPPPPP(6)VVV(3)D1(25)D2(25)RRRRRR(6)FFFF(4)IIIIII(6)DV(1)DE(1)CF(4)L(12)G(11)Z(1)CS(4)CT(4)FRAC(4)CE1(4)CE2(4)CONS(4)EAN(12)GL(6)|DA|D3(35)D4(35)CE3(6)CE4(6)MIDIA(6)PPPPPP(6)SF(1)|FFFFFFFF(n)|ST(1)|BNA(n)|G1(12)PG(4) (+CR+LF)
         */

        int version = 0;
        if (input.length() == 115) {
            //MGV5 V1
            int[] positions = {78, 77, 74};
            StringBuilder sb = new StringBuilder(input.replace("]", ""));
            for (int i = 0; i < positions.length; i++) {
                sb.insert(positions[i] + i, "0");
            }
            while (sb.length() < 275) {
                sb.append("0");
            }
            input = sb.toString();
        } else if (input.length() >= 115) {
            //MGV5 V2
            StringBuilder sb = new StringBuilder(input.replace("]", ""));
            while (sb.length() < 275) {
                sb.append("0");
            }
            input = sb.toString();
        } else {
            return false;
        }

        try {

            String[] input_parts = input.split("\\|");
            this.seccion = Integer.parseInt(input.substring(0, 2));
            switch (Integer.parseInt(input.substring(2, 3))) {

                case 0:
                    this.tipo = 'P';
                    break;
                case 1:
                    this.tipo = 'U';
                    break;
                case 2:
                    this.tipo = 'P';
                    break;
                case 3:
                    this.tipo = 'C';
                    break;
                case 4:
                    this.tipo = 'E';
                    break;
                case 5:
                    this.tipo = 'U';
                    break;
            }

            this.codigo = Integer.parseInt(input.substring(3, 9).trim());

            this.precio = Double.parseDouble(input.substring(9, 15).trim()) / 100;

            try {
                this.precio_2 = Double.parseDouble(input_parts[2].substring(88, 94).trim()) / 100;
            } catch (Exception e) {
                this.precio_2 = 0.0;
            }

            this.vencimiento = Integer.parseInt(input.substring(15, 18).trim());
            this.imp_vencimiento = this.vencimiento > 0;

            this.nombre = input.substring(18, 43).trim() + " " + input.substring(43, 68).trim();

            this.cod_inf_extra = Long.parseLong(input.substring(68, 74).trim());
            if (this.cod_inf_extra != 0) {
                if (!map_receing.containsKey(this.cod_inf_extra)) {
                    map_receing.put(this.cod_inf_extra, new ArrayList<Long>());
                }
                map_receing.get(this.cod_inf_extra).add(Long.valueOf(this.codigo));
            }

            // 74,78 - Cod_imagen
            this.cod_inf_nut = Long.parseLong(input.substring(78, 84).trim());
            if (this.cod_inf_nut != 0) {
                if (!map_nut_info.containsKey(this.cod_inf_nut)) {
                    map_nut_info.put(this.cod_inf_nut, new ArrayList<Long>());
                }
                map_nut_info.get(this.cod_inf_nut).add(Long.valueOf(this.codigo));
            }

            // 84,85 - Data validade
            // 85,86 - Cod_embalagem
            this.cod_ingredientes = Integer.parseInt(input.substring(86, 90).trim());
            if (this.cod_ingredientes != 0) {
                if (!map_ingredietes.containsKey(this.cod_ingredientes)) {
                    map_ingredietes.put(this.cod_ingredientes, new ArrayList<Long>());
                }
                map_ingredietes.get(this.cod_ingredientes).add(Long.valueOf(this.codigo));
            }

            this.cod_ingredientes1 = Integer.parseInt(input.substring(122, 126).trim());
            if (this.cod_ingredientes1 != 0) {
                if (!map_ingredietes2.containsKey(this.cod_ingredientes1)) {
                    map_ingredietes2.put(this.cod_ingredientes1, new ArrayList<Long>());
                }
                map_ingredietes2.get(this.cod_ingredientes1).add(Long.valueOf(this.codigo));
            }

            // 90,102 - Lote 
            this.cod_conservacion = Integer.parseInt(input.substring(134, 138).trim());
            // 136,139 - Conservacion

            if (this.cod_conservacion != 0) {
                if (!map_conservacion.containsKey(this.cod_conservacion)) {
                    map_conservacion.put(this.cod_conservacion, new ArrayList<Long>());
                }
                map_conservacion.get(this.cod_conservacion).add(Long.valueOf(this.codigo));
            }

            if (input.length() > 114) {
                this.primary_barcode_data = input.substring(102, 113).trim();
            } else {
                this.primary_barcode_data = input.substring(102, 111).trim();
            }

            try {
                this.cod_campo_extra2 = (int) Long.parseLong(input.substring(126, 130).trim());
                if (this.cod_campo_extra2 != 0) {
                    if (!map_extra2.containsKey(this.cod_campo_extra2)) {
                        map_extra2.put(this.cod_campo_extra2, new ArrayList<Long>());
                    }
                    map_extra2.get(this.cod_campo_extra2).add(Long.valueOf(this.codigo));
                }
            } catch (Exception e) {
                System.out.println("");
            }

            this.cod_tara = Integer.parseInt(input.substring(118, 122).trim());//TARA
            if (this.cod_tara != 0) {
                if (!map_tara.containsKey(this.cod_tara)) {
                    map_tara.put(this.cod_tara, new ArrayList<Long>());
                }
                map_tara.get(this.cod_tara).add(Long.valueOf(this.codigo));
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean parseItemsSystelMGV(int iId, String name, double dPrecio1, double dPrecio2, int iDepto, String sTipo, String sTara) {

        try {
            this.codigo = iId;
        } catch (Exception e) {
            return false;
        }

        this.nombre = name.trim();

        this.tipo = sTipo.charAt(0);

        this.precio = dPrecio1;
        this.precio_2 = dPrecio2;

        this.imp_vencimiento = true;
        if (this.imp_vencimiento) {
            this.vencimiento = 1;
            this.imp_vencimiento = this.vencimiento > 0;
        }
        this.seccion = iDepto;

        valor_tara = sTara;

        this.desc_campo_extra1 = "";
        this.desc_campo_extra2 = "";

        return true;
    }

    public boolean parseInputNutInfoItensSystelMGV(SystelNutInfo input) {

        try {
            this.info_set_id = "2";
            this.cod_inf_nut = 2;

            this.porcion_embalage = input.getPorcion_embalage();
            this.porcion = input.getPorcion();

            this.medida_casera = input.getMedida_casera();

            this.valor_energetico = input.getValor_energetico();
            this.valor_energetico_pct = input.getValor_energetico_pct();

            this.carbohidratos = input.getCarbohidratos();
            this.carbohidratos_pct = input.getCarbohidratos_pct();

            this.azucares_totales = input.getAzucares_totales();

            this.azucares_adicionales = input.getAzucares_adicionales();
            this.azucares_adicionales_pct = input.getAzucares_adicionales_pct();

            this.proteinas = input.getProteinas();

            this.proteinas_pct = input.getProteinas_pct();

            this.grasas_total = input.getGrasas_total();

            this.grasas_total_pct = input.getGrasas_total_pct();

            this.grasas_saturadas = input.getGrasas_saturadas();

            this.grasas_saturadas_pct = input.getGrasas_saturadas_pct();

            this.grasas_trans = input.getGrasas_trans();

            this.grasas_trans_pct = input.getGrasas_trans_pct();

            this.fibra = input.getFibra();

            this.fibra_pct = input.getFibra_pct();

            this.sodio = input.getSodio();

            this.sodio_pct = input.getSodio_pct();

            // no se están teniendo en cuenta los campos de lactosa y galactosa
            //tampoco a los campos adicionales
        } catch (Exception e) {
            System.err.println("");
        }
        return true;
    }

    public boolean parseInputNutInfoItensMGV(String input) {
        final String[] unidad_de_porcao = {
            "g",
            "ml",
            "un"
        };

        final String[] decimal_porcao = {
            "",
            ",25",
            ",33",
            ",5",
            ",66",
            ",75",};

        final String[] medida_caseira_utilizada = {
            "Colher(es) de Sopa",
            "Colher(es) de Café",
            "Colher(es) de Chá",
            "Xícara(s)",
            "De Xícara(s)",
            "Unidade(s)",
            "Pacote(s)",
            "Fatia(s)",
            "Fatia(s) Fina(s)",
            "Pedaço(s)",
            "Folha(s)",
            "Pão(es)",
            "Biscoito(s)",
            "Bisnaguinha(s)",
            "Disco(s)",
            "Copo(s)",
            "Porção(ões)",
            "Tablete(s)",
            "Sache(s)",
            "Almôndega(s)",
            "Bife(s)",
            "Filé(s)",
            "Concha(s)",
            "Bala(s)",
            "Prato(s) Fundo(s)",
            "Pitada(s)",
            "Lata(s)",
            "Xícara de Chá",
            "Prato raso"
        };

        try {

            if (!input.substring(0, 1).equals("N")) {
                return false;
            }
            String[] input_parts = input.split("\\|");

            this.cod_inf_nut = Long.parseLong(input.substring(1, 7).trim());
            this.info_set_id = "2";

            /*
0 001 150 0 01 3 30 0200 0200 000 000 050 020 000 000 000 00100 0 0 0 00000 00000

    {"column":  51, "color": "#fa06e6e8"}, // N(1)      input.substring(0, 1)
    {"column":  54, "color": "#fa06e6e8"}, // MMM(3)    input.substring(1, 4)
    {"column":  57, "color": "#fa06e6e8"}, // BBB(3)    input.substring(4, 7)
    {"column":  58, "color": "#fa06e6e8"}, // D(1)      input.substring(7, 8)
    {"column":  60, "color": "#fa06e6e8"}, // EE(2)     input.substring(8, 10)
    {"column":  61, "color": "#fa06e6e8"}, // F(1)      input.substring(10, 11)
    {"column":  63, "color": "#fa06e6e8"}, // GG(2)     input.substring(11, 13)
    {"column":  67, "color": "#fa06e6e8"}, // EEEE(4)   input.substring(13, 17)
    {"column":  71, "color": "#fa06e6e8"}, // IIII(4)   input.substring(17, 21)
    {"column":  74, "color": "#fa06e6e8"}, // JJJ(3)    input.substring(21, 24)
    {"column":  77, "color": "#fa06e6e8"}, // KKK(3)    input.substring(24, 27)
    {"column":  80, "color": "#fa06e6e8"}, // LLL(3)    input.substring(27, 30)
    {"column":  83, "color": "#fa06e6e8"}, // NNN(3)    input.substring(30, 33)
    {"column":  86, "color": "#fa06e6e8"}, // OOO(3)    input.substring(33, 36)
    {"column":  89, "color": "#fa06e6e8"}, // PPP(3)    input.substring(36, 39)
    {"column":  92, "color": "#fa06e6e8"}, // QQQ(3)    input.substring(39, 42)
    {"column":  97, "color": "#fa06e6e8"}, // UUUUU(5)  input.substring(42, 47)
    {"column":  98, "color": "#fa06e6e8"}, // R(1)      input.substring(47, 48)
    {"column":  99, "color": "#fa06e6e8"}, // S(1)      input.substring(48, 49)
    {"column":  100, "color": "#fa06e6e8"}, // T(1)     input.substring(49, 50)
             */
            if (input_parts.length > 1) {
//RDC Nº 429 - nueva tabla para brasil
                input = input_parts[1];
                this.porcion_embalage = input.substring(1, 4).trim() + " Porções";
                this.porcion = input.substring(4, 7)
                        + unidad_de_porcao[Integer.parseInt(input.substring(7, 8))];

                try {
                    this.medida_casera = input.substring(8, 10).replaceFirst("^0+(?!$)", "");
                } catch (Exception e) {
                    this.medida_casera = "";
                }

                try {
                    this.medida_casera = this.medida_casera + decimal_porcao[Integer.parseInt(input.substring(10, 11))];
                } catch (Exception e) {
                    this.medida_casera = "";
                }

                try {
                    this.medida_casera = this.medida_casera + " " + medida_caseira_utilizada[Integer.parseInt(input.substring(11, 13))];
                } catch (Exception e) {
                    this.medida_casera = "Porção(ões)";
                }

                try {
                    this.valor_energetico = input.substring(13, 17).replaceFirst("^0+(?!$)", "");
                    if (this.valor_energetico.equals(",0")) {
                        this.valor_energetico = "0";
                    }
                    this.valor_energetico_pct += Math.round((Double.parseDouble(
                            this.valor_energetico.replace(",", ".").trim()) / 20)) + "%";
                } catch (Exception e) {
                    this.valor_energetico_pct = "0%";
                }

                try {
                    this.carbohidratos = (input.substring(17, 20) + "," + input.substring(20, 21)).replaceFirst("^0+(?!$)", "");
                    if (this.carbohidratos.equals(",0")) {
                        this.carbohidratos = "0";
                    }
                    this.carbohidratos_pct += Math.round((Double.parseDouble(
                            this.carbohidratos.trim().replace(",", ".").trim()) / 3)) + "%";
                } catch (Exception e) {
                    this.carbohidratos_pct = "0%";
                }

                try {
                    this.azucares_totales = (input.substring(21, 23) + "," + input.substring(23, 24)).replaceFirst("^0+(?!$)", "");
                    if (this.azucares_totales.equals(",0")) {
                        this.azucares_totales = "0";
                    }
                } catch (Exception e) {
                    this.azucares_totales = "0";
                }

                try {
                    this.azucares_adicionales = (input.substring(24, 26) + "," + input.substring(26, 27)).replaceFirst("^0+(?!$)", "");
                    if (this.azucares_adicionales.equals(",0")) {
                        this.azucares_adicionales = "0";
                    }
                    this.azucares_adicionales_pct += Math.round((Double.parseDouble(
                            this.azucares_adicionales.trim().replace(",", ".").trim()) / 0.5)) + "%";
                } catch (Exception e) {
                    this.azucares_adicionales_pct = "0%";
                }

                try {
                    this.proteinas = (input.substring(27, 29) + "," + input.substring(29, 30)).replaceFirst("^0+(?!$)", "");
                    if (this.proteinas.equals(",0")) {
                        this.proteinas = "0";
                    }
                    this.proteinas_pct += Math.round((Double.parseDouble(
                            this.proteinas.trim().replace(",", ".").trim()) / 0.50)) + "%";
                } catch (Exception e) {
                    this.proteinas_pct = "0%";
                }

                try {
                    this.grasas_total = (input.substring(30, 32) + "," + input.substring(32, 33)).replaceFirst("^0+(?!$)", "");
                    if (this.grasas_total.equals(",0")) {
                        this.grasas_total = "0";
                    }
                    this.grasas_total_pct += Math.round((Double.parseDouble(
                            this.grasas_total.trim().replace(",", ".").trim()) / 0.65)) + "%";
                } catch (Exception e) {
                    this.grasas_total_pct = "0%";
                }

                try {
                    this.grasas_saturadas = (input.substring(33, 35) + "," + input.substring(35, 36)).replaceFirst("^0+(?!$)", "");
                    if (this.grasas_saturadas.equals(",0")) {
                        this.grasas_saturadas = "0";
                    }
                    this.grasas_saturadas_pct += Math.round((Double.parseDouble(
                            this.grasas_saturadas.trim().replace(",", ".").trim()) / 0.20)) + "%";
                } catch (Exception e) {
                    this.grasas_saturadas_pct = "0%";
                }

                try {
                    this.grasas_trans = (input.substring(36, 38) + "," + input.substring(38, 39)).replaceFirst("^0+(?!$)", "");
                    if (this.grasas_trans.equals(",0")) {
                        this.grasas_trans = "0";
                    }
                    this.grasas_trans_pct += Math.round((Double.parseDouble(
                            this.grasas_trans.trim().replace(",", ".").trim()) / 0.02)) + "%";
                } catch (Exception e) {
                    this.grasas_trans_pct = "0%";
                }

                try {
                    this.fibra = (input.substring(39, 41) + "," + input.substring(41, 42)).replaceFirst("^0+(?!$)", "");
                    if (this.fibra.equals(",0")) {
                        this.fibra = "0";
                    }
                    this.fibra_pct += Math.round((Double.parseDouble(
                            this.fibra.trim().replace(",", ".").trim()) / 0.25)) + "%";
                } catch (Exception e) {
                    this.fibra_pct = "0%";
                }

                try {
                    this.sodio = (input.substring(42, 46) + "," + input.substring(46, 47)).replaceFirst("^0+(?!$)", "");
                    if (this.sodio.equals(",0")) {
                        this.sodio = "0";
                    }
                    this.sodio_pct += Math.round((Double.parseDouble(
                            this.sodio.trim().replace(",", ".").trim()) / 20)) + "%";
                } catch (Exception e) {
                    this.sodio_pct = "0%";
                }

                // no se están teniendo en cuenta los campos de lactosa y galactosa
                //tampoco a los campos adicionales
            } else {
//RDC Nº 359/360         
                this.azucares_totales = "0";
                this.azucares_adicionales = "0";
                this.azucares_adicionales_pct = "0%";
                this.porcion_embalage = "0 Porções";

                this.porcion = input.substring(8, 11)
                        + unidad_de_porcao[Integer.parseInt(input.substring(11, 12))];

                try {
                    this.medida_casera = input.substring(12, 14).replaceFirst("^0+(?!$)", "");
                } catch (Exception e) {
                    this.medida_casera = "";
                }

                try {
                    this.medida_casera = this.medida_casera + decimal_porcao[Integer.parseInt(input.substring(14, 15))];
                } catch (Exception e) {
                    this.medida_casera = "";
                }

                try {
                    this.medida_casera = this.medida_casera + " " + medida_caseira_utilizada[Integer.parseInt(input.substring(15, 17))];
                } catch (Exception e) {
                    this.medida_casera = "Porção(ões)";
                }

                try {
                    this.valor_energetico = input.substring(17, 21).replaceFirst("^0+(?!$)", "");
                    if (this.valor_energetico.equals(",0")) {
                        this.valor_energetico = "0";
                    }
                    this.valor_energetico_pct += Math.round((Double.parseDouble(
                            this.valor_energetico.replace(",", ".").trim()) / 20)) + "%";
                } catch (Exception e) {
                    this.valor_energetico_pct = "0%";
                }

                try {
                    this.carbohidratos = (input.substring(21, 24) + "," + input.substring(24, 25)).replaceFirst("^0+(?!$)", "");
                    if (this.carbohidratos.equals(",0")) {
                        this.carbohidratos = "0";
                    }
                    this.carbohidratos_pct += Math.round((Double.parseDouble(
                            this.carbohidratos.trim().replace(",", ".").trim()) / 3)) + "%";
                } catch (Exception e) {
                    this.carbohidratos_pct = "0%";
                }

                try {
                    this.proteinas = (input.substring(25, 27) + "," + input.substring(27, 28)).replaceFirst("^0+(?!$)", "");
                    if (this.proteinas.equals(",0")) {
                        this.proteinas = "0";
                    }
                    this.proteinas_pct += Math.round((Double.parseDouble(
                            this.proteinas.trim().replace(",", ".").trim()) / 0.50)) + "%";
                } catch (Exception e) {
                    this.proteinas_pct = "0%";
                }

                try {
                    this.grasas_total = (input.substring(28, 30) + "," + input.substring(30, 31)).replaceFirst("^0+(?!$)", "");
                    if (this.grasas_total.equals(",0")) {
                        this.grasas_total = "0";
                    }
                    this.grasas_total_pct += Math.round((Double.parseDouble(
                            this.grasas_total.trim().replace(",", ".").trim()) / 0.65)) + "%";
                } catch (Exception e) {
                    this.grasas_total_pct = "0%";
                }

                try {
                    this.grasas_saturadas = (input.substring(31, 33) + "," + input.substring(33, 34)).replaceFirst("^0+(?!$)", "");
                    if (this.grasas_saturadas.equals(",0")) {
                        this.grasas_saturadas = "0";
                    }
                    this.grasas_saturadas_pct += Math.round((Double.parseDouble(
                            this.grasas_saturadas.trim().replace(",", ".").trim()) / 0.20)) + "%";
                } catch (Exception e) {
                    this.grasas_saturadas_pct = "0%";
                }

                try {
                    this.grasas_trans = (input.substring(34, 36) + "," + input.substring(36, 37)).replaceFirst("^0+(?!$)", "");
                    if (this.grasas_trans.equals(",0")) {
                        this.grasas_trans = "0";
                    }
                    this.grasas_trans_pct += Math.round((Double.parseDouble(
                            this.grasas_trans.trim().replace(",", ".").trim()) / 0.02)) + "%";
                } catch (Exception e) {
                    this.grasas_trans_pct = "0%";
                }

                try {
                    this.fibra = (input.substring(37, 39) + "," + input.substring(39, 40)).replaceFirst("^0+(?!$)", "");
                    if (this.fibra.equals(",0")) {
                        this.fibra = "0";
                    }
                    this.fibra_pct += Math.round((Double.parseDouble(
                            this.fibra.trim().replace(",", ".").trim()) / 0.25)) + "%";
                } catch (Exception e) {
                    this.fibra_pct = "0%";
                }

                try {
                    this.sodio = (input.substring(40, 44) + "," + input.substring(44, 45)).replaceFirst("^0+(?!$)", "");
                    if (this.sodio.equals(",0")) {
                        this.sodio = "0";
                    }
                    this.sodio_pct += Math.round((Double.parseDouble(
                            this.sodio.trim().replace(",", ".").trim()) / 20)) + "%";
                } catch (Exception e) {
                    this.sodio_pct = "0%";
                }
            }
        } catch (Exception e) {
            System.err.println("");
        }
        return true;
    }

    public boolean parseInputReceingItensMGV(String input) {
        try {
            this.cod_inf_extra = Long.parseLong(input.substring(0, 6));

            this.desc_campo_extra1 = input.substring(6).replaceAll("\\s+", " ").replace("]", "").trim();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean parseInputTaraMGV(String input) {
        if (input.substring(0, 1).equals("N")) {
            try {
                this.cod_tara = Integer.parseInt(input.substring(1, 5));
                this.valor_tara = (input.substring(6, 9).trim().replaceFirst("^0+(?!$)", "0") + "." + input.substring(9, 12).trim());
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            try {
                this.cod_tara = Integer.parseInt(input.substring(0, 4));
                this.valor_tara = (input.substring(4, 7).trim().replaceFirst("^0+(?!$)", "0") + "." + input.substring(9, 10).trim());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    }

    public boolean parseInputConservacionItensMGV(String input) {
        try {
            this.cod_conservacion = Integer.parseInt(input.substring(0, 4));

            this.desc_conservacion = input.substring(4).replaceAll("\\s+", " ").replace("]", "").trim();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean parseInputSDR(String input) {

        //se redefine esta variable para hacer dinámico al importador, en cuanto al largo del archivo
        //FILE_SDR_TXT[1] = input.length() - 1; //se quita esta opción porque en un archivo de brasil, viene la info de los ingradientes y explota por el largo
        this.seccion = Integer.parseInt(input.substring(FILE_SDR_DEP[0], FILE_SDR_DEP[1]).trim());
//        ///= Arrays.toString(nextLine).substring(FILE_SDR_NO_USAR[0], FILE_SDR_NO_USAR[1]).trim();
        this.tipo = Integer.parseInt(input.substring(FILE_SDR_PLU_TYPE[0], FILE_SDR_PLU_TYPE[1]).trim()) == 0 ? 'P' : 'U';
        this.codigo = Integer.parseInt(input.substring(FILE_SDR_PLU_CODE[0], FILE_SDR_PLU_CODE[1]).trim());
        this.precio = Double.parseDouble(input.substring(FILE_SDR_PRC[0], FILE_SDR_PRC[1]).trim()) / 100;
        this.vencimiento = Integer.parseInt(input.substring(FILE_SDR_CAD[0], FILE_SDR_CAD[1]).trim());
        this.imp_vencimiento = this.vencimiento > 0;

        this.nombre = input.substring(FILE_SDR_TXT[0], (FILE_SDR_TXT[0] + (input.length() - FILE_SDR_TXT[0])) - 1).trim();
        if (this.nombre.length() >= 56) {
            this.nombre = this.nombre.substring(0, 56);
        }
        return true;
    }

    public boolean parseInputSystelRDC429(String input) {
        String[] input_parsed = input.split("\\" + ConfigFile.getSeparator());

        if (input_parsed.length < 29) {
            return false;
        }

        this.codigo = Long.parseLong(input_parsed[1]);

        this.cod_inf_nut = "S".equalsIgnoreCase(input_parsed[14].trim()) ? 2 : 0;
        this.info_set_id = String.valueOf(this.cod_inf_nut);

        if (this.cod_inf_nut == 0) {
            // si no hay info nutri, salgo
            return false;
        }

        final Pattern pattern = Pattern.compile("(?i)(?:PORÇÃO|PORÇAO|PORCIÓN|PORCION)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
        final Matcher matcher = pattern.matcher(input_parsed[15].trim());

        this.medida_casera += matcher.replaceAll("").trim();
        this.porcion += this.medida_casera.substring(0, this.medida_casera.indexOf("g")).replace("(", "").replace(")", "").replaceAll("[^0-9]", "").trim();
        this.medida_casera = this.medida_casera.substring(this.medida_casera.indexOf("g") + 1, this.medida_casera.length()).trim();

        this.porcion_embalage = input_parsed[26].trim() + " Porções";

        this.valor_energetico += input_parsed[16].substring(0, input_parsed[16].indexOf("k") - 1).replaceAll("[^0-9]", "").trim();
        try {
            this.valor_energetico_pct += Math.round((Double.parseDouble(this.valor_energetico.replace(",", ".").trim()) / 20)) + "%";
        } catch (Exception e) {
            this.valor_energetico_pct = "0%";
        }

        this.carbohidratos += input_parsed[17].trim();
        try {
            this.carbohidratos_pct += Math.round((Double.parseDouble(
                    input_parsed[17].trim().replace(",", ".").trim()) / 3)) + "%";
        } catch (Exception e) {
            this.carbohidratos_pct = "0%";
        }

        this.proteinas += input_parsed[18].trim();
        try {
            this.proteinas_pct += Math.round((Double.parseDouble(this.proteinas.replace(",", ".").trim()) / 0.50)) + "%";
        } catch (Exception e) {
            this.proteinas_pct = "0%";
        }

        this.grasas_total += input_parsed[19].trim();
        try {
            this.grasas_total_pct += Math.round((Double.parseDouble(this.grasas_total.replace(",", ".").trim()) / 0.65)) + "%";
        } catch (Exception e) {
            this.grasas_total_pct = "0%";
        }

        this.grasas_saturadas += input_parsed[20].trim();
        try {
            this.grasas_saturadas_pct += Math.round((Double.parseDouble(this.grasas_saturadas.replace(",", ".").trim()) / 0.20)) + "%";
        } catch (Exception e) {
            this.grasas_saturadas_pct = "0%";
        }

        this.grasas_trans += input_parsed[21].trim();
        try {
            this.grasas_trans_pct += Math.round((Double.parseDouble(this.grasas_trans.replace(",", ".").trim()) / 0.02)) + "%";
        } catch (Exception e) {
            this.grasas_trans_pct = "0%";
        }

        this.fibra += input_parsed[22].trim();
        try {
            this.fibra_pct += Math.round((Double.parseDouble(this.fibra.replace(",", ".").trim()) / 0.25)) + "%";
        } catch (Exception e) {
            this.fibra_pct = "0%";
        }

        this.sodio += input_parsed[23].trim();
        try {
            this.sodio_pct += Math.round((Double.parseDouble(this.sodio.replace(",", ".").trim()) / 20)) + "%";
        } catch (Exception e) {
            this.sodio_pct = "0%";
        }

        this.azucares_totales += input_parsed[27].trim();

        this.azucares_adicionales += input_parsed[28].trim();
        try {
            this.azucares_adicionales_pct += Math.round((Double.parseDouble(this.azucares_adicionales.replace(",", ".").trim()) / 0.50)) + "%";
        } catch (Exception e) {
            this.sodio_pct = "0%";
        }

        return true;
    }

    public void doDepartments(PreparedStatement psDpt) throws SQLException {
        psDpt.setLong(1, this.seccion);
        psDpt.setString(2, "Seccion " + this.seccion);
        //customLogger.getInstance().writeLog(MainApp.INFO, "", "");
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Departamento: " + ibdata.seccion);
        psDpt.execute();
    }

    public void doDepartments(PreparedStatement psDpt, long id) throws SQLException {
        psDpt.setLong(1, id);
        psDpt.setString(2, "Seccion " + id);
        psDpt.execute();
    }

    public void doDepartments(PreparedStatement psDpt, long id, String name) throws SQLException {
        psDpt.setLong(1, id);
        psDpt.setString(2, name);
        psDpt.execute();
    }

    public void doProducts(PreparedStatement psPlu) throws SQLException {
        //-----------------------SQL_PRODUCT-----------------------
        psPlu.setLong(1, this.codigo);
        psPlu.setString(2, "" + this.codigo);
        psPlu.setString(3, this.nombre);

        switch (this.tipo) {
            case 'P':
                psPlu.setInt(4, 0);
                psPlu.setString(9, "1");
                break;
            case 'U':
                psPlu.setInt(4, 1);
                psPlu.setString(9, "2");
                break;
            case 'C':
                psPlu.setInt(4, 2);
                psPlu.setString(9, "1");
                break;
            case 'E':
                psPlu.setInt(4, 3);
                psPlu.setString(9, "1");
                break;
        }

        psPlu.setLong(5, this.seccion);
        psPlu.setString(6, "");
        psPlu.setString(7, this.imp_vencimiento ? "Y" : "N");

        if (this.vencimiento > 1) {
            psPlu.setInt(8, this.vencimiento - 1);
        } else {
            psPlu.setInt(8, this.vencimiento);
        }

        if (this.valor_tara.isEmpty()) {
            psPlu.setDouble(10, 0);
        } else {
            psPlu.setDouble(10, Double.parseDouble(this.valor_tara.replace(",", ".")));
        }
        String s_aux = this.info_extra.trim() + " " + this.desc_ingredientes.trim();
        psPlu.setString(11, s_aux.substring(0, Integer.min(1999, s_aux.length())));
        psPlu.setString(12, this.desc_campo_extra2.substring(0, Integer.min(1999, this.desc_campo_extra2.length())));
        psPlu.setString(13, this.desc_ingredientes.substring(0, Integer.min(1999, this.desc_ingredientes.length())));
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "PLU: " + this.codigo_plu);

        psPlu.execute();
    }

    public void doProductsRDC429(PreparedStatement psPlu) throws SQLException {
        //-----------------------SQL_PRODUCT-----------------------

        psPlu.setLong(1, this.codigo);
        psPlu.setString(2, String.valueOf(this.cod_inf_nut));
        psPlu.execute();

    }

    public void doProductsItensMGV(PreparedStatement psPlu) throws SQLException {
        //-----------------------SQL_PRODUCT-----------------------
        psPlu.setLong(1, this.codigo);
        psPlu.setString(2, "" + this.codigo);
        psPlu.setString(3, this.nombre);

        switch (this.tipo) {
            case 'P':
                psPlu.setInt(4, 0);
                psPlu.setString(9, "1");
                break;
            case 'U':
                psPlu.setInt(4, 1);
                psPlu.setString(9, "2");
                break;
            case 'C':
                psPlu.setInt(4, 2);
                psPlu.setString(9, "1");
                break;
            case 'E':
                psPlu.setInt(4, 3);
                psPlu.setString(9, "1");
                break;
        }

        psPlu.setLong(5, this.seccion);
        psPlu.setString(6, "");
        psPlu.setString(7, this.imp_vencimiento ? "Y" : "N");

        if (this.vencimiento > 1) {
            psPlu.setInt(8, this.vencimiento - 1);
        } else {
            psPlu.setInt(8, this.vencimiento);
        }

        if (this.valor_tara.isEmpty()) {
            psPlu.setDouble(10, 0);
        } else {
            psPlu.setDouble(10, Double.parseDouble(this.valor_tara.replace(",", ".")));
        }
        // String s_aux = this.info_extra.trim() + " " + this.desc_ingredientes.trim();
        //  psPlu.setString(11, s_aux.substring(0, Integer.min(1999, s_aux.length())));
        //  psPlu.setString(12, this.desc_campo_extra2.substring(0, Integer.min(1999, this.desc_campo_extra2.length())));
        //  psPlu.setString(13, this.desc_ingredientes.substring(0, Integer.min(1999, this.desc_ingredientes.length())));
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "PLU: " + this.codigo_plu);

        psPlu.execute();
    }

    public void doPrices(PreparedStatement psPrice) throws SQLException {
        psPrice.setString(1, "lst1");
        psPrice.setLong(2, this.codigo);

//                    if (this.precio  > 9999.99) {
//                        //customLogger.getInstance().writeLog(MainApp.ERROR, this.ip, "Precios: lista 1 | " + this.precio + " El precio no debe ser mayor a 9999.99");
//                        int x = 1 / 0;
//                    }
        psPrice.setDouble(3, this.precio);
        psPrice.setString(4, "Impo-" + MainApp.VersioneIP);
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Precios: lista 1 | " + this.codigo_plu);
        psPrice.execute();
//

        psPrice.setString(1, "lst2");
        psPrice.setLong(2, this.codigo);

//                    if (this.precio  > 9999.99) {
//                        //customLogger.getInstance().writeLog(MainApp.ERROR, this.ip, "Precios: lista 1 | " + this.precio + " El precio no debe ser mayor a 9999.99");
//                        int x = 1 / 0;
//                    }
        psPrice.setDouble(3, this.precio_2);
        psPrice.setString(4, "Impo-" + MainApp.VersioneIP);
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Precios: lista 1 | " + this.codigo_plu);
        psPrice.execute();

    }

    public void doNutInfo(PreparedStatement psNutInfo) throws SQLException {
        //-----------------------SQL_NUT_INFO-------------------------
        if (this.cod_inf_nut != 0) {

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, "PORÇÃO " + this.porcion);
            psNutInfo.setString(3, "PORÇÃO");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

//                        psNutInfo.setLong(1,this.codigo_plu);
//                        psNutInfo.setString(2, this.porcion);
//                        psNutInfo.setString(3,"PORÇÃO");
//                        psNutInfo.setInt(6, 0);                        psNutInfo.execute();
            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.valor_energetico);
            psNutInfo.setString(3, "VALOR ENERGÉTICO");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.valor_energetico_pct);
            psNutInfo.setString(3, "VALOR ENERGÉTICO");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.carbohidratos);
            psNutInfo.setString(3, "CARBOIDRATOS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.carbohidratos_pct);
            psNutInfo.setString(3, "CARBOIDRATOS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.proteinas);
            psNutInfo.setString(3, "PROTEÍNAS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.proteinas_pct);
            psNutInfo.setString(3, "PROTEÍNAS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_total);
            psNutInfo.setString(3, "GORDURAS TOTAIS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_total_pct);
            psNutInfo.setString(3, "GORDURAS TOTAIS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_saturadas);
            psNutInfo.setString(3, "GORDURAS SATURADAS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_saturadas_pct);
            psNutInfo.setString(3, "GORDURAS SATURADAS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_trans);
            psNutInfo.setString(3, "GORDURAS TRANS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_trans_pct);
            psNutInfo.setString(3, "GORDURAS TRANS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.fibra);
            psNutInfo.setString(3, "FIBRA ALIMENTAR");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.fibra_pct);
            psNutInfo.setString(3, "FIBRA ALIMENTAR");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.sodio);
            psNutInfo.setString(3, "SÓDIO");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.sodio_pct);
            psNutInfo.setString(3, "SÓDIO");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();
        }
    }

    public void doNutInfo2(PreparedStatement psNutInfo) throws SQLException {
        //-----------------------SQL_NUT_INFO-------------------------
        if (this.cod_inf_nut != 0) {

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.porcion_embalage);
            psNutInfo.setString(3, "Porções por embalagem:");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.porcion);
            psNutInfo.setString(3, "Porção:");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.medida_casera);
            psNutInfo.setString(3, "(Medida caseira)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, "%VD*");
            psNutInfo.setString(3, "%VD*");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.porcion);
            psNutInfo.setString(3, "000 g");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.valor_energetico);
            psNutInfo.setString(3, "Valor energético (kcal)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.valor_energetico_pct);
            psNutInfo.setString(3, "Valor energético (kcal)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.carbohidratos);
            psNutInfo.setString(3, "Carboidratos totais (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.carbohidratos_pct);
            psNutInfo.setString(3, "Carboidratos totais (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.azucares_totales);
            psNutInfo.setString(3, "  Açúcares totais (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.azucares_adicionales);
            psNutInfo.setString(3, "   Açúcares adicionados (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.azucares_adicionales_pct);
            psNutInfo.setString(3, "   Açúcares adicionados (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.proteinas);
            psNutInfo.setString(3, "Proteinas (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.proteinas_pct);
            psNutInfo.setString(3, "Proteinas (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_total);
            psNutInfo.setString(3, "Gorduras totais (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_total_pct);
            psNutInfo.setString(3, "Gorduras totais (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_saturadas);
            psNutInfo.setString(3, "  Gorduras saturadas (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_saturadas_pct);
            psNutInfo.setString(3, "  Gorduras saturadas (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_trans);
            psNutInfo.setString(3, "  Gorduras trans (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_trans_pct);
            psNutInfo.setString(3, "  Gorduras trans (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.fibra);
            psNutInfo.setString(3, "Fibra alimentar (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.fibra_pct);
            psNutInfo.setString(3, "Fibra alimentar (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.sodio);
            psNutInfo.setString(3, "Sódio (mg)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.sodio_pct);
            psNutInfo.setString(3, "Sódio (mg)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();
        }
    }

    public void doNutInfoRDC429(PreparedStatement psNutInfo) throws SQLException {
        //-----------------------SQL_NUT_INFO-------------------------
        if (this.cod_inf_nut != 0) {

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.porcion_embalage);
            psNutInfo.setString(3, "Porções por embalagem:");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.setInt(7, 1);
            psNutInfo.setInt(8, 0);
            psNutInfo.setString(9, "Y");
            psNutInfo.setInt(10, 3);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.porcion);
            psNutInfo.setString(3, "Porção:");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.setInt(7, 2);
            psNutInfo.setInt(8, 0);
            psNutInfo.setString(9, "Y");
            psNutInfo.setInt(10, 1);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.medida_casera);
            psNutInfo.setString(3, "(Medida caseira)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.setInt(7, 2);
            psNutInfo.setInt(8, 0);
            psNutInfo.setString(9, "Y");
            psNutInfo.setInt(10, 3);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, "%VD*");
            psNutInfo.setString(3, "%VD*");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 3);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.porcion);
            psNutInfo.setString(3, "000 g");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 3);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.valor_energetico);
            psNutInfo.setString(3, "Valor energético (kcal)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 4);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.valor_energetico_pct);
            psNutInfo.setString(3, "Valor energético (kcal)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 4);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.carbohidratos);
            psNutInfo.setString(3, "Carboidratos totais (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 5);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.carbohidratos_pct);
            psNutInfo.setString(3, "Carboidratos totais (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 5);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.azucares_totales);
            psNutInfo.setString(3, "  Açúcares totais (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 6);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.azucares_adicionales);
            psNutInfo.setString(3, "   Açúcares adicionados (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 7);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.azucares_adicionales_pct);
            psNutInfo.setString(3, "   Açúcares adicionados (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 7);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.proteinas);
            psNutInfo.setString(3, "Proteinas (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 8);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.proteinas_pct);
            psNutInfo.setString(3, "Proteinas (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 8);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_total);
            psNutInfo.setString(3, "Gorduras totais (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 9);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_total_pct);
            psNutInfo.setString(3, "Gorduras totais (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 9);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_saturadas);
            psNutInfo.setString(3, "  Gorduras saturadas (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 10);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_saturadas_pct);
            psNutInfo.setString(3, "  Gorduras saturadas (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 10);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_trans);
            psNutInfo.setString(3, "  Gorduras trans (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 11);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.grasas_trans_pct);
            psNutInfo.setString(3, "  Gorduras trans (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 11);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.fibra);
            psNutInfo.setString(3, "Fibra alimentar (g)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 12);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.fibra_pct);
            psNutInfo.setString(3, "Fibra alimentar (g)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 12);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.sodio);
            psNutInfo.setString(3, "Sódio (mg)");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 13);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo);
            psNutInfo.setString(2, this.sodio_pct);
            psNutInfo.setString(3, "Sódio (mg)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.setInt(7, 13);//posrow
            psNutInfo.setInt(8, 0);//rowto
            psNutInfo.setString(9, "N");//joincolum
            psNutInfo.setInt(10, 0);//poscolumto
            psNutInfo.execute();
        }
    }

    boolean parseInputExtra2MGV(String input) {
        try {
            this.cod_campo_extra2 = Integer.parseInt(input.substring(0, 4));

            this.desc_campo_extra2 = input.substring(4, input.length()).replaceAll("\\s+", " ").replace("]", "").trim();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    boolean parseInputIngredientesMGV(String input) {
        try {
            this.cod_ingredientes = Integer.parseInt(input.substring(0, 4));

            this.desc_ingredientes = input.substring(4, input.length()).replaceAll("\\s+", " ").replace("]", "").trim();
        } catch (Exception e) {
            return false;
        }

        this.desc_ingredientes = this.desc_ingredientes + " ";

        return true;
    }
}

/**
 *
 * @author gcastillo
 *
 * Contendra las funciones y datos necesarios para el manejo del CSV para
 * Tadicor.
 */
class GeneralPlu {

    public long codigo_plu = 0;
    public String codigo_erp = "0";
    public int seccion = 0;
    public String seccion_nombre = "";
    public int grupo = 0;
    public String grupo_nombre = "";
    public char tipo = 'P';
    public String nombre = "";
    public String description = "";
    public double precio = 0;
    public double precio_2 = 0;
    public int vencimiento = 0;
    public boolean imp_embalage = false;
    public boolean imp_vencimiento = false;
    public char activo = 'Y';
    //---
    public int cod_inf_extra = 0;
    public String info_extra = "";
    //---
    public int cod_inf_nut = 0;
    public String info_set_id = "0"; // Se agrega para manejar diferentes formatos de tablas.
    public String porcion = ""; // Se agrega ademas la medida casera.
    public String valor_energetico = "";
    public String carbohidratos = "";
    public String azucares_totales = "";
    public String azucares_totales_pct = "";
    public String azucares_adicionales = "";
    public String azucares_adicionales_pct = "";
    public String proteinas = "";
    public String grasas_total = "";
    public String grasas_saturadas = "";
    public String grasas_trans = "";
    public String fibra = "";
    public String sodio = "";
    public String valor_energetico_pct = "";
    public String carbohidratos_pct = "";
    public String proteinas_pct = "";
    public String grasas_total_pct = "";
    public String grasas_saturadas_pct = "";
    public String grasas_trans_pct = "";
    public String fibra_pct = "";
    public String sodio_pct = "";
    public String contiene_edulcorante = "";
    public String contiene_cafeina = "";

    //---
    public int cod_imagen = 0;
    public String path_imagen = "";
    //---
    public int cod_proveedor = 0;
    public String desc_proveedor = "";
    //---
    public int cod_tara = 0;
    public String valor_tara = "";
    //---
    public int cod_som = 0;
    public String desc_som = "";
    //---
    public int cod_fraccionador = 0;
    public String desc_fraccionador = "";
    //---
    public int cod_conservacion = 0;
    public String desc_conservacion = "";
    //---
    public int cod_campo_extra1 = 0;
    public String desc_campo_extra1 = "";
    //---
    public int cod_campo_extra2 = 0;
    public String desc_campo_extra2 = "";

    public String primary_barcode_flag = "N";
    public String primary_barcode_data = "";
    //---
    public int cod_origen = 0;
    public String desc_origen = "";
    //---
    public int cod_ingredientes = 0;
    public String desc_ingredientes = "";
    //---
    public List<Pair> par_precio_descuento; // key = precio. Value = rango

    public String PLU_label_w_name = "";
    public String PLU_label_u_name = "";
    public String label_ID = "";
    public String label_name = "";
    public String label_template = "";
    public int label_type = 0;

    //-- FIM --
    public GeneralPlu() {
        this.par_precio_descuento = new LinkedList<Pair>();
    }

    public void vaciarElementos() {
        codigo_plu = 0;
        codigo_erp = "0";
        seccion = 0;
        seccion_nombre = "";
        grupo = 0;
        grupo_nombre = "";
        tipo = 'P';
        nombre = "";
        description = "";
        precio = 0;
        precio_2 = 0;
        vencimiento = 0;
        imp_embalage = false;
        imp_vencimiento = false;
        activo = 'Y';
        //---
        cod_inf_extra = 0;
        info_extra = "";
        //---
        cod_inf_nut = 0;
        info_set_id = "0";
        porcion = ""; // Se agrega ademas la medida casera.
        valor_energetico = "";
        carbohidratos = "";
        proteinas = "";
        grasas_total = "";
        grasas_saturadas = "";
        grasas_trans = "";
        fibra = "";
        sodio = "";
        valor_energetico_pct = "";
        carbohidratos_pct = "";
        proteinas_pct = "";
        grasas_total_pct = "";
        grasas_saturadas_pct = "";
        grasas_trans_pct = "";
        fibra_pct = "";
        sodio_pct = "";
        //---
        cod_imagen = 0;
        path_imagen = "";
        //---        cod_proveedor = 0;
        desc_proveedor = "";
        //---
        cod_tara = 0;
        valor_tara = "";
        //---
        cod_som = 0;
        desc_som = "";
        //---
        cod_fraccionador = 0;
        desc_fraccionador = "";
        //---
        cod_conservacion = 0;
        desc_conservacion = "";
        //---
        cod_campo_extra1 = 0;
        desc_campo_extra1 = "";
        //---
        cod_campo_extra2 = 0;
        desc_campo_extra2 = "";
        //---
        primary_barcode_flag = "N";
        primary_barcode_data = "";
        //---
        cod_origen = 0;
        desc_origen = "";
        //---
        cod_ingredientes = 0;
        desc_ingredientes = "";
        //---
        par_precio_descuento.clear();
        azucares_totales = "";
        azucares_totales_pct = "";
        azucares_adicionales = "";
        azucares_adicionales_pct = "";
        contiene_edulcorante = "";
        contiene_cafeina = "";
        PLU_label_w_name = "";
        PLU_label_u_name = "";
        label_ID = "";
        label_name = "";
        label_template = "";
        label_type = 0;
    }

    /**
     *
     * @param input Linea a parsear.
     * @return True si llego al final del parseo de 1 PLU completo, false en
     * caso contrario.
     *
     *
     * 1- Código interno: de 1 a 5 dígitos 2- Código scanner: igual al código
     * interno 3- Descripción : 30 caracteres 4- Tipo de articulo: 1 pasable 0
     * unidad 5- Precio : real sin coma 6- Días de vencimiento (Cantidad de
     * días): sin dato hasta 3 dígitos 7- Segundo renglón de descripción: 30
     * caracteres 8- Código de departamento: 2 dígitos 9- Línea de ingrediente
     * 1: 50 caracteres 10- Línea de ingredientes 2: 50 caracteres 11- Línea de
     * ingredientes 3: 50 caracteres 12- Línea de ingredientes 4: 50 caracteres
     * 13- Línea de ingredientes 5: 50 caracteres 14- Línea de ingrediente 6: 50
     * caracteres 15- Línea de ingredientes 7: 50 caracteres 16- Línea de
     * ingredientes 8: 50 caracteres 17- Línea de ingredientes 9: 50 caracteres
     * 18- Línea de ingredientes 10: 50 caracteres 19- Tara: hasta 3 dígitos si
     * hay dato 20- Impresión de fecha: 21- Código etiqueta S: Imprime 2
     * etiqueta con nutricional N : no imprime 2da etiqueta con nutricional 22-
     * Inf. Nutric. Porción 23- Val. Energ X Porción 24- Val. Energ % Diario 25-
     * Carbohid X Porción 26- Carbohid %Porción 27- Azucares Totales Diarios 28-
     * Azucares totales % 29- Azucares Añadidos Diarios 30- Azucares Añadidos %
     * 31- Proteínas X Porción 32- Proteínas % Diario 33- Grasas Tot X Porc 34-
     * Grasas Tot % Diario 35- Grasas Sat X Porc 36- Grasas Sat % Diario 37-
     * Grasas Trans X Porc 38- Grasas Trans % Diario 39- Fibras X Porcion 40-
     * Fibras % Diario 41- Sodio X Porcion 42- Sodio % Diario 43- Tara: hasta 3
     * dígitos si hay dato 44- Impresión de fecha: nada 45- Leyenda contiene
     * edulcorante Código etiqueta S: Imprime N : no imprime 46- Leyenda
     * contiene Cafeína Código etiqueta S: Imprime N : no imprime *
     */
    public boolean parseInputCoto(String input) {
        int cant_pipes = StringUtils.countMatches(input, "|");
        if (cant_pipes != 43
                && cant_pipes != 44
                && cant_pipes != 45
                && cant_pipes != 25
                && (cant_pipes != 37 && cant_pipes != 20 && cant_pipes != 17)) {
            return false;
        }
        String[] input_parsed = input.split("\\|");

        DecimalFormat decimalFormat = new DecimalFormat("####0.0#"); // especifica el formato

        this.codigo_plu = Long.parseLong(String.valueOf(input_parsed[0].trim()));

        this.nombre = input_parsed[2].trim();

        this.tipo = "1".equals(input_parsed[3].trim()) ? 'P' : 'U';
        this.precio = Double.parseDouble(input_parsed[4].trim().replace(",", ".")) / 100;

        /*  VENCIMIENTO DEFINICION
        si viene vacío = fecha 0 dias pero imprime
        si viene un 0 = no imprime
        si si viene un dato > 0 = imprime ese dato
         */
        if (input_parsed[5].trim().isEmpty()) {
            this.imp_vencimiento = true;
            this.vencimiento = 0;
        } else if (input_parsed[5].trim().equalsIgnoreCase("0")) {
            this.imp_vencimiento = false;
            this.vencimiento = 0;
        } else {
            this.imp_vencimiento = true;
            this.vencimiento = Integer.parseInt(input_parsed[5].trim());
        }

        this.seccion = Integer.parseInt(input_parsed[7].trim());
        this.description = input_parsed[6].trim();

        this.desc_campo_extra1 = (input_parsed[8].trim()
                + input_parsed[9].trim()
                + input_parsed[10].trim()
                + input_parsed[11].trim()
                + input_parsed[12].trim()
                + input_parsed[13].trim()
                + input_parsed[14].trim()
                + input_parsed[15].trim()
                + input_parsed[16].trim()
                + input_parsed[17].trim());

        this.desc_campo_extra1 = this.desc_campo_extra1.replace("      ", "") + " ";

        if (input_parsed.length < 19) {
            // Esta clausula esta para los PLU que no tienen info NUT-
            this.cod_inf_nut = 0;
            return true;
        }

        this.cod_tara = input_parsed[18].isEmpty() ? 0 : 1;
        this.valor_tara = input_parsed[18].trim();

        this.imp_embalage = "S".equals(input_parsed[19].trim()) ? true : false;

        this.cod_inf_nut = "S".equals(input_parsed[20].trim()) ? 1 : 0;
        this.info_set_id = "3";

        this.porcion += input_parsed[21].trim();

        this.valor_energetico = input_parsed[22].trim().replaceFirst("^0+(?!$)", "");
        if (this.valor_energetico.equals(",0")) {
            this.valor_energetico = "0";
        }

        this.valor_energetico = this.valor_energetico + " kcal " + Math.round((Double.parseDouble(
                this.valor_energetico.replace(",", ".").trim()) * 4.1868)) + " kj";

        try {
            this.valor_energetico_pct += (decimalFormat.format(Double.parseDouble(input_parsed[23].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.valor_energetico_pct = "0%";
        }

        this.carbohidratos += input_parsed[24].trim().replace(".", ",") + " g";
        try {
            this.carbohidratos_pct += (decimalFormat.format(Double.parseDouble(input_parsed[25].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.carbohidratos_pct = "0%";
        }

        this.azucares_totales += input_parsed[26].trim().replace(".", ",") + " g";
        try {
            this.azucares_totales_pct += (decimalFormat.format(Double.parseDouble(input_parsed[27].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.azucares_totales_pct = "0%";
        }

        this.azucares_adicionales += input_parsed[28].trim().replace(".", ",") + " g";
        try {
            this.azucares_adicionales_pct += (decimalFormat.format(Double.parseDouble(input_parsed[29].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.azucares_adicionales_pct = "0%";
        }

        this.proteinas += input_parsed[30].trim().replace(".", ",") + " g";
        try {
            this.proteinas_pct += (decimalFormat.format(Double.parseDouble(input_parsed[31].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.proteinas_pct = "0%";
        }

        this.grasas_total += input_parsed[32].trim().replace(".", ",") + " g";
        try {
            this.grasas_total_pct += (decimalFormat.format(Double.parseDouble(input_parsed[33].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.grasas_total_pct = "0%";
        }

        this.grasas_saturadas += input_parsed[34].trim().replace(".", ",") + " g";
        try {
            this.grasas_saturadas_pct += (decimalFormat.format(Double.parseDouble(input_parsed[35].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.grasas_saturadas_pct = "0%";
        }

        this.grasas_trans += input_parsed[36].trim().replace(".", ",") + " g";
        try {
            this.grasas_trans_pct += (decimalFormat.format(Double.parseDouble(input_parsed[37].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.grasas_trans_pct = "0%";
        }

        this.fibra += input_parsed[38].trim().replace(".", ",") + " g";
        try {
            this.fibra_pct += (decimalFormat.format(Double.parseDouble(input_parsed[39].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.fibra_pct = "0%";
        }

        this.sodio += input_parsed[40].replace(",", ".") + " mg";

        try {
            this.sodio_pct += (decimalFormat.format(Double.parseDouble(input_parsed[41].replace(",", ".").trim())).replace(".", ",")) + "%";
        } catch (Exception e) {
            this.sodio_pct = "0%";
        }

        this.contiene_edulcorante = "S".equals(input_parsed[42].trim()) ? "Y" : "N";
        this.contiene_cafeina = "S".equals(input_parsed[43].trim()) ? "Y" : "N";

        if (cant_pipes == 44) { // SI TENGO 45 CAMPOS, LE PONEMOS EL MISMO TEMPLATE A AMBOS FORMATOS 
            try {
                this.PLU_label_w_name = ("1".equals(input_parsed[3].trim()) ? "P " : "U ") + input_parsed[44].toUpperCase().trim();
            } catch (Exception e) {
                this.PLU_label_w_name = "";
            }

            try {
                this.PLU_label_u_name = ("1".equals(input_parsed[3].trim()) ? "P " : "U ") + input_parsed[44].toUpperCase().trim();
            } catch (Exception e) {
                this.PLU_label_u_name = "";
            }
        } else if (cant_pipes == 45) { //SI TIENE 46 CAMPOS DIFERENCIAMOS LOS FORMATOS
            try {
                this.PLU_label_w_name = ("1".equals(input_parsed[3].trim()) ? "P " : "U ") + input_parsed[44].toUpperCase().trim();
            } catch (Exception e) {
                this.PLU_label_w_name = "";
            }

            try {
                this.PLU_label_u_name = ("1".equals(input_parsed[3].trim()) ? "P " : "U ") + input_parsed[45].toUpperCase().trim();
            } catch (Exception e) {
                this.PLU_label_u_name = "";
            }
        }
        return true;
    }

    boolean parseInputAnonima(String input) {
        String[] input_parsed = input.split("\\|");

        if (input_parsed.length < 20) {
            return false;
        }

        this.codigo_plu = Long.parseLong(String.valueOf(input_parsed[0].trim()));

        //this.primary_barcode_data = input_parsed[1].trim();
        this.primary_barcode_flag = "Y";

        this.nombre = input_parsed[2].trim();

        this.tipo = "1".equals(input_parsed[3].trim()) ? 'U' : 'P';
        this.precio = Double.parseDouble(input_parsed[4].trim().replace(",", ".")) / 100;

        /*  VENCIMIENTO DEFINICION
        si viene vacío = fecha 0 dias pero imprime
        si viene un 0 = no imprime
        si si viene un dato > 0 = imprime ese dato
         */
        if (input_parsed[5].trim().isEmpty()) {
            this.imp_vencimiento = true;
            this.vencimiento = 0;
        } else if (input_parsed[5].trim().equalsIgnoreCase("0")) {
            this.imp_vencimiento = false;
            this.vencimiento = 0;
        } else {
            this.imp_vencimiento = true;
            this.vencimiento = Integer.parseInt(input_parsed[5].trim());
        }

        this.description = input_parsed[6].trim();
        this.seccion = Integer.parseInt(input_parsed[7].trim());

        this.desc_ingredientes = (input_parsed[8]
                + "" + input_parsed[9]
                + "" + input_parsed[10]
                + "" + input_parsed[11]
                + "" + input_parsed[12]
                + "" + input_parsed[13]
                + "" + input_parsed[14]
                + "" + input_parsed[15]
                + "" + input_parsed[16]
                + "" + input_parsed[17]
                + "" + input_parsed[18]);

        this.desc_ingredientes = this.desc_ingredientes.replace("¦", "°").replace("¾", "ó").replace("  ", " ");

        this.valor_tara += input_parsed[19].trim();

        try {
            this.desc_campo_extra1 = (input_parsed[20].trim()
                    + input_parsed[21].trim());
        } catch (Exception e) {
            this.desc_campo_extra1 = input_parsed[20].trim();
        }

        if (input_parsed.length < 23) {
            // Esta clausula esta para los PLU que no tienen info NUT-
            this.cod_inf_nut = 0;
            return true;
        }

        this.cod_inf_nut = 1;
        this.info_set_id = "3";

        this.porcion += input_parsed[22].trim();

        try {
            this.valor_energetico = input_parsed[23].substring(0, input_parsed[23].indexOf("=")).replaceAll("[^0-9]", "");
            this.valor_energetico = this.valor_energetico + " kcal " + input_parsed[23].substring(input_parsed[23].indexOf("=") + 1).replaceAll("[^0-9]", "") + " kJ";
        } catch (Exception e) {
            return false;
        }

        try {
            this.valor_energetico_pct = Math.round((Double.parseDouble(input_parsed[24].replace(",", ".").trim()))) + " %";
        } catch (Exception e) {
            return false;
        }

        this.carbohidratos = input_parsed[25].trim().replace(".", ",") + " g";
        try {
            this.carbohidratos_pct = input_parsed[26].replace(",", ".").trim().replace(".", ",") + "%";
        } catch (Exception e) {
            return false;
        }

        if (input_parsed[27].trim().isEmpty()) {
            this.proteinas = "0 g";
        } else {
            this.proteinas = input_parsed[27].trim().replace(".", ",") + " g";
        }

        try {
            this.proteinas_pct = input_parsed[28].replace(",", ".").trim().replace(".", ",") + "%";
        } catch (Exception e) {
            return false;
        }

        if (input_parsed[29].trim().isEmpty()) {
            this.grasas_total = "0 g";
        } else {
            this.grasas_total = input_parsed[29].trim().replace(".", ",") + " g";
        }

        try {
            this.grasas_total_pct = input_parsed[30].replace(",", ".").trim().replace(".", ",") + "%";
        } catch (Exception e) {
            return false;
        }

        if (input_parsed[31].trim().isEmpty()) {
            this.grasas_saturadas = "0 g";
        } else {
            this.grasas_saturadas = input_parsed[31].trim().replace(".", ",") + " g";
        }
        try {
            this.grasas_saturadas_pct = input_parsed[32].replace(",", ".").trim().replace(".", ",") + "%";
        } catch (Exception e) {
            return false;
        }

        if (input_parsed[33].trim().isEmpty()) {
            this.grasas_trans = "0 g";
        } else {
            this.grasas_trans = input_parsed[33].trim().replace(".", ",") + " g";
        }

        if (input_parsed[35].trim().isEmpty()) {
            this.fibra = "0 g";
        } else {
            this.fibra = input_parsed[35].trim().replace(".", ",") + " g";
        }
        try {
            this.fibra_pct = input_parsed[36].replace(",", ".").trim().replace(".", ",") + "%";
        } catch (Exception e) {
            return false;
        }

        if (input_parsed[37].trim().isEmpty()) {
            this.sodio = "0 g";
        } else {
            this.sodio = input_parsed[37].replace(",", ".") + " mg";
        }
        try {
            this.sodio_pct = input_parsed[38].replace(",", ".").trim().replace(".", ",") + "%";
        } catch (Exception e) {
            return false;
        }

        this.contiene_edulcorante = "N";
        this.contiene_cafeina = "N";

        if (input_parsed.length > 39) {
            if (input_parsed[39].trim().isEmpty()) {
                this.azucares_totales = "0 g";
            } else {
                this.azucares_totales = input_parsed[39].trim().replace(".", ",") + " g";
            }
            if (input_parsed[40].trim().isEmpty()) {
                this.azucares_adicionales = "0 g";
            } else {
                this.azucares_adicionales = input_parsed[40].trim().replace(".", ",") + " g";
            }
        }

        return true;
    }

    public boolean parseInputLabelSystel(String input) {
        String[] input_parsed = input.split("\\;");
        if (input_parsed.length < 4) {
            return false;
        }

        try {
            this.label_ID = input_parsed[0];
            this.label_name = input_parsed[1].toUpperCase().trim();
            this.label_template = input_parsed[2];
            this.label_type = Integer.parseInt(input_parsed[3]);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean parseInputSicar(String input) {
        final int IDX_CODIGO = 0;
        final int IDX_NOMBRE = 2;
        final int IDX_PRECIO = 7;
        final int IDX_RANGO_P_1 = 8;
        final int IDX_RANGO_M_1 = 9;
        final int IDX_RANGO_P_2 = 10;
        final int IDX_RANGO_M_2 = 11;
        final int IDX_RANGO_P_3 = 12;
        final int IDX_RANGO_M_3 = 13;
        final int IDX_DPTO = 17;
        final int IDX_TIPO = 20;

        String[] input_parsed = input.split("\\;");

        if (input_parsed.length < 22) {
            return false;
        }
        if (!Utils.isNumeric(input_parsed[IDX_CODIGO])) {
            return false;
        }

        this.codigo_plu = Long.parseLong(input_parsed[IDX_CODIGO]);
        this.nombre = input_parsed[IDX_NOMBRE].trim();
        if (this.nombre.length() > 56) {
            this.nombre = this.nombre.substring(0, 56);
        }
        this.seccion_nombre = input_parsed[IDX_DPTO];
        this.precio = Double.parseDouble(input_parsed[IDX_PRECIO]);
        this.par_precio_descuento.add(
                new Pair(Double.parseDouble(input_parsed[IDX_RANGO_P_1]),
                        Double.parseDouble(input_parsed[IDX_RANGO_M_1]))
        );
        this.par_precio_descuento.add(
                new Pair(Double.parseDouble(input_parsed[IDX_RANGO_P_2]),
                        Double.parseDouble(input_parsed[IDX_RANGO_M_2]))
        );
        this.par_precio_descuento.add(
                new Pair(Double.parseDouble(input_parsed[IDX_RANGO_P_3]),
                        Double.parseDouble(input_parsed[IDX_RANGO_M_3]))
        );

        this.seccion_nombre = input_parsed[IDX_DPTO];
        this.tipo = input_parsed[IDX_TIPO].toCharArray()[0] == 'S' ? '1' : '0';
        return true;
    }

    public boolean parseInputBizerba(String input) {
        /*
        3  -- Código de producto (PLU)
        50 -- Descripción del producto (PLU)
        1  -- Peso o pieza (0 para peso y 1 para pieza)
        6  -- Precio normal (2 decimales)
        1  -- Alta o baja de PLU (0 para alta y 1 para baja)
        8  -- Código de barras
        1  -- Departamento
         */

        if (input.length() != 70) {
            return false;
        }

        this.codigo_plu = Long.parseLong(input.substring(0, 3));
        this.codigo_erp = input.substring(0, 3);
        this.nombre = input.substring(3, 53);
        this.tipo = input.substring(53, 54) == "1" ? 'U' : 'P';
        this.precio = Float.parseFloat(input.substring(54, 58) + '.' + input.substring(58, 60));
        this.activo = input.substring(60, 61) == "0" ? 'Y' : 'N';
        this.primary_barcode_flag = "Y";
        this.primary_barcode_data = input.substring(61, 69);
        this.seccion = Integer.parseInt(input.substring(69, 70));
        return true;
    }

    private static final int[] FILE_MT8450_PLU_CODE = new int[]{12, 17}; //PLU 
    private static final int[] FILE_MT8450_PRC = new int[]{34, 40};  //PRECIO  
    private static final int[] FILE_MT8450_CAD = new int[]{50, 53};    // días de vencimiento 
    private static int[] FILE_MT8450_TXT = new int[]{174, 207};      //NOMBRE línea 1
    private static int[] FILE_MT8450_TXT2 = new int[]{208, 220};      //NOMBRE línea 2

    public boolean parseInputMT8450(String input) {

        //se redefine esta variable para hacer dinámico al importador, en cuanto al largo del archivo
        FILE_MT8450_TXT2[1] = input.length() - 1;

        this.seccion = 0;
//        ///= Arrays.toString(nextLine).substring(FILE_MT8450_NO_USAR[0], FILE_MT8450_NO_USAR[1]).trim();
        this.tipo = 'P';
        this.codigo_plu = Long.parseLong(input.substring(FILE_MT8450_PLU_CODE[0], FILE_MT8450_PLU_CODE[1]).trim());
        this.precio = Double.parseDouble(input.substring(FILE_MT8450_PRC[0], FILE_MT8450_PRC[1]).trim()) / 100;
        this.vencimiento = Integer.parseInt(input.substring(FILE_MT8450_CAD[0], FILE_MT8450_CAD[1]).trim());
        this.imp_vencimiento = this.vencimiento > 0;
        if (input.length() > 209) {
            this.nombre = input.substring(FILE_MT8450_TXT[0], FILE_MT8450_TXT[1]).replace("^", "").trim() + " " + input.substring(FILE_MT8450_TXT2[0], FILE_MT8450_TXT2[1]).trim();
        } else if (input.length() > 176) {
            this.nombre = input.substring(FILE_MT8450_TXT[0], FILE_MT8450_TXT[1]).replace("^", "").trim();
        } else {
            return false;
        }
        return true;
    }

    public boolean parseInputSystel(String input) {
        String[] input_parsed = input.split("\\" + ConfigFile.getSeparator());
        DecimalFormat decimalFormat = new DecimalFormat("####0.0#"); // especifica el formato
        //if (input_parsed.length < 8 && input_parsed.length != 18 && (input_parsed.length != 38 && input_parsed.length != 18)) {
        if (input_parsed.length < 8 && input_parsed.length < 25) {
            return false;
        }

        this.codigo_plu = Long.parseLong(input_parsed[1]);

        this.nombre = input_parsed[2].trim();

        this.codigo_erp = input_parsed[3].trim();
        this.primary_barcode_flag = "Y";

//        this.tipo = "PESO".equalsIgnoreCase(input_parsed[6].trim()) ? 'P' : 'U';
        switch (input_parsed[6].trim().toUpperCase()) {
            case "PESO":
                this.tipo = 'P';
                break;
            case "PESABLE":
                this.tipo = 'P';
                break;
            case "P":
                this.tipo = 'P';
                break;
            case "UNIDAD":
                this.tipo = 'U';
                break;
            case "UNITARIO":
                this.tipo = 'U';
                break;
            case "U":
                this.tipo = 'U';
                break;
            default:
                this.tipo = 'P';
        }

        this.precio = Double.parseDouble(input_parsed[4].replace(",", ".").trim());
        this.precio_2 = Double.parseDouble(input_parsed[5].replace(",", ".").trim());

        this.imp_vencimiento = !input_parsed[7].trim().isEmpty();
        if (this.imp_vencimiento) {
            this.vencimiento = Integer.parseInt(input_parsed[7].trim());
            this.imp_vencimiento = this.vencimiento > 0;
        }
        this.seccion_nombre = input_parsed[0].trim();
        this.description = input_parsed[2].trim();

        if (input_parsed.length <= 8) {
            // Salimos porque es el formato corto de systel
            this.desc_campo_extra1 = "";
            this.desc_campo_extra2 = "";
            this.cod_inf_nut = 0;
            this.cod_tara = 0;
            this.valor_tara = "0";
            return true;
        }

        this.desc_campo_extra1 = input_parsed[8].trim().replace("      ", "");

        if (input_parsed.length <= 9) {
            // Salimos porque es el formato corto de systel

            this.cod_inf_nut = 0;
            this.cod_tara = 0;
            this.valor_tara = "0";
            return true;
        }

        this.cod_tara = input_parsed[9].isEmpty() ? 0 : 1;

        //la tara es expresada en gramos
        this.valor_tara = input_parsed[9].trim();
        /*if (input_parsed.length < 19) {
            // Esta clausula esta para los PLU que no tienen info NUT-
            this.cod_inf_nut = 0;
            return true;
        }*/
        this.desc_campo_extra2 = input_parsed[11].trim(); //es el origen
        this.desc_conservacion = input_parsed[12].trim();
        this.desc_ingredientes = input_parsed[13].trim();

        if ("S".equalsIgnoreCase(input_parsed[14].trim()) || input_parsed[14].trim().equalsIgnoreCase("1")) {
            this.cod_inf_nut = 1;
            this.info_set_id = "1";
        } else if (input_parsed[14].trim().equalsIgnoreCase("3")) {
            this.cod_inf_nut = 3;
            this.info_set_id = "3";
        } else {
            this.cod_inf_nut = 0;
            this.info_set_id = "0";
        }

        if (input_parsed.length < 16) {
            return true;
        }

        if (cod_inf_nut > 0) {
            this.porcion += input_parsed[15].trim();

            try {
                if (input_parsed[16].contains("k")) {
                    this.valor_energetico += input_parsed[16].substring(0, input_parsed[16].indexOf("k") - 1).replaceAll("[^0-9]", "").trim();
                } else {
                    this.valor_energetico += input_parsed[16].replaceAll("[^0-9]", "").trim();
                }
                this.valor_energetico = this.valor_energetico + " kcal " + Math.round((Double.parseDouble(this.valor_energetico.replace(",", ".").trim()) * 4.1868)) + " kJ";
            } catch (Exception e) {
                this.valor_energetico = "0g";
            }

            try {
                if (input_parsed[16].contains("k")) {
                    this.valor_energetico_pct += Math.round((Double.parseDouble(input_parsed[16].substring(0, input_parsed[16].indexOf("k") - 1).replaceAll("[^0-9]", "").replace(",", ".").trim()) / 20)) + "%";
                } else {
                    this.valor_energetico_pct += Math.round((Double.parseDouble(input_parsed[16].replaceAll("[^0-9]", "").replace(",", ".").trim()) / 20)) + "%";

                }
            } catch (Exception e) {
                this.valor_energetico_pct = "0%";
            }

            try {
                this.carbohidratos += input_parsed[17].trim().replace(".", ",") + " g";
                this.carbohidratos_pct += Math.round((Double.parseDouble(input_parsed[17].trim().replace(",", ".").trim()) / 3)) + "%";
            } catch (Exception e) {
                this.carbohidratos = "0g";
                this.carbohidratos_pct = "0%";
            }

            try {
                this.proteinas += input_parsed[18].trim().replace(".", ",") + " g";
                this.proteinas_pct += Math.round((Double.parseDouble(input_parsed[18].trim().replace(",", ".").trim()) / 0.75)) + "%";
            } catch (Exception e) {
                this.proteinas = "0g";
                this.proteinas_pct = "0%";
            }

            try {
                this.grasas_total += input_parsed[19].trim().replace(".", ",") + " g";
                this.grasas_total_pct += Math.round((Double.parseDouble(input_parsed[19].trim().replace(",", ".").trim()) / 0.55)) + "%";
            } catch (Exception e) {
                this.grasas_total = "0g";
                this.grasas_total_pct = "0%";
            }

            try {
                this.grasas_saturadas += input_parsed[20].trim().replace(".", ",") + " g";
                this.grasas_saturadas_pct += Math.round((Double.parseDouble(input_parsed[20].trim().replace(",", ".").trim()) / 0.22)) + "%";
            } catch (Exception e) {
                this.grasas_saturadas = "0g";
                this.grasas_saturadas_pct = "0%";
            }

            try {
                this.grasas_trans += input_parsed[21].trim().replace(".", ",") + " g";
            } catch (Exception e) {
                this.grasas_trans = "0g";
            }

            try {
                this.fibra += input_parsed[22].trim().replace(".", ",") + " g";
                this.fibra_pct += Math.round((Double.parseDouble(input_parsed[22].trim().replace(",", ".").trim()) / 0.25)) + "%";
            } catch (Exception e) {
                this.fibra = "0g";
                this.fibra_pct = "0%";
            }

            try {
                this.sodio += input_parsed[23].trim() + " mg";
                this.sodio_pct += Math.round((Double.parseDouble(input_parsed[23].trim().replace(",", ".").trim()) / 24)) + "%";
            } catch (Exception e) {
                this.sodio = "0mg";
                this.sodio_pct = "0%";
            }
        }

        if (input_parsed.length <= 25) {
            this.primary_barcode_data = "";
            return true;
        }

        if (!input_parsed[25].trim().isEmpty()) {
            this.primary_barcode_data = input_parsed[25].trim();

            /*  if (this.primary_barcode_data.length() == 12) {
                EAN13Bean generator = new EAN13Bean();
                UPCEANLogicImpl impl = generator.createLogicImpl();
                this.primary_barcode_data += impl.calcChecksum(this.primary_barcode_data);
            }
             */
        }

        if (input_parsed.length <= 26) {
            // si tiene más de 25 cmapos, estamos hablando de la versión 3 del archivo
            return true;
        }

        if (cod_inf_nut > 0) {
            try {
                this.cod_inf_nut = 3;
                this.info_set_id = "3";
                this.azucares_totales += input_parsed[26].trim().replace(".", ",") + " g";
            } catch (Exception e) {
                this.azucares_totales = "0g";
            }
            this.azucares_totales_pct = "(**)";

            try {
                this.cod_inf_nut = 3;
                this.info_set_id = "3";
                this.azucares_adicionales += input_parsed[27].trim().replace(".", ",") + " g";
            } catch (Exception e) {
                this.azucares_adicionales = "0g";
            }

            this.azucares_adicionales_pct = "(**)";

            this.contiene_edulcorante = "Y".equals(input_parsed[28].trim()) ? "Y" : "N";
            this.contiene_cafeina = "Y".equals(input_parsed[29].trim()) ? "Y" : "N";
        }

        this.activo = "Y".equals(input_parsed[30].trim()) ? 'Y' : 'N';

        try {
            this.grupo_nombre = input_parsed[31].trim();

        } catch (Exception e) {
            this.grupo_nombre = "";
        }

        return true;
    }

    public boolean parseInputSystelRangos(String input) {
        String[] input_parsed = input.split("\\" + ConfigFile.getSeparator());
        //if (input_parsed.length < 8 && input_parsed.length != 18 && (input_parsed.length != 38 && input_parsed.length != 18)) {
//        if (input_parsed.length < 3 && input_parsed.length != 3) {
//            return false;
//        }

        this.codigo_plu = Long.parseLong(input_parsed[0]);
        this.par_precio_descuento.add(new Pair(
                Double.parseDouble(input_parsed[2].replace(",", ".").trim()),
                Double.parseDouble(input_parsed[1].replace(",", ".").trim())));
        return true;
    }

    /**
     *
     * @param input Linea a parsear.
     * @return True si llego al final del parseo de 1 PLU completo, false en
     * caso contrario.
     */
    public boolean parseInputDigi(String input) {
        String[] input_parsed = input.split("\\|");
        /*if (input_parsed.length != 38 && input_parsed.length != 18) {
            return false;
        }*/

        this.codigo_plu = Long.parseLong(input_parsed[0]);

        this.nombre = input_parsed[2].trim();

        this.tipo = "4".equals(input_parsed[3].trim()) ? 'P' : 'U';
        this.precio = Double.parseDouble(input_parsed[4].trim()) / 100;
        this.imp_vencimiento = !input_parsed[5].trim().isEmpty();
        if (this.imp_vencimiento) {
            this.vencimiento = Integer.parseInt(input_parsed[5].trim());
            this.imp_vencimiento = this.vencimiento > 0;
        }
        this.seccion = Integer.parseInt(input_parsed[7].trim());
        this.description = input_parsed[6].trim();
        this.primary_barcode_flag = "Y";

        this.desc_campo_extra1 = (input_parsed[8].trim() + " "
                + input_parsed[9].trim() + " "
                + input_parsed[10].trim() + " "
                + input_parsed[11].trim() + " "
                + input_parsed[12].trim() + " "
                + input_parsed[13].trim() + " "
                + input_parsed[14].trim());

        this.desc_campo_extra1 = this.desc_campo_extra1.replace("      ", "") + " ";
        /*   input_parsed[16].trim() + " " + 
                                 input_parsed[17].trim() + " " + 
                                 input_parsed[18].trim() + " " + 
                                 input_parsed[19].trim() + " " + 
                                 input_parsed[20].trim() + " " + 
                                 input_parsed[21].trim() + " " + 
                                 input_parsed[22].trim() + " " + 
                                 input_parsed[23].trim() + " " + 
                                 input_parsed[45].trim());
         */

        this.desc_campo_extra2 = input_parsed[15].trim().replace("      ", "") + " ";

        /*if (input_parsed.length < 19) {
            // Esta clausula esta para los PLU que no tienen info NUT-
            this.cod_inf_nut = 0;
            return true;
        }*/
 /* APARENTEMENTE NO TIENE TARA
        this.cod_tara = input_parsed[15].isEmpty()? 0 : 1;
        this.valor_tara = input_parsed[15].trim();
         */
        //this.cod_inf_nut = "S".equals(input_parsed[20].trim())? 1 : 0;
        if (input_parsed.length <= 16) {
            this.cod_inf_nut = 0;
            return true;
        }
        this.cod_inf_nut = 1;

        this.porcion += input_parsed[16].trim();

        if (input_parsed.length <= 17) {
            return false;
        }
        this.valor_energetico += input_parsed[17].trim();
        this.valor_energetico_pct += input_parsed[18].trim() + "%";
        /*try {
            this.valor_energetico_pct += Math.round((Double.parseDouble(
                    input_parsed[22].replace(",", ".").trim()) / 20)) + "%";
        } catch (Exception e) {
            this.valor_energetico_pct = "0%";
        }*/

        if (input_parsed.length <= 19) {
            return false;
        }
        this.carbohidratos += input_parsed[19].trim();
        this.carbohidratos_pct += input_parsed[20].trim() + "%";
        /*try {
            this.carbohidratos_pct += Math.round((Double.parseDouble(
                    input_parsed[24].trim().replace(",", ".").trim()) / 3)) + "%";
        } catch (Exception e) {
            this.carbohidratos_pct = "0%";
        }*/

        if (input_parsed.length <= 21) {
            return false;
        }
        this.proteinas += input_parsed[21].trim();
        this.proteinas_pct += input_parsed[22].trim() + "%";
        /*try {
            this.proteinas_pct += Math.round((Double.parseDouble(
                    input_parsed[26].trim().replace(",", ".").trim()) / 0.75)) + "%";
        } catch (Exception e) {
            this.proteinas_pct = "0%";
        }*/

        if (input_parsed.length <= 23) {
            return false;
        }
        this.grasas_total += input_parsed[23].trim();
        this.grasas_total_pct += input_parsed[24].trim() + "%";
        /*try {
            this.grasas_total_pct += Math.round((Double.parseDouble(
                    input_parsed[28].trim().replace(",", ".").trim()) / 0.55)) + "%";
        } catch (Exception e) {
            this.grasas_total_pct = "0%";
        }*/

        if (input_parsed.length <= 25) {
            return false;
        }
        this.grasas_saturadas += input_parsed[25].trim();
        this.grasas_saturadas_pct += input_parsed[26].trim() + "%";
        /*try {
            this.grasas_saturadas_pct += Math.round((Double.parseDouble(
                    input_parsed[30].trim().replace(",", ".").trim()) / 0.22)) + "%";
        } catch (Exception e) {
            this.grasas_saturadas_pct = "0%";
        }*/

        if (input_parsed.length <= 27) {
            return false;
        }
        this.grasas_trans += input_parsed[27].trim();
        this.grasas_trans_pct += input_parsed[28].trim() + "%";

        if (input_parsed.length <= 29) {
            return false;
        }
        this.fibra += input_parsed[29].trim();
        this.fibra_pct += input_parsed[30].trim() + "%";
        /*try {
            this.fibra_pct += Math.round((Double.parseDouble(
                    input_parsed[34].trim().replace(",", ".").trim()) / 0.25)) + "%";
        } catch (Exception e) {
            this.fibra_pct = "0%";
        }*/

        if (input_parsed.length <= 31) {
            return false;
        }
        this.sodio += input_parsed[31].trim();
        this.sodio_pct += input_parsed[32].trim() + "%";
        /*try {
            this.sodio_pct += Math.round((Double.parseDouble(
                    input_parsed[36].trim().replace(",", ".").trim()) / 24)) + "%";
        } catch (Exception e) {
            this.sodio_pct = "0%";
        }*/

        return true;
    }

    public boolean parseInputPVMex(java.sql.ResultSet input_parsed) throws SQLException {
        //String[] input_parsed = input.split("\\|");
        /*if (input_parsed.length != 38 && input_parsed.length != 18) {
            return false;
        }*/

        this.codigo_plu = Long.parseLong(input_parsed.getString("codigo").trim());

        this.nombre = input_parsed.getString("descripcion").trim();

        this.tipo = "N".equals(input_parsed.getString(6).trim()) ? 'P' : 'U';
        this.precio = Double.parseDouble(input_parsed.getString("pventa").trim()) / 100;

        return true;
    }

    private static final int[] FILE_SDR_DEP = new int[]{1, 3};        //    "MOS","DEPTO",1,2,0                                         0 ID departamento & 1 name departamento
    private static final int[] FILE_SDR_NO_USAR = new int[]{3, 5};    //    "NO USAR",3,8,0                                            4 codigo_plu ERP
    private static final int[] FILE_SDR_PLU_TYPE = new int[]{5, 6};   //    "TYPE","TIPO PLU",9,12,0                                              2 PLU
    private static final int[] FILE_SDR_PLU_CODE = new int[]{6, 12};  //    "CODE","PLU CODIGO" ,13,37,0                                           3 nombre
    private static final int[] FILE_SDR_PRC = new int[]{12, 18};      //    "PRC","PRECIO",38,44,0                                          5 lista 1 & 6 lista 2
    private static final int[] FILE_SDR_CAD = new int[]{18, 21};      //    "CADUCIDAD  / No Pesado",45,45,0                              7 tipo producto 0=P - 1=U
    private static int[] FILE_SDR_TXT = new int[]{21, 71};      //    "TXT","NOMBRE PLU",46,57,0                                13 código de barras
    //private static int[] FILE_SDR_TXT ;

    public boolean parseInputSDR(String input) {

        //se redefine esta variable para hacer dinámico al importador, en cuanto al largo del archivo
        FILE_SDR_TXT[1] = input.length() - 1;

        this.seccion = Integer.parseInt(input.substring(FILE_SDR_DEP[0], FILE_SDR_DEP[1]).trim());
//        ///= Arrays.toString(nextLine).substring(FILE_SDR_NO_USAR[0], FILE_SDR_NO_USAR[1]).trim();
        this.tipo = Integer.parseInt(input.substring(FILE_SDR_PLU_TYPE[0], FILE_SDR_PLU_TYPE[1]).trim()) == 0 ? 'P' : 'U';
        this.codigo_plu = Long.parseLong(input.substring(FILE_SDR_PLU_CODE[0], FILE_SDR_PLU_CODE[1]).trim());
        this.precio = Double.parseDouble(input.substring(FILE_SDR_PRC[0], FILE_SDR_PRC[1]).trim()) / 100;
        this.vencimiento = Integer.parseInt(input.substring(FILE_SDR_CAD[0], FILE_SDR_CAD[1]).trim());
        this.imp_vencimiento = this.vencimiento > 0;

        this.nombre = input.substring(FILE_SDR_TXT[0], FILE_SDR_TXT[1]).trim();

        return true;
    }

    public boolean parseEleventas(int iId, String sCodigo, String sDescripcion, double dPrecio, int iDepto, String sDeptoNombre, String sTipo) {

        try {
            this.codigo_plu = Long.parseLong(sCodigo.trim());
        } catch (Exception e) {
            return false;
        }

        this.nombre = sDescripcion.trim();

        this.codigo_erp = String.valueOf(iId);

        this.primary_barcode_flag = "Y";
        this.primary_barcode_data = sCodigo.trim();

//        this.tipo = "PESO".equalsIgnoreCase(input_parsed[6].trim()) ? 'P' : 'U';
        switch (sTipo.trim()) {
            case "PESO":
                this.tipo = 'P';
                break;
            case "PESABLE":
                this.tipo = 'P';
                break;
            case "P":
                this.tipo = 'P';
                break;
            case "UNIDAD":
                this.tipo = 'U';
                break;
            case "UNITARIO":
                this.tipo = 'U';
                break;
            case "U":
                this.tipo = 'U';
                break;
            case "PZA":
                this.tipo = 'U';
                break;
            default:
                this.tipo = 'P';
        }

        this.precio = dPrecio;
        //this.precio_2 = dPrecio;

        this.imp_vencimiento = true;
        if (this.imp_vencimiento) {
            this.vencimiento = 1;
            this.imp_vencimiento = this.vencimiento > 0;
        }
        this.seccion = iDepto;
        this.seccion_nombre = sDeptoNombre.trim();
        this.description = sDescripcion.trim();

        this.desc_campo_extra1 = "";
        this.desc_campo_extra2 = "";

        this.activo = 'Y';

        return true;
    }

    public boolean parseMyBusinesPos(String iId, String sCodigo, String sDescripcion, double dPrecio, double dPrecio2, int iDepto, String sDeptoNombre, String sTipo, String sBarcode) {

        try {
            this.codigo_plu = Long.parseLong(iId);
        } catch (Exception e) {
            return false;
        }

        try {
            this.nombre = sDescripcion.trim();
        } catch (Exception e) {
            return false;
        }

        try {
            this.codigo_erp = sCodigo.trim();
        } catch (Exception e) {
            this.codigo_erp = "";
        }

        this.primary_barcode_flag = "Y";

        try {
            this.primary_barcode_data = sBarcode.trim();
        } catch (Exception e) {
            this.primary_barcode_data = "";
        }

//        this.tipo = "PESO".equalsIgnoreCase(input_parsed[6].trim()) ? 'P' : 'U';
        switch (sTipo.trim()) {
            case "PESO":
                this.tipo = 'P';
                break;
            case "PESABLE":
                this.tipo = 'P';
                break;
            case "P":
                this.tipo = 'P';
                break;
            case "UNIDAD":
                this.tipo = 'U';
                break;
            case "UNITARIO":
                this.tipo = 'U';
                break;
            case "U":
                this.tipo = 'U';
                break;
            case "PZA":
                this.tipo = 'U';
                break;
            default:
                this.tipo = 'P';
        }

        this.precio = dPrecio;
        this.precio_2 = dPrecio2;

        this.imp_vencimiento = true;
        if (this.imp_vencimiento) {
            this.vencimiento = 1;
            this.imp_vencimiento = this.vencimiento > 0;
        }
        this.seccion = iDepto;

        try {
            this.seccion_nombre = sDeptoNombre.trim();
        } catch (Exception e) {
            return false;
        }

        try {
            this.description = sDescripcion.trim();
        } catch (Exception e) {
            this.description = "";
        }

        this.desc_campo_extra1 = "";

        this.activo = 'Y';

        return true;
    }

    public boolean parseHanbai(String iId, String sCodigo, String sDescripcion, double dPrecio, double dPrecio2, String sDepto, String sDeptoNombre, String sTipo, String sBarcode, int iCaducidad) {

        try {
            this.codigo_plu = Long.parseLong(iId);
        } catch (Exception e) {
            return false;
        }

        try {
            this.nombre = sDescripcion.trim();
        } catch (Exception e) {
            return false;
        }

        try {
            this.codigo_erp = sCodigo.trim();
        } catch (Exception e) {
            this.codigo_erp = "";
        }

        this.primary_barcode_flag = "Y";

        try {
            this.primary_barcode_data = sBarcode.trim();
        } catch (Exception e) {
            this.primary_barcode_data = "";
        }

//        this.tipo = "PESO".equalsIgnoreCase(input_parsed[6].trim()) ? 'P' : 'U';
        switch (sTipo.trim()) {
            case "PESO":
                this.tipo = 'P';
                break;
            case "PESABLE":
                this.tipo = 'P';
                break;
            case "P":
                this.tipo = 'P';
                break;
            case "KG":
                this.tipo = 'P';
                break;
            case "UNIDAD":
                this.tipo = 'U';
                break;
            case "UNITARIO":
                this.tipo = 'U';
                break;
            case "U":
                this.tipo = 'U';
                break;
            case "PZA":
                this.tipo = 'U';
                break;
            case "PZ":
                this.tipo = 'U';
                break;
            default:
                this.tipo = 'P';
        }

        this.precio = dPrecio;
        this.precio_2 = dPrecio2;

        this.imp_vencimiento = true;
        if (this.imp_vencimiento) {
            this.vencimiento = 1;
            if (iCaducidad > 0) {
                this.vencimiento = iCaducidad;
            }
            this.imp_vencimiento = this.vencimiento > 0;
        }
        this.seccion = Integer.parseInt(sDepto);

        try {
            this.seccion_nombre = sDeptoNombre.trim();
        } catch (Exception e) {
            return false;
        }

        try {
            this.description = sDescripcion.trim();
        } catch (Exception e) {
            this.description = "";
        }

        this.desc_campo_extra1 = "";

        this.activo = 'Y';

        return true;
    }

    public void doDepartments(PreparedStatement psDpt) throws SQLException {

        if ((FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTEL)
                || (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SICAR)) {
            if (this.seccion_nombre.trim().isEmpty()) {
                psDpt.setString(1, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("SIN SECCION"));
            } else {
                psDpt.setString(1, this.seccion_nombre);
            }
        } else {
            psDpt.setLong(1, this.seccion);
            if (this.seccion_nombre.trim().isEmpty()) {
                psDpt.setString(2, "Seccion " + this.seccion);
            } else {
                psDpt.setString(2, this.seccion_nombre);
            }
        }

        //customLogger.getInstance().writeLog(MainApp.INFO, "", "");
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Departamento: " + ibdata.seccion);
        psDpt.execute();
    }

    public void doGroups(PreparedStatement psGrp) throws SQLException {

        if ((FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTEL)
                || (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SICAR)) {
            if (this.grupo_nombre.trim().isEmpty()) {
                psGrp.setString(1, java.util.ResourceBundle.getBundle("load/file/Bundle").getString("SIN AGRUPAR"));
            } else {
                psGrp.setString(1, this.grupo_nombre);
            }
        } else {
            psGrp.setLong(1, this.grupo);
            if (this.grupo_nombre.trim().isEmpty()) {
                psGrp.setString(2, "Grupo " + this.grupo);
            } else {
                psGrp.setString(2, this.grupo_nombre);
            }
        }

        //customLogger.getInstance().writeLog(MainApp.INFO, "", "");
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Departamento: " + ibdata.seccion);
        psGrp.execute();
    }

    public void doProducts(PreparedStatement psPlu, String ip) throws SQLException {
        CustomLogger customLogger = CustomLogger.getInstance();
        //-----------------------SQL_PRODUCT-----------------------
        psPlu.setLong(1, this.codigo_plu);

        if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTEL) {
            psPlu.setString(2, "" + this.codigo_erp);
        } else {
            psPlu.setString(2, "" + this.codigo_plu);
        }

        psPlu.setString(3, this.nombre);
        if (this.tipo == 'P') {
            psPlu.setInt(4, 0);
            psPlu.setString(9, "1");
        } else {
            psPlu.setInt(4, 1);
            psPlu.setString(9, "2");
        }
        psPlu.setLong(5, this.seccion);
        psPlu.setString(6, this.description);
        psPlu.setString(7, this.imp_vencimiento ? "Y" : "N");

        if (this.vencimiento > 1) {
            if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.COTO
                    || FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.ANONIMA) {
                psPlu.setInt(8, this.vencimiento);
            } else {
                psPlu.setInt(8, this.vencimiento - 1);
            }
        } else {
            psPlu.setInt(8, this.vencimiento);
        }

        if (this.valor_tara.isEmpty()) {
            psPlu.setDouble(10, 0);
        } else {
            if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.INTELLIBUILD || FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTEL) {
                psPlu.setDouble(10, Double.parseDouble(this.valor_tara.replace(",", ".")));
            } else {
                //el valor de la tara viene expresado en gramos y por eso hay que dividir ejemplo 51 / 1000 = 0.051
                psPlu.setDouble(10, (Double.parseDouble(this.valor_tara.replace(",", ".")) / 1000));
            }
        }

        psPlu.setString(11, this.desc_campo_extra1);
        psPlu.setString(12, this.desc_campo_extra2);

        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "PLU: " + this.codigo_plu);
        if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.COTO) {
            String ID_w = null;
            String ID_u = null;

            if (!this.PLU_label_w_name.equals("")) {
                if (FileLoader.getMap_label_format().containsKey(this.PLU_label_w_name)) {
                    ID_w = FileLoader.getMap_label_format().get(this.PLU_label_w_name);
                } else {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, this.PLU_label_w_name + java.util.ResourceBundle.getBundle("load/file/Bundle").getString("etiqueta no encontrada") + this.codigo_plu);
                }
            }

            if (!this.PLU_label_u_name.equals("")) {
                if (FileLoader.getMap_label_format().containsKey(this.PLU_label_u_name)) {
                    ID_u = FileLoader.getMap_label_format().get(this.PLU_label_u_name);
                } else {
                    customLogger.getInstance().writeLog(MainApp.ERROR, ip, this.PLU_label_u_name + java.util.ResourceBundle.getBundle("load/file/Bundle").getString("etiqueta no encontrada") + this.codigo_plu);
                }
            }

            psPlu.setString(13, ID_w);
            psPlu.setString(14, ID_u);

            //Coto solo tiene 14 argumentos.
            psPlu.execute();
            return;
        }

        psPlu.setString(13, this.primary_barcode_flag);
        psPlu.setString(14, this.primary_barcode_data);

        if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTEL
                || FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.ANONIMA) {
            // SYSTEL tiene hasta 16 argumentos nomas
            psPlu.setString(15, this.desc_ingredientes);
            psPlu.setString(16, this.desc_conservacion);
            psPlu.setString(17, String.valueOf(this.activo));

            if (psPlu.getParameterMetaData().getParameterCount() > 17) {
                psPlu.setLong(18, this.grupo);
            }

            psPlu.execute();
            return;

        }

        psPlu.setString(15, String.valueOf(this.activo));

        psPlu.execute();
    }

    public void doProductsEleventas(PreparedStatement psPlu) throws SQLException {
        //-----------------------SQL_PRODUCT-----------------------
        psPlu.setLong(1, this.codigo_plu);
        psPlu.setString(2, "" + this.codigo_erp);

        psPlu.setString(3, this.nombre);
        if (this.tipo == 'P') {
            psPlu.setInt(4, 0);
            psPlu.setString(9, "1");
        } else {
            psPlu.setInt(4, 1);
            psPlu.setString(9, "2");
        }
        psPlu.setLong(5, this.seccion);
        psPlu.setString(6, this.description);
        psPlu.setString(7, this.imp_vencimiento ? "Y" : "N");

        if (this.vencimiento > 1) {
            if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.COTO
                    || FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.ANONIMA) {
                psPlu.setInt(8, this.vencimiento);
            } else {
                psPlu.setInt(8, this.vencimiento - 1);
            }
        } else {
            psPlu.setInt(8, this.vencimiento);
        }

//        if (this.valor_tara.isEmpty()) {
        psPlu.setDouble(10, 0);
//        } else {
//            if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.INTELLIBUILD || FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTEL) {
//                psPlu.setDouble(10, Double.parseDouble(this.valor_tara.replace(",", ".")));
//            } else {
//                //el valor de la tara viene expresado en gramos y por eso hay que dividir ejemplo 51 / 1000 = 0.051
//                psPlu.setDouble(10, (Double.parseDouble(this.valor_tara.replace(",", ".")) / 1000));
//            }
//        }
//
        psPlu.setString(11, this.desc_campo_extra1);
        psPlu.setString(12, this.desc_campo_extra2);

        psPlu.setString(13, this.primary_barcode_flag);
        psPlu.setString(14, this.primary_barcode_data);

//        if (FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.SYSTEL
//                || FileLoader.getFtype() == load.file.ConfigProperties.ConfigFile.FileType.ANONIMA) {
//            // SYSTEL tiene hasta 16 argumentos nomas
//            psPlu.setString(15, this.desc_ingredientes);
//            psPlu.setString(16, this.desc_conservacion);
//            psPlu.setString(17, String.valueOf(this.activo));
//            psPlu.execute();
//            return;
//
//        }
        psPlu.setString(15, String.valueOf(this.activo));

        psPlu.execute();
    }

    public void doProductsMT8450(PreparedStatement psPlu) throws SQLException {
        //-----------------------SQL_PRODUCT-----------------------
        psPlu.setLong(1, this.codigo_plu);
        psPlu.setString(2, "" + this.codigo_plu);

        if (this.nombre.length() >= 56) {
            this.nombre = this.nombre.substring(0, 56);
        }

        psPlu.setString(3, this.nombre);

        switch (this.tipo) {
            case 'P':
                psPlu.setInt(4, 0);
                psPlu.setString(9, "1");
                break;
            case 'U':
                psPlu.setInt(4, 1);
                psPlu.setString(9, "2");
                break;
            case 'C':
                psPlu.setInt(4, 2);
                psPlu.setString(9, "1");
                break;
            case 'E':
                psPlu.setInt(4, 3);
                psPlu.setString(9, "1");
                break;
        }

        psPlu.setLong(5, this.seccion);
        psPlu.setString(6, "");
        psPlu.setString(7, this.imp_vencimiento ? "Y" : "N");

        if (this.vencimiento > 1) {
            psPlu.setInt(8, this.vencimiento - 1);
        } else {
            psPlu.setInt(8, this.vencimiento);
        }

        if (this.valor_tara.isEmpty()) {
            psPlu.setDouble(10, 0);
        } else {
            psPlu.setDouble(10, Double.parseDouble(this.valor_tara.replace(",", ".")));
        }
        // String s_aux = this.info_extra.trim() + " " + this.desc_ingredientes.trim();
        //  psPlu.setString(11, s_aux.substring(0, Integer.min(1999, s_aux.length())));
        //  psPlu.setString(12, this.desc_campo_extra2.substring(0, Integer.min(1999, this.desc_campo_extra2.length())));
        //  psPlu.setString(13, this.desc_ingredientes.substring(0, Integer.min(1999, this.desc_ingredientes.length())));
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "PLU: " + this.codigo_plu);

        psPlu.execute();
    }

    public void doPrices(PreparedStatement psPrice) throws SQLException {
        psPrice.setString(1, "lst1");
        psPrice.setLong(2, this.codigo_plu);

//                    if (this.precio  > 9999.99) {
//                        //customLogger.getInstance().writeLog(MainApp.ERROR, this.ip, "Precios: lista 1 | " + this.precio + " El precio no debe ser mayor a 9999.99");
//                        int x = 1 / 0;
//                    }
        psPrice.setDouble(3, this.precio);
        psPrice.setString(4, "Impo-" + MainApp.VersioneIP);
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Precios: lista 1 | " + this.codigo_plu);
        psPrice.execute();

        psPrice.setString(1, "lst2");
        psPrice.setLong(2, this.codigo_plu);

//                    if (this.precio  > 9999.99) {
//                        //customLogger.getInstance().writeLog(MainApp.ERROR, this.ip, "Precios: lista 1 | " + this.precio + " El precio no debe ser mayor a 9999.99");
//                        int x = 1 / 0;
//                    }
        psPrice.setDouble(3, this.precio_2);
        psPrice.setString(4, "Impo-" + MainApp.VersioneIP);
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Precios: lista 1 | " + this.codigo_plu);
        psPrice.execute();
    }

    public void doPricesEleventas(PreparedStatement psPrice) throws SQLException {
        psPrice.setString(1, "lst1");
        psPrice.setLong(2, this.codigo_plu);

//                    if (this.precio  > 9999.99) {
//                        //customLogger.getInstance().writeLog(MainApp.ERROR, this.ip, "Precios: lista 1 | " + this.precio + " El precio no debe ser mayor a 9999.99");
//                        int x = 1 / 0;
//                    }
        psPrice.setDouble(3, this.precio);
        psPrice.setString(4, "Impo-" + MainApp.VersioneIP);
        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Precios: lista 1 | " + this.codigo_plu);
        psPrice.execute();
//
//        psPrice.setString(1, "lst2");
//        psPrice.setLong(2, this.codigo_plu);
//
////                    if (this.precio  > 9999.99) {
////                        //customLogger.getInstance().writeLog(MainApp.ERROR, this.ip, "Precios: lista 1 | " + this.precio + " El precio no debe ser mayor a 9999.99");
////                        int x = 1 / 0;
////                    }
//        psPrice.setDouble(3, this.precio_2);
//        psPrice.setString(4, "Impo-" + MainApp.VersioneIP);
//        //customLogger.getInstance().writeLog(MainApp.INFO, this.ip, "Precios: lista 1 | " + this.codigo_plu);
//        psPrice.execute();
    }

    public void doNutInfo(PreparedStatement psNutInfo) throws SQLException {
        //-----------------------SQL_NUT_INFO-------------------------
        if (this.cod_inf_nut != 0) {

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, "PORCION " + this.porcion.replace("PORCION", "").replace("PORCIÓN", "").replace("porción", "").replace("porcion", "").trim());
            psNutInfo.setString(3, "PORCION");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

//                        psNutInfo.setLong(1,this.codigo_plu);
//                        psNutInfo.setString(2, this.porcion);
//                        psNutInfo.setString(3,"PORÇÃO");
//                        psNutInfo.execute();
            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.valor_energetico);
            psNutInfo.setString(3, "VALOR ENERGETICO");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.valor_energetico_pct);
            psNutInfo.setString(3, "VALOR ENERGETICO");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.carbohidratos);
            psNutInfo.setString(3, "CARBOHIDRATOS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.carbohidratos_pct);
            psNutInfo.setString(3, "CARBOHIDRATOS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.proteinas);
            psNutInfo.setString(3, "PROTEINAS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.proteinas_pct);
            psNutInfo.setString(3, "PROTEINAS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_total);
            psNutInfo.setString(3, "GRASAS TOTALES");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_total_pct);
            psNutInfo.setString(3, "GRASAS TOTALES");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_saturadas);
            psNutInfo.setString(3, "GRASAS SATURADAS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_saturadas_pct);
            psNutInfo.setString(3, "GRASAS SATURADAS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_trans);
            psNutInfo.setString(3, "GRASAS TRANS");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_trans_pct);
            psNutInfo.setString(3, "GRASAS TRANS");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.fibra);
            psNutInfo.setString(3, "FIBRA ALIMENTARIA");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.fibra_pct);
            psNutInfo.setString(3, "FIBRA ALIMENTARIA");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.sodio);
            psNutInfo.setString(3, "SODIO");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.sodio_pct);
            psNutInfo.setString(3, "SODIO");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, "1");
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();
        }
    }

    public void doNutInfo3(PreparedStatement psNutInfo) throws SQLException {
        //-----------------------SQL_NUT_INFO-------------------------
        if (this.cod_inf_nut != 0) {

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, "Porción " + this.porcion.replace("PORCION", "").replace("PORCIÓN", "").replace("porción", "").replace("porcion", "").trim());
            psNutInfo.setString(3, "Porción");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 2);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, "%VD(*)");
            psNutInfo.setString(3, "%VD(*)");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, "Cantidad por porción");
            psNutInfo.setString(3, "Cantidad por porción");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.valor_energetico);
            psNutInfo.setString(3, "Valor Energetico");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.valor_energetico_pct);
            psNutInfo.setString(3, "Valor Energetico");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.carbohidratos);
            psNutInfo.setString(3, "Carbohidratos, de los cuales:");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.carbohidratos_pct);
            psNutInfo.setString(3, "Carbohidratos, de los cuales:");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.azucares_totales);
            psNutInfo.setString(3, " Azucares totales");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, "(**)");
            psNutInfo.setString(3, " Azucares totales");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.azucares_adicionales);
            psNutInfo.setString(3, " Azucares añadidos");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, "(**)");
            psNutInfo.setString(3, " Azucares añadidos");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.proteinas);
            psNutInfo.setString(3, "Proteinas");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.proteinas_pct);
            psNutInfo.setString(3, "Proteinas");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_total);
            psNutInfo.setString(3, "Grasas totales");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_total_pct);
            psNutInfo.setString(3, "Grasas totales");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_saturadas);
            psNutInfo.setString(3, "Grasas saturadas");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_saturadas_pct);
            psNutInfo.setString(3, "Grasas saturadas");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.grasas_trans);
            psNutInfo.setString(3, "Grasas trans");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, "(**)");
            psNutInfo.setString(3, "Grasas trans");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.fibra);
            psNutInfo.setString(3, "Fibra alimentaria");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.fibra_pct);
            psNutInfo.setString(3, "Fibra alimentaria");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.sodio);
            psNutInfo.setString(3, "Sodio");
            psNutInfo.setInt(4, 1);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.sodio_pct);
            psNutInfo.setString(3, "Sodio");
            psNutInfo.setInt(4, 2);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.contiene_edulcorante);
            psNutInfo.setString(3, "Edulcorante");
            psNutInfo.setInt(4, 0);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

            psNutInfo.setLong(1, this.codigo_plu);
            psNutInfo.setString(2, this.contiene_cafeina);
            psNutInfo.setString(3, "Cafeina");
            psNutInfo.setInt(4, 0);
            psNutInfo.setString(5, this.info_set_id);
            psNutInfo.setInt(6, 0);
            psNutInfo.execute();

        }
    }

    public void doDiscounts(PreparedStatement psDiscounts) throws SQLException {
        //-----------------------SQL_DISCOUNT_SCHEMA-------------------------
        for (Pair par : this.par_precio_descuento) {
            psDiscounts.setLong(1, this.codigo_plu);
            psDiscounts.setDouble(2, (double) par.getValue());
            psDiscounts.setDouble(3, (double) par.getKey());
            psDiscounts.execute();
        }

    }

    public void doDeleteSchemaDiscount(PreparedStatement psDiscountsDel) throws SQLException {
        psDiscountsDel.execute();
    }

}
