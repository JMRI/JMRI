<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="en">

<head>
 <title>JMRI: Most Recent Release Note</title>
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
<META 
     HTTP-EQUIV="Refresh"
     CONTENT="0; URL=<?php
     
include_once "parseRelease.php";
print latestFilename();

?>">
</head>
<body>
This page should have immediately taken you to the
release note for the latest (in progress) JMRI test release.
<p>
If it hasn't, please 
<a href="<?php
include_once "parseRelease.php";
print latestFilename();
?>">click here</a>.

</body>
</html>

