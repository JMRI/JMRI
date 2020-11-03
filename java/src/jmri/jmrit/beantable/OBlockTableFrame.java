package jmri.jmrit.beantable;

import jmri.jmrit.beantable.BeanTableFrame;
import jmri.jmrit.logix.OBlock;

import javax.swing.*;

/**
 * Override to place OBlock panes in tabbed interface.
 * @see jmri.jmrit.beantable.oblock.TableFrames
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse copyright (c) 2020
 */
public class OBlockTableFrame extends BeanTableFrame<OBlock> {

    OBlockTablePanel oblockPanel;

    public OBlockTableFrame(OBlockTablePanel panel, String helpTarget) {

        super();

        oblockPanel = panel;

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.add(new jmri.configurexml.StoreMenu());
        fileMenu.add(panel.getPrintItem());
        menuBar.add(fileMenu);

        menuBar.add(panel.getOptionMenu());
        setJMenuBar(menuBar); // TODO configure to show up on oblock.TableFrames

        addHelpMenu(helpTarget, true);

        // install items in GUI
        getContentPane().add(oblockPanel);
        bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue()); // stays at end of box
        bottomBoxIndex = 0;

        getContentPane().add(bottomBox);

        // add extras, if desired by subclass
        extras();

    }

    @Override
    public void dispose() {
        if (oblockPanel != null) {
            oblockPanel.dispose();
        }
        super.dispose();
    }

}
