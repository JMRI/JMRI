package jmri.jmrix.roco.z21.simulator.configurexml;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Z21SimulatorClassMigrationTest {

    @Test
    public void testCTor() {
        Z21SimulatorClassMigration t = new Z21SimulatorClassMigration();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
