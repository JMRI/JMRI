package jmri.jmrix.grapevine;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of GrapevineMenu
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class GrapevineMenuTest {

    private GrapevineSystemConnectionMemo memo = null; 

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        GrapevineMenu action = new GrapevineMenu(memo);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SerialTrafficController tc = new SerialTrafficControlScaffold();
        memo = new GrapevineSystemConnectionMemo();
        memo.setTrafficController(tc);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
