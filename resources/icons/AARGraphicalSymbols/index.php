<!DOCTYPE html>
<html>
<head>
	<title>JMRI AAR Graphical Symbols</title>
</head>
<body>
<h1>JMRI AAR Graphical Symbols</h1>

These are from "American Railway Signaling Principles and Practices Chapter II. By the Signal Section, A.A.R. Revised August 1946. Figure 1 Graphical Symbols Wayside Signal Operating Characteristics ARA Sig.Sec. 1660A."

<?php
// find showIcons directory
$name = getcwd();
$n = strrpos($name, "/resources");

require(substr($name,0,$n+strlen("/resources"))."/showIcons.php");

showSubdirs();
showFiles();

?>
</table>


</body>
</html>
