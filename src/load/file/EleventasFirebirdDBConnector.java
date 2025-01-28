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

public class EleventasFirebirdDBConnector {

    private static String DATABASE_URL = "jdbc:firebirdsql:127.0.0.1/3050:/home/diego/Descargas/BasededatosEleventa/pdvdata2.fdb";
    private static String DATABASE_USER = "SYSDBA";
    private static String DATABASE_PASSWORD = "masterkey"; // Cambia esto por tu contraseña configurada

    public static List<EleventasProducto> getPLUs(String importDBIP, String importDBName, String importDBPass, String importDBPort, String importDBQuery, String importDBType, String importDBUser) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<EleventasProducto> productos = new ArrayList<>();
        //DATABASE_URL = "jdbc:firebirdsql:127.0.0.1/3050:" + importDBName;
        DATABASE_URL = "jdbc:firebirdsql:" + importDBIP + "/" + importDBPort + ":" + importDBName;
        //192.168.2.199:53100;databaseName=MyBusiness20;encrypt=true;trustServerCertificate=true";
        DATABASE_USER = importDBUser;
        DATABASE_PASSWORD = importDBPass;
        try {
            // Cargar el driver JDBC de Firebird

            Class.forName("org.firebirdsql.jdbc.FBDriver");

            java.util.Properties connectionProperties = new java.util.Properties();
            connectionProperties.put("user", DATABASE_USER);
            connectionProperties.put("password", DATABASE_PASSWORD);
            //connectionProperties.put("useStreamBlobs", "true");
            //connectionProperties.put("blobBufferSize", "32");
            // connectionProperties.put("type", "EMBEDDED");

            // Establecer conexión con la base de datos
            connection = DriverManager.getConnection(DATABASE_URL, connectionProperties);
//            System.out.println("Conexión a la base de datos establecida con éxito.");

            // Realizar operaciones con la base de datos
            // Por ejemplo, crear un Statement, ejecutar un SQL, etc.
            // Crear un Statement para ejecutar SQL
            stmt = connection.createStatement();

            // Ejecutar una consulta SQL
//            String sql = "SELECT P.ID, P.CODIGO, P.DESCRIPCION, P.PVENTA, P.DEPT, D.NOMBRE, CASE WHEN P.TVENTA = 'U' THEN 'U' ELSE 'P' END AS TIPO "
//                    + " FROM PRODUCTOS P"
//                    + " INNER JOIN DEPARTAMENTOS D ON D.ID = P.DEPT"
//                    + " WHERE P.ELIMINADO_EN is NULL";

            rs = stmt.executeQuery(importDBQuery);

//            // Procesar el ResultSet
            while (rs.next()) {
                // Suponiendo que la tabla tiene columnas como id, nombre, precio
//                int iId = rs.getInt("ID");
//                String sCodigo = rs.getString("CODIGO");
//                String sDdescripcion = rs.getString("DESCRIPCION");
//                double dPrecio = rs.getDouble("PVENTA");
//                int iDepto = rs.getInt("DEPT");
//                String sDeptoNombre = rs.getString("NOMBRE");
//                String sTipo = rs.getString("TIPO"); // 'U' - 'P'
//                
                productos.add(new EleventasProducto(rs.getInt("ID"),
                        rs.getString("CODIGO"),
                        rs.getString("DESCRIPCION"),
                        rs.getDouble("PVENTA"),
                        rs.getInt("DEPT"),
                        rs.getString("NOMBRE"),
                        rs.getString("TIPO")));

            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Error: El driver JDBC de Firebird no fue encontrado.");
            ex.printStackTrace();
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos Firebird.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error al conectar con la base de datos Firebird.");
            ex.printStackTrace();
        } finally {

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
        }
        return productos;
    }

    // Clase Producto para almacenar los datos
    public static class EleventasProducto {

        public int getId() {
            return id;
        }

        public void setId(int id) {
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
        private int id;
        private String codigo;
        private String descripcion;
        private double precio;
        private int depto;
        private String deptoNombre;
        private String tipo;

        public EleventasProducto(int id, String codigo, String descripcion, double precio, int depto, String deptoNombre, String tipo) {
            this.id = id;
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.precio = precio;
            this.depto = depto;
            this.deptoNombre = deptoNombre;
            this.tipo = tipo;
        }

        // Getters y setters (si es necesario)
        @Override
        public String toString() {
            return String.format("ID: %d, Codigo: %s, Descripcion: %s, Precio: %.2f, Depto: %d, DeptoNombre: %s, Tipo: %s",
                    id, codigo, descripcion, precio, depto, deptoNombre, tipo);
        }
    }
}
