package jmri.jmrit.logixng.util.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A recursive descent parser
 * 
 * @author Daniel Bergqvist 2019
 */
public class RecursiveDescentParser {

    private List<Token> _tokens;
    private final Map<String, Variable> _variables;
    
    
    public RecursiveDescentParser(Map<String, Variable> variables) {
        _variables = variables;
    }
    
    private State next(State state) {
        int newTokenIndex = state._tokenIndex+1;
        return new State(newTokenIndex, _tokens.get(newTokenIndex), state._tokenIndex, state._token);
    }
    
    
    private State accept(TokenType tokenType, State state) throws ParserException {
        if (state._token == null) {
            return null;
        }
        if (state._token._tokenType == tokenType) {
            int newTokenIndex = state._tokenIndex+1;
            Token newToken;
            int lastTokenPos = state._lastTokenPos;
            if (newTokenIndex < _tokens.size()) {
                newToken = _tokens.get(newTokenIndex);
            } else {
                lastTokenPos = state._token._pos + state._token._string.length();
                newToken = null;
            }
            return new State(newTokenIndex, newToken, lastTokenPos, state._token);
        } else {
            return null;
        }
    }
    
    
    private State expect(TokenType tokenType, State state) throws ParserException {
        State newState = accept(tokenType, state);
        if (newState == null) {
            throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
        }
        return newState;
    }
    
    
    public ExpressionNode parseExpression(String expression) throws ParserException {
        _tokens = Tokenizer.getTokens(expression);
        
        if (_tokens.isEmpty()) {
            return null;
        }
        
        ExpressionNodeAndState exprNodeAndState = firstRule.parse(new State(0, _tokens.get(0), 0, new Token()));
        
        if (exprNodeAndState == null) {
            return null;
        }
        
//        if (exprNodeAndState._exprNode != null) {
//            System.err.format("Expression: \"%s\"%n", exprNodeAndState._exprNode.getDefinitionString());
//        } else {
//            System.err.format("Expression: null%n");
//        }
        
        if ((exprNodeAndState._state != null)
                && (exprNodeAndState._state._tokenIndex < _tokens.size())) {
            
            throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntaxNotFullyParsed", exprNodeAndState._state._tokenIndex));
        }
        return exprNodeAndState._exprNode;
    }
    
    
    
    
    private static class State {
        
        private final int _tokenIndex;
        private final Token _token;
        private final int _lastTokenPos;
        private final Token _lastToken;
        
        public State(int tokenIndex, Token token, int lastTokenPos, Token lastToken) {
            _tokenIndex = tokenIndex;
            _token = token;
            _lastTokenPos = lastTokenPos;
            _lastToken = lastToken;
        }
    }
    
    
    private static class ExpressionNodeAndState {
        private final ExpressionNode _exprNode;
        private final State _state;
        
        private ExpressionNodeAndState(ExpressionNode exprNode, State state) {
            _exprNode = exprNode;
            _state = state;
        }
    }
    
    private interface Rule {
        
        public ExpressionNodeAndState parse(State state) throws ParserException;
        
    }
    
    
    // The rules below are numbered from the list on this page:
    // https://introcs.cs.princeton.edu/java/11precedence/
    
    private final Rule rule1 = new Rule1();
    private final Rule rule2 = new Rule2();
    private final Rule rule3 = new Rule3();
    private final Rule rule4 = new Rule4();
    private final Rule rule5 = new Rule5();
    private final Rule rule6 = new Rule6();
    private final Rule rule7 = new Rule7();
    private final Rule rule8 = new Rule8();
    private final Rule rule9 = new Rule9();
    private final Rule rule10 = new Rule10();
    private final Rule rule11 = new Rule11();
    private final Rule rule12 = new Rule12();
    private final Rule rule14 = new Rule14();
    private final Rule rule16 = new Rule16();
    private final Rule rule20 = new Rule20();
    private final Rule21_Function rule21_Function = new Rule21_Function();
    private final Rule21_Method rule21_Method = new Rule21_Method();
    
