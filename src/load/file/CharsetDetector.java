/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package load.file;

import java.io.FileInputStream;
import java.io.IOException;
import org.mozilla.universalchardet.UniversalDetector;


public class CharsetDetector {

//    public static void main(String[] args) {
//        String filePath = "/path/to/your/file.txt";
//        
//        try {
//            String encoding = detectCharset(filePath);
//            if (encoding != null) {
//                System.out.println("Detected encoding: " + encoding);
//                readFileWithEncoding(filePath, encoding);
//            } else {
//                System.out.println("No encoding detected. Using default encoding.");
//                readFileWithEncoding(filePath, StandardCharsets.UTF_8.name());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    public static String detectCharset(String filePath) throws IOException {
        String encoding = null;
        try {

            byte[] buf = new byte[4096];
            FileInputStream fis = new FileInputStream(filePath);

            UniversalDetector detector = new UniversalDetector(null);

            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();

            encoding = detector.getDetectedCharset();
            detector.reset();
            fis.close();
        } catch (Exception e) {
        }
        return encoding;
    }

//    public static String detectCharsetUniversal(String filePath) {
//        String encoding = "UTF-8"; // Valor predeterminado si no se detecta encoding
//        try (FileInputStream fis = new FileInputStream(filePath)) {
//            byte[] buf = new byte[4096];
//            UniversalDetector detector = new UniversalDetector(null);
//
//            int nread;
//            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
//                detector.handleData(buf, 0, nread);
//            }
//            detector.dataEnd();
//
//            // Obtener el encoding detectado
//            String detectedCharset = detector.getDetectedCharset();
//            if (detectedCharset != null) {
//                encoding = detectedCharset;
//            }
//
//            detector.reset();
//        } catch (IOException e) {
//            e.printStackTrace(); // Agrega un manejo de errores significativo aquí
//        }
//
//        return encoding;
//    }
//    
//        // Map de MIME types a posibles charsets
//    private static final Map<String, String> MIME_TO_CHARSET = new HashMap<>();
//
//    static {
//        MIME_TO_CHARSET.put("text/plain", "UTF-8");
//        MIME_TO_CHARSET.put("application/octet-stream", "UTF-8");
//        // Agrega más MIME types y charsets según necesites
//    }
//    
//    public static String detectCharsetTika(String filePath) {
//Tika tika = new Tika();
//        Metadata metadata = new Metadata();
//        String charset = "UTF-8"; // Charset predeterminado si no se detecta uno
//
//        try (InputStream inputStream = new FileInputStream(filePath)) {
//            // Detectar el tipo MIME con Tika
//            String mimeType = tika.detect(inputStream, metadata);
//
//            // Determinar el charset a partir del MIME type
//            if (MIME_TO_CHARSET.containsKey(mimeType)) {
//                charset = MIME_TO_CHARSET.get(mimeType);
//            } else {
//                // Si Tika devuelve un charset, úsalo
//                charset = mimeType;
//            }
//
//            // Verificar si el charset es válido, si no usar UTF-8 como fallback
//            if (!Charset.isSupported(charset)) {
//                charset = "UTF-8";
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return charset;
//    }    
//    public static void readFileWithEncoding(String filePath, String encoding) throws IOException {
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//        }
//    }
}
