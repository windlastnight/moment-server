package cn.rongcloud.moment.server.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sunyinglong on 2020/6/25
 */
public class DateTimeUtils {
    private DateTimeUtils() {}
    public static Date currentDt() {
        return new Date();
    }
    public static Date strToDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
