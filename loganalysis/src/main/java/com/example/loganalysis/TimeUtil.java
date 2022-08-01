package com.example.loganalysis;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class TimeUtil {
    /**
     * 给定字符串，转成时间戳，格式yyyy-MM-dd HH:mm:ss
     */
    public static long transDate(String str) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(str);
        } catch (ParseException e) {
            return -1;
        }
        long time = parse.getTime();
        return time;
    }
}
