package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ResetEngineMovesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ResetEngineMovesAction t = new ResetEngineMovesAction("Test Action");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ResetEngineMovesActionTest.class);

}
