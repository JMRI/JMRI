package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;

import jmri.Programmer;

/**
 * Holds a table of the available factory resets available for a particular
 * decoder.
 *
 * @author Howard G. Penny Copyright (C) 2005
 */
public class ResetTableModel extends ExtraMenuTableModel {

    public ResetTableModel(JLabel status, Programmer pProgrammer) {
        super(status, pProgrammer);
    }
}
