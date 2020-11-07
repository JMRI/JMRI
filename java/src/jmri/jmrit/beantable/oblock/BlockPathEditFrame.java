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
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

//    JPanel main = new JPanel();
//    private final JScrollPane scroll
//            = new JScrollPane(main,
//            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
//            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    // UI components for Add/Edit Path
    JLabel blockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameOBlock")));
    JLabel blockName = new JLabel();
    JLabel pathLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("PathName")));
    JTextField userName = new JTextField(15);
    JLabel userNameLabel = new JLabel();
    JLabel fromPortalLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("FromPortal")));
    JLabel toPortalLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ToPortal")));
    String[] p0 = {""};
    private final JComboBox<String> fromPortalComboBox = new JComboBox<>(p0);
    private final JComboBox<String> toPortalComboBox = new JComboBox<>(p0);
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
    //JTextArea comment  = new JTextArea();
    final JLabel status1 = new JLabel();
    final JLabel status2 = new JLabel();

    OBlock _block;
    OPath _path;
    PathTurnoutTableModel _tomodel;
    PortalManager pm;
    TableFrames _core;
    TableFrames.PathTurnoutJPanel _turnoutTablePane;
    boolean editMode = false;

    protected UserPreferencesManager pref;
    protected boolean isDirty = false;  // true to fire reminder to save work

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BlockPathEditFrame(String title, OBlock block, OPath path,
                              TableFrames.PathTurnoutJPanel table, TableFrames parent) {
        super(title, true, true);
        _block = block;
        _turnoutTablePane = table;
        _core = parent;
        if (path == null) {
            _newPath = true;
        } else {
            _path = path;
            _tomodel = table.getModel();
            log.debug("TurnoutModel.size = {}", _tomodel.getRowCount());
        }
        pm = InstanceManager.getDefault(PortalManager.class);
        layoutFrame();
        for (Portal pi : pm.getPortalSet()) {
            fromPortalComboBox.addItem(pi.getName());
            toPortalComboBox.addItem(pi.getName());
        }
        if (path != null) {
            populateFrame(path);
        }
    }

    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setSize(350, 400);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(blockName);
        if (_newPath) {
            p1.add(pathLabel);
        } else {
            p1.add(_userName);
        }
        p.add(p1);

        p1 = new JPanel();
        p1.add(fromPortalLabel);
        p1.add(fromPortalComboBox);
        p.add(p1);

        p1 = new JPanel();
        p1.add(toPortalLabel);
        p1.add(toPortalComboBox);
        p.add(p1);

        JPanel ptbl = new JPanel();
        ptbl.setLayout(new BorderLayout(10, 10));
        ptbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        ptbl.add(_turnoutTablePane, BorderLayout.CENTER);

        JPanel tblButtons = new JPanel();
        tblButtons.setLayout(new BorderLayout(10, 10));
        tblButtons.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        tblButtons.setLayout(new BoxLayout(tblButtons, BoxLayout.Y_AXIS));

        JButton addButton = new JButton(Bundle.getMessage("ButtonAddTurnout"));
        ActionListener addPathAction = e -> {
            _core.addTurnoutPane(_path);
        };
        addButton.addActionListener(addPathAction);
        addButton.setToolTipText(Bundle.getMessage("AddTurnoutTabbedPrompt"));
        tblButtons.add(addButton);
        // TODO add more, like a button Add... to frame?
        ptbl.add(tblButtons, BorderLayout.SOUTH);
        p.add(ptbl);

        p.add(Box.createVerticalGlue());

        JPanel p2 = new JPanel();
        p2.add(statusBar);
        p.add(p2);

        p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
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
        //main.add(scroll);
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
//        if (path != null) {
            try {
                Portal fromPortal = pm.providePortal((String) fromPortalComboBox.getSelectedItem());
                if (fromPortal != null) {
                    path.setFromPortal(fromPortal);
                }
                fromPortal = pm.providePortal((String) toPortalComboBox.getSelectedItem());
                if (fromPortal != null) {
                    path.setToPortal(fromPortal);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("PathCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
//        }
        // Notify changes
        if (_tomodel != null) {
            _tomodel.fireTableDataChanged();
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
        cancelAdd();
    }

    /**
     * Cancel Add mode.
     */
    private void cancelAdd() {
        if (isDirty) {
            showReminderMessage();
        }
        finishUpdate();
        status1.setText(Bundle.getMessage("AddXStatusInitial1", Bundle.getMessage("Path"), Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
        //status2.setText(Bundle.getMessage("AddXStatusInitial2", Bundle.getMessage("Path"), Bundle.getMessage("ButtonEdit")));
        isDirty = false;
        // hide addFrame
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

        // if in Edit, cancel edit mode
        if (editMode) {
            cancelEdit();
        }
        _tomodel.dispose();
        this.dispose();
    }

    /**
     * Cancels edit mode
     */
    protected void cancelEdit() {
        if (editMode) {
            status1.setText(Bundle.getMessage("AddXStatusInitial1", Bundle.getMessage("Path"), Bundle.getMessage("ButtonCreate"))); // I18N to include original button name in help string
            //status2.setText(Bundle.getMessage("AddXStatusInitial2", Bundle.getMessage("Path"), Bundle.getMessage("ButtonEdit")));
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
        //status2.setText(Bundle.getMessage("PathAddStatusInitial2", Bundle.getMessage("ButtonEdit")));
        status2.setVisible(true);
        setTitle(Bundle.getMessage("TitleAddPath"));
        clearPage();
        // reactivate the Path
        isDirty = true;
        // get out of edit mode
        editMode = false;
    }

    private void clearPage() {
        _userName.setText("");
        fromPortalComboBox.setSelectedItem(0);
        toPortalComboBox.setSelectedItem(0);
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
