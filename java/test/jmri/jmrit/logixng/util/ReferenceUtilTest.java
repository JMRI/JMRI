package jmri.jmrit.logixng.util;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test ReferenceUtil
 * 
 * @author Daniel Bergqvist 2019
 */
public class ReferenceUtilTest {

    private MemoryManager _memoryManager;
    private NamedTableManager _tableManager;
    
    // The system appropriate newline separator.
    private static final String _nl = System.getProperty("line.separator"); // NOI18N
    
    @Test
    public void testCtor() {
        ReferenceUtil t = new ReferenceUtil();
        Assert.assertNotNull("not null", t);
    }
    
    private void expectException(Runnable r, Class<? extends Exception> exceptionClass, String errorMessage) {
        boolean exceptionThrown = false;
        try {
            r.run();
        } catch (Exception e) {
            if (e.getClass() != exceptionClass) {
                log.error("Expected exception {}, found exception {}",
                        e.getClass().getName(), exceptionClass.getName());
            }
            Assert.assertTrue("Exception is correct", e.getClass() == exceptionClass);
            Assert.assertEquals("Exception message is correct", errorMessage, e.getMessage());
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception is thrown", exceptionThrown);
    }
    
    @Test
    public void testIsReference() {
        Assert.assertFalse("Is reference", ReferenceUtil.isReference("{}"));
        Assert.assertTrue("Is reference", ReferenceUtil.isReference("{A}"));
        Assert.assertTrue("Is reference", ReferenceUtil.isReference("{Abc 123}"));
        Assert.assertFalse("Is reference", ReferenceUtil.isReference("{"));
        Assert.assertFalse("Is reference", ReferenceUtil.isReference("}"));
        Assert.assertFalse("Is reference", ReferenceUtil.isReference("{Abc"));
        Assert.assertFalse("Is reference", ReferenceUtil.isReference("Abc}"));
        Assert.assertFalse("Is reference", ReferenceUtil.isReference("Abc"));
    }
    
    @Test
    public void testGetReference() {
        
        Memory m1 = _memoryManager.newMemory("IM1", "Memory 1");
        Memory m2 = _memoryManager.newMemory("IM2", "Memory 2");
        Memory m3 = _memoryManager.newMemory("IM3", "Memory 3");
        
        // Test references
        m1.setValue("Turnout 1");
        Assert.assertEquals("Reference is correct",
                "Turnout 1",
                ReferenceUtil.getReference("{IM1}"));
        
        m2.setValue("IM1");
        Assert.assertEquals("Reference is correct",
                "IM1",
                ReferenceUtil.getReference("{IM2}"));
        Assert.assertEquals("Reference is correct",
                "Turnout 1",
                ReferenceUtil.getReference("{{IM2}}"));
        
        m3.setValue("IM2");
        Assert.assertEquals("Reference is correct",
                "IM2",
                ReferenceUtil.getReference("{IM3}"));
        Assert.assertEquals("Reference is correct",
                "Turnout 1",
                ReferenceUtil.getReference("{{{IM3}}}"));
    }
    
    @Test
    public void testTables() {
        // IM1 = "{Yard table[Turnout 2,Sensor1]}
        
        Memory m1 = _memoryManager.newMemory("IM1", "Memory 1");
        Memory m15 = _memoryManager.newMemory("IM15", "Memory 15");
        Memory m333 = _memoryManager.newMemory("IM333", "Memory 333");
        
        // Test references
        m1.setValue("Turnout 1");
        Assert.assertEquals("Reference is correct",
                "Turnout 111",
                ReferenceUtil.getReference("{Yard table[Left turnout]}"));
        Assert.assertEquals("Reference is correct",
                "IT302",
                ReferenceUtil.getReference("{Yard table[Rightmost turnout,South yard]}"));
        Assert.assertEquals("Reference is correct",
                "Turnout 222",
                ReferenceUtil.getReference("{Yard table[Rightmost turnout,North yard]}"));
        
        // The line below reads 'Yard table[Rightmost turnout,East yard]' which
        // has the value IM15. And then reads the memory IM15 which has the value
        // 'Chicago north east'.
        m15.setValue("Chicago north east");
        Assert.assertEquals("Reference is correct",
                "Chicago north east",
                ReferenceUtil.getReference("{{Yard table[Rightmost turnout,East yard]}}"));
        
        // The line below reads the reference '{Turnout table[Yellow turnout]}'
        // which has the value 'Right turnout'. It then reads the reference
        // '{Other yard table[Turnouts,Green yard]}' which has the value
        // 'East yard'. It then reads the reference
        // '{Yard table[Right turnout,East yard]}' that has the value
        // 'Memory 333'. It then reads the reference '{Memory 333}' which has
        // the value 'Blue turnout'.
        m333.setValue("Blue turnout");
        Assert.assertEquals("Reference is correct",
                "Blue turnout",
                ReferenceUtil.getReference(
                        "{{Yard table[{Turnout table[Yellow turnout]},{Other yard table[Turnouts,Green yard]}]}}"));
    }
    
    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")  // This method test thrown exceptions
    public void testExceptions() {
        
        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference("abc");
        }, IllegalArgumentException.class, "Reference 'abc' is not a valid reference");
        
        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference("{}");
        }, IllegalArgumentException.class, "Reference '{}' is not a valid reference");
        
        expectException(() -> {
            ReferenceUtil.getReference("{IM999}");
        }, IllegalArgumentException.class, "Memory 'IM999' is not found");
        
        Memory m999 = _memoryManager.newMemory("IM999", "Memory 999");
        expectException(() -> {
            ReferenceUtil.getReference("{IM999}");
        }, IllegalArgumentException.class, "Memory 'IM999' has no value");
        
        m999.setValue("Turnout 1");
        Assert.assertEquals("Reference is correct",
                "Turnout 1",
                ReferenceUtil.getReference("{IM999}"));
    }
    
    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")  // This method test thrown exceptions
    public void testExceptions2() {
        
        ReferenceUtil.IntRef endRef = new ReferenceUtil.IntRef();
        
        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference("abc", 0, endRef);
        }, IllegalArgumentException.class, "Reference 'abc' is not a valid reference");
        
        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference("{aaa", 0, endRef);
        }, IllegalArgumentException.class, "Reference '{aaa' is not a valid reference");
        
        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference("{Some other table[1]}", 0, endRef);
        }, IllegalArgumentException.class, "Table 'Some other table' is not found");
        
        // Test exceptions
        expectException(() -> {
            ReferenceUtil.getReference("{Some other table[1,2]}", 0, endRef);
        }, IllegalArgumentException.class, "Table 'Some other table' is not found");
    }
    
    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")  // This method test thrown exceptions
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
        
        // Test special characters. Special characters must be escaped.
        Assert.assertEquals("Reference is correct",
                "Turnout 91",
                ReferenceUtil.getReference("{Memory \\, abc}"));
        expectException(() -> {
            ReferenceUtil.getReference("{Memory , abc}");
        }, IllegalArgumentException.class, "Reference '{Memory , abc}' is not a valid reference");
        
        Assert.assertEquals("Reference is correct",
                "Turnout 92",
                ReferenceUtil.getReference("{Memory \\[ abc}"));
        expectException(() -> {
            ReferenceUtil.getReference("{Memory [ abc}");
        }, IllegalArgumentException.class, "Reference '{Memory [ abc}' is not a valid reference");
        
        Assert.assertEquals("Reference is correct",
                "Turnout 93",
                ReferenceUtil.getReference("{Memory \\] abc}"));
        expectException(() -> {
            ReferenceUtil.getReference("{Memory ] abc}");
        }, IllegalArgumentException.class, "Reference '{Memory ] abc}' is not a valid reference");
        
        Assert.assertEquals("Reference is correct",
                "Turnout 94",
                ReferenceUtil.getReference("{Memory \\{ abc}"));
        expectException(() -> {
            ReferenceUtil.getReference("{Memory { abc}");
        }, IllegalArgumentException.class, "Reference '{Memory { abc}' is not a valid reference");
        
        // This will try to find the "Memory ", which exists.
        Assert.assertEquals("Reference is correct",
                "Turnout 95",
                ReferenceUtil.getReference("{Memory \\} abc}"));
        expectException(() -> {
            ReferenceUtil.getReference("{Memory } abc}");
        }, IllegalArgumentException.class, "Reference '{Memory } abc}' is not a valid reference");
        
        Assert.assertEquals("Reference is correct",
                "Turnout 96",
                ReferenceUtil.getReference("{Memory \\\\ abc}"));
        // Note that 'Memory \ abc' has an escaped space, so the backspace disappears.
        expectException(() -> {
            ReferenceUtil.getReference("{Memory \\ abc}");
        }, IllegalArgumentException.class, "Memory 'Memory  abc' is not found");
    }
    
    private void setupTables() {
        String yardTableData =
                "	Yard table\n" +
                "	North yard	East yard	South yard	West yard" + _nl +
                "Leftmost turnout	IT101	IT201	IT301	IT401" + _nl +
                "Left turnout	Turnout 111	IT203	IT303	IT403" + _nl +
                "Right turnout	IT104	Memory 333	IT304	IT404" + _nl +
                "Rightmost turnout	Turnout 222	IM15	IT302	IT402" + _nl;
        
        String turnoutTableData =
                "	Turnout table\n" +
                "	Column" + _nl +
                "Green turnout	IT101" + _nl +
                "Red turnout	Turnout" + _nl +
                "Yellow turnout	Right turnout" + _nl +
                "Blue turnout	Turnout 222" + _nl;
        
        String otherYardTableData =
                "	Other yard table\n" +
                "	Yellow yard	Green yard	Blue yard	Red yard" + _nl +
                "Turnouts	West yard	East yard	IT301	IT401" + _nl +
                "Sensors	Turnout 111	IT203	IT303	IT403" + _nl +
                "Lights	IT104	IT204	IT304	IT404" + _nl;
        
        _tableManager.loadTableFromCSV(yardTableData);
        _tableManager.loadTableFromCSV(turnoutTableData);
        _tableManager.loadTableFromCSV(otherYardTableData);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        _memoryManager = InstanceManager.getDefault(MemoryManager.class);
        _tableManager = InstanceManager.getDefault(NamedTableManager.class);
        setupTables();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    private final static Logger log = LoggerFactory.getLogger(ReferenceUtilTest.class);
}
