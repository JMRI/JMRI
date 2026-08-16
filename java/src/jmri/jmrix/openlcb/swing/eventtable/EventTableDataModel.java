package jmri.jmrix.openlcb.swing.eventtable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import jmri.*;
import jmri.jmrix.openlcb.*;
import jmri.jmrix.openlcb.swing.eventtable.configurexml.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.openlcb.*;
import org.openlcb.implementations.*;

/**
 * DataModel for the table in the EventTablePane
 * <p>
 * Persistence is handled via the 
 * {@link jmri.jmrix.openlcb.swing.eventtable.configurexml} package.  Note that
 * this class explicitly invokes persistence in the constructor and at shutdown;
 * the general mechanism to store in panel files is not used here intentionally.
 *
 * @author Bob Jacobsen Copyright (C) 2023, 2026
 * @since 5.17.3
 */
public class EventTableDataModel extends AbstractTableModel {

    public EventTableDataModel(MimicNodeStore store, EventTable stdEventTable, OlcbEventNameStore nameStore) {
        this.store = store;
        this.stdEventTable = stdEventTable;
        this.nameStore = nameStore;

        // listen for updated AlsoKnownAs (AKA) content
        stdEventTable.addPropertyChangeListener(eventInfoListener);
        // load stored contents
        loadNameStoreEventIDs();
        
        log.info("************************************");
        log.info("Not loading model data for debugging");
        log.info("************************************");
        // loadModelData();
        
        // arrange to store the current contents at shutdown
        initShutdownTask();
        
    }

    public static final int COL_EVENTID = 0;
    public static final int COL_EVENTNAME = 1;
    public static final int COL_TRIGGER = 2;
    public static final int COL_PRODUCER_NODE = 3;
    public static final int COL_PRODUCER_NAME = 4;
    public static final int COL_CONSUMER_NODE = 5;
    public static final int COL_CONSUMER_NAME = 6;
    public static final int COL_CONTEXT_INFO = 7;
    public static final int COL_COUNT = 8;

    MimicNodeStore store;
    EventTable stdEventTable;
    OlcbEventNameStore nameStore;
    IdTagManager tagManager;
    public JTable table; // public for configurexml test
    public TableRowSorter<EventTableDataModel> sorter; // public for configurexml test
    boolean popcornModeActive = false;

    PropertyChangeListener eventInfoListener = (java.beans.PropertyChangeEvent e) -> {
            eventInfoChanged(e);
        };
    
    PropertyChangeListener eventHandleListener = (java.beans.PropertyChangeEvent e) -> {
            handleDescriptionChanged(e);
        };
        
    // static so the data remains available through a window close-open cycle
    // public to allow access for testing; clean that up sometime with subclassing
    public static final ArrayList<TripleMemo> memos = new ArrayList<>(); // public for testing

    TripleMemo getTripleMemo(int row) {
        if (row >= memos.size()) {
            return null;
        }
        return memos.get(row);
    }

    public static void clearStatics() {
        // to be only used for testing, this clears all class-static information
        nameToNodeId.clear();
        nodeIdToName.clear();
        
        eventToDescriptions.clear();
        memos.clear();
    }
    
    // ********************************
    // Store for node name<->ID mapping
    // ********************************
 
    private static final Map<String, NodeID> nameToNodeId = new HashMap<>();
    private static final Map<NodeID, String> nodeIdToName = new HashMap<>();


    /**
     * @param nodeID The NodeID being searched for
     * @return The name associated with that NodeID or an empty string
     */
    public String getNodeName(NodeID nodeID) {
        var name = nodeIdToName.get(nodeID);
        if (name == null || name.isEmpty()) return nodeID.toString();
        return name;
    }
    
    /**
     * @param nodeID The NodeID being searched for
     * @return true if there is an associated name
     */
    public boolean hasNodeName(NodeID nodeID) {
        var name = nodeIdToName.get(nodeID);
        if (name == null || name.isEmpty()) return false;
        return true;
    }

    /**
     * @param name The node name being searched for
     * @return The NodeID associated with that name or a node ID constructed from the input
     */
    public NodeID getNodeID(String name) {
        var nid = nameToNodeId.get(name);
        if (nid == null) return new NodeID(name);
        return nid;    
    }

