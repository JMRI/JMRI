package jmri.jmrix.can.cbus.swing;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JTable;
import javax.swing.TransferHandler;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CbusSendEventPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusTableRowEventDnDHandlerTest  {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        t = new CbusTableRowEventDnDHandler(null,null);
        assertNotNull(t);
        t.dispose();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testTransferable() throws java.awt.datatransfer.UnsupportedFlavorException, java.io.IOException {
        
        dm = new CbusEventTableDataModel(memo,0,0);
        dm.provideEvent(123, 456);
        dm.provideEvent(222, 333);
        
        JTable table = new JTable(dm);
        table.setName("jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel");
        
        t = new CbusTableRowEventDnDHandler(memo,table);
        assertEquals(TransferHandler.COPY, t.getSourceActions(null));
        assertNull(t.createTransferable(null));
        assertNull(t.createTransferable(table));
        t.mouseMoved(0,0);
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        
        table.setRowSelectionInterval(0, 0);
        Transferable trnfr = t.createTransferable(table);
        JUnitUtil.waitFor(()->{ return(trnfr!=null); }, "Transferable Row 0 Not found");
        assertEquals("+N123E456", trnfr.getTransferData(DataFlavor.stringFlavor));

        table.setRowSelectionInterval(1, 1);
        Transferable trnfrb = t.createTransferable(table);
        JUnitUtil.waitFor(()->{ return(trnfrb!=null); }, "Transferable Row 1 Not found");
        assertEquals("+N222E333", trnfrb.getTransferData(DataFlavor.stringFlavor));
        t.mouseMoved(0,0);
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        
        table.setName("jmri.jmrix.can.cbus.node.CbusNodeEventTableDataModel");
        table.setRowSelectionInterval(0, 0);
        
        Transferable trnfrc = t.createTransferable(table);
        JUnitUtil.waitFor(()->{ return(trnfrc!=null); }, "Transferable NodeEv Row 0 Not found");
        assertEquals("+N123E456", trnfrc.getTransferData(DataFlavor.stringFlavor));
        
        table.setRowSelectionInterval(1, 1);
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        Transferable trnfrd = t.createTransferable(table);
        JUnitUtil.waitFor(()->{ return(trnfrd!=null); }, "Transferable NodeEv Row 1 Not found");
        assertEquals("+N222E333", trnfrd.getTransferData(DataFlavor.stringFlavor));
        
        table.setName("Incorrect Table");
        assertNull(t.createTransferable(table));
        
        dm.skipSaveOnDispose();
        dm.dispose();
        t.dispose();
    }
    
    private CanSystemConnectionMemo memo;
    private CbusTableRowEventDnDHandler t;
    private CbusEventTableDataModel dm;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);
        memo.configureManagers();
    }

    @AfterEach
    public void tearDown() {
        dm = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        t = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
