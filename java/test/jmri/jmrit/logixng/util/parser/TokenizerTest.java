package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Tokenizer
 * 
 * @author Daniel Bergqvist 2019
 */
public class TokenizerTest {

    private void checkFirstToken(
            List<Token> tokens,
            TokenType tokenType, String string) {
        
        assertFalse( tokens.isEmpty(), "list is not empty");
//        System.out.format("Type: %s, String: '%s'%n", tokens.get(0).getTokenType(), tokens.get(0).getString());
        assertEquals( tokenType, tokens.get(0).getTokenType(), "token type matches");
        assertEquals( string, tokens.get(0).getString(), "string matches");
        
        tokens.remove(0);
    }
    
    @Test
    public void testGetTokens() throws InvalidSyntaxException {

        List<Token> tokens;

        tokens = Tokenizer.getTokens("");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("R1ABC");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1ABC");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("R1ABC");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1ABC");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("321");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "321");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("32.221");
        checkFirstToken(tokens, TokenType.FLOATING_NUMBER, "32.221");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("321 353");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "321");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "353");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("321   353");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "321");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "353");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("321354");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "321354");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("\"A_String\"");
        checkFirstToken(tokens, TokenType.STRING, "A_String");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("\"A_Str\\\"ing\"");
        checkFirstToken(tokens, TokenType.STRING, "A_Str\"ing");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("\"A_Str\\\\ing\"");
        checkFirstToken(tokens, TokenType.STRING, "A_Str\\ing");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("\"A string\"");
        checkFirstToken(tokens, TokenType.STRING, "A string");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("1223 \"A string\"");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1223");
        checkFirstToken(tokens, TokenType.STRING, "A string");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("   \"A string\" 1234");
        checkFirstToken(tokens, TokenType.STRING, "A string");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1234");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("1223*\"A string\"");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1223");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.STRING, "A string");
        assertTrue( tokens.isEmpty(), "list is empty");

        InvalidSyntaxException e = assertThrows( InvalidSyntaxException.class, () ->
            Tokenizer.getTokens("\"A string\"1234"), "exception is thrown");
        assertEquals( "Invalid syntax at index 9", e.getMessage(), "exception message matches");


        tokens = Tokenizer.getTokens("\"A string\"*232");
        checkFirstToken(tokens, TokenType.STRING, "A string");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "232");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("1223+\"A string\"*232");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1223");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.STRING, "A string");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "232");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("1223 \"A string\"/\" \"");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1223");
        checkFirstToken(tokens, TokenType.STRING, "A string");
        checkFirstToken(tokens, TokenType.DIVIDE, "/");
        checkFirstToken(tokens, TokenType.STRING, " ");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("\"A string\"+\"Another string\"");
        checkFirstToken(tokens, TokenType.STRING, "A string");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.STRING, "Another string");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("(");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens(")");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        assertTrue( tokens.isEmpty(), "list is empty");

        tokens = Tokenizer.getTokens("(R1)");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("&&");
        checkFirstToken(tokens, TokenType.BOOLEAN_AND, "&");    // The second & is eaten by the parser and not included in the _string.
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("R1 && R2");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1");
        checkFirstToken(tokens, TokenType.BOOLEAN_AND, "&");    // The second & is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R2");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("!(12 < 2)");
        checkFirstToken(tokens, TokenType.BOOLEAN_NOT, "!");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        checkFirstToken(tokens, TokenType.LESS_THAN, "<");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "2");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("R1(x)");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("R1[x]");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("{x,y,z}[a]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("{x,y,z}[a..b]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a");
        checkFirstToken(tokens, TokenType.DOT_DOT, ".");    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "b");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("{x,y,z}[a..b,c]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a");
        checkFirstToken(tokens, TokenType.DOT_DOT, ".");    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "b");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "c");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("{x,y,z}[a,b..c]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "b");
        checkFirstToken(tokens, TokenType.DOT_DOT, ".");    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "c");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("{x,y,z}[a,b..c,d,e,f..g]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "b");
        checkFirstToken(tokens, TokenType.DOT_DOT, ".");    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "c");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "d");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "e");
        checkFirstToken(tokens, TokenType.COMMA, ",");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "f");
        checkFirstToken(tokens, TokenType.DOT_DOT, ".");    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "g");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("(R1(x))");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        tokens = Tokenizer.getTokens("R1(x)*(y+21.2)-2.12/R12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.FLOATING_NUMBER, "21.2");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        checkFirstToken(tokens, TokenType.SUBTRACKT, "-");
        checkFirstToken(tokens, TokenType.FLOATING_NUMBER, "2.12");
        checkFirstToken(tokens, TokenType.DIVIDE, "/");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R12");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("MyVar.MyField");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar");
        checkFirstToken(tokens, TokenType.DOT, ".");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyField");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("MyVar.MyMethod()");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar");
        checkFirstToken(tokens, TokenType.DOT, ".");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyMethod");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("MyVar.MyMethod(myParam)");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar");
        checkFirstToken(tokens, TokenType.DOT, ".");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyMethod");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myParam");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        
        
        
        
        tokens = Tokenizer.getTokens("myVar = 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar += 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.ASSIGN_ADD, "+");         // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar -= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.ASSIGN_SUBTRACKT, "-");   // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar *= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.ASSIGN_MULTIPLY, "*");    // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar /= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.ASSIGN_DIVIDE, "/");      // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar %= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.ASSIGN_MODULO, "%");      // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("++myVar");
        checkFirstToken(tokens, TokenType.INCREMENT, "+");          // The plus sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("--myVar");
        checkFirstToken(tokens, TokenType.DECREMENT, "-");          // The minus sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar++");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.INCREMENT, "+");          // The plus sign is eaten by the parser and not included in the _string.
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar--");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.DECREMENT, "-");          // The minus sign is eaten by the parser and not included in the _string.
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myList[12] = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myList[12] = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myList[myIndex] = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myList[myIndex] = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myMap{myKey} = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myMap{myKey} = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myMap{\"SomeKey\"} = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.STRING, "SomeKey");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myMap{\"SomeKey\"} = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.STRING, "SomeKey");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt");
        checkFirstToken(tokens, TokenType.MULTIPLY, "*");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("\"Hello\" + myList[12] + myList[myIndex] + myMap{myKey} + myMap{\"SomeKey\"}");
        checkFirstToken(tokens, TokenType.STRING, "Hello");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.STRING, "SomeKey");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
        tokens = Tokenizer.getTokens("myVar = \"Hello\" + myList[12] + myList[myIndex] + myMap{myKey} + myMap{\"SomeKey\"}");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar");
        checkFirstToken(tokens, TokenType.ASSIGN, "=");
        checkFirstToken(tokens, TokenType.STRING, "Hello");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList");
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex");
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        checkFirstToken(tokens, TokenType.ADD, "+");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{");
        checkFirstToken(tokens, TokenType.STRING, "SomeKey");
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}");
        assertTrue( tokens.isEmpty(), "list is empty");
        
        
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
