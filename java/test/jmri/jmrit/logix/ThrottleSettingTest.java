package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
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