    private final Rule firstRule = rule1;
    
    
    // Assignment
    // <rule1> ::= <rule2> ||
    //             <rule2> = <rule1> ||
    //             <rule2> += <rule1> ||
    //             <rule2> -= <rule1> ||
    //             <rule2> *= <rule1> ||
    //             <rule2> /= <rule1> ||
    //             <rule2> %= <rule1> ||
    //             <rule2> &= <rule1> ||
    //             <rule2> ^= <rule1> ||
    //             <rule2> |= <rule1> ||
    //             <rule2> <<= <rule1> ||
    //             <rule2> >>= <rule1> ||
    //             <rule2> >>>= <rule1>
    private class Rule1 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNodeAndState leftSide = rule2.parse(state);
            if (leftSide == null) {
                return null;
            }
            State newState = leftSide._state;
            if ((newState._token != null)
                    && (
                        (newState._token._tokenType == TokenType.ASSIGN)
                        || (newState._token._tokenType == TokenType.ASSIGN_ADD)
                        || (newState._token._tokenType == TokenType.ASSIGN_SUBTRACKT)
                        || (newState._token._tokenType == TokenType.ASSIGN_MULTIPLY)
                        || (newState._token._tokenType == TokenType.ASSIGN_DIVIDE)
                        || (newState._token._tokenType == TokenType.ASSIGN_MODULO)
                    )) {
                
                TokenType operatorTokenType = newState._token._tokenType;
                newState = next(newState);
                ExpressionNodeAndState rightSide = rule2.parse(newState);
                
                ExpressionNode exprNode = new ExpressionNodeAssignmentOperator(operatorTokenType, leftSide._exprNode, rightSide._exprNode);
                leftSide = new ExpressionNodeAndState(exprNode, rightSide._state);
            }
            return leftSide;
        }
        
    }
    
    
    // Rule2 is ternary. <rule3> | <rule3> ? <rule2> : <rule2>
    private class Rule2 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNodeAndState leftSide = rule3.parse(state);
            if (leftSide == null) {
                return null;
            }
            State newState = leftSide._state;
            if ((newState._token != null)
                    && ((newState._token._tokenType == TokenType.TERNARY_QUESTION_MARK))) {
                
                if (! leftSide._exprNode.canBeAssigned()) {
                    throw new InvalidSyntaxException(Bundle.getMessage("LeftSideCannotBeAssigned"));
                }
                
//                TokenType operatorTokenType = newState._token._tokenType;
                newState = next(newState);
                ExpressionNodeAndState middleSide = rule3.parse(newState);
                
//                ExpressionNode exprNode = new ExpressionNodeBooleanOperator(operatorTokenType, leftSide._exprNode, rightSide._exprNode);
//                leftSide = new ExpressionNodeAndState(exprNode, middleSide._state);
                newState = middleSide._state;
                
                if ((newState._token != null)
                        && ((newState._token._tokenType == TokenType.TERNARY_COLON))) {
                    
//                    TokenType operatorTokenType = newState._token._tokenType;
                    newState = next(newState);
                    ExpressionNodeAndState rightRightSide = rule3.parse(newState);
                    
                    ExpressionNode exprNode = new ExpressionNodeTernaryOperator(
                            leftSide._exprNode, middleSide._exprNode, rightRightSide._exprNode);
                    leftSide = new ExpressionNodeAndState(exprNode, rightRightSide._state);
                } else {
                    throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                }
            }
            return leftSide;
        }
        
    }
    
    
    // Logical OR
    // <rule3> ::= <rule4> | <rule4> || <rule4>
    private class Rule3 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNodeAndState leftSide = rule4.parse(state);
            if (leftSide == null) {
                return null;
            }
            State newState = leftSide._state;
            while ((newState._token != null)
                    && ((newState._token._tokenType == TokenType.BOOLEAN_OR))) {
                
                TokenType operatorTokenType = newState._token._tokenType;
                newState = next(newState);
                ExpressionNodeAndState rightSide = rule4.parse(newState);
                
                ExpressionNode exprNode = new ExpressionNodeBooleanOperator(operatorTokenType, leftSide._exprNode, rightSide._exprNode);
                leftSide = new ExpressionNodeAndState(exprNode, rightSide._state);
                newState = rightSide._state;
            }
            return leftSide;
        }
        
    }
    
    
    // Logical AND
    // <rule4> ::= <rule5> | <rule5> && <rule5>
    private class Rule4 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNodeAndState leftSide = rule5.parse(state);
            if (leftSide == null) {
                return null;
            }
            State newState = leftSide._state;
            while ((newState._token != null)
                    && ((newState._token._tokenType == TokenType.BOOLEAN_AND))) {
                
                TokenType operatorTokenType = newState._token._tokenType;
                newState = next(newState);
                ExpressionNodeAndState rightSide = rule5.parse(newState);
                
                ExpressionNode exprNode = new ExpressionNodeBooleanOperator(operatorTokenType, leftSide._exprNode, rightSide._exprNode);
                leftSide = new ExpressionNodeAndState(exprNode, rightSide._state);
                newState = rightSide._state;
            }
            return leftSide;
        }
        
    }
    
    
    // Bitwise OR
    // <rule5> ::= <rule6> | <rule6> | <rule6>
    private class Rule5 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            return rule6.parse(state);
        }
        
    }
    
    
    // Bitwise XOR
    // <rule6> ::= <rule7> | <rule7> ^ <rule7>
    private class Rule6 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            return rule7.parse(state);
        }
        
    }
    
    
    // Bitwise AND
    // <rule7> ::= <rule8> | <rule8> & <rule8>
    private class Rule7 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            return rule8.parse(state);
        }
        
    }
    
    
    // Equality
    // <rule8> ::= <rule9> | <rule9> == <rule9> | <rule9> != <rule9>
    private class Rule8 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNodeAndState leftSide = rule9.parse(state);
            if (leftSide == null) {
                return null;
            }
            State newState = leftSide._state;
            while ((newState._token != null)
                    && ((newState._token._tokenType == TokenType.EQUAL)
                            || (newState._token._tokenType == TokenType.NOT_EQUAL))) {

                TokenType operatorTokenType = newState._token._tokenType;
                newState = next(newState);
                ExpressionNodeAndState rightSide = rule9.parse(newState);

                ExpressionNode exprNode = new ExpressionNodeComparingOperator(operatorTokenType, leftSide._exprNode, rightSide._exprNode);
                leftSide = new ExpressionNodeAndState(exprNode, rightSide._state);
                newState = rightSide._state;
            }
            return leftSide;
        }
        
    }
    
    
    // Relational
    // <rule9> ::= <rule10> | <rule10> < <rule10> | <rule10> <= <rule10> | <rule10> > <rule10> | <rule10> >= <rule10>
    private class Rule9 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNodeAndState leftSide = rule10.parse(state);
            if (leftSide == null) {
                return null;
            }
            State newState = leftSide._state;
            while ((newState._token != null)
                    && ((newState._token._tokenType == TokenType.LESS_THAN)
                            || (newState._token._tokenType == TokenType.LESS_OR_EQUAL)
                            || (newState._token._tokenType == TokenType.GREATER_THAN)
                            || (newState._token._tokenType == TokenType.GREATER_OR_EQUAL))) {

                TokenType operatorTokenType = newState._token._tokenType;
                newState = next(newState);
                ExpressionNodeAndState rightSide = rule10.parse(newState);

                ExpressionNode exprNode = new ExpressionNodeComparingOperator(operatorTokenType, leftSide._exprNode, rightSide._exprNode);
                leftSide = new ExpressionNodeAndState(exprNode, rightSide._state);
                newState = rightSide._state;
            }
            return leftSide;
        }
        
    }
    
    
    // Shift. Not implemented yet.
    // <rule10> ::= <rule11> | <rule11> << <rule11> | <rule11> >> <rule11> | <rule11> >>> <rule11>
    private class Rule10 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            return rule11.parse(state);
        }
        
    }
    
    
    // Additive
    // <rule11> ::= <rule12> | <rule12> + <rule12> | <rule12> - <rule12>
    private class Rule11 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNodeAndState leftSide = rule12.parse(state);
            if (leftSide == null) {
                return null;
            }
            State newState = leftSide._state;
            while ((newState._token != null)
                    && ((newState._token._tokenType == TokenType.ADD)
                            || (newState._token._tokenType == TokenType.SUBTRACKT))) {

                TokenType operatorTokenType = newState._token._tokenType;
                newState = next(newState);
                ExpressionNodeAndState rightSide = rule12.parse(newState);

                ExpressionNode exprNode = new ExpressionNodeArithmeticOperator(operatorTokenType, leftSide._exprNode, rightSide._exprNode);
                leftSide = new ExpressionNodeAndState(exprNode, rightSide._state);
                newState = rightSide._state;
            }
            return leftSide;
        }
        
    }
    
    
    // Multiplicative
    // <rule12> ::= <rule13> | <rule13> * <rule13> | <rule13> / <rule13> | <rule13> % <rule13>
    private class Rule12 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNodeAndState leftSide = rule14.parse(state);
            if (leftSide == null) {
                return null;
            }
            State newState = leftSide._state;
            while ((newState._token != null)
                    && ((newState._token._tokenType == TokenType.MULTIPLY)
                            || (newState._token._tokenType == TokenType.DIVIDE)
                            || (newState._token._tokenType == TokenType.MODULO))) {

                TokenType operatorTokenType = newState._token._tokenType;
                newState = next(newState);
                ExpressionNodeAndState rightSide = rule14.parse(newState);

                ExpressionNode exprNode = new ExpressionNodeArithmeticOperator(operatorTokenType, leftSide._exprNode, rightSide._exprNode);
                leftSide = new ExpressionNodeAndState(exprNode, rightSide._state);
                newState = rightSide._state;
            }
            return leftSide;
        }
        
    }
    
    
    // Rule3 in Java is cast object and object creation. Not relevant here.
    
    
    // Unary pre-increment, unary pre-decrement, unary plus, unary minus, unary logical NOT, unary bitwise NOT
    // <rule14> ::= <rule16> | ! <rule16> | ~ <rule16>
    private class Rule14 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            
            State newState = accept(TokenType.BOOLEAN_NOT, state);
            
            if (newState != null) {
                ExpressionNodeAndState exprNodeAndState = rule14.parse(newState);
                if (exprNodeAndState._exprNode == null) {
                    throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                }
                
                ExpressionNode exprNode = new ExpressionNodeBooleanOperator(newState._lastToken._tokenType, null, exprNodeAndState._exprNode);
                return new ExpressionNodeAndState(exprNode, exprNodeAndState._state);
                
            } else {
                newState = accept(TokenType.BINARY_NOT, state);

                if (newState != null) {
                    ExpressionNodeAndState exprNodeAndState = rule14.parse(newState);
                    if (exprNodeAndState._state._token == null) {
                        throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                    }
                    
                    ExpressionNode exprNode = new ExpressionNodeArithmeticOperator(newState._lastToken._tokenType, null, exprNodeAndState._exprNode);
                    return new ExpressionNodeAndState(exprNode, exprNodeAndState._state);
                    
                } else {
                    return rule16.parse(state);
                }
            }
        }
        
    }
    
    
    // Rule15 in Java is unary post-increment, unary post-decrement.
    // That is: ++ and --. We might want to implement it here.
    
    
    // Parentheses
    // <rule16> ::= <rule20> ( <firstRule> )
    private class Rule16 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            
            State newState = accept(TokenType.LEFT_PARENTHESIS, state);
            
            if (newState != null) {
                ExpressionNodeAndState exprNodeAndState = firstRule.parse(newState);
                if (exprNodeAndState._state._token == null) {
                    throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                }
                newState = expect(TokenType.RIGHT_PARENTHESIS, exprNodeAndState._state);
                return new ExpressionNodeAndState(exprNodeAndState._exprNode, newState);
            } else {
                return rule20.parse(state);
            }
        }
        
    }
    
    
    // Identifiers and constants
    // <rule20> ::= <identifier>
    //              | <identifier> ( <rule21> )
    //              | <rule20> [ <rule21> ]
    //              | <rule20> { <rule21> }
    //              | <rule20> . <rule20>
    //              | <rule20> . <identifier> ( <rule21> )
    
            // <rule20> ::= <identifier>
            //              | <identifier> ( <rule21> )
            //              | <identifier> [ <rule21> ]
            //              | <identifier> { <rule21> }
            //              | <identifier> . <identifier>
            //              | <identifier> . <identifier> ( <rule21> )
            //              | <identifier> . <identifier> [ <rule21> ]
            //              | <identifier> . <identifier> { <rule21> }
            //              | <identifier> . <identifier>
            //              | <identifier> . <identifier> . <identifier> ( <rule21> )
            //              | <identifier> . <identifier> . <identifier> [ <rule21> ]
            //              | <identifier> . <identifier> . <identifier> { <rule21> }
            //              | <identifier> . <identifier> ( <rule21> ) . <identifier> ( <rule21> )
            //              | <identifier> . <identifier> ( <rule21> ) . <identifier> [ <rule21> ]
            //              | <identifier> . <identifier> ( <rule21> ) . <identifier> { <rule21> }
