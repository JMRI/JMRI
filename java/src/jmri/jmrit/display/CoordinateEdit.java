package jmri.jmrit.display;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays and allows user to modify {@literal x & y} coordinates of
 * positionable labels This class has been generalized to provide popup edit
 * dialogs for positionable item properties when TextFields are needed to input
 * data.
 * <P>
 * The class name no longer identifies the full purpose of the class, However
 * the name is retained because coordinate editing was the genesis. The current
 * list of properties served for editing is:
 * <ul>
 * <li>modify {@literal x & y} coordinates modify level modify tooltip modify
 * border size</li>
 * <li>modify margin size modify fixed size modify rotation degrees modify
 * scaling</li>
 * <li>modify text labels modify zoom scaling modify panel name</li>
 * </ul>
 * To use, write a static method that provides the dialog frame. Then write an
 * initX method that customizes the dialog for the property.
 *
 * @author Dan Boudreau Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2010
 */
public class CoordinateEdit extends JmriJFrame {

    Positionable pl;    // positional label tracked by this frame
    int oldX;
    int oldY;
    double oldD;
    String oldStr;

    // member declarations
    javax.swing.JLabel nameText = new javax.swing.JLabel();
    javax.swing.JLabel textX;
    javax.swing.JLabel textY;

    // buttons
    javax.swing.JButton okButton = new javax.swing.JButton();
    javax.swing.JButton cancelButton = new javax.swing.JButton();

    // text field
    javax.swing.JTextField xTextField;
    javax.swing.JTextField yTextField;

    //SpinnerNumberModel _spinModel;
    javax.swing.JSpinner spinX;
    javax.swing.JSpinner spinY;

    public static AbstractAction getCoordinateEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("SetXY", "...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("SetXY", ""), pos, true); // use property without ellipsis in variable
                f.initSetXY();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getLevelEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("SetLevel", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("SetLevel", ""), pos, true); // use property without ellipsis in variable
                f.initSetLevel();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getTooltipEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("SetTooltip", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("SetTooltip", ""), pos, true); // use property without ellipsis in variable
                f.initSetTip();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getBorderEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("SetBorderSize", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("SetBorderSize", ""), pos, true);
                f.initBorder();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getMarginEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("SetMarginSize", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("SetMarginSize", ""), pos, true);
                f.initMargin();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getFixedSizeEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("SetFixedSize", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("SetFixedSize", ""), pos, true);
                f.initFixedSize();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getRotateEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("rotate", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("rotate", ""), pos, true);
                f.initRotate();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getScaleEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("scale", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("scale", ""), pos, true);
                f.initScale();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getTextEditAction(final Positionable pos, final String title) {
        return new AbstractAction(Bundle.getMessage(title) + "...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage(title), pos, false);
                f.initText();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getLinkEditAction(final Positionable pos, final String title) {
        return new AbstractAction(Bundle.getMessage(title) + "...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage(title), pos, false);
                f.initLink();
                f.setVisible(true);
                f.setLocationRelativeTo((Component) pos);
            }
        };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getZoomEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("Zoom", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("Zoom", ""), pos, false);
                f.initZoom();
                f.setVisible(true);
                //f.setLocation(100,100);
                f.setLocationRelativeTo(pos.getEditor().getTargetPanel());
            }
        };
    }
    ////////////////////////////////////////////////////////////// 

