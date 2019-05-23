package jmri.jmrit.jython;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.script.ScriptOutput;
import jmri.util.JmriJFrame;

/**
 * This Action creates a JmriJFrame displaying the thread output log from the
 * {@link RunJythonScript} class.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Matthew Harris Copyright (C) 2010
 */
public class JythonWindow extends AbstractAction {

    private JTextArea area;
    private JFrame f;
    private JCheckBox autoScroll;
    private UserPreferencesManager pref;
    private JButton clearButton;

    public static final String alwaysOnTopCheck = JythonWindow.class.getName() + ".alwaysOnTop";
    public static final String alwaysScrollCheck = JythonWindow.class.getName() + ".alwaysScroll";
    protected JCheckBox alwaysOnTopCheckBox = new JCheckBox();

    /**
     * Constructor just initializes parent class.
     *
     * @param name Action name
     */
    public JythonWindow(String name) {
        super(name);
    }

    public JythonWindow() {
        super("Script Output Window");
    }

    /**
     * Invoking this action via an event triggers display of a file dialog. If a
     * file is selected, it's then invoked as a script.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        pref = InstanceManager.getDefault(UserPreferencesManager.class);

        f = new JmriJFrame(Bundle.getMessage("TitleOutputFrame"));
        f.getContentPane().add(
                new JScrollPane(
                        area = new javax.swing.JTextArea(ScriptOutput.getDefault().getOutputArea().getDocument(), null, 12, 50),
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
                ), BorderLayout.CENTER);

        // Add checkbox to enable/disable auto-scrolling
        JPanel p = new JPanel();
        p.add(clearButton = new JButton(Bundle.getMessage("ButtonClear")));
        p.add(autoScroll = new JCheckBox(Bundle.getMessage("CheckBoxAutoScroll"), true));
        autoScroll.setSelected(pref.getSimplePreferenceState(alwaysScrollCheck));
        alwaysOnTopCheckBox.setText("Window always on Top");
        alwaysOnTopCheckBox.setVisible(true);
        alwaysOnTopCheckBox.setToolTipText("If checked, this window be always be displayed in front of any other window");
        alwaysOnTopCheckBox.setSelected(pref.getSimplePreferenceState(alwaysOnTopCheck));
        p.add(alwaysOnTopCheckBox);
        f.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());

        autoScroll.addItemListener(new ItemListener() {

            // Reference to the JTextArea of this instantiation
            JTextArea ta = area;

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    doAutoScroll(ta, true);
                    pref.setSimplePreferenceState(alwaysScrollCheck, autoScroll.isSelected());
                }
            }
        });

        alwaysOnTopCheckBox.addActionListener((ActionEvent ae) -> {
            f.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
            pref.setSimplePreferenceState(alwaysOnTopCheck, alwaysOnTopCheckBox.isSelected());
        });

        clearButton.addActionListener((ActionEvent ae) -> {
            area.setText("");
        });
        f.getContentPane().add(p, BorderLayout.PAGE_END);

        // set a monospaced font
        int size = area.getFont().getSize();
        area.setFont(new Font("Monospaced", Font.PLAIN, size));

        // Add document listener to scroll to end when modified
        area.getDocument().addDocumentListener(new DocumentListener() {

            // References to the JTextArea and JCheckBox
            // of this instantiation
            JTextArea ta = area;
            JCheckBox chk = autoScroll;

            @Override
            public void insertUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }
        });

        // Scroll to end of document
        doAutoScroll(area, true);

        f.pack();
        f.setVisible(true);
    }

    /**
     * Method to position caret at end of JTextArea ta when scroll true.
     *
     * @param ta     Reference to JTextArea
     * @param scroll True to move to end
     */
    private void doAutoScroll(JTextArea ta, boolean scroll) {
        if (scroll) {
            ta.setCaretPosition(ta.getText().length());
        }
    }

    public JFrame getFrame() {
        return f;
    }

}
