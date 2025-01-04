package pers.gnosis.loaf.common;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author wangsiye
 */
public class DateTimeUtil {
    /**
     * 十一月
     */
    public static final int NOVEMBER = 11;
    /**
     * 3点钟
     */
    public static final int THREE_O_CLOCK = 3;
    /**
     * 5点钟
     */
    public static final int FIVE_O_CLOCK = 5;
    /**
     * 9点钟
     */
    public static final int NINE_O_CLOCK = 9;
    /**
     * 12点钟
     */
    public static final int TWELVE_O_CLOCK = 12;
    /**
     * 14点钟
     */
    public static final int FOURTEEN_O_CLOCK = 14;
    /**
     * 18点钟
     */
    public static final int EIGHTEEN_O_CLOCK = 18;
    /**
     * 23点钟
     */
    public static final int TWENTY_TREE_O_CLOCK = 23;

    public static String getPeriod() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if (hour < THREE_O_CLOCK) {
            return "夜深了";
        }
        if (hour < FIVE_O_CLOCK) {
            return "天亮了";
        }
        if (hour < NINE_O_CLOCK) {
            return "早上好";
        }
        if (hour < TWELVE_O_CLOCK) {
            return "上午好";
        }
        if (hour < FOURTEEN_O_CLOCK) {
            return "中午好";
        }
        if (hour < EIGHTEEN_O_CLOCK) {
            return "下午好";
        }
        if (hour < TWENTY_TREE_O_CLOCK) {
            return "晚上好";
        }
        return "";
    }

    /**
     * 获取当前距离指定时间的秒数
     * @param hour 时
     * @param minute 分
     * @param second 秒
     * @return 秒数
     */
    public static long calculateTimeLeft(int hour, int minute, int second) {
        LocalTime now = LocalTime.now();
        LocalTime targetTime = LocalTime.of(hour, minute, second);

        // 计算当前时间和18点的差值（以秒为单位）
        Duration duration = Duration.between(now, targetTime);
        return duration.isNegative() ? 0 : duration.getSeconds();
    }

    /**
     * 输入秒数，返回字符串“n小时n分n秒”
     * @param seconds 秒数
     * @return 字符串
     */
    public static String formatTime(long seconds) {
        if (seconds < 0) {
//            throw new IllegalArgumentException("秒数不能为负数");
            return "";
        }

        // 如果秒数小于60秒
        if (seconds < 60) {
            return seconds + "秒";
        }

        // 如果秒数在60秒到3600秒之间，表示分钟和秒
        if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "分" + remainingSeconds + "秒";
        }

        // 如果秒数大于或等于3600秒，表示小时、分钟和秒
        long hours = seconds / 3600;
        long remainingMinutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return hours + "小时" + remainingMinutes + "分" + remainingSeconds + "秒";
    }
}
