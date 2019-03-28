package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class JmriAbstractActionTest {

    @Test
    public void testAccess() {
        JmriAbstractAction a = new JmriAbstractAction("foo", new jmri.util.swing.sdi.JmriJFrameInterface()) {

            @Override
            public jmri.util.swing.JmriPanel makePanel() {
                return null;
            }
        };

        Assert.assertEquals("foo", a.getValue(javax.swing.Action.NAME));

        javax.swing.Icon i = new javax.swing.ImageIcon("resources/icons/throttles/PowerRed24.png");
        a = new JmriAbstractAction("foo", i, null) {
            @Override
            public jmri.util.swing.JmriPanel makePanel() {
                return null;
            }
        };

        Assert.assertEquals("foo", a.getValue(javax.swing.Action.NAME));
        Assert.assertEquals(i, a.getValue(javax.swing.Action.SMALL_ICON));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
