package jmri.jmrix.openlcb.swing.eventtable;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.swing.JmriJTablePersistenceManager;

import org.openlcb.*;
import org.openlcb.implementations.*;


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

    JCheckBox showRequiresLabel;
    JCheckBox showRequiresMatch; // requires at least one consumer and one producer exist

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
        table.setName("jmri.jmrix.openlcb.swing.eventtable.EventTablePane.table");

        var scrollPane = new JScrollPane(table);        // persist table options - see TODO below

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

        for (var memo : store.getNodeMemos()) {

            jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
                var destNodeID = memo.getNodeID();
                log.trace("sendRequestEvents {} {}", nid, destNodeID);
                Message m = new IdentifyEventsAddressedMessage(nid, destNodeID);
                connection.put(m, null);
            }, nextDelay);
            nextDelay += DELAY;
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
                // default filter is IN-USE and regular systems slot
                // the default is whatever the person last closed it with

                int row = entry.getIdentifier();

                var name = model.getValueAt(row, EventTableDataModel.COL_EVENTNAME);
                if ( showRequiresLabel.isSelected() && (name == null || name.toString().isEmpty()) ) return false;

                if ( showRequiresMatch.isSelected()) {
                    var memo = model.getTripleMemo(row);

                    if (memo.producer == null && !model.producerPresent(memo.consumer, memo.eventID)) {
                        // no matching producer
                        return false;
                    }

                    if (memo.consumer == null && !model.consumerPresent(memo.producer, memo.eventID)) {
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
                    var height = 2; // for margins
                    int increment = table.getFont().getSize()*15/10; // 1.5 line spacing
                    var first = true;   // no \n before first line

                    // scan the event info as available
                    for (var entry : stdEventTable.getEventInfo(memo.eventID).getAllEntries()) {
                        if (!first) result.append("\n");
                        first = false;
                        height += increment;
                        result.append(entry.getDescription());
                    }
                    // set height

                    // When constrained, these rows don't match up, need to find constrained row
                    var viewRow = sorter.convertRowIndexToView(row);
                    if (height < increment) {
                        height = height+increment; // when no lines, assume 1
                    }
                    if (height != table.getRowHeight(row)) {
                        table.setRowHeight(viewRow, height);
                    }

                    return new String(result);
                default: return "Illegal row "+row+" "+col;
            }
        }

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
        void clear() {
            memos = new ArrayList<>();
            fireTableDataChanged();
        }

        // static so the data remains available through a window close-open cycle
        static ArrayList<TripleMemo> memos = new ArrayList<>();

        /**
         * Record an event-producer pair
         * @param eventID Observed event
         * @param nodeID  Node that is known to produce the event
         */
        void recordProducer(EventID eventID, NodeID nodeID) {
            log.trace("recordProducer of {} in {}", eventID, nodeID);

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
            for (var memo : memos) {
                if (memo.eventID.equals(eventID) ) {
                    // if nodeID matches, already present; ignore
                    if (nodeID.equals(memo.producer)) {
                        // might be 2nd EventTablePane to process the data,
                        // hence memos would already have been processed. To
                        // handle that, always fire a change to the table.
                        fireTableDataChanged();
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
            if (bestEmpty != null) {
                // yes
                log.trace("   use bestEmpty");
                bestEmpty.producer = nodeID;
                bestEmpty.producerName = name;
                fireTableDataChanged();
                return;
            }

            // can we use the empty?
            if (empty != null) {
                // yes
                log.trace("   reuse empty");
                empty.producer = nodeID;
                empty.producerName = name;
                fireTableDataChanged();
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
            fireTableDataChanged();
        }

        /**
         * Record an event-consumer pair
         * @param eventID Observed event
         * @param nodeID  Node that is known to consume the event
         */
        void recordConsumer(EventID eventID, NodeID nodeID) {
            log.trace("recordConsumer of {} in {}", eventID, nodeID);

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
            for (var memo : memos) {
                if (memo.eventID.equals(eventID) ) {
                    // if nodeID matches, already present; ignore
                    if (nodeID.equals(memo.consumer)) {
                        // might be 2nd EventTablePane to process the data,
                        // hence memos would already have been processed. To
                        // handle that, always fire a change to the table.
                        fireTableDataChanged();
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
            if (bestEmpty != null) {
                // yes
                log.trace("   use bestEmpty");
                bestEmpty.consumer = nodeID;
                bestEmpty.consumerName = name;
                fireTableDataChanged();
                return;
            }

            // can we use the empty?
            if (empty != null) {
                // yes
                log.trace("   reuse empty");
                empty.consumer = nodeID;
                empty.consumerName = name;
                fireTableDataChanged();
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
            fireTableDataChanged();
        }

        boolean consumerPresent(NodeID nodeID, EventID eventID) {
            for (var memo : memos) {
                if (memo.eventID.equals(eventID) ) {
                    if (nodeID.equals(memo.consumer)) return true;
                }
            }
            return false;
        }

        boolean producerPresent(NodeID nodeID, EventID eventID) {
            for (var memo : memos) {
                if (memo.eventID.equals(eventID) ) {
                    if (nodeID.equals(memo.producer)) return true;
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
