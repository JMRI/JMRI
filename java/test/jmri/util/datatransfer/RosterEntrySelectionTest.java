package jmri.util.datatransfer;

import java.util.ArrayList;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test for the RosterEntrySelection class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RosterEntrySelectionTest {

    @Test
    public void testCtor(){
      ArrayList<String> selection = new ArrayList<String>();
      RosterEntrySelection res = new RosterEntrySelection(selection);
      Assertions.assertNotNull( res, "RosterEntrySelection constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

