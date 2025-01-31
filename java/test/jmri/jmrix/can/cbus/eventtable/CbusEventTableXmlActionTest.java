package jmri.jmrix.can.cbus.eventtable;

import java.io.File;

import jmri.jmrix.can.CanSystemConnectionMemo;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableXmlActionTest {

    @Test
    public void testLoadXmlNoFilePresent() {

        assertEquals( 0, model.getRowCount());

        CbusEventTableXmlAction.restoreEventsFromXmlTablestart(model);
        assertEquals( 0, model.getRowCount());

    }

    @Test
    public void testLoadGoodFile() throws java.io.IOException, java.text.ParseException {

        CbusEventTableXmlFile x = new CbusEventTableXmlFile(memo);

        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/eventtable/");
        java.io.File systemFile = new java.io.File(dir, "EventTableData-1.xml");

        java.nio.file.Files.copy(systemFile.toPath(), x.getFile(true).toPath(),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        CbusEventTableXmlAction.restoreEventsFromXmlTablestart(model);

        assertEquals( 3, model.getRowCount());

        CbusTableEvent te = model.provideEvent(0,1);
        assertNotNull(te);
        assertEquals( "Short Event 1", te.getName() );
        assertEquals( "Short Event 1 Comment", te.getComment() );
        assertEquals( 0, te.getTotalOnOff(true) );
        assertEquals( 0, te.getTotalOnOff(false) );
        assertEquals( 0, te.getTotalInOut(true) );
        assertEquals( 0, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertNull( te.getDate());

        te = model.provideEvent(0,2);
        assertNotNull(te);
        assertEquals( "Short Event 2",te.getName() );
        assertEquals( "Short Event 2 Comment",te.getComment() );
        assertEquals( 123456, te.getTotalOnOff(true) );
        assertEquals( 234567, te.getTotalOnOff(false) );
        assertEquals( 345678, te.getTotalInOut(true) );
        assertEquals( 456789, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertEquals(CbusEventTableXmlAction.getXmlDateStyle().parse("2019-08-22 15:23:49"), te.getDate());

        te = model.provideEvent(65535,65535);
        assertNotNull(te);
        assertEquals( "Long Event Node 65535 Event 65535",te.getName() );
        assertTrue( te.getComment().isEmpty() );
        assertEquals( 0, te.getTotalOnOff(true) );
        assertEquals( 20, te.getTotalOnOff(false) );
        assertEquals( 30, te.getTotalInOut(true) );
        assertEquals( 40, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertNull(te.getDate());

        assertEquals(3,model.getRowCount());
    }

    @Test
    public void testLoadBadFile() throws java.io.IOException {

        CbusEventTableXmlFile x = new CbusEventTableXmlFile(memo);

        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/eventtable/");
        java.io.File systemFile = new java.io.File(dir, "EventTableData-2.xml");

        java.nio.file.Files.copy(systemFile.toPath(), x.getFile(true).toPath(),
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        CbusEventTableXmlAction.restoreEventsFromXmlTablestart(model);

        JUnitAppender.assertErrorMessageStartsWith("Node or event number missing in event [[Attribute: NodeNum");
        JUnitAppender.assertErrorMessageStartsWith("Node or event number missing in event [[Attribute: EventNum");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Unable to parse date ");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");
        JUnitAppender.assertErrorMessageStartsWith("Unable to parse date ");
        JUnitAppender.assertErrorMessageStartsWith("Incorrect value in event [[Attribute:");

        assertEquals(9,model.getRowCount());
        assertTrue(model.getEventTableRow(0,1) >=0 );
        assertTrue(model.getEventTableRow(11,22) >=0 );
        assertTrue(model.getEventTableRow(11,33) >=0 );
        assertTrue(model.getEventTableRow(11,44) >=0 );
        assertTrue(model.getEventTableRow(11,55) >=0 );
        assertTrue(model.getEventTableRow(22,22) >=0 );
        assertTrue(model.getEventTableRow(22,33) >=0 );
        assertTrue(model.getEventTableRow(22,44) >=0 );
        assertTrue(model.getEventTableRow(22,55) >=0 );

    }

    @Test
    public void testSaveFile() throws java.text.ParseException {

        assertEquals( 0, model.getRowCount());

        CbusTableEvent event1 = model.provideEvent(111,222);
        event1.setCounts(1, 2, 0, 0);
        event1.setName("My Test Event 1 Name");
        event1.setDate(CbusEventTableXmlAction.getXmlDateStyle().parse("2019-08-22 13:45:49"));

        CbusTableEvent event2 = model.provideEvent(333,444);
        event2.setCounts(0, 0, 3, 4);
        event2.setComment("My Test Event 2 Comment");

        assertEquals( 2, model.getRowCount());

        CbusEventTableXmlAction.storeEventsToXml(model);

        model.clearAllEvents();
        assertEquals( 0, model.getRowCount());

        CbusEventTableXmlAction.restoreEventsFromXmlTablestart(model);
        assertEquals( 2, model.getRowCount());

        CbusTableEvent te = model.provideEvent(111,222);
        assertNotNull(te);
        assertEquals( "My Test Event 1 Name",te.getName() );
        assertEquals( "",te.getComment() );
        assertEquals( 1, te.getTotalOnOff(true) );
        assertEquals( 2, te.getTotalOnOff(false) );
        assertEquals( 0, te.getTotalInOut(true) );
        assertEquals( 0, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertEquals(CbusEventTableXmlAction.getXmlDateStyle().parse("2019-08-22 13:45:49"), te.getDate());

        te = model.provideEvent(333,444);
        assertNotNull(te);
        assertTrue(te.getName().isEmpty() );
        assertEquals( "My Test Event 2 Comment",te.getComment() );
        assertEquals( 0, te.getTotalOnOff(true) );
        assertEquals( 0, te.getTotalOnOff(false) );
        assertEquals( 3, te.getTotalInOut(true) );
        assertEquals( 4, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertNull(te.getDate());
    }

    private CbusEventTableDataModel model;
    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp( @TempDir File tempDir ) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));

        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);

        model = new CbusEventTableDataModel( memo, 2);

    }

    @AfterEach
    public void tearDown() {

        model.skipSaveOnDispose();

        CbusEventTableShutdownTask task = new CbusEventTableShutdownTask("Test Dispose",model);
        task.run();
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;

        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableActionTest.class);

}
