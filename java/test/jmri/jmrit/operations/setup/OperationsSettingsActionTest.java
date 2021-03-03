package jmri.jmrit.operations.setup;

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

    // private final static Logger log = LoggerFactory.getLogger(OperationsSetupActionTest.class);

}
