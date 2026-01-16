package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import jmri.jmrit.logixng.NamedTable;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test DefaultLogixNG
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultNamedTableTest {

    @Test
    public void testCtor() {
        assertNotNull( new DefaultInternalNamedTable("IQT10", "A table", 10, 15), "exists");
    }

    @Test
    public void testCSVFile() throws IOException {
        NamedTable table = AbstractNamedTable.loadTableFromCSV_File(
                "IQT1", null,
                new File("java/test/jmri/jmrit/logixng/panel_and_data_files/turnout_and_signals.csv"),
                true,
                DefaultCsvNamedTable.CsvType.TABBED);

        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File file = new File(FileUtil.getUserFilesPath() + "temp/" + "turnout_and_signals.csv");
//        System.out.format("Temporary file: %s%n", file.getAbsoluteFile());
        table.storeTableAsCSV(file);
        assertNotNull( table, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