    /**
     * @param name The node name being searched for
     * @return true if an NodeID is associated with that name
     */
    public boolean hasNodeID(String name) {
        var nid = nameToNodeId.get(name);
        if (nid == null) return false;
        return true;    
    }

    /**
     * Create a new name to/from NodeID association.
     * <p>
     * If a previous association exits, this one is ignored.
     *
     * @param nodeID associated EventID
     * @param name  associated name
     */
    public void addMatch(NodeID nodeID, String name) {
        if (! (nameToNodeId.containsKey(name) || nodeIdToName.containsKey(nodeID) )) {
            nameToNodeId.put(name, nodeID);
            nodeIdToName.put(nodeID, name);
            log.trace("setting dirty true");
        }
    }

    /**
     * Get all the NodeIDs available
     * @return Set of all available NodeIDs
     */
    public java.util.Set<NodeID> getNodeIDMatches() {
        return nodeIdToName.keySet();        
    }

    // ********************************
    // Store for also-known-as event description information
    // ********************************

    private static final Map<EventID, Set<String>> eventToDescriptions = new HashMap<>(); // public for testing, fix that eventually

    void updateAuxiliaryInformation(EventID event) {
        log.warn("update/recreate event aka info for {}", event);
        // make a new clear list
        var list = new HashSet<String>();
        eventToDescriptions.put(event, list);
        // make sure the list contains all the available descriptions
        for (var entry : stdEventTable.getEventInfo(event).getAllEntries()) {
            log.warn("   added: {}", entry.getDescription());
            list.add(entry.getDescription());
        }
    }

    public void addAuxiliaryInformation(EventID event, String description){
        var list = eventToDescriptions.get(event);
        if (list == null) {
            list = new HashSet<String>();
            eventToDescriptions.put(event, list);
        }
        list.add(description);
    }
    
    public Set<String> getAuxiliaryInformation(EventID event) {
        var retval = eventToDescriptions.get(event);
        if (retval == null) {
            retval = new HashSet<String>();
            eventToDescriptions.put(event, retval);
        }
        return retval;
    }

    public Set<EventID> getAuxiliaryKnownEvents() {
        return eventToDescriptions.keySet();
    }
    
    // ********************************

    // invoked when EventTable.addEvent is called to add an eventID and description
    protected void eventInfoChanged(PropertyChangeEvent e) {
        log.info("eventInfoChanged {} {}", e.getPropertyName(), e.getNewValue());
        // looking for events added to the EventTable
        if (e.getPropertyName().equals(EventTable.EVENT_ENTRY_ADDED)) {
            // this is information added to an event
            var holder = (EventTable.EventTableEntryHolder)e.getNewValue();
            updateAuxiliaryInformation(holder.getEntry().getEvent());
            // register a listener on the EventInfo from the holder
            holder.getList().addPropertyChangeListener(eventHandleListener);
            // and force update to get it drawn by the table
            handleTableUpdate(-1, -1);  // this batches these
        }
    }

