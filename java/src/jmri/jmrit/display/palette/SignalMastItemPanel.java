package jmri.jmrit.display.palette;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.JButton;
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
    private JPanel _blurb;

    public SignalMastItemPanel(DisplayFrame parentFrame, String type, String family, PickListModel<jmri.SignalMast> model) {
        super(parentFrame, type, family, model);
    }

    @Override
    public void init() {
        if (!_initialized) {
            _blurb = instructions();
            super.init();
            _iconFamilyPanel.add(_blurb);
            add(_iconFamilyPanel, 1);
            _showIconsButton.setEnabled(_mast != null);
        }
    }

    @Override
    public void init(ActionListener doneAction, HashMap<String, NamedIcon> iconMap) {
        super.init(doneAction, iconMap);
        remove(_iconFamilyPanel);
    }

    @Override
    protected void makeFamiliesPanel() {
        addIconsToPanel(_currentIconMap, _iconPanel, false); // need to have family iconMap identified before calling

        if (!_suppressDragging) {
            makeDragIconPanel();
            makeDndIcon(_currentIconMap);
        }
        addFamilyPanels(true);
    }

    @Override
    protected JPanel makeBottomPanel(ActionListener doneAction) {
        JPanel panel = new JPanel();
        _showIconsButton = new JButton(Bundle.getMessage("ShowIcons"));
        _showIconsButton.addActionListener(a -> {
            if (_iconPanel.isVisible()) {
                hideIcons();
            } else {
                showIcons();
            }
        });
        _showIconsButton.setToolTipText(Bundle.getMessage("ToolTipShowIcons"));
        panel.add(_showIconsButton);
        _bottom1Panel = new JPanel(new FlowLayout());
        _bottom1Panel.add(panel);
        if (doneAction != null) {
            _bottom1Panel.add(makeUpdateButton(doneAction));
        }
//        initIconFamiliesPanel(); // (if null: creates and) adds a new _iconFamilyPanel for the new mast map
        return _bottom1Panel;
    }

    private void makeIconMap() {
        if (_mast == null) {
            _currentIconMap = null;
            _showIconsButton.setEnabled(false);
            return;
        }
        _showIconsButton.setEnabled(true);
        _family = _mast.getSignalSystem().getSystemName();
        _currentIconMap = new HashMap<>();
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

    @Override
    protected void setFamily() {
        if (!_suppressDragging) {
            makeDragIconPanel();
            makeDndIcon(_currentIconMap); // empty key OK, this uses getDragIcon()
        }
        if (_currentIconMap != null) {
            addIconsToPanel(_currentIconMap, _iconPanel, false);
        }
    }

    @Override
    protected void showIcons() {
        if (!_update) {
            _blurb.setVisible(false);
            _blurb.invalidate();
        }
        super.showIcons();
    }
    
    @Override
    protected void hideIcons() {
        if (!_update) {
            _blurb.setVisible(true);
            _blurb.invalidate();
        }
        super.hideIcons();
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
