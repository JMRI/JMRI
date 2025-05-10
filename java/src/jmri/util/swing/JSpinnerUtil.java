package jmri.util.swing;

import javax.swing.JSpinner;
import javax.swing.text.DefaultFormatter;

/**
 * Utility class providing common methods for working with {@link JSpinner}
 * components in Swing.
 * @author Steve Young Copyright(C) 2025
 */
public class JSpinnerUtil {

    private JSpinnerUtil(){} // Class only supplies static methods.

    /**
     * Sets whether the {@link JSpinner}'s text editor commits the value to the
     * spinner model.
     * <p>
     * By default, JSpinners only update their value when enter is pressed
     * or focus leaves the JFormattedTextField.
     * </p>
     *
     * @param spinner the JSpinner to modify.
     * @param newVal  true to commit on valid edits, else false ( default JSpinner )
     */
    public static void setCommitsOnValidEdit( JSpinner spinner, boolean newVal) {
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)spinner.getEditor();
        var field = editor.getTextField();
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(newVal);
    }

}
