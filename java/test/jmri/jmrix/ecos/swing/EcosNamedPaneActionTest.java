package jmri.jmrix.ecos.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EcosNamedPaneActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.jmrix.ecos.EcosSystemConnectionMemo memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Ecos Named Pane Action Test");
        EcosNamedPaneAction t = new EcosNamedPaneAction("Test Action",jf,"test",memo);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosNamedPaneActionTest.class);

}
