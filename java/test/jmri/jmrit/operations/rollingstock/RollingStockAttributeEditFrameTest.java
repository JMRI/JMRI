package jmri.jmrit.operations.rollingstock;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RollingStockAttributeEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RollingStockAttributeEditFrame t = new RollingStockAttributeEditFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }
}
