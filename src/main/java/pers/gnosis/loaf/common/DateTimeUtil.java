package pers.gnosis.loaf.common;

import java.time.LocalDateTime;

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
}
