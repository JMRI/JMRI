package jmri.util;

import javax.swing.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the AccessibilityChecks class.
 *
 * @author Steve Young Copyright (C) 2022
 */
public class AccessibilityChecksTest {

    @Test
    public void testPassButtonInPanel() {
        JButton b = new JButton("Test Button");
        JPanel p = new JPanel();
        p.add(b);
        Assertions.assertEquals("", AccessibilityChecks.check(p),"no issues found");
    }

    @Test
    public void testPassButtonInPanelinFrame() {
        JButton b = new JButton("Test Button");
        JPanel p = new JPanel();
        p.add(b);
        JFrame f = new JFrame();
        f.add(p);
        Assertions.assertEquals("", AccessibilityChecks.check(f),"no issues found");
    }

    @Test
    public void testPanelButtonFailEmpty() {

        JButton b = new JButton("Test Button");
        b.getAccessibleContext().setAccessibleName("");
        b.getAccessibleContext().setAccessibleDescription("");
        JPanel p = new JPanel();
        p.add(b);
        Assertions.assertTrue(AccessibilityChecks.check(p).startsWith("1 Potential Issue"));
    }

    @Test
    public void testPassJSpinnerInPanel() {
        JSpinner b = new JSpinner();
        b.getAccessibleContext().setAccessibleName("Test Spinner");
        JPanel p = new JPanel();
        p.add(b);
        Assertions.assertTrue(AccessibilityChecks.check(p).startsWith("1 Potential Issue"));
    }

    @Test
    public void testPassJScrollPaneInPanel() {
        JScrollPane b = new JScrollPane(new JButton("Test Button"));
        b.getAccessibleContext().setAccessibleName("Test Scroll Pane");
        JPanel p = new JPanel();
        p.add(b);
        Assertions.assertEquals("", AccessibilityChecks.check(p),"no issues found");
    }

    @Test
    public void testPassJComboBoxInPanel() {
        JComboBox<String> b = new JComboBox<>(new String[]{"Foo", "Bar"});
        b.getAccessibleContext().setAccessibleName("Test ComboBox");
        JPanel p = new JPanel();
        p.add(b);
        Assertions.assertEquals("", AccessibilityChecks.check(p),"no issues found");
    }

    @Test
    public void testFailButtonInPanelinFrame() {
        JButton b = new JButton("Test Button");
        b.getAccessibleContext().setAccessibleName("");
        b.getAccessibleContext().setAccessibleDescription("");
        JPanel p = new JPanel();
        p.add(b);
        JFrame f = new JFrame();
        f.add(p);
        Assertions.assertTrue(AccessibilityChecks.check(p).startsWith("1 Potential Issue"));
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
