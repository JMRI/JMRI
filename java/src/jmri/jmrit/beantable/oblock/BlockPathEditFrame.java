package jmri.jmrit.beantable.oblock;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.logix.*;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Defines a GUI for editing OBlock - OPath objects in the _tabbed OBlock Table interface.
 * Based on {@link jmri.jmrit.audio.swing.AudioSourceFrame} and
 * {@link jmri.jmrit.beantable.routetable.AbstractRouteAddEditFrame}
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse (C) 2020
 */
public class BlockPathEditFrame extends JmriJFrame {

    // UI components for Add/Edit Path
    JLabel blockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameOBlock")), JLabel.TRAILING);
    JLabel blockName = new JLabel();
    JLabel pathLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("PathName")), JLabel.TRAILING);
    JTextField pathUserName = new JTextField(15);
    JLabel fromPortalLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("FromPortal")), JLabel.TRAILING);
    JLabel toPortalLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ToPortal")), JLabel.TRAILING);
    String[] p0 = {""};
    private final JComboBox<String> fromPortalComboBox = new JComboBox<>(p0);
    private final JComboBox<String> toPortalComboBox = new JComboBox<>(p0);
    JLabel statusBar = new JLabel(Bundle.getMessage("AddXStatusInitial1", Bundle.getMessage("Path"), Bundle.getMessage("ButtonOK")), JLabel.LEADING);
    static String SET_TO_CLOSED = Bundle.getMessage("Set") + " "
            + Bundle.getMessage("TurnoutStateClosed");
    static String SET_TO_THROWN = Bundle.getMessage("Set") + " "
            + Bundle.getMessage("TurnoutStateThrown");
    // the following 3 items copied from beanedit, place in separate static method?
    private final JSpinner lengthSpinner = new JSpinner(); // 2 digit decimal format field, initialized later as instance
    private final JRadioButton inch = new JRadioButton(Bundle.getMessage("LengthInches"));
    private final JRadioButton cm = new JRadioButton(Bundle.getMessage("LengthCentimeters"));

    private final BlockPathEditFrame frame = this;
    private boolean _newPath = false;
    protected final OBlockManager oblockManager = InstanceManager.getDefault(OBlockManager.class);
    PortalManager pm;
    private final OBlock _block;
    private OPath _path;
    TableFrames _core;
    BlockPathTableModel _pathmodel;
    TableFrames.PathTurnoutJPanel _turnoutTablePane;
    PathTurnoutTableModel _tomodel;

    protected UserPreferencesManager pref;
    protected boolean isDirty = false;  // true to fire reminder to save work

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BlockPathEditFrame(String title, OBlock block, OPath path,
                              TableFrames.PathTurnoutJPanel turnouttable, BlockPathTableModel pathmodel, TableFrames parent) {
        super(title, true, true);
        _block = block;
        _turnoutTablePane = turnouttable;
        _pathmodel = pathmodel;
        _core = parent;
        if (path == null) {
            _newPath = true;
        } else {
            _path = path;
            _tomodel = turnouttable.getModel();
            log.debug("TurnoutModel.size = {}", _tomodel.getRowCount());
        }
        // fill Portals combo
        pm = InstanceManager.getDefault(PortalManager.class);
        for (Portal pi : pm.getPortalSet()) {
            fromPortalComboBox.addItem(pi.getName());
            toPortalComboBox.addItem(pi.getName());
        }
        layoutFrame();
        blockName.setText(_block.getDisplayName());
        if (!_newPath) {
            populateFrame(path);
        }
    }

    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setSize(400, 500);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

        JPanel configGrid = new JPanel();
        GridLayout layout = new GridLayout(5, 2, 10, 0); // (int rows, int cols, int hgap, int vgap)
        configGrid.setLayout(layout);

        // row 1
        configGrid.add(blockLabel);
        configGrid.add(blockName);

        // row 2
        configGrid.add(pathLabel);
        JPanel p1 = new JPanel();
        p1.add(pathUserName);
        configGrid.add(p1);

        // row 3
        configGrid.add(fromPortalLabel);
        fromPortalComboBox.addActionListener(e -> {
            if ((fromPortalComboBox.getItemCount() > 0) && (fromPortalComboBox.getSelectedItem() != null) &&
                    (toPortalComboBox.getSelectedItem() != null)
                    && (fromPortalComboBox.getSelectedItem().equals(toPortalComboBox.getSelectedItem()))) {
                log.debug("resetting ToPortal");
                toPortalComboBox.setSelectedIndex(0); // clear the other one
            }
        });
        configGrid.add(fromPortalComboBox);

        // row 4
        configGrid.add(toPortalLabel);
        toPortalComboBox.addActionListener(e -> {
            if ((fromPortalComboBox.getItemCount() > 0) && (fromPortalComboBox.getSelectedItem() != null) &&
                    (toPortalComboBox.getSelectedItem() != null)
                    && (fromPortalComboBox.getSelectedItem().equals(toPortalComboBox.getSelectedItem()))) {
                log.debug("resetting ToPortal");
                fromPortalComboBox.setSelectedIndex(0); // clear the other one
            }
        });
        configGrid.add(toPortalComboBox);

        // row 5
