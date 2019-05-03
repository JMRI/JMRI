package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.EnginesTableFrame;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NceConsistEngineActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EnginesTableFrame etf = new EnginesTableFrame();
        NceConsistEngineAction t = new NceConsistEngineAction("Test Action",etf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(etf);
    }

    // private final static Logger log = LoggerFactory.getLogger(NceConsistEngineActionTest.class);

}
