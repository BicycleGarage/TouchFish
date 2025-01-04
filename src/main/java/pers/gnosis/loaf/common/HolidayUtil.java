package pers.gnosis.loaf.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import pers.gnosis.loaf.Holiday;
import pers.gnosis.loaf.LoafOnTheJob;
import pers.gnosis.loaf.pojo.bo.BaseDateBO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 初始化节假日数据的工具类<br />
 * 从网络获取节假日数据，进行解析、封装成节假日对象；<br />
 * 将节假日对象初始化成程序所需的各个细分数据。
 * @author wangsiye
 */
@Slf4j
public class HolidayUtil {

    /**
     * 网络连接重试次数
     */
    public static final int RETRY_TIME = 2;
    public static final String HOLIDAY_JSON_SUFFIX = "_Holiday.json";

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

        JSONArray holidayOfYearJson = getHolidayOfYear(String.valueOf(now.getYear()));
        List<Holiday> holidays = holidayOfYearJson.toJavaList(Holiday.class);
        if (now.getMonthValue() >= DateTimeUtil.NOVEMBER) {
            JSONArray holidayOfNextYearJson = getHolidayOfYear(String.valueOf(now.getYear() + 1));
            List<Holiday> nextYearHolidays = holidayOfNextYearJson.toJavaList(Holiday.class);
            if (nextYearHolidays != null && !nextYearHolidays.isEmpty()) {
                holidays.addAll(nextYearHolidays);
            }
        }
        baseDate.setHolidayList(holidays);

        baseDate.setNotOffHolidayDateList(holidays.stream()
                .filter(holiday -> !holiday.getIsOffDay())
                .map(Holiday::getDate)
                .collect(Collectors.toList()));

        baseDate.setHolidayDateList(holidays.stream()
                .filter(Holiday::getIsOffDay)
                .map(Holiday::getDate)
                .collect(Collectors.toList()));

        Map<String, List<Holiday>> nameHolidayListMap = holidays.stream()
                .filter(Holiday::getIsOffDay)
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
                if (holiday.getDate() != null && holiday.getDate().isAfter(now)) {
                    nameHolidayMap.put(holidayName, holiday);
                }
            }
        }
        Map<String, Holiday> resultMap = new LinkedHashMap<>();
        nameHolidayMap.entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getValue().getDate()))
                .forEach(entry -> resultMap.put(entry.getKey(), entry.getValue()));
        baseDate.setNameHolidayMapNoOffDay(resultMap);

        baseDate.setAdvancePayday(false);

        baseDate.setPaydayMap(PaydayUtil.initPaydayMap());
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
//        return "https://ghproxy.com/https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/" + year + ".json";
        // 2025-01-03 改用自己的腾讯云对象存储
        return "https://vuper-1303948677.cos.ap-guangzhou.myqcloud.com/" + year + "_Holiday.json";
    }

    /**
     * 获取指定年份的节假日信息
     *
     * @param year 年份 如："2022"
     * @return 节假日json
     */
    public static JSONArray getHolidayOfYear(String year){
        String json;
        // 先获取程序预存
        try {
            // 读取 JSON 文件内容为字符串
            String fileName = year + HolidayUtil.HOLIDAY_JSON_SUFFIX;
            json = ResourceFileReader.readFileAsString(fileName);
        } catch (Exception e) {
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
            log.error("发生异常：{}", e.getMessage(), e);
            // 如果请求失败, 尝试重新请求
            if (i < RETRY_TIME) {
                i++;
                try {
                    System.out.println("第" + i + "次获取失败, 尝试重新请求");
                    Thread.sleep(3000);
                    return get(url, i);
                } catch (InterruptedException ex) {
                    log.error("发生异常：{}", ex.getMessage(), ex);
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
                    log.error("发生异常：{}", e.getMessage(), e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return lines.toString();
    }
}
