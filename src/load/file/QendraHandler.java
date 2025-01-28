/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gcastillo
 */
public class QendraHandler {

    private static final int the_number = 1; //INTEGER,
    private static final int the_seccion = 2; //INTEGER,
    private static final int the_plu_name = 3; //VARCHAR(18),
    private static final int the_codigo = 4; //INTEGER,
    private static final int the_type = 5; //SMALLINT,
    private static final int the_tare = 6; //INTEGER,
    private static final int the_vencimiento = 7; //SMALLINT,
    private static final int the_price = 8; //FLOAT,
    private static final int the_price2 = 9; //FLOAT,
    private static final int the_others = 10; //BLOB SUB_TYPE 0,
    private static final int the_modifby = 11; //VARCHAR(60),
    private static final int the_porc_agua = 12; //DECIMAL,
    private static final int the_tn_activa = 13; //SMALLINT,
    private static final int the_tn_desc = 14; //VARCHAR(30),
    private static final int the_tn_cal_porc = 15; //FLOAT,
    private static final int the_tn_carbohidratos = 16; //FLOAT,
    private static final int the_tn_proteinas = 17; //FLOAT,
    private static final int the_tn_grasas_tot = 18; //FLOAT,
    private static final int the_tn_grasas_sat = 19; //FLOAT,
    private static final int the_tn_grasas_trans = 20; //FLOAT,
    private static final int the_tn_fibra = 21; //FLOAT,
    private static final int the_tn_sodio = 22; //FLOAT,
    private static final int the_origen = 23; //INTEGER,
    private static final int the_conserv = 24; //INTEGER,
    private static final int the_recing = 25; //INTEGER,
    private static final int the_lote = 26; //VARCHAR(13),
    private static final int the_ean_tipo = 27; //SMALLINT,
    private static final int the_ean_cfg = 28; //VARCHAR(12)

    private static final String VALUES_REGEX = "\\$\\{values\\}";
    private static final String SQL_PRODUCT = "execute procedure merge_product(${values})";
    private static final String SQL_DEPARTMENT = "execute procedure merge_department(${values})";
    private static final String SQL_ORIGEN = "execute procedure merge_origen(${values})";
    private static final String SQL_DATOS_CONSERV = "execute procedure merge_datos_conserv(${values})";
    private static final String SQL_INGREDIENTES = "execute procedure merge_ingredientes(${values})";
    private Connection jdbc_conn;
    private String value_others = "";
    private int id_ing = 0; // hasta 700
    private int id_ing_repetido = 0; // hasta 700
    private int id_cons = 0; // hasta 700
    private int id_ori = 0; // hasta 700
    private int id_plu = 0;// hasta 8000
    private boolean b_ing = false;
    private boolean b_ing_repetido = false;
    private boolean b_cons = false;
    private boolean b_ori = false;
    private final int iLimiteRegistrosPermitidos = 700;
    private final int iLimiteMaximoDeColumna = 300;
    private final HashMap<String, Integer> mapIngredientes = new HashMap<>();
    CustomLogger customLogger = CustomLogger.getInstance();
    public QendraHandler() {

    }

