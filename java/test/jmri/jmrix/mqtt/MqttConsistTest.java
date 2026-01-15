package jmri.jmrix.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * MqttConsistTest.java
 *
 * Test for the jmri.jmrix.mqtt.MqttConsist class
 *
 * @author Paul Bender Copyright (C) 2016,2017
 */
public class MqttConsistTest extends jmri.implementation.AbstractConsistTestBase {

    @Test
    @Override
    public void testGetConsistType(){
        // MQTT consists default to CS consists.
        assertEquals( Consist.CS_CONSIST,c.getConsistType(), "default consist type");
    }

    @Override
    @Test
    public void testSetConsistTypeAdvanced(){
        c.setConsistType( Consist.ADVANCED_CONSIST);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Override
    @Test
    public void testSetConsistTypeCS(){
        c.setConsistType( Consist.CS_CONSIST);
        assertEquals( Consist.CS_CONSIST,c.getConsistType(), "default consist type");
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkSizeLimitAdvanced(){
    }

    @Test
    public void checkSizeLimitCS(){
        c.setConsistType( Consist.CS_CONSIST);
        assertEquals( -1, c.sizeLimit(), "Consist Limit");
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkContainsAdvanced(){
    }

    @Test
    public void checkContainsCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        DccLocoAddress A = new DccLocoAddress(200,true);
        DccLocoAddress B = new DccLocoAddress(250,true);
        // nothing added, should be false for all.
        assertFalse( c.contains(A), "Advanced Consist Contains");
        assertFalse( c.contains(B), "Advanced Consist Contains");
        // add just A
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        assertTrue( c.contains(A), "Advanced Consist Contains");
        assertFalse( c.contains(B), "Advanced Consist Contains");
        // then add B
        c.restore(B,false);
        assertTrue( c.contains(A), "Advanced Consist Contains");
        assertTrue( c.contains(B), "Advanced Consist Contains");
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkGetLocoDirectionAdvanced(){
    }

    @Test
    public void checkGetLocoDirectionCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        DccLocoAddress A = new DccLocoAddress(200,true);
        DccLocoAddress B = new DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        assertTrue( c.getLocoDirection(A), "Direction in Advanced Consist");
        assertFalse( c.getLocoDirection(B), "Direction in Advanced Consist");
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkGetSetLocoRosterIDAdvanced(){
    }

    @Test
    public void checkGetSetLocoRosterIDCS() throws IOException,FileNotFoundException {
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        assertNotNull(entry);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        DccLocoAddress A = entry.getDccLocoAddress();
        DccLocoAddress B = new DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        c.setRosterId(A,"ATSF123");
        assertEquals( "ATSF123", c.getRosterId(A), "Roster ID A");
        assertNull( c.getRosterId(B), "Roster ID B");
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkRemoveWithGetRosterIDAdvanced(){
    }

    @Test
    public void checkRemoveWithGetRosterIDCS() throws IOException,FileNotFoundException {
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        assertNotNull(entry);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        DccLocoAddress A = entry.getDccLocoAddress();
        DccLocoAddress B = new DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        c.setRosterId(A,"ATSF123");
        assertEquals( "ATSF123", c.getRosterId(A), "Roster ID A");
        assertNull( c.getRosterId(B), "Roster ID B");
        c.remove(A);
        assertFalse( c.contains(A), "Roster A is no longer in consist");
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkAddRemoveWithRosterUpdateAdvanced(){
    }


    @Test
    public void checkAddRemoveWithRosterUpdateCS() throws IOException,FileNotFoundException {
        // verify the roster update process is active.
        jmri.InstanceManager.getDefault(jmri.jmrit.consisttool.ConsistPreferencesManager.class).setUpdateCV19(true);
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        assertNotNull(entry);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        DccLocoAddress A = entry.getDccLocoAddress();
        DccLocoAddress B = new DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        c.setRosterId(A,"ATSF123");

        assertEquals( "ATSF123", c.getRosterId(A), "Roster ID A");
        assertNull( c.getRosterId(B), "Roster ID B");
        c.remove(A);
        assertFalse( c.contains(A), "Roster A is no longer in consist");
    }


    @Test
    public void testActivateConsist(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        DccLocoAddress A = new DccLocoAddress(200,true);
        DccLocoAddress B = new DccLocoAddress(250,true);
        // nothing added, should be false for all.
        assertFalse( c.contains(A), "Consist Contains");
        assertFalse( c.contains(B), "Consist Contains");
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==1; }, "publish triggered");
        assertEquals("track/state", a.getLastTopic());
        assertEquals("", new String(a.getLastPayload()));

        // add just A
        c.add(A,true);
        assertTrue( c.contains(A), "Consist Contains");
        assertFalse( c.contains(B), "Consist Contains");

        // Activate consist
        ((MqttConsist)c).activate();
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==2; }, "publish triggered");
        assertEquals("cab/3/consist", a.getLastTopic());
        assertEquals("200", new String(a.getLastPayload()));

        // then add B
        c.add(B,false);
        assertTrue( c.contains(A), "Consist Contains");
        assertTrue( c.contains(B), "Consist Contains");

        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==3; }, "publish triggered");
        assertEquals("cab/3/consist", a.getLastTopic());
        assertEquals("200 -250", new String(a.getLastPayload()));

        c.remove(A);
        assertFalse( c.contains(A), "Consist Contains");
        assertTrue( c.contains(B), "Consist Contains");

        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==4; }, "publish triggered");
        assertEquals("cab/3/consist", a.getLastTopic());
        assertEquals("-250", new String(a.getLastPayload()));

         ((MqttConsist)c).deactivate();
         JUnitUtil.waitFor( ()->{ return a.getPublishCount()==5; }, "publish triggered");
        assertEquals("cab/3/consist", a.getLastTopic());
        assertEquals("", new String(a.getLastPayload()));

    }

    @Test
    public void checkGetLocoDirectionController(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        DccLocoAddress A = new DccLocoAddress(200,true);
        DccLocoAddress B = new DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
        c.restore(B,false); // revese direction.
        assertTrue( c.getLocoDirection(A), "Direction in Advanced Consist");
        assertFalse( c.getLocoDirection(B), "Direction in Advanced Consist");
    }


    private MqttAdapterScaffold a;
    private MqttSystemConnectionMemo memo;


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class,new ConsistPreferencesManager());
        // prepare an interface
        a = new MqttAdapterScaffold(true);
        memo = new MqttSystemConnectionMemo();
        memo.setMqttAdapter(a);

        c = new MqttConsist(3, memo, "cab/{0}/consist");
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.dispose();
        memo = null;
        a.dispose();
        a = null;

        c.dispose();
        c = null;
        JUnitUtil.tearDown();
    }

}
