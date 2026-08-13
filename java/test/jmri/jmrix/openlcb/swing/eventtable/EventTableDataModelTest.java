package jmri.jmrix.openlcb.swing.eventtable;

import java.util.ArrayList;

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
        model.addMatch(new NodeID("1.1.1.1.2.2"), "named");
        var targetEvent = new EventID("1.1.1.1.2.2.2.2");
        
        model.recordProducer(targetEvent, new NodeID("1.1.1.1.2.2"),"", false);
                
        var result = model.getValueAt(0, EventTableDataModel.COL_PRODUCER_NAME);
        assertEquals("named", result);
    }

    // test for remembered node name of consumed Event
    @Test
    public void tableModelConsumerNameTest() {
        model.addMatch(new NodeID("1.1.1.1.2.2"), "named");
        var targetEvent = new EventID("1.1.1.1.2.2.2.2");
        
        model.recordConsumer(targetEvent, new NodeID("1.1.1.1.2.2"),"");
                
        var result = model.getValueAt(0, EventTableDataModel.COL_CONSUMER_NAME);
        assertEquals("named", result);
    }

        
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
            public void readDetails() {}
            @Override
            protected void initShutdownTask() {}
        };
        EventTableDataModel.memos = new ArrayList<>(); // static content
        
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

    // private static final Logger log = LoggerFactory.getLogger(EventTableDataModelTest.class);

}
