package pers.gnosis.loaf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import pers.gnosis.loaf.common.CustomerPaydayButtonActionListener;
import pers.gnosis.loaf.common.NumberTextField;
import pers.gnosis.loaf.common.Utils;

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
public class TouchFish {

    /**
     * 字体标红色号
     */
    public static final Color COLOR_RED = new Color(245, 74, 69);
    /**
     * 首次发薪日
     */
    public static final int FIRST_PAYDAY = 10;
    /**
     * 二次发薪日
     */
    public static final int SECOND_PAYDAY = 15;
    /**
     * 十一月
     */
    public static final int NOVEMBER = 11;
    /**
     * 3点钟
     */
    public static final int THREE_O_CLOCK = 3;
    /**
     * 5点钟
     */
    public static final int FIVE_O_CLOCK = 5;
    /**
     * 9点钟
     */
    public static final int NINE_O_CLOCK = 9;
    /**
     * 12点钟
     */
    public static final int TWELVE_O_CLOCK = 12;
    /**
     * 14点钟
     */
    public static final int FOURTEEN_O_CLOCK = 14;
    /**
     * 18点钟
     */
    public static final int EIGHTEEN_O_CLOCK = 18;
    /**
     * 23点钟
     */
    public static final int TWENTY_TREE_O_CLOCK = 23;
    /**
     * 网络连接重试次数
     */
    public static final int RETRY_TIME = 2;

    /**
     * 今年节假日，同时包含放假、补班日（如周六日本应双休，但由于调休机制需要补班，其isOffDay=false）
     */
    public static List<Holiday> holidayList;
    /**
     * 名称-假日map：key=节假日名称，value=非补班日的此假期的第一天
     */
    public static Map<String, Holiday> nameHolidayMapNoOffDay;
    /**
     * 补班的周六日日期
     */
    public static List<LocalDate> notOffHolidayDateList;
    /**
     * 放假且不补班的假期日期
     */
    public static List<LocalDate> holidayDateList;

    public static void main(String[] args) {
        LocalDate now = LocalDate.now();

        initHolidayData(now);
        LocalDate nextWeekend = now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

        int height = nameHolidayMapNoOffDay.size() >= 5 ? 1050 : 600;
        JFrame jf = new JFrame("摸鱼小助手");
        jf.setPreferredSize(new Dimension(400, height));
        jf.setMinimumSize(new Dimension(400, height));
        jf.setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel panel1 = getHolidayPanel(nextWeekend);
        jf.add(panel1);

        JPanel panel2 = getNoticePanel(now);
        jf.add(panel2);

        JPanel paydayPanel = getPayday(now);
        jf.add(paydayPanel);

        JPanel panel3 = getLeftDayPanel(now, nextWeekend);
        jf.add(panel3);

        JPanel customerPaydayPanel = getCustomerPaydayPanel(now);
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
     * @param now 现在时间
     * @return 获取发薪日剩余天数JPanel
     */
    private static JPanel getPayday(LocalDate now) {
        List<LocalDate> paydayList = doGetPayday(now);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setLayout(new GridLayout(1 + paydayList.size() + 1, 1));
        List<JLabel> labelList = Utils.getDaysToPaydayLabel(now, paydayList);
        for (JLabel jLabel : labelList) {
            panel.add(jLabel);
        }

        return panel;
    }

    private static JPanel getCustomerPaydayPanel(LocalDate now) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        JPanel inputPaydayPanel = new JPanel();
        JPanel daysToPaydayPanel = new JPanel();

        JLabel customerPaydayLabel = new JLabel("自定义发薪日：");

        JTextField customerPaydayTextField = new JTextField(4);
        customerPaydayTextField.setDocument(new NumberTextField());

        JButton customerPaydayButton = new JButton("确定");
        customerPaydayButton.addActionListener(new CustomerPaydayButtonActionListener(
                daysToPaydayPanel, customerPaydayTextField, now, notOffHolidayDateList, holidayDateList));

        inputPaydayPanel.add(customerPaydayLabel);
        inputPaydayPanel.add(customerPaydayTextField);
        inputPaydayPanel.add(customerPaydayButton);

        panel.add(inputPaydayPanel);
        panel.add(daysToPaydayPanel);


        return panel;
    }

