/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package load.file;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystelDBConnector {

    private static String ip = "localhost";

    private static Connection connection = null;

    public static void closeConection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException ex) {
                Logger.getLogger(SystelDBConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static Connection getCon() {
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

    public static List<SystelProducto> getPLUs(List<String> deptos) {
        List<SystelProducto> productos = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {

            // Realizar operaciones con la base de datos
            // Crear un Statement para ejecutar SQL
            stmt = connection.createStatement();
            String importDBQuery = "SELECT\n"
                    + "    p.product_id,\n"
                    + "    p.\"name\",\n"
                    + "    p.\"attribute\",\n"
                    + "    p.department_id,\n"
                    + "    p.print_used_by_date,\n"
                    + "    p.used_by_date,\n"
                    + "    p.tare,\n"
                    + "    pr1.pricelist AS L1,\n"
                    + "    pr2.pricelist AS L2\n"
                    + "FROM\n"
                    + "    public.product p\n"
                    + "LEFT JOIN productprice pr1 ON pr1.product_id = p.product_id AND pr1.pricelist_version_id = 'lst1'\n"
                    + "LEFT JOIN productprice pr2 ON pr2.product_id = p.product_id AND pr2.pricelist_version_id = 'lst2'\n"
                    + "WHERE\n"
                    + "    p.department_id IN (" + deptos.toString().replace("[", "").replace("]", "") + ")\n"
                    + "ORDER BY\n"
                    + "    p.product_id;";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                productos.add(new SystelProducto(rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("attribute"),
                        rs.getInt("department_id"),
                        rs.getString("print_used_by_date"),
                        rs.getInt("used_by_date"),
                        rs.getString("tare"),
                        rs.getDouble("L1"),
                        rs.getDouble("L2")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return productos;
    }

    public static List<SystelExtra1> getExtra1(List<String> deptos) {
        List<SystelExtra1> extra1 = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {
            stmt = connection.createStatement();
            String importDBQuery = "SELECT\n"
                    + "    p.product_id,\n"
                    + "    p.extra_field1 \n"
                    + "FROM\n"
                    + "    public.product p\n"
                    + "WHERE\n"
                    + "    p.department_id IN (" + deptos.toString().replace("[", "").replace("]", "") + ")\n"
                    + "ORDER BY\n"
                    + "    p.product_id;";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                extra1.add(new SystelExtra1(rs.getInt("product_id"),
                        rs.getString("extra_field1")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return extra1;
    }

    public static List<SystelExtra2> getExtra2(List<String> deptos) {
        List<SystelExtra2> extra2 = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {
            stmt = connection.createStatement();
            String importDBQuery = "SELECT\n"
                    + "    p.product_id,\n"
                    + "    p.extra_field2 \n"
                    + "FROM\n"
                    + "    public.product p\n"
                    + "WHERE\n"
                    + "    p.department_id IN (" + deptos.toString().replace("[", "").replace("]", "") + ")\n"
                    + "ORDER BY\n"
                    + "    p.product_id;";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                extra2.add(new SystelExtra2(rs.getInt("product_id"),
                        rs.getString("extra_field2")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return extra2;
    }

    public static List<SystelTara> getTara(List<String> deptos) {
        List<SystelTara> tara = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {
            stmt = connection.createStatement();
            String importDBQuery = "SELECT\n"
                    + "    p.product_id,\n"
                    + "    p.tare \n"
                    + "FROM\n"
                    + "    public.product p\n"
                    + "WHERE\n"
                    + "    p.department_id IN (" + deptos.toString().replace("[", "").replace("]", "") + ")\n"
                    + "ORDER BY\n"
                    + "    p.product_id;";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                tara.add(new SystelTara(rs.getInt("product_id"),
                        rs.getFloat("tare")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return tara;
    }

    public static List<SystelConserva> getConserva(List<String> deptos) {
        List<SystelConserva> conserva = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {
            stmt = connection.createStatement();
            String importDBQuery = "SELECT\n"
                    + "    p.product_id,\n"
                    + "    p.preservation_info \n"
                    + "FROM\n"
                    + "    public.product p\n"
                    + "WHERE\n"
                    + "    p.department_id IN (" + deptos.toString().replace("[", "").replace("]", "") + ")\n"
                    + "ORDER BY\n"
                    + "    p.product_id;";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                conserva.add(new SystelConserva(rs.getInt("product_id"),
                        rs.getString("preservation_info")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return conserva;
    }

    public static List<SystelIngredient> getIngredients(List<String> deptos) {
        List<SystelIngredient> ingredient = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {
            stmt = connection.createStatement();
            String importDBQuery = "SELECT\n"
                    + "    p.product_id,\n"
                    + "    p.ingredients \n"
                    + "FROM\n"
                    + "    public.product p\n"
                    + "WHERE\n"
                    + "    p.department_id IN (" + deptos.toString().replace("[", "").replace("]", "") + ")\n"
                    + "ORDER BY\n"
                    + "    p.product_id;";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                ingredient.add(new SystelIngredient(rs.getInt("product_id"),
                        rs.getString("ingredients")));

            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return ingredient;
    }

    public static List<SystelNutInfo> getNutInfo(List<String> deptos) {
        List<SystelNutInfo> nutInfo = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {
            stmt = connection.createStatement();
            String importDBQuery = "SELECT\n"
                    + "   p.product_id,\n"
                    + "   MAX(CASE WHEN n.pos_row = 1 AND n.pos_column = 1 THEN n.value END) AS PORCION_X_EMB,\n"
                    + "   MAX(CASE WHEN n.pos_row = 2 AND n.pos_column = 1 THEN n.value END) AS PORCION,\n"
                    + "   MAX(CASE WHEN n.pos_row = 2 AND n.pos_column = 2 THEN n.value END) AS MED_CAS,\n"
                    + "   MAX(CASE WHEN n.pos_row = 4 AND n.pos_column = 1 THEN n.value END) AS VALOR_ENERGETICO,\n"
                    + "   MAX(CASE WHEN n.pos_row = 4 AND n.pos_column = 2 THEN n.value END) AS VALOR_ENERGETICO_POR,\n"
                    + "   MAX(CASE WHEN n.pos_row = 5 AND n.pos_column = 1 THEN n.value END) AS CARBOHIDRATOS,\n"
                    + "   MAX(CASE WHEN n.pos_row = 5 AND n.pos_column = 2 THEN n.value END) AS CARBOHIDRATOS_POR,\n"
                    + "   MAX(CASE WHEN n.pos_row = 6 AND n.pos_column = 1 THEN n.value END) AS AZUCARES_T,\n"
                    + "   MAX(CASE WHEN n.pos_row = 7 AND n.pos_column = 1 THEN n.value END) AS AZUCARES_A,\n"
                    + "   MAX(CASE WHEN n.pos_row = 7 AND n.pos_column = 2 THEN n.value END) AS AZUCARES_A_POR,\n"
                    + "   MAX(CASE WHEN n.pos_row = 8 AND n.pos_column = 1 THEN n.value END) AS PROTEINAS,\n"
                    + "   MAX(CASE WHEN n.pos_row = 8 AND n.pos_column = 2 THEN n.value END) AS PROTEINAS_POR,\n"
                    + "   MAX(CASE WHEN n.pos_row = 9 AND n.pos_column = 1 THEN n.value END) AS GRASAS_TOTALES,\n"
                    + "   MAX(CASE WHEN n.pos_row = 9 AND n.pos_column = 2 THEN n.value END) AS GRASAS_TOTALES_POR,\n"
                    + "   MAX(CASE WHEN n.pos_row = 10 AND n.pos_column = 1 THEN n.value END) AS GRASAS_SAT,\n"
                    + "   MAX(CASE WHEN n.pos_row = 10 AND n.pos_column = 2 THEN n.value END) AS GRASAS_SAT_POR,\n"
                    + "   MAX(CASE WHEN n.pos_row = 11 AND n.pos_column = 1 THEN n.value END) AS GRASAS_TRANS,\n"
                    + "   MAX(CASE WHEN n.pos_row = 11 AND n.pos_column = 2 THEN n.value END) AS GRASAS_TRANS_POR,\n"
                    + "   MAX(CASE WHEN n.pos_row = 12 AND n.pos_column = 1 THEN n.value END) AS FIBRA_ALIM,\n"
                    + "   MAX(CASE WHEN n.pos_row = 12 AND n.pos_column = 2 THEN n.value END) AS FIBRA_ALIM_POR,\n"
                    + "   MAX(CASE WHEN n.pos_row = 13 AND n.pos_column = 1 THEN n.value END) AS SODIO,\n"
                    + "   MAX(CASE WHEN n.pos_row = 13 AND n.pos_column = 2 THEN n.value END) AS SODIO_POR\n"
                    + "FROM public.product p\n"
                    + "INNER JOIN nut_info_el_instance n ON p.product_id = n.product_id AND n.nut_info_set_id = '2'\n"
                    + "WHERE p.product_id != 0\n"
                    + "  AND p.department_id IN (" + deptos.toString().replace("[", "").replace("]", "") + ")\n"
                    + "GROUP BY p.product_id\n";
            //+ "ORDER BY p.product_id;";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                nutInfo.add(new SystelNutInfo(rs.getInt("product_id"),
                        rs.getString("PORCION_X_EMB"),
                        rs.getString("PORCION"),
                        rs.getString("MED_CAS"),
                        rs.getString("VALOR_ENERGETICO"),
                        rs.getString("VALOR_ENERGETICO_POR"),
                        rs.getString("CARBOHIDRATOS"),
                        rs.getString("CARBOHIDRATOS_POR"),
                        rs.getString("AZUCARES_T"),
                        rs.getString("AZUCARES_A"),
                        rs.getString("AZUCARES_A_POR"),
                        rs.getString("PROTEINAS"),
                        rs.getString("PROTEINAS_POR"),
                        rs.getString("GRASAS_TOTALES"),
                        rs.getString("GRASAS_TOTALES_POR"),
                        rs.getString("GRASAS_SAT"),
                        rs.getString("GRASAS_SAT_POR"),
                        rs.getString("GRASAS_TRANS"),
                        rs.getString("GRASAS_TRANS_POR"),
                        rs.getString("FIBRA_ALIM"),
                        rs.getString("FIBRA_ALIM_POR"),
                        rs.getString("SODIO"),
                        rs.getString("SODIO_POR")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return nutInfo;
    }

    public static List<SystelDepto> getDeptos(List<String> deptos) {
        List<SystelDepto> departamentos = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {
            stmt = connection.createStatement();
            String importDBQuery = "select d.department_id , d.\"name\" "
                    + "from department d "
                    + "where d.updatedby ='importacion'  "
                    + "AND d.department_id IN (" + deptos.toString().replace("[", "").replace("]", "") + ") "
                    + "order by d.department_id ";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                departamentos.add(new SystelDepto(rs.getInt("department_id"),
                        rs.getString("name")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return departamentos;
    }

    public static List<SystelDepto> getAllDeptos() {
        List<SystelDepto> deptos = new ArrayList<>();

        ResultSet rs = null;
        Statement stmt = null;

        if (connection == null) {
            connection = getCon();
        }

        try {
            stmt = connection.createStatement();
            String importDBQuery = "select d.department_id , d.\"name\" from department d where d.updatedby ='importacion' order by d.department_id ";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                deptos.add(new SystelDepto(rs.getInt("department_id"),
                        rs.getString("name")));
            }
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos.");
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return deptos;
    }

    // Clase Producto para almacenar los datos
    public static class SystelProducto {

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public double getPrecio1() {
            return precio1;
        }

        public void setPrecio1(double precio) {
            this.precio1 = precio;
        }

        public double getPrecio2() {
            return precio2;
        }

        public void setPrecio2(double precio) {
            this.precio2 = precio;
        }

        public int getDepto() {
            return depto;
        }

        public void setDepto(int depto) {
            this.depto = depto;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getTara() {
            return tara;
        }

        public void setTara(String tara) {
            this.tara = tara;
        }

        public String getVencimiento() {
            return print_vencimiento;
        }

        public void setVencimiento(String vencimiento) {
            this.print_vencimiento = vencimiento;
        }

        public int getDia_vencimiento() {
            return dias_vencimiento;
        }

        public void setDia_vencimiento(int dia_vencimiento) {
            this.dias_vencimiento = dia_vencimiento;
        }

        private int id;
        private String nombre;
        private double precio1;
        private double precio2;
        private int depto;
        private String tipo;
        private String tara;
        private String print_vencimiento;
        private int dias_vencimiento;

        public SystelProducto(int id, String nombre, String tipo, int depto, String vencimiento, int dia_vencimiento, String tara, double l1, double l2) {
            this.id = id;
            this.nombre = nombre;
            this.depto = depto;
            this.print_vencimiento = vencimiento;
            this.dias_vencimiento = dia_vencimiento;
            this.tara = tara;
            this.precio1 = l1;
            this.precio2 = l2;

            switch (tipo) {
                case "0":
                    this.tipo = "P";
                    break;
                case "1":
                    this.tipo = "P";
                    break;
                case "2":
                    this.tipo = "P";
                    break;
                case "3":
                    this.tipo = "P";
                    break;
            }
        }

    }

    public static class SystelExtra1 {

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getExtra1() {
            return Extra1;
        }

        public void setExtra1(String Extra1) {
            this.Extra1 = Extra1;
        }

        private int id;
        private String Extra1;

        public SystelExtra1(int id, String extra1) {
            this.id = id;
            this.Extra1 = extra1;
        }

    }

    public static class SystelExtra2 {

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getExtra2() {
            return Extra2;
        }

        public void setExtra2(String Extra1) {
            this.Extra2 = Extra1;
        }

        private int id;
        private String Extra2;

        public SystelExtra2(int id, String extra2) {
            this.id = id;
            this.Extra2 = extra2;
        }

    }

    public static class SystelDepto {

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private int id;
        private String name;

        public SystelDepto(int id, String name) {
            this.id = id;
            this.name = name;
        }

    }

    public static class SystelTara {

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public float getTara() {
            return tara;
        }

        public void setTara(float Extra1) {
            this.tara = Extra1;
        }

        private int id;
        private float tara;

        public SystelTara(int id, float tara) {
            this.id = id;
            this.tara = tara;
        }

    }

    public static class SystelConserva {

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getConserva() {
            return conserva;
        }

        public void setConserva(String conserva) {
            this.conserva = conserva;
        }

        private int id;
        private String conserva;

        public SystelConserva(int id, String conserva) {
            this.id = id;
            this.conserva = conserva;
        }

    }

    public static class SystelIngredient {

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getIngredient() {
            return ingredient;
        }

        public void setIngredient(String ingredient) {
            this.ingredient = ingredient;
        }

        private int id;
        private String ingredient;

        public SystelIngredient(int id, String ingredient) {
            this.id = id;
            this.ingredient = ingredient;
        }

    }

    public static class SystelNutInfo {

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPorcion() {
            return porcion;
        }

        public void setPorcion(String porcion) {
            this.porcion = porcion;
        }

        public String getPorcion_embalage() {
            return porcion_embalage;
        }

        public void setPorcion_embalage(String porcion_embalage) {
            this.porcion_embalage = porcion_embalage;
        }

        public String getMedida_casera() {
            return medida_casera;
        }

        public void setMedida_casera(String medida_casera) {
            this.medida_casera = medida_casera;
        }

        public String getValor_energetico() {
            return valor_energetico;
        }

        public void setValor_energetico(String valor_energetico) {
            this.valor_energetico = valor_energetico;
        }

        public String getAzucares_totales() {
            return azucares_totales;
        }

        public void setAzucares_totales(String azucares_totales) {
            this.azucares_totales = azucares_totales;
        }

        public String getAzucares_adicionales() {
            return azucares_adicionales;
        }

        public void setAzucares_adicionales(String azucares_adicionales) {
            this.azucares_adicionales = azucares_adicionales;
        }

        public String getAzucares_adicionales_pct() {
            return azucares_adicionales_pct;
        }

        public void setAzucares_adicionales_pct(String azucares_adicionales_pct) {
            this.azucares_adicionales_pct = azucares_adicionales_pct;
        }

        public String getCarbohidratos() {
            return carbohidratos;
        }

        public void setCarbohidratos(String carbohidratos) {
            this.carbohidratos = carbohidratos;
        }

        public String getProteinas() {
            return proteinas;
        }

        public void setProteinas(String proteinas) {
            this.proteinas = proteinas;
        }

        public String getGrasas_total() {
            return grasas_total;
        }

        public void setGrasas_total(String grasas_total) {
            this.grasas_total = grasas_total;
        }

        public String getGrasas_saturadas() {
            return grasas_saturadas;
        }

        public void setGrasas_saturadas(String grasas_saturadas) {
            this.grasas_saturadas = grasas_saturadas;
        }

        public String getGrasas_trans() {
            return grasas_trans;
        }

        public void setGrasas_trans(String grasas_trans) {
            this.grasas_trans = grasas_trans;
        }

        public String getFibra() {
            return fibra;
        }

        public void setFibra(String fibra) {
            this.fibra = fibra;
        }

        public String getSodio() {
            return sodio;
        }

        public void setSodio(String sodio) {
            this.sodio = sodio;
        }

        public String getValor_energetico_pct() {
            return valor_energetico_pct;
        }

        public void setValor_energetico_pct(String valor_energetico_pct) {
            this.valor_energetico_pct = valor_energetico_pct;
        }

        public String getCarbohidratos_pct() {
            return carbohidratos_pct;
        }

        public void setCarbohidratos_pct(String carbohidratos_pct) {
            this.carbohidratos_pct = carbohidratos_pct;
        }

        public String getProteinas_pct() {
            return proteinas_pct;
        }

        public void setProteinas_pct(String proteinas_pct) {
            this.proteinas_pct = proteinas_pct;
        }

        public String getGrasas_total_pct() {
            return grasas_total_pct;
        }

        public void setGrasas_total_pct(String grasas_total_pct) {
            this.grasas_total_pct = grasas_total_pct;
        }

        public String getGrasas_saturadas_pct() {
            return grasas_saturadas_pct;
        }

        public void setGrasas_saturadas_pct(String grasas_saturadas_pct) {
            this.grasas_saturadas_pct = grasas_saturadas_pct;
        }

        public String getGrasas_trans_pct() {
            return grasas_trans_pct;
        }

        public void setGrasas_trans_pct(String grasas_trans_pct) {
            this.grasas_trans_pct = grasas_trans_pct;
        }

        public String getFibra_pct() {
            return fibra_pct;
        }

        public void setFibra_pct(String fibra_pct) {
            this.fibra_pct = fibra_pct;
        }

        public String getSodio_pct() {
            return sodio_pct;
        }

        public void setSodio_pct(String sodio_pct) {
            this.sodio_pct = sodio_pct;
        }

        private int id;
        private String porcion = "";
        private String porcion_embalage = ""; // Se agrega ademas la medida casera.
        private String medida_casera = "";
        private String valor_energetico = "";
        private String azucares_totales = "";
        private String azucares_adicionales = "";
        private String azucares_adicionales_pct = "";
        private String carbohidratos = "";
        private String proteinas = "";
        private String grasas_total = "";
        private String grasas_saturadas = "";
        private String grasas_trans = "";
        private String fibra = "";
        private String sodio = "";
        private String valor_energetico_pct = "";
        private String carbohidratos_pct = "";
        private String proteinas_pct = "";
        private String grasas_total_pct = "";
        private String grasas_saturadas_pct = "";
        private String grasas_trans_pct = "";
        private String fibra_pct = "";
        private String sodio_pct = "";

        public SystelNutInfo(int id, String PORCION_X_EMB, String PORCION, String MED_CAS,
                String VALOR_ENERGETICO, String VALOR_ENERGETICO_POR, String CARBOHIDRATOS, String CARBOHIDRATOS_POR,
                String AZUCARES_T, String AZUCARES_A, String AZUCARES_A_POR, String PROTEINAS,
                String PROTEINAS_POR, String GRASAS_TOTALES, String GRASAS_TOTALES_POR, String GRASAS_SAT,
                String GRASAS_SAT_POR, String GRASAS_TRANS, String GRASAS_TRANS_POR, String FIBRA_ALIM,
                String FIBRA_ALIM_POR, String SODIO, String SODIO_POR) {
            this.id = id;
            this.porcion_embalage = PORCION_X_EMB;
            this.porcion = PORCION;
            this.medida_casera = MED_CAS;
            this.valor_energetico = VALOR_ENERGETICO;
            this.valor_energetico_pct = VALOR_ENERGETICO_POR;
            this.carbohidratos = CARBOHIDRATOS;
            this.carbohidratos_pct = CARBOHIDRATOS_POR;
            this.azucares_totales = AZUCARES_T;
            this.azucares_adicionales = AZUCARES_A;
            this.azucares_adicionales_pct = AZUCARES_A_POR;
            this.proteinas = PROTEINAS;
            this.proteinas_pct = PROTEINAS_POR;
            this.grasas_total = GRASAS_TOTALES;
            this.grasas_total_pct = GRASAS_TOTALES_POR;
            this.grasas_saturadas = GRASAS_SAT;
            this.grasas_saturadas_pct = GRASAS_SAT_POR;
            this.grasas_trans = GRASAS_TRANS;
            this.grasas_trans_pct = GRASAS_TRANS_POR;
            this.fibra = FIBRA_ALIM;
            this.fibra_pct = FIBRA_ALIM_POR;
            this.sodio = SODIO;
            this.sodio_pct = SODIO_POR;
        }

    }

}
