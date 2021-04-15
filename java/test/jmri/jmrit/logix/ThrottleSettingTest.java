package jmri.jmrit.logix;

import jmri.util.JUnitUtil;
import jmri.SpeedStepMode;
import jmri.jmrit.logix.ThrottleSetting.Command;
import jmri.jmrit.logix.ThrottleSetting.ValueType;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ThrottleSettingTest {

    @Test
    public void testCTor() {
        ThrottleSetting t = new ThrottleSetting();
        assertThat(t).withFailMessage("exists").isNotNull();
    }
    
    @Test
    public void testCtor2() {
        ThrottleSetting ts = new ThrottleSetting(1000, "NoOp", "Enter Block", "OB1");
        assertThat(ts).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testCtor3() {
        ThrottleSetting ts = new ThrottleSetting(0, Command.FORWARD, 10, ValueType.VAL_FALSE, null, 0, "Fred");
        assertThat(ts).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testCtor4() {
        ThrottleSetting ts = new ThrottleSetting(0, Command.SPEED, 10, ValueType.VAL_FLOAT, null, 0, "Fred", 0);
        assertThat(ts).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testCtor5() {
        ThrottleSetting t = new ThrottleSetting(0, Command.WAIT_SENSOR, 10, ValueType.VAL_ON, null, 0, "Fred", 0);
        ThrottleSetting ts = new ThrottleSetting(t);
        assertThat(ts).withFailMessage("exists").isNotNull();
    }

    @Test
    public void setThrottleSetting() {
        ThrottleSetting ts = new ThrottleSetting();
        ts.setTime(100);
        ts.setCommand(Command.RUN_WARRANT);
        ts.setValue(ValueType.VAL_INACTIVE, SpeedStepMode.NMRA_DCC_14, 0);
        ThrottleSetting.CommandValue cmdVal = ts.getValue();
        assertThat(cmdVal).withFailMessage("exists").isNotNull();
        cmdVal.setFloat(.05f);
        ts.setTrackSpeed(300);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ThrottleSettingTest.class);

}
