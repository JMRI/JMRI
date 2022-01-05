package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.IOException;
import jmri.jmrit.logixng.NamedTable;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test DefaultLogixNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultNamedTableTest {

    private static final String expectedExcelTable101 =
            "IQT101\n" +
            "North yard | East yard | South yard | \n" +
            "Left turnoutIT101 | IT201 | IT301 | \n";

    private static final String expectedExcelTable102 =
            "IQT102\n" +
            "North yard | East yard | South yard | \n" +
            "Left turnoutIT101 | IT201 | IT301 | \n";

    private static final String expectedExcelTable101_SwedishCharacters = "";

    private static final String expectedExcelTable102_SwedishCharacters = "";


    private String getTableAsText(NamedTable table) {
        StringBuilder sb = new StringBuilder();
        sb.append(table.toString());
        sb.append("\n");
        for (int row=0; row < table.numRows(); row++) {
            for (int col=0; col < table.numColumns(); col++) {
                sb.append(table.getCell(row, col));
                if (col > 0) sb.append(" | ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DefaultInternalNamedTable("IQT10", "A table", 10, 15));
    }
    
    @Test
    public void testTSVFile() throws IOException {
        NamedTable table = AbstractNamedTable.loadTableFromTSV_File(
                "IQT1", null,
                new File("java/test/jmri/jmrit/logixng/panel_and_data_files/turnout_and_signals.csv"),
                true);
        
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File file = new File(FileUtil.getUserFilesPath() + "temp/" + "turnout_and_signals.csv");
//        System.out.format("Temporary file: %s%n", file.getAbsoluteFile());
        table.storeTableAsTSV(file);
        Assert.assertNotNull("exists", table);
    }

    @Test
    public void testExcelTSVFiles() throws IOException {
        NamedTable table = AbstractNamedTable.loadTableFromTSV_File(
                "IQT101", null,
                new File("java/test/jmri/jmrit/logixng/panel_and_data_files/ExcelTable.txt"),
                true);
        Assert.assertNotNull("exists", table);
        Assert.assertEquals(expectedExcelTable101, getTableAsText(table));

        table = AbstractNamedTable.loadTableFromTSV_File(
                "IQT102", null,
                new File("java/test/jmri/jmrit/logixng/panel_and_data_files/ExcelTable.tsv"),
                true);
        Assert.assertNotNull("exists", table);
        Assert.assertEquals(expectedExcelTable102, getTableAsText(table));
    }

    @Ignore("This test currently fails due to non English characters")
    @Test
    public void testExcelTSVFilesSwedishCharacters() throws IOException {
        NamedTable table = AbstractNamedTable.loadTableFromTSV_File(
                "IQT101", null,
                new File("java/test/jmri/jmrit/logixng/panel_and_data_files/ExcelTable_SwedishCharacters.txt"),
                true);
        Assert.assertNotNull("exists", table);
        Assert.assertEquals(expectedExcelTable101_SwedishCharacters, getTableAsText(table));

        table = AbstractNamedTable.loadTableFromTSV_File(
                "IQT102", null,
                new File("java/test/jmri/jmrit/logixng/panel_and_data_files/ExcelTable_SwedishCharacters.tsv"),
                true);
        Assert.assertNotNull("exists", table);
        Assert.assertEquals(expectedExcelTable102_SwedishCharacters, getTableAsText(table));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
