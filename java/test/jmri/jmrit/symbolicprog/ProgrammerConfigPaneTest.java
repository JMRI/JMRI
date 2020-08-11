package jmri.jmrit.symbolicprog;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ProgrammerConfigPaneTest {

    @Test
    public void testCTor() {
        ProgrammerConfigPane t = new ProgrammerConfigPane();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
        jmri.InstanceManager.setDefault(ProgrammerConfigManager.class,new ProgrammerConfigManager());
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProgrammerConfigPaneTest.class);

}
