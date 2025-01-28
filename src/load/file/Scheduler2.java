/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package load.file;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Scheduler2 {

    public static boolean isOnExecution = false;
    private long lastRunTime = -1; // Keeps track of last execution time in minutes

    public enum SCHEDULER_TYPE {
        MINUTES, HOURS
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private SCHEDULER_TYPE schedulerType;
    private List<String> hourList;
    private int intervalMinutes;
    private Runnable task;
    private final ReentrantLock configLock = new ReentrantLock();

    public Scheduler2(SCHEDULER_TYPE schedulerType, Runnable task) {
        this.schedulerType = schedulerType;
        this.task = task;
    }

    public void startExecution() {
        scheduler.scheduleWithFixedDelay(this::executeTask, 0, 1, TimeUnit.MINUTES);
    }

    private void executeTask() {
        configLock.lock();
        try {
            LocalDateTime now = LocalDateTime.now();
            if (schedulerType == SCHEDULER_TYPE.MINUTES) {
                executeAtFixedInterval(now);
            } else {
                executeAtFixedHours(now);
            }
        } finally {
            configLock.unlock();
        }
    }

    private void executeAtFixedInterval(LocalDateTime now) {
        long currentTimeMinutes = now.toEpochSecond(ZoneOffset.UTC) / 60;
        long intervalInMinutes = intervalMinutes * 60;

        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("@@@ Scheduler2 executeAtFixedInterval @@@ if (currentTimeMinutes - lastRunTime >= intervalMinutes): ct:" + (currentTimeMinutes - lastRunTime) + " intervalMinutes: " + intervalMinutes);
        System.out.println("@@@ Scheduler2 executeAtFixedInterval @@@ isOnExecution:" + isOnExecution);
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");

        if (currentTimeMinutes - lastRunTime >= intervalMinutes) {
            if (!isOnExecution) {
                lastRunTime = currentTimeMinutes;
                isOnExecution = true;
                task.run();
            } else {
                System.out.println("@@@ Scheduler2 executeAtFixedInterval @@@ YA ESTÁ EN EJECUCIÓN");
            }
        } else {
            System.out.println("@@@ Scheduler2 executeAtFixedInterval @@@ NO ES MOMENTO DE EJCUTARSE");
        }
        ConfigProperties prop = null;
        try {
            prop = new ConfigProperties();

            prop.getPropValues();
            setIntervalMinutes(Integer.parseInt(ConfigProperties.ConfigFile.getSchedulerTimeValue())); // Ejecutar cada x minutos
        } catch (Exception ex) {
            Logger.getLogger(Scheduler2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            prop = null;
        }
    }

    private void executeAtFixedHours(LocalDateTime now) {
        String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("@@@ Scheduler2 executeAtFixedHours @@@ currentTime / hourList: ct:" + currentTime + " hl:" + hourList.toString());
        System.out.println("@@@ Scheduler2 executeAtFixedHours @@@ isOnExecution:" + isOnExecution);
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");

        if (hourList.contains(currentTime)) {
            if (!isOnExecution) {
                isOnExecution = true;
                task.run();
            } else {
                System.out.println("@@@ Scheduler2 executeAtFixedHours @@@ YA ESTÁ EN EJECUCIÓN");
            }
        } else {
            System.out.println("@@@ Scheduler2 executeAtFixedHours @@@ NO ES MOMENTO DE EJCUTARSE");
        }
        ConfigProperties prop = null;
        try {
            prop = new ConfigProperties();

            prop.getPropValues();
            setHourList(Arrays.asList(ConfigProperties.ConfigFile.getSchedulerHours()).stream().sorted().collect(Collectors.toList())); // Ejecutar cada 35 minutos
            System.out.println("@@@ Scheduler2 executeAtFixedHours @@@ currentTime / hourList: ct:" + currentTime + " hl:" + hourList.toString());
            System.out.println("@@@ Scheduler2 executeAtFixedHours @@@ HL FROM FILE: " + Arrays.asList(ConfigProperties.ConfigFile.getSchedulerHours()).stream().sorted().collect(Collectors.toList()).toString());
        } catch (Exception ex) {
            Logger.getLogger(Scheduler2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            prop = null;
        }
    }

    public void setIntervalMinutes(int minutes) {
        configLock.lock();
        try {
            System.out.println("@@@ Scheduler2 setIntervalMinutes @@@ minutes:" + minutes);

            this.intervalMinutes = minutes;
//            this.lastRunTime = -1; // Reset to ensure next execution occurs at the new interval

        } finally {
            configLock.unlock();
        }
    }

    public void setHourList(List<String> hourList) {
        configLock.lock();
        try {
            System.out.println("@@@ Scheduler2 setHourList @@@ minutes:" + hourList.toString());
            this.hourList = hourList;
        } finally {
            configLock.unlock();
        }
    }

    public void setSchedulerType(SCHEDULER_TYPE type) {
        configLock.lock();
        try {
            this.schedulerType = type;
        } finally {
            configLock.unlock();
        }
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
