package jmri.jmrit.operations.routes.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class RouteBlockingOrderEditTableModelTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        RouteBlockingOrderEditTableModel t = new RouteBlockingOrderEditTableModel();
        Assert.assertNotNull("exists", t);
    }
}
