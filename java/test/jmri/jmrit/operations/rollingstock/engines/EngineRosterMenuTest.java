package jmri.jmrit.operations.rollingstock.engines;

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
public class EngineRosterMenuTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EnginesTableFrame etf = new EnginesTableFrame();
        EngineRosterMenu t = new EngineRosterMenu("test menu",1,etf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(etf);
    }

    // private final static Logger log = LoggerFactory.getLogger(EngineRosterMenuTest.class);

}
