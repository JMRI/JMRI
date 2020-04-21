package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintRoutesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintRoutesAction t = new PrintRoutesAction("Test Action",true);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createFiveLocationRoute();
        JUnitOperationsUtil.createThreeLocationRoute();
        PrintRoutesAction pra = new PrintRoutesAction("Test Action", true);
        Assert.assertNotNull("exists", pra);
        pra.actionPerformed(new ActionEvent("Test Action", 0, null));
        Assert.assertNotNull("exists", pra);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintRoutesActionTest.class);

}
