package jmri.jmrit.beantable.oblock;

import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.UserPreferencesManager;
import jmri.jmrit.logix.*;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * Defines a GUI for editing OBlock - OPath objects in the tabbed Table interface.
 * Based on {@link jmri.jmrit.audio.swing.AudioSourceFrame} and
 * {@link jmri.jmrit.beantable.routetable.AbstractRouteAddEditFrame}
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse (C) 2020
 */
public class BlockPathEditFrame extends JmriJFrame {

    JPanel main = new JPanel();
    private final JScrollPane scroll
            = new JScrollPane(main,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    PathTurnoutTableModel _tmodel;
    OBlock _block;
    OPath _path;
    PortalManager pm;

    // UI components for Add/Edit Path
    JLabel blockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameOBlock")));
    JLabel blockName = new JLabel();
    JLabel pathLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("PathName")));
    JTextField userName = new JTextField(15);
    JLabel userNameLabel = new JLabel();
    JLabel fromPortalLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("FromBlockName")));
    JLabel toPortalLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("OppBlockName")));
    private JComboBox<Portal> fromPortalComboBox = new JComboBox<>();
    private JComboBox<Portal> toPortalComboBox = new JComboBox<>();
    JLabel statusBar = new JLabel(Bundle.getMessage("AddBeanStatusEnter"), JLabel.LEADING);

    private final BlockPathEditFrame frame = this;
    private boolean _newPath = false;

    protected final OBlockManager oblockManager = InstanceManager.getDefault(OBlockManager.class);
    protected PortalManager portalManager;
    static final String[] COLUMN_NAMES = {
            Bundle.getMessage("ColumnSystemName"),
            Bundle.getMessage("ColumnUserName"),
            Bundle.getMessage("Include"),
            Bundle.getMessage("ColumnLabelSetState")};
    static String SET_TO_CLOSED = Bundle.getMessage("Set") + " "
            + Bundle.getMessage("TurnoutStateClosed");
    static String SET_TO_THROWN = Bundle.getMessage("Set") + " "
            + Bundle.getMessage("TurnoutStateThrown");

    static int ROW_HEIGHT;

    final JTextField _userName = new JTextField(22);
    final JLabel nameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    final JLabel userLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JTextArea comment  = new JTextArea();
    final JLabel status1 = new JLabel();
    final JLabel status2 = new JLabel();

    //    protected ArrayList<PathTurnout> _includedTurnoutList;
    PathTurnoutTableModel _pathTurnoutTableModel;
    JScrollPane _pathTurnoutScrollPane;
    NamedBeanComboBox<OBlock> fromOBlock;
    NamedBeanComboBox<OBlock> toOBlock;
    boolean editMode = false;

    protected UserPreferencesManager pref;
    //private final JRadioButton allButton = null;
    protected boolean routeDirty = false;  // true to fire reminder to save work
    //private boolean showAll = true;   // false indicates show only included Turnouts

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BlockPathEditFrame(String title, OBlock block, OPath path) {
        super(title, true, true);
        _block = block;
        if (path == null) {
            _newPath = true;
        }
        _path = path;
        pm = InstanceManager.getDefault(PortalManager.class);
        PathTurnoutTableModel _tmodel = new PathTurnoutTableModel(path, this);
        log.debug("TurnoutModel.size = {}", _tmodel.getRowCount());
        layoutFrame();
        fromPortalComboBox = new JComboBox<Portal>((Vector<Portal>) _block.getPortals());
        toPortalComboBox = new JComboBox<Portal>((Vector<Portal>) _block.getPortals());
        if (path != null) {
            populateFrame(path);
        }
    }

    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(blockName);
        if (_newPath) {
            p1.add(pathLabel);
        } else {
            p1.add(_userName);
        }
        p1.add(fromPortalLabel);
        p1.add(fromPortalComboBox);
        p1.add(toPortalLabel);
        p1.add(toPortalComboBox);
        p.add(p);

        p.add(Box.createVerticalGlue());

        // TODO add Turnout table + Add button EBR

        JPanel p2 = new JPanel();
        p2.add(statusBar);

        JButton cancel;
        p2.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        JButton apply;
        p2.add(apply = new JButton(Bundle.getMessage("ButtonApply")));
        apply.addActionListener(this::createPressed);
        JButton ok;
        p2.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            createPressed(e);
            frame.dispose();
        });
        p.add(p2, BorderLayout.SOUTH);

        frame.getContentPane().add(p);
        //frame.add(scroll);
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        _userName.setText(null);
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("AddBeanStatusEnter"));
        statusBar.setForeground(Color.gray);
        _newPath = true;
    }

    /**
     * Populate the Edit Path frame with current values.
     */
    public void populateFrame(OPath p) {
        if (p == null) {
            throw new IllegalArgumentException("Null OPath object");
        }
        _userName.setText(p.getName());
        userNameLabel.setText(p.getName());
        fromPortalComboBox.setSelectedItem(p.getFromPortal());
        toPortalComboBox.setSelectedItem(p.getToPortal());
        _newPath = false;
    }

    private void createPressed(ActionEvent e) {
        String user = _userName.getText().trim();
        if (user.equals("")) {
            // warn/help bar red
            statusBar.setText(Bundle.getMessage("WarningSysNameEmpty"));
            statusBar.setForeground(Color.red);
            _userName.setBackground(Color.red);
            return;
        } else {
            _userName.setBackground(Color.white);
        }

        OPath path = new OPath(_block, user);
        if (path != null) {
            try {
                Portal fromPortal = fromPortalComboBox.getItemAt(fromPortalComboBox.getSelectedIndex());
                if (fromPortal != null) {
                    path.setFromPortal(fromPortal);
                }
                fromPortal = toPortalComboBox.getItemAt(toPortalComboBox.getSelectedIndex());
                if (fromPortal != null) {
                    path.setToPortal(fromPortal);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("PathCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }
        // Notify changes
        if (_tmodel != null) {
            _tmodel.fireTableDataChanged();
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
        createButton.setToolTipText(Bundle.getMessage("TooltipCreateRoute"));

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
        cancelAdd();
    }

    /**
     * Cancel Add mode.
     */
    private void cancelAdd() {
        if (routeDirty) {
            showReminderMessage();
        }
        finishUpdate();
        status1.setText(Bundle.getMessage("PathAddStatusInitial1", Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
        //status2.setText(Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit")));
        routeDirty = false;
        // hide addFrame
        setVisible(false);
        _pathTurnoutTableModel.dispose();
        closeFrame();
    }

    protected void closeFrame(){
        // remind to save, if Route was created or edited
        if (routeDirty) {
            showReminderMessage();
            routeDirty = false;
        }
        // hide addFrame
        setVisible(false);

        // if in Edit, cancel edit mode
        if (editMode) {
            cancelEdit();
        }
        _pathTurnoutTableModel.dispose();
        this.dispose();
    }

    /**
     * Cancels edit mode
     */
    protected void cancelEdit() {
        if (editMode) {
            status1.setText(Bundle.getMessage("RouteAddStatusInitial1", Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
            //status2.setText(Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit")));
            finishUpdate();
            // get out of edit mode
            editMode = false;
        }
        closeFrame();
    }

    protected void finishUpdate() {
        // move to show all Turnouts if not there
//        cancelIncludedOnly();
        // Provide feedback to user
        // switch GUI back to selection mode
        //status2.setText(Bundle.getMessage("RouteAddStatusInitial2", Bundle.getMessage("ButtonEdit")));
        status2.setVisible(true);
        setTitle(Bundle.getMessage("TitleAddRoute"));
        clearPage();
        // reactivate the Route
        routeDirty = true;
        // get out of edit mode
        editMode = false;
    }

    private void clearPage() {
        //        _systemName.setText("");
        _userName.setText("");
        fromOBlock.setSelectedItem(null);
        toOBlock.setSelectedItem(null);
        //        for (int i = _turnoutList.size() - 1; i >= 0; i--) {
        //            _turnoutList.get(i).setIncluded(false);
        //        }
    }

    protected void showReminderMessage() {
        InstanceManager.getDefault(UserPreferencesManager.class).
                showInfoMessage(Bundle.getMessage("ReminderTitle"),  // NOI18N
                        Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemOBlockTable")),  // NOI18N
                        "BlockPathEditFrame", "remindSaveOBlock"); // NOI18N
    }

    private static final Logger log = LoggerFactory.getLogger(BlockPathEditFrame.class);

}
