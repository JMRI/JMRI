package jmri.jmrix.marklin.swing;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MarklinComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MarklinComponentFactoryTest {


    private jmri.jmrix.marklin.MarklinSystemConnectionMemo m = null;
 
    @Test
    @DisabledIfHeadless
    public void testCtor() {
        MarklinComponentFactory action = new MarklinComponentFactory(m);
        Assertions.assertNotNull(action, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.marklin.MarklinSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
