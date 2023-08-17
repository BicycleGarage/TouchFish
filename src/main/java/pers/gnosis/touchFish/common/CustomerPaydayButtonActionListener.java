package pers.gnosis.touchFish.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class CustomerPaydayButtonActionListener implements ActionListener {

    private JPanel daysToPaydayPanel;
    private JTextField customerPaydayTextField;
    private LocalDate now;
    private List<LocalDate> notOffHolidayDateList;
    private List<LocalDate> holidayDateList;

    public CustomerPaydayButtonActionListener(
            JPanel daysToPaydayPanel, JTextField customerPaydayTextField, LocalDate now,
            List<LocalDate> notOffHolidayDateList, List<LocalDate> holidayDateList) {
        this.daysToPaydayPanel = daysToPaydayPanel;
        this.customerPaydayTextField = customerPaydayTextField;
        this.now = now;
        this.notOffHolidayDateList = notOffHolidayDateList;
        this.holidayDateList = holidayDateList;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 清空上次的自定义发薪日label
        daysToPaydayPanel.removeAll();

        String customerPaydayTextFieldText = customerPaydayTextField.getText();
        if (customerPaydayTextFieldText == null || "".equals(customerPaydayTextFieldText)) {
            return;
        }
        int customerPaydayDayOfMonth = getNormalCustomerPayday(customerPaydayTextFieldText);

        LocalDate customerCurrentMonthPayday = Utils.getWorkDayPayday(
                now, customerPaydayDayOfMonth, notOffHolidayDateList, holidayDateList);
        LocalDate customerNextMonthPayday = Utils.getWorkDayPayday(
                now.plusMonths(1L), customerPaydayDayOfMonth, notOffHolidayDateList, holidayDateList);
        List<JLabel> daysToPaydayLabelList = Utils.getDaysToPaydayLabel(
                now, Arrays.asList(customerCurrentMonthPayday, customerNextMonthPayday));
        daysToPaydayPanel.setLayout(new GridLayout(daysToPaydayLabelList.size(), 1));
        for (JLabel label : daysToPaydayLabelList) {
            daysToPaydayPanel.add(label);
        }

        // 对panel内部的组件进行重新布局和绘制
        daysToPaydayPanel.revalidate();
        // 对panel本身进行重新绘制
        daysToPaydayPanel.repaint();
    }

    /**
     * 获取规格化发薪日值：<br />
     * 小于1的取1，大于31的取31；<br />
     * 若大于本月最大日，取本月最大日
     *
     * @param customerPaydayTextFieldText 用户填写的日期字符串，已限制为仅为数字，无需再控制异常
     * @return 规格化发薪日值
     */
    private int getNormalCustomerPayday(String customerPaydayTextFieldText) {
        int customerPaydayDayOfMonth = Integer.parseInt(customerPaydayTextFieldText);
        if (customerPaydayDayOfMonth > 32) {
            customerPaydayDayOfMonth = 31;
        }
        if (customerPaydayDayOfMonth < 1) {
            customerPaydayDayOfMonth = 1;
        }
        return customerPaydayDayOfMonth;
    }
}
