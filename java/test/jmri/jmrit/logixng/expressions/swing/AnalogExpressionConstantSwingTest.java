package jmri.jmrit.logixng.expressions.swing;

import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JPanel;

import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Tests for AnalogExpressionConstantSwing.
 * @author Steve Young Copyright (C) 2025
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class AnalogExpressionConstantSwingTest {

    @Test
    void testDecimalEnglish() {
        AnalogExpressionConstantSwing t = new AnalogExpressionConstantSwing();
        Assertions.assertNotNull(t);

        JPanel buttonPanel = new JPanel();
        ArrayList<String> list = new ArrayList<>();

        AnalogExpressionConstant aec = new AnalogExpressionConstant("IQAE915",null);
        aec.setValue(1.23d);

        t.createPanel(aec, buttonPanel);

        JPanel p = t.getConfigPanel(aec, buttonPanel);

        JFrame f = new JFrame("AECST");
        f.getContentPane().add(p);
        f.pack();

        ThreadingUtil.runOnGUI( () -> f.setVisible(true));

        JFrameOperator jfo = new JFrameOperator(f);
        JTextFieldOperator jtfo = new JTextFieldOperator(jfo);

        assertEquals( 1.23d, aec.getValue());

        jtfo.setText("");
        assertTrue(t.validate(list));
        t.updateObject(aec);
        assertEquals( 0d, aec.getValue());

        jtfo.setText("Not A Number");
        assertFalse(t.validate(list));

        jtfo.setText("1.23");
        assertTrue(t.validate(list));
        t.updateObject(aec);
        assertEquals( 1.23d, aec.getValue());

        JUnitUtil.dispose(f);

    }

    @Test
    void testDecimalItalian() {
        AnalogExpressionConstantSwing t = new AnalogExpressionConstantSwing();
        assertNotNull(t);

        JPanel buttonPanel = new JPanel();
        ArrayList<String> list = new ArrayList<>();

        AnalogExpressionConstant aec = new AnalogExpressionConstant("IQAE444",null);
        aec.setValue(4.44);

        Locale existing = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ITALIAN);

            t.createPanel(aec, buttonPanel);
            JPanel p = t.getConfigPanel(aec, buttonPanel);

            JFrame f = new JFrame("AECST444");
            f.getContentPane().add(p);
            f.pack();

            ThreadingUtil.runOnGUI( () -> f.setVisible(true));

            JFrameOperator jfo = new JFrameOperator(f);
            JTextFieldOperator jtfo = new JTextFieldOperator(jfo);
            assertEquals("4,44", jtfo.getText());

            jtfo.setText("7,89");
            assertTrue(t.validate(list));
            t.updateObject(aec);
            assertEquals( 7.89d, aec.getValue());

            JUnitUtil.dispose(f);

        } finally {
            Locale.setDefault(existing);
        }

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
