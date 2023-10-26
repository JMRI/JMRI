package jmri.jmrit.operations.routes.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ShowRoutesServingLocationActionTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        ShowRoutesServingLocationAction t = new ShowRoutesServingLocationAction(null);
        Assert.assertNotNull("exists",t);
    }
}
