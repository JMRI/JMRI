package jmri.jmrix.openlcb.swing.stleditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.nio.file.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.FileUtil;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriJFileChooser;
import jmri.util.swing.JmriJOptionPane;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.openlcb.MimicNodeStore.NodeMemo.UPDATE_PROP_SIMPLE_NODE_IDENT;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import org.openlcb.*;
import org.openlcb.cdi.cmd.*;
import org.openlcb.cdi.impl.ConfigRepresentation;


/**
 * Panel for editing STL logic.
 *
 * The primary mode is a connection to a Tower LCC+Q.  When a node is selected, the data
 * is transferred to Java lists and displayed using Java tables. If changes are to be retained,
 * the Store process is invoked which updates the Tower LCC+Q CDI.
 *
 * An alternate mode uses CSV files to import and export the data.  This enables offline development.
 * Since the CDI is loaded automatically when the node is selected, to transfer offline development
 * is a three step process:  Load the CDI, replace the content with the CSV content and then store
 * to the CDI.
 *
 * A third mode is to load a CDI backup file.  This can then be used with the CSV process for offline work.
 *
 * TODO (phase 2):
 *     Implement CDI write confirmation.
 *     Provide a store in process dialog - depends on write confirmation
 *     Implement reboot:
 *         rep.getConnection().getDatagramService().sendData(rep.getRemoteNodeID(), new int[] {0x20, 0xA9});
 *         Check compile messages.
 *
 * @author Dave Sand Copyright (C) 2024
 * @since 5.7.5
 */
