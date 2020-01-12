package jmri.jmrit.display.layoutEditor;

import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Provides a simple editor for selecting N NamedIcons, perhaps for use in
 * creating a panel icon.
 * <p>
 * See {@link jmri.jmrit.display.SensorIcon} for an item that might want to have
 * that type of information, and
 * {@link jmri.jmrit.display.layoutEditor.LayoutEditor} for an example of how to
 * use this.
 *
 * @author Bob Jacobsen Copyright (c) 2003
 * @see jmri.jmrit.display.SensorIcon
 * @see jmri.jmrit.display.layoutEditor.LayoutEditor
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
        JButton j = new IconButton(iconNum, iconList[iconNum]);
        j.setToolTipText(iconList[iconNum].getName());
        buttonList[iconNum] = j;

        // and add it to this panel
        JPanel p = new JPanel();
        p.add(new JLabel(label));
        p.add(j);
        this.add(p);
    }

    /**
     * Returns a new NamedIcon object for your own use.
     *
     * @param iconNum 0 to n-1
     * @return Unique object
     */
    public NamedIcon getIcon(int iconNum) {
        return new NamedIcon(iconList[iconNum]);
    }

    public void complete() {
        // add the catalog, so icons can be selected
        this.add(catalog);
    }

    private class IconButton extends JButton {

        IconButton(int index, Icon init) {  // init icon passed to avoid ref before ctor complete
            super(init);
            savedIndex = index;
            addActionListener((ActionEvent a) -> {
                pickIcon();
            });
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
