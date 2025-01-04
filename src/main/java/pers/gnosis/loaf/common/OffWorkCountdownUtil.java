package pers.gnosis.loaf.common;

import pers.gnosis.loaf.listener.CountdownDocumentListener;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 下班倒计时工具类
 */
public class OffWorkCountdownUtil {

    /**
     * 组装倒计时panel，实现自动更新倒计时逻辑
     *
     * @param baseDate 基本数据
     * @return 倒计时panel
     */
    public static JPanel makeupCountdownPanel(BaseDateBO baseDate) {

        JPanel countdownPanel = GUIUtil.getMyjPanelFlowLayout(true);
        // 不明原因往右了一点
        // 原因不是flowlayout的统一5左边距导致，因为滑块panel也为flowlayout，没有往右
        countdownPanel.setBorder(new EmptyBorder(0, -5, 0, 0));
        JTextField hourField = new JTextField(2);
        hourField.setDocument(new NumberTextField());
        hourField.setText("18");
        JTextField minuteField = new JTextField(2);
        minuteField.setDocument(new NumberTextField());
        minuteField.setText("0");

        JLabel leftSecondLabel = doMakeupCountdownPanel(baseDate, countdownPanel, hourField, minuteField);

        hourField.getDocument().addDocumentListener(
                new CountdownDocumentListener(baseDate, leftSecondLabel, hourField, minuteField));
        minuteField.getDocument().addDocumentListener(
                new CountdownDocumentListener(baseDate, leftSecondLabel, hourField, minuteField));

        // 设置定时器更新倒计时
        setCountdownTimer(baseDate, leftSecondLabel);

        return countdownPanel;
    }

    /**
     * 设置倒计时timer，用于触发发更新label
     * @param baseDate 基本数据
     * @param leftSecondLabel 倒计时label
     */
    public static void setCountdownTimer(BaseDateBO baseDate, JLabel leftSecondLabel) {

        if (baseDate.getOffWorkTimer() != null) {
            baseDate.getOffWorkTimer().cancel();
        }
        if (baseDate.getEndCountdownTimer() != null) {
            baseDate.getEndCountdownTimer().cancel();
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 更新倒计时时间
                baseDate.setTimeLeft(baseDate.getTimeLeft() - 1);

                // 记录开始时间
                final long startTime = System.currentTimeMillis();
                // 计算结束时间
                final long endTime = startTime + baseDate.getTimeLeft() * 1000;

                // 主要逻辑：更新倒计时label
                refreshCountdownLabel(leftSecondLabel, false, baseDate);

                // 停止定时器
                if (System.currentTimeMillis() >= endTime) {
                    timer.cancel();
                }
            }
        }, 0, 1000);
        baseDate.setOffWorkTimer(timer);

        Timer endTimer = new Timer();
        endTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 下班啦
                refreshCountdownLabel(leftSecondLabel, true, baseDate);
            }
            // 延迟1秒显示“下班啦！”
        }, baseDate.getTimeLeft() * 1000 + 1000);
        baseDate.setEndCountdownTimer(endTimer);
    }

    /**
     * 更新倒计时label
     * @param leftSecondLabel 倒计时label
     * @param isEnd 倒计时是否结束
     * @param baseDate 基本数据
     */
    public static void refreshCountdownLabel(JLabel leftSecondLabel, boolean isEnd, BaseDateBO baseDate) {
        leftSecondLabel.removeAll();
        leftSecondLabel.setText(getLeftSecondText(isEnd, baseDate.getTimeLeft()));
        leftSecondLabel.revalidate();
        leftSecondLabel.repaint();
    }

    /**
     * 更新panel主要内容
     *
     * @param baseDate       基本数据
     * @param countdownPanel 倒计时panel
     * @param hourField      倒计时-时输入框
     * @param minuteField    倒计时-分输入框
     * @return 每秒更新的倒计时label
     */
    private static JLabel doMakeupCountdownPanel(BaseDateBO baseDate, JPanel countdownPanel, JTextField hourField, JTextField minuteField) {

        JLabel labelText = new JLabel("距离下班");
        JLabel labelColon = new JLabel(":");

        JLabel leftSecondLabel = new JLabel();
        makeupLeftSecondLabel(baseDate, hourField, minuteField, false, leftSecondLabel);

        countdownPanel.add(labelText);
        countdownPanel.add(hourField);
        countdownPanel.add(labelColon);
        countdownPanel.add(minuteField);
        countdownPanel.add(leftSecondLabel);

        return leftSecondLabel;
    }

    /**
     * 构建倒计时label
     *
     * @param baseDate        基本数据
     * @param hourField       倒计时-时输入框
     * @param minuteField     倒计时-分输入框
     * @param isEnd           是否倒计时已经结束
     * @param leftSecondLabel 倒计时label
     */
    public static void makeupLeftSecondLabel(BaseDateBO baseDate, JTextField hourField, JTextField minuteField, boolean isEnd, JLabel leftSecondLabel) {
        leftSecondLabel.removeAll();

        String hourStr = hourField.getText();
        String minuteStr = minuteField.getText();
        int hour = 0;
        int minute = 0;
        if (hourStr != null && !hourStr.isEmpty() && minuteStr != null && !minuteStr.isEmpty()) {
            hour = Integer.parseInt(hourStr);
            minute = Integer.parseInt(minuteStr);
            if(hour > 23) {
                hour = 24;
                hourField.setText("24");
            }
            if(minute > 59) {
                minute = 59;
                hourField.setText("59");
            }
        }

        long timeLeft = DateTimeUtil.calculateTimeLeft(hour, minute, 0);
        leftSecondLabel.setText(getLeftSecondText(isEnd, timeLeft));
        baseDate.setTimeLeft(timeLeft);

        leftSecondLabel.revalidate();
        leftSecondLabel.repaint();
    }

    /**
     * 获取倒计时剩余时间
     * @param isEnd 是否倒计时结束
     * @param timeLeft 剩余时间
     * @return 下班倒计时字符串
     */
    private static String getLeftSecondText(boolean isEnd, long timeLeft) {
        return " 还有" + (isEnd ? "…… 已经下班啦！" : DateTimeUtil.formatTime(timeLeft));
    }
}
