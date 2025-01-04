package pers.gnosis.loaf.listener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pers.gnosis.loaf.common.OffWorkCountdownUtil;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@Getter
@Setter
@AllArgsConstructor
public class CountdownDocumentListener implements DocumentListener {

    private BaseDateBO baseDate;
    /**
     * 倒计时Label
     */
    private JLabel leftSecondLabel;
    /**
     * 倒计时-时输入框
     */
    private JTextField hourField;
    /**
     * 倒计时-分输入框
     */
    private JTextField minuteField;
    @Override
    public void insertUpdate(DocumentEvent e) {
        doUpdate();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        doUpdate();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        doUpdate();
    }

    /**
     * 更新Label
     */
    private void doUpdate() {
        OffWorkCountdownUtil.makeupLeftSecondLabel(baseDate, hourField, minuteField, false, leftSecondLabel);
        OffWorkCountdownUtil.setCountdownTimer(baseDate, leftSecondLabel);
    }
}
