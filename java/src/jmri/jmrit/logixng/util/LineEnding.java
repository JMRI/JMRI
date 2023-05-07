package jmri.jmrit.logixng.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum for line endings
 *
 * @author Daniel Bergqvist (C) 2023
 */
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")  // This class lets the user select the line ending

public enum LineEnding {

    WindowsCrLf(Bundle.getMessage("LineEnding_WindowsCrLf"), String.format("\r\n"), true),
    MacLinuxLf(Bundle.getMessage("LineEnding_MacLinuxLf"), String.format("\n"), true),
    System(Bundle.getMessage("LineEnding_System"), String.format("%n"), true),
    Space(Bundle.getMessage("LineEnding_Space"), " ", false),
    HtmlBreak(Bundle.getMessage("LineEnding_HtmlBreak"), "<br>", false),
    None(Bundle.getMessage("LineEnding_None"), "", false);

    private final String _text;
    private final String _lineEnding;
    private final boolean _isTrueLineEnding;

    private LineEnding(String text, String lineEnding, boolean isTrueLineEnding) {
        this._text = text;
        this._lineEnding = lineEnding;
        this._isTrueLineEnding = isTrueLineEnding;
    }

    public String getLineEnding() {
        return _lineEnding;
    }

    public boolean isTrueLineEnding() {
        return _isTrueLineEnding;
    }

    public static LineEnding[] trueValues() {
        List<LineEnding> list = new ArrayList<>();
        for (LineEnding le : LineEnding.values()) {
            if (le._isTrueLineEnding) list.add(le);
        }
        return list.toArray(new LineEnding[0]);
    }

    @Override
    public String toString() {
        return _text;
    }

}
