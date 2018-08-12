package jmri.util.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.Icon;
import javax.swing.JComboBox;

/**
 * Abstract Color Chooser extension that presents the JMRI preset colors in
 * a Combo Box with proper internationalization.
 *
 * @author Paul Bender Copyright (C) 2017
 * @since 4.9.6
 */
public class ComboBoxColorChooserPanel extends AbstractColorChooserPanel {

    private String[] colorText = {"Black", "DarkGray", "Gray",
       "LightGray", "White", "Red", "Pink", "Orange",
       "Yellow", "Green", "Blue", "Magenta", "Cyan"};    //NOI18N
    private Color[] colorCode = {Color.black, Color.darkGray, Color.gray,
       Color.lightGray, Color.white, Color.red, Color.pink, Color.orange,
       Color.yellow, Color.green, Color.blue, Color.magenta, Color.cyan};
    private int numColors = 13; //number of entries in the above arrays
    private JComboBox<String> colorCombo = null;

    @Override
    public void updateChooser(){
        Color color = getColorFromModel();
        // update the combo box to have the right color showing.
        for(int i = 0;i< numColors;i++){
            if(color.equals(colorCode[i])){
               colorCombo.setSelectedIndex(i);
               return;
            }
        }
    }

    @Override
    protected void buildChooser(){
        // remove everything from the chooser panel
        removeAll();
        // build the combo box.
        colorCombo = new JComboBox<String>();
        for (int i = 0; i < numColors; i++) {
            colorCombo.addItem(Bundle.getMessage(colorText[i]));   
        }
        colorCombo.setMaximumRowCount(numColors);
        colorCombo.addActionListener( (ActionEvent e) -> {
            int index = colorCombo.getSelectedIndex();
            getColorSelectionModel().setSelectedColor(colorCode[index]);
        });
        add(colorCombo);
    }

    @Override
    public String getDisplayName() {
         return Bundle.getMessage("ComboBoxColorChooserName");
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
