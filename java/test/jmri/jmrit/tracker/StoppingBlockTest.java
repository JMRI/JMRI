package jmri.jmrit.tracker;

import org.junit.jupiter.api.Test;

import jmri.Block;

import org.junit.jupiter.api.*;

/**
 * Tests for the StoppingBlock class
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class StoppingBlockTest {

    @Test
    public void testDirectCreate() {
        // check for exception in ctor
        Assertions.assertNotNull( new StoppingBlock(new Block("dummy")) );
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
