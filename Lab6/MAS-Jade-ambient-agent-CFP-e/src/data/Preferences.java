/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.Calendar;

/**
 *
 * @author Dell
 */
public class Preferences {

    public static final int WAKE_SUPERSOFT = 0;
    public static final int WAKE_SOFT = 1;
    public static final int WAKE_HARD = 2;

    int[] wakeHours;//Minute of the day
    int[] wakeStyle;

    public Preferences() {
        wakeHours = new int[7];
        wakeStyle = new int[7];
        for (int i = 0; i < wakeHours.length; i++) { // Random
            wakeHours[i] = (int) (Math.random() * 24 * 60);
            wakeStyle[i] = (int) (Math.random() * 3);
        }

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wakeHours.length; i++) {
            sb.append(wakeHours[i]);
            sb.append("\t");
        }
        sb.append('\n');
        for (int i = 0; i < wakeStyle.length; i++) {
            sb.append(wakeStyle[i]);
            sb.append("\t");
        }
        return sb.toString();
    }

    public Preferences(String s) {
        String[] sarr;
        sarr = s.split("\n");
        if (sarr.length != 2) {
            throw new IllegalArgumentException("Bad pref string: " + s);
        }
        wakeHours = new int[sarr[0].split("\t").length];
        wakeStyle = new int[sarr[1].split("\t").length];
        int i = 0;
        for (String x : sarr[0].split("\t")) {
            wakeHours[i] = Integer.parseInt(x);
            i++;
        }
        i = 0;
        for (String x : sarr[1].split("\t")) {
            wakeStyle[i] = Integer.parseInt(x);
            i++;
        }

    }
    public int getWakeMinute(int dow){
        if (dow<0 || dow>=wakeHours.length)
            throw new IllegalArgumentException("Bad Day of Week "+dow);
        return wakeHours[dow];
    }
    public int getWakeStyle(int dow){
        if (dow<0 || dow>=wakeStyle.length)
            throw new IllegalArgumentException("Bad Day of Week "+dow);
        return wakeStyle[dow];
    }
   public static int getMinuteInDay(){
        return Calendar.MINUTE+ Calendar.HOUR_OF_DAY*60;
    }
}
