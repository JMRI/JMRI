package jmri.script;

import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ScriptFileChooserTest {

    @Test
    public void testCTor() {
        ScriptFileChooser t = new ScriptFileChooser();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        InstanceManager.getDefault(ShutDownManager.class).deregister(InstanceManager.getDefault(BlockManager.class).shutDownTask);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ScriptFileChooserTest.class);

}
