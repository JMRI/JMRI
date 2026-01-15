package jmri.util;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Svata Dedic Copyright (c) 2019
 */
public class ValidatingInputPaneTest {

    /**
     * Checks that inconvertible input produces an error message.
     */
    @Test
    public void testInconvertibleInputFails() {
        ValidatingInputPane<Integer> intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        intValidator.validateText("nonInteger");
        assertTrue( intValidator.hasError(), "Non-number must produce error");
        NumberFormatException ex = assertInstanceOf(NumberFormatException.class, intValidator.getException());
        assertNotNull( ex, "Converter exception expected");
    }

    /**
     * Empty input should be accepted as no-op
     */
    @Test
    public void testEmptyInputOK() {
        ValidatingInputPane<Integer> intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        intValidator.validateText("");
        assertFalse( intValidator.hasError(), "Empty string must be accepted");
    }

    @Test
    public void testValidationRejects() {
        ValidatingInputPane<Integer> intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        intValidator.validator((i) -> i >= 5);
        intValidator.validateText("3");

        assertTrue( intValidator.hasError(), "Bad number should produce error");
        assertNull( intValidator.getException(), "Validator rejection does not record an exception");
        assertEquals( Bundle.getMessage("InputDialogError"), intValidator.getErrorMessage(), "General message expected");
    }

    @Test
    public void testValidationPasses() {
        ValidatingInputPane<Integer> intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        intValidator.validator((i) -> i >= 5);
        intValidator.validateText("6");

        assertFalse( intValidator.hasError(), "Validation must pass");
        assertNull( intValidator.getException(), "Exception must be null on OK result");
        assertNull( intValidator.getErrorMessage(), "No message for OK result");
    }

    @Test
    public void testValidationThrowsCustomMessage() {
        setupLargeValidator();
        // now should fail:
        intValidator.validateText("6");
        assertNotNull( intValidator.getException(), "Exception must non-null");
        assertEquals( "Large", intValidator.getException().getMessage(), "Exception must non-null");
        assertEquals( "Large", intValidator.getErrorMessage(), "Invalid message");

        // should pass
        intValidator.validateText("3");
        assertNull( intValidator.getErrorMessage(), "Message must be null on successful validation");
        assertFalse( intValidator.hasError(), "No error must be indicated");
    }

    private ValidatingInputPane<Integer> intValidator;
    private JButton control;

    private void setupLargeValidator() {
        setupLargeValidator(false);
    }

    private void setupLargeValidator(boolean useControl) {
        intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        if (useControl) {
            control = new JButton("OK");
            intValidator = intValidator.attachConfirmUI(control);
        }
        intValidator.validator((i) -> {
           if (i >= 5) {
               throw new IllegalArgumentException("Large");
           }
           return true;
        });
    }

    @Test
    public void testErrorStatusClearedAfterCorrection() {
        setupLargeValidator();
        // now should fail:
        intValidator.validateText("6");
        assertNotNull( intValidator.getException(), "Exception must non-null");

        // should pass
        intValidator.validateText("3");
        assertNull( intValidator.getErrorMessage(), "Message must be null on successful validation");
        assertFalse( intValidator.hasError(), "No error must be indicated");
    }

    /**
     * Wraps the tested panel into a Window and makes it visible. Runs the
     * Callable in EDT to properly process UI. Terminate
     * the test without failure in headless env.
     */
    private <T> void testInGUI(Callable<T> check) throws Exception {
        // terminate tests which require GUI
        assertFalse(GraphicsEnvironment.isHeadless());

        // display the panel
        JWindow dlg = new JWindow();
        dlg.setLayout(new BorderLayout());
        dlg.add(intValidator, BorderLayout.CENTER);
        if (control != null) {
            dlg.add(control, BorderLayout.SOUTH);
        }
        dlg.setVisible(true);

        Exception[] out = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                check.call();
            } catch (Exception ex) {
                out[0] = ex;
            }
        });

        dlg.dispose();

        if (out[0] != null) {
            throw out[0];
        }
    }

    @Test
    @DisabledIfHeadless
    public void testValidationHappensWhenAfterDisplayed() throws Exception {
        setupLargeValidator();

        intValidator.setText("6");
        // no validation yet.
        assertNull(intValidator.getErrorMessage());
        testInGUI(() -> {
            assertTrue(intValidator.hasError());
            return null;
        });
    }

    @Test
    @DisabledIfHeadless
    public void testTextChangeValidatesFalse() throws Exception {
        setupLargeValidator();

        intValidator.setText("3");
        // no validation yet.
        assertNull(intValidator.getErrorMessage());
        testInGUI(() -> {
            assertFalse(intValidator.hasError());

            JTextField input = intValidator.getTextField();
            input.getDocument().insertString(1, "3", null);

            // now the text should be validated and error message present
            assertTrue(intValidator.hasError());
            assertNotNull(intValidator.getException());
            return null;
        });
    }

    @Test
    @DisabledIfHeadless
    public void testClearAllNoError() throws Exception {
        setupLargeValidator();
        intValidator.setText("6");
        // no validation yet.
        assertNull(intValidator.getErrorMessage());
        testInGUI(() -> {
            assertTrue(intValidator.hasError());

            JTextField input = intValidator.getTextField();
            input.setSelectionStart(0);
            input.setSelectionEnd(1);
            // should clear the text
            input.replaceSelection("");

            // now the text should be validated and error message present
            assertFalse(intValidator.hasError());
            assertNull(intValidator.getException());
            return null;
        });
    }

    @Test
    @DisabledIfHeadless
    public void testControlBecomesDisabled() throws Exception {
        setupLargeValidator(true);
        intValidator.setText("4");
        testInGUI(() -> {
           assertTrue(control.isEnabled());
           intValidator.getTextField().replaceSelection("44");
           assertFalse(control.isEnabled());
           return null;
        });
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

}
