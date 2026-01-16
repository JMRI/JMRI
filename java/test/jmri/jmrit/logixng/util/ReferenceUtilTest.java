package jmri.jmrit.logixng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Test ReferenceUtil
 *
 * @author Daniel Bergqvist 2019
 */
public class ReferenceUtilTest {

    private MemoryManager _memoryManager;
    private NamedTableManager _tableManager;
    private NamedTable yardTable;

    // The system appropriate newline separator.
    private static final String NEWLINE = System.getProperty("line.separator"); // NOI18N

    // no Ctor test, tested class only supplies static methods

    private void expectException(Runnable r, Class<? extends Exception> exceptionClass, String errorMessage) {
        Exception e = assertThrowsExactly( exceptionClass, () -> r.run());
        assertEquals( errorMessage, e.getMessage(), "Exception message is correct");
    }

    @Test
    public void testIsReference() {
        assertFalse( ReferenceUtil.isReference(null), "Is reference");
        assertFalse( ReferenceUtil.isReference("{}"), "Is reference");
        assertTrue( ReferenceUtil.isReference("{A}"), "Is reference");
        assertTrue( ReferenceUtil.isReference("{Abc 123}"), "Is reference");
        assertFalse( ReferenceUtil.isReference("{"), "Is reference");
        assertFalse( ReferenceUtil.isReference("}"), "Is reference");
        assertFalse( ReferenceUtil.isReference("{Abc"), "Is reference");
        assertFalse( ReferenceUtil.isReference("Abc}"), "Is reference");
        assertFalse( ReferenceUtil.isReference("Abc"), "Is reference");
    }

    @Test
    public void testGetReference() {

        Memory m1 = _memoryManager.newMemory("IM1", "Memory 1");
        Memory m2 = _memoryManager.newMemory("IM2", "Memory 2");
        Memory m3 = _memoryManager.newMemory("IM3", "Memory 3");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test references
        m1.setValue("Turnout 1");
        assertEquals( "Turnout 1",
                ReferenceUtil.getReference(symbolTable, "{IM1}"), "Reference is correct");
        assertEquals( "Turnout 1",
                ReferenceUtil.getReference(symbolTable, "{  IM1  }"), "Reference is correct");

        m2.setValue("IM1");
        assertEquals( "IM1",
                ReferenceUtil.getReference(symbolTable, "{IM2}"), "Reference is correct");
        assertEquals( "IM1",
                ReferenceUtil.getReference(symbolTable, "{ IM2  }"), "Reference is correct");
        assertEquals( "Turnout 1",
                ReferenceUtil.getReference(symbolTable, "{{IM2}}"), "Reference is correct");
        assertEquals( "Turnout 1",
                ReferenceUtil.getReference(symbolTable, "{{   IM2 }}"), "Reference is correct");
        assertEquals( "Turnout 1",
                ReferenceUtil.getReference(symbolTable, "{ {   IM2 } }"), "Reference is correct");

        m3.setValue("IM2");
        assertEquals( "IM2",
                ReferenceUtil.getReference(symbolTable, "{IM3}"), "Reference is correct");
        assertEquals( "Turnout 1",
                ReferenceUtil.getReference(symbolTable, "{{{IM3}}}"), "Reference is correct");
    }