    // invoked from EventTable when EventInfo has a description added or deleted,
    // or a description is updated through the EventTableEntry 
    protected void handleDescriptionChanged(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case EventTable.DESCRIPTION_ADDED :
                handleDescriptionAdded(e);
                break;
            case EventTable.DESCRIPTION_REMOVED :
                handleDescriptionRemoved(e);
                break;
            case EventTable.DESCRIPTION_UPDATED :
                handleDescriptionUpdated(e);
                break;
            
            
            default: // we ignore other notifications
                break;
        }
    }
        
    void handleDescriptionAdded(PropertyChangeEvent e){
        log.warn("handleDescriptionAdded: {}", e.getPropertyName());
        var info = (EventTable.EventInfo)e.getNewValue();
        updateAuxiliaryInformation(info.getEventId());        
    }
    
    void handleDescriptionRemoved(PropertyChangeEvent e){
        log.warn("handleDescriptionRemoved: {}", e.getPropertyName());
        var info = (EventTable.EventInfo)e.getNewValue();
        updateAuxiliaryInformation(info.getEventId());        
    }
    
    void handleDescriptionUpdated(PropertyChangeEvent e){
        log.warn("handleDescriptionUpdated: {}", e.getPropertyName());
        var info = (EventTable.EventInfo)e.getNewValue();
        updateAuxiliaryInformation(info.getEventId());        
    }
    
    protected void loadNameStoreEventIDs() {
        // are there events in the Name Store? If so, add them
        for (var eventID: nameStore.getMatches()) {
            var memo = new TripleMemo(
                                eventID,
                                "",
                                null,
                                "",
                                null,
                                ""
                            );
            // check to see if already in there:
            boolean found = false;
            for (var check : memos) {
                if (memo.eventID.equals(check.eventID)) {
                    found = true;
                    break;
                }
            }
            if (! found) {
                memos.add(memo);
            }
        }
    }


    @Override
    public Object getValueAt(int row, int col) {
        if (row >= memos.size()) {
            log.warn("request out of range: {} greater than {}", row, memos.size());
            return "Illegal col "+row+" "+col;
        }
        var memo = memos.get(row);
        switch (col) {
            case COL_EVENTID: 
                String retval = memo.eventID.toShortString();
                if (!memo.rangeSuffix.isEmpty()) retval += " - "+memo.rangeSuffix;
                return retval;
            case COL_EVENTNAME:
                if (nameStore.hasEventName(memo.eventID)) {
                    return nameStore.getEventName(memo.eventID);
                } else {
                    return "";
                }
                
            case COL_TRIGGER:
                return Bundle.getMessage("TableColTrigger");
            case COL_PRODUCER_NODE:
                return memo.producer != null ? memo.producer.toString() : "";
            case COL_PRODUCER_NAME: return memo.producerName;
            case COL_CONSUMER_NODE:
                return memo.consumer != null ? memo.consumer.toString() : "";
            case COL_CONSUMER_NAME: return memo.consumerName;
            case COL_CONTEXT_INFO:

                // When table is constrained, these rows don't match up, need to find constrained row
                var viewRow = sorter.convertRowIndexToView(row);

                if (lineIncrement <= 0) { // load cache variable?
                    if (viewRow >= 0) {
                        lineIncrement = table.getRowHeight(viewRow); // do this if valid row
                    } else {
                        lineIncrement = table.getFont().getSize()*13/10; // line spacing from font if not valid row
                    }
                 }

                var result = new StringBuilder();

                var height = lineIncrement/3; // for margins
                var first = true;   // no \n before first line

                // interpret eventID and start with that if present
                String interp = memo.eventID.parse();
                if (interp != null && !interp.isEmpty()) {
                    height += lineIncrement;
                    result.append(interp);                        
                    first = false;
                }

                // scan the CD/CDI information as available
                for (var entry : getAuxiliaryInformation(memo.eventID)) {
                    if (!first) result.append("\n");
                    first = false;
                    height += lineIncrement;
                    result.append(entry);
                }

                // set height for multi-line output in the cell
                if (viewRow >= 0) { // make sure it's a valid visible row in the table; -1 signals not
                    // set height
                    if (height < lineIncrement) {
                        height = height+lineIncrement; // when no lines, assume 1
                    }
                    if (height != table.getRowHeight(viewRow)) { // avoid repeating repaint loop
                        table.setRowHeight(viewRow, height);
                    }
                } else {
                    lineIncrement = -1;  // reload on next request, hoping for a viewed row
                }
                return new String(result);
            default: return "Illegal column at "+row+" "+col;
        }
    }

    int lineIncrement = -1; // cache the line spacing for multi-line cells; 
                            // this gets the value before any adjustments done

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == COL_EVENTNAME) {
            if (row >= memos.size()) {
                log.warn("request out of range: {} greater than {}", row, memos.size());
                return;
            }
            var memo = memos.get(row);
            nameStore.addMatch(memo.eventID, value.toString());
            return;
        } else if (col == COL_TRIGGER) {
            var nodeMemo = memos.get(row);
            var sysMemo = jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.CanSystemConnectionMemo.class);
            var connection = sysMemo.get(org.openlcb.Connection.class);
            var srcNodeID = sysMemo.get(org.openlcb.NodeID.class);
            Message m = new ProducerConsumerEventReportMessage(srcNodeID, nodeMemo.eventID);
            connection.put(m, null);
            
        }
    }

    @Override
    public int getColumnCount() {
        return COL_COUNT;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COL_EVENTID:       return Bundle.getMessage("TableColEventId");
            case COL_EVENTNAME:     return Bundle.getMessage("TableColEventName");
            case COL_TRIGGER:       return Bundle.getMessage("TableColTrigger");
            case COL_PRODUCER_NODE: return Bundle.getMessage("TableColProducerNode");
            case COL_PRODUCER_NAME: return Bundle.getMessage("TableColProducerName");
            case COL_CONSUMER_NODE: return Bundle.getMessage("TableColConsumerNode");
            case COL_CONSUMER_NAME: return Bundle.getMessage("TableColConsumerName");
            case COL_CONTEXT_INFO:  return Bundle.getMessage("TableColContextInfo");
            default: return "ERROR "+col;
        }
    }

    @Override
    public int getRowCount() {
        return memos.size();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == COL_EVENTNAME || col == COL_TRIGGER;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == COL_TRIGGER) {
            return JButton.class;
        } else {
            return String.class;
        }
    }

    /**
     * Remove all existing data, generally just in advance of an update
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD") // Swing thread deconflicts
    void clear() {
        memos.clear();
        fireTableDataChanged();  // don't queue this one, must be immediate
    }

    /**
     * Notify the table that the contents have changed.
     * To reduce CPU load, this batches the changes
     * @param start first row changed; -1 means entire table (not used yet)
     * @param end   last row changed; -1 means entire table (not used yet)
     */
    void handleTableUpdate(int start, int end) {
        if (log.isTraceEnabled()) { // check logging level to avoid processing irrelevant traceback
            log.trace("handleTableUpdated", jmri.util.LoggingUtil.shortenStacktrace(new Exception("traceback")));
        }
        
        final int DELAY = 500;

        if (!pending) {
            jmri.util.ThreadingUtil.runOnGUIDelayed(() -> {
                pending = false;
                log.debug("handleTableUpdated fires table changed");
                fireTableDataChanged();
            }, DELAY);
            pending = true;
        }

    }
    boolean pending = false;

    private String chooseNodeName(NodeID nodeID) {
        String name = "";

        // see if in the current EventStore, done first to pick up changes
        var nodeMemo = store.findNode(nodeID);
        if (nodeMemo != null) {
            var ident = nodeMemo.getSimpleNodeIdent();
            if (ident != null) {
                    name = ident.getUserName();
                    // and remember this name for later
                    addMatch(nodeID, name);
                    
                }
        }
        // otherwise  if node name already encountered
        if (name.isEmpty() && hasNodeName(nodeID)) {
            name = getNodeName(nodeID);
        }

        // if all else fails, use a name constructed from SNIP information
        if (name.isEmpty() && nodeMemo != null) {
            var ident = nodeMemo.getSimpleNodeIdent();
            if (ident != null) {
                name = ident.getMfgName()+" - "+ident.getModelName()+" - "+ident.getHardwareVersion();
            }
        }

        return name;
    }

    /**
     * Record an event-producer pair
     * @param eventID Observed event
     * @param nodeID  Node that is known to produce the event
     * @param rangeSuffix the range mask string or "" for single events
     * @param pcer true if this Producer was inferred from a PCER message, false if from a Producer Identified message
     */
    public void recordProducer(EventID eventID, NodeID nodeID, String rangeSuffix, boolean pcer) {
        log.debug("recordProducer of {} in {}", eventID, nodeID);

        // update if the model has been cleared
        if (memos.size() <= 1) {
            handleTableUpdate(-1, -1);
        }

        String name = chooseNodeName(nodeID);

        // if this already exists, skip storing it
        // if you can, find a matching memo with an empty consumer value
        TripleMemo empty = null;    // an existing empty cell                       // TODO: switch to int index for handle update below
        TripleMemo bestEmpty = null;// an existing empty cell with matching consumer// TODO: switch to int index for handle update below
        TripleMemo sameNodeID = null;// cell with matching consumer                 // TODO: switch to int index for handle update below
        for (int i = 0; i < memos.size(); i++) {
            var memo = memos.get(i);
            if (memo.eventID.equals(eventID) && memo.rangeSuffix.equals(rangeSuffix) ) {
                // if nodeID matches, already present; ignore
                if (nodeID.equals(memo.producer)) {
                    // The node ID is already registered (hence appearing in table)
                    // for this producer.
                    //
                    // This might be the 2nd EventTablePane to process the data,
                    // hence memos would already have been processed. To
                    // handle that, need to fire a change to the table.
                    //
                    // On the other hand, this rapidly erases the
                    // popcorn display, so we disable it for that.
                    //
                    // We also disable it if this call was from a PCER message,
                    // as those are routine and should have been preceeded
                    // by a Producer Identified. Leaving this in results in
                    // excessive refreshes and e.g. frustrating loss of 
                    // cell selections.
                    //
                    if (! (popcornModeActive | pcer) ) {
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
                // if same consumer slot, remember it
                if (nodeID == memo.consumer) {
                    sameNodeID = memo;
                }
            }
        }

        // can we use the bestEmpty?
        if (bestEmpty != null) {
            // yes
            log.trace("   use bestEmpty");
            bestEmpty.producer = nodeID;
            bestEmpty.producerName = name;
            handleTableUpdate(-1, -1); // TODO: should be rows for bestEmpty, bestEmpty
            return;
        }

        // can we just insert into the empty?
        if (empty != null && sameNodeID == null) {
            // yes
            log.trace("   reuse empty");
            empty.producer = nodeID;
            empty.producerName = name;
            handleTableUpdate(-1, -1); // TODO: should be rows for empty, empty
            return;
        }

        // is there a sameNodeID to insert into?
        if (sameNodeID != null) {
            // yes
            log.trace("   switch to sameID");
            var fromSaveNodeID = sameNodeID.producer;
            var fromSaveNodeIDName = sameNodeID.producerName;
            sameNodeID.producer = nodeID;
            sameNodeID.producerName = name;
            // now leave behind old cell to make new one in next block
            nodeID = fromSaveNodeID;
            name = fromSaveNodeIDName;
        }

        // have to make a new one
        var memo = new TripleMemo(
                        eventID,
                        rangeSuffix,
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
     * @param rangeSuffix the range mask string or "" for single events
     */
    public void recordConsumer(EventID eventID, NodeID nodeID, String rangeSuffix) {
        log.debug("recordConsumer of {} in {}", eventID, nodeID);

        // update if the model has been cleared
        if (memos.size() <= 1) {
            handleTableUpdate(-1, -1);
        }

        String name = chooseNodeName(nodeID);

        // if this already exists, skip storing it
        // if you can, find a matching memo with an empty consumer value
        TripleMemo empty = null;    // an existing empty cell                       // TODO: switch to int index for handle update below
        TripleMemo bestEmpty = null;// an existing empty cell with matching producer// TODO: switch to int index for handle update below
        TripleMemo sameNodeID = null;// cell with matching consumer                 // TODO: switch to int index for handle update below
        for (int i = 0; i < memos.size(); i++) {
            var memo = memos.get(i);
            if (memo.eventID.equals(eventID) && memo.rangeSuffix.equals(rangeSuffix) ) {
                // if nodeID matches, already present; ignore
                if (nodeID.equals(memo.consumer)) {
                    // might be 2nd EventTablePane to process the data,
                    // hence memos would already have been processed. To
                    // handle that, always fire a change to the table.
                    log.trace("    nodeDI == memo.consumer");
                    handleTableUpdate(i, i);
                    return;
                }
                // if empty consumer slot, remember it
                if (memo.consumer == null) {
                    empty = memo;
                    // best empty has matching producer
                    if (nodeID.equals(memo.producer)) bestEmpty = memo;
                }
                // if same producer slot, remember it
                if (nodeID == memo.producer) {
                    sameNodeID = memo;
                }
            }
        }

        // can we use the best empty?
        if (bestEmpty != null) {
            // yes
            log.trace("   use bestEmpty");
            bestEmpty.consumer = nodeID;
            bestEmpty.consumerName = name;
            handleTableUpdate(-1, -1);  // should be rows for bestEmpty, bestEmpty
            return;
        }

        // can we just insert into the empty?
        if (empty != null && sameNodeID == null) {
            // yes
            log.trace("   reuse empty");
            empty.consumer = nodeID;
            empty.consumerName = name;
            handleTableUpdate(-1, -1);  // should be rows for empty, empty
            return;
        }

        // is there a sameNodeID to insert into?
        if (sameNodeID != null) {
            // yes
            log.trace("   switch to sameID");
            var fromSaveNodeID = sameNodeID.consumer;
            var fromSaveNodeIDName = sameNodeID.consumerName;
            sameNodeID.consumer = nodeID;
            sameNodeID.consumerName = name;
            // now leave behind old cell to make new one
            nodeID = fromSaveNodeID;
            name = fromSaveNodeIDName;
        }

        // have to make a new one
        log.trace("    make a new one");
        var memo = new TripleMemo(
                        eventID,
                        rangeSuffix,
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
        log.trace("highlightProducer {} {}", eventID, nodeID);
        for (int i = 0; i < memos.size(); i++) {
            var memo = memos.get(i);
            if (eventID.equals(memo.eventID)  && memo.rangeSuffix.equals("") && nodeID.equals(memo.producer)) {
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
        log.trace("highlightEvent {}", eventID);
        table.clearSelection(); // clear existing selections
        for (int i = 0; i < memos.size(); i++) {
            var memo = memos.get(i);
            if (eventID.equals(memo.eventID) && memo.rangeSuffix.equals("") ) {
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
            if (memo.eventID.equals(eventID) && memo.rangeSuffix.equals("") ) {
                if (memo.consumer!=null) return true;
            }
        }
        return false;
    }

    boolean producerPresent(EventID eventID) {
        for (var memo : memos) {
            if (memo.eventID.equals(eventID) && memo.rangeSuffix.equals("") ) {
                if (memo.producer!=null) return true;
            }
        }
        return false;
    }

    static public class TripleMemo { // would like to use a record for this; public for testing
        final public EventID eventID;
        final public String  rangeSuffix;
        // Event name is stored separately, see getValueAt()
        public NodeID producer;
        public String producerName;
        public NodeID consumer;
        public String consumerName;

        TripleMemo(EventID eventID, String rangeSuffix, NodeID producer, String producerName,
                    NodeID consumer, String consumerName) {
            this.eventID = eventID;
            this.rangeSuffix = rangeSuffix;
            this.producer = producer;
            this.producerName = producerName;
            this.consumer = consumer;
            this.consumerName = consumerName;
        }
    }

    public void loadModelData() {
        log.debug("reading Event Table model data");
        new EventTableDataModelXml(this).load();  // NOI18N
        log.debug("...done reading Event Table model data");
    }

    private Runnable shutDownTask = null;

    public void dispose() {
        // this does _not_ deregister the shutdown task, 
        // as that needs to happen at end of run to write
        // any changes after this is closed.
        //
        // Instead, we just do a write here
        log.debug("Start writing node name details...");
        try {
            writeNodeNameDetails();
        } catch (java.io.IOException ioe) {
            log.error("Exception writing event table data in dispose", ioe);
        }
        
        // drop the external listeners
        stdEventTable.removePropertyChangeListener(eventInfoListener);
        
    }
    
    
    protected void initShutdownTask(){
        // Create shutdown task to save
        log.debug("Register ShutDown task");
        if (this.shutDownTask == null) {
            this.shutDownTask = () -> {
                // Save event name details prior to exit, if necessary
                log.debug("Start writing event table data...");
                try {
                    writeNodeNameDetails();
                } catch (java.io.IOException ioe) {
                    log.error("Exception writing event table data", ioe);
                }
            };
            InstanceManager.getDefault(ShutDownManager.class).register(this.shutDownTask);
        }
    }

    /**
     * De-register the Shutdown task.
     */
    public void deregisterShutdownTask(){
        log.debug("Deregister ShutDown task");
        if ( shutDownTask != null ) {
            InstanceManager.getDefault(ShutDownManager.class).deregister(shutDownTask);
        }
    }

    public void writeNodeNameDetails() throws java.io.IOException {
        log.debug("storing node name map");
        new EventTableDataModelXml(this).store();  // NOI18N
        log.debug("...done writing event name details");
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTableDataModel.class);
}
