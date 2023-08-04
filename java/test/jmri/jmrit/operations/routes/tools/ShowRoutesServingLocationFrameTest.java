package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ShowRoutesServingLocationFrameTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ShowRoutesServingLocationFrame f = new ShowRoutesServingLocationFrame();
        f.initComponents(null);
        Assert.assertNotNull("exists",f);
    }
}
