// JmrixConfigPane.java

package jmri.jmrix;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
//import javax.swing.JSeparator;

/**
 * Provide GUI to configure communications links.
 * <P>
 * This is really just a catalog of connections to classes within the systems. 
 * Reflection is used to reduce coupling at load time.
 * <P>
 * Objects of this class are based on an underlying ConnectionConfig implementation,
 * which in turn is obtained from the InstanceManager.
 * Those must be created at load time by the ConfigXml process, or in 
 * some Application class.
 * <P>
 * To add a new system package, add entries
 * to the list in {@link #availableProtocolClasses}.
 * <P>
 * The classes referenced are the specific
 * subclasses of {@link jmri.jmrix.AbstractConnectionConfig}
 * which provides the methods providing data to the 
 * configuration GUI, and responding to its changes.
 * <p>
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2004, 2010
 * @version	$Revision: 1.62 $
 */
public class JmrixConfigPane extends JPanel {

    /**
     * Get access to a pane describing existing configuration
     * information, or create one if needed.
     * <P>
     * The index argument is used to connect the new pane to the right
     * communications info.  A value of "1" means the first (primary) port,
     * 2 is the second, etc.
     * @param index 1-N based index of the communications object to configure.
     */
    public static JmrixConfigPane instance(int index) {
        JmrixConfigPane retval = configPaneTable.get(new Integer(index));
        if (retval != null) return retval;
        
        Object c = InstanceManager.configureManagerInstance()
                                .findInstance(ConnectionConfig.class, index);
        log.debug("findInstance returned "+c);
        retval = new JmrixConfigPane((ConnectionConfig)c);
        configPaneTable.put(new Integer(index), retval);
        return retval;
    }

    static final java.util.Hashtable<Integer, JmrixConfigPane> configPaneTable 
                    = new java.util.Hashtable<Integer, JmrixConfigPane>();
    
    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
    public static final String NONE_SELECTED = rb.getString("noneSelected");
    public static final String NO_PORTS_FOUND = rb.getString("noPortsFound");
    
    public String[] availableProtocolClasses() {
        return  new String[] {

                              "jmri.jmrix.internal.ConnectionConfig",
                              "jmri.jmrix.acela.serialdriver.ConnectionConfig",
                              "jmri.jmrix.bachrus.serialdriver.ConnectionConfig",
                              "jmri.jmrix.cmri.serial.serialdriver.ConnectionConfig",
                              "jmri.jmrix.cmri.serial.sim.ConnectionConfig",
                              "jmri.jmrix.easydcc.serialdriver.ConnectionConfig",
                              "jmri.jmrix.easydcc.networkdriver.ConnectionConfig",  
                              "jmri.jmrix.grapevine.serialdriver.ConnectionConfig",
                              "jmri.jmrix.lenz.hornbyelite.ConnectionConfig",
                              "jmri.jmrix.lenz.li100.ConnectionConfig",
                              "jmri.jmrix.lenz.li100f.ConnectionConfig",
                              "jmri.jmrix.lenz.li101.ConnectionConfig",
                              "jmri.jmrix.lenz.liusb.ConnectionConfig",
                              "jmri.jmrix.loconet.locobufferusb.ConnectionConfig",
                              "jmri.jmrix.loconet.pr2.ConnectionConfig",
                              "jmri.jmrix.loconet.pr3.ConnectionConfig",
                              "jmri.jmrix.loconet.hexfile.ConnectionConfig",
                              "jmri.jmrix.loconet.Intellibox.ConnectionConfig",
                              "jmri.jmrix.loconet.locormi.ConnectionConfig",
                              "jmri.jmrix.loconet.loconetovertcp.ConnectionConfig",
                              "jmri.jmrix.loconet.locobufferii.ConnectionConfig",
                              "jmri.jmrix.loconet.locobuffer.ConnectionConfig",
                              "jmri.jmrix.loconet.ms100.ConnectionConfig",
                              "jmri.jmrix.nce.serialdriver.ConnectionConfig",
                              "jmri.jmrix.nce.networkdriver.ConnectionConfig",
                              "jmri.jmrix.nce.usbdriver.ConnectionConfig",
                              "jmri.jmrix.oaktree.serialdriver.ConnectionConfig",
                              "jmri.jmrix.powerline.serialdriver.ConnectionConfig",
                              "jmri.jmrix.qsi.serialdriver.ConnectionConfig",
                              "jmri.jmrix.rps.serial.ConnectionConfig",
                              "jmri.jmrix.secsi.serialdriver.ConnectionConfig",
                              "jmri.jmrix.sprog.sprog.ConnectionConfig",
                              "jmri.jmrix.sprog.sprogCS.ConnectionConfig",
                              "jmri.jmrix.tmcc.serialdriver.ConnectionConfig",
                              "jmri.jmrix.wangrow.serialdriver.ConnectionConfig",
                              "jmri.jmrix.lenz.xntcp.ConnectionConfig",
                              "jmri.jmrix.xpa.serialdriver.ConnectionConfig",
                              "jmri.jmrix.zimo.mx1.ConnectionConfig",
                              "jmri.jmrix.lenz.ztc640.ConnectionConfig",
                              // "jmri.jmrix.mrc.serialdriver.ConnectionConfig",     // experimental
                              "jmri.jmrix.srcp.networkdriver.ConnectionConfig",
                              "jmri.jmrix.direct.serial.ConnectionConfig",            // experimental

                              "jmri.jmrix.can.adapters.gridconnect.canrs.serialdriver.ConnectionConfig",
                              "jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver.ConnectionConfig",
                              "jmri.jmrix.can.adapters.lawicell.canusb.serialdriver.ConnectionConfig",
                              "jmri.jmrix.can.adapters.loopback.ConnectionConfig",

                              "jmri.jmrix.ecos.networkdriver.ConnectionConfig",
                              "jmri.jmrix.maple.serialdriver.ConnectionConfig",
                              "jmri.jmrix.lenz.xnetsimulator.ConnectionConfig", // experimental
                              "jmri.jmrix.lenz.liusbserver.ConnectionConfig" // experimental
        };
    }

