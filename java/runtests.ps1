<#
.SYNOPSIS
  Launch a subset of unit tests.
.DESCRIPTION
  This script launches all unit tests in the specified package and subpackages.
  Additionally, a specific test class name can be given. The test class name
  must exist in the given package or one of its subpackages.

  This script expects your working directory to be the root of the project.

  Maven (mvn) must be in system path for this script to work properly.
.PARAMETER Package
  JAVA package to look for tests in.
.PARAMETER TestClass
  Class name for a specific test to run.
.PARAMETER Clean
  If true, perform a 'mvn clean' before running the tests. Defaults to false.
.EXAMPLE
  . runtests.ps1 -Package jmri.jmrix -Clean:$true
.EXAMPLE
  . runtests.ps1 -Package jmri.jmrix 
.EXAMPLE
  . runtests.ps1 -Package jmri.jmrix -TestClass IpocsLightManagerTest
.NOTES
  Author: Fredrik Elestedt
  Date:   2020-09-12
#>
param(
  [Parameter(HelpMessage="JAVA package to look for tests in")]
  [string]$Package,
  [Parameter(HelpMessage="Class name for a specific test to run")]
  [string]$TestClass,
  [Parameter(HelpMessage="Perform a 'mvn clean' before running the tests")]
  [switch]$Clean = $FALSE
)

$subPath = $package.Replace(".", "/");
$subPath = "java/test/" + $subPath + "/";
$workingDir = (Get-Location).Path

$argumentHash = @{
  Path = $subPath
  Include = "*Test.java"
}
if ($TestClass.Length) {
  Write-Host "Parameter set"
  $argumentHash.Include = $TestClass + ".java"
}
$Children = (Get-ChildItem -Recurse @argumentHash)
$tests = "";
ForEach ($i in $Children) {
  $package = $i.FullName.Substring($workingDir.Length + 1 + "java/test/".Length);
  $package = $package.Replace("\", ".");
  $package = $package.Replace(".java", "");
  $tests += ",$package";
}
if ($tests.Length) {
  $tests = $tests.Substring(1);
}
if ($Clean -eq $TRUE) {
  Write-Host "Cleaning..."
  . mvn clean compile test -DskipTests
}
Write-Host "Running tests..."
. mvn -Dtest="$tests" test jacoco:report