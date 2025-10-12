package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SectionManagerTest {

    @Test
    public void testCTor() {
        SectionManager t = new jmri.managers.DefaultSectionManager();
        assertNotNull( t, "exists");
    }

    @Test
    public void testInstanceManagerAccess() {
        SectionManager t = InstanceManager.getDefault(SectionManager.class);
        assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SectionManagerTest.class);

}
