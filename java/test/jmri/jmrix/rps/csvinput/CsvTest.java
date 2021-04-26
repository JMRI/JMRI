package jmri.jmrix.rps.csvinput;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.rps.csvinput package.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class CsvTest {

    @Test
    public void testCreateReader() throws java.io.IOException {
        CSVParser parser = CSVParser.parse(new File("java/test/jmri/jmrix/rps/csvinput/testdata.csv"), StandardCharsets.UTF_8, CSVFormat.DEFAULT.withSkipHeaderRecord());
        Assert.assertNotNull("exists", parser);
    }

    @Test
    public void testReading() throws java.io.IOException {
        CSVParser parser = CSVParser.parse(new File("java/test/jmri/jmrix/rps/csvinput/testdata.csv"), StandardCharsets.UTF_8, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals("2 lines", 2, records.size());
        
        CSVRecord record = records.get(0);
        Assert.assertNotNull("read 1st line", record);
        Assert.assertEquals("1st line column count", 4, record.size());

        Assert.assertEquals("1st line datum 1", "1", record.get(0));
        Assert.assertEquals("1st line datum 2", "2", record.get(1));
        Assert.assertEquals("1st line datum 3", "3", record.get(2));
        Assert.assertEquals("1st line datum 4", "4", record.get(3));

        record = records.get(1);
        Assert.assertNotNull("read 2nd line", record);

        Assert.assertEquals("2nd line datum 1", "4", record.get(0));
        Assert.assertEquals("2nd line datum 2", "3", record.get(1));
        Assert.assertEquals("2nd line datum 3", "2", record.get(2));
        Assert.assertEquals("2nd line datum 4", "1", record.get(3));
    }

}
