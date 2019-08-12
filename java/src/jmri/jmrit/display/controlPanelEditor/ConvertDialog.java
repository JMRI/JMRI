package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.display.palette.FamilyItemPanel;
import jmri.jmrit.display.palette.IndicatorItemPanel;
import jmri.jmrit.display.palette.IndicatorTOItemPanel;
import jmri.jmrit.logix.OBlock;
import jmri.util.PlaceWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConvertDialog extends JDialog {

        private CircuitBuilder _parent;
        private PositionableLabel _pos;
        FamilyItemPanel _panel;
        DisplayFrame _filler;
        java.awt.Point location;

        ConvertDialog(CircuitBuilder cb, PositionableLabel pos, OBlock block) {
            super(cb._editor, true);
            _parent = cb;
            _pos = pos;
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _parent._editor.highlight(null);
                }
            });
            if (pos == null) {
                dispose();
                return;
            }
            _filler = pos.makePaletteFrame("Dummy");
            String title;
            ActionListener updateAction;
            if (pos instanceof TurnoutIcon) {
                title = "IndicatorTO";
                _panel = new IndicatorTOItemPanel(_filler, title, null, null, cb._editor) {
                    @Override
                    protected void showIcons() {
                         super.showIcons();
                         displayIcons();
                    }
                    @Override
                    protected void hideIcons() {
                        super.hideIcons();
                        displayIcons();
                    }
                };
                updateAction = (ActionEvent a) -> {
                    convertTO(block);
                };
            } else {
                title = "IndicatorTrack";
                _panel = new IndicatorItemPanel(_filler, title, null, cb._editor) {
                    @Override
                    protected void showIcons() {
                        super.showIcons();
                        displayIcons();
                    }
                    @Override
                    protected void hideIcons() {
                        super.hideIcons();
                        displayIcons();
                    }
                };
                updateAction = (ActionEvent a) -> {
                    convertSeg(block);
                };
            }
            _panel.init(updateAction);
            Dimension dim = _panel.getPreferredSize();
//            JScrollPane sp = new JScrollPane(_panel);
            dim = new Dimension(dim.width +25, dim.height + 25);
//            add(_panel);
//            sp.setPreferredSize(dim);
            _panel.setPreferredSize(dim);
            add(_panel);
            setTitle(Bundle.getMessage(title));
            pack();
            location = PlaceWindow.inside(cb._editor, pos, this);
            setLocation(location);
            setVisible(true);
        }

        /*
         * Do for dialog what FamilyItemPanel, ItemPanel and DisplayFrame 
         * need to do for reSizeDisplay and reSize
         */
        private void displayIcons() {
            Dimension oldDim = _panel.getSize();
            Dimension totalDim = getSize();
            _panel.invalidate();
            invalidate();
            Dimension newDim = _panel.getPreferredSize();
            Dimension deltaDim = new Dimension(totalDim.width - oldDim.width, totalDim.height - oldDim.height);
            Dimension dim = new Dimension(deltaDim.width + newDim.width + 10, 
                    deltaDim.height + newDim.height + 10);
            setPreferredSize(dim);
            pack();
            setLocation(location);
            repaint();
            if (log.isDebugEnabled()) {
                log.debug(" panelDim= ({}, {}) totalDim= ({}, {}) setPreferredSize to ({}, {})", 
                        oldDim.width, oldDim.height, newDim.width, newDim.height, dim.width, dim.height);
            }
        }

        private void convertTO(OBlock block) {
            IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_parent._editor);
            t.setOccBlockHandle(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(block.getSystemName(), block));
            t.setTurnout(((TurnoutIcon) _pos).getNamedTurnout());
            t.setFamily(_panel.getFamilyName());

            HashMap<String, HashMap<String, NamedIcon>> iconMap = ((IndicatorTOItemPanel)_panel).getIconMaps();
            Iterator<Entry<String, HashMap<String, NamedIcon>>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, HashMap<String, NamedIcon>> entry = it.next();
                String status = entry.getKey();
                Iterator<Entry<String, NamedIcon>> iter = entry.getValue().entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, NamedIcon> ent = iter.next();
                    t.setIcon(status, ent.getKey(), new NamedIcon(ent.getValue()));
                }
            }
            t.setLevel(Editor.TURNOUTS);
            t.setScale(_pos.getScale());
            t.rotate(_pos.getDegrees());
            finishConvert(t, block);
        }

        private void convertSeg(OBlock block) {
            IndicatorTrackIcon t = new IndicatorTrackIcon(_parent._editor);
            t.setOccBlockHandle(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(block.getSystemName(), block));
            t.setFamily(_panel.getFamilyName());

            HashMap<String, NamedIcon> iconMap = _panel.getIconMap();
            Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, NamedIcon> entry = it.next();
                t.setIcon(entry.getKey(), new NamedIcon(entry.getValue()));
            }
            t.setLevel(Editor.TURNOUTS);
            t.setScale(_pos.getScale());
            t.rotate(_pos.getDegrees());
            finishConvert(t, block);
        }

        /*
         * Replace references to _oldIcon with pos
         */
        private void finishConvert(Positionable pos, OBlock block) {
            ArrayList<Positionable> selectionGroup = _parent._editor.getSelectionGroup();
            selectionGroup.remove(_pos);
            selectionGroup.add(pos);
            ArrayList<Positionable> circuitIcons = _parent.getCircuitIcons(block);
            circuitIcons.remove(_pos);
            circuitIcons.add(pos);
            pos.setLocation(_pos.getLocation());
            _pos.remove();
            _parent._editor.putItem(pos);
            pos.updateSize();
            _parent._editor.highlight(null);
            dispose();
        }

        private final static Logger log = LoggerFactory.getLogger(ConvertDialog.class);
    }
