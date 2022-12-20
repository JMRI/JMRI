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

        Assert.assertEquals("start matches", start, tokens.get(0).getPos());
        Assert.assertEquals("end matches", end, tokens.get(0).getEndPos());

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
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 0, 1);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1", 1, 3);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 3, 4);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 4, 5);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 5, 6);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 6, 7);
        Assert.assertTrue("list is empty", tokens.isEmpty());

        tokens = Tokenizer.getTokens("R1(x)*(y+21.2)-2.12/R12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R1", 0, 2);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 2, 3);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "x", 3, 4);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 4, 5);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 5, 6);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 6, 7);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "y", 7, 8);
        checkFirstToken(tokens, TokenType.ADD, "+", 8, 9);
        checkFirstToken(tokens, TokenType.FLOATING_NUMBER, "21.2", 9, 13);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 13, 14);
        checkFirstToken(tokens, TokenType.SUBTRACKT, "-", 14, 15);
        checkFirstToken(tokens, TokenType.FLOATING_NUMBER, "2.12", 15, 19);
        checkFirstToken(tokens, TokenType.DIVIDE, "/", 19, 20);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "R12", 20, 23);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("MyVar.MyField");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar", 0, 5);
        checkFirstToken(tokens, TokenType.DOT, ".", 5, 6);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyField", 6, 13);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("MyVar.MyMethod()");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar", 0, 5);
        checkFirstToken(tokens, TokenType.DOT, ".", 5, 6);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyMethod", 6, 14);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 14, 15);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 15, 16);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("MyVar.MyMethod(myParam)");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyVar", 0, 5);
        checkFirstToken(tokens, TokenType.DOT, ".", 5, 6);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "MyMethod", 6, 14);
        checkFirstToken(tokens, TokenType.LEFT_PARENTHESIS, "(", 14, 15);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myParam", 15, 22);
        checkFirstToken(tokens, TokenType.RIGHT_PARENTHESIS, ")", 22, 23);
        Assert.assertTrue("list is empty", tokens.isEmpty());






        tokens = Tokenizer.getTokens("myVar = 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 6, 7);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 8, 10);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar += 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.ASSIGN_ADD, "+", 6, 8);         // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 9, 11);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar -= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.ASSIGN_SUBTRACKT, "-", 6, 8);   // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 9, 11);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar *= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.ASSIGN_MULTIPLY, "*", 6, 8);    // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 9, 11);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar /= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.ASSIGN_DIVIDE, "/", 6, 8);      // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 9, 11);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar %= 12");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.ASSIGN_MODULO, "%", 6, 8);      // The equal sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 9, 11);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 6, 7);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 8, 13);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 14, 15);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 16, 23);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("++myVar");
        checkFirstToken(tokens, TokenType.INCREMENT, "+", 0, 2);          // The plus sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 2, 7);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("--myVar");
        checkFirstToken(tokens, TokenType.DECREMENT, "-", 0, 2);          // The minus sign is eaten by the parser and not included in the _string.
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 2, 7);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar++");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.INCREMENT, "+", 5, 7);          // The plus sign is eaten by the parser and not included in the _string.
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar--");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.DECREMENT, "-", 5, 7);          // The minus sign is eaten by the parser and not included in the _string.
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myList[12] = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 6);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 6, 7);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 7, 9);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 9, 10);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 11, 12);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23", 13, 15);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myList[12] = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 6);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 6, 7);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 7, 9);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 9, 10);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 11, 12);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 13, 18);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 19, 20);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 21, 28);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myList[myIndex] = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 6);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 6, 7);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex", 7, 14);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 14, 15);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 16, 17);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23", 18, 20);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myList[myIndex] = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 0, 6);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 6, 7);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex", 7, 14);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 14, 15);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 16, 17);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 18, 23);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 24, 25);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 26, 33);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myMap{myKey} = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 5);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 5, 6);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey", 6, 11);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 11, 12);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 13, 14);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23", 15, 17);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myMap{myKey} = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 5);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 5, 6);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey", 6, 11);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 11, 12);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 13, 14);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 15, 20);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 21, 22);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 23, 30);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myMap{\"SomeKey\"} = 23");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 5);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 5, 6);
        checkFirstToken(tokens, TokenType.STRING, "SomeKey", 6, 15);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 15, 16);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 17, 18);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "23", 19, 21);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myMap{\"SomeKey\"} = myInt * myFloat");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 0, 5);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 5, 6);
        checkFirstToken(tokens, TokenType.STRING, "SomeKey", 6, 15);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 15, 16);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 17, 18);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myInt", 19, 24);
        checkFirstToken(tokens, TokenType.MULTIPLY, "*", 25, 26);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myFloat", 27, 34);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("\"Hello\" + myList[12] + myList[myIndex] + myMap{myKey} + myMap{\"SomeKey\"}");
        checkFirstToken(tokens, TokenType.STRING, "Hello", 0, 7);
        checkFirstToken(tokens, TokenType.ADD, "+", 8, 9);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 10, 16);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 16, 17);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 17, 19);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 19, 20);
        checkFirstToken(tokens, TokenType.ADD, "+", 21, 22);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 23, 29);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 29, 30);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex", 30, 37);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 37, 38);
        checkFirstToken(tokens, TokenType.ADD, "+", 39, 40);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 41, 46);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 46, 47);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey", 47, 52);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 52, 53);
        checkFirstToken(tokens, TokenType.ADD, "+", 54, 55);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 56, 61);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 61, 62);
        checkFirstToken(tokens, TokenType.STRING, "SomeKey", 62, 71);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 71, 72);
        Assert.assertTrue("list is empty", tokens.isEmpty());


        tokens = Tokenizer.getTokens("myVar = \"Hello\" + myList[12] + myList[myIndex] + myMap{myKey} + myMap{\"SomeKey\"}");
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myVar", 0, 5);
        checkFirstToken(tokens, TokenType.ASSIGN, "=", 6, 7);
        checkFirstToken(tokens, TokenType.STRING, "Hello", 8, 15);
        checkFirstToken(tokens, TokenType.ADD, "+", 16, 17);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 18, 24);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 24, 25);
        checkFirstToken(tokens, TokenType.INTEGER_NUMBER, "12", 25, 27);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 27, 28);
        checkFirstToken(tokens, TokenType.ADD, "+", 29, 30);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myList", 31, 37);
        checkFirstToken(tokens, TokenType.LEFT_SQUARE_BRACKET, "[", 37, 38);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myIndex", 38, 45);
        checkFirstToken(tokens, TokenType.RIGHT_SQUARE_BRACKET, "]", 45, 46);
        checkFirstToken(tokens, TokenType.ADD, "+", 47, 48);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 49, 54);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 54, 55);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myKey", 55, 60);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 60, 61);
        checkFirstToken(tokens, TokenType.ADD, "+", 62, 63);
        checkFirstToken(tokens, TokenType.IDENTIFIER, "myMap", 64, 69);
        checkFirstToken(tokens, TokenType.LEFT_CURLY_BRACKET, "{", 69, 70);
        checkFirstToken(tokens, TokenType.STRING, "SomeKey", 70, 79);
        checkFirstToken(tokens, TokenType.RIGHT_CURLY_BRACKET, "}", 79, 80);
        Assert.assertTrue("list is empty", tokens.isEmpty());


    }

    @Test
    public void testDaniel() throws InvalidSyntaxException {

        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("(a-b) + c*d");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("(a-b)+!c");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("(a-bbb)+!c?d:e");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("(a-bbb)+!c?d:(eee+f*ggggg)");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("(a-bbb)+!c?d:(eee+f*ggggg*hhhhhhhhhhh+jjjjjjjjjjj+fffffff)");


/*
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("12");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("12+31");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("12345+31");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("12*31+2");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("12+31*23");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("12345+31*23");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("(12+31)*(23-1)");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("(12345+31)*(23-1)");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("(12345+312345)*(23-1)");
        new jmri.jmrit.logixng.util.swing.FormulaDiagram().showDiagram("12+31*(23-1)+((9*2+3)-2)/23");
*/
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