    @Test
    public void testTables() {

        Memory m1 = _memoryManager.newMemory("IM1", "Memory 1");
        Memory m15 = _memoryManager.newMemory("IM15", "Memory 15");
        Memory m333 = _memoryManager.newMemory("IM333", "Memory 333");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        yardTable.setCell(null, "Other turnout");

        assertNull( ReferenceUtil.getReference(symbolTable, "{Yard table[Other turnout]}"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{  Yard table[Other turnout]}"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{Yard table[  Other turnout]}"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{Yard table[Other turnout  ]}"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{Yard table[Other turnout]  }"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{  Yard table[  Other turnout  ]  }"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{Yard table[  Other turnout  ]}"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{Yard table[Other turnout,North yard]}"), "Reference is correct");

        assertNull( ReferenceUtil.getReference(symbolTable, "{  Yard table[Other turnout,North yard]  }"), "Reference is correct");

        assertNull( ReferenceUtil.getReference(symbolTable, "{Yard table[  Other turnout , North yard  ]}"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{  Yard table[  Other turnout   ,   North yard   ]  }"), "Reference is correct");
        assertNull( ReferenceUtil.getReference(symbolTable, "{  Yard table  [  Other turnout   ,   North yard   ]  }"), "Reference is correct");

        m1.setValue("Turnout 1");
        assertEquals( "Turnout 111",
                ReferenceUtil.getReference(symbolTable, "{Yard table[Left turnout]}"), "Reference is correct");
        assertEquals( "Turnout 111",
                ReferenceUtil.getReference(symbolTable, "{  Yard table  [ Left turnout  ]}"), "Reference is correct");
        assertEquals( "IT302",
                ReferenceUtil.getReference(symbolTable, "{Yard table[Rightmost turnout,South yard]}"), "Reference is correct");
        assertEquals( "IT302",
                ReferenceUtil.getReference(symbolTable, "{Yard table[  Rightmost turnout ,   South yard  ]}"), "Reference is correct");
        assertEquals( "Turnout 222",
                ReferenceUtil.getReference(symbolTable, "{Yard table[Rightmost turnout,North yard]}"), "Reference is correct");
        assertEquals( "Turnout 222",
                ReferenceUtil.getReference(symbolTable, "{Yard table[ Rightmost turnout  , North yard ]}"), "Reference is correct");

        // The line below reads 'Yard table[Rightmost turnout,East yard]' which
        // has the value IM15. And then reads the memory IM15 which has the value
        // 'Chicago north east'.
        m15.setValue("Chicago north east");
        assertEquals( "Chicago north east",
                ReferenceUtil.getReference(symbolTable, "{{Yard table[Rightmost turnout,East yard]}}"), "Reference is correct");
        assertEquals( "Chicago north east",
                ReferenceUtil.getReference(symbolTable, "{{Yard table[ Rightmost turnout  ,  East yard ]}}"), "Reference is correct");
        assertEquals( "Chicago north east",
                ReferenceUtil.getReference(symbolTable, "{{  Yard table   [ Rightmost turnout  ,  East yard ]}}"), "Reference is correct");

        // The line below reads the reference '{Turnout table[Yellow turnout]}'
        // which has the value 'Right turnout'. It then reads the reference
        // '{Other yard table[Turnouts,Green yard]}' which has the value
        // 'East yard'. It then reads the reference
        // '{Yard table[Right turnout,East yard]}' that has the value
        // 'Memory 333'. It then reads the reference '{Memory 333}' which has
        // the value 'Blue turnout'.
        m333.setValue("Blue turnout");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{{Yard table[{Turnout table[Yellow turnout]},{Other yard table[Turnouts,Green yard]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{{Yard table[{Turnout table[ Yellow turnout ]},{Other yard table[  Turnouts , Green yard  ]}]}}"), "Reference is correct");

        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{{Yard table[{Turnout table[ Yellow turnout ]},  {Other yard table[  Turnouts , Green yard  ]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{{Yard table[{Turnout table[ Yellow turnout ]}  ,{Other yard table[  Turnouts , Green yard  ]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{{Yard table[{Turnout table[ Yellow turnout ]}  ,  {Other yard table[  Turnouts , Green yard  ]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{{  Yard table [{Turnout table[ Yellow turnout ]}  ,  {Other yard table[  Turnouts , Green yard  ]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{  {  Yard table [{Turnout table[ Yellow turnout ]}  ,  {Other yard table[  Turnouts , Green yard  ]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{  {  Yard table [ {  Turnout table  [ Yellow turnout ]}  ,  {Other yard table[  Turnouts , Green yard  ]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{  {  Yard table [ {  Turnout table  [ Yellow turnout ]  }  ,  {Other yard table[  Turnouts , Green yard  ]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{  {  Yard table [ {  Turnout table  [ Yellow turnout ]  }  ,  {  Other yard table  [  Turnouts , Green yard  ]}]}}"), "Reference is correct");

        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{  {  Yard table [ {  Turnout table  [ Yellow turnout ]  }  ,  {  Other yard table  [  Turnouts , Green yard  ]}]}}"), "Reference is correct");
        assertEquals( "Blue turnout",
                ReferenceUtil.getReference(symbolTable,
                        "{  {  Yard table [ {  Turnout table  [ Yellow turnout ]  }  ,  {  Other yard table  [  Turnouts , Green yard  ]  }  ]  }  }"), "Reference is correct");

    }

    @Test
    public void testExceptions() {

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test exceptions
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "abc");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Reference 'abc' is not a valid reference");

        // Test exceptions
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Reference '{}' is not a valid reference");

        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{IM999}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Memory 'IM999' is not found");

        Memory m999 = _memoryManager.newMemory("IM999", "Memory 999");
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{IM999}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Memory 'IM999' has no value");

        m999.setValue("Turnout 1");
        assertEquals( "Turnout 1",
                ReferenceUtil.getReference(symbolTable, "{IM999}"), "Reference is correct");
    }

    @Test
    public void testExceptions2() {

        ReferenceUtil.IntRef endRef = new ReferenceUtil.IntRef();

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getValue("", 0, endRef);
        }, IllegalArgumentException.class, "Reference '' is not a valid reference");

        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference(symbolTable, "abc", 0, endRef);
        }, IllegalArgumentException.class, "Reference 'abc' is not a valid reference");

        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference(symbolTable, "{aaa", 0, endRef);
        }, IllegalArgumentException.class, "Reference '{aaa' is not a valid reference");

        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference(symbolTable, "{abc[1]1", 0, endRef);
        }, IllegalArgumentException.class, "Reference '{abc[1]1' is not a valid reference");

        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference(symbolTable, "{Some other table[1]}", 0, endRef);
        }, IllegalArgumentException.class, "Table 'Some other table' is not found");

        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference(symbolTable, "{Some other table[1,2]}", 0, endRef);
        }, IllegalArgumentException.class, "Table 'Some other table' is not found");
    }

    @Test
    public void testSpecialCharacters() {

        Memory m91 = _memoryManager.newMemory("IM91", "Memory , abc");
        m91.setValue("Turnout 91");
        Memory m92 = _memoryManager.newMemory("IM92", "Memory [ abc");
        m92.setValue("Turnout 92");
        Memory m93 = _memoryManager.newMemory("IM93", "Memory ] abc");
        m93.setValue("Turnout 93");
        Memory m94 = _memoryManager.newMemory("IM94", "Memory { abc");
        m94.setValue("Turnout 94");
        Memory m95 = _memoryManager.newMemory("IM95", "Memory } abc");
        m95.setValue("Turnout 95");
        Memory m96 = _memoryManager.newMemory("IM96", "Memory \\ abc");
        m96.setValue("Turnout 96");
        Memory m97 = _memoryManager.newMemory("IM97", "Memory ");
        m97.setValue("Turnout 97");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        // Test special characters. Special characters must be escaped.
        assertEquals( "Turnout 91",
                ReferenceUtil.getReference(symbolTable, "{Memory \\, abc}"),
                "Reference is correct");
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{Memory , abc}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Reference '{Memory , abc}' is not a valid reference");

        assertEquals( "Turnout 92",
                ReferenceUtil.getReference(symbolTable, "{Memory \\[ abc}"),
                "Reference is correct");
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{Memory [ abc}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Reference '{Memory [ abc}' is not a valid reference");

        assertEquals( "Turnout 93",
                ReferenceUtil.getReference(symbolTable, "{Memory \\] abc}"),
                "Reference is correct");
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{Memory ] abc}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Reference '{Memory ] abc}' is not a valid reference");

        assertEquals( "Turnout 94",
                ReferenceUtil.getReference(symbolTable, "{Memory \\{ abc}"),
                "Reference is correct");
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{Memory { abc}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Reference '{Memory { abc}' is not a valid reference");

        // This will try to find the "Memory ", which exists.
        assertEquals( "Turnout 95",
                ReferenceUtil.getReference(symbolTable, "{Memory \\} abc}"),
                "Reference is correct");
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{Memory } abc}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Reference '{Memory } abc}' is not a valid reference");

        assertEquals( "Turnout 96",
                ReferenceUtil.getReference(symbolTable, "{Memory \\\\ abc}"),
                "Reference is correct");
        // Note that 'Memory \ abc' has an escaped space, so the backspace disappears.
        expectException(() -> {
            var ref = ReferenceUtil.getReference(symbolTable, "{Memory \\ abc}");
            fail("should have thrown, created " + ref);
        }, IllegalArgumentException.class, "Memory 'Memory  abc' is not found");
    }

    private void setupTables()
        throws IOException
    {
        // Note that editors, like NetBeans, may insert spaces instead of tabs
        // when pressing the <Tab> key, which may be a problem when editing the
        // CSV table data below, since the separator between columns must be a
        // tab character.

        String yardTableData =
                "\tNorth yard\tEast yard\tSouth yard\tWest yard" + NEWLINE +
                "Leftmost turnout\tIT101\tIT201\tIT301\tIT401" + NEWLINE +
                "Left turnout\tTurnout 111\tIT203\tIT303\tIT403" + NEWLINE +
                "Right turnout\tIT104\tMemory 333\tIT304\tIT404" + NEWLINE +
                "Rightmost turnout\tTurnout 222\tIM15\tIT302\tIT402" + NEWLINE +
                "Other turnout\tIT1\tIT2\tIT3\tIT4" + NEWLINE;

        String turnoutTableData =
                "\tColumn" + NEWLINE +
                "Green turnout\tIT101" + NEWLINE +
                "Red turnout\tTurnout" + NEWLINE +
                "Yellow turnout\tRight turnout" + NEWLINE +
                "Blue turnout\tTurnout 222" + NEWLINE;

        String otherYardTableData =
                "\tYellow yard\tGreen yard\tBlue yard\tRed yard" + NEWLINE +
                "Turnouts\tWest yard\tEast yard\tIT301\tIT401" + NEWLINE +
                "Sensors\tTurnout 111\tIT203\tIT303\tIT403" + NEWLINE +
                "Lights\tIT104\tIT204\tIT304\tIT404" + NEWLINE;

        yardTable = _tableManager.loadTableFromCSVData("IQT1", "Yard table", yardTableData);
        _tableManager.loadTableFromCSVData("IQT2", "Turnout table", turnoutTableData);
        _tableManager.loadTableFromCSVData("IQT3", "Other yard table", otherYardTableData);
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        _memoryManager = InstanceManager.getDefault(MemoryManager.class);
        _tableManager = InstanceManager.getDefault(NamedTableManager.class);
        setupTables();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReferenceUtilTest.class);
}
