package jmri.jmrit.operations.routes.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ShowRoutesServingLocationFrameTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        ShowRoutesServingLocationFrame f = new ShowRoutesServingLocationFrame();
        f.initComponents(null);
        Assert.assertNotNull("exists",f);
    }
}
