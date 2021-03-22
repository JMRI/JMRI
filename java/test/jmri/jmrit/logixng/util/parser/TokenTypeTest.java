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
    
    // Check if 'tokenType' can follow every token type in list and if
    // 'tokenType' can not follow any token type not in list
    private boolean checkCanFollow(TokenType tokenType, TokenType[] canFollow) {
        EnumSet<TokenType> canNotFollow = EnumSet.allOf(TokenType.class);
        for (TokenType tt : canFollow) {
            canNotFollow.remove(tt);
            if (!tokenType.canFollow(tt)) {
                log.error("TokenType {} can follow {}", tokenType.name(), tt.name());
                return false;
            }
        }
        
        for (TokenType tt : canNotFollow) {
            if (tokenType.canFollow(tt)) {
                log.error("TokenType {} cannot follow {}", tokenType.name(), tt.name());
                return false;
            }
        }
        
        return true;
    }
    
/*  
    // This method generates the code for the method testCanFollow() below.
    @Test
    public void testDaniel() {
        int count=0;
        for (TokenType tt : TokenType.values()) {
            List<TokenType> list = new ArrayList<>();
            for (TokenType tt2 : TokenType.values()) {
                if (tt.canFollow(tt2)) list.add(tt2);
            }
            System.out.format("        TokenType[] ttArray%d = {", ++count);
            for (int i=0; i < list.size(); i++) {
                if (i > 0) System.out.print(", ");
                System.out.print("TokenType." + list.get(i));
            }
            System.out.println("};");
            System.out.format("        Assert.assertTrue(\"canFollow\", checkCanFollow(TokenType.%s, ttArray%d));%n", tt.name(), count);
        }
    }
*/    
    
    @Test
    public void testCanFollow() {
        // If previous token is null, canFollow() returns true for every
        // token type.
        for (TokenType tt : TokenType.values()) {
            Assert.assertTrue("canFollow", tt.canFollow(null));
        }
        
        TokenType[] ttArray1 = {};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.ERROR, ttArray1));
        TokenType[] ttArray2 = {};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.SAME_AS_LAST, ttArray2));
        TokenType[] ttArray3 = {};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.NONE, ttArray3));
        TokenType[] ttArray4 = {};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.SPACE, ttArray4));
        TokenType[] ttArray5 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.COMMA, ttArray5));
        TokenType[] ttArray6 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.DOT_DOT, ttArray6));
        TokenType[] ttArray7 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.BOOLEAN_OR, ttArray7));
        TokenType[] ttArray8 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.BOOLEAN_AND, ttArray8));
        TokenType[] ttArray9 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.BINARY_OR, ttArray9));
        TokenType[] ttArray10 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.BINARY_XOR, ttArray10));
        TokenType[] ttArray11 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.BINARY_AND, ttArray11));
        TokenType[] ttArray12 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.EQUAL, ttArray12));
        TokenType[] ttArray13 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.NOT_EQUAL, ttArray13));
        TokenType[] ttArray14 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.LESS_THAN, ttArray14));
        TokenType[] ttArray15 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.LESS_OR_EQUAL, ttArray15));
        TokenType[] ttArray16 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.GREATER_THAN, ttArray16));
        TokenType[] ttArray17 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.GREATER_OR_EQUAL, ttArray17));
        TokenType[] ttArray18 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.SHIFT_LEFT, ttArray18));
        TokenType[] ttArray19 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.SHIFT_RIGHT, ttArray19));
        TokenType[] ttArray20 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.ADD, ttArray20));
        TokenType[] ttArray21 = {TokenType.ERROR, TokenType.SAME_AS_LAST, TokenType.NONE, TokenType.SPACE, TokenType.COMMA,
            TokenType.DOT_DOT, TokenType.BOOLEAN_OR, TokenType.BOOLEAN_AND, TokenType.BINARY_OR, TokenType.BINARY_XOR,
            TokenType.BINARY_AND, TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS_THAN, TokenType.LESS_OR_EQUAL,
            TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.SHIFT_LEFT, TokenType.SHIFT_RIGHT, TokenType.ADD,
            TokenType.SUBTRACKT, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO, TokenType.BOOLEAN_NOT,
            TokenType.BINARY_NOT, TokenType.LEFT_PARENTHESIS, TokenType.RIGHT_PARENTHESIS, TokenType.LEFT_SQUARE_BRACKET,
            TokenType.RIGHT_SQUARE_BRACKET, TokenType.LEFT_CURLY_BRACKET, TokenType.RIGHT_CURLY_BRACKET, TokenType.IDENTIFIER,
            TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING,
            TokenType.ASSIGN, TokenType.ASSIGN_ADD, TokenType.ASSIGN_SUBTRACKT, TokenType.ASSIGN_MULTIPLY, TokenType.ASSIGN_DIVIDE,
            TokenType.ASSIGN_MODULO, TokenType.TERNARY_QUESTION_MARK, TokenType.TERNARY_COLON,
        };
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.SUBTRACKT, ttArray21));
        TokenType[] ttArray22 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.MULTIPLY, ttArray22));
        TokenType[] ttArray23 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.DIVIDE, ttArray23));
        TokenType[] ttArray24 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.MODULO, ttArray24));
        TokenType[] ttArray25 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.BOOLEAN_NOT, ttArray25));
        TokenType[] ttArray26 = {TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.BINARY_NOT, ttArray26));
        TokenType[] ttArray27 = {TokenType.ERROR, TokenType.SAME_AS_LAST, TokenType.NONE, TokenType.SPACE, TokenType.COMMA,
            TokenType.DOT_DOT, TokenType.BOOLEAN_OR, TokenType.BOOLEAN_AND, TokenType.BINARY_OR, TokenType.BINARY_XOR,
            TokenType.BINARY_AND, TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS_THAN, TokenType.LESS_OR_EQUAL,
            TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.SHIFT_LEFT, TokenType.SHIFT_RIGHT, TokenType.ADD,
            TokenType.SUBTRACKT, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO, TokenType.BOOLEAN_NOT,
            TokenType.BINARY_NOT, TokenType.LEFT_PARENTHESIS, TokenType.RIGHT_PARENTHESIS, TokenType.LEFT_SQUARE_BRACKET,
            TokenType.RIGHT_SQUARE_BRACKET, TokenType.LEFT_CURLY_BRACKET, TokenType.RIGHT_CURLY_BRACKET, TokenType.IDENTIFIER,
            TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING,
            TokenType.ASSIGN, TokenType.ASSIGN_ADD, TokenType.ASSIGN_SUBTRACKT, TokenType.ASSIGN_MULTIPLY, TokenType.ASSIGN_DIVIDE,
            TokenType.ASSIGN_MODULO, TokenType.TERNARY_QUESTION_MARK, TokenType.TERNARY_COLON,
        };
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.LEFT_PARENTHESIS, ttArray27));
        TokenType[] ttArray28 = {TokenType.LEFT_PARENTHESIS, TokenType.RIGHT_PARENTHESIS, TokenType.RIGHT_SQUARE_BRACKET,
            TokenType.RIGHT_CURLY_BRACKET, TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER,
            TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.RIGHT_PARENTHESIS, ttArray28));
        TokenType[] ttArray29 = {TokenType.RIGHT_PARENTHESIS, TokenType.RIGHT_CURLY_BRACKET, TokenType.IDENTIFIER,
            TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.LEFT_SQUARE_BRACKET, ttArray29));
        TokenType[] ttArray30 = {TokenType.RIGHT_PARENTHESIS, TokenType.LEFT_SQUARE_BRACKET, TokenType.RIGHT_SQUARE_BRACKET,
            TokenType.RIGHT_CURLY_BRACKET, TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER,
            TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.RIGHT_SQUARE_BRACKET, ttArray30));
        TokenType[] ttArray31 = {TokenType.ERROR, TokenType.SAME_AS_LAST, TokenType.NONE, TokenType.SPACE, TokenType.COMMA,
            TokenType.DOT_DOT, TokenType.BOOLEAN_OR, TokenType.BOOLEAN_AND, TokenType.BINARY_OR, TokenType.BINARY_XOR,
            TokenType.BINARY_AND, TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS_THAN, TokenType.LESS_OR_EQUAL,
            TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.SHIFT_LEFT, TokenType.SHIFT_RIGHT, TokenType.ADD,
            TokenType.SUBTRACKT, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO, TokenType.BOOLEAN_NOT,
            TokenType.BINARY_NOT, TokenType.LEFT_PARENTHESIS, TokenType.RIGHT_PARENTHESIS, TokenType.LEFT_SQUARE_BRACKET,
            TokenType.RIGHT_SQUARE_BRACKET, TokenType.LEFT_CURLY_BRACKET, TokenType.RIGHT_CURLY_BRACKET, TokenType.IDENTIFIER,
            TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER, TokenType.STRING,
            TokenType.ASSIGN, TokenType.ASSIGN_ADD, TokenType.ASSIGN_SUBTRACKT, TokenType.ASSIGN_MULTIPLY, TokenType.ASSIGN_DIVIDE,
            TokenType.ASSIGN_MODULO, TokenType.TERNARY_QUESTION_MARK, TokenType.TERNARY_COLON,
        };
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.LEFT_CURLY_BRACKET, ttArray31));
        TokenType[] ttArray32 = {TokenType.RIGHT_PARENTHESIS, TokenType.RIGHT_SQUARE_BRACKET, TokenType.LEFT_CURLY_BRACKET,
            TokenType.RIGHT_CURLY_BRACKET, TokenType.IDENTIFIER, TokenType.INTEGER_NUMBER, TokenType.FLOATING_NUMBER,
            TokenType.STRING};
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.RIGHT_CURLY_BRACKET, ttArray32));
        TokenType[] ttArray33 = {TokenType.ERROR, TokenType.SAME_AS_LAST, TokenType.NONE, TokenType.SPACE, TokenType.COMMA,
            TokenType.DOT_DOT, TokenType.BOOLEAN_OR, TokenType.BOOLEAN_AND, TokenType.BINARY_OR, TokenType.BINARY_XOR,
            TokenType.BINARY_AND, TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS_THAN, TokenType.LESS_OR_EQUAL,
            TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.SHIFT_LEFT, TokenType.SHIFT_RIGHT, TokenType.ADD,
            TokenType.SUBTRACKT, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO, TokenType.BOOLEAN_NOT,
            TokenType.BINARY_NOT, TokenType.LEFT_PARENTHESIS, TokenType.RIGHT_PARENTHESIS, TokenType.LEFT_SQUARE_BRACKET,
            TokenType.RIGHT_SQUARE_BRACKET, TokenType.LEFT_CURLY_BRACKET, TokenType.RIGHT_CURLY_BRACKET,
            TokenType.ASSIGN, TokenType.ASSIGN_ADD, TokenType.ASSIGN_SUBTRACKT, TokenType.ASSIGN_MULTIPLY, TokenType.ASSIGN_DIVIDE,
            TokenType.ASSIGN_MODULO, TokenType.TERNARY_QUESTION_MARK, TokenType.TERNARY_COLON,
        };
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.IDENTIFIER, ttArray33));
        TokenType[] ttArray34 = {TokenType.ERROR, TokenType.SAME_AS_LAST, TokenType.NONE, TokenType.SPACE, TokenType.COMMA,
            TokenType.DOT_DOT, TokenType.BOOLEAN_OR, TokenType.BOOLEAN_AND, TokenType.BINARY_OR, TokenType.BINARY_XOR,
            TokenType.BINARY_AND, TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS_THAN, TokenType.LESS_OR_EQUAL,
            TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.SHIFT_LEFT, TokenType.SHIFT_RIGHT, TokenType.ADD,
            TokenType.SUBTRACKT, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO, TokenType.BOOLEAN_NOT,
            TokenType.BINARY_NOT, TokenType.LEFT_PARENTHESIS, TokenType.RIGHT_PARENTHESIS, TokenType.LEFT_SQUARE_BRACKET,
            TokenType.RIGHT_SQUARE_BRACKET, TokenType.LEFT_CURLY_BRACKET, TokenType.RIGHT_CURLY_BRACKET,
            TokenType.ASSIGN, TokenType.ASSIGN_ADD, TokenType.ASSIGN_SUBTRACKT, TokenType.ASSIGN_MULTIPLY, TokenType.ASSIGN_DIVIDE,
            TokenType.ASSIGN_MODULO, TokenType.TERNARY_QUESTION_MARK, TokenType.TERNARY_COLON,
        };
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.INTEGER_NUMBER, ttArray34));
        TokenType[] ttArray35 = {TokenType.ERROR, TokenType.SAME_AS_LAST, TokenType.NONE, TokenType.SPACE, TokenType.COMMA,
            TokenType.DOT_DOT, TokenType.BOOLEAN_OR, TokenType.BOOLEAN_AND, TokenType.BINARY_OR, TokenType.BINARY_XOR,
            TokenType.BINARY_AND, TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS_THAN, TokenType.LESS_OR_EQUAL,
            TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.SHIFT_LEFT, TokenType.SHIFT_RIGHT, TokenType.ADD,
            TokenType.SUBTRACKT, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO, TokenType.BOOLEAN_NOT,
            TokenType.BINARY_NOT, TokenType.LEFT_PARENTHESIS, TokenType.RIGHT_PARENTHESIS, TokenType.LEFT_SQUARE_BRACKET,
            TokenType.RIGHT_SQUARE_BRACKET, TokenType.LEFT_CURLY_BRACKET, TokenType.RIGHT_CURLY_BRACKET,
            TokenType.ASSIGN, TokenType.ASSIGN_ADD, TokenType.ASSIGN_SUBTRACKT, TokenType.ASSIGN_MULTIPLY, TokenType.ASSIGN_DIVIDE,
            TokenType.ASSIGN_MODULO, TokenType.TERNARY_QUESTION_MARK, TokenType.TERNARY_COLON,
        };
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.FLOATING_NUMBER, ttArray35));
        TokenType[] ttArray36 = {TokenType.ERROR, TokenType.SAME_AS_LAST, TokenType.NONE, TokenType.SPACE, TokenType.COMMA,
            TokenType.DOT_DOT, TokenType.BOOLEAN_OR, TokenType.BOOLEAN_AND, TokenType.BINARY_OR, TokenType.BINARY_XOR,
            TokenType.BINARY_AND, TokenType.EQUAL, TokenType.NOT_EQUAL, TokenType.LESS_THAN, TokenType.LESS_OR_EQUAL,
            TokenType.GREATER_THAN, TokenType.GREATER_OR_EQUAL, TokenType.SHIFT_LEFT, TokenType.SHIFT_RIGHT, TokenType.ADD,
            TokenType.SUBTRACKT, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO, TokenType.BOOLEAN_NOT,
            TokenType.BINARY_NOT, TokenType.LEFT_PARENTHESIS, TokenType.RIGHT_PARENTHESIS, TokenType.LEFT_SQUARE_BRACKET,
            TokenType.RIGHT_SQUARE_BRACKET, TokenType.LEFT_CURLY_BRACKET, TokenType.RIGHT_CURLY_BRACKET,
            TokenType.ASSIGN, TokenType.ASSIGN_ADD, TokenType.ASSIGN_SUBTRACKT, TokenType.ASSIGN_MULTIPLY, TokenType.ASSIGN_DIVIDE,
            TokenType.ASSIGN_MODULO, TokenType.TERNARY_QUESTION_MARK, TokenType.TERNARY_COLON,
        };
        Assert.assertTrue("canFollow", checkCanFollow(TokenType.STRING, ttArray36));
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
