package jmri.jmrit.operations.automation;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutomationResetActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AutomationTableFrame f = new AutomationTableFrame(null);
        AutomationResetAction t = new AutomationResetAction(f);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(f);
    }
    
    // private final static Logger log = LoggerFactory.getLogger(AutomationResetActionTest.class);
}
