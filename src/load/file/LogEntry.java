/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package load.file;

import java.time.LocalDateTime;

public class LogEntry {
    private final LocalDateTime timestamp;
    private final String level;
    private final String ip;
    private final String message;

    public LogEntry(String level, String ip, String message) {
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.ip = ip;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getIp() {
        return ip;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return timestamp.toString() + " [" + level + "]: " + ip + " - " + message;
    }
}