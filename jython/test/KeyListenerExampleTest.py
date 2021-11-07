# test KeyListenerExample
#  Just starts one up to make sure of syntax, etc.

import java
import java.awt

if (not java.awt.GraphicsEnvironment.isHeadless()) :

    execfile("jython/KeyListenerExample.py")

    # close the window
    f.dispose()
