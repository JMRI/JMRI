package jmri.jmrit.operations.setup;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RestoreFilesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RestoreFilesAction t = new RestoreFilesAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RestoreFilesActionTest.class);

}
