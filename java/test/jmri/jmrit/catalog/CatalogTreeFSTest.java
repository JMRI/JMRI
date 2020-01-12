package jmri.jmrit.catalog;

import jmri.NamedBean;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the CatalogTreeFS class
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 */
public class CatalogTreeFSTest {

    // class carries its own implementation of the
    // get/set parameter code, so we test that here
    @Test
    public void testSetParameter() {
        NamedBean n = new CatalogTreeFS("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }
        };

        n.setProperty("foo", "bar");
    }

    @Test
    public void testGetParameter() {
        NamedBean n = new CatalogTreeFS("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }
        };

        n.setProperty("foo", "bar");
        Assert.assertEquals("bar", n.getProperty("foo"));
    }

    @Test
    public void testGetSetNull() {
        NamedBean n = new CatalogTreeFS("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }
        };

        n.setProperty("foo", "bar");
        Assert.assertEquals("bar", n.getProperty("foo"));
        n.setProperty("foo", null);
        Assert.assertEquals(null, n.getProperty("foo"));
    }

}
