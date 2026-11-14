package jmri.jmrit.simulation.swing;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.UserPreferencesManager;
import jmri.jmrit.beantable.BlockTableAction;

/**
 * The units lengths are shown and entered in: whole millimetres, or inches to
 * 1/32 inch, shown as a fraction. Which is the choice made for Block lengths
 * in the Block table (centimetres or inches), so that a train is described in
 * the units its Blocks were.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
final class LengthUnits {

    /** Millimetres in an inch. */
    static final double MM_PER_INCH = 25.4;

    /** Steps in an inch. */
    static final int INCH_STEPS = 32;

    private final boolean metric;

    /**
     * @param metric true for millimetres, false for inches
     */
    LengthUnits(boolean metric) {
        this.metric = metric;
    }

    /**
     * @return the units chosen for Block lengths
     */
    @Nonnull
    static LengthUnits fromPreferences() {
        return new LengthUnits(InstanceManager.getDefault(UserPreferencesManager.class)
                .getSimplePreferenceState(BlockTableAction.BLOCK_METRIC_PREF));
    }

    /**
     * @return true for millimetres, false for inches
     */
    boolean isMetric() {
        return metric;
    }

    /**
     * @return the units' short name, for labels
     */
    @Nonnull
    String getName() {
        return Bundle.getMessage(metric ? "UnitMm" : "UnitInches");
    }

    /**
     * @param mm a length in millimetres
     * @return it in these units, to the nearest step: a whole millimetre, or
     *         1/32 inch
     */
    double fromMm(double mm) {
        return metric ? Math.rint(mm) : Math.rint(mm / MM_PER_INCH * INCH_STEPS) / INCH_STEPS;
    }

    /**
     * @param length a length in these units
     * @return it in millimetres
     */
    double toMm(double length) {
        return metric ? length : length * MM_PER_INCH;
    }

    /**
     * @return one step: 1 mm, or 1/32 inch
     */
    double step() {
        return metric ? 1.0 : 1.0 / INCH_STEPS;
    }

    /**
     * A spinner for a length in these units, showing whole millimetres, or
     * inches as a whole number and a fraction.
     *
     * @param mm    the length to show at first, in millimetres
     * @param minMm the shortest, in millimetres
     * @param maxMm the longest, in millimetres
     * @return the spinner, whose value is a Double in these units
     */
    @Nonnull
    @InvokeOnGuiThread
    JSpinner spinner(double mm, double minMm, double maxMm) {
        // a length that must be more than nothing is at least one step
        double min = minMm <= 0 ? 0.0 : Math.max(step(), fromMm(minMm));
        double max = fromMm(maxMm);
        double value = Math.max(min, Math.min(max, fromMm(mm)));
        JSpinner s = new JSpinner(new SpinnerNumberModel(value, min, max, step()));
        if (metric) {
            s.setEditor(new JSpinner.NumberEditor(s, "#"));
        } else {
            JSpinner.DefaultEditor editor = new JSpinner.DefaultEditor(s);
            JFormattedTextField field = editor.getTextField();
            field.setFormatterFactory(new DefaultFormatterFactory(new InchFormatter()));
            field.setEditable(true);
            field.setHorizontalAlignment(JFormattedTextField.RIGHT);
            field.setColumns(7);
            s.setEditor(editor);
        }
        return s;
    }

    /**
     * @param inches a length in inches
     * @return it to the nearest 1/32 inch, as a whole number and a fraction
     *         in its lowest terms, such as "7 3/16", "7" or "3/16"
     */
    @Nonnull
    static String inchesText(double inches) {
        long steps = Math.round(inches * INCH_STEPS);
        String sign = steps < 0 ? "-" : "";
        steps = Math.abs(steps);
        long whole = steps / INCH_STEPS;
        long part = steps % INCH_STEPS;
        if (part == 0) {
            return sign + whole;
        }
        long denominator = INCH_STEPS;
        while (part % 2 == 0) {
            part /= 2;
            denominator /= 2;
        }
        return sign + (whole == 0 ? "" : whole + " ") + part + "/" + denominator;
    }

    private static final Pattern INCHES = Pattern.compile(
            "\\s*(?:(\\d+)(?:\\s+(\\d+)\\s*/\\s*(\\d+))?|(\\d+)\\s*/\\s*(\\d+)|(\\d*\\.\\d+|\\d+\\.\\d*))\\s*(?:in|\")?\\s*");

    /**
     * @param text inches as written: a whole number, a fraction, both
     *             ("7 3/16"), or a decimal, with "in" or {@code "} after it if
     *             wished
     * @return the length in inches, to the nearest 1/32 inch
     * @throws ParseException if it is not a length
     */
    static double parseInches(@Nonnull String text) throws ParseException {
        Matcher m = INCHES.matcher(text);
        if (!m.matches()) {
            throw new ParseException(Bundle.getMessage("LengthNotUnderstood", text), 0);
        }
        double inches;
        if (m.group(1) != null) {
            inches = Long.parseLong(m.group(1));
            if (m.group(2) != null) {
                inches += fraction(m.group(2), m.group(3), text);
            }
        } else if (m.group(4) != null) {
            inches = fraction(m.group(4), m.group(5), text);
        } else {
            inches = Double.parseDouble(m.group(6));
        }
        return Math.rint(inches * INCH_STEPS) / INCH_STEPS;
    }

    private static double fraction(String numerator, String denominator, String text) throws ParseException {
        long d = Long.parseLong(denominator);
        if (d == 0) {
            throw new ParseException(Bundle.getMessage("LengthNotUnderstood", text), 0);
        }
        return (double) Long.parseLong(numerator) / d;
    }

    /** Shows and reads inches as a whole number and a fraction. */
    private static final class InchFormatter extends JFormattedTextField.AbstractFormatter {

        @Override
        public Object stringToValue(String text) throws ParseException {
            return parseInches(text == null ? "" : text);
        }

        @Override
        public String valueToString(Object value) {
            return value instanceof Number ? inchesText(((Number) value).doubleValue()) : "";
        }
    }
}
