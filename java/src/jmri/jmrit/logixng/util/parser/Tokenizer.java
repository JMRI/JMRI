package jmri.jmrit.logixng.util.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parses and calculates an expression, for example "sin(2*pi*x)/3"
 * 
 * @author Daniel Bergqvist 2019
 */
public class Tokenizer {
    
    // This class should never be instanciated.
    private Tokenizer() {
    }
    
    private static void addToken(Token currentToken, List<Token> tokens) {
        if ((currentToken._tokenType == TokenType.FLOATING_NUMBER) && isIntegerNumber(currentToken._string)) {
            currentToken._tokenType = TokenType.INTEGER_NUMBER;
        }
        
        tokens.add(currentToken);
    }
    
    public static List<Token> getTokens(String expression) throws InvalidSyntaxException {
        
        List<Token> tokens = new ArrayList<>();
        Token currentToken = new Token();
        
//        System.out.format("%n%n%n");
//        System.out.format("getTokens(): %s%n", expression);
        
        AtomicInteger eatNextChar = new AtomicInteger(0);
        
        char ch = ' ';
        char lastChar;
        
        for (int i=0; i < expression.length(); i++) {
            lastChar = ch;
            ch = expression.charAt(i);
            char nextChar = ' ';    // An extra space at the end of the _string doesn't matter
            if (i+1 < expression.length()) {
                nextChar = expression.charAt(i+1);
            }
            
//            System.out.format("index %d: %s, %s, %c, %c%n", i, currentToken._tokenType.name(), currentToken._string, ch, nextChar);
            
            
            
            // Check for token type STRING
            if (ch == '\"') {
                if (Character.isLetterOrDigit(lastChar)) {
                    throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntaxAtIndex", i));
                }
                
                if (currentToken._tokenType == TokenType.SPACE) {
                    currentToken = new Token();
                } else if (currentToken._tokenType != TokenType.NONE) {
//                    System.out.format("Add: index %d: %s, %s, %c, %c%n", i, currentToken._tokenType.name(), currentToken._string, ch, nextChar);
                    addToken(currentToken, tokens);
                    currentToken = new Token();
                }
                currentToken._tokenType = TokenType.STRING;
                
                boolean done = false;
                while (!done) {
                    i++;
                    if (i >= expression.length()) {
                        throw new InvalidSyntaxException(Bundle.getMessage("UnexpectedEndOfString"));
                    }
                    ch = expression.charAt(i);
                    nextChar = ' ';    // An extra space at the end of the _string doesn't matter
                    if (i+1 < expression.length()) {
                        nextChar = expression.charAt(i+1);
                    }
                    // Handle escaped characters
                    if ((ch == '\\') && ((nextChar == '\\') || (nextChar == '"'))) {

                        currentToken._string += nextChar;
                        i++;
                    } else if (ch != '\"') {
                        currentToken._string += ch;
                    }
                    
                    done = (ch == '\"');
                }
                
                if (Character.isLetterOrDigit(nextChar)) {
                    throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntaxAtIndex", i));
                }
                
//                System.out.format("Add: index %d: %s, %s, %c, %c%n", i, currentToken._tokenType.name(), currentToken._string, ch, nextChar);
                addToken(currentToken, tokens);
                currentToken = new Token();
                
                // Continue for loop
                continue;
            }
            
            
            char nextNextChar = ' ';        // An extra space at the end of the _string doesn't matter
            char nextNextNextChar = ' ';    // An extra space at the end of the _string doesn't matter
            if (i+2 < expression.length()) {
                nextNextChar = expression.charAt(i+2);
            }
            if (i+3 < expression.length()) {
                nextNextNextChar = expression.charAt(i+3);
            }
            
            TokenType nextToken = getTokenType(currentToken, ch, nextChar, nextNextChar, nextNextNextChar, eatNextChar);
//            System.out.format("index %d: %s, %c%n", i, nextToken.name(), ch);
            
            if (nextToken == TokenType.SAME_AS_LAST) {
                currentToken._string += ch;
                continue;
            }
            
            switch (nextToken) {
                case ERROR:
                    throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntaxAtIndex", i));
                    
                case ASSIGN:
                case ASSIGN_ADD:
                case ASSIGN_SUBTRACKT:
                case ASSIGN_MULTIPLY:
                case ASSIGN_DIVIDE:
                case ASSIGN_MODULO:
                case ASSIGN_AND:
                case ASSIGN_OR:
                case ASSIGN_XOR:
                case ASSIGN_SHIFT_LEFT:
                case ASSIGN_SHIFT_RIGHT:
                case ASSIGN_UNSIGNED_SHIFT_RIGHT:
                case TERNARY_QUESTION_MARK:
                case TERNARY_COLON:
                case LEFT_PARENTHESIS:
                case RIGHT_PARENTHESIS:
                case LEFT_SQUARE_BRACKET:
                case RIGHT_SQUARE_BRACKET:
                case LEFT_CURLY_BRACKET:
                case RIGHT_CURLY_BRACKET:
                case DOT:
                case DOT_DOT:
                case COMMA:
                case EQUAL:
                case NOT_EQUAL:
                case LESS_THAN:
                case LESS_OR_EQUAL:
                case GREATER_THAN:
                case GREATER_OR_EQUAL:
                case ADD:
                case SUBTRACKT:
                case MULTIPLY:
                case DIVIDE:
                case MODULO:
                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                case UNSIGNED_SHIFT_RIGHT:
                case BOOLEAN_AND:
                case BOOLEAN_OR:
                case BOOLEAN_NOT:
                case BINARY_AND:
                case BINARY_OR:
                case BINARY_XOR:
                case BINARY_NOT:
                case INCREMENT:
                case DECREMENT:
                case IDENTIFIER:
                case SPACE:
                case NONE:
                    if ((currentToken._tokenType != TokenType.NONE) && (currentToken._tokenType != TokenType.SPACE)) {
                        addToken(currentToken, tokens);
                        currentToken = new Token();
                    }
                    currentToken._tokenType = nextToken;
                    break;
                    
                case FLOATING_NUMBER:
                    if ((currentToken._tokenType == TokenType.FLOATING_NUMBER) && !currentToken._string.isEmpty() && !isFloatingNumber(currentToken._string)) {
//                        System.out.format("Not a number: '%s'%n", currentToken._string);
                        throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntaxAtIndex", i));
                    }
                    if ((currentToken._tokenType != TokenType.NONE) && (currentToken._tokenType != TokenType.SPACE)) {
                        addToken(currentToken, tokens);
                        currentToken = new Token();
                    }
                    currentToken._tokenType = nextToken;
                    break;
                    
                case STRING:
                    if (!currentToken._string.endsWith("\"")) {
//                        System.err.format("String: %s%n", currentToken._string);
                        throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntaxAtIndex", i));
                    }
                    if ((currentToken._tokenType != TokenType.NONE) && (currentToken._tokenType != TokenType.SPACE)) {
                        addToken(currentToken, tokens);
                        currentToken = new Token();
                    }
                    currentToken._tokenType = nextToken;
                    break;
                    
                default:
                    throw new RuntimeException("unknown token type: "+nextToken.name());
            }
            
            if (currentToken._tokenType != TokenType.SPACE) {
                currentToken._string += ch;
            }
            
            i += eatNextChar.get();
//            System.out.format("New string: '%s'%n", currentToken._string);
        }
        
        if (currentToken._tokenType != TokenType.NONE) {
            addToken(currentToken, tokens);
        }
        
        return tokens;
    }
    
