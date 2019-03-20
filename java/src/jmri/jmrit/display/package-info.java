/**
 * Provides control panel and associated visible icon classes.
 *
 * <h2>Related Documentation</h2>
 *
 * Several web pages discuss these classes:
 * <ul>
 *    <li><a href="http://jmri.org/Panels.html">The "Panels" page</a>
 * </ul>
 *
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML class diagram for package" height="33%" width="33%"></a>
 * <!-- Put @see and @since tags down here. -->
 *
 * @see jmri.jmrit.display.palette
 * @see jmri.jmrit.picker
 */
package jmri.jmrit.display;

/*
@startuml jmri/jmrit/display/doc-files/Heirarchy.png

interface Positionable
interface IndicatorTrack

Positionable <-- PositionableJComponent
Positionable <-- PositionableLabel
Positionable <-- PositionableJPanel
Positionable <-- PositionableIcon

Positionable <-- IndicatorTrack

javax.swing.JComponent <|-- PositionableJComponent
javax.swing.JLabel <|-- PositionableLabel
javax.swing.JPanel <|-- PositionableJPanel
javax.swing.JPanel <|-- IconAdder

PositionableLabel  <|-- PositionableIcon

PositionableJComponent <|--  PositionableShape

PositionableLabel  <|-- LightIcon
PositionableLabel  <|-- LocoIcon
PositionableLabel  <|-- MemoryIcon
PositionableLabel  <|-- MultiSensorIcon
PositionableLabel  <|-- ReporterIcon
PositionableLabel  <|-- SlipTurnoutIcon

PositionableIcon <|-- IndicatorTrackIcon
PositionableIcon <|-- SensorIcon
PositionableIcon <|-- SignalHeadIcon
PositionableIcon <|-- SignalMastIcon
PositionableIcon <|-- TurnoutIcon

@end
 */