    JComboBox modeBox = new JComboBox();
    JComboBox manuBox = new JComboBox();

    JPanel details = new JPanel();
    String[] classConnectionNameList;
    ConnectionConfig[] classConnectionList;
    String[] manufactureNameList;


    /**
     * Use "instance" to get one of these.  
     * That allows it to reconnect to existing information in an existing ConnectionConfig
     * object.
     * It's permitted to call this with a null argument, e.g. for when 
     * first configuring the system.
     */
    private JmrixConfigPane(ConnectionConfig original) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        manuBox.addItem(NONE_SELECTED);
        //manuBox.setModel(new javax.swing.DefaultComboBoxModel(jmri.jmrix.DCCManufacturerList.getSystemNames()))
        int n=1;
        manufactureNameList = jmri.jmrix.DCCManufacturerList.getSystemNames();
        for (int i=0; i<manufactureNameList.length; i++) {
            String manuName = manufactureNameList[i];
            if (original!=null && original.getManufacturer().equals(manuName)){
                manuBox.addItem(manuName);
                manuBox.setSelectedItem(manuName);
            } else {
                manuBox.addItem(manuName);
            }
        
        }
        manuBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateComboConnection();
            }
        });

        
        // get the list of ConnectionConfig items into a selection box
        classConnectionNameList = jmri.jmrix.DCCManufacturerList.getConnectionList((String)manuBox.getSelectedItem());
        classConnectionList = new jmri.jmrix.ConnectionConfig[classConnectionNameList.length+1];
        modeBox.addItem(NONE_SELECTED);
        if(manuBox.getSelectedIndex()!=0){
            modeBox.setEnabled(true);
        } else {
            modeBox.setSelectedIndex(0);
            modeBox.setEnabled(false);        
        }
        n=1;
        if (manuBox.getSelectedIndex()!=0){
            for (int i=0; i<classConnectionNameList.length; i++) {
                String className = classConnectionNameList[i];
                try {
                    ConnectionConfig config;
                    if (original!=null && original.getClass().getName().equals(className)) {
                        config = original;
                        log.debug("matched existing config object");
                        modeBox.addItem(config.name());
                        modeBox.setSelectedItem(config.name());
                        if (classConnectionNameList.length==1){
                            modeBox.setSelectedIndex(1);
                        }
                    } else {
                        Class<?> cl = Class.forName(classConnectionNameList[i]);
                        config = (ConnectionConfig)cl.newInstance();
                        modeBox.addItem(config.name());
                    }
                    classConnectionList[n++] = config;
                } catch (NullPointerException e) {
                    log.debug("Attempt to load "+classConnectionNameList[i]+" failed: "+e);
                    e.printStackTrace();
                } catch (Exception e) {
                    log.debug("Attempt to load "+classConnectionNameList[i]+" failed: "+e);
                }
            }
        }
        modeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                selection();
            }
        });
        JPanel manufacturerPanel = new JPanel();
        manufacturerPanel.setBorder(BorderFactory.createTitledBorder("System manufacturer:"));
        manufacturerPanel.add(manuBox);
        JPanel connectionPanel = new JPanel();
        connectionPanel.setBorder(BorderFactory.createTitledBorder("System connection:"));
        connectionPanel.add(modeBox);
        add(manufacturerPanel);
        add(connectionPanel);
        details.setBorder(BorderFactory.createTitledBorder("Settings:"));
        add(details);
        //add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        //updateComboConnection();
        selection();  // first time through, pretend we've selected a value
        			  // to load the rest of the GUI
    }

    public void updateComboConnection() {
        modeBox.removeAllItems();
        modeBox.addItem(NONE_SELECTED);
        classConnectionNameList = jmri.jmrix.DCCManufacturerList.getConnectionList((String)manuBox.getSelectedItem());
        classConnectionList = new jmri.jmrix.ConnectionConfig[classConnectionNameList.length+1];
        
        if(manuBox.getSelectedIndex()!=0){
            modeBox.setEnabled(true);
        } else {
            modeBox.setSelectedIndex(0);
            modeBox.setEnabled(false);        
        }
        
        int n=1;
        if (manuBox.getSelectedIndex()!=0){
            for (int i=0; i<classConnectionNameList.length; i++) {
                try {
                    jmri.jmrix.ConnectionConfig config;
                    Class<?> cl = Class.forName(classConnectionNameList[i]);
                    config = (jmri.jmrix.ConnectionConfig)cl.newInstance();
                    modeBox.addItem(config.name());
                    classConnectionList[n++] = config;
                    if (classConnectionNameList.length==1){
                        modeBox.setSelectedIndex(1);
                    }
                } catch (NullPointerException e) {
                    log.debug("Attempt to load "+classConnectionNameList[i]+" failed: "+e);
                    e.printStackTrace();
                } catch (Exception e) {
                    log.debug("Attempt to load "+classConnectionNameList[i]+" failed: "+e);
                }
            }
        }
    }
    
    void selection() {
        int current = modeBox.getSelectedIndex();
        details.removeAll();
        // first choice is -no- protocol chosen
        if (log.isDebugEnabled()) log.debug("new selection is "+current
        							+" "+modeBox.getSelectedItem());
        if ((current!=0)&&(current!=-1)){
            classConnectionList[current].loadDetails(details);
            classConnectionList[current].setManufacturer((String) manuBox.getSelectedItem());
        }
        validate();
        if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
        repaint();
    }
    
    public String getCurrentManufacturerName() {
        int current = modeBox.getSelectedIndex();
        if (current==0) return "(none)";
        System.out.println(classConnectionList[current].getManufacturer());
        return classConnectionList[current].getManufacturer();
    }

    public String getCurrentProtocolName() {
        int current = modeBox.getSelectedIndex();
        if (current==0) return "(none)";
        return classConnectionList[current].name();
    }
    public String getCurrentProtocolInfo() {
        int current = modeBox.getSelectedIndex();
        if (current==0) return "(none)";
        return classConnectionList[current].getInfo();
    }

    public Object getCurrentObject() {
        int current = modeBox.getSelectedIndex();
        if (current!=0) return classConnectionList[current];
        return null;
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JmrixConfigPane.class.getName());
}