    public boolean connectDB(String path) throws ClassNotFoundException {
        try {
            String driver_conn_string = "";
            /*driver_conn_string = "jdbc:firebirdsql://127.0.0.1:3050/"+path;                    
            Class.forName("org.firebirdsql.jdbc.FBDriver");
             */
            //java.sql.Driver driver;
            //driver = java.sql.DriverManager.getDriver(driver_conn_string);
            driver_conn_string = "jdbc:firebirdsql://127.0.0.1:3050/" + path;

            Class.forName("org.firebirdsql.jdbc.FBDriver");

            java.sql.Driver driver = java.sql.DriverManager.getDriver(driver_conn_string);

            java.util.Properties connectionProperties = new java.util.Properties();
            connectionProperties.put("user", "SYSDBA");
            connectionProperties.put("password", "masterkey");
            //connectionProperties.put("useStreamBlobs", "true");
            //connectionProperties.put("blobBufferSize", "32");
            connectionProperties.put("type", "EMBEDDED");

            jdbc_conn = driver.connect(driver_conn_string, connectionProperties);
            System.out.println("Connection established.");
            jdbc_conn.setAutoCommit(false);
            doDBUpdate();
            return true;
        } catch (SQLException e) {
            customLogger.writeLog(MainApp.ERROR, "localhost", "Importando a la DB de Qendra, "
                    /*        + "IP: 127.0.0.1, Port: 3050"
                    + ", DB: " + path
                    + ", User: SYSDBA"
                     */ + "....\r\n Con error: " + e.toString());
            return false;
        } catch (Exception e) {
            customLogger.writeLog(MainApp.ERROR, "localhost", "Importando a la DB de Qendra, "
                    /*        + "IP: 127.0.0.1, Port: 3050"
                    + ", DB: " + path
                    + ", User: SYSDBA"
                     */ + "....\r\n Con error: " + e.toString());
            return false;
        }
    }

    /**
     * Realizará SIN commitear a DB, la query para las secciones
     *
     * @param ibdata
     * @throws SQLException
     */
    private void doDepartment(IBPlu ibdata) throws SQLException {
        PreparedStatement psDpt = null;
        String questionmarks = "?,?";
        String query_dpt = SQL_DEPARTMENT;
        query_dpt = query_dpt.replaceFirst(VALUES_REGEX, questionmarks);

        psDpt = this.jdbc_conn.prepareStatement(query_dpt);

        psDpt.setInt(1, ibdata.seccion);
        psDpt.setString(2, "Seccion: " + Integer.toString(ibdata.seccion));
//System.out.println("DEPA " +ibdata.seccion);
        psDpt.execute();
    }

    /**
     * Realizará SIN commitear a DB, la query para los datos de origen
     *
     * @param ibdata
     * @throws SQLException
     */
    private void doOrigen(IBPlu ibdata) throws SQLException {
        if (!ibdata.desc_proveedor.trim().isEmpty() && id_ori <= iLimiteRegistrosPermitidos) {
            PreparedStatement psOrigen = null;
            String questionmarks = "?,?,?";
            String query_origen = SQL_ORIGEN;
            query_origen = query_origen.replaceFirst(VALUES_REGEX, questionmarks);

            psOrigen = this.jdbc_conn.prepareStatement(query_origen);

            psOrigen.setInt(1, id_ori);
            psOrigen.setString(2, "Ori: " + ibdata.cod_proveedor);
            psOrigen.setString(3, ibdata.desc_proveedor.trim().length() <= iLimiteMaximoDeColumna ? ibdata.desc_proveedor.replace("'", "").trim() : ibdata.desc_proveedor.replace("'", "").trim().substring(0, iLimiteMaximoDeColumna - 10));
//System.out.println("ORI " + ibdata.cod_proveedor + ibdata.desc_proveedor);
            psOrigen.execute();
            b_ori = true;
        }
    }

    /**
     * Realizará SIN commitear a DB, la query para los datos de conservacion
     *
     * @param ibdata
     * @throws SQLException
     */
    private void doDatosConserv(IBPlu ibdata) throws SQLException {
        if (!ibdata.desc_conservacion.trim().isEmpty() && id_cons <= iLimiteRegistrosPermitidos) {
            PreparedStatement psDatosConserv = null;
            String questionmarks = "?,?,?";
            String query_datos_conserv = SQL_DATOS_CONSERV;
            query_datos_conserv = query_datos_conserv.replaceFirst(VALUES_REGEX, questionmarks);

            psDatosConserv = this.jdbc_conn.prepareStatement(query_datos_conserv);

            psDatosConserv.setInt(1, id_cons);
            psDatosConserv.setString(2, "Cons: " + ibdata.cod_conservacion);
            psDatosConserv.setString(3, ibdata.desc_conservacion.trim().length() <= iLimiteMaximoDeColumna ? ibdata.desc_conservacion.replace("'", "").trim() : ibdata.desc_conservacion.replace("'", "").trim().substring(0, iLimiteMaximoDeColumna - 10));
//System.out.println("CONS " + ibdata.cod_conservacion + ibdata.desc_conservacion);
            psDatosConserv.execute();
            b_cons = true;
        }
    }

