package jmri.jmrit.display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jmri.jmrit.catalog.CatalogPane;
import com.sun.java.util.collections.ArrayList;

/**
 * Provides a simple editor for adding jmri.jmrit.display items
 * to a captive JFrame
 * <P>GUI is structured as a band of common parameters across the
 * top, then a series of things you can add. Unfortunately, I don't
 * know how to automatically get item widths when using a null layout
 * (Specific location) layout manager, so you have to specify these.
 * <P>
 * All created objects are put at the "front", ahead of everything that
 * is already there, except the background, which clearly has to be put at
 * the back.
 * <P>
 * The "contents" List keeps track of all the objects added to the target
 * frame for later manipulation.
 *
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.6 $
 */

public class PanelEditor extends JPanel {

    JTextField nextX = new JTextField("20",4);
    JTextField nextY = new JTextField("30",4);

    JButton labelAdd = new JButton("add");
    JTextField nextLabel = new JTextField(10);

    JButton iconAdd = new JButton("add");
    JButton pickIcon = new JButton("pick");
    JLabel nextIconLabel = new JLabel();
    Icon labelIcon = null;
    String labelIconName;

    JButton turnoutAdd = new JButton("add");
    JTextField nextTurnout = new JTextField(10);

    JButton closedIconButton = new JButton("Pick closed icon");
    Icon closedIcon;
    String closedIconName;
    JLabel closedIconLabel;

    JButton thrownIconButton = new JButton("Pick thrown icon");
    Icon thrownIcon;
    String thrownIconName;
    JLabel thrownIconLabel;

    JButton sensorAdd = new JButton("add");
    JTextField nextSensor = new JTextField(10);

    JButton activeIconButton = new JButton("Pick active icon");
    Icon activeIcon;
    String activeIconName;
    JLabel activeIconLabel;

    JButton inactiveIconButton = new JButton("Pick inactive icon");
    Icon inactiveIcon;
    String inactiveIconName;
    JLabel inactiveIconLabel;

    JButton backgroundAddButton = new JButton("Pick image");

    public CatalogPane catalog = new CatalogPane();

    public PanelEditor() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // common items
        JPanel common = new JPanel();
        common.setLayout(new FlowLayout());
        common.add(new JLabel(" x:"));
        common.add(nextX);
        common.add(new JLabel(" y:"));
        common.add(nextY);
        this.add(common);

