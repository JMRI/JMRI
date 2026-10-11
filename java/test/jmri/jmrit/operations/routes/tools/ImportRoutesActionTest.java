package jmri.jmrit.operations.routes.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ImportRoutesActionTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        ImportRoutesAction t = new ImportRoutesAction();
        Assert.assertNotNull("exists",t);
    }
}
