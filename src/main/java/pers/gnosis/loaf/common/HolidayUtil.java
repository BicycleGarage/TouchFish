package pers.gnosis.loaf.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import pers.gnosis.loaf.Holiday;
import pers.gnosis.loaf.LoafOnTheJob;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 初始化节假日数据的工具类<br />
 * 从网络获取节假日数据，进行解析、封装成节假日对象；<br />
 * 将节假日对象初始化成程序所需的各个细分数据。
 * @author wangsiye
 */
public class HolidayUtil {

    /**
     * 为兼容少数用户网络环境很差，本地预存一份节假日json
     */
    public static final String YEAR_2023_HOLIDAY_JSON = "{\"$schema\":\"https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/schema.json\",\"$id\":\"https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/2023.json\",\"year\":2023,\"papers\":[\"http://www.gov.cn/zhengce/zhengceku/2022-12/08/content_5730844.htm\"],\"days\":[{\"name\":\"元旦\",\"date\":\"2022-12-31\",\"isOffDay\":true},{\"name\":\"元旦\",\"date\":\"2023-01-01\",\"isOffDay\":true},{\"name\":\"元旦\",\"date\":\"2023-01-02\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-21\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-22\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-23\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-24\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-25\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-26\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-27\",\"isOffDay\":true},{\"name\":\"春节\",\"date\":\"2023-01-28\",\"isOffDay\":false},{\"name\":\"春节\",\"date\":\"2023-01-29\",\"isOffDay\":false},{\"name\":\"清明节\",\"date\":\"2023-04-05\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-04-23\",\"isOffDay\":false},{\"name\":\"劳动节\",\"date\":\"2023-04-29\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-04-30\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-05-01\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-05-02\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-05-03\",\"isOffDay\":true},{\"name\":\"劳动节\",\"date\":\"2023-05-06\",\"isOffDay\":false},{\"name\":\"端午节\",\"date\":\"2023-06-22\",\"isOffDay\":true},{\"name\":\"端午节\",\"date\":\"2023-06-23\",\"isOffDay\":true},{\"name\":\"端午节\",\"date\":\"2023-06-24\",\"isOffDay\":true},{\"name\":\"端午节\",\"date\":\"2023-06-25\",\"isOffDay\":false},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-09-29\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-09-30\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-01\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-02\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-03\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-04\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-05\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-06\",\"isOffDay\":true},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-07\",\"isOffDay\":false},{\"name\":\"中秋节、国庆节\",\"date\":\"2023-10-08\",\"isOffDay\":false}]}";
    public static final String YEAR_2024_HOLIDAY_JSON = "";
    public static final String HOLIDAY_JSON_PREFIX = "YEAR_";
    public static final String HOLIDAY_JSON_SUFFIX = "_HOLIDAY_JSON";
    /**
     * 网络连接重试次数
     */
    public static final int RETRY_TIME = 2;

    /**
     * 初始化节假日数：今年的节假日集合holidayList，名称-节假日map（不含补班日） nameHolidayMapNoOffDay
     *
     * @param now 现在日期
     * @param loafOnTheJob 待初始化的程序主体对象
     */
    public static void initHolidayData(LocalDate now, LoafOnTheJob loafOnTheJob) {
        BaseDateBO baseDate = loafOnTheJob.getBaseDate();

        baseDate.setNow(now);
        baseDate.setNextSaturday(now.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)));
        baseDate.setNextSunday(now.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)));

        JSONArray holidayOfYearJson = getHolidayOfYear(String.valueOf(now.getYear()), loafOnTheJob);
        List<Holiday> holidays = holidayOfYearJson.toJavaList(Holiday.class);
        if (now.getMonthValue() >= DateTimeUtil.NOVEMBER) {
            JSONArray holidayOfNextYearJson = getHolidayOfYear(String.valueOf(now.getYear() + 1), loafOnTheJob);
            List<Holiday> nextYearHolidays = holidayOfNextYearJson.toJavaList(Holiday.class);
            if (nextYearHolidays != null && nextYearHolidays.size() > 0) {
                holidays.addAll(nextYearHolidays);
            }
        }
        baseDate.setHolidayList(holidays);

        baseDate.setNotOffHolidayDateList(holidays.stream()
                .filter(holiday -> !holiday.getOffDay())
                .map(Holiday::getDate)
                .collect(Collectors.toList()));

        baseDate.setHolidayDateList(holidays.stream()
                .filter(Holiday::getOffDay)
                .map(Holiday::getDate)
                .collect(Collectors.toList()));

        Map<String, List<Holiday>> nameHolidayListMap = holidays.stream()
                .filter(Holiday::getOffDay)
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
     * @param loafOnTheJob
     * @return 节假日json
     */
    public static JSONArray getHolidayOfYear(String year, LoafOnTheJob loafOnTheJob){
        String json;
        // 先获取程序预存
        String currentYearHolidayJsonFieldName = HOLIDAY_JSON_PREFIX + year + HOLIDAY_JSON_SUFFIX;
        Class<? extends LoafOnTheJob> aClass = loafOnTheJob.getClass();
        Field declaredField = null;
        try {
            declaredField = aClass.getDeclaredField(currentYearHolidayJsonFieldName);
            declaredField.setAccessible(true);
            String currentYearHolidayJson = (String) declaredField.get(loafOnTheJob);
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
     * 首次初始尝试获取节假日
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
     * @param i 已重试的次数
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
}
