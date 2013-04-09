package jmri.jmrit.logix;

//import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickPanel;
import jmri.util.com.sun.TableSorter;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

public class TrackerTableAction extends AbstractAction {

    static int STRUT_SIZE = 10;
    
    private static TrackerTableAction _instance;
    private static ArrayList<Tracker> _trackerList = new ArrayList<Tracker>();
    private static TableFrame _frame;
    
    public TrackerTableAction(String menuOption) {
	    super(menuOption);
	    _instance = this;
    }
    static TrackerTableAction getInstance() {
    	if (_instance==null) {
    		_instance = new TrackerTableAction("Tracker");
    	}
    	return _instance;
    }
    public void actionPerformed(ActionEvent e) {
    	if (_frame!=null) {
    		_frame.setVisible(true);
    	} else {
        	_frame = new TableFrame();    		
    	}
    }
    synchronized static public void mouseClickedOnBlock(OBlock block) {
    	if (_frame!=null) {
    		_frame.mouseClickedOnBlock(block);
    	}
    }
    
    /**
     * Holds a table of Trackers that follow adjacent occupancy.  Needs to be 
     * a singleton to be opened and closed for trackers to report to it
     * @author Peter Cressman
     *
     */
    class TableFrame extends JmriJFrame implements PropertyChangeListener 
    {
        private TrackerTableModel _model;
        private JmriJFrame _pickFrame;
        JDialog _dialog;
        JTextField  _trainNameBox = new JTextField(30);
        JTextField  _trainLocationBox = new JTextField(30);
        JTextField  _status = new JTextField(80);
        boolean		_appendStatus = false;

        TableFrame() 
        {
            setTitle(Bundle.getMessage("TrackerTable"));
            _model = new TrackerTableModel(this);
            JTable table;   // = new JTable(_model);
            try {   // following might fail due to a missing method on Mac Classic
            	TableSorter sorter = new jmri.util.com.sun.TableSorter(_model);
                table = jmri.util.JTableUtil.sortableDataModel(sorter);
                sorter.setTableHeader(table.getTableHeader());
                // set model last so later casts will work
                ((jmri.util.com.sun.TableSorter)table.getModel()).setTableModel(_model);
            } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
                log.error("WarrantTable: Unexpected error: "+e);
                table = new JTable(_model);
            }
            table.getColumnModel().getColumn(TrackerTableModel.STOP_COL).setCellEditor(new ButtonEditor(new JButton()));
            table.getColumnModel().getColumn(TrackerTableModel.STOP_COL).setCellRenderer(new ButtonRenderer());
            for (int i=0; i<_model.getColumnCount(); i++) {
                int width = _model.getPreferredWidth(i);
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            table.setDragEnabled(true);
            table.setTransferHandler(new jmri.util.DnDTableExportHandler());
            JScrollPane tablePane = new JScrollPane(table);
            Dimension dim = table.getPreferredSize();
            table.getRowHeight(new JButton("STOPIT").getPreferredSize().height);
            dim.height = table.getRowHeight()*12;
            tablePane.getViewport().setPreferredSize(dim);

            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
            JLabel title = new JLabel(Bundle.getMessage("TrackerTable"));
            tablePanel.add(title, BorderLayout.NORTH);
            tablePanel.add(tablePane, BorderLayout.CENTER);

            JPanel panel = new JPanel();
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("lastEvent")));
            p.add(_status);
            _status.setEditable(false);
            panel.add(p);

