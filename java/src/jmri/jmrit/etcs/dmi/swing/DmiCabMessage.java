package jmri.jmrit.etcs.dmi.swing;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import jmri.jmrit.etcs.CabMessage;

import org.apiguardian.api.API;

/**
 * Class to represent a CabMessage received by the DMI.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class DmiCabMessage extends CabMessage {

    private final String[] msgArray;

    protected DmiCabMessage(CabMessage msg, Font font){
        super(msg.getMessageId(), msg.getMessage(), msg.getGroup(), msg.getAckRequired());
        msgArray = msgs(font );
    }

    protected String[] getMessageArray(){
        return java.util.Arrays.copyOf(msgArray, msgArray.length);
    }

    private String[] msgs( Font font){
        int maxWidth = 170; // Maximum width in pixels
        String[] result = splitString(getMessage(), maxWidth, font);
        log.debug("{} lines in message", result.length);
        return result;
    }

    private static String[] splitString(String input, int maxWidth, Font font) {
        List<String> substrings = new ArrayList<>();
        StringBuilder currentSubstring = new StringBuilder();
        String[] words = input.split(" "); // Split input into words

        for (String word : words) {
            if (currentSubstring.length() == 0) {
                currentSubstring.append(word);
            } else {
                String potentialSubstring = currentSubstring.toString() + " " + word;

                // Check if adding the word exceeds the maximum width
                if (getStringWidthInPx(potentialSubstring, font) <= maxWidth) {
                    currentSubstring.append(" ").append(word);
                } else {
                    // Current substring exceeds the maximum width, start a new substring
                    substrings.add(currentSubstring.toString());
                    currentSubstring = new StringBuilder(word);
                }
            }
        }

        if (currentSubstring.length() > 0) {
            substrings.add(currentSubstring.toString());
        }
        return substrings.toArray(String[]::new);
    }

    // Function to calculate the width of a string (in pixels)
    // values should be cached as this is not a fast method.
    private static int getStringWidthInPx(String input, Font font) {

        JFrame frame = new JFrame();
        JLabel label = new JLabel(input);
        label.setFont(font);
        frame.add(label);

        // Ensure the label is properly sized
        frame.pack();
        frame.setVisible(false);

        FontMetrics fontMetrics = label.getFontMetrics(font);
        int dd = fontMetrics.stringWidth(input);

        frame.dispose(); // Close the frame

        return dd;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiCabMessage.class);

}
