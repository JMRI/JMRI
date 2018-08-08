package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.palette.DecoratorPanel.AJSpinner;
import jmri.util.swing.JmriColorChooser;

/**
 * A dialog to color a Component in a Window.  The ColorChooser
 * interactively modifies the color of the component on the window
 * until the user either cancels or decides to keep the changes.
 * <p>
 * @author Pete Cressman Copyright (C) 2018
 * @since 4.13.1
 */
public class ColorDialog extends JDialog implements ChangeListener {

    public static final int ONLY = 0;
    public static final int FONT = 1;
    public static final int MARGIN = 2;
    public static final int BORDER = 3;
    public static final int TEXT = 4;

        JColorChooser _chooser;
        JComponent _target;
        Color _saveColor;
        PositionablePopupUtil _util;
        boolean _saveOpaque;
        ActionListener _colorAction;
        JPanel _preview;

        /**
         * 
         * @param client Window holding the component
         * @param t target whose color may be changed
         * @param ca callback to tell client the component's color was changed. 
         * May be null if client doesen't care.
         */
        public ColorDialog(Frame client, JComponent t, int type, ActionListener ca) {
            super(client, Bundle.getMessage("ColorChooser"), true);
            _target = t;
            if (t instanceof Positionable) {
                _util = ((Positionable)t).getPopupUtility();
                _util.setSuppressRecentColor(true);
                _saveColor = _util.getBackground();
            } else {
                _util = null;
                _saveColor = t.getBackground();
            }
            _saveOpaque = t.isOpaque();
            _colorAction = ca;

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(5, 5));

            switch (type) {
                case ONLY:
                    break;
                case FONT:
                    break;
                case MARGIN:
                    SpinnerNumberModel model = new SpinnerNumberModel(_util.getBorderSize(), 0, 100, 1);
                    add(DecoratorPanel.makeSpinPanel("marginSize", new AJSpinner(model, MARGIN), this));
                    break;
                case BORDER:
                    model = new SpinnerNumberModel(_util.getBorderSize(), 0, 100, 1);
                    add(DecoratorPanel.makeSpinPanel("borderSize", new AJSpinner(model, BORDER), this));
                    break;
                case TEXT:
                    break;
                default:
            }
            _chooser = JmriColorChooser.extendColorChooser(new JColorChooser(_saveColor));
            _chooser.getSelectionModel().addChangeListener(this);
            _chooser.setPreviewPanel(new JPanel());
            panel.add(_chooser, BorderLayout.NORTH);
            panel.add(makeDoneButtonPanel(), BorderLayout.SOUTH);

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
//            log.debug("ColorDialog: color= {}", _chooser.getColor());
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
                _util.setBackgroundColor(_saveColor);
                _util.setSuppressRecentColor(false);
            } else {
                _target.setBackground(_saveColor);
            }
            _target.setOpaque(_saveOpaque);
            log.debug("Cancel: color= {}", _saveColor);
            dispose();
            
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
                    default:
                        log.warn("Unexpected _which {}  in stateChanged", ((AJSpinner) obj)._which);
                        break;
                }
            } else {
                log.debug("stateChanged: color= {}", _chooser.getColor());
                _target.setOpaque(true);
                if (_util != null) {
                    _util.setBackgroundColor(_chooser.getColor());
                } else {
                    _target.setBackground(_chooser.getColor());
                }
            }
        }

        private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ColorDialog.class);
}

