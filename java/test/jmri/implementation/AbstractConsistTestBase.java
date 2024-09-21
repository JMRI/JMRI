package jmri.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.util.RosterTestUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of Consist classes.
 *
 * @author Paul Copyright (C) 2017
 */
abstract public class AbstractConsistTestBase {

    protected Consist c = null;

    abstract public void setUp();  // should set the consist under test to c.

    abstract public void tearDown(); // should clean up the consist c.

    @Test public void testCtor() {
        assertNotNull(c);
    }

    @Test
    public void checkDisposeMethod(){
        DccLocoAddress addressA = new DccLocoAddress(200,true);
        DccLocoAddress addressB = new DccLocoAddress(250,true);
        c.restore(addressA,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(addressB,false); // use restore here, as it does not send
                           // any data to the command station
        // before dispose, this should succeed.
        assertTrue(c.contains(addressA), "A Advanced Consist Contains");
        assertTrue(c.contains(addressB), "B Advanced Consist Contains");
        c.dispose();
        // after dispose, this should fail
        Exception ex = Assertions.assertThrows( NullPointerException.class,
            () -> c.contains(addressA), "A Advanced Consist Throws");
        assertNotNull(ex);
        ex = Assertions.assertThrows( NullPointerException.class,
            () -> c.contains(addressB), "B Advanced Consist Throws");
        assertNotNull(ex);
    }

    @Test
    public void testGetConsistType(){
        assertEquals(Consist.ADVANCED_CONSIST,c.getConsistType(), "default consist type");
    }

    @Test
    public void testSetConsistTypeAdvanced(){
        c.setConsistType(Consist.ADVANCED_CONSIST);
        assertEquals(Consist.ADVANCED_CONSIST,c.getConsistType(), "default consist type");
    }

    @Test
    public void testSetConsistTypeCS(){
        c.setConsistType(Consist.CS_CONSIST);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Test
    public void checkAddressAllowedGood(){
        assertTrue( c.isAddressAllowed(new DccLocoAddress(200,true)),"AddressAllowed");
    }

    @Test
    public void checkAddressAllowedBad(){
        assertFalse( c.isAddressAllowed(new DccLocoAddress(0,false)), "AddressAllowed");
    }

    @Test
    public void checkSizeLimitAdvanced(){
        c.setConsistType(Consist.ADVANCED_CONSIST);
        assertEquals(-1,c.sizeLimit(), "Advanced Consist Limit");
    }

    @Test
    public void checkContainsAdvanced(){
        c.setConsistType(Consist.ADVANCED_CONSIST);
        DccLocoAddress addressA = new DccLocoAddress(200,true);
        DccLocoAddress addressB = new DccLocoAddress(250,true);
        // nothing added, should be false for all.
        assertFalse( c.contains(addressA), "Advanced Consist Contains A");
        assertFalse( c.contains(addressB), "Advanced Consist Contains B");
        // add just addressA
        c.restore(addressA,true); // use restore here, as it does not send
                           // any data to the command station
        assertTrue( c.contains(addressA), "Advanced Consist Contains A");
        assertFalse( c.contains(addressB), "Advanced Consist Contains B");
        // then add addressB
        c.restore(addressB,false);
        assertTrue( c.contains(addressA), "Advanced Consist Contains A");
        assertTrue( c.contains(addressB), "Advanced Consist Contains B");
    }

    @Test
    public void checkGetLocoDirectionAdvanced(){
        c.setConsistType(Consist.ADVANCED_CONSIST);
        DccLocoAddress addressA = new DccLocoAddress(200,true);
        DccLocoAddress addressB = new DccLocoAddress(250,true);
        c.restore(addressA,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(addressB,false); // revese direction.
        assertTrue( c.getLocoDirection(addressA), "Direction in Advanced Consist A");
        assertFalse( c.getLocoDirection(addressB), "Direction in Advanced Consist B");
    }

    @Test
    public void checkGetSetLocoRosterIDAdvanced() throws IOException,FileNotFoundException {
        RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        assertNotNull(entry);
        c.setConsistType(Consist.ADVANCED_CONSIST);
        DccLocoAddress addressA = entry.getDccLocoAddress();
        DccLocoAddress addressB = new DccLocoAddress(250,true);
        c.restore(addressA,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(addressB,false); // revese direction.
        c.setRosterId(addressA,"ATSF123");
        assertEquals( "ATSF123",c.getRosterId(addressA), "Roster ID A");
        assertNull( c.getRosterId(addressB), "Roster ID B");
    }

    @Test
    public void checkRemoveWithGetRosterIDAdvanced() throws IOException,FileNotFoundException {
        RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        assertNotNull(entry);
        c.setConsistType(Consist.ADVANCED_CONSIST);
        DccLocoAddress addressA = entry.getDccLocoAddress();
        DccLocoAddress addressB = new DccLocoAddress(250,true);
        c.restore(addressA,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(addressB,false); // revese direction.
        c.setRosterId(addressA,"ATSF123");
        assertEquals( "ATSF123",c.getRosterId(addressA), "Roster ID A");
        assertNull( c.getRosterId(addressB), "Roster ID B");
        c.remove(addressA);
        assertFalse( c.contains(addressA), "Roster A is no longer in consist");
    }

    @Test
    public void checkAddRemoveWithRosterUpdateAdvanced() throws IOException,FileNotFoundException {
        // verify the roster update process is active.
        jmri.InstanceManager.getDefault(jmri.jmrit.consisttool.ConsistPreferencesManager.class).setUpdateCV19(true);
        RosterTestUtil.createTestRoster(new File(Roster.getDefault().getRosterLocation()),"rosterTest.xml");
        RosterEntry entry = Roster.getDefault().getEntryForId("ATSF123");
        assertNotNull(entry);
        c.setConsistType(Consist.ADVANCED_CONSIST);
        DccLocoAddress addressA = entry.getDccLocoAddress();
        DccLocoAddress addressB = new DccLocoAddress(250,true);
        c.restore(addressA,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(addressB,false); // revese direction.
        c.setRosterId(addressA,"ATSF123");

        // verify that roster ATSF123 has CV19 set to the consist address
        CvTableModel  cvTable = new CvTableModel(null, null);  // will hold CV objects
        VariableTableModel varTable = new VariableTableModel(null,new String[]{"Name","Value"},cvTable);
        entry.readFile();  // read, but don’t yet process

        // load from decoder file
        RosterTestUtil.loadDecoderFromLoco(entry,varTable);

        entry.loadCvModel(varTable, cvTable);
        CvValue cv19Value = cvTable.getCvByNumber("19");
        assertEquals( c.getConsistAddress().getNumber(),cv19Value.getValue(), "CV19 value after add");

        assertEquals( "ATSF123",c.getRosterId(addressA), "Roster ID A");
        assertNull( c.getRosterId(addressB), "Roster ID B");
        c.remove(addressA);
        assertFalse( c.contains(addressA), "Roster A is no longer in consist");

        cvTable = new CvTableModel(null, null);  // will hold CV objects
        varTable = new VariableTableModel(null,new String[]{"Name","Value"},cvTable);
        entry.readFile();  // read, but don’t yet process

        // load from decoder file
        RosterTestUtil.loadDecoderFromLoco(entry,varTable);

        entry.loadCvModel(varTable, cvTable);
        cv19Value = cvTable.getCvByNumber("19");
        assertEquals( 0, cv19Value.getValue(), "CV19 value after remove");
    }

}
