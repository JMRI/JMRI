package jmri.jmrix.cmri.serial.cmrinetmanager;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.activation.DataHandler;

import java.util.ResourceBundle;

import javax.swing.border.Border;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.border.TitledBorder;
import jmri.jmrix.cmri.serial.cmrinetmetrics.CMRInetMetricsData;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Frame for CMRInet Network Metrics.
 * @author	 Chuck Catania   Copyright (C) 2016
 */
public class CMRInetMetricsFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetMetricsActionBundle");

    // node table pane items
    protected JPanel networkMetricsPanel = null;
    protected Border networkMetricsBorder = BorderFactory.createEtchedBorder();
    protected Border networkMetricsBorderTitled = 
                     BorderFactory.createTitledBorder(networkMetricsBorder,"Error Metrics",TitledBorder.LEFT,TitledBorder.ABOVE_TOP);  
    
    protected JPanel networkMetricsDataPanel = null;
    protected Border networkMetricsDataBorder = BorderFactory.createEtchedBorder();
    protected Border networkMetricsDataBorderTitled = 
                     BorderFactory.createTitledBorder(networkMetricsDataBorder,"Data Metrics",TitledBorder.LEFT,TitledBorder.ABOVE_TOP);  
    
    protected JTable netMetricsTable = null;
    protected TableModel netMetricsTableModel = null;
    
    protected JTable netMetricsDataTable = null;
    protected TableModel netMetricsDataTableModel = null;
    
    // button pane items
    JButton doneButton = new JButton(rb.getString("DoneButtonText") );
    JButton saveMetricsButton = new JButton(rb.getString("SaveMetricsButtonText") );
    JButton resetAllMetricsButton = new JButton(rb.getString("ResetAllMetricsButtonText") );

    final JFileChooser metricsSaveChooser = jmri.jmrit.XmlFile.userFileChooser();

    private CMRISystemConnectionMemo _memo = null;

    public CMRInetMetricsFrame(CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;
    }
    
   public void initComponents() throws Exception
    {
        // For the class
        setTitle(rb.getString("WindowTitle"));
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setPreferredSize(new Dimension(845,400)); // 415 375
//        setBackground(Color.LIGHT_GRAY);
                
        // Set up the CMRInet ERROR metrics table
        //---------------------------------------       
        networkMetricsPanel = new JPanel(); 
        networkMetricsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        networkMetricsPanel.setLayout(new BoxLayout(networkMetricsPanel, BoxLayout.PAGE_AXIS));
        networkMetricsPanel.setBorder(networkMetricsBorder);
       
        netMetricsTableModel = new NetMetricsTableModel();        
        netMetricsTable = new JTable(netMetricsTableModel);
        
        netMetricsTable.setPreferredScrollableViewportSize(new Dimension(400,150));  //400 150                  
        netMetricsTable.setFillsViewportHeight(true);
        
        netMetricsTable.setShowGrid(false);
        netMetricsTable.setGridColor(Color.BLACK);
        
        netMetricsTable.setBackground(Color.WHITE);
        netMetricsTable.setRowSelectionAllowed(false);
        netMetricsTable.setFont(new Font("Helvetica", Font.BOLD, 13));
        netMetricsTable.setRowHeight(30);
        netMetricsTable.getTableHeader().setReorderingAllowed(false);
        netMetricsTable.addMouseListener(new ErrMetricButtonMouseListener(netMetricsTable));
       
        JScrollPane netMetricsTableScrollPane = new JScrollPane(netMetricsTable);        
        networkMetricsPanel.add(netMetricsTableScrollPane,BorderLayout.LINE_START); 
        
        TableColumnModel netMetricsTableModel = netMetricsTable.getColumnModel();
                        
        DefaultTableCellRenderer dtcen = new DefaultTableCellRenderer();  
        dtcen.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer dtlft = new DefaultTableCellRenderer();  
        dtlft.setHorizontalAlignment(SwingConstants.LEFT);        
        DefaultTableCellRenderer dtrgt = new DefaultTableCellRenderer();  
        dtrgt.setHorizontalAlignment(SwingConstants.RIGHT);        
        TableCellRenderer rendererFromHeader = netMetricsTable.getTableHeader().getDefaultRenderer();

        JLabel headerLabel = (JLabel) rendererFromHeader;
        headerLabel.setHorizontalAlignment(JLabel.CENTER);

        TableColumn errorNameColumn = netMetricsTableModel.getColumn(NetMetricsTableModel.ERRORNAME_COLUMN );
        errorNameColumn.setMinWidth(200);
        errorNameColumn.setMaxWidth(200);
        errorNameColumn.setCellRenderer(dtlft);  
        errorNameColumn.setResizable(false);

        TableColumn errorCountColumn = netMetricsTableModel.getColumn(NetMetricsTableModel.ERRORCOUNT_COLUMN);
        errorCountColumn.setMinWidth(100);
        errorCountColumn.setMaxWidth(100);
        errorCountColumn.setCellRenderer(dtrgt);  
        errorCountColumn.setResizable(false);

        TableColumn blankColumn = netMetricsTableModel.getColumn(NetMetricsTableModel.BLANK_COLUMN);
        blankColumn.setMinWidth(20);
        blankColumn.setMaxWidth(20);
        blankColumn.setCellRenderer(dtrgt);  
        blankColumn.setResizable(false);

        TableCellRenderer buttonRenderer = new ErrMetricButtonRenderer();
        TableColumn resetCountColumn = netMetricsTableModel.getColumn(NetMetricsTableModel.ERRORRESET_COLUMN);                
        resetCountColumn.setMinWidth(80);
        resetCountColumn.setMaxWidth(80);        
        resetCountColumn.setCellRenderer(buttonRenderer);
        resetCountColumn.setResizable(false);
       
        networkMetricsPanel.setBorder(networkMetricsBorderTitled);
        networkMetricsPanel.setPreferredSize(new Dimension(415,300));  //425 300
        networkMetricsPanel.setVisible(true);
        
        add(networkMetricsPanel);
       
        //--------------------------------------
        // Set up the CMRInet metrics DATA table
        //--------------------------------------       
        networkMetricsDataPanel = new JPanel(); 
        networkMetricsDataPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        networkMetricsDataPanel.setLayout(new BoxLayout(networkMetricsDataPanel, BoxLayout.LINE_AXIS));
        networkMetricsDataPanel.setBorder(networkMetricsDataBorder);
     
        netMetricsDataTableModel = new NetMetricsDataTableModel();        
        netMetricsDataTable = new JTable(netMetricsDataTableModel);
        
        netMetricsDataTable.setPreferredScrollableViewportSize(new Dimension(400,150));  //400 150                  
        netMetricsDataTable.setFillsViewportHeight(true);
        
        netMetricsDataTable.setShowGrid(false);
        netMetricsDataTable.setGridColor(Color.BLACK);
        
        netMetricsDataTable.setBackground(Color.WHITE);
        netMetricsDataTable.setRowSelectionAllowed(false);
        netMetricsDataTable.setFont(new Font("Helvetica", Font.BOLD, 13));
        netMetricsDataTable.setRowHeight(30);
        netMetricsDataTable.getTableHeader().setReorderingAllowed(false);
        netMetricsDataTable.addMouseListener(new DataButtonMouseListener(netMetricsDataTable));
       
        JScrollPane netMetricsDataTableScrollPane = new JScrollPane(netMetricsDataTable);        
        networkMetricsDataPanel.add(netMetricsDataTableScrollPane,BorderLayout.LINE_START); 
        
        TableColumnModel netMetricsDataTableModel = netMetricsDataTable.getColumnModel();                        
        TableCellRenderer dataRendererFromHeader = netMetricsDataTable.getTableHeader().getDefaultRenderer();

        JLabel dataHeaderLabel = (JLabel) dataRendererFromHeader;
        dataHeaderLabel.setHorizontalAlignment(JLabel.CENTER);
        
        TableColumn dataNameColumn = netMetricsDataTableModel.getColumn(NetMetricsDataTableModel.DATANAME_COLUMN );
        dataNameColumn.setMinWidth(200);
        dataNameColumn.setMaxWidth(200);
        dataNameColumn.setCellRenderer(dtlft);  
        dataNameColumn.setResizable(false);

        TableColumn dataCountColumn = netMetricsDataTableModel.getColumn(NetMetricsDataTableModel.DATACOUNT_COLUMN);
        dataCountColumn.setMinWidth(100);
        dataCountColumn.setMaxWidth(100);
        dataCountColumn.setCellRenderer(dtrgt);  
        dataCountColumn.setResizable(false);

        TableColumn dataBlankColumn = netMetricsDataTableModel.getColumn(NetMetricsDataTableModel.DATABLANK_COLUMN);
        dataBlankColumn.setMinWidth(20);
        dataBlankColumn.setMaxWidth(20);
        dataBlankColumn.setCellRenderer(dtrgt);  
        dataBlankColumn.setResizable(false);

        TableCellRenderer dataButtonRenderer = new DataButtonRenderer();
        TableColumn dataResetCountColumn = netMetricsDataTableModel.getColumn(NetMetricsDataTableModel.DATARESET_COLUMN);                
        dataResetCountColumn.setMinWidth(80);
        dataResetCountColumn.setMaxWidth(80);        
        dataResetCountColumn.setCellRenderer(buttonRenderer);
        dataResetCountColumn.setResizable(false);
       
        networkMetricsDataPanel.setBorder(networkMetricsDataBorderTitled);
        networkMetricsDataPanel.setPreferredSize(new Dimension(415,300));  //425 300
        networkMetricsDataPanel.setVisible(true);
        
        add(networkMetricsDataPanel);
 
        // Main button panel
        //------------------ 
        JPanel mainButtons = new JPanel();
        mainButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainButtons.setPreferredSize(new Dimension(845, 50));

        saveMetricsButton.setVisible(true); 
        saveMetricsButton.setEnabled(true);
        saveMetricsButton.setToolTipText(rb.getString("SaveMetricsButtonText") );
	saveMetricsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e) {
					saveMetricsButtonActionPerformed(e);
				}
			});
	mainButtons.add(saveMetricsButton);
        
        resetAllMetricsButton.setVisible(true);
        resetAllMetricsButton.setToolTipText(rb.getString("ResetAllMetricsButtonText") );
	resetAllMetricsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e) {
					resetAllMetricsButtonActionPerformed(e);
				}
			});
	mainButtons.add(resetAllMetricsButton);

        doneButton.setVisible(true);
        doneButton.setToolTipText(rb.getString("DoneButtonTip") );
	doneButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e) {
					doneButtonActionPerformed();
				}
			});
	mainButtons.add(doneButton);
        mainButtons.setVisible(true);
        add(mainButtons);
        
        addHelpMenu("package.jmri.jmrix.cmri.serial.serialmon.CMRInetMetricsFrame", true);

        // pack for display
        //-----------------
        pack();
        
    }
    /**
     * ----------------------------
     * Network statistics window
     * ----------------------------
     */
    public void doneButtonActionPerformed() {
        setVisible(false);
        dispose();
    }
    
    /**
     * 
     * Save Metrics button handler
     * Metric data is saved to a text file named CMRInetMetrics_YYYYMMDD_HHMMSS
     * The file is text lines, one for each metric displayed and the count
     * 
     */
     volatile PrintStream logStream = null;
    
     public void saveMetricsButtonActionPerformed(ActionEvent e) {
        String fileName = "CMRInetMetrics_";
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        long curTicks = System.currentTimeMillis(); 
        
        // Create a unique name and open the save file dialog
        //---------------------------------------------------
        fileName = "CMRInetMetrics_"+df.format(curTicks)+".txt";
        metricsSaveChooser.setSelectedFile(new File(fileName));
        
        int retVal = metricsSaveChooser.showSaveDialog(null);
        if (retVal == JFileChooser.APPROVE_OPTION)
        {
            try 
            {
              // Open the file and write the metric data
              //----------------------------------------
              logStream = new PrintStream (new FileOutputStream(metricsSaveChooser.getSelectedFile()));
              logStream.println("-----  CMRInet Error Metrics  -----");
              for (int i=0; i!=CMRInetMetricsData.CMRInetMetricErrLAST; i++)
              {
                logStream.println(String.format("%-30s %d",
                                  CMRInetMetricsData.CMRInetMetricErrName[i],
                                  CMRInetMetricsData.getMetricErrValue(i)));
              }
              logStream.print("\n"+"\n");
              logStream.println("-----  CMRInet Data Metrics  -----");
              for (int i=0; i!=CMRInetMetricsData.CMRInetMetricDataLAST; i++)
              {
                logStream.println(String.format("%-30s %d",
                                  CMRInetMetricsData.CMRInetMetricDataName[i],
                                  CMRInetMetricsData.getMetricDataValue(i)));
              }
              
              // Close the metrics log file
              //---------------------------
              synchronized (logStream)
              {
                logStream.flush();
                logStream.close();
              }   

            } catch (Exception ex)
            {
                log.error("exception "+ex);
            }
        }
    }


    /**
     * ------------------------------
     * Reset All Metrics button handler
     * ------------------------------
     */
    public void resetAllMetricsButtonActionPerformed(ActionEvent e) {
        CMRInetMetricsData.clearAllDataMetrics();
        CMRInetMetricsData.clearAllErrMetrics();
    }
    
    
    /**
     * Set up table for displaying the Error metrics
     */
    public class NetMetricsTableModel extends AbstractTableModel
    {
        
        public String getColumnName(int c) {return CMRInetMetricsErrColumnsNames[c];}
        public Class<?> getColumnClass(int r,int c) {
            switch (c) {
                case ERRORNAME_COLUMN :
                    return String.class;
                case ERRORCOUNT_COLUMN :
                    return Integer.class;
                case ERRORRESET_COLUMN :
                    return Object.class;
                default:
                    return Object.class;
            }
        };
	public boolean isCellEditable(int r,int c) { return false;}
        public int getColumnCount () {return NUMCOLUMNS;}
        public int getRowCount () {return CMRInetMetricsData.CMRInetMetricErrName.length;}
        public Object getValueAt (final int r,int c)
        {           
            switch (c)
            {
                case ERRORNAME_COLUMN :
                    return CMRInetMetricsData.CMRInetMetricErrName[r]; 
                case ERRORCOUNT_COLUMN :
                    return CMRInetMetricsData.CMRInetMetricErrCount[r];
                case ERRORRESET_COLUMN :
                    final JButton button = new JButton(CMRInetMetricsErrColumnsNames[c]);
                    button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) 
                        {
                          CMRInetMetricsData.zeroMetricErrValue( r );
                        }
                    });
                    
                    fireTableDataChanged();
                    return button;

                default:
                    return " ";
            }
            
        }
        
	public void setValueAt(int value, int r, int c)
        {
            switch (c)
            {
                case ERRORCOUNT_COLUMN :
                    CMRInetMetricsData.CMRInetMetricErrCount[r] = value;
                default:
            }
            fireTableDataChanged();
         }

        public static final int ERRORNAME_COLUMN   = 0;
        public static final int ERRORCOUNT_COLUMN  = 1;
        public static final int BLANK_COLUMN       = 2;
        public static final int ERRORRESET_COLUMN  = 3;
        public static final int NUMCOLUMNS = ERRORRESET_COLUMN+1;
                       
    }
	private static class ErrMetricButtonRenderer implements TableCellRenderer {		
		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JButton button = (JButton)value;
			if (isSelected)
                        {
                            button.setForeground(table.getSelectionForeground());
                            button.setBackground(table.getSelectionBackground());
                        } else 
                        {
                            button.setForeground(table.getForeground());
                            button.setBackground(UIManager.getColor("Button.background"));
                        }
                        
			return button;	
		}
	}
	
	private static class ErrMetricButtonMouseListener extends MouseAdapter {
		private final JTable table;
		
		public ErrMetricButtonMouseListener(JTable table) {
			this.table = table;
		}

		public void mouseClicked(MouseEvent e) {
			int column = table.getColumnModel().getColumnIndexAtX(e.getX());
			int row    = e.getY()/table.getRowHeight(); 

			if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0)
                        {
			    Object value = table.getValueAt(row, column);
			    if (value instanceof JButton)
                            {
			    	((JButton)value).doClick();
			    }
			}
		}
	}
 
            /**
     * Set up table for displaying the Error metrics
     */
    public class NetMetricsDataTableModel extends AbstractTableModel
    {
        
        public String getColumnName(int c) {return CMRInetMetricsDataColumnsNames[c];}
        public Class<?> getColumnClass(int r,int c) {
            switch (c) {
                case DATANAME_COLUMN :
                    return String.class;
                case DATACOUNT_COLUMN :
                    return Integer.class;
                case DATARESET_COLUMN :
                    return Object.class;
                default:
                    return Object.class;
            }
        };
	public boolean isCellEditable(int r,int c) { return false;}
        public int getColumnCount () {return DATANUMCOLUMNS;}
        public int getRowCount () {return CMRInetMetricsData.CMRInetMetricDataName.length;}
        public Object getValueAt (final int r,int c)
        {           
            switch (c)
            {
                case DATANAME_COLUMN :
                    return CMRInetMetricsData.CMRInetMetricDataName[r]; 
                case DATACOUNT_COLUMN :
                    return CMRInetMetricsData.CMRInetMetricDataCount[r];
                case DATARESET_COLUMN :
                    final JButton button = new JButton(CMRInetMetricsDataColumnsNames[c]);
                    button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) 
                        {
                          CMRInetMetricsData.zeroMetricDataValue( r );
                        }
                    });
                    
                    fireTableDataChanged();
                    return button;

                default:
                    return " ";
            }
            
        }
        
	public void setValueAt(int value, int r, int c)
        {
            switch (c)
            {
                case DATACOUNT_COLUMN :
                    CMRInetMetricsData.CMRInetMetricDataCount[r] = value;
                default:
            }
            fireTableDataChanged();
         }

        public static final int DATANAME_COLUMN   = 0;
        public static final int DATACOUNT_COLUMN  = 1;
        public static final int DATABLANK_COLUMN  = 2;
        public static final int DATARESET_COLUMN  = 3;
        public static final int DATANUMCOLUMNS = DATARESET_COLUMN+1;
                       
}

	private static class DataButtonRenderer implements TableCellRenderer {		
		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JButton button = (JButton)value;
			if (isSelected)
                        {
                            button.setForeground(table.getSelectionForeground());
                            button.setBackground(table.getSelectionBackground());
                        } else 
                        {
                            button.setForeground(table.getForeground());
                            button.setBackground(UIManager.getColor("Button.background"));
                        }
                        
			return button;	
		}
	}
	
	private static class DataButtonMouseListener extends MouseAdapter {
		private final JTable table;
		
		public DataButtonMouseListener(JTable table) {
			this.table = table;
		}

		public void mouseClicked(MouseEvent e) {
			int column = table.getColumnModel().getColumnIndexAtX(e.getX());
			int row    = e.getY()/table.getRowHeight(); 

			if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0)
                        {
			    Object value = table.getValueAt(row, column);
			    if (value instanceof JButton)
                            {
			    	((JButton)value).doClick();
			    }
			}
		}
	}
      
    private String[] CMRInetMetricsErrColumnsNames  = {"Error","Count"," ","Reset"};
    private String[] CMRInetMetricsDataColumnsNames = {"Metric","Count"," ","Reset"};

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CMRInetMetricsFrame.class.getName());
	
}

/* @(#)CMRInetMetricsFrame.java */
