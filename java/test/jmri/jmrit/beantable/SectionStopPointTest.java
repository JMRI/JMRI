package jmri.jmrit.beantable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.UserPreferencesManager;
import jmri.implementation.DefaultSection;
import jmri.jmrit.beantable.SectionStopPoint.Units;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Stop points entered and shown in millimetres or inches.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SectionStopPointTest {

    private static final float INCH = 25.4f;

    private static float mm(String text) throws ParseException {
        return SectionStopPoint.parse(text, Units.MM, Locale.UK);
    }

    private static float in(String text) throws ParseException {
        return SectionStopPoint.parse(text, Units.INCHES, Locale.UK);
    }

    @Test
    public void testMillimetres() throws ParseException {
        assertEquals(0.0f, mm(""), "blank is none");
        assertEquals(0.0f, mm("  "));
        assertEquals(420.0f, mm("420"));
        assertEquals(420.5f, mm("420.5"));
        assertEquals(420.0f, mm("420 mm"));
        assertEquals(420.0f, mm("420mm"));
        assertEquals(420.5f, SectionStopPoint.parse("420,5", Units.MM, Locale.GERMANY), "the locale's decimal point");
    }

    @Test
    public void testInches() throws ParseException {
        assertEquals(12 * INCH, in("12"), 0.001f);
        assertEquals(12.25f * INCH, in("12.25"), 0.001f);
        assertEquals((12 + 7 / 32f) * INCH, in("12 7/32"), 0.001f);
        assertEquals((12 + 7 / 32f) * INCH, in("12-7/32"), 0.001f);
        assertEquals((12 + 7 / 32f) * INCH, in("12  7 / 32"), 0.001f);
        assertEquals(7 / 32f * INCH, in("7/32"), 0.001f);
        assertEquals((12 + 7 / 32f) * INCH, in("391/32"), 0.001f);
        assertEquals((12 + 1 / 2f) * INCH, in("12 1/2"), 0.001f, "any fraction");
        assertEquals((12 + 7 / 32f) * INCH, in("12 7/32 in"), 0.001f);
        assertEquals((12 + 7 / 32f) * INCH, in("12 7/32\""), 0.001f);
        assertEquals(12 * INCH, in("12 inches"), 0.001f);
        assertEquals(0.0f, in(""));
    }

    @Test
    public void testInchesRoundToTheNearest32nd() throws ParseException {
        assertEquals((12 + 7 / 32f) * INCH, in("12.22"), 0.001f, "12.22 is nearest 12 7/32");
        assertEquals((12 + 7 / 32f) * INCH, in("12 21/96"), 0.001f, "21/96 is 7/32");
        assertEquals((1 / 32f) * INCH, in("0.02"), 0.001f, "0.02 is nearest 1/32");
        assertEquals(0.0f, in("0.01"), 0.001f, "0.01 is nearest 0");
    }

    @Test
    public void testNotADistance() {
        for (String text : new String[]{"about here", "12x", "-5", "12 7/0", "7/0", "12 7/", "1.2.3", "12 mm in"}) {
            assertThrows(ParseException.class, () -> in(text), "inches: " + text);
        }
        for (String text : new String[]{"about here", "12x", "-5", "12 in", "7/32"}) {
            assertThrows(ParseException.class, () -> mm(text), "mm: " + text);
        }
    }

    @Test
    public void testFormat() {
        assertEquals("", SectionStopPoint.format(0.0f, Units.MM, Locale.UK), "none");
        assertEquals("", SectionStopPoint.format(0.0f, Units.INCHES, Locale.UK), "none");
        assertEquals("420", SectionStopPoint.format(420.0f, Units.MM, Locale.UK));
        assertEquals("420.5", SectionStopPoint.format(420.5f, Units.MM, Locale.UK));
        assertEquals("1420.5", SectionStopPoint.format(1420.5f, Units.MM, Locale.UK), "no thousands separator");
        assertEquals("420,5", SectionStopPoint.format(420.5f, Units.MM, Locale.GERMANY));
        assertEquals("12 7/32", SectionStopPoint.format((12 + 7 / 32f) * INCH, Units.INCHES, Locale.UK));
        assertEquals("12 1/2", SectionStopPoint.format(12.5f * INCH, Units.INCHES, Locale.UK), "lowest terms");
        assertEquals("12", SectionStopPoint.format(12 * INCH, Units.INCHES, Locale.UK));
        assertEquals("7/32", SectionStopPoint.format(7 / 32f * INCH, Units.INCHES, Locale.UK));
        assertEquals("10", SectionStopPoint.format(254.0f, Units.INCHES, Locale.UK));
    }

    @Test
    public void testEvery32ndReadsBackAsShown() throws ParseException {
        for (int parts = 1; parts <= 32 * 120; parts++) {
            float mm = parts * INCH / 32;
            String shown = SectionStopPoint.format(mm, Units.INCHES, Locale.UK);
            assertEquals(mm, in(shown), 0.001f, shown);
        }
    }

    @Test
    public void testUnitsKeptWithTheSection() {
        Section s = new DefaultSection("TS1");
        InstanceManager.getDefault(UserPreferencesManager.class)
                .setSimplePreferenceState(BlockTableAction.BLOCK_METRIC_PREF, true);
        assertSame(Units.MM, SectionStopPoint.defaultUnits(), "the Block table shows cm");
        assertSame(Units.MM, SectionStopPoint.getUnits(s), "none kept: the default");
        InstanceManager.getDefault(UserPreferencesManager.class)
                .setSimplePreferenceState(BlockTableAction.BLOCK_METRIC_PREF, false);
        assertSame(Units.INCHES, SectionStopPoint.defaultUnits(), "the Block table shows inches");

        SectionStopPoint.setUnits(s, Units.MM);
        assertNull(s.getProperty(SectionStopPoint.UNITS_PROPERTY), "not kept with no stop point");
        s.setStopPointMm(Section.REVERSE, 300.0f);
        SectionStopPoint.setUnits(s, Units.MM);
        assertSame(Units.MM, SectionStopPoint.getUnits(s), "kept");
        s.setProperty(SectionStopPoint.UNITS_PROPERTY, "FURLONGS");
        assertSame(Units.INCHES, SectionStopPoint.getUnits(s), "unknown: the default");
    }

    @Test
    public void testSectionLength() {
        Block a = new Block("IB1");
        Block b = new Block("IB2");
        a.setLength(300.0f);
        assertSame(b, SectionStopPoint.blockWithNoLength(List.of(a, b)));
        b.setLength(200.0f);
        assertNull(SectionStopPoint.blockWithNoLength(List.of(a, b)));
        assertEquals(500.0f, SectionStopPoint.lengthMm(List.of(a, b)));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
