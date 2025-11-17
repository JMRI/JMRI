package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableFrame;
import jmri.util.JUnitOperationsUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class ResetEngineMovesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EnginesTableFrame enginesTableFrame = new EnginesTableFrame(true, null, null);
        ResetEngineMovesAction t = new ResetEngineMovesAction(enginesTableFrame);
        Assert.assertNotNull("exists", t);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(ResetEngineMovesActionTest.class);

}
