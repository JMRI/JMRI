package jmri.jmrit.operations.setup;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BackupDialogTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BackupDialog t = new BackupDialog();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(BackupDialogTest.class);

}
