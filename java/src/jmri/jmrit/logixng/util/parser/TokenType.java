package jmri.jmrit.logixng.util.parser;

/**
 * 
 * https://introcs.cs.princeton.edu/java/11precedence/
 * 
 * Types of tokens.
 * <P>
 * Rules:
 * <P>
 * &lt;rule3&gt; ::= &lt;rule4&gt; | &lt;rule3&gt; || &lt;rule4&gt;<BR>
 * &lt;rule4&gt; ::= &lt;rule5&gt; | &lt;rule4&gt; &amp;&amp; &lt;rule5&gt;<BR>
 * &lt;rule5&gt; ::= &lt;rule6&gt; | &lt;rule5&gt; | &lt;rule6&gt;<BR>
 * &lt;rule6&gt; ::= &lt;rule7&gt; | &lt;rule6&gt; ^ &lt;rule7&gt;<BR>
 * &lt;rule7&gt; ::= &lt;rule8&gt; | &lt;rule7&gt; &amp; &lt;rule8&gt;<BR>
 * &lt;rule8&gt; ::= &lt;rule9&gt; | &lt;rule8&gt; == &lt;rule9&gt; | &lt;rule8&gt; != &lt;rule9&gt;<BR>
 * &lt;rule9&gt; ::= &lt;rule10&gt; | &lt;rule9&gt; &lt; &lt;rule10&gt; | &lt;rule9&gt; &lt;= &lt;rule10&gt; | &lt;rule9&gt; &gt; &lt;rule10&gt; | &lt;rule9&gt; &gt;= &lt;rule10&gt;<BR>
 * &lt;rule10&gt; ::= &lt;rule11&gt; | &lt;rule10&gt; &lt;&lt; &lt;rule11&gt; | &lt;rule10&gt; &gt;&gt; &lt;rule11&gt;<BR>
 * &lt;rule11&gt; ::= &lt;rule12&gt; | &lt;rule11&gt; + &lt;rule12&gt; | &lt;rule11&gt; - &lt;rule12&gt;<BR>
 * &lt;rule12&gt; ::= &lt;rule13&gt; | &lt;rule12&gt; * &lt;rule13&gt; | &lt;rule12&gt; / &lt;rule13&gt;<BR>
 * &lt;rule14&gt; ::= &lt;rule16&gt; | ! &lt;rule14&gt; | ~ &lt;rule14&gt;<BR>
 * &lt;rule16&gt; ::= &lt;rule20&gt; ( &lt;rule3&gt; )<BR>
 * &lt;rule20&gt; ::= &lt;identifier&gt; | &lt;identifier&gt; ( &lt;rule21&gt; ) | &lt;integer number&gt; | &lt;floating number&gt; | &lt;string&gt;<BR>
 * &lt;rule21&gt; ::= &lt;empty&gt; | &lt;rule3&gt; | &lt;rule21&gt; , &lt;rule3&gt;<BR>
 */

/*
Rules:

<rule3> ::= <rule4> | <rule3> || <rule4>
<rule4> ::= <rule5> | <rule4> && <rule5>
<rule5> ::= <rule6> | <rule5> | <rule6>
<rule6> ::= <rule7> | <rule6> ^ <rule7>
<rule7> ::= <rule8> | <rule7> & <rule8>
<rule8> ::= <rule9> | <rule8> == <rule9> | <rule8> != <rule9>
<rule9> ::= <rule10> | <rule9> < <rule10> | <rule9> <= <rule10> | <rule9> > <rule10> | <rule9> >= <rule10>
<rule10> ::= <rule11> | <rule10> << <rule11> | <rule10> >> <rule11>
<rule11> ::= <rule12> | <rule11> + <rule12> | <rule11> - <rule12>
<rule12> ::= <rule13> | <rule12> * <rule13> | <rule12> / <rule13> | <rule12> % <rule13>
<rule14> ::= <rule16> | ! <rule14> | ~ <rule14>
<rule16> ::= <rule20> ( <rule3> )
<rule20> ::= <identifier> | <identifier> ( <rule21> ) | <integer number> | <floating number> | <string>
<rule21> ::= <empty> | <rule3> | <rule21> , <rule3>
*/


public enum TokenType {
    // For precedence, see: https://introcs.cs.princeton.edu/java/11precedence/
    ERROR(Integer.MIN_VALUE),           // Invalid token, for example an identifier starting with a digit
    SAME_AS_LAST(Integer.MIN_VALUE),    // The same token as last time
    NONE(Integer.MIN_VALUE),
    SPACE(Integer.MIN_VALUE),           // Any space character outside of a string, like space, newline, ...
    COMMA(-2),              // , , used for parameter lists
    DOT_DOT(-1),            // .. , used for intervalls
    DOT(-1),                // . , used for method calls and properties, for example myVar.myFunc(parameter)
    ASSIGN(1),              // =
    ASSIGN_ADD(1),          // +=
    ASSIGN_SUBTRACKT(1),    // -=
    ASSIGN_MULTIPLY(1),     // *=
    ASSIGN_DIVIDE(1),       // /=
    ASSIGN_MODULO(1),       // %=
    TERNARY_QUESTION_MARK(2),   // ?
    TERNARY_COLON(2),           // :
    BOOLEAN_OR(3),  // ||
    BOOLEAN_AND(4), // &&
    BINARY_OR(5),   // |
    BINARY_XOR(6),  // ^
    BINARY_AND(7),  // &
    EQUAL(8),       // ==
    NOT_EQUAL(8),   // !=
    LESS_THAN(9),       // <
    LESS_OR_EQUAL(9),   // <=
    GREATER_THAN(9),    // >
    GREATER_OR_EQUAL(9),// >=
    SHIFT_LEFT(10),     // <<
    SHIFT_RIGHT(10),    // >>
    ADD(11),            // +
    SUBTRACKT(11),      // -
    MULTIPLY(12),       // *
    DIVIDE(12),         // /
    MODULO(12),         // %
    BOOLEAN_NOT(14),    // !
    BINARY_NOT(14),     // ~
    INCREMENT(14),      // ++
    DECREMENT(14),      // --
    LEFT_PARENTHESIS(16),       // (
    RIGHT_PARENTHESIS(16),      // )
    LEFT_SQUARE_BRACKET(16),    // [
    RIGHT_SQUARE_BRACKET(16),   // ]
    LEFT_CURLY_BRACKET(16),     // {
    RIGHT_CURLY_BRACKET(16),    // }
    IDENTIFIER(Integer.MAX_VALUE),
    INTEGER_NUMBER(Integer.MAX_VALUE),
    FLOATING_NUMBER(Integer.MAX_VALUE),
    STRING(Integer.MAX_VALUE);
    
    private final int _priority;

    private TokenType(int priority) {
        _priority = priority;
    }

    public boolean hasLowerPrecedence(TokenType tokenType) {
        return _priority < tokenType._priority;
    }

    public boolean hasSamePrecedence(TokenType tokenType) {
        return _priority == tokenType._priority;
    }

    public boolean hasHigherPrecedence(TokenType tokenType) {
        return _priority > tokenType._priority;
    }
    
}
