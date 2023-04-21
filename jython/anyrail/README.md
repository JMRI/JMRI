The AnyRail program can export a track plan to a JMRI PanelPro XML file with a Layout Editor formatted Panel.

This script will create a new occupancy Sensor for each exported Block.

For more details, see the [PDF file](http://jmri.org/community/connections/AnyRail/AnyRailBuildBlockSensorList.pdf) on the JMRI web site.

Each time it becomes necessary to modify the AnyRail track plan and export a new version of the PanelPro file, the user is forced to rebuild the list of Sensors that define the related Occupancy status for each Block. After many such revisions and the resultant efforts on a large club layout, the painful need for a script has led to what you are reading now.

This script automates what would be a tedious and error prone process when done manually. It does however, require careful attention to the AnyRail track plan details.

This script is intended be executed EXACTLY ONCE on each NEWLY EXPORTED PanelPro XML file.

Version 2023-04-19:
Essentially rewritten with augmented comments

Avoid potential cross-thread contamination and resulting crashes by minimizing Table windows that get updated as Sensors are created and Blocks are edited. If any Table windows are minimized by the script, they are restored at the end of the execution.

Cope with Blocks exported from unnamed Sections and/or duplicated AnyRail Section names. Identified for track plan updates and later retries.

Cope with Blocks exported with Section names containing one or more Low Line characters AKA the "_" character. Turned out to be troublesome.

Report as Questionable any exported Blocks with Section names containing only WhiteSpace characters.

Extensive print output to the PanelPro Script Output window.

Tested with AnyRail Version 6.51.0
