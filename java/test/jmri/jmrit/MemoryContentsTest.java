package jmri.jmrit;

import java.io.File;
import java.io.IOException;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of MemoryContents
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @suthor B. Milhaupt Copyright (C) 2014
 */
public class MemoryContentsTest {

    @Test
    public void testReadNormalFile() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadNormalFile");

        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile.hex"));
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected Checksum Exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected Record Contents Exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        Assert.assertEquals("content restarts", 864, m.nextContent(500));
    }

    @Test
    public void testReadSegmentsTestFile() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadSegmentsTestFile");

        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_extSegRecords.hex"));
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected Checksum Exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected Record Contents Exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        verifyMemoryData(0x00000, 0x01, m);
        verifyMemoryData(0x00001, 0x02, m);
        verifyMemoryData(0x00002, -1, m);
        verifyMemoryData(0x00003, -1, m);

        verifyMemoryData(0x0FFFF, -1, m);
        verifyMemoryData(0x10000, 0x03, m);
        verifyMemoryData(0x10001, 0x04, m);
        verifyMemoryData(0x10002, -1, m);
        verifyMemoryData(0x10003, -1, m);

        verifyMemoryData(0x1FFFF, -1, m);
        verifyMemoryData(0x20000, 0x05, m);
        verifyMemoryData(0x20001, 0x06, m);
        verifyMemoryData(0x20002, -1, m);
        verifyMemoryData(0x20003, -1, m);

        verifyMemoryData(0x2FFFF, -1, m);
        verifyMemoryData(0x30000, 0x07, m);
        verifyMemoryData(0x30001, 0x08, m);
        verifyMemoryData(0x30002, -1, m);
        verifyMemoryData(0x30003, -1, m);

        verifyMemoryData(0x3FFFF, -1, m);

        verifyMemoryData(0x9FFFF, -1, m);
        jmri.util.JUnitAppender.assertErrorMessage("Error in getLocation(0x9ffff): accessed uninitialized page 9");
        
        verifyMemoryData(0xA0000, 0xa0, m);
        verifyMemoryData(0xA0001, 0xa1, m);
        verifyMemoryData(0xA0002, -1, m);
        verifyMemoryData(0xA0003, -1, m);
    }

    @Test
    public void testReadNormal24BitAddressFile() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadNormal24BitAddressFile");
        String filename = "java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_24bit.hex";
        try {
            m.readHex(new java.io.File(filename));
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception reading " + filename); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected Checksum Exception reading " + filename); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception reading" + filename); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected Record Contents Exception reading" + filename); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception reading" + filename);
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception reading" + filename);
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception reading" + filename);
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception reading" + filename);
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception reading" + filename);
        }

        verifyMemoryData(0x10000, 0x54, m);

        verifyMemoryData(0x10001, 0x32, m);

        verifyMemoryData(0x20001, 0xBE, m);

        verifyMemoryData(0x20002, 0xEF, m);

        // Write a copy of the MemoryContents to a new file
        // File path for this file generated by the testcase is modeled after
        // the temp file used in jmri.jmrix.rps.PackageFileTest.java
        String tempDirectoryName = "temp";
        if (!(new File(tempDirectoryName).isDirectory())) {
            // create the temp directory if it does not exist
            FileUtil.createDirectory(tempDirectoryName);
        }
        filename = tempDirectoryName + File.separator + "MemoryContentsTestOutputFile.hex";
        File MemoryContentsTestWrite_24AddrFile;

        MemoryContentsTestWrite_24AddrFile = new File(filename);
        if (MemoryContentsTestWrite_24AddrFile.exists()) {
            try {
                MemoryContentsTestWrite_24AddrFile.delete();
            } catch (java.lang.Exception e) {
                Assert.fail("Exception while trying to delete the existing file "
                        + filename + " before creating a new copy of the file.\n Exception reported: " + e.toString());
            }
            return;
        }

        m.setAddressFormat(MemoryContents.LoadOffsetFieldType.ADDRESSFIELDSIZE24BITS);
        m.addKeyValueComment("Harrumph", "TRUE");

        try {
            java.io.FileWriter writer = new java.io.FileWriter(MemoryContentsTestWrite_24AddrFile);
            m.writeHex(writer,
                    true, 16);
            writer.close(); // make sure to close the stream associated with the file
        } catch (java.io.IOException e) {
            Assert.fail("I/O exception attempting to write a .hex file " + e);
        } catch (jmri.jmrit.MemoryContents.MemoryFileAddressingFormatException e) {
            Assert.fail("Memory Addressing format exception attempting to write .hex file");
        }

        // make sure the new file is in 24-bit address format
        m.setAddressFormat(jmri.jmrit.MemoryContents.LoadOffsetFieldType.ADDRESSFIELDSIZE24BITS);
        MemoryContents n = new MemoryContents();

        try {
            n.readHex(new java.io.File(filename));
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception reading " + filename); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected Checksum Exception reading " + filename); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception reading " + filename); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected Record Contents Exception reading " + filename); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception reading " + filename);
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception reading " + filename);
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception reading " + filename);
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception reading " + filename);
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception reading " + filename + ": " + e.toString());
        }

        // attempt to delete the file if debug logging is not enabled.
        if (log.isDebugEnabled()) {
            log.debug("Path to written hex file is: " + filename);
        } else {
            MemoryContentsTestWrite_24AddrFile.delete();
        }

        if (n.nextContent(500) != 864) {
            Assert.fail("NextContent didn't reply as expected for nextContent(500) - "
                    + "expected 864, got "
                    + m.nextContent(500)
                    + ".");
        }

        // Code here uses manual validity checks (the "if" statements) below 
        // instead of Assert.assertEquals because of unexplainable lack of 
        // failure in some forced failing cases.        
        verifyMemoryData(0x00, 0xa8, n);

        verifyMemoryData(0x01, 0x29, n);

        if (m.locationInUse(0x02)) {
            verifyMemoryData(0x02, -1, n);
        }

        if (m.locationInUse(0x03)) {
            verifyMemoryData(0x03, -1, n);
        }

        if (m.locationInUse(0x04)) {
            verifyMemoryData(0x04, -1, n);
        }

        if (m.locationInUse(0x05)) {
            verifyMemoryData(0x05, -1, n);
        }

        if (m.locationInUse(0x06)) {
            verifyMemoryData(0x06, -1, n);
        }

        if (m.locationInUse(0x07)) {
            verifyMemoryData(0x07, -1, n);
        }

        verifyMemoryData(0xA0, 0x0B, n);

        verifyMemoryData(0xA1, 0x16, n);

        verifyMemoryData(0x400E, 0x5c, n);

        verifyMemoryData(0x400F, 0x09, n);

        verifyMemoryData(0x4010, 0xFF, n);

        verifyMemoryData(0x4011, 0x3F, n);
    }

    @Test
    public void testReadFileCksumError() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileCksumError");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_cksumErr.hex"));
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            expectedExceptionHappened = true;
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected Record Contents Exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }
        JUnitAppender.assertErrorMessage(
                "Record checksum error in line 29 - computed checksum = 0x1f, expected checksum = 0x1e.");

        Assert.assertTrue("Checksum Exception was expected", expectedExceptionHappened);
    }

    @Test
    public void testReadFileRecordTypeError() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileRecordTypeError");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_recordTypeError.hex"));
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected Checksum Exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            expectedExceptionHappened = true;
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected Record Contents Exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "Unknown RECTYP 0x3 was found in line 16.  Aborting file read.");

        Assert.assertTrue("Record Type Exception was expected", expectedExceptionHappened);
    }

    @Test
    public void testReadFileRecordLengthError() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileRecordLengthError");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_recordLenError.hex"));
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            expectedExceptionHappened = true;
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected Checksum Exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail(); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "Data record line length is incorrect for inferred addressing type and for data count field in line 10");

        Assert.assertTrue("Record Length Exception was expected", expectedExceptionHappened);

        if (!(m.extractValueOfKey("KeyName2").matches("ValueInfo99"))) {
            Assert.fail("Expect to retrieve 'ValueInfo99' as value of key KeyName2, but got '"
                    + m.extractValueOfKey("KeyName2")
                    + "' instead.");
        }
        Assert.assertNull(m.extractValueOfKey("non-existent_Key"));
        if (m.extractValueOfKey("fictitiousKeyName") != null) {
            Assert.fail("Failed to return <null> for non-existant key name 'fictitiousKeyName'.");
        }
    }

    @Test
    public void testReadFileFileNotFound() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileFileNotFound");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_file_doesnt_exist.hex"));
        } catch (java.io.FileNotFoundException e) {
            expectedExceptionHappened = true;
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        Assert.assertTrue("File-not-found Exception was expected", expectedExceptionHappened);

        // make sure that key/value from previous test is gone now.
        Assert.assertNull("old key/value disappears", m.extractValueOfKey(m.extractValueOfKey("KeyName2")));
        Assert.assertEquals("old data disappears", -1, m.nextContent(500));
    }

    @Test
    public void testReadFileMalformedLine() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileMalformedLine");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_malformed_line.hex"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            expectedExceptionHappened = true;
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "Data record line length is incorrect for inferred addressing type and for data count field in line 7");

        Assert.assertTrue("Record Content Length exception expected", expectedExceptionHappened);
    }

    @Test
    public void testReadFileNoEOFRecordFile() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileNoEOFRecordFile");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_noEOFRecord.hex"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            expectedExceptionHappened = true;
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "No EOF Record found in file - aborting.");

        Assert.assertTrue("No EOF Record exception expected", expectedExceptionHappened);
    }

    @Test
    public void testReadFileNoContentFile() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileNoContentFile");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_no_data_records.dmf"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected file record length exception");
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            expectedExceptionHappened = true;
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "No Data Records found in file - aborting.");

        Assert.assertTrue("'No records found' exception expected", expectedExceptionHappened);
    }

    @Test
    public void testReadFile16bitContentFile() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFile16bitContentFile");

        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_16bit.hex"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected file record length exception");
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }
    }

    @Test
    public void testReadFileRecordType02BadFile() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileRecordType02BadFile");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_RecType02bad.hex"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected file record length exception");
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception"); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            expectedExceptionHappened = true;
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "Unsupported Extended Segment Address Record data value 0x1000 in line 3");

        Assert.assertTrue("'Addressing range' exception expected", expectedExceptionHappened);
    }

    @Test
    public void testReadFileRecordType02BadFile2() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileRecordType02BadFile2");

        boolean expectedExceptionHappened = false;
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_RecType02bad_2.hex"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected file record length exception");
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception:" + e); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            expectedExceptionHappened = true;
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "Unsupported Extended Segment Address Record data value 0x1000 in line 2");

        Assert.assertTrue("'Addressing range' exception expected", expectedExceptionHappened);
    }

    @Test
    public void testReadFileHighSegments() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFileHighSegments");

        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_24bitHighSegs.hex"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected file record length exception");
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception:" + e); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        verifyMemoryData(0xFFFFFF, 0x7B, m);

        if (m.locationInUse(0xFFFFFE)) {
            verifyMemoryData(0xFFFFFE, -1, m);
        }

        if (m.locationInUse(0x0)) {
            verifyMemoryData(0x0, -1, m);
        }

        verifyMemoryData(0xF000FF, 0xD0, m);

        verifyMemoryData(0xFF0000, 0x9A, m);

        verifyMemoryData(0xD000FF, 0x12, m);

        verifyMemoryData(0xD00100, 0x34, m);
    }

    @Test
    public void testReadFilePageCross() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFilePageCross");

        boolean expectedExceptionHappened = false;

        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_16bit_pagecross.hex"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected file record length exception");
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception:" + e); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            expectedExceptionHappened = true;
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "Data crosses boundary which could lead to  mis-interpretation.  Aborting read at line :02FFFF000102FD");

        Assert.assertTrue("Address Range exception was expected", expectedExceptionHappened);

        if (m.locationInUse(0x00FFFF)) {
            verifyMemoryData(0x00FFFF, -1, m);
        }

        if (m.locationInUse(0x00FFFE)) {
            verifyMemoryData(0x00FFFE, -1, m);
        }

        if (m.locationInUse(0x00FF00)) {
            verifyMemoryData(0x00FF00, -1, m);
        }

        if (m.locationInUse(0x00FF01)) {
            verifyMemoryData(0x00FF01, -1, m);
        }

        if (m.locationInUse(0x00FF02)) {
            verifyMemoryData(0x00FF02, -1, m);
        }

        if (m.locationInUse(0x00FF03)) {
            verifyMemoryData(0x00FF03, -1, m);
        }

        if (m.locationInUse(0x010000)) {
            verifyMemoryData(0x010000, -1, m);
        }

        if (m.locationInUse(0x010001)) {
            verifyMemoryData(0x010001, -1, m);
        }

        if (m.locationInUse(0x010002)) {
            verifyMemoryData(0x010002, -1, m);
        }

        if (m.locationInUse(0x010003)) {
            verifyMemoryData(0x010003, -1, m);
        }
    }

    @Test
    public void testReadFile24bitPageCross() {
        MemoryContents m = new MemoryContents();
        log.debug("Begin of testReadFile24bitPageCross");

        boolean expectedExceptionHappened = false;

        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_24bit_pagecross.hex"));
        } catch (java.io.FileNotFoundException e) {
            Assert.fail("Unexpected file-not-found exception e");
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected file record length exception");
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected checksum exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected record contents exception:" + e); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            expectedExceptionHappened = true;
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }

        JUnitAppender.assertErrorMessage(
                "Data crosses boundary which could lead to  mis-interpretation.  Aborting read at line :0201FFFF000709EF");

        Assert.assertTrue("Address Range exception was expected", expectedExceptionHappened);

        if (m.locationInUse(0x01FFFF)) {
            verifyMemoryData(0x01FFFF, -1, m);
        }

        if (m.locationInUse(0x01FFFE)) {
            verifyMemoryData(0x01FFFE, -1, m);
        }

        if (m.locationInUse(0x020000)) {
            verifyMemoryData(0x020000, -1, m);
        }

        if (m.locationInUse(0x020001)) {
            verifyMemoryData(0x020001, -1, m);
        }

        if (m.locationInUse(0x020002)) {
            verifyMemoryData(0x020002, -1, m);
        }

        if (m.locationInUse(0x020003)) {
            verifyMemoryData(0x020003, -1, m);
        }

        if (m.locationInUse(0x010000)) {
            verifyMemoryData(0x010000, -1, m);
        }

        if (m.locationInUse(0x010001)) {
            verifyMemoryData(0x010001, -1, m);
        }

        if (m.locationInUse(0x010002)) {
            verifyMemoryData(0x010002, -1, m);
        }

        if (m.locationInUse(0x010003)) {
            verifyMemoryData(0x010003, -1, m);
        }

    }

    private void verifyMemoryData(int address, int expect, MemoryContents mc) {
        int reported = mc.getLocation(address);
        if (reported != expect) {
            Assert.fail("Verify that address 0x" + Integer.toHexString(address)
                    + " has correct value: expected 0x" + Integer.toHexString(expect)
                    + ", got 0x" + Integer.toHexString(reported));
        }
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }
    private final static Logger log = LoggerFactory.getLogger(MemoryContentsTest.class);

}
