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
    // the following 3 items copied from beanedit, place in separate static method?
    private final JSpinner lengthSpinner = new JSpinner(); // 2 digit decimal format field, initialized later as instance
    private final JRadioButton inch = new JRadioButton(Bundle.getMessage("LengthInches"));
    private final JRadioButton cm = new JRadioButton(Bundle.getMessage("LengthCentimeters"));

    private final BlockPathEditFrame frame = this;
    private boolean _newPath = false;
    protected final OBlockManager obm = InstanceManager.getDefault(OBlockManager.class);
    PortalManager pm;
    private final OBlock _block;
    private OPath _path;
    TableFrames _core;
    BlockPathTableModel _pathmodel;
    PathTurnoutTableModel _tomodel;
    TableFrames.PathTurnoutJPanel _turnoutTablePane;

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
        if (path == null || turnouttable == null) {
            _newPath = true;
        } else {
            _path = path;
            _tomodel = turnouttable.getModel();
            if (_tomodel != null) { // test uses a plain JTable without getRowCount()
                log.debug("TurnoutModel.size = {}", _tomodel.getRowCount());
            }
        }
        // fill Portals combo
        pm = InstanceManager.getDefault(PortalManager.class);
        for (Portal pi : pm.getPortalSet()) {
            fromPortalComboBox.addItem(pi.getName());
            toPortalComboBox.addItem(pi.getName());
        }
        layoutFrame();
        blockName.setText(_block.getDisplayName());
        if (_newPath) {
            resetFrame();
        } else {
            populateFrame(path);
        }
        addCloseListener(this);
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
        statusBar.setFont(statusBar.getFont().deriveFont(0.9f * blockName.getFont().getSize())); // a bit smaller
        statusBar.setForeground(Color.gray);
        p2.add(statusBar);
        p.add(p2);

        p.add(Box.createVerticalGlue());

        p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
        JButton cancel;
        p2.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> closeFrame());
        JButton ok;
        p2.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener(this::createPressed);
        p.add(p2, BorderLayout.SOUTH);

        frame.getContentPane().add(p);
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        pathUserName.setText(null);
        if (toPortalComboBox.getItemCount() < 2) {
            status(Bundle.getMessage("NotEnoughPortals"), true);
        } else {
            status(Bundle.getMessage("AddXStatusInitial1", Bundle.getMessage("Path"), Bundle.getMessage("ButtonCreate")),
                    false); // I18N to include original button name in help string
        }
        lengthSpinner.setValue(0f);
        _newPath = true;
    }

    /**
     * Populate the Edit Path frame with current values.
     *
     * @param p existing OPath to copy the attributes from
     */
    public void populateFrame(OPath p) {
        if (p == null) {
            throw new IllegalArgumentException("Null OPath object");
        }
        pathUserName.setText(p.getName());
        if (p.getFromPortal() != null) {
            log.debug("BPEF FROMPORTAL name = {}", p.getFromPortal().getName());
            fromPortalComboBox.setSelectedItem(p.getFromPortal().getName());
        }
        if (p.getToPortal() != null) {
            log.debug("BPEF TOPORTAL name = {}", p.getToPortal().getName());
            toPortalComboBox.setSelectedItem(p.getToPortal().getName());
        }
        if (_block.isMetric()) {
            cm.setSelected(true);
            lengthSpinner.setValue(_path.getLengthCm());
        } else {
            inch.setSelected(true); // set first while length = 0 to prevent recalc
            lengthSpinner.setValue(_path.getLengthIn());
        }
        status(Bundle.getMessage("AddXStatusInitial3", Bundle.getMessage("Path"), Bundle.getMessage("ButtonOK")), false);
        _newPath = false;
    }

    private void createPressed(ActionEvent e) {
        String user = pathUserName.getText().trim();
        if (user.equals("") || (_newPath && _block.getPathByName(user) != null)) { // check existing names before creating
            status(user.equals("") ? Bundle.getMessage("WarningSysNameEmpty") : Bundle.getMessage("DuplPathName", user), true);
            pathUserName.setBackground(Color.red);
            return;
        }
        if (_newPath) {
            Portal fromPortal = _block.getPortalByName((String) fromPortalComboBox.getSelectedItem());
            Portal toPortal = _block.getPortalByName((String) toPortalComboBox.getSelectedItem());
            if (fromPortal != null || toPortal != null) {
                _path = new OPath(user, _block, fromPortal, toPortal, null);
                if (!_block.addPath(_path)) {
                    status(Bundle.getMessage("AddPathFailed", user), true);
                } else {
                    _pathmodel.initTempRow();
                    _core.updateOBlockTablesMenu();
                    _pathmodel.fireTableDataChanged();
                    closeFrame(); // success
                }
            }
        } else if (!_path.getName().equals(user)) {
            _path.setName(user); // name change on existing path
        }
        try { // adapted from BlockPathTableModel setValue
            if (fromPortalComboBox.getSelectedIndex() <= 0) {
                // 0 = empty choice, need at least 1 Portal
                if (toPortalComboBox.getSelectedIndex() > 0) {
                    _path.setFromPortal(null); // portal can be removed from path by setting to null but we want at least 1
                } else {
                    status(Bundle.getMessage("WarnPortalOnPath"), true);
                    return;
                }
            } else {
                String fromP = (String) fromPortalComboBox.getSelectedItem();
                log.debug("looking for FromPortal {}", fromP);
                Portal fromPortal = _block.getPortalByName(fromP);
                if (fromPortal == null || pm.getPortal(fromP) == null) {
                    int val = _core.verifyWarning(Bundle.getMessage("BlockPortalConflict", fromP, _block.getDisplayName()));
                    if (val == 2) {
                        return; // abort
                    }
                    fromPortal = pm.providePortal(fromP);
                    if (!fromPortal.setFromBlock(_block, false)) {
                        val = _core.verifyWarning(Bundle.getMessage("BlockPathsConflict", fromP, fromPortal.getFromBlockName()));
                        if (val == 2) {
                            return;
                        }
                    }
                    fromPortal.setFromBlock(_block, true);
                }
                _path.setFromPortal(fromPortal);
                if (!fromPortal.addPath(_path)) {
                    status(Bundle.getMessage("AddPathFailed", fromP), true);
                    return;
                }
            }

            if (toPortalComboBox.getSelectedIndex() <= 0) {
                // 0 = empty choice, need at least 1 Portal
                if (fromPortalComboBox.getSelectedIndex() > 0) {
                    _path.setToPortal(null); // portal can be removed from path by setting to null
                } else {
                    status(Bundle.getMessage("WarnPortalOnPath"), true);
                    return;
                }
            } else {
                String toP = (String) toPortalComboBox.getSelectedItem();
                log.debug("looking for ToPortal {}", toP);
                Portal toPortal = _block.getPortalByName(toP);
                if (toPortal == null || pm.getPortal(toP) == null) {
                    int val = _core.verifyWarning(Bundle.getMessage("BlockPortalConflict", toP, _block.getDisplayName()));
                    if (val == 2) {
                        return; // abort
                    }
                    toPortal = pm.providePortal(toP);
                    if (!toPortal.setFromBlock(_block, false)) {
                        val = _core.verifyWarning(Bundle.getMessage("BlockPathsConflict", toP, toPortal.getFromBlockName()));
                        if (val == 2) {
                            return;
                        }
                    }
                    toPortal.setToBlock(_block, true);
                    _path.setToPortal(toPortal);
                    if (!toPortal.addPath(_path)) {
                        status(Bundle.getMessage("AddPathFailed", toP), true);
                        return;
                    }
                }
            }

            _path.setLength((float) lengthSpinner.getValue() * (cm.isSelected() ? 10.0f : 25.4f)); // stored in mm
            _block.setMetricUnits(cm.isSelected());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), Bundle.getMessage("PathCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
            status(Bundle.getMessage("AddPathFailed", user), true);
            return;
        }
        // Notify changes
        if (_pathmodel != null) {
            _pathmodel.fireTableDataChanged();
        }
        _core.setPathEdit(false);
        log.debug("BlockPathEditFrame.createPressed pathEdit=False");
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
        _core.setPathEdit(false);
        log.debug("BlockPathEditFrame.closeFrame pathEdit=False");
        this.dispose();
    }

    protected void showReminderMessage() {
        InstanceManager.getDefault(UserPreferencesManager.class).
                showInfoMessage(Bundle.getMessage("ReminderTitle"),  // NOI18N
                        Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemOBlockTable")),  // NOI18N
                        "BlockPathEditFrame", "remindSaveOBlock"); // NOI18N
    }

    // copied from beanedit, also used in BlockPathEditFrame
    private void updateLength() {
        float len = (float) lengthSpinner.getValue();
        if (inch.isSelected()) {
            lengthSpinner.setValue(len/2.54f);
        } else {
            lengthSpinner.setValue(len*2.54f);
        }
    }

    void status(String message, boolean warn){
        statusBar.setText(message);
        statusBar.setForeground(warn ? Color.red : Color.gray);
    }

    // listen for frame closing
    void addCloseListener(JmriJFrame frame) {
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                _core.setPathEdit(false);
                log.debug("BlockPathEditFrame.closeFrame pathEdit=False");
                frame.dispose();
            }
        });
    }

    private static final Logger log = LoggerFactory.getLogger(BlockPathEditFrame.class);

}
