package jmri.jmrit.tracker;

import org.junit.Test;

import jmri.Block;

/**
 * Tests for the StoppingBlock class
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class StoppingBlockTest {

    @Test
    public void testDirectCreate() {
        // check for exception in ctor
        new StoppingBlock(new Block("dummy"));
    }
}
