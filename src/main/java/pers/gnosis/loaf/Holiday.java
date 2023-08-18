package pers.gnosis.loaf;

import java.time.LocalDate;

/**
 * 假日对象
 */
public class Holiday {
    private LocalDate date;
    /**
     * 补班日期为false，放假日期为true
     */
    private Boolean isOffDay;
    private String name;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getOffDay() {
        return isOffDay;
    }

    public void setOffDay(Boolean offDay) {
        isOffDay = offDay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
