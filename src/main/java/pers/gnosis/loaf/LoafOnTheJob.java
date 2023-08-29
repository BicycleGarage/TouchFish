package pers.gnosis.loaf;

import pers.gnosis.loaf.common.CustomerPaydayButtonActionListener;
import pers.gnosis.loaf.common.DateTimeUtil;
import pers.gnosis.loaf.common.HolidayUtil;
import pers.gnosis.loaf.common.NumberTextField;
import pers.gnosis.loaf.common.PaydayUtil;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wangsiye
 */
public class LoafOnTheJob {

    /**
     * 字体标红色号
     */
    public static final Color COLOR_RED = new Color(245, 74, 69);

    private BaseDateBO baseDate;

    public LoafOnTheJob(LocalDate now) {
        this.baseDate = new BaseDateBO();
        // 初始化数据
        HolidayUtil.initHolidayData(now, this);
    }

    public static void main(String[] args) {
        LoafOnTheJob loafOnTheJob = new LoafOnTheJob(LocalDate.now());
        BaseDateBO baseDate = loafOnTheJob.getBaseDate();

        // 窗体初始化
        int height = baseDate.getNameHolidayMapNoOffDay().size() >= 5 ? 1050 : 600;
        JFrame jf = new JFrame("摸鱼小助手");
        jf.setPreferredSize(new Dimension(400, height));
        jf.setMinimumSize(new Dimension(400, height));
        jf.setLayout(new FlowLayout(FlowLayout.LEFT));

        // 添加主要功能的容器
        jf.add(loafOnTheJob.getCloseButtonAndAlwaysOnTopCheckBoxPanel(jf));
        jf.add(loafOnTheJob.getOpacitySliderPanel(jf));
        jf.add(loafOnTheJob.getHolidayPanel());
        jf.add(loafOnTheJob.getNoticePanel());
        jf.add(loafOnTheJob.getPayday());
        jf.add(loafOnTheJob.getLeftDayPanel());
        jf.add(loafOnTheJob.getCustomerPaydayPanel());

        // 设置窗口大小
        jf.setSize(250, 250);
        // 把窗口位置设置到屏幕中心
        jf.setLocationRelativeTo(null);
        // 当点击窗口的关闭按钮时退出程序（没有这一句，程序不会退出）
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // 显示窗口，前面创建的信息都在内存中，通过 jf.setVisible(true) 把内存中的窗口显示在屏幕上。
        jf.setVisible(true);

    }

    /**
     * 创建关闭按钮、置顶复选框的容器
     * @param jf 窗体
     * @return 关闭按钮、指定复选框容器
     */
    private JPanel getCloseButtonAndAlwaysOnTopCheckBoxPanel(JFrame jf) {
        JPanel closeButtonAndAlwaysOnTopCheckBoxPanel = new JPanel();
        JButton closeButton = new JButton("Kill me !");
        closeButton.addActionListener(event -> System.exit(0));
        closeButtonAndAlwaysOnTopCheckBoxPanel.add(closeButton);

        // 无实际作用，目的是关闭按钮和置顶显示间隔一段空隙
        closeButtonAndAlwaysOnTopCheckBoxPanel.add(new Label("        "));

        final boolean[] alwaysOnTop = {false};
        JCheckBox alwaysOnTopCheckBox = new JCheckBox("置顶显示：");
        alwaysOnTopCheckBox.addChangeListener(event -> {
            alwaysOnTop[0] = !alwaysOnTop[0];
            jf.setAlwaysOnTop(alwaysOnTop[0]);
        });
        closeButtonAndAlwaysOnTopCheckBoxPanel.add(alwaysOnTopCheckBox);
        return closeButtonAndAlwaysOnTopCheckBoxPanel;
    }

