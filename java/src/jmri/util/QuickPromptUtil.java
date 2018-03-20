package jmri.util;

import java.awt.Component;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utilities related to prompting for values
 *
 * @author George Warner Copyright: (c) 2017
 */
public class QuickPromptUtil {

    /**
     * Utility function to prompt for new string value
     *
     * @param message  the prompt message
     * @param title    the dialog title
     * @param oldValue the original string value
     * @return the new string value
     */
    static public String promptForString(Component parentComponent, String message, String title, String oldValue) {
        String result = oldValue;
        String newValue = (String) JOptionPane.showInputDialog(parentComponent,
                message, title, JOptionPane.PLAIN_MESSAGE,
                null, null, oldValue);
        if (newValue != null) {
            result = newValue;
        }
        return result;
    }

    /**
     * Utility function to prompt for new integer value
     *
     * @param message  the prompt message
     * @param title    the dialog title
     * @param oldValue the original integer value
     * @return the new integer value
     */
    static public int promptForInt(Component parentComponent, String message, String title, int oldValue) {
        int result = oldValue;
        String newValue = promptForString(parentComponent, message, title, Integer.toString(oldValue));
        if (newValue != null) {
            try {
                result = Integer.parseInt(newValue);
            } catch (NumberFormatException e) {
                result = oldValue;
            }
        }
        return result;
    }

    /**
     * Utility function to prompt for new float value
     *
     * @param message  the prompt message
     * @param title    the dialog title
     * @param oldValue the original float value
     * @return the new float value
     */
    static public float promptForFloat(Component parentComponent, String message, String title, float oldValue) {
        float result = oldValue;
        String newValue = promptForString(parentComponent, message, title, Float.toString(oldValue));
        if (newValue != null) {
            try {
                result = Float.parseFloat(newValue);
            } catch (NumberFormatException e) {
                result = oldValue;
            }
        }
        return result;
    }

    /**
     * Utility function to prompt for new double value
     *
     * @param message  the prompt message
     * @param title    the dialog title
     * @param oldValue the original double value
     * @return the new double value
     */
    static public double promptForDouble(Component parentComponent, String message, String title, double oldValue) {
        double result = oldValue;
        String newValue = promptForString(parentComponent, message, title, Double.toString(oldValue));
        if (newValue != null) {
            try {
                result = Double.parseDouble(newValue);
            } catch (NumberFormatException e) {
                result = oldValue;
            }
        }
        return result;
    }

    // initialize logging
    // private final static Logger log = LoggerFactory.getLogger(QuickPromptUtil.class);
}
