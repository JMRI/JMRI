// CoordinateEdit.java

package jmri.jmrit.display;

import java.awt.*;
import java.awt.event.ActionEvent;
//import java.awt.event.MouseEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.*;

import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;

/**
 * Displays and allows user to modify x & y coordinates of
 * positionable labels
 * This class has been generalized to provide popup edit dialogs for 
 * positionable item properties when TextFields are needed to input data.
 * The current list of properties served is:
 * <LI>
 *  modify x & y coordinates 
 *  modify level
 *  modify tooltip
 *  modify border size
 *  modify margin size
 *  modify fixed size
 *  modify rotation degress
 *  modify scaling
 *  modify text
 *  </LI>
 * To use, write a static method that provides the dialog frame.  Then
 * write an initX method that customizes the dialog for the property.
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2010
 * @version $Revision: 1.14 $
 */

public class CoordinateEdit extends JmriJFrame {

    static final java.util.ResourceBundle rb = 
                java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

	Positionable pl; 			// positional label tracked by this frame
	int oldX;
	int oldY;
    double oldD;
    String oldStr;

	// member declarations
	javax.swing.JLabel lableName = new javax.swing.JLabel();
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
        return new AbstractAction(rb.getString("SetXY")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("SetXY"), pos);
                    f.initSetXY();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getLevelEditAction(final Positionable pos) {
        return new AbstractAction(rb.getString("SetLevel")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("SetLevel"), pos);
                    f.initSetLevel();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getTooltipEditAction(final Positionable pos) {
        return new AbstractAction(rb.getString("SetTooltip")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("SetTooltip"), pos);
                    f.initSetTip();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////
    
    public static AbstractAction getBorderEditAction(final Positionable pos) {
        return new AbstractAction(rb.getString("SetBorderSize")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("SetBorderSize"), pos);
                    f.initBorder();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getMarginEditAction(final Positionable pos) {
        return new AbstractAction(rb.getString("SetMarginSize")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("SetMarginSize"), pos);
                    f.initMargin();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getFixedSizeEditAction(final Positionable pos) {
        return new AbstractAction(rb.getString("SetFixedSize")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("SetFixedSize"), pos);
                    f.initFixedSize();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getRotateEditAction(final Positionable pos) {
        return new AbstractAction(rb.getString("rotate")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("rotate"), pos);
                    f.initRotate();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getScaleEditAction(final Positionable pos) {
        return new AbstractAction(rb.getString("scale")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("scale"), pos);
                    f.initScale();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getTextEditAction(final Positionable pos, final String title) {
        return new AbstractAction(rb.getString(title)) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString(title), pos);
                    f.initText();
                    f.setVisible(true);	
                    f.setLocationRelativeTo((Component)pos);
                }
            };
    }
    //////////////////////////////////////////////////////////////

    public static AbstractAction getZoomEditAction(final Positionable pos) {
        return new AbstractAction(rb.getString("Zoom")) {
                public void actionPerformed(ActionEvent e) {
                    CoordinateEdit f = new CoordinateEdit();
                    f.addHelpMenu("package.jmri.jmrit.display.CoordinateEdit", true);
                    f.init(rb.getString("Zoom"), pos);
                    f.initZoom();
                    f.setVisible(true);	
                    f.setLocation(100,100);
                }
            };
    }
    ////////////////////////////////////////////////////////////// 

	public CoordinateEdit() {
		super();
	}

	public void windowClosed(java.awt.event.WindowEvent e) {
		super.windowClosed(e);
	}

	public void init(String title, Positionable pos) {
        pl = pos;
		lableName.setText("Name: ");
		lableName.setVisible(true);
		
		nameText.setText(pos.getNameString());
		nameText.setVisible(true);

		okButton.setText("  Set  ");
		okButton.setVisible(true);

		cancelButton.setText(" Cancel ");
		cancelButton.setVisible(true);

		setTitle(title);
        setLocation(pl.getLocation());
    }

    public void initSetXY() {
        oldX = pl.getX();
        oldY = pl.getY();

        textX = new javax.swing.JLabel();
		textX.setText("x= " + pl.getX());
		textX.setVisible(true);
        textY = new javax.swing.JLabel();
		textY.setText("y= " + pl.getY());
		textY.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0,0,10000,1);
        ChangeListener listener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int x = ((Number)spinX.getValue()).intValue();
                    int y = ((Number)spinY.getValue()).intValue();
                    pl.setLocation(x, y);
                    textX.setText("x= " + pl.getX());
                    textY.setText("y= " + pl.getY());
                }
            };
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(new Integer(pl.getX()));
        spinX.setToolTipText("Enter x coordinate");
        spinX.setMaximumSize(new Dimension(
				spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        spinX.addChangeListener(listener);
        model = new javax.swing.SpinnerNumberModel(0,0,10000,1);
        spinY = new javax.swing.JSpinner(model);
        spinY.setValue(new Integer(pl.getY()));
        spinY.setToolTipText("Enter y coordinate");
        spinY.setMaximumSize(new Dimension(
				spinY.getMaximumSize().width, spinY.getPreferredSize().height));
        spinY.addChangeListener(listener);

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(spinX, 1, 1);
		addItem(textY, 0, 2);
		addItem(spinY, 1, 2);
		addItem(cancelButton, 0, 3);
		addItem(okButton, 1, 3);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                int x = ((Number)spinX.getValue()).intValue();
                int y = ((Number)spinY.getValue()).intValue();
                pl.setLocation(x, y);
                textX.setText("x= " + pl.getX());
                textY.setText("y= " + pl.getY());
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.setLocation(oldX, oldY);
                dispose();
			}
		});
		pack();
	}

