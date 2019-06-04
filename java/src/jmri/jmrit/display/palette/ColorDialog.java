package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.palette.DecoratorPanel.AJSpinner;
import jmri.util.swing.JmriColorChooser;

/**
 * A dialog to color a Component in a Window.  The ColorChooser
 * interactively modifies the color of the component on the window
 * until the user either cancels or decides to keep the changes.
 *
 * @author Pete Cressman Copyright (C) 2018
 * @since 4.13.1
 */
public class ColorDialog extends JDialog implements ChangeListener {

    public static final int STRUT = 6;

    public static final int ONLY = 0;
    public static final int BORDER = DecoratorPanel.BORDER; // (= 1)
    public static final int MARGIN = DecoratorPanel.MARGIN; // (= 2)
    public static final int FWIDTH = DecoratorPanel.FWIDTH; // (= 3)
    public static final int FHEIGHT = DecoratorPanel.FHEIGHT;   // (= 4)
    public static final int FONT = 5;
    public static final int TEXT = 6;

        JColorChooser _chooser;
        JComponent _target;
        int _type;
        Color _saveColor;
        boolean _saveOpaque;
        String _saveText;
        PositionablePopupUtil _util;
        PositionablePopupUtil _saveUtil;
        ActionListener _colorAction;
        JPanel _preview;

        /**
         * 
         * @param client Window holding the component
         * @param t target whose color may be changed
         * @param type whicd attribute is being changed
         * @param ca callback to tell client the component's color was changed. 
         * May be null if client doesen't care.
         */
        public ColorDialog(Frame client, JComponent t, int type, ActionListener ca) {
            super(client, true);
            _target = t;
            _type = type;
            if (t instanceof Positionable) {
                Positionable pos = (Positionable)t;
                _util = pos.getPopupUtility();
                if (_util != null) {
                    _util.setSuppressRecentColor(true);
                    Positionable p = pos.deepClone();
                    _saveUtil = p.getPopupUtility();
                    p.remove();
                }
           } else {
                _util = null;
            }
            _saveOpaque = t.isOpaque();
            _colorAction = ca;

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createVerticalGlue());

            String title;
            switch (type) {
                case ONLY:
                    title = "PanelColor";
                    _saveColor = t.getBackground();
                    break;
                case BORDER:
                    title = "SetBorderSizeColor";
                    _saveColor = _util.getBorderColor();
                    SpinnerNumberModel model = new SpinnerNumberModel(_util.getBorderSize(), 0, 100, 1);
                    JPanel p = makePanel(DecoratorPanel.makeSpinPanel("borderSize", new AJSpinner(model, BORDER), this));
                    panel.add(p);
                    break;
                case MARGIN:
                    title = "SetMarginSizeColor";
                    _saveColor = _util.getBackground();
                    model = new SpinnerNumberModel(_util.getMargin(), 0, 100, 1);
                    p = makePanel(DecoratorPanel.makeSpinPanel("marginSize", new AJSpinner(model, MARGIN), this));
                    panel.add(p);
                    break;
                case FONT:
                    title = "SetFontSizeColor";
                    _saveColor = _util.getForeground();
                    ActionListener fontAction = ((ActionEvent event) -> {
                        update(); // callback
                    });
                    FontPanel fontPanel = new FontPanel(_util, fontAction);
                    panel.add(fontPanel);
                    fontPanel.setFontSelections();
                    break;
                case TEXT:
                    title = "SetTextSizeColor";
                    _saveColor = _util.getBackground();
                    _saveText = ((PositionableLabel)t).getUnRotatedText();
                    panel.add(makePanel(makeTextSpinnerPanel()));
                    panel.add(Box.createVerticalGlue());
                    panel.add(makePanel(makeTextPanel()));
                    break;
                default:
                    title = "ColorChooser";
                    _saveColor = t.getBackground();
            }
            panel.add(Box.createVerticalStrut(STRUT));
            setTitle(Bundle.getMessage(title));

            _chooser = JmriColorChooser.extendColorChooser(new JColorChooser(_saveColor));
            _chooser.getSelectionModel().addChangeListener(this);
            _chooser.setPreviewPanel(new JPanel());
            panel.add(_chooser);
            panel.add(Box.createVerticalStrut(STRUT));

