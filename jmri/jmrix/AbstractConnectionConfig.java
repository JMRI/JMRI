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
 * @version	$Revision: 1.5 $
 */
abstract public class AbstractConnectionConfig  implements jmri.jmrix.ConnectionConfig {

    /**
     * Ctor for an object being created during load process
     */
    public AbstractConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        adapter = p;
    }
    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass ctor will fill the adapter member.
     */
    public AbstractConnectionConfig() {
        adapter = null;
    }

    boolean init = false;

    void checkInitDone() {
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
    }

    protected JComboBox portBox = new JComboBox();
    protected JComboBox baudBox = new JComboBox();
    protected JComboBox opt1Box = new JComboBox();
    protected JComboBox opt2Box = new JComboBox();

    protected jmri.jmrix.SerialPortAdapter adapter;

    /**
     * Load the adapter with an appropriate object
     */
    abstract protected void setInstance();

    public String getInfo() {
        String t = (String)portBox.getSelectedItem();
        if (t!=null) return t;
        else return "(none)";
    }

    public void loadDetails(JPanel details) {
    	
        setInstance();

        Vector v = adapter.getPortNames();
    	if (log.isDebugEnabled()) {
    		log.debug("loadDetails called in class "+this.getClass().getName());
    		log.debug("adapter class: "+adapter.getClass().getName());
    		log.debug("loadDetails called for "+name());
        	log.debug("Found "+v.size()+" ports");
        }
        String portName;
        portBox.removeAllItems();
        for (int i=0; i<v.size(); i++) {
            if (i==0) portName = (String) v.elementAt(i);
                portBox.addItem(v.elementAt(i));
        }

        String[] baudList = adapter.validBaudRates();
        baudBox.removeAllItems();
    	if (log.isDebugEnabled()) log.debug("after remove, "+baudBox.getItemCount()+" items, first is "
    											+baudBox.getItemAt(0));        
        for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    	if (log.isDebugEnabled()) log.debug("after reload, "+baudBox.getItemCount()+" items, first is "
    											+baudBox.getItemAt(0));        
    	
        String[] opt1List = adapter.validOption1();
        opt1Box.removeAllItems();
        for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
        String[] opt2List = adapter.validOption2();
        opt2Box.removeAllItems();
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
        portBox.setSelectedItem(adapter.getCurrentPortName());
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

        checkInitDone();
    }

    static protected org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractConnectionConfig.class.getName());

}

