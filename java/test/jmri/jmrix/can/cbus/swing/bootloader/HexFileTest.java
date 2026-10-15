package jmri.jmrix.can.cbus.swing.bootloader;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static jmri.jmrix.can.cbus.swing.bootloader.HexFileTestUtil.*;

/**
 * Tests for the HexFile class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class HexFileTest {

    @TempDir
    File tempDir;

    @Test
    public void testCTor() {
        HexFile f = new HexFile("cbusHexFileTest");
        Assertions.assertNotNull( f, "exists");
    }

    @Test
    public void testCloseWithoutOpen() {
        HexFile f = new HexFile("cbusHexFileTest");
        Assertions.assertDoesNotThrow(() -> {
            f.close();
            f.close();
            f.dispose();
        });
    }

    @Test
    public void testRecordsBeforeRead() {
        HexFile f = new HexFile("cbusHexFileTest");
        Assertions.assertFalse(f.getRecordForAddress(0x800).isPresent(), "no record before read");
        HexRecord r = f.getNextRecord();
        Assertions.assertEquals(HexRecord.END, r.type, "END record when nothing read");
        JUnitAppender.assertWarnMessageStartsWith("No more hex records to read");
    }

    @Test
    public void testReadAndGetRecordForAddress() throws IOException {
        File file = write(tempDir, "records.hex",
                data(0x0800, 1, 2, 3, 4, 5, 6, 7, 8),
                data(0x0810, 9, 10, 11, 12, 13, 14, 15, 16),
                eof());
        HexFile f = new HexFile(file.getPath());
        f.openRd();
        f.read();
        f.close();

        Assertions.assertEquals(0x800, f.getProgStart(), "program start");
        Assertions.assertEquals(0x818, f.getProgEnd(), "program end");

        Optional<HexRecord> r = f.getRecordForAddress(0x800);
        Assertions.assertTrue(r.isPresent(), "record at start address");
        Assertions.assertEquals(0x800, r.get().address, "record address");
        Assertions.assertEquals(8, r.get().len, "record length");
        Assertions.assertEquals(1, r.get().getData(0), "record data");

        // Next record follows the one found
        HexRecord next = f.getNextRecord();
        Assertions.assertEquals(HexRecord.TYPE_DATA, next.type, "next is data");
        Assertions.assertEquals(0x810, next.address, "next record address");
        // Then the end record
        Assertions.assertEquals(HexRecord.END, f.getNextRecord().type, "then END");
        // And nothing more, but no exception
        Assertions.assertEquals(HexRecord.END, f.getNextRecord().type, "still END");
        JUnitAppender.assertWarnMessageStartsWith("No more hex records to read");

        // Address inside a record
        r = f.getRecordForAddress(0x814);
        Assertions.assertTrue(r.isPresent(), "record containing address");
        Assertions.assertEquals(0x810, r.get().address, "containing record address");
    }

    @Test
    public void testGetRecordForAbsentAddress() throws IOException {
        File file = write(tempDir, "absent.hex",
                data(0x0800, 1, 2, 3, 4, 5, 6, 7, 8),
                data(0x0810, 9, 10, 11, 12, 13, 14, 15, 16),
                eof());
        HexFile f = new HexFile(file.getPath());
        f.openRd();
        f.read();
        f.close();

        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertFalse(f.getRecordForAddress(0x900).isPresent(), "address after data");
            Assertions.assertFalse(f.getRecordForAddress(0x7FF).isPresent(), "address before data");
            Assertions.assertFalse(f.getRecordForAddress(0x818).isPresent(), "address just past last record");
            Assertions.assertFalse(f.getRecordForAddress(0x808).isPresent(), "address in gap");
        });
        // Not found leaves the index at the END record so programming stops cleanly
        Assertions.assertEquals(HexRecord.END, f.getNextRecord().type, "END after not found");
    }

    @Test
    public void testExtendedLinearAddress() throws IOException {
        File file = write(tempDir, "ext.hex",
                data(0x0800, 1, 2, 3, 4, 5, 6, 7, 8),
                extAddr(0x0030),
                data(0x0000, 0xFF, 0xFE, 0xFD, 0xFC, 0xFB, 0xFA, 0xF9, 0xF8),
                eof());
        HexFile f = new HexFile(file.getPath());
        f.openRd();
        f.read();
        f.close();

        Optional<HexRecord> r = f.getRecordForAddress(0x300000);
        Assertions.assertTrue(r.isPresent(), "record in extended address space");
        Assertions.assertEquals(0x300000, r.get().address, "extended record address");
        Assertions.assertEquals((byte) 0xFF, r.get().getData(0), "extended record data");

        // Extended address records are not kept as data records
        r = f.getRecordForAddress(0x800);
        Assertions.assertTrue(r.isPresent(), "first record");
        HexRecord next = f.getNextRecord();
        Assertions.assertEquals(HexRecord.TYPE_DATA, next.type, "next is the data record");
        Assertions.assertEquals(0x300000, next.address, "next data record address");
        Assertions.assertEquals(HexRecord.END, f.getNextRecord().type, "then END");
    }

    @Test
    public void testUnsupportedRecordTypeRejected() throws IOException {
        // Extended segment address record, type 02
        File file = write(tempDir, "seg.hex",
                data(0x0800, 1, 2, 3, 4, 5, 6, 7, 8),
                record(0x0000, 2, 0x10, 0x00),
                data(0x0000, 9, 10, 11, 12, 13, 14, 15, 16),
                eof());
        checkRejected(file, 2);

        // Start segment address record, type 03
        file = write(tempDir, "startseg.hex",
                record(0x0000, 3, 0x00, 0x00, 0x00, 0x00),
                data(0x0800, 1, 2, 3, 4, 5, 6, 7, 8),
                eof());
        checkRejected(file, 3);

        // Start linear address record, type 05
        file = write(tempDir, "startlin.hex",
                data(0x0800, 1, 2, 3, 4, 5, 6, 7, 8),
                record(0x0000, 5, 0x00, 0x00, 0x08, 0x00),
                eof());
        checkRejected(file, 5);

        // Unknown type
        file = write(tempDir, "unknown.hex",
                data(0x0800, 1, 2, 3, 4, 5, 6, 7, 8),
                record(0x0000, 7, 0x01),
                eof());
        checkRejected(file, 7);
    }

    private void checkRejected(File file, int type) throws IOException {
        HexFile f = new HexFile(file.getPath());
        f.openRd();
        IOException ex = Assertions.assertThrows(IOException.class, f::read, "type " + type + " rejected");
        Assertions.assertTrue(ex.getMessage().contains("Unsupported hex record type " + type),
                "message for type " + type + ": " + ex.getMessage());
        f.close();
        JUnitAppender.assertErrorMessageStartsWith("Unsupported hex record type " + type);
    }

    @Test
    public void testBadChecksumRejected() throws IOException {
        String good = data(0x0800, 1, 2, 3, 4, 5, 6, 7, 8);
        // Corrupt the last (checksum) byte
        String bad = good.substring(0, good.length() - 2) + "00";
        File file = write(tempDir, "badsum.hex", bad, eof());
        HexFile f = new HexFile(file.getPath());
        f.openRd();
        Assertions.assertThrows(IOException.class, f::read, "bad checksum rejected");
        f.close();
        JUnitAppender.assertErrorMessageStartsWith("Bad checksum");
        JUnitAppender.assertErrorMessageStartsWith("Exception reading hex record");
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
