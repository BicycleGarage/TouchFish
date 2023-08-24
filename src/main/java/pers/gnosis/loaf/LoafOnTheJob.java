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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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
     * 为兼容少数用户网络环境很差，本地预存一份节假日json
     */
    public static final String YEAR_2023_HOLIDAY_JSON = "{\"$schema\":\"https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/schema.json\",\"$id\":\"https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/2023.json\",\"year\":2023,\"papers\":[\"http://www.gov.cn/zhengce/zhengceku/2022-12/08/content_5730844.htm\"],\"days\":[{\"name\":\"元旦\",\"date\":\"2022-12-31\",\"isOffDay\":true},{\"name\":\"元旦\",\"date\":\"2023-01-01\",\"isOffDay\":true},{\"name\":\"元旦\",\"date\":\"2023-01-02\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-21\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-22\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-23\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-24\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-25\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-26\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-27\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-28\",\"isOffDay\":false},{\"name\":\"春节\",\"date\":\"2023-01-29\",\"isOffDay\":false},{\"name\":\"清明节\",\"date\":\"2023-04-05\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-04-23\",\"isOffDay\":false},{\"name\":\"劳动节\",\"date\":\"2023-04-29\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-04-30\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-05-01\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-05-02\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-05-03\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-05-06\",\"isOffDay\":false},{\"name\":\"端午节\",\"date\":\"2023-06-22\",\"isOffDay\":true},{\"name\":\"端午节\",\"date\":\"2023-06-23\",\"isOffDay\":true},{\"name\":\"端午节\",\"date\":\"2023-06-24\",\"isOffDay\":true},{\"name\":\"端午节\",\"date\":\"2023-06-25\",\"isOffDay\":false},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-09-29\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-09-30\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-01\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-02\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-03\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-04\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-05\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-06\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-07\",\"isOffDay\":false},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-08\",\"isOffDay\":false}]}";
    public static final String YEAR_2024_HOLIDAY_JSON = "";
    public static final String HOLIDAY_JSON_PREFIX = "YEAR_";
    public static final String HOLIDAY_JSON_SUFFIX = "_HOLIDAY_JSON";

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

        int height = baseDate.getNameHolidayMapNoOffDay().size() >= 5 ? 1050 : 600;
        JFrame jf = new JFrame("摸鱼小助手");
        jf.setPreferredSize(new Dimension(400, height));
        jf.setMinimumSize(new Dimension(400, height));
        jf.setLayout(new FlowLayout(FlowLayout.LEFT));


        // 记录鼠标按下时的坐标
        final int[] xOld = new int[1];
        final int[] yOld = new int[1];
        jf.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                xOld[0] = e.getX();
                yOld[0] = e.getY();
            }
        });
        jf.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int xOnScreen = e.getXOnScreen();
                int yOnScreen = e.getYOnScreen();
                int xx = xOnScreen - xOld[0];
                int yy = yOnScreen - yOld[0];
                jf.setLocation(xx, yy);
            }
        });

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

    private JPanel getOpacitySliderPanel(JFrame jFrame) {
        // 设置窗口无装饰，否则无法设置背景颜色
        jFrame.setUndecorated(true);
        jFrame.setOpacity(0.8F);

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
            jFrame.setOpacity(valueFloat / 100);
        });
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(0, 5, 0, 0));
        panel.add(new JLabel("透明度："));
        panel.add(slider);
        return panel;
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
    public JSONArray getHolidayOfYear(String year){
        String json;
        // 先获取程序预存
        String currentYearHolidayJsonFieldName = HOLIDAY_JSON_PREFIX + year + HOLIDAY_JSON_SUFFIX;
        Class<? extends LoafOnTheJob> aClass = this.getClass();
        Field declaredField = null;
        try {
            declaredField = aClass.getDeclaredField(currentYearHolidayJsonFieldName);
            declaredField.setAccessible(true);
            String currentYearHolidayJson = (String) declaredField.get(this);
            if(currentYearHolidayJson != null && !"".equals(currentYearHolidayJson)) {
                // 存在当前年份对应的程序内节假日json数据，直接使用
                json = currentYearHolidayJson;
            } else {
                // 不存在当前年份对应的程序内节假日json数据，从网络获取
                // 获取指定年份的url
                String url = getPath(year);
                // 获取返回结果
                json = get(url);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ignored) {
            // NoSuchFieldException表示没有当年对应的节假日json变量，要在代码中补充
            // SecurityException通常不会出现，仅在项目中的类包名与Java已有库的包名重复才会报错
            // IllegalAccessException不会出现，已经declaredField.setAccessible(true);

            // 出现异常，则从网络获取节假日
            json = get(getPath(year));
        }
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
