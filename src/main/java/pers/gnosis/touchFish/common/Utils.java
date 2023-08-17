package pers.gnosis.touchFish.common;

import javax.swing.*;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static final int SATURDAY_VALUE = DayOfWeek.SATURDAY.getValue();
    public static final int SUNDAY_VALUE = DayOfWeek.SUNDAY.getValue();
    public static final DecimalFormat FORMAT = new DecimalFormat("00");

    /**
     * 根据年月、发薪日，获取位于工作日的发薪日：若发薪日为周六日、节假日，则提前至最近的工作日
     *
     * @param now              日期，用于获取年月
     * @param paydayDayOfMonth 发薪日
     * @return 对应日期年月的、位于工作日的发薪日
     */
    public static LocalDate getWorkDayPayday(LocalDate now, int paydayDayOfMonth,
                                             List<LocalDate> notOffHolidayDateList,
                                             List<LocalDate> holidayDateList) {
        int currentMonthYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // 处理本月最大日：填31时，若本月只有30日，则改为30；2月份同理
        LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        int lastDay = lastDayOfMonth.getDayOfMonth();
        if (paydayDayOfMonth > lastDay) {
            paydayDayOfMonth = lastDay;
        }

        return doGetWorkDayPayday(LocalDate.of(currentMonthYear, currentMonth, paydayDayOfMonth),
                notOffHolidayDateList, holidayDateList);
    }

    /**
     * 获取工作日发薪日：若发薪日位于周六日或假期内，则提前到最近的工作日
     *
     * @param payday                未提前到工作日的发薪日
     * @param notOffHolidayDateList 补班的周六日日期
     * @param holidayDateList       放假且不补班的假期日期
     * @return 提前到工作日的发薪日
     */
    private static LocalDate doGetWorkDayPayday(LocalDate payday,
                                                List<LocalDate> notOffHolidayDateList,
                                                List<LocalDate> holidayDateList) {
        int paydayDayOfWeekValue = payday.getDayOfWeek().getValue();
        if (SATURDAY_VALUE == paydayDayOfWeekValue || SUNDAY_VALUE == paydayDayOfWeekValue) {
            if (notOffHolidayDateList.contains(payday)) {
                return payday;
            } else {
                payday = payday.minusDays(1L);
                return doGetWorkDayPayday(payday, notOffHolidayDateList, holidayDateList);
            }
        } else {
            if (holidayDateList.contains(payday)) {
                payday = payday.minusDays(1L);
                return doGetWorkDayPayday(payday, notOffHolidayDateList, holidayDateList);
            } else {
                return payday;
            }
        }
    }

    /**
     * 获取距离发薪日还有xx天的label
     *
     * @param now              目标日期，距离该日期有xx天
     * @param futurePaydayList 发薪日集合
     * @return 距离发薪日还有xx天的label
     */
    public static List<JLabel> getDaysToPaydayLabel(LocalDate now, List<LocalDate> futurePaydayList) {
        List<JLabel> labelList = new ArrayList<>();
        if (futurePaydayList == null) {
            return labelList;
        }
        labelList.add(new JLabel("距离发薪日还有：\n"));
        for (LocalDate payday : futurePaydayList) {
            if(payday.isBefore(now)) {
                continue;
            }
            labelList.add(new JLabel("距离" + FORMAT.format(payday.getMonthValue()) + "月"
                    + FORMAT.format(payday.getDayOfMonth()) + "日还有："
                    + now.until(payday, ChronoUnit.DAYS) + "天"));
        }
        return labelList;
    }
}
