package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Like {@link SplitVariableValue}, except that the string representation is
 * text.
 * <br><br>
 * Most attributes of {@link SplitVariableValue} are inherited.
 * <br><br>
 * Specific attributes for this class are:
 * <ul>
 * <li>
 * A {@code match} attribute (which must be a {@code regular expression}) can be
 * used to impose constraints on entered text.
 * </li>
 * <li>
 * A {@code termByteStr} attribute can be used to change the default string
 * terminator byte value. Valid values are 0-255 or "" to specify no terminator
 * byte. The default is "0" (a null byte).
 * </li>
 * <li>
 * A {@code padByteStr} attribute can be used to change the default string
 * padding byte value. Valid values are 0-255 or "" to specify no pad byte. The
 * default is "0" (a null byte).
 * </li>
 * <li>
 * A {@code charSet} attribute can be used to change the character set used to
 * encode or decode the text string. Valid values are any Java-supported
 * {@link java.nio.charset.Charset} name. If not specified, the default
 * character set of this Java virtual machine is used.
 * </li>
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2004, 2013, 2014
 * @author Dave Heap Copyright (C) 2016
 */
public class SplitTextVariableValue extends SplitVariableValue {

    public static final String NO_TERM_BYTE = "";
    public static final String NO_PAD_BYTE = "";

    String matchRegex;
    String termByteStr;
    String padByteStr;
    String charSet;
    Byte termByteVal;
    Byte padByteVal;
    int atest;

