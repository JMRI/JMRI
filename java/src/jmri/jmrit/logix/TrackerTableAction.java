package jmri.jmrit.logix;

//import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

/**
 * This singleton class displays a table of the occupancy detection trackers
 * @author Peter Cressman
 *
 */
public class TrackerTableAction extends AbstractAction {

    static int STRUT_SIZE = 10;
    
    private static TrackerTableAction _instance;
    private static ArrayList<Tracker> _trackerList = new ArrayList<Tracker>();
    private static TableFrame _frame;

    private TrackerTableAction(String menuOption) {
	    super(menuOption);
    }
    public static TrackerTableAction getInstance() {
    	if (_instance==null) {
    		_instance = new TrackerTableAction(Bundle.getMessage("MenuTrackers"));
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
    static public boolean markNewTracker(OBlock block, String name) {
    	if (_frame==null) {
        	_frame = new TableFrame();    		
    	}
		return _frame.addTracker(block, name);
    }
    
    /**
     * Holds a table of Trackers that follow adjacent occupancy.  Needs to be 
     * a singleton to be opened and closed for trackers to report to it
     * @author Peter Cressman
     *
     */
    static class TableFrame extends JmriJFrame implements PropertyChangeListener 
    {
        private TrackerTableModel _model;
        private JmriJFrame _pickFrame;
        JDialog _dialog;
        JTextField  _trainNameBox = new JTextField(30);
        JTextField  _trainLocationBox = new JTextField(30);
        JTextField  _status = new JTextField(80);
        boolean		_appendStatus = false;
        HashMap<OBlock, List<Tracker>> _blocks = new HashMap<OBlock, List<Tracker>>();

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
            dim.height = table.getRowHeight()*2;
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
            _status.setBackground(Color.white);
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
	            } else {
 	            	addTracker(block, _trainNameBox.getText());
	            	retOK = true;   	            		 	            	
	            }
	        }
	        return retOK;
	    }
	    
