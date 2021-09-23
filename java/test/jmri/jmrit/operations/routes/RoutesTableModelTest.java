package jmri.jmrit.operations.routes;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RoutesTableModelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RoutesTableModel t = new RoutesTableModel();
        Assert.assertNotNull("exists",t);
    }
}
