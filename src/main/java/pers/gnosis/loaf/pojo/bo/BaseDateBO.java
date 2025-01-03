package pers.gnosis.loaf.pojo.bo;

import pers.gnosis.loaf.Holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author wangsiye
 */
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
     */
    private Map<Integer, Integer> paydayMap;

    public List<Holiday> getHolidayList() {
        return holidayList;
    }

    public void setHolidayList(List<Holiday> holidayList) {
        this.holidayList = holidayList;
    }

    public Map<String, Holiday> getNameHolidayMapNoOffDay() {
        return nameHolidayMapNoOffDay;
    }

    public void setNameHolidayMapNoOffDay(Map<String, Holiday> nameHolidayMapNoOffDay) {
        this.nameHolidayMapNoOffDay = nameHolidayMapNoOffDay;
    }

    public List<LocalDate> getNotOffHolidayDateList() {
        return notOffHolidayDateList;
    }

    public void setNotOffHolidayDateList(List<LocalDate> notOffHolidayDateList) {
        this.notOffHolidayDateList = notOffHolidayDateList;
    }

    public List<LocalDate> getHolidayDateList() {
        return holidayDateList;
    }

    public void setHolidayDateList(List<LocalDate> holidayDateList) {
        this.holidayDateList = holidayDateList;
    }

    public LocalDate getNow() {
        return now;
    }

    public void setNow(LocalDate now) {
        this.now = now;
    }

    public LocalDate getNextSaturday() {
        return nextSaturday;
    }

    public void setNextSaturday(LocalDate nextSaturday) {
        this.nextSaturday = nextSaturday;
    }

    public LocalDate getNextSunday() {
        return nextSunday;
    }

    public void setNextSunday(LocalDate nextSunday) {
        this.nextSunday = nextSunday;
    }

    public boolean isAdvancePayday() {
        return advancePayday;
    }

    public void setAdvancePayday(boolean advancePayday) {
        this.advancePayday = advancePayday;
    }

    public Map<Integer, Integer> getPaydayMap() {
        return paydayMap;
    }

    public void setPaydayMap(Map<Integer, Integer> paydayMap) {
        this.paydayMap = paydayMap;
    }
}
