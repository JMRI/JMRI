package jmri.jmrit.beantable.routetable;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for jmri.jmrit.beantable.routtable.RouteSensor
 *
 * @author Paul Bender Copyright (C) 2020
 */
class RouteSensorTest {

    private RouteSensor rs;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUpLoggingAndCommonProperties();
        rs = new RouteSensor("IS0","Sensor");
    }

    @AfterEach
    void tearDown() {
        rs = null;
        JUnitUtil.tearDown();
    }

    @Test
    void getSysName() {
        assertThat(rs.getSysName()).isNotNull().isEqualTo("IS0");
    }

    @Test
    void getUserName() {
        assertThat(rs.getUserName()).isNotNull().isEqualTo("Sensor");
    }

    @Test
    void getDisplayName() {
        assertThat(rs.getDisplayName()).isNotNull().isEqualTo("Sensor");
    }

    @Test
    void getAndSetIncluded() {
        assertThat(rs.isIncluded()).isFalse();
        rs.setIncluded(true);
        assertThat(rs.isIncluded()).isTrue();
        rs.setIncluded(false);
        assertThat(rs.isIncluded()).isFalse();
    }

    @Test
    void getAndSetState() {
        assertThat(rs.getState()).isEqualTo(Sensor.INACTIVE);
        rs.setState(Sensor.ACTIVE);
        assertThat(rs.getState()).isEqualTo(Sensor.ACTIVE);
    }

    @Test
    void getAndSetToState() {
        assertThat(rs.getSetToState()).isEqualTo("Set Inactive");
        rs.setSetToState("Set Active");
        assertThat(rs.getSetToState()).isEqualTo("Set Active");
    }

}
