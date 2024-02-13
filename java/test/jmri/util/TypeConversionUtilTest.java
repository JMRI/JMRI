package jmri.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jmri.Reportable;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.util.TypeConversionUtil class.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class TypeConversionUtilTest {

    private void assertIAE(String message, Runnable r) {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            r.run();
        });
        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    public void testConvertToBoolean() {
        List<Integer> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        Map<Integer, Integer> map = new HashMap<>();

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            TypeConversionUtil.convertToBoolean(null, false);
        });
        Assertions.assertEquals("Value is null", thrown.getMessage());

        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean(false, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean(true, false));
        assertIAE("Value is null", () -> {TypeConversionUtil.convertToBoolean(null, false);});
        assertIAE("Value \"\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean("", false);});
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean("0", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean("0.000", false));
        assertIAE("Value \"[]\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(list, false);});
        assertIAE("Value \"[]\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(set, false);});
        assertIAE("Value \"{}\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(map, false);});
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean(0, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean(-0.499999, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean(0.499999, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean(0.0, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean(-0.0, false));
        assertIAE("Value \"Abc\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean("Abc", false);});
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean("123", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean("-32", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean("-0.4999", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean("0.4999", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean("-0.5", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean("0.5", false));
        list.add(0);    // A list that contains at least one element can't still not be converted to a boolean
        assertIAE("Value \"[0]\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(list, false);});
        set.add(0);     // A set that contains at least one element can't still not be converted to a boolean
        assertIAE("Value \"[0]\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(set, false);});
        map.put(0,0);   // A map that contains at least one key can't still not be converted to a boolean
        assertIAE("Value \"{0=0}\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean(map, false);});
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean(-0.5, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean(0.5, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean(123.56, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean(-123.56, false));
        assertIAE("Value \"0abc\" can't be converted to a boolean", () -> {TypeConversionUtil.convertToBoolean("0abc", false);});
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean("false", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean("faLSe", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean("false", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean("true", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean("tRUe", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean("TRUE", false));

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
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean(reportableFalse, false));

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
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean(reportableTrue, false));
    }

    @Test
    public void testConvertToBoolean_JythonRules() {
        List<Integer> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        Map<Integer, Integer> map = new HashMap<>();
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(null, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules("", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules("0", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules("0.000", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(list, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(set, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(map, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(0, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(-0.499999, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(0.499999, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(0.0, false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(-0.0, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules("Abc", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules("123", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules("-32", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules("-0.4999", false));
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules("0.4999", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules("-0.5", false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules("0.5", false));
        list.add(0);    // A list that contains at least one element is converted to true
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules(list, false));
        set.add(0);     // A set that contains at least one element is converted to true
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules(set, false));
        map.put(0,0);   // A map that contains at least one key is converted to true
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules(map, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules(-0.5, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules(0.5, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules(123.56, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules(-123.56, false));
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules("0abc", false));

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
        Assert.assertFalse("value is false", TypeConversionUtil.convertToBoolean_JythonRules(reportableFalse, false));

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
        Assert.assertTrue("value is true", TypeConversionUtil.convertToBoolean_JythonRules(reportableTrue, false));
    }

    @Test
    public void testConvertToDouble() {
        Assert.assertTrue("doubles are equal", 0.0 == TypeConversionUtil.convertToDouble(null, false));
        JUnitAppender.suppressWarnMessage("the object is null and the returned number is therefore 0.0");
        Assert.assertTrue("doubles are equal", 0.0 == TypeConversionUtil.convertToDouble("", false));
        JUnitAppender.suppressWarnMessage("the string \"\" cannot be converted to a number");
        Assert.assertTrue("doubles are equal", 123 == TypeConversionUtil.convertToDouble(123, false));         // Integer
        Assert.assertTrue("doubles are equal", 123 == TypeConversionUtil.convertToDouble(123L, false));        // Long
        Assert.assertTrue("doubles are equal", 123.523f == TypeConversionUtil.convertToDouble(123.523f, false));  // Float
        Assert.assertTrue("doubles are equal", 123.523 == TypeConversionUtil.convertToDouble(123.523d, false));  // Double
        Assert.assertTrue("doubles are equal", 12352.3 == TypeConversionUtil.convertToDouble(123.523e2, false));
        Assert.assertTrue("doubles are equal", 1.23523 == TypeConversionUtil.convertToDouble(123.523e-2, false));
        Assert.assertTrue("doubles are equal", 1 == TypeConversionUtil.convertToDouble(true, false));     // true is autoboxed to a Boolean and converted to 1
        Assert.assertTrue("doubles are equal", 0 == TypeConversionUtil.convertToDouble(false, false));    // false is autoboxed to a Boolean and converted to 0
        Assert.assertTrue("doubles are equal", 0.0 == TypeConversionUtil.convertToDouble("Abc", false));
        Assert.assertTrue("doubles are equal", 0.0 == TypeConversionUtil.convertToDouble("Ab12.32c", false));
        Assert.assertTrue("doubles are equal", 0.0 == TypeConversionUtil.convertToDouble("Abc12.34", false));
        Assert.assertTrue("doubles are equal", 123 == TypeConversionUtil.convertToDouble("123", false));
        Assert.assertTrue("doubles are equal", 123.523 == TypeConversionUtil.convertToDouble("123.523", false));
        Assert.assertTrue("doubles are equal", 12352.3 == TypeConversionUtil.convertToDouble("123.523e2", false));
        Assert.assertTrue("doubles are equal", 1.23523 == TypeConversionUtil.convertToDouble("123.523e-2", false));
        Assert.assertTrue("doubles are equal", 123 == TypeConversionUtil.convertToDouble("123abc", false));
        Assert.assertTrue("doubles are equal", 123.523 == TypeConversionUtil.convertToDouble("123.523abc", false));
        Assert.assertTrue("doubles are equal", 12352.3 == TypeConversionUtil.convertToDouble("123.523e2abc", false));
        Assert.assertTrue("doubles are equal", 1.23523 == TypeConversionUtil.convertToDouble("123.523e-2abc", false));
        Assert.assertTrue("doubles are equal", 0 == TypeConversionUtil.convertToDouble("true", false));   // "true" is treated as a string, not as a boolean
        Assert.assertTrue("doubles are equal", 0 == TypeConversionUtil.convertToDouble("false", false));  // "false" is treated as a string, not as a boolean
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
        Assert.assertTrue("doubles are equal", 12.34 == TypeConversionUtil.convertToDouble(reportable, false));
    }

    @Test
    public void testConvertToString() {
        Assert.assertTrue("strings are equal", "".equals(TypeConversionUtil.convertToString(null, false)));
        Assert.assertTrue("strings are equal", "".equals(TypeConversionUtil.convertToString("", false)));
        Assert.assertTrue("strings are equal", "123".equals(TypeConversionUtil.convertToString(123, false)));     // Integer
        Assert.assertTrue("strings are equal", "123".equals(TypeConversionUtil.convertToString(123L, false)));    // Long
        Assert.assertTrue("strings are equal", "123.523".equals(TypeConversionUtil.convertToString(123.523f, false)));    // Float
        Assert.assertTrue("strings are equal", "123.523".equals(TypeConversionUtil.convertToString(123.523d, false)));    // Double
        Assert.assertTrue("strings are equal", "true".equals(TypeConversionUtil.convertToString(true, false)));
        Assert.assertTrue("strings are equal", "false".equals(TypeConversionUtil.convertToString(false, false)));
        Assert.assertTrue("strings are equal", "Abc".equals(TypeConversionUtil.convertToString("Abc", false)));

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
        Assert.assertTrue("strings are equal", "Something".equals(TypeConversionUtil.convertToString(reportable, false)));
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
