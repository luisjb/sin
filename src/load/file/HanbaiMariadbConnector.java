package load.file;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HanbaiMariadbConnector {

    // Configuración de la conexión a la base de datos
    private static String DATABASE_URL = "jdbc:mariadb://";
    private static String DATABASE_USER = "root"; // Usuario por defecto de MariaDB
    private static String DATABASE_PASSWORD = "root";

    public static List<HanbaiProducto> getPLUs(String importDBIP, String importDBName, String importDBPass, String importDBPort, String importDBQuery, String importDBType, String importDBUser) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<HanbaiProducto> productos = new ArrayList<>();

        // Configurar la URL de conexión
        DATABASE_URL = "jdbc:mariadb://" + importDBIP + ":" + importDBPort + "/" + importDBName;
        DATABASE_USER = importDBUser;
        DATABASE_PASSWORD = importDBPass;

        try {
            // Cargar el driver JDBC de MariaDB
            Class.forName("org.mariadb.jdbc.Driver");

            // Establecer la conexión con la base de datos
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            System.out.println("Conexión a la base de datos establecida con éxito.");

            // Crear un Statement para ejecutar SQL
            stmt = connection.createStatement();

            // Ejecutar la consulta SQL
            rs = stmt.executeQuery(importDBQuery);

            // Procesar el ResultSet
            while (rs.next()) {
                productos.add(new HanbaiProducto(
                        rs.getString("SKU"),
                        rs.getString("idarticulo"),
                        rs.getString("Concepto"),
                        rs.getDouble("Precio1_Neto"),
                        rs.getDouble("Precio2_Neto"),
                        rs.getString("Codigo"),
                        rs.getString("Categoria"),
                        rs.getString("Unidad"),
                        rs.getString("EAN"),
                        rs.getInt("caducidad")
                ));
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Error: El driver JDBC de MariaDB no fue encontrado.");
            ex.printStackTrace();
        } catch (SQLException ex) {
            System.out.println("Error al conectar con la base de datos MariaDB.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("Error inesperado al conectar con la base de datos MariaDB.");
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
    public static class HanbaiProducto {

        private String id;
        private String codigo;
        private String descripcion;
        private double precio;
        private double precio2;
        private String depto;
        private int caducidad;


        private String deptoNombre;
        private String tipo;
        private String codebar;

        public HanbaiProducto(String id, String codigo, String descripcion, double precio, double precio2, String depto, String deptoNombre, String tipo, String codebar, int caducidad) {
            this.id = id;
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.precio = precio;
            this.precio2 = precio2;
            this.depto = depto;
            this.deptoNombre = deptoNombre;
            this.tipo = tipo;
            this.codebar = codebar;
            this.caducidad = caducidad;
        }

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

        public String getDepto() {
            return depto;
        }

        public void setDepto(String depto) {
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
        
        public int getCaducidad() {
            return caducidad;
        }

        public void setCaducidad(int caducidad) {
            this.caducidad = caducidad;
        }
        
        @Override
        public String toString() {
            return String.format("ID: %s, Codigo: %s, Descripcion: %s, Precio: %.2f, Precio2: %.2f, Depto: %s, DeptoNombre: %s, Tipo: %s, cb: %s",
                    id, codigo, descripcion, precio, precio2, depto, deptoNombre, tipo, codebar);
        }
    }
}