            panel.add(makeDoneButtonPanel());
            panel.add(Box.createVerticalGlue());

            super.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cancel();
                }
            });
            
            
            setContentPane(panel);
            setLocation(jmri.util.PlaceWindow.nextTo(client, t, this));

            pack();
            setVisible(true);
        }

        JPanel makePanel(JPanel p) {
            JPanel panel = new JPanel();
            panel.add(p);
            return panel;
        }

        JPanel makeTextSpinnerPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            SpinnerNumberModel model = new SpinnerNumberModel(_util.getFixedWidth(), 0, 1000, 1);
            panel.add(DecoratorPanel.makeSpinPanel("fixedWidth", new AJSpinner(model, FWIDTH), this));
            panel.add(Box.createHorizontalStrut(STRUT));
            model = new SpinnerNumberModel(_util.getFixedHeight(), 0, 1000, 1);
            panel.add(DecoratorPanel.makeSpinPanel("fixedHeight", new AJSpinner(model, FHEIGHT), this));
            return panel;
        }

        JPanel makeTextPanel() {
            JPanel panel = new JPanel();
            JTextField textField = new JTextField(_saveText, 25);
            textField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent evt) {
                }
                @Override
                public void keyPressed(KeyEvent evt) {
                }
                @Override
                public void keyReleased(KeyEvent evt) {
                    JTextField tmp = (JTextField) evt.getSource();
                    ((PositionableLabel)_target).setText(tmp.getText());
                    update();
                }
            });
            panel.add(textField);
            return panel;
        }

        protected JPanel makeDoneButtonPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener((ActionEvent event) -> {
                    log.debug("Done button: color= {}", _chooser.getColor());
                    if (_colorAction != null) {
                        _colorAction.actionPerformed(null);
                    }
                    if (_util != null) {
                        _util.setSuppressRecentColor(false);
                    }
                    JmriColorChooser.addRecentColor(_chooser.getColor());
                    dispose();
            });
            panel.add(doneButton);

            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener((ActionEvent event) -> {
                    cancel();
                });

            panel.add(cancelButton);

            return panel;
        }

        void cancel() {
            if (_util != null) {
                Positionable pl = (Positionable)_target;
                pl.getEditor().setAttributes(_saveUtil, pl);
                _util.setSuppressRecentColor(false);
                pl.updateSize();
                if (_type == TEXT) {
                    ((PositionableLabel)_target).setText(_saveText);
                }
            } else {
                _target.setBackground(_saveColor);
            }
            _target.setOpaque(_saveOpaque);
            log.debug("Cancel: color= {}", _saveColor);
            dispose();
        }

        private void update() {
            if (_util != null) {
                Positionable pl = (Positionable)_target;
                pl.getEditor().setAttributes(_util, pl);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object obj = e.getSource();
            if (obj instanceof AJSpinner) {
                int num = ((Number) ((AJSpinner) obj).getValue()).intValue();
                switch (((AJSpinner) obj)._which) {
                    case BORDER:
                        _util.setBorderSize(num);
                        break;
                    case MARGIN:
                        _util.setMargin(num);
                        break;
                    case FWIDTH:
                        _util.setFixedWidth(num);
                        break;
                    case FHEIGHT:
                        _util.setFixedHeight(num);
                        break;
                    default:
                        log.warn("Unexpected _which {}  in stateChanged", ((AJSpinner) obj)._which);
                        break;
                }
            } else {
                log.debug("stateChanged: color= {}", _chooser.getColor());
                if (_util != null) {
                    switch (_type) {
                        case BORDER:
                            _util.setBorderColor(_chooser.getColor());
                            break;
                        case MARGIN:
                            _util.setBackgroundColor(_chooser.getColor());
                            break;
                        case FONT:
                        case TEXT:
                            _util.setForeground(_chooser.getColor());
                            break;
                        default:
                    }
                } else {
                    _target.setOpaque(true);
                    _target.setBackground(_chooser.getColor());
                }
            }
            update();
        }

        private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ColorDialog.class);
}

