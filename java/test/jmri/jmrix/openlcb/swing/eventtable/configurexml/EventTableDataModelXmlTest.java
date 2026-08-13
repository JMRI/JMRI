package jmri.jmrix.openlcb.swing.eventtable.configurexml;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.jmrix.openlcb.*;
import jmri.jmrix.openlcb.swing.eventtable.EventTableDataModel;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.openlcb.*;
import org.openlcb.implementations.EventTable;

/**
 * Tests for EventTable persistence
 *
 * @author   Bob Jacobsen 2026
 */
public class EventTableDataModelXmlTest {

    @Test
    public void testSaveAndRestore() {
    
        var cxml1 = new EventTableDataModelXml(model) {
            protected String getModelFileDirectoryName() {
                return "test";
            }
        };
        model.addMatch(new NodeID("1.1.1.1.2.2"), "named");
        
        var targetEventP = new EventID("1.1.1.1.2.2.2.2");
        model.recordProducer(targetEventP, new NodeID("1.1.1.1.2.2"),"", false);
        
        var targetEventC = new EventID("1.1.1.1.3.3.2.2");
        model.recordConsumer(targetEventC, new NodeID("1.1.1.1.3.3"),"");

        try {
            cxml1.store();
        } catch (Exception e) {
            Assert.fail("got unexpected Exception");
        }
    
        model = getModel(); // test load with a new one

        var cxml2 = new EventTableDataModelXml(model) {
            protected String getModelFileDirectoryName() {
                return "test";
            }
        };

        cxml2.load();
        
        Assert.assertEquals(1, model.getNodeIDMatches().size());       
        Assert.assertEquals(2, model.getRowCount()); // separate producer and consumer rows
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
        model.deregisterShutdownTask();
        olcbStore.deregisterShutdownTask();
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTableDataModelXmlTest.class);
}

