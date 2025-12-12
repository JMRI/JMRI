package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTest {

    @Test
    public void testCTor() {
        CbusEvent t = new CbusEvent(0,1);
        assertNotNull(t);
    }

    @Test
    public void testSetandGet() {
        CbusEvent t = new CbusEvent(123,456);
        assertEquals(123,t.getNn());
        assertEquals(456,t.getEn());
        assertEquals(CbusEvent.EvState.UNKNOWN,t.getState());
        t.setState(CbusEvent.EvState.ON);
        assertEquals(CbusEvent.EvState.ON,t.getState());
        t.setState(CbusEvent.EvState.OFF);
        assertEquals(CbusEvent.EvState.OFF,t.getState());
        t.setName("Jon Smith");
        assertEquals("Jon Smith",t.getName());
        t.setEn(4);
        t.setNn(7);
        assertEquals(7,t.getNn());
        assertEquals(4,t.getEn());
    }    

    @Test
    public void testMatches() {
        CbusEvent t = new CbusEvent(123,456);
        assertFalse(t.matches(123,111));
        assertFalse(t.matches(111,456));
        assertFalse(t.matches(111,222));
        assertTrue(t.matches(123,456));
    }

    @Test
    public void testSending() {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        memo.configureManagers();

        CbusEvent t = new CbusEvent(123,456);
        t.sendOn();
        assertEquals("[5f8] 90 00 7B 01 C8" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) ); 
        t.sendOff();
        assertEquals("[5f8] 91 00 7B 01 C8" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) );
        t.sendRequest();
        assertEquals("[5f8] 92 00 7B 01 C8" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) );
            
        CbusEvent ta = new CbusEvent(0,12345);
        ta.sendOn();
        assertEquals("[5f8] 98 00 00 30 39" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) ); 
        ta.sendOff();
        assertEquals("[5f8] 99 00 00 30 39" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) );
        ta.sendRequest();
        assertEquals("[5f8] 9A 00 00 30 39" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) );

        ta.setState(CbusEvent.EvState.ON);
        assertEquals(CbusEvent.EvState.ON,ta.getState());
        ta.sendEvent(CbusEvent.EvState.TOGGLE);
        assertEquals(CbusEvent.EvState.OFF,ta.getState());
        ta.sendEvent(CbusEvent.EvState.TOGGLE);    
        assertEquals(CbusEvent.EvState.ON,ta.getState());

        tc.terminateThreads();
        memo.dispose();
    }    

    @Test
    public void testToString() {
        CbusEvent t = new CbusEvent(0,456);
        assertEquals("EN:456 ",t.toString());
        t.setName("Jon Smith");
        assertEquals("EN:456 Jon Smith ",t.toString());
    }

    @Test
    @SuppressWarnings({"unlikely-arg-type", "IncompatibleEquals"}) // CanReply and CanMessage are CanFrame with custom equals
    public void testEquals(){
    
        CbusEvent t = new CbusEvent(123,456);
        assertTrue(t.equals(new CbusEvent(123,456)));
        assertFalse(t.equals(new CbusEvent(0,456)));
        assertFalse(t.equals(new CbusEvent(123,0)));
        assertFalse(t.equals(new CbusEvent(4,4)));
        assertFalse(t.equals("123456"));
        
    }

    @Test
    public void testHashCode(){
        int hash = new CbusEvent(123,456).hashCode();
        assertTrue(hash==new CbusEvent(123,456).hashCode());
        assertFalse(hash==new CbusEvent(0,456).hashCode());
        assertFalse(hash==new CbusEvent(123,0).hashCode());
        assertFalse(hash==new CbusEvent(4,4).hashCode());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTest.class);

}
