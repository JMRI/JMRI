package jmri.jmrit.signalsystemeditor;

/**
 * A string with a comment
 * @author Daniel Bergqvist (C) 2022
 */
public class StringWithComment {

    private String _string;
    private String _comment;

    public StringWithComment(String string) {
        this._string = string;
    }

    public void setString(String string) {
        this._string = string;
    }

    public String getString() {
        return this._string;
    }

    public void setComment(String comment) {
        this._comment = comment;
    }

    public String getComment() {
        return this._comment;
    }

}
