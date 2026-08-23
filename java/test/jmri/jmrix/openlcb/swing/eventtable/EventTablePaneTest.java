package jmri.jmrix.openlcb.swing.eventtable;

import java.io.File;

import javax.swing.*;
import javax.swing.table.*;

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
public class EventTablePaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void CsvWriteReadTest() {
        // first add some system content
        olcbStore.addMatch(new EventID("1.2.3.4.5.6.7.8"), "namedEvent");
        
        // create objects to test
        var testPanel = (EventTablePane)panel;
        testPanel.model = getModel();  
        testPanel.nameStore = olcbStore;
        
        // load more content directly to model
        testPanel.model.addMatch(new NodeID("1.2.3.4.5.6"), "namedNode");
               
        testPanel.model.recordProducer(new EventID("1.2.3.4.5.6.7.8"), new NodeID("1.2.3.4.5.6"), "", false);
        testPanel.model.recordConsumer(new EventID("1.2.3.4.5.6.7.8"), new NodeID("1.2.3.4.5.6"), "");
        
        // write a temporary CSV file
        testPanel.csvWriteOperation(new File("temp/eventNameTable.csv"));
        
        // clear the state
        testPanel.model = getModel();   
        
        olcbStore.deregisterShutdownTask();
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
        testPanel.nameStore = olcbStore;
        
        // read that back
        testPanel.csvReadOperation(new File("temp/eventNameTable.csv"));
        
        // and check the event name mapping
        var resultName = model.getValueAt(0, EventTableDataModel.COL_EVENTNAME);
        assertEquals("namedEvent", resultName);
        
    }
    
    
    // infrastructure
    
    final NodeID nid1 = new NodeID(new byte[]{0, 0, 0, 0, 0, 1});
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
  
        EventTableDataModel.clearStatics();  // ensure static content starts empty
  
        model = new EventTableDataModel(store, eventTable, olcbStore){
            // kill default persistence
            @Override
            public void loadModelData() {}
            @Override
            protected void initShutdownTask() {}
        };
        var table = new JTable(model);
        model.table = table;
        model.sorter = new TableRowSorter<>(model);
        
        return model;
    }
        
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

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTablePaneTest.class);

}
