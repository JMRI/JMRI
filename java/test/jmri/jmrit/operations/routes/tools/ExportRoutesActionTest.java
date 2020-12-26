package jmri.jmrit.operations.routes.tools;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportRoutesActionTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        ExportRoutesAction t = new ExportRoutesAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportTrainRosterActionTest.class);

}
