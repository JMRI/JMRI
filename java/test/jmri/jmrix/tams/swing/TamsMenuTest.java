package jmri.jmrix.tams.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of TamsMenu
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TamsMenuTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        // the constructor looks for the default ListedTableFrame class, 
        // which is set by the ListedTableFrame constructor.
        new jmri.jmrit.beantable.ListedTableFrame();
        TamsMenu action = new TamsMenu(new jmri.jmrix.tams.TamsSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
