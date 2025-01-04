package pers.gnosis.loaf;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 假日对象
 */
@Getter
@Setter
public class Holiday {
    private LocalDate date;
    /**
     * 补班日期为false，放假日期为true
     */
    private Boolean isOffDay;
    private String name;
}
