// JmrixConfigPane.java

package jmri.jmrix;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Provide GUI to configure communications links.
 * <P>
 * This is
 * really just a catalog of connections to classes within
 * the systems. Reflection is used to reduce coupling.
 * <P>
 * To add a new system package, add entries
 * to the list in {@link #availableProtocolClasses}.
 * <P>
 * The classes referenced are the specific
 * subclasses of {@link jmri.jmrix.AbstractConnectionConfig}
 * which provides the methods providing data to the 
 * configuration GUI, and responding to its changes.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2004
 * @version	$Revision: 1.13 $
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
        Object c = InstanceManager.configureManagerInstance()
                                .findInstance(ConnectionConfig.class, index);
        log.debug("findInstance returned "+c);
        return (new JmrixConfigPane((ConnectionConfig)c));
    }

    public String[] availableProtocolClasses() {
        return  new String[] {
                              "jmri.jmrix.cmri.serial.serialdriver.ConnectionConfig",
                              "jmri.jmrix.easydcc.serialdriver.ConnectionConfig",
                              "jmri.jmrix.lenz.li100.ConnectionConfig",
                              "jmri.jmrix.loconet.locobuffer.ConnectionConfig",
                              "jmri.jmrix.loconet.Intellibox.ConnectionConfig",
                              "jmri.jmrix.loconet.locobufferii.ConnectionConfig",
                              "jmri.jmrix.loconet.ms100.ConnectionConfig",
                              "jmri.jmrix.loconet.hexfile.ConnectionConfig",
                              "jmri.jmrix.loconet.locormi.ConnectionConfig",
                              "jmri.jmrix.loconet.loconetovertcp.ConnectionConfig",
                              "jmri.jmrix.nce.serialdriver.ConnectionConfig",
                              "jmri.jmrix.sprog.serialdriver.ConnectionConfig",
                              "jmri.jmrix.wangrow.serialdriver.ConnectionConfig",
                              "jmri.jmrix.xpa.serialdriver.ConnectionConfig",
                              "jmri.jmrix.zimo.mx1.ConnectionConfig",
                              "jmri.jmrix.easydcc.networkdriver.ConnectionConfig",  // last, as experimental
                              "jmri.jmrix.direct.serial.ConnectionConfig"  // last, as experimental
        };
    }

    JComboBox modeBox = new JComboBox();

    JPanel details = new JPanel();;
    String[] classNameList;
    ConnectionConfig[] classList;

    /**
     * Use "instance" to get one of these.  That allows it
     * to reconnect to existing information.
     */
    private JmrixConfigPane(ConnectionConfig original) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        classNameList = availableProtocolClasses();
        classList = new ConnectionConfig[classNameList.length+1];

        // get the list of ConnectionConfig items into a selection box
        modeBox.addItem("(none selected)");
        int n=1;
        for (int i=0; i<classNameList.length; i++) {
            String className = classNameList[i];
            try {
                ConnectionConfig config;
                if (original!=null && original.getClass().getName().equals(className)) {
                    config = original;
                    log.debug("matched existing config object");
                    modeBox.addItem(config.name());
                    modeBox.setSelectedItem(config.name());
                } else {
                    Class cl = Class.forName(classNameList[i]);
                    config = (ConnectionConfig)cl.newInstance();
                    modeBox.addItem(config.name());
                }
                classList[n++] = config;
            } catch (NullPointerException e) {
                log.debug("Attempt to load "+classNameList[i]+" failed: "+e);
                e.printStackTrace();
            } catch (Exception e) {
                log.debug("Attempt to load "+classNameList[i]+" failed: "+e);
            }
        }
        add(modeBox);
        add(details);
        add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        modeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                selection();
            }
        });
        selection();  // first time through, pretend we've selected a value
        			  // to load the rest of the GUI
    }

    void selection() {
        int current = modeBox.getSelectedIndex();
        details.removeAll();
        // first choice is -no- protocol chosen
        if (log.isDebugEnabled()) log.debug("new selection is "+current
        							+" "+modeBox.getSelectedItem());
        if (current!=0) classList[current].loadDetails(details);
        validate();
        if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
        repaint();
    }

    public String getCurrentProtocolName() {
        int current = modeBox.getSelectedIndex();
        if (current==0) return "(none)";
        return classList[current].name();
    }
    public String getCurrentProtocolInfo() {
        int current = modeBox.getSelectedIndex();
        if (current==0) return "(none)";
        return classList[current].getInfo();
    }

    public Object getCurrentObject() {
        int current = modeBox.getSelectedIndex();
        if (current!=0) return classList[current];
        return null;
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JmrixConfigPane.class.getName());
}

