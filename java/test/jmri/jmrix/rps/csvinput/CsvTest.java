package jmri.jmrix.rps.csvinput;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jmri.util.JUnitUtil;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.rps.csvinput package.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class CsvTest {

    @Test
    public void testCreateReader() throws java.io.IOException {
        var format = CSVFormat.DEFAULT;
        format.builder().setSkipHeaderRecord(true);
        CSVParser parser = CSVParser.parse(new File("java/test/jmri/jmrix/rps/csvinput/testdata.csv"),
            StandardCharsets.UTF_8, format);
        assertNotNull( parser, "exists");
    }

    @Test
    public void testReading() throws java.io.IOException {
        CSVParser parser = CSVParser.parse(new File("java/test/jmri/jmrix/rps/csvinput/testdata.csv"),
            StandardCharsets.UTF_8, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals( 2, records.size(), "2 lines");

        CSVRecord record = records.get(0);
        assertNotNull( record, "read 1st line");
        assertEquals( 4, record.size(), "1st line column count");

        assertEquals( "1", record.get(0), "1st line datum 1");
        assertEquals( "2", record.get(1), "1st line datum 2");
        assertEquals( "3", record.get(2), "1st line datum 3");
        assertEquals( "4", record.get(3), "1st line datum 4");

        record = records.get(1);
        assertNotNull( record, "read 2nd line");

        assertEquals( "4", record.get(0), "2nd line datum 1");
        assertEquals( "3", record.get(1), "2nd line datum 2");
        assertEquals( "2", record.get(2), "2nd line datum 3");
        assertEquals( "1", record.get(3), "2nd line datum 4");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
