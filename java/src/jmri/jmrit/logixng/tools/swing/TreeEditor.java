package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;

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
import jmri.util.ThreadingUtil;
import jmri.util.swing.JComboBoxUtil;

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
    
    
    private final LogixNGPreferences _prefs = InstanceManager.getDefault(LogixNGPreferences.class);
    
    ClipboardEditor _clipboardEditor = null;
    
    private JDialog _renameSocketDialog = null;
    private JDialog _selectItemTypeDialog = null;
    private JDialog _addItemDialog = null;
    private JDialog _editActionExpressionDialog = null;
    private JDialog _editLocalVariablesDialog = null;
    private JDialog _changeUsernameDialog = null;
    private final JTextField _socketNameTextField = new JTextField(20);
    private final JTextField _systemName = new JTextField(20);
    private final JTextField _addUserName = new JTextField(20);
    private final JTextField _usernameField = new JTextField(50);
    
    protected boolean _showReminder = false;
    
    private final Comparator<SwingConfiguratorInterface> _swingConfiguratorComboBoxComparator
            = (SwingConfiguratorInterface o1, SwingConfiguratorInterface o2) -> o1.toString().compareTo(o2.toString());
    
    private final SortedComboBoxModel<SwingConfiguratorInterface> _swingConfiguratorComboBoxModel
            = new SortedComboBoxModel<>(_swingConfiguratorComboBoxComparator);
    
    private final JComboBox<Category> _categoryComboBox = new JComboBox<>();
    private final JComboBox<SwingConfiguratorInterface> _swingConfiguratorComboBox = new JComboBox<>(_swingConfiguratorComboBoxModel);
    private final JLabel _renameSocketLabel = new JLabel(Bundle.getMessage("SocketName") + ":");  // NOI18N
    private final JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));   // NOI18N
    private final JLabel _sysNameLabel = new JLabel(Bundle.getMessage("SystemName") + ":");  // NOI18N
    private final JLabel _userNameLabel = new JLabel(Bundle.getMessage("UserName") + ":");   // NOI18N
    private final String _systemNameAuto = this.getClass().getName() + ".AutoSystemName";             // NOI18N
    private final JLabel _categoryLabel = new JLabel(Bundle.getMessage("Category") + ":");  // NOI18N
    private final JLabel _typeLabel = new JLabel(Bundle.getMessage("Type") + ":");   // NOI18N
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
        
        
        PopupMenu popup = new PopupMenu();
        popup.init();
