package jmri.jmrit.logixng.util.parser;

/**
 * A token used by the tokenizer and the parser
 */
public final class Token {

    final TokenType _tokenType;
    final String _string;
    private final int _pos;
    private final int _endPos;

    // This constructor is used by tests
    public Token() {
        _tokenType = TokenType.NONE;
        _string = "";
        _pos = 0;
        _endPos = 0;
    }

    public Token(TokenType tokenType, String string, int pos) {
        _tokenType = tokenType;
        _string = string;
        _pos = pos;
        if (_string != null) {
            _endPos = _pos + _string.length();
        } else {
            _endPos = _pos;
        }
    }

    public Token(TokenType tokenType, String string, int pos, int endPos) {
        _tokenType = tokenType;
        _string = string;
        _pos = pos;
        _endPos = endPos;
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

    public int getEndPos() {
        return _endPos;
    }

}
