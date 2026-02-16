package com.kiddo.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String getDateString(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, offset);
        Date d = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(d);
    }

    public static boolean isWeekend(String dateKey) {
        try {
            String[] parts = dateKey.split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);
            Calendar cal = Calendar.getInstance();
            cal.set(y, m, day, 0, 0, 0);
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY;
        } catch (Exception e) {
            return false;
        }
    }

    public static String formatDate(String dateKey) {
        try {
            String[] parts = dateKey.split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);
            Calendar cal = Calendar.getInstance();
            cal.set(y, m, day);
            String[] days = {"Domingo","Segunda","Terça","Quarta","Quinta","Sexta","Sábado"};
            String[] months = {"jan","fev","mar","abr","mai","jun","jul","ago","set","out","nov","dez"};
            String dayName = days[cal.get(Calendar.DAY_OF_WEEK)-1];
            return dayName + ", " + day + " " + months[m];
        } catch (Exception e) {
            return dateKey;
        }
    }
}
