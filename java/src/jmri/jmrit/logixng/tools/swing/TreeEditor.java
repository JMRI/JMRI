package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;

import jmri.*;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.swing.FunctionsHelpDialog;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.ThreadingUtil;

import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Base class for LogixNG editors
 *
 * @author Daniel Bergqvist 2020
 */
public class TreeEditor extends TreeViewer {

    // Enums used to configure TreeEditor
    public enum EnableClipboard { EnableClipboard, DisableClipboard }
    public enum EnableRootRemoveCutCopy { EnableRootRemoveCutCopy, DisableRootRemoveCutCopy }
    public enum EnableRootPopup { EnableRootPopup, DisableRootPopup }
    public enum EnableExecuteEvaluate { EnableExecuteEvaluate, DisableExecuteEvaluate }


    private static final String ACTION_COMMAND_RENAME_SOCKET = "rename_socket";
    private static final String ACTION_COMMAND_REMOVE = "remove";
    private static final String ACTION_COMMAND_EDIT = "edit";
    private static final String ACTION_COMMAND_CUT = "cut";
    private static final String ACTION_COMMAND_COPY = "copy";
    private static final String ACTION_COMMAND_PASTE = "paste";
    private static final String ACTION_COMMAND_PASTE_COPY = "pasteCopy";
    private static final String ACTION_COMMAND_ENABLE = "enable";
    private static final String ACTION_COMMAND_DISABLE = "disable";
    private static final String ACTION_COMMAND_LOCK = "lock";
    private static final String ACTION_COMMAND_UNLOCK = "unlock";
    private static final String ACTION_COMMAND_LOCAL_VARIABLES = "local_variables";
    private static final String ACTION_COMMAND_CHANGE_USERNAME = "change_username";
    private static final String ACTION_COMMAND_EXECUTE_EVALUATE = "execute_evaluate";
//    private static final String ACTION_COMMAND_EXPAND_TREE = "expandTree";

    // There should only be one clipboard editor open at any time so this is static.
    // This field must only be accessed on the GUI thread.
    private static ClipboardEditor _clipboardEditor = null;

    private final LogixNGPreferences _prefs = InstanceManager.getDefault(LogixNGPreferences.class);

    private JDialog _renameSocketDialog = null;
    private JDialog _addItemDialog = null;
    private JDialog _editActionExpressionDialog = null;
    private JDialog _editLocalVariablesDialog = null;
    private JDialog _changeUsernameDialog = null;
    private final JTextField _socketNameTextField = new JTextField(20);
    private final JTextField _systemName = new JTextField(20);
    private final JTextField _addUserName = new JTextField(20);
    private final JTextField _usernameField = new JTextField(50);

    protected boolean _showReminder = false;
    private boolean _lockPopupMenu = false;

    private final JLabel _renameSocketLabel = new JLabel(Bundle.getMessage("SocketName") + ":");  // NOI18N
    private final JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));   // NOI18N
    private final JLabel _sysNameLabel = new JLabel(Bundle.getMessage("SystemName") + ":");  // NOI18N
    private final JLabel _userNameLabel = new JLabel(Bundle.getMessage("UserName") + ":");   // NOI18N
    private final String _systemNameAuto = getClassName() + ".AutoSystemName";             // NOI18N
    private JButton _create;
    private JButton _edit;

    private SwingConfiguratorInterface _addSwingConfiguratorInterface;
    private SwingConfiguratorInterface _addSwingConfiguratorInterfaceMaleSocket;
    private SwingConfiguratorInterface _editSwingConfiguratorInterface;
    private final List<Map.Entry<SwingConfiguratorInterface, Base>> _swingConfiguratorInterfaceList = new ArrayList<>();

    private LocalVariableTableModel _localVariableTableModel;

    private final boolean _enableClipboard;
    private final boolean _disableRootRemoveCutCopy;
    private final boolean _disableRootPopup;
    private final boolean _enableExecuteEvaluate;

    /**
     * Construct a TreeEditor.
     *
     * @param femaleRootSocket         the root of the tree
     * @param enableClipboard          should clipboard be enabled on the menu?
     * @param enableRootRemoveCutCopy  should the popup menu items remove,
     *                                 cut and copy be enabled or disabled?
     * @param enableRootPopup          should the popup menu be disabled for root?
     * @param enableExecuteEvaluate    should the popup menu show execute/evaluate?
     */
    public TreeEditor(
            @Nonnull FemaleSocket femaleRootSocket,
            EnableClipboard enableClipboard,
            EnableRootRemoveCutCopy enableRootRemoveCutCopy,
            EnableRootPopup enableRootPopup,
            EnableExecuteEvaluate enableExecuteEvaluate) {

        super(femaleRootSocket);
        _enableClipboard = enableClipboard == EnableClipboard.EnableClipboard;
        _disableRootRemoveCutCopy = enableRootRemoveCutCopy == EnableRootRemoveCutCopy.DisableRootRemoveCutCopy;
        _disableRootPopup = enableRootPopup == EnableRootPopup.DisableRootPopup;
        _enableExecuteEvaluate = enableExecuteEvaluate == EnableExecuteEvaluate.EnableExecuteEvaluate;
    }

    @Override
    final public void initComponents() {
        super.initComponents();

        // The menu is created in parent class TreeViewer
        JMenuBar menuBar = getJMenuBar();

        JMenu toolsMenu = new JMenu(Bundle.getMessage("MenuTools"));
        if (_enableClipboard) {
            JMenuItem openClipboardItem = new JMenuItem(Bundle.getMessage("MenuOpenClipboard"));
            openClipboardItem.addActionListener((ActionEvent e) -> {
                openClipboard();
            });
            toolsMenu.add(openClipboardItem);
        }
        menuBar.add(toolsMenu);

        JTree tree = _treePane._tree;

        tree.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getModifiersEx() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
                    if (e.getKeyCode() == 'R') {    // Remove
                        TreePath path = tree.getSelectionPath();
                        if (path != null) {
                            FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                            if (femaleSocket.isConnected()) {
                                removeItem((FemaleSocket) path.getLastPathComponent(), path);
                            }
                        }
                    }
                    if (e.getKeyCode() == 'E') {    // Edit
                        TreePath path = tree.getSelectionPath();
                        if (path != null) {
                            FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                            if (femaleSocket.isConnected()) {
                                editItem(femaleSocket, path);
                            }
                        }
                    }
                    if (e.getKeyCode() == 'N') {    // New
                        TreePath path = tree.getSelectionPath();
                        if (path != null) {
                            FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                            if (femaleSocket.isConnected()) {
                                return;
                            }
                            if (parentIsSystem(femaleSocket) && abortEditAboutSystem(femaleSocket.getParent())) {
                                return;
                            }
                            Rectangle rect = tree.getPathBounds(path);
                            openPopupMenu(tree, path, rect.x, rect.y, true);
                        }
                    }
                    if (e.getKeyCode() == 'D') {    // Disable
                        TreePath path = tree.getSelectionPath();
                        if (path != null) {
                            FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                            if (femaleSocket.isConnected()) {
                                doIt(ACTION_COMMAND_DISABLE, femaleSocket, path);
                            }
                        }
                    }
                }
                if (e.getModifiersEx() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() + InputEvent.SHIFT_DOWN_MASK) {
                    if (e.getKeyCode() == 'V') {    // Paste copy
                        TreePath path = tree.getSelectionPath();
                        if (path != null) {
                            FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                            if (!femaleSocket.isConnected()) {
                                pasteCopy((FemaleSocket) path.getLastPathComponent(), path);
                            }
                        }
                    }
                    if (e.getKeyCode() == 'D') {    // Enable
                        TreePath path = tree.getSelectionPath();
                        if (path != null) {
                            FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                            if (femaleSocket.isConnected()) {
                                doIt(ACTION_COMMAND_ENABLE, femaleSocket, path);
                            }
                        }
                    }
                }

                for (FemaleSocketOperation oper : FemaleSocketOperation.values()) {
                    if (e.getKeyCode() == oper.getKeyCode()
                            && e.getModifiersEx() == oper.getModifiers()) {

                        TreePath path = tree.getSelectionPath();
                        if (path != null) {
                            FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                            if (femaleSocket.isSocketOperationAllowed(oper) && !parentIsLocked(femaleSocket)) {
                                doIt(oper.name(), femaleSocket, path);
                            }
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        var mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        tree.getActionMap().put(tree.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    cutItem((FemaleSocket) path.getLastPathComponent(), path);
                }
            }
        });

        tree.getActionMap().put(tree.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    copyItem((FemaleSocket) path.getLastPathComponent());
                }
            }
        });

        tree.getActionMap().put(tree.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    pasteItem((FemaleSocket) path.getLastPathComponent(), path);
                }
            }
        });


        tree.addMouseListener(
                new MouseAdapter() {
                    // On Windows, the popup is opened on mousePressed,
                    // on some other OS, the popup is opened on mouseReleased

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            openPopupMenu(tree, tree.getClosestPathForLocation(e.getX(), e.getY()), e.getX(), e.getY(), false);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            openPopupMenu(tree, tree.getClosestPathForLocation(e.getX(), e.getY()), e.getX(), e.getY(), false);
                        }
                    }
                }
        );
    }

    private void openPopupMenu(JTree tree, TreePath path, int x, int y, boolean onlyAddItems) {
        if (isPopupMenuLocked()) return;

        if (path != null) {
            // Check that the user has clicked on a row.
            Rectangle rect = tree.getPathBounds(path);
            if ((y >= rect.y) && (y <= rect.y + rect.height)) {
                // Select the row the user clicked on
                tree.setSelectionPath(path);

                FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                new PopupMenu(x, y, femaleSocket, path, onlyAddItems);
            }
        }
    }

    public static void openClipboard() {
        if (_clipboardEditor == null) {
            _clipboardEditor = new ClipboardEditor();
            _clipboardEditor.initComponents();
            _clipboardEditor.setVisible(true);

            _clipboardEditor.addClipboardEventListener(() -> {
                _clipboardEditor.clipboardData.forEach((key, value) -> {
                    if (key.equals("Finish")) {                  // NOI18N
                        _clipboardEditor = null;
                    }
                });
            });
        } else {
            _clipboardEditor.setVisible(true);
        }
    }

    private static String getClassName() {
        return jmri.jmrit.logixng.LogixNG_UserPreferences.class.getName();
    }

    /**
     * Run the thread action on either the ConditionalNG thread or the
     * GUI thread.
     * If the conditionalNG is not null, run it on the conditionalNG thread.
     * If the conditionalNG is null, run it on the GUI thread.
     * The conditionalNG is null when editing the clipboard or a module.
     * @param conditionalNG the conditionalNG or null if no conditionalNG
     * @param ta the thread action
     */
    private void runOnConditionalNGThreadOrGUIThreadEventually(
            ConditionalNG conditionalNG, ThreadingUtil.ThreadAction ta) {

        if (conditionalNG != null) {
            LogixNG_Thread thread = conditionalNG.getCurrentThread();
            thread.runOnLogixNGEventually(ta);
        } else {
            // Run the thread action on the GUI thread. And we already are on the GUI thread.
            ta.run();
        }
    }

    /**
     * When a pop-up action is selected that opens a dialog, the popup menu is locked until the
     * dialog is closed.
     * @return true if the popup menu is locked.
     */
    final protected boolean isPopupMenuLocked() {
        if (_lockPopupMenu) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("TreeEditor_PopupLockMessage"),
                    Bundle.getMessage("TreeEditor_PopupLockTitle"),
                    JmriJOptionPane.INFORMATION_MESSAGE);
        }
        return _lockPopupMenu;
    }

    final protected void setPopupMenuLock(boolean lock) {
        _lockPopupMenu = lock;
    }


    /**
     * Respond to the Add menu choice in the popup menu.
     *
     * @param femaleSocket the female socket
     * @param path the path to the item the user has clicked on
     */
    final protected void renameSocketPressed(FemaleSocket femaleSocket, TreePath path) {
        setPopupMenuLock(true);
        _renameSocketDialog = new JDialog(
                this,
                Bundle.getMessage(
                        "RenameSocketDialogTitle",
                        femaleSocket.getLongDescription()),
                false);
//        _renameSocketDialog.addHelpMenu(
//                "package.jmri.jmrit.logixng.tools.swing.ConditionalNGAddEdit", true);     // NOI18N
        _renameSocketDialog.setLocation(50, 30);
        Container contentPanel = _renameSocketDialog.getContentPane();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel p;
        p = new JPanel();
//        p.setLayout(new FlowLayout());
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(_renameSocketLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_socketNameTextField, c);
        _socketNameTextField.setText(femaleSocket.getName());

        contentPanel.add(p);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener((ActionEvent e) -> {
            cancelRenameSocketPressed(null);
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N

        _renameSocketDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelRenameSocketPressed(null);
            }
        });

        _create = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
        panel5.add(_create);
        _create.addActionListener((ActionEvent e) -> {
            if (femaleSocket.validateName(_socketNameTextField.getText())) {
                femaleSocket.setName(_socketNameTextField.getText());
                cancelRenameSocketPressed(null);
                for (TreeModelListener l : _treePane.femaleSocketTreeModel.listeners) {
                    TreeModelEvent tme = new TreeModelEvent(
                            femaleSocket,
                            path.getPath()
                    );
                    l.treeNodesChanged(tme);
                }
                _treePane._tree.updateUI();
                setPopupMenuLock(false);
            } else {
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ValidateFemaleSocketMessage", _socketNameTextField.getText()),
                        Bundle.getMessage("ValidateFemaleSocketTitle"),
                        JmriJOptionPane.ERROR_MESSAGE);
            }
        });

        contentPanel.add(panel5);

