package jmri.jmrit.display;

import jmri.*;
import jmri.jmrit.catalog.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.sun.java.util.collections.*;

/**
 * Provides a simple editor for selecting N NamedIcons, perhaps
 * for use in creating a panel icon (see SensorIcon for an item
 * that might want to have that type of information, and
 * PanelEditor for an example of how to use this)
 *
 * <p>Copyright: Copyright (c) 2003</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.1 $
 * @see jmri.jmrit.display.SensorIcon
 * @see jmri.jmrit.display.PanelEditor
 * @see jmri.jmrit.catalog
 */

public class MultiIconEditor extends JPanel {

    JButton[] buttonList;
    NamedIcon[] iconList;

    public CatalogPane catalog = new CatalogPane();

    public MultiIconEditor(int nIcons) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        buttonList = new JButton[nIcons];
        iconList = new NamedIcon[nIcons];

    }

    public void setIcon(int iconNum, String label, String name) {
        iconList[iconNum] = new NamedIcon(name, name);
        // make a button to change that icon
        JButton j = new IconButton(iconNum);
        buttonList[iconNum] = j;

        // and add it to this panel
        JPanel p = new JPanel();
        p.add(new JLabel(label));
        p.add(j);
        this.add(p);
    }

    public NamedIcon getIcon(int iconNum) {
        return iconList[iconNum];
    }

    public void complete() {
        // add the catalog, so icons can be selected
        this.add(catalog);
    }


    private class IconButton extends JButton {
        IconButton(int index) {
            super(iconList[index]);
            savedIndex = index;
            addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        pickIcon();
                    }
                }
                                        );
        }

        int savedIndex;

        void pickIcon() {
            NamedIcon newIcon = catalog.getSelectedIcon();
            iconList[savedIndex] = newIcon;
            buttonList[savedIndex].setIcon(newIcon);
        }
    }

    /**
     * Clean up when its time to make it all go away
     */
    public void dispose() {
        // clean up GUI aspects
        this.removeAll();
        iconList = null;
        buttonList = null;
        catalog = null;
    }
}
