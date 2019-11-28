package jmri.util;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

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
    public static String promptForString(Component parentComponent, String message, String title, String oldValue) {
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
    public static int promptForInt(Component parentComponent, String message, String title, int oldValue) {
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
     * Utility function to prompt for new integer value. Allows to constrain
     * values using a Predicate (validator).
     * <p>
     * The validator may throw an {@link IllegalArgumentException} whose
     * {@link IllegalArgumentException#getLocalizedMessage()} will be displayed.
     * The Predicate may also simply return {@code false}, which causes just
     * general message (the value is invalid) to be printed. If the Predicate
     * rejects the input, the OK button is disabled and the user is unable to
     * confirm the dialog.
     * <p>
     * The function returns the original value if the dialog was cancelled or
     * the entered value was empty or invalid. Otherwise, it returns the new
     * value entered by the user.
     *
     * @param parentComponent the parent component
     * @param message         the prompt message.
     * @param title           title for the dialog
     * @param oldValue        the original value
     * @param validator       the validator instance. May be {@code null}
     * @return the updated value, or the original one.
     */
    public static Integer promptForInteger(Component parentComponent, @Nonnull String message, @Nonnull String title, Integer oldValue, @CheckForNull Predicate<Integer> validator) {
        Integer result = oldValue;
        Integer newValue = promptForData(parentComponent, message, title, oldValue, validator, (val) -> {
            try {
                return Integer.valueOf(Integer.parseInt(val));
            } catch (NumberFormatException ex) {
                // original exception ignored; wrong message.
                throw new NumberFormatException(Bundle.getMessage("InputDialogNotNumber"));
            }
        });
        if (newValue != null) {
            result = newValue;
        }
        return result;
    }

    private static <T> T promptForData(Component parentComponent,
            @Nonnull String message, @Nonnull String title, T oldValue,
            @CheckForNull Predicate<T> validator,
            @CheckForNull Function<String, T> converter) {
        String result = oldValue == null ? "" : oldValue.toString(); // NOI18N
        JButton okOption = new JButton(Bundle.getMessage("InputDialogOK")); // NOI18N
        JButton cancelOption = new JButton(Bundle.getMessage("InputDialogCancel")); // NOI18N
        okOption.setDefaultCapable(true);

        ValidatingInputPane<T> validating = new ValidatingInputPane<T>(converter)
                .message(message)
                .validator(validator)
                .attachConfirmUI(okOption);
        validating.setText(result);
        JOptionPane pane = new JOptionPane(validating, JOptionPane.PLAIN_MESSAGE,
                OK_CANCEL_OPTION, null, new Object[]{okOption, cancelOption});

        pane.putClientProperty("OptionPane.buttonOrientation", SwingConstants.RIGHT); // NOI18N
        JDialog dialog = pane.createDialog(parentComponent, title);
        dialog.getRootPane().setDefaultButton(okOption);
        dialog.setResizable(true);

        class AL implements ActionListener {

            boolean confirmed;

            @Override
            public void actionPerformed(ActionEvent e) {
                Object s = e.getSource();
                if (s == okOption) {
                    confirmed = true;
                    dialog.setVisible(false);
                }
                if (s == cancelOption) {
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
            T res = validating.getValue();
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
    public static float promptForFloat(Component parentComponent, String message, String title, float oldValue) {
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
    public static double promptForDouble(Component parentComponent, String message, String title, double oldValue) {
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

    /**
     * Creates a min/max predicate which will check the bounds. Suitable for
     * {@link #promptForInteger(java.awt.Component, java.lang.String, java.lang.String, Integer, java.util.function.Predicate)}.
     *
     * @param min        minimum value. Use {@link Integer#MIN_VALUE} to disable
     *                   check.
     * @param max        maximum value, inclusive. Use {@link Integer#MAX_VALUE}
     *                   to disable check.
     * @param valueLabel label to be included in the message. Must be already
     *                   I18Ned.
     * @return predicate instance
     */
    public static Predicate<Integer> checkIntRange(Integer min, Integer max, String valueLabel) {
        return new IntRangePredicate(min, max, valueLabel);
    }

    /**
     * Base for range predicates (int, float). Checks for min/max - if
     * configured, produces an exception with an appropriate message if check
     * fails.
     *
     * @param <T> the data type
     */
    private static abstract class NumberRangePredicate<T extends Number> implements Predicate<T> {

        protected final T min;
        protected final T max;
        protected final String label;

        public NumberRangePredicate(T min, T max, String label) {
            this.min = min;
            this.max = max;
            this.label = label;
        }

        protected abstract boolean acceptLow(T val, T bound);

        protected abstract boolean acceptHigh(T val, T bound);

        @Override
        public boolean test(T t) {
            boolean ok = true;

            if (min != null && !acceptLow(t, min)) {
                ok = false;
            } else if (max != null && !acceptHigh(t, max)) {
                ok = false;
            }
            if (ok) {
                return true;
            }
            final String msgKey;
            if (label != null) {
                if (min == null) {
                    msgKey = "NumberCheckOutOfRangeMax"; // NOI18N
                } else if (max == null) {
                    msgKey = "NumberCheckOutOfRangeMin"; // NOI18N
                } else {
                    msgKey = "NumberCheckOutOfRangeBoth"; // NOI18N
                }
            } else {
                if (min == null) {
                    msgKey = "NumberCheckOutOfRangeMax2"; // NOI18N
                } else if (max == null) {
                    msgKey = "NumberCheckOutOfRangeMin2"; // NOI18N
                } else {
                    msgKey = "NumberCheckOutOfRangeBoth2"; // NOI18N
                }
            }
            throw new IllegalArgumentException(
                    Bundle.getMessage(msgKey, label, min, max)
            );
        }
    }

    // This is currently unused, ready for converting the 
    // promptForFloat 
    static final class FloatRangePredicate extends NumberRangePredicate<Float> {

        public FloatRangePredicate(Float min, Float max, String label) {
            super(min, max, label);
        }

        @Override
        protected boolean acceptLow(Float val, Float bound) {
            return val >= bound;
        }

        @Override
        protected boolean acceptHigh(Float val, Float bound) {
            return val <= bound;
        }
    }

    static final class IntRangePredicate extends NumberRangePredicate<Integer> {

        public IntRangePredicate(Integer min, Integer max, String label) {
            super(min, max, label);
        }

        @Override
        protected boolean acceptLow(Integer val, Integer bound) {
            return val >= bound;
        }

        @Override
        protected boolean acceptHigh(Integer val, Integer bound) {
            return val <= bound;
        }
    }

    // initialize logging
    // private final static Logger log = LoggerFactory.getLogger(QuickPromptUtil.class);
}
