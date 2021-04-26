package jmri.jmrit.operations.setup;

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

    // private final static Logger log = LoggerFactory.getLogger(ManageBackupsActionTest.class);

}
