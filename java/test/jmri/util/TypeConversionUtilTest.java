package jmri.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jmri.Reportable;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jmri.util.TypeConversionUtil class.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class TypeConversionUtilTest {

    private void assertIAE(String message, Runnable r) {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            r.run();
        });
        assertEquals(message, thrown.getMessage());
    }

    @Test
    public void testConvertToBoolean() {
        List<Integer> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        Map<Integer, Integer> map = new HashMap<>();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            TypeConversionUtil.convertToBoolean(null, false);
        });
        assertEquals("Value is null", thrown.getMessage());

        assertFalse( TypeConversionUtil.convertToBoolean(false, false), "value is false");
        assertTrue( TypeConversionUtil.convertToBoolean(true, false), "value is true");
        assertIAE("Value is null", () -> {TypeConversionUtil.convertToBoolean(null, false);});
        assertIAE("Value \"\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean("", false);});
        assertFalse( TypeConversionUtil.convertToBoolean("0", false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean("0.000", false), "value is false");
        assertIAE("Value \"[]\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(list, false);});
        assertIAE("Value \"[]\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(set, false);});
        assertIAE("Value \"{}\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(map, false);});
        assertFalse( TypeConversionUtil.convertToBoolean(0, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean(-0.499999, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean(0.499999, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean(0.0, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean(-0.0, false), "value is false");
        assertIAE("Value \"Abc\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean("Abc", false);});
        assertTrue( TypeConversionUtil.convertToBoolean("123", false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean("-32", false), "value is true");
        assertFalse( TypeConversionUtil.convertToBoolean("-0.4999", false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean("0.4999", false), "value is false");
        assertTrue( TypeConversionUtil.convertToBoolean("-0.5", false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean("0.5", false), "value is true");
        list.add(0);    // A list that contains at least one element can't still not be converted to a boolean
        assertIAE("Value \"[0]\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(list, false);});
        set.add(0);     // A set that contains at least one element can't still not be converted to a boolean
        assertIAE("Value \"[0]\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(set, false);});
        map.put(0,0);   // A map that contains at least one key can't still not be converted to a boolean
        assertIAE("Value \"{0=0}\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(map, false);});
        assertTrue( TypeConversionUtil.convertToBoolean(-0.5, false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean(0.5, false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean(123.56, false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean(-123.56, false), "value is true");
        assertIAE("Value \"0abc\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean("0abc", false);});
        assertFalse( TypeConversionUtil.convertToBoolean("false", false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean("faLSe", false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean("false", false), "value is false");
        assertTrue( TypeConversionUtil.convertToBoolean("true", false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean("tRUe", false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean("TRUE", false), "value is true");

        // Test report
        Reportable reportableFalse = new Reportable() {
            @Override
            public String toReportString() {
                return "false";
            }
            @Override
            public String toString() {
                return "";
            }
        };
        // Test that the method toReportString() is used for Reportable objects
        assertFalse( TypeConversionUtil.convertToBoolean(reportableFalse, false), "value is false");

        // Test report
        Reportable reportableTrue = new Reportable() {
            @Override
            public String toReportString() {
                return "true";
            }
            @Override
            public String toString() {
                return "";
            }
        };
        // Test that the method toReportString() is used for Reportable objects
        assertTrue( TypeConversionUtil.convertToBoolean(reportableTrue, false), "value is true");
    }

    @Test
    public void testConvertToBoolean_JythonRules() {
        List<Integer> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        Map<Integer, Integer> map = new HashMap<>();
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(null, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules("", false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules("0", false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules("0.000", false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(list, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(set, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(map, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(0, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(-0.499999, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(0.499999, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(0.0, false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(-0.0, false), "value is false");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules("Abc", false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules("123", false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules("-32", false), "value is true");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules("-0.4999", false), "value is false");
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules("0.4999", false), "value is false");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules("-0.5", false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules("0.5", false), "value is true");
        list.add(0);    // A list that contains at least one element is converted to true
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules(list, false), "value is true");
        set.add(0);     // A set that contains at least one element is converted to true
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules(set, false), "value is true");
        map.put(0,0);   // A map that contains at least one key is converted to true
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules(map, false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules(-0.5, false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules(0.5, false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules(123.56, false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules(-123.56, false), "value is true");
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules("0abc", false), "value is true");

        // Test report
        Reportable reportableFalse = new Reportable() {
            @Override
            public String toReportString() {
                return "";
            }
            @Override
            public String toString() {
                return "true";
            }
        };
        // Test that the method toReportString() is used for Reportable objects
        assertFalse( TypeConversionUtil.convertToBoolean_JythonRules(reportableFalse, false), "value is false");

        // Test report
        Reportable reportableTrue = new Reportable() {
            @Override
            public String toReportString() {
                return "true";
            }
            @Override
            public String toString() {
                return "";
            }
        };
        // Test that the method toReportString() is used for Reportable objects
        assertTrue( TypeConversionUtil.convertToBoolean_JythonRules(reportableTrue, false), "value is true");
    }

    @Test
    public void testConvertToDouble() {
        assertTrue( 0.0 == TypeConversionUtil.convertToDouble(null, false), "doubles are equal");
        JUnitAppender.suppressWarnMessage("the object is null and the returned number is therefore 0.0");
        assertTrue( 0.0 == TypeConversionUtil.convertToDouble("", false), "doubles are equal");
        JUnitAppender.suppressWarnMessage("the string \"\" cannot be converted to a number");
        assertTrue( 123 == TypeConversionUtil.convertToDouble(123, false), "doubles are equal Integer");
        assertTrue( 123 == TypeConversionUtil.convertToDouble(123L, false), "doubles are equal Long");
        assertTrue( 123.523f == TypeConversionUtil.convertToDouble(123.523f, false), "doubles are equal Float");
        assertTrue( 123.523 == TypeConversionUtil.convertToDouble(123.523d, false), "doubles are equal Double");
        assertTrue( 12352.3 == TypeConversionUtil.convertToDouble(123.523e2, false), "doubles are equal");
        assertTrue( 1.23523 == TypeConversionUtil.convertToDouble(123.523e-2, false), "doubles are equal");
        assertTrue( 1 == TypeConversionUtil.convertToDouble(true, false),
            "doubles are equal true is autoboxed to a Boolean and converted to 1");
        assertTrue( 0 == TypeConversionUtil.convertToDouble(false, false),
            "doubles are equal false is autoboxed to a Boolean and converted to 0");
        assertTrue( 0.0 == TypeConversionUtil.convertToDouble("Abc", false), "doubles are equal");
        assertTrue( 0.0 == TypeConversionUtil.convertToDouble("Ab12.32c", false), "doubles are equal");
        assertTrue( 0.0 == TypeConversionUtil.convertToDouble("Abc12.34", false), "doubles are equal");
        assertTrue( 123 == TypeConversionUtil.convertToDouble("123", false), "doubles are equal");
        assertTrue( 123.523 == TypeConversionUtil.convertToDouble("123.523", false), "doubles are equal");
        assertTrue( 12352.3 == TypeConversionUtil.convertToDouble("123.523e2", false), "doubles are equal");
        assertTrue( 1.23523 == TypeConversionUtil.convertToDouble("123.523e-2", false), "doubles are equal");
        assertTrue( 123 == TypeConversionUtil.convertToDouble("123abc", false), "doubles are equal");
        assertTrue( 123.523 == TypeConversionUtil.convertToDouble("123.523abc", false), "doubles are equal");
        assertTrue( 12352.3 == TypeConversionUtil.convertToDouble("123.523e2abc", false), "doubles are equal");
        assertTrue( 1.23523 == TypeConversionUtil.convertToDouble("123.523e-2abc", false), "doubles are equal");
        assertTrue( 0 == TypeConversionUtil.convertToDouble("true", false),
            "doubles are equal \"true\" is treated as a string, not as a boolean");
        assertTrue( 0 == TypeConversionUtil.convertToDouble("false", false),
            "doubles are equal \"false\" is treated as a string, not as a boolean");
        JUnitAppender.suppressWarnMessage("the string \"Abc\" cannot be converted to a number");

        // Test report
        Reportable reportable = new Reportable() {
            @Override
            public String toReportString() {
                return "12.34Something";
            }
            @Override
            public String toString() {
                return "Something other";
            }
        };
        // Test that the method toReportString() is used for Reportable objects
        assertTrue( 12.34 == TypeConversionUtil.convertToDouble(reportable, false), "doubles are equal");
    }

    @Test
    public void testConvertToString() {
        assertEquals( "", TypeConversionUtil.convertToString(null, false), "strings are equal");
        assertEquals( "", TypeConversionUtil.convertToString("", false), "strings are equal");
        assertEquals( "123", TypeConversionUtil.convertToString(123, false), "strings are equal Integer");
        assertEquals( "123", TypeConversionUtil.convertToString(123L, false), "strings are equal Long");
        assertEquals( "123.523", TypeConversionUtil.convertToString(123.523f, false), "strings are equal Float");
        assertEquals( "123.523", TypeConversionUtil.convertToString(123.523d, false), "strings are equal Double");
        assertEquals( "true", TypeConversionUtil.convertToString(true, false), "strings are equal");
        assertEquals( "false", TypeConversionUtil.convertToString(false, false), "strings are equal");
        assertEquals( "Abc", TypeConversionUtil.convertToString("Abc", false), "strings are equal");

        // Test report
        Reportable reportable = new Reportable() {
            @Override
            public String toReportString() {
                return "Something";
            }
            @Override
            public String toString() {
                return "Something other";
            }
        };
        // Test that the method toReportString() is used for Reportable objects
        assertEquals( "Something", TypeConversionUtil.convertToString(reportable, false), "strings are equal");
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
