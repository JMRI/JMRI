package jmri.jmrix.openlcb.swing.eventtable;

import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import jmri.jmrix.openlcb.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.openlcb.*;
import org.openlcb.implementations.EventTable;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2026
 */
public class EventTableDataModelTest {

    // test handling of duplicate names in the name to/from node ID store
    @Test
    public void duplicateNodeNameTest() {
        var node1 = new NodeID("1.2.3.4.5.6");
        var node2 = new NodeID("6.5.4.3.2.1");
        
        model.addMatch(node1, "sample");
        model.addMatch(node2, "sample");
        
        // The first match added should win
        assertEquals(node1, model.getNodeID("sample"));
        assertEquals("sample", model.getNodeName(node1));
        assertTrue(model.hasNodeName(node1));
        assertFalse(model.hasNodeName(node2));
        
    }
    
    // test for adding produced events
    @Test
    public void tableModelLoadProducerTest() {
        var targetEvent = new EventID("1.1.1.1.2.2.2.2");
        
        model.recordProducer(targetEvent, new NodeID("1.1.1.1.2.2"),"", false);
        
        assertEquals(1, model.getRowCount());
        
        // duplicate event and node is not a new row
        model.recordProducer(targetEvent, new NodeID("1.1.1.1.2.2"),"", false);

        assertEquals(1, model.getRowCount());

        var result = model.getValueAt(0, EventTableDataModel.COL_EVENTID);
        assertEquals(targetEvent.toShortString(), result);

        // duplicate event, different source node is a new row
        model.recordProducer(targetEvent, new NodeID("1.1.1.1.3.3"),"", false);

        assertEquals(2, model.getRowCount());
        
    }

    // test for remembered node name of produced Event
    @Test
    public void tableModelProducerNameTest() {
        model.addMatch(new NodeID("1.1.1.1.2.2"), "namedP");
        assertEquals("namedP", model.getNodeName(new NodeID("1.1.1.1.2.2")));
        
        var targetEvent = new EventID("1.1.1.1.2.2.2.2");
        
        model.recordProducer(targetEvent, new NodeID("1.1.1.1.2.2"),"", false);

        var resultNode = model.getValueAt(0, EventTableDataModel.COL_PRODUCER_NODE);
        assertEquals("01.01.01.01.02.02", resultNode);
                
        var resultName = model.getValueAt(0, EventTableDataModel.COL_PRODUCER_NAME);
        assertEquals("namedP", resultName);
    }

    // test for remembered node name of consumed Event
    @Test
    public void tableModelConsumerNameTest() {
        model.addMatch(new NodeID("1.1.1.1.2.2"), "namedC");
        var targetEvent = new EventID("1.1.1.1.2.2.2.2");
        
        model.recordConsumer(targetEvent, new NodeID("1.1.1.1.2.2"),"");
                
        var resultNode = model.getValueAt(0, EventTableDataModel.COL_CONSUMER_NODE);
        assertEquals("01.01.01.01.02.02", resultNode);
                
        var resultName = model.getValueAt(0, EventTableDataModel.COL_CONSUMER_NAME);
        assertEquals("namedC", resultName);
    }

    // test for notification of description for producer
    @Test
    public void tableModelDescNotifyTest() {

        model.addMatch(new NodeID("1.1.1.1.2.2"), "namedP");
        assertEquals("namedP", model.getNodeName(new NodeID("1.1.1.1.2.2")));
        
        var targetEvent = new EventID("0F.1.1.1.2.2.2.2");
        
        model.recordProducer(targetEvent, new NodeID("1.1.1.1.2.2"),"", false);

        // the following line triggers addition to the model via a listener
        var holder = eventTable.addEvent(targetEvent, "desc1");

        var resultName = model.getValueAt(0, EventTableDataModel.COL_CONTEXT_INFO);
        assertEquals("desc1", resultName);
        
        // remove the description
        log.warn("this doesn't distinquish 'remove by edit' and 'remove when window closes'");
        holder.release();
        
        resultName = model.getValueAt(0, EventTableDataModel.COL_CONTEXT_INFO);
        assertEquals("", resultName);

        // add a new one
        holder.getList().add("desc2");
        
        resultName = model.getValueAt(0, EventTableDataModel.COL_CONTEXT_INFO);
        assertEquals("desc2", resultName);

        // and update one
        holder.getEntry().updateDescription("desc3");

        resultName = model.getValueAt(0, EventTableDataModel.COL_CONTEXT_INFO);
        assertEquals("desc3", resultName);
        
        
    }



    // ***********   Infrastructure Below ************* 
        
    final NodeID nidSource = new NodeID(new byte[]{0, 0, 0, 0, 0, 1});
    MimicNodeStore store;
    EventTable eventTable;
    OlcbEventNameStore olcbStore;
    
    final Connection connection = new AbstractConnection() {
        @Override
        public void put(Message msg, Connection sender) {
        }
    };

    EventTableDataModel model;
    
    EventTableDataModel getModel() {
        if (model != null) {
            model.deregisterShutdownTask();
        }
        model = new EventTableDataModel(store, eventTable, olcbStore){
            // kill default persistence
            @Override
            protected void loadNameStoreEventIDs() {}
            @Override
            public void loadModelData() {}
            @Override
            protected void initShutdownTask() {}
        };
        var table = new JTable(model);
        model.table = table;
        model.sorter = new TableRowSorter<>(model);
        EventTableDataModel.clearStatics();  // ensure static content starts empty
        
        return model;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        
        store = new MimicNodeStore(connection, nidSource);
        eventTable = new EventTable();
        olcbStore = new OlcbEventNameStore() {
            @Override 
            public void readDetails() {
                // don't read the eventNames.xml file
            }
            @Override
            public void writeEventNameDetails() {
                // don't write the eventNames.xml file
            }
        };

        model = getModel();
    }

    @AfterEach
    public void tearDown() {
        olcbStore.deregisterShutdownTask();
        model.deregisterShutdownTask();
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTableDataModelTest.class);

}
