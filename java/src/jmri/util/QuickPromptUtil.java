package jmri.util;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import org.python.google.common.base.Objects;
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
     * Utility function to prompt for new integer value. Allows to constrain values using a Predicate (validator).
     * <p>
     * The validator may throw an {@link IllegalArgumentException} whose {@link IllegalArgumentException#getLocalizedMessage()} will
     * be displayed. The Predicate may also simply return {@code false}, which causes just general message (the value is invalid) to
     * be printed. If the Predicate rejects the input, the OK button is disabled and the user is unable to confirm the dialog.
     * </p>
     * <p>
     * The function returns the original value if the dialog was cancelled or the entered value was empty or invalid. Otherwise, it
     * returns the new value entered by the user.
     * </p>
     * @param parentComponent the parent component
     * @param message the prompt message.
     * @param title title for the dialog
     * @param oldValue the original value
     * @param validator the validator instance. May be {@code null}
     * @return the updated value, or the original one.
     */
    static public int promptForInt(Component parentComponent, @Nonnull String message, @Nonnull String title, int oldValue, @CheckForNull Predicate<Integer> validator) {
        String result = Integer.toString(oldValue);
        JButton okOption = new JButton(Bundle.getMessage("InputDialogOK"));
        JButton cancelOption = new JButton(Bundle.getMessage("InputDialogCancel"));
        okOption.setDefaultCapable(true);

        ValidatingInputPane<Integer> validating = new ValidatingInputPane<Integer>((val) -> {
                try {
                    return Integer.parseInt(val);
                } catch (NumberFormatException ex) {
                    throw new NumberFormatException(Bundle.getMessage("InputDialogNotNumber"));
                }
            })
                .message(message)
                .validator(validator)
                .attachConfirmUI(okOption);
        validating.setText(result);
        
        JOptionPane    pane = new JOptionPane(validating, JOptionPane.PLAIN_MESSAGE,
                        OK_CANCEL_OPTION, null, new Object[] { okOption, cancelOption });
        
        pane.putClientProperty("OptionPane.buttonOrientation", SwingConstants.RIGHT);
        pane.setInitialSelectionValue(result);
        JDialog dialog = pane.createDialog(parentComponent, title);
        dialog.getRootPane().setDefaultButton(okOption);
        dialog.setResizable(true);
        pane.selectInitialValue();

        class AL implements ActionListener {
            boolean confirmed;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                Object s = e.getSource();
                if (s == okOption || Objects.equal("confirm", e.getActionCommand())) {
                    confirmed = true;
                    dialog.setVisible(false);
                }
                if (s == cancelOption || Objects.equal("cancel", e.getActionCommand())) {
                    dialog.setVisible(false);
                }
            }
        }
        
        AL al = new AL();
        okOption.addActionListener(al);
        cancelOption.addActionListener(al);

        dialog.setVisible(true);
        dialog.dispose();
        
        if (al.confirmed) {
            Integer res = validating.getValue();
            if (res != null) {
                return res;
            }
        }
        return oldValue;
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
