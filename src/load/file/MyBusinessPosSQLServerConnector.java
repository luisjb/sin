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

public class MyBusinessPosSQLServerConnector {

    // Configuración de la conexión a la base de datos
    //private static String DATABASE_URL = "jdbc:sqlserver://192.168.2.199:53100;databaseName=MyBusiness20;encrypt=true;trustServerCertificate=true";
    private static String DATABASE_URL = "jdbc:sqlserver://";
    private static String DATABASE_USER = "sa";
    private static String DATABASE_PASSWORD = "12345678";

    public static List<MyBusinessPosProducto> getPLUs(String importDBIP, String importDBName, String importDBPass, String importDBPort, String importDBQuery, String importDBType, String importDBUser) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<MyBusinessPosProducto> productos = new ArrayList<>();

        DATABASE_URL = "jdbc:sqlserver://" + importDBIP + ":" + importDBPort + ";databaseName=" + importDBName + ";encrypt=true;trustServerCertificate=true";
        //192.168.2.199:53100;databaseName=MyBusiness20;encrypt=true;trustServerCertificate=true";
        DATABASE_USER = importDBUser;
        DATABASE_PASSWORD = importDBPass;

        try {
            // Cargar el driver JDBC de SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Establecer la conexión con la base de datos
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            System.out.println("Conexión a la base de datos establecida con éxito.");

            // Crear un Statement para ejecutar SQL
            stmt = connection.createStatement();

            // Ejecutar una consulta SQL
//            String sql = "SELECT articulo, descrip, precio1, precio2, 1 as ID_DEPT, 'SIN DEPARTAMENTO' as NOMBRE_DEPT, "
//                    + " CASE WHEN granel = 1 THEN 'P' ELSE 'U' END AS TIPO"
//                    + " FROM dbo.prods";
            rs = stmt.executeQuery(importDBQuery);

//                String iId = "";
//                String sCodigo = "";
//                String sDdescripcion = "";
//                double dPrecio = 0.0D;
//                double dPrecio2 = 0.0D;
//                int iDepto = 1;
//                String sDeptoNombre = "";
//                String sTipo = ""; // 'U' - 'P'
            // Procesar el ResultSet
            while (rs.next()) {
                // Suponiendo que la tabla tiene columnas como id, nombre, precio
//                String iId = rs.getInt("ID");
//                String sCodigo = rs.getString("CODIGO");
//                String sDdescripcion = rs.getString("DESCRIPCION");
//                double dPrecio = rs.getDouble("PVENTA");
//                double dPrecio2 = rs.getDouble("PVENTA");
//                int iDepto = rs.getInt("DEPT");
//                String sDeptoNombre = rs.getString("NOMBRE");
//                String sTipo = rs.getString("TIPO"); // 'U' - 'P'
//                
                productos.add(new MyBusinessPosProducto(rs.getString("articulo"),
                        rs.getString("articulo"),
                        rs.getString("descrip"),
                        rs.getDouble("precio1"),
                        rs.getDouble("precio2"),
                        rs.getInt("ID_DEPT"),
                        rs.getString("NOMBRE_DEPT"),
                        rs.getString("TIPO"),
                        rs.getString("BARCODE")));

            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Error: El driver JDBC de SQL Server no fue encontrado.");
            ex.printStackTrace();
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos SQL Server.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos SQL Server.");
            ex.printStackTrace();
        } finally {
            // Cerrar ResultSet, Statement y Connection
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return productos;
        }
    }

    // Clase Producto para almacenar los datos
    public static class MyBusinessPosProducto {

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public double getPrecio() {
            return precio;
        }

        public void setPrecio(double precio) {
            this.precio = precio;
        }

        public double getPrecio2() {
            return precio2;
        }

        public void setPrecio2(double precio2) {
            this.precio2 = precio2;
        }

        public int getDepto() {
            return depto;
        }

        public void setDepto(int depto) {
            this.depto = depto;
        }

        public String getDeptoNombre() {
            return deptoNombre;
        }

        public void setDeptoNombre(String deptoNombre) {
            this.deptoNombre = deptoNombre;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getCodebar() {
            return codebar;
        }

        public void setCodebar(String codebar) {
            this.codebar = codebar;
        }
        private String id;
        private String codigo;
        private String descripcion;
        private double precio;
        private double precio2;
        private int depto;
        private String deptoNombre;
        private String tipo;
        private String codebar;

        public MyBusinessPosProducto(String id, String codigo, String descripcion, double precio, double precio2, int depto, String deptoNombre, String tipo, String codebar) {
            this.id = id;
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.precio = precio;
            this.precio2 = precio2;
            this.depto = depto;
            this.deptoNombre = deptoNombre;
            this.tipo = tipo;
            this.codebar = codebar;
        }

        // Getters y setters (si es necesario)
        @Override
        public String toString() {
            return String.format("ID: %s, Codigo: %s, Descripcion: %s, Precio: %.2f, Precio2: %.2f, Depto: %d, DeptoNombre: %s, Tipo: %s, cb: %s",
                    id, codigo, descripcion, precio, precio2, depto, deptoNombre, tipo, codebar);
        }
    }
}
