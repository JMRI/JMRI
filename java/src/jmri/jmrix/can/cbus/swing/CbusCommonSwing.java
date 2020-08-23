package jmri.jmrix.can.cbus.swing;

import java.awt.Color;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import jmri.jmrix.can.cbus.CbusEventDataElements.EvState;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;

/**
 * Common CBUS swing functions.
 * @author Steve Young Copyright (C) 2020
 */
public class CbusCommonSwing {
    
    public static final Color VERY_LIGHT_RED = new Color(255,176,173);
    public static final Color VERY_LIGHT_GREEN = new Color(165,255,164);
    public static final Color WHITE_GREEN = new Color(0xf5,0xf5,0xf5);
    public static final Color GOLD = new Color(255,204,51);
    
    /**
     * Set cell background with alternating rows.
     * @param isSelected true if selected.
     * @param f cell component.
     * @param table cell table.
     * @param row cell row.
     */
    public static void setCellBackground( boolean isSelected, JComponent f, JTable table, int row){
        f.setBackground(isSelected ? table.getSelectionBackground() : 
            (( row % 2 == 0 ) ? table.getBackground() : WHITE_GREEN ) );
    }
    
    public static void setCellFocus(boolean hasFocus, JComponent f, JTable table) {
        f.setBorder(hasFocus ? BorderFactory.createMatteBorder(1, 1, 1, 1, Color.blue) : 
            BorderFactory.createEmptyBorder(1, 1, 1, 1) );
    }
    
    public static void hideNumbersLessThan(int min, String string, JTextField f){
        try {
            if (Integer.parseInt(string)<min) {
                f.setText("");
            }
        } catch (NumberFormatException ex) {}
    }
    
    public static void setCellFromCbusEventEnum(Object object, JTextField f){
        if ( object instanceof EvState ) {
            EvState state = (EvState) object;
            switch (state) {
                case ON:
                    setTextBackGround(f,Bundle.getMessage("CbusEventOn"),VERY_LIGHT_GREEN);
                    break;
                case OFF:
                    setTextBackGround(f,Bundle.getMessage("CbusEventOff"),VERY_LIGHT_RED);
                    break;
                case REQUEST:
                    setTextBackGround(f,Bundle.getMessage("CbusEventRequest"),GOLD);
                    break;
                case UNKNOWN:
                    f.setText("");
                    break;
                default:
                    break;
            }
        }
    }
    
    public static void setCellFromBackupEnum(Object object, JTextField f){
        if ( object instanceof BackupType ) {
            BackupType type = (BackupType) object;
            switch (type) {
                case INCOMPLETE:
                    setTextBackGround(f,Bundle.getMessage("BackupIncomplete"),VERY_LIGHT_RED);
                    break;
                case COMPLETE:
                    setTextBackGround(f,Bundle.getMessage("BackupComplete"),VERY_LIGHT_GREEN);
                    break;
                case COMPLETEDWITHERROR:
                    setTextBackGround(f,Bundle.getMessage("BackupCompleteError"),VERY_LIGHT_RED);
                    break;
                case NOTONNETWORK:
                    setTextBackGround(f,Bundle.getMessage("BackupNotOnNetwork"),VERY_LIGHT_RED);
                    break;
                case OUTSTANDING:
                    setTextBackGround(f,Bundle.getMessage("BackupOutstanding"),GOLD);
                    break;
                case SLIM:
                    setTextBackGround(f,Bundle.getMessage("NodeInSlim"),GOLD);
                    break;
                default:
                    break;
            }
        }
    }
    
    private static void setTextBackGround(JTextField f, String text, Color bg) {
        f.setBackground( bg );
        f.setText(text);
    }
    
    public static void setCellFromDate(Object object, JTextField f, DateFormat dformat){
        if (object instanceof Date) {
            f.setText(dformat.format((Date) object));
        } 
    }
    
    public static void setCellTextHighlighter(String textForSearch, String string, JTextField f){
        if(Pattern.compile(Pattern.quote(textForSearch), Pattern.CASE_INSENSITIVE).matcher(string).find()){
            string = string.toLowerCase();
            textForSearch = textForSearch.toLowerCase();
            int indexOf = string.indexOf(textForSearch);
            try {
                f.getHighlighter().addHighlight(
                    indexOf,
                    indexOf+textForSearch.length(),
                    new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(Color.CYAN)
                );
            } catch (BadLocationException e) {}
        }
    }
    
    /**
     * Configure a table to have our standard rows and columns.
     * <p>
     * This is optional, in that other table formats can use this table model.
     * But we put it here to help keep it consistent.
     * @param table table to configure
     */
    public static void configureTable(JTable table) {
        // allow reordering of the columns
        table.getTableHeader().setReorderingAllowed(true);

        table.createDefaultColumnsFromModel();
        
        // prevent the TableColumnModel from being recreated and loosing custom cell renderers
        table.setAutoCreateColumnsFromModel(false); 
        
        // shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        table.setRowHeight(new JTextField("Y").getPreferredSize().height+6);
        
        // give table a name for JmriJTablePersistenceManager
        table.setName(table.getModel().getClass().getName());
        
        // resize columns to initial size
        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = Math.min(260,new JTextField(table.getColumnName(i)).getPreferredSize().width*2);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        table.sizeColumnsToFit(-1);
        
    }
    
}
