package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TransitCreationTool
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TransitCreationToolTest {

    @Test
    public void testCtor() {
        TransitCreationTool t = new TransitCreationTool();
        assertNotNull( t, "exists");
    }

    @Test
    public void testGetBeans() {
        TransitCreationTool t = new TransitCreationTool();
        // getBeans should return an empty list.
        List<NamedBean> list = t.getBeans();
        assertNotNull( list, "list exists");
        assertTrue( list.isEmpty(), "list empty");
    }

    @Test
    public void testToolInUseAtStart() {
        TransitCreationTool t = new TransitCreationTool();
        // tool should not be in use.
        assertFalse( t.isToolInUse(), "tool in use at start");
    }

    @Test
    public void testInUseAfterAdd() throws JmriException {
        TransitCreationTool t = new TransitCreationTool();
        // add a new named bean to the list.
        
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

        // tool should be in use.
        assertTrue( t.isToolInUse(), "tool in use after add");
    }

    @Test
    public void testAddandCancel() throws JmriException {
        TransitCreationTool t = new TransitCreationTool();
        // add a new named bean to the list.

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

        // tool should be in use.
        assertTrue( t.isToolInUse(), "tool in use after add");
        // clear the list
        t.cancelTransitCreate();
        // tool should no longer be in use.
        assertFalse( t.isToolInUse(), "tool in use after cancel");

    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
