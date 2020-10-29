package jmri.jmrit.beantable.oblock;

import jmri.jmrit.logix.OBlock;
import jmri.util.JmriJFrame;

import javax.swing.*;
//import javax.swing.event.ChangeEvent;
import java.awt.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to edit OBlock objects.
 * Adapted from AbstractAudioFrame + -ListenerFrame 2020 by Egbert Broerse.
 *
 * @author Matthew Harris copyright (c) 2009
 */
public class OBlockEditFrame extends JmriJFrame {

    OBlockEditFrame frame = this;

    JPanel main = new JPanel();
    private final JScrollPane scroll
            = new JScrollPane(main,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    OBlockTableModel model;

    // Common UI components for Add/Edit OBlock
    private static final JLabel SYS_NAME_LABEL = new JLabel(Bundle.getMessage("LabelSystemName"));
    JTextField sysName = new JTextField(5);
    private static final JLabel USER_NAME_LABEL = new JLabel(Bundle.getMessage("LabelUserName"));
    JTextField userName = new JTextField(15);

    /**
     * Standard constructor
     *
     * @param title Title of this OBlockFrame
     * @param model OBlockTableModel holding OBlock data
     */
    public OBlockEditFrame(String title, OBlockTableModel model) {
        super(title);
        this.model = model;
    }

    /**
     * Method to layout the frame.
     * <p>
     * This contains common items.
     * <p>
     * Sub-classes will override this method and provide additional GUI items.
     */
    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JPanel p;

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(SYS_NAME_LABEL);
        p.add(sysName);
        frame.getContentPane().add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(USER_NAME_LABEL);
        p.add(userName);
        frame.getContentPane().add(p);

        frame.add(scroll);
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        sysName.setText(null);
        userName.setText(null);

        //this.newOBlock = true;
    }

    /**
     * Populate the OBlock frame with current values.
     *
     * @param a OBlock object to use
     */
    public void populateFrame(OBlock a) {
        sysName.setText(a.getSystemName());
        userName.setText(a.getUserName());
    }

    /**
     * Check System Name user input.
     *
     * @param entry string retrieved from text field
     * @param counter index of all similar (Path/Portal) items
     * @param prefix (Oblock/Portal/path/signal) system name prefix string to compare entry against
     * @return true if prefix doesn't match
     */
    protected boolean entryError(String entry, String prefix, String counter) {
        if (!entry.startsWith(prefix)) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("OBlockCreateError", prefix),
                    Bundle.getMessage("OBlockCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
            sysName.setText(prefix + counter);
            return true;
        }
        return false;
    }

    private static final Logger log = LoggerFactory.getLogger(OBlockEditFrame.class);

}
