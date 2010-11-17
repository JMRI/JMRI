// BlockTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Block;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * BlockTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2008
 * @version     $Revision: 1.16 $
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
    }

    public BlockTableAction() { this("Block Table");}
	
	private String noneText = rb.getString("BlockNone");
	private String gradualText = rb.getString("BlockGradual");
	private String tightText = rb.getString("BlockTight");
	private String severeText = rb.getString("BlockSevere");
	private String[] curveOptions = {noneText, gradualText, tightText, severeText};
	private DecimalFormat twoDigit = new DecimalFormat("0.00");

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Block objects
     */
    protected void createModel() {
        m = new BeanTableDataModel() {

        	static public final int DIRECTIONCOL = NUMCOLUMN;
			static public final int LENGTHCOL = DIRECTIONCOL+1;
			static public final int CURVECOL = LENGTHCOL+1;

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
            public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnBlockInUse(); }
            public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnBlockInUse(boo); }
            public void clickOn(NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }

    		public int getColumnCount(){ 
    		    return CURVECOL+1;
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
				else super.setValueAt(value, row, col);					
    		}

	   		public String getColumnName(int col) {
        		if (col==DIRECTIONCOL) return "Direction";
        		if (col==VALUECOL) return "Value";
				if (col==CURVECOL) return rb.getString("BlockCurveColName");
				if (col==LENGTHCOL) return rb.getString("BlockLengthColName");
        		return super.getColumnName(col);
        	}

    		public Class<?> getColumnClass(int col) {
    			if (col==DIRECTIONCOL) return String.class;
    			if (col==VALUECOL) return String.class;  // not a button
				if (col==CURVECOL) return JComboBox.class;
				if (col==LENGTHCOL) return String.class;
    			else return super.getColumnClass(col);
		    }

    		public int getPreferredWidth(int col) {
    			if (col==DIRECTIONCOL) return new JTextField(7).getPreferredSize().width;
    			if (col==CURVECOL) return new JTextField(8).getPreferredSize().width;
    			if (col==LENGTHCOL) return new JTextField(7).getPreferredSize().width;
    			else return super.getPreferredWidth(col);
		    }

    		public void configValueColumn(JTable table) {
        		// value column isn't button, so config is null
		    }
			
			public boolean isCellEditable(int row, int col) {
				if (col==CURVECOL) return true;
				else if (col==LENGTHCOL) return true;
				else return super.isCellEditable(row,col);
			}
			
			public void configureTable(JTable table) {
				table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
				table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
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
        };
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
		f.addToBottomBox (inchBox);
		inchBox.setToolTipText(rb.getString("InchBoxToolTip"));
		inchBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					inchBoxChanged();
				}
			});
		f.addToBottomBox (centimeterBox);
		centimeterBox.setToolTipText(rb.getString("CentimeterBoxToolTip"));
		centimeterBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					centimeterBoxChanged();
				}
			});
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

    protected void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddBlock"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.BlockAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            p.add(sysName);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            addFrame.getContentPane().add(p);

            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText().toUpperCase();
        InstanceManager.blockManagerInstance().createNewBlock(sName, user);
    }
    //private boolean noWarn = false;

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BlockTableAction.class.getName());
}

/* @(#)BlockTableAction.java */
