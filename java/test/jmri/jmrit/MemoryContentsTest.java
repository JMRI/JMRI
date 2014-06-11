// MemoryContentsTest.java

package jmri.jmrit;

import java.io.File;
import java.io.IOException;
import jmri.util.FileUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;

/**
 * Test simple functioning of MemoryContents
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision$
 */

public class MemoryContentsTest extends TestCase {

    public void testReadNormalFile() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        
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

    public void testReadNormal24BitAddressFile() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        
        try {
            m.readHex(new java.io.File("java/test/jmri/jmrit/MemoryContentsTestFiles/TestFiles/MemoryContentsTestFile_24bit.hex"));
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
        
        verifyMemoryData(0x10000, 0x54, m);

        verifyMemoryData(0x10001, 0x32, m);

        verifyMemoryData(0x20001, 0xBE, m);

        verifyMemoryData(0x20002, 0xEF, m);

        // Write a copy of the MemoryContents to a new file
        FileUtil.createDirectory(FileUtil.getUserFilesPath()+"/TestDir");
        File MemoryContentsTestWrite_24AddrFile = null;
        try {
             MemoryContentsTestWrite_24AddrFile = java.io.File.createTempFile("MemContentsTestWrite_24Addr", null);
        } catch (IOException e) {
            Assert.fail("Unexpected IO Exception creating temp file: "+e);
        }
        String tempFilePathAndName = MemoryContentsTestWrite_24AddrFile.getPath();
        
        m.setAddressFormat(MemoryContents.LoadOffsetFieldType.ADDRESSFIELDSIZE24BITS);
        m.addKeyValueComment("Harrumph", "TRUE");
        try {
            m.writeHex(new java.io.FileWriter(MemoryContentsTestWrite_24AddrFile),
                    true,16);
        } catch (java.io.IOException e) {
            Assert.fail("I/O exception attempting to write a .hex file " + e);
        } catch (jmri.jmrit.MemoryContents.MemoryFileAddressingFormatException e) {
            Assert.fail("Memory Addressing format exception attempting to write .hex file");
        }
        
        log.info("Path to written hex file is: "+tempFilePathAndName);

        // make sure the new file is in 24-bit address format
        m.setAddressFormat(jmri.jmrit.MemoryContents.LoadOffsetFieldType.ADDRESSFIELDSIZE24BITS);
        MemoryContents n = new MemoryContents();
        
        try {
            n.readHex(new java.io.File(tempFilePathAndName));
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordLengthException e) {
            Assert.fail("Unexpected record length exception when reading the written .hex file"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileChecksumException e) {
            Assert.fail("Unexpected Checksum Exception when reading the written .hex file"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileUnknownRecordType e) {
            Assert.fail("Unexpected unknown record type exception when reading the written .hex file"); // got an unexpected exception so fail.
        } catch (jmri.jmrit.MemoryContents.MemoryFileRecordContentException e) {
            Assert.fail("Unexpected Record Contents Exception when reading the written .hex file: "+e); // got an unexpected exception so fail.
        } catch (MemoryContents.MemoryFileNoDataRecordsException e) {
            Assert.fail("Unexpected 'no records found' exception when reading the written .hex file");
        } catch (MemoryContents.MemoryFileNoEOFRecordException e) {
            Assert.fail("Unexpected 'no EOF record found' exception when reading the written .hex file");
        } catch (MemoryContents.MemoryFileRecordFoundAfterEOFRecord e) {
            Assert.fail("Unexpected 'record after EOF record' exception");
        } catch (MemoryContents.MemoryFileAddressingRangeException e) {
            Assert.fail("unexpected 'addressing range' exception");
        } catch (IOException e) {
            Assert.fail("unexpected 'IOException' exception");
        }
        
        if (n.nextContent(500) != 864) {
            Assert.fail("NextContent didn't reply as expected for nextContent(500) - "
                    + "expected 864, got "
                    +m.nextContent(500)
                    +".");
        }

        // Code here uses manual validity checks (the "if" statements) below 
        // instead of Assert.assertEquals because of unexplainable lack of 
        // failure in some forced failing cases.        
        verifyMemoryData(0x00, 0xa8, n);

        verifyMemoryData(0x01, 0x29, n);

        verifyMemoryData(0x02, -1, n);

        verifyMemoryData(0x03, -1, n);

        verifyMemoryData(0x04, -1, n);

        verifyMemoryData(0x05, -1, n);

        verifyMemoryData(0x06, -1, n);

        verifyMemoryData(0x07, -1, n);

        verifyMemoryData(0xA0, 0x0B, n);

        verifyMemoryData(0xA1, 0x16, n);

        verifyMemoryData(0x400E, 0x5c, n);

        verifyMemoryData(0x400F, 0x09, n);
        
        verifyMemoryData(0x4010, 0xFF, n);

        verifyMemoryData(0x4011, 0x3F, n);
    }

    public void testReadFileCksumError() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();

        log.info("Expect a 'computed checksum error' in the log");
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
        
        Assert.assertTrue("Checksum Exception was expected", expectedExceptionHappened);
        log.info("Should have seen a 'computed checksum error' in the log immediately above this entry");
    }
    
    public void testReadFileRecordTypeError() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        
        log.info("Expect a 'Unknown hex record type error' in the log");
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
        
        Assert.assertTrue("Record Type Exception was expected", expectedExceptionHappened);
        log.info("Should have seen a 'Unknown hex record type error' in the log immediately above this entry");
    }
        
