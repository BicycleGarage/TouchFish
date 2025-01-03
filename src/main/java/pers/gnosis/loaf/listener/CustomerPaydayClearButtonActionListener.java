package pers.gnosis.loaf.listener;

import pers.gnosis.loaf.common.PaydayUtil;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * @author wangsiye
 */
public class CustomerPaydayClearButtonActionListener implements ActionListener {

    /**
     * 展示距离发薪日panel
     */
    private final JPanel paydayPanel;
    /**
     * 展示发薪日列表panel
     */
    private final JPanel showPaydayPanel;
    /**
     * 展示发薪日列表Label
     */
    private JLabel paydayListLabel;
    private final BaseDateBO baseDate;

    public CustomerPaydayClearButtonActionListener(JPanel paydayPanel,
                                                   JPanel showPaydayPanel,
                                                   JLabel paydayListLabel,
                                                   BaseDateBO baseDate) {
        this.paydayPanel = paydayPanel;
        this.showPaydayPanel = showPaydayPanel;
        this.paydayListLabel = paydayListLabel;
        this.baseDate = baseDate;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        baseDate.setPaydayMap(new HashMap<>());
        paydayListLabel = PaydayUtil.initPaydayListLabel(baseDate);
        paydayPanel.removeAll();
        paydayPanel.revalidate();
        paydayPanel.repaint();
        showPaydayPanel.removeAll();
        showPaydayPanel.add(paydayListLabel);
        showPaydayPanel.revalidate();
        showPaydayPanel.repaint();
    }

}
