package jmri.util.swing;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 * Static methods to display an Exception Dialog.
 * <p>
 * The Exception and additional details about what was happening when the
 * exception occurred can be passed in using an ExceptionContext object.
 * <p>
 * The Dialog has buttons for pasting Exception and System details to Clipboard.
 * @author Gregory Madsen Copyright (C) 2012
 * @author Steve Young Copyright (C) 2023
 */
public class ExceptionDisplayFrame {

    private ExceptionDisplayFrame(){}

    private static final EmptyBorder border = new EmptyBorder(10, 20, 10, 20);

    /**
     * Display an ExceptionDisplayFrame.
     *
     * @param context the ExceptionContext to display details for.
     * @param owner   the associated Component, can be null.
     */
    public static void displayExceptionDisplayFrame(@CheckForNull final Component owner, @Nonnull final ExceptionContext context) {
        JmriJOptionPane.showMessageDialog(owner, 
            initComponents(context), 
            context.getTitle(), 
            JmriJOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an ExceptionDisplayFrame.
     *
     * @param ex    the Exception to display details for.
     * @param owner the associated Component, can be null.
     *
     */
    public static void displayExceptionDisplayFrame(@CheckForNull Component owner, @Nonnull Exception ex) {
        displayExceptionDisplayFrame( owner, new ExceptionContext( ex, "", "") );
    }

    @Nonnull
    private static JPanel initComponents(@Nonnull ExceptionContext context) {
        JPanel contentPane = new JPanel();
        contentPane.setBorder(border);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("<html><h2>" + context.getPreface() + "</h2></html>"));
        contentPane.add(titlePanel);

        if ( !context.getHint().isBlank() ) {
            JPanel hintPanel = new JPanel();
            hintPanel.add(new JLabel("<html><h3>" + context.getHint() + "</h3></html>"));
            contentPane.add(hintPanel);
        }

        contentPane.add(getSummaryPanel(context));
        contentPane.add(getButtonPanel(context));
        contentPane.add(getStrackTracePanel(context));
        return contentPane;
    }

    @Nonnull
    private static JPanel getSummaryPanel(@Nonnull ExceptionContext context){
        JPanel summaryPanel = new JPanel();
        JTextArea jta = new JTextArea(context.getSummary());
        jta.setBorder(border);
        summaryPanel.add(jta);
        return summaryPanel;
    }

    @Nonnull
    private static JPanel getStrackTracePanel(@Nonnull ExceptionContext context){
        JPanel strackTracePanel = new JPanel();
        JTextArea ta = new JTextArea(context.getStackTraceAsString(10));
        ta.setToolTipText(Bundle.getMessage("ExceptionDisplayStackTraceToolTip"));
        ta.setBorder(border);
        strackTracePanel.add(ta);
        return strackTracePanel;
    }

    @Nonnull
    private static JPanel getButtonPanel(@Nonnull ExceptionContext context){

        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        JButton exCopyButton = new JButton(Bundle.getMessage("ExceptionDisplayCopyButton"));
        exCopyButton.addActionListener((ActionEvent e) ->
            systemClipboard.setContents(new StringSelection(context.getClipboardString(false)), null));

        JButton systemCopyButton = new JButton(Bundle.getMessage("ExceptionSystemCopyButton"));
        systemCopyButton.addActionListener((ActionEvent e) ->
            systemClipboard.setContents(new StringSelection(context.getClipboardString(true)), null));

        JPanel b1 = new JPanel();
        JPanel b2 = new JPanel();
        b1.add(exCopyButton);
        b2.add(systemCopyButton);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(b1);
        p.add(b2);
        return p;
    }

}