    /**
     * Realizará SIN commitear a DB, la query para los ingredientes
     *
     * @param ibdata
     * @throws SQLException
     */
    private void doIngredientes(IBPlu ibdata) throws SQLException {
        //verificamos si el ingrediente ya existe
        id_ing_repetido = 0;
        if (mapIngredientes.containsKey(String.valueOf(ibdata.cod_ingredientes))) {
            id_ing_repetido = mapIngredientes.get(String.valueOf(ibdata.cod_ingredientes));
            b_ing_repetido = true;
            return;
        }

        if (!ibdata.desc_ingredientes.trim().isEmpty() && id_ing <= iLimiteRegistrosPermitidos) {
            PreparedStatement psIngredientes = null;
            String questionmarks = "?,?,?";
            String query_ingredientes = SQL_INGREDIENTES;
            query_ingredientes = query_ingredientes.replaceFirst(VALUES_REGEX, questionmarks);

            psIngredientes = this.jdbc_conn.prepareStatement(query_ingredientes);

            psIngredientes.setInt(1, id_ing);
            psIngredientes.setString(2, String.valueOf(ibdata.cod_ingredientes));
            psIngredientes.setString(3, ibdata.desc_ingredientes.trim().length() <= iLimiteMaximoDeColumna ? ibdata.desc_ingredientes.replace("'", "").trim() : ibdata.desc_ingredientes.replace("'", "").trim().substring(0, iLimiteMaximoDeColumna - 10));

            psIngredientes.execute();

            mapIngredientes.put(String.valueOf(ibdata.cod_ingredientes), id_ing);

            b_ing = true;
        }
    }

