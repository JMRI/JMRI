package jmri.jmrix.mqtt;

import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * NceConsistTest.java
 *
 * Test for the jmri.jmrix.nce.NceConsist class
 *
 * @author Paul Bender Copyright (C) 2016,2017
 */

public class MqttConsistTest extends jmri.implementation.AbstractConsistTestBase {

    @Test
    @Override
    public void testGetConsistType(){
        // MQTT consists default to CS consists.
        Assert.assertEquals("default consist type",jmri.Consist.CS_CONSIST,c.getConsistType());
    }

    @Override
    @Test public void testSetConsistTypeAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Override
    @Test public void testSetConsistTypeCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.CS_CONSIST,c.getConsistType());
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkSizeLimitAdvanced(){
    }

    @Test public void checkSizeLimitCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("Consist Limit",-1,c.sizeLimit());
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkContainsAdvanced(){
    }
    @Test public void checkContainsCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        // nothing added, should be false for all.
        Assert.assertFalse("Advanced Consist Contains",c.contains(A));
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));
        // add just A
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));
        // then add B
        c.restore(B,false);
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkGetLocoDirectionAdvanced(){
    }

    @Test public void checkGetLocoDirectionCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        Assert.assertTrue("Direction in Advanced Consist",c.getLocoDirection(A));
        Assert.assertFalse("Direction in Advanced Consist",c.getLocoDirection(B));
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkGetSetLocoRosterIDAdvanced(){
    }

    @Test public void checkGetSetLocoRosterIDCS() throws IOException,FileNotFoundException {
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = entry.getDccLocoAddress();
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        c.setRosterId(A,"ATSF123");
        Assert.assertEquals("Roster ID A","ATSF123",c.getRosterId(A));
        Assert.assertNull("Roster ID B",c.getRosterId(B));
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkRemoveWithGetRosterIDAdvanced(){
    }

    @Test public void checkRemoveWithGetRosterIDCS() throws IOException,FileNotFoundException {
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = entry.getDccLocoAddress();
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        c.setRosterId(A,"ATSF123");
        Assert.assertEquals("Roster ID A","ATSF123",c.getRosterId(A));
        Assert.assertNull("Roster ID B",c.getRosterId(B));
        c.remove(A);
        Assert.assertFalse("Roster A is no longer in consist",c.contains(A));
    }

    @Override
    @Test
    @Disabled("Does not support Advanced consists")
    public void checkAddRemoveWithRosterUpdateAdvanced(){
    }


    @Test public void checkAddRemoveWithRosterUpdateCS() throws IOException,FileNotFoundException {
        // verify the roster update process is active.
        jmri.InstanceManager.getDefault(jmri.jmrit.consisttool.ConsistPreferencesManager.class).setUpdateCV19(true);
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = entry.getDccLocoAddress();
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        c.setRosterId(A,"ATSF123");

        Assert.assertEquals("Roster ID A","ATSF123",c.getRosterId(A));
        Assert.assertNull("Roster ID B",c.getRosterId(B));
        c.remove(A);
        Assert.assertFalse("Roster A is no longer in consist",c.contains(A));
    }


    @Test public void testActivateConsist(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        // nothing added, should be false for all.
        Assert.assertFalse("Consist Contains",c.contains(A));
        Assert.assertFalse("Consist Contains",c.contains(B));
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==1; }, "publish triggered");
        Assert.assertEquals("track/state", a.getLastTopic());
        Assert.assertEquals("", new String(a.getLastPayload()));

        // add just A
        c.add(A,true);
        Assert.assertTrue("Consist Contains",c.contains(A));
        Assert.assertFalse("Consist Contains",c.contains(B));

        // Activate consist
        ((MqttConsist)c).activate();
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==2; }, "publish triggered");
        Assert.assertEquals("cab/3/consist", a.getLastTopic());
        Assert.assertEquals("200", new String(a.getLastPayload()));

        // then add B
        c.add(B,false);
        Assert.assertTrue("Consist Contains",c.contains(A));
        Assert.assertTrue("Consist Contains",c.contains(B));

        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==3; }, "publish triggered");
        Assert.assertEquals("cab/3/consist", a.getLastTopic());
        Assert.assertEquals("200 -250", new String(a.getLastPayload()));

        c.remove(A);
        Assert.assertFalse("Consist Contains",c.contains(A));
        Assert.assertTrue("Consist Contains",c.contains(B));

        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==4; }, "publish triggered");
        Assert.assertEquals("cab/3/consist", a.getLastTopic());
        Assert.assertEquals("-250", new String(a.getLastPayload()));

         ((MqttConsist)c).deactivate();
         JUnitUtil.waitFor( ()->{ return a.getPublishCount()==5; }, "publish triggered");
        Assert.assertEquals("cab/3/consist", a.getLastTopic());
        Assert.assertEquals("", new String(a.getLastPayload()));

    }

    @Test public void checkGetLocoDirectionController(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
        c.restore(B,false); // revese direction.
        Assert.assertTrue("Direction in Advanced Consist",c.getLocoDirection(A));
        Assert.assertFalse("Direction in Advanced Consist",c.getLocoDirection(B));
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
        a = null;

        c.dispose();
        c = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
