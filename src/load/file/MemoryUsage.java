/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import static load.file.MainApp.isWindows;

/**
 *
 * @author diego
 */
public class MemoryUsage {

    public static void getRAM() {
        // Obtener la cantidad de memoria RAM que está utilizando el proceso
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Obtener el uso de memoria física
        long totalPhysicalMemorySize = osBean.getTotalPhysicalMemorySize();
        long freePhysicalMemorySize = osBean.getFreePhysicalMemorySize();
        long usedPhysicalMemorySize = totalPhysicalMemorySize - freePhysicalMemorySize;

        System.out.println("");
        System.out.println("///// RAM STATUS /////");
        System.out.println("Total Physical Memory: " + totalPhysicalMemorySize / (1024 * 1024) + " MB");
        System.out.println("Free Physical Memory: " + freePhysicalMemorySize / (1024 * 1024) + " MB");
        System.out.println("Used Physical Memory: " + usedPhysicalMemorySize / (1024 * 1024) + " MB");
        System.out.println("");

        String processName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = processName.split("@")[0];
        System.out.println("El PID del proceso actual es: " + pid);
        System.out.println("");
        System.out.println("PID Process:" + pid);
        System.out.println("");

        if (isWindows()) {
            fromWindows(pid);
        } else {
            fromLinux(pid);
        }

//        System.out.println("");
//        System.out.println("///// RAM STATUS /////");
//        
//        System.out.println("Total Physical Memory: " + totalPhysicalMemorySize / (1024 * 1024) + " MB");
//        System.out.println("Free Physical Memory: " + freePhysicalMemorySize / (1024 * 1024) + " MB");
//        System.out.println("Used Physical Memory: " + usedPhysicalMemorySize / (1024 * 1024) + " MB");
//
//        System.out.println("");
    }

    public static void fromLinux(String pid) {

        try {
            String[] cmd = {"/bin/sh", "-c", "ps -o rss= -p " + pid};
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                long memoryUsageKB = Long.parseLong(line.trim());
                System.out.println("Memory Usage: " + (memoryUsageKB / 1024) + " MB");
//                System.out.println("Memory Usage: " + memoryUsageKB + " KB");
//                System.out.println("Memory Usage: " + (memoryUsageKB / 1024) + " MB");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fromWindows(String pid) {

        try {
            String[] cmd = {"cmd.exe", "/c", "tasklist /FI \"PID eq " + pid + "\" /FO LIST"};
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Mem Usage:")) {
                    String memoryUsage = line.split(":")[1].trim();

                    System.out.println("Memory Usage: " + (memoryUsage) + " MB");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
