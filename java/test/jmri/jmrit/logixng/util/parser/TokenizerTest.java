package jmri.jmrit.logixng.util.parser;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Tokenizer
 *
 * @author Daniel Bergqvist 2019
 */
public class TokenizerTest {

    private void checkFirstToken(
            List<Token> tokens,
            TokenType tokenType,
            String string,
            int start,
            int end) {

        Assert.assertTrue("list is not empty", tokens.size() > 0);
        Assert.assertEquals("token type matches", tokenType, tokens.get(0).getTokenType());
        Assert.assertEquals("string matches", string, tokens.get(0).getString());

        if (start != 0 || end != 0) {
            Assert.assertEquals("start matches", start, tokens.get(0).getPos());
            Assert.assertEquals("end matches", end, tokens.get(0).getEndPos());
        }

        tokens.remove(0);
    }

    @Test
    public void testGetTokens() throws InvalidSyntaxException {

        List<Token> tokens;
        AtomicBoolean exceptionIsThrown = new AtomicBoolean();

        tokens = Tokenizer.getTokens("");
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("R1ABC");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1ABC", 0, 5);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("321");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "321", 0, 3);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("32.221");
        checkFirstToken(tokens, TokenType.FLOATING_NUMBER, "32.221", 0, 6);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("321 353");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "321", 0, 3);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "353", 4, 7);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("321   353");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "321", 0, 3);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "353", 6, 9);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("321354");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "321354", 0, 6);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("\"A_String\"*aaa");
        checkFirstToken(tokens, TokenType.STRING, "A_String", 0, 10);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 10, 11);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "aaa", 11, 14);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("\"A_String\" * aaa");
        checkFirstToken(tokens, TokenType.STRING, "A_String", 0, 10);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 11, 12);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "aaa", 13, 16);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("\"A_String\"     *        aaa");
        checkFirstToken(tokens, TokenType.STRING, "A_String", 0, 10);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 15, 16);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "aaa", 24, 27);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("\"A_String\"");
        checkFirstToken(tokens, TokenType.STRING, "A_String", 0, 10);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("\"A_Str\\\"ing\"");
        checkFirstToken(tokens, TokenType.STRING, "A_Str\"ing", 0, 12);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("\"A_Str\\\\ing\"");
        checkFirstToken(tokens, TokenType.STRING, "A_Str\\ing", 0, 12);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("\"A string\"");
        checkFirstToken(tokens, TokenType.STRING, "A string", 0, 10);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("1223 \"A string\"");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1223", 0, 4);
        checkFirstToken(tokens, TokenType.STRING, "A string", 5, 15);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("   \"A string\" 1234");
        checkFirstToken(tokens, TokenType.STRING, "A string", 3, 13);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1234", 14, 18);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("1223*\"A string\"");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1223", 0, 4);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 4, 5);
        checkFirstToken(tokens, TokenType.STRING, "A string", 5, 15);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        exceptionIsThrown.set(false);
        try {
            Tokenizer.getTokens("\"A string\"1234");
        } catch (InvalidSyntaxException e) {
            Assert.assertTrue("exception message matches", "Invalid syntax error".equals(e.getMessage()));
            exceptionIsThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", exceptionIsThrown.get());

        tokens = Tokenizer.getTokens("\"A string\"*232");
        checkFirstToken(tokens, TokenType.STRING, "A string", 0, 10);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 10, 11);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "232", 11, 14);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("1223+\"A string\"*232");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1223", 0, 4);
        checkFirstToken(tokens, TokenType.ADD, "+", 4, 5);
        checkFirstToken(tokens, TokenType.STRING, "A string", 5, 15);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 15, 16);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "232", 16, 19);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("1223 \"A string\"/\" \"");
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "1223", 0, 4);
        checkFirstToken(tokens, TokenType.STRING, "A string", 5, 15);
        checkFirstToken(tokens, TokenType.DIVIDE, "/", 15, 16);
        checkFirstToken(tokens, TokenType.STRING, " ", 16, 19);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("\"A string\"+\"Another string\"");
        checkFirstToken(tokens, TokenType.STRING, "A string", 0, 10);
        checkFirstToken(tokens, TokenType.ADD, "+", 10, 11);
        checkFirstToken(tokens, TokenType.STRING, "Another string", 11, 27);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("(");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 1);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens(")");
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 0, 1);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("(R1)");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 1);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1", 1, 3);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 3, 4);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("&&");
        checkFirstToken(tokens, TokenType.BOOLEAN_AND, "&", 0, 2);    // The second & is eaten by the parser and not included in the _string.
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("R1 && R2");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1", 0, 2);
        checkFirstToken(tokens, TokenType.BOOLEAN_AND, "&", 3, 5);    // The second & is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R2", 6, 8);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("!(12 < 2)");
        checkFirstToken(tokens, TokenType.BOOLEAN_NOT, "!", 0, 1);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 1, 2);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 2, 4);
        checkFirstToken(tokens, TokenType.LESS_THAN, "<", 5, 6);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "2", 7, 8);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 8, 9);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("R1(x)");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1", 0, 2);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 2, 3);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 3, 4);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 4, 5);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("R1[x]");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1", 0, 2);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 2, 3);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 3, 4);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 4, 5);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("{x,y,z}[a]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 1);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 1, 2);
        checkFirstToken(tokens, TokenType.COMMA, ",", 2, 3);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y", 3, 4);
        checkFirstToken(tokens, TokenType.COMMA, ",", 4, 5);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z", 5, 6);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 6, 7);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 7, 8);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a", 8, 9);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 9, 10);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("{x,y,z}[a..b]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 1);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 1, 2);
        checkFirstToken(tokens, TokenType.COMMA, ",", 2, 3);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y", 3, 4);
        checkFirstToken(tokens, TokenType.COMMA, ",", 4, 5);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z", 5, 6);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 6, 7);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 7, 8);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a", 8, 9);
        checkFirstToken(tokens, TokenType.DOT_DOT, ".", 9, 11);    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "b", 11, 12);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 12, 13);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("{x,y,z}[a..b,c]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 1);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 1, 2);
        checkFirstToken(tokens, TokenType.COMMA, ",", 2, 3);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y", 3, 4);
        checkFirstToken(tokens, TokenType.COMMA, ",", 4, 5);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z", 5, 6);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 6, 7);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 7, 8);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a", 8, 9);
        checkFirstToken(tokens, TokenType.DOT_DOT, ".", 9, 11);    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "b", 11, 12);
        checkFirstToken(tokens, TokenType.COMMA, ",", 12, 13);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "c", 13, 14);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 14, 15);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("{x,y,z}[a,b..c]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 1);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 1, 2);
        checkFirstToken(tokens, TokenType.COMMA, ",", 2, 3);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y", 3, 4);
        checkFirstToken(tokens, TokenType.COMMA, ",", 4, 5);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z", 5, 6);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 6, 7);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 7, 8);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a", 8, 9);
        checkFirstToken(tokens, TokenType.COMMA, ",", 9, 10);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "b", 10, 11);
        checkFirstToken(tokens, TokenType.DOT_DOT, ".", 11, 13);    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "c", 13, 14);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 14, 15);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("{x,y,z}[a,b..c,d,e,f..g]");
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 1);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 1, 2);
        checkFirstToken(tokens, TokenType.COMMA, ",", 2, 3);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y", 3, 4);
        checkFirstToken(tokens, TokenType.COMMA, ",", 4, 5);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "z", 5, 6);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 6, 7);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 7, 8);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "a", 8, 9);
        checkFirstToken(tokens, TokenType.COMMA, ",", 9, 10);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "b", 10, 11);
        checkFirstToken(tokens, TokenType.DOT_DOT, ".", 11, 13);    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "c", 13, 14);
        checkFirstToken(tokens, TokenType.COMMA, ",", 14, 15);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "d", 15, 16);
        checkFirstToken(tokens, TokenType.COMMA, ",", 16, 17);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "e", 17, 18);
        checkFirstToken(tokens, TokenType.COMMA, ",", 18, 19);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "f", 19, 20);
        checkFirstToken(tokens, TokenType.DOT_DOT, ".", 20, 22);    // The second dot is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "g", 22, 23);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 23, 24);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("(R1(x))");
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("R1(x)*(y+21.2)-2.12/R12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 0, 0);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.FLOATING_NUMBER, "21.2", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 0, 0);
        checkFirstToken(tokens, TokenType.SUBTRACKT, "-", 0, 0);
        checkFirstToken(tokens, TokenType.FLOATING_NUMBER, "2.12", 0, 0);
        checkFirstToken(tokens, TokenType.DIVIDE, "/", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R12", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("MyVar.MyField");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar", 0, 0);
        checkFirstToken(tokens, TokenType.DOT, ".", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyField", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("MyVar.MyMethod()");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar", 0, 0);
        checkFirstToken(tokens, TokenType.DOT, ".", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyMethod", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("MyVar.MyMethod(myParam)");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar", 0, 0);
        checkFirstToken(tokens, TokenType.DOT, ".", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyMethod", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myParam", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());






        tokens = Tokenizer.getTokens("myVar = 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar += 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN_ADD, "+", 0, 0);         // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar -= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN_SUBTRACKT, "-", 0, 0);   // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar *= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN_MULTIPLY, "*", 0, 0);    // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar /= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN_DIVIDE, "/", 0, 0);      // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar %= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN_MODULO, "%", 0, 0);      // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 0, 0);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("++myVar");
        checkFirstToken(tokens, TokenType.INCREMENT, "+", 0, 0);          // The plus sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("--myVar");
        checkFirstToken(tokens, TokenType.DECREMENT, "-", 0, 0);          // The minus sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar++");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.INCREMENT, "+", 0, 0);          // The plus sign is eaten by the parser and not included in the _string.
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar--");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.DECREMENT, "-", 0, 0);          // The minus sign is eaten by the parser and not included in the _string.
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myList[12] = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myList[12] = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 0, 0);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myList[myIndex] = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myList[myIndex] = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 0, 0);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myMap{myKey} = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myMap{myKey} = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 0, 0);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myMap{\"SomeKey\"} = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 0);
        checkFirstToken(tokens, TokenType.STRING, "SomeKey", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myMap{\"SomeKey\"} = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 0);
        checkFirstToken(tokens, TokenType.STRING, "SomeKey", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 0, 0);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("\"Hello\" + myList[12] + myList[myIndex] + myMap{myKey} + myMap{\"SomeKey\"}");
        checkFirstToken(tokens, TokenType.STRING, "Hello", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 0);
        checkFirstToken(tokens, TokenType.STRING, "SomeKey", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar = \"Hello\" + myList[12] + myList[myIndex] + myMap{myKey} + myMap{\"SomeKey\"}");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 0);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 0, 0);
        checkFirstToken(tokens, TokenType.STRING, "Hello", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 0, 0);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 0, 0);
        checkFirstToken(tokens, TokenType.ADD, "+", 0, 0);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 0);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 0, 0);
        checkFirstToken(tokens, TokenType.STRING, "SomeKey", 0, 0);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 0, 0);
        Assert.assertTrue("list is empty", tokens.isEmpty());


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

}