//        JPanel p3 = new JPanel();
//        p3.setLayout(new BoxLayout(p3, BoxLayout.LINE_AXIS));
//        p3.add(Box.createHorizontalGlue());
        // copied from beanedit, also in BlockPathEditFrame
        lengthSpinner.setModel(
                new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f), Float.valueOf(1000f), Float.valueOf(0.01f)));
        lengthSpinner.setEditor(new JSpinner.NumberEditor(lengthSpinner, "###0.00"));
        lengthSpinner.setPreferredSize(new JTextField(8).getPreferredSize());
        lengthSpinner.setValue(0f); // reset from possible previous use

        ButtonGroup bg = new ButtonGroup();
        bg.add(inch);
        bg.add(cm);

        p1 = new JPanel();
        p1.add(inch);
        p1.add(cm);
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
        inch.setSelected(true);
        inch.addActionListener(e -> {
            cm.setSelected(!inch.isSelected());
            updateLength();
        });
        cm.addActionListener(e -> {
            inch.setSelected(!cm.isSelected());
            updateLength();
        });
        configGrid.add(p1);

        JPanel p2 = new JPanel();
        p2.add(lengthSpinner);
        lengthSpinner.setToolTipText(Bundle.getMessage("LengthToolTip", Bundle.getMessage("Path")));
        configGrid.add(p2);

        p.add(configGrid);

        JPanel totbl = new JPanel();
        totbl.setLayout(new BorderLayout(10, 10));
        totbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        totbl.add(_turnoutTablePane, BorderLayout.CENTER);
        p.add(totbl);

        p2 = new JPanel();
        p2.add(statusBar);
        p.add(p2);

        p.add(Box.createVerticalGlue());

        p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
        JButton cancel;
        p2.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> frame.dispose());
