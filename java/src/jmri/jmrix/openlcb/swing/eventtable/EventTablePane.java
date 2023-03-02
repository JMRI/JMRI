package jmri.jmrix.openlcb.swing.eventtable;

import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.swing.JmriJTablePersistenceManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.swing.*;


/**
 * Pane for displaying a table of relationships of nodes, producers and consumers
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2023
 * @since 5.3.4
 */
public class EventTablePane extends jmri.util.swing.JmriPanel
        implements jmri.jmrix.can.swing.CanPanelInterface {

    protected CanSystemConnectionMemo memo;
    Connection connection;
    NodeID nid;

    MimicNodeStore store;
    EventTableDataModel model;
    JTable table;
    Monitor monitor;

    JCheckBox showRequiresLabel; // requires a user-provided name to display
    JCheckBox showRequiresMatch; // requires at least one consumer and one producer exist to display
    JCheckBox popcorn;           // popcorn mode displays events in real time

    JFormattedTextField findID;

    private transient TableRowSorter<EventTableDataModel> sorter;

    public String getTitle(String menuTitle) {
        return Bundle.getMessage("TitleEventTable");
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.connection = memo.get(Connection.class);
        this.nid = memo.get(NodeID.class);

        store = memo.get(MimicNodeStore.class);
        EventTable stdEventTable = memo.get(OlcbInterface.class).getEventTable();
        if (stdEventTable == null) log.warn("no OLCB EventTable found");

        model = new EventTableDataModel(store, stdEventTable);
        sorter = new TableRowSorter<>(model);


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Add to GUI here

        table = new JTable(model);

        model.table = table;
        model.sorter = sorter;
        table.setAutoCreateRowSorter(true);
        table.setRowSorter(sorter);
        table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);
        table.getTableHeader().setBackground(Color.LIGHT_GRAY);
        table.setName("jmri.jmrix.openlcb.swing.eventtable.EventTablePane.table"); // for persistence
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);

        var scrollPane = new JScrollPane(table);

        // restore the column layout and start monitoring it
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
            tpm.resetState(table);
            tpm.persist(table);
        });

        add(scrollPane);

        var buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        add(buttonPanel);

        var updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
        updateButton.addActionListener(this::sendRequestEvents);
        buttonPanel.add(updateButton);

        buttonPanel.add(add(new JSeparator(JSeparator.VERTICAL)));

        showRequiresLabel = new JCheckBox(Bundle.getMessage("BoxShowRequiresLabel"));
        showRequiresLabel.addActionListener((ActionEvent e) -> {
            filter();
        });
        buttonPanel.add(showRequiresLabel);

        showRequiresMatch = new JCheckBox(Bundle.getMessage("BoxShowRequiresMatch"));
        showRequiresMatch.addActionListener((ActionEvent e) -> {
            filter();
        });
        buttonPanel.add(showRequiresMatch);

        popcorn = new JCheckBox(Bundle.getMessage("BoxPopcorn"));
        popcorn.addActionListener((ActionEvent e) -> {
            popcornButtonChanged();
        });
        buttonPanel.add(popcorn);

        buttonPanel.add(add(new JSeparator(JSeparator.VERTICAL)));

        findID = EventIdTextField.getEventIdTextField();
        buttonPanel.add(findID);
        JButton find = new JButton("Find");
        buttonPanel.add(find);
        find.addActionListener(this::findRequested);

        buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());

        // hook up to receive traffic
        monitor = new Monitor(model);
        memo.get(OlcbInterface.class).registerMessageListener(monitor);
    }

    public EventTablePane() {
    }

    @Override
    public void dispose() {
        // Save the column layout
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
           tpm.stopPersisting(table);
        });
        // remove traffic connection
        memo.get(OlcbInterface.class).unRegisterMessageListener(monitor);
        // and complete this
        super.dispose();
    }

    @Override
    public java.util.List<JMenu> getMenus() {
        // create a file menu
        var retval = new ArrayList<JMenu>();
        var fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        var csvItem = new JMenuItem("Save to CSV...", KeyEvent.VK_S);
        KeyStroke ctrlSKeyStroke = KeyStroke.getKeyStroke("control S");
        if (jmri.util.SystemType.isMacOSX()) {
            ctrlSKeyStroke = KeyStroke.getKeyStroke("meta S");
        }
        csvItem.setAccelerator(ctrlSKeyStroke);
        csvItem.addActionListener(this::writeToCsvFile);
        fileMenu.add(csvItem);
        retval.add(fileMenu);
        return retval;
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.openlcb.swing.eventtable.EventTablePane";
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Event Table");
        }
        return getTitle(Bundle.getMessage("TitleEventTable"));
    }

    public void sendRequestEvents(java.awt.event.ActionEvent e) {
        model.clear();
        final int DELAY = 75; // msec between operations - 64 events at speed
        int nextDelay = 0;

        // assumes that a VerifyNodes has been done and all nodes are in the MimicNodeStore
        for (var memo : store.getNodeMemos()) {

            jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
                var destNodeID = memo.getNodeID();
                log.trace("send IdentifyEventsAddressedMessage {} {}", nid, destNodeID);
                Message m = new IdentifyEventsAddressedMessage(nid, destNodeID);
                connection.put(m, null);
            }, nextDelay);

            nextDelay += DELAY;
        }
        // Our reference to the node names in the MimicNodeStore will
        // trigger a SNIP request if we don't have them yet.  In that case.
        // we want to trigger a table refresh to make sure they get displayed.
        // TODO: This might be better triggered by seeing a SNIP reply
        final int INTERVAL = 5000;
        jmri.util.ThreadingUtil.runOnGUIDelayed(() -> {
            model.handleTableUpdate(0, model.getRowCount()-1);
        }, INTERVAL);
        jmri.util.ThreadingUtil.runOnGUIDelayed(() -> {
            model.handleTableUpdate(0, model.getRowCount()-1);
        }, INTERVAL*2);

    }

    void popcornButtonChanged() {
        model.popcornModeActive = popcorn.isSelected();
        log.info("Popcorn mode {}", model.popcornModeActive);
    }


    public void findRequested(java.awt.event.ActionEvent e) {
        log.debug("Request find event {}", findID.getText());
        model.highlightEvent(new EventID(findID.getText()));
    }

    // CSV file chooser
    // static to remember choice from one use to another.
    static JFileChooser fileChooser = null;

    /**
     * Write out contents in CSV form
     * @param e Needed for signature of method, but ignored here
     */
    public void writeToCsvFile(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save CSV file");
        }
        fileChooser.rescanCurrentDirectory();
        fileChooser.setSelectedFile(new File("eventtable.csv"));

        int retVal = fileChooser.showSaveDialog(this);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to export to CSV file {}", file);
            }

            try (CSVPrinter str = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
                str.printRecord("Event ID", "Event Name", "Producer Node", "Producer Node Name",
                                "Consumer Node", "Consumer Node Name", "Paths");
                for (int i = 0; i < model.getRowCount(); i++) {
                    String contextInfo = model.getValueAt(i, EventTableDataModel.COL_CONTEXT_INFO).toString().replace("\n", " / "); // multi-line cell

                    str.printRecord(model.getValueAt(i, EventTableDataModel.COL_EVENTID),
                                    model.getValueAt(i, EventTableDataModel.COL_EVENTNAME),
                                    model.getValueAt(i, EventTableDataModel.COL_PRODUCER_NODE),
                                    model.getValueAt(i, EventTableDataModel.COL_PRODUCER_NAME),
                                    model.getValueAt(i, EventTableDataModel.COL_CONSUMER_NODE),
                                    model.getValueAt(i, EventTableDataModel.COL_CONSUMER_NAME),
                                    contextInfo
                            );
                }
                str.flush();
            } catch (IOException ex) {
                log.error("Error writing file", ex);
            }
        }
    }

    /**
     * Set up filtering of displayed rows
     */
    private void filter() {
        RowFilter<EventTableDataModel, Integer> rf = new RowFilter<EventTableDataModel, Integer>() {
            /**
             * @return true if row is to be displayed
             */
            @Override
            public boolean include(RowFilter.Entry<? extends EventTableDataModel, ? extends Integer> entry) {

                int row = entry.getIdentifier();

                var name = model.getValueAt(row, EventTableDataModel.COL_EVENTNAME);
                if ( showRequiresLabel.isSelected() && (name == null || name.toString().isEmpty()) ) return false;

                if ( showRequiresMatch.isSelected()) {
                    var memo = model.getTripleMemo(row);

                    if (memo.producer == null && !model.producerPresent(memo.eventID)) {
                        // no matching producer
                        return false;
                    }

                    if (memo.consumer == null && !model.consumerPresent(memo.eventID)) {
                        // no matching producer
                        return false;
                    }
                }

                return true;
            }
        };
        sorter.setRowFilter(rf);
    }

    /**
     * Nest class to display multiple lines in a cell
     */
    static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

      public MultiLineCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
      }

      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
          setForeground(table.getSelectionForeground());
          setBackground(table.getSelectionBackground());
        } else {
          setForeground(table.getForeground());
          setBackground(table.getBackground());
        }
        setFont(table.getFont());
        if (hasFocus) {
          if (table.isCellEditable(row, column)) {
            setForeground(UIManager.getColor("Table.focusCellForeground"));
            setBackground(UIManager.getColor("Table.focusCellBackground"));
          }
        }
        setText((value == null) ? "" : value.toString());
        return this;
      }
    }

    /**
     * Nested class to hold data model
     */
    protected static class EventTableDataModel extends AbstractTableModel {

        EventTableDataModel(MimicNodeStore store, EventTable stdEventTable) {
            this.store = store;
            this.stdEventTable = stdEventTable;
            tagManager = InstanceManager.getDefault(IdTagManager.class);
        }

        static final int COL_EVENTID = 0;
        static final int COL_EVENTNAME = 1;
        static final int COL_PRODUCER_NODE = 2;
        static final int COL_PRODUCER_NAME = 3;
        static final int COL_CONSUMER_NODE = 4;
        static final int COL_CONSUMER_NAME = 5;
        static final int COL_CONTEXT_INFO = 6;
        static final int COL_COUNT = 7;

        MimicNodeStore store;
        EventTable stdEventTable;
        IdTagManager tagManager;
        JTable table;
        TableRowSorter<EventTableDataModel> sorter;
        boolean popcornModeActive = false;

        TripleMemo getTripleMemo(int row) {
            if (row >= memos.size()) {
                return null;
            }
            return memos.get(row);
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row >= memos.size()) {
                log.warn("request out of range: {} greater than {}", row, memos.size());
                return "Illegal col "+row+" "+col;
            }
            var memo = memos.get(row);
            switch (col) {
                case COL_EVENTID: return memo.eventID.toShortString();
                case COL_EVENTNAME:
                    var tag = tagManager.getIdTag(tagPrefix+memo.eventID.toShortString());
                    if (tag == null) return "";
                    return tag.getUserName();
                case COL_PRODUCER_NODE:
                    return memo.producer != null ? memo.producer.toString() : "";
                case COL_PRODUCER_NAME: return memo.producerName;
                case COL_CONSUMER_NODE:
                    return memo.consumer != null ? memo.consumer.toString() : "";
                case COL_CONSUMER_NAME: return memo.consumerName;
                case COL_CONTEXT_INFO:
                    // set up for multi-line output in the cell
                    var result = new StringBuilder();
                    if (lineIncrement <= 0) { // load cached value
                        lineIncrement = table.getFont().getSize()*13/10; // line spacing
                    }
                    var height = lineIncrement/3; // for margins
                    var first = true;   // no \n before first line

                    // scan the event info as available
                    for (var entry : stdEventTable.getEventInfo(memo.eventID).getAllEntries()) {
                        if (!first) result.append("\n");
                        first = false;
                        height += lineIncrement;
                        result.append(entry.getDescription());
                    }
                    // When table is constrained, these rows don't match up, need to find constrained row
                    var viewRow = sorter.convertRowIndexToView(row);
                    // set height
                    if (height < lineIncrement) {
                        height = height+lineIncrement; // when no lines, assume 1
                    }
                    if (height != table.getRowHeight(row)) {
                        table.setRowHeight(viewRow, height);
                    }

                    return new String(result);
                default: return "Illegal row "+row+" "+col;
            }
        }

        int lineIncrement = -1; // cache the line spacing for multi-line cells

        static final String tagPrefix = "ID_OpenLCB_";  // Prefix of IdTag system name

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col != COL_EVENTNAME) return;
            if (row >= memos.size()) {
                log.warn("request out of range: {} greater than {}", row, memos.size());
                return;
            }
            var memo = memos.get(row);
            var tag = tagManager.provideIdTag("ID_OpenLCB_"+memo.eventID.toShortString());
            tag.setUserName(value.toString());
        }

        @Override
        public int getColumnCount() {
            return COL_COUNT;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case COL_EVENTID:       return "Event ID";
                case COL_EVENTNAME:     return "Event Name";
                case COL_PRODUCER_NODE: return "Producer Node";
                case COL_PRODUCER_NAME: return "Producer Node Name";
                case COL_CONSUMER_NODE: return "Consumer Node";
                case COL_CONSUMER_NAME: return "Consumer Node Name";
                case COL_CONTEXT_INFO:  return "Path(s) from Configure Dialog";
                default: return "ERROR "+col;
            }
        }

        @Override
        public int getRowCount() {
            return memos.size();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == COL_EVENTNAME;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        /**
         * Remove all existing data, generally just in advance of an update
         */
        @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD") // Swing thread deconflicts
        void clear() {
            memos = new ArrayList<>();
            fireTableDataChanged();  // don't queue this one, must be immediate
        }

        // static so the data remains available through a window close-open cycle
        static ArrayList<TripleMemo> memos = new ArrayList<>();

        /**
         * Notify the table that the contents have changed.
         * To reduce CPU load, this batches the changes
         * @param start first row changed; -1 means entire table
         * @param end   last row changed; -1 means entire table
         */
        void handleTableUpdate(int start, int end) {
            final int DELAY = 250;

            if (!pending) {
                jmri.util.ThreadingUtil.runOnGUIDelayed(() -> {
                    pending = false;
                    fireTableDataChanged();
                }, DELAY);
                pending = true;
            }

        }
        boolean pending = false;

        /**
         * Record an event-producer pair
         * @param eventID Observed event
         * @param nodeID  Node that is known to produce the event
         */
        void recordProducer(EventID eventID, NodeID nodeID) {
            log.debug("recordProducer of {} in {}", eventID, nodeID);

            // update if the model has been cleared
            if (memos.size() <= 1) {
                handleTableUpdate(-1, -1);
            }

            var nodeMemo = store.findNode(nodeID);
            String name = "";
            if (nodeMemo != null) {
                var ident = nodeMemo.getSimpleNodeIdent();
                    if (ident != null) {
                        name = ident.getUserName();
                    }
            }


            // if this already exists, skip storing it
            // if you can, find a matching memo with an empty consumer value
            TripleMemo empty = null;
            TripleMemo bestEmpty = null;
            for (int i = 1; i < memos.size(); i++) {
                var memo = memos.get(i);
                if (memo.eventID.equals(eventID) ) {
                    // if nodeID matches, already present; ignore
                    if (nodeID.equals(memo.producer)) {
                        // might be 2nd EventTablePane to process the data,
                        // hence memos would already have been processed. To
                        // handle that, need to fire a change to the table.
                        // On the other hand, this rapidly erases the
                        // popcorn display, so we disable it for that.
                        if (!popcornModeActive) {
                            handleTableUpdate(i, i);
                        }
                        return;
                    }
                    // if empty producer slot, remember it
                    if (memo.producer == null) {
                        empty = memo;
                        // best empty has matching consumer
                        if (nodeID.equals(memo.consumer)) bestEmpty = memo;
                    }
                }
            }

            // can we use the bestEmpty?
            // N.B. This doesn't always work. If c(A) arrives before c(B), p(B)
            // will be slotted into c(A) before a "best" match is available.
            if (bestEmpty != null) {
                // yes
                log.trace("   use bestEmpty");
                bestEmpty.producer = nodeID;
                bestEmpty.producerName = name;
                handleTableUpdate(-1, -1);
                return;
            }

            // can we use the empty?
            if (empty != null) {
                // yes
                log.trace("   reuse empty");
                empty.producer = nodeID;
                empty.producerName = name;
                handleTableUpdate(-1, -1);
                return;
            }

            // have to make a new one
            var memo = new TripleMemo(
                            eventID,
                            nodeID,
                            name,
                            null,
                            ""
                        );
            memos.add(memo);
            handleTableUpdate(memos.size()-1, memos.size()-1);
        }

        /**
         * Record an event-consumer pair
         * @param eventID Observed event
         * @param nodeID  Node that is known to consume the event
         */
        void recordConsumer(EventID eventID, NodeID nodeID) {
            log.debug("recordConsumer of {} in {}", eventID, nodeID);

            // update if the model has been cleared
            if (memos.size() <= 1) {
                handleTableUpdate(-1, -1);
            }

            var nodeMemo = store.findNode(nodeID);
            String name = "";
            if (nodeMemo != null) {
                var ident = nodeMemo.getSimpleNodeIdent();
                    if (ident != null) {
                        name = ident.getUserName();
                    }
            }

            // if this already exists, skip storing it
            // if you can, find a matching memo with an empty consumer value
            TripleMemo empty = null;
            TripleMemo bestEmpty = null;
            for (int i = 1; i < memos.size(); i++) {
                var memo = memos.get(i);
                if (memo.eventID.equals(eventID) ) {
                    // if nodeID matches, already present; ignore
                    if (nodeID.equals(memo.consumer)) {
                        // might be 2nd EventTablePane to process the data,
                        // hence memos would already have been processed. To
                        // handle that, always fire a change to the table.
                        handleTableUpdate(i, i);
                        return;
                    }
                    // if empty consumer slot, remember it
                    if (memo.consumer == null) {
                        empty = memo;
                        // best empty has matching producer
                        if (nodeID.equals(memo.producer)) bestEmpty = memo;
                    }
                }
            }

            // can we use the best empty?
            // N.B. This doesn't always work. If p(A) arrives before p(B), c(B)
            // will be slotted into p(A) before a "best" match is available.
            if (bestEmpty != null) {
                // yes
                log.trace("   use bestEmpty");
                bestEmpty.consumer = nodeID;
                bestEmpty.consumerName = name;
                handleTableUpdate(-1, -1);
                return;
            }

            // can we use the empty?
            if (empty != null) {
                // yes
                log.trace("   reuse empty");
                empty.consumer = nodeID;
                empty.consumerName = name;
                handleTableUpdate(-1, -1);
                return;
            }

            // have to make a new one
            var memo = new TripleMemo(
                            eventID,
                            null,
                            "",
                            nodeID,
                            name
                        );
            memos.add(memo);
            handleTableUpdate(memos.size()-1, memos.size()-1);
         }

        // This causes the display to jump around as it tried to keep
        // the selected cell visible.
        // TODO: A better approach might be to change
        // the cell background color via a custom cell renderer
        void highlightProducer(EventID eventID, NodeID nodeID) {
            if (!popcornModeActive) return;
            log.trace("highlightProducer", eventID, nodeID);
            for (int i = 0; i < memos.size(); i++) {
                var memo = memos.get(i);
                if (eventID.equals(memo.eventID) && nodeID.equals(memo.producer)) {
                    try {
                        var viewRow = sorter.convertRowIndexToView(i);
                        log.trace("highlight event ID {} row {} viewRow {}", eventID, i, viewRow);
                        if (viewRow >= 0) {
                            table.changeSelection(viewRow, COL_PRODUCER_NODE, false, false);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // can happen on first encounter of an event before table is updated
                        log.trace("failed to highlight event ID {} row {}", eventID.toShortString(), i);
                    }
                }
            }
        }

        // highlights (selects) all the eventID cells with a particular event,
        // Most LAFs will move the first of these on-scroll-view.
        void highlightEvent(EventID eventID) {
            log.trace("highlightEvent", eventID);
            table.clearSelection(); // clear existing selections
            for (int i = 0; i < memos.size(); i++) {
                var memo = memos.get(i);
                if (eventID.equals(memo.eventID)) {
                    try {
                        var viewRow = sorter.convertRowIndexToView(i);
                        log.trace("highlight event ID {} row {} viewRow {}", eventID, i, viewRow);
                        if (viewRow >= 0) {
                            table.changeSelection(viewRow, COL_EVENTID, true, false);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // can happen on first encounter of an event before table is updated
                        log.trace("failed to highlight event ID {} row {}", eventID.toShortString(), i);
                    }
                }
            }
        }

        boolean consumerPresent(EventID eventID) {
            for (var memo : memos) {
                if (memo.eventID.equals(eventID) ) {
                    if (memo.consumer!=null) return true;
                }
            }
            return false;
        }

        boolean producerPresent(EventID eventID) {
            for (var memo : memos) {
                if (memo.eventID.equals(eventID) ) {
                    if (memo.producer!=null) return true;
                }
            }
            return false;
        }

        static class TripleMemo {
            EventID eventID;
            // Event name is stored as an IdTag
            NodeID producer;
            String producerName;
            NodeID consumer;
            String consumerName;

            TripleMemo(EventID eventID, NodeID producer, String producerName,
                        NodeID consumer, String consumerName) {
                this.eventID = eventID;
                this.producer = producer;
                this.producerName = producerName;
                this.consumer = consumer;
                this.consumerName = consumerName;
            }
        }
    }

    /**
     * Internal class to watch OpenLCB traffic
     */

    static class Monitor extends MessageDecoder {

        Monitor(EventTableDataModel model) {
            this.model = model;
        }

        EventTableDataModel model;

        /**
         * Handle "Producer/Consumer Event Report" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
            var nodeID = msg.getSourceNodeID();
            var eventID = msg.getEventID();
            model.recordProducer(eventID, nodeID);
            model.highlightProducer(eventID, nodeID);
        }

        /**
         * Handle "Consumer Identified" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender){
            var nodeID = msg.getSourceNodeID();
            var eventID = msg.getEventID();
            model.recordConsumer(eventID, nodeID);
        }

        /**
         * Handle "Producer Identified" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender){
            var nodeID = msg.getSourceNodeID();
            var eventID = msg.getEventID();
            model.recordProducer(eventID, nodeID);
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    public static class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("Openlcb Event Table",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    EventTablePane.class.getName(),
                    jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTablePane.class);
}
