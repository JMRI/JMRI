package jmri.jmrix.loconet;

import jmri.LocoAddress;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.LnReporter class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LnReporterTest {

    @Test
    public void testLnReporterCreate() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        Assert.assertNotNull("exists", a1);
    }

    @Test
    public void testTranspond3Enter146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x7D, 0x03, 0x00});
        a.message(l);
        Assert.assertEquals("Transponding 3 enter 146", "3 enter", a.getLastReport().toString());
    }

    @Test
    public void testTranspond257Enter146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x02, 0x01, 0x00});
        a.message(l);
        Assert.assertEquals("Transponding 257 enter 146", "257 enter", a.getLastReport().toString());
    }

    @Test
    public void testTranspond257Exit146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x02, 0x01, 0x00});
        a.message(l);
        Assert.assertEquals("Transponding 257 exits 146", "257 exits", a.getLastReport().toString());
    }

    @Test
    public void testTranspond3Exits146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x7D, 0x03, 0x00});
        a.message(l);
        Assert.assertEquals("Transponding 3 exits 146", "3 exits", a.getLastReport().toString());
    }

    @Test
    public void testTranspond1056Enter175() {
        LnReporter a = new LnReporter(175, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2E, 0x08, 0x20, 0x04});
        a.message(l);
        Assert.assertEquals("Transponding 1056 enter 175", "1056 enter", a.getLastReport().toString());
    }

    @Test
    public void testLnReporterLissy1() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x60, 0x01, 0x42, 0x35, 0x05});
        a1.message(l);
        Assert.assertEquals("Lissy message 1", "8501 seen southbound", a1.getLastReport().toString());
    }

    @Test
    public void testLnReporterLissy2() {
        LnReporter a3 = new LnReporter(3, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x40, 0x03, 0x42, 0x35, 0x05});
        a3.message(l);
        Assert.assertEquals("Lissy message 2", "8501 seen northbound", a3.getLastReport().toString());
    }

    @Test
    public void testLnReporterGetLocoAddress() {
        LnReporter r = new LnReporter(3, tc, "L");
        LocoAddress t = r.getLocoAddress("7413 enter");
        Assert.assertEquals("getLocoAddress", t.getNumber(), 7413);
    }

    private jmri.jmrix.loconet.LocoNetInterfaceScaffold tc;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
