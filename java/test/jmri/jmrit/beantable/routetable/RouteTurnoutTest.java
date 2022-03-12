package jmri.jmrit.beantable.routetable;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for jmri.jmrit.beantable.routtable.RouteTurnout
 *
 * @author Paul Bender Copyright (C) 2020
 */
class RouteTurnoutTest {

    private RouteTurnout rt;

    @BeforeEach
    void setUp(){
        JUnitUtil.setUpLoggingAndCommonProperties();
        rt = new RouteTurnout("IT1","Turnout");
    }

    @AfterEach
    void tearDown(){
        rt = null;
        JUnitUtil.tearDown();
    }

    @Test
    void getSysName() {
        assertThat(rt.getSysName()).isEqualTo("IT1");
    }

    @Test
    void getUserName() {
        assertThat(rt.getUserName()).isEqualTo("Turnout");
    }

    @Test
    void getDisplayName() {
        assertThat(rt.getDisplayName()).isEqualTo("Turnout");
    }

    @Test
    void getAndSetIncluded() {
        assertThat(rt.isIncluded()).isFalse();
        rt.setIncluded(true);
        assertThat(rt.isIncluded()).isTrue();
        rt.setIncluded(false);
        assertThat(rt.isIncluded()).isFalse();
    }

    @Test
    void getAndSetState() {
        assertThat(rt.getState()).isEqualTo(Turnout.THROWN);
        rt.setState(Turnout.CLOSED);
        assertThat(rt.getState()).isEqualTo(Turnout.CLOSED);
    }

    @Test
    void getAndSetSetToState() {
        assertThat(rt.getSetToState()).isEqualTo("Set Thrown");
        rt.setSetToState("Set Closed");
        assertThat(rt.getSetToState()).isEqualTo("Set Closed");
        rt.setSetToState("Set Toggle");
        assertThat(rt.getSetToState()).isEqualTo("Set Toggle");
    }

}
