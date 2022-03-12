package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

class ConvertDialog extends JDialog {

        private final CircuitBuilder _parent;
        private final PositionableLabel _pos;
        FamilyItemPanel _panel;
        DisplayFrame _filler;

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
                _panel = new IndicatorTOItemPanel(_filler, title, null, null) {
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
                updateAction = (ActionEvent a) -> convertTO(block);
            } else {
                title = "IndicatorTrack";
                _panel = new IndicatorItemPanel(_filler, title, null) {
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
                updateAction = (ActionEvent a) -> convertSeg(block);
            }

            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("notIndicatorIcon")));
            _panel.add(p);
            _panel.init(makeBottomPanel(updateAction, block));
            Dimension dim = _panel.getPreferredSize();

            javax.swing.JScrollPane sp = new javax.swing.JScrollPane(_panel);
            dim = new Dimension(dim.width +25, dim.height + 25);
            sp.setPreferredSize(dim);
            sp.setPreferredSize(dim);
            add(sp);
            setTitle(Bundle.getMessage(title));
            pack();
            jmri.InstanceManager.getDefault(jmri.util.PlaceWindow.class).nextTo(cb._editor, pos, this);
             setVisible(true);
        }

        private JPanel makeBottomPanel(ActionListener updateAction, OBlock block) {
            JPanel panel = new JPanel();
            JButton button = new JButton(Bundle.getMessage("convert"));
            button.addActionListener(updateAction);
            panel.add(button);
            
            button = new JButton(Bundle.getMessage("skip"));
            button.addActionListener((ActionEvent a) -> dispose());
            panel.add(button);
            return panel;
        }
        /*
         * Do for dialog what FamilyItemPanel, ItemPanel and DisplayFrame 
         * need to do for reSizeDisplay and reSize
         */
        private void displayIcons() {
            Dimension newDim = _panel.getPreferredSize();
            Dimension deltaDim = _panel.shellDimension(_panel);
            Dimension dim = new Dimension(deltaDim.width + newDim.width, deltaDim.height + newDim.height);
            setPreferredSize(dim);
            invalidate();
            pack();
        }

        private void convertTO(OBlock block) {
            IndicatorTurnoutIcon t = new IndicatorTurnoutIcon(_parent._editor);
            t.setOccBlockHandle(InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(block.getSystemName(), block));
            if ( _pos instanceof TurnoutIcon ) {
                t.setTurnout(((TurnoutIcon) _pos).getNamedTurnout());
            }
            t.setFamily(_panel.getFamilyName());

            HashMap<String, HashMap<String, NamedIcon>> iconMap = ((IndicatorTOItemPanel)_panel).getIconMaps();
            for (Entry<String, HashMap<String, NamedIcon>> entry : iconMap.entrySet()) {
                String status = entry.getKey();
                for (Entry<String, NamedIcon> ent : entry.getValue().entrySet()) {
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
            for (Entry<String, NamedIcon> entry : iconMap.entrySet()) {
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
            try {
                _parent._editor.putItem(pos);
            } catch (Positionable.DuplicateIdException e) {
                log.error("Editor.putItem() has thrown DuplicateIdException", e);
            }
            pos.updateSize();
            _parent._editor.highlight(null);
            dispose();
            _filler.dispose();
        }

        private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConvertDialog.class);
    }