    /**
     * 获取发薪日<br />
     * 发薪日：当月、下月的10、15日，若发薪日为周六日、节假日，则提前至最近的工作日
     *
     * @param now 现在时间
     * @return 发薪日集合：固定4个，本月和下月的10、15日（若发薪日为周六日、节假日，则提前至最近的工作日）
     */
    private static List<LocalDate> doGetPayday(LocalDate now) {
        LocalDate currentMonthFirstPayday = Utils.getWorkDayPayday(
                now, FIRST_PAYDAY, notOffHolidayDateList, holidayDateList);
        LocalDate currentMonthSecondPayday = Utils.getWorkDayPayday(
                now, SECOND_PAYDAY, notOffHolidayDateList, holidayDateList);

        LocalDate nextMonthNow = now.plusMonths(1L);
        LocalDate nextMonthFirstPayday = Utils.getWorkDayPayday(
                nextMonthNow, FIRST_PAYDAY, notOffHolidayDateList, holidayDateList);
        LocalDate nextMonthSecondPayday = Utils.getWorkDayPayday(
                nextMonthNow, SECOND_PAYDAY, notOffHolidayDateList, holidayDateList);

        return Arrays.asList(currentMonthFirstPayday, currentMonthSecondPayday,
                nextMonthFirstPayday, nextMonthSecondPayday);
    }

    /**
     * 获取假期剩余天数
     *
     * @param now         现在时间
     * @param nextWeekend 周末
     * @return 剩余天数JPanel
     */
    private static JPanel getLeftDayPanel(LocalDate now, LocalDate nextWeekend) {
        JPanel panel3 = new JPanel();
        panel3.setBorder(new EmptyBorder(10, 10, 10, 10));
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
     * @param now 现在时间
     * @return 提示信息JPanel
     */
    private static JPanel getNoticePanel(LocalDate now) {
        JPanel panel2 = new JPanel();
        panel2.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel2.setLayout(new GridLayout(5, 1));
        List<JLabel> noticeText = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日");
        noticeText.add(new JLabel(formatter.format(now) + getPeriod() + "，摸鱼人"));
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
     * @param nextWeekend 周末
     * @return 最近假期JPanel
     */
    private static JPanel getHolidayPanel(LocalDate nextWeekend) {
        JPanel panel1 = new JPanel();
        panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel1.setLayout(new GridLayout(2 + nameHolidayMapNoOffDay.size(), 1));
        List<JLabel> holidayLabelList = new ArrayList<>();
        holidayLabelList.add(new JLabel("最近的假期："));
        holidayLabelList.add(new JLabel("周末：" + nextWeekend.toString()));
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
    private static void initHolidayData(LocalDate now) {
        JSONArray holidayOfYearJson = getHolidayOfYear(String.valueOf(now.getYear()));
        List<Holiday> holidays = holidayOfYearJson.toJavaList(Holiday.class);
        if (now.getMonthValue() >= NOVEMBER) {
            JSONArray holidayOfNextYearJson = getHolidayOfYear(String.valueOf(now.getYear() + 1));
            List<Holiday> nextYearHolidays = holidayOfNextYearJson.toJavaList(Holiday.class);
            if (nextYearHolidays != null && nextYearHolidays.size() > 0) {
                holidays.addAll(nextYearHolidays);
            }
        }
        holidayList = holidays;

        notOffHolidayDateList = holidays.stream()
                .filter(holiday -> {
                    int holidayDayOfWeekValue = holiday.getDate().getDayOfWeek().getValue();
                    return (holidayDayOfWeekValue == Utils.SATURDAY_VALUE
                            || holidayDayOfWeekValue == Utils.SUNDAY_VALUE)
                            && !holiday.getOffDay();
                })
                .map(Holiday::getDate)
                .collect(Collectors.toList());

        holidayDateList = holidays.stream()
                .filter(Holiday::getOffDay)
                .map(Holiday::getDate)
                .collect(Collectors.toList());

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
        nameHolidayMapNoOffDay = resultMap;
    }


    private static String getPeriod() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if (hour < THREE_O_CLOCK) {
            return "夜深了";
        }
        if (hour < FIVE_O_CLOCK) {
            return "天亮了";
        }
        if (hour < NINE_O_CLOCK) {
            return "早上好";
        }
        if (hour < TWELVE_O_CLOCK) {
            return "上午好";
        }
        if (hour < FOURTEEN_O_CLOCK) {
            return "中午好";
        }
        if (hour < EIGHTEEN_O_CLOCK) {
            return "下午好";
        }
        if (hour < TWENTY_TREE_O_CLOCK) {
            return "晚上好";
        }
        return "";
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
        URL getUrl = null;
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
            String line = "";
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
}
