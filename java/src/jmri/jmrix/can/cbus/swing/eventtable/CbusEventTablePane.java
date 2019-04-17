package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ResourceBundle;
import java.util.regex.PatternSyntaxException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DropMode;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import jmri.jmrix.can.cbus.eventtable.CbusTableEvent;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a Cbus event table. Menu code copied from BeanTableFrame.
 *
 * @author Andrew Crosland (C) 2009
 * @author Kevin Dickerson (C) 2012
 * @author Steve Young (C) 2018
 * @see CbusEventTableDataModel
 *
 * @since 2.99.2
 */
public class CbusEventTablePane extends jmri.jmrix.can.swing.CanPanel implements TableModelListener {

    public CbusEventTableDataModel eventModel=null;
    public JTable eventTable=null;
    public JScrollPane eventScroll;
    public JSplitPane split;
    protected JPanel pane1;
    protected JPanel toppanelcontainer;
    protected JPanel neweventcontainer = new JPanel();
    private JSpinner newnodenumberSpinner;
    private JSpinner newevnumberSpinner;
    private JButton newevbutton;
    public String currentRowCount;
    protected JPanel filterpanel = new JPanel();
    
    private final JTextField filterText = new JTextField("",8);
    
    
    private String textForSearch = "";
    private double _splitratio = 0.95;

    private JScrollPane scrolltablefeedback;
    private DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final Color VERY_LIGHT_RED = new Color(255,176,173);
    public static final Color VERY_LIGHT_GREEN = new Color(165,255,164);

    private JMenu evColMenu = new JMenu(Bundle.getMessage("evColMenuName"));
    private JMenu evStatMenu = new JMenu(Bundle.getMessage("evStatMenuName"));
    private JMenu evJmMenu = new JMenu(Bundle.getMessage("evJmMenuName"));
    private List<JCheckBoxMenuItem> colMenuList = new ArrayList<JCheckBoxMenuItem>();

