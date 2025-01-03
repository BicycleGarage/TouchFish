package pers.gnosis.loaf.listener;

import pers.gnosis.loaf.common.PaydayUtil;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * @author wangsiye
 */
public class CustomerPaydayButtonActionListener implements ActionListener {

    public static final int MAX_DAY_OF_MONTH = 31;
    private final JPanel daysToPaydayPanel;
    private final JTextField customerPaydayTextField;
    private final BaseDateBO baseDateBO;

    public CustomerPaydayButtonActionListener(JPanel daysToPaydayPanel,
                                              JTextField customerPaydayTextField,
                                              BaseDateBO baseDate) {
        this.daysToPaydayPanel = daysToPaydayPanel;
        this.customerPaydayTextField = customerPaydayTextField;
        this.baseDateBO = baseDate;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 清空上次的自定义发薪日label
        daysToPaydayPanel.removeAll();

        String customerPaydayTextFieldText = customerPaydayTextField.getText();
        if (customerPaydayTextFieldText == null || customerPaydayTextFieldText.isEmpty()) {
            return;
        }
        int customerPaydayDayOfMonth = getNormalCustomerPayday(customerPaydayTextFieldText);

        LocalDate now = baseDateBO.getNow();
        LocalDate nextMonthNow = now.plusMonths(1L);

        LocalDate customerCurrentMonthPayday = PaydayUtil.getWorkDayPayday(
                now.getYear(), now.getMonthValue(), customerPaydayDayOfMonth, baseDateBO);
        LocalDate customerNextMonthPayday = PaydayUtil.getWorkDayPayday(
                nextMonthNow.getYear(), nextMonthNow.getMonthValue(), customerPaydayDayOfMonth, baseDateBO);
        List<JLabel> daysToPaydayLabelList = PaydayUtil.getDaysToPaydayLabel(
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
        if (customerPaydayDayOfMonth > MAX_DAY_OF_MONTH) {
            customerPaydayDayOfMonth = MAX_DAY_OF_MONTH;
        }
        if (customerPaydayDayOfMonth < 1) {
            customerPaydayDayOfMonth = 1;
        }
        return customerPaydayDayOfMonth;
    }
}
