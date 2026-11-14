package jmri.jmrix.can.cbus.swing.bootloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static jmri.jmrix.can.cbus.node.CbusNodeConstants.*;
import static jmri.jmrix.can.cbus.swing.bootloader.HexFileTestUtil.*;

/**
 * Tests for the CbusPicHexFile class
 *
 * @author Bob Andrew Crosland (C) 2022
 */
public class CbusPicHexFileTest {

    @TempDir
    File tempDir;

    private static final int MODULE_ID = 0x0A;

    @Test
    public void testCTor() {
        HexFile f = new HexFile("cbusPicHexFileTest");
        Assertions.assertNotNull(f, "exists");
    }

    /**
     * Read a hex file built from the given records.
     */
    private CbusPicHexFile readFile(String name, List<String> records) throws IOException {
        File file = write(tempDir, name, records.toArray(new String[0]));
        CbusPicHexFile f = new CbusPicHexFile(file.getPath());
        f.openRd();
        try {
            f.read();
        } finally {
            f.close();
        }
        return f;
    }

    /**
     * Build the records for a file with code at 0x800 - 0x81F, a parameter
     * block at 0x820 and some code after it, using the given record length.
     */
    private List<String> fileWithParams(int[] params, int recLen, boolean codeAfter) {
        List<String> recs = new ArrayList<>();
        recs.addAll(dataRecords(0x800, fill(0x20, 0x12), recLen));
        recs.addAll(dataRecords(0x820, params, recLen));
        if (codeAfter) {
            recs.addAll(dataRecords(0x840, fill(0x20, 0x34), recLen));
        }
        recs.add(eof());
        return recs;
    }

    private void checkValidNewParams(CbusParameters p) {
        Assertions.assertTrue(p.areValid(), "parameters valid");
        Assertions.assertEquals(20, p.getParam(NUM_PARAM_IDX), "parameter count");
        Assertions.assertEquals(CbusConstants.MANU_MERG, p.getParam(MANU_ID_IDX), "manufacturer");
        Assertions.assertEquals(MODULE_ID, p.getParam(MODULE_ID_IDX), "module ID");
        Assertions.assertEquals('a', p.getParam(MINOR_VER_IDX), "minor version");
        Assertions.assertEquals(2, p.getParam(MAJOR_VER_IDX), "major version");
        Assertions.assertEquals(13, p.getParam(PROC_TYPE_IDX), "processor type");
        Assertions.assertEquals(0x800, p.getLoadAddress(), "load address");
    }

    @Test
    public void testValidNewParameterBlock() throws IOException {
        int[] params = newParamBlock(CbusConstants.MANU_MERG, MODULE_ID, 0x800);
        // Whole block in one 32-byte record
        CbusPicHexFile f = readFile("valid32.hex", fileWithParams(params, 32, true));
        checkValidNewParams(f.getParams());
    }

    @Test
    public void testValidNewParameterBlockSplitRecords() throws IOException {
        int[] params = newParamBlock(CbusConstants.MANU_MERG, MODULE_ID, 0x800);
        // Block split over four 8-byte records, checksum must accumulate over all of them
        CbusPicHexFile f = readFile("valid8.hex", fileWithParams(params, 8, true));
        checkValidNewParams(f.getParams());
        // and over two 16-byte records
        f = readFile("valid16.hex", fileWithParams(params, 16, true));
        checkValidNewParams(f.getParams());
    }

    @Test
    public void testValidNewParameterBlockInLargeRecord() throws IOException {
        int[] params = newParamBlock(CbusConstants.MANU_MERG, MODULE_ID, 0x800);
        // 64-byte record from 0x800 that encompasses the whole block
        int[] big = new int[0x40];
        System.arraycopy(fill(0x20, 0x12), 0, big, 0, 0x20);
        System.arraycopy(params, 0, big, 0x20, 0x20);
        List<String> recs = new ArrayList<>();
        recs.add(data(0x800, big));
        recs.addAll(dataRecords(0x840, fill(0x20, 0x34), 64));
        recs.add(eof());
        CbusPicHexFile f = readFile("valid64.hex", recs);
        checkValidNewParams(f.getParams());
    }

    @Test
    public void testValidNewParameterBlockAtEndOfFile() throws IOException {
        int[] params = newParamBlock(CbusConstants.MANU_MERG, MODULE_ID, 0x800);
        // Nothing after the parameter block
        CbusPicHexFile f = readFile("validEnd.hex", fileWithParams(params, 8, false));
        checkValidNewParams(f.getParams());
    }

