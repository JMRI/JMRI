package jmri.util.swing;

/**
 * Interface to label a JTable DataModel that 
 * has a method for  resizing.
 *
 * @see MultiLineCellEditor
 */

public interface ResizableRowDataModel {

    void resizeRowToText(int modelRow, int numberOfLines);

}
