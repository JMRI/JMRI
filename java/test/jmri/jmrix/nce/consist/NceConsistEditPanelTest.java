package jmri.jmrix.nce.consist;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NceConsistEditPanelTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new NceConsistEditPanel();
        helpTarget="package.jmri.jmrix.nce.consist.NceConsistEditFrame";
        title="NCE_: Edit NCE Consist";
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceConsistEditPanelTest.class);

}
