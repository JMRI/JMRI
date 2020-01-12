package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.openlcb.NodeID;

/**
 * Tests for the jmri.jmrix.openlcb.OpenLcbLocoAddress class.
 *
 * @author Bob Jacobsen Copyright 2008, 2010, 2011
 */
public class OpenLcbLocoAddressTest {

    @Test
    public void testEqualsNull() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        Assert.assertTrue(!a.equals(null));
    }

    @Test
    public void testEquals() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        Assert.assertTrue(a.equals(new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}))));
    }

    @Test
    public void testNotEqualsDifferentNode() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        Assert.assertTrue(!a.equals(new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 0, 0}))));
    }

    @SuppressWarnings("unlikely-arg-type") // OpenLcbLocoAddress seems to be unrelated to String
    @Test
    public void testEqualsWrongType() {
        OpenLcbLocoAddress a = new OpenLcbLocoAddress(new NodeID(new byte[]{1, 2, 3, 4, 5, 6}));
        Assert.assertTrue(!a.equals("foo"));
        Assert.assertTrue(!"foo".equals(a));
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
