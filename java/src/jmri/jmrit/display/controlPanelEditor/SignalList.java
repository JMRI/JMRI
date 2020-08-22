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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2020
 *
 */
public class SignalList extends JList<SignalPair> {

    private final SignalListModel _signalListModel;

    SignalList(OBlock block, EditFrame parent) {
        super();
        _signalListModel = new SignalListModel(block, parent);
        setModel(_signalListModel);
        setCellRenderer(new SignalCellRenderer());
        setPreferredSize(new Dimension(300, 120));
        setVisibleRowCount(5);
    }

    void dataChange() {
        _signalListModel.dataChange();
    }

    void setSelected(Portal portal) {
        for (SignalPair sp : _signalListModel._list) {
            if (sp._portal.equals(portal)) {
                setSelectedValue(sp, true);
                return;
            }
        }
        clearSelection();
    }

    private static class SignalCellRenderer extends JLabel implements ListCellRenderer<SignalPair>{

        @Override
        public Component getListCellRendererComponent(
                JList<? extends SignalPair> list, // the list
                SignalPair value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // does the cell have focus
        {
            setText(value.getDiscription());
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

    static class SignalListModel extends AbstractListModel<SignalPair> implements PropertyChangeListener {

        OBlock _homeBlock;
        private final EditFrame _parent;
        List<SignalPair> _list = new ArrayList<>();

        SignalListModel(OBlock block, EditFrame parent) {
            _homeBlock = block;
            _parent = parent;
            _homeBlock.addPropertyChangeListener(this);
            makeList();
        }

        private void makeList() {
            for (SignalPair sp : _list) {
                sp._portal.removePropertyChangeListener(this);
            }
            _list.clear();
            for (Portal portal : _homeBlock.getPortals()) {
                NamedBean signal = portal.getSignalProtectingBlock(_homeBlock);
                if (signal != null) {
                    _list.add(new SignalPair(signal, portal));
                }
            }
            for (SignalPair sp : _list) {
                sp._portal.addPropertyChangeListener(this);
            }
        }

        @Override
        public int getSize() {
            return _list.size();
        }

        @Override
        public SignalPair getElementAt(int index) {
            if (index < getSize()) {
                return _list.get(index);
            }
            return null;
        }

        public void dataChange() {
            fireContentsChanged(this, 0, 0);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            Object source = e.getSource();
            String property = e.getPropertyName();
            if (log.isDebugEnabled()) {
                log.debug("property = {} source= {}", property, source.getClass().getName());                
            }
            if (source instanceof OBlock && property.equals("deleted")) {
                _homeBlock.removePropertyChangeListener(this);
                _parent.closingEvent(true);
            } else {
                makeList();
                fireContentsChanged(this, 0, 0);
                if (property.equals("signalChange") || property.equals("NameChange")) {
                    _parent.clearListSelection();
                }
            }
        }
    }
    private final static Logger log = LoggerFactory.getLogger(SignalList.class);
}
