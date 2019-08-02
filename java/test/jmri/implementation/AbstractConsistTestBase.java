package jmri.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import jmri.Consist;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.VariableTableModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of Consist classes.
 *
 * @author	Paul Copyright (C) 2017
 */
abstract public class AbstractConsistTestBase {

    protected Consist c = null;
    @Before
    abstract public void setUp();  // should set the consist under test to c.
    @After
    abstract public void tearDown(); // should clean up the consist c.

    @Test public void testCtor() {
        Assert.assertNotNull(c);
    }

    @Test(expected=java.lang.NullPointerException.class)
    public void checkDisposeMethod(){
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // use restore here, as it does not send
                           // any data to the command station
        // before dispose, this should succeed.
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
        c.dispose();
        // after dispose, this should fail
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
    }

    @Test public void testGetConsistType(){
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Test public void checkAddressAllowedGood(){
        Assert.assertTrue("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(200,true)));
    }

    @Test public void checkAddressAllowedBad(){
        Assert.assertFalse("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(0,false)));
    }

    @Test public void checkSizeLimitAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("Advanced Consist Limit",-1,c.sizeLimit());
    }

    @Test public void checkContainsAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
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

    @Test public void checkGetLocoDirectionAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        Assert.assertTrue("Direction in Advanced Consist",c.getLocoDirection(A));
        Assert.assertFalse("Direction in Advanced Consist",c.getLocoDirection(B));
    }

    @Test public void checkGetSetLocoRosterIDAdvanced() throws IOException,FileNotFoundException {
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = entry.getDccLocoAddress();
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        c.setRosterId(A,"ATSF123");
        Assert.assertEquals("Roster ID A","ATSF123",c.getRosterId(A));
        Assert.assertNull("Roster ID B",c.getRosterId(B));
    }

    @Test public void checkRemoveWithGetRosterIDAdvanced() throws IOException,FileNotFoundException {
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
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

    @Test public void checkAddRemoveWithRosterUpdateAdvanced() throws IOException,FileNotFoundException {
        // verify the roster update process is active.
        jmri.InstanceManager.getDefault(jmri.jmrit.consisttool.ConsistPreferencesManager.class).setUpdateCV19(true);
        jmri.util.RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = entry.getDccLocoAddress();
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        c.setRosterId(A,"ATSF123");

        // verify that roster ATSF123 has CV19 set to the consist address
        CvTableModel  cvTable = new CvTableModel(null, null);  // will hold CV objects
        VariableTableModel varTable = new VariableTableModel(null,new String[]{"Name","Value"},cvTable);
        entry.readFile();  // read, but don’t yet process

        // load from decoder file
        jmri.util.RosterTestUtil.loadDecoderFromLoco(entry,varTable);

        entry.loadCvModel(varTable, cvTable);
        CvValue cv19Value = cvTable.getCvByNumber("19");
        Assert.assertEquals("CV19 value after add",c.getConsistAddress().getNumber(),cv19Value.getValue());

        Assert.assertEquals("Roster ID A","ATSF123",c.getRosterId(A));
        Assert.assertNull("Roster ID B",c.getRosterId(B));
        c.remove(A);
        Assert.assertFalse("Roster A is no longer in consist",c.contains(A));

        cvTable = new CvTableModel(null, null);  // will hold CV objects
        varTable = new VariableTableModel(null,new String[]{"Name","Value"},cvTable);
        entry.readFile();  // read, but don’t yet process

        // load from decoder file
        jmri.util.RosterTestUtil.loadDecoderFromLoco(entry,varTable);

        entry.loadCvModel(varTable, cvTable);
        cv19Value = cvTable.getCvByNumber("19");
        Assert.assertEquals("CV19 value after remove",0,cv19Value.getValue());
    }


    // The minimal setup for log4J

}
