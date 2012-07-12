// AbstractSimulatorConnectionConfig.java

package jmri.jmrix;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JOptionPane;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Abstract base class for common implementation of the Simulator ConnectionConfig
 * Currently uses the serial adapter, but this will change to the simulator adapter
 * in due course.
 *
 * @author      Kevin Dickerson   Copyright (C) 2001, 2003
 * @version	$Revision$
 */

//
abstract public class AbstractSimulatorConnectionConfig extends AbstractConnectionConfig implements jmri.jmrix.ConnectionConfig {

    /**
     * Ctor for an object being created during load process
     * Currently uses the serialportadapter, but this will 
     * change to a simulator port adapter in due course.
     */
    public AbstractSimulatorConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        adapter = p;
    }

    public jmri.jmrix.SerialPortAdapter getAdapter() { return adapter; }
    
    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass setInstance() will fill the adapter member.
     */
    public AbstractSimulatorConnectionConfig() {
        adapter = null;
    }

    protected boolean init = false;

    protected void checkInitDone() {
    	if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;

        opt1Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureOption1((String)opt1Box.getSelectedItem());
            }
        });
        opt2Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureOption2((String)opt2Box.getSelectedItem());
            }
        });
        opt3Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureOption3((String)opt3Box.getSelectedItem());
            }
        });
        opt4Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureOption4((String)opt4Box.getSelectedItem());
            }
        });

        if(adapter.getSystemConnectionMemo()!=null){
            systemPrefixField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
            });
            systemPrefixField.addFocusListener( new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
                public void focusGained(FocusEvent e){ }
            });
            connectionNameField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())){
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
            });
            connectionNameField.addFocusListener( new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())){
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
                public void focusGained(FocusEvent e){ }
            });
        }
        init = true;
    }

    public void updateAdapter(){
        adapter.configureOption1((String)opt1Box.getSelectedItem());
        adapter.configureOption2((String)opt2Box.getSelectedItem());
        adapter.configureOption3((String)opt3Box.getSelectedItem());
        adapter.configureOption4((String)opt4Box.getSelectedItem());

        if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    
    protected String[] baudList;
    protected jmri.jmrix.SerialPortAdapter adapter = null;

    protected String systemPrefix;
    protected String connectionName;

    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    /**
     * Returns the port the simulator is connected to
     * which is "none";
     */
    public String getInfo() {
        return rb.getString("none");
    }

    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
	public void loadDetails(final JPanel details) {
        _details = details;
        setInstance();

        if(adapter.getSystemConnectionMemo()!=null){
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    	
        opt1List = adapter.validOption1();
        opt1Box.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt1Box.getActionListeners().length >0)
        	opt1Box.removeActionListener(opt1Box.getActionListeners()[0]);
        for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);

        opt2List = adapter.validOption2();
        opt2Box.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt2Box.getActionListeners().length >0)
        	opt2Box.removeActionListener(opt2Box.getActionListeners()[0]);
        for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

        opt3List = adapter.validOption3();
        opt3Box.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt3Box.getActionListeners().length >0)
        	opt3Box.removeActionListener(opt3Box.getActionListeners()[0]);
        for (int i=0; i<opt3List.length; i++) opt3Box.addItem(opt3List[i]);

        opt4List = adapter.validOption4();
        opt4Box.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt4Box.getActionListeners().length >0)
        	opt4Box.removeActionListener(opt4Box.getActionListeners()[0]);
        for (int i=0; i<opt4List.length; i++) opt4Box.addItem(opt2List[i]);

        if (opt1List.length>1) {
            opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
            opt1Box.setEnabled(true);
            opt1Box.setSelectedItem(adapter.getCurrentOption1Setting());
        } else {
            opt1Box.setToolTipText("There are no options for this protocol");
            opt1Box.setEnabled(false);
        }
        if (opt2List.length>1) {
            opt2Box.setToolTipText("");
            opt2Box.setEnabled(true);
            opt2Box.setSelectedItem(adapter.getCurrentOption2Setting());
        } else {
            opt2Box.setToolTipText("There are no options for this protocol");
            opt2Box.setEnabled(false);
        }
        if (opt3List.length>1) {
            opt3Box.setToolTipText("");
            opt3Box.setEnabled(true);
            opt3Box.setSelectedItem(adapter.getCurrentOption3Setting());
        } else {
            opt3Box.setToolTipText("There are no options for this protocol");
            opt3Box.setEnabled(false);
        }
        if (opt4List.length>1) {
            opt4Box.setToolTipText("");
            opt4Box.setEnabled(true);
            opt4Box.setSelectedItem(adapter.getCurrentOption4Setting());
        } else {
            opt4Box.setToolTipText("There are no options for this protocol");
            opt4Box.setEnabled(false);
        }
    
        showAdvanced.setFont(showAdvanced.getFont().deriveFont(9f));
        showAdvanced.setForeground(Color.blue);
        showAdvanced.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e){
                    showAdvancedItems();
                }
            });
        showAdvancedItems();
        init = false;		// need to reload action listeners
        checkInitDone();
    }
    
    protected void showAdvancedItems(){
        _details.removeAll();
        _details.setLayout(new GridLayout(0,2));	// 0 = any number of rows
        boolean incAdvancedOptions=false;
        if ((isOptList1Advanced())&&(opt1List.length>1)) incAdvancedOptions=true;
        if ((isOptList2Advanced())&&(opt2List.length>1)) incAdvancedOptions=true;
        if ((isOptList3Advanced())&&(opt3List.length>1)) incAdvancedOptions=true;
        if ((isOptList4Advanced())&&(opt4List.length>1)) incAdvancedOptions=true;
        addStandardDetails(incAdvancedOptions);
        
        if (showAdvanced.isSelected()) {
            if ((isOptList1Advanced())&&(opt1List.length>1)) {
                _details.add(opt1BoxLabel = new JLabel(adapter.option1Name()));
                _details.add(opt1Box);
            }
            if ((isOptList2Advanced())&&(opt2List.length>1)) {
                _details.add(opt2BoxLabel = new JLabel(adapter.option2Name()));
                _details.add(opt2Box);
            }           
            if ((isOptList3Advanced())&&(opt3List.length>1)) {
                _details.add(opt3BoxLabel = new JLabel(adapter.option3Name()));
                _details.add(opt3Box);
            }           
            if ((isOptList4Advanced())&&(opt4List.length>1)) {
                _details.add(opt4BoxLabel = new JLabel(adapter.option4Name()));
                _details.add(opt4Box);
            }           
        }
        _details.validate();
        if (_details.getTopLevelAncestor()!=null){
            ((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).setSize(((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).getPreferredSize());
            ((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).pack();
        }
        _details.repaint();
    }
    
    protected void addStandardDetails(boolean incAdvanced){
        if ((!isOptList1Advanced())&&(opt1List.length>1)){
            _details.add(opt1BoxLabel);
            _details.add(opt1Box);
        }
        
        if ((!isOptList2Advanced())&&(opt2List.length>1)) {
            _details.add(opt2BoxLabel);
            _details.add(opt2Box);
        }

        if ((!isOptList3Advanced())&&(opt3List.length>1)) {
            _details.add(opt3BoxLabel);
            _details.add(opt3Box);
        }
        
        if ((!isOptList4Advanced())&&(opt4List.length>1)) {
            _details.add(opt4BoxLabel);
            _details.add(opt4Box);
        }
        
        if(adapter.getSystemConnectionMemo()!=null){
            _details.add(systemPrefixLabel);
            _details.add(systemPrefixField);
            _details.add(connectionNameLabel);
            _details.add(connectionNameField);
        }
        if (incAdvanced){
            _details.add(new JLabel(" "));
            _details.add(showAdvanced);
        }
    }

    public String getManufacturer() { return adapter.getManufacturer(); }
    public void setManufacturer(String manufacturer) { adapter.setManufacturer(manufacturer); }
    
    public String getConnectionName() {
        if(adapter.getSystemConnectionMemo()!=null)
            return adapter.getSystemConnectionMemo().getUserName();
        else return null;
    }
    
    public boolean getDisabled() {
        if (adapter==null) return true;
        return adapter.getDisabled();
    }
    public void setDisabled(boolean disabled) { adapter.setDisabled(disabled); }
    
    public void dispose(){
        if (adapter!=null){
            adapter.dispose();
            adapter=null;
        }
    }

    final static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractSimulatorConnectionConfig.class.getName());
}