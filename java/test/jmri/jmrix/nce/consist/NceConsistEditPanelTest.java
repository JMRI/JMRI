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
public class NceConsistEditPanelTest {

    @Test
    public void testCTor() {
        NceConsistEditPanel t = new NceConsistEditPanel();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetHelpTarget() {
        NceConsistEditPanel t = new NceConsistEditPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.consist.NceConsistEditFrame",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        NceConsistEditPanel t = new NceConsistEditPanel();
        Assert.assertEquals("title","NCE_: Edit NCE Consist",t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        NceConsistEditPanel t = new NceConsistEditPanel();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceConsistEditPanelTest.class);

}