//        _renameSocketDialog.setLocationRelativeTo(component);
        _renameSocketDialog.setLocationRelativeTo(null);
        _renameSocketDialog.pack();
        _renameSocketDialog.setVisible(true);
    }

    /**
     * Respond to the Add menu choice in the popup menu.
     *
     * @param femaleSocket the female socket
     * @param swingConfiguratorInterface the swing configurator used to configure the new class
     * @param path the path to the item the user has clicked on
     */
    final protected void createAddFrame(FemaleSocket femaleSocket, TreePath path,
            SwingConfiguratorInterface swingConfiguratorInterface) {
        // possible change
        _showReminder = true;
        // make an Add Item Frame
        if (_addItemDialog == null) {
            MutableObject<String> commentStr = new MutableObject<>();
            _addSwingConfiguratorInterface = swingConfiguratorInterface;
            // Create item
            _create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
            _create.addActionListener((ActionEvent e) -> {
                _treePane._femaleRootSocket.unregisterListeners();

                runOnConditionalNGThreadOrGUIThreadEventually(
                        _treePane._femaleRootSocket.getConditionalNG(),
                        () -> {

                    List<String> errorMessages = new ArrayList<>();

                    boolean isValid = true;

                    if (!_prefs.getShowSystemUserNames()
                            || (_systemName.getText().isEmpty() && _autoSystemName.isSelected())) {
                        _systemName.setText(_addSwingConfiguratorInterface.getAutoSystemName());
                    }

                    checkAndAdjustSystemName();

                    if (_addSwingConfiguratorInterface.getManager()
                            .validSystemNameFormat(_systemName.getText()) != Manager.NameValidity.VALID) {
                        isValid = false;
                        errorMessages.add(Bundle.getMessage("InvalidSystemName", _systemName.getText()));
                    }

                    isValid &= _addSwingConfiguratorInterface.validate(errorMessages);

                    if (isValid) {
                        MaleSocket socket;
                        if (_addUserName.getText().isEmpty()) {
                            socket = _addSwingConfiguratorInterface.createNewObject(_systemName.getText(), null);
                        } else {
                            socket = _addSwingConfiguratorInterface.createNewObject(_systemName.getText(), _addUserName.getText());
                        }
                        _addSwingConfiguratorInterfaceMaleSocket.updateObject(socket);
    //                    for (Map.Entry<SwingConfiguratorInterface, Base> entry : _swingConfiguratorInterfaceList) {
    //                        entry.getKey().updateObject(entry.getValue());
    //                    }
                        socket.setComment(commentStr.getValue());
                        try {
                            femaleSocket.connect(socket);
                        } catch (SocketAlreadyConnectedException ex) {
                            throw new RuntimeException(ex);
                        }

                        femaleSocket.forEntireTree((Base b) -> {
                            b.addPropertyChangeListener(_treePane);
                        });

                        ThreadingUtil.runOnGUIEventually(() -> {
                            _addSwingConfiguratorInterface.dispose();
                            _addItemDialog.dispose();
                            _addItemDialog = null;

                            for (TreeModelListener l : _treePane.femaleSocketTreeModel.listeners) {
                                TreeModelEvent tme = new TreeModelEvent(
                                        femaleSocket,
                                        path.getPath()
                                );
                                l.treeNodesChanged(tme);
                            }
                            _treePane._tree.expandPath(path);
                            _treePane._tree.updateUI();

                            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                                prefMgr.setCheckboxPreferenceState(_systemNameAuto, _autoSystemName.isSelected());
                            });
                        });
                        setPopupMenuLock(false);
                    } else {
                        StringBuilder errorMsg = new StringBuilder();
                        for (String s : errorMessages) {
                            if (errorMsg.length() > 0) errorMsg.append("<br>");
                            errorMsg.append(s);
                        }
                        JmriJOptionPane.showMessageDialog(null,
                                Bundle.getMessage("ValidateErrorMessage", errorMsg),
                                Bundle.getMessage("ValidateErrorTitle"),
                                JmriJOptionPane.ERROR_MESSAGE);
                    }
                    ThreadingUtil.runOnGUIEventually(() -> {
                        if (_treePane._femaleRootSocket.isActive()) {
                            _treePane._femaleRootSocket.registerListeners();
                        }
                    });
                });
            });
            _create.setToolTipText(Bundle.getMessage("CreateButtonHint"));  // NOI18N

            if (_addSwingConfiguratorInterface != null) {
                makeAddEditFrame(true, femaleSocket, _create, commentStr);
            }
        }
    }

    /**
     * Check the system name format.  Add prefix and/or $ as neeeded.
     */
    void checkAndAdjustSystemName() {
        if (_autoSystemName.isSelected()) {
            return;
        }

        var sName = _systemName.getText().trim();
        var prefix = _addSwingConfiguratorInterface.getManager().getSubSystemNamePrefix();

        if (!sName.isEmpty() && !sName.startsWith(prefix)) {
            var isNumber = sName.matches("^\\d+$");
            var hasDollar = sName.startsWith("$");

            var newName = new StringBuilder(prefix);
            if (!isNumber && !hasDollar) {
                newName.append("$");
            }
            newName.append(sName);
            sName = newName.toString();
        }

        _systemName.setText(sName);
        return;
    }

    /**
     * Respond to the Edit menu choice in the popup menu.
     *
     * @param femaleSocket the female socket
     * @param path the path to the item the user has clicked on
     */
    final protected void editPressed(FemaleSocket femaleSocket, TreePath path) {
        setPopupMenuLock(true);

        // possible change
        _showReminder = true;
        // make an Edit Frame
        if (_editActionExpressionDialog == null) {
            Base object = femaleSocket.getConnectedSocket().getObject();
            MutableObject<String> commentStr = new MutableObject<>(object.getComment());

            // Edit ConditionalNG
            _edit = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
            _edit.addActionListener((ActionEvent e) -> {

                runOnConditionalNGThreadOrGUIThreadEventually(
                        _treePane._femaleRootSocket.getConditionalNG(),
                        () -> {

                    List<String> errorMessages = new ArrayList<>();

                    boolean isValid = true;

                    if (_editSwingConfiguratorInterface.getManager() != null) {
                        if (_editSwingConfiguratorInterface.getManager()
                                .validSystemNameFormat(_systemName.getText()) != Manager.NameValidity.VALID) {
                            isValid = false;
                            errorMessages.add(Bundle.getMessage("InvalidSystemName", _systemName.getText()));
                        }
                    } else {
                        log.debug("_editSwingConfiguratorInterface.getManager() returns null");
                    }

                    isValid &= _editSwingConfiguratorInterface.validate(errorMessages);

                    boolean canClose = true;
                    for (Map.Entry<SwingConfiguratorInterface, Base> entry : _swingConfiguratorInterfaceList) {
                        if (!entry.getKey().canClose()) {
                            canClose = false;
                            break;
                        }
                    }

                    if (isValid && canClose) {
                        ThreadingUtil.runOnGUIEventually(() -> {
                            femaleSocket.unregisterListeners();

//                            Base object = femaleSocket.getConnectedSocket().getObject();
                            if (_addUserName.getText().isEmpty()) {
                                ((NamedBean)object).setUserName(null);
                            } else {
                                ((NamedBean)object).setUserName(_addUserName.getText());
                            }
                            ((NamedBean)object).setComment(commentStr.getValue());
                            for (Map.Entry<SwingConfiguratorInterface, Base> entry : _swingConfiguratorInterfaceList) {
                                entry.getKey().updateObject(entry.getValue());
                                entry.getKey().dispose();
                            }
                            for (TreeModelListener l : _treePane.femaleSocketTreeModel.listeners) {
                                TreeModelEvent tme = new TreeModelEvent(
                                        femaleSocket,
                                        path.getPath()
                                );
                                l.treeNodesChanged(tme);
                            }
                            _editActionExpressionDialog.dispose();
                            _editActionExpressionDialog = null;
                            _treePane._tree.updateUI();

//                            if (femaleSocket.isActive()) femaleSocket.registerListeners();
                            if (_treePane._femaleRootSocket.isActive()) {
                                _treePane._femaleRootSocket.registerListeners();
                            }
                        });
                        setPopupMenuLock(false);
                    } else if (!isValid) {
                        StringBuilder errorMsg = new StringBuilder();
                        for (String s : errorMessages) {
                            if (errorMsg.length() > 0) errorMsg.append("<br>");
                            errorMsg.append(s);
                        }
                        ThreadingUtil.runOnGUIEventually(() -> {
                            JmriJOptionPane.showMessageDialog(null,
                                    Bundle.getMessage("ValidateErrorMessage", errorMsg),
                                    Bundle.getMessage("ValidateErrorTitle"),
                                    JmriJOptionPane.ERROR_MESSAGE);
                        });
                    }
                });
            });
            _edit.setToolTipText(Bundle.getMessage("EditButtonHint"));  // NOI18N

            makeAddEditFrame(false, femaleSocket, _edit, commentStr);
        }
    }

    /**
     * Create or edit action/expression dialog.
     *
     * @param addOrEdit true if add, false if edit
     * @param femaleSocket the female socket to which we want to add something
     * @param button a button to add to the dialog
     * @param commentStr the new comment
     */
    final protected void makeAddEditFrame(
            boolean addOrEdit,
            FemaleSocket femaleSocket,
            JButton button,
            MutableObject<String> commentStr) {

        JDialog dialog  = new JDialog(
                this,
                Bundle.getMessage(
                        addOrEdit ? "AddMaleSocketDialogTitle" : "EditMaleSocketDialogTitle",
                        femaleSocket.getLongDescription()),
                false);
//        frame.addHelpMenu(
//                "package.jmri.jmrit.logixng.tools.swing.ConditionalNGAddEdit", true);     // NOI18N
        Container contentPanel = dialog.getContentPane();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel p;
        p = new JPanel();
//        p.setLayout(new FlowLayout());
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        if (_prefs.getShowSystemUserNames()) {
            c.gridx = 0;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.EAST;
            p.add(_sysNameLabel, c);
            c.gridy = 1;
            p.add(_userNameLabel, c);
            c.gridy = 2;
            c.gridx = 1;
            c.gridy = 0;
            c.anchor = java.awt.GridBagConstraints.WEST;
            c.weightx = 1.0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
            p.add(_systemName, c);
            c.gridy = 1;
            p.add(_addUserName, c);
            if (!femaleSocket.isConnected()) {
                c.gridx = 2;
                c.gridy = 1;
                c.anchor = java.awt.GridBagConstraints.WEST;
                c.weightx = 1.0;
                c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
                c.gridy = 0;
                p.add(_autoSystemName, c);
            }

            if (addOrEdit) {
                _systemName.setToolTipText(Bundle.getMessage("SystemNameHint",
                        _addSwingConfiguratorInterface.getExampleSystemName()));
                _addUserName.setToolTipText(Bundle.getMessage("UserNameHint"));
            }
        } else {
            c.gridx = 0;
            c.gridy = 0;
        }
        contentPanel.add(p);

        if (femaleSocket.isConnected()) {
            _systemName.setText(femaleSocket.getConnectedSocket().getSystemName());
            _systemName.setEnabled(false);
            _addUserName.setText(femaleSocket.getConnectedSocket().getUserName());
        } else {
            _systemName.setText("");
            _systemName.setEnabled(true);
            _addUserName.setText("");
        }

        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());

        Base object = null;

        // Get panel for the item
        _swingConfiguratorInterfaceList.clear();
        List<JPanel> panels = new ArrayList<>();
        if (femaleSocket.isConnected()) {
            object = femaleSocket.getConnectedSocket();
            while (object instanceof MaleSocket) {
                SwingConfiguratorInterface swi =
                        SwingTools.getSwingConfiguratorForClass(object.getClass());
                panels.add(swi.getConfigPanel(object, panel5));
                _swingConfiguratorInterfaceList.add(new HashMap.SimpleEntry<>(swi, object));
                object = ((MaleSocket)object).getObject();
            }
            if (object != null) {
                _editSwingConfiguratorInterface =
                        SwingTools.getSwingConfiguratorForClass(object.getClass());
                _editSwingConfiguratorInterface.setJDialog(dialog);
                panels.add(_editSwingConfiguratorInterface.getConfigPanel(object, panel5));
                _swingConfiguratorInterfaceList.add(new HashMap.SimpleEntry<>(_editSwingConfiguratorInterface, object));

                dialog.setTitle(Bundle.getMessage(
                        addOrEdit ? "AddMaleSocketDialogTitleWithType" : "EditMaleSocketDialogTitleWithType",
                        femaleSocket.getLongDescription(),
                        _editSwingConfiguratorInterface.toString())
                );
            } else {
                // 'object' should be an action or expression but is null
                JPanel panel = new JPanel();
                panel.add(new JLabel("Error: femaleSocket.getConnectedSocket().getObject().getObject()....getObject() doesn't return a non MaleSocket"));
                panels.add(panel);
                log.error("femaleSocket.getConnectedSocket().getObject().getObject()....getObject() doesn't return a non MaleSocket");
            }
        } else {
            Class<? extends MaleSocket> maleSocketClass =
                    _addSwingConfiguratorInterface.getManager().getMaleSocketClass();
            _addSwingConfiguratorInterfaceMaleSocket =
                    SwingTools.getSwingConfiguratorForClass(maleSocketClass);

            _addSwingConfiguratorInterfaceMaleSocket.setJDialog(dialog);
            panels.add(_addSwingConfiguratorInterfaceMaleSocket.getConfigPanel(panel5));

            _addSwingConfiguratorInterface.setJDialog(dialog);
            panels.add(_addSwingConfiguratorInterface.getConfigPanel(panel5));

            dialog.setTitle(Bundle.getMessage(
                    addOrEdit ? "AddMaleSocketDialogTitleWithType" : "EditMaleSocketDialogTitleWithType",
                    femaleSocket.getLongDescription(),
                    _addSwingConfiguratorInterface.toString())
            );
        }
        JPanel panel34 = new JPanel();
        panel34.setLayout(new BoxLayout(panel34, BoxLayout.Y_AXIS));
        for (int i = panels.size()-1; i >= 0; i--) {
            JPanel panel = panels.get(i);
            if (panel.getComponentCount() > 0) {
                panel34.add(Box.createVerticalStrut(30));
                panel34.add(panel);
            }
        }
        panel3.add(panel34);
        contentPanel.add(panel3);

        // Edit comment
        JButton editComment = new JButton(Bundle.getMessage("ButtonEditComment"));    // NOI18N
        panel5.add(editComment);
        String comment = object != null ? object.getComment() : "";
        editComment.addActionListener((ActionEvent e) -> {
            commentStr.setValue(new EditCommentDialog().showDialog(comment));
        });

        // Function help
        JButton showFunctionHelp = new JButton(Bundle.getMessage("ButtonFunctionHelp"));    // NOI18N
        panel5.add(showFunctionHelp);
        showFunctionHelp.addActionListener((ActionEvent e) -> {
            InstanceManager.getDefault(FunctionsHelpDialog.class).showDialog();
        });
