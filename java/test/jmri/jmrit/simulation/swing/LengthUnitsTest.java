package jmri.jmrit.simulation.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.beantable.BlockTableAction;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Lengths in whole millimetres, or in inches to 1/32 inch.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LengthUnitsTest {

    @Test
    public void testUnitsFollowTheBlockTable() {
        UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
        p.setSimplePreferenceState(BlockTableAction.BLOCK_METRIC_PREF, false);
        assertFalse(LengthUnits.fromPreferences().isMetric(), "Block lengths in inches");
        assertEquals("in", LengthUnits.fromPreferences().getName());
        p.setSimplePreferenceState(BlockTableAction.BLOCK_METRIC_PREF, true);
        assertTrue(LengthUnits.fromPreferences().isMetric(), "Block lengths in centimetres");
        assertEquals("mm", LengthUnits.fromPreferences().getName());
    }

    @Test
    public void testMillimetresAreWhole() {
        LengthUnits mm = new LengthUnits(true);
        assertEquals(200.0, mm.fromMm(200.4));
        assertEquals(201.0, mm.fromMm(200.6));
        assertEquals(200.0, mm.toMm(200.0));
        assertEquals(1.0, mm.step());
    }

    @Test
    public void testInchesAreToAThirtySecond() {
        LengthUnits in = new LengthUnits(false);
        assertEquals(1.0 / 32, in.step());
        // 200 mm is 7.874 inches: 251.97 thirty-seconds, so 252/32 = 7 7/8
        assertEquals(7.875, in.fromMm(200.0));
        assertEquals(25.4, in.toMm(1.0), 1e-9);
        assertEquals(200.025, in.toMm(7.875), 1e-9);
        // 1/32 inch is 0.79375 mm: half of it rounds up, less rounds down
        assertEquals(1.0 / 32, in.fromMm(0.4));
        assertEquals(0.0, in.fromMm(0.39));
    }

    @Test
    public void testInchesAreShownAsFractions() {
        assertEquals("7 7/8", LengthUnits.inchesText(7.875));
        assertEquals("7", LengthUnits.inchesText(7.0));
        assertEquals("3/16", LengthUnits.inchesText(0.1875));
        assertEquals("1/32", LengthUnits.inchesText(1.0 / 32));
        assertEquals("12 31/32", LengthUnits.inchesText(12 + 31.0 / 32));
        assertEquals("0", LengthUnits.inchesText(0.0));
        assertEquals("8", LengthUnits.inchesText(7.99), "to the nearest 1/32");
    }

    @Test
    public void testInchesAreReadAsWritten() throws ParseException {
        assertEquals(7.1875, LengthUnits.parseInches("7 3/16"));
        assertEquals(7.1875, LengthUnits.parseInches("  7  3 / 16  "));
        assertEquals(0.1875, LengthUnits.parseInches("3/16"));
        assertEquals(7.0, LengthUnits.parseInches("7"));
        assertEquals(7.0, LengthUnits.parseInches("7 in"));
        assertEquals(7.0, LengthUnits.parseInches("7\""));
        assertEquals(7.1875, LengthUnits.parseInches("7.1875"));
        assertEquals(7.1875, LengthUnits.parseInches("7.19"), "to the nearest 1/32");
        assertEquals(0.5, LengthUnits.parseInches(".5"));
        assertEquals(0.75, LengthUnits.parseInches("6/8"), "a fraction not in its lowest terms");
        for (String bad : new String[]{"", "seven", "7 3/0", "7-3/16", "3/", "1/2/3", "7 3"}) {
            assertThrows(ParseException.class, () -> LengthUnits.parseInches(bad), bad);
        }
    }

    @Test
    public void testSpinnerSteps() {
        JSpinner mm = ThreadingUtil.runOnGUIwithReturn(() -> new LengthUnits(true).spinner(200.0, 1.0, 1000.0));
        assertEquals(200.0, mm.getValue());
        ThreadingUtil.runOnGUI(() -> mm.setValue(mm.getNextValue()));
        assertEquals(201.0, mm.getValue(), "1 mm a step");

        JSpinner in = ThreadingUtil.runOnGUIwithReturn(() -> new LengthUnits(false).spinner(200.0, 1.0, 1000.0));
        assertEquals(7.875, in.getValue());
        JFormattedTextField field = ((JSpinner.DefaultEditor) in.getEditor()).getTextField();
        assertEquals("7 7/8", field.getText());
        ThreadingUtil.runOnGUI(() -> in.setValue(in.getNextValue()));
        assertEquals(7.90625, in.getValue(), "1/32 inch a step");
        assertEquals("7 29/32", field.getText());
        assertTrue(field.isEditable());

        ThreadingUtil.runOnGUI(() -> {
            field.setText("9 1/4");
            try {
                field.commitEdit();
            } catch (ParseException e) {
                fail("9 1/4 not taken", e);
            }
        });
        assertEquals(9.25, in.getValue(), "typed as a fraction");

        JSpinner front = ThreadingUtil.runOnGUIwithReturn(() -> new LengthUnits(false).spinner(0.0, 0.0, 1000.0));
        assertEquals(0.0, front.getValue(), "a length that may be nothing starts at 0");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
