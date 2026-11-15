package jmri.jmrit.beantable;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.Section;
import jmri.UserPreferencesManager;

/**
 * Enter and show a Section's stop points in millimetres, or in inches to the
 * nearest 1/32 inch. Stop points are always kept in millimetres (see
 * {@link Section#getStopPointMm(int)}); the units they were entered in are
 * kept with the Section so they are shown the same way next time.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class SectionStopPoint {

    /** Key of the Section property holding the units its stop points are shown in. */
    public static final String UNITS_PROPERTY = "StopPointUnits"; // NOI18N

    /** Millimetres in an inch. */
    static final double MM_PER_INCH = 25.4;

    /** Inches are given to the nearest 1/32. */
    static final int INCH_PARTS = 32;

    /**
     * The units a stop point is entered and shown in.
     */
    public enum Units {
        /** Millimetres. */
        MM("StopPointUnitsMm"), // NOI18N
        /** Inches, to the nearest 1/32. */
        INCHES("StopPointUnitsInches"); // NOI18N

        private final String key;

        Units(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return SectionTableAction.rbx.getString(key);
        }
    }

    // a whole number of inches and a fraction: "12 7/32" or "12-7/32"
    private static final Pattern MIXED = Pattern.compile("(\\d+)(?:\\s+|\\s*-\\s*)(\\d+)\\s*/\\s*(\\d+)");
    // a fraction alone: "7/32" or "391/32"
    private static final Pattern FRACTION = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");

    private SectionStopPoint() {
    }

    /**
     * @return the units new stop points are shown in: millimetres if the
     *         Block table shows lengths in centimetres, otherwise inches
     */
    @Nonnull
    public static Units defaultUnits() {
        UserPreferencesManager p = InstanceManager.getNullableDefault(UserPreferencesManager.class);
        return p != null && p.getSimplePreferenceState(BlockTableAction.BLOCK_METRIC_PREF) ? Units.MM : Units.INCHES;
    }

    /**
     * @param section the Section
     * @return the units its stop points were entered in, or
     *         {@link #defaultUnits()} if none were kept
     */
    @Nonnull
    public static Units getUnits(@Nonnull Section section) {
        Object value = section.getProperty(UNITS_PROPERTY);
        if (value != null) {
            try {
                return Units.valueOf(value.toString());
            } catch (IllegalArgumentException ex) {
                // fall through to the default
            }
        }
        return defaultUnits();
    }

    /**
     * Keep the units a Section's stop points were entered in. They are kept
     * only while the Section has a stop point.
     *
     * @param section the Section
     * @param units   the units
     */
    public static void setUnits(@Nonnull Section section, @Nonnull Units units) {
        if (section.getStopPointMm(Section.FORWARD) > 0.0f || section.getStopPointMm(Section.REVERSE) > 0.0f) {
            section.setProperty(UNITS_PROPERTY, units.name());
        } else {
            section.removeProperty(UNITS_PROPERTY);
        }
    }

    /**
     * Read a stop point as entered.
     *
     * @param text  the text: blank or 0 for none; in millimetres a number such
     *              as "420" or "420.5"; in inches a number such as "12" or
     *              "12.25", a whole number and a fraction such as "12 7/32",
     *              or a fraction such as "7/32"; a trailing "mm", "in" or
     *              {@code "} is allowed
     * @param units the units it is in
     * @return the stop point in millimetres; in inches it is rounded to the
     *         nearest 1/32 inch first
     * @throws ParseException if the text is not a distance of 0 or more
     */
    public static float parse(@Nonnull String text, @Nonnull Units units) throws ParseException {
        return parse(text, units, Locale.getDefault());
    }

    /**
     * Read a stop point as entered, with the decimal point of a locale.
     *
     * @param text   the text, as for {@link #parse(String, Units)}
     * @param units  the units it is in
     * @param locale the locale whose decimal point is used
     * @return the stop point in millimetres
     * @throws ParseException if the text is not a distance of 0 or more
     */
    public static float parse(@Nonnull String text, @Nonnull Units units, @Nonnull Locale locale)
            throws ParseException {
        String s = stripUnits(text.trim(), units);
        if (s.isEmpty()) {
            return 0.0f;
        }
        if (units == Units.MM) {
            return (float) decimal(s, locale);
        }
        double inches;
        Matcher m = MIXED.matcher(s);
        if (m.matches()) {
            inches = Integer.parseInt(m.group(1)) + fraction(m.group(2), m.group(3), text);
        } else {
            m = FRACTION.matcher(s);
            if (m.matches()) {
                inches = fraction(m.group(1), m.group(2), text);
            } else {
                inches = decimal(s, locale);
            }
        }
        long parts = Math.round(inches * INCH_PARTS);
        return (float) (parts * MM_PER_INCH / INCH_PARTS);
    }

    private static String stripUnits(String s, Units units) {
        String lower = s.toLowerCase(Locale.ROOT);
        String[] suffixes = units == Units.MM
                ? new String[]{"mm"} // NOI18N
                : new String[]{"inches", "inch", "in", "\"", "″"}; // NOI18N
        for (String suffix : suffixes) {
            if (lower.endsWith(suffix)) {
                return s.substring(0, s.length() - suffix.length()).trim();
            }
        }
        return s;
    }

    private static double fraction(String numerator, String denominator, String text) throws ParseException {
        int d = Integer.parseInt(denominator);
        if (d == 0) {
            throw new ParseException(text, 0);
        }
        return Integer.parseInt(numerator) / (double) d;
    }

    private static double decimal(String s, Locale locale) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Number n = NumberFormat.getInstance(locale).parse(s, pos);
        if (n == null || pos.getIndex() != s.length() || n.doubleValue() < 0 || Double.isNaN(n.doubleValue())
                || Double.isInfinite(n.doubleValue())) {
            throw new ParseException(s, pos.getErrorIndex() < 0 ? pos.getIndex() : pos.getErrorIndex());
        }
        return n.doubleValue();
    }

    /**
     * Show a stop point.
     *
     * @param mm    the stop point in millimetres; 0 or less for none
     * @param units the units to show it in
     * @return "" for none; in millimetres a number such as "420" or "420.5";
     *         in inches a whole number and a fraction in its lowest terms,
     *         such as "12 7/32", "12 1/2", "12" or "7/32"
     */
    @Nonnull
    public static String format(float mm, @Nonnull Units units) {
        return format(mm, units, Locale.getDefault());
    }

    /**
     * Show a stop point, with the decimal point of a locale.
     *
     * @param mm     the stop point in millimetres; 0 or less for none
     * @param units  the units to show it in
     * @param locale the locale whose decimal point is used
     * @return as for {@link #format(float, Units)}
     */
    @Nonnull
    public static String format(float mm, @Nonnull Units units, @Nonnull Locale locale) {
        if (!(mm > 0.0f)) {
            return "";
        }
        if (units == Units.MM) {
            NumberFormat f = NumberFormat.getInstance(locale);
            f.setMaximumFractionDigits(1);
            f.setGroupingUsed(false);
            return f.format(mm);
        }
        long parts = Math.round(mm / MM_PER_INCH * INCH_PARTS);
        long whole = parts / INCH_PARTS;
        long numerator = parts % INCH_PARTS;
        if (numerator == 0) {
            return Long.toString(whole);
        }
        long denominator = INCH_PARTS;
        while (numerator % 2 == 0) {
            numerator /= 2;
            denominator /= 2;
        }
        String fraction = numerator + "/" + denominator;
        return whole == 0 ? fraction : whole + " " + fraction;
    }

    /**
     * The Section length a stop point is checked against: the sum of the
     * lengths of its Blocks.
     *
     * @param blocks the Section's Blocks
     * @return the length in millimetres
     */
    public static float lengthMm(@Nonnull Iterable<jmri.Block> blocks) {
        float length = 0.0f;
        for (jmri.Block b : blocks) {
            length += b.getLengthMm();
        }
        return length;
    }

    /**
     * @param blocks the Section's Blocks
     * @return the first Block with no length, or null if they all have one
     */
    @CheckForNull
    public static jmri.Block blockWithNoLength(@Nonnull Iterable<jmri.Block> blocks) {
        for (jmri.Block b : blocks) {
            if (!(b.getLengthMm() > 0.0f)) {
                return b;
            }
        }
        return null;
    }
}
