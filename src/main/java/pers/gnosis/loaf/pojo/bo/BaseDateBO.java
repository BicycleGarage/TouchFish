package pers.gnosis.loaf.pojo.bo;

import lombok.Getter;
import lombok.Setter;
import pers.gnosis.loaf.Holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * @author wangsiye
 */
@Setter
@Getter
public class BaseDateBO {
    private LocalDate now;
    private LocalDate nextSaturday;
    private LocalDate nextSunday;
    /**
     * 今年节假日，同时包含放假、补班日（如周六日本应双休，但由于调休机制需要补班，其isOffDay=false）
     */
    private List<Holiday> holidayList;
    /**
     * 名称-假日map：key=节假日名称，value=非补班日的此假期的第一天
     */
    private Map<String, Holiday> nameHolidayMapNoOffDay;
    /**
     * 补班的周六日日期
     */
    private List<LocalDate> notOffHolidayDateList;
    /**
     * 放假且不补班的假期日期
     */
    private List<LocalDate> holidayDateList;
    /*
    是否提前发薪，默认否
     */
    private boolean advancePayday = false;
    /**
     * 用户指定发薪日
     * 用map是为方便查找
     * value暂无意义，与key相同
     * 默认包含15日
     */
    private Map<Integer, Integer> paydayMap;
    /**
     * 倒计时的时
     */
    private int hour;
    /**
     * 倒计时的分
     */
    private int minuted;
    /**
     * 倒计时剩余秒数
     */
    private long timeLeft;
    /**
     * 逐秒进行的倒计时的timer
     */
    private Timer offWorkTimer;
    /**
     * 结束倒计时的timer，用于倒计时结束之后做些操作
     */
    private Timer endCountdownTimer;
}
