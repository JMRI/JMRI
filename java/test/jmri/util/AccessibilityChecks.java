package jmri.util;

import javax.annotation.Nonnull;
import java.awt.Container;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;

import org.junit.jupiter.api.Assertions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accessibility Tool for checking swing JFrames and JPanels.
 * <p>
 * Checks for JButton, JTextArea, JTextField components to
 * ensure they have accessible content for screen reading software.
 * 
 * @author Steve Young Copyright (C) 2022
 */
public class AccessibilityChecks {

    private final static boolean LOGSYSTEMOUT = Boolean.getBoolean("jmri.util.AccessibilityChecks.logToSystemOut"); // false unless set true

    private final static boolean WARNISSUES   = Boolean.getBoolean("jmri.util.AccessibilityChecks.warnOnIssue"); // false unless set true

    private final static boolean ASSERTFAIL   = Boolean.getBoolean("jmri.util.AccessibilityChecks.failOnIssue"); // false unless set true

    private final static boolean INCLUDELAF   = Boolean.getBoolean("jmri.util.AccessibilityChecks.includeLaf"); // false unless set true

    /**
     * Check a JPanel or Container for Accessibility issues.
     * <p>
     * Typical usage would be to pass a JPanel.
     * 
     * @param contentPane eg. JFrame.getContentPane() or a JPanel
     * @return Empty string if no errors, else String containing details.
     */
    @Nonnull
    public static String check( @Nonnull final Container contentPane) {
        return check(contentPane, false);
    }
    
    /**
     * Check a JPanel or Container for Accessibility issues.
     * <p>
     * Typical usage would be to pass a JPanel.
     * 
     * @param contentPane eg. JFrame.getContentPane() or a JPanel
     * @param failOnIssue set true to fail the unit test if any issue found.
     * @return Empty string if no errors, else String containing details.
     */
    @Nonnull
    public static String check( @Nonnull final Container contentPane, final boolean failOnIssue) {
        return feedBack(getSingleContentPaneList(contentPane), failOnIssue);
    }

    /**
     * Check a Frame for Accessibility issues.
     * <p>
     * Typical usage would be to pass a JFrame.
     * Searches the frame via 
     * getContentPane() , getLayeredPane(), getRootPane()
     * for issues.
     * 
     * @param frame a JFrame for which to search through.
     * @return Empty string if no errors, else String containing details.
     */
    @Nonnull
    public static String check(@Nonnull JFrame frame) {
        return check(frame, false);
    }

    /**
     * Check a Frame for Accessibility issues.
     * <p>
     * Typical usage would be to pass a JFrame.
     * Searches the frame via 
     * getContentPane() , getLayeredPane(), getRootPane()
     * for issues.
     * 
     * @param frame a JFrame for which to search through.
     * @param failOnIssue set true to fail the unit test if any issue found.
     * @return Empty string if no errors, else String containing details.
     */
    @Nonnull
    public static String check(@Nonnull JFrame frame, final boolean failOnIssue) {
        HashSet<JComponent> set = new HashSet<>();
        set.addAll(getSingleContentPaneList(frame.getContentPane()));
        set.addAll(getSingleContentPaneList(frame.getLayeredPane()));
        set.addAll(getSingleContentPaneList(frame.getRootPane()));
        return feedBack(set, failOnIssue);
    }

    private static Set<JComponent> getSingleContentPaneList(@Nonnull final Container contentPane){
        HashSet<JComponent> set = new HashSet<>();
        set.addAll(checkComponent(contentPane, JButton.class));
        set.addAll(checkComponent(contentPane, JTextArea.class));
        set.addAll(checkComponent(contentPane, JTextField.class));
        return set;
    }

    private static String feedBack(Set<JComponent> components, boolean forceFailOnIssue) {
        if (components.isEmpty()){
            return "";
        }
        String msg = getMessageString(components);
        if ( LOGSYSTEMOUT ) {
            System.out.println(msg);
        }
        if ( WARNISSUES ) {
            log.warn("{}",msg);
        }
        if ( ASSERTFAIL || forceFailOnIssue ) {
            Assertions.fail(msg);
        }
        return msg;
    }

    private static boolean includeComponent(JComponent component){
        if (!INCLUDELAF) {
            if ( component.getClass().getName().contains(".laf.") ) {
                return false;
            }
            if ( component.getClass().getName().contains(".plaf.") ) {
                return false;
            }
        }
        return true;
    }

    private static String getMessageString(Set<JComponent> components){
        StringBuilder sb = new StringBuilder();
        sb.append(components.size()).append(" Potential Issue(s) found. ");
        components.forEach(s -> {
            sb.append(System.getProperty("line.separator"));
            sb.append("No accessible Content for: ").append(s.getClass());
            if ( s.getName() != null ) {
                sb.append(" Name:").append(s.getName());
            }
            if ( s.getToolTipText() != null ) {
                sb.append(" ToolTip:").append(s.getToolTipText());
            }
        });
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }

    private static Set<JComponent> checkComponent( final Container container, final Class<? extends JComponent> componentType ) {
        Set<? extends JComponent> as = findComponents(container, componentType);
        HashSet<JComponent> list = new HashSet<>();
        as.forEach(s -> {
            String accessibleContent = s.getAccessibleContext().getAccessibleName();
            if (accessibleContent == null || accessibleContent.isEmpty()) {
                if (includeComponent(s)) {
                    list.add(s);
                }
            }
        });
        return list;
    }

    // recursive loop to find all components which match
    // is there a way of matching a set of JButton, JTextarea etc. into this ?
    private static <T extends JComponent> Set<T> findComponents( final Container container, final Class<T> componentType ) {
        return Stream.concat(
            Arrays.stream(container.getComponents())
                .filter(componentType::isInstance)
                .map(componentType::cast),
            Arrays.stream(container.getComponents())
                .filter(Container.class::isInstance)
                .map(Container.class::cast)
                .flatMap(c -> findComponents(c, componentType).stream())
        ).collect(Collectors.toSet());
    }

    private final static Logger log = LoggerFactory.getLogger(AccessibilityChecks.class);

}
