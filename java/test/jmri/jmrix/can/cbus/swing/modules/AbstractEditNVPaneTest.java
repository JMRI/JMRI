package jmri.jmrix.can.cbus.swing.modules;

import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of AbstractEditNVPane
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class AbstractEditNVPaneTest {
    
    @Test
    public void testCtor() {

        CbusNode nd = new CbusNode(memo, 12345);
        int [] nvs = new int[] {1, 1};
        nd.getNodeNvManager().setNVs(nvs);
        AbstractEditNVPaneImpl t = new AbstractEditNVPaneImpl(model, nd);
        Assert.assertNotNull("exists",t);
    }
    
    // Abstract class cannot be instantiated directly
    private static class AbstractEditNVPaneImpl extends AbstractEditNVPane {

        AbstractEditNVPaneImpl(CbusNodeNVTableDataModel dataModel, CbusNode node) {
            super(dataModel, node);
        }

        @Override
        public AbstractEditNVPane getContent() {
            return null;
        }

        @Override
        public void tableChanged(TableModelEvent e) {
        }
    }
    
    private CanSystemConnectionMemo memo = null;
    private CbusNodeNVTableDataModel model = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        model = new CbusNodeNVTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(model);
        model.dispose();
        model = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
    
}
