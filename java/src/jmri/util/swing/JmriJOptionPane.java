package jmri.util.swing;

import java.awt.*;
import java.util.Locale;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * JmriJOptionPane provides a set of static methods to display Dialogs and retrieve user input.
 * These can directly replace the javax.swing.JOptionPane static methods.
 * <p>
 * If the parentComponent is null, all Dialogs created will be Modal.
 * These will block the whole JVM UI until they are closed.
 * These may appear behind Window frames with Always On Top enabled and may not be accessible.
 * These Dialogs are positioned in the centre of the screen.
 * <p>
 * If a parentComponent is provided, the Dialogs will be created Modal to
 * ( will block ) the parent Window Frame, other Frames are not blocked.
 * These Dialogs will appear in the centre of the parent Frame.
 *
 * @since 5.5.4
 * @author Steve Young Copyright (C) 2023
 */
public class JmriJOptionPane {

    public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
    public static final int OK_OPTION = JOptionPane.OK_OPTION;
    public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;
    public static final int YES_OPTION = JOptionPane.YES_OPTION;
    public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
    public static final int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
    public static final int NO_OPTION = JOptionPane.NO_OPTION;

    public static final int CLOSED_OPTION = JOptionPane.CLOSED_OPTION;
    public static final int DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;
    public static final Object UNINITIALIZED_VALUE = JOptionPane.UNINITIALIZED_VALUE;

    public static final int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
    public static final int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
    public static final int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;
    public static final int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;

    public static final String YES_STRING = UIManager.getString("OptionPane.yesButtonText", Locale.getDefault());
    public static final String NO_STRING = UIManager.getString("OptionPane.noButtonText", Locale.getDefault());

    // class only supplies static methods
    protected JmriJOptionPane(){}

    /**
     * Displays an informational message dialog with an OK button.
     * @param parentComponent The parent component relative to which the dialog is displayed.
     * @param message         The message to be displayed in the dialog.
     * @throws HeadlessException if the current environment is headless (no GUI available).
     */
    public static void showMessageDialog(@CheckForNull Component parentComponent,
        Object message) throws HeadlessException {
        showMessageDialog(parentComponent, message, 
            UIManager.getString("OptionPane.messageDialogTitle", Locale.getDefault()),
            INFORMATION_MESSAGE);
    }

    /**
     * Displays a message dialog with an OK button.
     * @param parentComponent The parent component relative to which the dialog is displayed.
     * @param message         The message to be displayed in the dialog.
     * @param title           The title of the dialog.
     * @param messageType     The type of message to be displayed (e.g., {@link #WARNING_MESSAGE}).
     * @throws HeadlessException if the current environment is headless (no GUI available).
     */
    public static void showMessageDialog(@CheckForNull Component parentComponent,
        Object message, String title, int messageType) {
        showOptionDialog(parentComponent, message, title, DEFAULT_OPTION,
            messageType, null, null, null);
    }

    /**
     * Displays a confirmation dialog with a message and title.
     * The dialog includes options for the user to confirm or cancel an action.
     *
     * @param parentComponent The parent component relative to which the dialog is displayed.
     * @param message         The message to be displayed in the dialog.
     * @param title           The title of the dialog.
     * @param optionType      The type of options to be displayed (e.g., {@link #YES_NO_OPTION}, {@link #OK_CANCEL_OPTION}).
     * @return An integer representing the user's choice: {@link #YES_OPTION}, {@link #NO_OPTION}, {@link #CANCEL_OPTION}, or {@link #CLOSED_OPTION}.
     * @throws HeadlessException if the current environment is headless (no GUI available).
     */
    public static int showConfirmDialog(@CheckForNull Component parentComponent,
        Object message, String title, int optionType)
        throws HeadlessException {
        return showOptionDialog(parentComponent, message, title, optionType,
            QUESTION_MESSAGE, null, null, null);
    }

    /**
     * Displays a confirmation dialog with a message and title.The dialog includes options for the user to confirm or cancel an action.
     *
     * @param parentComponent The parent component relative to which the dialog is displayed.
     * @param message         The message to be displayed in the dialog.
     * @param title           The title of the dialog.
     * @param optionType      The type of options to be displayed (e.g., {@link #YES_NO_OPTION}, {@link #OK_CANCEL_OPTION}).
     * @param messageType     The type of message to be displayed (e.g., {@link #ERROR_MESSAGE}).
     * @return An integer representing the user's choice: {@link #YES_OPTION}, {@link #NO_OPTION}, {@link #CANCEL_OPTION}, or {@link #CLOSED_OPTION}.
     * @throws HeadlessException if the current environment is headless (no GUI available).
     */
    public static int showConfirmDialog(@CheckForNull Component parentComponent,
        Object message, String title, int optionType, int messageType)
        throws HeadlessException {
        return showOptionDialog(parentComponent, message, title, optionType,
            messageType, null, null, null);
    }

