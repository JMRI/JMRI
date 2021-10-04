<!DOCTYPE html>
<html>
<head>
	<title>JMRI AAR Graphical Symbols</title>
</head>
<body>
<h1>JMRI AAR Graphical Symbols</h1>


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
