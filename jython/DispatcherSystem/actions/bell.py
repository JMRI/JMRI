# Actions in this directory or in the user directory can be called before or during a dispatch
import java
import jmri

snd = jmri.jmrit.Sound("resources/sounds/Bell.wav")
snd.play()
