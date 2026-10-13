package jmri.jmrix.can.cbus.eventtable;

import jmri.jmrix.can.CanSystemConnectionMemo;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusBasicEventTableModelTest {

    @Test
    public void testCTor() {
        
        CbusBasicEventTableModel t = new CbusBasicEventTableModel(memo);
        Assertions.assertNotNull(t);
        
    }

    // The row lookup cache must stay correct when rows are removed or the table is cleared
    @Test
    public void testEventTableRowAfterRowRemoval() {

        CbusBasicEventTableModel t = new CbusBasicEventTableModel(memo);
        CbusTableEvent a = t.provideEvent(1, 1);
        CbusTableEvent b = t.provideEvent(0, 2); // short event
        CbusTableEvent c = t.provideEvent(65535, 65535); // highest node and event
        Assertions.assertEquals(3, t.getRowCount());
        Assertions.assertSame(a, t.provideEvent(1, 1), "existing event returned");
        Assertions.assertEquals(0, t.getEventTableRow(1, 1));
        Assertions.assertEquals(1, t.getEventTableRow(0, 2));
        Assertions.assertEquals(2, t.getEventTableRow(65535, 65535));
        Assertions.assertEquals(-1, t.getEventTableRow(2, 1), "not on table");
        Assertions.assertEquals(-1, t.getEventTableRow(1, 2), "node and event not transposed");

        t.ta.removeRow(0); // rows shift up, cached rows are now stale
        Assertions.assertEquals(-1, t.getEventTableRow(1, 1), "removed event not found");
        Assertions.assertEquals(0, t.getEventTableRow(0, 2), "b now row 0");
        Assertions.assertEquals(1, t.getEventTableRow(65535, 65535), "c now row 1");
        Assertions.assertSame(b, t.provideEvent(0, 2));
        Assertions.assertSame(c, t.provideEvent(65535, 65535));

        CbusTableEvent a2 = t.provideEvent(1, 1); // re-added at the end
        Assertions.assertNotSame(a, a2);
        Assertions.assertEquals(2, t.getEventTableRow(1, 1));
        Assertions.assertEquals(3, t.getRowCount());
        t.ta.dispose();
    }

    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);
        memo.configureManagers();
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusBasicEventTableModelTest.class);

}
