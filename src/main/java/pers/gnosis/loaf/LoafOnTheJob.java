package pers.gnosis.loaf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import pers.gnosis.loaf.common.CustomerPaydayButtonActionListener;
import pers.gnosis.loaf.common.DateTimeUtil;
import pers.gnosis.loaf.common.NumberTextField;
import pers.gnosis.loaf.common.PaydayUtil;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wangsiye
 */
public class LoafOnTheJob {

    /**
     * 字体标红色号
     */
    public static final Color COLOR_RED = new Color(245, 74, 69);
    /**
     * 网络连接重试次数
     */
    public static final int RETRY_TIME = 2;

    private BaseDateBO baseDate;

    public LoafOnTheJob(LocalDate now) {
        this.baseDate = new BaseDateBO();
        initHolidayData(now);
    }

    public static void main(String[] args) {
        LoafOnTheJob loafOnTheJob = new LoafOnTheJob(LocalDate.now());
        BaseDateBO baseDate = loafOnTheJob.getBaseDate();
        LocalDate now = baseDate.getNow();

        int height = baseDate.getNameHolidayMapNoOffDay().size() >= 5 ? 1050 : 600;
        JFrame jf = new JFrame("摸鱼小助手");
        jf.setPreferredSize(new Dimension(400, height));
        jf.setMinimumSize(new Dimension(400, height));
        jf.setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel panel1 = loafOnTheJob.getHolidayPanel();
        jf.add(panel1);

        JPanel panel2 = loafOnTheJob.getNoticePanel();
        jf.add(panel2);

        JPanel paydayPanel = loafOnTheJob.getPayday();
        jf.add(paydayPanel);

        JPanel panel3 = loafOnTheJob.getLeftDayPanel();
        jf.add(panel3);

        JPanel customerPaydayPanel = loafOnTheJob.getCustomerPaydayPanel();
        jf.add(customerPaydayPanel);

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
        LocalDate nextWeekend = baseDate.getNextWeekend();

        JPanel panel3 = new JPanel();
        panel3.setBorder(new EmptyBorder(10, 10, 10, 10));
        Map<String, Holiday> nameHolidayMapNoOffDay = baseDate.getNameHolidayMapNoOffDay();
        panel3.setLayout(new GridLayout(1 + nameHolidayMapNoOffDay.size(), 1));
        List<JLabel> holidayLeftDaysLabelList = new ArrayList<>();
        holidayLeftDaysLabelList.add(new JLabel("距离本周周末还有：" + now.until(nextWeekend, ChronoUnit.DAYS) + "天"));
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
        panel1.setLayout(new GridLayout(2 + nameHolidayMapNoOffDay.size(), 1));
        List<JLabel> holidayLabelList = new ArrayList<>();
        holidayLabelList.add(new JLabel("最近的假期："));
        holidayLabelList.add(new JLabel("周末：" + baseDate.getNextWeekend().toString()));
        for (Holiday holiday : nameHolidayMapNoOffDay.values()) {
            holidayLabelList.add(new JLabel(holiday.getName() + "：" + holiday.getDate().toString()));
        }
        for (JLabel jLabel : holidayLabelList) {
            panel1.add(jLabel);
        }
        return panel1;
    }

