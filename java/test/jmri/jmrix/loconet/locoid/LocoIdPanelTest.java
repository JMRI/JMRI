package jmri.jmrix.loconet.locoid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoIdPanelTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new LocoIdPanel();
        helpTarget = "package.jmri.jmrix.loconet.locoid.LocoIdFrame";
        title = Bundle.getMessage("MenuItemSetID");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoIdPanelTest.class);

}
