package jmri.util.datatransfer;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Description: tests for the RosterEntrySelection class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RosterEntrySelectionTest {

    @Test
    public void testCtor(){
      ArrayList<String> selection = new ArrayList<String>();
      RosterEntrySelection res = new RosterEntrySelection(selection);
      Assert.assertNotNull("RosterEntrySelection constructor",res);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}

