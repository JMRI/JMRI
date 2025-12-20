package jmri.jmrit.beantable.routetable;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for jmri.jmrit.beantable.routtable.RouteTurnout
 *
 * @author Paul Bender Copyright (C) 2020
 */
class RouteTurnoutTest {

    private RouteTurnout rt;

    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
        rt = new RouteTurnout("IT1","Turnout");
    }

    @AfterEach
    public void tearDown(){
        rt = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void getSysName() {
        assertEquals( "IT1", rt.getSysName() );
    }

    @Test
    public void getUserName() {
        assertEquals( "Turnout", rt.getUserName() );
    }

    @Test
    public void getDisplayName() {
        assertEquals( "Turnout", rt.getDisplayName() );
    }

    @Test
    public void getAndSetIncluded() {
        assertFalse( rt.isIncluded() );
        rt.setIncluded(true);
        assertTrue( rt.isIncluded() );
        rt.setIncluded(false);
        assertFalse( rt.isIncluded() );
    }

    @Test
    public void getAndSetState() {
        assertEquals( Turnout.THROWN, rt.getState() );
        rt.setState(Turnout.CLOSED);
        assertEquals( Turnout.CLOSED, rt.getState() );
    }

    @Test
    public void getAndSetSetToState() {
        assertEquals( "Set Thrown", rt.getSetToState() );
        rt.setSetToState("Set Closed");
        assertEquals( "Set Closed", rt.getSetToState() );
        rt.setSetToState("Set Toggle");
        assertEquals( "Set Toggle", rt.getSetToState() );
    }

}
