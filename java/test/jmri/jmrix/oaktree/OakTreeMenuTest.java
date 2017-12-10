package jmri.jmrix.oaktree;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.oaktree.SerialTrafficControlScaffold;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;
import jmri.jmrix.oaktree.SerialTrafficController;

/**
 * Test simple functioning of OakTreeMenu
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class OakTreeMenuTest {

    private SerialTrafficController tc = null;
    private OakTreeSystemConnectionMemo m = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        OakTreeMenu action = new OakTreeMenu(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
        m = new OakTreeSystemConnectionMemo();
        m.setSystemPrefix("ABC");
        m.setTrafficController(tc);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