    public void testReadFileRecordLengthError() throws java.io.FileNotFoundException {
        MemoryContents m = new MemoryContents();
        
        log.info("Expect a 'Record Length error' in the log.");
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
        
        Assert.assertTrue("Record Length Exception was expected", expectedExceptionHappened);

        if (! (m.extractValueOfKey("KeyName2").matches("ValueInfo99"))) {
            Assert.fail("Expect to retrieve 'ValueInfo99' as value of key KeyName2, but got '"
                    + m.extractValueOfKey("KeyName2")
                    +"' instead.");
        }
        Assert.assertNull(m.extractValueOfKey("non-existent_Key"));
        if (m.extractValueOfKey("fictitiousKeyName") != null) {
            Assert.fail("Failed to return <null> for non-existant key name 'fictitiousKeyName'.");
        } 
    }

    public void testReadFileFileNotFound() {
        MemoryContents m = new MemoryContents();
        
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
    
    public void testReadFileMalformedLine() {
        MemoryContents m = new MemoryContents();
        
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
        
        Assert.assertTrue("Record Content Length exception expected", expectedExceptionHappened);
    }
    
    public void testReadFileNoEOFRecordFile() {
        MemoryContents m = new MemoryContents();
        
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
        
            Assert.assertTrue("No EOF Record exception expected", expectedExceptionHappened);
    }
    
    public void testReadFileNoContentFile() {
        MemoryContents m = new MemoryContents();
        
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
        
        Assert.assertTrue("'No records found' exception expected", expectedExceptionHappened);
    }
    
    public void testReadFile16bitContentFile() {
        MemoryContents m = new MemoryContents();
        
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
    
    public void testReadFileRecordType02BadFile() {
        MemoryContents m = new MemoryContents();
        
        log.info("Expect an 'Unsupported Extended Segment Address Record data value' error in the log.");
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
        
        Assert.assertTrue("'Addressing range' exception expected", expectedExceptionHappened);
        log.info("Should have seen an 'Unsupported Extended Segment Address Record data value' error in the log.");
    }
    
    public void testReadFileRecordType02BadFile2() {
        MemoryContents m = new MemoryContents();
        
        log.info("Expect an 'Unsupported Extended Segment Address Record data value' error in the log.");
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
            Assert.fail("Unexpected record contents exception:"+e); // got an unexpected exception so fail.
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
        
        Assert.assertTrue("'Addressing range' exception expected", expectedExceptionHappened);
        log.info("Should have seen an 'Unsupported Extended Segment Address Record data value' error in the log.");
    }
    
    public void testReadFileHighSegments() {
        MemoryContents m = new MemoryContents();
        
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
            Assert.fail("Unexpected record contents exception:"+e); // got an unexpected exception so fail.
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
        
        verifyMemoryData(0xFFFFFE, -1, m);

        verifyMemoryData(0x0, -1, m);
        
        verifyMemoryData(0xF000FF, 0xD0, m);

        verifyMemoryData(0xFF0000, 0x9A, m);
        
        verifyMemoryData(0xD000FF, 0x12, m);
        
        verifyMemoryData(0xD00100, 0x34, m);
    }
    
    public void testReadFilePageCross() {
        MemoryContents m = new MemoryContents();
        
        boolean expectedExceptionHappened = false;
        
        log.info("Expect a 'Data crosses boundary error' in the log");
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
            Assert.fail("Unexpected record contents exception:"+e); // got an unexpected exception so fail.
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
        
        Assert.assertTrue("Address Range exception was expected", expectedExceptionHappened);
        log.info("Should have seen a 'Data crosses boundary error' in the log immediately above this entry");
        
        log.info("Expect to find ten 'getLocation when no data at that location' errors in log.");

        verifyMemoryData(0x00FFFF, -1, m);
        
        verifyMemoryData(0x00FFFE, -1, m);

        verifyMemoryData(0x00FF00, -1, m);
        
        verifyMemoryData(0x00FF01, -1, m);

        verifyMemoryData(0x00FF02, -1, m);
        
        verifyMemoryData(0x00FF03, -1, m);

        verifyMemoryData(0x010000, -1, m);
        
        verifyMemoryData(0x010001, -1, m);

        verifyMemoryData(0x010002, -1, m);
        
        verifyMemoryData(0x010003, -1, m);

        log.info("Should have seen ten 'getLocation when no data at that location' errors in log.");
    }

    public void testReadFile24bitPageCross() {
        MemoryContents m = new MemoryContents();
        
        boolean expectedExceptionHappened = false;
        
        log.info("Expect a 'Data crosses boundary error' in the log");
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
            Assert.fail("Unexpected record contents exception:"+e); // got an unexpected exception so fail.
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
        
        Assert.assertTrue("Address Range exception was expected", expectedExceptionHappened);
        log.info("Should have seen a 'Data crosses boundary error' in the log immediately above this entry");

        log.info("Expect to find ten 'getLocation when no data at that location' errors in log.");
        verifyMemoryData(0x01FFFF, -1, m);
        
        verifyMemoryData(0x01FFFE, -1, m);

        verifyMemoryData(0x020000, -1, m);
        
        verifyMemoryData(0x020001, -1, m);

        verifyMemoryData(0x020002, -1, m);
        
        verifyMemoryData(0x020003, -1, m);
        
        verifyMemoryData(0x010000, -1, m);
        
        verifyMemoryData(0x010001, -1, m);

        verifyMemoryData(0x010002, -1, m);
        
        verifyMemoryData(0x010003, -1, m);
        log.info("Should have seen ten 'getLocation when no data at that location' errors in log.");
        
    }


    
    
    private void verifyMemoryData(int address, int expect, MemoryContents mc) {
        int reported = mc.getLocation(address);
        if (reported != expect) {
            Assert.fail("Verify that address 0x" + Integer.toHexString(address)
                    + " has correct value: expected 0x" + Integer.toHexString(expect)
                    + ", got 0x" + Integer.toHexString(reported));
    }
}
    public MemoryContentsTest(String s) {
            super(s);
    }

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", MemoryContentsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(MemoryContentsTest.class);
		return suite;
	}

        // The minimal setup for log4J
        @Override protected void setUp() {apps.tests.Log4JFixture.setUp(); }
        @Override protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

	static Logger log = Logger.getLogger(MemoryContentsTest.class.getName());

}
