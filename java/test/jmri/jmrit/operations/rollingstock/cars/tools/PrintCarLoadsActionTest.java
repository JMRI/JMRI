package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintCarLoadsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Print Car Loads Frame");
        PrintCarLoadsAction t = new PrintCarLoadsAction(true,jf);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintCarLoadsActionTest.class);

}