    public static AbstractAction getNameEditAction(final Positionable pos) {
        return new AbstractAction(Bundle.getMessage("renamePanelMenu", "...")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                CoordinateEdit f = new CoordinateEdit();
                f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                f.init(Bundle.getMessage("renamePanelMenu", ""), pos, false);
                f.initSetName();
                f.setVisible(true);
                //f.setLocation(100,100);
                f.setLocationRelativeTo(pos.getEditor().getTargetPanel());
            }
        };
    }
    ////////////////////////////////////////////////////////////// 

    public CoordinateEdit() {
        super(false, false);
    }

    @Override
    public void windowClosed(java.awt.event.WindowEvent e) {
        super.windowClosed(e);
    }

    public void init(String title, Positionable pos, boolean showName) {
        pl = pos;
        if (showName) {
            nameText.setText(java.text.MessageFormat.format(Bundle.getMessage("namelabel"), pos.getNameString()));
            nameText.setVisible(true);
        }
        okButton.setText(Bundle.getMessage("ButtonOK"));
        okButton.setVisible(true);

        cancelButton.setText(Bundle.getMessage("ButtonCancel"));
        cancelButton.setVisible(true);

        Dimension dim = (new JButton("XXXXXXXX")).getPreferredSize();
        okButton.setMinimumSize(dim);
        cancelButton.setMinimumSize(dim);
        setTitle(title);
        //setLocation(pl.getLocation());
    }

    public void initSetXY() {
        oldX = pl.getX();
        oldY = pl.getY();

        textX = new javax.swing.JLabel();
        textX.setText("X: " + pl.getX());
        textX.setVisible(true);
        textY = new javax.swing.JLabel();
        textY.setText("Y: " + pl.getY());
        textY.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 10000, 1);
        ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int x = ((Number) spinX.getValue()).intValue();
                int y = ((Number) spinY.getValue()).intValue();
                pl.setLocation(x, y);
                textX.setText("X: " + pl.getX());
                textY.setText("Y: " + pl.getY());
            }
        };
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(Integer.valueOf(pl.getX()));
        spinX.setToolTipText(Bundle.getMessage("EnterXcoord"));
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        spinX.addChangeListener(listener);
        model = new javax.swing.SpinnerNumberModel(0, 0, 10000, 1);
        spinY = new javax.swing.JSpinner(model);
        spinY.setValue(Integer.valueOf(pl.getY()));
        spinY.setToolTipText(Bundle.getMessage("EnterYcoord"));
        spinY.setMaximumSize(new Dimension(
                spinY.getMaximumSize().width, spinY.getPreferredSize().height));
        spinY.addChangeListener(listener);

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(true);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int x = ((Number) spinX.getValue()).intValue();
                int y = ((Number) spinY.getValue()).intValue();
                pl.setLocation(x, y);
                textX.setText("X: " + pl.getX());
                textY.setText("Y: " + pl.getY());
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.setLocation(oldX, oldY);
                dispose();
            }
        });
        // make large enough to easily move
        setMinimumSize(new Dimension(250, 175));
        pack();
    }

    public void initSetLevel() {
        oldX = pl.getDisplayLevel();
        textX = new javax.swing.JLabel();
        textX.setText(Bundle.getMessage("Level") + ": " + pl.getDisplayLevel());
        textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 10, 1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(Integer.valueOf(pl.getDisplayLevel()));
        spinX.setToolTipText(Bundle.getMessage("EnterLevel"));
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(false);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int l = ((Number) spinX.getValue()).intValue();
                pl.getEditor().setSelectionsDisplayLevel(l, pl);
                textX.setText(Bundle.getMessage("Level") + ": " + l);
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getEditor().setSelectionsDisplayLevel(oldX, pl);
                dispose();
            }
        });
        setMinimumSize(new Dimension(250, 175));
        pack();
    }

    public void initSetTip() {
        oldStr = pl.getTooltip().getText();
        textX = new javax.swing.JLabel();
        textX.setText(Bundle.getMessage("TooltipLabel") + ": ");
        textX.setVisible(true);

        xTextField = new javax.swing.JTextField(15);
        xTextField.setText(pl.getTooltip().getText());
        xTextField.setToolTipText(Bundle.getMessage("EnterTooltip"));
//  xTextField.setMaximumSize(new Dimension(
//    xTextField.getMaximumSize().width+100, xTextField.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addTextItems();

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getTooltip().setText(xTextField.getText()); // is fetched from pane OK but not stored in icon pl
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getTooltip().setText(oldStr);
                dispose();
            }
        });
        pack();
    }

    public void initBorder() {
        PositionablePopupUtil util = pl.getPopupUtility();
        oldX = util.getBorderSize();

        textX = new javax.swing.JLabel();
        textX.setText(Bundle.getMessage("Border") + ": " + util.getBorderSize());
        textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1000, 1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(Integer.valueOf(util.getBorderSize()));
        spinX.setToolTipText("Enter border size");
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(false);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int l = ((Number) spinX.getValue()).intValue();
                PositionablePopupUtil util = pl.getPopupUtility();
                util.setBorderSize(l);
                pl.getEditor().setAttributes(util, pl);
                textX.setText(Bundle.getMessage("Border") + ": " + l);
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getPopupUtility().setBorderSize(oldX);
                dispose();
            }
        });
        pack();
    }

    public void initMargin() {
        PositionablePopupUtil util = pl.getPopupUtility();
        oldX = util.getMargin();

        textX = new javax.swing.JLabel();
        textX.setText(Bundle.getMessage("Margin") + ": " + util.getMargin());
        textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1000, 1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(Integer.valueOf(util.getMargin()));
        spinX.setToolTipText("Enter margin size");
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(false);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int l = ((Number) spinX.getValue()).intValue();
                PositionablePopupUtil util = pl.getPopupUtility();
                pl.getPopupUtility().setMargin(l);
                pl.getEditor().setAttributes(util, pl);
                textX.setText(Bundle.getMessage("Margin") + ": " + l);
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getPopupUtility().setMargin(oldX);
                dispose();
            }
        });
        pack();
    }

    public void initFixedSize() {
        PositionablePopupUtil util = pl.getPopupUtility();
        oldX = util.getFixedHeight();
        oldY = util.getFixedWidth();

        textX = new javax.swing.JLabel();
        textX.setText("Height = " + util.getFixedHeight());
        textX.setVisible(true);
        textY = new javax.swing.JLabel();
        textY.setText("Width = " + util.getFixedWidth());
        textY.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 1000, 1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(Integer.valueOf(util.getFixedHeight()));
        spinX.setToolTipText(Bundle.getMessage("FixedSizeHeight"));
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        model = new javax.swing.SpinnerNumberModel(0, 0, 1000, 1);
        spinY = new javax.swing.JSpinner(model);
        spinY.setValue(Integer.valueOf(util.getFixedWidth()));
        spinY.setToolTipText(Bundle.getMessage("FixedSizeWidth"));
        spinY.setMaximumSize(new Dimension(
                spinY.getMaximumSize().width, spinY.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(true);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int height = ((Number) spinX.getValue()).intValue();
                int width = ((Number) spinY.getValue()).intValue();
                PositionablePopupUtil util = pl.getPopupUtility();
                util.setFixedSize(width, height);
                pl.getEditor().setAttributes(util, pl);
                textX.setText("Height: " + util.getFixedHeight());
                textY.setText("Width: " + util.getFixedWidth());
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getPopupUtility().setFixedSize(oldY, oldX);
                dispose();
            }
        });
        pack();
    }

    public void initRotate() {
        oldX = pl.getDegrees();

        textX = new javax.swing.JLabel();
        int deg = oldX;
        textX.setText(java.text.MessageFormat.format(Bundle.getMessage("Angle"), deg));
        textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0, -360, 360, 1);
        spinX = new javax.swing.JSpinner(model);
