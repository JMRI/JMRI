package jmri.util.table;

import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/**
 * Extends JTable with Column Header ToolTips.
 * @author Steve Young Copyright (C) 2020
 */
public class JTableWithColumnToolTips extends JTable {
    
        private final String[] colTips;
        
        /**
         * Create a JTable with Column ToolTips.
         * @param model JTable Model to base table on.
         * @param tips String Array of Column Tool tips.
         */
        public JTableWithColumnToolTips(javax.swing.table.TableModel model, String[] tips){
            super(model);
            colTips = tips;
        }
    
        // Override JTable Header to implement table header tool tips.
        @Override
        protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
                @Override
                public String getToolTipText(MouseEvent e) {
                    try {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return colTips[realIndex];    
                    } catch (RuntimeException e1) { }
                    return null;
                }
            };
        }
    }
