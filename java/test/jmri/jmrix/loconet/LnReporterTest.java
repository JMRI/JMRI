package jmri.jmrix.loconet;

import jmri.IdTag;
import jmri.InstanceManager;
import jmri.Reportable;
import jmri.LocoAddress;
import jmri.NamedBean;
import jmri.PhysicalLocationReporter;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.PhysicalLocation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.LnReporter class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LnReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return "3 enter";
    }

    @Test
    public void testLnReporterCreate() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        Assert.assertNotNull("exists", a1);
    }

    @Test
    public void testTranspond3Enter146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x7D, 0x03, 0x00});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 3 enter 146", "3 enter", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testTranspond257Enter146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x02, 0x01, 0x00});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 257 enter 146", "257 enter", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testTranspond257Exit146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x02, 0x01, 0x00});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 257 exits 146", "257 exits", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testTranspond3Exits146() {
        LnReporter a = new LnReporter(146, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x7D, 0x03, 0x00});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 3 exits 146", "3 exits", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testTranspond1056Enter175() {
        LnReporter a = new LnReporter(175, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2E, 0x08, 0x20, 0x04});
        a.messageFromManager(l);
        Assert.assertEquals("Transponding 1056 enter 175", "1056 enter", ((Reportable)a.getLastReport()).toReportString());
    }

    @Test
    public void testLnReporterLissy1() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x60, 0x01, 0x42, 0x35, 0x05});
        a1.messageFromManager(l);
        Assert.assertEquals("Lissy message 1", "8501 seen southbound", ((Reportable)a1.getLastReport()).toReportString());
    }

    @Test
    public void testLnReporterLissy2() {
        LnReporter a3 = new LnReporter(3, tc, "L");
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x08, 0x00, 0x40, 0x03, 0x42, 0x35, 0x05});
        a3.messageFromManager(l);
        Assert.assertEquals("Lissy message 2", "8501 seen northbound", ((Reportable)a3.getLastReport()).toReportString());
    }
    
    @Test
    public void testMessageFromManagerFindReport() {
        Assert.assertEquals("MessageFromManagerFindReport- check initial state", 
                -1, r.getState());
        Assert.assertEquals("check reporter number", 3, ((LnReporter)r).getNumber());

        LocoNetMessage m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x00, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00});
        ((LnReporter)r).messageFromManager(m);

        Assert.assertEquals("MessageFromManagerFindReport- check state after message 1", 
                7, r.getState());
        
        Assert.assertEquals("MessageFromManagerFindReport- check report string after message 1", 
                "7 enter", r.getCurrentReport().toString() );

        Assert.assertEquals("MessageFromManagerFindReport- check last loco after message 1",
                7, ((LnReporter)r).lastLoco);
        
        Assert.assertEquals("MessageFromManagerFindReport- check location last seen after message 1", 
                "LR3", ((IdTag)((LnReporter)r).getCurrentReport()).getWhereLastSeen().toString());
        Assert.assertEquals("MessageFromManagerFindReport- check ID last seen after message 1", 
                "LD7", ((IdTag)((LnReporter)r).getCurrentReport()).getDisplayName());

        LnReporter r2 = new LnReporter(17, tc, "L");
        Assert.assertEquals("MessageFromManagerFindReport - check state of new reporter",
                -1, r2.getState());

        LocoNetMessage m2 = new LocoNetMessage(new int[] {0xe5, 0x09, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00});
        r2.messageFromManager(m2);
        Assert.assertEquals("MessageFromManagerFindReport- check state after message 1", 
                3, r2.getState());
        
    }
    
    @Test
    public void testGetBeanPhysicalLocation() {
        // NOTE: it is unclear how JMRI makes use of the "physical location" 
        // feature with respect to Reporters and IdTags, so testing here is 
        // sketchy at best.
        LocoNetMessage m = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2E, 0x08, 0x20, 0x04});
        ((LnReporter)r).messageFromManager(m);
        Assert.assertEquals("check physical location message 1", PhysicalLocation.Origin, ((LnReporter)r).getPhysicalLocation());
        Assert.assertEquals("check physical location string message 1", PhysicalLocation.Origin, ((LnReporter)r).getPhysicalLocation("balderdash"));
    }

    @Test
    public void testLnReporterGetLocoAddress() {
        LocoAddress t = ((LnReporter)r).getLocoAddress("7413 enter");
        Assert.assertEquals("getLocoAddress 7431 enter", t.getNumber(), 7413);
        
        LocoAddress t2 = ((LnReporter)r).getLocoAddress(null);
        Assert.assertNull("getLocoAddress <null>", t2);
        
        LocoAddress t3 = ((LnReporter)r).getLocoAddress("abdc enter");
        Assert.assertNull("getLocoAddress abcd enter", t3);
        
        
    }

    @Test
    public void testIsTranspondingLocationReport() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0x81, 0x7e});
        Assert.assertFalse("isTranspondingLocationReport-0x81", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xE5 0x09 0x00", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x49, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xD0 0x49", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x59, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xD0 0x59", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x69, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xD0 0x69", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x79, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingLocationReport-0xD0 0x79", ((LnReporter)r).isTranspondingLocationReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x01, 0x00, 0x00, 0x00, 0x00});
        Assert.assertTrue("isTranspondingLocationReport-0xD0 0x01", ((LnReporter)r).isTranspondingLocationReport(m));
    }


    @Test
    public void testIsTranspondingFindReport() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0x81, 0x7e});
        Assert.assertFalse("isTranspondingLocationReport-0x81", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertTrue("isTranspondingFindReport-0xE5 0x09 0x00", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x01", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x02", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x04", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x08", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x10", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x20", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x40", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x09 0x7f", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xe5, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xE5 0x08 0x00", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x49, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x49", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x59, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x59", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x69, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x69", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x79, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x79", ((LnReporter)r).isTranspondingFindReport(m));

        m = new LocoNetMessage(new int[] {0xD0, 0x01, 0x00, 0x00, 0x00, 0x00});
        Assert.assertFalse("isTranspondingFindReport-0xD0 0x01", ((LnReporter)r).isTranspondingFindReport(m));
    }

    @Test
    public void testGetLocoAddrFromTranspondingMsg() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0, 0, 0, 0, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0 0 0", 0, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 1, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 1 0 0", 128, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 2, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 2 0 0", 256, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 4, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 4 0 0", 512, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 8, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 8 0 0", 1024, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x10, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x10 0 0", 2048, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x20, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x20 0 0", 4096, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0 0", 8192, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x01, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x01 0", 8193, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x02, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x02 0", 8194, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x04, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x04 0", 8196, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x08, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x08 0", 8200, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));


        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x10, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x10 0", 8208, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x20, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x20 0", 8224, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x40, 0x40, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x40 0x40 0", 8256, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0 0", 0, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x01, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x01 0", 1, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x02, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x02 0", 2, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x04, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x04 0", 4, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x08, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x08 0", 8, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x10, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x10 0", 16, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x20, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x20 0", 32, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x40, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x40 0", 64, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));

        m = new LocoNetMessage(new int[] {0, 0, 0, 0x7D, 0x7f, 0});
        Assert.assertEquals("getLocoAddrFromTranspondingMsg-0 0 0 0x7D 0x7F 0", 127, ((LnReporter)r).getLocoAddrFromTranspondingMsg(m));
    }
    
    @Test
    public void testGetNumber() {
        Assert.assertEquals("getNumber - A", 3, ((LnReporter)r).getNumber());
        
        r = new LnReporter(1, tc, "L");
        Assert.assertEquals("getNumber - B", 1, ((LnReporter)r).getNumber());
        
        r = new LnReporter(42, tc, "L");
        Assert.assertEquals("getNumber - C", 42, ((LnReporter)r).getNumber());
    }
    
    @Test
    public void testGetPhysicalLocationAndAddress() {
//        Assert.assertEquals("initial physical location", 
//            PhysicalLocationReporter.Direction.UNKNOWN, 
//            ((LnReporter)r).getDirection(((Reportable)r.getLastReport()).toReportString()));
        
        
        LocoNetMessage m = new LocoNetMessage(new int[] {0xe5, 0x09, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00});
        ((LnReporter)r).messageFromManager(m);
        Assert.assertEquals("physical location after message 1", 
            PhysicalLocationReporter.Direction.ENTER, 
            ((LnReporter)r).getDirection(((Reportable)r.getLastReport()).toReportString()));
        Assert.assertEquals("loco addr after message 1", "128(D)", 
                ((LnReporter)r).getLocoAddress(
                        ((Reportable)r.getLastReport()).toReportString()).toString());

        m = new LocoNetMessage(new int[]{0xD0, 0x01, 0x11, 0x02, 0x01, 0x00});
        ((LnReporter)r).messageFromManager(m);
        Assert.assertEquals("physical location after message 2", 
            PhysicalLocationReporter.Direction.EXIT,
            ((LnReporter)r).getDirection(((Reportable)r.getLastReport()).toReportString()));
        Assert.assertEquals("loco addr after message 2", "257(D)", 
                ((LnReporter)r).getLocoAddress(
                        ((Reportable)r.getLastReport()).toReportString()).toString());
        
        m = new LocoNetMessage(new int[]{0xD0, 0x21, 0x11, 0x02, 0x01, 0x00});
        ((LnReporter)r).messageFromManager(m);
        Assert.assertEquals("physical location after message 2", 
            PhysicalLocationReporter.Direction.ENTER,
            ((LnReporter)r).getDirection(((Reportable)r.getLastReport()).toReportString()));

        Assert.assertEquals("getDirection- check nonsense string", 
                PhysicalLocationReporter.Direction.UNKNOWN,
                ((LnReporter)r).getDirection("harrumph 84"));

        Assert.assertEquals("getDirection- check short string", 
                PhysicalLocationReporter.Direction.UNKNOWN,
                ((LnReporter)r).getDirection("harrumph"));

        Assert.assertEquals("getDirection- check seen lissy 1", 
                PhysicalLocationReporter.Direction.ENTER,
                ((LnReporter)r).getDirection("54 seen northbound"));

        Assert.assertEquals("getDirection- check seen lissy 2", 
                PhysicalLocationReporter.Direction.ENTER,
                ((LnReporter)r).getDirection("54 seen southbound"));

        Assert.assertEquals("getDirection- check exit", 
                PhysicalLocationReporter.Direction.EXIT,
                ((LnReporter)r).getDirection("155 exits"));

    }

    @Test
    public void testLnReporterLissyBad() {
        LnReporter a1 = new LnReporter(1, tc, "L");
        
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4, 0x07, 0x00, 0x60, 0x01, 0x42, 0x35, 0x05});
        a1.messageFromManager(l);
        Assert.assertEquals("bad Lissy message check report is null", null, a1.getLastReport());
    }

    
    @Test
    public void testGetDriectionString() {
        
    }
    @Test
    public void testCollectionAfterMessage() {
        LnReporter lr = ((LnReporter)r);

        LocoNetMessage l = new LocoNetMessage(new int[]{0xD0, 0x20, 0x02, 0x7D, 0x03, 0x73});
        lr.messageFromManager(l);

       // Check that the collection has one element.
       Assert.assertEquals("Collection Size 1 after message", 1, lr.getCollection().size());
       Assert.assertTrue("Current Report contained in collection",lr.getCollection().contains(lr.getCurrentReport()));


       l = new LocoNetMessage(new int[]{0xD0, 0x00, 0x02, 0x7D, 0x03, 0x53});
       lr.messageFromManager(l);

       // Check that the collection was cleared.
       Assert.assertEquals("Collection Size 0 after clear message", 0, lr.getCollection().size());
       Assert.assertTrue("Collection Empty", lr.getCollection().isEmpty());
       // eventually, the current report should be changed to null on an exit.
       //Assert.assertNull("Current Report Null",lr.getCurrentReport());
    }

    private jmri.jmrix.loconet.LocoNetInterfaceScaffold tc;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();
        new TranspondingTagManager(); // this class registers itself.
        r = new LnReporter(3, tc, "L");
    }

    @After
    @Override
    public void tearDown() {
	    r = null;
	    tc = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
