package jmri.jmrit.signalling.entryexit;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;


import java.awt.event.*;
import javax.swing.*;
import java.util.Hashtable;

import jmri.jmrit.signalling.EntryExitPairs;


public class StackNXPanel extends JPanel{

    transient EntryExitPairs manager = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);
    
    private JPanel entryExitPanel;
    
    private DefaultListModel listModel;
    private JList list = new JList();
    JScrollPane listScrollPane = new JScrollPane(list);
    
    public StackNXPanel(){
        super();
        initGUI();
    }
    
    private void initGUI()
    {
        listModel = new DefaultListModel();
        setLayout(new BorderLayout());
        entryExitPanel = new JPanel();
        entryExitPanel.setDoubleBuffered(true);
        entryExitPanel.setLayout(new BoxLayout(entryExitPanel, BoxLayout.Y_AXIS));
        entryExitPanel.add(listScrollPane);
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                //This method can be called only if
                //there's a valid selection
                //so go ahead and remove whatever's selected.
                manager.cancelStackedRoute(listToDest.get(list.getSelectedValue()), false);
            }
        
        });
        add(cancelButton,BorderLayout.SOUTH);
        add(entryExitPanel,BorderLayout.CENTER);
        
        updateGUI();
    }
    
    Hashtable<String, DestinationPoints> listToDest = new Hashtable<String, DestinationPoints>();
    
    public void updateGUI() {
        listModel.clear();
        listToDest = new Hashtable<String, DestinationPoints>();
        for(DestinationPoints dp:manager.getStackedInterlocks()){
            listToDest.put(dp.getDisplayName(), dp);
            listModel.addElement(dp.getDisplayName());
        }
        list.setModel(listModel);
        list.setVisibleRowCount(10);
    }
}