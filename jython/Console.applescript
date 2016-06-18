-- Open the current JMRI session log in the OS X Console.app

-- This can be invoked within JMRI on OS X using any method used to invoke
-- Python scripts or can be invoked from the OS X Script Editor
tell application "Finder"
	set JMRI to folder "JMRI" in folder "Preferences" in folder "Library" in home
	open file "session.log" in folder "log" in JMRI
end tell