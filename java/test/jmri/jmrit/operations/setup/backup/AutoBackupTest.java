package jmri.jmrit.operations.setup.backup;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoBackupTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        AutoBackup t = new AutoBackup();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(AutoBackupTest.class);

}
