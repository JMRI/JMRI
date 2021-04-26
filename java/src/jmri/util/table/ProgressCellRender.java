package jmri.util.table;

import java.awt.Color;
import java.awt.Component;
import javax.annotation.CheckForNull;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Cell Renderer for a progress bar.
 * 
 * Displays progress bar changing from red ( 0f ) to green ( 1f )
 * Percentage value also displayed as text.
 * @author Steve Young Copyright (C) 2020
 */ 
public class ProgressCellRender extends JProgressBar implements TableCellRenderer {

    private final Color _oddRowColor;
    
    /**
     * Create a new Instance.
     * @param oddRowColor optional value for alternate row colour, can be null.
     */
    public ProgressCellRender( @CheckForNull Color oddRowColor ){
        super();
        _oddRowColor = oddRowColor;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
        boolean hasFocus, int row, int column) {
        int progress = 0;
        int fullValprogress = 0;
        float fp = 0.00f;
        if (value instanceof Float) {
            fp = (Float) value;
            progress = Math.round( fp * 100f);
            fullValprogress = Math.round( fp * 1000f);
            if ( progress==100 && fullValprogress<1000 ){
                progress = 99;
            }
        }

        // progress value from 0 to 100
        // As progress increases bar changes from red to green via yellow
        setForeground(new Color(Math.min(0.8f, 2.0f * (1 - fp)),Math.min(0.8f, 2.0f * fp ),0));
        setBorderPainted(false);
        setStringPainted(true);
        setValue(fullValprogress);
        if ( progress < 99 ) {
            setMaximum(1000);
        }
        setString(progress + "%");
        if (_oddRowColor!=null) {
            setBackground( isSelected ? table.getSelectionBackground() : 
                ( row % 2 == 0 ) ? table.getBackground() : _oddRowColor);
        }
        return this;
    }
}
