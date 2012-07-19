// BlockTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Block;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JFrame;
import jmri.Reporter;
import jmri.Sensor;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * BlockTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2008
 * @version     $Revision$
 */

public class BlockTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public BlockTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary Block manager available
        if (jmri.InstanceManager.blockManagerInstance()==null) {
            setEnabled(false);
        }
		inchBox.setSelected(true);
		centimeterBox.setSelected(false);
        
        defaultBlockSpeedText = ("Use Global " + jmri.InstanceManager.blockManagerInstance().getDefaultSpeed());
        speedList.add(defaultBlockSpeedText);
        java.util.Vector<String> _speedMap = jmri.implementation.SignalSpeedMap.getMap().getValidSpeedNames();
        for(int i = 0; i<_speedMap.size(); i++){
            if (!speedList.contains(_speedMap.get(i))){
                speedList.add(_speedMap.get(i));
            }
        }
        updateSensorList();
    }

    public BlockTableAction() { this("Block Table");}
	
	private String noneText = rb.getString("BlockNone");
	private String gradualText = rb.getString("BlockGradual");
	private String tightText = rb.getString("BlockTight");
	private String severeText = rb.getString("BlockSevere");
	private String[] curveOptions = {noneText, gradualText, tightText, severeText};
    private java.util.Vector<String> speedList = new java.util.Vector<String>();
    private String[] sensorList;
	private DecimalFormat twoDigit = new DecimalFormat("0.00");
    String defaultBlockSpeedText;
    
    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Block objects
     */
    protected void createModel() {
        m = new BeanTableDataModel() {

        	static public final int DIRECTIONCOL = NUMCOLUMN;
			static public final int LENGTHCOL = DIRECTIONCOL+1;
			static public final int CURVECOL = LENGTHCOL+1;
            static public final int STATECOL = CURVECOL+1;
            static public final int SENSORCOL = STATECOL+1;
            static public final int REPORTERCOL = SENSORCOL+1;
            static public final int CURRENTREPCOL = REPORTERCOL+1;
            static public final int PERMISCOL = CURRENTREPCOL+1;
            static public final int SPEEDCOL = PERMISCOL+1;
            
        	public String getValue(String name) {
        		if (name == null) {
        			BeanTableDataModel.log.warn("requested getValue(null)");
        			return "(no name)";
        		}
        		Block b = InstanceManager.blockManagerInstance().getBySystemName(name);
        		if (b == null) {
        			BeanTableDataModel.log.debug("requested getValue(\""+name+"\"), Block doesn't exist");
        			return "(no Block)";
        		}
        		Object m = b.getValue();
            	if (m!=null)
                	return m.toString();
                else
                	return "";
            }
            public Manager getManager() { return InstanceManager.blockManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.blockManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.blockManagerInstance().getByUserName(name);}
            protected String getMasterClassName() { return getClassName(); }

            public void clickOn(NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }

            //Permissive and speed columns are temp disabled
    		public int getColumnCount(){ 
    		    return SPEEDCOL+1;
     		}

    		public Object getValueAt(int row, int col) {
    			// some error checking
    			if (row >= sysNameList.size()){
    				BeanTableDataModel.log.debug("requested getValueAt(\""+row+"\"), row outside of range");
    				return "Error table size";
    			}
				Block b = (Block)getBySystemName(sysNameList.get(row));
				if (b == null) {
					BeanTableDataModel.log.debug("requested getValueAt(\""+row+"\"), Block doesn't exist");
					return "(no Block)";
				}
	   			if (col==DIRECTIONCOL) {
					return jmri.Path.decodeDirection(b.getDirection());
				}
				else if (col==CURVECOL) {
					JComboBox c = new JComboBox(curveOptions);
					if (b.getCurvature()==Block.NONE) c.setSelectedItem(0);
					else if (b.getCurvature()==Block.GRADUAL) c.setSelectedItem(gradualText);
					else if (b.getCurvature()==Block.TIGHT) c.setSelectedItem(tightText);
					else if (b.getCurvature()==Block.SEVERE) c.setSelectedItem(severeText);
					return c;
				}
				else if (col==LENGTHCOL) {
					double len = 0.0;
					if (inchBox.isSelected())
						len = b.getLengthIn();
					else 
						len = b.getLengthCm();
					return (twoDigit.format(len));
				}
                else if (col==PERMISCOL){
                    boolean val = b.getPermissiveWorking();
                    return Boolean.valueOf(val);
                }
                else if (col==SPEEDCOL){
                    String speed = b.getBlockSpeed();
                    if(!speedList.contains(speed)){
                        speedList.add(speed);
                    }
                    JComboBox c = new JComboBox(speedList);
                    c.setEditable(true);
                    c.setSelectedItem(speed);
                    return c;
                }
                else if (col==STATECOL){
                    switch(b.getState()){
                        case (Block.OCCUPIED) : return rb.getString("BlockOccupied");
                        case (Block.UNOCCUPIED) : return rb.getString("BlockUnOccupied");
                        case (Block.UNKNOWN) : return rb.getString("BlockUnknown");
                        default : return rb.getString("BlockInconsistent");
                    }
                }
                else if (col==SENSORCOL){
                    Sensor sensor = b.getSensor();
                    JComboBox c = new JComboBox(sensorList);
                    String name = "";
                    if(sensor!=null){
                        name = sensor.getDisplayName();
                    }
                    c.setSelectedItem(name);
                    return c;
                }
                else if (col==REPORTERCOL){
                    Reporter r = b.getReporter();
                    return (r!=null) ? r.getDisplayName() : null;
                }
                else if (col==CURRENTREPCOL){
                    return Boolean.valueOf(b.isReportingCurrent());
                }
                else return super.getValueAt(row, col);
			}    		

    		public void setValueAt(Object value, int row, int col) {
				Block b = (Block)getBySystemName(sysNameList.get(row));
        		if (col==VALUECOL) {
					b.setValue(value);
            		fireTableRowsUpdated(row,row);
        		}
				else if (col==LENGTHCOL) {
					float len = Float.valueOf((String)value).floatValue();
					if (inchBox.isSelected()) 
						b.setLength(len*25.4f);
					else
						b.setLength(len*10.0f);
            		fireTableRowsUpdated(row,row);
				}
				else if (col==CURVECOL) {
                    
					String cName = (String)((JComboBox)value).getSelectedItem();
                    if (cName.equals(noneText)) b.setCurvature(Block.NONE);
					else if (cName.equals(gradualText)) b.setCurvature(Block.GRADUAL);
					else if (cName.equals(tightText)) b.setCurvature(Block.TIGHT);
					else if (cName.equals(severeText)) b.setCurvature(Block.SEVERE);
            		fireTableRowsUpdated(row,row);
				}
                else if (col==PERMISCOL){
                    boolean boo = ((Boolean) value).booleanValue();
                    b.setPermissiveWorking(boo);
                    fireTableRowsUpdated(row,row);
                }
                else if (col==SPEEDCOL){
                    String speed = (String)((JComboBox)value).getSelectedItem();
                    try {
                        b.setBlockSpeed(speed);
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                        return;
                    }
                    if (!speedList.contains(speed) && !speed.contains("Global")){
                        speedList.add(speed);
                    }
                    fireTableRowsUpdated(row,row);
                }
                else if (col==REPORTERCOL){
                    Reporter r = null;
                    if (value !="" || value!=null) {
                        r = jmri.InstanceManager.reporterManagerInstance().provideReporter((String)value);
                    }
                    b.setReporter(r);
                    fireTableRowsUpdated(row,row);
                }
                else if (col==SENSORCOL){
                    String strSensor = (String)((JComboBox)value).getSelectedItem();
                    b.setSensor(strSensor);
                    if(b.getSensor()!=null && b.getSensor().getReporter()!=null){
                        String msg = java.text.MessageFormat.format(rb
                                .getString("BlockAssignReporter"), new Object[] { b.getSensor().getDisplayName(), b.getSensor().getReporter().getDisplayName() });
                        if(JOptionPane.showConfirmDialog(addFrame,
                                                             msg,rb.getString("BlockAssignReporterTitle"),
                                                             JOptionPane.YES_NO_OPTION)==0)
                            b.setReporter(b.getSensor().getReporter());
                    }
                    fireTableRowsUpdated(row,row);
                    return;
                }
                else if (col==CURRENTREPCOL){
                    boolean boo = ((Boolean) value).booleanValue();
                    b.setReportingCurrent(boo);
                    fireTableRowsUpdated(row,row);
                }
				else super.setValueAt(value, row, col);					
    		}

	   		public String getColumnName(int col) {
        		if (col==DIRECTIONCOL) return "Direction";
        		if (col==VALUECOL) return "Value";
				if (col==CURVECOL) return rb.getString("BlockCurveColName");
				if (col==LENGTHCOL) return rb.getString("BlockLengthColName");
                if (col==PERMISCOL) return rb.getString("BlockPermColName");
                if (col==SPEEDCOL) return rb.getString("BlockSpeedColName");
                if (col==STATECOL) return rb.getString("BlockState");
                if (col==REPORTERCOL) return rb.getString("BlockReporter");
                if (col==SENSORCOL) return rb.getString("BlockSensor");
                if (col==CURRENTREPCOL) return rb.getString("BlockReporterCurrent");
        		return super.getColumnName(col);
        	}

    		public Class<?> getColumnClass(int col) {
    			if (col==DIRECTIONCOL) return String.class;
    			if (col==VALUECOL) return String.class;  // not a button
				if (col==CURVECOL) return JComboBox.class;
				if (col==LENGTHCOL) return String.class;
                if (col==PERMISCOL) return Boolean.class;
                if (col==SPEEDCOL) return JComboBox.class;
                if (col==STATECOL) return String.class;
                if (col==REPORTERCOL) return String.class;
                if (col==SENSORCOL) return JComboBox.class;
                if (col==CURRENTREPCOL) return Boolean.class;
    			else return super.getColumnClass(col);
		    }

    		public int getPreferredWidth(int col) {
    			if (col==DIRECTIONCOL) return new JTextField(7).getPreferredSize().width;
    			if (col==CURVECOL) return new JTextField(8).getPreferredSize().width;
    			if (col==LENGTHCOL) return new JTextField(7).getPreferredSize().width;
                if (col==PERMISCOL) return new JTextField(7).getPreferredSize().width;
                if (col==SPEEDCOL) return new JTextField(7).getPreferredSize().width;
                if (col==STATECOL) return new JTextField(8).getPreferredSize().width;
                if (col==REPORTERCOL) return new JTextField(8).getPreferredSize().width;
                if (col==SENSORCOL) return new JTextField(8).getPreferredSize().width;
                if (col==CURRENTREPCOL) return new JTextField(7).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }

    		public void configValueColumn(JTable table) {
        		// value column isn't button, so config is null
		    }
			
			public boolean isCellEditable(int row, int col) {
				if (col==CURVECOL) return true;
				else if (col==LENGTHCOL) return true;
                else if (col==PERMISCOL) return true;
                else if (col==SPEEDCOL) return true;
                else if (col==STATECOL) return false;
                else if (col==REPORTERCOL) return true;
                else if (col==SENSORCOL) return true;
                else if (col==CURRENTREPCOL) return true;
				else return super.isCellEditable(row,col);
			}
			
			public void configureTable(JTable table) {
				table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
				table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                jmri.InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
				super.configureTable(table);
			}
			
			protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
			    return true;
				// return (e.getPropertyName().indexOf("alue")>=0);
			}

			public JButton configureButton() {
				BeanTableDataModel.log.error("configureButton should not have been called");
				return null;
			}
            
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if(e.getSource() instanceof jmri.SensorManager){
                    if(e.getPropertyName().equals("length") || e.getPropertyName().equals("DisplayListName")){
                        updateSensorList();
                    }
                }
                if (e.getPropertyName().equals("DefaultBlockSpeedChange")){
                    updateSpeedList();
                } else {
                    super.propertyChange(e);
                }
            }
            
            protected String getBeanType(){
                return AbstractTableAction.rbean.getString("BeanNameBlock");
            }
            
            synchronized public void dispose() {
                super.dispose();
                jmri.InstanceManager.sensorManagerInstance().removePropertyChangeListener(this);
            }
        };
    }
    
    private void updateSensorList(){
        String[] nameList = jmri.InstanceManager.sensorManagerInstance().getSystemNameArray();
        String[] displayList = new String[nameList.length];
        for(int i = 0; i<nameList.length; i++){
            NamedBean nBean = jmri.InstanceManager.sensorManagerInstance().getBeanBySystemName(nameList[i]);
            if (nBean!=null){
                displayList[i] = nBean.getDisplayName();
            }
        }
        java.util.Arrays.sort(displayList);
        sensorList = new String[displayList.length+1];
        sensorList[0] = "";
        int i = 1;
        for(String name:displayList){
            sensorList[i] = name;
            i++;
        }
    }

    private void updateSpeedList(){
        speedList.remove(defaultBlockSpeedText);
        defaultBlockSpeedText = ("Use Global " + jmri.InstanceManager.blockManagerInstance().getDefaultSpeed());
        speedList.add(0, defaultBlockSpeedText);
        m.fireTableDataChanged();
    }
    
    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleBlockTable"));
    }
	
	JCheckBox inchBox = new JCheckBox(rb.getString("LengthInches"));
	JCheckBox centimeterBox = new JCheckBox(rb.getString("LengthCentimeters"));
	
	/**
	 * Add the checkboxes
	 */
	public void addToFrame(BeanTableFrame f) {
		//final BeanTableFrame finalF = f;	// needed for anonymous ActionListener class
		f.addToBottomBox (inchBox, this.getClass().getName());
		inchBox.setToolTipText(rb.getString("InchBoxToolTip"));
		inchBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					inchBoxChanged();
				}
			});
		f.addToBottomBox (centimeterBox, this.getClass().getName());
		centimeterBox.setToolTipText(rb.getString("CentimeterBoxToolTip"));
		centimeterBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					centimeterBoxChanged();
				}
			});
	}
    
    public void setMenuBar(BeanTableFrame f){
        final jmri.util.JmriJFrame finalF = f;			// needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        JMenu pathMenu = new JMenu("Paths");
        menuBar.add(pathMenu);
        JMenuItem item = new JMenuItem("Delete Paths...");
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    deletePaths(finalF);
        	}
            });
            
        
        JMenu speedMenu = new JMenu("Speeds");
        item = new JMenuItem("Defaults...");
        speedMenu.add(item);
        item.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                    setDefaultSpeeds(finalF);
        	}
            });
        menuBar.add(speedMenu);
    
    }
    
    protected void setDefaultSpeeds(JFrame _who){
        JComboBox blockSpeedCombo = new JComboBox(speedList);
        blockSpeedCombo.setEditable(true);
        
        JPanel block = new JPanel();
        block.add(new JLabel("Block Speed"));
        block.add(blockSpeedCombo);
        
        blockSpeedCombo.removeItem(defaultBlockSpeedText);
        
        blockSpeedCombo.setSelectedItem(InstanceManager.blockManagerInstance().getDefaultSpeed());
        
        int retval = JOptionPane.showOptionDialog(_who,
                                          "Select the default values for the speeds through the blocks\n" , "Block Speeds",
                                          0, JOptionPane.INFORMATION_MESSAGE, null,
                                          new Object[]{"Cancel", "OK", block}, null );
        if (retval != 1) {
            return;
        }
        
        String speedValue = (String) blockSpeedCombo.getSelectedItem();
        //We will allow the turnout manager to handle checking if the values have changed
        try {
            InstanceManager.blockManagerInstance().setDefaultSpeed(speedValue);
        } catch (jmri.JmriException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speedValue);
            return;
        }
    }
    
	private void inchBoxChanged() {
		centimeterBox.setSelected(!inchBox.isSelected());
		m.fireTableDataChanged();  // update view
	}
	private void centimeterBoxChanged() {
		inchBox.setSelected(!centimeterBox.isSelected());
		m.fireTableDataChanged();  // update view
	}
		
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.BlockTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
    
    JComboBox cur = new JComboBox(curveOptions);
    JTextField lengthField = new JTextField(7);
    JTextField blockSpeed = new JTextField(7);
    JCheckBox checkPerm = new JCheckBox(rb.getString("BlockPermColName"));
    
    JTextField numberToAdd = new JTextField(10);
    JCheckBox range = new JCheckBox(rb.getString("LabelNumberToAdd"));
    JCheckBox _autoSystemName = new JCheckBox(rb.getString("LabelAutoSysName"));
    jmri.UserPreferencesManager pref;
    
    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddBlock"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.BlockAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        okPressed(e);
                    }
                };
            addFrame.add(new AddNewBeanPanel(sysName, userName, numberToAdd, range, _autoSystemName, "ButtonOK", listener));
        }
        if(pref.getSimplePreferenceState(systemNameAuto))
            _autoSystemName.setSelected(true);
        addFrame.pack();
        addFrame.setVisible(true);
    }
    
    JComboBox speeds = new JComboBox();
    
    JPanel additionalAddOption(){
        
        GridLayout additionLayout = new GridLayout(0,2);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(additionLayout);
        mainPanel.add(new JLabel(rb.getString("BlockLengthColName")));
        mainPanel.add(lengthField);
        
        mainPanel.add(new JLabel(rb.getString("BlockCurveColName")));
        mainPanel.add(cur);
        
        mainPanel.add(new JLabel("  "));
        mainPanel.add(checkPerm);
        
        speeds = new JComboBox();
        speeds.setEditable(true);
        for (int i=0; i<speedList.size(); i++) {
            speeds.addItem(speedList.get(i));
        }
        
        mainPanel.add(new JLabel("blockSpeed"));
        mainPanel.add(speeds);
        
        //return displayList;
        lengthField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
                String text = lengthField.getText();
                if (!validateNumericalInput(text)){
                    String msg = java.text.MessageFormat.format(rb
                        .getString("ShouldBeNumber"), new Object[] { rb.getString("BlockLengthColName") });
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showInfoMessage(rb.getString("ErrorTitle"), msg, getClassName(), "length", false, false, org.apache.log4j.Level.WARN);
                }
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        
        return mainPanel;
    }
    
    String systemNameAuto = this.getClass().getName()+".AutoSystemName";

    boolean validateNumericalInput(String text){
        if (text.length()!=0){
           try{
                Integer.parseInt(text);
            } catch (java.lang.NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }
    
    void okPressed(ActionEvent e) {
        int intNumberToAdd = 1;
        if (range.isSelected()){
            try {
                intNumberToAdd = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");
                String msg = java.text.MessageFormat.format(rb
                    .getString("ShouldBeNumber"), new Object[] { rb.getString("LabelNumberToAdd") });
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                                showInfoMessage(rb.getString("ErrorTitle"),msg,""+ex, "",true, false, org.apache.log4j.Level.ERROR);
                return;
            }
        }
        if (intNumberToAdd>=65){
            String msg = java.text.MessageFormat.format(rb
                    .getString("WarnExcessBeans"), new Object[] { intNumberToAdd, AbstractTableAction.rbean.getString("BeanNameBlock") });
            if(JOptionPane.showConfirmDialog(addFrame,
                                                 msg,rb.getString("WarningTitle"),
                                                 JOptionPane.YES_NO_OPTION)==1)
                return;
        }
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText().toUpperCase();
        StringBuilder b;
        
        for (int x = 0; x< intNumberToAdd; x++){    
            if (x!=0){
                if (user!=null){
                    b = new StringBuilder(userName.getText());
                    b.append(":");
                    b.append(Integer.toString(x));
                    user=b.toString();
                }
                if(!_autoSystemName.isSelected()){
                    b = new StringBuilder(sysName.getText());
                    b.append(":");
                    b.append(Integer.toString(x));
                    sName=b.toString();
                }
            }
            Block blk;
            try {
                if (_autoSystemName.isSelected())
                    blk = InstanceManager.blockManagerInstance().createNewBlock(user);
                else
                    blk = InstanceManager.blockManagerInstance().createNewBlock(sName, user);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating       
            }
            if (blk!=null){
                if (lengthField.getText().length()!=0)
                    blk.setLength(Integer.parseInt(lengthField.getText()));
                /*if (blockSpeed.getText().length()!=0)
                    blk.setSpeedLimit(Integer.parseInt(blockSpeed.getText()));*/
                try {
                    blk.setBlockSpeed((String)speeds.getSelectedItem());
                } catch (jmri.JmriException ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + (String)speeds.getSelectedItem());
                }
                if(checkPerm.isSelected())
                    blk.setPermissiveWorking(true);
                String cName = (String)cur.getSelectedItem();
                if (cName.equals(noneText)) blk.setCurvature(Block.NONE);
                else if (cName.equals(gradualText)) blk.setCurvature(Block.GRADUAL);
                else if (cName.equals(tightText)) blk.setCurvature(Block.TIGHT);
                else if (cName.equals(severeText)) blk.setCurvature(Block.SEVERE);
            }
        }
        pref.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
       // InstanceManager.blockManagerInstance().createNewBlock(sName, user);
    }  
    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                    rb.getString("ErrorBlockAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    //private boolean noWarn = false;
   
    void deletePaths(jmri.util.JmriJFrame f) {
		// Set option to prevent the path information from being saved.
        
        Object[] options = {"Remove",
                    "Keep"};

        int retval = JOptionPane.showOptionDialog(f, rb.getString("BlockPathMessage"), rb.getString("BlockPathSaveTitle"),
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (retval != 0) {
            InstanceManager.blockManagerInstance().savePathInfo(true);
            log.info("Requested to save path information via Block Menu.");
        } else {
            InstanceManager.blockManagerInstance().savePathInfo(false);
            log.info("Requested not to save path information via Block Menu.");
        }

    }

    public String getClassDescription() { return rb.getString("TitleBlockTable"); }
    
    protected String getClassName() { return BlockTableAction.class.getName(); }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BlockTableAction.class.getName());
}

/* @(#)BlockTableAction.java */
