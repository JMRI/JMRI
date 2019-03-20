package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.TrainsTableFrame;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainsScriptActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame ttf = new TrainsTableFrame();
        TrainsScriptAction t = new TrainsScriptAction("Test Action",ttf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(ttf);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsScriptActionTest.class);

}
