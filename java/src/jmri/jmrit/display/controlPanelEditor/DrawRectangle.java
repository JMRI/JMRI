package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.Point;
/*
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import jmri.jmrit.display.Positionable;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 * 
 */

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class DrawRectangle  extends DrawFrame {
	
	
	public DrawRectangle(String title, ShapeDrawer parent) {
		super(title, parent);
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        contentPane.add(makePanel());
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(10,10));
        border.add(contentPane);
        setContentPane(border);
        pack();
   }
	
   protected JPanel makePanel() {
	   JPanel panel = new JPanel();
	   return panel;
   }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DrawRectangle.class.getName());
}