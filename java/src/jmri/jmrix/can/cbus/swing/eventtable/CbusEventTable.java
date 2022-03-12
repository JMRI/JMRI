package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.eventtable.CbusTableEvent;

import jmri.jmrix.can.cbus.swing.CbusCommonSwing;
import jmri.jmrix.can.cbus.swing.CbusTableRowEventDnDHandler;
import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.swing.StayOpenCheckBoxItem;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Pane providing a CBUS Event table.
 *
 * @author Steve Young (C) 2019
 * @see CbusEventTablePane
 *
 * @since 4.15.5
 */
public class CbusEventTable extends JScrollPane implements TableModelListener {

    private final CbusEventTablePane _mainPane;
    private CbusTableRowEventDnDHandler eventDragHandler;
    private TableRowSorter<CbusEventTableDataModel> sorter;

    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss EEE d MMM YYYY"); // NOI18N
    /**
     * Create a new CBUS Node Event Table Pane
     * @param mainPane main Event Table Pane
     */
    public CbusEventTable( CbusEventTablePane mainPane ) {
        super(mainPane.eventTable);
        _mainPane = mainPane;
        init();
        
    }

    final void init(){
        
        // configure items for GUI
        CbusCommonSwing.configureTable(_mainPane.eventTable);
    
        setPreferredSize(new Dimension(450, 200));
    
        setColumnRenderers((XTableColumnModel)_mainPane.eventTable.getColumnModel());
        
        initColumnVisibleCheckboxes();
        
        _mainPane.eventModel.addTableModelListener(this);
        
        sorter = new TableRowSorter<>(_mainPane.eventModel);
        _mainPane.eventTable.setRowSorter(sorter);
        
        _mainPane.eventTable.setDragEnabled(true);
        _mainPane.eventTable.setDropMode(DropMode.ON);
        eventDragHandler = new CbusTableRowEventDnDHandler(_mainPane.getMemo(), _mainPane.eventTable);
        _mainPane.eventTable.setTransferHandler(eventDragHandler);
        
    }
    
    private final static int[] EVENT_COL_MENU = new int[]{CbusEventTableDataModel.NAME_COLUMN,
        CbusEventTableDataModel.NODE_COLUMN, CbusEventTableDataModel.EVENT_COLUMN,CbusEventTableDataModel.NODENAME_COLUMN,
        CbusEventTableDataModel.COMMENT_COLUMN, 
         CbusEventTableDataModel.STLR_ON_COLUMN,CbusEventTableDataModel.STLR_OFF_COLUMN};
    
    private final static int[] LATEST_COL_MENU = new int[]{
        CbusEventTableDataModel.STATE_COLUMN, CbusEventTableDataModel.CANID_COLUMN,
        CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN, CbusEventTableDataModel.EVENT_DAT_1 ,
        CbusEventTableDataModel.EVENT_DAT_2 , CbusEventTableDataModel.EVENT_DAT_3
    };
    
