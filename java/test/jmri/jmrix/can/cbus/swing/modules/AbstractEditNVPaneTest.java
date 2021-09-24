package jmri.jmrix.can.cbus.swing.modules;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CbusNodeInfoPane
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class AbstractEditNVPaneTest {
    
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CbusNode nd = new CbusNode(memo, 12345);
        int [] nvs = new int[] {1, 1};
        nd.getNodeNvManager().setNVs(nvs);
        AbstractEditNVPaneImpl t = new AbstractEditNVPaneImpl(model, nd);
        Assert.assertNotNull("exists",t);
    }
    
    // Abstract class cannot be instantiated directly
    public class AbstractEditNVPaneImpl extends AbstractEditNVPane {

        public AbstractEditNVPaneImpl(CbusNodeNVTableDataModel dataModel, CbusNode node) {
            super(dataModel, node);
        }

        @Override
        public JPanel getContent() {
            return null;
        }

        @Override
        public void tableChanged(TableModelEvent e) {
        }
    }
    
    private CanSystemConnectionMemo memo;
    private CbusNodeNVTableDataModel model;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        model = new CbusNodeNVTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
    }

    @AfterEach
    public void tearDown() {
        model.dispose();
        model = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
    
}
