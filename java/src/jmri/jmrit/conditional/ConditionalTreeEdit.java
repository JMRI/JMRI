package jmri.jmrit.conditional;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.Audio;
import jmri.Conditional;
import jmri.Conditional.Operator;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Logix;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.implementation.DefaultConditional;
import jmri.implementation.DefaultConditionalAction;
import jmri.jmrit.beantable.LRouteTableAction;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;

/**
 * A tree based editor for maintaining Logix Conditionals, State Variables and
 * Actions.
 * <p>
 * The tree has 3 levels. The first level are the conditionals contained in the
 * selected Logix. The second level contains the antecedent, logic type and
 * trigger mode settings. The third level contains the detail Variable and
 * Action lines.
 * <p>
 * Compare with the other Conditional Edit tool {@link ConditionalListEdit}
 *
 * @author Dave Sand copyright (c) 2017
 */
public class ConditionalTreeEdit extends ConditionalEditBase {

    /**
     * Create a new Conditional Tree View editor.
     *
     * @param sName The system name of the current Logix
     */
    public ConditionalTreeEdit(String sName) {
        super(sName);
        buildConditionalComponents();
        buildActionComponents();
        buildVariableComponents();
        makeEditLogixWindow();
        setFocusListeners();
        setEditMode(false);
    }

    public ConditionalTreeEdit() {
    }

    JmriJFrame _editLogixFrame = null;
    JPanel _curDetailPanel = new JPanel();
    JTextField _editLogixUserName = new JTextField(20);   // Logix User Name field

    // ------------ Edit detail components ------------
    JPanel _detailGrid = new JPanel();
    JPanel _detailFooter = new JPanel();
    JPanel _gridPanel;  // Child of _detailGrid, contains the current grid labels and fields
    JTextField _editConditionalUserName;
    JTextField _editAntecedent;
    JComboBox<Conditional.AntecedentOperator> _editOperatorMode;
    boolean _editActive = false;
    JButton _cancelAction;
    JButton _updateAction;
    String _pickCommand = null;
    Conditional.ItemType _pickItem = Conditional.ItemType.NONE;

    // ------------ Tree variables ------------
    JTree _cdlTree;
    DefaultTreeModel _cdlModel;
    DefaultMutableTreeNode _cdlRoot;
    TreeSelectionListener _cdlListener;
    TreePath _curTreePath = null;

    // ------------ Tree components ------------
    ConditionalTreeNode _cdlNode = null;
    ConditionalTreeNode _varHead = null;
    ConditionalTreeNode _varNode = null;
    ConditionalTreeNode _actHead = null;
    ConditionalTreeNode _actNode = null;
    ConditionalTreeNode _leafNode = null;

    // ------------ Current tree node variables ------------
    ConditionalTreeNode _curNode = null;
    String _curNodeName = null;
    String _curNodeType = null;
    String _curNodeText = null;
    int _curNodeRow = -1;

    // ------------ Button bar components ------------
    JPanel _leftButtonBar;
    JPanel _labelPanel;
    JPanel _addButtonPanel;
    JPanel _toggleButtonPanel;
    JPanel _checkButtonPanel;
    JPanel _moveButtonPanel;
    JPanel _deleteButtonPanel;
    JPanel _helpButtonPanel;

    JLabel _conditionalLabel = new JLabel(Bundle.getMessage("LabelConditionalActions"));  // NOI18N
    JLabel _antecedentLabel = new JLabel(Bundle.getMessage("LabelAntecedentActions"));  // NOI18N
    JLabel _logicTypeLabel = new JLabel(Bundle.getMessage("LabelLogicTypeActions"));  // NOI18N
    JLabel _triggerModeLabel = new JLabel(Bundle.getMessage("LabelTriggerModeActions"));  // NOI18N
    JLabel _variablesLabel = new JLabel(Bundle.getMessage("LabelVariablesActions"));  // NOI18N
    JLabel _variableLabel = new JLabel(Bundle.getMessage("LabelVariableActions"));  // NOI18N
    JLabel _actionsLabel = new JLabel(Bundle.getMessage("LabelActionsActions"));  // NOI18N
    JLabel _actionLabel = new JLabel(Bundle.getMessage("LabelActionActions"));  // NOI18N

    // ------------ Current conditional components ------------
    Conditional _curConditional;
    List<ConditionalVariable> _variableList;   // Current Variable List
    List<ConditionalAction> _actionList;       // Current Action List
    ConditionalVariable _curVariable;               // Current Variable
    ConditionalAction _curAction;                   // Current Action
    Conditional.ItemType _curVariableItem = Conditional.ItemType.NONE;
    Conditional.ItemType _curActionItem = Conditional.ItemType.NONE;
    String _curConditionalName = "";
    String _antecedent;
    Conditional.AntecedentOperator _logicType;
    boolean _triggerMode;
    boolean _newActionItem = false;
    boolean _newVariableItem = false;
    TreeSet<String> _oldTargetNames = new TreeSet<>();

    // ------------ Select Conditional Variables ------------
    JComboBox<String> _selectLogixBox = new JComboBox<>();
    JComboBox<String> _selectConditionalBox = new JComboBox<>();
    TreeMap<String, String> _selectLogixMap = new TreeMap<>();
    ArrayList<String> _selectConditionalList = new ArrayList<>();

    // ------------ Components of Edit Variable pane ------------
    JComboBox<Conditional.ItemType> _variableItemBox;
    JComboBox<Conditional.Type> _variableStateBox;
    JComboBox<String> _variableOperBox;
    JCheckBox _variableNegated;
    JCheckBox _variableTriggerActions;
    JTextField _variableNameField;
    JLabel _variableNameLabel = new JLabel(Bundle.getMessage("LabelItemName"));  // NOI18N
    JComboBox<String> _variableCompareOpBox;
    JComboBox<String> _variableSignalBox;
    JComboBox<Conditional.Type> _variableCompareTypeBox;
    JLabel _variableMemoryValueLabel = new JLabel("");
    JTextField _variableData1Field;
    JTextField _variableData2Field;

    // ------------ Components of Edit Action pane ------------
    JComboBox<Conditional.ItemType> _actionItemBox;
    JComboBox<Conditional.Action> _actionTypeBox;
    JLabel _actionTypeLabel = new JLabel("Type");  // NOI18N
    JTextField _actionNameField;
    JLabel _actionNameLabel = new JLabel("Name");  // NOI18N
    JComboBox<String> _actionBox;
    JLabel _actionBoxLabel = new JLabel("Box");  // NOI18N
    JTextField _longActionString;
    JLabel _longActionLabel = new JLabel("Long");  // NOI18N
    JTextField _shortActionString;
    JLabel _shortActionLabel = new JLabel("Short");  // NOI18N
    JComboBox<String> _actionOptionBox;
    JButton _actionSetButton;

    // ============  Edit conditionals for the current Logix ============

