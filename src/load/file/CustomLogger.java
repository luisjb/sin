/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomLogger {

    private final List<LogEntry> logBuffer = new ArrayList<>();
    private final List<LogEntry> logBufferError = new ArrayList<>();
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor();
    private static CustomLogger instance;
    private static String filePath;


    // Constructor privado para el patrón Singleton
    private CustomLogger() {
    }

    // Método estático para obtener la instancia Singleton
    public static synchronized CustomLogger getInstance() {
        if (instance == null) {
            instance = new CustomLogger();
        }
        return instance;
    }

    // Método para configurar el archivo de log
    public static synchronized void setLogFilePath(String logFilePath) {
        filePath = logFilePath;
    }

    // Método para agregar una entrada de log
    public void writeLog(String level, String ip, String message) {
        logExecutor.submit(() -> {
            synchronized (logBuffer) {
                logBuffer.add(new LogEntry(level, ip, message));
                if (level.trim().equalsIgnoreCase("ERROR:") || level.trim().equalsIgnoreCase("WARNING:")) {
                    logBufferError.add(new LogEntry(level, ip, message));
                }
            }
        });
    }

    // Método para agregar una entrada de log
    public String readLog(boolean sorted) {
        // Método para obtener los logs ordenados como una lista de líneas
        List<LogEntry> sortedLogs;
        synchronized (logBuffer) {
            // Crear una copia ordenada del logBuffer
            sortedLogs = new ArrayList<>(logBuffer);
            if (sorted) {
                sortedLogs.sort(Comparator.comparing(LogEntry::getTimestamp));
            }
        }
        StringBuilder output = new StringBuilder();
        // Convertir cada entrada en texto y devolver como lista de strings
        for (LogEntry log : sortedLogs) {
            output.append(log.toString());
            output.append("\n");
        }
        return output.toString();
    }

    // Método para agregar una entrada de log
    public String readLogError(boolean sorted) {
        // Método para obtener los logs ordenados como una lista de líneas
        List<LogEntry> sortedLogs;
        synchronized (logBufferError) {
            // Crear una copia ordenada del logBuffer
            sortedLogs = new ArrayList<>(logBufferError);
            if (sorted) {
                sortedLogs.sort(Comparator.comparing(LogEntry::getTimestamp));
            }
        }
        StringBuilder output = new StringBuilder();
        // Convertir cada entrada en texto y devolver como lista de strings
        for (LogEntry log : sortedLogs) {
            output.append(log.toString());
            output.append("\n");
        }
        return output.toString();
    }
    // Método para vaciar el buffer y escribir los logs
    public void flushLogs() {
        if (filePath == null || filePath.isEmpty()) {
            System.err.println("No se ha establecido el filePath del log.");
            return;
        }

        logExecutor.submit(() -> {
            List<LogEntry> logsToWrite;
            synchronized (logBuffer) {
                logsToWrite = new ArrayList<>(logBuffer);
                logsToWrite.sort(Comparator.comparing(LogEntry::getTimestamp));
                logBuffer.clear();
                logBufferError.clear();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                for (LogEntry log : logsToWrite) {
                    writer.write(log.toString());
                    writer.newLine();
                }
                System.out.println("Logs escritos correctamente en: " + filePath);
            } catch (IOException e) {
                System.err.println("Error al escribir en el archivo de log: " + filePath);
                e.printStackTrace();
            }
        });
    }

    // Método para cerrar el executor al final de la aplicación
    public void shutdown() {
//        flushLogs(); // Asegura que todos los logs se hayan escrito
        logExecutor.shutdown();
    }
}
