package jmri.jmrit.beantable;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BeanTablePaneTest extends jmri.util.swing.JmriPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();

        panel = new BeanTablePane();
        helpTarget="package.jmri.jmrit.beantable.BeanTablePane";
    }

    @AfterEach
    @Override
    public void tearDown() {
        panel = null;
        title = null;
        helpTarget = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BeanTablePaneTest.class);

}