    protected Highlighter tableFeedbackHighlighter;
    protected Highlighter h;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        try {
            eventModel = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel.class);
        } catch (NullPointerException e) {
            eventModel = new CbusEventTableDataModel(memo, 10,
                CbusEventTableDataModel.MAX_COLUMN); // controller, row, column
        }
        
        init();
    }

    public CbusEventTablePane() {
        super();
    }

    public void init() {
        JMenuItem stlrUpdateItem = new JMenuItem(Bundle.getMessage("UpdateCols"));
        evJmMenu.add(stlrUpdateItem);
        stlrUpdateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventModel.ta.updatejmricols();
            }
        });        
        
        JTable eventTable = new JTable(eventModel) {
            // Override JTable Header to implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        try {
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return CbusEventTableDataModel.columnToolTips[realIndex];    
                        } catch (RuntimeException e1) {
                            //catch null pointer exception if mouse is over an empty line
                        }
                        return null;
                    }
                };
            }
        };

        // Use XTableColumnModel so we can control which columns are visible
        final  XTableColumnModel tcm = new XTableColumnModel();
        eventTable.setColumnModel(tcm);
        eventTable.createDefaultColumnsFromModel();
        
        eventTable.setAutoCreateRowSorter(true);
        
        final TableRowSorter<CbusEventTableDataModel> sorter = new TableRowSorter<CbusEventTableDataModel>(eventModel);
        eventTable.setRowSorter(sorter);
        
        h = eventModel.tablefeedback().getHighlighter();
        scrolltablefeedback = new JScrollPane (eventModel.tablefeedback());
        
        eventTable.setRowSelectionAllowed(true);
        eventTable.setColumnSelectionAllowed(false);
        
        eventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        eventTable.setRowHeight(26);
        
        tcm.getColumn(CbusEventTableDataModel.NAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusEventTableDataModel.NODENAME_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusEventTableDataModel.COMMENT_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusEventTableDataModel.NODE_COLUMN).setCellRenderer(getRenderer());        
        tcm.getColumn(CbusEventTableDataModel.EVENT_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusEventTableDataModel.STATE_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusEventTableDataModel.STLR_ON_COLUMN).setCellRenderer(getRenderer());
        tcm.getColumn(CbusEventTableDataModel.STLR_OFF_COLUMN).setCellRenderer(getRenderer());
        
        TableColumn delBColumn = tcm.getColumn(CbusEventTableDataModel.DELETE_BUTTON_COLUMN);
        delBColumn.setCellEditor(new ButtonEditor(new JButton()));
        delBColumn.setCellRenderer(new ButtonRenderer());
        
        TableColumn onBColumn = tcm.getColumn(CbusEventTableDataModel.ON_BUTTON_COLUMN);
        onBColumn.setCellEditor(new ButtonEditor(new JButton()));
        onBColumn.setCellRenderer(new ButtonRenderer());
        
        TableColumn offBColumn = tcm.getColumn(CbusEventTableDataModel.OFF_BUTTON_COLUMN);
        offBColumn.setCellEditor(new ButtonEditor(new JButton()));
        offBColumn.setCellRenderer(new ButtonRenderer());    
        
        TableColumn togBColumn = tcm.getColumn(CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN);
        togBColumn.setCellEditor(new ButtonEditor(new JButton()));
        togBColumn.setCellRenderer(new ButtonRenderer());        
        
        TableColumn rqStatColumn = tcm.getColumn(CbusEventTableDataModel.STATUS_REQUEST_BUTTON_COLUMN);
        rqStatColumn.setCellEditor(new ButtonEditor(new JButton()));
        rqStatColumn.setCellRenderer(new ButtonRenderer());   

        // format the last updated date time
        TableColumn timeColumn = tcm.getColumn(CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN);
        timeColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value != null && value instanceof Date) {
                    super.setValue(DATE_FORMAT.format((Date) value));
                } else {
                    super.setValue(value);
                }
            }
        });        
        
        // configure items for GUI
        eventModel.configureTable(eventTable);

        for (int i = 0; i < tcm.getColumnCount(); i++) {
            int colnumber=i;
            String colName = eventTable.getColumnName(colnumber);
            StayOpenCBItem showcol = new StayOpenCBItem(colName);
            colMenuList.add(showcol);
            showcol.setToolTipText(CbusEventTableDataModel.columnToolTips[i]);
            
            if ( i < 7 ) {
                //tcm.setColumnVisible(tcm.getColumnByModelIndex(colnumber), false);
                showcol.setSelected(true);
            }            
            if (
                colnumber == CbusEventTableDataModel.NAME_COLUMN ||
                colnumber == CbusEventTableDataModel.NODE_COLUMN ||
                colnumber == CbusEventTableDataModel.EVENT_COLUMN ||
                colnumber == CbusEventTableDataModel.NODENAME_COLUMN ||
                colnumber == CbusEventTableDataModel.COMMENT_COLUMN ||
                colnumber == CbusEventTableDataModel.STATE_COLUMN ||
                colnumber == CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN ||
                colnumber == CbusEventTableDataModel.ON_BUTTON_COLUMN ||
                colnumber == CbusEventTableDataModel.OFF_BUTTON_COLUMN ||
                colnumber == CbusEventTableDataModel.CANID_COLUMN ||
                colnumber == CbusEventTableDataModel.DELETE_BUTTON_COLUMN
            ) {
                evColMenu.add(showcol); // event columns
            }
            else if (
                colnumber==CbusEventTableDataModel.STATUS_REQUEST_BUTTON_COLUMN ||
                colnumber==CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN ||
                colnumber==CbusEventTableDataModel.SESSION_TOTAL_COLUMN ||
                colnumber==CbusEventTableDataModel.SESSION_ON_COLUMN ||
                colnumber==CbusEventTableDataModel.SESSION_OFF_COLUMN ||
                colnumber==CbusEventTableDataModel.SESSION_IN_COLUMN ||
                colnumber==CbusEventTableDataModel.SESSION_OUT_COLUMN
            ){
                evStatMenu.add(showcol); // count columns
            }
            else if (
                colnumber==CbusEventTableDataModel.STLR_ON_COLUMN ||
                colnumber==CbusEventTableDataModel.STLR_OFF_COLUMN
            ){
                evJmMenu.add(showcol);
            }
            
            showcol.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TableColumn column  = tcm.getColumnByModelIndex(colnumber);
                    boolean     visible = tcm.isColumnVisible(column);
                    tcm.setColumnVisible(column, !visible);
                }
            });
        }
        
        for (int ia = 7; ia < (CbusEventTableDataModel.MAX_COLUMN); ia++) {
            TableColumn column  = tcm.getColumnByModelIndex(ia);
            tcm.setColumnVisible(column, false);
        }        
        
        eventModel.addTableModelListener(this);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
        // main pane
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BorderLayout());
        
        JPanel toppanelcontainer = new JPanel();
        toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        // scroller for main table
        eventScroll = new JScrollPane(eventTable);
        eventScroll.setPreferredSize(new Dimension(450, 200));
        
        h.removeAllHighlights();

        // add new event
        neweventcontainer.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), (Bundle.getMessage("NewEvent"))));
        
        JPanel newnode = new JPanel();
        JPanel newev = new JPanel();

        newnode.add(new JLabel(Bundle.getMessage("CbusNode")));
        newnodenumberSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        JComponent comp = newnodenumberSpinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        newnodenumberSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkNewevent();
            }
        });
        newnode.add(newnodenumberSpinner);
        newnode.setToolTipText(Bundle.getMessage("NewNodeTip"));
        newnodenumberSpinner.setToolTipText(Bundle.getMessage("NewNodeTip"));
        
        newev.add(new JLabel(Bundle.getMessage("CbusEvent")));
        newevnumberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 65535, 1));
        JComponent compe = newevnumberSpinner.getEditor();
        JFormattedTextField fielde = (JFormattedTextField) compe.getComponent(0);
        DefaultFormatter formattere = (DefaultFormatter) fielde.getFormatter();
        formattere.setCommitsOnValidEdit(true);
        newevnumberSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkNewevent();
            }
        });
        
        newev.add(newevnumberSpinner);
        newevbutton = new JButton((Bundle.getMessage("NewEvent")));
        ActionListener newEventaction = ae -> {
            int ev = (Integer) newevnumberSpinner.getValue();
            int nd = (Integer) newnodenumberSpinner.getValue();
            eventModel.addEvent(nd,ev,0,CbusTableEvent.EvState.UNKNOWN,"","",0,0,0,0);
        };
        
        newevbutton.addActionListener(newEventaction);
        
        neweventcontainer.add(newnode);
        neweventcontainer.add(newev); 
        neweventcontainer.add(newevbutton);        
        
        filterpanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("FilterSurround")));
        
        JButton clearfilterButton = new JButton(Bundle.getMessage("ClearFilter"));
        ActionListener clearfilter = ae -> {
            filterText.setText("");
        };
        
        filterText.setMaximumSize( filterText.getPreferredSize() );
        
        clearfilterButton.addActionListener(clearfilter);
        
        filterpanel.add(filterText);
        filterpanel.add(clearfilterButton);
        
        toppanelcontainer.add(filterpanel);
        toppanelcontainer.add(neweventcontainer);
        pane1.add(toppanelcontainer, BorderLayout.PAGE_START);
   
        filterText.getDocument().addDocumentListener(new DocumentListener() {
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

            public void update(DocumentEvent e) {
                textForSearch = filterText.getText(); // better searches if not trimmed
                if (textForSearch.length() == 0) {
                    sorter.setRowFilter(null);
                    h.removeAllHighlights();
                } else {
                    try {
                        sorter.setRowFilter(
                        RowFilter.regexFilter("(?i)" + textForSearch)); // case insensitive
                    } catch (PatternSyntaxException pse) {
                        // log.error(" bad regex ");
                    }
                    updateLoghighlighter();
                }
            }
        });
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            eventScroll, scrolltablefeedback);
        split.setResizeWeight(_splitratio);
        split.setContinuousLayout(true);

        pane1.add(split, BorderLayout.CENTER);
        
        add(pane1);
        pane1.setVisible(true);
        
        eventTable.setDragEnabled(true);
        eventTable.setDropMode(DropMode.ON);
        eventTable.setTransferHandler(new CbusEventTableRowDnDHandler());
        
    }
    
    public void checkNewevent() {
        int newno = (Integer) newnodenumberSpinner.getValue();
        int newev = (Integer) newevnumberSpinner.getValue();
        
        if (( eventModel.seeIfEventOnTable(newno,newev) ) < 0 ) {
           // log.warn("enable");
            newevbutton.setEnabled(true);
        } else {
           // log.warn("disable");
            newevbutton.setEnabled(false);
        }
    }
    
    @Override
    public void tableChanged(TableModelEvent e) {
        checkNewevent();
        if (filterText.getText().length() != 0) {
            updateLoghighlighter();
        }
    }
  
    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.eventtable.EventTablePane";
    }
    
    /**
     * Creates a Menu List
     * <p>
     * File - Print, Print Preview, Save, SaveAs csv
     * Display - show / hide Create new event pane, show/hide bottom feedback pane
     * </p>
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        Frame mFrame = new Frame();

        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        JMenu printMenu = new JMenu(("Print"));
        JMenu displayMenu = new JMenu(Bundle.getMessage("Display"));
        JMenuItem saveItem = new JMenuItem(rb.getString("MenuItemSave")+" csv");

        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventModel.ta.saveTable();
            }
        });
        saveItem.setEnabled(eventModel.isTableDirty()); // disable menuItem if table was saved and has not changed since
        // model ta _saveFile == null check ?
        JMenuItem saveAsItem = new JMenuItem(rb.getString("MenuItemSaveAs")+ " csv");
        saveAsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventModel.ta.saveAsTable();
            }
        });

        // add print menu items
        JMenuItem printItem = new JMenuItem(rb.getString("PrintTable"));
        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HardcopyWriter writer = null;
                try {
                    writer = new HardcopyWriter(mFrame, getTitle(), 10, .8, .5, .5, .5, false);
                } catch (HardcopyWriter.PrintCanceledException ex) {
                    // log.debug("Print cancelled");
                    return;
                }
                writer.increaseLineSpacing(20);
                eventModel.ta.printTable(writer); // close() is taken care of in printTable()
            }
        });
        
        JMenuItem previewItem = new JMenuItem(rb.getString("PreviewTable"));

        previewItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HardcopyWriter writer = null;
                try {
                    writer = new HardcopyWriter(mFrame, getTitle(), 10, .8, .5, .5, .5, true);
                } catch (HardcopyWriter.PrintCanceledException ex) {
                    log.debug("Preview cancelled");
                    return;
                }
                writer.increaseLineSpacing(20);
                eventModel.ta.printTable(writer); // close() is taken care of in printTable()
            }
        });
        
        JCheckBoxMenuItem shownewevent = new JCheckBoxMenuItem((Bundle.getMessage("NewEvent")));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        shownewevent.setSelected(true);
        shownewevent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newEvShow = shownewevent.isSelected();
                // log.debug(" show new event checkbox show {} ", newEvShow);
                neweventcontainer.setVisible(newEvShow);
            }
        });
        
        JCheckBoxMenuItem showinfopanel = new JCheckBoxMenuItem(Bundle.getMessage("ShowInfoPanel"));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        showinfopanel.setSelected(true);
        showinfopanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean infoShow = showinfopanel.isSelected();
                
                scrolltablefeedback.setVisible(infoShow);
                validate();
                repaint();
                split.setDividerLocation(_splitratio);
            }
        });
        
        
        JCheckBoxMenuItem showfilterpanel = new JCheckBoxMenuItem(Bundle.getMessage("FilterSurround"));
        // shownewevent.setMnemonic(KeyEvent.VK_C);
        showfilterpanel.setSelected(true);
        showfilterpanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterpanel.setVisible(showfilterpanel.isSelected());
            }
        });

        fileMenu.add(saveItem);        
        fileMenu.add(saveAsItem);
        
        displayMenu.add(showfilterpanel);        
        displayMenu.add(shownewevent);
        displayMenu.add(showinfopanel);        
        
        printMenu.add(printItem);        
        printMenu.add(previewItem);        
        
        menuList.add(fileMenu);
        menuList.add(printMenu);
        menuList.add(evColMenu);
        menuList.add(evStatMenu);
        menuList.add(evJmMenu);
        menuList.add(displayMenu);
        return menuList;
    }

    /**
     * Cell Renderer for string table columns, highlights any text in filter input
     */    
    private TableCellRenderer getRenderer() {
        return new TableCellRenderer() {
            JTextField f = new JTextField();

            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object arg1, boolean isSelected, boolean hasFocus, 
                int row, int col) {
                String string="";
                if(arg1 != null){
                    string = arg1.toString();
                    try {
                        if (Integer.parseInt(string)==0){
                            string="";
                        }
                    } catch (NumberFormatException ex) {
                    }

                    f.setText(string);
                    // log.debug(" string :{}:",string );
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
                        } catch (BadLocationException e) {
                            log.warn(" badlocation exception", e);
                        }
                    }
                } else {
                    f.setText("");
                    f.getHighlighter().removeAllHighlights();
                }

                if (isSelected) {
                    f.setBackground( table.getSelectionBackground() );
                    
                } else {
                    f.setBackground( table.getBackground() );
                }
                
                if (hasFocus) {
                   f.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.blue));
                } else {
                    f.setBorder( table.getBorder() );
                }
                
                if ( arg1 instanceof CbusTableEvent.EvState ) {
                    if ( Objects.equals(arg1 , CbusTableEvent.EvState.ON )) {
                        f.setBackground( VERY_LIGHT_GREEN );
                        f.setText(Bundle.getMessage("CbusEventOn"));
                    }
                    else if ( Objects.equals(arg1 , CbusTableEvent.EvState.OFF )) {
                        f.setBackground( VERY_LIGHT_RED );
                        f.setText(Bundle.getMessage("CbusEventOff"));
                    }
                    else if ( Objects.equals(arg1 , CbusTableEvent.EvState.UNKNOWN )) {
                        f.setText("");
                    }
                }
                return f;
            }
        };
    }
    
    /**
     * Highlights text in table log based on the filter text
     */    
    private void updateLoghighlighter(){
        String feedbacktext = eventModel.tablefeedback().getText().toLowerCase();
        String textForSearch = filterText.getText().toLowerCase();        
        h.removeAllHighlights();

        Pattern pattern = Pattern.compile( Pattern.quote(textForSearch) );
        Matcher matcher = pattern.matcher(feedbacktext);                    

        while(matcher.find()) {
            try {
                h.addHighlight( matcher.start(), matcher.end(),
                    new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(Color.CYAN)
                );
            } catch (BadLocationException e) {
                log.warn("badlocation exception ",e);
            }
        }
    }

    /**
     * Checkbox item which does not appear to close the menu pane when clicked
     */  
    public static class StayOpenCBItem extends JCheckBoxMenuItem {
    
        private MenuElement[] path;
        {
            getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (getModel().isArmed() && isShowing()) {
                        path = MenuSelectionManager.defaultManager().getSelectedPath();
                    }
                }
            });
        }
    
        public StayOpenCBItem(String text) {
            super(text);
        }
    
        @Override
        public void doClick(int pressTime) {
            super.doClick(pressTime);
            MenuSelectionManager.defaultManager().setSelectedPath(path);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("EventTableTitle"));
        }
        return Bundle.getMessage("EventTableTitle");
    }
    
    protected void update() {
        log.warn("update called");
        eventModel.fireTableDataChanged();
        // TODO disable menuItem if table was saved and has not changed since
        // replacing menuItem by a new getMenus(). Note saveItem.setEnabled(eventModel.isTableDirty());
    }

    @Override
    public void dispose() {
        eventModel.removeTableModelListener(this);
        eventTable = null;
        eventScroll = null;
        super.dispose();
    }

    public static class CbusEventTableRowDnDHandler extends TransferHandler {
    
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    
        @Override
        public Transferable createTransferable(JComponent c) {
            
            if (!(c instanceof JTable )){
                return null;
            }
            
            JTable table = (JTable) c;
            int row = table.getSelectedRow();
            if (row < 0) {
                return null;
            }
            row = table.convertRowIndexToModel(row);
            
            int nn = (Integer) table.getModel().getValueAt(row, CbusEventTableDataModel.NODE_COLUMN); // node number
            int en = (Integer) table.getModel().getValueAt(row, CbusEventTableDataModel.EVENT_COLUMN); // event number
            
            StringBuilder jmriAddress = new StringBuilder(13);
            jmriAddress.append("+");
            if ( nn > 0 ) {
                jmriAddress.append("N");
                jmriAddress.append(nn);
                jmriAddress.append("E");
            }
            jmriAddress.append(en);
            
            return new StringSelection( jmriAddress.toString() );
            
        }

    }

    /**
     * Nested class to create one of these using old-style defaults.
     * Used as a startup action
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemEventTable"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusEventTablePane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventTablePane.class);

}