    /**
     * Realizará SIN commitear a DB, la query para el producto
     *
     * @param ibdata
     * @param number
     * @throws SQLException
     */
    private void doProduct(IBPlu ibdata, int number) throws SQLException {
        PreparedStatement psPlu = null;
        String questionmarks = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
        String query_prod = SQL_PRODUCT;
        query_prod = query_prod.replaceFirst(VALUES_REGEX, questionmarks);

        psPlu = this.jdbc_conn.prepareStatement(query_prod);

        psPlu.setLong(the_number, ibdata.codigo); // INTEGER,
        psPlu.setInt(the_seccion, ibdata.seccion); // INTEGER,
        psPlu.setString(the_plu_name, (ibdata.nombre.trim().length() <= 18 ? ibdata.nombre.trim() : ibdata.nombre.trim().substring(0, 17))); // VARCHAR(18),
        psPlu.setLong(the_codigo, ibdata.codigo); // INTEGER,
        psPlu.setShort(the_type, (ibdata.tipo == 'P' ? Short.parseShort("0") : Short.parseShort("1"))); // SMALLINT,
        psPlu.setInt(the_tare, (ibdata.valor_tara.trim().isEmpty() ? Integer.parseInt("0") : Integer.parseInt(ibdata.valor_tara.replace(",", "").replace(".", "")))); // INTEGER,
        psPlu.setInt(the_vencimiento, ibdata.vencimiento); // SMALLINT,
        psPlu.setFloat(the_price, (float) ibdata.precio); // FLOAT,
        psPlu.setFloat(the_price2, (float) ibdata.precio); // FLOAT,
        value_others = ibdata.desc_campo_extra1 + " " + ibdata.desc_campo_extra2;
        psPlu.setString(the_others, value_others.trim().isEmpty() ? null : value_others); // VARCHAR(12)
        psPlu.setString(the_modifby, "importador"); // VARCHAR(60),
        psPlu.setFloat(the_porc_agua, 0); // DECIMAL,
        psPlu.setShort(the_tn_activa, (ibdata.cod_inf_nut != 0 ? Short.parseShort("1") : Short.parseShort("0"))); // SMALLINT,
        if (ibdata.cod_inf_nut != 0) {
            psPlu.setString(the_tn_desc, ibdata.porcion); // VARCHAR(30),
            try {
                psPlu.setFloat(the_tn_cal_porc, ibdata.valor_energetico.contains("QNS") ? Float.parseFloat("0") : Float.parseFloat(ibdata.valor_energetico.substring(0, 5).replace(",", ".").replace("kcal", "").replace("kJ", "").replace("=", "").replace("(", "").replace(")", "").trim())); // FLOAT, =  
            } catch (Exception e) {
                psPlu.setFloat(the_tn_cal_porc, Float.parseFloat("0"));
            }
            try {
                psPlu.setFloat(the_tn_carbohidratos, ibdata.carbohidratos.contains("QNS") ? Float.parseFloat("0") : Float.parseFloat(ibdata.carbohidratos.replace(",", ".").replace("(", "").replace(")", "").replace("g", "").trim())); // FLOAT,
            } catch (Exception e) {
                psPlu.setFloat(the_tn_carbohidratos, Float.parseFloat("0"));
            }
            try {
                psPlu.setFloat(the_tn_proteinas, ibdata.proteinas.contains("QNS") ? Float.parseFloat("0") : Float.parseFloat(ibdata.proteinas.replace(",", ".").replace("(", "").replace(")", "").replace("g", "").trim())); // FLOAT,
            } catch (Exception e) {
                psPlu.setFloat(the_tn_proteinas, Float.parseFloat("0"));
            }
            try {
                psPlu.setFloat(the_tn_grasas_tot, ibdata.grasas_total.contains("QNS") ? Float.parseFloat("0") : Float.parseFloat(ibdata.grasas_total.replace(",", ".").replace("(", "").replace(")", "").replace("g", "").trim())); // FLOAT,
            } catch (Exception e) {
                psPlu.setFloat(the_tn_grasas_tot, Float.parseFloat("0"));
            }
            try {
                psPlu.setFloat(the_tn_grasas_sat, ibdata.grasas_saturadas.contains("QNS") ? Float.parseFloat("0") : Float.parseFloat(ibdata.grasas_saturadas.replace(",", ".").replace("(", "").replace(")", "").replace("g", "").trim())); // FLOAT,
            } catch (Exception e) {
                psPlu.setFloat(the_tn_grasas_sat, Float.parseFloat("0"));
            }
            try {
                psPlu.setFloat(the_tn_grasas_trans, ibdata.grasas_trans.contains("QNS") ? Float.parseFloat("0") : Float.parseFloat(ibdata.grasas_trans.replace(",", ".").replace("(", "").replace(")", "").replace("g", "").trim())); // FLOAT,
            } catch (Exception e) {
                psPlu.setFloat(the_tn_grasas_trans, Float.parseFloat("0"));
            }
            try {
                psPlu.setFloat(the_tn_fibra, ibdata.fibra.contains("QNS") ? Float.parseFloat("0") : Float.parseFloat(ibdata.fibra.replace(",", ".").replace("(", "").replace(")", "").replace("g", "").trim())); // FLOAT,
            } catch (Exception e) {
                psPlu.setFloat(the_tn_fibra, Float.parseFloat("0"));
            }
            try {
                psPlu.setFloat(the_tn_sodio, ibdata.sodio.contains("QNS") ? Float.parseFloat("0") : Float.parseFloat(ibdata.sodio.replace(",", ".").replace("(", "").replace(")", "").replace("g", "").trim())); // FLOAT,
            } catch (Exception e) {
                psPlu.setFloat(the_tn_sodio, Float.parseFloat("0"));
            }
        } else {
            psPlu.setString(the_tn_desc, ""); // VARCHAR(30),
            psPlu.setFloat(the_tn_cal_porc, Float.parseFloat("0"));
            psPlu.setFloat(the_tn_carbohidratos, Float.parseFloat("0"));
            psPlu.setFloat(the_tn_proteinas, Float.parseFloat("0"));
            psPlu.setFloat(the_tn_grasas_tot, Float.parseFloat("0"));
            psPlu.setFloat(the_tn_grasas_sat, Float.parseFloat("0"));
            psPlu.setFloat(the_tn_grasas_trans, Float.parseFloat("0"));
            psPlu.setFloat(the_tn_fibra, Float.parseFloat("0"));
            psPlu.setFloat(the_tn_sodio, Float.parseFloat("0"));

        }
        if (b_ori) {
            psPlu.setInt(the_origen, id_ori); // INTEGER,
        } else {
            psPlu.setInt(the_origen, Integer.parseInt("0")); // INTEGER,    
        }
        if (b_cons) {
            psPlu.setInt(the_conserv, id_cons); // INTEGER,
        } else {
            psPlu.setInt(the_conserv, Integer.parseInt("0")); // INTEGER,
        }
        if (b_ing) {
            psPlu.setInt(the_recing, id_ing); // INTEGER,
        } else if (b_ing_repetido) {
            psPlu.setInt(the_recing, id_ing_repetido); // INTEGER,
        } else {
            psPlu.setInt(the_recing, Integer.parseInt("0")); // INTEGER,
        }
        psPlu.setString(the_lote, "0"); // VARCHAR(13),
        //psPlu.setShort(the_ean_tipo, (ibdata.primary_barcode_flag.isEmpty() ? Short.parseShort("0") : Short.parseShort("1"))); // SMALLINT,
        //Ale pidio que siempre sea EAN_GENERAL.
        psPlu.setShort(the_ean_tipo, (Short.parseShort("0"))); // SMALLINT,
        psPlu.setString(the_ean_cfg, (ibdata.primary_barcode_data.trim().isEmpty() ? null : ibdata.primary_barcode_data)); // VARCHAR(12)
//System.out.println("PROD " + number + ibdata.nombre);
        psPlu.execute();

    }

