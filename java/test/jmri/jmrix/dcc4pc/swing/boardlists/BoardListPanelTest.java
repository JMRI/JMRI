package jmri.jmrix.dcc4pc.swing.boardlists;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of BoardListPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class BoardListPanelTest {

    @Test
    public void testMemoCtor() {
        BoardListPanel action = new BoardListPanel();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
