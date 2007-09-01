<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: panelfile.xsl,v 1.2 2007-09-01 05:19:22 jacobsen Exp $ -->

<!-- Stylesheet to convert a JMRI panel file into an HTML page -->

<!-- Used by default when the panel file is displayed in a web browser-->

<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>


<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='layout-config'>

<html>
	<head>
		<title>JMRI panel file</title>
	</head>
	
	<body>
		<h2>JMRI panel file</h2>

                <xsl:apply-templates/>

	<hr/>
	This page produced by the 
	<A HREF="http://jmri.sf.net">JMRI project</A>.
	<A href="http://sourceforge.net"> 
	<IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A>

	</body>
</html>

</xsl:template>

<!-- Index through turnouts elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/turnouts">
<h3>Turnouts</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal turnout elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<xsl:template match="turnout">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<!-- Index through signalheads elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/signalheads">
<h3>Signal Heads</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal signalhead elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through sensors elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/sensors">
<h3>Sensors</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal sensor elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through memories elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/memories">
<h3>Memories</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal memory elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through reporters elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/reporters">
<h3>Reporters</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal reporter elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through routes elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/routes">
<h3>Routes</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal route elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through logixs elements -->
<!-- each one becomes a separate section -->
<xsl:template match="layout-config/logixs/logix">
<h3>Logix <xsl:value-of select="@systemName"/>
<xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
</h3>
    <!-- index through individual logix elements -->
        <xsl:call-template name="oneLogix"/>
</xsl:template>

<!-- Index through blocks (SSL) elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/blocks">
<h3>Blocks</h3>
    <table border="1">
    <tr><td>Controlled Signal</td><td>Watched Turnout</td></tr>
    <!-- index through individal turnout elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<xsl:template match="turnout">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="signalhead">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="sensor">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="memory">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="reporter">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="route">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="logix">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="block">
<tr><td><xsl:value-of select="@signal"/></td><td><xsl:value-of select="@watchedturnout"/></td></tr>
</xsl:template>

<!-- conditionals are not directly displayed -->
<xsl:template match="conditional">
</xsl:template>

<!-- template to show a particular logix -->
<xsl:template name="oneLogix">
    <!-- index is at the logix element here -->
    <xsl:for-each select="logixConditional">
        <xsl:call-template name="oneConditional">
                <xsl:with-param name="name" select="@systemName"/>
        </xsl:call-template>
    </xsl:for-each>
</xsl:template>

<!-- template to show a particular conditional -->
<xsl:template name="oneConditional">
        <xsl:param name="name"/>
    <!-- index through individual conditional elements, looking for match -->
    <xsl:for-each select="/layout-config/conditionals/conditional">
		<xsl:if test="( @systemName = $name )" >
		<!-- here have found correct conditional -->
            <h4>Conditional <xsl:value-of select="@systemName"/>
            <xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
            </h4>
		    <xsl:for-each select="conditionalStateVariable">
		        <xsl:call-template name="conditionalStateVariable"/>
		    </xsl:for-each>
		    <xsl:for-each select="conditionalAction">
		        <xsl:call-template name="conditionalAction"/>
		    </xsl:for-each>
        </xsl:if>        
    </xsl:for-each>
</xsl:template>

<xsl:template name="conditionalStateVariable">
State: 
operator="<xsl:value-of select="@operator"/>"
type="<xsl:value-of select="@type"/>"
systemName="<xsl:value-of select="@systemName"/>"
num1="<xsl:value-of select="@num1"/>"
num2="<xsl:value-of select="@num2"/>"
triggerCalc="<xsl:value-of select="@triggerCalc"/>"
<br/>
</xsl:template>

<xsl:template name="conditionalAction">
Action:
option="<xsl:value-of select="@option"/>"
delay="<xsl:value-of select="@delay"/>"
type="<xsl:value-of select="@type"/>"
systemName="<xsl:value-of select="@systemName"/>"
data="<xsl:value-of select="@data"/>"
string="<xsl:value-of select="@string"/>"
<br/>
</xsl:template>

<xsl:template match="paneleditor">
<h3>Panel: <xsl:value-of select="@name"/>
</h3>
</xsl:template>


</xsl:stylesheet>
