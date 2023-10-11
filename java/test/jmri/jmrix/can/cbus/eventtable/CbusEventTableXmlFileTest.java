package jmri.jmrix.can.cbus.eventtable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.util.*;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventTableXmlFileTest {

    @Test
    public void testCTor() {
        CbusEventTableXmlFile t = new CbusEventTableXmlFile(memo);
        Assertions.assertNotNull(t,"exists");
    }

    @Test
    public void testProvideNewFile() {
        CbusEventTableXmlFile t = new CbusEventTableXmlFile(memo);
        Assertions.assertNull(t.getFile(false),"file not created");
        Assertions.assertNotNull(t.getFile(true),"file created");
    }

    @Test
    public void testMigratedFile() {

        Assertions.assertNotNull(memo);
        CbusEventTableXmlFile x = new CbusEventTableXmlFile(memo);

        File dir = new File("java/test/jmri/jmrix/can/cbus/eventtable/");
        File systemFile = new File(dir, "EventTableData-1.xml");

        FileUtil.createDirectory(x.oldFileLocation);

        File newFile = new File (x.oldFileLocation, x.getFileName());

        try {
            java.nio.file.Files.copy(systemFile.toPath(), newFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ex){
            Assertions.fail("Could not copy temp file to path ", ex );
        }

        CbusEventTableXmlAction.restoreEventsFromXmlTablestart(model);
        Assertions.assertEquals(3, model.getRowCount());
        JUnitAppender.assertWarnMessage("Migrated existing CBUS Event Table Data to CAN");

    }

    private CbusEventTableDataModel model = null;
    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp( @TempDir Path tempDir ) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));

        // tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        // memo.setTrafficController(tcis);

        memo.get(CbusConfigurationManager.class).provide(CbusEventTableDataModel.class);
        model = memo.get(CbusEventTableDataModel.class);
        model.skipSaveOnDispose();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableXmlFileTest.class);

}
