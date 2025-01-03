package pers.gnosis.loaf.common;

import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;

/**
 * @author wangsiye
 */
public class NumberTextField extends PlainDocument {
    public NumberTextField() {
        super();
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr)
            throws javax.swing.text.BadLocationException {
        if (str == null) {
            return;
        }

        char[] s = str.toCharArray();
        StringBuilder numericString = new StringBuilder();
        for (char c : s) {
            if (Character.isDigit(c)) {
                numericString.append(c);
            }
        }

        if (numericString.length() > 0) {
            super.insertString(offset, numericString.toString(), attr);
        }
    }
}
