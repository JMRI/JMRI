package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.io.File;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.FileUtil;
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
public class CbusEventTablePane extends jmri.jmrix.can.swing.CanPanel {

    protected CbusEventTableDataModel eventModel=null;
    protected JTable eventTable=null;
    protected JScrollPane eventScroll;
    protected JSplitPane split;
    protected JPanel pane1;
    protected JPanel toppanelcontainer;
    protected JPanel neweventcontainer = new JPanel();
    protected JPanel filterpanel = new JPanel();
    protected JFileChooser fc = new JFileChooser(FileUtil.getUserFilesPath());
    protected final static JTextField filterText = new JTextField("",8);
    private javax.swing.filechooser.FileNameExtensionFilter fcuxmlfilter = new javax.swing.filechooser.FileNameExtensionFilter(
        "FCU xml", "xml");
    
    private String textForSearch = "";
    private double _splitratio = 0.95;
    static private int MAX_LINES = 500; // tablefeedback screen log size
    protected static TextAreaFIFO tablefeedback = new TextAreaFIFO(MAX_LINES);
    protected JScrollPane scrolltablefeedback = new JScrollPane (tablefeedback);    
    private DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS dd/MM/yy");
    public static final Color VERY_LIGHT_RED = new Color(255,176,173);
    public static final Color VERY_LIGHT_GREEN = new Color(165,255,164);
    public static final Color GOLD = new Color(255,204,51);
    
