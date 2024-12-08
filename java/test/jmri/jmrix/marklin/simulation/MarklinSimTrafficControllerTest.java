package jmri.jmrix.marklin.simulation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MarklinSimTrafficController.
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinSimTrafficController();
    }

    @AfterEach
    @Override
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
