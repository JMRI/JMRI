package jmri.jmrix.openlcb.swing.eventtable.configurexml;

import javax.swing.*;
import javax.swing.table.*;

import jmri.jmrix.openlcb.*;
import jmri.jmrix.openlcb.swing.eventtable.EventTableDataModel;
import jmri.util.JUnitUtil;

// import org.jdom2.Element;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.openlcb.*;
import org.openlcb.implementations.EventTable;

/**
 * Tests for EventTableDataModel persistence
 *
 * @author   Bob Jacobsen 2026
 */
public class EventTableDataModelXmlTest {

    @Test
    public void testSaveAndRestore() {
    
        var cxml1 = new EventTableDataModelXml(model) {
            @Override
            protected String getModelFileDirectoryName() {
                return "test";
            }
        };
        
        // the event name store is separately set up from this configureXML write/read
        var targetEventP = new EventID("1.1.1.1.2.2.2.2");
        olcbStore.addMatch(targetEventP, "eventP");

        model.addMatch(new NodeID("1.1.1.1.2.2"), "namedNode");
        model.recordProducer(targetEventP, new NodeID("1.1.1.1.2.2"),"", false);
        
        var targetEventC = new EventID("0F.1.1.1.3.3.2.2");
        olcbStore.addMatch(targetEventC, "eventC");
        model.recordConsumer(targetEventC, new NodeID("1.1.1.1.3.3"),"");

        model.addAuxiliaryInformation(targetEventC, "describe C1");
        model.addAuxiliaryInformation(targetEventC, "describe C2");
        model.addAuxiliaryInformation(targetEventC, "describe C3");
        
        try {
            cxml1.store();
        } catch (Exception e) {
            Assert.fail("got unexpected Exception");
        }
    
        // Now test read
        model = getModel(); // test load with a new one

        var cxml2 = new EventTableDataModelXml(model) {
            @Override
            protected String getModelFileDirectoryName() {
                return "test";
            }
        };

        cxml2.load();
        
        Assert.assertEquals(1, model.getNodeIDMatches().size());       
        Assert.assertEquals("namedNode", model.getNodeName(new NodeID("1.1.1.1.2.2")));       
        Assert.assertEquals(new NodeID("1.1.1.1.2.2"), model.getNodeID("namedNode"));       

        Assert.assertEquals(2, model.getRowCount()); // separate producer and consumer rows

        Assert.assertEquals("01.01.01.01.02.02.02.02", model.getValueAt(0,EventTableDataModel.COL_EVENTID));
        Assert.assertEquals("01.01.01.01.02.02", model.getValueAt(0,EventTableDataModel.COL_PRODUCER_NODE));
        Assert.assertEquals("namedNode", model.getValueAt(0,EventTableDataModel.COL_PRODUCER_NAME));
        Assert.assertEquals("eventP", model.getValueAt(0,EventTableDataModel.COL_EVENTNAME));
        Assert.assertEquals("Well-Known 01.01.01.01.02.02.02.02", model.getValueAt(0,EventTableDataModel.COL_CONTEXT_INFO));

        Assert.assertEquals("0F.01.01.01.03.03.02.02", model.getValueAt(1,EventTableDataModel.COL_EVENTID));
        Assert.assertEquals("", model.getValueAt(1,EventTableDataModel.COL_PRODUCER_NODE));
        Assert.assertEquals("", model.getValueAt(1,EventTableDataModel.COL_PRODUCER_NAME));
        Assert.assertEquals("eventC", model.getValueAt(1,EventTableDataModel.COL_EVENTNAME));
        Assert.assertEquals("describe C1\ndescribe C2\ndescribe C3", model.getValueAt(1,EventTableDataModel.COL_CONTEXT_INFO));

    }

    @Test
    public void testSaveAndRestoreDifferentNodeID() {
    
        var cxml1 = new EventTableDataModelXml(model) {
            @Override
            protected String getModelFileDirectoryName() {
                return "test";
            }
        };
        
        // the event name store is separately set up from this configureXML write/read
        var targetEventP = new EventID("1.1.1.1.2.2.2.2");
        olcbStore.addMatch(targetEventP, "eventP");

        model.addMatch(new NodeID("1.1.1.1.2.2"), "namedNode");
        model.recordProducer(targetEventP, new NodeID("1.1.1.1.2.2"),"", false);
        
        var targetEventC = new EventID("0F.1.1.1.3.3.2.2");
        olcbStore.addMatch(targetEventC, "eventC");
        model.recordConsumer(targetEventC, new NodeID("1.1.1.1.3.3"),"");

        model.addAuxiliaryInformation(targetEventC, "describe C");
        
        try {
            cxml1.store();
        } catch (Exception e) {
            Assert.fail("got unexpected Exception");
        }
    
        // Now test read with a different nodeID associated with the node name
        model = getModel(); // test load with a new one

        model.addMatch(new NodeID("8.8.1.1.2.2"), "namedNode"); // different node from what was stored

        var cxml2 = new EventTableDataModelXml(model) {
            @Override
            protected String getModelFileDirectoryName() {
                return "test";
            }
        };

        cxml2.load();
        
        Assert.assertEquals(1, model.getNodeIDMatches().size());       
        Assert.assertEquals("namedNode", model.getNodeName(new NodeID("8.8.1.1.2.2")));       
        Assert.assertEquals(new NodeID("8.8.1.1.2.2"), model.getNodeID("namedNode"));       

        Assert.assertEquals(2, model.getRowCount()); // separate producer and consumer rows

        Assert.assertEquals("01.01.01.01.02.02.02.02", model.getValueAt(0,EventTableDataModel.COL_EVENTID));
        Assert.assertEquals("08.08.01.01.02.02", model.getValueAt(0,EventTableDataModel.COL_PRODUCER_NODE));
        Assert.assertEquals("namedNode", model.getValueAt(0,EventTableDataModel.COL_PRODUCER_NAME));
        Assert.assertEquals("eventP", model.getValueAt(0,EventTableDataModel.COL_EVENTNAME));
        Assert.assertEquals("Well-Known 01.01.01.01.02.02.02.02", model.getValueAt(0,EventTableDataModel.COL_CONTEXT_INFO));
        
        Assert.assertEquals("0F.01.01.01.03.03.02.02", model.getValueAt(1,EventTableDataModel.COL_EVENTID));
        Assert.assertEquals("", model.getValueAt(1,EventTableDataModel.COL_PRODUCER_NODE));
        Assert.assertEquals("", model.getValueAt(1,EventTableDataModel.COL_PRODUCER_NAME));
        Assert.assertEquals("eventC", model.getValueAt(1,EventTableDataModel.COL_EVENTNAME));
        Assert.assertEquals("describe C", model.getValueAt(1,EventTableDataModel.COL_CONTEXT_INFO));

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

        store = new MimicNodeStore(connection, nidSource);
        eventTable = new EventTable();

        model = getModel();
    }

    @AfterEach
    public void tearDown() {
        model.deregisterShutdownTask();
        olcbStore.deregisterShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTableDataModelXmlTest.class);
}

