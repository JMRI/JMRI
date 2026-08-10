package jmri.jmrix.openlcb.swing.eventtable;

import jmri.jmrix.openlcb.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openlcb.*;
import org.openlcb.implementations.EventTable;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2026
 */
public class EventTableDataModelTest {

    @Test
    public void tableTest() {
    }

    // test for adding produced events
    @Test
    public void tableModelLoadProducerTest() {
        var model = new EventTableDataModel(store, eventTable, olcbStore);
        
        model.recordProducer(new EventID("1.1.1.1.2.2.2.2"), new NodeID("1.1.1.1.2.2"),"", false);
        
        assertEquals(1, model.getRowCount());
        
        // duplicate event and node is not a new row
        model.recordProducer(new EventID("1.1.1.1.2.2.2.2"), new NodeID("1.1.1.1.2.2"),"", false);

        assertEquals(1, model.getRowCount());

        // duplicate event, different source node is a new row
        model.recordProducer(new EventID("1.1.1.1.2.2.2.2"), new NodeID("1.1.1.1.3.3"),"", false);

        assertEquals(2, model.getRowCount());
        
    }

    final NodeID nid1 = new NodeID(new byte[]{0, 0, 0, 0, 0, 1});
    MimicNodeStore store;
    EventTable eventTable;
    OlcbEventNameStore olcbStore;
    
    final Connection connection = new AbstractConnection() {
        @Override
        public void put(Message msg, Connection sender) {
        }
    };

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        
        store = new MimicNodeStore(connection, nid1);
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
    }

    @AfterEach
    public void tearDown() {
        olcbStore.deregisterShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(EventTableDataModelTest.class);

}