    private static TokenType getTokenType(Token currentToken, char ch, char nextChar, char nextNextChar, char nextNextNextChar, AtomicInteger eatNextChar) {
        
        eatNextChar.set(0);
        
        if (ch == '"') {
            return TokenType.STRING;
        }
        
        if (Character.isSpaceChar(ch)) {
            return TokenType.SPACE;
        }
        
        if (currentToken._tokenType == TokenType.STRING) {
            return TokenType.SAME_AS_LAST;
        }
        
        if (ch == '.') {
            if (nextChar == '.') {
                if ((currentToken._tokenType != TokenType.DOT_DOT)) {
                    eatNextChar.set(1);
                    return TokenType.DOT_DOT;
                } else {
                    // Three dots in a row is an error
                    return TokenType.ERROR;
                }
            } else if ((currentToken._tokenType == TokenType.IDENTIFIER)
                    || (currentToken._tokenType == TokenType.NONE)
                    || (currentToken._tokenType == TokenType.RIGHT_PARENTHESIS)
                    || (currentToken._tokenType == TokenType.RIGHT_SQUARE_BRACKET)
                    || (currentToken._tokenType == TokenType.RIGHT_CURLY_BRACKET)
                    ) {
                return TokenType.DOT;
            }
        }
        
        if (ch == '?') {
            return TokenType.TERNARY_QUESTION_MARK;
        }
        
        if (ch == ':') {
            return TokenType.TERNARY_COLON;
        }
        
        if ((ch == '=') && (nextChar != '=')) {
            return TokenType.ASSIGN;
        }
        
        if (nextChar == '=') {
            switch (ch) {
                case '+':
                    eatNextChar.set(1);
                    return TokenType.ASSIGN_ADD;
                case '-':
                    eatNextChar.set(1);
                    return TokenType.ASSIGN_SUBTRACKT;
                case '*':
                    eatNextChar.set(1);
                    return TokenType.ASSIGN_MULTIPLY;
                case '/':
                    eatNextChar.set(1);
                    return TokenType.ASSIGN_DIVIDE;
                case '%':
                    eatNextChar.set(1);
                    return TokenType.ASSIGN_MODULO;
                default:
                    // Do nothing
            }
        }
        
        if (ch == '<') {
            switch (nextChar) {
                case '=':
                    eatNextChar.set(1);
                    return TokenType.LESS_OR_EQUAL;
                case '<':
                    if (nextNextChar == '=') {
                        eatNextChar.set(2);
                        return TokenType.ASSIGN_SHIFT_LEFT;
                    } else {
                        eatNextChar.set(1);
                        return TokenType.SHIFT_LEFT;
                    }
                default:
                    return TokenType.LESS_THAN;
            }
        }
        
        if (ch == '>') {
            switch (nextChar) {
                case '=':
                    eatNextChar.set(1);
                    return TokenType.GREATER_OR_EQUAL;
                case '>':
                    if (nextNextChar == '=') {
                        eatNextChar.set(2);
                        return TokenType.ASSIGN_SHIFT_RIGHT;
                    } else if (nextNextChar == '>') {
                        if (nextNextNextChar == '=') {
                            eatNextChar.set(3);
                            return TokenType.ASSIGN_UNSIGNED_SHIFT_RIGHT;
                        } else {
                            eatNextChar.set(2);
                            return TokenType.UNSIGNED_SHIFT_RIGHT;
                        }
                    } else {
                        eatNextChar.set(1);
                        return TokenType.SHIFT_RIGHT;
                    }
                default:
                    return TokenType.GREATER_THAN;
            }
        }
        
        if (ch == '=') {
            if (nextChar == '=') {
                eatNextChar.set(1);
                return TokenType.EQUAL;
            } else {
                return TokenType.ERROR;
            }
        }
        
        if (ch == '!') {
            if (nextChar == '=') {
                eatNextChar.set(1);
                return TokenType.NOT_EQUAL;
            } else {
                return TokenType.BOOLEAN_NOT;
            }
        }
        
        if (ch == '|') {
            if (nextChar == '|') {
                eatNextChar.set(1);
                return TokenType.BOOLEAN_OR;
            } else if (nextChar == '=') {
                eatNextChar.set(1);
                return TokenType.ASSIGN_OR;
            } else {
                return TokenType.BINARY_OR;
            }
        }
        
        if (ch == '&') {
            if (nextChar == '&') {
                eatNextChar.set(1);
                return TokenType.BOOLEAN_AND;
            } else if (nextChar == '=') {
                eatNextChar.set(1);
                return TokenType.ASSIGN_AND;
            } else {
                return TokenType.BINARY_AND;
            }
        }
        
        if (ch == '~') {
            return TokenType.BINARY_NOT;
        }
        
        if (ch == ',') {
            return TokenType.COMMA;
        }
        
        if (ch == '+') {
            if (nextChar == '+') {
                eatNextChar.set(1);
                return TokenType.INCREMENT;
            } else {
                return TokenType.ADD;
            }
        }
        
        if (ch == '-') {
            if (nextChar == '-') {
                eatNextChar.set(1);
                return TokenType.DECREMENT;
            } else {
                return TokenType.SUBTRACKT;
            }
        }
        
        if (ch == '*') {
            return TokenType.MULTIPLY;
        }
        
        if (ch == '/') {
            return TokenType.DIVIDE;
        }
        
        if (ch == '%') {
            return TokenType.MODULO;
        }
        
        if (ch == '^') {
            if (nextChar == '=') {
                eatNextChar.set(1);
                return TokenType.ASSIGN_XOR;
            } else {
                return TokenType.BINARY_XOR;
            }
        }
        
        if (ch == '(') {
            return TokenType.LEFT_PARENTHESIS;
        }
        
        if (ch == ')') {
            return TokenType.RIGHT_PARENTHESIS;
        }
        
        if (ch == '[') {
            return TokenType.LEFT_SQUARE_BRACKET;
        }
        
        if (ch == ']') {
            return TokenType.RIGHT_SQUARE_BRACKET;
        }
        
        if (ch == '{') {
            return TokenType.LEFT_CURLY_BRACKET;
        }
        
        if (ch == '}') {
            return TokenType.RIGHT_CURLY_BRACKET;
        }
        
        if ((currentToken._tokenType == TokenType.FLOATING_NUMBER) &&
                (isFloatingNumber(currentToken._string+ch) || isFloatingNumber(currentToken._string+ch+nextChar))) {
            return TokenType.SAME_AS_LAST;
        }
        
        if ((currentToken._tokenType == TokenType.IDENTIFIER) && (Character.isLetterOrDigit(ch) || (ch == '_'))) {
            return TokenType.SAME_AS_LAST;
        }
        
        if (Character.isDigit(ch)) {
            return TokenType.FLOATING_NUMBER;
        }
        
        if ((currentToken._tokenType == TokenType.FLOATING_NUMBER) &&
                (Character.isLetterOrDigit(ch))) {
            return TokenType.ERROR;
        }
        
        if (Character.isDigit(ch)) {
            return TokenType.FLOATING_NUMBER;
        }
        
        if (Character.isLetter(ch) || (ch == '_')) {
            return TokenType.IDENTIFIER;
        }
        
        return TokenType.ERROR;
    }
    
    private static boolean isIntegerNumber(String str) {
        return str.matches("\\d+");
    }
    
    private static boolean isFloatingNumber(String str) {
        return str.matches("\\d+") || str.matches("\\d+\\.\\d+");
    }
    
}