//        showFunctionHelp.setToolTipText("FunctionHelpButtonHint");      // NOI18N

        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener((ActionEvent e) -> {
            if (!femaleSocket.isConnected()) {
                cancelCreateItem(null);
            } else {
                cancelEditPressed(null);
            }
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N

        panel5.add(button);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (addOrEdit) {
                    cancelCreateItem(null);
                } else {
                    cancelEditPressed(null);
                }
            }
        });

        contentPanel.add(panel5);

        _autoSystemName.addItemListener((ItemEvent e) -> {
            autoSystemName();
        });
//        addLogixNGFrame.setLocationRelativeTo(component);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        dialog.getRootPane().setDefaultButton(button);

        if (addOrEdit) {
            _addItemDialog = dialog;
        } else {
            _editActionExpressionDialog = dialog;
        }

        _autoSystemName.setSelected(true);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            _autoSystemName.setSelected(prefMgr.getCheckboxPreferenceState(_systemNameAuto, true));
        });

        _systemName.setEnabled(addOrEdit);

        dialog.setVisible(true);
    }

    /**
     * Respond to the Local Variables menu choice in the popup menu.
     *
     * @param femaleSocket the female socket
     * @param path the path to the item the user has clicked on
     */
    final protected void editLocalVariables(FemaleSocket femaleSocket, TreePath path) {
        // possible change
        _showReminder = true;
        setPopupMenuLock(true);
        // make an Edit Frame
        if (_editLocalVariablesDialog == null) {
            MaleSocket maleSocket = femaleSocket.getConnectedSocket();

            // Edit ConditionalNG
            _edit = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
            _edit.addActionListener((ActionEvent e) -> {
                List<String> errorMessages = new ArrayList<>();
                boolean hasErrors = false;
                for (SymbolTable.VariableData v : _localVariableTableModel.getVariables()) {
                    if (v.getName().isEmpty()) {
                        errorMessages.add(Bundle.getMessage("VariableNameIsEmpty", v.getName()));
                        hasErrors = true;
                    }
                    if (! SymbolTable.validateName(v.getName())) {
                        errorMessages.add(Bundle.getMessage("VariableNameIsNotValid", v.getName()));
                        hasErrors = true;
                    }
                }

                if (hasErrors) {
                    StringBuilder errorMsg = new StringBuilder();
                    for (String s : errorMessages) {
                        if (errorMsg.length() > 0) errorMsg.append("<br>");
                        errorMsg.append(s);
                    }
                    JmriJOptionPane.showMessageDialog(null,
                            Bundle.getMessage("ValidateErrorMessage", errorMsg),
                            Bundle.getMessage("ValidateErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);

                } else {
                    _treePane._femaleRootSocket.unregisterListeners();

                    runOnConditionalNGThreadOrGUIThreadEventually(
                            _treePane._femaleRootSocket.getConditionalNG(),
                            () -> {

                        maleSocket.clearLocalVariables();
                        for (SymbolTable.VariableData variableData : _localVariableTableModel.getVariables()) {
                            maleSocket.addLocalVariable(variableData);
                        }

                        ThreadingUtil.runOnGUIEventually(() -> {
                            _editLocalVariablesDialog.dispose();
                            _editLocalVariablesDialog = null;
                            if (_treePane._femaleRootSocket.isActive()) {
                                _treePane._femaleRootSocket.registerListeners();
                            }
                            for (TreeModelListener l : _treePane.femaleSocketTreeModel.listeners) {
                                TreeModelEvent tme = new TreeModelEvent(
                                        femaleSocket,
                                        path.getPath()
                                );
                                l.treeNodesChanged(tme);
                            }
                            _treePane._tree.updateUI();
                        });
                        setPopupMenuLock(false);
                    });
                }
            });
//            _edit.setToolTipText(Bundle.getMessage("EditButtonHint"));  // NOI18N

//            makeAddEditFrame(false, femaleSocket, _editSwingConfiguratorInterface, _edit);  // NOI18N

            _editLocalVariablesDialog = new JDialog(
                    this,
                    Bundle.getMessage(
                            "EditLocalVariablesDialogTitle",
                            femaleSocket.getLongDescription()),
                    false);
    //        frame.addHelpMenu(
    //                "package.jmri.jmrit.logixng.tools.swing.ConditionalNGAddEdit", true);     // NOI18N
            Container contentPanel = _editLocalVariablesDialog.getContentPane();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            JTable table = new JTable();
            _localVariableTableModel = new LocalVariableTableModel(maleSocket);
            table.setModel(_localVariableTableModel);
            table.setDefaultRenderer(InitialValueType.class,
                    new LocalVariableTableModel.TypeCellRenderer());
            table.setDefaultEditor(InitialValueType.class,
                    new LocalVariableTableModel.TypeCellEditor());
            table.setDefaultRenderer(LocalVariableTableModel.Menu.class,
                    new LocalVariableTableModel.MenuCellRenderer());
            table.setDefaultEditor(LocalVariableTableModel.Menu.class,
                    new LocalVariableTableModel.MenuCellEditor(table, _localVariableTableModel));
            _localVariableTableModel.setColumnForMenu(table);
            JScrollPane scrollpane = new JScrollPane(table);
            scrollpane.setPreferredSize(new Dimension(400, 200));
            contentPanel.add(scrollpane);

            // set up create and cancel buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            // Function help
            JButton showFunctionHelp = new JButton(Bundle.getMessage("ButtonFunctionHelp"));    // NOI18N
            buttonPanel.add(showFunctionHelp);
            showFunctionHelp.addActionListener((ActionEvent e) -> {
                InstanceManager.getDefault(FunctionsHelpDialog.class).showDialog();
            });
//            showFunctionHelp.setToolTipText("FunctionHelpButtonHint");      // NOI18N

            // Add local variable
            JButton add = new JButton(Bundle.getMessage("TableAddVariable"));
            buttonPanel.add(add);
            add.addActionListener((ActionEvent e) -> {
                _localVariableTableModel.add();
            });

            // Cancel
            JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
            buttonPanel.add(cancel);
            cancel.addActionListener((ActionEvent e) -> {
                _editLocalVariablesDialog.setVisible(false);
                _editLocalVariablesDialog.dispose();
                _editLocalVariablesDialog = null;
                setPopupMenuLock(false);
            });
    //        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
            cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N

            buttonPanel.add(_edit);
            _editLocalVariablesDialog.getRootPane().setDefaultButton(_edit);

            _editLocalVariablesDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _editLocalVariablesDialog.setVisible(false);
                    _editLocalVariablesDialog.dispose();
                    _editLocalVariablesDialog = null;
                    setPopupMenuLock(false);
                }
            });

            contentPanel.add(buttonPanel);

            _autoSystemName.addItemListener((ItemEvent e) -> {
                autoSystemName();
            });
    //        addLogixNGFrame.setLocationRelativeTo(component);
            _editLocalVariablesDialog.pack();
            _editLocalVariablesDialog.setLocationRelativeTo(null);

            _editLocalVariablesDialog.setVisible(true);
        }
    }

    /**
     * Respond to the Change user name menu choice in the popup menu.
     *
     * @param femaleSocket the female socket
     * @param path the path to the item the user has clicked on
     */
    final protected void changeUsername(FemaleSocket femaleSocket, TreePath path) {
        // possible change
        _showReminder = true;
        setPopupMenuLock(true);
        // make an Edit Frame
        if (_changeUsernameDialog == null) {
            MaleSocket maleSocket = femaleSocket.getConnectedSocket();

            // Edit ConditionalNG
            _edit = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
            _edit.addActionListener((ActionEvent e) -> {

                boolean hasErrors = false;
                if (hasErrors) {
                    String errorMsg = "";
                    JmriJOptionPane.showMessageDialog(null,
                            Bundle.getMessage("ValidateErrorMessage", errorMsg),
                            Bundle.getMessage("ValidateErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);

                } else {
                    _treePane._femaleRootSocket.unregisterListeners();

                    runOnConditionalNGThreadOrGUIThreadEventually(
                            _treePane._femaleRootSocket.getConditionalNG(),
                            () -> {

                        String username = _usernameField.getText();
                        if (username.equals("")) username = null;

                        // Only change user name if it's changed
                        if (((username == null) && (maleSocket.getUserName() != null))
                                || ((username != null) && !username.equals(maleSocket.getUserName()))) {

                            if (username != null) {
                                NamedBean nB = maleSocket.getManager().getByUserName(username);
                                if (nB != null) {
                                    String uname = username;
                                    ThreadingUtil.runOnGUIEventually(() -> {
                                        log.error("User name is not unique {}", uname);
                                        String msg = Bundle.getMessage("WarningUserName", new Object[]{("" + uname)});
                                        JmriJOptionPane.showMessageDialog(null, msg,
                                                Bundle.getMessage("WarningTitle"),
                                                JmriJOptionPane.ERROR_MESSAGE);
                                    });
                                    username = null;
                                }
                            }

                            maleSocket.setUserName(username);

                            MaleSocket m = maleSocket;
                            while (! (m instanceof NamedBean)) m = (MaleSocket) m.getObject();

                            NamedBeanHandleManager nbMan = InstanceManager.getDefault(NamedBeanHandleManager.class);
                            if (nbMan.inUse(maleSocket.getSystemName(), (NamedBean)m)) {
                                String msg = Bundle.getMessage("UpdateToUserName", new Object[]{maleSocket.getManager().getBeanTypeHandled(), username, maleSocket.getSystemName()});
                                int optionPane = JmriJOptionPane.showConfirmDialog(null,
                                        msg, Bundle.getMessage("UpdateToUserNameTitle"),
                                        JmriJOptionPane.YES_NO_OPTION);
                                if (optionPane == JmriJOptionPane.YES_OPTION) {
                                    //This will update the bean reference from the systemName to the userName
                                    try {
                                        nbMan.updateBeanFromSystemToUser((NamedBean)m);
                                    } catch (JmriException ex) {
                                        //We should never get an exception here as we already check that the username is not valid
                                        log.error("Impossible exception setting user name", ex);
                                    }
                                }
                            }
                        }

                        ThreadingUtil.runOnGUIEventually(() -> {
                            if (_treePane._femaleRootSocket.isActive()) {
                                _treePane._femaleRootSocket.registerListeners();
                            }
                            _changeUsernameDialog.dispose();
                            _changeUsernameDialog = null;
                            for (TreeModelListener l : _treePane.femaleSocketTreeModel.listeners) {
                                TreeModelEvent tme = new TreeModelEvent(
                                        femaleSocket,
                                        path.getPath()
                                );
                                l.treeNodesChanged(tme);
                            }
                            _treePane._tree.updateUI();
                        });
                        setPopupMenuLock(false);
                    });
                }
            });
//            _edit.setToolTipText(Bundle.getMessage("EditButtonHint"));  // NOI18N

//            makeAddEditFrame(false, femaleSocket, _editSwingConfiguratorInterface, _edit);  // NOI18N

            _changeUsernameDialog = new JDialog(
                    this,
                    Bundle.getMessage(
                            "EditLocalVariablesDialogTitle",
                            femaleSocket.getLongDescription()),
                    false);
    //        frame.addHelpMenu(
    //                "package.jmri.jmrit.logixng.tools.swing.ConditionalNGAddEdit", true);     // NOI18N
            Container contentPanel = _changeUsernameDialog.getContentPane();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

//            JPanel tablePanel = new JPanel();

            JLabel usernameLabel = new JLabel("Username");
            _usernameField.setText(maleSocket.getUserName());

            contentPanel.add(usernameLabel);
            contentPanel.add(_usernameField);

            // set up create and cancel buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            // Cancel
            JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
            buttonPanel.add(cancel);
            cancel.addActionListener((ActionEvent e) -> {
                _changeUsernameDialog.setVisible(false);
                _changeUsernameDialog.dispose();
                _changeUsernameDialog = null;
                setPopupMenuLock(false);
            });
    //        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
            cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N

            buttonPanel.add(_edit);
            _changeUsernameDialog.getRootPane().setDefaultButton(_edit);

            _changeUsernameDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _changeUsernameDialog.setVisible(false);
                    _changeUsernameDialog.dispose();
                    _changeUsernameDialog = null;
                    setPopupMenuLock(false);
                }
            });

            contentPanel.add(buttonPanel);

            _autoSystemName.addItemListener((ItemEvent e) -> {
                autoSystemName();
            });
    //        addLogixNGFrame.setLocationRelativeTo(component);
            _changeUsernameDialog.pack();
            _changeUsernameDialog.setLocationRelativeTo(null);

            _changeUsernameDialog.setVisible(true);
        }
    }

    /**
     * Enable/disable fields for data entry when user selects to have system
     * name automatically generated.
     */
    final protected void autoSystemName() {
        if (_autoSystemName.isSelected()) {
            _systemName.setEnabled(false);
            _sysNameLabel.setEnabled(false);
        } else {
            _systemName.setEnabled(true);
            _sysNameLabel.setEnabled(true);
        }
    }

    /**
     * Respond to the Cancel button in Rename socket window.
     * <p>
     * Note: Also get there if the user closes the Rename socket window.
     *
     * @param e The event heard
     */
    final protected void cancelRenameSocketPressed(ActionEvent e) {
        _renameSocketDialog.setVisible(false);
        _renameSocketDialog.dispose();
        _renameSocketDialog = null;
        setPopupMenuLock(false);
        this.setVisible(true);
    }

    /**
     * Respond to the Cancel button in Add ConditionalNG window.
     * <p>
     * Note: Also get there if the user closes the Add ConditionalNG window.
     *
     * @param e The event heard
     */
    final protected void cancelCreateItem(ActionEvent e) {
        _addItemDialog.setVisible(false);
        _addSwingConfiguratorInterface.dispose();
        _addItemDialog.dispose();
        _addItemDialog = null;
        setPopupMenuLock(false);
//        _inCopyMode = false;
        this.setVisible(true);
    }


    /**
     * Respond to the Cancel button in Add ConditionalNG window.
     * <p>
     * Note: Also get there if the user closes the Add ConditionalNG window.
     *
     * @param e The event heard
     */
    final protected void cancelEditPressed(ActionEvent e) {
        for (Map.Entry<SwingConfiguratorInterface, Base> entry : _swingConfiguratorInterfaceList) {
            // Abort if we cannot close the dialog
            if (!entry.getKey().canClose()) return;
        }

        _editActionExpressionDialog.setVisible(false);

        for (Map.Entry<SwingConfiguratorInterface, Base> entry : _swingConfiguratorInterfaceList) {
            entry.getKey().dispose();
        }
        _editActionExpressionDialog.dispose();
        _editActionExpressionDialog = null;
        setPopupMenuLock(false);
        this.setVisible(true);
    }


    protected void executeEvaluate(SwingConfiguratorInterface swi, MaleSocket maleSocket) {
        swi.executeEvaluate(maleSocket);
    }

    private boolean itemIsSystem(FemaleSocket femaleSocket) {
        return (femaleSocket.isConnected())
                && femaleSocket.getConnectedSocket().isSystem();
    }

    private boolean parentIsSystem(FemaleSocket femaleSocket) {
        Base parent = femaleSocket.getParent();
        while ((parent != null) && !(femaleSocket.getParent() instanceof MaleSocket)) {
            parent = parent.getParent();
        }
        return (parent != null) && ((MaleSocket)parent).isSystem();
    }

    /**
     * Asks the user if edit a system node.
     * @return true if not edit system node, else return false
     */
    private boolean abortEditAboutSystem(Base b) {
        int result = JmriJOptionPane.showConfirmDialog(
                this,
                Bundle.getMessage("TreeEditor_ChangeSystemNode"),
                b.getLongDescription(),
                JmriJOptionPane.YES_NO_OPTION);

        return ( result != JmriJOptionPane.YES_OPTION );
    }

    private void editItem(FemaleSocket femaleSocket, TreePath path) {
        if (itemIsSystem(femaleSocket) && abortEditAboutSystem(femaleSocket.getConnectedSocket())) {
            return;
        }
        editPressed(femaleSocket, path);
    }

    private void removeItem(FemaleSocket femaleSocket, TreePath path) {
        if ((parentIsSystem(femaleSocket) || itemIsSystem(femaleSocket)) && abortEditAboutSystem(femaleSocket.getConnectedSocket())) {
            return;
        }
        DeleteBeanWorker worker = new DeleteBeanWorker(femaleSocket, path);
        worker.execute();
    }

    private void cutItem(FemaleSocket femaleSocket, TreePath path) {
        if ((parentIsSystem(femaleSocket) || itemIsSystem(femaleSocket)) && abortEditAboutSystem(femaleSocket.getConnectedSocket())) {
            return;
        }

        if (femaleSocket.isConnected()) {
            _treePane._femaleRootSocket.unregisterListeners();

            runOnConditionalNGThreadOrGUIThreadEventually(
                    _treePane._femaleRootSocket.getConditionalNG(),
                    () -> {
                Clipboard clipboard =
                        InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();
                List<String> errors = new ArrayList<>();
                MaleSocket maleSocket = femaleSocket.getConnectedSocket();
                femaleSocket.disconnect();
                if (!clipboard.add(maleSocket, errors)) {
                    JmriJOptionPane.showMessageDialog(this,
                            String.join("<br>", errors),
                            Bundle.getMessage("TitleError"),
                            JmriJOptionPane.ERROR_MESSAGE);
                }
                ThreadingUtil.runOnGUIEventually(() -> {
                    maleSocket.forEntireTree((Base b) -> {
                        b.removePropertyChangeListener(_treePane);
                        if (_clipboardEditor != null) {
                            b.addPropertyChangeListener(_clipboardEditor._treePane);
                        }
                    });
                    _treePane._femaleRootSocket.registerListeners();
                    _treePane.updateTree(femaleSocket, path.getPath());
                });
            });
        } else {
            log.error("_currentFemaleSocket is not connected");
        }
    }

    private void copyItem(FemaleSocket femaleSocket) {
        if ((parentIsSystem(femaleSocket) || itemIsSystem(femaleSocket)) && abortEditAboutSystem(femaleSocket.getConnectedSocket())) {
            return;
        }

        if (femaleSocket.isConnected()) {
           _treePane._femaleRootSocket.unregisterListeners();

           runOnConditionalNGThreadOrGUIThreadEventually(
                   _treePane._femaleRootSocket.getConditionalNG(),
                   () -> {
               Clipboard clipboard =
                       InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();
               Map<String, String> systemNames = new HashMap<>();
               Map<String, String> userNames = new HashMap<>();
               MaleSocket maleSocket = null;
               try {
                   maleSocket = (MaleSocket) femaleSocket
                           .getConnectedSocket()
                           .getDeepCopy(systemNames, userNames);
                   List<String> errors = new ArrayList<>();
                   if (!clipboard.add(
                           maleSocket,
                           errors)) {
                       JmriJOptionPane.showMessageDialog(this,
                               String.join("<br>", errors),
                               Bundle.getMessage("TitleError"),
                               JmriJOptionPane.ERROR_MESSAGE);
                   }
               } catch (JmriException ex) {
                   log.error("getDeepCopy thrown exception: {}", ex, ex);
                   ThreadingUtil.runOnGUIEventually(() -> {
                       JmriJOptionPane.showMessageDialog(null,
                               "An exception has occured: "+ex.getMessage(),
                               "An error has occured",
                               JmriJOptionPane.ERROR_MESSAGE);
                   });
               }
               if (maleSocket != null) {
                   MaleSocket socket = maleSocket;
                   ThreadingUtil.runOnGUIEventually(() -> {
                       socket.forEntireTree((Base b) -> {
                           if (_clipboardEditor != null) {
                               b.addPropertyChangeListener(_clipboardEditor._treePane);
                           }
                       });
                   });
               }
           });

           _treePane._femaleRootSocket.registerListeners();
       } else {
           log.error("_currentFemaleSocket is not connected");
       }
    }

    private void pasteItem(FemaleSocket femaleSocket, TreePath path) {
        if (parentIsSystem(femaleSocket) && abortEditAboutSystem(femaleSocket.getParent())) {
            return;
        }

        if (! femaleSocket.isConnected()) {
            _treePane._femaleRootSocket.unregisterListeners();

            runOnConditionalNGThreadOrGUIThreadEventually(
                    _treePane._femaleRootSocket.getConditionalNG(),
                    () -> {
                Clipboard clipboard =
                        InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();
                try {
                    if (clipboard.getTopItem() == null) {
                        return;
                    }
                    if (!femaleSocket.isCompatible(clipboard.getTopItem())) {
                        log.error("Top item on clipboard is not compatible with the female socket");
                        return;
                    }
                    femaleSocket.connect(clipboard.fetchTopItem());
                    List<String> errors = new ArrayList<>();
                    if (!femaleSocket.setParentForAllChildren(errors)) {
                        JmriJOptionPane.showMessageDialog(this,
                                String.join("<br>", errors),
                                Bundle.getMessage("TitleError"),
                                JmriJOptionPane.ERROR_MESSAGE);
                    }
                } catch (SocketAlreadyConnectedException ex) {
                    log.error("item cannot be connected", ex);
                }
                ThreadingUtil.runOnGUIEventually(() -> {
                    _treePane._femaleRootSocket.forEntireTree((Base b) -> {
                        // Remove the listener if it is already
                        // added so we don't end up with duplicate
                        // listeners.
                        b.removePropertyChangeListener(_treePane);
                        b.addPropertyChangeListener(_treePane);
                    });
                    _treePane._femaleRootSocket.registerListeners();
                    _treePane.updateTree(femaleSocket, path.getPath());
                });
            });
        } else {
            log.error("_currentFemaleSocket is connected");
        }
    }

    private void pasteCopy(FemaleSocket femaleSocket, TreePath path) {
        if (parentIsSystem(femaleSocket) && abortEditAboutSystem(femaleSocket.getParent())) {
            return;
        }

        if (! femaleSocket.isConnected()) {
            _treePane._femaleRootSocket.unregisterListeners();

            runOnConditionalNGThreadOrGUIThreadEventually(
                    _treePane._femaleRootSocket.getConditionalNG(),
                    () -> {
                Clipboard clipboard =
                        InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();

                if (clipboard.getTopItem() == null) {
                    return;
                }
                if (!femaleSocket.isCompatible(clipboard.getTopItem())) {
                    log.error("Top item on clipboard is not compatible with the female socket");
                    return;
                }
                Map<String, String> systemNames = new HashMap<>();
                Map<String, String> userNames = new HashMap<>();
                MaleSocket maleSocket = null;
                try {
                    maleSocket = (MaleSocket) clipboard.getTopItem()
                            .getDeepCopy(systemNames, userNames);
                } catch (JmriException ex) {
                    log.error("getDeepCopy thrown exception: {}", ex, ex);
                    ThreadingUtil.runOnGUIEventually(() -> {
                        JmriJOptionPane.showMessageDialog(null,
                                "An exception has occured: "+ex.getMessage(),
                                "An error has occured",
                                JmriJOptionPane.ERROR_MESSAGE);
                    });
                }
                if (maleSocket != null) {
                    try {
                        femaleSocket.connect(maleSocket);
                        List<String> errors = new ArrayList<>();
                        if (!femaleSocket.setParentForAllChildren(errors)) {
                            JmriJOptionPane.showMessageDialog(this,
                                    String.join("<br>", errors),
                                    Bundle.getMessage("TitleError"),
                                    JmriJOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SocketAlreadyConnectedException ex) {
                        log.error("item cannot be connected", ex);
                    }
                    ThreadingUtil.runOnGUIEventually(() -> {
                        _treePane._femaleRootSocket.forEntireTree((Base b) -> {
                            // Remove the listener if it is already
                            // added so we don't end up with duplicate
                            // listeners.
                            b.removePropertyChangeListener(_treePane);
                            b.addPropertyChangeListener(_treePane);
                        });
                        _treePane._femaleRootSocket.registerListeners();
                        _treePane.updateTree(femaleSocket, path.getPath());
                    });
                }
            });
        } else {
            log.error("_currentFemaleSocket is connected");
        }
    }

    private void doIt(String command, FemaleSocket femaleSocket, TreePath path) {
        Base parent = femaleSocket.getParent();
        while ((parent != null) && !(femaleSocket.getParent() instanceof MaleSocket)) {
            parent = parent.getParent();
        }
        boolean parentIsSystem = (parent != null) && ((MaleSocket)parent).isSystem();
        boolean itemIsSystem = itemIsSystem(femaleSocket);

        switch (command) {
            case ACTION_COMMAND_RENAME_SOCKET:
                if (parentIsSystem && abortEditAboutSystem(femaleSocket.getParent())) break;
                renameSocketPressed(femaleSocket, path);
                break;

            case ACTION_COMMAND_EDIT:
                editItem(femaleSocket, path);
                break;

            case ACTION_COMMAND_REMOVE:
                removeItem(femaleSocket, path);
                break;

            case ACTION_COMMAND_CUT:
                cutItem(femaleSocket, path);
                break;

            case ACTION_COMMAND_COPY:
                copyItem(femaleSocket);
                break;

            case ACTION_COMMAND_PASTE:
                pasteItem(femaleSocket, path);
                break;

            case ACTION_COMMAND_PASTE_COPY:
                pasteCopy(femaleSocket, path);
                break;

            case ACTION_COMMAND_ENABLE:
                if (itemIsSystem && abortEditAboutSystem(femaleSocket.getConnectedSocket())) break;

                femaleSocket.getConnectedSocket().setEnabled(true);
                runOnConditionalNGThreadOrGUIThreadEventually(
                        _treePane._femaleRootSocket.getConditionalNG(),
                        () -> {
                    ThreadingUtil.runOnGUIEventually(() -> {
                        _treePane._femaleRootSocket.unregisterListeners();
                        _treePane.updateTree(femaleSocket, path.getPath());
                        _treePane._femaleRootSocket.registerListeners();
                    });
                });
                break;

            case ACTION_COMMAND_DISABLE:
                if (itemIsSystem && abortEditAboutSystem(femaleSocket.getConnectedSocket())) break;

                femaleSocket.getConnectedSocket().setEnabled(false);
                runOnConditionalNGThreadOrGUIThreadEventually(
                        _treePane._femaleRootSocket.getConditionalNG(),
                        () -> {
                    ThreadingUtil.runOnGUIEventually(() -> {
                        _treePane._femaleRootSocket.unregisterListeners();
                        _treePane.updateTree(femaleSocket, path.getPath());
                        _treePane._femaleRootSocket.registerListeners();
                    });
                });
                break;

            case ACTION_COMMAND_LOCK:
                if (itemIsSystem && abortEditAboutSystem(femaleSocket.getConnectedSocket())) break;

                femaleSocket.forEntireTree((item) -> {
                    if (item instanceof MaleSocket) {
                        ((MaleSocket)item).setLocked(true);
                    }
                });
                _treePane.updateTree(femaleSocket, path.getPath());
                break;

            case ACTION_COMMAND_UNLOCK:
                if (itemIsSystem && abortEditAboutSystem(femaleSocket.getConnectedSocket())) break;

                femaleSocket.forEntireTree((item) -> {
                    if (item instanceof MaleSocket) {
                        ((MaleSocket)item).setLocked(false);
                    }
                });
                _treePane.updateTree(femaleSocket, path.getPath());
                break;

            case ACTION_COMMAND_LOCAL_VARIABLES:
                if (itemIsSystem && abortEditAboutSystem(femaleSocket.getConnectedSocket())) break;
                editLocalVariables(femaleSocket, path);
                break;

            case ACTION_COMMAND_CHANGE_USERNAME:
                if (itemIsSystem && abortEditAboutSystem(femaleSocket.getConnectedSocket())) break;
                changeUsername(femaleSocket, path);
                break;

            case ACTION_COMMAND_EXECUTE_EVALUATE:
                Base object = femaleSocket.getConnectedSocket();
                if (object == null) throw new NullPointerException("object is null");
                while (object instanceof MaleSocket) {
                    object = ((MaleSocket)object).getObject();
                }
                SwingConfiguratorInterface swi =
                        SwingTools.getSwingConfiguratorForClass(object.getClass());
                executeEvaluate(swi, femaleSocket.getConnectedSocket());
                break;

/*
            case ACTION_COMMAND_EXPAND_TREE:
                // jtree expand sub tree
                // https://stackoverflow.com/questions/15210979/how-do-i-auto-expand-a-jtree-when-setting-a-new-treemodel
                // https://www.tutorialspoint.com/how-to-expand-jtree-row-to-display-all-the-nodes-and-child-nodes-in-java
                // To expand all rows, do this:
                for (int i = 0; i < tree.getRowCount(); i++) {
                    tree.expandRow(i);
                }

                tree.expandPath(_currentPath);
                tree.updateUI();
                break;
*/
            default:
                // Check if the action is a female socket operation
                if (!checkFemaleSocketOperation(femaleSocket, parentIsSystem, itemIsSystem, command)) {
                    log.error("e.getActionCommand() returns unknown value {}", command);
                }
        }
    }

    private boolean checkFemaleSocketOperation(
            FemaleSocket femaleSocket,
            boolean parentIsSystem,
            boolean itemIsSystem,
            String command) {

        for (FemaleSocketOperation oper : FemaleSocketOperation.values()) {
            if (oper.name().equals(command)) {
                if ((parentIsSystem || itemIsSystem) && abortEditAboutSystem(femaleSocket.getParent())) return true;
                femaleSocket.doSocketOperation(oper);
                return true;
            }
        }
        return false;
    }

    private boolean parentIsLocked(FemaleSocket femaleSocket) {
        Base parent = femaleSocket.getParent();
        while ((parent != null) && !(parent instanceof MaleSocket)) {
            parent = parent.getParent();
        }
        return (parent != null) && ((MaleSocket)parent).isLocked();
    }

    protected final class PopupMenu extends JPopupMenu implements ActionListener {

        private final JTree _tree;
//        private final FemaleSocketTreeModel _model;
        private final FemaleSocket _currentFemaleSocket;
        private final TreePath _currentPath;

        private JMenuItem menuItemRenameSocket;
        private JMenuItem menuItemRemove;
        private JMenuItem menuItemCut;
        private JMenuItem menuItemCopy;
        private JMenuItem menuItemPaste;
        private JMenuItem menuItemPasteCopy;
        private final Map<FemaleSocketOperation, JMenuItem> menuItemFemaleSocketOperation
                = new HashMap<>();
        private JMenuItem menuItemEnable;
        private JMenuItem menuItemDisable;
        private JMenuItem menuItemLock;
        private JMenuItem menuItemUnlock;
        private JMenuItem menuItemLocalVariables;
        private JMenuItem menuItemChangeUsername;
        private JMenuItem menuItemExecuteEvaluate;
//        private JMenuItem menuItemExpandTree;

        private final boolean _isConnected;
        private final boolean _canConnectFromClipboard;
        private final boolean _disableForRoot;
        private final boolean _isLocked;
        private final boolean _parentIsLocked;


        PopupMenu(int x, int y, FemaleSocket femaleSocket, TreePath path, boolean onlyAddItems) {

            if (_treePane._tree == null) throw new IllegalArgumentException("_tree is null");

            _tree = _treePane._tree;

            _currentFemaleSocket = femaleSocket;
            _currentPath = path;
            _isConnected = femaleSocket.isConnected();

            Clipboard clipboard = InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();

            MaleSocket topItem = clipboard.getTopItem();

            _canConnectFromClipboard =
                    topItem != null
                    && femaleSocket.isCompatible(topItem)
                    && !femaleSocket.isAncestor(topItem);

            _disableForRoot = _disableRootRemoveCutCopy
                    && (_currentFemaleSocket == _treePane._femaleRootSocket);

            _isLocked = _isConnected && femaleSocket.getConnectedSocket().isLocked();

            _parentIsLocked = parentIsLocked(femaleSocket);

            if (onlyAddItems) {
                addNewItemTypes(this);
            } else {
                if (_disableRootPopup
                        && (_currentFemaleSocket == _treePane._femaleRootSocket)) {
                    JmriJOptionPane.showMessageDialog(null,
                            Bundle.getMessage("TreeEditor_RootHasNoPopupMenu"),
                            Bundle.getMessage("TreeEditor_Info"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }

                menuItemRenameSocket = new JMenuItem(Bundle.getMessage("PopupMenuRenameSocket"));
                menuItemRenameSocket.addActionListener(this);
                menuItemRenameSocket.setActionCommand(ACTION_COMMAND_RENAME_SOCKET);
                add(menuItemRenameSocket);
                addSeparator();

                if (!_isConnected && !_parentIsLocked) {
                    JMenu addMenu = new JMenu(Bundle.getMessage("PopupMenuAdd"));
//                    addMenu.setMnemonic(KeyEvent.VK_F);
//                    addMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                    addNewItemTypes(addMenu);
                    add(addMenu);
                }

                if (_isConnected && !_isLocked) {
                    JMenuItem menuItemEdit = new JMenuItem(Bundle.getMessage("PopupMenuEdit"));
                    menuItemEdit.addActionListener(this);
                    menuItemEdit.setActionCommand(ACTION_COMMAND_EDIT);
                    menuItemEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                    add(menuItemEdit);
                }
                addSeparator();
                menuItemRemove = new JMenuItem(Bundle.getMessage("PopupMenuRemove"));
                menuItemRemove.addActionListener(this);
                menuItemRemove.setActionCommand(ACTION_COMMAND_REMOVE);
                menuItemRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                add(menuItemRemove);
                addSeparator();
                menuItemCut = new JMenuItem(Bundle.getMessage("PopupMenuCut"));
                menuItemCut.addActionListener(this);
                menuItemCut.setActionCommand(ACTION_COMMAND_CUT);
                menuItemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                add(menuItemCut);
                menuItemCopy = new JMenuItem(Bundle.getMessage("PopupMenuCopy"));
                menuItemCopy.addActionListener(this);
                menuItemCopy.setActionCommand(ACTION_COMMAND_COPY);
                menuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                add(menuItemCopy);
                menuItemPaste = new JMenuItem(Bundle.getMessage("PopupMenuPaste"));
                menuItemPaste.addActionListener(this);
                menuItemPaste.setActionCommand(ACTION_COMMAND_PASTE);
                menuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                add(menuItemPaste);
                menuItemPasteCopy = new JMenuItem(Bundle.getMessage("PopupMenuPasteCopy"));
                menuItemPasteCopy.addActionListener(this);
                menuItemPasteCopy.setActionCommand(ACTION_COMMAND_PASTE_COPY);
                menuItemPasteCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() + InputEvent.SHIFT_DOWN_MASK));
                add(menuItemPasteCopy);
                addSeparator();

                for (FemaleSocketOperation oper : FemaleSocketOperation.values()) {
                    JMenuItem menuItem = new JMenuItem(oper.toString());
                    menuItem.addActionListener(this);
                    menuItem.setActionCommand(oper.name());
                    add(menuItem);
                    menuItemFemaleSocketOperation.put(oper, menuItem);
                    if (oper.hasKey()) {
                        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                                oper.getKeyCode(), oper.getModifiers()));
                    }
                }

                addSeparator();
                menuItemEnable = new JMenuItem(Bundle.getMessage("PopupMenuEnable"));
                menuItemEnable.addActionListener(this);
                menuItemEnable.setActionCommand(ACTION_COMMAND_ENABLE);
                menuItemEnable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() + InputEvent.SHIFT_DOWN_MASK));
                add(menuItemEnable);
                menuItemDisable = new JMenuItem(Bundle.getMessage("PopupMenuDisable"));
                menuItemDisable.addActionListener(this);
                menuItemDisable.setActionCommand(ACTION_COMMAND_DISABLE);
                menuItemDisable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                add(menuItemDisable);
                menuItemLock = new JMenuItem(Bundle.getMessage("PopupMenuLock"));
                menuItemLock.addActionListener(this);
                menuItemLock.setActionCommand(ACTION_COMMAND_LOCK);
                add(menuItemLock);
                menuItemUnlock = new JMenuItem(Bundle.getMessage("PopupMenuUnlock"));
                menuItemUnlock.addActionListener(this);
                menuItemUnlock.setActionCommand(ACTION_COMMAND_UNLOCK);
                add(menuItemUnlock);

                addSeparator();
                menuItemLocalVariables = new JMenuItem(Bundle.getMessage("PopupMenuLocalVariables"));
                menuItemLocalVariables.addActionListener(this);
                menuItemLocalVariables.setActionCommand(ACTION_COMMAND_LOCAL_VARIABLES);
                add(menuItemLocalVariables);

                addSeparator();
                menuItemChangeUsername = new JMenuItem(Bundle.getMessage("PopupMenuChangeUsername"));
                menuItemChangeUsername.addActionListener(this);
                menuItemChangeUsername.setActionCommand(ACTION_COMMAND_CHANGE_USERNAME);
                add(menuItemChangeUsername);

                if (_enableExecuteEvaluate) {
                    addSeparator();
                    menuItemExecuteEvaluate = new JMenuItem();  // The text is set later
                    menuItemExecuteEvaluate.addActionListener(this);
                    menuItemExecuteEvaluate.setActionCommand(ACTION_COMMAND_EXECUTE_EVALUATE);
                    add(menuItemExecuteEvaluate);
                }
    /*
                addSeparator();
                menuItemExpandTree = new JMenuItem(Bundle.getMessage("PopupMenuExpandTree"));
                menuItemExpandTree.addActionListener(this);
                menuItemExpandTree.setActionCommand(ACTION_COMMAND_EXPAND_TREE);
                add(menuItemExpandTree);
    */
                setOpaque(true);
                setLightWeightPopupEnabled(true);

                menuItemRemove.setEnabled(_isConnected && !_isLocked && !_parentIsLocked && !_disableForRoot);
                menuItemCut.setEnabled(_isConnected && !_isLocked && !_parentIsLocked && !_disableForRoot);
                menuItemCopy.setEnabled(_isConnected && !_disableForRoot);
                menuItemPaste.setEnabled(!_isConnected && !_parentIsLocked && _canConnectFromClipboard);
                menuItemPasteCopy.setEnabled(!_isConnected && !_parentIsLocked && _canConnectFromClipboard);

                if (_isConnected && !_disableForRoot) {
                    menuItemEnable.setEnabled(!femaleSocket.getConnectedSocket().isEnabled() && !_isLocked);
                    menuItemDisable.setEnabled(femaleSocket.getConnectedSocket().isEnabled() && !_isLocked);
                } else {
                    menuItemEnable.setEnabled(false);
                    menuItemDisable.setEnabled(false);
                }

                for (FemaleSocketOperation oper : FemaleSocketOperation.values()) {
                    JMenuItem menuItem = menuItemFemaleSocketOperation.get(oper);
                    menuItem.setEnabled(femaleSocket.isSocketOperationAllowed(oper) && !_parentIsLocked);
                }

                AtomicBoolean isAnyLocked = new AtomicBoolean(false);
                AtomicBoolean isAnyUnlocked = new AtomicBoolean(false);

                _currentFemaleSocket.forEntireTree((item) -> {
                    if (item instanceof MaleSocket) {
                        isAnyLocked.set(isAnyLocked.get() || ((MaleSocket)item).isLocked());
                        isAnyUnlocked.set(isAnyUnlocked.get() || !((MaleSocket)item).isLocked());
                    }
                });
                menuItemLock.setEnabled(isAnyUnlocked.get());
                menuItemUnlock.setEnabled(isAnyLocked.get());

                menuItemLocalVariables.setEnabled(
                        femaleSocket.isConnected()
                        && femaleSocket.getConnectedSocket().isSupportingLocalVariables()
                        && !_isLocked);

                menuItemChangeUsername.setEnabled(femaleSocket.isConnected() && !_isLocked);

                if (_enableExecuteEvaluate) {
                    menuItemExecuteEvaluate.setEnabled(femaleSocket.isConnected());

                    if (femaleSocket.isConnected()) {
                        Base object = _currentFemaleSocket.getConnectedSocket();
                        if (object == null) throw new NullPointerException("object is null");
                        while (object instanceof MaleSocket) {
                            object = ((MaleSocket)object).getObject();
                        }
                        menuItemExecuteEvaluate.setText(
                                SwingTools.getSwingConfiguratorForClass(object.getClass())
                                        .getExecuteEvaluateMenuText());
                    }
                }
            }

            show(_tree, x, y);
        }

        private void addNewItemTypes(Container container) {
            Map<Category, List<Class<? extends Base>>> connectableClasses =
                    _currentFemaleSocket.getConnectableClasses();
            List<Category> list = new ArrayList<>(connectableClasses.keySet());
            Collections.sort(list);
            for (Category category : list) {
                List<SwingConfiguratorInterface> sciList = new ArrayList<>();
                List<Class<? extends Base>> classes = connectableClasses.get(category);
                if (classes != null && !classes.isEmpty()) {
                    for (Class<? extends Base> clazz : classes) {
                        SwingConfiguratorInterface sci = SwingTools.getSwingConfiguratorForClass(clazz);
                        if (sci != null) {
                            sciList.add(sci);
                        } else {
                            log.error("Class {} has no swing configurator interface", clazz.getName());
                        }
                    }
                }

                Collections.sort(sciList);

                JMenu categoryMenu = new JMenu(category.toString());
                for (SwingConfiguratorInterface sci : sciList) {
                    JMenuItem item = new JMenuItem(sci.toString());
                    item.addActionListener((e) -> {
                        createAddFrame(_currentFemaleSocket, _currentPath, sci);
                    });
                    categoryMenu.add(item);
                }
                container.add(categoryMenu);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doIt(e.getActionCommand(), _currentFemaleSocket, _currentPath);
        }

    }


    // This class is copied from BeanTableDataModel
    private class DeleteBeanWorker extends SwingWorker<Void, Void> {

        private final FemaleSocket _currentFemaleSocket;
        private final TreePath _currentPath;
        MaleSocket _maleSocket;

        public DeleteBeanWorker(FemaleSocket currentFemaleSocket, TreePath currentPath) {
            _currentFemaleSocket = currentFemaleSocket;
            _currentPath = currentPath;
            _maleSocket = _currentFemaleSocket.getConnectedSocket();
        }

        public int getDisplayDeleteMsg() {
            return InstanceManager.getDefault(UserPreferencesManager.class).getMultipleChoiceOption(getClassName(), "deleteInUse");
        }

        public void setDisplayDeleteMsg(int boo) {
            InstanceManager.getDefault(UserPreferencesManager.class).setMultipleChoiceOption(getClassName(), "deleteInUse", boo);
        }

        public void doDelete() {
            _treePane._femaleRootSocket.unregisterListeners();
            try {
                _currentFemaleSocket.disconnect();
                _maleSocket.getManager().deleteBean(_maleSocket, "DoDelete");
            } catch (PropertyVetoException e) {
                //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
                log.error("Unexpected doDelete failure for {}, {}", _maleSocket, e.getMessage() );
            } finally {
                if (_treePane._femaleRootSocket.isActive()) {
                    _treePane._femaleRootSocket.registerListeners();
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Void doInBackground() {
            StringBuilder message = new StringBuilder();
            try {
                _maleSocket.getManager().deleteBean(_maleSocket, "CanDelete");  // NOI18N
            } catch (PropertyVetoException e) {
                if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { // NOI18N
                    log.warn("Do not Delete {}, {}", _maleSocket, e.getMessage());
                    message.append(Bundle.getMessage(
                            "VetoDeleteBean",
                            ((NamedBean)_maleSocket.getObject()).getBeanType(),
                            ((NamedBean)_maleSocket.getObject()).getDisplayName(
                                    NamedBean.DisplayOptions.USERNAME_SYSTEMNAME),
                            e.getMessage()));
                    JmriJOptionPane.showMessageDialog(null, message.toString(),
                            Bundle.getMessage("WarningTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return null;
                }
                message.append(e.getMessage());
            }
            List<String> listenerRefs = new ArrayList<>();
            _maleSocket.getListenerRefsIncludingChildren(listenerRefs);
            int count = listenerRefs.size();
            log.debug("Delete with {}", count);
            if (getDisplayDeleteMsg() == 0x02 && message.toString().isEmpty()) {
                doDelete();
            } else {
                final JDialog dialog = new JDialog();
                dialog.setTitle(Bundle.getMessage("WarningTitle"));
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                if (count > 0) { // warn of listeners attached before delete

                    String prompt = _maleSocket.getChildCount() > 0 ? "DeleteWithChildrenPrompt" : "DeletePrompt";
                    JLabel question = new JLabel(Bundle.getMessage(
                            prompt,
                            ((NamedBean)_maleSocket.getObject())
                                    .getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME)));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);

                    ArrayList<String> tempListenerRefs = new ArrayList<>();

                    tempListenerRefs.addAll(listenerRefs);

                    if (tempListenerRefs.size() > 0) {
                        ArrayList<String> listeners = new ArrayList<>();
                        for (int i = 0; i < tempListenerRefs.size(); i++) {
                            if (!listeners.contains(tempListenerRefs.get(i))) {
                                listeners.add(tempListenerRefs.get(i));
                            }
                        }

                        message.append("<br>");
                        message.append(Bundle.getMessage("ReminderInUse", count));
                        message.append("<ul>");
                        for (int i = 0; i < listeners.size(); i++) {
                            message.append("<li>");
                            message.append(listeners.get(i));
                            message.append("</li>");
                        }
                        message.append("</ul>");

                        JEditorPane pane = new JEditorPane();
                        pane.setContentType("text/html");
                        pane.setText("<html>" + message.toString() + "</html>");
                        pane.setEditable(false);
                        JScrollPane jScrollPane = new JScrollPane(pane);
                        container.add(jScrollPane);
                    }
                } else {
                    String prompt = _maleSocket.getChildCount() > 0 ? "DeleteWithChildrenPrompt" : "DeletePrompt";
                    String msg = MessageFormat.format(Bundle.getMessage(prompt),
                            new Object[]{_maleSocket.getSystemName()});
                    JLabel question = new JLabel(msg);
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                }

                final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
                remember.setFont(remember.getFont().deriveFont(10f));
                remember.setAlignmentX(Component.CENTER_ALIGNMENT);

                JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));
                JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));
                JPanel button = new JPanel();
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.add(yesButton);
                button.add(noButton);
                container.add(button);

                noButton.addActionListener((ActionEvent e) -> {
                    //there is no point in remembering this the user will never be
                    //able to delete a bean!
                    dialog.dispose();
                });

                yesButton.addActionListener((ActionEvent e) -> {
                    if (remember.isSelected()) {
                        setDisplayDeleteMsg(0x02);
                    }
                    doDelete();
                    dialog.dispose();
                });
                container.add(remember);
                container.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.setAlignmentY(Component.CENTER_ALIGNMENT);
                dialog.getContentPane().add(container);
                dialog.pack();
                dialog.setLocation(
                        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - dialog.getWidth() / 2,
                        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - dialog.getHeight() / 2);
                dialog.setModal(true);
                dialog.setVisible(true);
            }
            return null;
        }

        /**
         * {@inheritDoc} Minimal implementation to catch and log errors
         */
        @Override
        protected void done() {
            try {
                get();  // called to get errors
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                log.error("Exception while deleting bean", e);
            }
            _treePane.updateTree(_currentFemaleSocket, _currentPath.getPath());
        }
    }



    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreeEditor.class);

}