    /**
     * 初始化节假日数：今年的节假日集合holidayList，名称-节假日map（不含补班日） nameHolidayMapNoOffDay
     *
     * @param now 现在日期
     */
    private void initHolidayData(LocalDate now) {
        baseDate.setNow(now);
        baseDate.setNextWeekend(now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)));

        JSONArray holidayOfYearJson = getHolidayOfYear(String.valueOf(now.getYear()));
        List<Holiday> holidays = holidayOfYearJson.toJavaList(Holiday.class);
        if (now.getMonthValue() >= DateTimeUtil.NOVEMBER) {
            JSONArray holidayOfNextYearJson = getHolidayOfYear(String.valueOf(now.getYear() + 1));
            List<Holiday> nextYearHolidays = holidayOfNextYearJson.toJavaList(Holiday.class);
            if (nextYearHolidays != null && nextYearHolidays.size() > 0) {
                holidays.addAll(nextYearHolidays);
            }
        }
        baseDate.setHolidayList(holidays);

        baseDate.setNotOffHolidayDateList(holidays.stream()
                .filter(holiday -> {
                    int holidayDayOfWeekValue = holiday.getDate().getDayOfWeek().getValue();
                    return (holidayDayOfWeekValue == PaydayUtil.SATURDAY_VALUE
                            || holidayDayOfWeekValue == PaydayUtil.SUNDAY_VALUE)
                            && !holiday.getOffDay();
                })
                .map(Holiday::getDate)
                .collect(Collectors.toList()));

        baseDate.setHolidayDateList(holidays.stream()
                .filter(Holiday::getOffDay)
                .map(Holiday::getDate)
                .collect(Collectors.toList()));

        Map<String, List<Holiday>> nameHolidayListMap = holidays.stream()
                .filter(holiday -> !holiday.getOffDay())
                .collect(Collectors.groupingBy(holiday -> holiday.getName() + "(" + holiday.getDate().getYear() + ")"));
        Map<String, Holiday> nameHolidayMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<Holiday>> nameHolidayEntry : nameHolidayListMap.entrySet()) {
            String holidayName = nameHolidayEntry.getKey();
            List<Holiday> holidayListByName = nameHolidayEntry.getValue();
            Holiday holidayBegin = holidayListByName.stream()
                    .min(Comparator.comparing(Holiday::getDate))
                    .orElse(new Holiday());
            Holiday holiday = nameHolidayMap.get(holidayName);
            if (holiday == null) {
                holiday = holidayBegin;
                if (holiday.getDate() != null && holiday.getDate().compareTo(now) > 0) {
                    nameHolidayMap.put(holidayName, holiday);
                }
            }
        }
        Map<String, Holiday> resultMap = new LinkedHashMap<>();
        nameHolidayMap.entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getValue().getDate()))
                .forEach(entry -> resultMap.put(entry.getKey(), entry.getValue()));
        baseDate.setNameHolidayMapNoOffDay(resultMap);
    }

    /**
     * 获取指定年的所有节假日
     *
     * @param year 指定年
     * @return 节假日json
     */
    private static String getPath(String year) {
        // 源json地址
        // return "https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/" + year + ".json";
        // 国内镜像地址
        // return "https://natescarlet.coding.net/p/github/d/holiday-cn/git/raw/master/" + year + ".json";
        // cdn地址
        // 2023-04-04 从2022-08-05开始，要求登录才能下载开源仓库的文件。
        // return "https://cdn.jsdelivr.net/gh/NateScarlet/holiday-cn@master/" + year + ".json";
        // ghproxy 加速镜像
        return "https://ghproxy.com/https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/" + year + ".json";
    }

    /**
     * 获取指定年份的节假日信息
     *
     * @param year 年份 如："2022"
     * @return 节假日json
     */
    public static JSONArray getHolidayOfYear(String year) {
        // 获取指定年份的url
        String url = getPath(year);
        // 获取返回结果
        String json = get(url);
        // 解析返回结果
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getJSONArray("days");
    }

    /**
     * 设置尝试次数
     *
     * @param url 获取节假日地址
     * @return 节假日json
     */
    public static String get(String url) {
        return get(url, 0);
    }

    /**
     * 请求第三方接口的方法
     *
     * @param url 请求的url
     * @return 节假日json
     */
    public static String get(String url, int i) {
        // 请求url
        URL getUrl;
        // 连接
        HttpURLConnection connection = null;
        // 输入流
        BufferedReader reader = null;
        // 返回结果
        StringBuilder lines = new StringBuilder();
        try {
            // 初始化url
            getUrl = new URL(url);
            // 获取url的连接
            connection = (HttpURLConnection) getUrl.openConnection();
            // 发起连接
            connection.connect();
            // 获取输入流
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            // 读取返回结果
            String line;
            // 读取每一行
            while ((line = reader.readLine()) != null) {
                // 拼接返回结果
                lines.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 如果请求失败, 尝试重新请求
            if (i < RETRY_TIME) {
                i++;
                try {
                    System.out.println("第" + i + "次获取失败, 尝试重新请求");
                    Thread.sleep(3000);
                    return get(url, i);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("获取失败, 请检查网络或稍后重试");
            }
        } finally {
            // 在finally中关闭资源
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return lines.toString();
    }

    public BaseDateBO getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(BaseDateBO baseDate) {
        this.baseDate = baseDate;
    }
}
