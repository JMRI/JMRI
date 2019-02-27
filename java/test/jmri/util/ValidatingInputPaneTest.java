package jmri.util;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import java.awt.BorderLayout;
import java.util.concurrent.Callable;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Svata Dedic Copyright (c) 2019
 */
public class ValidatingInputPaneTest {
    @Rule
    public AssumeGUIRule nonHeadless = new AssumeGUIRule();
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    /**
     * Checks that inconvertible input produces an error message.
     */
    @Test
    public void testInconvertibleInputFails() {
        ValidatingInputPane<Integer> intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        intValidator.validateText("nonInteger");
        assertTrue("Non-number must produce error", intValidator.hasError());
        assertNotNull("Converter exception expected", intValidator.getException());
        assertTrue(intValidator.getException() instanceof NumberFormatException);
    }
    
    /**
     * Empty input should be accepted as no-op
     */
    @Test
    public void testEmptyInputOK() {
        ValidatingInputPane<Integer> intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        intValidator.validateText("");
        assertFalse("Empty string must be accepted", intValidator.hasError());
    }
    
    @Test
    public void testValidationRejects() {
        ValidatingInputPane<Integer> intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        intValidator.validator((i) -> i >= 5);
        intValidator.validateText("3");
        
        assertTrue("Bad number should produce error", intValidator.hasError());
        assertNull("Validator rejection does not record an exception", intValidator.getException());
        assertEquals("General message expected", Bundle.getMessage("InputDialogError"), intValidator.getErrorMessage());
    }
    
    @Test
    public void testValidationPasses() {
        ValidatingInputPane<Integer> intValidator = new ValidatingInputPane<>((s) -> Integer.parseInt(s));
        intValidator.validator((i) -> i >= 5);
        intValidator.validateText("6");
        
        assertFalse("Validation must pass", intValidator.hasError());
        assertNull("Exception must be null on OK result", intValidator.getException());
        assertNull("No message for OK result", intValidator.getErrorMessage());
    }
    
    @Test
    public void testValidationThrowsCustomMessage() {
        setupLargeValidator();
        // now should fail:
        intValidator.validateText("6");
        assertNotNull("Exception must non-null", intValidator.getException());
        assertEquals("Exception must non-null", "Large", intValidator.getException().getMessage());
        assertEquals("Invalid message", "Large", intValidator.getErrorMessage());

        // should pass
        intValidator.validateText("3");
        assertNull("Message must be null on successful validation", intValidator.getErrorMessage());
        assertFalse("No error must be indicated", intValidator.hasError());
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
        assertNotNull("Exception must non-null", intValidator.getException());

        // should pass
        intValidator.validateText("3");
        assertNull("Message must be null on successful validation", intValidator.getErrorMessage());
        assertFalse("No error must be indicated", intValidator.hasError());
    }
    
    private <T> T testInGUI(Callable<T> check) throws Exception {
        // display the panel
        JWindow dlg = new JWindow();
        dlg.setLayout(new BorderLayout());
        dlg.add(intValidator, BorderLayout.CENTER);
        if (control != null) {
            dlg.add(control, BorderLayout.SOUTH);
        }
        dlg.show();
        
        Exception[] out = new Exception[1];
        Object[] result = new Object[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                result[0] = check.call();
            } catch (Exception ex) {
                out[0] = ex;
            }
        });
        
        dlg.dispose();
        
        if (out[0] != null) {
            throw out[0];
        }
        return (T)result[0];
    }
    
    @Test
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
}