        this.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        // add a background image
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(new JLabel("add background image: "));
            panel.add(backgroundAddButton);
            panel.add(labelAdd);
            backgroundAddButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addBackground();
                    }
                }
            );
            this.add(panel);
        }

        // add a text label
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(new JLabel("add label: "));
            panel.add(nextLabel);
            panel.add(labelAdd);
            labelAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addLabel();
                    }
                }
            );
            this.add(panel);
        }

        this.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        // add an icon label
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(new JLabel("add icon: "));
            panel.add(nextIconLabel);
            panel.add(pickIcon);
            pickIcon.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        pickLabelIcon();
                    }
                }
            );
            panel.add(iconAdd);
            iconAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addIcon();
                    }
                }
            );
            this.add(panel);
        }

        this.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        // Add a turnout indicator
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            TurnoutIcon to = new TurnoutIcon();
            closedIcon = to.getClosedIcon();
            thrownIcon = to.getThrownIcon();
            closedIconLabel = new JLabel(closedIcon);
            thrownIconLabel = new JLabel(thrownIcon);

            panel.add(new JLabel("indicate turnout: "));
            panel.add(nextTurnout);
            panel.add(closedIconLabel);
            panel.add(closedIconButton);
            closedIconButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        pickClosedIcon();
                    }
                }
            );
            panel.add(thrownIconLabel);
            panel.add(thrownIconButton);
            thrownIconButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        pickThrownIcon();
                    }
                }
            );
            panel.add(turnoutAdd);
            turnoutAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addTurnout();
                    }
                }
            );
            this.add(panel);
        }

        // Add a sensor indicator
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            SensorIcon to = new SensorIcon();
            activeIcon = to.getActiveIcon();
            inactiveIcon = to.getInactiveIcon();
            activeIconLabel = new JLabel(activeIcon);
            inactiveIconLabel = new JLabel(inactiveIcon);

            panel.add(new JLabel("indicate sensor: "));
            panel.add(nextSensor);
            panel.add(activeIconLabel);
            panel.add(activeIconButton);
            activeIconButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        pickActiveIcon();
                    }
                }
            );
            panel.add(inactiveIconLabel);
            panel.add(inactiveIconButton);
            inactiveIconButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        pickInactiveIcon();
                    }
                }
            );
            panel.add(sensorAdd);
            sensorAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addSensor();
                    }
                }
            );
            this.add(panel);
        }

        // allow for selection of icons
        add(catalog);

    }  // end ctor

    /**
     * Select the icon for an icon label
     */
    void pickLabelIcon() {
        labelIcon = pickIcon();
        labelIconName = pickIconName();
        nextIconLabel.setIcon(labelIcon);
    }

    /**
     * Select the icon for "thrown"
     */
    void pickThrownIcon() {
        thrownIcon = pickIcon();
        thrownIconName = pickIconName();
        thrownIconLabel.setIcon(thrownIcon);
    }

    /**
     * Select the image for "closed"
     */
    void pickClosedIcon() {
        closedIcon = pickIcon();
        closedIconName = pickIconName();
        closedIconLabel.setIcon(closedIcon);
    }

    /**
     * Select the icon for "active"
     */
    void pickActiveIcon() {
        activeIcon = pickIcon();
        activeIconName = pickIconName();
        activeIconLabel.setIcon(activeIcon);
    }

    /**
     * Select the image for "inactive"
     */
    void pickInactiveIcon() {
        inactiveIcon = pickIcon();
        inactiveIconName = pickIconName();
        inactiveIconLabel.setIcon(inactiveIcon);
    }

    /**
     * Select and create and image
     */
    Icon pickIcon() {
        return catalog.getSelectedIcon();
    }

    String pickIconName() {
        return catalog.getSelectedIconName();
    }

    /**
     * Button pushed, add a background image (do this early!)
     */
    void addBackground() {
        JFileChooser inputFileChooser = new JFileChooser(" ");
		int retVal = inputFileChooser.showOpenDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        log.debug("Open image file: "+inputFileChooser.getSelectedFile().getPath());
		ImageIcon icon = new ImageIcon(inputFileChooser.getSelectedFile().getPath());
        JLabel l = new PositionableLabel(icon, "another sensor icon name");
        l.setSize(icon.getIconWidth(), icon.getIconHeight());
        target.add(l);
        contents.add(l);
        backgroundAddButton.setEnabled(false);   // theres only one
        target.moveToBack(l);
        target.revalidate();
    }

    /**
     * Add a turnout indicator to the target
     */
    void addTurnout() {
        TurnoutIcon l = new TurnoutIcon();
        if (closedIcon!=null) l.setClosedIcon(closedIcon);
        if (thrownIcon!=null) l.setThrownIcon(thrownIcon);
        l.setTurnout(nextTurnout.getText());

        log.debug("turnout height, width: "+l.getHeight()+" "+l.getWidth());
        setNextLocation(l);
        l.invalidate();
        target.add(l);
        contents.add(l);
        target.moveToFront(l);

        // reshow the panel
        target.validate();
    }

    /**
     * Add a turnout indicator to the target
     */
    void addSensor() {
        SensorIcon l = new SensorIcon();
        if (activeIcon!=null) l.setActiveIcon(activeIcon);
        if (inactiveIcon!=null) l.setInactiveIcon(inactiveIcon);
        l.setSensor(nextSensor.getText());

        log.debug("sensor height, width: "+l.getHeight()+" "+l.getWidth());
        setNextLocation(l);
        l.invalidate();
        target.add(l);
        contents.add(l);
        target.moveToFront(l);

        // reshow the panel
        target.validate();
    }

    /**
     * Add a label to the target
     */
    void addLabel() {
        JComponent l = new PositionableLabel(nextLabel.getText());
        setNextLocation(l);
        l.invalidate();
        target.add(l);
        contents.add(l);
        target.moveToFront(l);

        // reshow the panel
        target.validate();
        log.debug("label height, width: "+l.getHeight()+" "+l.getWidth());
    }

    /**
     * Add an icon to the target
     */
    void addIcon() {
        PositionableLabel l = new PositionableLabel(labelIcon, labelIconName );
        setNextLocation(l);
        l.invalidate();
        target.add(l);
        contents.add(l);
        target.moveToFront(l);

        // reshow the panel
        target.validate();
    }

    /**
     * Set an objects location and size as it's created.
     * Size comes from the preferredSize; location comes
     * from the fields where the user can spec it.
     */
    void setNextLocation(JComponent obj) {
        int x = Integer.parseInt(nextX.getText());
        int y = Integer.parseInt(nextY.getText());
        obj.setBounds(x,y,obj.getPreferredSize().width,obj.getPreferredSize().height);
    }

    /**
     * Set the JLayeredPane containing the objects to be editted.
     */
    public void setTarget(JLayeredPane f) {
        target = f;
    }
    public JLayeredPane getTarget() { return target;}
    JLayeredPane target;

    /**
     * Get the frame containing the results
     */
    public JFrame getFrame() { return frame; }
    public void setFrame(JFrame f) {
        frame = f;
    }
    JFrame frame;

    public ArrayList contents = new ArrayList();

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditor.class.getName());
}