/*    
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    List<String> list3 = new ArrayList<>();
    List<String> list4 = new ArrayList<>();
    list1.add(0,list2);
    list2.add(0,list2);
    list3.add(0,list2);
    list4.add(0,"Hello");
    list1.get(0).get(0).get(0) = "SomethingElse";
*/
    //              | <integer number>
    //              | <floating number>
    //              | <string>
    
    
    // Identifiers and constants
    // <rule20> ::= <identifier>
    //              | <identifier> <rule20a>
    //              | <integer number>
    //              | <floaing number>
    //              | <string>
    // 
    // <rule20a> :: = <rule20b> | <rule20b> <rule20a>
    // 
    // <rule20b> :: = . <identifier>
    //              | [ <rule21> ]
    //              | { <rule21> }
    private class Rule20 implements Rule {

        @Override
        public ExpressionNodeAndState parse(State state) throws ParserException {
            ExpressionNode exprNode;
            State newState;
            
            if ((newState = accept(TokenType.INTEGER_NUMBER, state)) != null) {
                exprNode = new ExpressionNodeIntegerNumber(newState._lastToken);
                return new ExpressionNodeAndState(exprNode, newState);
            } else if ((newState = accept(TokenType.FLOATING_NUMBER, state)) != null) {
                exprNode = new ExpressionNodeFloatingNumber(newState._lastToken);
                return new ExpressionNodeAndState(exprNode, newState);
            }
            
            
            ExpressionNodeAndState expressionNodeAndState;
            if ((newState = accept(TokenType.IDENTIFIER, state)) != null) {
                State newState2;
                if ((newState2 = accept(TokenType.LEFT_PARENTHESIS, newState)) != null) {
                    ExpressionNodeAndState exprNodeAndState =
                            rule21_Function.parse(newState2, newState._lastToken._string);
                    if (exprNodeAndState._state._token == null) {
                        throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                    }
                    exprNode = exprNodeAndState._exprNode;
                    newState2 = expect(TokenType.RIGHT_PARENTHESIS, exprNodeAndState._state);
                    expressionNodeAndState = new ExpressionNodeAndState(exprNodeAndState._exprNode, newState2);
                } else {
                    exprNode = new ExpressionNodeIdentifier(newState._lastToken, _variables);
                    expressionNodeAndState = new ExpressionNodeAndState(exprNode, newState);
                }
            } else if ((newState = accept(TokenType.STRING, state)) != null) {
                exprNode = new ExpressionNodeString(newState._lastToken);
                expressionNodeAndState = new ExpressionNodeAndState(exprNode, newState);
            } else {
                return null;
            }
            
            
            int count=0;
            boolean completed = false;
            do {
                if (count++ > 100) throw new RuntimeException("Infinite loop");
                
                State newState2;
                if ((newState2 = accept(TokenType.DOT, newState)) != null) {
                    State newState3;
                    if ((newState3 = accept(TokenType.IDENTIFIER, newState2)) != null) {
                        State newState4;
                        if ((newState4 = accept(TokenType.LEFT_PARENTHESIS, newState3)) != null) {
                            ExpressionNodeAndState exprNodeAndState2 =
                                    rule21_Method.parse(newState4, newState._lastToken._string, newState3._lastToken._string);
                            if (exprNodeAndState2._state._token == null) {
                                throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                            }
                            newState4 = expect(TokenType.RIGHT_PARENTHESIS, exprNodeAndState2._state);
                            exprNode = new ExpressionNodeComplex(
                                    exprNode, (ExpressionNodeWithParameter) exprNodeAndState2._exprNode);
                            expressionNodeAndState = new ExpressionNodeAndState(exprNode, newState4);
                            newState = newState4;
                        } else {
                            exprNode = new ExpressionNodeComplex(
                                    exprNode,
                                    new ExpressionNodeInstanceVariable(newState3._lastToken._string, _variables));
//                            exprNode = new ExpressionNodeInstanceVariable(newState3._lastToken._string, _variables);
                            expressionNodeAndState = new ExpressionNodeAndState(exprNode, newState3);
                            newState = newState3;
                        }
                    } else {
                        throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                    }
                } else if ((newState2 = accept(TokenType.LEFT_SQUARE_BRACKET, newState)) != null) {
                    State newState3;
                    ExpressionNodeAndState exprNodeAndState2 = rule1.parse(newState2);
                    if (exprNodeAndState2._state._token == null) {
                        throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                    }
                    newState3 = expect(TokenType.RIGHT_SQUARE_BRACKET, exprNodeAndState2._state);
                    exprNode = new ExpressionNodeComplex(
                            exprNode,
                            new ExpressionNodeArray(exprNodeAndState2._exprNode));
//                    exprNode = new ExpressionNodeArray(exprNodeAndState2._exprNode);
                    expressionNodeAndState = new ExpressionNodeAndState(exprNode, newState3);
                    newState = newState3;
                } else if ((newState2 = accept(TokenType.LEFT_CURLY_BRACKET, newState)) != null) {
                    State newState3;
                    ExpressionNodeAndState exprNodeAndState2 = rule1.parse(newState2);
                    if (exprNodeAndState2._state._token == null) {
                        throw new InvalidSyntaxException(Bundle.getMessage("InvalidSyntax"));
                    }
                    newState3 = expect(TokenType.RIGHT_CURLY_BRACKET, exprNodeAndState2._state);
                    exprNode = new ExpressionNodeComplex(
                            exprNode,
                            new ExpressionNodeMap(exprNodeAndState2._exprNode));
//                    exprNode = new ExpressionNodeMap(exprNodeAndState2._exprNode);
                    expressionNodeAndState = new ExpressionNodeAndState(exprNode, newState3);
                    newState = newState3;
                } else {
                    completed = true;
                }
            } while (!completed);
            
            return expressionNodeAndState;
        }
        
    }
    
    
    // <rule21> ::= <empty> | <rule21> | <rule21> , <rule3>
    private class Rule21_Function {

        public ExpressionNodeAndState parse(State state, String identifier) throws ParserException {
            
            List<ExpressionNode> parameterList = new ArrayList<>();
            
            State newState = state;
            State newState2;
            if ((accept(TokenType.RIGHT_PARENTHESIS, newState)) == null) {
                ExpressionNodeAndState exprNodeAndState = rule3.parse(state);
                parameterList.add(exprNodeAndState._exprNode);
                
                while ((newState2 = accept(TokenType.COMMA, exprNodeAndState._state)) != null) {
                    exprNodeAndState = rule3.parse(newState2);
                    parameterList.add(exprNodeAndState._exprNode);
                }
                
                newState = exprNodeAndState._state;
            }
            ExpressionNode exprNode = new ExpressionNodeFunction(identifier, parameterList);
            return new ExpressionNodeAndState(exprNode, newState);
        }
        
    }
    
    
    // <rule21> ::= <empty> | <rule21> | <rule21> , <rule3>
    private class Rule21_Method {

        public ExpressionNodeAndState parse(State state, String variable, String method) throws ParserException {
            
            List<ExpressionNode> parameterList = new ArrayList<>();
            
            State newState = state;
            State newState2;
            if ((accept(TokenType.RIGHT_PARENTHESIS, newState)) == null) {
                ExpressionNodeAndState exprNodeAndState = rule3.parse(state);
                parameterList.add(exprNodeAndState._exprNode);
                
                while ((newState2 = accept(TokenType.COMMA, exprNodeAndState._state)) != null) {
                    exprNodeAndState = rule3.parse(newState2);
                    parameterList.add(exprNodeAndState._exprNode);
                }
                
                newState = exprNodeAndState._state;
            }
            ExpressionNode exprNode = new ExpressionNodeMethod(method, _variables, parameterList);
            return new ExpressionNodeAndState(exprNode, newState);
        }
        
    }
    
    
}