    private void initColumnVisibleCheckboxes(){
    
        JMenuItem resetSession = new JMenuItem(Bundle.getMessage("ResetSessionCount")); // NOI18N
        resetSession.addActionListener((ActionEvent e) -> _mainPane.eventModel.ta.resetAllSessionTotals() );
        _mainPane.evStatMenu.add(resetSession);
        _mainPane.evStatMenu.add( new JSeparator() );
        
        StayOpenCheckBoxItem[] cbArray = new StayOpenCheckBoxItem[_mainPane.eventModel.getColumnCount()];
        
        // initialise and set default column visibiity
        for (int i = 0; i < _mainPane.eventModel.getColumnCount(); i++) {
            StayOpenCheckBoxItem cbi = new StayOpenCheckBoxItem(_mainPane.eventModel.getColumnName(i));
            cbArray[i] = cbi;
            cbArray[i].setToolTipText(CbusEventTableDataModel.CBUS_EV_TABLE_COL_TOOLTIPS[i]);
            TableColumn column  = ((XTableColumnModel)_mainPane.eventTable.getColumnModel()).getColumnByModelIndex(i);
            cbi.addActionListener((ActionEvent e) -> {
                ((XTableColumnModel)_mainPane.eventTable.getColumnModel()).setColumnVisible(column, cbi.isSelected());
            });
            final int ii = i;
            ((XTableColumnModel)_mainPane.eventTable.getColumnModel()).setColumnVisible(((XTableColumnModel)_mainPane.eventTable.getColumnModel()).getColumnByModelIndex(i),
                Arrays.stream(CbusEventTableDataModel.INITIAL_COLS).anyMatch(j -> j == ii)
                );
            
        }
        
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
            tpm.persist(_mainPane.eventTable, true);
        });
        
        XTableColumnModel tcm = (XTableColumnModel)_mainPane.eventTable.getColumnModel();
        for (int i = 0; i < _mainPane.eventModel.getColumnCount(); i++) {
            cbArray[i].setSelected(tcm.isColumnVisible(tcm.getColumnByModelIndex(i)));
            
            int finali = i;
            if ( Arrays.stream(EVENT_COL_MENU).anyMatch(j -> j == finali)){
                _mainPane.evColMenu.add(cbArray[i]); // event columns
            } else if ( Arrays.stream(LATEST_COL_MENU).anyMatch(j -> j == finali)){
                _mainPane.evJmMenu.add(cbArray[i]); // latest columns
            } else if ( Arrays.stream(CbusEventTableDataModel.BUTTON_COLUMNS).anyMatch(j -> j == finali)) {
                _mainPane.buttonMenu.add(cbArray[i]); // button columns
            } else {
                _mainPane.evStatMenu.add(cbArray[i]); // stat columns
            }
        }
        
        _mainPane.evStatMenu.add( new JSeparator(),7 );
        _mainPane.evColMenu.add( new JSeparator(),5 );
        _mainPane.evJmMenu.add( new JSeparator(),3 );
        
    }
    
        
    private void setColumnRenderers(XTableColumnModel tcm){
        for (int i = 0; i < CbusEventTableDataModel.MAX_COLUMN; i++) {
            final int finali = i;
            if ( Arrays.stream(CbusEventTableDataModel.BUTTON_COLUMNS).anyMatch(j -> j == finali)){
                tcm.getColumn(i).setCellEditor(new ButtonEditor(new JButton()));
                tcm.getColumn(i).setCellRenderer(new ButtonRenderer());
            } 
            else if ( i== CbusEventTableDataModel.STLR_ON_COLUMN || i== CbusEventTableDataModel.STLR_OFF_COLUMN) {
                tcm.getColumn(i).setCellRenderer(new CbusEventBeanCellRenderer(_mainPane.filterText, 22));         
            }
            else {
                tcm.getColumn(i).setCellRenderer(getRenderer());
            }
        }
    }
    
    
    /**
     * Cell Renderer for table columns, highlights any text in filter input
     */    
    private TableCellRenderer getRenderer() {
        return new TableCellRenderer() {
            JTextField f = new JTextField();

            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object arg1, boolean isSelected, boolean hasFocus, 
                int row, int col) {
                
                if(arg1 != null){
                    String string = arg1.toString();
                    f.setText(string);
                    f.setHorizontalAlignment(JTextField.CENTER);
                    
                    if (col != CbusEventTableDataModel.EVENT_COLUMN) {
                        CbusCommonSwing.hideNumbersLessThan(1, string, f);
                    }
                    
                    CbusCommonSwing.setCellTextHighlighter(_mainPane.filterText.getText(), string, f);
                    CbusCommonSwing.setCellFromDate(arg1,f,DATE_FORMAT);

                } else {
                    f.setText("");
                    f.getHighlighter().removeAllHighlights();
                }
                
                CbusCommonSwing.setCellBackground(isSelected, f, table,row);
                CbusCommonSwing.setCellFocus(hasFocus, f, table);
                CbusCommonSwing.setCellFromCbusEventEnum(arg1, f);
                
                return f;
            }
        };
    }
    
    protected void addFilterListener( JTextField filter ){
    
        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
            
        });
    }
    
    public void update(DocumentEvent e) {
        String textForSearch = _mainPane.filterText.getText(); // better searches if not trimmed
        if (textForSearch.length() == 0) {
            sorter.setRowFilter(null);
            _mainPane.clearfilterButton.setEnabled(false);
        } else {
            _mainPane.clearfilterButton.setEnabled(true);
            try {
                sorter.setRowFilter(
                RowFilter.regexFilter("(?i)" + textForSearch)); // case insensitive
            } catch (PatternSyntaxException pse) {
                // log.error(" bad regex ");
            }
        }
    }
    
    @Override
    public void tableChanged(TableModelEvent e) {
        _mainPane.neweventcontainer.setNewButtonActive( !_mainPane.eventModel.getEvents().contains( new CbusTableEvent(null,
            _mainPane.neweventcontainer.getNn(), _mainPane.neweventcontainer.getEn())) );
    }
    
    public void addEvent(int nn,int en){
        _mainPane.eventModel.provideEvent(nn, en);
        tableChanged(null);
    }
    

    public void dispose() {
        _mainPane.eventModel.removeTableModelListener(this);
        eventDragHandler.dispose();
    }

 //   private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTablePane.class);

}
