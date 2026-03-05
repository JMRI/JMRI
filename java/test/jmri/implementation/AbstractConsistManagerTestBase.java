package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.LocoAddress;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
abstract public class AbstractConsistManagerTestBase {

    protected jmri.ConsistManager cm = null;
    
    // implementing classes should set cm to a valid value in setUp and 
    // cleanup in tearDown.

    abstract public void setUp();

    abstract public void tearDown();


    @Test
    public void testCTor() {
        assertNotNull(cm, "exists");
    }

    @Test
    public void testConsists() {
        DccLocoAddress locoAddress_12 = new DccLocoAddress(12, false);
        DccLocoAddress locoAddress_34 = new DccLocoAddress(34, false);

        assertNotNull(cm, "AbstractConsistManager constructor return");
        // Test create new consist
        assertEquals(12, cm.getConsist(locoAddress_12).getConsistAddress().getNumber(),
                "consist address is 12");
        // Test getting an existing consist
        assertEquals(12, cm.getConsist(locoAddress_12).getConsistAddress().getNumber(),
                "consist address is 12");
        // Add another consist
        assertEquals(34, cm.getConsist(locoAddress_34).getConsistAddress().getNumber(),
                "consist address is 34");
        // Get list
        assertEquals(2, cm.getConsistList().size(), "consist list has two elements");
    }

    @Test
    public void testListener() {
        AtomicBoolean listenerHasTrigged = new AtomicBoolean(false);

        // Test notify listeners
        cm.addConsistListListener(() -> {
            listenerHasTrigged.set(true);
        });
        cm.notifyConsistListChanged();
        assertTrue(listenerHasTrigged.get(), "listener has trigged");
    }

    @Test
    public void testDecodeErrorCode() {
        // Test decodeErrorCode
        assertEquals("Not Implemented ", cm.decodeErrorCode(ConsistListener.NotImplemented));
        assertEquals("Operation Completed Successfully ", cm.decodeErrorCode(ConsistListener.OPERATION_SUCCESS));
        assertEquals("Consist Error ", cm.decodeErrorCode(ConsistListener.CONSIST_ERROR));
        assertEquals("Address not controled by this device.", cm.decodeErrorCode(ConsistListener.LOCO_NOT_OPERATED));
        assertEquals("Locomotive already consisted", cm.decodeErrorCode(ConsistListener.ALREADY_CONSISTED));
        assertEquals("Locomotive Not Consisted ", cm.decodeErrorCode(ConsistListener.NOT_CONSISTED));
        assertEquals("Speed Not Zero ", cm.decodeErrorCode(ConsistListener.NONZERO_SPEED));
        assertEquals("Address Not Conist Address ", cm.decodeErrorCode(ConsistListener.NOT_CONSIST_ADDR));
        assertEquals("Delete Error ", cm.decodeErrorCode(ConsistListener.DELETE_ERROR));
        assertEquals("Stack Full ", cm.decodeErrorCode(ConsistListener.STACK_FULL));
        assertEquals("Unknown Status Code: 61440", cm.decodeErrorCode(0xF000));
    }

    @Test
    public void testGetConsist(){
        // getConsist with a valid address should always return
        // a consist.
        DccLocoAddress addr = new DccLocoAddress(5,false);
        assertNotNull(cm.getConsist(addr), "add consist");
        assertEquals(1, cm.getConsistList().size(), "list has 1 entry");
    }

    @Test
    public void testGetConsistListEmpty(){
        // by default, there should be no consists
        assertNotNull(cm, "consist exists");
        assertNotNull(cm.getConsistList(), "list exists");
        assertTrue(cm.getConsistList().isEmpty(), "empty list");
    }

    @Test
    public void testDelConsist(){
        DccLocoAddress addr = new DccLocoAddress(5,false);
        cm.getConsist(addr);
        int size = cm.getConsistList().size();
        cm.delConsist(addr);
        assertEquals(size-1, cm.getConsistList().size(), "list has (size-1) entries");
    }

    @Test
    public void testIsCommandStationConsistPossible(){
        // default is false, override if necessary
        assertFalse(cm.isCommandStationConsistPossible(), "CS Consist Possible");
    }

    @Test
    public void testCsConsistNeedsSeperateAddress(){
        Assumptions.assumeTrue(cm.isCommandStationConsistPossible(),
            "CommandStation Consist not possible for this hardware type");
        // default is false, override if necessary
        assertFalse(cm.csConsistNeedsSeperateAddress(), "CS Consist Needs Seperate Address");
    }

    @Test
    public void testShouldRequestUpdateFromLayout(){
        Assumptions.assumeTrue(cm instanceof AbstractConsistManager,
            cm.getClass() + "is not an AbstractConsistManager");
        // default is true, override if necessary
        assertTrue(((AbstractConsistManager)cm).shouldRequestUpdateFromLayout(),
            "Should Request Update From Layout");
    }

    @Test
    public void testRequestUpdateFromLayout(){
        Assumptions.assumeTrue(cm instanceof AbstractConsistManager,
            cm.getClass() + "is not an AbstractConsistManager");
        Assumptions.assumeTrue(((AbstractConsistManager)cm).shouldRequestUpdateFromLayout(),
                "Hardware type should NOT request ipdate from layout.");
        // this test just calls the method, so checks for exceptions.
        // derived classes should override and check the expected message
        // sequence
        ((AbstractConsistManager)cm).requestUpdateFromLayout();
    }

    @Test
    public void testGetConsistLocoAddress() {
        // getConsist with a LocoAddress object typically throws an error
        // (There are no current impemenations for non-DCC systems)
        LocoAddress addr = new LocoAddress() {
            @Override
            public int getNumber() {
                return 42;
            }

            @Override
            public Protocol getProtocol() {
                return jmri.LocoAddress.Protocol.M4;
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cm.getConsist(addr));
        assertNotNull(ex);
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractConsistManagerTestBase.class);

}
