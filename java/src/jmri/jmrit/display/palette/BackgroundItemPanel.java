package jmri.jmrit.display.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


import javax.swing.event.ChangeListener; 
import javax.swing.event.ChangeEvent; 

import jmri.util.JmriJFrame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.display.Editor;

import jmri.jmrit.catalog.NamedIcon;

/**
*  JPanels for the various item types that come from tool Tables - e.g. Sensors, Turnouts, etc.
*/
public class BackgroundItemPanel extends IconItemPanel {

    /**
    * Constructor for plain icons and backgrounds
    */
    public BackgroundItemPanel(JmriJFrame parentFrame, String type, String family, Editor editor) {
        super(parentFrame, type, family, editor);
        _level = Editor.BKG;
    }

    public void init() {
    	if (!_initialized) {
    		Thread.yield();
    		super.init();
    		add(initBottomPanel(), 2);
    		setSize(getPreferredSize());
    	}
    }

    protected JPanel instructions() {
        JPanel panel = super.instructions();
        JPanel blurb = (JPanel)panel.getComponent(0);
        blurb.add(new JLabel(java.text.MessageFormat.format(ItemPalette.rbp.getString("ToColorBackground"), 
                                                       ItemPalette.rbp.getString("ButtonBackgroundColor"))));
        blurb.add(javax.swing.Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        return panel;
    }

    private JPanel initBottomPanel() {
        JPanel bottomPanel = new JPanel();
        JButton backgroundButton = new JButton(ItemPalette.rbp.getString("ButtonBackgroundColor"));
        backgroundButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    hideCatalog();
                    new ColorDialog(_editor);
                }
        });
        backgroundButton.setToolTipText(ItemPalette.rbp.getString("ToolTipEditColor"));
        bottomPanel.add(backgroundButton);
        return bottomPanel;
    }
    
    class ColorDialog extends JDialog implements ChangeListener {

        JColorChooser _chooser;
        Editor        _editor;
        JPanel        _preview;

        ColorDialog(Editor editor) {
            super(_paletteFrame, ItemPalette.rbp.getString("ColorChooser"), true);
            _editor = editor;

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(5,5));

            _chooser = new JColorChooser(editor.getTargetPanel().getBackground());
            _chooser.getSelectionModel().addChangeListener(this);
            _preview = new JPanel();
            _preview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 4), 
                                                             ItemPalette.rbp.getString("PanelColor")));
            _preview.add (new JLabel(new NamedIcon("resources/logo.gif", "resources/logo.gif")), BorderLayout.CENTER);
            _chooser.setPreviewPanel(_preview);
            panel.add(_chooser, BorderLayout.CENTER);
            panel.add(makeDoneButtonPanel(), BorderLayout.SOUTH);

            setContentPane(panel);
            _preview.setBackground(_editor.getBackground());
            _preview.getParent().setBackground(_editor.getBackground());
            setSize(_paletteFrame.getSize().width, this.getPreferredSize().height);
            setLocationRelativeTo(_paletteFrame);
            pack();
            setVisible(true);
        }

        protected JPanel makeDoneButtonPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            JButton doneButton = new JButton(ItemPalette.rbp.getString("doneButton"));
            doneButton.addActionListener(new ActionListener() {
                    ColorDialog dialog;
                    public void actionPerformed(ActionEvent a) {
                        _editor.setBackgroundColor(_chooser.getColor());
                        dialog.dispose();
                    }
                    ActionListener init(ColorDialog d) {
                        dialog = d;
                        return this;
                    }
            }.init(this));
            panel.add(doneButton);

            JButton cancelButton = new JButton(ItemPalette.rbp.getString("cancelButton"));
            cancelButton.addActionListener(new ActionListener() {
                    ColorDialog dialog;
                    public void actionPerformed(ActionEvent a) {
                        dialog.dispose();
                    }
                    ActionListener init(ColorDialog d) {
                        dialog = d;
                        return this;
                    }
            }.init(this));
            panel.add(cancelButton);

            return panel;
        }
        public void stateChanged(ChangeEvent e) {
            _preview.setBackground(_chooser.getColor());
            _preview.getParent().setBackground(_chooser.getColor());
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BackgroundItemPanel.class.getName());
}
