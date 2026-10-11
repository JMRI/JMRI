package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Bob Jacobsen Copyright 2010
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

        assertEquals("foo", a.getValue(javax.swing.Action.NAME));

        javax.swing.Icon i = new javax.swing.ImageIcon("resources/icons/throttles/power_red.png");
        Assertions.assertTrue(i.getIconHeight() > 0, "an Icon exists" );
        a = new JmriAbstractAction("foo", i, null) {
            @Override
            public jmri.util.swing.JmriPanel makePanel() {
                return null;
            }
        };

        assertEquals("foo", a.getValue(javax.swing.Action.NAME));
        assertEquals(i, a.getValue(javax.swing.Action.SMALL_ICON));
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
