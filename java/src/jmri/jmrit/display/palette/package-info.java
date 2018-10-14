/**
 * Select from palette of items
 *
 * <h2>Related Documentation</h2>
 *
 *
 * <a href="doc-files/Heirarchy.png"><img src="doc-files/Heirarchy.png" alt="UML State diagram" height="33%" width="33%"></a>
 * <!-- Put @see and @since tags down here. -->
 *
 * @see jmri.jmrit.display
 * @see jmri.jmrit.picker
 */
package jmri.jmrit.display.palette;

/*
@startuml jmri/jmrit/display/palette/doc-files/Heirarchy.png

javax.swing.JPanel <|-- ItemPanel

ItemPanel <|-- FamilyItemPanel
ItemPanel <|-- IconItemPanel
ItemPanel <|-- TextItemPanel

IconItemPanel <|-- BackgroundItemPanel
IconItemPanel <|-- ClockItemPanel

FamilyItemPanel <|-- IndicatorItemPanel
FamilyItemPanel <|-- PortalItemPanel
FamilyItemPanel <|-- RPSItemPanel
FamilyItemPanel <|-- "TableItemPanel<E>"

"TableItemPanel<E>" <|-- IndicatorTOItemPanel
"TableItemPanel<E>" <|-- MemoryItemPanel
"TableItemPanel<E>" <|-- MultiSensorItemPanel
"TableItemPanel<E>" <|-- ReporterItemPanel
"TableItemPanel<E>" <|-- SignalHeadItemPanel
"TableItemPanel<E>" <|-- SignalMastItemPanel
@end
 */