    public SplitTextVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
    }

    @Override
    public void stepOneActions(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        atest = 77;
        matchRegex = extra1;
        termByteStr = extra2;
        padByteStr = extra3;
        charSet = extra4;
        if (!termByteStr.equals(NO_TERM_BYTE)) {
            termByteVal = (byte) Integer.parseUnsignedInt(termByteStr);
        }
        if (!padByteStr.equals(NO_PAD_BYTE)) {
            padByteVal = (byte) Integer.parseUnsignedInt(padByteStr);
        }
        log.debug("stepOneActions");
        log.debug("atest=" + atest);
        log.debug("termByteStr=\"" + termByteStr + "\",padByteStr=\"" + padByteStr + "\"");
        log.debug("termByteVal=" + termByteVal + ",padByteVal=" + padByteVal);
    }

    @Override
    public void stepTwoActions() {
        log.debug("stepTwoActions");
        log.debug("atest=" + atest);
        log.debug("termByteStr=\"" + termByteStr + "\",padByteStr=\"" + padByteStr + "\"");
        log.debug("termByteVal=" + termByteVal + ",padByteVal=" + padByteVal);
        _columns = cvCount + 2; //update column width now we have a better idea
    }

    boolean isMatched(String s) {
        if (matchRegex != null && !matchRegex.equals("")) {
            return s.matches(matchRegex);
        } else {
            return true;
        }
    }

    byte[] getBytesFromText(String s) {
        byte[] ret = {};
//        log.debug("defaultCharset()=" + defaultCharset().name());
//        log.debug("displayName()=" + defaultCharset().displayName());
//        log.debug("aliases()=" + defaultCharset().aliases());
        try {
            ret = s.getBytes(charSet);
        } catch (UnsupportedEncodingException ex) {
            unsupportedCharset();
        }
        return ret;
    }

    String getTextFromBytes(byte[] v) {
        String ret = "";
        int textBytesLength = v.length;
        for (int i = 0; i < v.length; i++) {
            if (!termByteStr.equals(NO_TERM_BYTE) && (v[i] == termByteVal)) {
                textBytesLength = i;
                break;
            }
        }
        if (textBytesLength > 0) {
            byte[] textBytes = new byte[textBytesLength];
            System.arraycopy(v, 0, textBytes, 0, textBytesLength);
            try {
                ret = new String(textBytes, charSet);
            } catch (UnsupportedEncodingException ex) {
                unsupportedCharset();
            }
        }
        return ret; //fall through
    }

    void unsupportedCharset() {
        synchronized (this) {
            JOptionPane.showMessageDialog(new JFrame(), Bundle.getMessage("UnsupportedCharset", charSet, _name),
                    Bundle.getMessage("DecoderDefError"), JOptionPane.ERROR_MESSAGE); // NOI18N
        }
        log.error(Bundle.getMessage("UnsupportedCharset", charSet, _name));
    }

    @Override
    int[] getCvValsFromTextField() {
//        log.debug("getCvValsFromTextField");
//        log.debug("atest=" + atest);
//        log.debug("termByteStr=\"" + termByteStr + "\",padByteStr=\"" + padByteStr + "\"");
//        log.debug("termByteVal=" + termByteVal + ",padByteVal=" + padByteVal);
        // get new bytes from string
        byte[] newEntries = getBytesFromText(_textField.getText());

        log.debug("getCvValsFromTextField>newEntries.length=" + newEntries.length);
        int[] retVals = new int[cvCount];

        // convert to UnsignedInt in retVals
        // string may be shorter, so pad to length
        for (int i = 0; i < cvCount; i++) {
            if (i < newEntries.length) {
                retVals[i] = Byte.toUnsignedInt(newEntries[i]);
            } else if ((i == newEntries.length) && !termByteStr.equals(NO_TERM_BYTE)) {
//                log.debug("terminating with " + termByteVal);
                retVals[i] = termByteVal;
            } else if (!padByteStr.equals(NO_PAD_BYTE)) {
//                log.debug("padding with " + padByteVal);
                retVals[i] = padByteVal;
            }
//            log.debug("retVals[" + i + "] set to " + retVals[i]);
        }
        return retVals;
    }

    /**
     * Contains byte-value specific code.
     * <br><br>
     * Calculates new value for _textField and invokes
     * {@link #setValue(String) setValue(newVal)} to make and notify the change
     *
     * @param intVals array of new CV values
     */
    @Override
    void updateVariableValue(int[] intVals) {

        byte[] byteVals = new byte[intVals.length];

        for (int i = 0; i < intVals.length; i++) {
            byteVals[i] = (byte) intVals[i];
        }
        String newVal = getTextFromBytes(byteVals);
        log.debug("Variable=" + _name + "; set value to '" + newVal + "';length = " + newVal.length());
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; set value to " + newVal);
        }
        log.debug("setValue(newVal)to {}", newVal);
        setValue(newVal);  // check for duplicate is done inside setValue
        log.debug("done setValue(newVal)to {}", newVal);
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; in property change after setValueFromString call");
        }
    }

    /**
     * Contains byte-value specific code.
     * <br><br>
     * firePropertyChange for "Value" with new and old contents of _textField
     */
    @Override
    void exitField() {
        // there may be a lost focus event left in the queue when disposed so protect
        String oldVal = oldContents;
        String newVal = _textField.getText();
        if (!isMatched(newVal)) {        // check for match to regex if applicable
            _textField.setText(oldVal); // if mismatch, restore old value
            return;                     // & return without triggering property change
        }
        if (!oldVal.equals(newVal)) {
            log.debug("Value changed from '{}' to '{}", oldVal, newVal);
            // special care needed if _textField is shrinking
            _fieldShrink = (newVal.length() < oldVal.length());
            log.debug("_fieldShrink=" + _fieldShrink);
            updatedTextField();
            prop.firePropertyChange("Value", oldVal, newVal);
        }
    }

    /**
     * Contains byte-value specific code.
     * <br><br>
     * invokes {@link #updatedTextField updatedTextField()}
     * <br><br>
     * firePropertyChange for "Value" with new contents of _textField
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; actionPerformed");
        }
        byte[] newVal = getBytesFromText(_textField.getText());
        updatedTextField();
        prop.firePropertyChange("Value", null, newVal);
    }

    @Override
    public int getIntValue() {
        log.error("getValue doesn't make sense for a split text value");
        return 0;
    }

    @Override
    public void setValue(String value) {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; enter setValue " + value);
        }
        String oldVal = _textField.getText();
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; setValue with new value " + value + " old value " + oldVal);
        }
        _textField.setText(value);
        if (!oldVal.equals(value) || getState() == VariableValue.UNKNOWN) {
            actionPerformed(null);
        }
        prop.firePropertyChange("Value", oldVal, value);
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; exit setValue " + value);

        }
    }

    @Override
    public void setIntValue(int i) {
        log.warn("setIntValue doesn't make sense for a split text value: " + i);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SplitTextVariableValue.class
            .getName());

}
