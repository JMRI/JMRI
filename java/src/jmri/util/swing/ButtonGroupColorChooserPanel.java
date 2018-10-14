package jmri.util.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.Icon;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;
import javax.swing.JRadioButton;

/**
 * Abstract Color Chooser extension that presents the JMRI preset colors in
 * as a Radio Button list with proper internationalization.
 *
 * @author Paul Bender Copyright (C) 2017
 * @since 4.9.6
 */
public class ButtonGroupColorChooserPanel extends AbstractColorChooserPanel {

    private String[] colorText = {"Black", "DarkGray", "Gray",
       "LightGray", "White", "Red", "Pink", "Orange",
       "Yellow", "Green", "Blue", "Magenta", "Cyan"};    //NOI18N
    private Color[] colorCode = {Color.black, Color.darkGray, Color.gray,
       Color.lightGray, Color.white, Color.red, Color.pink, Color.orange,
       Color.yellow, Color.green, Color.blue, Color.magenta, Color.cyan};
    private int numColors = 13; //number of entries in the above arrays
    private ButtonGroup colorButtonGroup = null;

    @Override
    public void updateChooser(){
        Color color = getColorFromModel();
        // update the combo box to have the right color showing.
        for(int i = 0;i< numColors;i++){
            if(color.equals(colorCode[i])){
               String buttonLabel = Bundle.getMessage(colorText[i]);
               Enumeration e = colorButtonGroup.getElements();
               while(e.hasMoreElements()) {
                  JRadioButton button = (JRadioButton)e.nextElement();
                  if(buttonLabel.equals(button.getText())){
                     button.setSelected(true);
                     return;
                  }
               }
            }
        }
    }

    @Override
    protected void buildChooser(){
        setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
        // build the combo box.
        colorButtonGroup  = new ButtonGroup();
        for (int i = 0; i < numColors; i++) {
            addButton(colorButtonGroup,colorCode[i],Bundle.getMessage(colorText[i]));   
        }
    }

    private void addButton(ButtonGroup bg, Color color, String buttonLabel ) {
        JRadioButton button = new JRadioButton(buttonLabel);
        button.addActionListener( (ActionEvent e) -> {
            getColorSelectionModel().setSelectedColor(color);
        });
       bg.add(button);
       this.add(button); // add the button to the panel.
    }

    @Override
    public String getDisplayName() {
         return Bundle.getMessage("ButtonGroupColorChooserName");
    }

    @Override
    public Icon getSmallDisplayIcon(){
       return null;
    }

    @Override
    public Icon getLargeDisplayIcon(){
       return null;
    }
}
