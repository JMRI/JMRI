package jmri.jmrit.operations.setup;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OperationsSetupPanelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        OperationsSetupPanel t = new OperationsSetupPanel();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(OperationsSetupPanelTest.class);

}
