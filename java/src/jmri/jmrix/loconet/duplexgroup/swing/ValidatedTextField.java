// ValidatedTextField.java
package jmri.jmrix.loconet.duplexgroup.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends JTextField to provide a data validation function and a colorization
 * function.
 *
 * Supports two types of validated field: . generic text fields with length
 * and/or character set limited by a JAVA regular expression
 *
 * . integral numeric fields with minimum and maximum allowed values
 *
 * @author B. Milhaupt Copyright 2010, 2011
 */
public class ValidatedTextField extends javax.swing.JTextField {

    /**
     *
     */
    private static final long serialVersionUID = 4592093311837686838L;
    ValidatedTextField thisone;

    /**
     * Provides a validated text field, where the validation mechanism requires
     * a String value which passes the matching defined in validationRegExpr .
     *
     * Validation occurs as part of the process of focus leaving the field. When
     * validation fails, the focus remains within the field, and the field
     * foreground and background colors are changed.
     *
     * When focus leaves the field and the field value is valid, the value will
     * be checked against the "Last Queried Value". If the current field value
     * matches the "Last Queried Value", the field is colorized using the
     * default field foreground and background colors. If instead the current
     * field value does not match the "Last Queried Value", the field background
     * color is changed to reflect that the value is not yet saved. Use the
     * setLastQueriedValue() method to set the value for this comparison.
     *
     * Parameter len defines the width of the text field entry box, in
     * characters.
     *
     * Parameter forceUppercase determines if all alphabetic characters are
     * forced to uppercase.
     *
     * Parameter validationRegExpr defines a java regular expression which is
     * used when validating the text input. A string such as
     * "^[0-9]{2}[a-zA-Z]{3,4}$" would require the text field to be a 5 or 6
     * character string which starts with exactly two digits and followed by
     * either 3 or 4 upper-case or lower-case letters.
     *
     * Parameter validationErrorMessage is passed as an argument to the property
     * change listener for the instantiating class.
     *
     * @param len
     * @param forceUppercase
     * @param validationRegExpr
     * @param validationErrorMessage
     */
    public ValidatedTextField(Integer len,
            boolean forceUppercase,
            String validationRegExpr,
            String validationErrorMessage) {
        super("0", len);
        fieldType = FieldType.TEXT;
        validateRegExpr = validationRegExpr;
        validationErrorText = "ERROR:" + validationErrorMessage;
        minAllowedValue = 0;
        forceUpper = forceUppercase;
        maxAllowedValue = 0;
        allow0Length = false;

        verifier = new MyVerifier();

        // set default background color for invalid field data
        setInvalidBackgroundColor(COLOR_BG_ERROR);

        thisone = this;
        thisone.setInputVerifier(verifier);
        thisone.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                setEditable(true);
                setEnabled(true);
            }

