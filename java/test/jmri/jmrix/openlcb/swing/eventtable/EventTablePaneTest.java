package jmri.jmrix.openlcb.swing.eventtable;

import jmri.jmrix.openlcb.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
//import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openlcb.*;
import org.openlcb.implementations.EventTable;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2026
 */
public class EventTablePaneTest extends jmri.util.swing.JmriPanelTest {


    @Test
    public void tableTest() {
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
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        panel = new EventTablePane();
        title = "Event Table";
        helpTarget = "package.jmri.jmrix.openlcb.swing.eventtable.EventTablePane";
        
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
    @Override
    public void tearDown() {
        olcbStore.deregisterShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(EventTablePaneTest.class);

}
