package jmri.jmrit.display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Provides a simple editor for adding jmri.jmrit.display items
 * to a captive JFrame
 * <P>GUI is structured as a band of common parameters across the
 * top, then a series of things you can add. Unfortunately, I don't
 * know how to automatically get item widths when using a null layout
 * (Specific location) layout manager, so you have to specify these.
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Id: PanelEditor.java,v 1.3 2002-06-26 03:43:06 jacobsen Exp $
 */

public class PanelEditor extends JPanel {

    JTextField nextX = new JTextField("20",4);
    JTextField nextY = new JTextField("30",4);
    JTextField nextHeight = new JTextField("40",4);
    JTextField nextWidth = new JTextField("90",4);

    JButton labelAdd = new JButton("add");
    JTextField nextLabel = new JTextField(10);

    JButton iconAdd = new JButton("add");
    JButton pickIcon = new JButton("pick");
    JLabel nextIconLabel = new JLabel();
    Icon labelIcon = null;

    JButton turnoutAdd = new JButton("add");
    JTextField nextTurnout = new JTextField(10);
    JButton closedIconButton = new JButton("Pick closed icon");
    Icon closedIcon = null;
    JLabel closedIconLabel = new JLabel();
    JButton thrownIconButton = new JButton("Pick thrown icon");
    Icon thrownIcon = null;
    JLabel thrownIconLabel = new JLabel();

    public PanelEditor() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // common items
        JPanel common = new JPanel();
        common.setLayout(new FlowLayout());
        common.add(new JLabel(" x:"));
        common.add(nextX);
        common.add(new JLabel(" y:"));
        common.add(nextY);
        common.add(new JLabel(" height:"));
        common.add(nextHeight);
        common.add(new JLabel(" width:"));
        common.add(nextWidth);
        this.add(common);

        this.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

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

        // add an icon
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

    }  // end ctor

    /**
     * Select the icon for an icon label
     */
    void pickLabelIcon() {
        labelIcon = pickIcon();
        nextIconLabel.setIcon(labelIcon);
    }

    /**
     * Select the icon for "thrown"
     */
    void pickThrownIcon() {
        thrownIcon = pickIcon();
        thrownIconLabel.setIcon(thrownIcon);
    }

    /**
     * Select the image for "closed"
     */
    void pickClosedIcon() {
        closedIcon = pickIcon();
        closedIconLabel.setIcon(closedIcon);
    }

    /**
     * Select and create and image
     */
    Icon pickIcon() {
        JFileChooser inputFileChooser = new JFileChooser(" ");
		//inputFileChooser.setSelectedFile(new File("lnpacket.hex"));
		int retVal = inputFileChooser.showOpenDialog(this);
		if (retVal != JFileChooser.APPROVE_OPTION) return null;  // give up if no file selected
        log.debug("Open image file: "+inputFileChooser.getSelectedFile().getPath());
		return new ImageIcon(inputFileChooser.getSelectedFile().getPath());
    }

    /**
     * Add a turnout indicator to the target
     */
    void addTurnout() {
        TurnoutIcon l = new TurnoutIcon();
        if (closedIcon!=null) l.setClosedIcon(closedIcon);
        if (thrownIcon!=null) l.setThrownIcon(thrownIcon);
        l.setTurnout(nextTurnout.getText());

        setNextLocation(l);
        l.invalidate();
        target.add(l);

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

        // reshow the panel
        target.validate();
    }

    /**
     * Add an icon to the target
     */
    void addIcon() {
        JComponent l = new PositionableLabel(labelIcon);
        setNextLocation(l);
        l.invalidate();
        target.add(l);

        // reshow the panel
        target.validate();
    }

    /**
     * Get a Point object representing where the next item
     * is to be located
     */
    void setNextLocation(JComponent obj) {
        int x = Integer.parseInt(nextX.getText());
        int y = Integer.parseInt(nextY.getText());
        int h = Integer.parseInt(nextHeight.getText());
        int w = Integer.parseInt(nextWidth.getText());
        obj.setBounds(x,y,h,w);
    }

    /**
     * Set the frame to be editted.
     */
    public void setTarget(JPanel f) {
        target = f;
    }

    JPanel target;

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditor.class.getName());
}