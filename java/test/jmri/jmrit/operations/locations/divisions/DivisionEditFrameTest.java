package jmri.jmrit.operations.locations.divisions;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class DivisionEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Division division = new Division("testId", "testName");
        Assert.assertNotNull("exists", division);
        DivisionEditFrame def = new DivisionEditFrame(division);
        Assert.assertNotNull("exists", def);
        JUnitUtil.dispose(def);
    }
}