    /**
     * Realizara la insercion o actualizacion de un producto en su completitud.
     * Commitea los cambios en la DB.
     *
     * @param ibdata Los datos del PLU.
     * @param number Numero de balanza para usar en Qendra
     * @throws SQLException
     */
    public void InsertProduct(IBPlu ibdata, long number) throws Exception {
        try {

            //los boolean b_* son para saber si se inserta un nuevo registro y se debe incrementar los índices
            //los integer id_* son para mantener una secuencia en los nros de ID
            if (id_plu == 0) {
                //la primera vez que graba algo, si la DB no tiene datos, me aseguro que tenga unos dummies para las tablas relacionales
                saveDummy();

                this.doDepartment(ibdata);
                b_ori = false;
                this.doOrigen(ibdata);
                b_cons = false;
                this.doDatosConserv(ibdata);
                b_ing = false;
                b_ing_repetido = false;
                this.doIngredientes(ibdata);

                id_plu++;

                this.doProduct(ibdata, id_plu);

                this.jdbc_conn.commit();

                if (b_ori) {
                    id_ori++;
                }
                if (b_cons) {
                    id_cons++;
                }
                if (b_ing) {
                    id_ing++;
                }
            } else {
                //si ya tengo datos, procedo con normalidad
                if (id_plu <= 8000) {
                    this.doDepartment(ibdata);
                    b_ori = false;
                    this.doOrigen(ibdata);
                    b_cons = false;
                    this.doDatosConserv(ibdata);
                    b_ing = false;
                    b_ing_repetido = false;
                    this.doIngredientes(ibdata);
                    this.doProduct(ibdata, id_plu);

                    this.jdbc_conn.commit();

                    if (b_ori) {
                        id_ori++;
                    }
                    if (b_cons) {
                        id_cons++;
                    }
                    if (b_ing) {
                        id_ing++;
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(QendraHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void doDBUpdate() {
        try {
            this.jdbc_conn.prepareStatement(QUERY_CREATE_PRODUCT).execute();
            this.jdbc_conn.prepareStatement(QUERY_CREATE_DEPARTMENT).execute();
            this.jdbc_conn.prepareStatement(QUERY_CREATE_ORIGEN).execute();
            this.jdbc_conn.prepareStatement(QUERY_CREATE_DATOS_CONSERV).execute();
            this.jdbc_conn.prepareStatement(QUERY_CREATE_INGREDIENTES).execute();
            this.jdbc_conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(QendraHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static final String QUERY_CREATE_PRODUCT = "CREATE OR ALTER PROCEDURE merge_product(\n"
            + " THE_NUMBER INTEGER,\n"
            + " THE_SECCION INTEGER,\n"
            + " THE_PLU_NAME VARCHAR(18),\n"
            + " THE_CODIGO INTEGER,\n"
            + " THE_TYPE SMALLINT,\n"
            + " THE_TARE NUMERIC(5,0),\n"
            + " THE_VENCIMIENTO NUMERIC(4,0),\n"
            + " THE_PRICE FLOAT,\n"
            + " THE_PRICE2 FLOAT,\n"
            //  + " THE_OTHERS BLOB SUB_TYPE BINARY,\n"
            + " THE_OTHERS VARCHAR(350),\n"
            + " THE_MODIFBY VARCHAR(60),\n"
            + " THE_PORC_AGUA NUMERIC(4,2),\n"
            + " THE_TN_ACTIVA SMALLINT,\n"
            + " THE_TN_DESC VARCHAR(30),\n"
            + " THE_TN_CAL_PORC FLOAT,\n"
            + " THE_TN_CARBOHIDRATOS FLOAT,\n"
            + " THE_TN_PROTEINAS FLOAT,\n"
            + " THE_TN_GRASAS_TOT FLOAT,\n"
            + " THE_TN_GRASAS_SAT FLOAT,\n"
            + " THE_TN_GRASAS_TRANS FLOAT,\n"
            + " THE_TN_FIBRA FLOAT,\n"
            + " THE_TN_SODIO FLOAT,\n"
            + " THE_ORIGEN INTEGER,\n"
            + " THE_CONSERV INTEGER,\n"
            + " THE_RECING INTEGER,\n"
            + " THE_LOTE VARCHAR(13),\n"
            + " THE_EAN_TIPO SMALLINT,\n"
            + " THE_EAN_CFG VARCHAR(12)\n"
            + ")\n"
            + "AS\n"
            + "BEGIN\n"
            + " BEGIN\n"
            + "  INSERT INTO PLU(ID, ID_SECCION, DESCRIPCION, COD_LOCAL, TIPO_VENTA, TARA, VENCIMIENTO, PRECIO, PRECIO2, OTROS, ULTIMA_MODIF,\n"
            + "		  MODIFBY, IMPFLAG, PORC_AGUA, TN_ACTIVA, TN_DESC, TN_CAL_PORCION, TN_CARBOHIDRATOS, TN_PROTEINAS, TN_GRASAS_TOT,\n"
            + "		  TN_GRASAS_SAT, TN_GRASAS_TRANS, TN_FIBRA, TN_SODIO, ID_ORIGEN, ID_CONSERV, ID_RECING,\n"
            + "		  LOTE, EAN_TIPO, EAN_CFG)\n"
            + "VALUES(\n"
            + " :the_codigo,\n"
            + " :the_seccion,\n"
            + " :the_plu_name,\n"
            + " :the_number,\n"
            + " :the_type,\n"
            + " :the_tare,\n"
            + " :the_vencimiento,\n"
            + " :the_price,\n"
            + " :the_price2,\n"
            + " :the_others,\n"
            + " CURRENT_TIMESTAMP,\n"
            + " :the_modifby,\n"
            + " 1,\n"
            + " :the_porc_agua,\n"
            + " :the_tn_activa,\n"
            + " :the_tn_desc,\n"
            + " :the_tn_cal_porc,\n"
            + " :the_tn_carbohidratos,\n"
            + " :the_tn_proteinas,\n"
            + " :the_tn_grasas_tot,\n"
            + " :the_tn_grasas_sat,\n"
            + " :the_tn_grasas_trans,\n"
            + " :the_tn_fibra,\n"
            + " :the_tn_sodio,\n"
            + " :the_origen,\n"
            + " :the_conserv,\n"
            + " :the_recing,\n"
            + " :the_lote,\n"
            + " :the_ean_tipo,\n"
            + " :the_ean_cfg\n"
            + ");\n"
            + "\n"
            + " WHEN ANY DO\n"
            + "  UPDATE PLU SET \n"
            + "   ID_SECCION = :the_seccion,\n"
            + "   DESCRIPCION = :the_plu_name,\n"
            + "   COD_LOCAL = :the_number, \n"
            + "   TIPO_VENTA = :the_type, \n"
            + "   TARA = :the_tare,\n"
            + "   VENCIMIENTO = :the_vencimiento,\n"
            + "   PRECIO = :the_price,\n"
            + "   PRECIO2 = :the_price2,\n"
            + "   OTROS = :the_others,\n"
            + "   ULTIMA_MODIF = CURRENT_TIMESTAMP,\n"
            + "   MODIFBY = :the_modifby,\n"
            + "   IMPFLAG = 1,\n"
            + "   PORC_AGUA = :the_porc_agua,\n"
            + "   TN_ACTIVA = :the_tn_activa,\n"
            + "   TN_DESC = :the_tn_desc,\n"
            + "   TN_CAL_PORCION = :the_tn_cal_porc,\n"
            + "   TN_CARBOHIDRATOS = :the_tn_carbohidratos,\n"
            + "   TN_PROTEINAS = :the_tn_proteinas,\n"
            + "   TN_GRASAS_TOT = :the_tn_grasas_tot,\n"
            + "   TN_GRASAS_SAT = :the_tn_grasas_sat,\n"
            + "   TN_GRASAS_TRANS = :the_tn_grasas_trans,\n"
            + "   TN_FIBRA = :the_tn_fibra,\n"
            + "   TN_SODIO = :the_tn_sodio,\n"
            + "   ID_ORIGEN = :the_origen,\n"
            + "   ID_CONSERV = :the_conserv,\n"
            + "   ID_RECING = :the_recing,\n"
            + "   LOTE = :the_lote,\n"
            + "   EAN_TIPO = :the_ean_tipo,\n"
            + "   EAN_CFG = :the_ean_cfg\n"
            + "  WHERE ID = :the_codigo;\n"
            + " END\n"
            + "END";

    private static final String QUERY_CREATE_DEPARTMENT = "CREATE OR ALTER PROCEDURE merge_department(\n"
            + " the_id INTEGER,\n"
            + " the_nombre VARCHAR(18)\n"
            + ")\n"
            + "AS\n"
            + "BEGIN\n"
            + " BEGIN\n"
            + "  INSERT INTO SECCIONES(ID, NOMBRE, IMPFLAG)\n"
            + "VALUES(\n"
            + " :the_id,\n"
            + " :the_nombre,\n"
            + " 1\n"
            + ");\n"
            + "\n"
            + " WHEN ANY DO\n"
            + "  UPDATE SECCIONES SET \n"
            + "   NOMBRE = :the_nombre,\n"
            + "   IMPFLAG = 1\n"
            + "  WHERE ID = :the_id;\n"
            + " END\n"
            + "END";

    private static final String QUERY_CREATE_ORIGEN = "CREATE OR ALTER PROCEDURE merge_origen(\n"
            + " the_id INTEGER,\n"
            + " the_nombre VARCHAR(20),\n"
            + " the_info VARCHAR(300)\n"
            + ")\n"
            + "AS\n"
            + "BEGIN\n"
            + " BEGIN\n"
            + "  INSERT INTO ORIGENES(ID, ACTIVO, NOMBRE, INFO)\n"
            + "VALUES(\n"
            + " :the_id,\n"
            + " 1, \n"
            + " :the_nombre,\n"
            + " :the_info\n"
            + ");\n"
            + "\n"
            + " WHEN ANY DO\n"
            + "  UPDATE ORIGENES SET \n"
            + "   NOMBRE = :the_nombre,\n"
            + "   ACTIVO = 1,\n"
            + "   INFO = :the_info\n"
            + "  WHERE ID = :the_id;\n"
            + " END\n"
            + "END";

    private static final String QUERY_CREATE_DATOS_CONSERV = "CREATE OR ALTER PROCEDURE merge_datos_conserv(\n"
            + " the_id INTEGER,\n"
            + " the_nombre VARCHAR(20),\n"
            + " the_info VARCHAR(300)\n"
            + ")\n"
            + "AS\n"
            + "BEGIN\n"
            + " BEGIN\n"
            + "  INSERT INTO DATOS_CONSERV(ID, ACTIVO, NOMBRE, INFO)\n"
            + "VALUES(\n"
            + " :the_id,\n"
            + " 1, \n"
            + " :the_nombre,\n"
            + " :the_info\n"
            + ");\n"
            + "\n"
            + " WHEN ANY DO\n"
            + "  UPDATE DATOS_CONSERV SET \n"
            + "   NOMBRE = :the_nombre,\n"
            + "   ACTIVO = 1,\n"
            + "   INFO = :the_info\n"
            + "  WHERE ID = :the_id;\n"
            + " END\n"
            + "END";

    private static final String QUERY_CREATE_INGREDIENTES = "CREATE OR ALTER PROCEDURE merge_ingredientes(\n"
            + " the_id INTEGER,\n"
            + " the_nombre VARCHAR(20),\n"
            + " the_info VARCHAR(300)\n"
            + ")\n"
            + "AS\n"
            + "BEGIN\n"
            + " BEGIN\n"
            + "  INSERT INTO INGREDIENTES(ID, ACTIVO, NOMBRE, INFO)\n"
            + "VALUES(\n"
            + " :the_id,\n"
            + " 1, \n"
            + " :the_nombre,\n"
            + " :the_info\n"
            + ");\n"
            + "\n"
            + " WHEN ANY DO\n"
            + "  UPDATE INGREDIENTES SET \n"
            + "   NOMBRE = :the_nombre,\n"
            + "   ACTIVO = 1,\n"
            + "   INFO = :the_info\n"
            + "  WHERE ID = :the_id;\n"
            + " END\n"
            + "END";

    private void saveDummy() {
        try {
            //ORIGEN DUMMY
            PreparedStatement psOrigen = null;
            String questionmarks = "?,?,?";
            String query_origen = SQL_ORIGEN;
            query_origen = query_origen.replaceFirst(VALUES_REGEX, questionmarks);
            psOrigen = this.jdbc_conn.prepareStatement(query_origen);

            psOrigen.setInt(1, id_ori++);
            psOrigen.setString(2, "Ori: 0");
            psOrigen.setString(3, "");

            psOrigen.execute();

            //CONSERVACION DUMMY
            PreparedStatement psDatosConserv = null;
            questionmarks = "?,?,?";
            String query_datos_conserv = SQL_DATOS_CONSERV;
            query_datos_conserv = query_datos_conserv.replaceFirst(VALUES_REGEX, questionmarks);
            psDatosConserv = this.jdbc_conn.prepareStatement(query_datos_conserv);

            psDatosConserv.setInt(1, id_cons++);
            psDatosConserv.setString(2, "Cons: 0");
            psDatosConserv.setString(3, "");

            psDatosConserv.execute();

            //INGREDIENTES DUMMY            
            PreparedStatement psIngredientes = null;
            questionmarks = "?,?,?";
            String query_ingredientes = SQL_INGREDIENTES;
            query_ingredientes = query_ingredientes.replaceFirst(VALUES_REGEX, questionmarks);
            psIngredientes = this.jdbc_conn.prepareStatement(query_ingredientes);

            psIngredientes.setInt(1, id_ing++);
            psIngredientes.setString(2, "Ingr: 0");
            psIngredientes.setString(3, "");

            psIngredientes.execute();

            this.jdbc_conn.commit();
        } catch (Exception e) {
            Logger.getLogger(QendraHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
