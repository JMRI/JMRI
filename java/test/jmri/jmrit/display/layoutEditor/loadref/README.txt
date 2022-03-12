This directory contains comparison files
for load/store testing. They are named the
same as the corresponding file in the 
"load" directory.

In general, the .xml files tagged with older version numbers 
have been run through the most recent version so they 
compare OK.  For example, load/Decorations-4-19-4.xml is
a 4.19.4 format file that we want to be sure to be able to read.
loadref/Decorations-4-19-4.xml (here) is the expected
output from running that through the most recent version
of the code. This means that these .xml files are 
updated as the code changes. Note that, because various substitutions can 
be made to simplify testing, these may not 
be schema-valid.

The .png files are meant to be invarient examples of the display 
from older versions.  Unlike the .xml files, we do not expect
much change in the display from old panel files (users don't like that).
So we keep these around for comparison with the contents of temp/temp after running

./runtest.csh java/test/jmri/jmrit/display/layoutEditor/LoadAndStoreTest

Because of font differences, the .png files are kept in macos and linux (and eventually windows)
subdirectories.

Made from 4.19.4 with macOS Mojave Java 1.8.0_151-b12

    12546 May 12 14:12 LayoutEditorTest-4-19-4.xml.1.png
    95307 May 12 14:12 Decorations-4-19-4.xml.1.png

Made from 4.19.5 with macOS Mojave Java 1.8.0_151-b12

    16400 May 12 12:57 ThreeWay-Error-4-19-5.xml.1.png

Made from 4.19.6 with macOS Mojave Java 1.8.0_151-b12

    29797 May 12 13:15 Decorations-4-19-6.xml
    95296 May 12 13:15 Decorations-4-19-6.xml.1.png
    23768 May 12 13:15 LayoutEditorTest-4-19-6.xml
    12546 May 12 13:15 LayoutEditorTest-4-19-6.xml.1.png
    10416 May 12 13:15 OneOfEach-4-19-6.xml
    43798 May 12 13:15 OneOfEach-4-19-6.xml.1.png
     9942 May 12 13:15 OneSiding-4-19-6.xml
    26025 May 12 13:15 OneSiding-4-19-6.xml.1.png
    38585 May 12 13:15 ThreeWay-Error-4-19-6.xml
    16400 May 12 13:15 ThreeWay-Error-4-19-6.xml.1.png

Also made with 4.19.6 as above, these are the references for older files (change is mostly enum as alpha name or signal order)
    37214 May 12 12:56 ThreeWay-Error-4-19-5.xml
    28598 May 12 14:12 Decorations-4-19-4.xml
    22843 May 12 14:12 LayoutEditorTest-4-19-4.xml

Made with 4.19.6++  (panels 3 and 4 are Control Panel Editor panels)
   18981 May 26 16:21 Sidlo-2020-04-Chotebor.xml.6.png
   36256 May 26 16:21 Sidlo-2020-04-Chotebor.xml.5.png
   32560 May 26 16:21 Sidlo-2020-04-Chotebor.xml.2.png
   43666 May 26 16:21 Sidlo-2020-04-Chotebor.xml.1.png
  541337 May 26 16:21 Sidlo-2020-04-Chotebor.xml
