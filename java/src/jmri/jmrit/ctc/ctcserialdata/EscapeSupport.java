package jmri.jmrit.ctc.ctcserialdata;

import java.util.ArrayList;

/**
 * This class supports handling "~" (Tilde), "," (Comma) and ";" (Semicolon)
 * in data fields.  When those are encountered, they are escaped.  When the
 * de-escaping routine sees those sequences, it reconstructs the fields properly.
 * 
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 */
public class EscapeSupport {
    
    static final public String ESCAPE_CHARACTER = "~";
    static final public String ESCAPED_ESCAPE_STRING = "~~";
    static final public String CSV_SEPARATOR_STRING = ",";
    static final public String ESCAPED_CSV_STRING = "~0";
    static final public String SSV_SEPARATOR_STRING = ";";
    static final public String ESCAPED_SSV_STRING = "~1";
    static public String convertNonEscapeToEscape(String nonEscapeString) {
        if (null == nonEscapeString) return null;    // Safety, do NOTHING!
        String returnString = nonEscapeString.replace(ESCAPE_CHARACTER, ESCAPED_ESCAPE_STRING);
        returnString = returnString.replace(CSV_SEPARATOR_STRING, ESCAPED_CSV_STRING);
        return returnString.replace(SSV_SEPARATOR_STRING, ESCAPED_SSV_STRING);
    }
    static public String convertEscapeToNonEscape(String escapedString) {
        if (null == escapedString) return null;    // Safety, do NOTHING!
        String returnString = escapedString.replace(ESCAPED_ESCAPE_STRING, ESCAPE_CHARACTER);
        returnString = returnString.replace(ESCAPED_CSV_STRING, CSV_SEPARATOR_STRING);
        return returnString.replace(ESCAPED_SSV_STRING, SSV_SEPARATOR_STRING);
    }
}