	    public boolean addTracker(OBlock block, String name){
            if ((block.getState() & OBlock.OCCUPIED) == 0) {
	            JOptionPane.showMessageDialog(this, Bundle.getMessage("blockUnoccupied", block.getDisplayName()), 
	                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
	            return false;
            } else if (nameInuse(_trainNameBox.getText())) {
	            JOptionPane.showMessageDialog(this, Bundle.getMessage("duplicateName", _trainNameBox.getText()), 
	                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);	            	
	            return false;
            } else {
            	String oldName = blockInUse(block);
            	if (oldName!=null) {
     	            JOptionPane.showMessageDialog(this, Bundle.getMessage("blockInUse", oldName, block.getDisplayName()),
     	                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
    	            return false;
 	            } else {
 	              	Tracker newTracker = new Tracker(block, name);
 	 	           	_trackerList.add(newTracker);
 	 	        	addBlockListeners(newTracker);
 	 	            _model.fireTableDataChanged();
 	 		    	_status.setText(Bundle.getMessage("blockInUse", _trainNameBox.getText(), block.getDisplayName()));
 		            return true;   	            		 	            	
 	            }
            }
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
	    
	    private void addBlockListeners(Tracker newTracker) {
	    	List<OBlock> range = newTracker.getRange();
	    	Iterator<OBlock> iter = range.iterator();
	    	while (iter.hasNext()) {
	    		OBlock b = iter.next();
	    		List<Tracker> trackers = _blocks.get(b);
	    		if (trackers==null) {
	    			trackers = new ArrayList<Tracker>();
	    			trackers.add(newTracker);
	    			_blocks.put(b, trackers);
	    			b.addPropertyChangeListener(this);
	    		} else {
	    			if (trackers.size()==0) {
		    			b.addPropertyChangeListener(this);	    				
	    			}
	    			if (!trackers.contains(newTracker)) {
	    				trackers.add(newTracker);
	    			}
	    		}
	    	}	    	
	    }

	    private void removeBlockListeners(Tracker tracker) {
	    	removeBlockListeners(tracker.getRange(), tracker);
	    }
	    
	    private void removeBlockListeners(List<OBlock> range, Tracker tracker) {
	    	Iterator<OBlock> iter = range.iterator();
	    	while (iter.hasNext()) {
	    		OBlock b = iter.next();
	    		List<Tracker> trackers = _blocks.get(b);
	    		if (trackers!=null) {
    				trackers.remove(tracker);
	    			if (trackers.size()==0) {
		    			b.removePropertyChangeListener(this);
	    			}
	    		} else {
					log.error("Block "+b.getDisplayName()+" in the range of "+
							tracker.getCurrentBlock().getDisplayName()+" for train "
							+tracker.getTrainName()+" has no listeners");
	    		}
	    	}	    	
	    }
	    
	    public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("state") ) {
	        	OBlock b = (OBlock)evt.getSource();
	        	int state = ((Number)evt.getNewValue()).intValue();
		        if (log.isDebugEnabled()) log.debug("block= "+b.getDisplayName()+" state= "+state);
		        // The "jiggle" (see tracker.showBlockValue() causes some state changes to be duplicated.
		        // The following washes out the extra notifications
	            if ((state & (OBlock.UNOCCUPIED | OBlock.RUNNING)) == (OBlock.UNOCCUPIED | OBlock.RUNNING)) {
	            	b.setState(state & ~OBlock.RUNNING);
            		return;		// will do the tracker.move() on the next (repeat0 call
	            } else if ((state & OBlock.RUNNING) != 0) {
	            	return;		// repeats previous call that was completed.	            	
	            }
	    		List<Tracker> trackers = _blocks.get(b);
	    		if (trackers==null) {
	    			log.error("No Trackers found for block "+b.getDisplayName()+" going to state= "+state);
	    			b.removePropertyChangeListener(this);
	    		} else {
	    			if (trackers.size()==1) {
	    				processTrackerStateChange(trackers.get(0), b, state);
	    			} else {
    			    	boolean found = false;
	    				if ((state & OBlock.OCCUPIED) != 0) {
		    				String[] trains = new String[trackers.size()];
		    		    	Iterator<Tracker> iter = trackers.iterator();
		    		    	int i=0;
		    		    	while (iter.hasNext()) {
		    		    		trains[i++] = iter.next().getTrainName();
		    		    	}	    				
		    				Object selection = JOptionPane.showInputDialog(this, 
		    						Bundle.getMessage("MultipleTrackers", b.getDisplayName()),Bundle.getMessage("WarningTitle"),
		    								JOptionPane.INFORMATION_MESSAGE, null, trains, null);
		    				if (selection!=null) {
		    			    	iter = _trackerList.iterator();
		    			    	while (iter.hasNext()) {
		    			    		Tracker t = iter.next();
		    			    		if (((String)selection).equals(t.getTrainName())) {
		    		    				processTrackerStateChange(t, b, state);
		    		    				found = true;
		    		    				break;
		    			    		}
		    			    	}
		    				}
	    				} else {
	    			    	Iterator<Tracker> iter = _trackerList.iterator();
	    			    	while (iter.hasNext()) {
	    			    		Tracker t = iter.next();
	    			    		if (t.getTrainName().equals(b.getValue())) {
	    		    				processTrackerStateChange(t, b, state);
	    		    				found = true;
	    		    				break;
	    			    		}
	    			    	}
	    				}
	    				if (!found) {
	    					_status.setText(Bundle.getMessage("UnknownOccupier", 
	    							b.getValue(), b.getDisplayName()));
	    				}	    					
	    			}
	    		}
	            if ((state & OBlock.UNOCCUPIED) != 0) {
	            	b.setValue(null);
	            }
			}
            _model.fireTableDataChanged();
	    }
	    
	    private void processTrackerStateChange(Tracker tracker, OBlock block, int state) {
			List<OBlock> range = tracker.getRange();
			switch (tracker.move(block, state) ) {
				case Tracker.NO_BLOCK:
					String msg = Bundle.getMessage("TrackerNoCurrentBlock", tracker.getTrainName(), 
							tracker.getCurrentBlock().getDisplayName());
		        	_status.setText(msg);
		            JOptionPane.showMessageDialog(this, msg, 
		                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
		            stopTrain(tracker);
		        	break;
				case Tracker.ENTER_BLOCK:
					removeBlockListeners(range, tracker);
		        	_status.setText(Bundle.getMessage("TrackerBlockChange", tracker.getTrainName(),
		        			tracker.getPreviousBlock().getDisplayName(), tracker.getCurrentBlock().getDisplayName()));
		        	addBlockListeners(tracker);
		        	break;
				case Tracker.LEAVE_BLOCK:
					break;
				case Tracker.ERROR_BLOCK:
					log.error("Block "+block.getDisplayName()+" not adjacent to "+
							tracker.getCurrentBlock().getDisplayName()+" the current block of "+tracker.getTrainName());
			}	    	
	    }
	    		
	    protected void stopTrain(Tracker t) {
			removeBlockListeners(t);
	    	_trackerList.remove(t);
	    	t.dropRange();
	    	_status.setText(Bundle.getMessage("TrackerStopped", t.getTrainName()));
	    }
    }
    
    static class TrackerTableModel extends AbstractTableModel {

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
                case NAME_COL: return new JTextField(20).getPreferredSize().width;
                case STATUS_COL: return new JTextField(60).getPreferredSize().width;
                case STOP_COL: return new JButton("STOPIT").getPreferredSize().width;
            }
            return 5;
        }

    }

    static Logger log = LoggerFactory.getLogger(TrackerTableAction.class.getName());
}
