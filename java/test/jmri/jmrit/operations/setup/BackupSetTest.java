package jmri.jmrit.operations.setup;

import java.io.File;

import jmri.jmrit.operations.OperationsTestCase;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BackupSetTest extends OperationsTestCase {

    @Test
    public void testCTor(@TempDir File folder) {
        BackupSet t = new BackupSet(folder);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(BackupSetTest.class.getName());

}
