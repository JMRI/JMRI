package jmri.jmrit.beantable;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BeanTablePaneTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();

        panel = new BeanTablePane();
        helpTarget="package.jmri.jmrit.beantable.BeanTablePane";
    }

    @After
    @Override
    public void tearDown() {
        panel = null;
        title = null;
        helpTarget = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BeanTablePaneTest.class);

}
