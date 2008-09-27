// TransitTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.EntryPoint;
import jmri.Section;
import jmri.SectionManager;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.Block;
import jmri.BlockManager;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;

import jmri.util.JmriJFrame;
import java.util.ArrayList;

/**
 * Swing action to create and register a
 * TransitTable GUI.
 *
 * @author	Dave Duchamp    Copyright (C) 2008
 * @version     $Revision: 1.1 $
 */

public class TransitTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public TransitTableAction(String actionName) {
		super(actionName);
		// set manager - no need to use InstanceManager here
		transitManager = jmri.InstanceManager.transitManagerInstance();
        // disable ourself if there is no Transit manager available
        if (sectionManager==null) {
            setEnabled(false);
        }

    }

    public TransitTableAction() { this("Transit Table");}
	
	static final ResourceBundle rbx = ResourceBundle
			.getBundle("jmri.jmrit.beantable.SectionTransitTableBundle");

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Transit objects
     */
    void createModel() {
        m = new BeanTableDataModel() {

		static public final int MODECOL = NUMCOLUMN;
		static public final int EDITCOL = MODECOL+1;	

       	public String getValue(String name) {
        		if (name == null) {
        			super.log.warn("requested getValue(null)");
        			return "(no name)";
        		}
        		Transit z = InstanceManager.transitManagerInstance().getBySystemName(name);
        		if (z == null) {
        			super.log.debug("requested getValue(\""+name+"\"), Transit doesn't exist");
        			return "(no Transit)";
        		}
				return "Transit";
            }
            public Manager getManager() { return InstanceManager.transitManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.transitManagerInstance().getBySystemName(name);}
            public NamedBean getByUserName(String name) { return InstanceManager.transitManagerInstance().getByUserName(name);}

            public void clickOn(NamedBean t) {
            }

    		public int getColumnCount(){ 
    		    return EDITCOL+1;
     		}

    		public Object getValueAt(int row, int col) {
				if (col==MODECOL) {
            		Transit z = (Transit)getBySystemName((String)sysNameList.get(row));
                    if (z == null) {
						return " ";
					}
					else {
						int mode = z.getMode();
						if (mode==Transit.UNKNOWN) return (rbx.getString("Unknown"));
						else if (mode==Transit.AUTOMATIC) return (rbx.getString("TransitAutomatic"));
						else if (mode==Transit.MANUAL) return (rbx.getString("TransitManual"));
						else if (mode==Transit.DISPATCHED) return (rbx.getString("TransitDispatched"));
					}
				}
				else if (col==VALUECOL) {
            		Transit z = (Transit)getBySystemName((String)sysNameList.get(row));
                    if (z == null) {
						return "";
					}
					else {
						int state = z.getState();
						if (state==Transit.IDLE) return (rbx.getString("TransitIdle"));
						else if (state==Transit.ASSIGNED) return (rbx.getString("TransitAssigned"));
						else if (state==Transit.RUNNING) return (rbx.getString("TransitRunning"));
					}
				}
				else if (col==EDITCOL) return rb.getString("ButtonEdit"); 
				else return super.getValueAt(row, col);
				return null;
			}    		

    		public void setValueAt(Object value, int row, int col) {
        		if (col==MODECOL) {
					return;
				}
 				else if (col == EDITCOL) {
					// set up to edit
					String sName = (String) getValueAt(row, SYSNAMECOL);
					editPressed(sName);
				} 
				else super.setValueAt(value, row, col);
    		}

	   		public String getColumnName(int col) {
				if (col==MODECOL) return (rbx.getString("TransitMode"));
				if (col==EDITCOL) return "";   // no namne on Edit column
        		return super.getColumnName(col);
        	}

    		public Class getColumnClass(int col) {
				if (col==VALUECOL) return String.class;  // not a button
    			if (col==MODECOL) return String.class;  // not a button
 				if (col==EDITCOL) return JButton.class;
   			else return super.getColumnClass(col);
		    }

 			public boolean isCellEditable(int row, int col) {
				if (col == MODECOL) return false;
				if (col == VALUECOL) return false;
				if (col == EDITCOL) return true;
				else return super.isCellEditable(row, col);
			}
			
			public int getPreferredWidth(int col) {
 				// override default value for SystemName and UserName columns
				if (col == SYSNAMECOL)return new JTextField(9).getPreferredSize().width;
				if (col == USERNAMECOL)return new JTextField(17).getPreferredSize().width;
				if (col == VALUECOL)return new JTextField(6).getPreferredSize().width;
				// new columns
				if (col == MODECOL)return new JTextField(9).getPreferredSize().width;
     			if (col == EDITCOL) return new JTextField(6).getPreferredSize().width;
   			else return super.getPreferredWidth(col);
		    }

    		public void configValueColumn(JTable table) {
        		// value column isn't button, so config is null
		    }

			boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
			    return true;
				// return (e.getPropertyName().indexOf("alue")>=0);
			}

			public JButton configureButton() {
				super.log.error("configureButton should not have been called");
				return null;
			}
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleTransitTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.beantable.TransitTable";
    }
	
	// instance variables
	private boolean editMode = false;
	private TransitManager transitManager = null;
	private SectionManager sectionManager = InstanceManager.sectionManagerInstance();
	private Transit curTransit = null;
	private SectionTableModel sectionTableModel = null;
	private ArrayList sectionList = new ArrayList();
	private int[] direction = new int[50];
	private int[] sequence = new int[50];
	private int[] action = new int[50];
	private int[] data = new int[50];
	private boolean[] alternate = new boolean[50];
	private int maxSections = 50;  // must be equal to the dimension of the above arrays
	private ArrayList primarySectionBoxList = new ArrayList();
	private int[] priSectionDirection = new int[100];
	private ArrayList alternateSectionBoxList = new ArrayList();
	private int[] altSectionDirection = new int[100];
	private Section curSection = null;
	private int curSectionDirection = 0;
	private Section prevSection = null;
	private int prevSectionDirection = 0;
	private int curSequenceNum = 0;

	// add/create variables
    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JLabel sysNameFixed = new JLabel("");
    JTextField userName = new JTextField(17);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));
	JButton create = null;
	JButton update = null;
	JButton deleteSections = null;
	JComboBox primarySectionBox = new JComboBox();
	JButton addNextSection = null;
	JComboBox alternateSectionBox = new JComboBox();
	JButton addAlternateSection = null;

     /**
	 * Responds to the Add... button and the Edit buttons in Transit Table 
	 */
	void addPressed(ActionEvent e) {
		editMode = false;
		if ((sectionManager.getSystemNameList().size()) > 0) {
			addEditPressed();
		}
		else {
			javax.swing.JOptionPane.showMessageDialog(null, rbx
					.getString("Message21"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
	}
	void editPressed(String sName) {
		curTransit = transitManager.getBySystemName(sName);
		if (curTransit==null) {
			// no transit - should never happen, but protects against a $%^#@ exception
			return;
		}
		sysNameFixed.setText(sName);
		editMode = true;
		addEditPressed();
	}
	void addEditPressed() {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddTransit"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.TransitAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); 
			p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
			p.add(sysNameFixed);
            p.add(sysName);
			sysName.setToolTipText(rbx.getString("TransitSystemNameHint"));
			p.add (new JLabel("     "));
            p.add(userNameLabel);
            p.add(userName);
			userName.setToolTipText(rbx.getString("TransitUserNameHint"));
            addFrame.getContentPane().add(p);
			addFrame.getContentPane().add(new JSeparator());
			JPanel p1 = new JPanel();
			p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
			JPanel p11 = new JPanel();
			p11.setLayout(new FlowLayout());
			p11.add(new JLabel(rbx.getString("SectionTableMessage")));
			p1.add(p11);
			JPanel p12 = new JPanel();
			// initialize table of sections
			sectionTableModel = new SectionTableModel();
			JTable sectionTable = new JTable(sectionTableModel);
			sectionTable.setRowSelectionAllowed(false);
			sectionTable.setPreferredScrollableViewportSize(new java.awt.Dimension(550,150));
			TableColumnModel sectionColumnModel = sectionTable.getColumnModel();
			TableColumn sequenceColumn = sectionColumnModel.getColumn(SectionTableModel.SEQUENCE_COLUMN);
			sequenceColumn.setResizable(true);
			sequenceColumn.setMinWidth(50);
			sequenceColumn.setMaxWidth(70);
			TableColumn sectionColumn = sectionColumnModel.getColumn(SectionTableModel.SECTIONNAME_COLUMN);
			sectionColumn.setResizable(true);
			sectionColumn.setMinWidth(150);
			sectionColumn.setMaxWidth(210);
			JComboBox actionCombo = new JComboBox();
			actionCombo.addItem(rbx.getString("None"));
			actionCombo.addItem(rbx.getString("Pause"));
			actionCombo.addItem(rbx.getString("Wait"));
			TableColumn actionColumn = sectionColumnModel.getColumn(SectionTableModel.ACTION_COLUMN);
			actionColumn.setCellEditor(new DefaultCellEditor(actionCombo));
			sectionTable.setRowHeight(actionCombo.getPreferredSize().height);
			actionColumn.setPreferredWidth(actionCombo.getPreferredSize().width);
			actionColumn.setResizable(false);
			TableColumn dataColumn = sectionColumnModel.getColumn(SectionTableModel.DATA_COLUMN);
			dataColumn.setResizable(true);
			dataColumn.setMinWidth(50);
			dataColumn.setMaxWidth(70);
			TableColumn alternateColumn = sectionColumnModel.getColumn(SectionTableModel.ALTERNATE_COLUMN);
			alternateColumn.setResizable(true);
			alternateColumn.setMinWidth(140);
			alternateColumn.setMaxWidth(170);
			JScrollPane sectionTableScrollPane = new JScrollPane(sectionTable);
			p12.add(sectionTableScrollPane, BorderLayout.CENTER);
			p1.add(p12);
			JPanel p13 = new JPanel();
			p13.setLayout(new FlowLayout());
			p13.add (deleteSections = new JButton(rbx.getString("DeleteSectionsButton")));
            deleteSections.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteAllSections(e);
                }
            });
			deleteSections.setToolTipText(rbx.getString("DeleteSectionsButtonHint"));
			p13.add (new JLabel("     "));
			p13.add (primarySectionBox);
			primarySectionBox.setToolTipText(rbx.getString("PrimarySectionBoxHint"));
			p13.add (addNextSection = new JButton(rbx.getString("AddPrimaryButton")));
            addNextSection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addNextSectionPressed(e);
                }
            });
			addNextSection.setToolTipText(rbx.getString("AddPrimaryButtonHint"));			
			p1.add(p13);
			JPanel p14 = new JPanel();
			p14.setLayout(new FlowLayout());
			p14.add (alternateSectionBox);
			alternateSectionBox.setToolTipText(rbx.getString("AlternateSectionBoxHint"));
			p14.add (addAlternateSection = new JButton(rbx.getString("AddAlternateButton")));
            addAlternateSection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addAlternateSectionPressed(e);
                }
            });
			addAlternateSection.setToolTipText(rbx.getString("AddAlternateButtonHint"));			
			p1.add(p14);
			addFrame.getContentPane().add(p1);
			// set up bottom buttons
			addFrame.getContentPane().add(new JSeparator());
			JButton cancel = null;
			JPanel pb = new JPanel();
			pb.setLayout (new FlowLayout());
            pb.add(cancel = new JButton(rb.getString("ButtonCancel")));
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
			cancel.setToolTipText(rbx.getString("CancelButtonHint"));
            pb.add(create = new JButton(rb.getString("ButtonCreate")));
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
			create.setToolTipText(rbx.getString("SectionCreateButtonHint"));
            pb.add(update = new JButton(rb.getString("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
			update.setToolTipText(rbx.getString("SectionUpdateButtonHint"));
			addFrame.getContentPane().add(pb);
        }
		if (editMode) {
			// setup for edit window
			create.setVisible(false);
			update.setVisible(true);
			sysName.setVisible(false);
			sysNameFixed.setVisible(true);
			initializeEditInformation();
		}
		else {
			// setup for create window
			create.setVisible(true);
			update.setVisible(false);
			sysName.setVisible(true);
			sysNameFixed.setVisible(false);
			deleteAllSections(null);
		}
		initializeSectionCombos();
        addFrame.pack();
        addFrame.setVisible(true);
    }
	private void initializeEditInformation() {
		sectionList.clear();
		curSection = null;
		curSectionDirection = 0;
		curSequenceNum = 0;
		prevSection = null;
		prevSectionDirection = 0;
		if (curTransit!=null) {		
			userName.setText(curTransit.getUserName());
			ArrayList tsList = curTransit.getTransitSectionList();
			for (int i = 0; i<tsList.size(); i++) {
				TransitSection ts = (TransitSection)tsList.get(i);
				if (ts!=null) {
					sectionList.add((Object)ts.getSection());
					sequence[i] = ts.getSequenceNumber();
					direction[i] = ts.getDirection();
					action[i] = ts.getAction();
					data[i] = ts.getData();
					alternate[i] = ts.isAlternate();
				}
			}
			int index = sectionList.size()-1;
			while (alternate[index] && (index>0)) index--;
			if (index>=0) {
				curSection = (Section)sectionList.get(index);
				curSequenceNum = sequence[index];
				if (index>0) curSectionDirection = direction[index];
				index --;
				while (alternate[index] && (index>=0)) index--;
				if (index>=0) {
					prevSection = (Section)sectionList.get(index);
					prevSectionDirection = direction[index];
				}
			}
		}
		sectionTableModel.fireTableDataChanged();
	}
	private void deleteAllSections(ActionEvent e) {
		sectionList.clear();
		for (int i = 0; i<maxSections; i++) {
			direction[i] = Section.FORWARD;
			sequence[i] = 0;
			action[i] = TransitSection.NONE;
			data[i] = 5;
			alternate[i] = false;
		}
		curSection = null;
		curSectionDirection = 0;
		prevSection = null;
		prevSectionDirection = 0;
		curSequenceNum = 0;
		initializeSectionCombos();
		sectionTableModel.fireTableDataChanged();
	}
	void addNextSectionPressed(ActionEvent e) {
		if (sectionList.size()>maxSections) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message23"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (primarySectionBoxList.size()==0) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message25"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);	
			return;		
		}
		int index = primarySectionBox.getSelectedIndex();
		Section s = (Section)primarySectionBoxList.get(index);
		if (s!=null) {
			int j = sectionList.size();
			sectionList.add((Object)s);
			direction[j] = priSectionDirection[index];
			curSequenceNum ++;
			sequence[j] = curSequenceNum;
			action[j] = TransitSection.NONE;
			data[j] = 5;
			alternate[j] = false;
			if ( (sectionList.size()==2) && (curSection!=null) ) {
				if (forwardConnected(curSection,s,0)) {
					direction[0] = Section.REVERSE;
				}
				curSectionDirection = direction[0];
			}
			prevSection = curSection;
			prevSectionDirection = curSectionDirection;
			curSection = s;
			if (prevSection!=null) {
				curSectionDirection = direction[j];
			}
			initializeSectionCombos();
		}	
		sectionTableModel.fireTableDataChanged();
	}
	void addAlternateSectionPressed(ActionEvent e) {
		if (sectionList.size()>maxSections) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message23"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (alternateSectionBoxList.size()==0) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message24"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);	
			return;		
		}
		int index = alternateSectionBox.getSelectedIndex();
		Section s = (Section)alternateSectionBoxList.get(index);
		if (s!=null) {
			int j = sectionList.size();
			sectionList.add((Object)s);
			direction[j] = altSectionDirection[index];
			sequence[j] = curSequenceNum;
			action[j] = TransitSection.NONE;
			data[j] = 5;
			alternate[j] = true;
			initializeSectionCombos();
		}	
		sectionTableModel.fireTableDataChanged();
		return;
	}
    void createPressed(ActionEvent e) {
		if (!checkTransitInformation()) {
			return;
		}
        String uName = userName.getText();
        if (uName.equals("")) uName=null;
        String sName = sysName.getText().toUpperCase();
		// attempt to create the new Transit
        curTransit = transitManager.createNewTransit(sName, uName);
		if (curTransit==null) {
			javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
					.getString("Message22"), rbx.getString("ErrorTitle"),
					javax.swing.JOptionPane.ERROR_MESSAGE);			
			return;
		}
		sysName.setText(curTransit.getSystemName());
		setTransitInformation();
		addFrame.setVisible(false);
    }
	void cancelPressed(ActionEvent e) {
		addFrame.setVisible(false);
	}
	void updatePressed(ActionEvent e) {
		if (!checkTransitInformation()) {
			return;
		}
		// check if user name has been changed
        String uName = userName.getText();
        if (uName.equals("")) uName=null;
		if ( (uName!=null) && (!uName.equals(curTransit.getUserName())) ) {
			// check that new user name is unique
			Transit tTransit = transitManager.getByUserName(uName);
			if (tTransit!=null) {
				javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
						.getString("Message22"), rbx.getString("ErrorTitle"),
						javax.swing.JOptionPane.ERROR_MESSAGE);			
				return;
			}
		}				
		curTransit.setUserName(uName);
		if (setTransitInformation()) {
			// successful update
			addFrame.setVisible(false);
		}
	}
	private boolean checkTransitInformation() {
// add code here
		return true;
	}
	private boolean setTransitInformation() {
		if (curTransit==null) return false;
		curTransit.removeAllSections();
		for (int i = 0; i<sectionList.size(); i++) {
			TransitSection ts = new TransitSection((Section)sectionList.get(i),
				sequence[i], direction[i], action[i], data[i], alternate[i]);
			if (ts==null) {
				log.error("Trouble creating TransitSection");
				return false;
			}
			curTransit.addTransitSection(ts);
		}
		return true;
	}
	private void initializeSectionCombos() {
		ArrayList allSections = (ArrayList)sectionManager.getSystemNameList();
		primarySectionBox.removeAllItems();
		alternateSectionBox.removeAllItems();
		primarySectionBoxList.clear();
		alternateSectionBoxList.clear();
		if (sectionList.size()==0) {
			// no Sections currently in Transit - all Sections and all Directions OK
			for (int i = 0; i<allSections.size(); i++) {
				String sName = (String)allSections.get(i);
				Section s = sectionManager.getBySystemName(sName);
				if (s!=null) {
					if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
						sName = sName+"( "+s.getUserName()+" )";
					primarySectionBox.addItem(sName);
				    primarySectionBoxList.add((Object)s);
					priSectionDirection[primarySectionBoxList.size()-1] = Section.FORWARD;
				}
			}
		}
		else {
			// limit to Sections that connect to the current Section and are not the previous Section
			for (int i = 0; i<allSections.size(); i++) {
				String sName = (String)allSections.get(i);
				Section s = sectionManager.getBySystemName(sName);
				if (s!=null) {
					if ( (s!=prevSection) && (forwardConnected(s,curSection,curSectionDirection)) ) {
						if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
							sName = sName+"( "+s.getUserName()+" )";
						primarySectionBox.addItem(sName);
						primarySectionBoxList.add((Object)s);
						priSectionDirection[primarySectionBoxList.size()-1] = Section.FORWARD;
					}
					else if ( (s!=prevSection) && (reverseConnected(s,curSection,curSectionDirection)) ) {
						if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
							sName = sName+"( "+s.getUserName()+" )";
						primarySectionBox.addItem(sName);
						primarySectionBoxList.add((Object)s);
						priSectionDirection[primarySectionBoxList.size()-1] = Section.REVERSE;
					}
				}
			}
			// check if there are any alternate Section choices
			if ( prevSection!=null ) {
				for (int i = 0; i<allSections.size(); i++) {
					String sName = (String)allSections.get(i);
					Section s = sectionManager.getBySystemName(sName);
					if (s!=null) {
						if ( (notIncludedWithSeq(s,curSequenceNum)) && 
											forwardConnected(s,prevSection,prevSectionDirection) ) {
							if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
								sName = sName+"( "+s.getUserName()+" )";
							alternateSectionBox.addItem(sName);
							alternateSectionBoxList.add((Object)s);							
							altSectionDirection[primarySectionBoxList.size()-1] = Section.FORWARD;
						}
						else if ( notIncludedWithSeq(s,curSequenceNum) && 
											reverseConnected(s,prevSection,prevSectionDirection) ) {
							if ( (s.getUserName()!=null) && (!s.getUserName().equals("")) )
								sName = sName+"( "+s.getUserName()+" )";
							alternateSectionBox.addItem(sName);
							alternateSectionBoxList.add((Object)s);							
							altSectionDirection[primarySectionBoxList.size()-1] = Section.REVERSE;
						}
					}
				}
			}							
		}
	}
	private boolean connected(Section s1, Section s2) {
		if ( (s1!=null) && (s2!=null) ) {
			ArrayList s1Entries = (ArrayList)s1.getEntryPointList();
			ArrayList s2Entries = (ArrayList)s2.getEntryPointList();
			for (int i = 0; i<s1Entries.size(); i++) {
				Block b = ((EntryPoint)s1Entries.get(i)).getFromBlock();
				for (int j = 0; j<s2Entries.size(); j++) {
					if (b == ((EntryPoint)s2Entries.get(j)).getBlock()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean forwardConnected(Section s1, Section s2, int restrictedDirection) {
		if ( (s1!=null) && (s2!=null) ) {
			ArrayList s1ForwardEntries = (ArrayList)s1.getForwardEntryPointList();
			ArrayList s2Entries = new ArrayList();
			if ( restrictedDirection == Section.FORWARD ) {
				s2Entries = (ArrayList)s2.getReverseEntryPointList();
			}
			else if ( restrictedDirection == Section.REVERSE ) {
				s2Entries = (ArrayList)s2.getForwardEntryPointList();
			}
			else {
				s2Entries = (ArrayList)s2.getEntryPointList();
			}
			for (int i = 0; i<s1ForwardEntries.size(); i++) {
				Block b1 = ((EntryPoint)s1ForwardEntries.get(i)).getFromBlock();
				for (int j = 0; j<s2Entries.size(); j++) {
					Block b2 = ((EntryPoint)s2Entries.get(j)).getFromBlock();
					if ( (b1 == ((EntryPoint)s2Entries.get(j)).getBlock()) &&
							(b2 == ((EntryPoint)s1ForwardEntries.get(i)).getBlock()) ) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean reverseConnected(Section s1, Section s2, int restrictedDirection) {
		if ( (s1!=null) && (s2!=null) ) {
			ArrayList s1ReverseEntries = (ArrayList)s1.getReverseEntryPointList();
			ArrayList s2Entries = new ArrayList();
			if ( restrictedDirection == Section.FORWARD ) {
				s2Entries = (ArrayList)s2.getReverseEntryPointList();
			}
			else if ( restrictedDirection == Section.REVERSE ) {
				s2Entries = (ArrayList)s2.getForwardEntryPointList();
			}
			else {
				s2Entries = (ArrayList)s2.getEntryPointList();
			}
			for (int i = 0; i<s1ReverseEntries.size(); i++) {
				Block b1 = ((EntryPoint)s1ReverseEntries.get(i)).getFromBlock();
				for (int j = 0; j<s2Entries.size(); j++) {
					Block b2 = ((EntryPoint)s2Entries.get(j)).getFromBlock();
					if ( (b1 == ((EntryPoint)s2Entries.get(j)).getBlock()) &&
							(b2 == ((EntryPoint)s1ReverseEntries.get(i)).getBlock()) ) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean notIncludedWithSeq(Section s, int seq) {
		for (int i = 0; i<sectionList.size(); i++) {
			if ( (sectionList.get(i)==(Object)s) && (seq==sequence[i]) ) return false;
		}
		return true;
	}
	
    private boolean noWarn = false;
	
	/**
	 * Table model for Sections in Create/Edit Transit window
	 */
	public class SectionTableModel extends javax.swing.table.AbstractTableModel implements
			java.beans.PropertyChangeListener {

		public static final int SEQUENCE_COLUMN = 0;
		public static final int SECTIONNAME_COLUMN = 1;
		public static final int ACTION_COLUMN = 2;
		public static final int DATA_COLUMN = 3;
		public static final int ALTERNATE_COLUMN = 4;

		public SectionTableModel() {
			super();
			sectionManager.addPropertyChangeListener(this);
		}

		public void propertyChange(java.beans.PropertyChangeEvent e) {
			if (e.getPropertyName().equals("length")) {
				// a new NamedBean is available in the manager
				fireTableDataChanged();
			}
		}

		public Class getColumnClass(int c) {
			if ( c==ACTION_COLUMN ) 
				return JComboBox.class;
			return String.class;
		}

		public int getColumnCount() {
			return ALTERNATE_COLUMN+1;
		}

		public int getRowCount() {
			return (sectionList.size());
		}

		public boolean isCellEditable(int r, int c) {
			if ( c==ACTION_COLUMN ) 
				return (true);
			if ( ( c==DATA_COLUMN ) && ( action[r]==TransitSection.PAUSE ) )
				return (true);
			return (false);
		}

		public String getColumnName(int col) {
			switch (col) {
			case SEQUENCE_COLUMN:
				return rbx.getString("SequenceColName");
			case SECTIONNAME_COLUMN:
				return rbx.getString("SectionName");
			case ACTION_COLUMN:
				return rbx.getString("ActionColName");
			case ALTERNATE_COLUMN:
				return rbx.getString("AlternateColName");
			default:
				return "";
			}
		}

		public int getPreferredWidth(int col) {
			switch (col) {
			case SEQUENCE_COLUMN:
				return new JTextField(8).getPreferredSize().width;				
			case SECTIONNAME_COLUMN:
				return new JTextField(17).getPreferredSize().width;
			case ACTION_COLUMN:
				return new JTextField(12).getPreferredSize().width;				
			case DATA_COLUMN:
				return new JTextField(8).getPreferredSize().width;				
			case ALTERNATE_COLUMN:
				return new JTextField(12).getPreferredSize().width;	
			}
			return new JTextField(5).getPreferredSize().width;
		}

		public Object getValueAt(int r, int c) {
			int rx = r;
			if (rx > sectionList.size()) {
				return null;
			}
			switch (c) {
				case SEQUENCE_COLUMN:
					return (""+sequence[rx]);
				case SECTIONNAME_COLUMN:
					String s = ((Section)(sectionList.get(rx))).getSystemName();
					String u = ((Section)(sectionList.get(rx))).getUserName();
					if ( (u!=null) && (!u.equals("")) ) {
						return (s+"( "+u+" )");
					}
					return s;
				case ACTION_COLUMN:
					if ( action[rx]==TransitSection.NONE )
						return rbx.getString("None");
					else if ( action[rx]==TransitSection.PAUSE )
						return rbx.getString("Pause");
					else if ( action[rx]==TransitSection.WAIT )
						return rbx.getString("Wait");
				case DATA_COLUMN:
					if ( action[rx]==TransitSection.PAUSE )
						return (""+data[rx]);
					return (" ");						
				case ALTERNATE_COLUMN:
					if ( alternate[rx] )
						return rbx.getString("Alternate");
					return rbx.getString("Primary");						
				default:
					return rbx.getString("Unknown");
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col==ACTION_COLUMN) {
				if (((String)value).equals(rbx.getString("None"))) {
					action[row] = TransitSection.NONE;
				}
				else if (((String)value).equals(rbx.getString("Pause"))) {
					action[row] = TransitSection.PAUSE;
				}
				else if (((String)value).equals(rbx.getString("Wait"))) {
					action[row] = TransitSection.WAIT;
				}
			}
			if (col==DATA_COLUMN) {
				data[row] = Integer.parseInt((String)value);
			}
			return;
		}
	}

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TransitTableAction.class.getName());
}

/* @(#)TransitTableAction.java */
