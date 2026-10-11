package jmri.jmrit.operations.setup.gui;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OperationsSettingsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        OperationsSettingsAction t = new OperationsSettingsAction();
        Assert.assertNotNull("exists",t);
    }

    // private static final Logger log = LoggerFactory.getLogger(OperationsSetupActionTest.class);

}