//        spinX.setValue(Integer.valueOf(((NamedIcon)pLabel.getIcon()).getDegrees()));
        spinX.setValue(deg);
        spinX.setToolTipText(Bundle.getMessage("enterDegrees"));
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(false);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int k = ((Number) spinX.getValue()).intValue();
                pl.getEditor().setSelectionsRotation(k, pl);
                textX.setText(java.text.MessageFormat.format(Bundle.getMessage("Angle"), k));
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
        pack();
    }

    public void initScale() {
        //int scale = (int)Math.round(pl.getScale()*100);
        oldD = pl.getScale();

        textX = new javax.swing.JLabel();
        textX.setText(java.text.MessageFormat.format(Bundle.getMessage("Scale"), oldD * 100));
        textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(100.0, 10.0, 5000.0, 1.0);
        spinX = new javax.swing.JSpinner(model);
        if (log.isDebugEnabled()) {
            log.debug("scale%= " + (int) Math.round(oldD * 100));
        }
        spinX.setValue((int) Math.round(oldD * 100));
        spinX.setToolTipText(Bundle.getMessage("enterScale"));
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(false);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                double s = ((Number) spinX.getValue()).doubleValue() / 100;
                pl.getEditor().setSelectionsScale(s, pl);
                textX.setText(java.text.MessageFormat.format(Bundle.getMessage("Scale"), pl.getScale() * 100));
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
        pack();
    }

    public void initText() {
        PositionableLabel pLabel = (PositionableLabel) pl;
        oldStr = pLabel.getUnRotatedText();
        textX = new javax.swing.JLabel();
        textX.setText(Bundle.getMessage("TextLabel") + ":");
        textX.setVisible(true);

        xTextField = new javax.swing.JTextField(15);
        xTextField.setText(pLabel.getUnRotatedText());
        xTextField.setToolTipText(Bundle.getMessage("TooltipEnterText"));

        getContentPane().setLayout(new GridBagLayout());
        addTextItems();

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                PositionableLabel pp = (PositionableLabel) pl;
                String t = xTextField.getText();
                boolean hasText = (t != null && t.length() > 0);
                if (pp.isIcon() || hasText) {
                    pp._text = hasText;
                    if (pp instanceof SensorIcon) {
                       ((SensorIcon)pp).setOriginalText(t); 
                    }
                    pp.setText(t);                        
                    pp.updateSize();
                    dispose();
                } else {
                    xTextField.setText(Bundle.getMessage("warningNullText"));
                }
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                PositionableLabel pp = (PositionableLabel) pl;
                pp.setText(oldStr);
                pp.updateSize();
                dispose();
            }
        });
        pack();
    }

    public void initLink() {
        LinkingObject pLabel = (LinkingObject) pl;
        oldStr = pLabel.getUrl();
        textX = new javax.swing.JLabel();
        textX.setText(Bundle.getMessage("LinkEqual"));
        textX.setVisible(true);

        xTextField = new javax.swing.JTextField(15);
        xTextField.setText(pLabel.getUrl());
        xTextField.setToolTipText(Bundle.getMessage("EnterLink"));

        getContentPane().setLayout(new GridBagLayout());
        addTextItems();
        oldX = 0;  // counter for warning

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                LinkingObject pp = (LinkingObject) pl;
                String t = xTextField.getText();
                boolean hasText = (t != null && t.length() > 0);
                if (hasText || oldX > 0) {
                    pp.setUrl(t);
                    pp.updateSize();
                    dispose();
                } else {
                    xTextField.setText("Link disappears with null text!");
                    oldX++;
                }
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                LinkingObject pp = (LinkingObject) pl;
                pp.setUrl(oldStr);
                pp.updateSize();
                dispose();
            }
        });
        pack();
    }

    public void initZoom() {
        oldD = pl.getScale();

        textX = new javax.swing.JLabel();
        textX.setText(java.text.MessageFormat.format(Bundle.getMessage("Scale"), oldD * 100));
        textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(100.0, 1.0, 5000.0, 1.0);
        spinX = new javax.swing.JSpinner(model);
        if (log.isDebugEnabled()) {
            log.debug("scale%= " + (int) Math.round(oldD * 100));
        }
        spinX.setToolTipText(Bundle.getMessage("enterZoom"));
        spinX.setMaximumSize(new Dimension(
                spinX.getMaximumSize().width, spinX.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addSpinItems(false);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                double s = ((Number) spinX.getValue()).doubleValue() / 100;
                pl.setScale(s);
                pl.getEditor().setPaintScale(s);
                textX.setText(java.text.MessageFormat.format(Bundle.getMessage("Scale"), pl.getScale() * 100));
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
        pack();
    }

    public void initSetName() {
        oldStr = pl.getEditor().getName();

        textX = new javax.swing.JLabel();
        textX.setText(java.text.MessageFormat.format(Bundle.getMessage("namelabel"), oldStr));
        textX.setVisible(true);

        xTextField = new javax.swing.JTextField(15);
        xTextField.setText(oldStr);
        xTextField.setToolTipText(Bundle.getMessage("PromptNewName"));
//  xTextField.setMaximumSize(new Dimension(1000, xTextField.getPreferredSize().height));
//    xTextField.getMaximumSize().width+100, xTextField.getPreferredSize().height));

        getContentPane().setLayout(new GridBagLayout());

        addTextItems();

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String t = xTextField.getText();
                Editor ed = pl.getEditor();
                ed.setName(t);
                ed.setTitle();
                textX.setText(java.text.MessageFormat.format(Bundle.getMessage("namelabel"), t));
                dispose();
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getEditor().setName(oldStr);
                dispose();
            }
        });
        pack();
    }

    protected void addSpinItems(boolean addY) {
        addItem(nameText, 0, 0, 2, true);
        addItem(textX, 0, 1, 1, true);
        addItem(spinX, 1, 1, 1, false);
        if (addY) {
            addItem(textY, 0, 2, 1, true);
            addItem(spinY, 1, 2, 1, false);
            addItem(cancelButton, 0, 3, 1, false);
            addItem(okButton, 1, 3, 1, false);
        } else {
            addItem(cancelButton, 0, 2, 1, false);
            addItem(okButton, 1, 2, 1, false);
        }
        validate();
    }

    private void addTextItems() {
        addItem(nameText, 0, 0, 2, true);
        addItem(textX, 0, 1, 1, true);
        addItem(xTextField, 1, 1, 1, true);
        addItem(cancelButton, 0, 2, 1, false);
        addItem(okButton, 1, 2, 1, false);
        validate();
    }

    private void addItem(JComponent c, int x, int y, int w, boolean horzExpand) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = w;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        gc.anchor = java.awt.GridBagConstraints.CENTER;
        if (horzExpand) {
            gc.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        }
        getContentPane().add(c, gc);
    }

    private final static Logger log = LoggerFactory.getLogger(CoordinateEdit.class.getName());
}
