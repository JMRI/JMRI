package jmri.implementation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SignalMastRepeaterTest {

    @Test
    public void testCTor() {
        VirtualSignalMast s1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        VirtualSignalMast s2 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        SignalMastRepeater t = new SignalMastRepeater(s1,s2);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(SignalMastRepeaterTest.class);

}