    @Test
    public void testCorruptedNewParameterBlockRejected() throws IOException {
        int[] params = newParamBlock(CbusConstants.MANU_MERG, MODULE_ID, 0x800);
        // Change a parameter without updating the checksum
        params[MODULE_ID_IDX - 1] = MODULE_ID + 1;
        CbusPicHexFile f = readFile("corrupt32.hex", fileWithParams(params, 32, true));
        Assertions.assertFalse(f.getParams().areValid(), "corrupted block in one record rejected");
        f = readFile("corrupt8.hex", fileWithParams(params, 8, true));
        Assertions.assertFalse(f.getParams().areValid(), "corrupted block in split records rejected");

        // Correct parameters but wrong checksum
        params = newParamBlock(CbusConstants.MANU_MERG, MODULE_ID, 0x800);
        params[PARAM_CHECK_IDX - 1] ^= 0x01;
        f = readFile("badsum.hex", fileWithParams(params, 32, true));
        Assertions.assertFalse(f.getParams().areValid(), "wrong checksum rejected");
    }

    @Test
    public void testAllZeroBlockRejected() throws IOException {
        // An all zero block has a "matching" zero checksum but is not a parameter block
        CbusPicHexFile f = readFile("zero.hex", fileWithParams(fill(32, 0), 8, true));
        Assertions.assertFalse(f.getParams().areValid(), "all zero block rejected");
    }

    @Test
    public void testNoParameterBlock() throws IOException {
        List<String> recs = new ArrayList<>();
        recs.addAll(dataRecords(0x800, fill(0x80, 0x12), 16));
        recs.add(eof());
        CbusPicHexFile f = readFile("noparams.hex", recs);
        Assertions.assertFalse(f.getParams().areValid(), "no parameter block");
    }

    @Test
    public void testOldParameterBlock() throws IOException {
        // Old style 7 parameter block at 0x810, no valid new style block
        int[] old = new int[] {CbusConstants.MANU_MERG, 'b', MODULE_ID, 32, 2, 16, 1, 0x00};
        List<String> recs = new ArrayList<>();
        recs.addAll(dataRecords(0x800, fill(0x10, 0x12), 8));
        recs.add(data(0x810, old));
        recs.addAll(dataRecords(0x818, fill(0x48, 0x12), 8));
        recs.add(eof());
        CbusPicHexFile f = readFile("old.hex", recs);
        CbusParameters p = f.getParams();
        Assertions.assertTrue(p.areValid(), "old parameters valid");
        Assertions.assertEquals(7, p.getParam(NUM_PARAM_IDX), "old parameter count");
        Assertions.assertEquals(CbusConstants.MANU_MERG, p.getParam(MANU_ID_IDX), "old manufacturer");
        Assertions.assertEquals('b', p.getParam(MINOR_VER_IDX), "old minor version");
        Assertions.assertEquals(MODULE_ID, p.getParam(MODULE_ID_IDX), "old module ID");
        Assertions.assertEquals(1, p.getParam(MAJOR_VER_IDX), "old major version");
        Assertions.assertEquals(0x800, p.getLoadAddress(), "old load address");
    }

    @Test
    public void testOldParameterBlockNotMerg() throws IOException {
        // Old style block is only assumed valid for MERG modules
        int[] old = new int[] {CbusConstants.MANU_MERG + 1, 'b', MODULE_ID, 32, 2, 16, 1, 0x00};
        List<String> recs = new ArrayList<>();
        recs.addAll(dataRecords(0x800, fill(0x10, 0x12), 8));
        recs.add(data(0x810, old));
        recs.addAll(dataRecords(0x818, fill(0x48, 0x12), 8));
        recs.add(eof());
        CbusPicHexFile f = readFile("oldNotMerg.hex", recs);
        Assertions.assertFalse(f.getParams().areValid(), "non MERG old parameters not valid");
    }

    @Test
    public void testNewParameterBlockPreferredOverOld() throws IOException {
        int[] params = newParamBlock(CbusConstants.MANU_MERG, MODULE_ID, 0x800);
        int[] old = new int[] {CbusConstants.MANU_MERG, 'b', MODULE_ID + 1, 32, 2, 16, 1, 0x00};
        List<String> recs = new ArrayList<>();
        recs.addAll(dataRecords(0x800, fill(0x10, 0x12), 8));
        recs.add(data(0x810, old));
        recs.addAll(dataRecords(0x818, fill(0x08, 0x12), 8));
        recs.addAll(dataRecords(0x820, params, 8));
        recs.addAll(dataRecords(0x840, fill(0x20, 0x34), 8));
        recs.add(eof());
        CbusPicHexFile f = readFile("both.hex", recs);
        checkValidNewParams(f.getParams());
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
