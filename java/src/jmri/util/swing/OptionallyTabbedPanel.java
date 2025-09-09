package jmri.util.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import jmri.util.swing.WrapLayout;

/**
 Class representing a JPanel that can contain and display N items in a WrapLayout.
 If N is greater than a "tabMax" parameter, the display is automatically done
 via a JTabbedPane with tabMax entries on all but the last tab.

@author Bob Jacobsen   (c) 2025

*/

public class OptionallyTabbedPanel extends JPanel {

    /**
     * @param tabMax the maximum number of items to display before creating a new tab
     */
    public OptionallyTabbedPanel(int tabMax) {
        super();
        this.tabMax = tabMax;
        
        super.add(singlePane);
        super.add(tabbedPane);
        
        currentlyTabbed = false;
        tabbedPane.setVisible(false);
        
        singlePane.setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));
    }

    private int tabMax;  // must be provided in ctor
    private ArrayList<Component> components = new ArrayList<>(); // to allow re-layout
    
    private boolean currentlyTabbed;
    
    JPanel singlePane = new JPanel();  // when displaying less than tabMax
    
    JTabbedPane tabbedPane = new JTabbedPane(); // when displaying more than tabMax
    JPanel currentTab;

    @Override
    public Component add(Component component) {

        int fullCount = components.size();
        
        if (!currentlyTabbed && fullCount < tabMax) {
            // stay in single-pane mode
            singlePane.add(component);
            components.add(component);
        } else {
            // are we in tabbed mode, or starting tabbed mode?
            if (!currentlyTabbed) {
                // just starting tabbed mode
                currentlyTabbed = true;
                singlePane.setVisible(false);
                tabbedPane.setVisible(true);        
                singlePane.removeAll();
                
                // move existing contents to a 1st pane here
                currentTab = new JPanel();
                currentTab.setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));
                tabbedPane.add(currentTab, "0-"+(tabMax-1));
                for (var item : components) {
                    currentTab.add(item);
                }
                
                // create a new tab
                currentTab = new JPanel();
                currentTab.setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));
                tabbedPane.add(currentTab, ""+fullCount+"-"+(fullCount+1));
                
                // and add to that new tab
                currentTab.add(component);
                components.add(component);
                
                updateTabLabel();
                
            } else {
                int paneCount = currentTab.getComponentCount();
                if (paneCount >= tabMax) {
                    // create a new tabbedPane
                    currentTab = new JPanel();
                    currentTab.setLayout(new WrapLayout(FlowLayout.CENTER, 2, 2));
                    tabbedPane.add(currentTab, ""+fullCount+"-"+(fullCount+1));
                    // and add to that new tab
                    currentTab.add(component);
                    components.add(component);

                    updateTabLabel();

                } else {
                    // stay in tab for now
                    currentTab.add(component);
                    components.add(component);

                    updateTabLabel();
                }
            }
        }
        
        return component;  // returns component argument
    }

    // Update the label on the current tab to the proper numbers
    void updateTabLabel() {
        int numTab = tabbedPane.getComponentCount()-1; // addressing is zero based
        int first = numTab*tabMax;
        int last = numTab*tabMax+currentTab.getComponentCount()-1;
        if (numTab == 0) last = last + 1;  // first tab includes 0
        tabbedPane.setTitleAt(numTab, ""+first+" - "+last);
    }
    
    @Override
    public void removeAll() {
        // reset to single pane mode
        currentlyTabbed = false;
        components = new ArrayList<>();
        
        for (var pane : tabbedPane.getComponents()) {
            if (pane instanceof Container) ((Container)pane).removeAll();
        }
        tabbedPane.removeAll();
        singlePane.removeAll();

    }

}
