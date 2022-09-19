import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TouchFish {

    private final static String HOLIDAY_PATTERN = "%-3s";
    private final static String TITLE_PATTERN = "%-4s";
    private final static String CONTEXT_PATTERN = "%-12s";
    public static final Color COLOR_RED = new Color(245, 74, 69);

    public static void main(String[] args) {
        LocalDate now = LocalDate.now();

        Map<String, Holiday> nameHolidayMap = getNameHolidayMap(now);
        LocalDate nextWeekend = now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

//        printHolidays(nameHolidayMap, nextWeekend);
//        printHolidayLeftDays(now, nameHolidayMap, nextWeekend);

        JFrame jf = new JFrame("摸鱼小助手");
        jf.setPreferredSize(new Dimension(400, 200));
        jf.setMinimumSize(new Dimension(400, 200));
        jf.setLayout(new BorderLayout());

        JPanel panel1 = getHolidayPanel(nameHolidayMap, nextWeekend);
        jf.add(panel1, BorderLayout.NORTH);

        JPanel panel2 = getNoticePanel(now);
        jf.add(panel2, BorderLayout.CENTER);

        JPanel panel3 = getLeftDayPanel(now, nameHolidayMap, nextWeekend);
        jf.add(panel3, BorderLayout.SOUTH);

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
     * 获取假期剩余天数
     * @param now 现在时间
     * @param nameHolidayMap 今年的剩余假期
     * @param nextWeekend 周末
     * @return 剩余天数JPanel
     */
    private static JPanel getLeftDayPanel(LocalDate now, Map<String, Holiday> nameHolidayMap, LocalDate nextWeekend) {
        JPanel panel3 = new JPanel();
        panel3.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel3.setLayout(new GridLayout(1 + nameHolidayMap.size(),1));
        List<JLabel> holidayLeftDaysLabelList = new ArrayList<>();
        holidayLeftDaysLabelList.add(new JLabel("距离本周周末还有：" + now.until(nextWeekend, ChronoUnit.DAYS) + "天"));
        for (Holiday holiday : nameHolidayMap.values()) {
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
     * @param now 现在时间
     * @return 提示信息JPanel
     */
    private static JPanel getNoticePanel(LocalDate now) {
        JPanel panel2 = new JPanel();
        panel2.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel2.setLayout(new GridLayout(5,1));
        List<JLabel> noticeText = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日");
        noticeText.add(new JLabel(formatter.format(now) + getPeriod()+ "，摸鱼人"));
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
     * @param nameHolidayMap 今年的剩余假期
     * @param nextWeekend 周末
     * @return 最近假期JPanel
     */
    private static JPanel getHolidayPanel(Map<String, Holiday> nameHolidayMap, LocalDate nextWeekend) {
        JPanel panel1 = new JPanel();
        panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel1.setLayout(new GridLayout(2 + nameHolidayMap.size(),1));
        List<JLabel> holidayLabelList = new ArrayList<>();
        holidayLabelList.add(new JLabel("最近的假期："));
        holidayLabelList.add(new JLabel("周末：" + nextWeekend.toString()));
        for (Holiday holiday : nameHolidayMap.values()) {
            holidayLabelList.add(new JLabel(holiday.getName() + "：" + holiday.getDate().toString()));
        }
        for (JLabel jLabel : holidayLabelList) {
            panel1.add(jLabel);
        }
        return panel1;
    }

    /**
     * 获取今年的假期map
     * @param now 现在日期
     * @return 今年的假期map
     */
    private static Map<String, Holiday> getNameHolidayMap(LocalDate now) {
        JSONArray holidayOfYearJson = getHolidayOfYear(String.valueOf(now.getYear()));
        List<Holiday> holidays = holidayOfYearJson.toJavaList(Holiday.class);
        Map<String, List<Holiday>> nameHolidayListMap = holidays.stream().collect(Collectors.groupingBy(Holiday::getName));
        Map<String, Holiday> nameHolidayMap = new HashMap<>();
        for (Map.Entry<String, List<Holiday>> nameHolidayEntry : nameHolidayListMap.entrySet()) {
            String holidayName = nameHolidayEntry.getKey();
            List<Holiday> holidayListByName = nameHolidayEntry.getValue();
            Holiday holidayBegin = holidayListByName.stream()
                    .min(Comparator.comparing(Holiday::getDate))
                    .orElse(new Holiday());
            Holiday holiday = nameHolidayMap.get(holidayName);
            if(holiday == null) {
                holiday = holidayBegin;
                if(holiday.getDate() != null && holiday.getDate().compareTo(now) > 0) {
                    nameHolidayMap.put(holidayName, holiday);
                }
            }
        }
        return nameHolidayMap;
    }

    /**
     * 输出今年剩余假期
     * @param nameHolidayMap 今年的假期map
     * @param nextWeekend 本周末
     */
    private static void printHolidays(Map<String, Holiday> nameHolidayMap, LocalDate nextWeekend) {
        System.out.println("今年剩余的假期：");
        printHoliday("周末：", nextWeekend.toString());
        for (Holiday holiday : nameHolidayMap.values()) {
            printHoliday(holiday.getName() + "：", holiday.getDate().toString());
        }
        System.out.println();
    }

    /**
     * 输出距离各假日还有几天
     * @param now 现在日期
     * @param nameHolidayMap 今年的假期map
     * @param nextWeekend 本周末
     */
    private static void printHolidayLeftDays(LocalDate now, Map<String, Holiday> nameHolidayMap, LocalDate nextWeekend) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日");
        System.out.println(formatter.format(now) + getPeriod()+ "，摸鱼人，我是本群摸鱼小助手，下面是摸鱼提醒");
        System.out.println("工作再累，一定不要忘记摸鱼哦");
        System.out.println("有事没事，起身去茶水间去厕所去廊道走走，别老在工位上坐着，钱是老板的，但命是自己的");
        printLeftDays("本周周末",
                String.valueOf(now.until(nextWeekend, ChronoUnit.DAYS)));
        for (Holiday holiday : nameHolidayMap.values()) {
            printLeftDays(holiday.getName() + "假期",
                    String.valueOf(now.until(holiday.getDate(), ChronoUnit.DAYS)));
        }
    }

    private static void printHoliday(String title, String context){
        System.out.printf(TouchFish.HOLIDAY_PATTERN, title);
        System.out.print(context);
        System.out.println();
    }

    private static void printLeftDays(String title, String days){
        System.out.printf(TouchFish.CONTEXT_PATTERN, "距离" +title+ "还有");
        System.out.printf(TouchFish.TITLE_PATTERN, days);
        System.out.print("天");
        System.out.println();
    }

    private static String getPeriod(){
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        if(hour < 3){
            return "夜深了";
        }
        if(hour < 5){
            return "天亮了";
        }
        if(hour < 9){
            return "早上好";
        }
        if(hour < 12){
            return "上午好";
        }
        if(hour < 14){
            return "中午好";
        }
        if(hour < 18){
            return "下午好";
        }
        if(hour < 23){
            return "晚上好";
        }
        return "";
    }

    /**
     * 获取指定年的所有节假日
     * @param year 指定年
     * @return 节假日json
     */
    private static String getPath(String year) {
        // 源json地址
        // return "https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/" + year + ".json";
        // 国内镜像地址
        // return "https://natescarlet.coding.net/p/github/d/holiday-cn/git/raw/master/" + year + ".json";
        // cdn地址
        return "https://cdn.jsdelivr.net/gh/NateScarlet/holiday-cn@master/" + year + ".json";
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
            if (i < 2) {
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
