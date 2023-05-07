package jmri.jmrix.can.cbus.node;

import java.io.File;
import java.nio.file.Path;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupFileTest {

    @Test
    public void testCTorNull() {
        CbusNodeBackupFile t = new CbusNodeBackupFile(null);
        Assertions.assertNotNull(t,"exists");
    }

    @Test
    public void testCTorMemo() {
        CbusNodeBackupFile t = new CbusNodeBackupFile(memo);
        Assertions.assertNotNull(t,"exists");
    }

    @Test
    public void testDeleteNodeNotExist() {
        CbusNodeBackupFile t = new CbusNodeBackupFile(memo);
        Assertions.assertNotNull(t,"exists");
        Assertions.assertTrue(t.deleteFile(999));
    }

    @Test
    public void testProvideNewFile() {
        CbusNodeBackupFile t = new CbusNodeBackupFile(memo);
        Assertions.assertNull(t.getFile(777, false),"file not created");
        Assertions.assertNotNull(t.getFile(777, true),"file created");
    }

    @Test
    public void testMigrate()  throws java.io.IOException {
        CbusNode node = new CbusNode(memo,41376);
        CbusNodeBackupManager t = new CbusNodeBackupManager(node);
        CbusNodeBackupFile nbaf = new CbusNodeBackupFile(memo);
        
        java.io.File dir = new java.io.File("java/test/jmri/jmrix/can/cbus/node/");
        java.io.File systemFile = new java.io.File(dir, "41376.xml");
        
        FileUtil.createDirectory(nbaf.oldFileLocation);
        File newFile = new File (nbaf.oldFileLocation, "41376.xml");
        
        java.nio.file.Files.copy(systemFile.toPath(), newFile.toPath(), 
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertEquals(0, t.getBackups().size(),"File should not be loaded");
        
        t.doLoad();
        Assertions.assertEquals(14, t.getBackups().size(), "File loaded");

        JUnitAppender.assertWarnMessage("Migrated existing CBUS Node Data to CAN");

    }

    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp( @TempDir Path tempDir ) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        memo = new CanSystemConnectionMemo();
        memo.configureManagers(); // no manager but will register memo to instancemanager
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeBackupFile.class);

}
