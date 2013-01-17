// ManagerDefaultsConfigPane.java

package apps;

import javax.swing.*;

import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jmri.managers.ManagerDefaultSelector;

/**
 * Provide GUI to configure InstanceManager defaults.
 * <P>
 *
 * @author      Bob Jacobsen   Copyright (C)  2010
 * @version	$Revision$
 * @since 2.9.5
 */
public class ManagerDefaultsConfigPane extends jmri.util.swing.JmriPanel {

    public ManagerDefaultsConfigPane() {
    
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        matrix = new JPanel();
        add(matrix);
        ManagerDefaultSelector.instance.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent e) {
                if(e.getPropertyName().equals("Updated")){
                    update();
                }
            }
        });
        update();
    }
    
    JPanel matrix;
    
    /**
     * Invoke when first displayed to load
     * and present options
     */
    public void update() {
        matrix.removeAll();
        
        // this doesn't find non-migrated systems, how do we handle that eventually?
        List<Object> connList = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
        if (connList!=null){
            reloadConnections(connList);
        } else {
            matrix.add(new JLabel("No new-form system connections configured"));
        }
    }
    
    void reloadConnections(List<Object> connList) {
        matrix.setLayout(new jmri.util.javaworld.GridLayout2(connList.size()+1, ManagerDefaultSelector.instance.knownManagers.length+1));
        matrix.add(new JLabel(""));
        
        for (ManagerDefaultSelector.Item item : ManagerDefaultSelector.instance.knownManagers) {
            matrix.add(new JLabel(item.typeName));
        }
        groups = new ButtonGroup[ManagerDefaultSelector.instance.knownManagers.length];
        for (int i = 0; i<ManagerDefaultSelector.instance.knownManagers.length; i++) groups[i] = new ButtonGroup();
        for (int x = 0; x<connList.size(); x++){
            jmri.jmrix.SystemConnectionMemo memo = (jmri.jmrix.SystemConnectionMemo)connList.get(x);
            String name = memo.getUserName();
            matrix.add(new JLabel(name));
            int i = 0;
            for (ManagerDefaultSelector.Item item : ManagerDefaultSelector.instance.knownManagers) {
                if (memo.provides(item.managerClass)) {
                    JRadioButton r = new SelectionButton(name, item.managerClass);
                    matrix.add(r);
                    groups[i].add(r);
                    if (x == connList.size()-1 && ManagerDefaultSelector.instance.getDefault(item.managerClass)==null) {
                    	r.setSelected(true);
                    }
                } else {
                    // leave a blank
                    JRadioButton r = new JRadioButton();
                    r.setEnabled(false);
                    matrix.add(r);
                }
                i++; //we need to increment 'i' as we are going onto the next group even if we added a blank button
            }
        }
        revalidate();
        
    }
    
    ButtonGroup[] groups;
    
    /**
     * Captive class to track changes
     */
    static class SelectionButton extends JRadioButton {
        SelectionButton(String name, Class<?> managerClass) {
            super();
            this.managerClass = managerClass;
            this.name = name;
            
            if (name.equals(ManagerDefaultSelector.instance.getDefault(managerClass))) 
                this.setSelected(true);

            addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (isSelected())
                        ManagerDefaultSelector.instance.setDefault(SelectionButton.this.managerClass, SelectionButton.this.name);
                }
            });

        }
        String name;
        Class<?> managerClass;
        @Override
        public void setSelected(boolean t) {
            super.setSelected(t);
            if (t)
                ManagerDefaultSelector.instance.setDefault(this.managerClass, this.name);
        }
    }
}

