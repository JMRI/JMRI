package jmri.jmrix.can.cbus.swing.modules;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.*;


/**
 * Class to display CBUS command station flag settings
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CmdStaFlags extends JPanel {
        
    protected int _index;
    protected String _title;
    protected int flags;
    protected JRadioButton [] buttons;
    protected UpdateNV _flagUpdateFn;

    /**
     * 
     * @param index of the flags, not the NV array index which may be offset
     * @param title of the flags object
     * @param flagStrings array of strings to name each flag bit
     * @param flagTtStrings array of tooltip strings for each flag bit
     * @param update the callback function to update the table data model
     */
    public CmdStaFlags(int index, String title, String [] flagStrings, String [] flagTtStrings, UpdateNV update) {
        super();

        _index = index;
        _title = title;
        _flagUpdateFn = update;

        buttons = new JRadioButton[8];
        for (int i = 0; i < 8; i++) {
            buttons[i] = new JRadioButton(flagStrings[i]);
            buttons[i].setToolTipText(flagTtStrings[i]);
            buttons[i].addActionListener((ActionEvent e) -> {
                flagActionListener();
            });
        }
    }

    /**
     * Get the panel to display the flags
     * 
     * @return JPanel displaying the flags
     */
    public JPanel getContents() {

        JPanel gridPane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder t = BorderFactory.createTitledBorder(border, _title);
        gridPane.setBorder(t);

        for (int i = 0; i < 8; i++) {
            gridPane.add(buttons[i], c);
            c.gridy++;
        }
        setButtons();

        return gridPane;
    }

    /**
     * Call the callback to update from flags state.
     */
    protected void flagActionListener() {
        int value = buttons[7].isSelected() ? 1 : 0;
        for (int i = 6; i >= 0; i--) {
            value = (value << 1) + (buttons[i].isSelected() ? 1 : 0);
        }
        setFlags(value);
        _flagUpdateFn.setNewVal(_index);
    }

    /**
     * Update the flags settings
     * 
     * @param value settings
     */
    public void setFlags(int value) {
        flags = value;
        setButtons();
    }

    /**
     * Get the flags settings
     * 
     * @return flags as an int
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Set the buttons to the state of the flags
     */
    protected void setButtons() {
        for (int i = 0; i < 8; i++) {
            if ((flags & (1<<i)) > 0) {
                buttons[i].setSelected(true);
            } else {
                buttons[i].setSelected(false);
            }
        }
    }

}