//        if (_newPath = false) { // expected refresh of Turnout Table difficult to achieve, only show OK button, closing pane
//            JButton apply;
//            p2.add(apply = new JButton(Bundle.getMessage("ButtonApply")));
//            apply.addActionListener(this::createPressed);
//        }
        JButton ok;
        p2.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            createPressed(e);
            closeFrame();
        });
        p.add(p2, BorderLayout.SOUTH);

        frame.getContentPane().add(p);
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        pathUserName.setText(null);
        // reset statusBar text?
        statusBar.setText(Bundle.getMessage("AddXStatusInitial1", Bundle.getMessage("Path"), Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
        statusBar.setForeground(Color.gray);
        lengthSpinner.setValue(0f);
        _newPath = true;
    }

    /**
     * Populate the Edit Path frame with current values.
     */
    public void populateFrame(OPath p) {
        if (p == null) {
            throw new IllegalArgumentException("Null OPath object");
        }
        pathUserName.setText(p.getName());
        // TODO select the Portals EBR
        if (p.getFromPortal() != null) {
            log.debug("BPEF FROMPORTAL name = {}", p.getFromPortal().getName());
            //fromBlockComboBox.setSelectedItemByName(p.getFromBlockName());
            fromPortalComboBox.setSelectedItem(p.getFromPortal().getName());
        }
        if (p.getToPortal() != null) {
            log.debug("BPEF TOPORTAL name = {}", p.getToPortal().getName());
            toPortalComboBox.setSelectedItem(p.getToPortal().getName());
        }
        statusBar.setText(Bundle.getMessage("AddXStatusInitial3", Bundle.getMessage("Path"), Bundle.getMessage("ButtonOK")));
        lengthSpinner.setValue(_block.getLengthIn());
        _newPath = false;
    }

    private void createPressed(ActionEvent e) {
        String user = pathUserName.getText().trim();
        if (user.equals("")) {
            // warn/help bar red
            statusBar.setText(Bundle.getMessage("WarningSysNameEmpty"));
            statusBar.setForeground(Color.red);
            pathUserName.setBackground(Color.red);
            return;
        } else {
            pathUserName.setBackground(Color.white);
        }

        if (_newPath) {
            _path = new OPath(_block, user);
        } else {
            _path.setName(user);
        }
        try {
            Portal fromPortal;
            if (fromPortalComboBox.getSelectedIndex() <= 0) { // 0 = empty choice
                fromPortal = null;
            } else {
                fromPortal = pm.getPortal((String) fromPortalComboBox.getSelectedItem());
                log.debug("looking for Portal {}", fromPortalComboBox.getSelectedItem());
            }
            //if (fromPortal != null) {
                _path.setFromPortal(fromPortal); // portal can be removed by setting to null
            //}
            if (toPortalComboBox.getSelectedIndex() <= 0) { // 0 = empty choice
                fromPortal = null;
            } else {
                fromPortal = pm.getPortal((String) toPortalComboBox.getSelectedItem());
                log.debug("looking for Portal {}", toPortalComboBox.getSelectedItem());
            }
            //if (fromPortal != null) {
                _path.setToPortal(fromPortal);
            //}
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("PathCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
        // Notify changes
        //sendChange?
        if (_pathmodel != null) {
            _pathmodel.fireTableDataChanged(); // change BlockEdit > Path BlockPathTableModel, not this one
        }
    }

    protected JPanel getButtonPanel() {
        final JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        final JButton createButton = new JButton(Bundle.getMessage("ButtonCreate"));
        final JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
        // add Buttons panel
        JPanel pb = new JPanel();
        pb.setLayout(new FlowLayout(FlowLayout.TRAILING));
        // Cancel (Add) button
        pb.add(cancelButton);
        cancelButton.addActionListener(this::cancelAddPressed);
        // Create button
        pb.add(createButton);
        createButton.addActionListener(this::createPressed);
        createButton.setToolTipText(Bundle.getMessage("TooltipCreatePath"));

        // Show the initial buttons, and hide the others
        cancelButton.setVisible(true); // show CancelAdd button
        updateButton.setVisible(true);
        createButton.setVisible(true);
        return pb;
    }

    /**
     * Respond to the CancelAdd button.
     *
     * @param e the action event
     */
    private void cancelAddPressed(ActionEvent e) {
        if (isDirty) {
            showReminderMessage();
        }
        statusBar.setText(Bundle.getMessage("AddXStatusInitial1", Bundle.getMessage("Path"), Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
        isDirty = false;
        // hide addPathFrame
        setVisible(false);
        if (_tomodel != null) {
            _tomodel.dispose();
        }
        closeFrame();
    }

    protected void closeFrame(){
        // remind to save, if Path was created or edited
        if (isDirty) {
            showReminderMessage();
            isDirty = false;
        }
        // hide addFrame
        setVisible(false);

        if (_tomodel != null) {
            _tomodel.dispose();
        }
        this.dispose();
    }

    protected void showReminderMessage() {
        InstanceManager.getDefault(UserPreferencesManager.class).
                showInfoMessage(Bundle.getMessage("ReminderTitle"),  // NOI18N
                        Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemOBlockTable")),  // NOI18N
                        "BlockPathEditFrame", "remindSaveOBlock"); // NOI18N
    }

    // copied from beanedit, also in BlockPathEditFrame
    private void updateLength() {
        float len = (float) lengthSpinner.getValue();
        if (inch.isSelected()) {
            lengthSpinner.setValue(len/2.54f);
        } else {
            lengthSpinner.setValue(len*2.54f);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(BlockPathEditFrame.class);

}
