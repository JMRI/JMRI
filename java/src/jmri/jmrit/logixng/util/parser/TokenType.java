package jmri.jmrit.logixng.util.parser;

/**
 * Types of tokens.
 * 
 * https://introcs.cs.princeton.edu/java/11precedence/
 */

public enum TokenType {
    ERROR,              // Invalid token, for example an identifier starting with a digit
    SAME_AS_LAST,       // The same token as last time
    NONE,
    SPACE,              // Any space character outside of a string, like space, newline, ...
    COMMA,              // , , used for parameter lists
    DOT_DOT,            // .. , used for intervalls
    DOT,                // . , used for method calls and properties, for example myVar.myFunc(parameter)
    ASSIGN,             // =
    ASSIGN_ADD,         // +=
    ASSIGN_SUBTRACKT,   // -=
    ASSIGN_MULTIPLY,    // *=
    ASSIGN_DIVIDE,      // /=
    ASSIGN_MODULO,      // %=
    TERNARY_QUESTION_MARK,   // ?
    TERNARY_COLON,           // :
    BOOLEAN_OR,         // ||
    BOOLEAN_AND,        // &&
    BINARY_OR,          // |
    BINARY_XOR,         // ^
    BINARY_AND,         // &
    EQUAL,              // ==
    NOT_EQUAL,          // !=
    LESS_THAN,          // <
    LESS_OR_EQUAL,      // <=
    GREATER_THAN,       // >
    GREATER_OR_EQUAL,   // >=
    SHIFT_LEFT,         // <<
    SHIFT_RIGHT,        // >>
    ADD,                // +
    SUBTRACKT,          // -
    MULTIPLY,           // *
    DIVIDE,             // /
    MODULO,             // %
    BOOLEAN_NOT,        // !
    BINARY_NOT,         // ~
    INCREMENT,          // ++
    DECREMENT,          // --
    LEFT_PARENTHESIS,       // (
    RIGHT_PARENTHESIS,      // )
    LEFT_SQUARE_BRACKET,    // [
    RIGHT_SQUARE_BRACKET,   // ]
    LEFT_CURLY_BRACKET,     // {
    RIGHT_CURLY_BRACKET,    // }
    IDENTIFIER,
    INTEGER_NUMBER,
    FLOATING_NUMBER,
    STRING;
}