    protected Highlighter tableFeedbackHighlighter;
    protected static Highlighter h = tablefeedback.getHighlighter();

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        eventModel = new CbusEventTableDataModel(memo, 10,
                CbusEventTableDataModel.MAX_COLUMN); // controller, row, column
        init();
    }

    public CbusEventTablePane() {
        super();
    }

    public void init() {
        
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
                            return eventModel.columnToolTips[realIndex];    
                        } catch (RuntimeException e1) {
                            //catch null pointer exception if mouse is over an empty line
                        }
                        return null;
                    }
                };
            }
        };

        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        eventTable.setColumnModel(tcm);
        
        eventTable.setAutoCreateRowSorter(true);
        
        final TableRowSorter<CbusEventTableDataModel> sorter = new TableRowSorter<CbusEventTableDataModel>(eventModel);
        eventTable.setRowSorter(sorter);
        
        eventTable.createDefaultColumnsFromModel();
        
        eventTable.setRowSelectionAllowed(true);
        eventTable.setColumnSelectionAllowed(false);
        eventTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        TableColumnModel eventTableModel = eventTable.getColumnModel();
        
        TableColumn evNaColumn = eventTableModel.getColumn(CbusEventTableDataModel.NAME_COLUMN);
        evNaColumn.setCellRenderer(getRenderer());
        TableColumn ndNaColumn = eventTableModel.getColumn(CbusEventTableDataModel.NODENAME_COLUMN);
        ndNaColumn.setCellRenderer(getRenderer());        
        TableColumn cmntColumn = eventTableModel.getColumn(CbusEventTableDataModel.COMMENT_COLUMN);                
        cmntColumn.setCellRenderer(getRenderer());         
        TableColumn evColumn = eventTableModel.getColumn(CbusEventTableDataModel.NODE_COLUMN);
        evColumn.setCellRenderer(getRenderer());        
        TableColumn ndColumn = eventTableModel.getColumn(CbusEventTableDataModel.EVENT_COLUMN);                
        ndColumn.setCellRenderer(getRenderer());
        
        TableColumn typeColumn = eventTableModel.getColumn(CbusEventTableDataModel.TYPE_COLUMN);                
        typeColumn.setCellRenderer(new TypeRenderer());
        
        TableColumn fbreqColumn = eventTableModel.getColumn(CbusEventTableDataModel.FEEDBACKREQUIRED_COLUMN);                
        fbreqColumn.setCellRenderer(new OsRenderer());        
        TableColumn osColumn = eventTableModel.getColumn(CbusEventTableDataModel.FEEDBACKOUTSTANDING_COLUMN);                
        osColumn.setCellRenderer(new OsRenderer());
        TableColumn fbEvColumn = eventTableModel.getColumn(CbusEventTableDataModel.FEEDBACKEVENT_COLUMN);
        fbEvColumn.setCellRenderer(new OsRenderer());        
        TableColumn fbNdColumn = eventTableModel.getColumn(CbusEventTableDataModel.FEEDBACKNODE_COLUMN);                
        fbNdColumn.setCellRenderer(new OsRenderer());
        TableColumn fbToColumn = eventTableModel.getColumn(CbusEventTableDataModel.FEEDBACKTIMEOUT_COLUMN);                
        fbToColumn.setCellRenderer(new OsRenderer());
        TableColumn laFbColumn = eventTableModel.getColumn(CbusEventTableDataModel.LASTFEEDBACK_COLUMN);                
        laFbColumn.setCellRenderer(new LafbRenderer());
        
        TableColumn caIdColumn = eventTableModel.getColumn(CbusEventTableDataModel.CANID_COLUMN);
        TableColumn seOnColumn = eventTableModel.getColumn(CbusEventTableDataModel.SESSION_ON_COLUMN);
        TableColumn seOffColumn = eventTableModel.getColumn(CbusEventTableDataModel.SESSION_OFF_COLUMN);
        
        TableColumn delBColumn = eventTableModel.getColumn(CbusEventTableDataModel.DELETE_BUTTON_COLUMN);
        delBColumn.setCellEditor(new ButtonEditor(new JButton()));
        delBColumn.setCellRenderer(new ButtonRenderer());
        
        TableColumn onBColumn = eventTableModel.getColumn(CbusEventTableDataModel.ON_BUTTON_COLUMN);
        onBColumn.setCellEditor(new ButtonEditor(new JButton()));
        onBColumn.setCellRenderer(new ButtonRenderer());
        
        TableColumn offBColumn = eventTableModel.getColumn(CbusEventTableDataModel.OFF_BUTTON_COLUMN);
        offBColumn.setCellEditor(new ButtonEditor(new JButton()));
        offBColumn.setCellRenderer(new ButtonRenderer());    
        
        TableColumn togBColumn = eventTableModel.getColumn(CbusEventTableDataModel.TOGGLE_BUTTON_COLUMN);
        togBColumn.setCellEditor(new ButtonEditor(new JButton()));
        togBColumn.setCellRenderer(new ButtonRenderer());        
        
        TableColumn rqStatColumn = eventTableModel.getColumn(CbusEventTableDataModel.STATUS_REQUEST_BUTTON_COLUMN);
        rqStatColumn.setCellEditor(new ButtonEditor(new JButton()));
        rqStatColumn.setCellRenderer(new ButtonRenderer());   

        // format the last updated date time
        TableColumn timeColumn = eventTableModel.getColumn(CbusEventTableDataModel.LATEST_TIMESTAMP_COLUMN);
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

        // general GUI config
        
        tcm.setColumnVisible(caIdColumn, false);
        tcm.setColumnVisible(delBColumn, false);
        tcm.setColumnVisible(onBColumn, false);
        tcm.setColumnVisible(offBColumn, false);
        tcm.setColumnVisible(seOnColumn, false);
        tcm.setColumnVisible(seOffColumn, false);
        
        addMouseListenerToHeader(eventTable);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
        // main pane
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BorderLayout());
        
        JPanel toppanelcontainer = new JPanel();
        toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        // scroller for main table
        eventScroll = new JScrollPane(eventTable);
        eventScroll.setPreferredSize(new Dimension(450, 200));
        
        tablefeedback.setEditable ( false ); // set textArea non-editable
       

        h.removeAllHighlights();

        
        // add new event
        neweventcontainer.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), (Bundle.getMessage("NewEvent"))));
        
        JPanel newnode = new JPanel();
        JPanel newev = new JPanel();

        
        newnode.add(new JLabel(Bundle.getMessage("CbusNode")));
        JSpinner newnodenumberSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
        newnode.add(newnodenumberSpinner);
        newnode.setToolTipText(Bundle.getMessage("NewNodeTip"));
        newnodenumberSpinner.setToolTipText(Bundle.getMessage("NewNodeTip"));
        
        newev.add(new JLabel(Bundle.getMessage("CbusEvent")));
        JSpinner newevnumberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 65535, 1));
        newev.add(newevnumberSpinner);
        
        JButton newevbutton = new JButton((Bundle.getMessage("NewEvent")));
        
        ActionListener newEventaction = ae -> {
            int ev = (Integer) newevnumberSpinner.getValue();
            int nd = (Integer) newnodenumberSpinner.getValue();
            // log.warn("new event button clicked ev {} nd {} ");
            int response=eventModel.newEventFromButton(ev,nd);
            if (response==-1){
                JOptionPane.showMessageDialog(null, 
                (Bundle.getMessage("NoMakeEvent")), Bundle.getMessage("WarningTitle"),
                JOptionPane.ERROR_MESSAGE);
            }
        };
        
        newevbutton.addActionListener(newEventaction);
        
        neweventcontainer.add(newev);        
        neweventcontainer.add(newnode);
        neweventcontainer.add(newevbutton);        
        
 

        filterpanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("FilterSurround")));
        
       // JLabel label = new JLabel("Filter");
        //filterpanel.add(label, BorderLayout.WEST);

        
        
        
        
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
                eventModel.saveTable();
            }
        });
        saveItem.setEnabled(eventModel.isTableDirty()); // disable menuItem if table was saved and has not changed since

        JMenuItem saveAsItem = new JMenuItem(rb.getString("MenuItemSaveAs")+ " csv");
        saveAsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventModel.saveAsTable();
            }
        });

        
        
        JMenuItem mnItemOpenFile = new JMenuItem(Bundle.getMessage("ImportMenuMergFcu147")); //  FCU 
        mnItemOpenFile.setToolTipText("Tested FCU v 1.4.7.42 - v 1.4.7.45"); // NOT I18N
        mnItemOpenFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String myFile = fileChooser();
                if (myFile!=null) {
                    eventModel.readTheFCU14742File(myFile);
                }
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
                eventModel.printTable(writer); // close() is taken care of in printTable()
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
                eventModel.printTable(writer); // close() is taken care of in printTable()
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
                // log.debug(" showinfopanel checkbox show {} ", newEvShow);
                
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
        fileMenu.add(new JSeparator()); // SEPARATOR
        fileMenu.add(mnItemOpenFile); // FCU Import
        
        displayMenu.add(showfilterpanel);        
        displayMenu.add(shownewevent);
        displayMenu.add(showinfopanel);        
        
        printMenu.add(printItem);        
        printMenu.add(previewItem);        
        
        menuList.add(fileMenu);
        menuList.add(printMenu);
        menuList.add(displayMenu);
        return menuList;
    }

    /**
     * Get file to read from
     */    
    public String fileChooser(){
        // Get file to read from
        fc.setFileFilter(fcuxmlfilter);
        int retVal = fc.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return ""; // Canceled
        }
        if (fc.getSelectedFile() == null) {
            return ""; // Canceled
        }
        File f = fc.getSelectedFile();
        String fileName = f.getAbsolutePath();
        return fileName;
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
                
                return f;
            }
        };
    }
    
    /**
     * Sets background for off / on column for easier read
     */
    protected static class TypeRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (Objects.equals(value.toString(),Bundle.getMessage("CbusEventOn"))) {
                cellComponent.setBackground( VERY_LIGHT_GREEN );
            }
            else if (Objects.equals(value.toString(),Bundle.getMessage("CbusEventOff"))) {
                cellComponent.setBackground( VERY_LIGHT_RED );
            }
            else {
                if (isSelected) {
                    cellComponent.setBackground( table.getSelectionBackground() );
                } else {
                    cellComponent.setBackground( table.getBackground() );
                }
            }
            return cellComponent;
        }
    }

    
    
    /**
     * Sets background to spot the lesser used number columns
     */    
    protected static class OsRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int val = (int) value;
            
            if (val>0) {
                cellComponent.setBackground( GOLD );
                cellComponent.setForeground( table.getForeground() );                
            }
            else {
                if (isSelected) {
                    cellComponent.setBackground( table.getSelectionBackground() );
                    cellComponent.setForeground( table.getSelectionBackground() );                    
                } else {
                    cellComponent.setBackground( table.getBackground() );
                    cellComponent.setForeground( table.getBackground() );
                }
            }
            return cellComponent;
        }
    }    
    
    
    /**
     * Sets background to last feedback column
     */    
    protected static class LafbRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (Objects.equals(value.toString(),Bundle.getMessage("LfbFinding"))) {
                cellComponent.setBackground( GOLD );
                cellComponent.setForeground( table.getForeground() );                
            }
            else if (Objects.equals(value.toString(),Bundle.getMessage("LfbGood"))) {
                cellComponent.setBackground( VERY_LIGHT_GREEN );
                cellComponent.setForeground( table.getForeground() );                
            }
            else if (Objects.equals(value.toString(),Bundle.getMessage("LfbBad"))) {
                cellComponent.setBackground( VERY_LIGHT_RED );
                cellComponent.setForeground( table.getForeground() );                
            }
            
            else {
                if (isSelected) {
                    cellComponent.setBackground( table.getSelectionBackground() );
                    cellComponent.setForeground( table.getSelectionBackground() );                    
                } else {
                    cellComponent.setBackground( table.getBackground() );
                    cellComponent.setForeground( table.getBackground() );
                }
            }
            return cellComponent;
        }
    }    
    
    
    /**
     * Highlights text in table log based on the filter text
     */    
    private static void updateLoghighlighter(){
        String feedbacktext = tablefeedback.getText().toLowerCase();
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
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("EventTableTitle"));
        }
        return Bundle.getMessage("EventTableTitle");
    }
    
    
    /**
     * Process the column header click
     * @param e     the event data
     * @param table the JTable
     */
    protected void showTableHeaderPopup(MouseEvent e, JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        XTableColumnModel tcm = (XTableColumnModel) table.getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(false); i++) {
            TableColumn tc = tcm.getColumnByModelIndex(i);
            String columnName = table.getModel().getColumnName(i);
            String xtTooltip = eventModel.columnToolTips[i];
            if (columnName != null && !columnName.equals("")) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(
                    (table.getModel().getColumnName(i) + " : " + xtTooltip), 
                    tcm.isColumnVisible(tc));
                menuItem.addActionListener(new HeaderActionListener(tc, tcm));
                popupMenu.add(menuItem);
            }
        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Adds the column header pop listener to a JTable using XTableColumnModel
     * @param table The JTable effected.
     */
    protected void addMouseListenerToHeader(JTable table) {
        MouseListener mouseHeaderListener = new TableHeaderListener(table);
        table.getTableHeader().addMouseListener(mouseHeaderListener);
    }
    
    /**
     * Class to support listner for popup menu on XTableColum model.
     */
    static class HeaderActionListener implements ActionListener {

        TableColumn tc;
        XTableColumnModel tcm;

        HeaderActionListener(TableColumn tc, XTableColumnModel tcm) {
            this.tc = tc;
            this.tcm = tcm;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
            //Do not allow the last column to be hidden
            if (!check.isSelected() && tcm.getColumnCount(true) == 1) {
                return;
            }
            tcm.setColumnVisible(tc, check.isSelected());
        }
    }

    /**
     * Class to support Columnheader popup menu on XTableColum model.
     */
    class TableHeaderListener extends MouseAdapter {

        JTable table;

        TableHeaderListener(JTable tbl) {
            super();
            table = tbl;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showTableHeaderPopup(e, table);
            }
        }
    }
    
    public void update() {
        eventModel.fireTableDataChanged();
        // TODO disable menuItem if table was saved and has not changed since
        // replacing menuItem by a new getMenus(). Note saveItem.setEnabled(eventModel.isTableDirty());
    }

    private boolean mShown = false;

    protected static void updateLogFromModel(int cbuserror, String cbustext){
        
       // log.warn("in event table error = {} text = {} length = {} ",cbuserror,cbustext,cbustext.length());
        // tablefeedback.append("\n");
        
        if (cbuserror==3) {
            tablefeedback.append ("\n * * * * * * * * * * * * * * * * * * * * * * " + cbustext);
        } else {
            tablefeedback.append( "\n"+cbustext);
        }
        
       // textForSearch = filterText.getText(); // better searches if not trimmed
        if (filterText.getText().length() != 0) {
            updateLoghighlighter();
        }
    }

    
    // on startup
    @Override
    public void addNotify() {
        super.addNotify();        
        if (mShown) {
            return;
        }

        mShown = true;
    }

    @Override
    public void dispose() {
        String className = this.getClass().getSimpleName();
        log.debug("dispose called {} ",className);
        
        eventModel.dispose();
        eventModel = null;
        eventTable = null;
        eventScroll = null;
        super.dispose();
    }

    

    
    /**
     * Keeps the message log windows to a reasonable length
     * https://community.oracle.com/thread/1373400
     */
    protected static class TextAreaFIFO extends JTextArea implements DocumentListener {
        private int maxLines;
    
        public TextAreaFIFO(int lines) {
            maxLines = lines;
            getDocument().addDocumentListener( this );
        }
    
        public void insertUpdate(DocumentEvent e) {
            javax.swing.SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    removeLines();
                }
            });
        }
        public void removeUpdate(DocumentEvent e) {}
        public void changedUpdate(DocumentEvent e) {}
        public void removeLines()
        {
            Element root = getDocument().getDefaultRootElement();
            while (root.getElementCount() > maxLines) {
                Element firstLine = root.getElement(0);
                try {
                    getDocument().remove(0, firstLine.getEndOffset());
                } catch(BadLocationException ble) {
                    System.out.println(ble);
                }
            }
        setCaretPosition( getDocument().getLength() );
        }
    }
    
    
    /**
     * Nested class to create one of these using old-style defaults.
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
