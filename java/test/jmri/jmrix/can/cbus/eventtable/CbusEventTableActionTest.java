package jmri.jmrix.can.cbus.eventtable;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableActionTest {

    @Test
    public void testCTor() {
        
        CbusEventTableAction t = new CbusEventTableAction(null);
        Assert.assertNotNull("exists",t);
        t = null;
    }
    
    @Test
    public void testLoadXmlNoFilePresent() {
        
        CbusEventTableAction t = model.getCbusEventTableAction();
        Assert.assertNotNull("exists",t);
        
        Assert.assertTrue(model.getRowCount()==0);
        
        t.restoreEventsFromXmlTablestart();
        Assert.assertTrue(model.getRowCount()==0);
        
    }
    
    @Test
    public void testLoadGoodFile() throws java.io.IOException, java.text.ParseException {
        
        CbusEventTableAction t = model.getCbusEventTableAction();
        CbusEventTableAction.CbusEventTableXmlFile x = new CbusEventTableAction.CbusEventTableXmlFile();

        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/eventtable/");
        java.io.File systemFile = new java.io.File(dir, "EventTableData-1.xml");

        java.nio.file.Files.copy(systemFile.toPath(), x.getFile(true).toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
        t.restoreEventsFromXmlTablestart();
        
        Assert.assertEquals("Entries loaded",3,model.getRowCount());
        
        CbusTableEvent te = model.provideEvent(0,1);
        Assert.assertNotNull("exists",te);
        Assert.assertEquals("te 1 name", "Short Event 1",te.getName() );
        Assert.assertEquals("te 1 comment", "Short Event 1 Comment",te.getComment() );
        Assert.assertEquals("te 1 on 0", 0, te.getTotalOn() );
        Assert.assertEquals("te 1 off 0", 0, te.getTotalOff() );
        Assert.assertEquals("te 1 in 0", 0, te.getTotalIn() );
        Assert.assertEquals("te 1 out 0", 0, te.getTotalOut() );
        Assert.assertEquals("te 1 state unknown",CbusTableEvent.EvState.UNKNOWN,te.getState());
        Assert.assertEquals("te 1 date unknown",null,te.getDate());
        
        te = model.provideEvent(0,2);
        Assert.assertNotNull("exists",te);
        Assert.assertEquals("te 2 name", "Short Event 2",te.getName() );
        Assert.assertEquals("te 2 comment", "Short Event 2 Comment",te.getComment() );
        Assert.assertEquals("te 2 on 0", 123456, te.getTotalOn() );
        Assert.assertEquals("te 2 off 0", 234567, te.getTotalOff() );
        Assert.assertEquals("te 2 in 0", 345678, te.getTotalIn() );
        Assert.assertEquals("te 2 out 0", 456789, te.getTotalOut() );
        Assert.assertEquals("te 2 state unknown",CbusTableEvent.EvState.UNKNOWN,te.getState());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-08-22 15:23:49"), te.getDate());
        
        te = model.provideEvent(65535,65535);
        Assert.assertNotNull("exists",te);
        Assert.assertEquals("te 65535 - 65535 name", "Long Event Node 65535 Event 65535",te.getName() );
        Assert.assertEquals("te 65535 - 65535 comment", "",te.getComment() );
        Assert.assertEquals("te 65535 - 65535 on 0", 0, te.getTotalOn() );
        Assert.assertEquals("te 65535 - 65535 off 0", 20, te.getTotalOff() );
        Assert.assertEquals("te 65535 - 65535 in 0", 30, te.getTotalIn() );
        Assert.assertEquals("te 65535 - 65535 out 0", 40, te.getTotalOut() );
        Assert.assertEquals("te 65535 - 65535 state unknown",CbusTableEvent.EvState.UNKNOWN,te.getState());
        Assert.assertEquals("te 65535 - 65535 date unknown",null,te.getDate());
        
        Assert.assertEquals("Still 3 Entries loaded",3,model.getRowCount());
    }
    
    @Test
    public void testLoadBadFile() throws java.io.IOException {
        
        CbusEventTableAction t = model.getCbusEventTableAction();
        CbusEventTableAction.CbusEventTableXmlFile x = new CbusEventTableAction.CbusEventTableXmlFile();

        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/eventtable/");
        java.io.File systemFile = new java.io.File(dir, "EventTableData-2.xml");

        java.nio.file.Files.copy(systemFile.toPath(), x.getFile(true).toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
        t.restoreEventsFromXmlTablestart();
        
        JUnitAppender.assertErrorMessageStartsWith("Node or event number missing in event [[Attribute: NodeNum");
        JUnitAppender.assertErrorMessageStartsWith("Node or event number missing in event [[Attribute: EventNum");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");        
        JUnitAppender.assertErrorMessageStartsWith("Unable to parse date [[Attribute: NodeNum=");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Unable to parse date [[Attribute: NodeNum=");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect off / on / in / out value in event [[Attribute:");
        
        Assert.assertEquals("9 Entry loaded",9,model.getRowCount());
        Assert.assertTrue(model.seeIfEventOnTable(0,1)>-1);
        Assert.assertTrue(model.seeIfEventOnTable(11,22)>-1);
        Assert.assertTrue(model.seeIfEventOnTable(11,33)>-1);
        Assert.assertTrue(model.seeIfEventOnTable(11,44)>-1);
        Assert.assertTrue(model.seeIfEventOnTable(11,55)>-1);
        Assert.assertTrue(model.seeIfEventOnTable(22,22)>-1);
        Assert.assertTrue(model.seeIfEventOnTable(22,33)>-1);
        Assert.assertTrue(model.seeIfEventOnTable(22,44)>-1);
        Assert.assertTrue(model.seeIfEventOnTable(22,55)>-1);
        
    }
    
    @Test
    public void testSaveFile() throws java.text.ParseException {
        
        CbusEventTableAction t = model.getCbusEventTableAction();
        
        Assert.assertTrue(model.getRowCount()==0);
        
        CbusTableEvent event1 = model.provideEvent(111,222);
        event1.setTotalOn(1);
        event1.setTotalOff(2);
        event1.setName("My Test Event 1 Name");
        event1.setDate(t.xmlDateStyle.parse("2019-08-22 13:45:49"));
        
        CbusTableEvent event2 = model.provideEvent(333,444);
        event2.setTotalIn(3);
        event2.setTotalOut(4);
        event2.setComment("My Test Event 2 Comment");
        
        Assert.assertTrue("Row Count after adding 2 events",model.getRowCount()==2);
        
        t.storeEventsToXml();
        
        model.clearAllEvents();
        Assert.assertTrue("Row Count before restore 0",model.getRowCount()==0);
        
        t.restoreEventsFromXmlTablestart();
        Assert.assertTrue("Row Count after restore 2",model.getRowCount()==2);
        
        CbusTableEvent te = model.provideEvent(111,222);
        Assert.assertNotNull("te 1 exists",te);
        Assert.assertEquals("te 1 name", "My Test Event 1 Name",te.getName() );
        Assert.assertEquals("te 1 comment", "",te.getComment() );
        Assert.assertEquals("te 1 on 1", 1, te.getTotalOn() );
        Assert.assertEquals("te 1 off 2", 2, te.getTotalOff() );
        Assert.assertEquals("te 1 in 0", 0, te.getTotalIn() );
        Assert.assertEquals("te 1 out 0", 0, te.getTotalOut() );
        Assert.assertEquals("te 1 state unknown",CbusTableEvent.EvState.UNKNOWN,te.getState());
        Assert.assertEquals(t.xmlDateStyle.parse("2019-08-22 13:45:49"), te.getDate());
        
        te = model.provideEvent(333,444);
        Assert.assertNotNull("te 2 exists",te);
        Assert.assertEquals("te 2 name", "",te.getName() );
        Assert.assertEquals("te 2 comment", "My Test Event 2 Comment",te.getComment() );
        Assert.assertEquals("te 2 on 0", 0, te.getTotalOn() );
        Assert.assertEquals("te 2 off 0", 0, te.getTotalOff() );
        Assert.assertEquals("te 2 in 0", 3, te.getTotalIn() );
        Assert.assertEquals("te 2 out 0", 4, te.getTotalOut() );
        Assert.assertEquals("te 2 state unknown",CbusTableEvent.EvState.UNKNOWN,te.getState());
        Assert.assertEquals(null, te.getDate());
    }
    
    private CbusEventTableDataModel model;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // The minimal setup for log4J
    @Before
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        model = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
      
    }

    @After
    public void tearDown() {
        model.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableActionTest.class);

}
