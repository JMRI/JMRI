package jmri.jmrit.operations.setup.backup;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ManageBackupsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ManageBackupsAction t = new ManageBackupsAction();
        Assert.assertNotNull("exists",t);
    }

    // private static final Logger log = LoggerFactory.getLogger(ManageBackupsActionTest.class);

}