    /**
     * Displays a custom option dialog.
     * @param parentComponent The parent component relative to which the dialog is displayed.
     * @param message         The message to be displayed in the dialog.
     * @param title           The title of the dialog.
     * @param optionType      The type of options to be displayed (e.g., {@link #YES_NO_OPTION}, {@link #OK_CANCEL_OPTION}).
     * @param messageType     The type of message to be displayed (e.g., {@link #INFORMATION_MESSAGE}, {@link #WARNING_MESSAGE}).
     * @param icon            The icon to be displayed in the dialog.
     * @param options         An array of objects representing the options available to the user.
     * @param initialValue    The initial value selected in the dialog.
     * @return An integer representing the index of the selected option, or {@link #CLOSED_OPTION} if the dialog is closed.
     * @throws HeadlessException If the current environment is headless (no GUI available).
     */
    public static int showOptionDialog(@CheckForNull Component parentComponent,
        Object message, String title, int optionType, int messageType,
        Icon icon, Object[] options, Object initialValue)
        throws HeadlessException {
        log.debug("showConfirmDialog comp {} ", parentComponent);

        JOptionPane pane = new JOptionPane(message, messageType,
            optionType, icon, options, initialValue);
        pane.setInitialValue(initialValue);
        displayDialog(pane, parentComponent, title);

        Object selectedValue = pane.getValue();
        if ( selectedValue == null ) {
            return CLOSED_OPTION;
        }
        if ( options == null ) {
            if ( selectedValue instanceof Integer ) {
                return ((Integer)selectedValue);
            }
            return CLOSED_OPTION;
        }
        for(int counter = 0, maxCounter = options.length; counter < maxCounter; counter++ ) {
            if ( options[counter].equals(selectedValue)) {
                return counter;
            }
        }
        return CLOSED_OPTION;
    }

    /**
     * Displays an input dialog.
     * @param parentComponent       The parent component relative to which the dialog is displayed.
     * @param message               The message to be displayed in the dialog.
     * @param initialSelectionValue The initial value pre-selected in the input dialog.
     * @return The user's input value, or {@code null} if the dialog is closed or the input value is uninitialized.
     * @throws HeadlessException   if the current environment is headless (no GUI available).
     */
    @CheckForNull
    public static String showInputDialog(@CheckForNull Component parentComponent,
        String message, String initialSelectionValue ){
        return (String)showInputDialog(parentComponent, message,
            UIManager.getString("OptionPane.inputDialogTitle",
            Locale.getDefault()), QUESTION_MESSAGE, null, null,
            initialSelectionValue);
    }

    /**
     * Displays an input dialog.
     * @param parentComponent      The parent component relative to which the dialog is displayed.
     * @param message              The message to be displayed in the dialog.
     * @param title                The title of the dialog.
     * @param messageType          The type of message to be displayed (e.g., {@link #INFORMATION_MESSAGE}, {@link #WARNING_MESSAGE}).
     * @param icon                 The icon to be displayed in the dialog.
     * @param selectionValues      An array of objects representing the input selection values.
     * @param initialSelectionValue The initial value pre-selected in the input dialog.
     * @return The user's input value, or {@code null} if the dialog is closed or the input value is uninitialized.
     * @throws HeadlessException   if the current environment is headless (no GUI available).
     */
    @CheckForNull
    public static Object showInputDialog(@CheckForNull Component parentComponent,
        Object message, String title, int messageType, Icon icon,
        Object[] selectionValues, Object initialSelectionValue)
        throws HeadlessException {
        JOptionPane pane = new JOptionPane(message, messageType,
            OK_CANCEL_OPTION, icon, null, initialSelectionValue);

        pane.setWantsInput(true);
        pane.setSelectionValues(selectionValues);
        pane.setInitialSelectionValue(initialSelectionValue);
        pane.selectInitialValue();
        displayDialog(pane, parentComponent, title);

        Object value = pane.getInputValue();
        if (value == UNINITIALIZED_VALUE) {
            return null;
        }
        return value;
    }

    private static void displayDialog(JOptionPane pane, Component parentComponent, String title){
        pane.setComponentOrientation(JOptionPane.getRootFrame().getComponentOrientation());
        JDialog dialog = pane.createDialog(parentComponent, title);
        if ( parentComponent != null ) {
            dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        }
        setDialogLocation(parentComponent, dialog);
        dialog.setAlwaysOnTop(true);
        dialog.toFront();
        dialog.setVisible(true); // and waits for input
        dialog.dispose();
    }

    /**
     * Sets the position of a dialog relative to a parent component.
     * This method positions the dialog at the centre of
     * the parent component or its parent window.
     *
     * @param parentComponent The parent component relative to which the dialog should be positioned.
     * @param dialog           The dialog whose position is being set.
     */
    private static void setDialogLocation( @CheckForNull Component parentComponent, @Nonnull Dialog dialog) {
        log.debug("set dialog position for comp {} dialog {}", parentComponent, dialog.getTitle());
        int centreWidth;
        int centreHeight;
        Window w = findWindowForComponent(parentComponent);
        if ( w == null || !w.isVisible() ) {
            centreWidth = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
            centreHeight = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
        } else {
            Point topLeft = w.getLocationOnScreen();
            Dimension size = w.getSize();
            centreWidth = topLeft.x + ( size.width / 2 );
            centreHeight = topLeft.y + ( size.height / 2 );
        }
        int centerX = centreWidth - ( dialog.getWidth() / 2 );
        int centerY = centreHeight - ( dialog.getHeight() / 2 );
        dialog.setLocation( new Point(centerX, centerY));
    }

    @CheckForNull
    private static Window findWindowForComponent(Component component){
        if (component == null) {
            return null;
        }
        if (component instanceof Window) {
            return (Window) component;
        }
        return findWindowForComponent(component.getParent());
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JmriJOptionPane.class);

}
