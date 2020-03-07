package jmri.util;

import java.awt.Component;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A helper Panel for input-validating input boxes. It converts and validates the
 * text input, disabling {@link #confirmUI} component (usually a button) when
 * the input is not valid.
 * 
 * @author Svata Dedic Copyright (c) 2019
 */
final class ValidatingInputPane<T> extends javax.swing.JPanel  {
    private final Function<String, T> convertor;
    private final DocumentListener l = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            validateInput();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateInput();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    };

    /**
     * Callback that validates the input after conversion.
     */
    private Predicate<T>    validator;

    /**
     * The confirmation component. The component is disabled when the
     * input is rejected by converter or validator
     */
    private JComponent      confirmUI;
    
    /**
     * Holds the last seen error. {@code null} for no error - valid input
     */
    private String lastError;
    
    /**
     * Last custom exception. {@code null}, if no error or if
     * the validator just rejected with no message.
     */
    private IllegalArgumentException customException;
    
    /**
     * Creates new form ValidatingInputPane.
     * @param convertor converts String to the desired data type.
     */
    public ValidatingInputPane(Function<String, T> convertor) {
        initComponents();
        errorMessage.setVisible(false);
        this.convertor = convertor;
    }
    
    /**
     * Attaches a component used to confirm/proceed. The component will
     * be disabled if the input is erroneous. The first validation will happen
     * after this component appears on the screen. Typically, the OK button
     * should be passed here.
     * 
     * @param confirm the "confirm" control.
     * @return this instance.
     */
    ValidatingInputPane<T> attachConfirmUI(JComponent confirm) {
        this.confirmUI = confirm;
        return this;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        inputText.getDocument().addDocumentListener(l);
        SwingUtilities.invokeLater(this::validateInput);
    }
    
    /**
     * Configures a prompt message for the panel. The prompt message
     * appears above the input line.
     * @param msg message text
     * @return this instance.
     */
    ValidatingInputPane<T> message(String msg) {
        promtptMessage.setText(msg);
        return this;
    }
    
    /**
     * Returns the exception from the most recent validation. Only exceptions
     * from unsuccessful conversion or thrown by validator are returned. To check
     * whether the input is valid, call {@link #hasError()}. If the validator 
     * just rejects the input with no exception, this method returns {@code null}
     * @return exception thrown by converter or validator.
     */
    IllegalArgumentException getException() {
        return customException;
    }
    
    /**
     * Configures the validator. Validator is called to check the value after
     * the String input is converted to the target type. The validator can either
     * just return {@code false} to reject the value with a generic message, or
     * throw a {@link IllegalArgumentException} subclass with a custom message.
     * The message will be then displayed below the input line.
     * 
     * @param val validator instance, {@code null} to disable.
     * @return this instance
     */
    ValidatingInputPane<T> validator(Predicate<T> val) {
        this.validator = val;
        return this;
    }
    
    /**
     * Determines if the input is erroneous.
     * @return error status
     */
    boolean hasError() {
        return lastError != null;
    }
    
    /**
     * Sets the input value, as text.
     * @param text input text
     */
    void setText(String text) {
        inputText.setText(text);
    }
    
    /**
     * Gets the input value, as text.
     * @return the input text
     */
    String getText() {
        return inputText.getText().trim();
    }
    
    /**
     * Gets the input value after conversion. May throw {@link IllegalArgumentException}
     * if the conversion fails (text input cannot be converted to the target type).
     * Returns {@code null} for empty (all whitespace) input.
     * @return the entered value or {@code null} for empty input.
     */
    T getValue() {
        String s = getText();
        return s.isEmpty() ? null : convertor.apply(s);
    }
    
    /**
     * Gets the error message. Either a custom message from an exception
     * thrown by converter or validator, or the default message for failed validation.
     * Returns {@code null} for valid input.
     * @return if input is invalid, returns the error message. If the input is valid, returns {@code null}.
     */
    String getErrorMessage() {
        return lastError;
    }
    
    private void validateInput() {
        if (isVisible()) {
            validateText(getText());
        }
    }
    
    private void clearErrors() {
        if (confirmUI != null) {
            confirmUI.setEnabled(true);
        }
        errorMessage.setText("");
        errorMessage.setVisible(false);
        customException = null;
        lastError = null;
    }
    
    /**
     * Should be called from tests only
     */
    void validateText(String text) {
        String msg;
        if (text.isEmpty()) {
            clearErrors();
            return;
        }
        try {
            T value = convertor.apply(text);
            if (validator == null || 
                validator.test(value)) {
                clearErrors();
                return;
            }
            msg = Bundle.getMessage("InputDialogError");
        } catch (IllegalArgumentException ex) {
            msg = ex.getLocalizedMessage();
            customException = ex;
        }
        lastError = msg;
        errorMessage.setText(msg);
        errorMessage.setVisible(true);
        if (confirmUI != null) {
            confirmUI.setEnabled(false);
        }
        Component c = SwingUtilities.getRoot(this);
        if (c != null) {
            c.invalidate();
            if (c instanceof JDialog) {
                ((JDialog)c).pack();
            }
        }
    }

    // only for testing
    JTextField getTextField() {
        return inputText;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        promtptMessage = new javax.swing.JLabel();
        inputText = new javax.swing.JTextField();
        errorMessage = new javax.swing.JTextArea();

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        promtptMessage.setText(" ");

        errorMessage.setEditable(false);
        errorMessage.setBackground(getBackground());
        errorMessage.setColumns(20);
        errorMessage.setForeground(java.awt.Color.red);
        errorMessage.setRows(2);
        errorMessage.setToolTipText("");
        errorMessage.setAutoscrolls(false);
        errorMessage.setBorder(null);
        errorMessage.setFocusable(false);
        errorMessage.setRequestFocusEnabled(false);
        errorMessage.setVerifyInputWhenFocusTarget(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(promtptMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(errorMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                    .addComponent(inputText))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(promtptMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inputText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea errorMessage;
    private javax.swing.JTextField inputText;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JLabel promtptMessage;
    // End of variables declaration//GEN-END:variables
}
