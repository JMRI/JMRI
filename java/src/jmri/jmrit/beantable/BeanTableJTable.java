package jmri.jmrit.beantable;

import java.awt.event.KeyEvent;

import jmri.NamedBean;

/**
 * JTable for displaying a BeanTableDataModel.
 * Code originally within BeanTableDataModel.
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2007
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021, 2024
 * @param <T> Bean type, e.g. Sensor or Turnout.
 */
public class BeanTableJTable<T extends NamedBean> extends javax.swing.JTable {

    private final BeanTableDataModel<T> model;

    public BeanTableJTable(BeanTableDataModel<T> beanTableModel) {
        super(beanTableModel);
        model = beanTableModel;
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent e) {
        java.awt.Point p = e.getPoint();
        int modelRowIndex = convertRowIndexToModel(rowAtPoint(p));
        int modelColumnIndex = convertColumnIndexToModel(columnAtPoint(p));
        return model.getCellToolTip(this, modelRowIndex, modelColumnIndex);
    }

    /**
     * Disable Windows Key or Mac Meta Keys being pressed acting
     * as a trigger for editing the focused cell.
     * Causes unexpected behaviour, i.e. button presses.
     * {@inheritDoc}
     */
    @Override
    public boolean editCellAt(int row, int column, java.util.EventObject e) {
        if ( (e instanceof KeyEvent) &&
            ( ((KeyEvent) e).getKeyCode() == KeyEvent.VK_WINDOWS ||
                ( (KeyEvent) e).getKeyCode() == KeyEvent.VK_META )){
            return false;
        }
        return super.editCellAt(row, column, e);
    }

}