    public void initSetLevel() {
        oldX = pl.getDisplayLevel();
        textX = new javax.swing.JLabel();
		textX.setText("level= " +pl.getDisplayLevel());
		textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0,0,10,1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(new Integer(pl.getDisplayLevel()));
        spinX.setToolTipText("Enter display level");
        spinX.setMaximumSize(new Dimension(
				spinX.getMaximumSize().width, spinX.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(spinX, 1, 1);
		addItem(cancelButton, 0, 2);
		addItem(okButton, 1, 2);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                int l = ((Number)spinX.getValue()).intValue();
                pl.setDisplayLevel(l);
                textX.setText("level= " + l);
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.setDisplayLevel(oldX);
                dispose();
			}
		});
		pack();
	}

    public void initSetTip() {
        oldStr = pl.getTooltip().getText();
        textX = new javax.swing.JLabel();
		textX.setText("Tooltip ");
		textX.setVisible(true);

        xTextField = new javax.swing.JTextField(15);
		xTextField.setText(pl.getTooltip().getText());
		xTextField.setToolTipText("Enter Tooltip");
		xTextField.setMaximumSize(new Dimension(
				xTextField.getMaximumSize().width+100, xTextField.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(xTextField, 1, 1);
		addItem(cancelButton, 0, 2);
		addItem(okButton, 1, 2);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getTooltip().setText(xTextField.getText());
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.getTooltip().setText(oldStr);
                dispose();
			}
		});
		pack();
	}

    public void initBorder() {
        PositionableLabel pLabel = (PositionableLabel)pl;
        oldX = pLabel.getBorderSize();

        textX = new javax.swing.JLabel();
		textX.setText("Border= "+pLabel.getBorderSize());
		textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0,0,1000,1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(new Integer(pLabel.getBorderSize()));
        spinX.setToolTipText("Enter border size");
        spinX.setMaximumSize(new Dimension(
				spinX.getMaximumSize().width, spinX.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(spinX, 1, 1);
		addItem(cancelButton, 0, 2);
		addItem(okButton, 1, 2);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                int l = ((Number)spinX.getValue()).intValue();
                ((PositionableLabel)pl).setBorderSize(l);
                textX.setText("Border= " + l);
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                ((PositionableLabel)pl).setBorderSize(oldX);
                dispose();
			}
		});
		pack();
	}

    public void initMargin() {
        PositionableLabel pLabel = (PositionableLabel)pl;
        oldX = pLabel.getMargin();

        textX = new javax.swing.JLabel();
		textX.setText("Margin= "+pLabel.getMargin());
		textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0,0,1000,1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(new Integer(pLabel.getMargin()));
        spinX.setToolTipText("Enter margin size");
        spinX.setMaximumSize(new Dimension(
				spinX.getMaximumSize().width, spinX.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(spinX, 1, 1);
		addItem(cancelButton, 0, 2);
		addItem(okButton, 1, 2);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                int l = ((Number)spinX.getValue()).intValue();
                ((PositionableLabel)pl).setMargin(l);
                textX.setText("Margin= " + l);
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                ((PositionableLabel)pl).setMargin(oldX);
                dispose();
			}
		});
		pack();
	}

    public void initFixedSize() {
        PositionableLabel pLabel = (PositionableLabel)pl;
        oldX = pLabel.getFixedHeight();
        oldY = pLabel.getFixedWidth();

        textX = new javax.swing.JLabel();
		textX.setText("Height= " + pLabel.getFixedHeight());
		textX.setVisible(true);
        textY = new javax.swing.JLabel();
		textY.setText("Width= " + pLabel.getFixedWidth());
		textY.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0,0,1000,1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(new Integer(pLabel.getFixedHeight()));
        spinX.setToolTipText(rb.getString("FixedSizeHeight"));
        spinX.setMaximumSize(new Dimension(
				spinX.getMaximumSize().width, spinX.getPreferredSize().height));
        model = new javax.swing.SpinnerNumberModel(0,0,1000,1);
        spinY = new javax.swing.JSpinner(model);
        spinY.setValue(new Integer(pLabel.getFixedWidth()));
        spinY.setToolTipText(rb.getString("FixedSizeWidth"));
        spinY.setMaximumSize(new Dimension(
				spinY.getMaximumSize().width, spinY.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(spinX, 1, 1);
		addItem(textY, 0, 2);
		addItem(spinY, 1, 2);
		addItem(cancelButton, 0, 3);
		addItem(okButton, 1, 3);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                int height = ((Number)spinX.getValue()).intValue();
                int width = ((Number)spinY.getValue()).intValue();
                PositionableLabel pLabel = (PositionableLabel)pl;
                pLabel.setFixedSize(width, height);
                textX.setText("Height= " + pLabel.getFixedHeight());
                textY.setText("Width= " + pLabel.getFixedWidth());
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                ((PositionableLabel)pl).setFixedSize(oldY, oldX);
                dispose();
			}
		});
		pack();
	}

    public void initRotate() {
        PositionableLabel pLabel = (PositionableLabel)pl;
        oldX = ((NamedIcon)pLabel.getIcon()).getDegrees();

        textX = new javax.swing.JLabel();
		textX.setText("Angle= "+((NamedIcon)pLabel.getIcon()).getDegrees());
		textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(0,-360,360,1);
        spinX = new javax.swing.JSpinner(model);
        spinX.setValue(new Integer(((NamedIcon)pLabel.getIcon()).getDegrees()));
        spinX.setToolTipText("Enter rotate degrees");
        spinX.setMaximumSize(new Dimension(
				spinX.getMaximumSize().width, spinX.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(spinX, 1, 1);
		addItem(cancelButton, 0, 2);
		addItem(okButton, 1, 2);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                int l = ((Number)spinX.getValue()).intValue();
                ((PositionableLabel)pl).rotate(l);
                textX.setText("Angle= " + l);
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
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
		textX.setText("Scale= "+oldD*100);
//		textX.setText("Scale= "+scale);
		textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(100.0,10.0,5000.0,1.0);
        spinX = new javax.swing.JSpinner(model);
        if (log.isDebugEnabled()) { log.debug("scale%= "+(int)Math.round(oldD*100));
        }
        spinX.setToolTipText("Enter scale percentage");
        spinX.setMaximumSize(new Dimension(
				spinX.getMaximumSize().width, spinX.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(spinX, 1, 1);
		addItem(cancelButton, 0, 2);
		addItem(okButton, 1, 2);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                pl.setScale(((Number)spinX.getValue()).doubleValue()/100);
                textX.setText("Scale= " + pl.getScale()*100);
//                textX.setText("Scale= " + (int)Math.round(pl.getScale()*100));
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
			}
		});
		pack();
	}

    public void initText() {
        PositionableLabel pLabel = (PositionableLabel)pl;
        oldStr = pLabel.getText();
        textX = new javax.swing.JLabel();
		textX.setText("Text= ");
		textX.setVisible(true);

        xTextField = new javax.swing.JTextField(15);
		xTextField.setText(pLabel.getText());
		xTextField.setToolTipText("Enter Text");
		xTextField.setMaximumSize(new Dimension(
				xTextField.getMaximumSize().width+100, xTextField.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(xTextField, 1, 1);
		addItem(cancelButton, 0, 2);
		addItem(okButton, 1, 2);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                PositionableLabel pp = (PositionableLabel)pl;
                String t = xTextField.getText();
                boolean hasText = (t!=null && t.length()>0);
                if (pp.isIcon()) {
                    pp._text = hasText; 
                    pp.setText(t);
                    dispose();
                } else if (hasText) {
                    pp.setText(t);
                    dispose();
                } else {
                    xTextField.setText("Item disappears with null text!");
                }
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                ((PositionableLabel)pl).setText(oldStr);
                dispose();
			}
		});
		pack();
	}

    public void initZoom() {
        //int scale = (int)Math.round(pl.getScale()*100);
        oldD = pl.getScale();

        textX = new javax.swing.JLabel();
		textX.setText("Scale= "+oldD*100);
//		textX.setText("Scale= "+scale);
		textX.setVisible(true);

        SpinnerNumberModel model = new SpinnerNumberModel(100.0,1.0,5000.0,1.0);
        spinX = new javax.swing.JSpinner(model);
        if (log.isDebugEnabled()) { log.debug("scale%= "+(int)Math.round(oldD*100));
        }
        spinX.setToolTipText("Enter scale percentage");
        spinX.setMaximumSize(new Dimension(
				spinX.getMaximumSize().width, spinX.getPreferredSize().height));

		getContentPane().setLayout(new GridBagLayout());
		
		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(spinX, 1, 1);
		addItem(cancelButton, 0, 2);
		addItem(okButton, 1, 2);

		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                double s = ((Number)spinX.getValue()).doubleValue()/100;
                pl.setScale(s);
                ((PositionableJComponent)pl)._editor.setPaintScale(s);
                textX.setText("Scale= " + pl.getScale()*100);
                dispose();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
			}
		});
		pack();
	}


	private void addItem(JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		getContentPane().add(c, gc);
	}
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CoordinateEdit.class.getName());
}
