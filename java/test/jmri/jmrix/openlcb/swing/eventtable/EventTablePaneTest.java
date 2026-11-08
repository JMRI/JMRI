package jmri.jmrix.openlcb.swing.eventtable;

import java.io.File;

import javax.swing.*;
import javax.swing.table.*;

import jmri.jmrix.openlcb.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openlcb.*;
import org.openlcb.implementations.EventTable;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2026
 */
public class EventTablePaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void CsvWriteReadTest(@TempDir File folder) {
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
        File csvFile = new File(folder, "eventNameTable.csv");
        testPanel.model.csvWriteOperation(csvFile);
        assertTrue(csvFile.isFile(), "CSV file written");
        
        // clear the state: a new name store with no names, and a model using it
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
        testPanel.model = getModel();
        testPanel.nameStore = olcbStore;
        
        // read that back
        testPanel.model.csvReadOperation(csvFile);
        
        // and check the event name mapping came from the file
        assertEquals("namedEvent", olcbStore.getEventName(new EventID("1.2.3.4.5.6.7.8")));
        
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
