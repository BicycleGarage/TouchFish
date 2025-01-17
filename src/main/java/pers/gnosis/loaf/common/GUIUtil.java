package pers.gnosis.loaf.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GUIUtil {
    /**
     * 生成统一样式JPanel
     * 使用GridLayout
     * @param gridLayoutRowCount GridLayout行数
     * @return JPanel
     */
    public static JPanel getMyjPanelSingleColumn(int gridLayoutRowCount, boolean isChild) {
        return getMyjPanel(gridLayoutRowCount, 1, isChild);
    }

    /**
     * 生成统一样式JPanel
     * 使用GridLayout
     * @param gridLayoutRowCount GridLayout行数
     * @param gridLayoutColumnCount GridLayout列数
     * @return JPanel
     */
    public static JPanel getMyjPanel(int gridLayoutRowCount, int gridLayoutColumnCount, boolean isChild) {
        JPanel panel = new JPanel();
        setBorder(isChild, panel);
        panel.setLayout(new GridLayout(gridLayoutRowCount, gridLayoutColumnCount));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * 生成统一样式JPanel
     * 使用默认FlowLayout
     * @param isChild 是否为内部panel，若是，则无边距
     * @return JPanel
     */
    public static JPanel getMyjPanelFlowLayout(boolean isChild) {
        JPanel panel = new JPanel();
        FlowLayout layout = (FlowLayout) panel.getLayout();
        layout.setAlignment(FlowLayout.LEFT);
        setBorder(isChild, panel);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * 根据是否为内部panel设置统一边距
     * @param isChild 是否为内部panel，若是，则无边距
     * @param panel  panel
     */
    private static void setBorder(boolean isChild, JPanel panel) {
        if(!isChild) {
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        } else {
            panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }

    /**
     * frame内最外层panel
     * @return JPanel
     */
    public static JPanel getMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * 无边距boxlayout的panel，纵向排列，左对齐
     * @return JPanel
     */
    public static JPanel getMyPanelBoxLayout(boolean isChild) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        setBorder(isChild, panel);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }
}
