/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author gcastillo
 */
public class SchedulerOLD extends Exception {

    public static boolean isOnExecution = false;

    public enum SCHEDULER_TYPE {
        MINUTES,
        DAYS
    }
    private final ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(1);

    //private Runnable runnable;
    private String timeValue;
    private SchedulerOLD.SCHEDULER_TYPE schedulerType = SchedulerOLD.SCHEDULER_TYPE.MINUTES;
    private List<String> dayList;
    private int dayListIndex = 0;

    public SchedulerOLD(String timeValue, SCHEDULER_TYPE type, String[] dayList) {
        //this.runnable = runnable;

        this.timeValue = getTimeValue();
        this.schedulerType = getSchedulerType();
        //this.dayList = new ArrayList<String>();
        //java.util.Collections.sort(this.dayList);
        this.dayList = getDayList();
        setFirstIndex();
    }

    private SCHEDULER_TYPE getSchedulerType() {

        if (Integer.parseInt(ConfigProperties.ConfigFile.getSchedulerPeriod()) == 0) {

            schedulerType = SchedulerOLD.SCHEDULER_TYPE.MINUTES;
        } else {

            schedulerType = SchedulerOLD.SCHEDULER_TYPE.DAYS;

        }
        return schedulerType;
    }

    public List<String> getDayList() {
        try {
            ConfigProperties.getPropValues();
        } catch (IOException ex) {
            Logger.getLogger(SchedulerOLD.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Arrays.asList(ConfigProperties.ConfigFile.getSchedulerHours()).stream().sorted().collect(Collectors.toList());
    }

//    public void setDayList(List<String> dayList) {
//        this.dayList = dayList;
//    }
    /* public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
     */
    public String getTimeValue() {
        getDayList();
        return ConfigProperties.ConfigFile.getSchedulerTimeValue();
    }

//    public void setTimeValue(String timeValue) {
//        this.timeValue = timeValue;
//    }
    private long computeNextDelay(int targetHour, int targetMin, int targetSec) {
        //         System.out.println("computeNextDelay()");

        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNextTarget = zonedNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
        if (zonedNow.compareTo(zonedNextTarget) > 0) {
            zonedNextTarget = zonedNextTarget.plusDays(1);
        }

        Duration duration = Duration.between(zonedNow, zonedNextTarget);
        //         System.out.println("   computeNextDelay()  duration.getSeconds(): " + duration.getSeconds());
        return duration.getSeconds();
    }

    private void setFirstIndex() {
//          System.out.println("setFirstIndex()");
        long min_delay = Long.MAX_VALUE;
        long new_delay = 0;
        for (int i = 0; i < getDayList().size(); i++) {
            if (!getDayList().get(i).isEmpty()) {
                new_delay = computeNextDelay(Integer.parseInt(getDayList().get(i).split(":")[0]),
                        Integer.parseInt(getDayList().get(i).split(":")[1]),
                        0);

                //               System.out.println("   new_delay:" + new_delay + " / min_delay:" + min_delay);
                //               System.out.println("   setFirstIndex() dayListIndex: " + dayListIndex);
                if (new_delay < min_delay) {
                    min_delay = new_delay;
                    dayListIndex = i;
                }
                //               System.out.println("   new_delay:" + new_delay + " / min_delay" + min_delay);
//                System.out.println("   setFirstIndex() dayListIndex: " + dayListIndex);
            }
        }
    }

    private void setNextIndex() {
//         System.out.println("setNextIndex()");
        dayListIndex++;
//        System.out.println("   setNextIndex() dayListIndex: " + dayListIndex);
//        System.out.println("   setNextIndex() getDayList().size(): " + getDayList().size());
        if (dayListIndex >= getDayList().size()) {
            dayListIndex = 0;
        }
    }
    //int count = 0;

    public void startExecution() {
        System.out.println("startExecution()");
        try {

            setFirstIndex();

            Runnable taskWrapper = new Runnable() {

                @Override
                public void run() {
                    System.out.println("SCHEDULER WAITING..." + " Time:" + LocalDateTime.now().toString());
//                    System.out.println("startExecution() RUN");
                    //count++;

//                    System.out.println("");
//                    System.out.println("");
//                    System.out.println("  SE    startExecution(1) - isOnExecution: " + isOnExecution + " count:" + count);
//                    System.out.println("");
                    if (!isOnExecution) {
                        isOnExecution = true;
                        //System.gc();
                        try {
                            MainApp main = new MainApp();
                            main.setVisible(false);

                        } catch (Exception e) {
                            isOnExecution = false;
                            System.out.println("");
                            System.out.println("");
                            System.out.println("  SE    startExecution(ERROR) - isOnExecution: " + isOnExecution + " Time:" + LocalDateTime.now().toString() + "  -  " + e.getMessage());
                            System.out.println("");
                            System.out.println("");
                        }

                        //  main.dispose();
                        //   stop();
                        System.out.println("");
                        System.out.println("");
                        System.out.println("  SE    startExecution(2) - isOnExecution: " + isOnExecution + " Time:" + LocalDateTime.now().toString());
                        System.out.println("");
                        System.out.println("");
                    }
//                    System.out.println("  SE    startExecution(3) - isOnExecution: " + isOnExecution + " count:" + count);
//                    System.out.println("");
//                    System.out.println("");
                    startExecution();
//                    System.out.println("");
//                    System.out.println("");
//                    System.out.println("  SE    startExecution(4) - isOnExecution: " + isOnExecution + " count:" + count);
//                    System.out.println("");
//                    System.out.println("");

                }

            };

            if (getSchedulerType() == SCHEDULER_TYPE.DAYS) {
                System.out.println("   ### SCHEDULER_TYPE.DAYS dayListIndex: " + dayListIndex);
                System.out.println("   ### SCHEDULER_TYPE.DAYS getDayList().size(): " + getDayList().size());
                System.out.println("   ### SCHEDULER_TYPE.DAYS getDayList(): " + Arrays.asList(ConfigProperties.ConfigFile.getSchedulerHours()).stream().sorted().collect(Collectors.toList()).toString());

                long delay = computeNextDelay(Integer.parseInt(getDayList().get(dayListIndex).split(":")[0]), Integer.parseInt(getDayList().get(dayListIndex).split(":")[1]), 0);
                scheduler.schedule(taskWrapper, delay, TimeUnit.SECONDS);
                setNextIndex();

            } else {
                scheduler.scheduleAtFixedRate(taskWrapper, 0, Integer.parseInt(timeValue) * 60, TimeUnit.SECONDS);

            }
        } catch (Exception ex) {
            //           System.out.println("Exception startExecution(): " + ex);
            Logger.getLogger(SchedulerOLD.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("   SCHEDULER FALTAL ERROR : " + ex.getMessage());

        }

    }

    public void stop() {
        System.out.println("**************   SCHEDULER shutdown **************");
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
