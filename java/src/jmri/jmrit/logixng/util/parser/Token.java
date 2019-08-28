package jmri.jmrit.logixng.util.parser;

/**
 * A token used by the tokenizer and the parser
 */
public final class Token {

    TokenType _tokenType;
    String _string;
    int _pos;

    public Token() {
        _tokenType = TokenType.NONE;
        _string = "";
        _pos = 0;
    }
    
    public Token(TokenType tokenType, String string, int pos) {
        _tokenType = tokenType;
        _string = string;
        _pos = pos;
    }
    
    public TokenType getTokenType() {
        return _tokenType;
    }
    
    public String getString() {
        return _string;
    }
    
    public int getPos() {
        return _pos;
    }
    
}
