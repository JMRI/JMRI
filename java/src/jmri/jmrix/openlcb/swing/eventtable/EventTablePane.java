package jmri.jmrix.openlcb.swing.eventtable;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.*;
import jmri.util.ThreadingUtil;

import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.swing.MultiLineCellRenderer;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.swing.*;


/**
 * Pane for displaying a table of relationships of nodes, producers and consumers.
 *
 * @author Bob Jacobsen Copyright (C) 2023, 2026
 * @since 5.3.4
 */
public class EventTablePane extends jmri.util.swing.JmriPanel
        implements jmri.jmrix.can.swing.CanPanelInterface {

    protected CanSystemConnectionMemo memo;
    Connection connection;
    NodeID nid;
    OlcbEventNameStore nameStore;
    OlcbNodeGroupStore groupStore;

    MimicNodeStore mimcStore;
    EventTableDataModel model;
    JTable table;
    Monitor monitor;

    JComboBox<String> matchGroupName;   // required group name to display; index <= 0 is all
    JCheckBox showRequiresLabel; // requires a user-provided name to display
    JCheckBox showRequiresMatch; // requires at least one consumer and one producer exist to display
    JCheckBox popcorn;           // popcorn mode displays events in real time

    JFormattedTextField findID;
    JTextField findTextID;

    private transient TableRowSorter<EventTableDataModel> sorter;

    public String getTitle(String menuTitle) {
        return Bundle.getMessage("TitleEventTable");
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.connection = memo.get(Connection.class);
        this.nid = memo.get(NodeID.class);
        this.nameStore = memo.get(OlcbEventNameStore.class);
        this.groupStore = InstanceManager.getDefault(OlcbNodeGroupStore.class);
        this.mimcStore = memo.get(MimicNodeStore.class);
        EventTable stdEventTable = memo.get(OlcbInterface.class).getEventTable();
        if (stdEventTable == null) log.warn("no OLCB EventTable found");

        model = new EventTableDataModel(mimcStore, stdEventTable, nameStore);
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
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        table.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        table.setDefaultEditor(JButton.class, buttonEditor);

        
        // render in fixed size font
        var defaultFont = table.getFont();
        var fixedFont = new Font(Font.MONOSPACED, Font.PLAIN, defaultFont.getSize());
        table.setFont(fixedFont);

        var scrollPane = new JScrollPane(table);

        // restore the column layout and start monitoring it
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
            tpm.resetState(table);
            tpm.persist(table);
        });

        add(scrollPane);

        var buttonPanel = new JToolBar();
        buttonPanel.setLayout(new jmri.util.swing.WrapLayout());

        add(buttonPanel);

        var updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
        updateButton.addActionListener(this::sendRequestEvents); 
        updateButton.setToolTipText(Bundle.getMessage("ButtonUpdateTt"));
        buttonPanel.add(updateButton);
        
        matchGroupName = new JComboBox<>();
        updateMatchGroupName();     // before adding listener
        matchGroupName.addActionListener((ActionEvent e) -> {
            filter();
        });
        groupStore.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            updateMatchGroupName();
        });
        buttonPanel.add(matchGroupName);
        
        showRequiresLabel = new JCheckBox(Bundle.getMessage("BoxShowRequiresLabel"));
        showRequiresLabel.addActionListener((ActionEvent e) -> {
            filter();
        });
        showRequiresLabel.setToolTipText(Bundle.getMessage("BoxShowRequiresLabelTt"));
        showRequiresLabel.setOpaque(false); // make transparent
        buttonPanel.add(showRequiresLabel);

        showRequiresMatch = new JCheckBox(Bundle.getMessage("BoxShowRequiresMatch"));
        showRequiresMatch.addActionListener((ActionEvent e) -> {
            filter();
        });
        showRequiresMatch.setToolTipText(Bundle.getMessage("BoxShowRequiresMatchTt"));
        showRequiresMatch.setOpaque(false); // make transparent
        buttonPanel.add(showRequiresMatch);

        popcorn = new JCheckBox(Bundle.getMessage("BoxPopcorn"));
        popcorn.addActionListener((ActionEvent e) -> {
            popcornButtonChanged();
        });
        popcorn.setToolTipText(Bundle.getMessage("BoxPopcornTt"));
        popcorn.setOpaque(false); // make transparent
        buttonPanel.add(popcorn);

        JPanel findpanel = new JPanel(); // keep button and text together
        findpanel.setOpaque(false); // make transparent
        findpanel.setToolTipText(Bundle.getMessage("FindPanelFindEventTt"));
        buttonPanel.add(findpanel);
        
        JLabel find = new JLabel(Bundle.getMessage("FindPanelFindEvent"));
        findpanel.add(find);

        findID = new EventIdTextField();
        findID.setToolTipText(Bundle.getMessage("FindPanelFindEventFieldTt"));
        findID.addActionListener(this::findRequested);
        findID.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
           }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                // on release so the searchField has been updated
                log.trace("keyTyped {} content {}", keyEvent.getKeyCode(), findTextID.getText());
                findRequested(null);
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }
        });
        findpanel.add(findID);
        JButton addButton = new JButton(Bundle.getMessage("FindPanelButtonAdd"));
        addButton.addActionListener(this::addRequested);
        addButton.setToolTipText(Bundle.getMessage("FindPanelButtonAddTt"));        
        findpanel.add(addButton);

        findpanel = new JPanel();  // keep button and text together
        findpanel.setOpaque(false); // make transparent
        findpanel.setToolTipText(Bundle.getMessage("FindPanelFindNameTt"));
        buttonPanel.add(findpanel);

        JLabel findText = new JLabel(Bundle.getMessage("FindPanelFindName"));
        findpanel.add(findText);

        findTextID = new JTextField(16);
        findTextID.addActionListener(this::findTextRequested);
        findTextID.setToolTipText(Bundle.getMessage("FindPanelFindNameTt"));
        findTextID.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
           }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                // on release so the searchField has been updated
                log.trace("keyTyped {} content {}", keyEvent.getKeyCode(), findTextID.getText());
                findTextRequested(null);
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }
        });
        findpanel.add(findTextID);        

        JButton sensorButton = new JButton(Bundle.getMessage("FindPanelButtonSensor"));
        sensorButton.addActionListener(this::sensorRequested);
        sensorButton.setToolTipText(Bundle.getMessage("FindPanelButtonSensorTt"));
        buttonPanel.add(sensorButton);
        
        JButton turnoutButton = new JButton(Bundle.getMessage("FindPanelButtonTurnout"));
        turnoutButton.addActionListener(this::turnoutRequested);
        turnoutButton.setToolTipText(Bundle.getMessage("FindPanelButtonTurnoutTt"));
        buttonPanel.add(turnoutButton);

        buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());

        // hook up to receive traffic
        monitor = new Monitor(model);
        memo.get(OlcbInterface.class).registerMessageListener(monitor);
    }

    public EventTablePane() {
        // interface and connections built in initComponents(..)
    }
    
    // load updateMatchGroup combobox with current contents
    protected void updateMatchGroupName() {
        matchGroupName.removeAllItems();
        matchGroupName.addItem(Bundle.getMessage("FrameAllGroups"));
        
        var list = groupStore.getGroupNames();
        for (String group : list) {
            matchGroupName.addItem(group);
        }        

        matchGroupName.setVisible(matchGroupName.getItemCount() > 1);
    }

    @Override
    public void dispose() {
        // Save the column layout
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
           tpm.stopPersisting(table);
        });
        // remove traffic connection
        memo.get(OlcbInterface.class).unRegisterMessageListener(monitor);
        // drop model connections
        model = null;
        monitor = null;
        // and complete this
        super.dispose();
    }

    @Override
    public java.util.List<JMenu> getMenus() {
        // create a file menu
        var retval = new ArrayList<JMenu>();
        var fileMenu = new JMenu(Bundle.getMessage("PaneMenuFile"));
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        var csvWriteItem = new JMenuItem(Bundle.getMessage("PaneSaveToCsv"), KeyEvent.VK_S);
        KeyStroke ctrlSKeyStroke = KeyStroke.getKeyStroke("control S");
        if (jmri.util.SystemType.isMacOSX()) {
            ctrlSKeyStroke = KeyStroke.getKeyStroke("meta S");
        }
        csvWriteItem.setAccelerator(ctrlSKeyStroke);
        csvWriteItem.addActionListener(this::writeToCsvFile);
        fileMenu.add(csvWriteItem);
        
        var csvReadItem = new JMenuItem(Bundle.getMessage("PaneReadFromCsv"), KeyEvent.VK_O);
        KeyStroke ctrlOKeyStroke = KeyStroke.getKeyStroke("control O");
        if (jmri.util.SystemType.isMacOSX()) {
            ctrlOKeyStroke = KeyStroke.getKeyStroke("meta O");
        }
        csvReadItem.setAccelerator(ctrlOKeyStroke);
        csvReadItem.addActionListener(this::readFromCsvFile);
        fileMenu.add(csvReadItem);
        
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
            return (memo.getUserName() + " " + Bundle.getMessage("TitleEventTable"));
        }
        return getTitle(Bundle.getMessage("TitleEventTable"));
    }

    public void sendRequestEvents(java.awt.event.ActionEvent e) {
        model.clear();

        model.loadNameStoreEventIDs();
        model.handleTableUpdate(-1, -1);

        final int IDENTIFY_EVENTS_DELAY = 250; // msec between operations - 250 events at speed
        int nextDelay = 0;

        // assumes that a VerifyNodes has been done and all nodes are in the MimicNodeStore
        for (var memo : mimcStore.getNodeMemos()) {

            jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
                var destNodeID = memo.getNodeID();
                log.trace("send IdentifyEventsAddressedMessage {} {}", nid, destNodeID);
                Message m = new IdentifyEventsAddressedMessage(nid, destNodeID);
                connection.put(m, null);
            }, nextDelay);

            nextDelay += IDENTIFY_EVENTS_DELAY;
        }
        // Our reference to the node names in the MimicNodeStore will
        // trigger a SNIP request if we don't have them yet.  In case that happens
        // we want to trigger a table refresh to make sure they get displayed.
        final int REFRESH_INTERVAL = 1000;
        jmri.util.ThreadingUtil.runOnGUIDelayed(() -> {
            model.handleTableUpdate(-1,-1);
        }, nextDelay+REFRESH_INTERVAL);
        jmri.util.ThreadingUtil.runOnGUIDelayed(() -> {
            model.handleTableUpdate(-1,-1);
        }, nextDelay+REFRESH_INTERVAL*2);
        jmri.util.ThreadingUtil.runOnGUIDelayed(() -> {
            model.handleTableUpdate(-1,-1);
        }, nextDelay+REFRESH_INTERVAL*4);

    }

    void popcornButtonChanged() {
        model.popcornModeActive = popcorn.isSelected();
        log.debug("Popcorn mode {}", model.popcornModeActive);
    }


    public void findRequested(java.awt.event.ActionEvent e) {
        var text = findID.getText();
        // take off all the trailing .00
        text = text.strip().replaceAll("(.00)*$", "");
        log.debug("Request find event [{}]", text);
        // just search event ID
        table.clearSelection();
        if (findTextSearch(text, EventTableDataModel.COL_EVENTID)) return;
    }
    
    public void findTextRequested(java.awt.event.ActionEvent e) {
        String text = findTextID.getText();
        log.debug("Request find text {}", text);
        // first search event name, then from config, then producer name, then consumer name
        table.clearSelection();
        if (findTextSearch(text, EventTableDataModel.COL_EVENTNAME)) return;
        if (findTextSearch(text, EventTableDataModel.COL_CONTEXT_INFO)) return;
        if (findTextSearch(text, EventTableDataModel.COL_PRODUCER_NAME)) return;
        if (findTextSearch(text, EventTableDataModel.COL_CONSUMER_NAME)) return;
        return;

        //model.highlightEvent(new EventID(findID.getText()));
    }
    
    protected boolean findTextSearch(String text, int column) {
        text = text.toUpperCase();
        try {
            for (int row = 0; row < model.getRowCount(); row++) {
                var cell = table.getValueAt(row, column);
                if (cell == null) continue;
                var value = cell.toString().toUpperCase();
                if (value.startsWith(text)) {
                    table.changeSelection(row, column, false, false);
                    return true;
                }
            }
        } catch (RuntimeException e) {
            // we get ArrayIndexOutOfBoundsException occasionally for no known reason
            log.debug("unexpected AIOOBE");
        }
        return false;
    }
    
    public void addRequested(java.awt.event.ActionEvent e) {
        var text = findID.getText();
        EventID eventID = new EventID(text);
        // first, add the event
        var memo = new EventTableDataModel.TripleMemo(
                            eventID,
                            "",
                            null,
                            "",
                            null,
                            ""
                        );
        // check to see if already in there:
        boolean found = false;
        for (var check : EventTableDataModel.memos) {
            if (memo.eventID.equals(check.eventID)) {
                found = true;
                break;
            }
        }
        if (! found) {
            EventTableDataModel.memos.add(memo);
        }
        model.fireTableDataChanged();
        // now select that one
        findRequested(e);
        
    }
    
    public void sensorRequested(java.awt.event.ActionEvent e) {
        // loop over sensors to find the OpenLCB ones
        var beans = InstanceManager.getDefault(SensorManager.class).getNamedBeanSet();
        for (NamedBean bean : beans ) {
            if (bean instanceof OlcbSensor) {
                oneSensorToTag(true,  bean); // active
                oneSensorToTag(false, bean); // inactive
            }
        }
    }

    private void oneSensorToTag(boolean isActive, NamedBean bean) {
        var sensor = (OlcbSensor) bean;
        var sensorID = sensor.getEventID(isActive);
        if (! isEventNamePresent(sensorID)) {
            // add the association
            nameStore.addMatch(sensorID, sensor.getEventName(isActive));
        }
    }

    public void turnoutRequested(java.awt.event.ActionEvent e) {
        // loop over turnouts to find the OpenLCB ones
        var beans = InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet();
        for (NamedBean bean : beans ) {
            if (bean instanceof OlcbTurnout) {
                oneTurnoutToTag(true,  bean); // thrown
                oneTurnoutToTag(false, bean); // closed
            }
        }
    }

    private void oneTurnoutToTag(boolean isThrown, NamedBean bean) {
        var turnout = (OlcbTurnout) bean;
        var turnoutID = turnout.getEventID(isThrown);
        if (! isEventNamePresent(turnoutID)) {
            // add the association
            nameStore.addMatch(turnoutID, turnout.getEventName(isThrown));
        }
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
            fileChooser = new jmri.util.swing.JmriJFileChooser();
        }
        fileChooser.setDialogTitle(Bundle.getMessage("PaneSaveCsvFile"));
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

                    str.print(model.getValueAt(i, EventTableDataModel.COL_EVENTID));
                    str.print(model.getValueAt(i, EventTableDataModel.COL_EVENTNAME));
                    str.print(model.getValueAt(i, EventTableDataModel.COL_PRODUCER_NODE));
                    str.print(model.getValueAt(i, EventTableDataModel.COL_PRODUCER_NAME));
                    str.print(model.getValueAt(i, EventTableDataModel.COL_CONSUMER_NODE));
                    str.print(model.getValueAt(i, EventTableDataModel.COL_CONSUMER_NAME));

                    String[] contexts = model.getValueAt(i, EventTableDataModel.COL_CONTEXT_INFO).toString().split("\n"); // multi-line cell
                    for (String context : contexts) {
                        str.print(context);
                    }
                    
                    str.println();
                }
                str.flush();
            } catch (IOException ex) {
                log.error("Error writing file", ex);
            }
        }
    }

    /**
     * Read event names from a CSV file
     * @param e Needed for signature of method, but ignored here
     */
    public void readFromCsvFile(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new jmri.util.swing.JmriJFileChooser();
        }
        fileChooser.setDialogTitle(Bundle.getMessage("PaneOpenCsvFile"));
        fileChooser.rescanCurrentDirectory();

        int retVal = fileChooser.showOpenDialog(this);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to read from CSV file {}", file);
            }

            try (Reader in = new FileReader(file)) {
                Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
                
                for (CSVRecord record : records) {
                    String eventIDname = record.get(0);
                     // Is the 1st column really an event ID
                    EventID eid;
                    try {
                        eid = new EventID(eventIDname);
                    } catch (IllegalArgumentException e1) {
                        // really shouldn't happen, as table manages column contents
                        log.warn("Column 0 doesn't contain an EventID: {}", eventIDname);
                        continue;
                    }
                    // here we have a valid EventID, assign the name if currently blank
                    if (! isEventNamePresent(eid)) {
                        String eventName = record.get(1);
                        nameStore.addMatch(eid, eventName);
                    }         
                }
                log.debug("File reading complete");
                // cause the table to update
                model.fireTableDataChanged();
                
            } catch (IOException ex) {
                log.error("Error reading file", ex);
            }
        }
    }

    /**
     * Check whether a Event Name tag is defined or not.
     * Check for other uses before changing this.
     * @param eventID EventID in native form
     * @return true is the event name tag is present
     */
    public boolean isEventNamePresent(EventID eventID) {
        return nameStore.hasEventName(eventID);
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
                        // no matching consumer
                        return false;
                    }
                }

                // check for group match
                if ( matchGroupName.getSelectedIndex() > 0) {  // -1 is empty combobox
                    String group = matchGroupName.getSelectedItem().toString();
                    var memo = model.getTripleMemo(row);
                    if ( (! groupStore.isNodeInGroup(memo.producer, group))
                        && (! groupStore.isNodeInGroup(memo.consumer, group)) ) {
                            return false;
                    }
                }
                
                // passed all filters
                return true;
            }
        };
        sorter.setRowFilter(rf);
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
            ThreadingUtil.runOnGUIEventually(()->{
                var nodeID = msg.getSourceNodeID();
                var eventID = msg.getEventID();
                model.recordProducer(eventID, nodeID, "", true);
                model.highlightProducer(eventID, nodeID);
            });
        }

        /**
         * Handle "Consumer Identified" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender){
            ThreadingUtil.runOnGUIEventually(()->{
                var nodeID = msg.getSourceNodeID();
                var eventID = msg.getEventID();
                model.recordConsumer(eventID, nodeID, "");
            });
        }

        /**
         * Handle "Producer Identified" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender){
            ThreadingUtil.runOnGUIEventually(()->{
                var nodeID = msg.getSourceNodeID();
                var eventID = msg.getEventID();
                model.recordProducer(eventID, nodeID, "", false);
            });
        }

        @Override
        public void handleConsumerRangeIdentified(ConsumerRangeIdentifiedMessage msg, Connection sender){
            ThreadingUtil.runOnGUIEventually(()->{
                final var nodeID = msg.getSourceNodeID();
                final var eventID = msg.getEventID();
                
                final long rangeSuffix = eventID.rangeSuffix();
                // have to set low part of event ID to 0's as it might be 1's
                EventID zeroedEID = new EventID(eventID.toLong() & (~rangeSuffix));
                
                model.recordConsumer(zeroedEID, nodeID, (new EventID(eventID.toLong() | rangeSuffix)).toShortString());
            });
        }
    
        @Override
        public void handleProducerRangeIdentified(ProducerRangeIdentifiedMessage msg, Connection sender){
            ThreadingUtil.runOnGUIEventually(()->{
                final var nodeID = msg.getSourceNodeID();
                final var eventID = msg.getEventID();
                
                final long rangeSuffix = eventID.rangeSuffix();
                // have to set low part of event ID to 0's as it might be 1's
                EventID zeroedEID = new EventID(eventID.toLong() & (~rangeSuffix));
                
                model.recordProducer(zeroedEID, nodeID, (new EventID(eventID.toLong() | rangeSuffix)).toShortString(), false);
            });
        }

        /*
         * We no longer handle "Simple Node Ident Info Reply" messages because of
         * excessive redisplays.  Instead, we expect the MimicNodeStore to handle
         * these and provide the information when requested.
         */
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    public static class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("LCC Event Table",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    EventTablePane.class.getName(),
                    jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
        }
        
        public Default(String name, jmri.util.swing.WindowInterface iface) {
            super(name,
                    iface,
                    EventTablePane.class.getName(),
                    jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));        
        }

        public Default(String name, Icon icon, jmri.util.swing.WindowInterface iface) {
            super(name,
                    icon, iface,
                    EventTablePane.class.getName(),
                    jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));        
        }
    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTablePane.class);
}
