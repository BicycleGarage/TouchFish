package pers.gnosis.loaf.listener;

import pers.gnosis.loaf.common.PaydayUtil;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author wangsiye
 */
public class CustomerPaydayAddButtonActionListener implements ActionListener {

    public static final int MAX_DAY_OF_MONTH = 31;
    private final JPanel paydayPanel;
    private final JTextField customerPaydayTextField;
    private final BaseDateBO baseDate;

    public CustomerPaydayAddButtonActionListener(JPanel paydayPanel,
                                                 JTextField customerPaydayTextField,
                                                 BaseDateBO baseDate) {
        this.paydayPanel = paydayPanel;
        this.customerPaydayTextField = customerPaydayTextField;
        this.baseDate = baseDate;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 清空上次的自定义发薪日label
        paydayPanel.removeAll();

        String customerPaydayTextFieldText = customerPaydayTextField.getText();
        if (customerPaydayTextFieldText == null || customerPaydayTextFieldText.isEmpty()) {
            return;
        }
        int customerPaydayDayOfMonth = getNormalCustomerPayday(customerPaydayTextFieldText);
        if (baseDate.getPaydayMap().containsKey(customerPaydayDayOfMonth)) {
            return;
        }
        baseDate.getPaydayMap().put(customerPaydayDayOfMonth, customerPaydayDayOfMonth);


        PaydayUtil.initPaydayPanel(baseDate, paydayPanel);

        // 对panel内部的组件进行重新布局和绘制
        paydayPanel.revalidate();
        // 对panel本身进行重新绘制
        paydayPanel.repaint();
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
