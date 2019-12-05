package jmri.jmrit.logixng.util;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test ReferenceUtil
 * 
 * @author Daniel Bergqvist 2019
 */
public class ReferenceUtilTest {

    private MemoryManager _memoryManager;
    private NamedTableManager _tableManager;
    
    // The system appropriate newline separator.
//    private static final String _nl = System.getProperty("line.separator"); // NOI18N
//    private static final String _nl = "\r";
//    private static final String _nl = "\n";
    private static final String _nl = "\r\n";
    
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
            Assert.assertTrue("Exception is correct", e.getClass() == exceptionClass);
            Assert.assertEquals("Exception message is correct", errorMessage, e.getMessage());
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception is thrown", exceptionThrown);
    }
    
    @Test
    public void testGetReference() {
        
        Memory m1 = _memoryManager.newMemory("IM1", "Memory 1");
        Memory m2 = _memoryManager.newMemory("IM2", "Memory 2");
        Memory m3 = _memoryManager.newMemory("IM3", "Memory 3");
        Memory m4 = _memoryManager.newMemory("IM4", "Memory 4");
        Memory m5 = _memoryManager.newMemory("IM5", "Memory 5");
        Memory m6 = _memoryManager.newMemory("IM6", "Memory 6");
        Memory m7 = _memoryManager.newMemory("IM7", "Memory 7");
        Memory m8 = _memoryManager.newMemory("IM8", "Memory 8");
        Memory m9 = _memoryManager.newMemory("IM9", "Memory 9");
        Memory m10 = _memoryManager.newMemory("IM10", "Memory 10");
        Memory m11 = _memoryManager.newMemory("IM11", "Memory 11");
        Memory m12 = _memoryManager.newMemory("IM12", "Memory 12");
        
        
        ReferenceUtil ru = new ReferenceUtil();
        
        // Test references
        m1.setValue("Turnout 1");
        Assert.assertEquals("Reference is correct", "Turnout 1", ru.getReference("{IM1}"));
        
        m2.setValue("IM1");
        Assert.assertEquals("Reference is correct", "IM1", ru.getReference("{IM2}"));
        Assert.assertEquals("Reference is correct", "Turnout 1", ru.getReference("{{IM2}}"));
        
        m3.setValue("IM2");
        Assert.assertEquals("Reference is correct", "IM2", ru.getReference("{IM3}"));
        Assert.assertEquals("Reference is correct", "Turnout 1", ru.getReference("{{{IM3}}}"));
    }
    
    @Test
    public void testTables() {
        // IM1 = "{Yard table[Turnout 2,Sensor1]}
        
        Memory m1 = _memoryManager.newMemory("IM1", "Memory 1");
        Memory m2 = _memoryManager.newMemory("IM2", "Memory 2");
        Memory m3 = _memoryManager.newMemory("IM3", "Memory 3");
        Memory m4 = _memoryManager.newMemory("IM4", "Memory 4");
        Memory m5 = _memoryManager.newMemory("IM5", "Memory 5");
        Memory m6 = _memoryManager.newMemory("IM6", "Memory 6");
        Memory m7 = _memoryManager.newMemory("IM7", "Memory 7");
        Memory m8 = _memoryManager.newMemory("IM8", "Memory 8");
        Memory m9 = _memoryManager.newMemory("IM9", "Memory 9");
        Memory m10 = _memoryManager.newMemory("IM10", "Memory 10");
        Memory m11 = _memoryManager.newMemory("IM11", "Memory 11");
        Memory m12 = _memoryManager.newMemory("IM12", "Memory 12");
        Memory m15 = _memoryManager.newMemory("IM15", "Memory 15");
        
        
        ReferenceUtil ru = new ReferenceUtil();
        
        // Test references
        m1.setValue("Turnout 1");
        Assert.assertEquals("Reference is correct",
                "Turnout 111",
                ru.getReference("{Yard table[Left turnout]}"));
        Assert.assertEquals("Reference is correct",
                "IT302",
                ru.getReference("{Yard table[Rightmost turnout,South yard]}"));
        Assert.assertEquals("Reference is correct",
                "Turnout 222",
                ru.getReference("{Yard table[Rightmost turnout,North yard]}"));
        
        // The line below reads 'Yard table[Rightmost turnout,East yard]' which
        // has the value IM15. And then reads the memory IM15 which has the value
        // 'Chicago north east'.
        m15.setValue("Chicago north east");
        Assert.assertEquals("Reference is correct",
                "Chicago north east",
                ru.getReference("{{Yard table[Rightmost turnout,East yard]}}"));
        
    }
    
    @Ignore
    @Test
    public void testExceptions() {
        
        Memory m1 = _memoryManager.newMemory("IM1", "Memory 1");
        
        ReferenceUtil ru = new ReferenceUtil();
        
        // Test exceptions
        expectException(() -> {
            ru.getReference("{}");
        }, IllegalArgumentException.class, "Reference '{}' is not a valid reference");
        
        expectException(() -> {
            ru.getReference("{IM999}");
        }, IllegalArgumentException.class, "Memory 'IM999' is not found");
        
        Memory m999 = _memoryManager.newMemory("IM999", "Memory 999");
        expectException(() -> {
            ru.getReference("{IM999}");
        }, IllegalArgumentException.class, "Memory 'IM999' has no value");
        
        m999.setValue("Turnout 1");
        Assert.assertEquals("Reference is correct", "Turnout 1", ru.getReference("{IM999}"));
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
        
        ReferenceUtil ru = new ReferenceUtil();
        
        // Test special characters. Special characters must be escaped.
        Assert.assertEquals("Reference is correct", "Turnout 91", ru.getReference("{Memory \\, abc}"));
        expectException(() -> {
            ru.getReference("{Memory , abc}");
        }, IllegalArgumentException.class, "Reference '{Memory , abc}' is not a valid reference");
        
        Assert.assertEquals("Reference is correct", "Turnout 92", ru.getReference("{Memory \\[ abc}"));
        expectException(() -> {
            ru.getReference("{Memory [ abc}");
        }, IllegalArgumentException.class, "Reference '{Memory [ abc}' is not a valid reference");
        
        Assert.assertEquals("Reference is correct", "Turnout 93", ru.getReference("{Memory \\] abc}"));
        expectException(() -> {
            ru.getReference("{Memory ] abc}");
        }, IllegalArgumentException.class, "Reference '{Memory ] abc}' is not a valid reference");
        
        Assert.assertEquals("Reference is correct", "Turnout 94", ru.getReference("{Memory \\{ abc}"));
        expectException(() -> {
            ru.getReference("{Memory { abc}");
        }, IllegalArgumentException.class, "Reference '{Memory { abc}' is not a valid reference");
        
        // This will try to find the "Memory ", which exists.
        Assert.assertEquals("Reference is correct", "Turnout 95", ru.getReference("{Memory \\} abc}"));
//        ru.getReference("{Memory } abc}");
        expectException(() -> {
            ru.getReference("{Memory } abc}");
        }, IllegalArgumentException.class, "Reference '{Memory } abc}' is not a valid reference");
        
        Assert.assertEquals("Reference is correct", "Turnout 96", ru.getReference("{Memory \\\\ abc}"));
        // Note that 'Memory \ abc' has an escaped space, so the backspace disappears.
        expectException(() -> {
            ru.getReference("{Memory \\ abc}");
        }, IllegalArgumentException.class, "Memory 'Memory  abc' is not found");
        
        
        
        
        
        // {Signal 1}
        // {Signal 1,2}     // Bad!
        // {Signal 1\,2}    // Fine!
        // Signal 1,2       // Bad
        // {{Memory 1}}     // Memory 1 => Memory 2 => Value
        
        
        // Yard table[1,2]
        // Yard table[Signal 1,Signal2]
        // Yard table[{Memory1},{Memory2}]
        // {Memory3}[{Memory1},{Memory2}]
        // {{Memory4}}[{Memory1},{Memory2}]
        // {{Memory4}}[{{{Memory1}}},{{{Memory2}}}]
        // {{{Memory4}}[{{{Memory1}}},{{{Memory2}}}]}
        // {Memory7}[{{{Memory4}}[{{{Memory1}}},{{{Memory2}}}]},{{{Memory4}}[{{{Memory1}}},{{{Memory2}}}]}]
        // {{Memory7}[{{{Memory4}}[{{{Memory1}}},{{{Memory2}}}]},{{{Memory4}}[{{{Memory1}}},{{{Memory2}}}]}]}
        
        
    }
    
    private void setupTables() {
        String yardTableData =
                "	Yard table\n" +
                "	North yard	East yard	South yard	West yard" + _nl +
                "Leftmost turnout	IT101	IT201	IT301	IT401" + _nl +
                "Left turnout	Turnout 111	IT203	IT303	IT403" + _nl +
                "Right turnout	IT104	IT204	IT304	IT404" + _nl +
                "Rightmost turnout	Turnout 222	IM15	IT302	IT402" + _nl;
        
        _tableManager.loadTableFromCSV(yardTableData);
//        NamedTable yardTable = tableManager.newTable(yardTableData);
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
    
}
