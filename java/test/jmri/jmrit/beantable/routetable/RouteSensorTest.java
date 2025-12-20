package jmri.jmrit.beantable.routetable;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for jmri.jmrit.beantable.routetable.RouteSensor
 *
 * @author Paul Bender Copyright (C) 2020
 */
class RouteSensorTest {

    private RouteSensor rs;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        rs = new RouteSensor("IS0","Sensor");
    }

    @AfterEach
    public void tearDown() {
        rs = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void getSysName() {
        assertEquals( "IS0", rs.getSysName() );
    }

    @Test
    public void getUserName() {
        assertEquals( "Sensor", rs.getUserName() );
    }

    @Test
    public void getDisplayName() {
        assertEquals( "Sensor", rs.getDisplayName() );
    }

    @Test
    public void getAndSetIncluded() {
        assertFalse( rs.isIncluded() );
        rs.setIncluded(true);
        assertTrue( rs.isIncluded() );
        rs.setIncluded(false);
        assertFalse( rs.isIncluded() );
    }

    @Test
    public void getAndSetState() {
        assertEquals( Sensor.INACTIVE, rs.getState() );
        rs.setState(Sensor.ACTIVE);
        assertEquals( Sensor.ACTIVE, rs.getState() );
    }

    @Test
    public void getAndSetToState() {
        assertEquals( "Set Inactive", rs.getSetToState() );
        rs.setSetToState("Set Active");
        assertEquals( "Set Active", rs.getSetToState() );
    }

}
