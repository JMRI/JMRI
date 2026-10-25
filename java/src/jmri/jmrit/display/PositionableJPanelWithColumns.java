package jmri.jmrit.display;

import javax.swing.JTextField;

/**
 * Base class for positionable panels that have a variable number of columns
 */
public class PositionableJPanelWithColumns extends PositionableJPanel {

    protected JTextField _textBox = new PositionableJTextField();
    protected int _nCols;

    /**
     * Create a positionable panel with column-based content.
     *
     * @param editor the panel's editor
     */
    public PositionableJPanelWithColumns(Editor editor) {
        super(editor);
    }

    public void setNumColumns(int nCols) {
        _textBox.setColumns(nCols);
        _nCols = nCols;
    }

    public int getNumColumns() {
        return _nCols;
    }
}
