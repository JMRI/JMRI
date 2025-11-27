package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.jmrit.logixng.NamedTable;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test DefaultCsvNamedTable.
 *
 * @author Daniel Bergqvist 2025
 */
public class DefaultCsvNamedTableTest {

    public static Stream<Arguments> getFiles(File directory) {
        ArrayList<Arguments> files = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                files.addAll(getFiles(file).collect(Collectors.toList()));
            }
        } else if (directory.getName().endsWith(".csv")) {
            files.add(Arguments.of(directory));
        }
        return files.stream();
    }

    public static Stream<Arguments> data() {
        return getFiles(new File("java/test/jmri/jmrit/logixng/implementation/load"));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void loadAndStoreTest(File file) throws Exception {
        // The test will overwrite the file so make a copy of it and test the copy.
        Path copy = new File(FileUtil.getUserFilesPath() + file.getName()).toPath();
        Files.copy(file.toPath(), copy, StandardCopyOption.REPLACE_EXISTING);

        // Test the copy, not the original file!
        checkCSVFile(copy.toFile());
    }

    private void checkCSVFile(File file) throws IOException {
        List<String> beforeLines = Files.readAllLines(file.toPath());

        NamedTable table = AbstractNamedTable.loadTableFromCSV_File(
                "IQT1", null, file, true, DefaultCsvNamedTable.CsvType.TABBED);
        table.storeTableAsCSV();
        Assertions.assertNotNull(table, "exists");

        List<String> afterLines = Files.readAllLines(file.toPath());

        Assertions.assertEquals(beforeLines.size(), afterLines.size(),
                String.format("Both before file and after file has same number of lines. Before: %d, after: %d. File: %s",
                        beforeLines.size(), afterLines.size(), file));

        for (int i=0; i < beforeLines.size(); i++) {
            Assertions.assertEquals(beforeLines.get(i), afterLines.get(i), String.format("Line %d is correct. File: %s", i, file));
        }
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp(@TempDir java.io.File tempDir) throws IOException  {
        JUnitUtil.setUp();

//        tempDir = new File("temp/csvAAA/"); // For temporary testing only

        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