/*        
        // The JTree can get big, so allow it to scroll
        JScrollPane scrollpane = new JScrollPane(tree);

        // create panel
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));
        
        // Display it all in a window and make the window appear
        pPanel.add(scrollpane, "Center");

        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(pPanel);
        
//        initMinimumSize(new Dimension(panelWidth700, panelHeight500));
*/        
    }

    final public void openClipboard() {
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
     * Respond to the Add menu choice in the popup menu.
     *
     * @param femaleSocket the female socket
     * @param path the path to the item the user has clicked on
     */
    final protected void renameSocketPressed(FemaleSocket femaleSocket, TreePath path) {
        _renameSocketDialog  = new JDialog(
                this,
                Bundle.getMessage(
                        "RenameSocketDialogTitle",
                        femaleSocket.getLongDescription()),
                true);
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
            } else {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("ValidateFemaleSocketMessage", _socketNameTextField.getText()),
                        Bundle.getMessage("ValidateFemaleSocketTitle"),
                        JOptionPane.ERROR_MESSAGE);
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
     * @param path the path to the item the user has clicked on
     */
    final protected void addPressed(FemaleSocket femaleSocket, TreePath path) {
        
        Map<Category, List<Class<? extends Base>>> connectableClasses =
                femaleSocket.getConnectableClasses();
        
        _categoryComboBox.removeAllItems();
        List<Category> list = new ArrayList<>(connectableClasses.keySet());
        Collections.sort(list);
        for (Category item : list) {
            _categoryComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_categoryComboBox);
        
        for (ItemListener l : _categoryComboBox.getItemListeners()) {
            _categoryComboBox.removeItemListener(l);
        }
        
        _categoryComboBox.addItemListener((ItemEvent e) -> {
            Category category = _categoryComboBox.getItemAt(_categoryComboBox.getSelectedIndex());
            _swingConfiguratorComboBox.removeAllItems();
            List<Class<? extends Base>> classes = connectableClasses.get(category);
            if (classes != null) {
                for (Class<? extends Base> clazz : classes) {
                    SwingConfiguratorInterface sci = SwingTools.getSwingConfiguratorForClass(clazz);
                    if (sci != null) {
                        _swingConfiguratorComboBox.addItem(sci);
                    } else {
                        log.error("Class {} has no swing configurator interface", clazz.getName());
                    }
                }
                JComboBoxUtil.setupComboBoxMaxRows(_swingConfiguratorComboBox);
            }
        });
        
        // Ensure the type combo box gets updated
        _categoryComboBox.setSelectedIndex(-1);
        if (_categoryComboBox.getItemCount() > 0) {
            _categoryComboBox.setSelectedIndex(0);
        }
        
        
        _selectItemTypeDialog  = new JDialog(
                this,
                Bundle.getMessage(
                        "AddMaleSocketDialogTitle",
                        femaleSocket.getLongDescription()),
                true);
//        selectItemTypeFrame.addHelpMenu(
//                "package.jmri.jmrit.logixng.tools.swing.ConditionalNGAddEdit", true);     // NOI18N
        _selectItemTypeDialog.setLocation(50, 30);
        Container contentPanel = _selectItemTypeDialog.getContentPane();
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
        p.add(_categoryLabel, c);
        c.gridy = 1;
        p.add(_typeLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_categoryComboBox, c);
        c.gridy = 1;
        p.add(_swingConfiguratorComboBox, c);
        
        _categoryComboBox.setToolTipText(Bundle.getMessage("CategoryNamesHint"));    // NOI18N
        _swingConfiguratorComboBox.setToolTipText(Bundle.getMessage("TypeNamesHint"));   // NOI18N
        contentPanel.add(p);
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        
        contentPanel.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener((ActionEvent e) -> {
            cancelAddPressed(null);
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N

        _selectItemTypeDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelAddPressed(null);
            }
        });

        _create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
        panel5.add(_create);
        _create.addActionListener((ActionEvent e) -> {
            cancelAddPressed(null);
            
            SwingConfiguratorInterface swingConfiguratorInterface =
                    _swingConfiguratorComboBox.getItemAt(_swingConfiguratorComboBox.getSelectedIndex());
//            System.err.format("swingConfiguratorInterface: %s%n", swingConfiguratorInterface.getClass().getName());
            createAddFrame(femaleSocket, path, swingConfiguratorInterface);
        });
        
        contentPanel.add(panel5);

        _autoSystemName.addItemListener((ItemEvent e) -> {
            autoSystemName();
        });
//        addLogixNGFrame.setLocationRelativeTo(component);
        _selectItemTypeDialog.setLocationRelativeTo(null);
        _selectItemTypeDialog.pack();
        _selectItemTypeDialog.setVisible(true);
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
                    } else {
                        StringBuilder errorMsg = new StringBuilder();
                        for (String s : errorMessages) {
                            if (errorMsg.length() > 0) errorMsg.append("<br>");
                            errorMsg.append(s);
                        }
                        JOptionPane.showMessageDialog(null,
                                Bundle.getMessage("ValidateErrorMessage", errorMsg),
                                Bundle.getMessage("ValidateErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
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
     * Respond to the Edit menu choice in the popup menu.
     *
     * @param femaleSocket the female socket
     * @param path the path to the item the user has clicked on
     */
    final protected void editPressed(FemaleSocket femaleSocket, TreePath path) {
        // possible change
        _showReminder = true;
        // make an Edit Frame
        if (_editActionExpressionDialog == null) {
            MutableObject<String> commentStr = new MutableObject<>();
            
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
                    
                    if (isValid) {
                        ThreadingUtil.runOnGUIEventually(() -> {
                            femaleSocket.unregisterListeners();
                            
                            Base object = femaleSocket.getConnectedSocket().getObject();
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
                    } else {
                        StringBuilder errorMsg = new StringBuilder();
                        for (String s : errorMessages) {
                            if (errorMsg.length() > 0) errorMsg.append("<br>");
                            errorMsg.append(s);
                        }
                        ThreadingUtil.runOnGUIEventually(() -> {
                            JOptionPane.showMessageDialog(null,
                                    Bundle.getMessage("ValidateErrorMessage", errorMsg),
                                    Bundle.getMessage("ValidateErrorTitle"),
                                    JOptionPane.ERROR_MESSAGE);
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
        
        JDialog frame  = new JDialog(
                this,
                Bundle.getMessage(
                        addOrEdit ? "AddMaleSocketDialogTitle" : "EditMaleSocketDialogTitle",
                        femaleSocket.getLongDescription()),
                true);
//        frame.addHelpMenu(
//                "package.jmri.jmrit.logixng.tools.swing.ConditionalNGAddEdit", true);     // NOI18N
        Container contentPanel = frame.getContentPane();
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
                panels.add(_editSwingConfiguratorInterface.getConfigPanel(object, panel5));
                _swingConfiguratorInterfaceList.add(new HashMap.SimpleEntry<>(_editSwingConfiguratorInterface, object));
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
            panels.add(_addSwingConfiguratorInterfaceMaleSocket.getConfigPanel(panel5));
            
            panels.add(_addSwingConfiguratorInterface.getConfigPanel(panel5));
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

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
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
        frame.pack();
        frame.setLocationRelativeTo(null);
        
        if (addOrEdit) {
            _addItemDialog = frame;
        } else {
            _editActionExpressionDialog = frame;
        }
        
        _autoSystemName.setSelected(true);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            _autoSystemName.setSelected(prefMgr.getCheckboxPreferenceState(_systemNameAuto, true));
        });
        
        frame.setVisible(true);
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
                    JOptionPane.showMessageDialog(null,
                            Bundle.getMessage("ValidateErrorMessage", errorMsg),
                            Bundle.getMessage("ValidateErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    
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
                    true);
    //        frame.addHelpMenu(
    //                "package.jmri.jmrit.logixng.tools.swing.ConditionalNGAddEdit", true);     // NOI18N
            Container contentPanel = _editLocalVariablesDialog.getContentPane();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            
            JPanel tablePanel = new JPanel();
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
            tablePanel.add(scrollpane, BorderLayout.CENTER);
            contentPanel.add(tablePanel);
            
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
            });
    //        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
            cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N
            
            buttonPanel.add(_edit);
            
            _editLocalVariablesDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _editLocalVariablesDialog.setVisible(false);
                    _editLocalVariablesDialog.dispose();
                    _editLocalVariablesDialog = null;
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
        // make an Edit Frame
        if (_changeUsernameDialog == null) {
            MaleSocket maleSocket = femaleSocket.getConnectedSocket();
            
            // Edit ConditionalNG
            _edit = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
            _edit.addActionListener((ActionEvent e) -> {
                
                boolean hasErrors = false;
                if (hasErrors) {
                    String errorMsg = "";
                    JOptionPane.showMessageDialog(null,
                            Bundle.getMessage("ValidateErrorMessage", errorMsg),
                            Bundle.getMessage("ValidateErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    
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
                                        JOptionPane.showMessageDialog(null, msg,
                                                Bundle.getMessage("WarningTitle"),
                                                JOptionPane.ERROR_MESSAGE);
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
                                int optionPane = JOptionPane.showConfirmDialog(null,
                                        msg, Bundle.getMessage("UpdateToUserNameTitle"),
                                        JOptionPane.YES_NO_OPTION);
                                if (optionPane == JOptionPane.YES_OPTION) {
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
                    true);
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
            });
    //        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
            cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N
            
            buttonPanel.add(_edit);
            
            _changeUsernameDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _changeUsernameDialog.setVisible(false);
                    _changeUsernameDialog.dispose();
                    _changeUsernameDialog = null;
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
        this.setVisible(true);
    }
    
    /**
     * Respond to the Cancel button in Add ConditionalNG window.
     * <p>
     * Note: Also get there if the user closes the Add ConditionalNG window.
     *
     * @param e The event heard
     */
    final protected void cancelAddPressed(ActionEvent e) {
        _selectItemTypeDialog.setVisible(false);
        _selectItemTypeDialog.dispose();
        _selectItemTypeDialog = null;
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
    final protected void cancelCreateItem(ActionEvent e) {
        _addItemDialog.setVisible(false);
        _addSwingConfiguratorInterface.dispose();
        _addItemDialog.dispose();
        _addItemDialog = null;
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
        _editActionExpressionDialog.setVisible(false);
//        _editSwingConfiguratorInterface.dispose();
        for (Map.Entry<SwingConfiguratorInterface, Base> entry : _swingConfiguratorInterfaceList) {
            entry.getKey().dispose();
//            entry.getKey().updateObject(entry.getValue());
//        for (SwingConfiguratorInterface swi : _swingConfiguratorInterfaceList) {
//            swi.dispose();
        }
        _editActionExpressionDialog.dispose();
        _editActionExpressionDialog = null;
//        _inCopyMode = false;
        this.setVisible(true);
    }
    
    
    protected void executeEvaluate(SwingConfiguratorInterface swi, MaleSocket maleSocket) {
        swi.executeEvaluate(maleSocket);
    }
    
    
    
    private static final class SortedComboBoxModel<E> extends DefaultComboBoxModel<E> {

        private final Comparator<E> comparator;

        /*
         *  Create an empty model that will use the specified Comparator
         */
        public SortedComboBoxModel(@Nonnull Comparator<E> comparator) {
            super();
            this.comparator = comparator;
        }

        @Override
        public void addElement(E element) {
            insertElementAt(element, 0);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void insertElementAt(E element, int index) {
            int size = getSize();

            //  Determine where to insert element to keep model in sorted order
            int i = 0;
            for (; i < size; i++) {
                E o = getElementAt(i);

                if (comparator.compare(o, element) > 0) {
                    break;
                }
            }

            super.insertElementAt(element, i);

            //  Select an element when it is added to the beginning of the model
            if (i == 0 && element != null) {
                setSelectedItem(element);
            }
        }
    }
    
    
    protected class PopupMenu extends JPopupMenu implements ActionListener {
        
        private static final String ACTION_COMMAND_RENAME_SOCKET = "rename_socket";
        private static final String ACTION_COMMAND_ADD = "add";
        private static final String ACTION_COMMAND_REMOVE = "remove";
        private static final String ACTION_COMMAND_EDIT = "edit";
        private static final String ACTION_COMMAND_CUT = "cut";
        private static final String ACTION_COMMAND_COPY = "copy";
        private static final String ACTION_COMMAND_PASTE = "paste";
        private static final String ACTION_COMMAND_ENABLE = "enable";
        private static final String ACTION_COMMAND_DISABLE = "disable";
        private static final String ACTION_COMMAND_LOCK = "lock";
        private static final String ACTION_COMMAND_UNLOCK = "unlock";
        private static final String ACTION_COMMAND_LOCAL_VARIABLES = "local_variables";
        private static final String ACTION_COMMAND_CHANGE_USERNAME = "change_username";
        private static final String ACTION_COMMAND_EXECUTE_EVALUATE = "execute_evaluate";
//        private static final String ACTION_COMMAND_EXPAND_TREE = "expandTree";
        
        private final JTree _tree;
//        private final FemaleSocketTreeModel _model;
        private FemaleSocket _currentFemaleSocket;
        private TreePath _currentPath;
        
        private JMenuItem menuItemRenameSocket;
        private JMenuItem menuItemAdd;
        private JMenuItem menuItemRemove;
        private JMenuItem menuItemEdit;
        private JMenuItem menuItemCut;
        private JMenuItem menuItemCopy;
        private JMenuItem menuItemPaste;
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
        
        PopupMenu() {
            if (_treePane._tree == null) throw new IllegalArgumentException("_tree is null");
            _tree = _treePane._tree;
        }
        
        private void init() {
            menuItemRenameSocket = new JMenuItem(Bundle.getMessage("PopupMenuRenameSocket"));
            menuItemRenameSocket.addActionListener(this);
            menuItemRenameSocket.setActionCommand(ACTION_COMMAND_RENAME_SOCKET);
            add(menuItemRenameSocket);
            addSeparator();
            menuItemAdd = new JMenuItem(Bundle.getMessage("PopupMenuAdd"));
            menuItemAdd.addActionListener(this);
            menuItemAdd.setActionCommand(ACTION_COMMAND_ADD);
            add(menuItemAdd);
            addSeparator();
            menuItemEdit = new JMenuItem(Bundle.getMessage("PopupMenuEdit"));
            menuItemEdit.addActionListener(this);
            menuItemEdit.setActionCommand(ACTION_COMMAND_EDIT);
            add(menuItemEdit);
            menuItemRemove = new JMenuItem(Bundle.getMessage("PopupMenuRemove"));
            menuItemRemove.addActionListener(this);
            menuItemRemove.setActionCommand(ACTION_COMMAND_REMOVE);
            add(menuItemRemove);
            addSeparator();
            menuItemCut = new JMenuItem(Bundle.getMessage("PopupMenuCut"));
            menuItemCut.addActionListener(this);
            menuItemCut.setActionCommand(ACTION_COMMAND_CUT);
            add(menuItemCut);
            menuItemCopy = new JMenuItem(Bundle.getMessage("PopupMenuCopy"));
            menuItemCopy.addActionListener(this);
            menuItemCopy.setActionCommand(ACTION_COMMAND_COPY);
            add(menuItemCopy);
            menuItemPaste = new JMenuItem(Bundle.getMessage("PopupMenuPaste"));
            menuItemPaste.addActionListener(this);
            menuItemPaste.setActionCommand(ACTION_COMMAND_PASTE);
            add(menuItemPaste);
            addSeparator();
            
            for (FemaleSocketOperation oper : FemaleSocketOperation.values()) {
                JMenuItem menuItem = new JMenuItem(oper.toString());
                menuItem.addActionListener(this);
                menuItem.setActionCommand(oper.name());
                add(menuItem);
                menuItemFemaleSocketOperation.put(oper, menuItem);
            }
            
            addSeparator();
            menuItemEnable = new JMenuItem(Bundle.getMessage("PopupMenuEnable"));
            menuItemEnable.addActionListener(this);
            menuItemEnable.setActionCommand(ACTION_COMMAND_ENABLE);
            add(menuItemEnable);
            menuItemDisable = new JMenuItem(Bundle.getMessage("PopupMenuDisable"));
            menuItemDisable.addActionListener(this);
            menuItemDisable.setActionCommand(ACTION_COMMAND_DISABLE);
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
            
            final PopupMenu popupMenu = this;
            
            _tree.addMouseListener(
                    new MouseAdapter() {
                        
                        // On Windows, the popup is opened on mousePressed,
                        // on some other OS, the popup is opened on mouseReleased
                        
                        @Override
                        public void mousePressed(MouseEvent e) {
                            openPopupMenu(e);
                        }
                        
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            openPopupMenu(e);
                        }
                        
                        private void openPopupMenu(MouseEvent e) {
                            if (e.isPopupTrigger() && !popupMenu.isVisible()) {
                                // Get the row the user has clicked on
                                TreePath path = _tree.getClosestPathForLocation(e.getX(), e.getY());
                                if (path != null) {
                                    // Check that the user has clicked on a row.
                                    Rectangle rect = _tree.getPathBounds(path);
                                    if ((e.getY() >= rect.y) && (e.getY() <= rect.y + rect.height)) {
                                        FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                                        showPopup(e.getX(), e.getY(), femaleSocket, path);
                                    }
                                }
                            }
                        }
                    }
            );
        }
        
        private void showPopup(int x, int y, FemaleSocket femaleSocket, TreePath path) {
            _currentFemaleSocket = femaleSocket;
            _currentPath = path;
            
            Clipboard clipboard = InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();
            
            MaleSocket topItem = clipboard.getTopItem();
            
            boolean isConnected = femaleSocket.isConnected();
            boolean canConnectFromClipboard =
                    topItem != null
                    && femaleSocket.isCompatible(topItem)
                    && !femaleSocket.isAncestor(topItem);
            
            if (_disableRootPopup
                    && (_currentFemaleSocket == _treePane._femaleRootSocket)) {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("TreeEditor_RootHasNoPopupMenu"),
                        Bundle.getMessage("TreeEditor_Info"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean disableForRoot = _disableRootRemoveCutCopy
                    && (_currentFemaleSocket == _treePane._femaleRootSocket);
            
            menuItemAdd.setEnabled(!isConnected);
            menuItemRemove.setEnabled(isConnected && !disableForRoot);
            menuItemEdit.setEnabled(isConnected);
            menuItemCut.setEnabled(isConnected && !disableForRoot);
            menuItemCopy.setEnabled(isConnected && !disableForRoot);
            menuItemPaste.setEnabled(!isConnected && canConnectFromClipboard);
            
            if (isConnected && !disableForRoot) {
                menuItemEnable.setEnabled(!femaleSocket.getConnectedSocket().isEnabled());
                menuItemDisable.setEnabled(femaleSocket.getConnectedSocket().isEnabled());
            } else {
                menuItemEnable.setEnabled(false);
                menuItemDisable.setEnabled(false);
            }
            
            for (FemaleSocketOperation oper : FemaleSocketOperation.values()) {
                JMenuItem menuItem = menuItemFemaleSocketOperation.get(oper);
                menuItem.setEnabled(femaleSocket.isSocketOperationAllowed(oper));
            }
            
            if (femaleSocket.isConnected()) {
                MaleSocket connectedSocket = femaleSocket.getConnectedSocket();
                menuItemLock.setEnabled(!connectedSocket.isLocked());
                menuItemUnlock.setEnabled(connectedSocket.isLocked());
            } else {
                menuItemLock.setEnabled(false);
                menuItemUnlock.setEnabled(false);
            }
            menuItemLock.setEnabled(false);     // Not implemented yet
            menuItemUnlock.setEnabled(false);   // Not implemented yet
            
            menuItemLocalVariables.setEnabled(femaleSocket.isConnected());
            
            menuItemChangeUsername.setEnabled(femaleSocket.isConnected());
            
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
            
            show(_tree, x, y);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case ACTION_COMMAND_RENAME_SOCKET:
                    renameSocketPressed(_currentFemaleSocket, _currentPath);
                    break;
                    
                case ACTION_COMMAND_ADD:
                    addPressed(_currentFemaleSocket, _currentPath);
                    break;
                    
                case ACTION_COMMAND_EDIT:
                    editPressed(_currentFemaleSocket, _currentPath);
                    break;
                    
                case ACTION_COMMAND_REMOVE:
                    DeleteBeanWorker worker = new DeleteBeanWorker(_currentFemaleSocket, _currentPath);
                    worker.execute();
                    break;
                    
                case ACTION_COMMAND_CUT:
                    if (_currentFemaleSocket.isConnected()) {
                        _treePane._femaleRootSocket.unregisterListeners();
                        
                        runOnConditionalNGThreadOrGUIThreadEventually(
                                _treePane._femaleRootSocket.getConditionalNG(),
                                () -> {
                            Clipboard clipboard =
                                    InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();
                            List<String> errors = new ArrayList<>();
                            if (!clipboard.add(_currentFemaleSocket.getConnectedSocket(), errors)) {
                                JOptionPane.showMessageDialog(this,
                                        String.join("<br>", errors),
                                        Bundle.getMessage("TitleError"),
                                        JOptionPane.ERROR_MESSAGE);
                            }
                            _currentFemaleSocket.disconnect();
                            ThreadingUtil.runOnGUIEventually(() -> {
                                _treePane._femaleRootSocket.registerListeners();
                                _treePane.updateTree(_currentFemaleSocket, _currentPath.getPath());
                            });
                        });
                   } else {
                        log.error("_currentFemaleSocket is not connected");
                    }
                    break;
                    
                case ACTION_COMMAND_COPY:
                    if (_currentFemaleSocket.isConnected()) {
                        _treePane._femaleRootSocket.unregisterListeners();
                        
                        runOnConditionalNGThreadOrGUIThreadEventually(
                                _treePane._femaleRootSocket.getConditionalNG(),
                                () -> {
                            Clipboard clipboard =
                                    InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();
                            Map<String, String> systemNames = new HashMap<>();
                            Map<String, String> userNames = new HashMap<>();
                            try {
                                List<String> errors = new ArrayList<>();
                                if (!clipboard.add(
                                        (MaleSocket) _currentFemaleSocket.getConnectedSocket().getDeepCopy(systemNames, userNames),
                                        errors)) {
                                    JOptionPane.showMessageDialog(this,
                                            String.join("<br>", errors),
                                            Bundle.getMessage("TitleError"),
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (JmriException ex) {
                                log.error("getDeepCopy thrown exception: {}", ex, ex);
                                ThreadingUtil.runOnGUIEventually(() -> {
                                    JOptionPane.showMessageDialog(null,
                                            "An exception has occured: "+ex.getMessage(),
                                            "An error has occured",
                                            JOptionPane.ERROR_MESSAGE);
                                });
                            }
                        });
                        
                        _treePane._femaleRootSocket.registerListeners();
                    } else {
                        log.error("_currentFemaleSocket is not connected");
                    }
                    break;
                    
                case ACTION_COMMAND_PASTE:
                    if (! _currentFemaleSocket.isConnected()) {
                        _treePane._femaleRootSocket.unregisterListeners();
                        
                        runOnConditionalNGThreadOrGUIThreadEventually(
                                _treePane._femaleRootSocket.getConditionalNG(),
                                () -> {
                            Clipboard clipboard =
                                    InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();
                            try {
                                _currentFemaleSocket.connect(clipboard.fetchTopItem());
                                List<String> errors = new ArrayList<>();
                                if (!_currentFemaleSocket.setParentForAllChildren(errors)) {
                                    JOptionPane.showMessageDialog(this,
                                            String.join("<br>", errors),
                                            Bundle.getMessage("TitleError"),
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (SocketAlreadyConnectedException ex) {
                                log.error("item cannot be connected", ex);
                            }
                            ThreadingUtil.runOnGUIEventually(() -> {
                                _treePane._femaleRootSocket.registerListeners();
                                _treePane.updateTree(_currentFemaleSocket, _currentPath.getPath());
                            });
                        });
                    } else {
                        log.error("_currentFemaleSocket is connected");
                    }
                    break;
                    
                case ACTION_COMMAND_ENABLE:
                    _currentFemaleSocket.getConnectedSocket().setEnabled(true);
                    runOnConditionalNGThreadOrGUIThreadEventually(
                            _treePane._femaleRootSocket.getConditionalNG(),
                            () -> {
                        ThreadingUtil.runOnGUIEventually(() -> {
                            _treePane._femaleRootSocket.unregisterListeners();
                            _treePane.updateTree(_currentFemaleSocket, _currentPath.getPath());
                            _treePane._femaleRootSocket.registerListeners();
                        });
                    });
                    break;
                    
                case ACTION_COMMAND_DISABLE:
                    _currentFemaleSocket.getConnectedSocket().setEnabled(false);
                    runOnConditionalNGThreadOrGUIThreadEventually(
                            _treePane._femaleRootSocket.getConditionalNG(),
                            () -> {
                        ThreadingUtil.runOnGUIEventually(() -> {
                            _treePane._femaleRootSocket.unregisterListeners();
                            _treePane.updateTree(_currentFemaleSocket, _currentPath.getPath());
                            _treePane._femaleRootSocket.registerListeners();
                        });
                    });
                    break;
                    
                case ACTION_COMMAND_LOCK:
                    break;
                    
                case ACTION_COMMAND_UNLOCK:
                    break;
                    
                case ACTION_COMMAND_LOCAL_VARIABLES:
                    editLocalVariables(_currentFemaleSocket, _currentPath);
                    break;
                    
                case ACTION_COMMAND_CHANGE_USERNAME:
                    changeUsername(_currentFemaleSocket, _currentPath);
                    break;
                    
                case ACTION_COMMAND_EXECUTE_EVALUATE:
                    Base object = _currentFemaleSocket.getConnectedSocket();
                    if (object == null) throw new NullPointerException("object is null");
                    while (object instanceof MaleSocket) {
                        object = ((MaleSocket)object).getObject();
                    }
                    SwingConfiguratorInterface swi =
                            SwingTools.getSwingConfiguratorForClass(object.getClass());
                    executeEvaluate(swi, _currentFemaleSocket.getConnectedSocket());
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
                    if (! checkFemaleSocketOperation(_currentFemaleSocket, e.getActionCommand())) {
                        log.error("e.getActionCommand() returns unknown value {}", e.getActionCommand());
                    }
            }
        }
        
        private boolean checkFemaleSocketOperation(FemaleSocket femaleSocket, String command) {
            for (FemaleSocketOperation oper : FemaleSocketOperation.values()) {
                if (oper.name().equals(command)) {
                    femaleSocket.doSocketOperation(oper);
                    return true;
                }
            }
            return false;
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
            return InstanceManager.getDefault(UserPreferencesManager.class).getMultipleChoiceOption(TreeEditor.class.getName(), "deleteInUse");
        }
        
        public void setDisplayDeleteMsg(int boo) {
            InstanceManager.getDefault(UserPreferencesManager.class).setMultipleChoiceOption(TreeEditor.class.getName(), "deleteInUse", boo);
        }
        
        private void findAllChilds(FemaleSocket femaleSocket, List<Map.Entry<FemaleSocket, MaleSocket>> sockets) {
            if (!femaleSocket.isConnected()) return;
            MaleSocket maleSocket = femaleSocket.getConnectedSocket();
            sockets.add(new HashMap.SimpleEntry<>(femaleSocket, maleSocket));
            for (int i=0; i < maleSocket.getChildCount(); i++) {
                findAllChilds(maleSocket.getChild(i), sockets);
            }
        }
        
        public void doDelete(List<Map.Entry<FemaleSocket, MaleSocket>> sockets) {
            for (Map.Entry<FemaleSocket, MaleSocket> entry : sockets) {
                try {
                    FemaleSocket femaleSocket = entry.getKey();
                    femaleSocket.disconnect();
                    
                    MaleSocket maleSocket = entry.getValue();
                    maleSocket.getManager().deleteBean(maleSocket, "DoDelete");
                } catch (PropertyVetoException e) {
                    //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
                    log.error(e.getMessage());
                }
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Void doInBackground() {
            _treePane._femaleRootSocket.unregisterListeners();
            
            List<Map.Entry<FemaleSocket, MaleSocket>> sockets = new ArrayList<>();
            
            findAllChilds(_currentFemaleSocket, sockets);
            
            StringBuilder message = new StringBuilder();
            try {
                for (Map.Entry<FemaleSocket, MaleSocket> entry : sockets) {
                    entry.getValue().getManager().deleteBean(_maleSocket, "CanDelete");  // NOI18N
                }
            } catch (PropertyVetoException e) {
                if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { // NOI18N
                    log.warn(e.getMessage());
                    message.append(Bundle.getMessage("VetoDeleteBean", ((NamedBean)_maleSocket.getObject()).getBeanType(), ((NamedBean)_maleSocket.getObject()).getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME), e.getMessage()));
                    JOptionPane.showMessageDialog(null, message.toString(),
                            Bundle.getMessage("WarningTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                message.append(e.getMessage());
            }
            int count = _maleSocket.getListenerRefs().size();
            log.debug("Delete with {}", count);
            if (getDisplayDeleteMsg() == 0x02 && message.toString().isEmpty()) {
                doDelete(sockets);
            } else {
                final JDialog dialog = new JDialog();
                dialog.setTitle(Bundle.getMessage("WarningTitle"));
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                if (count > 0) { // warn of listeners attached before delete
                    
                    String prompt = _maleSocket.getChildCount() > 0 ? "DeleteWithChildrenPrompt" : "DeletePrompt";
                    JLabel question = new JLabel(Bundle.getMessage(prompt, ((NamedBean)_maleSocket.getObject()).getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME)));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                    
                    ArrayList<String> listenerRefs = new ArrayList<>();
                    
                    for (Map.Entry<FemaleSocket, MaleSocket> entry : sockets) {
                        listenerRefs.addAll(entry.getValue().getListenerRefs());
                    }
                    
                    if (listenerRefs.size() > 0) {
                        ArrayList<String> listeners = new ArrayList<>();
                        for (int i = 0; i < listenerRefs.size(); i++) {
                            if (!listeners.contains(listenerRefs.get(i))) {
                                listeners.add(listenerRefs.get(i));
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
                    doDelete(sockets);
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
            if (_treePane._femaleRootSocket.isActive()) {
                _treePane._femaleRootSocket.registerListeners();
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
