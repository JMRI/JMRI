# This is an example of running a command from JMRI scripting and getting a result back:

import java

cmd = "ls"

process = java.lang.Runtime.getRuntime().exec(cmd)
inputStream = process.getInputStream()
result = java.io.BufferedReader(java.io.InputStreamReader(inputStream)).lines().collect(java.util.stream.Collectors.joining("\n"));

print result

# You can replace that “ls” with whatever command you want to execute.
# The `result` variable contains the standard output from the command as a string
# `process.getErrorStream()`` will get the error output from the command
