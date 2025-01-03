package pers.gnosis.loaf.common;

import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author wangsiye
 */
public class PaydayUtil {
    /**
     * 首次发薪日
     */
    public static final int FIRST_PAYDAY = 10;
    /**
     * 二次发薪日
     */
    public static final int SECOND_PAYDAY = 15;
    public static final int SATURDAY_VALUE = DayOfWeek.SATURDAY.getValue();
    public static final int SUNDAY_VALUE = DayOfWeek.SUNDAY.getValue();
    public static final DecimalFormat FORMAT = new DecimalFormat("00");
    public static final boolean ADVANCE_PAYDAY = true;
    public static final boolean DELAY_PAYDAY = false;


    /**
     * 获取发薪日<br />
     * 发薪日：当月、下月的10、15日，若发薪日为周六日、节假日，则提前至最近的工作日
     *
     * @param baseDate 基本时间对象
     * @return 发薪日集合：固定4个，本月和下月的10、15日（若发薪日为周六日、节假日，则提前至最近的工作日）
     */
    public static List<LocalDate> doGetPayday(BaseDateBO baseDate) {
        LocalDate now = baseDate.getNow();
        int year = now.getYear();
        int month = now.getMonthValue();

        List<LocalDate> paydayDateList = new ArrayList<>();

        Map<Integer, Integer> paydayMap = baseDate.getPaydayMap();
        if(paydayMap == null || paydayMap.isEmpty()) {
            return paydayDateList;
        }

        // 当月发薪日
        paydayMap.keySet()
                .forEach(payday -> paydayDateList.add(
                        PaydayUtil.getWorkDayPayday(year, month, payday, baseDate)));

        // 下月发薪日
        LocalDate nextMonthNow = now.plusMonths(1L);
        int nextMonthYear = nextMonthNow.getYear();
        int nextMonth = nextMonthNow.getMonthValue();
        paydayMap.keySet()
                .forEach(payday -> paydayDateList.add(
                        PaydayUtil.getWorkDayPayday(nextMonthYear, nextMonth, payday, baseDate)));

        return paydayDateList;
    }

    /**
     * 根据年月、发薪日，获取位于工作日的发薪日：若发薪日为周六日、节假日，则提前至最近的工作日
     *
     * @param year             年
     * @param month            月
     * @param paydayDayOfMonth 发薪日
     * @param baseDate         基本数据
     * @return 对应日期年月的、位于工作日的发薪日
     */
    public static LocalDate getWorkDayPayday(int year, int month, int paydayDayOfMonth, BaseDateBO baseDate) {

        // 处理本月最大日：填31时，若本月只有30日，则改为30；2月份同理
        LocalDate lastDayOfMonth = LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());
        int lastDay = lastDayOfMonth.getDayOfMonth();
        if (paydayDayOfMonth > lastDay) {
            paydayDayOfMonth = lastDay;
        }

        return doGetWorkDayPayday(LocalDate.of(year, month, paydayDayOfMonth),
                baseDate.getNotOffHolidayDateList(), baseDate.getHolidayDateList(), baseDate.isAdvancePayday());
    }

    /**
     * 获取工作日发薪日：若发薪日位于周六日或假期内，则提前到最近的工作日
     *
     * @param payday                未提前到工作日的发薪日
     * @param notOffHolidayDateList 补班的周六日日期
     * @param holidayDateList       放假且不补班的假期日期
     * @param advancePayday         是否提前发薪
     * @return 提前到工作日的发薪日
     */
    private static LocalDate doGetWorkDayPayday(LocalDate payday,
                                                List<LocalDate> notOffHolidayDateList,
                                                List<LocalDate> holidayDateList, boolean advancePayday) {
        int paydayDayOfWeekValue = payday.getDayOfWeek().getValue();
        if (SATURDAY_VALUE == paydayDayOfWeekValue || SUNDAY_VALUE == paydayDayOfWeekValue) {
            if (notOffHolidayDateList.contains(payday)) {
                return payday;
            } else {
                if(advancePayday) {
                    payday = payday.minusDays(1L);
                } else {
                    payday = payday.plusDays(1L);
                }
                return doGetWorkDayPayday(payday, notOffHolidayDateList, holidayDateList, advancePayday);
            }
        } else {
            if (holidayDateList.contains(payday)) {
                if(advancePayday) {
                    payday = payday.minusDays(1L);
                } else {
                    payday = payday.plusDays(1L);
                }
                return doGetWorkDayPayday(payday, notOffHolidayDateList, holidayDateList, advancePayday);
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
            if (payday.isBefore(now)) {
                continue;
            }
            labelList.add(new JLabel("距离" + FORMAT.format(payday.getMonthValue()) + "月"
                    + FORMAT.format(payday.getDayOfMonth()) + "日还有："
                    + now.until(payday, ChronoUnit.DAYS) + "天"));
        }
        return labelList;
    }

    /**
     * 初始化发薪日展示
     * @param baseDate 基本数据
     * @param paydayPanel 发薪日展示panel
     */
    public static void initPaydayPanel(BaseDateBO baseDate, JPanel paydayPanel) {
        List<LocalDate> paydayList = PaydayUtil.doGetPayday(baseDate);
        List<JLabel> labelList = PaydayUtil.getDaysToPaydayLabel(baseDate.getNow(), paydayList);
        paydayPanel.setLayout(new GridLayout(labelList.size() + 1, 1));
        for (JLabel jLabel : labelList) {
            paydayPanel.add(jLabel);
        }
    }
}