    /**
     * Create the edit logix window.
     * <p>
     * The left side contains a tree structure containing the conditionals for
     * the current Logix. The right side contains detail edit panes based on the
     * current tree row selection.
     */
    void makeEditLogixWindow() {
        _editLogixFrame = new JmriJFrame(Bundle.getMessage("TitleEditLogix"));  // NOI18N
        _editLogixFrame.addHelpMenu(
                "package.jmri.jmrit.conditional.ConditionalTreeEditor", true);  // NOI18N
        Container contentPane = _editLogixFrame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        // ------------ Header ------------
        JPanel header = new JPanel();
        JPanel logixNames = new JPanel();
        logixNames.setLayout(new BoxLayout(logixNames, BoxLayout.X_AXIS));

        JLabel systemNameLabel = new JLabel(Bundle.getMessage("ColumnSystemName") + ":");  // NOI18N
        logixNames.add(systemNameLabel);
        logixNames.add(Box.createHorizontalStrut(5));

        JLabel fixedSystemName = new JLabel(_curLogix.getSystemName());
        logixNames.add(fixedSystemName);
        logixNames.add(Box.createHorizontalStrut(20));

        JLabel userNameLabel = new JLabel(Bundle.getMessage("ColumnUserName") + ":");  // NOI18N
        logixNames.add(userNameLabel);
        logixNames.add(Box.createHorizontalStrut(5));

        _editLogixUserName.setText(_curLogix.getUserName());
        logixNames.add(_editLogixUserName);
        _editLogixUserName.setToolTipText(Bundle.getMessage("LogixUserNameHint2"));  // NOI18N
        _editLogixUserName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String uName = _editLogixUserName.getText().trim();
                if (!(uName.equals(_curLogix.getUserName()))) {
                    // user name has changed - check if already in use
                    if (uName.length() > 0) {
                        Logix p = _logixManager.getByUserName(uName);
                        if (p != null) {
                            // Logix with this user name already exists
                            log.error("Failure to update Logix with Duplicate User Name: {}", uName); // NOI18N
                            JOptionPane.showMessageDialog(_editLogixFrame,
                                    Bundle.getMessage("Error6"), Bundle.getMessage("ErrorTitle"), // NOI18N
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    // user name is unique, change it
                    logixData.clear();
                    logixData.put("chgUname", uName);  // NOI18N
                    fireLogixEvent();
                    _showReminder = true;
                }
            }
        });

        header.add(logixNames);
        contentPane.add(header, BorderLayout.NORTH);

        // ------------ body - tree (left side) ------------
        JTree treeContent = buildConditionalTree();
        JScrollPane treeScroll = new JScrollPane(treeContent);

        // ------------ body - detail (right side) ------------
        JPanel detailPane = new JPanel();
        detailPane.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.DARK_GRAY));
        detailPane.setLayout(new BoxLayout(detailPane, BoxLayout.Y_AXIS));

        // ------------ Edit Detail Panel ------------
        makeDetailGrid("EmptyGrid");  // NOI18N

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        _cancelAction = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
        _cancelAction.setToolTipText(Bundle.getMessage("HintCancelButton"));  // NOI18N
        panel.add(_cancelAction);
        _cancelAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });
        panel.add(Box.createHorizontalStrut(10));

        _updateAction = new JButton(Bundle.getMessage("ButtonUpdate"));  // NOI18N
        _updateAction.setToolTipText(Bundle.getMessage("UpdateButtonHint"));  // NOI18N
        panel.add(_updateAction);
        _updateAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePressed();
            }
        });
        _detailFooter.add(panel);

        JPanel detailEdit = new JPanel(new BorderLayout());
        detailEdit.add(_detailGrid, BorderLayout.NORTH);
        detailEdit.add(_detailFooter, BorderLayout.SOUTH);
        detailPane.add(detailEdit);
        _editLogixUserName.setEnabled(true);

        JSplitPane bodyPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, detailPane);
        bodyPane.setDividerSize(10);
        bodyPane.setResizeWeight(.35);
        bodyPane.setOneTouchExpandable(true);
        contentPane.add(bodyPane);

        // ------------ footer ------------
        JPanel footer = new JPanel(new BorderLayout());
        _labelPanel = new JPanel();
        _labelPanel.add(_conditionalLabel);
        _leftButtonBar = new JPanel();
        _leftButtonBar.add(_labelPanel);

        // ------------ Add Button ------------
        JButton addButton = new JButton(Bundle.getMessage("ButtonAddText")); // NOI18N
        addButton.setToolTipText(Bundle.getMessage("HintAddButton"));        // NOI18N
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPressed();
            }
        });
        _addButtonPanel = new JPanel();
        _addButtonPanel.add(addButton);
        _leftButtonBar.add(_addButtonPanel);

        // ------------ Help Button ------------
        JButton helpButton = new JButton(Bundle.getMessage("ButtonHelp"));  // NOI18N
        helpButton.setToolTipText(Bundle.getMessage("HintHelpButton"));     // NOI18N
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpPressed();
            }
        });
        _helpButtonPanel = new JPanel();
        _helpButtonPanel.add(helpButton);
        _helpButtonPanel.setVisible(false);
        _leftButtonBar.add(_helpButtonPanel);

        // ------------ Toggle Button ------------
        JButton toggleButton = new JButton(Bundle.getMessage("ButtonToggle"));  // NOI18N
        toggleButton.setToolTipText(Bundle.getMessage("HintToggleButton"));     // NOI18N
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePressed();
            }
        });
        _toggleButtonPanel = new JPanel();
        _toggleButtonPanel.add(toggleButton);
        _toggleButtonPanel.setVisible(false);
        _leftButtonBar.add(_toggleButtonPanel);

        // ------------ Check Button ------------
        JButton checkButton = new JButton(Bundle.getMessage("ButtonCheck"));  // NOI18N
        checkButton.setToolTipText(Bundle.getMessage("HintCheckButton"));     // NOI18N
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkPressed();
            }
        });
        _checkButtonPanel = new JPanel();
        _checkButtonPanel.add(checkButton);
        _checkButtonPanel.setVisible(true);
        _leftButtonBar.add(_checkButtonPanel);

        // ------------ Delete Button ------------
        JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete")); // NOI18N
        deleteButton.setToolTipText(Bundle.getMessage("HintDeleteButton"));    // NOI18N
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePressed();
            }
        });
        _deleteButtonPanel = new JPanel();
        _deleteButtonPanel.add(deleteButton);
        _deleteButtonPanel.setVisible(false);
        _leftButtonBar.add(_deleteButtonPanel);

        footer.add(_leftButtonBar, BorderLayout.WEST);
        JPanel rightButtonBar = new JPanel();

        // ------------ Move Buttons ------------
        JLabel moveLabel = new JLabel(Bundle.getMessage("LabelMove"));      // NOI18N

        JButton upButton = new JButton(Bundle.getMessage("ButtonUp"));      // NOI18N
        upButton.setToolTipText(Bundle.getMessage("HintUpButton"));         // NOI18N
        JButton downButton = new JButton(Bundle.getMessage("ButtonDown"));  // NOI18N
        downButton.setToolTipText(Bundle.getMessage("HintDownButton"));     // NOI18N

        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downButton.setEnabled(false);
                upButton.setEnabled(false);
                upPressed();
            }
        });

        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upButton.setEnabled(false);
                downButton.setEnabled(false);
                downPressed();
            }
        });

        _moveButtonPanel = new JPanel();
        _moveButtonPanel.add(moveLabel);
        _moveButtonPanel.add(upButton);
        _moveButtonPanel.add(new JLabel("|"));
        _moveButtonPanel.add(downButton);
        _moveButtonPanel.setVisible(false);
        _leftButtonBar.add(_moveButtonPanel);

        // ------------ Done Button ------------
        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));  // NOI18N
        doneButton.setToolTipText(Bundle.getMessage("HintDoneButton"));     // NOI18N
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                donePressed();
            }
        });
        JPanel doneButtonPanel = new JPanel();
        doneButtonPanel.add(doneButton);
        rightButtonBar.add(doneButtonPanel);

        footer.add(rightButtonBar, BorderLayout.EAST);
        contentPane.add(footer, BorderLayout.SOUTH);

        _editLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                donePressed();
            }
        });
        _editLogixFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        _editLogixFrame.pack();
        _editLogixFrame.setVisible(true);
    }

    /**
     * Initialize conditional components.
     */
    void buildConditionalComponents() {
        _editConditionalUserName = new JTextField(20);
        _editAntecedent = new JTextField(20);
        _editOperatorMode = new JComboBox<>();
        for (Conditional.AntecedentOperator operator : Conditional.AntecedentOperator.values()) {
            _editOperatorMode.addItem(operator);
        }
    }

    // ------------ Create Conditional GridBag panels ------------

    /**
     * Build new GridBag content. The grid panel is hidden, emptied, re-built and
     * made visible.
     *
     * @param gridType The type of grid to create
     */
    void makeDetailGrid(String gridType) {
        _detailGrid.setVisible(false);
        _detailGrid.removeAll();
        _detailFooter.setVisible(true);

        _gridPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.ipadx = 5;

        switch (gridType) {
            case "EmptyGrid":  // NOI18N
                makeEmptyGrid(c);
                _detailFooter.setVisible(false);
                break;

            // ------------ Conditional Edit Grids ------------
            case "Conditional":  // NOI18N
                makeConditionalGrid(c);
                break;

            case "Antecedent":  // NOI18N
                makeAntecedentGrid(c);
                break;

            case "LogicType":  // NOI18N
                makeLogicTypeGrid(c);
                break;

            // ------------ Variable Edit Grids ------------
            case "EmptyVariable":  // NOI18N
                makeEmptyVariableGrid(c);
                break;

            case "StandardVariable":  // NOI18N
                makeStandardVariableGrid(c);
                break;

            case "SignalAspectVariable":  // NOI18N
                makeSignalAspectVariableGrid(c);
                break;

            case "ConditionalVariable":  // NOI18N
                makeConditionalVariableGrid(c);
                break;

            case "MemoryVariable":  // NOI18N
                makeMemoryVariableGrid(c);
                break;

            case "FastClockVariable":  // NOI18N
                makeFastClockVariableGrid(c);
                break;

            // ------------ Action Edit Grids ------------
            case "EmptyAction":  // NOI18N
                makeEmptyActionGrid(c);
                break;

            case "NameTypeAction":  // NOI18N
                makeNameTypeActionGrid(c, false);   // Skip change/trigger row
                break;

            case "NameTypeActionFinal":  // NOI18N
                makeNameTypeActionGrid(c, true);    // Include change/trigger row
                break;

            case "TypeAction":  // NOI18N
                makeTypeActionGrid(c, false);       // Skip change/trigger row
                break;

            case "TypeActionFinal":  // NOI18N
                makeTypeActionGrid(c, true);        // Include change/trigger row
                break;

            case "TypeShortAction":  // NOI18N
                makeTypeShortActionGrid(c);
                break;

            case "StandardAction":  // NOI18N
                makeStandardActionGrid(c, true);    // Include change/trigger row
                break;

            case "ShortFieldAction":  // NOI18N
                makeShortFieldActionGrid(c, true);  // Include Action Box
                break;

            case "ShortFieldNoBoxAction":  // NOI18N
                makeShortFieldActionGrid(c, false); // Skip Action Box
                break;

            case "FileAction":  // NOI18N
                makeFileActionGrid(c);
                break;

            default:
                log.warn("Invalid grid type: '{}'", gridType);  // NOI18N
                makeEmptyGrid(c);
        }

        _detailGrid.add(_gridPanel);
        _detailGrid.setVisible(true);
    }

    /**
     * This grid is used when there are no edit grids required.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeEmptyGrid(GridBagConstraints c) {
        // Variable type box
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        JLabel row0Label = new JLabel("This page is intentionally blank");  // NOI18N
        _gridPanel.add(row0Label, c);
    }

    /**
     * This grid is used to edit the Conditional User Name.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeConditionalGrid(GridBagConstraints c) {
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row0Label = new JLabel(Bundle.getMessage("ConditionalUserName"));  // NOI18N
        row0Label.setToolTipText(Bundle.getMessage("ConditionalUserNameHint"));  // NOI18N
        _gridPanel.add(row0Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_editConditionalUserName, c);
    }

    /**
     * This grid is used to edit the Antecedent when the Logic Type is Mixed.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeAntecedentGrid(GridBagConstraints c) {
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row0Label = new JLabel(Bundle.getMessage("LabelAntecedentHeader"));  // NOI18N
        row0Label.setToolTipText(Bundle.getMessage("LabelAntecedentHint"));  // NOI18N
        _gridPanel.add(row0Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_editAntecedent, c);
    }

    /**
     * This grid is used to edit the Logic Type.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeLogicTypeGrid(GridBagConstraints c) {
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row0Label = new JLabel(Bundle.getMessage("LabelLogicType"));  // NOI18N
        row0Label.setToolTipText(Bundle.getMessage("TypeLogicHint"));  // NOI18N
        _gridPanel.add(row0Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_editOperatorMode, c);
    }

    // ------------ Process button bar and tree events ------------

    /**
     * Add new items: Conditionals, Variables or Actions.
     */
    void addPressed() {
        if (_curNode == null) {
            // New conditional with no prior selection
            _curNodeType = "Conditional";  // NOI18N
        }

        switch (_curNodeType) {
            case "Conditional":     // NOI18N
                // make system name for new conditional
                int num = _curLogix.getNumConditionals() + 1;
                _curConditional = null;
                String cName = null;
                while (_curConditional == null) {
                    cName = _curLogix.getSystemName() + "C" + Integer.toString(num);
                    _curConditional = _conditionalManager.createNewConditional(cName, "");
                    num++;
                    if (num == 1000) {
                        break;
                    }
                }
                if (_curConditional == null) {
                    // should never get here unless there is an assignment conflict
                    log.error("Failure to create Conditional with System Name: {}", cName);  // NOI18N
                    return;
                }
                // add to Logix at the end of the calculate order
                _curLogix.addConditional(cName, -1);
                _actionList = new ArrayList<>();
                _variableList = new ArrayList<>();
                _curConditional.setAction(_actionList);
                _curConditional.setStateVariables(_variableList);
                _showReminder = true;

                // Build tree components
                Conditional curConditional = _curLogix.getConditional(cName);
                _curNode = new ConditionalTreeNode(buildNodeText("Conditional", curConditional, 0), "Conditional", cName, // NOI18N
                        _curLogix.getNumConditionals() - 1);
                _cdlRoot.add(_curNode);
                _leafNode = new ConditionalTreeNode(buildNodeText("Antecedent", curConditional, 0), "Antecedent", cName, 0);   // NOI18N
                _curNode.add(_leafNode);
                _varHead = new ConditionalTreeNode(buildNodeText("Variables", curConditional, 0), "Variables", cName, 0);     // NOI18N
                _curNode.add(_varHead);
                _leafNode = new ConditionalTreeNode(buildNodeText("LogicType", curConditional, 0), "LogicType", cName, 0);      // NOI18N
                _curNode.add(_leafNode);
                _triggerMode = curConditional.getTriggerOnChange();
                _leafNode = new ConditionalTreeNode(buildNodeText("TriggerMode", curConditional, 0), "TriggerMode", cName, 0);      // NOI18N
                _curNode.add(_leafNode);
                _actHead = new ConditionalTreeNode(buildNodeText("Actions", curConditional, 0), "Actions", cName, 0);      // NOI18N
                _curNode.add(_actHead);
                _cdlModel.nodeStructureChanged(_cdlRoot);

                // Switch to new node
                _cdlTree.setSelectionPath(new TreePath(_curNode.getPath()));
                break;

            case "Variables":    // NOI18N
                newVariable();
                break;

            case "Variable":     // NOI18N
                newVariable();
                break;

            case "Actions":    // NOI18N
                newAction();
                break;

            case "Action":     // NOI18N
                newAction();
                break;

            default:
                log.error("Add called for unsupported node type: '{}'", _curNodeType);  // NOI18N
        }
    }

    /**
     * Create a new variable Can be invoked by a Variables or Variable node.
     */
    void newVariable() {
        if (LRouteTableAction.LOGIX_INITIALIZER.equals(_curLogix.getSystemName())) {
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Error49"), Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        cancelPressed();    // Make sure that there are no active edit sessions
        _showReminder = true;
        _curVariableItem = Conditional.ItemType.NONE;
        ConditionalVariable variable = new ConditionalVariable();
        _variableList.add(variable);
        _newVariableItem = true;
        setMoveButtons();   // Buttons will be disabled

        int size = _variableList.size();
        _curVariable = _variableList.get(size - 1);
        // default of operator for postion 0 (row 1) is Conditional.OPERATOR_NONE
        if (size > 1) {
            if (_logicType == Conditional.AntecedentOperator.ALL_OR) {
                _curVariable.setOpern(Conditional.Operator.OR);
            } else {
                _curVariable.setOpern(Conditional.Operator.AND);
            }
        }
        appendToAntecedent();
        size--;

        // Update tree structure
        if (_curNodeType.equals("Variables")) {  // NOI18N
            _varHead = _curNode;
        } else {
            _varHead = (ConditionalTreeNode) _curNode.getParent();
        }
        _leafNode = new ConditionalTreeNode(buildNodeText("Variable", _curVariable, size), "Variable", _curNodeName, size);  // NOI18N
        _varHead.add(_leafNode);
        _cdlModel.nodeStructureChanged(_curNode);
        _varHead.setRow(size + 1);
        _cdlModel.nodeStructureChanged(_varHead);

        // Switch to new node
        ConditionalTreeNode tempNode = (ConditionalTreeNode) _varHead.getLastChild();
        TreePath newPath = new TreePath(tempNode.getPath());
        _cdlTree.setSelectionPath(newPath);
        _cdlTree.expandPath(newPath);
    }

    /**
     * Create a new action Can be invoked by a Actions or Action node.
     */
    void newAction() {
        cancelPressed();    // Make sure that there are no active edit sessions
        _showReminder = true;
        _curActionItem = Conditional.ItemType.NONE;
        ConditionalAction action = new DefaultConditionalAction();
        _actionList.add(action);
        _newActionItem = true;
        setMoveButtons();   // Buttons will be disabled

        int size = _actionList.size();
        _curAction = _actionList.get(size - 1);
        size--;

        // Update tree structure
        if (_curNodeType.equals("Actions")) {  // NOI18N
            _actHead = _curNode;
        } else {
            _actHead = (ConditionalTreeNode) _curNode.getParent();
        }
        _leafNode = new ConditionalTreeNode(buildNodeText("Action", _curAction, size), "Action", _curNodeName, size);  // NOI18N
        _actHead.add(_leafNode);
        _cdlModel.nodeStructureChanged(_curNode);
        _actHead.setRow(size + 1);
        _cdlModel.nodeStructureChanged(_actHead);

        // Switch to new node
        ConditionalTreeNode tempNode = (ConditionalTreeNode) _actHead.getLastChild();
        TreePath newPath = new TreePath(tempNode.getPath());
        _cdlTree.setSelectionPath(newPath);
        _cdlTree.expandPath(newPath);
    }

    /**
     * Set up the edit environment for the selected node Called from
     * {@link #treeRowSelected}. This takes the place of an actual button.
     */
    void editPressed() {
        switch (_curNodeType) {
            case "Conditional":     // NOI18N
                _editConditionalUserName.setText(_curConditional.getUserName());
                makeDetailGrid("Conditional");  // NOI18N
                break;

            case "Antecedent":      // NOI18N
                Conditional.AntecedentOperator chkLogicType = _curConditional.getLogicType();
                if (chkLogicType != Conditional.AntecedentOperator.MIXED) {
                    makeDetailGrid("EmptyGrid");  // NOI18N
                    return;
                }
                _labelPanel.add(_antecedentLabel);
                _helpButtonPanel.setVisible(true);
                _editAntecedent.setText(translateAntecedent(_curConditional.getAntecedentExpression(), false));
                makeDetailGrid("Antecedent");  // NOI18N
                break;

            case "LogicType":       // NOI18N
                Conditional.AntecedentOperator curLogicType = _curConditional.getLogicType();
                _editOperatorMode.setSelectedItem(curLogicType);
                makeDetailGrid("LogicType");  // NOI18N
                break;

            case "Variable":     // NOI18N
                _labelPanel.add(_variableLabel);
                _curVariable = _variableList.get(_curNodeRow);
                _curVariableItem = _curVariable.getType().getItemType();
                initializeStateVariables();
                if (_logicType != Conditional.AntecedentOperator.MIXED) {
                    setMoveButtons();
                }
                _oldTargetNames.clear();
                loadReferenceNames(_variableList, _oldTargetNames);
                break;

            case "Action":     // NOI18N
                _labelPanel.add(_actionLabel);
                _curAction = _actionList.get(_curNodeRow);
                _actionOptionBox.removeAllItems();
                for (int i = 1; i <= Conditional.NUM_ACTION_OPTIONS; i++) {
                    _actionOptionBox.addItem(DefaultConditionalAction.getOptionString(i, _triggerMode));
                }
                _curActionItem = _curAction.getType().getItemType();
                initializeActionVariables();
                setMoveButtons();
                break;

            default:
                log.error("Edit called for unsupported node type: '{}'", _curNodeType);  // NOI18N
        }
    }

    /**
     * Apply the updates to the current node.
     */
    void updatePressed() {
        switch (_curNodeType) {
            case "Conditional":     // NOI18N
                userNameChanged(_editConditionalUserName.getText().trim());
                break;

            case "Antecedent":      // NOI18N
                antecedentChanged(_editAntecedent.getText().trim());
                break;

            case "LogicType":       // NOI18N
                logicTypeChanged(_editOperatorMode.getItemAt(_editOperatorMode.getSelectedIndex()));
                break;

            case "Variable":       // NOI18N
                updateVariable();
                break;

            case "Action":         // NOI18N
                updateAction();
                break;

            default:
                log.warn("Invalid update button press");  // NOI18N
        }
        setEditMode(false);
        _cdlTree.setSelectionPath(_curTreePath);
        _cdlTree.grabFocus();
    }

    /**
     * Change the conditional user name.
     *
     * @param newName The proposed new name
     */
    void userNameChanged(String newName) {
        // Check if the User Name has been changed
        if (!newName.equals(_curConditional.getUserName())) {
            // user name has changed - check if already in use
            if (!checkConditionalUserName(newName, _curLogix)) {
                return;
            }
            // user name is unique or blank, change it
            _curConditional.setUserName(newName);
            _curNode.setText(buildNodeText("Conditional", _curConditional, 0));  // NOI18N
            _cdlModel.nodeChanged(_curNode);

            // Update any conditional references
            ArrayList<String> refList = _conditionalManager.getWhereUsed(_curNodeName);
            if (refList != null) {
                for (String ref : refList) {
                    Conditional cRef = _conditionalManager.getBySystemName(ref);
                    List<ConditionalVariable> varList = cRef.getCopyOfStateVariables();
                    int idx = 0;
                    for (ConditionalVariable var : varList) {
                        // Find the affected conditional variable
                        if (var.getName().equals(_curNodeName)) {
                            if (newName.length() > 0) {
                                var.setGuiName(newName);
                            } else {
                                var.setGuiName(_curNodeName);
                            }

                            // Is the reference (ref) in the same Logix as the target (_curNodeName)
                            // Skip any cross conditional references
                            String varLogixName = _conditionalManager.getParentLogix(ref).getSystemName();
                            String curLogixSName = _curLogix.getSystemName();
                            if (varLogixName.equals(curLogixSName)) {
                                // Yes, update the tree node
                                int cdlCount = _cdlRoot.getChildCount();
                                for (int j = 0; j < cdlCount; j++) {
                                    // See if a conditional node contains a reference
                                    ConditionalTreeNode cdlNode = (ConditionalTreeNode) _cdlRoot.getChildAt(j);
                                    if (cdlNode.getName().equals(ref)) {
                                        // The affected variable node will be down 2 levels
                                        ConditionalTreeNode variables = (ConditionalTreeNode) cdlNode.getChildAt(1);
                                        ConditionalTreeNode variable = (ConditionalTreeNode) variables.getChildAt(idx);
                                        variable.setText(buildNodeText("Variable", var, idx));    // NOI18N
                                        _cdlModel.nodeChanged(variable);

                                    }
                                }
                            }
                        }
                        idx++;
                    }
                    // Commit the changes
                    cRef.setStateVariables(varList);
                    // Refresh the local copy in case cRef was a parallel copy
                    _variableList = _curConditional.getCopyOfStateVariables();
                }
            }
        }
    }

    /**
     * Respond to a change of Logic Type in the dialog window by showing/hiding
     * the _antecedentPanel when Mixed is selected.
     *
     * @param newType The selected logic type
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Except for the root node, all nodes are ConditionalTreeNode")  // NOI18N
    void logicTypeChanged(Conditional.AntecedentOperator newType) {
        if (_logicType == newType) {
            return;
        }

        makeAntecedent();
        Operator oper;
        if (newType != Conditional.AntecedentOperator.MIXED) {
            oper = Conditional.Operator.OR;
            if (newType == Conditional.AntecedentOperator.ALL_AND) {
                oper = Conditional.Operator.AND;
            }

            // Update the variable list and tree node entries
            ConditionalTreeNode varHead = (ConditionalTreeNode) _curNode.getPreviousSibling();
            for (int i = 1; i < _variableList.size(); i++) {
                ConditionalVariable curVar = _variableList.get(i);
                curVar.setOpern(oper);

                ConditionalTreeNode varNode = (ConditionalTreeNode) varHead.getChildAt(i);
                varNode.setText(buildNodeText("Variable", curVar, i));
                _cdlModel.nodeChanged(varNode);
            }
        }

        // update LogicType entry and tree node
        _curConditional.setLogicType(newType, _antecedent); // non-localized string to store Conditional Antecedent
        _logicType = newType;
        _curNode.setText(buildNodeText("LogicType", _curConditional, 0));  // NOI18N
        _cdlModel.nodeChanged(_curNode);

        // update the variables list
        _curConditional.setStateVariables(_variableList);

        // Update antecedent node text
        ConditionalTreeNode parentNode = (ConditionalTreeNode) _curNode.getParent();
        ConditionalTreeNode antNode = (ConditionalTreeNode) parentNode.getFirstChild();
        if (antNode.getType().equals("Antecedent")) {  // NOI18N
            antNode.setText(buildNodeText("Antecedent", _curConditional, 0));  // NOI18N
            _cdlModel.nodeChanged(antNode);
        } else {
            log.warn("Unable to find the antecedent node");  // NOI18N
        }
    }

    /**
     * Update the antecedent.
     *
     * @param antecedentText the new antecedent
     */
    void antecedentChanged(String antecedentText) {
        if (validateAntecedent(antecedentText)) {
            _antecedent = translateAntecedent(antecedentText, true);
            _curConditional.setLogicType(_logicType, _antecedent);
            _curNode.setText(buildNodeText("Antecedent", _curConditional, 0));
            _cdlModel.nodeChanged(_curNode);
        }
    }

    /**
     * Build the antecedent statement.
     */
    void makeAntecedent() {
        _antecedent = makeAntecedent(_variableList);
    }

    /**
     * Add a R# to the antecedent statement.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Except for the root node, all nodes are ConditionalTreeNode")  // NOI18N
    void appendToAntecedent() {
        _antecedent = appendToAntecedent(_logicType, _variableList.size(), _antecedent);
        _curConditional.setLogicType(_logicType, _antecedent);

        // Update antecedent node text
        ConditionalTreeNode antNode;
        if (_curNodeType.equals("Variables")) {  // NOI18N
            antNode = (ConditionalTreeNode) _curNode.getPreviousSibling();
        } else {
            antNode = (ConditionalTreeNode) ((ConditionalTreeNode) _curNode.getParent()).getPreviousSibling();
        }

        if (antNode.getType().equals("Antecedent")) {  // NOI18N
            antNode.setText(buildNodeText("Antecedent", _curConditional, 0));  // localized display text NOI18N
            _cdlModel.nodeChanged(antNode);
        } else {
            log.warn("Unable to find the antecedent node");  // NOI18N
        }
    }

    /**
     * Check the antecedent and logic type.
     * <p>
     * @param antecedentText The user supplied antecedent text
     * @return false if antecedent can't be validated
     */
    boolean validateAntecedent(String antecedentText) {
        return validateAntecedent(_logicType, antecedentText, _variableList, _curConditional);
    }

    /**
     * Update the Actions trigger mode, adjust the Action descriptions.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Except for the root node, all nodes are ConditionalTreeNode")  // NOI18N
    void togglePressed() {
        // Toggle the trigger mode
        _curLogix.deActivateLogix();
        _curConditional.setTriggerOnChange(!_curConditional.getTriggerOnChange());
        _triggerMode = _curConditional.getTriggerOnChange();
        _curLogix.activateLogix();

        // Update node text
        _curNode.setText(buildNodeText("TriggerMode", _curConditional, 0));  // NOI18N
        _cdlModel.nodeChanged(_curNode);

        // refresh the action list to get the updated action descriptions
        _actionList = _curConditional.getCopyOfActions();
        // get next sibling and update the children node text
        ConditionalTreeNode actionsNode = (ConditionalTreeNode) _curNode.getNextSibling();
        for (int i = 0; i < _actionList.size(); i++) {
            ConditionalAction action = _actionList.get(i);
            ConditionalTreeNode actNode = (ConditionalTreeNode) actionsNode.getChildAt(i);
            actNode.setText(action.description(_triggerMode));
            _cdlModel.nodeChanged(actNode);
        }
    }

    /**
     * Refresh the Conditional or Variable state.
     */
    void checkPressed() {
        if (_curNodeType == null || _curNodeType.equals("Conditional")) {
            for (int i = 0; i < _cdlRoot.getChildCount(); i++) {
                ConditionalTreeNode cdlNode = (ConditionalTreeNode) _cdlRoot.getChildAt(i);
                Conditional cdl = _conditionalManager.getBySystemName(cdlNode.getName());
                cdlNode.setText(buildNodeText("Conditional", cdl, i));  // NOI18N
                _cdlModel.nodeChanged(cdlNode);
            }
            return;
        }

        if (_curNodeType.equals("Variables")) {  // NOI18N
            for (int i = 0; i < _variableList.size(); i++) {
                ConditionalVariable variable = _variableList.get(i);
                ConditionalTreeNode varNode = (ConditionalTreeNode) _curNode.getChildAt(i);
                varNode.setText(buildNodeText("Variable", variable, i));  // NOI18N
                _cdlModel.nodeChanged(varNode);
            }
        }
    }

    /**
     * Process the node delete request.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Except for the root node, all nodes are ConditionalTreeNode")  // NOI18N
    void deletePressed() {
        TreePath parentPath;
        ConditionalTreeNode parentNode;
        TreeSet<String> oldTargetNames = new TreeSet<>();
        TreeSet<String> newTargetNames = new TreeSet<>();
        setEditMode(false);

        // Conditional
        switch (_curNodeType) {
            case "Conditional":   // NOI18N
                loadReferenceNames(_variableList, oldTargetNames);
                // Delete the conditional.
                _curLogix.deActivateLogix();
                String[] msgs = _curLogix.deleteConditional(_curNodeName);
                _curLogix.activateLogix();
                if (msgs != null) {
                    // Unable to delete due to existing conditional references
                    JOptionPane.showMessageDialog(_editLogixFrame,
                            Bundle.getMessage("Error11", (Object[]) msgs), // NOI18N
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);  // NOI18N
                    return;
                }
                updateWhereUsed(oldTargetNames, newTargetNames, _curNodeName);
                _showReminder = true;

                // Update the tree
                _cdlRoot.remove(_curNodeRow);
                _cdlModel.nodeStructureChanged(_cdlRoot);

                // Update the row numbers
                int childCount = _cdlRoot.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    _curNode = (ConditionalTreeNode) _cdlRoot.getChildAt(i);
                    _curNode.setRow(i);
                    _cdlModel.nodeChanged(_curNode);
                }

                if (_curLogix.getNumConditionals() < 1 && !_suppressReminder) {
                    // warning message - last Conditional deleted
                    JOptionPane.showMessageDialog(_editLogixFrame,
                            Bundle.getMessage("Warn1"), Bundle.getMessage("WarningTitle"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                }
                setMoveButtons();
                break;

            case "Variable":      // NOI18N
                loadReferenceNames(_variableList, oldTargetNames);
                if (_variableList.size() < 2 && !_suppressReminder) {
                    // warning message - last State Variable deleted
                    JOptionPane.showMessageDialog(_editLogixFrame,
                            Bundle.getMessage("Warn3"), Bundle.getMessage("WarningTitle"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                }

                // Adjust operator
                if (_curNodeRow == 0 && _variableList.size() > 1) {
                    _variableList.get(1).setOpern(Conditional.Operator.NONE);
                }

                // Remove the row, update and refresh the Variable list, update references
                _variableList.remove(_curNodeRow);
                updateVariableList();
                loadReferenceNames(_variableList, newTargetNames);
                updateWhereUsed(oldTargetNames, newTargetNames, _curNodeName);
                _showReminder = true;

                // Update the tree components
                parentPath = _curTreePath.getParentPath();
                parentNode = (ConditionalTreeNode) _curNode.getParent();
                parentNode.setRow(_variableList.size());
                _cdlModel.nodeChanged(parentNode);

                // Update the antecedent
                _curNode = (ConditionalTreeNode) parentNode.getPreviousSibling();
                antecedentChanged("");

                // Update the variable children
                parentNode.removeAllChildren();
                for (int v = 0; v < _variableList.size(); v++) {
                    ConditionalVariable variable = _variableList.get(v);
                    _leafNode = new ConditionalTreeNode(buildNodeText("Variable", variable, v), // NOI18N
                            "Variable", _curNodeName, v);  // NOI18N
                    parentNode.add(_leafNode);
                }

                _curNode = null;
                _newVariableItem = false;
                cleanUpVariable();

                _cdlModel.nodeStructureChanged(parentNode);
                _cdlTree.setSelectionPath(parentPath);
                break;

            case "Action":        // NOI18N
                // Remove the row, update and refresh the Action list
                removeActionTimers();
                _actionList.remove(_curNodeRow);
                updateActionList();
                _showReminder = true;

                // Update the tree components
                parentPath = _curTreePath.getParentPath();
                parentNode = (ConditionalTreeNode) _curNode.getParent();
                parentNode.setRow(_actionList.size());
                _cdlModel.nodeChanged(parentNode);
                parentNode.removeAllChildren();
                for (int a = 0; a < _actionList.size(); a++) {
                    ConditionalAction action = _actionList.get(a);
                    _leafNode = new ConditionalTreeNode(buildNodeText("Action", action, a), // NOI18N
                            "Action", _curNodeName, a);      // NOI18N
                    parentNode.add(_leafNode);
                }

                _curNode = null;
                _newActionItem = false;
                cleanUpAction();

                _cdlModel.nodeStructureChanged(parentNode);
                _cdlTree.setSelectionPath(parentPath);
                break;

            default:
                log.error("Delete called for unsupported node type: '{}'", _curNodeType);  // NOI18N
        }
    }

    /**
     * Move a conditional, variable or action row up 1 row.
     */
    void upPressed() {
        _showReminder = true;

        switch (_curNodeType) {
            case "Conditional":         // NOI18N
                // Update Logix index
                _curLogix.deActivateLogix();
                _curLogix.swapConditional(_curNodeRow - 1, _curNodeRow);
                _curLogix.activateLogix();
                moveTreeNode("Up");     // NOI18N
                break;

            case "Variable":            // NOI18N
                ConditionalVariable tempVar = _variableList.get(_curNodeRow);
                int newVarRow = _curNodeRow - 1;
                _variableList.set(_curNodeRow, _variableList.get(newVarRow));
                _variableList.set(newVarRow, tempVar);
                // Adjust operator
                if (newVarRow == 0) {
                    _variableList.get(newVarRow).setOpern(Conditional.Operator.NONE);
                    Operator newOper = (_logicType == Conditional.AntecedentOperator.ALL_AND)
                            ? Conditional.Operator.AND : Conditional.Operator.OR;
                    _variableList.get(_curNodeRow).setOpern(newOper);
                }
                updateVariableList();
                moveTreeNode("Up");     // NOI18N
                break;

            case "Action":              // NOI18N
                ConditionalAction tempAct = _actionList.get(_curNodeRow);
                int newActRow = _curNodeRow - 1;
                _actionList.set(_curNodeRow, _actionList.get(newActRow));
                _actionList.set(newActRow, tempAct);
                removeActionTimers();
                updateActionList();
                moveTreeNode("Up");     // NOI18N
                break;

            default:
                log.warn("Move Up called for unsupported node type: '{}'", _curNodeType);  // NOI18N
        }
    }

    /**
     * Move a conditional, variable or action row down 1 row.
     */
    void downPressed() {
        _showReminder = true;

        switch (_curNodeType) {
            case "Conditional":         // NOI18N
                _curLogix.deActivateLogix();
                _curLogix.swapConditional(_curNodeRow, _curNodeRow + 1);
                _curLogix.activateLogix();
                moveTreeNode("Down");   // NOI18N
                break;

            case "Variable":            // NOI18N
                ConditionalVariable tempVar = _variableList.get(_curNodeRow);
                int newVarRow = _curNodeRow + 1;
                _variableList.set(_curNodeRow, _variableList.get(newVarRow));
                _variableList.set(newVarRow, tempVar);
                // Adjust operator
                if (_curNodeRow == 0) {
                    _variableList.get(_curNodeRow).setOpern(Conditional.Operator.NONE);
                    Operator newOper = (_logicType == Conditional.AntecedentOperator.ALL_AND)
                            ? Conditional.Operator.AND : Conditional.Operator.OR;
                    _variableList.get(newVarRow).setOpern(newOper);
                }
                updateVariableList();
                moveTreeNode("Down");   // NOI18N
                break;

            case "Action":              // NOI18N
                ConditionalAction tempAct = _actionList.get(_curNodeRow);
                int newActRow = _curNodeRow + 1;
                _actionList.set(_curNodeRow, _actionList.get(newActRow));
                _actionList.set(newActRow, tempAct);
                removeActionTimers();
                updateActionList();
                moveTreeNode("Down");   // NOI18N
                break;

            default:
                log.warn("Move Down called for unsupported node type: '{}'", _curNodeType);  // NOI18N
        }
    }

    /**
     * Remove Action timers and listeners before Action list structure changes.
     * This relates to moving and deleting rows.  New actions at the end are not problem.
     * The issue is that the timer listeners are tied to the action row number.
     * This can result in orphan timers and listeners that keep running.
     * @since 4.11.2
     */
    void removeActionTimers() {
        // Use the real list, not a copy.
        DefaultConditional cdl = (DefaultConditional) _curConditional;
        for (ConditionalAction act : cdl.getActionList()) {
            if (act.getTimer() != null) {
                act.stopTimer();
                act.setTimer(null);
                act.setListener(null);
            }
        }
    }

    /**
     * Move a tree node in response to a up or down request.
     *
     * @param direction The direction of movement, Up or Down
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Except for the root node, all nodes are ConditionalTreeNode")  // NOI18N
    void moveTreeNode(String direction) {
        // Update the node
        int oldRow = _curNodeRow;
        if (direction.equals("Up")) {    // NOI18N
            _curNodeRow -= 1;
        } else {
            _curNodeRow += 1;
        }
        _curNode.setRow(_curNodeRow);
        if (_curNodeType.equals("Variable")) {  // NOI18N
            _curNode.setText(buildNodeText("Variable", _variableList.get(_curNodeRow), _curNodeRow));   // NOI18N
        }
        _cdlModel.nodeChanged(_curNode);

        // Update the sibling
        ConditionalTreeNode siblingNode;
        if (direction.equals("Up")) {    // NOI18N
            siblingNode = (ConditionalTreeNode) _curNode.getPreviousSibling();
            siblingNode.setRow(siblingNode.getRow() + 1);
        } else {
            siblingNode = (ConditionalTreeNode) _curNode.getNextSibling();
            siblingNode.setRow(siblingNode.getRow() - 1);
        }
        if (_curNodeType.equals("Variable")) {  // NOI18N
            siblingNode.setText(buildNodeText("Variable", _variableList.get(oldRow), oldRow));  // NOI18N
        }
        _cdlModel.nodeChanged(siblingNode);

        // Update the tree
        if (_curNodeType.equals("Conditional")) {   // NOI18N
            _cdlRoot.insert(_curNode, _curNodeRow);
            _cdlModel.nodeStructureChanged(_cdlRoot);
        } else {
            ConditionalTreeNode parentNode = (ConditionalTreeNode) _curNode.getParent();
            parentNode.insert(_curNode, _curNodeRow);
            _cdlModel.nodeStructureChanged(parentNode);
        }
        _cdlTree.setSelectionPath(new TreePath(_curNode.getPath()));
        setMoveButtons();
    }

    /**
     * Enable/Disable the Up and Down buttons based on the postion in the list.
     */
    void setMoveButtons() {
        if (_curNode == null) {
            return;
        }

        Component[] compList = _moveButtonPanel.getComponents();
        JButton up = (JButton) compList[1];
        JButton down = (JButton) compList[3];

        up.setEnabled(true);
        down.setEnabled(true);

        int rows;
        if (_curNodeType.equals("Conditional")) {       // NOI18N
            rows = _curLogix.getNumConditionals();
        } else {
            ConditionalTreeNode parent = (ConditionalTreeNode) _curNode.getParent();
            rows = parent.getRow();
        }

        if (_curNodeRow < 1) {
            up.setEnabled(false);
        }
        if (_curNodeRow >= rows - 1) {
            down.setEnabled(false);
        }

        // Disable move buttons during Variable or Action add or edit processing, or nothing selected
        if ((_newVariableItem && _curNodeType.equals("Variable")) // NOI18N
                || (_newActionItem && _curNodeType.equals("Action"))
                || (_editActive)
                || (_cdlTree.getSelectionCount() == 0)) {  // NOI18N
            up.setEnabled(false);
            down.setEnabled(false);
        }

        _moveButtonPanel.setVisible(true);
    }

    /**
     * Respond to Help button press in the Edit Logix menu bar. Only visible when
     * using mixed mode and an antecedent node is selected.
     */
    void helpPressed() {
        JOptionPane.showMessageDialog(null,
                new String[]{
                    Bundle.getMessage("ConditionalHelpText1"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText2"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText3"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText4"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText5"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText6"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText7") // NOI18N
                },
                Bundle.getMessage("MenuHelp"), JOptionPane.INFORMATION_MESSAGE);  // NOI18N
    }

    /**
     * Cancel the current node edit.
     */
    void cancelPressed() {
        switch (_curNodeType) {
            case "Variable":       // NOI18N
                cancelEditVariable();
                break;

            case "Action":         // NOI18N
                cancelEditAction();
                break;

            default:
                break;
        }
        makeDetailGrid("EmptyGrid");  // NOI18N
        setEditMode(false);
        _cdlTree.setSelectionPath(_curTreePath);
        _cdlTree.grabFocus();
    }

    /**
     * Clean up, notify the parent Logix that edit session is done.
     */
    void donePressed() {
        if (_curNodeType != null) {
            switch (_curNodeType) {
                case "Variable":       // NOI18N
                    cancelEditVariable();
                    break;

                case "Action":         // NOI18N
                    cancelEditAction();
                    break;

                default:
                    break;
            }
        }
        closeSinglePanelPickList();
        if (_pickTables != null) {
            _pickTables.dispose();
            _pickTables = null;
        }

        _editLogixFrame.setVisible(false);
        _editLogixFrame.dispose();
        _editLogixFrame = null;

        logixData.clear();
        logixData.put("Finish", _curLogix.getSystemName());  // NOI18N
        fireLogixEvent();
    }

    public void bringToFront() {
        _editLogixFrame.toFront();
    }

    // ============  Tree Content and Navigation ============

    /**
     * Create the conditional tree structure using the current Logix.
     *
     * @return _cdlTree The tree ddefinition with its content
     */
    JTree buildConditionalTree() {
        _cdlRoot = new DefaultMutableTreeNode("Root Node");      // NOI18N
        _cdlModel = new DefaultTreeModel(_cdlRoot);
        _cdlTree = new JTree(_cdlModel);

        createConditionalContent();

        // build the tree GUI
        _cdlTree.expandPath(new TreePath(_cdlRoot));
        _cdlTree.setRootVisible(false);
        _cdlTree.setShowsRootHandles(true);
        _cdlTree.setScrollsOnExpand(true);
        _cdlTree.setExpandsSelectedPaths(true);
        _cdlTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        // tree listeners
        _cdlTree.addTreeSelectionListener(_cdlListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (_editActive) {
                    if (e.getNewLeadSelectionPath() != _curTreePath) {
                        _cdlTree.setSelectionPath(e.getOldLeadSelectionPath());
                        showNodeEditMessage();
                    }
                    return;
                }

                _curTreePath = _cdlTree.getSelectionPath();
                if (_curTreePath != null) {
                    Object chkLast = _curTreePath.getLastPathComponent();
                    if (chkLast instanceof ConditionalTreeNode) {
                        treeRowSelected((ConditionalTreeNode) chkLast);
                    }
                }
            }
        });

        _cdlTree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent e) {
                ConditionalTreeNode checkNode = (ConditionalTreeNode) e.getPath().getLastPathComponent();
                if (checkNode.getType().equals("Variables")) {  // NOI18N
                    // Include the field descriptions in the node name
                    checkNode.setText(buildNodeText("Variables", _curConditional, 1));  // NOI18N
                    _cdlModel.nodeChanged(checkNode);
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent e) {
                ConditionalTreeNode checkNode = (ConditionalTreeNode) e.getPath().getLastPathComponent();
                if (checkNode.getType().equals("Variables")) {  // NOI18N
                    // Remove the field descriptions from the node name
                    checkNode.setText(buildNodeText("Variables", _curConditional, 0));  // NOI18N
                    _cdlModel.nodeChanged(checkNode);
                }

                if (_cdlTree.getSelectionCount() == 0) {
                    makeDetailGrid("EmptyGrid");  // NOI18N
                }
            }
        });

        return _cdlTree;
    }

    /**
     * Create the tree content. Level 1 are the conditionals, Level 2 includes the
     * antecedent, logic type, trigger mode and parent nodes for Variables and
     * Actions, Level 3 contains the detail Variable and Action entries.
     */
    void createConditionalContent() {
        int _numConditionals = _curLogix.getNumConditionals();
        for (int i = 0; i < _numConditionals; i++) {
            String csName = _curLogix.getConditionalByNumberOrder(i);
            Conditional curConditional = _curLogix.getConditional(csName);
            _cdlNode = new ConditionalTreeNode(buildNodeText("Conditional", curConditional, 0), "Conditional", csName, i);    // NOI18N
            _cdlRoot.add(_cdlNode);

            _leafNode = new ConditionalTreeNode(buildNodeText("Antecedent", curConditional, 0), "Antecedent", csName, 0);   // NOI18N
            _cdlNode.add(_leafNode);

            _variableList = curConditional.getCopyOfStateVariables();
            int varCount = _variableList.size();
            _varHead = new ConditionalTreeNode(buildNodeText("Variables", _curConditional, 0), "Variables", csName, varCount);     // NOI18N
            _cdlNode.add(_varHead);
            for (int v = 0; v < _variableList.size(); v++) {
                ConditionalVariable variable = _variableList.get(v);
                _leafNode = new ConditionalTreeNode(buildNodeText("Variable", variable, v), "Variable", csName, v);
                _varHead.add(_leafNode);
            }

            _leafNode = new ConditionalTreeNode(buildNodeText("LogicType", curConditional, 0), "LogicType", csName, 0);      // NOI18N
            _cdlNode.add(_leafNode);

            boolean triggerMode = curConditional.getTriggerOnChange();
            _leafNode = new ConditionalTreeNode(buildNodeText("TriggerMode", curConditional, 0), "TriggerMode", csName, 0);      // NOI18N
            _cdlNode.add(_leafNode);

            _actionList = curConditional.getCopyOfActions();
            int actCount = _actionList.size();
            _actHead = new ConditionalTreeNode("Actions", "Actions", csName, actCount);      // NOI18N
            _cdlNode.add(_actHead);
            for (int a = 0; a < _actionList.size(); a++) {
                ConditionalAction action = _actionList.get(a);
                _leafNode = new ConditionalTreeNode(action.description(triggerMode), "Action", csName, a);      // NOI18N
                _actHead.add(_leafNode);
            }
        }
    }

    /**
     * Change the button row based on the currently selected node type. Invoke
     * edit where appropriate.
     *
     * @param selectedNode The node object
     */
    void treeRowSelected(ConditionalTreeNode selectedNode) {
        // Set the current node variables
        _curNode = selectedNode;
        _curNodeName = selectedNode.getName();
        _curNodeType = selectedNode.getType();
        _curNodeText = selectedNode.getText();
        _curNodeRow = selectedNode.getRow();

        // Set the current conditional variables if different conditional
        if (!_curConditionalName.equals(_curNodeName)) {
            _curConditional = _conditionalManager.getConditional(_curNodeName);
            _antecedent = _curConditional.getAntecedentExpression();
            _logicType = _curConditional.getLogicType();
            _triggerMode = _curConditional.getTriggerOnChange();
            _variableList = _curConditional.getCopyOfStateVariables();
            _actionList = _curConditional.getCopyOfActions();
            _curConditionalName = _curNodeName;
        }

        // Reset button bar
        _addButtonPanel.setVisible(false);
        _checkButtonPanel.setVisible(false);
        _toggleButtonPanel.setVisible(false);
        _moveButtonPanel.setVisible(false);
        _deleteButtonPanel.setVisible(false);
        _helpButtonPanel.setVisible(false);

        _labelPanel.removeAll();
        switch (_curNodeType) {
            case "Conditional":     // NOI18N
                _labelPanel.add(_conditionalLabel);
                _addButtonPanel.setVisible(true);
                _checkButtonPanel.setVisible(true);
                _deleteButtonPanel.setVisible(true);
                setMoveButtons();
                editPressed();
                break;

            case "Antecedent":      // NOI18N
                editPressed();
                break;

            case "LogicType":       // NOI18N
                editPressed();
                break;

            case "TriggerMode":     // NOI18N
                _labelPanel.add(_triggerModeLabel);
                _toggleButtonPanel.setVisible(true);
                makeDetailGrid("EmptyGrid");  // NOI18N
                break;

            case "Variables":       // NOI18N
                _labelPanel.add(_variablesLabel);
                _addButtonPanel.setVisible(true);
                _checkButtonPanel.setVisible(true);
                makeDetailGrid("EmptyGrid");  // NOI18N
                break;

            case "Variable":        // NOI18N
                _labelPanel.add(_variableLabel);
                _addButtonPanel.setVisible(true);
                _deleteButtonPanel.setVisible(true);
                if (_logicType != Conditional.AntecedentOperator.MIXED) {
                    setMoveButtons();
                }
                editPressed();
                break;

            case "Actions":         // NOI18N
                _labelPanel.add(_actionsLabel);
                _addButtonPanel.setVisible(true);
                makeDetailGrid("EmptyGrid");  // NOI18N
                break;

            case "Action":          // NOI18N
                _labelPanel.add(_actionLabel);
                _addButtonPanel.setVisible(true);
                _deleteButtonPanel.setVisible(true);
                setMoveButtons();
                editPressed();
                break;

            default:
                log.warn("Should not be here");  // NOI18N
        }
    }

    /**
     * Create the localized node text display strings based on node type.
     *
     * @param nodeType  The type of the node
     * @param component The conditional object or child object
     * @param idx       Optional index value
     * @return nodeText containing the text to display on the node
     */
    String buildNodeText(String nodeType, Object component, int idx) {
        Conditional cdl;
        ConditionalAction act;
        ConditionalVariable var;

        switch (nodeType) {
            case "Conditional":  // NOI18N
                cdl = (Conditional) component;
                String cdlStatus = (cdl.getState() == Conditional.TRUE)
                        ? Bundle.getMessage("True") // NOI18N
                        : Bundle.getMessage("False");  // NOI18N
                String cdlNames = cdl.getSystemName() + " -- " + cdl.getUserName();
                String cdlFill = StringUtils.repeat("&nbsp;", 5);  // NOI18N
                String cdlLine = "<html>" + cdlNames + cdlFill + "<strong>[ " + cdlStatus + " ]</strong></html>";  // NOI18N
                return cdlLine;

            case "Antecedent":  // NOI18N
                cdl = (Conditional) component;
                String antecedent = translateAntecedent(cdl.getAntecedentExpression(), false);
                if (cdl.getLogicType() != Conditional.AntecedentOperator.MIXED) {
                    antecedent = "- - - - - - - - -";
                }
                return Bundle.getMessage("LogixAntecedent") + " " + antecedent;   // NOI18N

            case "LogicType":  // NOI18N
                cdl = (Conditional) component;
                Conditional.AntecedentOperator logicType = cdl.getLogicType();
                String logicName; // used for display only
                switch (logicType) {
                    case ALL_AND:
                        logicName = Bundle.getMessage("LogicAND");      // NOI18N
                        break;
                    case ALL_OR:
                        logicName = Bundle.getMessage("LogicOR");       // NOI18N
                        break;
                    case MIXED:
                        logicName = Bundle.getMessage("LogicMixed");    // NOI18N
                        break;
                    default:
                        logicName = "None"; // only used for invalid LogicType
                }
                return Bundle.getMessage("LabelLogicTypeActions") + "  " + logicName;   // NOI18N

            case "TriggerMode":  // NOI18N
                cdl = (Conditional) component;
                boolean triggerMode = cdl.getTriggerOnChange();
                String triggerText;
                if (triggerMode) {
                    triggerText = Bundle.getMessage("triggerOnChange"); // NOI18N
                } else {
                    triggerText = Bundle.getMessage("triggerOnAny");    // NOI18N
                }
                return Bundle.getMessage("LabelTriggerModeActions") + "  " + triggerText; // NOI18N

            case "Variables":  // NOI18N
                if (idx == 0) {
                    // The node is not expanded, return plain content
                    return Bundle.getMessage("NodeVariablesCollapsed");  // NOI18N
                } else {
                    // The node is expanded, include the field names
                    return String.format("%s   [[ %s || %s || %s ]]", // NOI18N
                            Bundle.getMessage("NodeVariablesExpanded"), // NOI18N
                            Bundle.getMessage("ColumnLabelDescription"), // NOI18N
                            Bundle.getMessage("ColumnLabelTriggersCalculation"), // NOI18N
                            Bundle.getMessage("ColumnState")); // NOI18N
                }

            case "Variable":  // NOI18N
                var = (ConditionalVariable) component;

                String rowNum = "R" + (idx + 1) + (idx > 9 ? " " : "  "); // NOI18N
                String rowOper = var.getOpernString() + " ";

                String rowNot = "";
                if (var.isNegated()) {
                    rowNot = Bundle.getMessage("LogicNOT") + " ";     // NOI18N
                }

                String boldFormat = "  || <strong>%s</strong>";  // NOI18N
                String rowTrigger = String.format(boldFormat,
                        (var.doTriggerActions()) ? Bundle.getMessage("ButtonYes") : Bundle.getMessage("ButtonNo"));  // NOI18N
                String rowStatus = String.format(boldFormat,
                        (var.evaluate()) ? Bundle.getMessage("True") : Bundle.getMessage("False"));  // NOI18N

                String varLine = "<html>" + rowNum + rowOper + rowNot + var.toString() + rowTrigger + rowStatus + "</html>";  // NOI18N
                return varLine;

            case "Actions":  // NOI18N
                return Bundle.getMessage("NodeActions");  // NOI18N

            case "Action":   // NOI18N
                act = (ConditionalAction) component;
                return act.description(_triggerMode);

            default:
                return "None";
        }
    }

    /**
     * Display reminder to save.
     */
    void showNodeEditMessage() {
        if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showInfoMessage(Bundle.getMessage("NodeEditTitle"), // NOI18N
                            Bundle.getMessage("NodeEditText"), // NOI18N
                            getClassName(),
                            "SkipNodeEditMessage"); // NOI18N
        }
    }

    /**
     * Focus gained implies intent to make changes, set up for edit.
     */
    transient FocusListener detailFocusEvent = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
            if (!_editActive) {
                setEditMode(true);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
        }
    };

    /**
     * Add the focus listener to each detail edit field.
     */
    void setFocusListeners() {
        _editConditionalUserName.addFocusListener(detailFocusEvent);
        _editAntecedent.addFocusListener(detailFocusEvent);
        _editOperatorMode.addFocusListener(detailFocusEvent);
        _variableItemBox.addFocusListener(detailFocusEvent);
        _variableOperBox.addFocusListener(detailFocusEvent);
        _variableNegated.addFocusListener(detailFocusEvent);
        _variableTriggerActions.addFocusListener(detailFocusEvent);
        _variableNameField.addFocusListener(detailFocusEvent);
        _variableStateBox.addFocusListener(detailFocusEvent);
        _variableSignalBox.addFocusListener(detailFocusEvent);
        _selectLogixBox.addFocusListener(detailFocusEvent);
        _selectConditionalBox.addFocusListener(detailFocusEvent);
        _variableCompareOpBox.addFocusListener(detailFocusEvent);
        _variableCompareTypeBox.addFocusListener(detailFocusEvent);
        _variableData1Field.addFocusListener(detailFocusEvent);
        _variableData2Field.addFocusListener(detailFocusEvent);
        _actionItemBox.addFocusListener(detailFocusEvent);
        _actionNameField.addFocusListener(detailFocusEvent);
        _actionTypeBox.addFocusListener(detailFocusEvent);
        _actionBox.addFocusListener(detailFocusEvent);
        _shortActionString.addFocusListener(detailFocusEvent);
        _longActionString.addFocusListener(detailFocusEvent);
        _actionSetButton.addFocusListener(detailFocusEvent);
        _actionOptionBox.addFocusListener(detailFocusEvent);
    }

    /**
     * Enable/disable buttons based on edit state.
     * Open pick lists based on the current SelectionMode.
     * The edit state controls the ability to select tree nodes.
     *
     * @param active True to make edit active, false to make edit inactive
     */
    void setEditMode(boolean active) {
        _editActive = active;
        _cancelAction.setEnabled(active);
        _updateAction.setEnabled(active);
        Component delButton = _deleteButtonPanel.getComponent(0);
        if (delButton instanceof JButton) {
            delButton.setEnabled(!active);
        }
        Component addButton = _addButtonPanel.getComponent(0);
        if (addButton instanceof JButton) {
            addButton.setEnabled(!active);
        }
        if (_curNodeType != null) {
            if (_curNodeType.equals("Conditional") || _curNodeType.equals("Variable") || _curNodeType.equals("Action")) {  // NOI18N
                setMoveButtons();
            }
        }
        if (active) {
            setPickWindow("Activate", Conditional.ItemType.NONE);  // NOI18N
        } else {
            setPickWindow("Deactivate", Conditional.ItemType.NONE);  // NOI18N
        }
    }

    /**
     * Ceate tabbed Pick Taba;e or Pick Single based on Selection Mode.
     * Called by {@link #setEditMode} when edit mode becomes active.
     * Called by {@link #variableTypeChanged} and {@link #actionItemChanged} when item type changes.
     *
     * @param cmd The source or action to be performed.
     * @param item The item type for Variable or Action or zero
     */
    void setPickWindow(String cmd, Conditional.ItemType item) {
        if (_selectionMode == SelectionMode.USECOMBO) {
            return;
        }
        // Save the item information
        if (cmd.equals("Variable") || cmd.equals("Action")) {  // NOI18N
            _pickCommand = cmd;
            _pickItem = item;
            if (_editActive) {
                if (_selectionMode == SelectionMode.USEMULTI) {
                    doPickList();
                } else {
                    doPickSingle();
                }
            }
        }
        if (cmd.equals("Activate")) {  // NOI18N
            if (_curNodeType.equals("Variable") || _curNodeType.equals("Action")) {  // NOI18N
                // Open the appropriate window based on the save values
                if (_selectionMode == SelectionMode.USEMULTI) {
                    doPickList();
                } else {
                    doPickSingle();
                }
            }
        }
        if (cmd.equals("Deactivate")) {  // NOI18N
            // Close/dispose the pick window
            hidePickListTable();
            closeSinglePanelPickList();
        }
    }

    /**
     * Create a Variable or Action based tabbed PickList with appropriate tab selected.
     */
    void doPickList() {
        if (_pickItem == Conditional.ItemType.NONE) {
            return;
        }
        if (_pickTables == null) {
            openPickListTable();
        }
        if (_pickCommand.equals("Variable")) {
            setPickListTab(_pickItem, false);
        } else if (_pickCommand.equals("Action")) {
            setPickListTab(_pickItem, true);
        }
    }

    /**
     * Create a Variable or Action based single pane PickList.
     */
    void doPickSingle() {
        if (_pickCommand.equals("Variable")) {
            createSinglePanelPickList(_pickItem, new PickSingleListener(_variableNameField, _pickItem), false);
        } else if (_pickCommand.equals("Action")) {
            createSinglePanelPickList(_pickItem, new PickSingleListener(_actionNameField, _pickItem), true);
        }
    }

    // ============  Edit Variable Section ============

    /**
     * Called once during class initialization to define the GUI objects. Where
     * possible, the combo box content is loaded.
     */
    void buildVariableComponents() {
        // Item Type
        _variableItemBox = new JComboBox<>();
        for (Conditional.ItemType itemType : Conditional.ItemType.getStateVarList()) {
            _variableItemBox.addItem(itemType);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_variableItemBox);
        _variableItemBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Conditional.ItemType newVariableItem = _variableItemBox.getItemAt(_variableItemBox.getSelectedIndex());
                if (log.isDebugEnabled()) {
                    log.debug("_variableItemBox Listener: new = {}, curr = {}, row = {}",  // NOI18N
                            newVariableItem, _curVariableItem, _curNodeRow);
                }
                if (newVariableItem != _curVariableItem) {
                    if (_curNodeRow >= 0) {
                        _curVariable = new ConditionalVariable();
                        _variableList.set(_curNodeRow, _curVariable);
                    }
                    _curVariableItem = newVariableItem;
                }
                variableTypeChanged(newVariableItem);
            }
        });

        // Oper type
        _variableOperBox = new JComboBox<String>();
        _variableOperBox.addItem(Bundle.getMessage("LogicAND"));  // NOI18N
        _variableOperBox.addItem(Bundle.getMessage("LogicOR"));  // NOI18N

        // Negation
        _variableNegated = new JCheckBox();

        // trigger
        _variableTriggerActions = new JCheckBox();

        // Item Name
        _variableNameField = new JTextField(20);

        // Combo box section for selecting conditional reference
        //   First box selects the Logix, the second selects the conditional within the logix
        _selectLogixBox.addItem("XXXXXXXXXXXXXXXXXXXXX");  // NOI18N
        _selectConditionalBox.addItem("XXXXXXXXXXXXXXXXXXXXX");  // NOI18N
        _selectLogixBox.addActionListener(selectLogixBoxListener);
        _selectConditionalBox.addActionListener(selectConditionalBoxListener);

        // State Box
        _variableStateBox = new JComboBox<>();
        _variableStateBox.addItem(Conditional.Type.XXXXXXX);  // NOI18N

        // Aspects
        _variableSignalBox = new JComboBox<>();
        _variableSignalBox.addItem("XXXXXXXXX");  // NOI18N

        // Compare operator
        _variableCompareOpBox = new JComboBox<>();
        for (int i = 1; i <= ConditionalVariable.NUM_COMPARE_OPERATIONS; i++) {
            _variableCompareOpBox.addItem(ConditionalVariable.getCompareOperationString(i));
        }

        // Compare type
        _variableCompareTypeBox = new JComboBox<>();
        for (Conditional.Type type : Conditional.Type.getMemoryItems()) {
            _variableCompareTypeBox.addItem(type);
        }
        _variableCompareTypeBox.addActionListener(compareTypeBoxListener);

        // Data 1
        _variableData1Field = new JTextField(10);

        // Data 2
        _variableData2Field = new JTextField(10);
    }

    // ------------ Make Variable Edit Grid Panels ------------

    /**
     * Create a one row grid with just the Variable Type box. This is the base
     * for larger grids as well as the initial grid for new State Variables.
     *
     * @param c the constraints object used for the grid construction
     */
    void makeEmptyVariableGrid(GridBagConstraints c) {
        // Variable type box
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row0Label = new JLabel(Bundle.getMessage("LabelVariableType"));  // NOI18N
        row0Label.setToolTipText(Bundle.getMessage("VariableTypeHint"));  // NOI18N
        _gridPanel.add(row0Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableItemBox, c);
    }

    /*
     * Create the Oper, Not and Trigger rows.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeOptionsVariableGrid(GridBagConstraints c) {
        makeEmptyVariableGrid(c);

        // Oper Select
        c.gridy = 1;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row1Label = new JLabel(Bundle.getMessage("ColumnLabelOperator"));  // NOI18N
        row1Label.setToolTipText(Bundle.getMessage("VariableOperHint"));  // NOI18N
        _gridPanel.add(row1Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableOperBox, c);

        // Not Select
        c.gridy = 2;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row2Label = new JLabel(Bundle.getMessage("ColumnLabelNot"));  // NOI18N
        row2Label.setToolTipText(Bundle.getMessage("VariableNotHint"));  // NOI18N
        _gridPanel.add(row2Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableNegated, c);

        // Trigger Select
        c.gridy = 3;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row3Label = new JLabel(Bundle.getMessage("ColumnLabelTriggersCalculation"));  // NOI18N
        row3Label.setToolTipText(Bundle.getMessage("VariableTriggerHint"));  // NOI18N
        _gridPanel.add(row3Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableTriggerActions, c);
    }

    /**
     * Create the standard Name and State rows.
     * The name field will be either a text field or a combo box.
     * The name field label is a variable to support run time changes.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeStandardVariableGrid(GridBagConstraints c) {
        makeOptionsVariableGrid(c);

        // Name Field
        c.gridy = 4;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        _gridPanel.add(_variableNameLabel, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        if (_selectionMode == SelectionMode.USECOMBO) {
            _gridPanel.add(_comboNameBox, c);
        } else {
            _gridPanel.add(_variableNameField, c);
        }

        // State Box
        c.gridy = 5;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row5Label = new JLabel(Bundle.getMessage("LabelVariableState"));  // NOI18N
        row5Label.setToolTipText(Bundle.getMessage("VariableStateHint"));  // NOI18N
        _gridPanel.add(row5Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableStateBox, c);
    }

    /**
     * Add the Aspect field for signal heads and signal masts.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeSignalAspectVariableGrid(GridBagConstraints c) {
        makeStandardVariableGrid(c);

        // Mast Aspect Box
        c.gridy = 6;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row5Label = new JLabel(Bundle.getMessage("LabelVariableAspect"));  // NOI18N
        row5Label.setToolTipText(Bundle.getMessage("VariableAspectHint"));  // NOI18N
        _gridPanel.add(row5Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableSignalBox, c);
    }

    /**
     * Create the Logix and Conditional rows and the State row.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeConditionalVariableGrid(GridBagConstraints c) {
        makeOptionsVariableGrid(c);

        // Logix Selection ComboBox
        c.gridy = 4;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row4Label = new JLabel(Bundle.getMessage("SelectLogix"));  // NOI18N
        row4Label.setToolTipText(Bundle.getMessage("VariableLogixHint"));  // NOI18N
        _gridPanel.add(row4Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_selectLogixBox, c);

        // Conditional Selection ComboBox
        c.gridy = 5;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row5Label = new JLabel(Bundle.getMessage("SelectConditional"));  // NOI18N
        row5Label.setToolTipText(Bundle.getMessage("VariableConditionalHint"));  // NOI18N
        _gridPanel.add(row5Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_selectConditionalBox, c);

        // State Box
        c.gridy = 6;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row6Label = new JLabel(Bundle.getMessage("LabelVariableState"));  // NOI18N
        row6Label.setToolTipText(Bundle.getMessage("VariableStateHint"));  // NOI18N
        _gridPanel.add(row6Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableStateBox, c);
    }

    /**
     * Create the Memory specific rows.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeMemoryVariableGrid(GridBagConstraints c) {
        makeOptionsVariableGrid(c);

        // Name Field
        c.gridy = 4;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row4Label = new JLabel(Bundle.getMessage("LabelItemName"));  // NOI18N
        row4Label.setToolTipText(Bundle.getMessage("NameHintMemory"));  // NOI18N
        _gridPanel.add(row4Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        if (_selectionMode == SelectionMode.USECOMBO) {
            _gridPanel.add(_comboNameBox, c);
        } else {
            _gridPanel.add(_variableNameField, c);
        }

        // Comparison Operator
        c.gridy = 5;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row5Label = new JLabel(Bundle.getMessage("LabelCompareOp"));  // NOI18N
        row5Label.setToolTipText(Bundle.getMessage("CompareHintMemory"));  // NOI18N
        _gridPanel.add(row5Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableCompareOpBox, c);

        // Compare As
        c.gridy = 6;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row6Label = new JLabel(Bundle.getMessage("LabelCompareType"));  // NOI18N
        row6Label.setToolTipText(Bundle.getMessage("CompareTypeHint"));  // NOI18N
        _gridPanel.add(row6Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableCompareTypeBox, c);

        // Literal Value (default) / Memory Value (name)
        c.gridy = 7;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        _gridPanel.add(_variableMemoryValueLabel, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableData1Field, c);
    }

    /**
     * Create the Fast Clock start and end time rows.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeFastClockVariableGrid(GridBagConstraints c) {
        makeOptionsVariableGrid(c);

        // Start Time Field
        c.gridy = 4;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row4Label = new JLabel(Bundle.getMessage("LabelStartTime"));  // NOI18N
        row4Label.setToolTipText(Bundle.getMessage("DataHintTime"));  // NOI18N
        _gridPanel.add(row4Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableData1Field, c);

        // End Time Field
        c.gridy = 5;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row5Label = new JLabel(Bundle.getMessage("LabelEndTime"));  // NOI18N
        row5Label.setToolTipText(Bundle.getMessage("DataHintTime"));  // NOI18N
        _gridPanel.add(row5Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_variableData2Field, c);
    }

    // ------------ Main Variable methods ------------

    /**
     * Set display to show current state variable (curVariable) parameters.
     */
    void initializeStateVariables() {
        Conditional.Type testType = _curVariable.getType();
        if (log.isDebugEnabled()) {
            log.debug("initializeStateVariables: testType= {}", testType);  // NOI18N
        }
        Conditional.ItemType itemType = testType.getItemType();
        log.debug("initializeStateVariables: itemType= {}, testType= {}", itemType, testType);  // NOI18N
        if (itemType == _variableItemBox.getSelectedItem()) {
            // Force a refresh of variableTypeChanged
            variableTypeChanged(itemType);
        }
        _variableItemBox.setSelectedItem(itemType);
        _variableOperBox.setSelectedItem(_curVariable.getOpernString());
        _variableNegated.setSelected(_curVariable.isNegated());
        _variableTriggerActions.setSelected(_curVariable.doTriggerActions());

        switch (itemType) {
            case NONE:
                _variableNameField.setText("");
                break;

            case SENSOR:
            case TURNOUT:
            case LIGHT:
            case CONDITIONAL:
            case WARRANT:
                _variableStateBox.setSelectedItem(testType);
                _variableNameField.setText(_curVariable.getName());
                break;

            case SIGNALHEAD:
                _variableStateBox.setSelectedItem(testType);
                _variableNameField.setText(_curVariable.getName());
                if (Conditional.Type.isSignalHeadApperance(testType)) {
                    _variableStateBox.setSelectedItem(Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS);
                    _variableSignalBox.setSelectedItem(_curVariable.getType());
                }
                break;

            case SIGNALMAST:
                // set display to show current state variable (curVariable) parameters
                _variableStateBox.setSelectedItem(testType);
                _variableNameField.setText(_curVariable.getName());
                if (testType == Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS) {
                    _variableSignalBox.setSelectedItem(_curVariable.getDataString());
                }
                break;

            case MEMORY:
                _variableCompareTypeBox.setSelectedIndex(
                        Conditional.Type.getIndexInList(Conditional.Type.getMemoryItems(), testType));
                _variableNameField.setText(_curVariable.getName());
                int num1 = _curVariable.getNum1() - 1;
                if (num1 == -1) {  // former code was only equals
                    num1 = ConditionalVariable.EQUAL - 1;
                }
                _variableCompareOpBox.setSelectedIndex(num1);
                _variableData1Field.setText(_curVariable.getDataString());
                break;

            case CLOCK:
                int time = _curVariable.getNum1();
                _variableData1Field.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                time = _curVariable.getNum2();
                _variableData2Field.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                _variableNameField.setText("");
                break;

            case OBLOCK:
                _variableNameField.setText(_curVariable.getName());
                //_variableStateBox.removeAllItems();
                for (Conditional.Type type : Conditional.Type.getOBlockItems()) {
                    _variableStateBox.addItem(type);
                    if (type.toString().equals(OBlock.getLocalStatusName(_curVariable.getDataString()))) {
                        _variableStateBox.setSelectedItem(type);
                    }
                }
//                Iterator<String> names = OBlock.getLocalStatusNames();
//                while (names.hasNext()) {
//                    _variableStateBox.addItem(names.next());
//                }
//                _variableStateBox.setSelectedItem(OBlock.getLocalStatusName(_curVariable.getDataString()));
                break;

            case ENTRYEXIT:
                _variableNameField.setText(_curVariable.getBean().getUserName());
                _variableStateBox.setSelectedItem(testType);
                break;

            default:
                break;
        }
        _detailGrid.setVisible(true);
    }

    /**
     * Respond to change in variable type chosen in the State Variable combo box.
     *
     * @param itemType value representing the newly selected Conditional type,
     *                 i.e. ITEM_TYPE_SENSOR
     */
    private void variableTypeChanged(Conditional.ItemType itemType) {
        Conditional.Type testType = _curVariable.getType();
        log.debug("variableTypeChanged: itemType= {}, testType= {}", itemType, testType);  // NOI18N
        _variableStateBox.removeAllItems();
        _variableNameField.removeActionListener(variableSignalHeadNameListener);
        _variableNameField.removeActionListener(variableSignalMastNameListener);
        _variableStateBox.removeActionListener(variableSignalTestStateListener);
        _detailGrid.setVisible(false);

        if (_comboNameBox != null) {
            for (ActionListener item : _comboNameBox.getActionListeners()) {
                _comboNameBox.removeActionListener(item);
            }
            _comboNameBox.removeFocusListener(detailFocusEvent);
        }
        setPickWindow("Variable", itemType);  // NOI18N

        _variableOperBox.setSelectedItem(_curVariable.getOpernString());
        _variableNegated.setSelected(_curVariable.isNegated());
        _variableTriggerActions.setSelected(_curVariable.doTriggerActions());

        switch (itemType) {
            case NONE:
                makeDetailGrid("EmptyVariable");  // NOI18N
                break;

            case SENSOR:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintSensor"));  // NOI18N
                for (Conditional.Type type : Conditional.Type.getSensorItems()) {
                    _variableStateBox.addItem(type);
                }
                setVariableNameBox(itemType);
                makeDetailGrid("StandardVariable");  // NOI18N
                break;

            case TURNOUT:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintTurnout"));  // NOI18N
                for (Conditional.Type type : Conditional.Type.getTurnoutItems()) {
                    _variableStateBox.addItem(type);
                }
                setVariableNameBox(itemType);
                makeDetailGrid("StandardVariable");  // NOI18N
                break;

            case LIGHT:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintLight"));  // NOI18N
                for (Conditional.Type type : Conditional.Type.getLightItems()) {
                    _variableStateBox.addItem(type);
                }
                setVariableNameBox(itemType);
                makeDetailGrid("StandardVariable");  // NOI18N
                break;

            case SIGNALHEAD:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintSignal"));  // NOI18N
                loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());

                for (Conditional.Type type : Conditional.Type.getSignalHeadStateMachineItems()) {
                    _variableStateBox.addItem(type);
                }

                setVariableNameBox(itemType);
                if (testType == Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS) {
                    makeDetailGrid("SignalAspectVariable");  // NOI18N
                } else {
                    makeDetailGrid("StandardVariable");  // NOI18N
                }

                _variableNameField.addActionListener(variableSignalHeadNameListener);
                _variableStateBox.addActionListener(variableSignalTestStateListener);
                break;

            case SIGNALMAST:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintSignalMast"));  // NOI18N
                _variableNameField.addActionListener(variableSignalMastNameListener);
                _variableStateBox.addActionListener(variableSignalTestStateListener);
                loadJComboBoxWithMastAspects(_variableSignalBox, _variableNameField.getText().trim());

                for (Conditional.Type type : Conditional.Type.getSignalMastItems()) {
                    _variableStateBox.addItem(type);
                }
                setVariableNameBox(itemType);
                if (testType == Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS) {
                    makeDetailGrid("SignalAspectVariable");  // NOI18N
                } else {
                    makeDetailGrid("StandardVariable");  // NOI18N
                }
                break;

            case MEMORY:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintMemory"));  // NOI18N
                setVariableNameBox(itemType);
                makeDetailGrid("MemoryVariable");  // NOI18N
                compareTypeChanged(testType);   // Force the label update
                break;

            case CONDITIONAL:
                for (Conditional.Type type : Conditional.Type.getConditionalItems()) {
                    _variableStateBox.addItem(type);
                }
                loadSelectLogixBox();
                makeDetailGrid("ConditionalVariable");  // NOI18N
                _selectLogixBox.addActionListener(selectLogixBoxListener);
                _selectConditionalBox.addActionListener(selectConditionalBoxListener);
                break;

            case WARRANT:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintWarrant"));  // NOI18N
                for (Conditional.Type type : Conditional.Type.getWarrantItems()) {
                    _variableStateBox.addItem(type);
                }
                setVariableNameBox(itemType);
                makeDetailGrid("StandardVariable");  // NOI18N
                break;

            case CLOCK:
                makeDetailGrid("FastClockVariable");  // NOI18N
                break;

            case OBLOCK:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintOBlock"));  // NOI18N
                _variableStateBox.removeAllItems();
                for (Conditional.Type type : Conditional.Type.getOBlockItems()) {
                    _variableStateBox.addItem(type);
                }
//                Iterator<String> names = OBlock.getLocalStatusNames();
//                while (names.hasNext()) {
//                    _variableStateBox.addItem(names.next());
//                }
                setVariableNameBox(itemType);
                makeDetailGrid("StandardVariable");  // NOI18N
                break;

            case ENTRYEXIT:
                _variableNameLabel.setToolTipText(Bundle.getMessage("NameHintEntryExit"));  // NOI18N
                _variableNameField.setText(_curVariable.getName());
                for (Conditional.Type type : Conditional.Type.getEntryExitItems()) {
                    _variableStateBox.addItem(type);
                }
                setVariableNameBox(itemType);
                makeDetailGrid("StandardVariable");  // NOI18N
                break;

            default:
                break;
        }
    }

    /**
     * Update the name combo box selection based on the current contents of the
     * name field. Called by variableItemChanged.
     *
     * @since 4.7.3
     * @param itemType The item type, such as sensor or turnout.
     */
    void setVariableNameBox(Conditional.ItemType itemType) {
        if (_selectionMode != SelectionMode.USECOMBO) {
            return;
        }
        _comboNameBox = createNameBox(itemType);
        if (_comboNameBox == null) {
            return;
        }
        // Select the current entry, add the listener
        _comboNameBox.setSelectedItemByName(_curVariable.getName());
        _comboNameBox.addActionListener(new NameBoxListener(_variableNameField));
        _comboNameBox.addFocusListener(detailFocusEvent);
    }

    // ------------ Variable detail methods ------------

    /**
     * Respond to Cancel variable button
     */
    void cancelEditVariable() {
        if (_newVariableItem) {
            _newVariableItem = false;
            deletePressed();
        }
        cleanUpVariable();
    }

    /**
     * Respond to Update Variable button in the Edit Variable pane.
     */
    void updateVariable() {
        if (!validateVariable()) {
            return;
        }
        _newVariableItem = false;
        _curConditional.setStateVariables(_variableList);

        // Update conditional references
        TreeSet<String> newTargetNames = new TreeSet<>();
        loadReferenceNames(_variableList, newTargetNames);
        updateWhereUsed(_oldTargetNames, newTargetNames, _curNodeName);

        // Update the tree nodeChanged
        _curNode.setText(buildNodeText("Variable", _curVariable, _curNodeRow));  // NOI18N
        _cdlModel.nodeChanged(_curNode);
        cleanUpVariable();
    }

    /**
     * Clean up: Cancel, Update and Delete Variable buttons.
     */
    void cleanUpVariable() {
        if (_logicType != Conditional.AntecedentOperator.MIXED) {
            setMoveButtons();
        }
    }

    /**
     * Load the Logix selection box. Set the selection to the current Logix.
     *
     * @since 4.7.4
     */
    void loadSelectLogixBox() {
        // Get the current Logix name for selecting the current combo box row
        String cdlName = _curVariable.getName();
        String lgxName;
        if (cdlName.length() == 0 || (_curVariable.getType() != Conditional.Type.CONDITIONAL_TRUE
                && _curVariable.getType() != Conditional.Type.CONDITIONAL_FALSE)) {
            // Use the current logix name for "add" state variable
            lgxName = _curLogix.getSystemName();
        } else {
            Logix x = _conditionalManager.getParentLogix(cdlName);
            if (x == null) {
                log.error("Unable to find the Logix for {}, using the current Logix", cdlName);  // NOI18N
                lgxName = _curLogix.getSystemName();
            } else {
                lgxName = x.getSystemName();
            }
        }

        _selectLogixBox.removeAllItems();
        _selectLogixMap.clear();

        String itemKey = "";
        for (Logix lgx : _logixManager.getNamedBeanSet()) {
            String sName = lgx.getSystemName();
            if (sName.equals("SYS")) {  // NOI18N
                // Cannot refer to sensor name groups
                continue;
            }
            String uName = lgx.getUserName();
            String itemName = "";
            if (uName == null || uName.length() < 1) {
                itemName = sName;
            } else {
                itemName = uName + " ( " + sName + " )";
            }
            _selectLogixMap.put(itemName, sName);
            if (lgxName.equals(sName)) {
                itemKey = itemName;
            }
        }

        // Load the combo box
        for (String item : _selectLogixMap.keySet()) {
            _selectLogixBox.addItem(item);
        }

        JComboBoxUtil.setupComboBoxMaxRows(_selectLogixBox);
        _selectLogixBox.setSelectedItem(itemKey);
        loadSelectConditionalBox(lgxName);
    }

    /**
     * Load the Conditional selection box. The first row is a prompt.
     *
     * @since 4.7.4
     * @param logixName The Logix system name for selecting the owned
     *                  Conditionals
     */
    void loadSelectConditionalBox(String logixName) {
        // Get the current Conditional name for selecting the current combo box row
        String cdlName = _curVariable.getName();

        _selectConditionalBox.removeAllItems();
        _selectConditionalList.clear();

        // Create the first row
        String itemKey = Bundle.getMessage("SelectFirstRow");  // NOI18N
        _selectConditionalBox.addItem(itemKey);
        _selectConditionalList.add("-None-");  // NOI18N

        Logix x = _logixManager.getBySystemName(logixName);
        if (x == null) {
            log.error("Logix '{}' not found while building the conditional list", logixName);  // NOI18N
            return;
        }
        if (x.getNumConditionals() == 0) {
            return;
        }
        for (String cName : _conditionalManager.getSystemNameListForLogix(x)) {
            Conditional c = _conditionalManager.getConditional(cName);
            if (_curConditional.getSystemName().equals(c.getSystemName())) {
                // Don't add myself to the list
                continue;
            }
            String uName = c.getUserName();
            String itemName = "";
            if (uName == null || uName.length() < 1) {
                itemName = cName;
            } else {
                itemName = uName + " ( " + cName + " )";
            }
            _selectConditionalBox.addItem(itemName);
            _selectConditionalList.add(cName);
            if (cdlName.equals(cName)) {
                itemKey = itemName;
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_selectConditionalBox);
        _selectConditionalBox.setSelectedItem(itemKey);
    }

    /**
     * Check if Memory type in a Conditional was changed by the user.
     * <p>
     * Update GUI if it has.
     *
     * @param testType One of the four types
     */
    private void compareTypeChanged(Conditional.Type testType) {
        if ((testType == Conditional.Type.MEMORY_COMPARE)
                || (testType == Conditional.Type.MEMORY_COMPARE_INSENSITIVE)) {
            _variableMemoryValueLabel.setText(Bundle.getMessage("LabelMemoryValue"));  // NOI18N
            _variableMemoryValueLabel.setToolTipText(Bundle.getMessage("DataHintMemory"));  // NOI18N
        } else {
            _variableMemoryValueLabel.setText(Bundle.getMessage("LabelLiteralValue"));  // NOI18N
            _variableMemoryValueLabel.setToolTipText(Bundle.getMessage("DataHintValue"));  // NOI18N
        }
    }

    /**
     * Fetch valid localized appearances for a given Signal Head.
     * <p>
     * Warn if head is not found.
     *
     * @param box            the comboBox on the setup pane to fill
     * @param signalHeadName user or system name of the Signal Head
     */
    void loadJComboBoxWithHeadAppearances(JComboBox<String> box, String signalHeadName) {
        box.removeAllItems();
        log.debug("loadJComboBoxWithSignalHeadAppearances called with name: {}", signalHeadName);  // NOI18N
        SignalHead h = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHeadName);
        if (h == null) {
            box.addItem(Bundle.getMessage("PromptLoadHeadName"));  // NOI18N
        } else {
            String[] v = h.getValidStateNames();
            for (int i = 0; i < v.length; i++) {
                box.addItem(v[i]);
            }
            box.setSelectedItem(h.getAppearanceName());
        }
    }

    /**
     * Fetch valid aspects for a given Signal Mast.
     * <p>
     * Warn if mast is not found.
     *
     * @param box      the comboBox on the setup pane to fill
     * @param mastName user or system name of the Signal Mast
     */
    void loadJComboBoxWithMastAspects(JComboBox<String> box, String mastName) {
        box.removeAllItems();
        SignalMast m = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(mastName);
        if (m == null) {
            box.addItem(Bundle.getMessage("PromptLoadMastName"));  // NOI18N
        } else {
            java.util.Vector<String> v = m.getValidAspects();
            for (int i = 0; i < v.size(); i++) {
                box.addItem(v.get(i));
            }
            box.setSelectedItem(m.getAspect());
        }
    }

    // ------------ Variable update processes ------------

    /**
     * Validate Variable data from Edit Variable panel, and transfer it to
     * current variable object as appropriate.
     * <p>
     * Messages are sent to the user for any errors found. This routine returns
     * false immediately after finding the first error, even if there might be
     * more errors.
     *
     * @return true if all data checks out OK, otherwise false
     */
    boolean validateVariable() {
        String name = _variableNameField.getText().trim();
        _variableNameField.setText(name);
        _curVariable.setDataString("");
        _curVariable.setNum1(0);
        _curVariable.setNum2(0);

        updateVariableOperator();
        updateVariableNegation();
        _curVariable.setTriggerActions(_variableTriggerActions.isSelected());

        Conditional.ItemType itemType = _variableItemBox.getItemAt(_variableItemBox.getSelectedIndex());
        Conditional.Type testType = Conditional.Type.NONE;
        switch (itemType) {
            case SENSOR:
            case TURNOUT:
            case LIGHT:
            case SIGNALHEAD:
            case SIGNALMAST:
            case CONDITIONAL:
            case WARRANT:
            case ENTRYEXIT:
                testType = _variableStateBox.getItemAt(_variableStateBox.getSelectedIndex());
                break;
            case MEMORY:
                testType = _variableCompareTypeBox.getItemAt(_variableCompareTypeBox.getSelectedIndex());
                break;
            case CLOCK:
                testType = Conditional.Type.FAST_CLOCK_RANGE;
                break;
            case OBLOCK:
                testType = Conditional.Type.BLOCK_STATUS_EQUALS;
                break;
            default:
                JOptionPane.showMessageDialog(_editLogixFrame,
                        Bundle.getMessage("ErrorVariableType"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
        }
        _curVariable.setType(testType);
        log.debug("validateVariable: itemType= {}, testType= {}", itemType, testType);  // NOI18N
        switch (itemType) {
            case SENSOR:
                name = validateSensorReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case TURNOUT:
                name = validateTurnoutReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case CONDITIONAL:
                name = validateConditionalReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                Conditional c = _conditionalManager.getBySystemName(name);
                if (c == null) {
                    return false;
                }
                String uName = c.getUserName();
                if (uName == null || uName.isEmpty()) {
                    _curVariable.setGuiName(c.getSystemName());
                } else {
                    _curVariable.setGuiName(uName);
                }
                break;
            case LIGHT:
                name = validateLightReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case MEMORY:
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                String name2 = _variableData1Field.getText();
                if ((testType == Conditional.Type.MEMORY_COMPARE)
                        || (testType == Conditional.Type.MEMORY_COMPARE_INSENSITIVE)) {
                    name2 = validateMemoryReference(name2);
                    if (name2 == null) {
                        return false;
                    }
                }
                _curVariable.setDataString(name2);
                _curVariable.setNum1(_variableCompareOpBox.getSelectedIndex() + 1);
                break;
            case CLOCK:
                int beginTime = parseTime(_variableData1Field.getText());
                if (beginTime < 0) {
                    // parse error occurred - message has been sent
                    return (false);
                }
                int endTime = parseTime(_variableData2Field.getText());
                if (endTime < 0) {
                    return (false);
                }
                // set beginning and end time (minutes since midnight)
                _curVariable.setNum1(beginTime);
                _curVariable.setNum2(endTime);
                name = "Clock";  // NOI18N
                break;
            case SIGNALHEAD:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                if (testType == Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS) {
                    String appStr = (String) _variableSignalBox.getSelectedItem();
                    if (appStr != null) {
                        Conditional.Type type = ConditionalVariable.stringToVariableTest(appStr);
                        if (type == Conditional.Type.ERROR) {
                            JOptionPane.showMessageDialog(_editLogixFrame, Bundle.getMessage("ErrorAppearance"), Bundle.getMessage("ErrorTitle"), // NOI18N
                                    JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        _curVariable.setType(type);
                        _curVariable.setDataString(appStr);
                        log.debug("SignalHead \"{}\"of type '{}' _variableSignalBox.getSelectedItem()= {}", name, testType, _variableSignalBox.getSelectedItem()); // NOI18N
                    } else {
                        log.warn("null selection in _variableSignalBox");
                    }
                }
                break;
            case SIGNALMAST:
                name = validateSignalMastReference(name);
                if (name == null) {
                    return false;
                }
                if (testType == Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS) {
                    if (_variableSignalBox.getSelectedIndex() < 0) {
                        JOptionPane.showMessageDialog(_editLogixFrame,
                                Bundle.getMessage("ErrorAspect"), Bundle.getMessage("ErrorTitle"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    // save the selected aspect for comparison
                    _curVariable.setDataString((String) _variableSignalBox.getSelectedItem());
                    //                _curVariable.setType(ConditionalVariable.stringToVariableTest(appStr));
                }
                break;
            case WARRANT:
                name = validateWarrantReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case OBLOCK:
                name = validateOBlockReference(name);
                if (name == null) {
                    return false;
                }
                String stri18n = (String) _variableStateBox.getSelectedItem();
                if (stri18n != null) {
                    _curVariable.setDataString(OBlock.getSystemStatusName(stri18n));
                    log.debug("OBlock \"{}\"of type '{}' _variableSignalBox.getSelectedItem()= {}", name, testType, _variableSignalBox.getSelectedItem()); // NOI18N
                }
                break;
            case ENTRYEXIT:
                name = validateEntryExitReference(name);
                if (name == null) {
                    return false;
                }
                break;
            default:
                JOptionPane.showMessageDialog(_editLogixFrame,
                        Bundle.getMessage("ErrorVariableType"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
        }
        _curVariable.setName(name);
        boolean result = _curVariable.evaluate();
        log.debug("State Variable \"{}\" of type '{}' state= {} type= {}",
                name, testType.getTestTypeString(),
                result, _curVariable.getType());  // NOI18N
        if (_curVariable.getType() == Conditional.Type.NONE) {
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("ErrorVariableState"), Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return (true);
    }

    /**
     * Update the variable operation. If a change has occurred, also update the
     * antecedent and antecedent tree node.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Except for the root node, all nodes are ConditionalTreeNode")  // NOI18N
    void updateVariableOperator() {
        Operator oldOper = _curVariable.getOpern();
        if (_curNodeRow > 0) {
            if (_variableOperBox.getSelectedIndex() == 0) {
                _curVariable.setOpern(Conditional.Operator.AND);
            } else {
                _curVariable.setOpern(Conditional.Operator.OR);
            }
        } else {
            _curVariable.setOpern(Conditional.Operator.NONE);
        }
        if (_curVariable.getOpern() != oldOper) {
            makeAntecedent();
            _curConditional.setLogicType(_logicType, _antecedent); // non-localized
            ConditionalTreeNode antLeaf = (ConditionalTreeNode) ((ConditionalTreeNode) _curNode.getParent()).getPreviousSibling();
            antLeaf.setText(buildNodeText("Antecedent", _curConditional, 0));  // NOI18N
            _cdlModel.nodeChanged(antLeaf);
        }
    }

    /**
     * Update the variable negation. If a change has occurred, also update the
     * antecedent and antecedent tree node.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "Except for the root node, all nodes are ConditionalTreeNode")  // NOI18N
    void updateVariableNegation() {
        boolean state = _curVariable.isNegated();
        if (_variableNegated.isSelected()) {
            _curVariable.setNegation(true);
        } else {
            _curVariable.setNegation(false);
        }
        if (_curVariable.isNegated() != state) {
            makeAntecedent();
            _curConditional.setLogicType(_logicType, _antecedent); // non-localized
            ConditionalTreeNode antLeaf = (ConditionalTreeNode) ((ConditionalTreeNode) _curNode.getParent()).getPreviousSibling();
            antLeaf.setText(buildNodeText("Antecedent", _curConditional, 0));
            _cdlModel.nodeChanged(antLeaf);
        }
    }

    /**
     * Update the conditional variable list and refresh the local copy.
     * The parent Logix is de-activated and re-activated. This ensures
     * that listeners are properly handled.
     * @since 4.11.2
     */
    void updateVariableList() {
        _curLogix.deActivateLogix();
        _curConditional.setStateVariables(_variableList);
        _variableList = _curConditional.getCopyOfStateVariables();
        _curLogix.activateLogix();
    }

    // ------------ Variable detail listeners ------------

    transient ActionListener variableSignalHeadNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal head name changes, but only
            // while in signal head mode
            log.debug("variableSignalHeadNameListener fires; _variableNameField : {}", _variableNameField.getText().trim());  // NOI18N
            loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());
        }
    };

    transient ActionListener variableSignalMastNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal mast name changes, but only
            // while in signal mast mode
            log.debug("variableSignalMastNameListener fires; _variableNameField : {}", _variableNameField.getText().trim());  // NOI18N
            loadJComboBoxWithMastAspects(_variableSignalBox, _variableNameField.getText().trim());
        }
    };

    transient ActionListener variableSignalTestStateListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.debug("variableSignalTestStateListener fires; _variableItemBox.getSelectedIndex()= \"{}\" _variableStateBox.getSelectedIndex()= \"{}\"", // NOI18N
                    _variableItemBox.getSelectedIndex(),
                    _variableStateBox.getSelectedIndex());

            Conditional.ItemType itemType = _variableItemBox.getItemAt(_variableItemBox.getSelectedIndex());

            if (_variableStateBox.getSelectedIndex() == 1) {
                if (itemType == Conditional.ItemType.SIGNALHEAD) {
                    loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());
                    _detailGrid.setVisible(false);
                    makeDetailGrid("SignalAspectVariable");  // NOI18N
                } else if (itemType == Conditional.ItemType.SIGNALMAST) {
                    loadJComboBoxWithMastAspects(_variableSignalBox, _variableNameField.getText().trim());
                    _detailGrid.setVisible(false);
                    makeDetailGrid("SignalAspectVariable");  // NOI18N
                } else {
                    _detailGrid.setVisible(false);
                    makeDetailGrid("StandardVariable");  // NOI18N
                }
            } else {
                _detailGrid.setVisible(false);
                makeDetailGrid("StandardVariable");  // NOI18N
            }
        }
    };

    transient ActionListener selectLogixBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String lgxItem = (String) _selectLogixBox.getSelectedItem();
            if (lgxItem != null) {
                String lgxName = _selectLogixMap.get(lgxItem);
                if (lgxName != null) {
                    loadSelectConditionalBox(lgxName);
                }
            }
        }
    };

    transient ActionListener selectConditionalBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int cdlIndex = _selectConditionalBox.getSelectedIndex();
            if (cdlIndex > 0 && cdlIndex < _selectConditionalList.size()) {
                String cdlName = _selectConditionalList.get(cdlIndex);
                _variableNameField.setText(cdlName);
            }
        }
    };

    transient ActionListener compareTypeBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selection = _variableCompareTypeBox.getSelectedIndex();
            compareTypeChanged(Conditional.Type.getMemoryItems().get(selection));
        }
    };

    // ============ Edit Action Section ============

    /**
     * Called once during class initialization to define the GUI objects. Where
     * possible, the combo box content is loaded.
     */
    void buildActionComponents() {
        // Item Type
        _actionItemBox = new JComboBox<>();
        for (Conditional.ItemType itemType : Conditional.ItemType.values()) {
            _actionItemBox.addItem(itemType);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_actionItemBox);
        _actionItemBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Conditional.ItemType newActionItem =
                        _actionItemBox.getItemAt(_actionItemBox.getSelectedIndex());
                if (log.isDebugEnabled()) {
                    log.debug("_actionItemBox Listener: new = {}, curr = {}, row = {}",  // NOI18N
                            newActionItem, _curActionItem, _curNodeRow);
                }
                if (newActionItem != _curActionItem) {
                    if (_curNodeRow >= 0) {
                        _curAction = new DefaultConditionalAction();
                        _actionList.set(_curNodeRow, _curAction);
                    }
                    _curActionItem = newActionItem;
                }
                actionItemChanged(newActionItem);
            }
        });

        // Item Name
        _actionNameField = new JTextField(20);

        // Action Type Box
        _actionTypeBox = new JComboBox<>();
        _actionTypeBox.addItem(Conditional.Action.NONE);

        // Action State Box
        _actionBox = new JComboBox<String>();
        _actionBox.addItem("");

        // Short strings: Delay time, memory value/copy target
        _shortActionString = new JTextField(15);

        // On Change / Trigger options
        _actionOptionBox = new JComboBox<String>();

        // File Selector
        _actionSetButton = new JButton("..."); // "File" replaced by ...
        _actionSetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateAction();
                setFileLocation(e);
            }
        });

        // File names, etc.
        _longActionString = new JTextField(30);
    }

    // ------------ Make Action Edit Grid Panels ------------

    /**
     * Create a one row grid with just the Action Item box. This is the base for
     * larger grids as well as the initial grid for new Actions.
     *
     * @param c the constraints object used for the grid construction
     */
    void makeEmptyActionGrid(GridBagConstraints c) {
        // Action item box
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row0Label = new JLabel(Bundle.getMessage("LabelActionItem"));  // NOI18N
        row0Label.setToolTipText(Bundle.getMessage("ActionItemHint"));  // NOI18N
        _gridPanel.add(row0Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_actionItemBox, c);
    }

    /**
     * Create the standard Name and Type rows.
     * The name field will be either a text field or a combo box.
     * The name field label is a variable to support run time changes.
     *
     * @param c The constraints object used for the grid construction
     * @param finalRow Controls whether the tigger combo box is included
     */
    void makeNameTypeActionGrid(GridBagConstraints c, boolean finalRow) {
        makeEmptyActionGrid(c);

        Conditional.Action actionType = _curAction.getType();
        Conditional.ItemType itemType = actionType.getItemType();

        // Name Field
        c.gridy = 1;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        _gridPanel.add(_actionNameLabel, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        if ((_selectionMode == SelectionMode.USECOMBO)
                && (itemType != Conditional.ItemType.AUDIO)) {
            _gridPanel.add(_comboNameBox, c);
        } else {
            _gridPanel.add(_actionNameField, c);
        }

        // Action Type Box
        c.gridy = 2;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row5Label = new JLabel(Bundle.getMessage("LabelActionType"));  // NOI18N
        row5Label.setToolTipText(Bundle.getMessage("ActionTypeHint"));  // NOI18N
        _gridPanel.add(row5Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_actionTypeBox, c);

        if (itemType == Conditional.ItemType.NONE) {
            // Skip the change/trigger section for new Actions
            return;
        }

        if (finalRow) {
            makeChangeTriggerActionGrid(c);
        }
    }

    /**
     * Create the standard Type row.
     *
     * @param c The constraints object used for the grid construction
     * @param finalRow Controls whether the tigger combo box is included
     */
    void makeTypeActionGrid(GridBagConstraints c, boolean finalRow) {
        makeEmptyActionGrid(c);

        Conditional.Action actionType = _curAction.getType();
        Conditional.ItemType itemType = actionType.getItemType();

        // Action Type Box
        c.gridy = 1;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row5Label = new JLabel(Bundle.getMessage("LabelActionType"));  // NOI18N
        row5Label.setToolTipText(Bundle.getMessage("ActionTypeHint"));  // NOI18N
        _gridPanel.add(row5Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_actionTypeBox, c);

        if (itemType == Conditional.ItemType.NONE) {
            // Skip the change/trigger section for new Actions
            return;
        }

        if (finalRow) {
            makeChangeTriggerActionGrid(c);
        }
    }

    /**
     * Add the action box to the grid.
     *
     * @param c        The constraints object used for the grid construction
     * @param finalRow Controls whether the tigger combo box is included
     */
    void makeStandardActionGrid(GridBagConstraints c, boolean finalRow) {
        makeNameTypeActionGrid(c, false);

        // Action Box
        c.gridy = 3;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        _gridPanel.add(_actionBoxLabel, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_actionBox, c);

        if (finalRow) {
            makeChangeTriggerActionGrid(c);
        }
    }

    /**
     * Add the short name field to the grid.
     *
     * @param c          The constraints object used for the grid construction
     * @param includeBox Controls whether the normal action type combo box is
     *                   included
     */
    void makeShortFieldActionGrid(GridBagConstraints c, boolean includeBox) {
        if (includeBox) {
            makeStandardActionGrid(c, false);
        } else {
            makeNameTypeActionGrid(c, false);
        }

        // Add the short text field
        c.gridy = 4;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        _gridPanel.add(_shortActionLabel, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_shortActionString, c);

        makeChangeTriggerActionGrid(c);
    }

    /**
     * Just a short text field, no name field. Used by set clock and jython
     * command.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeTypeShortActionGrid(GridBagConstraints c) {
        makeTypeActionGrid(c, false);

        // Add the short text field
        c.gridy = 2;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        _gridPanel.add(_shortActionLabel, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_shortActionString, c);

        makeChangeTriggerActionGrid(c);
    }

    /**
     * Add the file selection components.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeFileActionGrid(GridBagConstraints c) {
        makeTypeActionGrid(c, false);

        // Add the file selecttor
        c.gridy = 2;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        _gridPanel.add(_shortActionLabel, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_actionSetButton, c);

        c.gridwidth = 2;    // span both columns
        // Add the long text field
        c.gridy = 3;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        _gridPanel.add(_longActionString, c);
        c.gridwidth = 1;

        makeChangeTriggerActionGrid(c);
    }

    /**
     * Add the change/trigger box the grid. This is the last item in an Action
     * and is usually called from one of the other entry points.
     *
     * @param c The constraints object used for the grid construction
     */
    void makeChangeTriggerActionGrid(GridBagConstraints c) {
        // Action item box
        c.gridy = 9;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel row0Label = new JLabel(Bundle.getMessage("LabelActionOption"));  // NOI18N
        row0Label.setToolTipText(Bundle.getMessage("ActionOptionHint"));  // NOI18N
        _gridPanel.add(row0Label, c);
        c.gridx = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _gridPanel.add(_actionOptionBox, c);
    }

    // ------------ Main Action methods ------------

    /**
     * Set display to show current action (curAction) parameters.
     */
    void initializeActionVariables() {
        Conditional.Action actionType = _curAction.getType();
        Conditional.ItemType itemType = actionType.getItemType();
        log.debug("initializeActionVariables: itemType= {}, actionType= {}", itemType, actionType);  // NOI18N
        _actionItemBox.setSelectedItem(itemType);
        _actionNameField.setText(_curAction.getDeviceName());
        switch (itemType) {
            case NONE:
                _actionNameField.setText("");
                break;

            case SENSOR:
                _actionTypeBox.setSelectedItem(actionType);
                if ((actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                if (actionType == Conditional.Action.SET_SENSOR
                        || actionType == Conditional.Action.DELAYED_SENSOR
                        || actionType == Conditional.Action.RESET_DELAYED_SENSOR) {
                    if (_curAction.getActionData() == Sensor.ACTIVE) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Sensor.INACTIVE) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                }
                break;

            case TURNOUT:
                _actionTypeBox.setSelectedItem(actionType);
                if ((actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                if ((actionType == Conditional.Action.SET_TURNOUT)
                        || (actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    if (_curAction.getActionData() == Turnout.CLOSED) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Turnout.THROWN) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if (actionType == Conditional.Action.LOCK_TURNOUT) {
                    if (_curAction.getActionData() == Turnout.UNLOCKED) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Turnout.LOCKED) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                }
                break;

            case LIGHT:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.SET_LIGHT) {
                    if (_curAction.getActionData() == Light.ON) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Light.OFF) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if ((actionType == Conditional.Action.SET_LIGHT_INTENSITY)
                        || (actionType == Conditional.Action.SET_LIGHT_TRANSITION_TIME)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;

            case SIGNALHEAD:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.SET_SIGNAL_APPEARANCE) {
                    loadJComboBoxWithHeadAppearances(_actionBox, _actionNameField.getText().trim());
                }
                break;

            case CLOCK:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.SET_FAST_CLOCK_TIME) {
                    int time = _curAction.getActionData();
                    _longActionString.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                    _actionNameField.setText("");
                }
                break;

            case MEMORY:
                _actionTypeBox.setSelectedItem(actionType);
                _shortActionString.setText(_curAction.getActionString());
                break;

            case WARRANT:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.CONTROL_TRAIN) {
                    if (_curAction.getActionData() == Warrant.HALT) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Warrant.RESUME) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Warrant.ABORT) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if (actionType == Conditional.Action.SET_TRAIN_ID
                        || actionType == Conditional.Action.SET_TRAIN_NAME
                        || actionType == Conditional.Action.THROTTLE_FACTOR) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;

            case OBLOCK:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.SET_BLOCK_VALUE) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;

            case ENTRYEXIT:
                _actionNameField.setText(_curAction.getBean().getUserName());
                _actionTypeBox.setSelectedItem(actionType);
                break;

            case AUDIO:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.PLAY_SOUND) {
                    _longActionString.setText(_curAction.getActionString());
                } else if (actionType == Conditional.Action.CONTROL_AUDIO) {
                    switch (_curAction.getActionData()) {
                        case Audio.CMD_PLAY:
                            _actionBox.setSelectedIndex(0);
                            break;
                        case Audio.CMD_STOP:
                            _actionBox.setSelectedIndex(1);
                            break;
                        case Audio.CMD_PLAY_TOGGLE:
                            _actionBox.setSelectedIndex(2);
                            break;
                        case Audio.CMD_PAUSE:
                            _actionBox.setSelectedIndex(3);
                            break;
                        case Audio.CMD_RESUME:
                            _actionBox.setSelectedIndex(4);
                            break;
                        case Audio.CMD_PAUSE_TOGGLE:
                            _actionBox.setSelectedIndex(5);
                            break;
                        case Audio.CMD_REWIND:
                            _actionBox.setSelectedIndex(6);
                            break;
                        case Audio.CMD_FADE_IN:
                            _actionBox.setSelectedIndex(7);
                            break;
                        case Audio.CMD_FADE_OUT:
                            _actionBox.setSelectedIndex(8);
                            break;
                        case Audio.CMD_RESET_POSITION:
                            _actionBox.setSelectedIndex(9);
                            break;
                        default:
                            log.warn("Unexpected _curAction.getActionData() of {}", _curAction.getActionData());  // NOI18N
                            break;
                    }
                }
                break;

            case SCRIPT:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.RUN_SCRIPT) {
                    _longActionString.setText(_curAction.getActionString());
                } else if (actionType == Conditional.Action.JYTHON_COMMAND) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;

            case SIGNALMAST:
            case LOGIX:
            case OTHER: // ACTION_TRIGGER_ROUTE
                _actionTypeBox.setSelectedItem(actionType);
                break;

            default:
                log.error("Unhandled type: {}", itemType);  // NOI18N
                break;
        }
        _actionOptionBox.setSelectedIndex(_curAction.getOption() - 1);
    }

    /**
     * Respond to a change in an Action Type comboBox.
     * <p>
     * Set components visible for the selected type.
     *
     * @param type index of the newly selected Action type
     */
    void actionItemChanged(Conditional.ItemType type) {
        Conditional.Action actionType = _curAction.getType();
        log.debug("actionItemChanged: itemType= {}, actionType= {}", type, actionType);  // NOI18N
        _detailGrid.setVisible(false);
        _actionTypeBox.removeActionListener(_actionTypeListener);
        _shortActionString.setText("");
        _longActionString.setText("");
        _actionTypeBox.removeAllItems();
        _actionTypeBox.addItem(Conditional.Action.NONE);
        _actionBox.removeAllItems();
        Conditional.ItemType itemType = actionType.getItemType();
        if (type != Conditional.ItemType.NONE) {  // actionItem listener choice overrides current item
            itemType = type;
        }
        if (itemType != actionType.getItemType()) {
            actionType = Conditional.Action.NONE;    // chosen item type does not support action type
        }

        _actionNameField.removeActionListener(actionSignalHeadNameListener);
        _actionNameField.removeActionListener(actionSignalMastNameListener);

        if (_comboNameBox != null) {
            for (ActionListener item : _comboNameBox.getActionListeners()) {
                _comboNameBox.removeActionListener(item);
            }
            _comboNameBox.removeFocusListener(detailFocusEvent);
        }
        setPickWindow("Action", itemType);  // NOI18N

        switch (itemType) {
            case NONE:
                makeDetailGrid("EmptyAction");  // NOI18N
                break;

            case TURNOUT:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintTurnout"));  // NOI18N
                String turnoutGrid = "NameTypeAction";  // NOI18N
                boolean delayTurnout = false;

                for (Conditional.Action action : Conditional.Action.getTurnoutItems()) {
                    _actionTypeBox.addItem(action);
                }

                if ((actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    delayTurnout = true;
                    _shortActionLabel.setText(Bundle.getMessage("LabelDelayTime"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintDelayedTurnout"));  // NOI18N
                }
                if ((actionType == Conditional.Action.SET_TURNOUT)
                        || (actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    turnoutGrid = (delayTurnout) ? "ShortFieldAction" : "StandardAction";  // NOI18N
                    _actionBoxLabel.setText(Bundle.getMessage("LabelActionTurnout"));  // NOI18N
                    _actionBoxLabel.setToolTipText(Bundle.getMessage("TurnoutSetHint"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("TurnoutStateClosed"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("TurnoutStateThrown"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("Toggle"));  // NOI18N
                } else if (actionType == Conditional.Action.LOCK_TURNOUT) {
                    turnoutGrid = (delayTurnout) ? "ShortFieldAction" : "StandardAction";  // NOI18N
                    _actionBoxLabel.setText(Bundle.getMessage("LabelActionLock"));  // NOI18N
                    _actionBoxLabel.setToolTipText(Bundle.getMessage("LockSetHint"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("TurnoutUnlock"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("TurnoutLock"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("Toggle"));  // NOI18N
                } else if ((actionType == Conditional.Action.CANCEL_TURNOUT_TIMERS)
                        || (actionType == Conditional.Action.NONE)) {
                    turnoutGrid = "NameTypeActionFinal";  // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(turnoutGrid);
                break;

            case SENSOR:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintSensor"));  // NOI18N
                String sensorGrid = "NameTypeAction";  // NOI18N
                boolean delaySensor = false;

                for (Conditional.Action action : Conditional.Action.getSensorItems()) {
                    _actionTypeBox.addItem(action);
                }
                if ((actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    delaySensor = true;
                    _shortActionLabel.setText(Bundle.getMessage("LabelDelayTime"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintDelayedSensor"));  // NOI18N
                }
                if ((actionType == Conditional.Action.SET_SENSOR)
                        || (actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    sensorGrid = (delaySensor) ? "ShortFieldAction" : "StandardAction";  // NOI18N
                    _actionBoxLabel.setText(Bundle.getMessage("LabelActionSensor"));  // NOI18N
                    _actionBoxLabel.setToolTipText(Bundle.getMessage("SensorSetHint"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("SensorStateActive"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("SensorStateInactive"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("Toggle"));  // NOI18N
                } else if ((actionType == Conditional.Action.CANCEL_SENSOR_TIMERS)
                        || (actionType == Conditional.Action.NONE)) {
                    sensorGrid = "NameTypeActionFinal";  // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(sensorGrid);
                break;

            case SIGNALHEAD:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintSignal"));  // NOI18N
                String signalHeadGrid = "NameTypeAction";  // NOI18N
                _actionNameField.addActionListener(actionSignalHeadNameListener);

                for (Conditional.Action action : Conditional.Action.getSignalHeadItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.SET_SIGNAL_APPEARANCE) {
                    signalHeadGrid = "StandardAction";  // NOI18N
                    _actionBoxLabel.setText(Bundle.getMessage("LabelActionSignal"));  // NOI18N
                    _actionBoxLabel.setToolTipText(Bundle.getMessage("SignalSetHint"));  // NOI18N
                    loadJComboBoxWithHeadAppearances(_actionBox, _actionNameField.getText().trim());
                } else if (actionType != Conditional.Action.NONE) {
                    signalHeadGrid = "NameTypeActionFinal";  // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(signalHeadGrid);
                break;

            case SIGNALMAST:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintSignalMast"));  // NOI18N
                String signalMastGrid = "NameTypeAction";  // NOI18N
                _actionNameField.addActionListener(actionSignalMastNameListener);

                for (Conditional.Action action : Conditional.Action.getSignalMastItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.SET_SIGNALMAST_ASPECT) {
                    signalMastGrid = "StandardAction";  // NOI18N
                    _actionBoxLabel.setText(Bundle.getMessage("LabelSignalAspect"));  // NOI18N
                    _actionBoxLabel.setToolTipText(Bundle.getMessage("SignalMastSetHint"));  // NOI18N
                    loadJComboBoxWithMastAspects(_actionBox, _actionNameField.getText().trim());
                } else if (actionType != Conditional.Action.NONE) {
                    signalMastGrid = "NameTypeActionFinal";  // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(signalMastGrid);
                break;

            case LIGHT:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintLight"));  // NOI18N
                String lightGrid = "NameTypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getLightItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.SET_LIGHT_INTENSITY) {
                    lightGrid = "ShortFieldNoBoxAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelLightIntensity"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintLightIntensity"));  // NOI18N
                } else if (actionType == Conditional.Action.SET_LIGHT_TRANSITION_TIME) {
                    lightGrid = "ShortFieldNoBoxAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelTransitionTime"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintLightTransitionTime"));  // NOI18N
                } else if (actionType == Conditional.Action.SET_LIGHT) {
                    lightGrid = "StandardAction";  // NOI18N
                    _actionBoxLabel.setText(Bundle.getMessage("LabelActionLight"));  // NOI18N
                    _actionBoxLabel.setToolTipText(Bundle.getMessage("LightSetHint"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("LightOn"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("LightOff")); // NOI18N
                    _actionBox.addItem(Bundle.getMessage("Toggle"));   // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(lightGrid);
                break;

            case MEMORY:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintMemory"));  // NOI18N
                String memoryGrid = "NameTypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getMemoryItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.COPY_MEMORY) {
                    memoryGrid = "ShortFieldNoBoxAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelMemoryLocation"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintToMemory"));  // NOI18N
                } else if (actionType == Conditional.Action.SET_MEMORY) {
                    memoryGrid = "ShortFieldNoBoxAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelValue"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintMemory"));  // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(memoryGrid);
                break;

            case CLOCK:
                String clockGrid = "TypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getClockItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.SET_FAST_CLOCK_TIME) {
                    clockGrid = "TypeShortAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelSetTime"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintTime"));  // NOI18N
                } else if ((actionType == Conditional.Action.START_FAST_CLOCK)
                        || (actionType == Conditional.Action.STOP_FAST_CLOCK)) {
                    clockGrid = "TypeActionFinal";  // NOI18N
                }

                makeDetailGrid(clockGrid);
                break;

            case LOGIX:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintLogix"));  // NOI18N
                String logixGrid = "NameTypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getLogixItems()) {
                    _actionTypeBox.addItem(action);
                }

                if ((actionType == Conditional.Action.ENABLE_LOGIX)
                        || (actionType == Conditional.Action.DISABLE_LOGIX)) {
                    logixGrid = "NameTypeActionFinal";  // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(logixGrid);
                break;

            case WARRANT:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintWarrant"));  // NOI18N
                String warrantGrid = "NameTypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getWarrantItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.CONTROL_TRAIN) {
                    warrantGrid = "StandardAction";  // NOI18N
                    _actionBoxLabel.setText(Bundle.getMessage("LabelControlTrain"));  // NOI18N
                    _actionBoxLabel.setToolTipText(Bundle.getMessage("DataHintTrainControl"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("WarrantHalt"));   // NOI18N
                    _actionBox.addItem(Bundle.getMessage("WarrantResume")); // NOI18N
                    _actionBox.addItem(Bundle.getMessage("WarrantAbort"));  // NOI18N
                } else if (actionType == Conditional.Action.SET_TRAIN_ID
                        || actionType == Conditional.Action.SET_TRAIN_NAME
                        || actionType == Conditional.Action.THROTTLE_FACTOR) {
                    warrantGrid = "ShortFieldNoBoxAction";  // NOI18N
                    if (actionType == Conditional.Action.SET_TRAIN_ID) {
                        _shortActionLabel.setText(Bundle.getMessage("LabelTrainId"));  // NOI18N
                        _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintTrainId"));  // NOI18N
                    } else if (actionType == Conditional.Action.SET_TRAIN_NAME) {
                        _shortActionLabel.setText(Bundle.getMessage("LabelTrainName"));  // NOI18N
                        _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintTrainName"));  // NOI18N
                    } else { // must be Conditional.Action.THROTTLE_FACTOR, so treat as such
                        _shortActionLabel.setText(Bundle.getMessage("LabelThrottleFactor"));  // NOI18N
                        _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintThrottleFactor"));  // NOI18N
                    }
                }

                setActionNameBox(itemType);
                makeDetailGrid(warrantGrid);
                break;

            case OBLOCK:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintOBlock"));  // NOI18N
                String oblockGrid = "NameTypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getOBlockItems()) {
                    _actionTypeBox.addItem(action);
                }
                if (actionType == Conditional.Action.SET_BLOCK_VALUE) {
                    oblockGrid = "ShortFieldNoBoxAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelBlockValue"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("DataHintBlockValue"));  // NOI18N
                } else if ((actionType == Conditional.Action.DEALLOCATE_BLOCK)
                        || (actionType == Conditional.Action.SET_BLOCK_ERROR)
                        || (actionType == Conditional.Action.CLEAR_BLOCK_ERROR)
                        || (actionType == Conditional.Action.SET_BLOCK_OUT_OF_SERVICE)
                        || (actionType == Conditional.Action.SET_BLOCK_IN_SERVICE)) {
                    oblockGrid = "NameTypeActionFinal";  // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(oblockGrid);
                break;

            case ENTRYEXIT:
                for (Conditional.Action action : Conditional.Action.getEntryExitItems()) {
                    _actionTypeBox.addItem(action);
                }
                setActionNameBox(itemType);
                makeDetailGrid("NameTypeActionFinal");
                break;

            case AUDIO:
                _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintOBlock"));  // NOI18N
                String audioGrid = "TypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getAudioItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.PLAY_SOUND) {
                    audioGrid = "FileAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelSelectFile"));  // NOI18N
                    _actionSetButton.setToolTipText(Bundle.getMessage("SetHintSound"));  // NOI18N
                } else if (actionType == Conditional.Action.CONTROL_AUDIO) {
                    audioGrid = "StandardAction";  // NOI18N
                    _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintAudio"));  // NOI18N
                    _actionBoxLabel.setText(Bundle.getMessage("LabelActionAudio"));  // NOI18N
                    _actionBoxLabel.setToolTipText(Bundle.getMessage("SetHintAudio"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourcePlay"));        // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceStop"));        // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourcePlayToggle"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourcePause"));       // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceResume"));      // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourcePauseToggle")); // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceRewind"));      // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceFadeIn"));      // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceFadeOut"));     // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioResetPosition"));     // NOI18N
                }

                makeDetailGrid(audioGrid);
                break;

            case SCRIPT:
                String scriptGrid = "TypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getScriptItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.RUN_SCRIPT) {
                    scriptGrid = "FileAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelSelectFile"));  // NOI18N
                    _actionSetButton.setToolTipText(Bundle.getMessage("SetHintScript"));  // NOI18N
                } else if (actionType == Conditional.Action.JYTHON_COMMAND) {
                    scriptGrid = "TypeShortAction";  // NOI18N
                    _shortActionLabel.setText(Bundle.getMessage("LabelScriptCommand"));  // NOI18N
                    _shortActionLabel.setToolTipText(Bundle.getMessage("SetHintJythonCmd"));  // NOI18N
                }

                makeDetailGrid(scriptGrid);
                break;

            case OTHER:
                String otherGrid = "TypeAction";  // NOI18N

                for (Conditional.Action action : Conditional.Action.getOtherItems()) {
                    _actionTypeBox.addItem(action);
                }

                if (actionType == Conditional.Action.TRIGGER_ROUTE) {
                    otherGrid = "NameTypeActionFinal";  // NOI18N
                    _actionNameLabel.setToolTipText(Bundle.getMessage("NameHintRoute"));  // NOI18N
                }

                setActionNameBox(itemType);
                makeDetailGrid(otherGrid);
                break;

            default:
                break;
        }
        _actionTypeListener.setItemType(itemType);
        _actionTypeBox.addActionListener(_actionTypeListener);
    }

    /**
     * Update the name combo box selection based on the current contents of the
     * name field.
     *
     * @since 4.7.3
     * @param itemType The item type, such as sensor or turnout.
     */
    void setActionNameBox(Conditional.ItemType itemType) {
        if (_selectionMode != SelectionMode.USECOMBO) {
            return;
        }
        _comboNameBox = createNameBox(itemType);
        if (_comboNameBox == null) {
            return;
        }
        // Select the current entry
        _comboNameBox.setSelectedItemByName(_curAction.getDeviceName());
        _comboNameBox.addActionListener(new NameBoxListener(_actionNameField));
        _comboNameBox.addFocusListener(detailFocusEvent);
    }

    // ------------ Action detail methods ------------

    /**
     * Respond to Cancel action button and window closer of the Edit Action
     * window.
     * <p>
     * Also does cleanup of Update and Delete buttons.
     */
    void cancelEditAction() {
        if (_newActionItem) {
            _newActionItem = false;
            deletePressed();
        }
        cleanUpAction();
    }

    /**
     * Respond to Update button.
     */
    void updateAction() {
        if (!validateAction()) {
            return;
        }
        _newActionItem = false;
        updateActionList();

        // Update the Action node
        _curNode.setText(_curAction.description(_triggerMode));
        _cdlModel.nodeChanged(_curNode);
        cleanUpAction();
    }

    /**
     * Clean up Update and Delete Action buttons.
     */
    void cleanUpAction() {
        setMoveButtons();
    }

    /*.*
     * Convert user setting in Conditional Action configuration pane to integer
     * for processing.
     *
     * @param itemType            value for current item type
     * @param actionTypeSelection index of selected item in configuration
     *                            comboBox
     * @return integer representing the selected action
     */
/*    static Conditional.Action getActionTypeFromBox(Conditional.ItemType itemType, int actionTypeSelection) {
//        if (itemType < 0 || actionTypeSelection < 0) {
//            return Conditional.Action.NONE;
//        }
        if (actionTypeSelection < 0) {
            return Conditional.Action.NONE;
        }
        switch (itemType) {
            case SENSOR:
                return Conditional.Action.getSensorItems().get(actionTypeSelection);
            case TURNOUT:
                return Conditional.Action.getTurnoutItems().get(actionTypeSelection);
            case LIGHT:
                return Conditional.Action.getLightItems().get(actionTypeSelection);
            case SIGNALHEAD:
                return Conditional.Action.getSignalHeadItems().get(actionTypeSelection);
            case SIGNALMAST:
                return Conditional.Action.getSignalMastItems().get(actionTypeSelection);
            case MEMORY:
                return Conditional.Action.getMemoryItems().get(actionTypeSelection);
            case LOGIX:
                return Conditional.Action.getLogixItems().get(actionTypeSelection);
            case WARRANT:
                return Conditional.Action.getWarrantItems().get(actionTypeSelection);
            case OBLOCK:
                return Conditional.Action.getOBlockItems().get(actionTypeSelection);
            case CLOCK:
                return Conditional.Action.getClockItems().get(actionTypeSelection);
            case AUDIO:
                return Conditional.Action.getAudioItems().get(actionTypeSelection);
            case SCRIPT:
                return Conditional.Action.getScriptItems().get(actionTypeSelection);
            case OTHER:
                return Conditional.Action.getOtherItems().get(actionTypeSelection);
            case ENTRYEXIT:
                return Conditional.Action.getEntryExitItems().get(actionTypeSelection);
            default:
                // fall through
                break;
        }
        return Conditional.Action.NONE;
    }
*/
    JFileChooser sndFileChooser = null;
    JFileChooser scriptFileChooser = null;
    JFileChooser defaultFileChooser = null;

    /**
     * Respond to the [...] button in the Edit Action window action section.
     * <p>
     * Ask user to select an audio or python script file on disk.
     *
     * @param e the event heard
     */
    void setFileLocation(ActionEvent e) {
        ConditionalAction action = _actionList.get(_curNodeRow);
        JFileChooser currentChooser;
        Conditional.Action actionType = action.getType();
        if (actionType == Conditional.Action.PLAY_SOUND) {
            if (sndFileChooser == null) {
                sndFileChooser = new JFileChooser(System.getProperty("user.dir") // NOI18N
                        + java.io.File.separator + "resources" // NOI18N
                        + java.io.File.separator + "sounds");  // NOI18N
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("wav sound files");  // NOI18N
                filt.addExtension("wav");  // NOI18N
                sndFileChooser.setFileFilter(filt);
            }
            currentChooser = sndFileChooser;
        } else if (actionType == Conditional.Action.RUN_SCRIPT) {
            if (scriptFileChooser == null) {
                scriptFileChooser = new JFileChooser(FileUtil.getScriptsPath());
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Python script files");  // NOI18N
                filt.addExtension("py");
                scriptFileChooser.setFileFilter(filt);
            }
            currentChooser = scriptFileChooser;
        } else {
            log.warn("Unexpected actionType[{}] = {}", actionType.name(), actionType);  // NOI18N
            if (defaultFileChooser == null) {
                defaultFileChooser = new JFileChooser(FileUtil.getUserFilesPath());
                defaultFileChooser.setFileFilter(new jmri.util.NoArchiveFileFilter());
            }
            currentChooser = defaultFileChooser;
        }

        currentChooser.rescanCurrentDirectory();
        int retVal = currentChooser.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            // set selected file location in data string
            try {
                _longActionString.setText(FileUtil.getPortableFilename(currentChooser.getSelectedFile().getCanonicalPath()));
            } catch (java.io.IOException ex) {
                if (log.isDebugEnabled()) {
                    log.error("exception setting file location: ", ex);  // NOI18N
                }
                _longActionString.setText("");
            }
        }
    }

    // ------------ Action update processes ------------

    /**
     * Validate Action data from Edit Action Window, and transfer it to current
     * action object as appropriate.
     * <p>
     * Messages are sent to the user for any errors found. This routine returns
     * false immediately after finding an error, even if there might be more
     * errors.
     *
     * @return true if all data checks out OK, otherwise false.
     */
    boolean validateAction() {
        Conditional.ItemType itemType = _actionItemBox.getItemAt(_actionItemBox.getSelectedIndex());
        Conditional.Action actionType = Conditional.Action.NONE;
        Conditional.Action selection = _actionTypeBox.getItemAt(_actionTypeBox.getSelectedIndex());
        if (selection == Conditional.Action.NONE) {
            JOptionPane.showMessageDialog(
                    _editLogixFrame, Bundle.getMessage("makeSelection"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String name = _actionNameField.getText().trim();
        String actionString = _shortActionString.getText().trim();
        _curAction.setActionString("");
        _curAction.setActionData(-1);
        boolean referenceByMemory = false;
        if (name.length() > 0 && name.charAt(0) == '@') {
            String memName = name.substring(1);
            if (!confirmIndirectMemory(memName)) {
                return false;
            }
            memName = validateMemoryReference(memName);
            if (memName == null) {
                return false;
            }
            referenceByMemory = true;
        }
        switch (itemType) {
            case SENSOR:
                if (!referenceByMemory) {
                    name = validateSensorReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if ((actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                }
                if ((actionType == Conditional.Action.SET_SENSOR)
                        || (actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Sensor.ACTIVE);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Sensor.INACTIVE);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case TURNOUT:
                if (!referenceByMemory) {
                    name = validateTurnoutReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if ((actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                }
                if ((actionType == Conditional.Action.SET_TURNOUT)
                        || (actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Turnout.CLOSED);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Turnout.THROWN);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                } else if (actionType == Conditional.Action.LOCK_TURNOUT) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Turnout.UNLOCKED);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Turnout.LOCKED);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case LIGHT:
                if (!referenceByMemory) {
                    name = validateLightReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if (actionType == Conditional.Action.SET_LIGHT_INTENSITY) {
                    Light lgtx = getLight(name);
                    // check if light user name was entered
                    if (lgtx == null) {
                        return false;
                    }
                    if (!lgtx.isIntensityVariable()) {
                        JOptionPane.showMessageDialog(_editLogixFrame,
                                Bundle.getMessage("Error45", name), // NOI18N
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);  // NOI18N
                        return (false);
                    }
                    if (!validateIntensityReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                } else if (actionType == Conditional.Action.SET_LIGHT_TRANSITION_TIME) {
                    Light lgtx = getLight(name);
                    // check if light user name was entered
                    if (lgtx == null) {
                        return false;
                    }
                    if (!lgtx.isTransitionAvailable()) {
                        JOptionPane.showMessageDialog(_editLogixFrame,
                                Bundle.getMessage("Error40", name), // NOI18N
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);  // NOI18N
                        return (false);
                    }
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                } else if (actionType == Conditional.Action.SET_LIGHT) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Light.ON);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Light.OFF);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case SIGNALHEAD:
                if (!referenceByMemory) {
                    name = validateSignalHeadReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if (actionType == Conditional.Action.SET_SIGNAL_APPEARANCE) {
                    String appStr = (String) _actionBox.getSelectedItem();
                    _curAction.setActionData(DefaultConditionalAction.stringToActionData(appStr));
                    _curAction.setActionString(appStr);
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case SIGNALMAST:
                if (!referenceByMemory) {
                    name = validateSignalMastReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if (actionType == Conditional.Action.SET_SIGNALMAST_ASPECT) {
                    _curAction.setActionString((String) _actionBox.getSelectedItem());
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case MEMORY:
                if (referenceByMemory) {
                    JOptionPane.showMessageDialog(_editLogixFrame, Bundle.getMessage("Warn6"), Bundle.getMessage("WarningTitle"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                actionType = selection;
                if (actionType == Conditional.Action.COPY_MEMORY) {
                    actionString = validateMemoryReference(actionString);
                    if (actionString == null) {
                        return false;
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                _curAction.setActionString(actionString);
                break;
            case LOGIX:
                if (!referenceByMemory) {
                    name = validateLogixReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case WARRANT:
                if (!referenceByMemory) {
                    name = validateWarrantReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                if (actionType == Conditional.Action.CONTROL_TRAIN) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Warrant.HALT);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Warrant.RESUME);
                    } else {
                        _curAction.setActionData(Warrant.ABORT);
                    }
                } else if (actionType == Conditional.Action.SET_TRAIN_ID
                        || actionType == Conditional.Action.SET_TRAIN_NAME
                        || actionType == Conditional.Action.THROTTLE_FACTOR) {
                    _curAction.setActionString(actionString);
                }
                break;
            case OBLOCK:
                if (!referenceByMemory) {
                    name = validateOBlockReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                if (actionType == Conditional.Action.SET_BLOCK_VALUE) {
                    _curAction.setActionString(actionString);
                }
                break;
            case ENTRYEXIT:
                if (!referenceByMemory) {
                    name = validateEntryExitReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case CLOCK:
                actionType = selection;
                if (actionType == Conditional.Action.SET_FAST_CLOCK_TIME) {
                    int time = parseTime(_shortActionString.getText().trim());
                    if (time < 0) {
                        return (false);
                    }
                    _curAction.setActionData(time);
                }
                break;
            case AUDIO:
                actionType = selection;
                if (actionType == Conditional.Action.PLAY_SOUND) {
                    _curAction.setActionString(_longActionString.getText().trim());
                } else if (actionType == Conditional.Action.CONTROL_AUDIO) {
                    if (!referenceByMemory) {
                        name = validateAudioReference(name);
                        if (name == null) {
                            return false;
                        }
                    }
                    _actionNameField.setText(name);
                    _curAction.setDeviceName(name);
                    switch (_actionBox.getSelectedIndex()) {
                        case 0:
                            _curAction.setActionData(Audio.CMD_PLAY);
                            break;
                        case 1:
                            _curAction.setActionData(Audio.CMD_STOP);
                            break;
                        case 2:
                            _curAction.setActionData(Audio.CMD_PLAY_TOGGLE);
                            break;
                        case 3:
                            _curAction.setActionData(Audio.CMD_PAUSE);
                            break;
                        case 4:
                            _curAction.setActionData(Audio.CMD_RESUME);
                            break;
                        case 5:
                            _curAction.setActionData(Audio.CMD_PAUSE_TOGGLE);
                            break;
                        case 6:
                            _curAction.setActionData(Audio.CMD_REWIND);
                            break;
                        case 7:
                            _curAction.setActionData(Audio.CMD_FADE_IN);
                            break;
                        case 8:
                            _curAction.setActionData(Audio.CMD_FADE_OUT);
                            break;
                        case 9:
                            _curAction.setActionData(Audio.CMD_RESET_POSITION);
                            break;
                        default:
                            log.warn("Unexpected _actionBox.getSelectedIndex() of {}", _actionBox.getSelectedIndex());  // NOI18N
                            break;
                    }
                }
                break;
            case SCRIPT:
                actionType = selection;
                if (actionType == Conditional.Action.RUN_SCRIPT) {
                    _curAction.setActionString(_longActionString.getText().trim());
                } else if (actionType == Conditional.Action.JYTHON_COMMAND) {
                    _curAction.setActionString(_shortActionString.getText().trim());
                }
                break;
            case OTHER:
                actionType = selection;
                if (actionType == Conditional.Action.TRIGGER_ROUTE) {
                    if (!referenceByMemory) {
                        name = validateRouteReference(name);
                        if (name == null) {
                            return false;
                        }
                    }
                    _actionNameField.setText(name);
                    _curAction.setDeviceName(name);
                }
                break;
            default:
                break;
        }
        _curAction.setType(actionType);
        if (actionType != Conditional.Action.NONE) {
            _curAction.setOption(_actionOptionBox.getSelectedIndex() + 1);
        } else {
            _curAction.setOption(0);
        }
        return (true);
    }

    /**
     * Update the conditional action list and refresh the local copy.
     * The parent Logix is de-activated and re-activated.  This ensures
     * that listeners are properly handled, specifically the delayed sensor
     * and turnout options.
     * @since 4.11.2
     */
    void updateActionList() {
        _curLogix.deActivateLogix();
        _curConditional.setAction(_actionList);
        _actionList = _curConditional.getCopyOfActions();
        _curLogix.activateLogix();
    }

    // ------------ Action detail listeners ------------

    /**
     * Listener for _actionTypeBox.
     */
    class ActionTypeListener implements ActionListener {

        Conditional.ItemType _itemType;

        @Override
        public void actionPerformed(ActionEvent e) {
            Conditional.ItemType select1 = _actionItemBox.getItemAt(_actionItemBox.getSelectedIndex());
            Conditional.Action select2 = _actionTypeBox.getItemAt(_actionTypeBox.getSelectedIndex());
            if (log.isDebugEnabled()) {
                log.debug("ActionTypeListener: itemType = {}, local itemType = {}, actionType = {}",  // NOI18N
                        select1, _itemType, select2);
            }
            if (select1 != _itemType) {
                log.error("ActionTypeListener actionItem selection ({}) != expected actionItem ({})",  // NOI18N
                        select1, _itemType);
            }
            if (_curAction != null) {
                if (select1 != Conditional.ItemType.NONE && _itemType == select1) {
                    _curAction.setType(select2);
                    if (select1 == _itemType) {
                        String text = _actionNameField.getText();
                        if (text != null && text.length() > 0) {
                            _curAction.setDeviceName(text);
                        }
                    }
                    actionItemChanged(_itemType);
                    initializeActionVariables();
                }
            }
        }

        public void setItemType(Conditional.ItemType type) {
            _itemType = type;
        }
    }

    ActionTypeListener _actionTypeListener = new ActionTypeListener();

    transient ActionListener actionSignalHeadNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal head name changes, but only
            // while in signal head mode
            log.debug("actionSignalHeadNameListener fires; _actionNameField : {}", _actionNameField.getText().trim());  // NOI18N
            loadJComboBoxWithHeadAppearances(_actionBox, _actionNameField.getText().trim());
        }
    };

    transient ActionListener actionSignalMastNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal mast name changes, but only
            // while in signal mast mode
            log.debug("actionSignalMastNameListener fires; _actionNameField : {}", _actionNameField.getText().trim());  // NOI18N
            loadJComboBoxWithMastAspects(_actionBox, _actionNameField.getText().trim());
        }
    };

    /**
     * Conditional Tree Node Definition.
     */
    static class ConditionalTreeNode extends DefaultMutableTreeNode {

        private String cdlText;
        private String cdlType;
        private String cdlName;
        private int cdlRow;

        public ConditionalTreeNode(String nameText, String type, String sysName, int row) {
            this.cdlText = nameText;
            this.cdlType = type;
            this.cdlName = sysName;
            this.cdlRow = row;
        }

        public String getType() {
            return cdlType;
        }

        public String getName() {
            return cdlName;
        }

        public void setName(String newName) {
            cdlName = newName;
        }

        public int getRow() {
            return cdlRow;
        }

        public void setRow(int newRow) {
            cdlRow = newRow;
        }

        public String getText() {
            return cdlText;
        }

        public void setText(String newText) {
            cdlText = newText;
        }

        @Override
        public String toString() {
            return cdlText;
        }
    }

    @Override
    protected String getClassName() {
        return ConditionalTreeEdit.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalTreeEdit.class);

}
