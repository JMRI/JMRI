// AbstractConnectionConfig.java

package jmri.jmrix;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Abstract base class for common implementation of the ConnectionConfig
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.24 $
 */
abstract public class AbstractConnectionConfig implements jmri.jmrix.ConnectionConfig {

    /**
     * Ctor for an object being created during load process
     */
    public AbstractConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        adapter = p;
    }
    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass setInstance() will fill the adapter member.
     */
    public AbstractConnectionConfig() {
    }

    boolean init = false;

    protected void checkInitDone() {
    	if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;
        portBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.setPort((String)portBox.getSelectedItem());
            }
        });
        baudBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureBaudRate((String)baudBox.getSelectedItem());
            }
        });
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
        
        init = true;
    }

    protected JComboBox portBox = new JComboBox();
    protected JComboBox baudBox = new JComboBox();
    protected JComboBox opt1Box = new JComboBox();
    protected JComboBox opt2Box = new JComboBox();

    protected jmri.jmrix.SerialPortAdapter adapter = null;

    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    public String getInfo() {
        String t = (String)portBox.getSelectedItem();
        if (t!=null) return t;
        else return "(none)";
    }

    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
	public void loadDetails(JPanel details) {
    	
        setInstance();

        Vector<String> v;
        try {
            v = adapter.getPortNames();
    	    if (log.isDebugEnabled()) {
    		    log.debug("loadDetails called in class "+this.getClass().getName());
    		    log.debug("adapter class: "+adapter.getClass().getName());
    		    log.debug("loadDetails called for "+name());
        	    if (v!=null) log.debug("Found "+v.size()+" ports");
        	    else log.debug("Zero-length port vector");
            }
        } catch (java.lang.UnsatisfiedLinkError e1) {
            log.error("UnsatisfiedLinkError - the javax.comm library has not been installed properly");
            log.error("java.library.path="+System.getProperty("java.library.path","<unknown>"));
            javax.swing.JOptionPane.showMessageDialog(null, "Failed to load comm library.\nYou have to fix that before setting preferences.");
            return;
        }

        portBox.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(portBox.getActionListeners().length >0)
        	portBox.removeActionListener(portBox.getActionListeners()[0]);
        if(v==null){
        	log.error("port name Vector v is null!");
        	return;
        }
        for (int i=0; i<v.size(); i++) {
                portBox.addItem(v.elementAt(i));
        }
        if (v.size()==0)
        	portBox.addItem(rb.getString("noPortsFound"));

        String[] baudList = adapter.validBaudRates();
        baudBox.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(baudBox.getActionListeners().length >0)
        	baudBox.removeActionListener(baudBox.getActionListeners()[0]);
    	if (log.isDebugEnabled()) log.debug("after remove, "+baudBox.getItemCount()+" items, first is "
    											+baudBox.getItemAt(0));        
        for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    	if (log.isDebugEnabled()) log.debug("after reload, "+baudBox.getItemCount()+" items, first is "
    											+baudBox.getItemAt(0));        
    	
        String[] opt1List = adapter.validOption1();
        opt1Box.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt1Box.getActionListeners().length >0)
        	opt1Box.removeActionListener(opt1Box.getActionListeners()[0]);
        for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
        String[] opt2List = adapter.validOption2();
        opt2Box.removeAllItems();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt2Box.getActionListeners().length >0)
        	opt2Box.removeActionListener(opt2Box.getActionListeners()[0]);
        for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

        if (baudList.length>1) {
            baudBox.setToolTipText("Must match the baud rate setting of your hardware");
            baudBox.setEnabled(true);
        } else {
            baudBox.setToolTipText("The baud rate is fixed for this protocol");
            baudBox.setEnabled(false);
        }
        if (opt1List.length>1) {
            opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
            opt1Box.setEnabled(true);
        } else {
            opt1Box.setToolTipText("There are no options for this protocol");
            opt1Box.setEnabled(false);
        }
        if (opt2List.length>1) {
            opt2Box.setToolTipText("");
            opt2Box.setEnabled(true);
        } else {
            opt2Box.setToolTipText("There are no options for this protocol");
            opt2Box.setEnabled(false);
        }

        int rows = 2;
        if (opt1List.length>1) rows++;
        if (opt2List.length>1) rows++;

        details.setLayout(new GridLayout(rows,2));
        details.add(new JLabel("Serial port: "));
        details.add(portBox);
        
        String portName = adapter.getCurrentPortName();
        if (portName != null && !portName.equals(rb.getString("noneSelected")) && !portName.equals(rb.getString("noPortsFound"))){
        	// portBox must contain portName even if it doesn't exist
        	if(!v.contains(portName))
        		portBox.insertItemAt(portName, 0);
        	portBox.setSelectedItem(portName);
        } else {
            portBox.insertItemAt(rb.getString("noneSelected"),0);
            portBox.setSelectedIndex(0);
        }
          
        details.add(new JLabel("Baud rate:"));
        details.add(baudBox);
        baudBox.setSelectedItem(adapter.getCurrentBaudRate());
        if (opt1List.length>1) {
            details.add(new JLabel(adapter.option1Name()));
            details.add(opt1Box);
            opt1Box.setSelectedItem(adapter.getCurrentOption1Setting());
        }
        if (opt2List.length>1) {
            details.add(new JLabel(adapter.option2Name()));
            details.add(opt2Box);
            opt2Box.setSelectedItem(adapter.getCurrentOption2Setting());
        }

        init = false;		// need to reload action listeners
        checkInitDone();
    }

    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractConnectionConfig.class.getName());

}

