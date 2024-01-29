package jmri.jmrix.openlcb.swing.stleditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriPanel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.openlcb.MimicNodeStore.ADD_PROP_NODE;
import static org.openlcb.MimicNodeStore.CLEAR_ALL_NODES;
import static org.openlcb.MimicNodeStore.NodeMemo.UPDATE_PROP_SIMPLE_NODE_IDENT;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.swing.*;
import org.openlcb.swing.memconfig.MemConfigDescriptionPane;
import org.openlcb.cdi.impl.ConfigRepresentation;
// import org.openlcb.MimicNodeStore.NodeMemo;


/**
 * Panel for editing STL logic.
 *
 * TODO
 *    Make the node selector full width
 *    Implement CDI connection
 *
 * @author Dave Sand Copyright (C) 2024
 * @since 5.7.x
 */
public class StlEditorPane extends jmri.util.swing.JmriPanel
        implements jmri.jmrix.can.swing.CanPanelInterface {

    private CanSystemConnectionMemo _canMemo;
    private OlcbInterface _iface;
    private ConfigRepresentation _cdi;
    private MimicNodeStore _store;

//     NodeSelector nodeSelector;

    boolean cancelled = false;
    boolean ready = false;
    static boolean _dirty = false;
    int _logicRow = -1;     // The last selected row, -1 for none
    int _groupRow = -1;

    JLabel _statusField;

    JComboBox<NodeEntry> _nodeBox;
    private DefaultComboBoxModel<NodeEntry> _nodeModel = new DefaultComboBoxModel<NodeEntry>();

    JComboBox<Operator> _operators = new JComboBox<>(Operator.values());

    private List<GroupRow> _groupList = new ArrayList<>();
    private List<LogicRow> _logicList = new ArrayList<>();
    private List<InputRow> _inputList = new ArrayList<>();
    private List<OutputRow> _outputList = new ArrayList<>();
    private List<ReceiverRow> _receiverList = new ArrayList<>();
    private List<TransmitterRow> _transmitterList = new ArrayList<>();

    private JTable _groupTable;
    private JTable _logicTable;
    private JTable _inputTable;
    private JTable _outputTable;
    private JTable _receiverTable;
    private JTable _transmitterTable;

    private JScrollPane _logicScrollPane;

    private JTabbedPane _detailTabs;

    private JPanel _editButtons;
    private JButton _addButton;
    private JButton _insertButton;
    private JButton _moveUpButton;
    private JButton _moveDownButton;
    private JButton _deleteButton;
    private JButton _percentButton;
    private JButton _refreshButton;
    private JButton _storeButton;

    private Properties _cdiTest = new Properties();

    // CDI Names
    private static String INPUT_NAME = "Logic_Inputs.Group_I%s(%s).Input_Description";
    private static String INPUT_TRUE = "Logic_Inputs.Group_I%s(%s).True";
    private static String INPUT_FALSE = "Logic_Inputs.Group_I%s(%s).False";
    private static String OUTPUT_NAME = "Logic_Outputs.Group_Q%s(%s).Output_Description";
    private static String OUTPUT_TRUE = "Logic_Outputs.Group_Q%s(%s).True";
    private static String OUTPUT_FALSE = "Logic_Outputs.Group_Q%s(%s).False";
    private static String RECEIVER_NAME = "Track_Receivers.Rx_Circuit(%s).Remote_Mast_Description";
    private static String RECEIVER_EVENT = "Track_Receivers.Rx_Circuit(%s).Link_Address";
    private static String TRANSMITTER_NAME = "Track_Transmitters.Tx_Circuit(%s).Track_Circuit_Description";
    private static String TRANSMITTER_EVeNT = "Track_Transmitters.Tx_Circuit(%s).Link_Address";
    private static String GROUP_NAME = "Conditionals.Logic(%s).Group_Description";
    private static String GROUP_LINE = "Conditionals.Logic(%s).Line_%s";

    // Regex Patterns
    private static Pattern PARSE_VARIABLE = Pattern.compile("[IQYZM](\\d+)\\.(\\d+)");
    private static Pattern PARSE_LABEL = Pattern.compile("\\D\\w{0,3}:");
    private static Pattern PARSE_TIMERWORD = Pattern.compile("W#[0123]#\\d{1,3}");
    private static Pattern PARSE_TIMERVAR = Pattern.compile("T\\d{1,2}");

    public StlEditorPane() {
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        _canMemo = memo;
        _iface = memo.get(OlcbInterface.class);
        _store = memo.get(MimicNodeStore.class);

        // Add to GUI here
        setLayout(new BorderLayout());

        var footer = new JPanel();
        footer.setLayout(new BorderLayout());

        _addButton = new JButton(Bundle.getMessage("ButtonAdd"));
        _insertButton = new JButton(Bundle.getMessage("ButtonInsert"));
        _moveUpButton = new JButton(Bundle.getMessage("ButtonMoveUp"));
        _moveDownButton = new JButton(Bundle.getMessage("ButtonMoveDown"));
        _deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        _percentButton = new JButton("0%");
        _refreshButton = new JButton(Bundle.getMessage("ButtonRefresh"));
        _storeButton = new JButton(Bundle.getMessage("ButtonStore"));

        _percentButton.setEnabled(false);
        _storeButton.setEnabled(false);

        _addButton.addActionListener(this::pushedAddButton);
        _insertButton.addActionListener(this::pushedInsertButton);
        _moveUpButton.addActionListener(this::pushedMoveUpButton);
        _moveDownButton.addActionListener(this::pushedMoveDownButton);
        _deleteButton.addActionListener(this::pushedDeleteButton);
        _percentButton.addActionListener(this::pushedPercentButton);
        _refreshButton.addActionListener(this::pushedRefreshButton);
        _storeButton.addActionListener(this::pushedStoreButton);

        _editButtons = new JPanel();
        _editButtons.add(_addButton);
        _editButtons.add(_insertButton);
        _editButtons.add(_moveUpButton);
        _editButtons.add(_moveDownButton);
        _editButtons.add(_deleteButton);
        footer.add(_editButtons, BorderLayout.WEST);

        var dataButtons = new JPanel();
        dataButtons.add(_percentButton);
        dataButtons.add(_storeButton);
        footer.add(dataButtons, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        // Define the node selector which goes in the header
        var nodeSelector = new JPanel();
        nodeSelector.setLayout(new FlowLayout());
//         header.add(nodeSelector);

        _nodeBox = new JComboBox<NodeEntry>(_nodeModel);

        // Load node selector combo box
        for (MimicNodeStore.NodeMemo nodeMemo : _store.getNodeMemos() ) {
            newNodeInList(nodeMemo);
        }

        _nodeBox.addActionListener(this::nodeSelected);
        JComboBoxUtil.setupComboBoxMaxRows(_nodeBox);


        _nodeBox.revalidate();
//         _nodeBox.repaint();
        nodeSelector.add(_nodeBox);
        add(nodeSelector, BorderLayout.NORTH);

        // Define the center section of the window which consists of 5 tabs
        _detailTabs = new JTabbedPane();

        _detailTabs.add(Bundle.getMessage("ButtonStl"), buildLogicPanel());  // NOI18N
        _detailTabs.add(Bundle.getMessage("ButtonI"), buildInputPanel());  // NOI18N
        _detailTabs.add(Bundle.getMessage("ButtonQ"), buildOutputPanel());  // NOI18N
        _detailTabs.add(Bundle.getMessage("ButtonY"), buildReceiverPanel());  // NOI18N
        _detailTabs.add(Bundle.getMessage("ButtonZ"), buildTransmitterPanel());  // NOI18N

        _detailTabs.addChangeListener(this::tabSelected);

        add(_detailTabs, BorderLayout.CENTER);

        setReady(false);
    }

    // --------------  tab configurations ---------

    private JScrollPane buildGroupPanel() {
        // Create scroll pane
        var model = new GroupModel();
        _groupTable = new JTable(model);
        var scrollPane = new JScrollPane(_groupTable);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _groupTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        var  selectionModel = _groupTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(this::handleGroupRowSelection);

        return scrollPane;
    }

    private JSplitPane buildLogicPanel() {
        // Create scroll pane
        var model = new LogicModel();
        _logicTable = new JTable(model);
        _logicScrollPane = new JScrollPane(_logicTable);

        // resize columns
        for (int i = 0; i < _logicTable.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _logicTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        // Use the operators combo box for the operator column
        var col = _logicTable.getColumnModel().getColumn(1);
        col.setCellEditor(new DefaultCellEditor(_operators));
        JComboBoxUtil.setupComboBoxMaxRows(_operators);

        var  selectionModel = _logicTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(this::handleLogicRowSelection);

        var logicPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildGroupPanel(), _logicScrollPane);
        logicPanel.setDividerSize(10);
        logicPanel.setResizeWeight(.10);

        return logicPanel;
    }

    private JScrollPane buildInputPanel() {
        // Create scroll pane
        var model = new InputModel();
        _inputTable = new JTable(model);
        var scrollPane = new JScrollPane(_inputTable);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _inputTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        return scrollPane;
    }

    private JScrollPane buildOutputPanel() {
        // Create scroll pane
        var model = new OutputModel();
        _outputTable = new JTable(model);
        var scrollPane = new JScrollPane(_outputTable);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _outputTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        return scrollPane;
    }

    private JScrollPane buildReceiverPanel() {
        // Create scroll pane
        var model = new ReceiverModel();
        _receiverTable = new JTable(model);
        var scrollPane = new JScrollPane(_receiverTable);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _receiverTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        return scrollPane;
    }

    private JScrollPane buildTransmitterPanel() {
        // Create scroll pane
        var model = new TransmitterModel();
        _transmitterTable = new JTable(model);
        var scrollPane = new JScrollPane(_transmitterTable);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _transmitterTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        return scrollPane;
    }

    private void tabSelected(ChangeEvent e) {
        if (_detailTabs.getSelectedIndex() == 0) {
            _editButtons.setVisible(true);
        } else {
            _editButtons.setVisible(false);
        }
    }

    // --------------  Logic table methods ---------

    public void handleGroupRowSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            var currentRow = _groupRow;
            _groupRow = _groupTable.getSelectedRow();
            log.info("group {} selected, handle logic change", _groupRow);
            if (currentRow >= 0) {
                // Transfer the working logic list to the group's logic list
                _groupList.get(currentRow).setLogicList(_logicList);
            }

            // Replace the working logic list content with the new group logic list
            _logicList.clear();
            _logicList.addAll(_groupList.get(_groupRow).getLogicList());
            _logicTable.revalidate();
            _logicScrollPane.revalidate();
            _logicScrollPane.repaint();
            pushedPercentButton(null);
        }
    }

    private void pushedPercentButton(ActionEvent e) {
        encode(_groupList.get(_groupRow));
        _percentButton.setText(_groupList.get(_groupRow).getSize());
    }

    public void handleLogicRowSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            _logicRow = _logicTable.getSelectedRow();
            _moveUpButton.setEnabled(_logicRow > 0);
            _moveDownButton.setEnabled(_logicRow < _logicTable.getRowCount() - 1);
        }
    }

    private void pushedAddButton(ActionEvent e) {
        _logicList.add(new LogicRow("", null, "", ""));
        _logicRow = _logicList.size() - 1;
        _logicTable.revalidate();
        _logicTable.setRowSelectionInterval(_logicRow, _logicRow);
        _logicScrollPane.revalidate();
    }

    private void pushedInsertButton(ActionEvent e) {
        if (_logicRow >= 0 && _logicRow < _logicList.size()) {
            _logicList.add(_logicRow, new LogicRow("", null, "", ""));
            _logicTable.revalidate();
            _logicTable.setRowSelectionInterval(_logicRow, _logicRow);
            _logicScrollPane.revalidate();
        }
    }

    private void pushedMoveUpButton(ActionEvent e) {
        if (_logicRow >= 0 && _logicRow < _logicList.size()) {
            var logicRow = _logicList.remove(_logicRow);
            _logicList.add(_logicRow - 1, logicRow);
            _logicRow--;
            _logicTable.revalidate();
            _logicTable.setRowSelectionInterval(_logicRow, _logicRow);
        }
    }

    private void pushedMoveDownButton(ActionEvent e) {
        if (_logicRow >= 0 && _logicRow < _logicList.size()) {
            var logicRow = _logicList.remove(_logicRow);
            _logicList.add(_logicRow + 1, logicRow);
            _logicRow++;
            _logicTable.revalidate();
            _logicTable.setRowSelectionInterval(_logicRow, _logicRow);
        }
    }

    private void pushedDeleteButton(ActionEvent e) {
        if (_logicRow >= 0 && _logicRow < _logicList.size()) {
            _logicList.remove(_logicRow);
            _logicTable.revalidate();
            _logicScrollPane.revalidate();
        }
    }

    // --------------  Encode/Decode methods ---------

    private String nameToVariable(String name) {
        String variable = null;

        if (name != null && !name.isEmpty()) {
            if (!name.contains("~")) {
                // Search input and output tables
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 8; j++) {
                        int row = (i * 8) + j;
                        if (_inputList.get(row).getName().equals(name)) {
                            return "I" + i + "." + j;
                        }
                    }
                }

                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 8; j++) {
                        int row = (i * 8) + j;
                        if (_outputList.get(row).getName().equals(name)) {
                            return "Q" + i + "." + j;
                        }
                    }
                }
            } else {
                // Search receiver and transmitter tables
                var splitName = name.split("~");
                var baseName = splitName[0];
                var speed = 0;
                try {
                    speed = Integer.parseInt(splitName[1]);
                } catch (NumberFormatException e) {
                    log.error("Unable to get speed value for name '{}', default zero", name);
                    speed = 0;
                }
                for (int i = 0; i < 16; i++) {
                    if (_receiverList.get(i).getName().equals(baseName)) {
                        return "Y" + i + "." + speed;
                    }
                }

                for (int i = 0; i < 16; i++) {
                    if (_transmitterList.get(i).getName().equals(baseName)) {
                        return "Z" + i + "." + speed;
                    }
                }
            }
        }

        return variable;
    }

    private String variableToName(String variable) {
        String name = "";

        if (variable.length() > 1) {
            var varType = variable.substring(0, 1);
            var match = PARSE_VARIABLE.matcher(variable);
            if (match.find() && match.groupCount() == 2) {
                int first = -1;
                int second = -1;
                int row = -1;

                try {
                    first = Integer.parseInt(match.group(1));
                    second = Integer.parseInt(match.group(2));
                } catch (NumberFormatException e) {
                    log.error("Unable to parse variable name '{}'", variable);
                    return name;
                }

                switch (varType) {
                    case "I":
                        row = (first * 8) + second;
                        name = _inputList.get(row).getName();
                        break;
                    case "Q":
                        row = (first * 8) + second;
                        name = _outputList.get(row).getName();
                        break;
                    case "Y":
                        row = first;
                        name = _receiverList.get(row).getName() + "~" + second;
                        break;
                    case "Z":
                        row = first;
                        name = _receiverList.get(row).getName() + "~" + second;
                        break;
                    default:
                        log.error("Variable '{}' has an invalid first letter (IQYZ)", variable);
               }
            }
        }

        return name;
    }

    private void encode(GroupRow groupRow) {
        List<String> lines = new ArrayList<>();
        String longLine = "";

        var logicList = groupRow.getLogicList();
        for (var row : logicList) {
            var sb = new StringBuilder();
            var jumpLabel = false;

            if (!row.getLabel().isEmpty()) {
                sb.append(row.getLabel() + " ");
            }

            if (row.getOper() != null) {
                var oper = row.getOper();
                var operName = oper.name();

                // Fix special enums
                if (operName.equals("Cp")) {
                    operName = ")";
                } else if (operName.equals("EQ")) {
                    operName = "=";
                } else if (operName.contains("p")) {
                    operName = operName.replace("p", "(");
                }

                if (operName.startsWith("J")) {
                    jumpLabel =true;
                }
                sb.append(operName + " ");
            }

            if (!row.getName().isEmpty()) {
                var name = row.getName().trim();

                if (jumpLabel) {
                    sb.append(name + " ");
                    jumpLabel = false;
                } else if (isMemory(name)) {
                    sb.append(name + " ");
                } else if (isTimerWord(name)) {
                    sb.append(name + " ");
                } else if (isTimerVar(name)) {
                    sb.append(name + " ");
                } else {
                    var variable = nameToVariable(name);
                    if (variable == null) {
                        log.error("bad name");
                    } else {
                        sb.append(variable + " ");
                    }
                }
            }

            if (!row.getComment().isEmpty()) {
                var comment = row.getComment().trim();
                sb.append("/* " + comment + " */ ");
            }

            log.info("{}", sb.toString());
            longLine = longLine + sb.toString();
        }

        var size = longLine.length();
        groupRow.setLine1("");
        groupRow.setLine2("");
        groupRow.setLine3("");
        groupRow.setLine4("");

        if (longLine.length() < 64) {
            groupRow.setLine1(longLine);
            log.info("Line 1:  {}", groupRow.getLine1());
            return;
        } else {
            groupRow.setLine1(longLine.substring(0, 63));
            log.info("Line 1:  {}", groupRow.getLine1());
            longLine = longLine.substring(63);
        }

        if (longLine.length() < 64) {
            groupRow.setLine2(longLine);
            log.info("Line 2:  {}", groupRow.getLine2());
            return;
        } else {
            groupRow.setLine2(longLine.substring(0, 63));
            log.info("Line 2:  {}", groupRow.getLine2());
            longLine = longLine.substring(63);
        }

        if (longLine.length() < 64) {
            groupRow.setLine3(longLine);
            log.info("Line 3:  {}", groupRow.getLine3());
            return;
        } else {
            groupRow.setLine3(longLine.substring(0, 63));
            log.info("Line 3:  {}", groupRow.getLine3());
            longLine = longLine.substring(63);
        }

        if (longLine.length() < 64) {
            groupRow.setLine4(longLine);
            log.info("Line 4:  {}", groupRow.getLine4());
            return;
        } else {
            groupRow.setLine4(longLine.substring(0, 63));
            log.info("Line 4:  {}", groupRow.getLine4());
            longLine = longLine.substring(63);
        }

        log.error("The line overflowed, content truncated:  {}", longLine);
    }

    private boolean isMemory(String name) {
        var match = PARSE_VARIABLE.matcher(name);
        return (match.find() && name.startsWith("M"));
    }

    private boolean isTimerWord(String name) {
        var match = PARSE_TIMERWORD.matcher(name);
        return match.find();
    }

    private boolean isTimerVar(String name) {
        var match = PARSE_TIMERVAR.matcher(name);
        return match.find();
    }

    private void decode(GroupRow groupRow) {
        var sb = new StringBuilder();
        sb.append(groupRow.getLine1());
        sb.append(groupRow.getLine2());
        sb.append(groupRow.getLine3());
        sb.append(groupRow.getLine4());

        String[] tokens = sb.toString().split(" ");
        for (int i = 0; i < tokens.length; i++) {
            log.info("{} :: {}", i, tokens[i]);
        }

        // Find the operators and create empty logic rows.
        Operator oper;
        for (int i = 0; i < tokens.length; i++) {
            oper = getEnum(tokens[i]);
            if (oper != null) {
                var logic = new LogicRow("", oper, "", "");
                logic.setDecodeLine(i);
                groupRow.getLogicList().add(logic);
            }
        }

        // For each logic row, look for labels, names and comments.
        var logicList = groupRow.getLogicList();
        for (LogicRow logic : logicList) {
            int decodeLine = logic.getDecodeLine();

            // Look for a label
            if (decodeLine > 0) {
                var label = tokens[decodeLine - 1];
                var match = PARSE_LABEL.matcher(label);
                if (match.find()) {
                    logic.setLabel(label);
                }
            }

            // Look for a name
            if (decodeLine < tokens.length - 1) {
                var name = tokens[decodeLine + 1];

                if (logic.getOper().name().startsWith("J")) {   // Jump label
                    logic.setName(name);
                    decodeLine++;
                } else if (isTimerWord(name)) {  // Load timer
                    logic.setName(name);
                    decodeLine++;
                } else if (isMemory(name)) {  // Memory variable
                    logic.setName(name);
                    decodeLine++;
                } else if (isTimerVar(name)) {  // Timer variable
                    logic.setName(name);
                    decodeLine++;
                } else {
                    var match = PARSE_VARIABLE.matcher(name);
                    if (match.find()) {
                        logic.setName(variableToName(name));
                        decodeLine++;
                    }
                }
            }

            // Look for a comment, decoderLine was incremented if there was a name.
            if (decodeLine < tokens.length - 2) {
                var idx = decodeLine + 1;
                var slashAstr = tokens[idx];
                if (slashAstr.equals("/*")) {
                    sb = new StringBuilder();
                    idx++;
                    var word = tokens[idx];

                    while (!word.equals("*/")) {
                        sb.append(word + " ");
                        idx++;
                        word = tokens[idx];
                    }
                    logic.setComment(sb.toString());
                }
            }
        }
    }

    private Operator getEnum(String name) {
        try {
            var temp = name;
            if (name.equals("=")) {
                temp = "EQ";
            } else if (name.equals(")")) {
                temp = "Cp";
            } else if (name.endsWith("(")) {
                temp = name.replace("(", "p");
            }

            Operator oper = Enum.valueOf(Operator.class, temp);
            return oper;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // --------------  node selector ---------

    void setReady(boolean t) {
        ready = t;
    }

    /**
     * When a node is selected, load the CDI.
     * @parm e The combo box action event.
     */
    private void nodeSelected(ActionEvent e) {
        var item = _nodeBox.getSelectedItem();
        log.info("nodeSelected: {}", _nodeBox.getSelectedItem());
        loadData();
    }

    private void newNodeInList(MimicNodeStore.NodeMemo nodeMemo) {
        // Add filter for Tower LCC+Q

//         int i = 0;
//         if (_nodeModel.getIndexOf(nodeMemo.getNodeID()) >= 0) {
//             // already exists. Do nothing.
//             return;
//         }
        NodeEntry e = new NodeEntry(nodeMemo);

//         while ((i < _nodeModel.getSize()) && (_nodeModel.getElementAt(i).compareTo(e) < 0)) {
//             ++i;
//         }
//         _nodeModel.insertElementAt(e, i);
        _nodeModel.addElement(e);
    }

    void pushedCancel(ActionEvent e) {
        if (ready) {
            cancelled = true;
        }
    }

    // Notifies that the contents of a given entry have changed. This will delete and re-add the
    // entry to the model, forcing a refresh of the box.
    private void updateComboBoxModelEntry(NodeEntry nodeEntry) {
        int idx = _nodeModel.getIndexOf(nodeEntry.getNodeID());
        if (idx < 0) {
            return;
        }
        NodeEntry last = _nodeModel.getElementAt(idx);
        if (last != nodeEntry) {
            // not the same object -- we're talking about an abandoned entry.
            nodeEntry.dispose();
            return;
        }
        NodeEntry sel = (NodeEntry) _nodeModel.getSelectedItem();
        _nodeModel.removeElementAt(idx);
        _nodeModel.insertElementAt(nodeEntry, idx);
        _nodeModel.setSelectedItem(sel);
    }

    protected class NodeEntry implements Comparable<NodeEntry>, PropertyChangeListener {
        final MimicNodeStore.NodeMemo nodeMemo;
        String description = "";

        NodeEntry(MimicNodeStore.NodeMemo memo) {
            this.nodeMemo = memo;
            memo.addPropertyChangeListener(this);
            updateDescription();
        }

        /**
         * Constructor for prototype display value
         *
         * @param description prototype display value
         */
        private NodeEntry(String description) {
            this.nodeMemo = null;
            this.description = description;
        }

        public NodeID getNodeID() {
            return nodeMemo.getNodeID();
        }

        private void updateDescription() {
            int termCount = 99;
            SimpleNodeIdent ident = nodeMemo.getSimpleNodeIdent();
            StringBuilder sb = new StringBuilder();
            sb.append(nodeMemo.getNodeID().toString());
            int count = 0;
            if (count < termCount) {
                count += addToDescription(ident.getUserName(), sb);
            }
            if (count < termCount) {
                count += addToDescription(ident.getUserDesc(), sb);
            }
            if (count < termCount) {
                if (!ident.getMfgName().isEmpty() || !ident.getModelName().isEmpty()) {
                    count += addToDescription(ident.getMfgName() + " " +ident.getModelName(),
                        sb);
                }
            }
            if (count < termCount) {
                count += addToDescription(ident.getSoftwareVersion(), sb);
            }
            String newDescription = sb.toString();
            if (!description.equals(newDescription)) {
                description = newDescription;
                // update combo box model.
//                 updateComboBoxModelEntry(this);
            }
        }

        private int addToDescription(String s, StringBuilder sb) {
            if (s.isEmpty()) {
                return 0;
            }
            sb.append(" - ");
            sb.append(s);
            return 1;
        }

        private long reorder(long n) {
            return (n < 0) ? Long.MAX_VALUE - n : Long.MIN_VALUE + n;
        }

        @Override
        public int compareTo(NodeEntry otherEntry) {
            long l1 = reorder(getNodeID().toLong());
            long l2 = reorder(otherEntry.getNodeID().toLong());
            return Long.compare(l1, l2);
        }

        @Override
        public String toString() {
            return description;
        }

        @Override
        @SuppressFBWarnings(value = "EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS",
                justification = "Purposefully attempting lookup using NodeID argument in model " +
                        "vector.")
        public boolean equals(Object o) {
            if (o instanceof NodeEntry) {
                return getNodeID().equals(((NodeEntry) o).getNodeID());
            }
            if (o instanceof NodeID) {
                return getNodeID().equals(o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getNodeID().hashCode();
        }

        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            //log.warning("Received model entry update for " + nodeMemo.getNodeID());
            if (propertyChangeEvent.getPropertyName().equals(UPDATE_PROP_SIMPLE_NODE_IDENT)) {
                updateDescription();
            }
        }

        public void dispose() {
            //log.warning("dispose of " + nodeMemo.getNodeID().toString());
            nodeMemo.removePropertyChangeListener(this);
        }
    }

    // --------------  load lists ---------

    private void loadData() {
        try {
            FileInputStream in = new FileInputStream("/Users/das/JMRI/_Profiles/STL_Editor.jmri/STL Editor.properties");
            _cdiTest.load(in);
        }
        catch (Exception exx) {
            log.error("Properties load failed {}", exx);
        }

        // Load data
        loadInputs();
        loadOutputs();
        loadReceivers();
        loadTransmitters();
        loadGroups();

        for (GroupRow row : _groupList) {
            decode(row);
        }

        setReady(true);

        _groupTable.setRowSelectionInterval(0, 0);

        _percentButton.setEnabled(true);
        _storeButton.setEnabled(true);
    }

    private void pushedRefreshButton(ActionEvent e) {
        loadData();
    }

    private void loadGroups() {
        _groupList.clear();
        _logicList.clear();

        for (int i = 0; i < 16; i++) {
            String groupName = _cdiTest.getProperty(String.format(GROUP_NAME, i));
            var groupRow = new GroupRow(groupName);

            groupRow.setLine1(_cdiTest.getProperty(String.format(GROUP_LINE, i, 1)));
            groupRow.setLine2(_cdiTest.getProperty(String.format(GROUP_LINE, i, 2)));
            groupRow.setLine3(_cdiTest.getProperty(String.format(GROUP_LINE, i, 3)));
            groupRow.setLine4(_cdiTest.getProperty(String.format(GROUP_LINE, i, 4)));

            _groupList.add(groupRow);
        }


//         _logicList.add(new LogicList("L001:", Operator.A, "O-E14 /* Check for block occupancy */", 0));
//         _logicList.add(new LogicList("", Operator.Op, "", 0));
//         _logicList.add(new LogicList("", Operator.AN, "T-Hopkins-West /* turnout closed */", 0));
//         _logicList.add(new LogicList("", Operator.A, "O-Main /* block occupied */", 0));
//         _logicList.add(new LogicList("", Operator.Cp, "", 0));
//         _logicList.add(new LogicList("", Operator.Op, "", 0));
//         _logicList.add(new LogicList("", Operator.A, "T-Hopkins-West /* turnout thrown */", 0));
//         _logicList.add(new LogicList("", Operator.A, "O-Side /* block occupied */", 0));
//         _logicList.add(new LogicList("", Operator.Cp, "", 0));
//         _logicList.add(new LogicList("", Operator.R, "Mast /* use the stop aspect */", 0));
//         _logicList.add(new LogicList("", Operator.JU, "L009", 0));
//         _logicList.add(new LogicList("L002:", null, "/* set the other aspects */", 0));
//         _logicList.add(new LogicList("L009:", null, "/* Finished the signal logic */", 0));

        _groupTable.revalidate();
//         _logicTable.revalidate();

        // Select first group
//         _groupTable.setRowSelectionInterval(0, 0);
    }

    private void loadInputs() {
        _inputList.clear();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                String name = _cdiTest.getProperty(String.format(INPUT_NAME, i, j));
                String trueEvent = _cdiTest.getProperty(String.format(INPUT_TRUE, i, j));
                String falseEvent = _cdiTest.getProperty(String.format(INPUT_FALSE, i, j));
                _inputList.add(new InputRow(name, trueEvent, falseEvent));
            }
        }
        _inputTable.revalidate();
    }

    private void loadOutputs() {
        _outputList.clear();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                String name = _cdiTest.getProperty(String.format(OUTPUT_NAME, i, j));
                String trueEvent = _cdiTest.getProperty(String.format(OUTPUT_TRUE, i, j));
                String falseEvent = _cdiTest.getProperty(String.format(OUTPUT_FALSE, i, j));
                _outputList.add(new OutputRow(name, trueEvent, falseEvent));
            }
        }
        _outputTable.revalidate();
    }

    private void loadReceivers() {
        _receiverList.clear();
        for (int i = 0; i < 16; i++) {
            String name = _cdiTest.getProperty(String.format(RECEIVER_NAME, i));
            String event = _cdiTest.getProperty(String.format(RECEIVER_EVENT, i));
            _receiverList.add(new ReceiverRow(name, event));
        }
        _receiverTable.revalidate();
    }

    private void loadTransmitters() {
        _transmitterList.clear();
        for (int i = 0; i < 16; i++) {
            String name = _cdiTest.getProperty(String.format(TRANSMITTER_NAME, i));
            String event = _cdiTest.getProperty(String.format(TRANSMITTER_EVeNT, i));
            _transmitterList.add(new TransmitterRow(name, event));
        }
        _transmitterTable.revalidate();
    }

    // --------------  store data ---------

    private void pushedStoreButton(ActionEvent e) {
        log.info("Store the data to the properties file");
        // Store data
        storeInputs();
        storeOutputs();
        storeReceivers();
        storeTransmitters();
        storeGroups();

        try {
            FileOutputStream out = new FileOutputStream("/Users/das/JMRI/_Profiles/STL_Editor.jmri/STL Editor.properties");
            _cdiTest.store(out, "test");
        }
        catch (Exception exx) {
            log.error("Properties store failed {}", exx);
        }

        _dirty = false;
    }

    private void storeGroups() {
        if (_groupRow >= 0) {
            // Transfer the working logic list to the current group's logic list
            _groupList.get(_groupRow).setLogicList(_logicList);
        }

        // store the group data
        for (int i = 0; i < 16; i++) {
            var row = _groupList.get(i);

            // update the group lines
            encode(row);

            _cdiTest.setProperty(String.format(GROUP_NAME, i), row.getName());
            _cdiTest.setProperty(String.format(GROUP_LINE, i, 1), row.getLine1());
            _cdiTest.setProperty(String.format(GROUP_LINE, i, 2), row.getLine2());
            _cdiTest.setProperty(String.format(GROUP_LINE, i, 3), row.getLine3());
            _cdiTest.setProperty(String.format(GROUP_LINE, i, 4), row.getLine4());
        }
    }

    private void storeInputs() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var row = _inputList.get((i * 8) + j);
                _cdiTest.setProperty(String.format(INPUT_NAME, i, j), row.getName());
                _cdiTest.setProperty(String.format(INPUT_TRUE, i, j), row.getEventTrue());
                _cdiTest.setProperty(String.format(INPUT_FALSE, i, j), row.getEventFalse());
            }
        }
    }

    private void storeOutputs() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var row = _outputList.get((i * 8) + j);
                _cdiTest.setProperty(String.format(OUTPUT_NAME, i, j), row.getName());
                _cdiTest.setProperty(String.format(OUTPUT_TRUE, i, j), row.getEventTrue());
                _cdiTest.setProperty(String.format(OUTPUT_FALSE, i, j), row.getEventFalse());
            }
        }
    }

    private void storeReceivers() {
        for (int i = 0; i < 16; i++) {
            var row = _receiverList.get(i);
            _cdiTest.setProperty(String.format(RECEIVER_NAME, i), row.getName());
            _cdiTest.setProperty(String.format(RECEIVER_EVENT, i), row.getEventId());
        }
    }

    private void storeTransmitters() {
        for (int i = 0; i < 16; i++) {
            var row = _transmitterList.get(i);
            _cdiTest.setProperty(String.format(TRANSMITTER_NAME, i), row.getName());
            _cdiTest.setProperty(String.format(TRANSMITTER_EVeNT, i), row.getEventId());
        }
    }

    // --------------  table lists ---------

    /**
     * The Group row contains the name and the raw data for one of the 16 groups.
     * It also contains the decoded logic for the group in the logic list.
     */
    private static class GroupRow {
        String _name;
        String _line1 = "";
        String _line2 = "";
        String _line3 = "";
        String _line4 = "";
        List<LogicRow> _logicList = new ArrayList<>();


        GroupRow(String name) {
            _name = name.isEmpty() ? "--" : name;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
            _dirty = true;
        }

        List<LogicRow> getLogicList() {
            return _logicList;
        }

        void setLogicList(List<LogicRow> logicList) {
            _logicList.clear();
            _logicList.addAll(logicList);
        }

        String getLine1() {
            return _line1;
        }

        void setLine1(String newLine) {
            _line1 = newLine;
            _dirty = true;
        }

        String getLine2() {
            return _line2;
        }

        void setLine2(String newLine) {
            _line2 = newLine;
            _dirty = true;
        }

        String getLine3() {
            return _line3;
        }

        void setLine3(String newLine) {
            _line3 = newLine;
            _dirty = true;
        }

        String getLine4() {
            return _line4;
        }

        void setLine4(String newLine) {
            _line4 = newLine;
            _dirty = true;
        }

        String getSize() {
            int size = _line1.length() + _line2.length() + _line3.length() + _line4.length();
            size = (size * 100) / 255;
            return String.valueOf(size) + "%";
        }
    }

    /**
     * The definition of a logic row
     */
    private static class LogicRow {
        String _label;
        Operator _oper;
        String _name;
        String _comment;
        String _variable = "";
        int _decodeLine = 0;

        LogicRow(String label, Operator oper, String name, String comment) {
            _label = label;
            _oper = oper;
            _name = name;
            _comment = comment;
        }

        String getLabel() {
            return _label;
        }

        void setLabel(String newLabel) {
            _label = newLabel;
            _dirty = true;
        }

        Operator getOper() {
            return _oper;
        }

        void setOper(Operator newOper) {
            _oper = newOper;
            _dirty = true;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
            _dirty = true;
        }

        String getComment() {
            return _comment;
        }

        void setComment(String newComment) {
            _comment = newComment;
            _dirty = true;
        }

        String getVariable() {
            return _variable;
        }

        void setVariable(String newVariable) {
            _variable = newVariable;
        }

        int getDecodeLine() {
            return _decodeLine;
        }

        void setDecodeLine(int decodeLine) {
            _decodeLine = decodeLine;
        }
    }

    /**
     * The name and assigned true and false events for an Input.
     */
    private static class InputRow {
        String _name;
        String _eventTrue;
        String _eventFalse;

        InputRow(String name, String eventTrue, String eventFalse) {
            _name = name;
            _eventTrue = eventTrue;
            _eventFalse = eventFalse;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
            _dirty = true;
        }

        String getEventTrue() {
            return _eventTrue;
        }

        void setEventTrue(String newEventTrue) {
            _eventTrue = newEventTrue;
            _dirty = true;
        }

        String getEventFalse() {
            return _eventFalse;
        }

        void setEventFalse(String newEventFalse) {
            _eventFalse = newEventFalse;
            _dirty = true;
        }
    }

    /**
     * The name and assigned true and false events for an Output.
     */
    private static class OutputRow {
        String _name;
        String _eventTrue;
        String _eventFalse;

        OutputRow(String name, String eventTrue, String eventFalse) {
            _name = name;
            _eventTrue = eventTrue;
            _eventFalse = eventFalse;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
            _dirty = true;
        }

        String getEventTrue() {
            return _eventTrue;
        }

        void setEventTrue(String newEventTrue) {
            _eventTrue = newEventTrue;
            _dirty = true;
        }

        String getEventFalse() {
            return _eventFalse;
        }

        void setEventFalse(String newEventFalse) {
            _eventFalse = newEventFalse;
            _dirty = true;
        }
    }

    /**
     * The name and assigned event id for a circuit receiver.
     */
    private static class ReceiverRow {
        String _name;
        String _eventid;

        ReceiverRow(String name, String eventid) {
            _name = name;
            _eventid = eventid;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
            _dirty = true;
        }

        String getEventId() {
            return _eventid;
        }

        void setEventId(String newEventid) {
            _eventid = newEventid;
            _dirty = true;
        }
    }

    /**
     * The name and assigned event id for a circuit transmitter.
     */
    private static class TransmitterRow {
        String _name;
        String _eventid;

        TransmitterRow(String name, String eventid) {
            _name = name;
            _eventid = eventid;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
            _dirty = true;
        }

        String getEventId() {
            return _eventid;
        }

        void setEventId(String newEventid) {
            _eventid = newEventid;
            _dirty = true;
        }
    }

    // --------------  table models ---------

    /**
     * TableModel for Group table entries.
     */
    class GroupModel extends AbstractTableModel {

        GroupModel() {
        }

        public static final int NAME_COLUMN = 0;

        @Override
        public int getRowCount() {
            return _groupList.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case NAME_COLUMN:
                    return Bundle.getMessage("ColumnName");  // NOI18N
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case NAME_COLUMN:
                    return _groupList.get(r).getName();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            switch (c) {
                case NAME_COLUMN:
                    _groupList.get(r).setName((String) type);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (c == NAME_COLUMN);
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case NAME_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }
    }

    /**
     * TableModel for STL table entries.
     */
    class LogicModel extends AbstractTableModel {

        LogicModel() {
        }

        public static final int LABEL_COLUMN = 0;
        public static final int OPER_COLUMN = 1;
        public static final int NAME_COLUMN = 2;
        public static final int COMMENT_COLUMN = 3;
        public static final int VAR_COLUMN = 4;

        @Override
        public int getRowCount() {
            return _logicList.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == OPER_COLUMN) return JComboBox.class;
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case LABEL_COLUMN:
                    return Bundle.getMessage("ColumnLabel");  // NOI18N
                case OPER_COLUMN:
                    return Bundle.getMessage("ColumnOper");  // NOI18N
                case NAME_COLUMN:
                    return Bundle.getMessage("ColumnName");  // NOI18N
                case COMMENT_COLUMN:
                    return Bundle.getMessage("ColumnComment");  // NOI18N
                case VAR_COLUMN:
                    return Bundle.getMessage("ColumnVar");  // NOI18N
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case LABEL_COLUMN:
                    return _logicList.get(r).getLabel();
                case OPER_COLUMN:
                    return _logicList.get(r).getOper();
                case NAME_COLUMN:
                    return _logicList.get(r).getName();
                case COMMENT_COLUMN:
                    return _logicList.get(r).getComment();
                case VAR_COLUMN:
                    return _logicList.get(r).getVariable();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            switch (c) {
                case LABEL_COLUMN:
                    _logicList.get(r).setLabel((String) type);
                    break;
                case OPER_COLUMN:
                    var z = (Operator) type;
                    if (z != null) {
                        if (z.name().startsWith("z")) {
                            return;
                        }
                        if (z.name().equals("x0")) {
                            _logicList.get(r).setOper(null);
                            return;
                        }
                    }
                    _logicList.get(r).setOper((Operator) type);
                    break;
                case NAME_COLUMN:
                    _logicList.get(r).setName((String) type);
                    break;
                case COMMENT_COLUMN:
                    _logicList.get(r).setComment((String) type);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == LABEL_COLUMN || c == OPER_COLUMN || c == NAME_COLUMN || c == COMMENT_COLUMN));
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case LABEL_COLUMN:
                case VAR_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case OPER_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                case NAME_COLUMN:
                    return new JTextField(40).getPreferredSize().width;
                case COMMENT_COLUMN:
                    return new JTextField(40).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }
    }

    /**
     * TableModel for Input table entries.
     */
    class InputModel extends AbstractTableModel {

        InputModel() {
        }

        public static final int INPUT_COLUMN = 0;
        public static final int NAME_COLUMN = 1;
        public static final int TRUE_COLUMN = 2;
        public static final int FALSE_COLUMN = 3;

        @Override
        public int getRowCount() {
            return _inputList.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case INPUT_COLUMN:
                    return Bundle.getMessage("ColumnInput");  // NOI18N
                case NAME_COLUMN:
                    return Bundle.getMessage("ColumnName");  // NOI18N
                case TRUE_COLUMN:
                    return Bundle.getMessage("ColumnTrue");  // NOI18N
                case FALSE_COLUMN:
                    return Bundle.getMessage("ColumnFalse");  // NOI18N
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case INPUT_COLUMN:
                    int grp = r / 8;
                    int rem = r % 8;
                    return "I" + grp + "." + rem;
                case NAME_COLUMN:
                    return _inputList.get(r).getName();
                case TRUE_COLUMN:
                    return _inputList.get(r).getEventTrue();
                case FALSE_COLUMN:
                    return _inputList.get(r).getEventFalse();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            switch (c) {
                case NAME_COLUMN:
                    _inputList.get(r).setName((String) type);
                    break;
                case TRUE_COLUMN:
                    _inputList.get(r).setEventTrue((String) type);
                    break;
                case FALSE_COLUMN:
                    _inputList.get(r).setEventFalse((String) type);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == NAME_COLUMN) || (c == TRUE_COLUMN) || (c == FALSE_COLUMN));
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case INPUT_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case NAME_COLUMN:
                    return new JTextField(50).getPreferredSize().width;
                case TRUE_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                case FALSE_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }
    }

    /**
     * TableModel for Output table entries.
     */
    class OutputModel extends AbstractTableModel {
        OutputModel() {
        }

        public static final int OUTPUT_COLUMN = 0;
        public static final int NAME_COLUMN = 1;
        public static final int TRUE_COLUMN = 2;
        public static final int FALSE_COLUMN = 3;

        @Override
        public int getRowCount() {
            return _outputList.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case OUTPUT_COLUMN:
                    return Bundle.getMessage("ColumnInput");  // NOI18N
                case NAME_COLUMN:
                    return Bundle.getMessage("ColumnName");  // NOI18N
                case TRUE_COLUMN:
                    return Bundle.getMessage("ColumnTrue");  // NOI18N
                case FALSE_COLUMN:
                    return Bundle.getMessage("ColumnFalse");  // NOI18N
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case OUTPUT_COLUMN:
                    int grp = r / 8;
                    int rem = r % 8;
                    return "Q" + grp + "." + rem;
                case NAME_COLUMN:
                    return _outputList.get(r).getName();
                case TRUE_COLUMN:
                    return _outputList.get(r).getEventTrue();
                case FALSE_COLUMN:
                    return _outputList.get(r).getEventFalse();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            switch (c) {
                case NAME_COLUMN:
                    _outputList.get(r).setName((String) type);
                    break;
                case TRUE_COLUMN:
                    _outputList.get(r).setEventTrue((String) type);
                    break;
                case FALSE_COLUMN:
                    _outputList.get(r).setEventFalse((String) type);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == NAME_COLUMN) || (c == TRUE_COLUMN) || (c == FALSE_COLUMN));
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case OUTPUT_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case NAME_COLUMN:
                    return new JTextField(50).getPreferredSize().width;
                case TRUE_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                case FALSE_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }
    }

    /**
     * TableModel for circuit receiver table entries.
     */
    class ReceiverModel extends AbstractTableModel {

        ReceiverModel() {
        }

        public static final int CIRCUIT_COLUMN = 0;
        public static final int NAME_COLUMN = 1;
        public static final int EVENTID_COLUMN = 2;

        @Override
        public int getRowCount() {
            return _receiverList.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case CIRCUIT_COLUMN:
                    return Bundle.getMessage("ColumnCircuit");  // NOI18N
                case NAME_COLUMN:
                    return Bundle.getMessage("ColumnName");  // NOI18N
                case EVENTID_COLUMN:
                    return Bundle.getMessage("ColumnEventID");  // NOI18N
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case CIRCUIT_COLUMN:
                    return "Y" + r;
                case NAME_COLUMN:
                    return _receiverList.get(r).getName();
                case EVENTID_COLUMN:
                    return _receiverList.get(r).getEventId();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            switch (c) {
                case NAME_COLUMN:
                    _receiverList.get(r).setName((String) type);
                    break;
                case EVENTID_COLUMN:
                    _receiverList.get(r).setEventId((String) type);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == NAME_COLUMN) || (c == EVENTID_COLUMN));
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case CIRCUIT_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case NAME_COLUMN:
                    return new JTextField(50).getPreferredSize().width;
                case EVENTID_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }
    }

    /**
     * TableModel for circuit transmitter table entries.
     */
    class TransmitterModel extends AbstractTableModel {

        TransmitterModel() {
        }

        public static final int CIRCUIT_COLUMN = 0;
        public static final int NAME_COLUMN = 1;
        public static final int EVENTID_COLUMN = 2;

        @Override
        public int getRowCount() {
            return _transmitterList.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case CIRCUIT_COLUMN:
                    return Bundle.getMessage("ColumnCircuit");  // NOI18N
                case NAME_COLUMN:
                    return Bundle.getMessage("ColumnName");  // NOI18N
                case EVENTID_COLUMN:
                    return Bundle.getMessage("ColumnEventID");  // NOI18N
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case CIRCUIT_COLUMN:
                    return "Z" + r;
                case NAME_COLUMN:
                    return _transmitterList.get(r).getName();
                case EVENTID_COLUMN:
                    return _transmitterList.get(r).getEventId();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            switch (c) {
                case NAME_COLUMN:
                    _transmitterList.get(r).setName((String) type);
                    break;
                case EVENTID_COLUMN:
                    _transmitterList.get(r).setEventId((String) type);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == NAME_COLUMN) || (c == EVENTID_COLUMN));
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case CIRCUIT_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case NAME_COLUMN:
                    return new JTextField(50).getPreferredSize().width;
                case EVENTID_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }
    }

    // --------------  Operator Enum ---------

    public enum Operator {
        x0(Bundle.getMessage("Separator0")),
        z1(Bundle.getMessage("Separator1")),
        A(Bundle.getMessage("OperatorA")),
        AN(Bundle.getMessage("OperatorAN")),
        O(Bundle.getMessage("OperatorO")),
        ON(Bundle.getMessage("OperatorON")),
        X(Bundle.getMessage("OperatorX")),
        XN(Bundle.getMessage("OperatorXN")),

        z2(Bundle.getMessage("Separator2")),    // The STL parens are represented by lower case p
        Ap(Bundle.getMessage("OperatorAp")),
        ANp(Bundle.getMessage("OperatorANp")),
        Op(Bundle.getMessage("OperatorOp")),
        ONp(Bundle.getMessage("OperatorONp")),
        Xp(Bundle.getMessage("OperatorXp")),
        XNp(Bundle.getMessage("OperatorXNp")),
        Cp(Bundle.getMessage("OperatorCp")),    // Close paren

        z3(Bundle.getMessage("Separator3")),
        EQ(Bundle.getMessage("OperatorEQ")),    // = operator
        R(Bundle.getMessage("OperatorR")),
        S(Bundle.getMessage("OperatorS")),

        z4(Bundle.getMessage("Separator4")),
        NOT(Bundle.getMessage("OperatorNOT")),
        SET(Bundle.getMessage("OperatorSET")),
        CLR(Bundle.getMessage("OperatorCLR")),
        SAVE(Bundle.getMessage("OperatorSAVE")),

        z5(Bundle.getMessage("Separator5")),
        JU(Bundle.getMessage("OperatorJU")),
        JC(Bundle.getMessage("OperatorJC")),
        JCN(Bundle.getMessage("OperatorJCN")),
        JCB(Bundle.getMessage("OperatorJCB")),
        JNB(Bundle.getMessage("OperatorJNB")),
        JBI(Bundle.getMessage("OperatorJBI")),
        JNBI(Bundle.getMessage("OperatorJNBI")),

        z6(Bundle.getMessage("Separator6")),
        FN(Bundle.getMessage("OperatorFN")),
        FP(Bundle.getMessage("OperatorFP")),

        z7(Bundle.getMessage("Separator7")),
        L(Bundle.getMessage("OperatorL")),
        FR(Bundle.getMessage("OperatorFR")),
        SP(Bundle.getMessage("OperatorSP")),
        SE(Bundle.getMessage("OperatorSE")),
        SD(Bundle.getMessage("OperatorSD")),
        SS(Bundle.getMessage("OperatorSS")),
        SF(Bundle.getMessage("OperatorSF"));

        private final String _text;

        private Operator(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    // --------------  misc items ---------

    @Override
    public void dispose() {
        // and complete this
        super.dispose();
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.openlcb.swing.memtool.MemoryToolPane";
    }

    @Override
    public String getTitle() {
        if (_canMemo != null) {
            return (_canMemo.getUserName() + " STL Editor");
        }
        return Bundle.getMessage("TitleSTLEditor");
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    public static class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("STL Editor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    StlEditorPane.class.getName(),
                    jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StlEditorPane.class);
}