            p = new JPanel();
            JButton button = new JButton(Bundle.getMessage("MenuNewTracker"));
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                    	newTrackerDialog();
                    }
            });
            tablePanel.add(p, BorderLayout.CENTER);
            p.add(button);
            
            button = new JButton(Bundle.getMessage("MenuRefresh"));
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                    	_model.fireTableDataChanged();
                    }
            });
            tablePanel.add(p, BorderLayout.CENTER);
            p.add(button);
            
            button = new JButton(Bundle.getMessage("MenuBlockPicker"));
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                    	openPickList();
                    }
            });
            tablePanel.add(p, BorderLayout.CENTER);
            p.add(button);
            
            panel.add(p);
            tablePanel.add(panel, BorderLayout.CENTER);
            
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dispose();
                }
            });
            /*
            JMenuBar menuBar = new JMenuBar();
            JMenu trackerMenu = new JMenu(Bundle.getMessage("MenuTrackers"));
            JMenuItem newTracker = new JMenuItem(Bundle.getMessage("MenuNewTracker"));
            newTracker.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                    	newTrackerDialog();
                    }
                });
            trackerMenu.add(newTracker);
            JMenuItem picker = new JMenuItem(Bundle.getMessage("MenuBlockPicker"));
            picker.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                    	openPickList();
                    }
                });
            trackerMenu.add(picker);
            JMenuItem refresh = new JMenuItem(Bundle.getMessage("MenuRefresh"));
            refresh.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                    	_model.fireTableDataChanged();
                    }
                });
            trackerMenu.add(refresh);
            menuBar.add(trackerMenu);
            setJMenuBar(menuBar);
//            addHelpMenu("package.jmri.jmrit.logix.Tracker", true);
 */
            setContentPane(tablePanel);
            
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                	_model.fireTableDataChanged();
                }
            });
            setLocation(0,100);
            setVisible(true);
            pack();
        }
        protected void mouseClickedOnBlock(OBlock block) {
        	if (_dialog!=null) {
        		_trainLocationBox.setText(block.getDisplayName());
        	}
        }
   	/**
	    * Create a new OBlock
	    * Used by New to set up _editCircuitFrame
	    * Sets _currentBlock to created new OBlock
	    */
	    private void newTrackerDialog() {
	        _dialog = new JDialog(this, Bundle.getMessage("MenuNewTracker"), false);
	        JPanel panel = new JPanel();
	        panel.setLayout(new BorderLayout(10,10));
	        JPanel mainPanel = new JPanel();
	        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

	        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
	        JPanel p = new JPanel();
	        p.add(new JLabel(Bundle.getMessage("createTracker")));
	        mainPanel.add(p);

	        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
	        mainPanel.add(makeTrackerNamePanel());
	        mainPanel.add(Box.createVerticalStrut(STRUT_SIZE));
	        mainPanel.add(makeDoneButtonPanel());
	        panel.add(mainPanel);
	        _dialog.getContentPane().add(panel);
	        _dialog.setLocation(this.getLocation().x+100, this.getLocation().y+100);
	        _dialog.pack();
	        _dialog.setVisible(true);
	    }
	    
	    private JPanel makeTrackerNamePanel() {
	    	_trainNameBox.setText("");
	    	_trainLocationBox.setText("");
	        JPanel namePanel = new JPanel();
	        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
	        JPanel p = new JPanel(); 
	        p.setLayout(new java.awt.GridBagLayout());
	        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
	        c.gridwidth  = 1;
	        c.gridheight = 1;
	        c.gridx = 0;
	        c.gridy = 0;
	        c.anchor = java.awt.GridBagConstraints.EAST;
	        p.add(new JLabel(Bundle.getMessage("TrainName")), c);
	        c.gridy = 1;
	        p.add(new JLabel(Bundle.getMessage("TrainLocation")),c);
	        c.gridx = 1;
	        c.gridy = 0;
	        c.anchor = java.awt.GridBagConstraints.WEST;
	        c.weightx = 1.0;
	        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
	        p.add(_trainNameBox,c);
	        c.gridy = 1;
	        p.add(_trainLocationBox,c);
	        namePanel.add(p);
	        return namePanel;
	    }

	    private JPanel makeDoneButtonPanel() {
	        JPanel buttonPanel = new JPanel();
	        JPanel panel0 = new JPanel();
	        panel0.setLayout(new FlowLayout());
	        JButton doneButton;
            doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        if (doDoneAction()) {
                            _dialog.dispose();
                            _dialog = null;
                        }
                    }
            });
	        panel0.add(doneButton);
	        buttonPanel.add(panel0);
	        return buttonPanel;
	    }

	    private boolean doDoneAction() {
	    	boolean retOK = false;
   	        String blockName = _trainLocationBox.getText();
	        if (blockName != null) {
	            OBlock block = InstanceManager.getDefault(OBlockManager.class).getOBlock(blockName.trim());
	            if (block==null) {
    	            JOptionPane.showMessageDialog(this, Bundle.getMessage("BlockNotFound", blockName), 
    	                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
	            } else if ((block.getState() & OBlock.UNOCCUPIED) > 0) {
    	            JOptionPane.showMessageDialog(this, Bundle.getMessage("blockUnoccupied", block.getDisplayName()), 
    	                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
	            } else if (nameInuse(_trainNameBox.getText())) {
    	            JOptionPane.showMessageDialog(this, Bundle.getMessage("duplicateName", _trainNameBox.getText()), 
    	                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);	            	
	            } else {
	            	String name = blockInUse(block);
	            	if (name!=null) {
	     	            JOptionPane.showMessageDialog(this, Bundle.getMessage("blockInUse", name, block.getDisplayName()),
	     	                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
	 	            } else {
		            	Tracker newTracker = new Tracker(block, _trainNameBox.getText());
		            	_trackerList.add(newTracker);
		            	newTracker.addPropertyChangeListener(this);
		                _model.fireTableDataChanged();
		    	    	_status.setText(Bundle.getMessage("blockInUse", _trainNameBox.getText(), block.getDisplayName()));
		            	retOK = true;   	            		 	            	
	 	            }
	            }
	        }
	        return retOK;
	    }
	    
	    String blockInUse(OBlock b) {
	    	Iterator<Tracker> iter = _trackerList.iterator();
	    	while (iter.hasNext()) {
	    		Tracker t = iter.next();
	    		if (b.equals(t.getCurrentBlock())) {
	    			return t.getTrainName();
	    		}
	    	}
	    	return null;
	    }
	    boolean nameInuse(String name) {
	    	Iterator<Tracker> iter = _trackerList.iterator();
	    	while (iter.hasNext()) {
	    		Tracker t = iter.next();
	    		if (name.equals(t.getTrainName())) {
	    			return true;
	    		}
	    	}
	    	return false;	    	
	    }
	    void openPickList() {
	        _pickFrame = new JmriJFrame();
	        JPanel content = new JPanel();
	        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

	        JPanel blurb = new JPanel();
	        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
	        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
//	        blurb.add(new JLabel(Bundle.getMessage("DragOccupancyName")));
//	        blurb.add(new JLabel(Bundle.getMessage("DragErrorName")));
	        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
	        JPanel panel = new JPanel();
	        panel.add(blurb);
	        content.add(panel);
	        PickListModel[] models = {PickListModel.oBlockPickModelInstance()};
	        content.add(new PickPanel(models));

	        _pickFrame.setContentPane(content);
/*	        _pickFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	                public void windowClosing(java.awt.event.WindowEvent e) {
	                    closePickList();                   
	                }
	            });*/
	        _pickFrame.setLocationRelativeTo(this);
	        _pickFrame.toFront();
	        _pickFrame.setVisible(true);
	        _pickFrame.pack();
	    }
	    
	    protected void stopTrain(Tracker t) {
	    	t.stopTrain(true);
	    	t.removePropertyChangeListener(this);
	    	_trackerList.remove(t);
	    	_status.setText(Bundle.getMessage("TrackerStopped", t.getTrainName()));
	    }

	    
	    public void propertyChange(PropertyChangeEvent e) {
	    	String name = (String)e.getSource();
	    	String msg = null;
	        if (e.getPropertyName().equals("BlockChange")) {
	            _model.fireTableDataChanged();
	            if (_appendStatus) {
	            	msg =_status.getText();
	            	_appendStatus = false;
	            }
	        	_status.setText(Bundle.getMessage("TrackerBlockChange", name,
    					e.getOldValue(), e.getNewValue())+(msg==null ? "":"("+msg+")"));
	        } else if(e.getPropertyName().equals("BlockOccupied")) {
	        	_status.setText(Bundle.getMessage("TrackerBlockOccupied", name, e.getNewValue()));
	        	_appendStatus = true;
	        } else if(e.getPropertyName().equals("ErrorNoBlock")) {
	            if (_appendStatus) {
	            	msg =_status.getText();
	            	_appendStatus = false;
	            }
	        	_status.setText(Bundle.getMessage("TrackerNoCurrentBlock", name, e.getNewValue())+
	        			(msg==null ? "":"("+msg+")"));
	        }
            _model.fireTableDataChanged();
	    }
    }
    
    public class TrackerTableModel extends AbstractTableModel {

        public static final int NAME_COL = 0;
        public static final int STATUS_COL = 1;
        public static final int STOP_COL = 2;
        public static final int NUMCOLS = 3;
        
        TableFrame _parent;

        public TrackerTableModel(TableFrame f) {
            super();
            _parent = f;
        }

        public int getColumnCount () {
            return NUMCOLS;
        }
        public int getRowCount() {
            return _trackerList.size();
        }
        public String getColumnName(int col) {
            switch (col) {
                case NAME_COL: return Bundle.getMessage("TrainName");
                case STATUS_COL: return Bundle.getMessage("status");
            }
            return "";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case NAME_COL:
                    return _trackerList.get(rowIndex).getTrainName();
                case STATUS_COL:
                    return _trackerList.get(rowIndex).getStatus();
                case STOP_COL:
                    return Bundle.getMessage("Stop");
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
            if (col==STOP_COL) {
            	Tracker t = _trackerList.get(row);
            	_parent.stopTrain(t);
                 fireTableDataChanged();
                return;
            }
        }

        public boolean isCellEditable(int row, int col) {
        	if (col==STOP_COL) {
                return true;        	
        	}
        	return false;
        }

        public Class<?> getColumnClass(int col) {
            if (col==STOP_COL) {
                return JButton.class;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case NAME_COL: return new JTextField(25).getPreferredSize().width;
                case STATUS_COL: return new JTextField(80).getPreferredSize().width;
                case STOP_COL: return new JButton("STOPIT").getPreferredSize().width;
            }
            return 5;
        }

    }

    static Logger log = LoggerFactory.getLogger(TrackerTableAction.class.getName());
}
