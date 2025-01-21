package jmri.jmrit.operations.routes.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ImportRoutesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportRoutes t = new ImportRoutes();
        Assert.assertNotNull("exists", t);
    }
}
