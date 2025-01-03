package pers.gnosis.loaf.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ResourceFileReader {

    /**
     * 从 resources 目录中读取文件内容为字符串
     *
     * @param fileName 文件名，相对于 resources 的路径
     * @return 文件内容字符串
     * @throws IllegalArgumentException 如果文件未找到或读取失败
     */
    public static String readFileAsString(String fileName) {
        try (InputStream inputStream = getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new IllegalArgumentException("读取文件失败: " + fileName, e);
        }
    }

    /**
     * 从 resources 目录中获取文件的 InputStream
     *
     * @param fileName 文件名，相对于 resources 的路径
     * @return InputStream
     * @throws IllegalArgumentException 如果文件未找到
     */
    public static InputStream getResourceAsStream(String fileName) {
        InputStream inputStream = ResourceFileReader.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("文件未找到: " + fileName);
        }
        return inputStream;
    }
}
