package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ResetCarMovesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Reset Car Moves Frame");
        ResetCarMovesAction t = new ResetCarMovesAction("Test Action",jf);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ResetCarMovesActionTest.class);

}
