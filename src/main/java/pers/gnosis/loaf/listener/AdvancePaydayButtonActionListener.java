package pers.gnosis.loaf.listener;

import pers.gnosis.loaf.common.PaydayUtil;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author wangsiye
 */
public class AdvancePaydayButtonActionListener implements ActionListener {

    private final JPanel paydayPanel;
    private final BaseDateBO baseDateBO;
    /**
     * 点击按钮切换是否提前发薪
     */
    private final boolean advancePayday;

    public AdvancePaydayButtonActionListener(JPanel paydayPanel,
                                             BaseDateBO baseDate,
                                             boolean advancePayday) {
        this.paydayPanel = paydayPanel;
        this.baseDateBO = baseDate;
        this.advancePayday = advancePayday;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (advancePayday == baseDateBO.isAdvancePayday()) {
            // 点击当前已选中的按钮，不做操作
            return;
        }

        // 清空上次的自定义发薪日label
        paydayPanel.removeAll();

        baseDateBO.setAdvancePayday(advancePayday);
        PaydayUtil.initPaydayPanel(baseDateBO, paydayPanel);

        // 对panel内部的组件进行重新布局和绘制
        paydayPanel.revalidate();
        // 对panel本身进行重新绘制
        paydayPanel.repaint();
    }
}
