package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.Route;
import jmri.util.JUnitOperationsUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintRouteActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = new Route("Test Route", "Test ID");
        PrintRouteAction t = new PrintRouteAction("Test Action", true, r);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = JUnitOperationsUtil.createThreeLocationTurnRoute();
        PrintRouteAction pra = new PrintRouteAction("Test Action", true, r);
        Assert.assertNotNull("exists", pra);
        pra.actionPerformed(new ActionEvent("Test Action", 0, null));
    }
}
