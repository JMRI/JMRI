package jmri.jmrix.openlcb.swing.stleditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
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
 * Pane for editing STL logic.
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

    JLabel statusField;

    JComboBox<NodeEntry> box;
    private DefaultComboBoxModel<NodeEntry> model = new DefaultComboBoxModel<NodeEntry>();

    private List<LogicList> _logicList;
    private List<InputList> _inputList;
    private List<OutputList> _outputList;
    private List<ReceiverList> _receiverList;
    private List<TransmitterList> _transmitterList;

    public StlEditorPane() {
    }

    public String getTitle(String menuTitle) {
        return Bundle.getMessage("TitleMemoryTool");
    }

    @Override
    public boolean isMultipleInstances() {
        return false;
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        _canMemo = memo;
        _iface = memo.get(OlcbInterface.class);
        _store = memo.get(MimicNodeStore.class);

        EventTable stdEventTable = _canMemo.get(OlcbInterface.class).getEventTable();
        if (stdEventTable == null) {
            log.error("no OLCB EventTable found");
            return;
        }

        // Add to GUI here
        setLayout(new BorderLayout());
        var header = new JPanel();
        add(header, BorderLayout.NORTH);

        var body = new JPanel();
        add(body, BorderLayout.CENTER);

        var footer = new JPanel();
        footer.add(new JButton("Store"));
        add(footer, BorderLayout.SOUTH);

        // Define the node selector which goes in the header
        var nodeSelector = new JPanel();
        nodeSelector.setLayout(new FlowLayout());
        header.add(nodeSelector);

        box = new JComboBox<NodeEntry>(model);
        nodeSelector.add(box);

        for (MimicNodeStore.NodeMemo nodeMemo : _store.getNodeMemos() ) {
            newNodeInList(nodeMemo);
        }

        box.addActionListener(this::nodeSelected);
        JComboBoxUtil.setupComboBoxMaxRows(box);

        // Define the body which consists of 5 tab
        JTabbedPane detailTabs = new JTabbedPane();

        detailTabs.add(Bundle.getMessage("ButtonStl"), buildLogicPanel());  // NOI18N
        detailTabs.add(Bundle.getMessage("ButtonI"), buildInputPanel());  // NOI18N
        detailTabs.add(Bundle.getMessage("ButtonQ"), buildOutputPanel());  // NOI18N
        detailTabs.add(Bundle.getMessage("ButtonY"), buildReceiverPanel());  // NOI18N
        detailTabs.add(Bundle.getMessage("ButtonZ"), buildTransmitterPanel());  // NOI18N

        body.add(detailTabs);

        body.setBorder(BorderFactory.createLineBorder(Color.green, 2));
        detailTabs.setBorder(BorderFactory.createLineBorder(Color.blue));

        setReady(false);
    }

    // --------------  tab configurations ---------

    private JPanel buildLogicPanel() {
        var panel = new JPanel();
//         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Load list data
        _logicList = new ArrayList<>();
        _logicList.add(new LogicList("L001:", "A", "O-E14 /* Check for block occupancy */", "I0.3"));
        _logicList.add(new LogicList("", "R", "XYZ-AS", "Q1.1"));
        _logicList.add(new LogicList("", "JU", "L009", ""));
        _logicList.add(new LogicList("L009:", "", "", ""));

        var model = new LogicModel();
        var table = new JTable(model);
        table.setRowSelectionAllowed(false);
        var scrollpane = new JScrollPane(table);
        panel.add(scrollpane);

        // resize columns
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        // Force panel width
//         var fix = new JPanel();
//             var label = new JLabel("-");
//             var dim = new Dimension(100, 10);
//             label.setPreferredSize(dim);
//             label.setMinimumSize(dim);
//             label.setMaximumSize(dim);
//             fix.add(label);
//         panel.add(fix);

        return panel;
    }

    private JPanel buildInputPanel() {
        var panel = new JPanel();

        // Load list data
        _inputList = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            if (i < 6) {
                _inputList.add(new InputList("some user name", "00.01.02.03.04.05.06.07", "99.88.77.66.55.44.33.22"));
            } else {
                _inputList.add(new InputList("", "00.01.02.03.04.05.06.07", "99.88.77.66.55.44.33.22"));
            }
        }

        var model = new InputModel();
        var table = new JTable(model);
        table.setRowSelectionAllowed(false);
        var scrollpane = new JScrollPane(table);
        panel.add(scrollpane);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        return panel;
    }

    private JPanel buildOutputPanel() {
        var panel = new JPanel();

        // Load list data
        _outputList = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            if (i < 6) {
                _outputList.add(new OutputList("some user name", "00.01.02.03.04.05.06.07", "99.88.77.66.55.44.33.22"));
            } else {
                _outputList.add(new OutputList("", "00.01.02.03.04.05.06.07", "99.88.77.66.55.44.33.22"));
            }
        }

        var model = new OutputModel();
        var table = new JTable(model);
        table.setRowSelectionAllowed(false);
        var scrollpane = new JScrollPane(table);
        panel.add(scrollpane);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        return panel;
    }

    private JPanel buildReceiverPanel() {
        var panel = new JPanel();

        // Load list data
        _receiverList = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            if (i < 6) {
                _receiverList.add(new ReceiverList("some user name", "00.01.02.03.04.05.06.07"));
            } else {
                _receiverList.add(new ReceiverList("", "00.01.02.03.04.05.06.07"));
            }
        }

        var model = new ReceiverModel();
        var table = new JTable(model);
        table.setRowSelectionAllowed(false);
        var scrollpane = new JScrollPane(table);
        panel.add(scrollpane);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        return panel;
    }

    private JPanel buildTransmitterPanel() {
        var panel = new JPanel();

        // Load list data
        _transmitterList = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            if (i < 6) {
                _transmitterList.add(new TransmitterList("some user name", "00.01.02.03.04.05.06.07"));
            } else {
                _transmitterList.add(new TransmitterList("", "00.01.02.03.04.05.06.07"));
            }
        }

        var model = new TransmitterModel();
        var table = new JTable(model);
        table.setRowSelectionAllowed(false);
        var scrollpane = new JScrollPane(table);
        panel.add(scrollpane);

        // resize columns
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }

        return panel;
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
        var item = box.getSelectedItem();
        log.info("nodeSelected: {}", box.getSelectedItem());
        setReady(true);
    }

    private void newNodeInList(MimicNodeStore.NodeMemo nodeMemo) {
        // Add filter for Tower LCC+Q

        int i = 0;
        if (model.getIndexOf(nodeMemo.getNodeID()) >= 0) {
            // already exists. Do nothing.
            return;
        }
        NodeEntry e = new NodeEntry(nodeMemo);
        while ((i < model.getSize()) && (model.getElementAt(i).compareTo(e) < 0)) {
            ++i;
        }
        model.insertElementAt(e, i);
    }

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
        return getTitle(Bundle.getMessage("TitleEventTable"));
    }

    void pushedCancel(ActionEvent e) {
        if (ready) {
            cancelled = true;
        }
    }

    // Notifies that the contents ofa given entry have changed. This will delete and re-add the
    // entry to the model, forcing a refresh of the box.
    private void updateComboBoxModelEntry(NodeEntry nodeEntry) {
        int idx = model.getIndexOf(nodeEntry.getNodeID());
        if (idx < 0) {
            return;
        }
        NodeEntry last = model.getElementAt(idx);
        if (last != nodeEntry) {
            // not the same object -- we're talking about an abandoned entry.
            nodeEntry.dispose();
            return;
        }
        NodeEntry sel = (NodeEntry) model.getSelectedItem();
        model.removeElementAt(idx);
        model.insertElementAt(nodeEntry, idx);
        model.setSelectedItem(sel);
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
                updateComboBoxModelEntry(this);
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

    // --------------  table lists ---------
    /**
     * The name and assigned event id for a circuit transmitter.
     */
    private static class LogicList {
        String _label;
        String _oper;
        String _name;
        String _variable;

        LogicList(String label, String oper, String name, String variable) {
            _label = label;
            _oper = oper;
            _name = name;
            _variable = variable;
        }

        String getLabel() {
            return _label;
        }

        void setLabel(String newLabel) {
            _label = newLabel;
        }

        String getOper() {
            return _oper;
        }

        void setOper(String newOper) {
            _oper = newOper;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
        }

        String getVariable() {
            return _variable;
        }

        void setVariable(String newVariable) {
            _variable = newVariable;
        }
    }

    /**
     * The name and assigned true and false events for an Input.
     */
    private static class InputList {
        String _name;
        String _eventTrue;
        String _eventFalse;

        InputList(String name, String eventTrue, String eventFalse) {
            _name = name;
            _eventTrue = eventTrue;
            _eventFalse = eventFalse;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
        }

        String getEventTrue() {
            return _eventTrue;
        }

        void setEventTrue(String newEventTrue) {
            _eventTrue = newEventTrue;
        }

        String getEventFalse() {
            return _eventFalse;
        }

        void setEventFalse(String newEventFalse) {
            _eventFalse = newEventFalse;
        }
    }

    /**
     * The name and assigned true and false events for an Output.
     */
    private static class OutputList {
        String _name;
        String _eventTrue;
        String _eventFalse;

        OutputList(String name, String eventTrue, String eventFalse) {
            _name = name;
            _eventTrue = eventTrue;
            _eventFalse = eventFalse;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
        }

        String getEventTrue() {
            return _eventTrue;
        }

        void setEventTrue(String newEventTrue) {
            _eventTrue = newEventTrue;
        }

        String getEventFalse() {
            return _eventFalse;
        }

        void setEventFalse(String newEventFalse) {
            _eventFalse = newEventFalse;
        }
    }

    /**
     * The name and assigned event id for a circuit receiver.
     */
    private static class ReceiverList {
        String _name;
        String _eventid;

        ReceiverList(String name, String eventid) {
            _name = name;
            _eventid = eventid;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
        }

        String getEventId() {
            return _eventid;
        }

        void setEventId(String newEventid) {
            _eventid = newEventid;
        }
    }

    /**
     * The name and assigned event id for a circuit transmitter.
     */
    private static class TransmitterList {
        String _name;
        String _eventid;

        TransmitterList(String name, String eventid) {
            _name = name;
            _eventid = eventid;
        }

        String getName() {
            return _name;
        }

        void setName(String newName) {
            _name = newName;
        }

        String getEventId() {
            return _eventid;
        }

        void setEventId(String newEventid) {
            _eventid = newEventid;
        }
    }

    // --------------  table models ---------

    /**
     * TableModel for STL table entries.
     */
    class LogicModel extends AbstractTableModel /* implements PropertyChangeListener */ {

        LogicModel() {
//             InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
        }

        public static final int LABEL_COLUMN = 0;
        public static final int OPER_COLUMN = 1;
        public static final int NAME_COLUMN = 2;
        public static final int VAR_COLUMN = 3;

        @Override
        public int getRowCount() {
            return _logicList.size();
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
                case LABEL_COLUMN:
                    return Bundle.getMessage("ColumnLabel");  // NOI18N
                case OPER_COLUMN:
                    return Bundle.getMessage("ColumnOper");  // NOI18N
                case NAME_COLUMN:
                    return Bundle.getMessage("ColumnName");  // NOI18N
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
                    _logicList.get(r).setOper((String) type);
                    break;
                case NAME_COLUMN:
                    _logicList.get(r).setName((String) type);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == LABEL_COLUMN || c == OPER_COLUMN || c == NAME_COLUMN));
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case LABEL_COLUMN:
                case VAR_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case OPER_COLUMN:
                    return new JTextField(10).getPreferredSize().width;
                case NAME_COLUMN:
                    return new JTextField(50).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }
    }

    /**
     * TableModel for Input table entries.
     */
    class InputModel extends AbstractTableModel /* implements PropertyChangeListener */ {

        InputModel() {
//             InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
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
                    return new JTextField(20).getPreferredSize().width;
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
    class OutputModel extends AbstractTableModel /* implements PropertyChangeListener */ {
        OutputModel() {
//             InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
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
                case OUTPUT_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case NAME_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
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
    class ReceiverModel extends AbstractTableModel /* implements PropertyChangeListener */ {

        ReceiverModel() {
//             InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
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
                    return new JTextField(30).getPreferredSize().width;
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
    class TransmitterModel extends AbstractTableModel /* implements PropertyChangeListener */ {

        TransmitterModel() {
//             InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
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
                    return new JTextField(30).getPreferredSize().width;
                case EVENTID_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }
    }

    // --------------  other items ---------

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
