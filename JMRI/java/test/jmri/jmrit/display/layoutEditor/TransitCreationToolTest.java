package jmri.jmrit.display.layoutEditor;

import java.util.List;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of TransitCreationTool
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TransitCreationToolTest {

    @Test
    public void testCtor() {
        TransitCreationTool t = new TransitCreationTool();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testGetBeans() {
        TransitCreationTool t = new TransitCreationTool();
        // getBeans should return an empty list.
        List<NamedBean> list = t.getBeans();
        Assert.assertNotNull("list exists", list);
        Assert.assertTrue("list empty", list.isEmpty());
    }

    @Test
    public void testToolInUseAtStart() {
        TransitCreationTool t = new TransitCreationTool();
        // tool should not be in use.
        Assert.assertFalse("tool in use at start", t.isToolInUse());
    }

    @Test
    public void testInUseAfterAdd() {
        TransitCreationTool t = new TransitCreationTool();
        // add a new named bean to the list.
        try {
            t.addNamedBean(new jmri.implementation.AbstractNamedBean("sys", "usr") {
                @Override
                public int getState() {
                    return 0;
                }

                @Override
                public void setState(int i) {
                }

                @Override
                public String getBeanType() {
                    return "";
                }
            });
        } catch (JmriException je) {
            Assert.fail("Unable to add new named bean");
        }

        // tool should be in use.
        Assert.assertTrue("tool in use after add", t.isToolInUse());
    }

    @Test
    public void testAddandCancel() {
        TransitCreationTool t = new TransitCreationTool();
        // add a new named bean to the list.
        try {
            t.addNamedBean(new jmri.implementation.AbstractNamedBean("sys", "usr") {
                @Override
                public int getState() {
                    return 0;
                }

                @Override
                public void setState(int i) {
                }

                @Override
                public String getBeanType() {
                    return "";
                }
            });
        } catch (JmriException je) {
            Assert.fail("Unable to add new named bean");
        }

        // tool should be in use.
        Assert.assertTrue("tool in use after add", t.isToolInUse());
        // clear the list
        t.cancelTransitCreate();
        // tool should no longer be in use.
        Assert.assertFalse("tool in use after cancel", t.isToolInUse());

    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