            public void focusLost(FocusEvent e) {
                exitFieldColorizer();
                setEditable(true);
            }
        });
    }

    /**
     * Provides a validated text field, where the validation mechanism requires
     * a String value which passes the matching defined in validationRegExpr,
     * and where the string begins with a number which must be within a
     * specified integral range.
     *
     * Validation occurs as part of the process of focus leaving the field. When
     * validation fails, the focus remains within the field, and the field
     * foreground and background colors are changed.
     *
     * When focus leaves the field and the field value is valid, the value will
     * be checked against the "Last Queried Value". If the current field value
     * matches the "Last Queried Value", the field is colorized using the
     * default field foreground and background colors. If instead the current
     * field value does not match the "Last Queried Value", the field background
     * color is changed to reflect that the value is not yet saved. Use the
     * setLastQueriedValue() method to set the value for this comparison.
     *
     * Parameter len defines the width of the text field entry box, in
     * characters.
     *
     * Parameter allow0LengthValue determines if a value of 0 characters is
     * allowed as a valid value.
     *
     * Parameter forceUppercase determines if all alphabetic characters are
     * forced to uppercase.
     *
     * Parameter minValue is the smallest allowed value.
     *
     * Parameter maxValue is the largest allowed value.
     *
     * Parameter validationRegExpr defines a java regular expression which is
     * used when validating the text input. A string such as
     * "^[0-9]{2}[a-zA-Z]{3,4}$" would require the text field to be a 5 or 6
     * character string which starts with exactly two digits and followed by
     * either 3 or 4 upper-case or lower-case letters.
     *
     * Parameter validationErrorMessage is passed as an argument to the property
     * change listener for the instantiating class.
     *
     * @param len
     * @param allow0LengthValue
     * @param forceUppercase
     * @param minValue
     * @param maxValue
     * @param validationRegExpr
     * @param validationErrorMessage
     */
    public ValidatedTextField(
            Integer len,
            boolean allow0LengthValue,
            boolean forceUppercase,
            Integer minValue,
            Integer maxValue,
            String validationRegExpr,
            String validationErrorMessage) {
        super("0", len);
        fieldType = FieldType.INTEGRALNUMERICPLUSSTRING;
        validateRegExpr = validationRegExpr;
        validationErrorText = "ERROR:" + validationErrorMessage;
        minAllowedValue = minValue;
        forceUpper = forceUppercase;
        maxAllowedValue = maxValue;
        allow0Length = allow0LengthValue;

        verifier = new MyVerifier();

        thisone = this;
        thisone.setInputVerifier(verifier);
        thisone.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                setEditable(true);
                setEnabled(true);
            }

            public void focusLost(FocusEvent e) {
                exitFieldColorizer();
                setEditable(true);
            }
        });
    }

    /**
     * Provides a validated text field for integral values, where the validation
     * mechanism requires a numeric value between a minimum and maximum value.
     *
     * Validation occurs as part of the process of focus leaving the field. When
     * validation fails, the focus remains within the field, and the field
     * foreground and background colors are changed.
     *
     * When focus leaves the field and the field value is valid, the value will
     * be checked against the "Last Queried Value". If the current field value
     * matches the "Last Queried Value", the field is colorized using the
     * default field foreground and background colors. If instead the current
     * field value does not match the "Last Queried Value", the field background
     * color is changed to reflect that the value is not yet saved. Use the
     * setLastQueriedValue() method to set the value for this comparison.
     *
     * Parameter len defines the width of the text field entry box, in
     * characters.
     *
     * Parameter allow0LengthValue determines if a value of 0 characters is
     * allowed as a valid value.
     *
     * Parameter minValue is the smallest allowed value.
     *
     * Parameter maxValue is the largest allowed value.
     *
     * Parameter validationErrorMessage is passed as an argument to the property
     * change listener for the instantiating class.
     *
     * @param len
     * @param allow0LengthValue
     * @param minValue
     * @param maxValue
     * @param validationErrorMessage
     */
    public ValidatedTextField(
            Integer len,
            boolean allow0LengthValue,
            Integer minValue,
            Integer maxValue,
            String validationErrorMessage) {
        super("0", len);
        validateRegExpr = null;
        validationErrorText = "ERROR:" + validationErrorMessage;
        fieldType = FieldType.INTEGRALNUMERIC;
        minAllowedValue = minValue;
        maxAllowedValue = maxValue;
        forceUpper = false;
        allow0Length = allow0LengthValue;

        verifier = new MyVerifier();

        thisone = this;
        thisone.setInputVerifier(verifier);
        thisone.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                setEditable(true);
                setEnabled(true);
            }

            public void focusLost(FocusEvent e) {
                exitFieldColorizer();
                setEditable(true);
            }
        });
    }

    /**
     * Provides a validated text field, where the validation mechanism requires
     * a Numeric value which is a hexadecimal value which is valid and within a
     * given numeric range.
     *
     * Validation occurs as part of the process of focus leaving the field. When
     * validation fails, the focus remains within the field, and the field
     * foreground and background colors are changed.
     *
     * When focus leaves the field and the field value is valid, the value will
     * be checked against the "Last Queried Value". If the current field value
     * matches the "Last Queried Value", the field is colorized using the
     * default field foreground and background colors. If instead the current
     * field value does not match the "Last Queried Value", the field background
     * color is changed to reflect that the value is not yet saved. Use the
     * setLastQueriedValue() method to set the value for this comparison.
     *
     * Parameter minAcceptableVal defines the lowest acceptable value.
     *
     * Parameter maxAcceptableVal defines the lowest acceptable value.
     *
     * Parameter validationRegExpr defines a java regular expression which is
     * used when validating the text input. A string such as
     * "^[0-9]{2}[a-zA-Z]{3,4}$" would require the text field to be a 5 or 6
     * character string which starts with exactly two digits and followed by
     * either 3 or 4 upper-case or lower-case letters.
     *
     * Parameter validationErrorMessage is passed as an argument to the property
     * change listener for the instantiating class.
     *
     * @param len
     * @param minAcceptableVal
     * @param maxAcceptableVal
     * @param validationErrorMessage
     */
    public ValidatedTextField(Integer len,
            int minAcceptableVal,
            int maxAcceptableVal,
            String validationErrorMessage) {
        super("0", len);
        fieldType = FieldType.LIMITEDHEX;
        validateRegExpr = null;
        validationErrorText = "ERROR:" + validationErrorMessage;
        minAllowedValue = minAcceptableVal;
        forceUpper = true;
        maxAllowedValue = maxAcceptableVal;
        allow0Length = false;

        verifier = new MyVerifier();

        // set default background color for invalid field data
        setInvalidBackgroundColor(COLOR_BG_ERROR);

        thisone = this;
        thisone.setInputVerifier(verifier);
        thisone.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                setEditable(true);
                setEnabled(true);
            }

            public void focusLost(FocusEvent e) {
                exitFieldColorizer();
                setEditable(true);
            }
        });
    }

    private String lastQueryValue;    // used for GUI field colorization
    private String validateRegExpr;     // used for validation of TEXT ValidatedTextField objects
    private Integer minAllowedValue;    // used for validation of INTEGRALNUMERIC ValidatedTextField objects
    private Integer maxAllowedValue;    // used for validation of INTEGRALNUMERIC ValidatedTextField objects
    private boolean allow0Length;  // used for validation

    private String validationErrorText; // text used when validation fails
    private FieldType fieldType;        // used to distinguis between INTEGRALNUMERIC-only and TEXT ValidatedTextField objects
    private boolean forceUpper;         // used for forcing all input to upper-case for TEXT ValidatedTextField objects
    private MyVerifier verifier;        // internal mechanism used for verifying field data before focus is lost

    /**
     * Method to colorize enabled field based on comparison with the last
     * queried value. If the field is disabled, no colorization occurs.
     */
    private void exitFieldColorizer() {
        // colorize the text field entry box based on comparison with last queried value

        if (thisone.isEnabled()) {
            thisone.setForeground(COLOR_OK);
            thisone.firePropertyChange(VTF_PC_STAT_LN_UPDATE, "_", " ");

            if ((getText() == null) || (getText().length() == 0)) {
                // handle 0-length current value; 0-length is allowed
                if (allow0Length == true) {
                    if ((lastQueryValue == null) || (lastQueryValue.length() == 0)) {
                        setBackground(COLOR_BG_UNEDITED);
                        return;
                    } else {
                        setBackground(COLOR_BG_EDITED);
                        return;
                    }
                } else {
                    // 0-length current value; 0-length is not allowed
                    // (validator should prevent from getting to this case)
                    return;
                }
            }

            if ((lastQueryValue == null) || (lastQueryValue.length() == 0)) {
                // handle 0-length last qurey value
                // (already know current value is not 0-length)
                setBackground(COLOR_BG_EDITED);
                return;
            }
            if (!lastQueryValue.equals(thisone.getText())) {
                // mismatch between last queried value and current field value
                thisone.setBackground(COLOR_BG_EDITED);
            } else {
                // match between last queried value and current field value
                thisone.setBackground(COLOR_BG_UNEDITED);
            }
            return;
        } else {
            // don't change background color of disabled field
            return;
        }
    }

    /**
     * Validates the field information. Does not make any GUI changes. A field
     * value that is zero-length is considered invalid.
     *
     * @return TRUE if current field information is valid, else FALSE
     */
    public boolean isValid() {
        String value;
        if (thisone == null) {
            return false;
        }
        value = getText();
        if (fieldType == FieldType.TEXT) {
            if ((value.length() < 1) && (allow0Length == false)) {
                return false;
            } else if (((allow0Length == true) && (value.length() == 0))
                    || (value.matches(validateRegExpr))) {
                return true;
            } else {
                return false;
            }
        } else if (fieldType == FieldType.INTEGRALNUMERIC) {
            try {
                if ((allow0Length == true) && (value.length() == 0)) {
                    return true;
                } else if (value.length() == 0) {
                    return false;
                } else if ((Integer.parseInt(value) >= minAllowedValue)
                        && (Integer.parseInt(value) <= maxAllowedValue)) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else if (fieldType == FieldType.INTEGRALNUMERICPLUSSTRING) {
            Integer findLocation = 999;
            Integer location = 999;
            if ((allow0Length == true) && (value.length() == 0)) {
                return true;
            } else if (value.length() == 0) {
                return false;
            }

            location = value.indexOf('c');
            if ((location != -1) && (location < findLocation)) {
                findLocation = location;
            }
            location = value.indexOf('C');
            if ((location != -1) && (location < findLocation)) {
                findLocation = location;
            }
            location = value.indexOf('t');
            if ((location != -1) && (location < findLocation)) {
                findLocation = location;
            }
            location = value.indexOf('T');
            if ((location != -1) && (location < findLocation)) {
                findLocation = location;
            }
            if (findLocation == 999) {
                return false;
            }

            try {
                Integer address = Integer.parseInt(value.substring(0, findLocation));
                if ((address < minAllowedValue)
                        || (address > maxAllowedValue)) {
                    return false;
                } else if ((value.length() < 2) || (!value.matches(validateRegExpr))) {
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        } else if (fieldType == FieldType.LIMITEDHEX) {
            try {
                if (value.length() == 0) {
                    return false;
                } else if ((Integer.parseInt(value, 16) >= minAllowedValue)
                        && (Integer.parseInt(value, 16) <= maxAllowedValue)) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            // unknown validation field type
            return false;
        }
    }

    /**
     * Method to set the "Last Queried Value". This value is used by the
     * colorization process when focus is exiting the field.
     *
     * @param lastQueriedValue
     */
    public void setLastQueriedValue(String lastQueriedValue) {
        lastQueryValue = lastQueriedValue;
        exitFieldColorizer();
    }

    /**
     * Method to retrieve the current value of the "Last Queried Value". See
     * also setLastQueriedValue().
     *
     * @return lastQueryValue
     */
    public String getLastQueriedValue() {
        return lastQueryValue;
    }

    /**
     * Enumeration type which differentiates the supported data types. Each
     * different type requires special-case coding within the methods defined
     * within this class.
     */
    private enum FieldType {

        TEXT, INTEGRALNUMERIC, INTEGRALNUMERICPLUSSTRING, LIMITEDHEX
    }

    /**
     * Private class used in conjunction with the basic GUI JTextField to
     * provide the mechanisms required to validate the text field data upon loss
     * of focus, and colorize the text field in case of validation failure.
     */
    private class MyVerifier extends javax.swing.InputVerifier implements java.awt.event.ActionListener {

        public boolean shouldYieldFocus(javax.swing.JComponent input) {
            if (input.getClass() == ValidatedTextField.class) {

                if (((ValidatedTextField) input).forceUpper) {
                    ((ValidatedTextField) input).setText(((ValidatedTextField) input).getText().toUpperCase());
                }

                boolean inputOK = verify(input);
                if (inputOK) {
                    input.setForeground(COLOR_OK);
                    input.setBackground(COLOR_BG_OK);
                    return true;
                } else {
                    // if there was a good way to make a beep sound here, this would be a good place to do so.
                    //java.awt.Toolkit.getDefaultToolkit().beep();  // this didn't work under WinXP for unknown reasons.

                    input.setForeground(COLOR_ERROR_VAL);
                    input.setBackground(invalidBackgroundColor);
                    ((javax.swing.text.JTextComponent) input).selectAll();
                    thisone.firePropertyChange(VTF_PC_STAT_LN_UPDATE, " _ ", validationErrorText);
                    return false;
                }

            } else {
                return false;
            }
        }

        public boolean verify(javax.swing.JComponent input) {
            if (input.getClass() == ValidatedTextField.class) {
                return ((ValidatedTextField) input).isValid();
            } else {
                return false;
            }
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            javax.swing.JTextField source = (javax.swing.JTextField) e.getSource();
            shouldYieldFocus(source); //ignore return value
            source.selectAll();
        }
    }
    private java.awt.Color invalidBackgroundColor = null;

    /**
     * Sets the color used for the field background when the field value is
     * invalid.
     * <p>
     * @param c - Background Color to be used when the value is invalid
     */
    public void setInvalidBackgroundColor(java.awt.Color c) {
        invalidBackgroundColor = c;
    }

    public static final String VTF_PC_STAT_LN_UPDATE = "VTFPCK_STAT_LN_UPDATE";

    // defines for colorizing the user input GUI elements and status line
    public final static java.awt.Color COLOR_BG_EDITED = java.awt.Color.orange; // use default color for the component
    public final static java.awt.Color COLOR_ERROR_VAL = java.awt.Color.black;
    public final static java.awt.Color COLOR_OK = java.awt.Color.black;
    public final static java.awt.Color COLOR_BG_OK = java.awt.Color.white;
    public final static java.awt.Color COLOR_BG_UNEDITED = COLOR_BG_OK;
    public final static java.awt.Color COLOR_BG_ERROR = java.awt.Color.red;

    private final static Logger log = LoggerFactory.getLogger(ValidatedTextField.class.getName());
}
