package jmri.jmrix.can.cbus.eventtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableXmlActionTest {
    
    @Test
    public void testLoadXmlNoFilePresent() {
        
        assertThat(model.getRowCount()).isEqualTo(0);
        
        CbusEventTableXmlAction.restoreEventsFromXmlTablestart(model);
        assertThat(model.getRowCount()).isEqualTo(0);
        
    }
    
    @Test
    public void testLoadGoodFile() throws java.io.IOException, java.text.ParseException {
        
        CbusEventTableXmlFile x = new CbusEventTableXmlFile();

        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/eventtable/");
        java.io.File systemFile = new java.io.File(dir, "EventTableData-1.xml");

        java.nio.file.Files.copy(systemFile.toPath(), x.getFile(true).toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
        CbusEventTableXmlAction.restoreEventsFromXmlTablestart(model);
        
        assertThat(model.getRowCount()).isEqualTo(3);
        
        CbusTableEvent te = model.provideEvent(0,1);
        assertThat(te).isNotNull();
        assertThat(te.getName() ).isEqualTo("Short Event 1");
        assertThat(te.getComment() ).isEqualTo("Short Event 1 Comment");
        assertThat(te.getTotalOnOff(true) ).isEqualTo(0);
        assertThat(te.getTotalOnOff(false) ).isEqualTo(0);
        assertThat(te.getTotalInOut(true) ).isEqualTo(0);
        assertThat(te.getTotalInOut(false) ).isEqualTo(0);
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertThat(te.getDate()).isNull();
        
        te = model.provideEvent(0,2);
        assertThat(te).isNotNull();
        assertEquals( "Short Event 2",te.getName() );
        assertEquals( "Short Event 2 Comment",te.getComment() );
        assertEquals( 123456, te.getTotalOnOff(true) );
        assertEquals( 234567, te.getTotalOnOff(false) );
        assertEquals( 345678, te.getTotalInOut(true) );
        assertEquals( 456789, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertEquals(CbusEventTableXmlAction.getXmlDateStyle().parse("2019-08-22 15:23:49"), te.getDate());
        
        te = model.provideEvent(65535,65535);
        assertThat(te).isNotNull();
        assertEquals( "Long Event Node 65535 Event 65535",te.getName() );
        assertThat( te.getComment() ).isEmpty();
        assertEquals( 0, te.getTotalOnOff(true) );
        assertEquals( 20, te.getTotalOnOff(false) );
        assertEquals( 30, te.getTotalInOut(true) );
        assertEquals( 40, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertThat(te.getDate()).isNull();
 
        assertEquals(3,model.getRowCount());
    }
    
    @Test
    public void testLoadBadFile() throws java.io.IOException {
        
        CbusEventTableXmlFile x = new CbusEventTableXmlFile();

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
        assertThat(model.getEventTableRow(0,1)).isGreaterThanOrEqualTo(0);
        assertThat(model.getEventTableRow(11,22)).isGreaterThanOrEqualTo(0);
        assertThat(model.getEventTableRow(11,33)).isGreaterThanOrEqualTo(0);
        assertThat(model.getEventTableRow(11,44)).isGreaterThanOrEqualTo(0);
        assertThat(model.getEventTableRow(11,55)).isGreaterThanOrEqualTo(0);
        assertThat(model.getEventTableRow(22,22)).isGreaterThanOrEqualTo(0);
        assertThat(model.getEventTableRow(22,33)).isGreaterThanOrEqualTo(0);
        assertThat(model.getEventTableRow(22,44)).isGreaterThanOrEqualTo(0);
        assertThat(model.getEventTableRow(22,55)).isGreaterThanOrEqualTo(0);
        
    }
    
    @Test
    public void testSaveFile() throws java.text.ParseException {
        
        assertThat(model.getRowCount()).isEqualTo(0);
        
        CbusTableEvent event1 = model.provideEvent(111,222);
        event1.setCounts(1, 2, 0, 0);
        event1.setName("My Test Event 1 Name");
        event1.setDate(CbusEventTableXmlAction.getXmlDateStyle().parse("2019-08-22 13:45:49"));
        
        CbusTableEvent event2 = model.provideEvent(333,444);
        event2.setCounts(0, 0, 3, 4);
        event2.setComment("My Test Event 2 Comment");
        
        assertThat(model.getRowCount()).isEqualTo(2);
        
        CbusEventTableXmlAction.storeEventsToXml(model);
        
        model.clearAllEvents();
        assertThat(model.getRowCount()).isEqualTo(0);
        
        CbusEventTableXmlAction.restoreEventsFromXmlTablestart(model);
        assertThat(model.getRowCount()).isEqualTo(2);
        
        CbusTableEvent te = model.provideEvent(111,222);
        assertThat(te).isNotNull();
        assertEquals( "My Test Event 1 Name",te.getName() );
        assertEquals( "",te.getComment() );
        assertEquals( 1, te.getTotalOnOff(true) );
        assertEquals( 2, te.getTotalOnOff(false) );
        assertEquals( 0, te.getTotalInOut(true) );
        assertEquals( 0, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertEquals(CbusEventTableXmlAction.getXmlDateStyle().parse("2019-08-22 13:45:49"), te.getDate());
        
        te = model.provideEvent(333,444);
        assertThat(te).isNotNull();
        assertThat(te.getName() ).isEmpty();
        assertEquals( "My Test Event 2 Comment",te.getComment() );
        assertEquals( 0, te.getTotalOnOff(true) );
        assertEquals( 0, te.getTotalOnOff(false) );
        assertEquals( 3, te.getTotalInOut(true) );
        assertEquals( 4, te.getTotalInOut(false) );
        assertEquals(CbusTableEvent.EvState.UNKNOWN,te.getState());
        assertThat(te.getDate()).isNull();
    }
    
    
    private CbusEventTableDataModel model;
    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;
    
    @TempDir 
    protected Path tempDir;

    @BeforeEach
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        jmri.InstanceManager.store(new CbusPreferences(),CbusPreferences.class );
        model = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
      
    }

    @AfterEach
    public void tearDown() {
        
        model.skipSaveOnDispose();
        
        CbusEventTableShutdownTask task = new CbusEventTableShutdownTask("Test Dispose",model);
        task.run();
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableActionTest.class);

}
