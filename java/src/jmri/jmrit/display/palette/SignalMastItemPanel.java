package jmri.jmrit.display.palette;

import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import jmri.SignalAppearanceMap;
import jmri.SignalMast;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TableItemPanel extension for placing of SignalMast items with a fixed set of icons.
 *
 * @author Pete Cressman Copyright (c) 2010, 2011, 2020
 * @author Egbert Broerse 2017
 */
public class SignalMastItemPanel extends TableItemPanel<SignalMast> {

    private SignalMast _mast;

    public SignalMastItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<jmri.SignalMast> model) {
        super(parentFrame, type, family, model);
        _currentIconMap = new HashMap<>();
    }

    @Override
    public void init() {
        super.init();
        _showIconsButton.setEnabled(_mast != null);
    }

    @Override
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        super.init(doneAction, iconMap);
    }

    @Override
    protected JPanel makeItemButtonPanel() {
        JPanel panel = new JPanel();
        panel.add(makeShowIconsButton());
        return panel;
    }

    @Override
    protected JPanel makeSpecialBottomPanel(boolean update) {
        JPanel panel = new JPanel();
        panel.add(makeShowIconsButton());
        return panel;
    }

    private void makeIconMap() {
        if (_mast == null) {
            _currentIconMap.clear();
            _showIconsButton.setEnabled(false);
            return;
        }
        _showIconsButton.setEnabled(true);
        _family = _mast.getSignalSystem().getSystemName();
        SignalAppearanceMap appMap = _mast.getAppearanceMap();
        Enumeration<String> e = _mast.getAppearanceMap().getAspects();
        while (e.hasMoreElements()) {
            String aspect = e.nextElement();
            String s = appMap.getImageLink(aspect, _family);
            if (s !=null && !s.equals("")) {
                if (!s.contains("preference:")) {
                    s = s.substring(s.indexOf("resources"));
                }
                NamedIcon n = new NamedIcon(s, s);
                _currentIconMap.put(aspect, n);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("makeIconMap for {}  size= {}", _family, _currentIconMap.size());
        }
    }

    /**
     * ListSelectionListener action.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        _mast = getDeviceNamedBean();
        makeIconMap();
        super.valueChanged(e);
        updateFamiliesPanel();
        setFamily();
        if (log.isDebugEnabled()) {
            log.debug("table valueChanged for= {}, row = {}", _itemType, _table.getSelectedRow());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastItemPanel.class);

}