    /**
     * 创建透明度滑条容器
     * @param jf 窗体
     * @return 透明度滑条容器
     */
    private JPanel getOpacitySliderPanel(JFrame jf) {

        // 设置窗口无装饰，否则无法设置背景颜色
        jf.setUndecorated(true);
        jf.setOpacity(0.8F);
        // 添加拖拽功能
        addCustomDragEvent(jf);

        // 创建一个水平方向的滑动条，初始值为50，最小值为20（太透明会看不见），最大值为100
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 20, 100, 80);
        // 设置主刻度间隔为10
        slider.setMajorTickSpacing(10);
        // 设置次刻度间隔为5
        slider.setMinorTickSpacing(5);
        // 显示刻度
        slider.setPaintTicks(true);
        // 显示刻度标签
        slider.setPaintLabels(true);
        slider.addChangeListener(event -> {
            float valueFloat = slider.getValue();
            jf.setOpacity(valueFloat / 100);
        });
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(0, 5, 0, 0));
        panel.add(new JLabel("透明度："));
        panel.add(slider);
        return panel;
    }

    /**
     * 为窗体添加自定义拖拽事件<br />
     * 去掉窗体装饰后，因窗口顶部标题栏消失，导致无法拖拽<br />
     * 这里实现鼠标按住界面任意位置，可以拖拽窗口的自定义事件
     * @param jFrame 窗体
     */
    private void addCustomDragEvent(JFrame jFrame) {
        // 记录鼠标按下时的坐标
        final int[] xOld = new int[1];
        final int[] yOld = new int[1];
        jFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                xOld[0] = e.getX();
                yOld[0] = e.getY();
            }
        });
        jFrame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int xOnScreen = e.getXOnScreen();
                int yOnScreen = e.getYOnScreen();
                int xx = xOnScreen - xOld[0];
                int yy = yOnScreen - yOld[0];
                jFrame.setLocation(xx, yy);
            }
        });
    }

    /**
     * 获取发薪日剩余天数
     * 获取当月、下月的10、15日及距离其天数，若发薪日为周六日、节假日，则提前至最近的工作日
     *
     * @return 获取发薪日剩余天数JPanel
     */
    private JPanel getPayday() {
        LocalDate now = baseDate.getNow();
        List<LocalDate> paydayList = PaydayUtil.doGetPayday(baseDate);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        List<JLabel> labelList = PaydayUtil.getDaysToPaydayLabel(now, paydayList);
        panel.setLayout(new GridLayout(labelList.size(), 1));
        for (JLabel jLabel : labelList) {
            panel.add(jLabel);
        }

        return panel;
    }

    /**
     * 创建自定义发薪日容器
     * @return 自定义发薪日容器
     */
    private JPanel getCustomerPaydayPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new GridLayout(2, 1));

        JPanel inputPaydayPanel = new JPanel();
        // JPanel默认布局为FlowLayout，其内部元素左边距有5，去掉左边距使该panel与GridLayout布局的panel对齐
        inputPaydayPanel.setBorder(new EmptyBorder(0, -5, 0, 0));
        JPanel daysToPaydayPanel = new JPanel();

        JLabel customerPaydayLabel = new JLabel("自定义发薪日：");

        JTextField customerPaydayTextField = new JTextField(4);
        customerPaydayTextField.setDocument(new NumberTextField());

        JButton customerPaydayButton = new JButton("确定");
        customerPaydayButton.addActionListener(new CustomerPaydayButtonActionListener(
                daysToPaydayPanel, customerPaydayTextField, baseDate));

        inputPaydayPanel.add(customerPaydayLabel);
        inputPaydayPanel.add(customerPaydayTextField);
        inputPaydayPanel.add(customerPaydayButton);

        panel.add(inputPaydayPanel);
        panel.add(daysToPaydayPanel);

        return panel;
    }

    /**
     * 获取假期剩余天数
     *
     * @return 剩余天数JPanel
     */
    private JPanel getLeftDayPanel() {
        LocalDate now = baseDate.getNow();
        LocalDate nextSaturday = baseDate.getNextSaturday();
        LocalDate nextSunday = baseDate.getNextSunday();

        JPanel panel3 = new JPanel();
        panel3.setBorder(new EmptyBorder(10, 10, 10, 10));
        Map<String, Holiday> nameHolidayMapNoOffDay = baseDate.getNameHolidayMapNoOffDay();
        panel3.setLayout(new GridLayout(3 + nameHolidayMapNoOffDay.size(), 1));
        List<JLabel> holidayLeftDaysLabelList = new ArrayList<>();
        holidayLeftDaysLabelList.add(new JLabel("距离本周周末："));
        holidayLeftDaysLabelList.add(new JLabel("周六 还有" + now.until(nextSaturday, ChronoUnit.DAYS) + "天"));
        holidayLeftDaysLabelList.add(new JLabel("周日 还有" + now.until(nextSunday, ChronoUnit.DAYS) + "天"));
        for (Holiday holiday : nameHolidayMapNoOffDay.values()) {
            holidayLeftDaysLabelList.add(new JLabel("距离" + holiday.getName() + "假期还有："
                    + now.until(holiday.getDate(), ChronoUnit.DAYS) + "天"));
        }
        for (JLabel jLabel : holidayLeftDaysLabelList) {
            panel3.add(jLabel);
        }
        return panel3;
    }

    /**
     * 获取提示信息
     *
     * @return 提示信息JPanel
     */
    private JPanel getNoticePanel() {
        JPanel panel2 = new JPanel();
        panel2.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel2.setLayout(new GridLayout(5, 1));
        List<JLabel> noticeText = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日");
        noticeText.add(new JLabel(formatter.format(baseDate.getNow()) + DateTimeUtil.getPeriod() + "，摸鱼人"));
        noticeText.add(new JLabel("我是摸鱼小助手，下面是摸鱼提醒"));
        noticeText.add(new JLabel("工作再累，一定不要忘记摸鱼哦"));
        noticeText.add(new JLabel("有事没事，起身去茶水间去厕所去廊道走走，别老在工位上坐着"));
        JLabel importantLabel = new JLabel("钱是老板的，但命是自己的");
        importantLabel.setForeground(COLOR_RED);
        noticeText.add(importantLabel);
        for (JLabel jLabel : noticeText) {
            panel2.add(jLabel);
        }
        return panel2;
    }

    /**
     * 获取最近假期
     *
     * @return 最近假期JPanel
     */
    private JPanel getHolidayPanel() {
        JPanel panel1 = new JPanel();
        panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
        Map<String, Holiday> nameHolidayMapNoOffDay = baseDate.getNameHolidayMapNoOffDay();
        panel1.setLayout(new GridLayout(3 + nameHolidayMapNoOffDay.size(), 1));
        List<JLabel> holidayLabelList = new ArrayList<>();
        holidayLabelList.add(new JLabel("最近的假期："));
        List<LocalDate> notOffHolidayDateList = baseDate.getNotOffHolidayDateList();
        LocalDate nextSaturday = baseDate.getNextSaturday();
        LocalDate nextSunday = baseDate.getNextSunday();
        holidayLabelList.add(new JLabel( "周六：" + nextSaturday.toString() +
                (notOffHolidayDateList.contains(nextSaturday) ? "（补班）" : "")));
        holidayLabelList.add(new JLabel("周日：" + nextSunday.toString() +
                (notOffHolidayDateList.contains(nextSunday) ? "（补班）" : "")));
        for (Holiday holiday : nameHolidayMapNoOffDay.values()) {
            holidayLabelList.add(new JLabel(holiday.getName() + "：" + holiday.getDate().toString()));
        }
        for (JLabel jLabel : holidayLabelList) {
            panel1.add(jLabel);
        }
        return panel1;
    }

    public BaseDateBO getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(BaseDateBO baseDate) {
        this.baseDate = baseDate;
    }
}