public class StlEditorPane extends jmri.util.swing.JmriPanel
        implements jmri.jmrix.can.swing.CanPanelInterface {

    /**
     * The STL Editor is dependent on the Tower LCC+Q software version
     */
    private static int TOWER_LCC_Q_NODE_VERSION = 106;
    private static String TOWER_LCC_Q_NODE_VERSION_STRING = "v1.06";

    private CanSystemConnectionMemo _canMemo;
    private OlcbInterface _iface;
    private ConfigRepresentation _cdi;
    private MimicNodeStore _store;

    private boolean _dirty = false;
    private int _logicRow = -1;     // The last selected row, -1 for none
    private int _groupRow = 0;
    private List<String> _csvMessages = new ArrayList<>();

    private String _csvDirectoryPath = "";

    private DefaultComboBoxModel<NodeEntry> _nodeModel = new DefaultComboBoxModel<NodeEntry>();
    private JComboBox<NodeEntry> _nodeBox;

    private JComboBox<Operator> _operators = new JComboBox<>(Operator.values());

    private List<GroupRow> _groupList = new ArrayList<>();
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
    private JButton _exportButton;
    private JButton _importButton;

    private JMenuItem _refreshItem;
    private JMenuItem _storeItem;
    private JMenuItem _exportItem;
    private JMenuItem _importItem;
    private JMenuItem _loadItem;

    // CDI Names
    private static String INPUT_NAME = "Logic Inputs.Group I%s(%s).Input Description";
    private static String INPUT_TRUE = "Logic Inputs.Group I%s(%s).True";
    private static String INPUT_FALSE = "Logic Inputs.Group I%s(%s).False";
    private static String OUTPUT_NAME = "Logic Outputs.Group Q%s(%s).Output Description";
    private static String OUTPUT_TRUE = "Logic Outputs.Group Q%s(%s).True";
    private static String OUTPUT_FALSE = "Logic Outputs.Group Q%s(%s).False";
    private static String RECEIVER_NAME = "Track Receivers.Rx Circuit(%s).Remote Mast Description";
    private static String RECEIVER_EVENT = "Track Receivers.Rx Circuit(%s).Link Address";
    private static String TRANSMITTER_NAME = "Track Transmitters.Tx Circuit(%s).Track Circuit Description";
    private static String TRANSMITTER_EVENT = "Track Transmitters.Tx Circuit(%s).Link Address";
    private static String GROUP_NAME = "Conditionals.Logic(%s).Group Description";
    private static String GROUP_MULTI_LINE = "Conditionals.Logic(%s).MultiLine";

    // Regex Patterns
    private static Pattern PARSE_VARIABLE = Pattern.compile("[IQYZM](\\d+)\\.(\\d+)");
    private static Pattern PARSE_LABEL = Pattern.compile("\\D\\w{0,3}:");
    private static Pattern PARSE_TIMERWORD = Pattern.compile("W#[0123]#\\d{1,3}");
    private static Pattern PARSE_TIMERVAR = Pattern.compile("T\\d{1,2}");
    private static Pattern PARSE_HEXPAIR = Pattern.compile("^[0-9a-fA-F]{2}$");
    private static Pattern PARSE_VERSION = Pattern.compile("^.*(\\d+)\\.(\\d+)$");

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
        _exportButton = new JButton(Bundle.getMessage("ButtonExport"));
        _importButton = new JButton(Bundle.getMessage("ButtonImport"));

        _refreshButton.setEnabled(false);
        _storeButton.setEnabled(false);

        _addButton.addActionListener(this::pushedAddButton);
        _insertButton.addActionListener(this::pushedInsertButton);
        _moveUpButton.addActionListener(this::pushedMoveUpButton);
        _moveDownButton.addActionListener(this::pushedMoveDownButton);
        _deleteButton.addActionListener(this::pushedDeleteButton);
        _percentButton.addActionListener(this::pushedPercentButton);
        _refreshButton.addActionListener(this::pushedRefreshButton);
        _storeButton.addActionListener(this::pushedStoreButton);
        _exportButton.addActionListener(this::pushedExportButton);
        _importButton.addActionListener(this::pushedImportButton);

        _editButtons = new JPanel();
        _editButtons.add(_addButton);
        _editButtons.add(_insertButton);
        _editButtons.add(_moveUpButton);
        _editButtons.add(_moveDownButton);
        _editButtons.add(_deleteButton);
        footer.add(_editButtons, BorderLayout.WEST);

        var dataButtons = new JPanel();
        dataButtons.add(_percentButton);
        dataButtons.add(new JLabel(" | "));
        dataButtons.add(_importButton);
        dataButtons.add(_exportButton);
        dataButtons.add(new JLabel(" | "));
        dataButtons.add(_refreshButton);
        dataButtons.add(_storeButton);
        footer.add(dataButtons, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        // Define the node selector which goes in the header
        var nodeSelector = new JPanel();
        nodeSelector.setLayout(new FlowLayout());

        _nodeBox = new JComboBox<NodeEntry>(_nodeModel);

        // Load node selector combo box
        for (MimicNodeStore.NodeMemo nodeMemo : _store.getNodeMemos() ) {
            newNodeInList(nodeMemo);
        }

        _nodeBox.addActionListener(this::nodeSelected);
        JComboBoxUtil.setupComboBoxMaxRows(_nodeBox);

        // Force combo box width
        var dim = _nodeBox.getPreferredSize();
        var newDim = new Dimension(400, (int)dim.getHeight());
        _nodeBox.setPreferredSize(newDim);

        nodeSelector.add(_nodeBox);
        add(nodeSelector, BorderLayout.NORTH);

        // Define the center section of the window which consists of 5 tabs
        _detailTabs = new JTabbedPane();

        _detailTabs.add(Bundle.getMessage("ButtonG"), buildLogicPanel());  // NOI18N
        _detailTabs.add(Bundle.getMessage("ButtonI"), buildInputPanel());  // NOI18N
        _detailTabs.add(Bundle.getMessage("ButtonQ"), buildOutputPanel());  // NOI18N
        _detailTabs.add(Bundle.getMessage("ButtonY"), buildReceiverPanel());  // NOI18N
        _detailTabs.add(Bundle.getMessage("ButtonZ"), buildTransmitterPanel());  // NOI18N

        _detailTabs.addChangeListener(this::tabSelected);

        _detailTabs.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        add(_detailTabs, BorderLayout.CENTER);

        initalizeLists();
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

        _groupTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        var  selectionModel = _groupTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(this::handleGroupRowSelection);

        return scrollPane;
    }

    private JSplitPane buildLogicPanel() {
        // Create scroll pane
        var model = new LogicModel();
        _logicTable = new JTable(model);
        var logicScrollPane = new JScrollPane(_logicTable);

        // resize columns
        for (int i = 0; i < _logicTable.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            _logicTable.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        _logicTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Use the operators combo box for the operator column
        var col = _logicTable.getColumnModel().getColumn(1);
        col.setCellEditor(new DefaultCellEditor(_operators));
        JComboBoxUtil.setupComboBoxMaxRows(_operators);

        var  selectionModel = _logicTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(this::handleLogicRowSelection);

        var logicPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildGroupPanel(), logicScrollPane);
        logicPanel.setDividerSize(10);
        logicPanel.setResizeWeight(.10);
        logicPanel.setDividerLocation(150);

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

        _inputTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

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

        _outputTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

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

        _receiverTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

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

        _transmitterTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        return scrollPane;
    }

    private void tabSelected(ChangeEvent e) {
        if (_detailTabs.getSelectedIndex() == 0) {
            _editButtons.setVisible(true);
        } else {
            _editButtons.setVisible(false);
        }
    }

    // --------------  Initialization ---------

    private void initalizeLists() {
        // Group List
        for (int i = 0; i < 16; i++) {
            _groupList.add(new GroupRow(""));
        }

        // Input List
        for (int i = 0; i < 128; i++) {
            _inputList.add(new InputRow("", "", ""));
        }

        // Output List
        for (int i = 0; i < 128; i++) {
            _outputList.add(new OutputRow("", "", ""));
        }

        // Receiver List
        for (int i = 0; i < 16; i++) {
            _receiverList.add(new ReceiverRow("", ""));
        }

        // Transmitter List
        for (int i = 0; i < 16; i++) {
            _transmitterList.add(new TransmitterRow("", ""));
        }
    }

    // --------------  Logic table methods ---------

    private void handleGroupRowSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            _groupRow = _groupTable.getSelectedRow();
            _logicTable.revalidate();
            _logicTable.repaint();
            pushedPercentButton(null);
        }
    }

    private void pushedPercentButton(ActionEvent e) {
        encode(_groupList.get(_groupRow));
        _percentButton.setText(_groupList.get(_groupRow).getSize());
    }

    private void handleLogicRowSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            _logicRow = _logicTable.getSelectedRow();
            _moveUpButton.setEnabled(_logicRow > 0);
            _moveDownButton.setEnabled(_logicRow < _logicTable.getRowCount() - 1);
        }
    }

    private void pushedAddButton(ActionEvent e) {
        var logicList = _groupList.get(_groupRow).getLogicList();
        logicList.add(new LogicRow("", null, "", ""));
        _logicRow = logicList.size() - 1;
        _logicTable.revalidate();
        _logicTable.setRowSelectionInterval(_logicRow, _logicRow);
        setDirty(true);
    }

    private void pushedInsertButton(ActionEvent e) {
        var logicList = _groupList.get(_groupRow).getLogicList();
        if (_logicRow >= 0 && _logicRow < logicList.size()) {
            logicList.add(_logicRow, new LogicRow("", null, "", ""));
            _logicTable.revalidate();
            _logicTable.setRowSelectionInterval(_logicRow, _logicRow);
        }
        setDirty(true);
    }

    private void pushedMoveUpButton(ActionEvent e) {
        var logicList = _groupList.get(_groupRow).getLogicList();
        if (_logicRow >= 0 && _logicRow < logicList.size()) {
            var logicRow = logicList.remove(_logicRow);
            logicList.add(_logicRow - 1, logicRow);
            _logicRow--;
            _logicTable.revalidate();
            _logicTable.setRowSelectionInterval(_logicRow, _logicRow);
        }
        setDirty(true);
    }

    private void pushedMoveDownButton(ActionEvent e) {
        var logicList = _groupList.get(_groupRow).getLogicList();
        if (_logicRow >= 0 && _logicRow < logicList.size()) {
            var logicRow = logicList.remove(_logicRow);
            logicList.add(_logicRow + 1, logicRow);
            _logicRow++;
            _logicTable.revalidate();
            _logicTable.setRowSelectionInterval(_logicRow, _logicRow);
        }
        setDirty(true);
    }

    private void pushedDeleteButton(ActionEvent e) {
        var logicList = _groupList.get(_groupRow).getLogicList();
        if (_logicRow >= 0 && _logicRow < logicList.size()) {
            logicList.remove(_logicRow);
            _logicTable.revalidate();
        }
        setDirty(true);
    }

    // --------------  Encode/Decode methods ---------

    private String nameToVariable(String name) {
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
                return name;

            } else {
                // Search receiver and transmitter tables
                var splitName = name.split("~");
                var baseName = splitName[0];
                var aspectName = splitName[1];
                var aspectNumber = 0;
                try {
                    aspectNumber = Integer.parseInt(aspectName);
                    if (aspectNumber < 0 || aspectNumber > 7) {
                        warningDialog(Bundle.getMessage("TitleAspect"), Bundle.getMessage("MessageAspect", aspectNumber));
                        aspectNumber = 0;
                    }
                } catch (NumberFormatException e) {
                    warningDialog(Bundle.getMessage("TitleAspect"), Bundle.getMessage("MessageAspect", aspectName));
                    aspectNumber = 0;
                }
                for (int i = 0; i < 16; i++) {
                    if (_receiverList.get(i).getName().equals(baseName)) {
                        return "Y" + i + "." + aspectNumber;
                    }
                }

                for (int i = 0; i < 16; i++) {
                    if (_transmitterList.get(i).getName().equals(baseName)) {
                        return "Z" + i + "." + aspectNumber;
                    }
                }
                return name;
            }
        }

        return null;
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
                    warningDialog(Bundle.getMessage("TitleVariable"), Bundle.getMessage("MessageVariable", variable));
                    return name;
                }

                switch (varType) {
                    case "I":
                        row = (first * 8) + second;
                        name = _inputList.get(row).getName();
                        if (name.isEmpty()) {
                            name = variable;
                        }
                        break;
                    case "Q":
                        row = (first * 8) + second;
                        name = _outputList.get(row).getName();
                        if (name.isEmpty()) {
                            name = variable;
                        }
                        break;
                    case "Y":
                        row = first;
                        name = _receiverList.get(row).getName() + "~" + second;
                        break;
                    case "Z":
                        row = first;
                        name = _transmitterList.get(row).getName() + "~" + second;
                        break;
                    default:
                        log.error("Variable '{}' has an invalid first letter (IQYZ)", variable);
               }
            }
        }

        return name;
    }

    private void encode(GroupRow groupRow) {
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
                sb.append(operName);
            }

            if (!row.getName().isEmpty()) {
                var name = row.getName().trim();

                if (jumpLabel) {
                    sb.append(" " + name);
                    jumpLabel = false;
                } else if (isMemory(name)) {
                    sb.append(" " + name);
                } else if (isTimerWord(name)) {
                    sb.append(" " + name);
                } else if (isTimerVar(name)) {
                    sb.append(" " + name);
                } else {
                    var variable = nameToVariable(name);
                    if (variable == null) {
                        JmriJOptionPane.showMessageDialog(null,
                                Bundle.getMessage("MessageBadName", groupRow.getName(), name),
                                Bundle.getMessage("TitleBadName"),
                                JmriJOptionPane.ERROR_MESSAGE);
                        log.error("bad name: {}", name);
                    } else {
                        sb.append(" " + variable);
                    }
                }
            }

            if (!row.getComment().isEmpty()) {
                var comment = row.getComment().trim();
                sb.append(" // " + comment);
            }

            sb.append("\n");

            longLine = longLine + sb.toString();
        }

        log.debug("MultiLine: {}", longLine);

        if (longLine.length() < 256) {
            groupRow.setMultiLine(longLine);
        } else {
            var overflow = longLine.substring(255);
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("MessageOverflow", groupRow.getName(), overflow),
                    Bundle.getMessage("TitleOverflow"),
                    JmriJOptionPane.ERROR_MESSAGE);
            log.error("The line overflowed, content truncated:  {}", overflow);
        }
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
        String[] lines = groupRow.getMultiLine().split("\\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                continue;
            }
            String[] tokens = lines[i].split(" ");

            var label = "";
            var name = "";
            var comment = "";
            Operator oper = null;

            boolean needOperator = true;

            for (int j = 0; j < tokens.length; j++) {
                var token = tokens[j];

                // Get label
                if (j == 0) {
                    var match = PARSE_LABEL.matcher(token);
                    if (match.find()) {
                        label = token;
                        continue;
                    }
                }

                // Get operator
                if (needOperator) {
                    oper = getEnum(token);
                    if (oper != null) {
                        needOperator = false;
                        continue;
                    }
                }

                // Get comment
                if (token.equals("//")) {
                    int commentPosition = lines[i].indexOf("//");
                    comment = lines[i].substring(commentPosition + 3);
                    break;
                }

                // Get name
                if (oper != null) {
                    if (oper.name().startsWith("J")) {   // Jump label
                        name = token;
                    } else if (isMemory(token)) {  // Memory variable
                        name = token;
                    } else if (isTimerWord(token)) { // Load timer
                        name = token;
                    } else if (isTimerVar(token)) {  // Timer variable
                        name = token;
                    } else {
                        var match = PARSE_VARIABLE.matcher(token);
                        if (match.find()) {
                            name = variableToName(token);
                        } else {
                            name = token;
                        }
                    }
                }
            }

            var logic = new LogicRow(label, oper, name, comment);
            groupRow.getLogicList().add(logic);
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

    private void nodeSelected(ActionEvent e) {
        NodeEntry node = (NodeEntry) _nodeBox.getSelectedItem();
        log.debug("nodeSelected: {}", node);

        if (isValidNodeVersionNumber(node.getNodeMemo())) {
            _cdi = _iface.getConfigForNode(node.getNodeID());
            if (_cdi.getRoot() != null) {
                loadDone();
            } else {
                JmriJOptionPane.showMessageDialogNonModal(this,
                        Bundle.getMessage("MessageCdiLoad", node),
                        Bundle.getMessage("TitleCdiLoad"),
                        JmriJOptionPane.INFORMATION_MESSAGE,
                        null);
                _cdi.addPropertyChangeListener(new CdiListener());
            }
        }
    }

    public class CdiListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            log.debug("Event = {}", propertyName);

            if (propertyName.equals("UPDATE_CACHE_COMPLETE")) {
                Window[] windows = Window.getWindows();
                for (Window window : windows) {
                    if (window instanceof JDialog) {
                        JDialog dialog = (JDialog) window;
                        if (dialog.getTitle().equals(Bundle.getMessage("TitleCdiLoad"))) {
                            dialog.dispose();
                        }
                    }
                }
                loadDone();
            }
        }
    }

    private void loadDone() {
        loadCdiData();
    }

    private void newNodeInList(MimicNodeStore.NodeMemo nodeMemo) {
        // Filter for Tower LCC+Q
        NodeID node = nodeMemo.getNodeID();
        String id = node.toString();
        log.debug("node id: {}", id);
        if (!id.startsWith("02.01.57.4")) {
            return;
        }

        int i = 0;
        if (_nodeModel.getIndexOf(nodeMemo.getNodeID()) >= 0) {
            // already exists. Do nothing.
            return;
        }
        NodeEntry e = new NodeEntry(nodeMemo);

        while ((i < _nodeModel.getSize()) && (_nodeModel.getElementAt(i).compareTo(e) < 0)) {
            ++i;
        }
        _nodeModel.insertElementAt(e, i);
    }

    private boolean isValidNodeVersionNumber(MimicNodeStore.NodeMemo nodeMemo) {
        SimpleNodeIdent ident = nodeMemo.getSimpleNodeIdent();
        String versionString = ident.getSoftwareVersion();

        int version = 0;
        var match = PARSE_VERSION.matcher(versionString);
        if (match.find()) {
            var major = match.group(1);
            var minor = match.group(2);
            version = Integer.parseInt(major + minor);
        }

        if (version < TOWER_LCC_Q_NODE_VERSION) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("MessageVersion",
                            nodeMemo.getNodeID(),
                            versionString,
                            TOWER_LCC_Q_NODE_VERSION_STRING),
                    Bundle.getMessage("TitleVersion"),
                    JmriJOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    // Notifies that the contents of a given entry have changed. This will delete and re-add the
    // entry to the model, forcing a refresh of the box.
    public void updateComboBoxModelEntry(NodeEntry nodeEntry) {
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

    protected static class NodeEntry implements Comparable<NodeEntry>, PropertyChangeListener {
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
        public NodeEntry(String description) {
            this.nodeMemo = null;
            this.description = description;
        }

        public NodeID getNodeID() {
            return nodeMemo.getNodeID();
        }

        MimicNodeStore.NodeMemo getNodeMemo() {
            return nodeMemo;
        }

        private void updateDescription() {
            SimpleNodeIdent ident = nodeMemo.getSimpleNodeIdent();
            StringBuilder sb = new StringBuilder();
            sb.append(nodeMemo.getNodeID().toString());

            addToDescription(ident.getUserName(), sb);
            addToDescription(ident.getUserDesc(), sb);
            if (!ident.getMfgName().isEmpty() || !ident.getModelName().isEmpty()) {
                addToDescription(ident.getMfgName() + " " +ident.getModelName(), sb);
            }
            addToDescription(ident.getSoftwareVersion(), sb);
            String newDescription = sb.toString();
            if (!description.equals(newDescription)) {
                description = newDescription;
            }
        }

        private void addToDescription(String s, StringBuilder sb) {
            if (!s.isEmpty()) {
                sb.append(" - ");
                sb.append(s);
            }
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

    // --------------  load CDI data ---------

    private void loadCdiData() {
        if (!replaceData()) {
            return;
        }

        // Load data
        loadCdiInputs();
        loadCdiOutputs();
        loadCdiReceivers();
        loadCdiTransmitters();
        loadCdiGroups();

        for (GroupRow row : _groupList) {
            decode(row);
        }

        setDirty(false);

        _groupTable.setRowSelectionInterval(0, 0);

        _groupTable.repaint();

        _exportButton.setEnabled(true);
        _refreshButton.setEnabled(true);
        _storeButton.setEnabled(true);
        _exportItem.setEnabled(true);
        _refreshItem.setEnabled(true);
        _storeItem.setEnabled(true);
    }

    private void pushedRefreshButton(ActionEvent e) {
        loadCdiData();
    }

    private void loadCdiGroups() {
        for (int i = 0; i < 16; i++) {
            var groupRow = _groupList.get(i);
            groupRow.clearLogicList();

            var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(GROUP_NAME, i));
            groupRow.setName(entry.getValue());
            entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(GROUP_MULTI_LINE, i));
            groupRow.setMultiLine(entry.getValue());
        }

        _groupTable.revalidate();
    }

    private void loadCdiInputs() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var inputRow = _inputList.get((i * 8) + j);

                var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(INPUT_NAME, i, j));
                inputRow.setName(entry.getValue());
                var event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(INPUT_TRUE, i, j));
                inputRow.setEventTrue(event.getValue().toShortString());
                event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(INPUT_FALSE, i, j));
                inputRow.setEventFalse(event.getValue().toShortString());
            }
        }
        _inputTable.revalidate();
    }

    private void loadCdiOutputs() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var outputRow = _outputList.get((i * 8) + j);

                var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(OUTPUT_NAME, i, j));
                outputRow.setName(entry.getValue());
                var event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(OUTPUT_TRUE, i, j));
                outputRow.setEventTrue(event.getValue().toShortString());
                event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(OUTPUT_FALSE, i, j));
                outputRow.setEventFalse(event.getValue().toShortString());
            }
        }
        _outputTable.revalidate();
    }

    private void loadCdiReceivers() {
        for (int i = 0; i < 16; i++) {
            var receiverRow = _receiverList.get(i);

            var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(RECEIVER_NAME, i));
            receiverRow.setName(entry.getValue());
            var event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(RECEIVER_EVENT, i));
            receiverRow.setEventId(event.getValue().toShortString());
        }
        _receiverTable.revalidate();
    }

    private void loadCdiTransmitters() {
        for (int i = 0; i < 16; i++) {
            var transmitterRow = _transmitterList.get(i);

            var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(TRANSMITTER_NAME, i));
            transmitterRow.setName(entry.getValue());
            var event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(TRANSMITTER_EVENT, i));
            transmitterRow.setEventId(event.getValue().toShortString());
        }
        _transmitterTable.revalidate();
    }

    // --------------  store CDI data ---------

    private void pushedStoreButton(ActionEvent e) {
        _csvMessages.clear();

        // Store CDI data
        storeInputs();
        storeOutputs();
        storeReceivers();
        storeTransmitters();
        storeGroups();

        setDirty(false);

        _csvMessages.add(Bundle.getMessage("StoreDone"));
        var msgType = JmriJOptionPane.ERROR_MESSAGE;
        if (_csvMessages.size() == 1) {
            msgType = JmriJOptionPane.INFORMATION_MESSAGE;
        }
        JmriJOptionPane.showMessageDialog(this,
                String.join("\n", _csvMessages),
                Bundle.getMessage("TitleCdiStore"),
                msgType);
    }

    private void storeGroups() {
        // store the group data
        for (int i = 0; i < 16; i++) {
            var row = _groupList.get(i);

            // update the group line
            encode(row);

            var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(GROUP_NAME, i));
            entry.setValue(row.getName());
            entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(GROUP_MULTI_LINE, i));
            entry.setValue(row.getMultiLine());

            log.debug("Group: {}", row.getName());
            log.debug("Logic: {}", row.getMultiLine());
        }
    }

    private void storeInputs() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var row = _inputList.get((i * 8) + j);

                var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(INPUT_NAME, i, j));
                entry.setValue(row.getName());
                var event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(INPUT_TRUE, i, j));
                event.setValue(new EventID(row.getEventTrue()));
                event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(INPUT_FALSE, i, j));
                event.setValue(new EventID(row.getEventFalse()));
            }
        }
    }

    private void storeOutputs() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var row = _outputList.get((i * 8) + j);

                var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(OUTPUT_NAME, i, j));
                entry.setValue(row.getName());
                var event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(OUTPUT_TRUE, i, j));
                event.setValue(new EventID(row.getEventTrue()));
                event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(OUTPUT_FALSE, i, j));
                event.setValue(new EventID(row.getEventFalse()));
            }
        }
    }

    private void storeReceivers() {
        for (int i = 0; i < 16; i++) {
            var row = _receiverList.get(i);

            var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(RECEIVER_NAME, i));
            entry.setValue(row.getName());
            var event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(RECEIVER_EVENT, i));
            event.setValue(new EventID(row.getEventId()));
        }
    }

    private void storeTransmitters() {
        for (int i = 0; i < 16; i++) {
            var row = _transmitterList.get(i);

            var entry = (ConfigRepresentation.StringEntry) _cdi.getVariableForKey(String.format(TRANSMITTER_NAME, i));
            entry.setValue(row.getName());
            var event = (ConfigRepresentation.EventEntry) _cdi.getVariableForKey(String.format(TRANSMITTER_EVENT, i));
            event.setValue(new EventID(row.getEventId()));
        }
    }

    // --------------  Backup Import ---------

    private void loadBackupData(ActionEvent m) {
        if (!replaceData()) {
            return;
        }

        var fileChooser = new JmriJFileChooser(FileUtil.getUserFilesPath());
        fileChooser.setApproveButtonText(Bundle.getMessage("LoadCdiButton"));
        fileChooser.setDialogTitle(Bundle.getMessage("LoadCdiTitle"));
        var filter = new FileNameExtensionFilter(Bundle.getMessage("LoadCdiFilter"), "txt");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);

        int response = fileChooser.showOpenDialog(this);
        if (response == JFileChooser.CANCEL_OPTION) {
            return;
        }

        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(fileChooser.getSelectedFile().getAbsolutePath()));
        } catch (IOException e) {
            log.error("Failed to load file.", e);
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("Logic Inputs.Group")) {
                loadBackupInputs(i, lines);
                i += 128 * 3;
            }

            if (lines.get(i).startsWith("Logic Outputs.Group")) {
                loadBackupOutputs(i, lines);
                i += 128 * 3;
            }
            if (lines.get(i).startsWith("Track Receivers")) {
                loadBackupReceivers(i, lines);
                i += 16 * 2;
            }
            if (lines.get(i).startsWith("Track Transmitters")) {
                loadBackupTransmitters(i, lines);
                i += 16 * 2;
            }
            if (lines.get(i).startsWith("Conditionals.Logic")) {
                loadBackupGroups(i, lines);
                i += 16 * 2;
            }
        }

        for (GroupRow row : _groupList) {
            decode(row);
        }

        setDirty(false);
        _groupTable.setRowSelectionInterval(0, 0);
        _groupTable.repaint();

        _exportButton.setEnabled(true);
        _exportItem.setEnabled(true);
    }

    private String getLineValue(String line) {
        if (line.endsWith("=")) {
            return "";
        }
        int index = line.indexOf("=");
        var newLine = line.substring(index + 1);
        newLine = Util.unescapeString(newLine);
        return newLine;
    }

    private void loadBackupInputs(int index, List<String> lines) {
        for (int i = 0; i < 128; i++) {
            var inputRow = _inputList.get(i);

            inputRow.setName(getLineValue(lines.get(index)));
            inputRow.setEventTrue(getLineValue(lines.get(index + 1)));
            inputRow.setEventFalse(getLineValue(lines.get(index + 2)));
            index += 3;
        }

        _inputTable.revalidate();
    }

    private void loadBackupOutputs(int index, List<String> lines) {
        for (int i = 0; i < 128; i++) {
            var outputRow = _outputList.get(i);

            outputRow.setName(getLineValue(lines.get(index)));
            outputRow.setEventTrue(getLineValue(lines.get(index + 1)));
            outputRow.setEventFalse(getLineValue(lines.get(index + 2)));
            index += 3;
        }

        _outputTable.revalidate();
    }

    private void loadBackupReceivers(int index, List<String> lines) {
        for (int i = 0; i < 16; i++) {
            var receiverRow = _receiverList.get(i);

            receiverRow.setName(getLineValue(lines.get(index)));
            receiverRow.setEventId(getLineValue(lines.get(index + 1)));
            index += 2;
        }

        _receiverTable.revalidate();
    }

    private void loadBackupTransmitters(int index, List<String> lines) {
        for (int i = 0; i < 16; i++) {
            var transmitterRow = _transmitterList.get(i);

            transmitterRow.setName(getLineValue(lines.get(index)));
            transmitterRow.setEventId(getLineValue(lines.get(index + 1)));
            index += 2;
        }

        _transmitterTable.revalidate();
    }

    private void loadBackupGroups(int index, List<String> lines) {
        for (int i = 0; i < 16; i++) {
            var groupRow = _groupList.get(i);
            groupRow.clearLogicList();

            groupRow.setName(getLineValue(lines.get(index)));
            groupRow.setMultiLine(getLineValue(lines.get(index + 1)));
            index += 2;
        }

        _groupTable.revalidate();
        _logicTable.revalidate();
    }

    // --------------  CSV Import ---------

    private void pushedImportButton(ActionEvent e) {
        if (!replaceData()) {
            return;
        }

        _csvMessages.clear();
        importCsvData();
        setDirty(false);

        _exportButton.setEnabled(true);
        _exportItem.setEnabled(true);

        if (!_csvMessages.isEmpty()) {
            JmriJOptionPane.showMessageDialog(this,
                    String.join("\n", _csvMessages),
                    Bundle.getMessage("TitleCsvImport"),
                    JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    private void importCsvData() {
        if (!setCsvDirectoryPath(true)) {
            return;
        }

        importGroupLogic();
        importInputs();
        importOutputs();
        importReceivers();
        importTransmitters();

        _groupTable.setRowSelectionInterval(0, 0);

        _groupTable.repaint();
    }

    private void importGroupLogic() {
        List<CSVRecord> records = getCsvRecords("group_logic.csv");
        if (records.isEmpty()) {
            return;
        }

        var skipHeader = true;
        int groupNumber = -1;
        for (CSVRecord record : records) {
            if (skipHeader) {
                skipHeader = false;
                continue;
            }

            List<String> values = new ArrayList<>();
            record.forEach(values::add);

            if (values.size() == 1) {
                // Create Group
                groupNumber++;
                var groupRow = _groupList.get(groupNumber);
                groupRow.setName(values.get(0));
                groupRow.setMultiLine("");
                groupRow.clearLogicList();
            } else if (values.size() == 5) {
                var oper = getEnum(values.get(2));
                var logicRow = new LogicRow(values.get(1), oper, values.get(3), values.get(4));
                _groupList.get(groupNumber).getLogicList().add(logicRow);
            } else {
                _csvMessages.add(Bundle.getMessage("ImportGroupError", record.toString()));
            }
        }

        _groupTable.revalidate();
        _logicTable.revalidate();
    }

    private void importInputs() {
        List<CSVRecord> records = getCsvRecords("inputs.csv");
        if (records.isEmpty()) {
            return;
        }

        for (int i = 0; i < 129; i++) {
            if (i == 0) {
                continue;
            }

            var record = records.get(i);
            List<String> values = new ArrayList<>();
            record.forEach(values::add);

            if (values.size() == 4) {
                var inputRow = _inputList.get(i - 1);
                inputRow.setName(values.get(1));
                inputRow.setEventTrue(values.get(2));
                inputRow.setEventFalse(values.get(3));
            } else {
                _csvMessages.add(Bundle.getMessage("ImportInputError", record.toString()));
            }
        }

        _inputTable.revalidate();
    }

    private void importOutputs() {
        List<CSVRecord> records = getCsvRecords("outputs.csv");
        if (records.isEmpty()) {
            return;
        }

        for (int i = 0; i < 17; i++) {
            if (i == 0) {
                continue;
            }

            var record = records.get(i);
            List<String> values = new ArrayList<>();
            record.forEach(values::add);

            if (values.size() == 4) {
                var outputRow = _outputList.get(i - 1);
                outputRow.setName(values.get(1));
                outputRow.setEventTrue(values.get(2));
                outputRow.setEventFalse(values.get(3));
            } else {
                _csvMessages.add(Bundle.getMessage("ImportOuputError", record.toString()));
            }
        }

        _outputTable.revalidate();
    }

    private void importReceivers() {
        List<CSVRecord> records = getCsvRecords("receivers.csv");
        if (records.isEmpty()) {
            return;
        }

        for (int i = 0; i < 17; i++) {
            if (i == 0) {
                continue;
            }

            var record = records.get(i);
            List<String> values = new ArrayList<>();
            record.forEach(values::add);

            if (values.size() == 3) {
                var receiverRow = _receiverList.get(i - 1);
                receiverRow.setName(values.get(1));
                receiverRow.setEventId(values.get(2));
            } else {
                _csvMessages.add(Bundle.getMessage("ImportReceiverError", record.toString()));
            }
        }

        _receiverTable.revalidate();
    }

    private void importTransmitters() {
        List<CSVRecord> records = getCsvRecords("transmitters.csv");
        if (records.isEmpty()) {
            return;
        }

        for (int i = 0; i < 17; i++) {
            if (i == 0) {
                continue;
            }

            var record = records.get(i);
            List<String> values = new ArrayList<>();
            record.forEach(values::add);

            if (values.size() == 3) {
                var transmitterRow = _transmitterList.get(i - 1);
                transmitterRow.setName(values.get(1));
                transmitterRow.setEventId(values.get(2));
            } else {
                _csvMessages.add(Bundle.getMessage("ImportTransmitterError", record.toString()));
            }
        }

        _transmitterTable.revalidate();
    }

    private List<CSVRecord> getCsvRecords(String fileName) {
        var recordList = new ArrayList<CSVRecord>();
        FileReader fileReader;
        try {
            fileReader = new FileReader(_csvDirectoryPath + fileName);
        } catch (FileNotFoundException ex) {
            _csvMessages.add(Bundle.getMessage("ImportFileNotFound", fileName));
            return recordList;
        }

        BufferedReader bufferedReader;
        CSVParser csvFile;

        try {
            bufferedReader = new BufferedReader(fileReader);
            csvFile = new CSVParser(bufferedReader, CSVFormat.DEFAULT);
            recordList.addAll(csvFile.getRecords());
            csvFile.close();
            bufferedReader.close();
            fileReader.close();
        } catch (IOException iox) {
            _csvMessages.add(Bundle.getMessage("ImportFileIOError", iox.getMessage(), fileName));
        }

        return recordList;
    }

    // --------------  CSV Export ---------

    private void pushedExportButton(ActionEvent e) {
        _csvMessages.clear();
        exportCsvData();
        setDirty(false);

        _csvMessages.add(Bundle.getMessage("ExportDone"));
        var msgType = JmriJOptionPane.ERROR_MESSAGE;
        if (_csvMessages.size() == 1) {
            msgType = JmriJOptionPane.INFORMATION_MESSAGE;
        }
        JmriJOptionPane.showMessageDialog(this,
                String.join("\n", _csvMessages),
                Bundle.getMessage("TitleCsvExport"),
                msgType);
    }

    private void exportCsvData() {
        if (!setCsvDirectoryPath(false)) {
            return;
        }

        try {
            exportGroupLogic();
            exportInputs();
            exportOutputs();
            exportReceivers();
            exportTransmitters();
        } catch (IOException ex) {
            _csvMessages.add(Bundle.getMessage("ExportIOError", ex.getMessage()));
        }

    }

    private void exportGroupLogic() throws IOException {
        var fileWriter = new FileWriter(_csvDirectoryPath + "group_logic.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("GroupName"), Bundle.getMessage("ColumnLabel"),
                 Bundle.getMessage("ColumnOper"), Bundle.getMessage("ColumnName"), Bundle.getMessage("ColumnComment"));

        for (int i = 0; i < 16; i++) {
            var row = _groupList.get(i);
            var groupName = row.getName();
            csvFile.printRecord(groupName);
            var logicRow = row.getLogicList();
            for (LogicRow logic : logicRow) {
                var operName = logic.getOperName();
                csvFile.printRecord("", logic.getLabel(), operName, logic.getName(), logic.getComment());
            }
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    private void exportInputs() throws IOException {
        var fileWriter = new FileWriter(_csvDirectoryPath + "inputs.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("ColumnInput"), Bundle.getMessage("ColumnName"),
                 Bundle.getMessage("ColumnTrue"), Bundle.getMessage("ColumnFalse"));

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var variable = "I" + i + "." + j;
                var row = _inputList.get((i * 8) + j);
                csvFile.printRecord(variable, row.getName(), row.getEventTrue(), row.getEventFalse());
            }
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    private void exportOutputs() throws IOException {
        var fileWriter = new FileWriter(_csvDirectoryPath + "outputs.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("ColumnOutput"), Bundle.getMessage("ColumnName"),
                 Bundle.getMessage("ColumnTrue"), Bundle.getMessage("ColumnFalse"));

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var variable = "Q" + i + "." + j;
                var row = _outputList.get((i * 8) + j);
                csvFile.printRecord(variable, row.getName(), row.getEventTrue(), row.getEventFalse());
            }
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    private void exportReceivers() throws IOException {
        var fileWriter = new FileWriter(_csvDirectoryPath + "receivers.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("ColumnCircuit"), Bundle.getMessage("ColumnName"),
                 Bundle.getMessage("ColumnEventID"));

        for (int i = 0; i < 16; i++) {
            var variable = "Y" + i;
            var row = _receiverList.get(i);
            csvFile.printRecord(variable, row.getName(), row.getEventId());
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    private void exportTransmitters() throws IOException {
        var fileWriter = new FileWriter(_csvDirectoryPath + "transmitters.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("ColumnCircuit"), Bundle.getMessage("ColumnName"),
                 Bundle.getMessage("ColumnEventID"));

        for (int i = 0; i < 16; i++) {
            var variable = "Z" + i;
            var row = _transmitterList.get(i);
            csvFile.printRecord(variable, row.getName(), row.getEventId());
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    /**
     * Select the directory that will be used for the CSV file set.
     * @param isOpen - True for CSV Import and false for CSV Export.
     * @return true if a directory was selected.
     */
    private boolean setCsvDirectoryPath(boolean isOpen) {
        var directoryChooser = new JmriJFileChooser(FileUtil.getUserFilesPath());
        directoryChooser.setApproveButtonText(Bundle.getMessage("SelectCsvButton"));
        directoryChooser.setDialogTitle(Bundle.getMessage("SelectCsvTitle"));
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int response = 0;
        if (isOpen) {
            response = directoryChooser.showOpenDialog(this);
        } else {
            response = directoryChooser.showSaveDialog(this);
        }
        if (response != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        _csvDirectoryPath = directoryChooser.getSelectedFile().getAbsolutePath() + FileUtil.SEPARATOR;

        return true;
    }

    // --------------  Data Utilities ---------

    private void setDirty(boolean dirty) {
        _dirty = dirty;
    }

    private boolean isDirty() {
        return _dirty;
    }

    private boolean replaceData() {
        if (isDirty()) {
            int response = JmriJOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("MessageRevert"),
                    Bundle.getMessage("TitleRevert"),
                    JmriJOptionPane.YES_NO_OPTION);
            if (response != JmriJOptionPane.YES_OPTION) {
                return false;
            }
        }
        return true;
    }

    private void warningDialog(String title, String message) {
        JmriJOptionPane.showMessageDialog(this,
            message,
            title,
            JmriJOptionPane.WARNING_MESSAGE);
    }

    // --------------  Data validation ---------

    static boolean isLabelValid(String label) {
        if (label.isEmpty()) {
            return true;
        }

        var match = PARSE_LABEL.matcher(label);
        if (match.find()) {
            return true;
        }

        JmriJOptionPane.showMessageDialog(null,
                Bundle.getMessage("MessageLabel", label),
                Bundle.getMessage("TitleLabel"),
                JmriJOptionPane.ERROR_MESSAGE);
        return false;
    }

    static boolean isEventValid(String event) {
        var valid = true;

        if (event.isEmpty()) {
            return valid;
        }

        var hexPairs = event.split("\\.");
        if (hexPairs.length != 8) {
            valid = false;
        } else {
            for (int i = 0; i < 8; i++) {
                var match = PARSE_HEXPAIR.matcher(hexPairs[i]);
                if (!match.find()) {
                    valid = false;
                    break;
                }
            }
        }

        if (!valid) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("MessageEvent", event),
                    Bundle.getMessage("TitleEvent"),
                    JmriJOptionPane.ERROR_MESSAGE);
            log.error("bad event: {}", event);
        }

        return valid;
    }

    // --------------  table lists ---------

    /**
     * The Group row contains the name and the raw data for one of the 16 groups.
     * It also contains the decoded logic for the group in the logic list.
     */
    static class GroupRow {
        String _name;
        String _multiLine = "";
        List<LogicRow> _logicList = new ArrayList<>();


        GroupRow(String name) {
            _name = name;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
        }

        List<LogicRow> getLogicList() {
            return _logicList;
        }

        void setLogicList(List<LogicRow> logicList) {
            _logicList.clear();
            _logicList.addAll(logicList);
        }

        void clearLogicList() {
            _logicList.clear();
        }

        String getMultiLine() {
            return _multiLine;
        }

        void setMultiLine(String newMultiLine) {
            _multiLine = newMultiLine.strip();
        }

        String getSize() {
            int size = (_multiLine.length() * 100) / 255;
            return String.valueOf(size) + "%";
        }
    }

    /**
     * The definition of a logic row
     */
    static class LogicRow {
        String _label;
        Operator _oper;
        String _name;
        String _comment;

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
            var label = newLabel.trim();
            if (isLabelValid(label)) {
                _label = label;
            }
        }

        Operator getOper() {
            return _oper;
        }

        String getOperName() {
            if (_oper == null) {
                return "";
            }

            String operName = _oper.name();

            // Fix special enums
            if (operName.equals("Cp")) {
                operName = ")";
            } else if (operName.equals("EQ")) {
                operName = "=";
            } else if (operName.contains("p")) {
                operName = operName.replace("p", "(");
            }

            return operName;
        }

        void setOper(Operator newOper) {
            _oper = newOper;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName.trim();
        }

        String getComment() {
            return _comment;
        }

        void setComment(String newComment) {
            _comment = newComment;
        }
    }

    /**
     * The name and assigned true and false events for an Input.
     */
    static class InputRow {
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
            _name = newName.trim();
        }

        String getEventTrue() {
            if (_eventTrue.length() == 0) return "00.00.00.00.00.00.00.00";
            return _eventTrue;
        }

        void setEventTrue(String newEventTrue) {
            var event = newEventTrue.trim();
            if (isEventValid(event)) {
                _eventTrue = event;
            }
        }

        String getEventFalse() {
            if (_eventFalse.length() == 0) return "00.00.00.00.00.00.00.00";
            return _eventFalse;
        }

        void setEventFalse(String newEventFalse) {
            var event = newEventFalse.trim();
            if (isEventValid(event)) {
                _eventFalse = event;
            }
        }
    }

    /**
     * The name and assigned true and false events for an Output.
     */
    static class OutputRow {
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
            _name = newName.trim();
        }

        String getEventTrue() {
            if (_eventTrue.length() == 0) return "00.00.00.00.00.00.00.00";
            return _eventTrue;
        }

        void setEventTrue(String newEventTrue) {
            var event = newEventTrue.trim();
            if (isEventValid(event)) {
                _eventTrue = event;
            }
        }

        String getEventFalse() {
            if (_eventFalse.length() == 0) return "00.00.00.00.00.00.00.00";
            return _eventFalse;
        }

        void setEventFalse(String newEventFalse) {
            var event = newEventFalse.trim();
            if (isEventValid(event)) {
                _eventFalse = event;
            }
        }
    }

    /**
     * The name and assigned event id for a circuit receiver.
     */
    static class ReceiverRow {
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
            _name = newName.trim();
        }

        String getEventId() {
            if (_eventid.length() == 0) return "00.00.00.00.00.00.00.00";
            return _eventid;
        }

        void setEventId(String newEventid) {
            var event = newEventid.trim();
            if (isEventValid(event)) {
                _eventid = event;
            }
        }
    }

    /**
     * The name and assigned event id for a circuit transmitter.
     */
    static class TransmitterRow {
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
            _name = newName.trim();
        }

        String getEventId() {
            if (_eventid.length() == 0) return "00.00.00.00.00.00.00.00";
            return _eventid;
        }

        void setEventId(String newEventid) {
            var event = newEventid.trim();
            if (isEventValid(event)) {
                _eventid = event;
            }
        }
    }

    // --------------  table models ---------

    /**
     * TableModel for Group table entries.
     */
    class GroupModel extends AbstractTableModel {

        GroupModel() {
        }

        public static final int ROW_COLUMN = 0;
        public static final int NAME_COLUMN = 1;

        @Override
        public int getRowCount() {
            return _groupList.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case ROW_COLUMN:
                    return "";
                case NAME_COLUMN:
                    return Bundle.getMessage("ColumnName");
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case ROW_COLUMN:
                    return r + 1;
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
                    setDirty(true);
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
                case ROW_COLUMN:
                    return new JTextField(4).getPreferredSize().width;
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

        @Override
        public int getRowCount() {
            var logicList = _groupList.get(_groupRow).getLogicList();
            return logicList.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
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
                default:
                    return "unknown";  // NOI18N
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            var logicList = _groupList.get(_groupRow).getLogicList();
            switch (c) {
                case LABEL_COLUMN:
                    return logicList.get(r).getLabel();
                case OPER_COLUMN:
                    return logicList.get(r).getOper();
                case NAME_COLUMN:
                    return logicList.get(r).getName();
                case COMMENT_COLUMN:
                    return logicList.get(r).getComment();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            var logicList = _groupList.get(_groupRow).getLogicList();
            switch (c) {
                case LABEL_COLUMN:
                    logicList.get(r).setLabel((String) type);
                    setDirty(true);
                    break;
                case OPER_COLUMN:
                    var z = (Operator) type;
                    if (z != null) {
                        if (z.name().startsWith("z")) {
                            return;
                        }
                        if (z.name().equals("x0")) {
                            logicList.get(r).setOper(null);
                            return;
                        }
                    }
                    logicList.get(r).setOper((Operator) type);
                    setDirty(true);
                    break;
                case NAME_COLUMN:
                    logicList.get(r).setName((String) type);
                    setDirty(true);
                    break;
                case COMMENT_COLUMN:
                    logicList.get(r).setComment((String) type);
                    setDirty(true);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return true;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case LABEL_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case OPER_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                case NAME_COLUMN:
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
                    setDirty(true);
                    break;
                case TRUE_COLUMN:
                    _inputList.get(r).setEventTrue((String) type);
                    setDirty(true);
                    break;
                case FALSE_COLUMN:
                    _inputList.get(r).setEventFalse((String) type);
                    setDirty(true);
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
                    return Bundle.getMessage("ColumnOutput");  // NOI18N
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
                    setDirty(true);
                    break;
                case TRUE_COLUMN:
                    _outputList.get(r).setEventTrue((String) type);
                    setDirty(true);
                    break;
                case FALSE_COLUMN:
                    _outputList.get(r).setEventFalse((String) type);
                    setDirty(true);
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
                    setDirty(true);
                    break;
                case EVENTID_COLUMN:
                    _receiverList.get(r).setEventId((String) type);
                    setDirty(true);
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
                    setDirty(true);
                    break;
                case EVENTID_COLUMN:
                    _transmitterList.get(r).setEventId((String) type);
                    setDirty(true);
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
    public java.util.List<JMenu> getMenus() {
        // create a file menu
        var retval = new ArrayList<JMenu>();
        var fileMenu = new JMenu(Bundle.getMessage("MenuFile"));

        _refreshItem = new JMenuItem(Bundle.getMessage("MenuRefresh"));
        _storeItem = new JMenuItem(Bundle.getMessage("MenuStore"));
        _importItem = new JMenuItem(Bundle.getMessage("MenuImport"));
        _exportItem = new JMenuItem(Bundle.getMessage("MenuExport"));
        _loadItem = new JMenuItem(Bundle.getMessage("MenuLoad"));

        _refreshItem.addActionListener(this::pushedRefreshButton);
        _storeItem.addActionListener(this::pushedStoreButton);
        _importItem.addActionListener(this::pushedImportButton);
        _exportItem.addActionListener(this::pushedExportButton);
        _loadItem.addActionListener(this::loadBackupData);

        fileMenu.add(_refreshItem);
        fileMenu.add(_storeItem);
        fileMenu.addSeparator();
        fileMenu.add(_importItem);
        fileMenu.add(_exportItem);
        fileMenu.addSeparator();
        fileMenu.add(_loadItem);

        _refreshItem.setEnabled(false);
        _storeItem.setEnabled(false);
        _exportItem.setEnabled(false);

        retval.add(fileMenu);
        return retval;
    }

    @Override
    public void dispose() {
        // and complete this
        super.dispose();
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.openlcb.swing.stleditor.StlEditorPane";
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
