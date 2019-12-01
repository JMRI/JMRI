package jmri.jmrit.logixng.util;

import java.util.List;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.util.parser.InvalidSyntaxException;
import jmri.jmrit.logixng.util.parser.Token;
import jmri.jmrit.logixng.util.parser.TokenType;
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

    private MemoryManager memoryManager;
    
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
            Assert.assertEquals("Exception is correct", exceptionClass, e.getClass());
            Assert.assertTrue("Exception is correct", e.getClass() == exceptionClass);
            Assert.assertEquals("Exception message is correct", errorMessage, e.getMessage());
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception is thrown", exceptionThrown);
    }
    
    @Test
    public void testGetReference() {
        
        Memory m1 = memoryManager.newMemory("IM1", "Memory 1");
        Memory m2 = memoryManager.newMemory("IM2", "Memory 2");
        Memory m3 = memoryManager.newMemory("IM3", "Memory 3");
        Memory m4 = memoryManager.newMemory("IM4", "Memory 4");
        Memory m5 = memoryManager.newMemory("IM5", "Memory 5");
        Memory m6 = memoryManager.newMemory("IM6", "Memory 6");
        Memory m7 = memoryManager.newMemory("IM7", "Memory 7");
        Memory m8 = memoryManager.newMemory("IM8", "Memory 8");
        Memory m9 = memoryManager.newMemory("IM9", "Memory 9");
        Memory m10 = memoryManager.newMemory("IM10", "Memory 10");
        Memory m11 = memoryManager.newMemory("IM11", "Memory 11");
        Memory m12 = memoryManager.newMemory("IM12", "Memory 12");
        
        
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
        
        Memory m1 = memoryManager.newMemory("IM1", "Memory 1");
        Memory m2 = memoryManager.newMemory("IM2", "Memory 2");
        Memory m3 = memoryManager.newMemory("IM3", "Memory 3");
        Memory m4 = memoryManager.newMemory("IM4", "Memory 4");
        Memory m5 = memoryManager.newMemory("IM5", "Memory 5");
        Memory m6 = memoryManager.newMemory("IM6", "Memory 6");
        Memory m7 = memoryManager.newMemory("IM7", "Memory 7");
        Memory m8 = memoryManager.newMemory("IM8", "Memory 8");
        Memory m9 = memoryManager.newMemory("IM9", "Memory 9");
        Memory m10 = memoryManager.newMemory("IM10", "Memory 10");
        Memory m11 = memoryManager.newMemory("IM11", "Memory 11");
        Memory m12 = memoryManager.newMemory("IM12", "Memory 12");
        
        
        ReferenceUtil ru = new ReferenceUtil();
        
        // Test references
        m1.setValue("Turnout 1");
        Assert.assertEquals("Reference is correct", "Turnout 111", ru.getReference("{Yard table[Turnout 1]}"));
        Assert.assertEquals("Reference is correct", "Turnout 222", ru.getReference("{Yard table[Turnout 1,Green yard]}"));
        
    }
    
    @Test
    public void testExceptions() {
        
        Memory m1 = memoryManager.newMemory("IM1", "Memory 1");
        
        ReferenceUtil ru = new ReferenceUtil();
        
        // Test exceptions
        expectException(() -> {
            ru.getReference("{}");
        }, IllegalArgumentException.class, "Reference '{}' is not a valid reference");
        
        expectException(() -> {
            ru.getReference("{IM999}");
        }, IllegalArgumentException.class, "Memory 'IM999' is not found");
        
        Memory m999 = memoryManager.newMemory("IM999", "Memory 999");
        expectException(() -> {
            ru.getReference("{IM999}");
        }, IllegalArgumentException.class, "Memory 'IM999' has no value");
        
        m999.setValue("Turnout 1");
        Assert.assertEquals("Reference is correct", "Turnout 1", ru.getReference("{IM999}"));
    }
    
    @Ignore
    @Test
    public void testSpecialCharacters() {
        
        Memory m91 = memoryManager.newMemory("IM91", "Memory , abc");
        m91.setValue("Turnout 91");
        Memory m92 = memoryManager.newMemory("IM92", "Memory [ abc");
        m92.setValue("Turnout 92");
        Memory m93 = memoryManager.newMemory("IM93", "Memory ] abc");
        m93.setValue("Turnout 93");
        Memory m94 = memoryManager.newMemory("IM94", "Memory { abc");
        m94.setValue("Turnout 94");
        Memory m95 = memoryManager.newMemory("IM95", "Memory } abc");
        m95.setValue("Turnout 95");
        Memory m96 = memoryManager.newMemory("IM96", "Memory \\ abc");
        m96.setValue("Turnout 96");
        
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
        
        Assert.assertEquals("Reference is correct", "Turnout 95", ru.getReference("{Memory \\} abc}"));
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
        
        
        Assert.assertTrue("Precedence function is correct", TokenType.BOOLEAN_AND.hasLowerPrecedence(TokenType.EQUAL));
        
        
        
        
        
        
        Assert.assertTrue("Precedence function is correct", TokenType.BOOLEAN_AND.hasLowerPrecedence(TokenType.EQUAL));
        Assert.assertTrue("Precedence function is correct", !TokenType.EQUAL.hasLowerPrecedence(TokenType.BOOLEAN_AND));
        Assert.assertTrue("Precedence function is correct", TokenType.EQUAL.hasHigherPrecedence(TokenType.BOOLEAN_AND));
        Assert.assertTrue("Precedence function is correct", !TokenType.BOOLEAN_AND.hasHigherPrecedence(TokenType.EQUAL));
        Assert.assertTrue("Precedence function is correct", TokenType.EQUAL.hasSamePrecedence(TokenType.NOT_EQUAL));
        Assert.assertTrue("Precedence function is correct", !TokenType.EQUAL.hasSamePrecedence(TokenType.BOOLEAN_AND));
        Assert.assertTrue("Precedence function is correct", TokenType.NOT_EQUAL.hasSamePrecedence(TokenType.EQUAL));
        Assert.assertTrue("Precedence function is correct", !TokenType.BOOLEAN_AND.hasSamePrecedence(TokenType.EQUAL));
        
        // Test precedence
        Assert.assertTrue("Precedence is correct", TokenType.COMMA.hasLowerPrecedence(TokenType.DOT_DOT));
        Assert.assertTrue("Precedence is correct", TokenType.DOT_DOT.hasLowerPrecedence(TokenType.BOOLEAN_OR));
        
        Assert.assertTrue("Precedence is correct", TokenType.BOOLEAN_OR.hasLowerPrecedence(TokenType.BOOLEAN_AND));
        Assert.assertTrue("Precedence is correct", TokenType.BOOLEAN_AND.hasLowerPrecedence(TokenType.BINARY_OR));
        Assert.assertTrue("Precedence is correct", TokenType.BINARY_OR.hasLowerPrecedence(TokenType.BINARY_XOR));
        Assert.assertTrue("Precedence is correct", TokenType.BINARY_XOR.hasLowerPrecedence(TokenType.BINARY_AND));
        Assert.assertTrue("Precedence is correct", TokenType.BINARY_AND.hasLowerPrecedence(TokenType.EQUAL));
        
        Assert.assertTrue("Precedence is correct", TokenType.EQUAL.hasSamePrecedence(TokenType.NOT_EQUAL));
        Assert.assertTrue("Precedence is correct", TokenType.EQUAL.hasLowerPrecedence(TokenType.LESS_THAN));
        
        Assert.assertTrue("Precedence is correct", TokenType.LESS_THAN.hasSamePrecedence(TokenType.LESS_OR_EQUAL));
        Assert.assertTrue("Precedence is correct", TokenType.LESS_THAN.hasSamePrecedence(TokenType.GREATER_THAN));
        Assert.assertTrue("Precedence is correct", TokenType.LESS_THAN.hasSamePrecedence(TokenType.GREATER_OR_EQUAL));
        Assert.assertTrue("Precedence is correct", TokenType.LESS_THAN.hasLowerPrecedence(TokenType.SHIFT_LEFT));
        
        Assert.assertTrue("Precedence is correct", TokenType.SHIFT_LEFT.hasSamePrecedence(TokenType.SHIFT_RIGHT));
        Assert.assertTrue("Precedence is correct", TokenType.SHIFT_LEFT.hasLowerPrecedence(TokenType.ADD));
        
        Assert.assertTrue("Precedence is correct", TokenType.ADD.hasSamePrecedence(TokenType.SUBTRACKT));
        Assert.assertTrue("Precedence is correct", TokenType.ADD.hasLowerPrecedence(TokenType.MULTIPLY));
        
        Assert.assertTrue("Precedence is correct", TokenType.MULTIPLY.hasSamePrecedence(TokenType.DIVIDE));
        Assert.assertTrue("Precedence is correct", TokenType.MULTIPLY.hasSamePrecedence(TokenType.MODULO));
        Assert.assertTrue("Precedence is correct", TokenType.MULTIPLY.hasLowerPrecedence(TokenType.BINARY_NOT));
        
        Assert.assertTrue("Precedence is correct", TokenType.MULTIPLY.hasLowerPrecedence(TokenType.BINARY_NOT));
        
        Assert.assertTrue("Precedence is correct", TokenType.BINARY_NOT.hasSamePrecedence(TokenType.BOOLEAN_NOT));
        Assert.assertTrue("Precedence is correct", TokenType.BINARY_NOT.hasLowerPrecedence(TokenType.LEFT_PARENTHESIS));
        
        Assert.assertTrue("Precedence is correct", TokenType.LEFT_PARENTHESIS.hasSamePrecedence(TokenType.RIGHT_PARENTHESIS));
        Assert.assertTrue("Precedence is correct", TokenType.LEFT_PARENTHESIS.hasSamePrecedence(TokenType.LEFT_SQUARE_BRACKET));
        Assert.assertTrue("Precedence is correct", TokenType.LEFT_PARENTHESIS.hasSamePrecedence(TokenType.RIGHT_SQUARE_BRACKET));
        Assert.assertTrue("Precedence is correct", TokenType.LEFT_PARENTHESIS.hasSamePrecedence(TokenType.LEFT_CURLY_BRACKET));
        Assert.assertTrue("Precedence is correct", TokenType.LEFT_PARENTHESIS.hasSamePrecedence(TokenType.RIGHT_CURLY_BRACKET));
        Assert.assertTrue("Precedence is correct", TokenType.LEFT_PARENTHESIS.hasLowerPrecedence(TokenType.IDENTIFIER));
        
        Assert.assertTrue("Precedence is correct", TokenType.IDENTIFIER.hasSamePrecedence(TokenType.FLOATING_NUMBER));
        Assert.assertTrue("Precedence is correct", TokenType.IDENTIFIER.hasSamePrecedence(TokenType.STRING));
    }
    
    private void checkFirstToken(
            List<Token> tokens,
            TokenType tokenType, String string) {
        
        Assert.assertTrue("list is not empty", tokens.size() > 0);
//        System.out.format("Type: %s, String: '%s'%n", tokens.get(0).getTokenType(), tokens.get(0).getString());
        Assert.assertTrue("token type matches", tokens.get(0).getTokenType() == tokenType);
        Assert.assertTrue("string matches", string.equals(tokens.get(0).getString()));
        
        tokens.remove(0);
    }
    
    @Test
    public void testGetTokens() throws InvalidSyntaxException {
        
        
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        memoryManager = InstanceManager.getDefault(MemoryManager.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
