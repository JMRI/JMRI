package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.TrainsTableFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsScriptActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame ttf = new TrainsTableFrame();
        TrainsScriptAction t = new TrainsScriptAction(ttf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(ttf);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsScriptActionTest.class);

}
