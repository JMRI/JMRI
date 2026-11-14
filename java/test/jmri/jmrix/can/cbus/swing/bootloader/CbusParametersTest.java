package jmri.jmrix.can.cbus.swing.bootloader;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static jmri.jmrix.can.cbus.node.CbusNodeConstants.*;

/**
 * Tests for the CbusParameters class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class CbusParametersTest {

    @Test
    public void testCTor() {
        CbusParameters p = new CbusParameters();
        Assertions.assertNotNull( p, "exists");
        Assertions.assertEquals( CbusParameters.MAX_PARAMS + 1, p.paramData.length, "Param Data length");
        Assertions.assertFalse( p.areValid(), "not valid");
    }

    @Test
    public void testIsValidIndex() {
        CbusParameters p = new CbusParameters();
        Assertions.assertTrue( p.isValidIndex(0), "index 0");
        Assertions.assertTrue( p.isValidIndex(CbusParameters.MAX_PARAMS), "last parameter index");
        Assertions.assertFalse( p.isValidIndex(CbusParameters.MAX_PARAMS + 1), "beyond last parameter index");
        Assertions.assertFalse( p.isValidIndex(-1), "index -1");
    }

    @Test
    public void testCTorFromBytes() {
        CbusParameters p = new CbusParameters(new byte[] {(byte) 165, 'a', 10, 32, 2, 16, 2});
        Assertions.assertTrue( p.areValid(), "valid");
        Assertions.assertEquals( 7, p.getParam(NUM_PARAM_IDX), "count");
        Assertions.assertEquals( 165, p.getParam(MANU_ID_IDX), "manufacturer");
        Assertions.assertEquals( 2, p.getParam(MAJOR_VER_IDX), "major version");
        Assertions.assertEquals( 0x800, p.getLoadAddress(), "load address for 7 parameters");
    }

    @Test
    public void testValidate() {
        CbusParameters fp = new CbusParameters(new byte[] {(byte) 165, 'a', 10, 32, 2, 16, 2, 0, 13, 1});
        CbusParameters hp = new CbusParameters(new byte[] {(byte) 165, 'b', 10, 32, 2, 16, 3, 0, 13, 1});
        Assertions.assertTrue( fp.validate(fp, hp), "matching parameters");

        Assertions.assertFalse( fp.validate(fp, null), "null hardware parameters");
        Assertions.assertFalse( fp.validate(null, hp), "null file parameters");
        Assertions.assertFalse( fp.validate(fp, new CbusParameters()), "invalid hardware parameters");

        CbusParameters other = new CbusParameters(new byte[] {(byte) 165, 'a', 11, 32, 2, 16, 2, 0, 13, 1});
        Assertions.assertFalse( fp.validate(fp, other), "module ID mismatch");
        JUnitAppender.assertErrorMessageStartsWith("Module ID mismatch");

        other = new CbusParameters(new byte[] {(byte) 165, 'a', 10, 32, 2, 16, 2, 0, 14, 1});
        Assertions.assertFalse( fp.validate(fp, other), "processor type mismatch");
        JUnitAppender.assertErrorMessageStartsWith("Processor type mismatch");
    }

    @Test
    public void testToString() {
        CbusParameters p = new CbusParameters();
        Assertions.assertEquals( Bundle.getMessage("ParamsInvalid"), p.toString(), "invalid parameters");

        p = new CbusParameters(new byte[] {(byte) 165, 'a', 10});
        Assertions.assertEquals( "165 97 10 ", p.toString(), "all parameters listed");
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
