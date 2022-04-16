package jmri.jmrit.logixng;

/**
 * Enum for row or column.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public enum TableRowOrColumn {
    Row(Bundle.getMessage("TableRowOrColumn_Row"),Bundle.getMessage("TableRowOrColumn_Row_lowercase")),
    Column(Bundle.getMessage("TableRowOrColumn_Column"), Bundle.getMessage("TableRowOrColumn_Column_lowercase"));
    
    private final String _text;
    private final String _textLowerCase;

    private TableRowOrColumn(String text, String textLowerCase) {
        this._text = text;
        this._textLowerCase = textLowerCase;
    }

    @Override
    public String toString() {
        return _text;
    }

    public String toStringLowerCase() {
        return _textLowerCase;
    }

    public TableRowOrColumn getOpposite() {
        if (this == Row) {
            return Column;
        } else {
            return Row;
        }
    }
    
}
