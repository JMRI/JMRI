# AutoDispatcher 2
#
#    This script provides full layout automation, using connectivity info
#    provided by Layout Editor panels.
#
# This file is part of JMRI.
#
# JMRI is free software; you can redistribute it and/or modify it under
# the terms of version 2 of the GNU General Public License as published
# by the Free Software Foundation. See the "COPYING" file for a copy
# of this license.
#
# JMRI is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# for more details.
#
# Author:  Giorgio Terdina copyright (c) 2009, 2010, 2011
#
# 2.01 beta - Fixed problem wih signalheads without UserName
# 2.02 beta - Added test for empty sections
# 2.03 beta - Added test for no valid section found
# 2.04 beta - Corrected removal of SignalMasts
# 2.05 beta - Reverted signals to red aspect as soon as train enters next block
# 2.06 beta - Added new methods (getScale, getDeceleration, etc.) for custom AE
# 2.07 beta - Corrected bug when minimum speed is selected in SignalType
# 2.08 beta - Removed default speed from Speeds window, since it was confusing
# 2.09 beta - Corrected problem when manually changing train's section
# 2.10 beta - Corrected initialization of allocationReady at train departure
# 2.11 beta - Removed bell chime for warnings printed before loading preferences
# 2.12 beta - Modified Signal Edit window to make it more understandable
# 2.13 beta - Compensated difference for emergency stop between Multimaus and other CS
# 2.14 beta - Fixed bug for unbalanced brackets in schedule
# 2.15 beta - Added detailed error messages for wrong schedule
# 2.16 beta - Fixed problem of $P stopping trains controlled by "Braker" engineer 
# 2.17 beta - Added possibility of separating schedule tokens with comas ","
# 2.18 beta - Added error message when destination is a transit-only section
# 2.19 beta - Set default engineer to Auto, when custom script is not found
# 2.20 beta - Added blinking of new flashingLunar signal aspect (since JMRI 2.7.8)
# 2.21 beta - Corrected problem with OFF:Fx block action
# 2.22 beta - Added test for wrong input in Locomotives window
# 2.23 beta - Added possibility of stopping trains at the beginning of sections
# 2.24 beta - Fixed hang-up when schedule alternative has only one section and its name is wrong
# 2.25 beta - Added error message for nested "[" in schedule
# 2.26 beta - Fixed problem with static variable in ADTrain addressed as self.variable
# 2.27 beta - Fixed direction in transit-only sections when dealing with reversing tracks
# 2.28 beta - Closed "Train Detail" window when "Apply" is clicked
# 2.29 beta - Corrected loop in match method caused when an unknown section name was found
# 2.30 beta - Modified "import" statements to reflect package structure of JMRI 2.9.x
# 2.31 beta - Unified versions for JMRI 2.8 and 2.9.x and initialized block tracking at startup
# 2.32 beta - Implementd "train start actions" and "AutoStart trains" option.
# 2.33 beta - Enabled pause/resume buttons while script being stopped.
# 2.34 beta - Updated blinkSignals as previous methods were deprecated (Greg).
# 2.35 - Added $IFH (if held) command in schedule.
# 2.35 - Added $TC and $TT (set turnout or accessory) commands in schedule.
# 2.35 - Added $ST (Start at fast clock time) command in schedule.
# 2.35 - Released throttle when train is in manual section.
# 2.35 - Added custom section RGB colors.
# 2.36 - Avoided duplicate operation of turnouts due to changes in Layout Editor.
# 2.37 - Added section tracking using JMRI memory variables.
# 2.38 - Modified to ignored Power OFF when using XpressNet Simulator (since it's unreliable).
# 2.39 - Removed Operations interface (nobody ever used it!)
# 2.40 - Fixed thread race condition when stopping trains
# 2.41 - Set default minimum interval between speed commands (maxIdle) to 60 seconds
# 2.42 - Adapted to refactoring occurred in JMRI 3.32 (different access to default directory)
# 2.43 - timeout and retry on acquisition failure, handle new syntax of LayoutEditor class
# 2.44 - refactor to work with the Java ScriptEngine - Randall Wood

from AutoDispatcher.AutoDispatcher2 import AutoDispatcher
           
a = AutoDispatcher()
a.setup()
