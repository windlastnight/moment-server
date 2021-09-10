package cn.rongcloud.moment.server.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sunyinglong on 2020/6/25
 */
public class DateTimeUtils {

    /**
     *  无切分字符的日期表示
     */
    private static String NON_SPLIT_DAY_PATTERN = "yyyyMMdd";

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

    public static List<String> dailyListWithStartAndEnd(String begin, String end, String pattern, int step) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        long beginTs = 0L;
        long endTs = 0L;

        try {
            beginTs = sdf.parse(begin).getTime();
            endTs = sdf.parse(end).getTime();
        } catch (ParseException e) {

        }

        Calendar c = Calendar.getInstance();
        c.setTime(new Date(beginTs));

        List<String> dailyStrings = new ArrayList<>();

        while (c.getTimeInMillis() <= endTs) {
            dailyStrings.add(sdf.format(c.getTime()));

            c.add(Calendar.DATE, step);
        }

        return dailyStrings;
    }

    public static List<String> dailyListWithStartAndEnd(String begin, String end, String pattern) {
        return dailyListWithStartAndEnd(begin, end, pattern, 1);
    }
    public static String getDateTimeStringWithoutSplitByMillis(long ts){
        return new SimpleDateFormat(NON_SPLIT_DAY_PATTERN).format(new Date(ts));
    }
}
