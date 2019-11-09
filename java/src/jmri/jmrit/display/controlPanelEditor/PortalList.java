package jmri.jmrit.display.controlPanelEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2014
 *
 */
public class PortalList extends JList<Portal> {

    private PortalListModel _portalListModel;

    PortalList(OBlock block) {
        super();
        _portalListModel = new PortalListModel(block);
        setModel(_portalListModel);
        setCellRenderer(new PortalCellRenderer());
        setPreferredSize(new Dimension(300, 120));
        setVisibleRowCount(5);
    }

    void dataChange() {
        _portalListModel.dataChange();
    }

    private static class PortalCellRenderer extends JLabel implements ListCellRenderer<Portal>{

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Portal> list, // the list
                Portal value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // does the cell have focus
        {
            String s = value.getDescription();
            setText(s);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    static class PortalListModel extends AbstractListModel<Portal> implements PropertyChangeListener{

        OBlock _homeBlock;
        List<Portal> _list = new ArrayList<>();

        PortalListModel(OBlock block) {
            _homeBlock = block;
            makeList();
        }
        
        private void makeList() {
            for (Portal p : _list) {
                p.removePropertyChangeListener(this);
            }
            _list = _homeBlock.getPortals();
            for (Portal p : _list) {
                p.addPropertyChangeListener(this);
            }
        }

        @Override
        public int getSize() {
            return _homeBlock.getPortals().size();
        }

        @Override
        public Portal getElementAt(int index) {
            if (index < getSize()) {
                return _homeBlock.getPortals().get(index);
            }
            return null;
        }

        public void dataChange() {
            fireContentsChanged(this, 0, 0);
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (!(e.getSource() instanceof Portal)) {
                return;
            }
            makeList();
            fireContentsChanged(this, 0, 0);
        }
    }
}
