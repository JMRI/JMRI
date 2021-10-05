package jmri.jmrit.logixng.util.parser;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test Tokenizer
 * 
 * @author Daniel Bergqvist 2019
 */
public class TokenTypeTest {

    @Test
    public void testLeaf() {
        Assert.assertFalse("TokenType is leaf", TokenType.ERROR.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.SAME_AS_LAST.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.NONE.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.SPACE.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.COMMA.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.DOT_DOT.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.BOOLEAN_OR.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.BOOLEAN_AND.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.BINARY_OR.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.BINARY_XOR.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.BINARY_AND.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.EQUAL.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.NOT_EQUAL.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.LESS_THAN.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.LESS_OR_EQUAL.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.GREATER_THAN.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.GREATER_OR_EQUAL.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.SHIFT_LEFT.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.SHIFT_RIGHT.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.ADD.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.SUBTRACKT.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.MULTIPLY.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.DIVIDE.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.MODULO.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.BOOLEAN_NOT.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.BINARY_NOT.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.LEFT_PARENTHESIS.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.RIGHT_PARENTHESIS.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.LEFT_SQUARE_BRACKET.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.RIGHT_SQUARE_BRACKET.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.LEFT_CURLY_BRACKET.isLeaf());
        Assert.assertFalse("TokenType is leaf", TokenType.RIGHT_CURLY_BRACKET.isLeaf());
        Assert.assertTrue("TokenType is leaf", TokenType.IDENTIFIER.isLeaf());
        Assert.assertTrue("TokenType is leaf", TokenType.INTEGER_NUMBER.isLeaf());
        Assert.assertTrue("TokenType is leaf", TokenType.FLOATING_NUMBER.isLeaf());
        Assert.assertTrue("TokenType is leaf", TokenType.STRING.isLeaf());
    }
    
    @Test
    public void testTokenType() {
        // Test precedence functions
        
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
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenTypeTest.class);
}
