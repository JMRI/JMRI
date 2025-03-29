package jmri.jmrit.operations.setup.gui;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BuildReportOptionActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        BuildReportOptionAction t = new BuildReportOptionAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(BuildReportOptionActionTest.class);

}
