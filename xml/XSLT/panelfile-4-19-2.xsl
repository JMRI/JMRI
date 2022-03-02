<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI panel file into an HTML page              -->
<!-- Used by default when the panel file is displayed in a web browser      -->
<!-- This version corresponds to the 4.19.2 schema update                   -->

<!-- Updates made for 4.19.2 schema and additional JMRI capabilities:
   [Added formatting for logixNG and ctcdata]                   (2022-02-18)
   [Reformatting was done to logix display]                     (2022-02-18)
   [Added info for turnout, sensors, lights, signal masts.]     (2022-03-01)
   [Added additional directions for blocks.]                    (2022-03-01)
   [Added info for ctc code buttons.]                           (2022-03-01)
   [Added formatting for panel editor and layout editor.]       (2022-03-01)
   [Bug fix in SSL related to displaying sensor names.]         (2022-03-01)
   [Changed some text alignment to center for readability]      (2022-03-02)
-->

<!-- This file is part of JMRI.  Copyright 2007-2011, 2016, 2018, 2022.     -->

<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->
<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2022')" />

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>


<!-- Define variables for translation -->
<xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
<xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

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
		<title>JMRI Panel File Contents</title>
	</head>

	<body>
		<h1>JMRI Panel File Contents</h1>
        <h4>[Not all detailed attributes are displayed.  Please check the underlying XML file.]<br/>
        [Help maintain this JMRI capability: Please report any unformatted data to the JMRI Development Team.]</h4>
                <xsl:apply-templates/>

        <p></p>
<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
	</body>
</html>

</xsl:template>

<!-- ***** Helper Functions ****************************************************************** -->
<xsl:template name="substring-after-last">
    <xsl:param name="string" />
    <xsl:param name="delimiter" />
    <xsl:choose>
      <xsl:when test="contains($string, $delimiter)">
        <xsl:call-template name="substring-after-last">
          <xsl:with-param name="string"
            select="substring-after($string, $delimiter)" />
          <xsl:with-param name="delimiter" select="$delimiter" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of
                  select="$string" /></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through turnouts types -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/turnouts">
<h2>Turnouts (<xsl:call-template name="substring-after-last">
  <xsl:with-param name="string" select="substring-before(@class,'ManagerXml')" /><xsl:with-param name="delimiter" select="'.'" />
  </xsl:call-template>
)</h2>
     <table border="1">
     <xsl:for-each select="operations">
       <tr><td>Operations automate:</td><td style="text-align:center"><xsl:value-of select="@automate" /></td></tr>
     </xsl:for-each>
     <xsl:if test='defaultclosedspeed !="" or defaultthrownspeed !=""'>
       <tr><td>Default closed speed:</td><td style="text-align:center"><xsl:value-of select="defaultclosedspeed" /></td></tr>
       <tr><td>Default thrown speed:</td><td style="text-align:center"><xsl:value-of select="defaultthrownspeed" /></td></tr>
     </xsl:if>
     </table>
     <br/>

    <table border="1">
    <tr>
      <th>System Name</th>
      <th>User Name</th>
      <th>Fdbk?</th>
      <th>Inv?</th>
      <th>Lckd?</th>
      <th>Auto?</th>
      <th>Comment</th>
    </tr>
    <!-- index through individual turnout elements -->
    <xsl:apply-templates select="turnout"/>
    </table>
</xsl:template>

<xsl:template match="operations">
</xsl:template>
<xsl:template match="defaultclosedspeed">
</xsl:template>
<xsl:template match="defaultthrownspeed">
</xsl:template>


<!-- *************************************************************************************** -->
<!-- Index through lights elements -->
<!-- each one becomes a table      -->
<xsl:template match="layout-config/lights">
    <h2>Lights (<xsl:call-template name="substring-after-last">
  <xsl:with-param name="string" select="substring-before(@class,'ManagerXml')" /><xsl:with-param name="delimiter" select="'.'" />
  </xsl:call-template>
)</h2>
    <table border="1">
        <tr>
            <th>System Name</th>
            <th>User Name</th>
            <th>Comment</th>
        </tr>
        <!-- index through individual light elements -->
        <xsl:apply-templates select="light"/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through signalheads elements -->
<!-- each one becomes a table           -->
<xsl:template match="layout-config/signalheads">
<h2>Signal Heads</h2>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th><th>Type</th><th>Output</th><th>Comment</th></tr>
    <!-- index through individual signalhead elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through signalmasts elements -->
<!-- each one becomes a table           -->
<xsl:template match="layout-config/signalmasts">
    <h2>Signal Masts (<xsl:call-template name="substring-after-last">
        <xsl:with-param name="string" select="substring-before(@class,'ManagerXml')" /><xsl:with-param name="delimiter" select="'.'" />
        </xsl:call-template>
     )</h2>
    <table border="1">
      <tr><th>System Name</th><th>User Name</th><th>Type</th><th>Can Be<br/>Unlit</th><th>Disabled<br/>Aspect</th><th>Comment</th><th>Output</th><th>Aspect<br/>Settings</th></tr>
      <!-- index through individual signal mast elements/classes, see below) -->
        <!--update for new types/elements-->
      <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through signalgroups elements -->
<!-- each one becomes a table            -->
<xsl:template match="layout-config/signalgroups">
    <h2>Signal Groups</h2>
    <table border="1">
        <tr><th>System Name</th><th>User Name</th><th>Master</th><th>Members</th><th>Comment</th></tr>
        <!-- index through individual signalgroup elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through signalmastlogics elements -->
<!-- each one becomes a separate table       -->
<xsl:template match="layout-config/signalmastlogics">
<h2>Signal Mast Logics</h2>
Logic delay: <xsl:value-of select="logicDelay"/> ms<br/>
    <!-- index through individual signalmastlogic elements -->
    <xsl:call-template name="oneSML"/>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- template to show a particular signalmastlogic -->
<xsl:template name="oneSML"> <!--1 table per SML-->
    <!-- index through individual signalmastlogic elements -->
    <xsl:for-each select="signalmastlogic">
        <!--table header-->
        <h3>Source mast: <xsl:value-of select="sourceSignalMast"/></h3>
        <table border="1">
            <tr><th>Destination Mast</th><th>Turnouts</th><th>Sensors</th><th>Enabled</th><th>Use LE</th><th>Comment</th></tr>
            <xsl:for-each select="destinationMast">
                <tr> <!--1 row per destination mast-->
                    <td><xsl:value-of select="destinationSignalMast"/></td>
                    <td>
                        <xsl:for-each select="turnouts/turnout">
                            <xsl:value-of select="turnoutName"/>: <xsl:value-of select="turnoutState"/><br/>
                        </xsl:for-each>
                    </td>
                    <td>
                        <xsl:for-each select="sensors/sensor">
                            <xsl:value-of select="sensorName"/>: <xsl:value-of select="sensorState"/><br/>
                        </xsl:for-each>
                    </td>
                    <td style="text-align:center"><xsl:value-of select="enabled"/></td>
                    <td style="text-align:center"><xsl:value-of select="useLayoutEditor"/></td>
                    <td style="text-align:center"><xsl:value-of select="comment"/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:for-each>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through sensors elements -->
<!-- each one becomes a table       -->
<xsl:template match="layout-config/sensors">
<h2>Sensors (<xsl:call-template name="substring-after-last">
  <xsl:with-param name="string" select="substring-before(@class,'ManagerXml')" /><xsl:with-param name="delimiter" select="'.'" />
  </xsl:call-template>
)</h2>
    Default sensor state: <xsl:value-of select="@defaultInitialState"/>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th><th>Inv?</th><th>Comment</th></tr>
    <!-- index through individual sensor elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through memories elements -->
<!-- each one becomes a table        -->
<xsl:template match="layout-config/memories">
<h2>Memory Variables</h2>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th><th>Comment</th><th>Value</th></tr>
    <!-- index through individual memory elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through reporters elements -->
<!-- each one becomes a table         -->
<xsl:template match="layout-config/reporters">
<h2>Reporters</h2>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th><th>Comment</th></tr>
    <!-- index through individual reporter elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through routes elements -->
<!-- each one becomes a table      -->
<xsl:template match="layout-config/routes">
<h2>Routes</h2>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th>
    <th>In Sensors</th><th>In Turnouts</th>
    <th>Out Turnouts</th>
    <th>Out Sensors</th>
    <th>Comment</th>
    </tr>
    <!-- index through individual route elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through layoutblocks elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/layoutblocks">
    <h2>Layout Blocks</h2>
    <table border="1">
        <tr>
            <th>System Name</th>
            <th>User Name</th>
            <th>Occupancy Sensor</th>
            <th>Memory</th>
            <th>Occupied<br/>Sense</th>
            <th>Track<br/>Color</th>
            <th>Occupied<br/>Color</th>
            <th>Extra<br/>Color</th>
        </tr>
        <!-- index through individual turnout elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through oblocks (occupancy blocks) elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/oblocks">
    <h2>Occupancy Blocks</h2>
    <table border="1">
        <tr>
            <th>System Name</th>
            <th>User Name</th>
            <th>Portals</th>
            <th>Length</th>
            <th>Curve</th>
            <th>Permiss.</th>
        </tr>
        <!-- index through individual turnout elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through sections elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/sections">
    <h2>Sections</h2>
    <table border="1">
        <tr><th>System Name</th><th>User Name</th><th>Entry (order)</th><th>Exit</th><th>Comment</th></tr>
        <!-- index through individual section elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="layout-config/blocks">
    <xsl:if test="@class = 'jmri.jmrit.blockboss.configurexml.BlockBossLogicXml'" > <!-- old top element for SSL -->
        <xsl:call-template name="signalelements"/>
    </xsl:if>

    <xsl:if test="@class = 'jmri.configurexml.BlockManagerXml'" >
        <!-- Index through blocks elements -->
        <!-- each one becomes a table -->
        <h2>Blocks</h2>
            <table border="1">
            <tr>
                <th>System Name</th>
                <th>User Name</th>
                <th>Sensor</th>
                <th>Paths</th>
                <th>Length</th>
                <th>Permissive</th>
            </tr>
            <!-- index through individual block elements -->
            <xsl:for-each select="block">
                <!-- blocks are written out twice, once with 'permissive' and once without, keep the one with -->
                <xsl:if test="permissive != '' ">
                <tr><td>
                        <xsl:element name="a"><xsl:attribute name="id">Block-<xsl:value-of select="@systemName"/></xsl:attribute></xsl:element>
                        <xsl:value-of select="systemName"/></td>
                    <td><xsl:value-of select="userName"/></td>
                    <td>
                        <xsl:for-each select="sensor"><!-- is this clause actually necessary? -->
                        <xsl:value-of select="@systemName"/><br/>
                        </xsl:for-each>

                        <xsl:for-each select="occupancysensor">
                        <xsl:value-of select="."/><br/>
                        </xsl:for-each>
                        </td>
                    <td><table><xsl:for-each select="path"> <!-- nested table to align spacing, intentionally no border -->
                        <tr>
                            <td><xsl:choose>
                                <xsl:when test="( @todir = 16 )" >North</xsl:when>
                                <xsl:when test="( @todir = 32 )" >South</xsl:when>
                                <xsl:when test="( @todir = 64 )" >East</xsl:when>
                                <xsl:when test="( @todir = 80 )" >Northeast</xsl:when>
                                <xsl:when test="( @todir = 96 )" >Southeast</xsl:when>
                                <xsl:when test="( @todir = 128 )" >West</xsl:when>
                                <xsl:when test="( @todir = 144 )" >Northwest</xsl:when>
                                <xsl:when test="( @todir = 160 )" >Southwest</xsl:when>
                                <xsl:when test="( @todir = 256 )" >CW</xsl:when>
                                <xsl:when test="( @todir = 512 )" >CCW</xsl:when>
                                <xsl:when test="( @todir = 1024 )" >Left</xsl:when>
                                <xsl:when test="( @todir = 2048 )" >Right</xsl:when>
                                <xsl:when test="( @todir = 4096 )" >Up</xsl:when>
                                <xsl:when test="( @todir = 8192 )" >Down</xsl:when>
                                <xsl:otherwise><xsl:value-of select="@todir"/></xsl:otherwise>
                                </xsl:choose></td>
                            <td>to
                                <xsl:element name="a"><xsl:attribute name="href">#Block-<xsl:value-of select="@block"/></xsl:attribute><xsl:value-of select="@block"/></xsl:element>,
                                </td>
                            <td><xsl:choose>
                                <xsl:when test="( @fromdir = 16 )" >North</xsl:when>
                                <xsl:when test="( @fromdir = 32 )" >South</xsl:when>
                                <xsl:when test="( @fromdir = 64 )" >East</xsl:when>
                                <xsl:when test="( @fromdir = 80 )" >Northeast</xsl:when>
                                <xsl:when test="( @fromdir = 96 )" >Southeast</xsl:when>
                                <xsl:when test="( @fromdir = 128 )" >West</xsl:when>
                                <xsl:when test="( @fromdir = 144 )" >Northwest</xsl:when>
                                <xsl:when test="( @fromdir = 160 )" >Southwest</xsl:when>
                                <xsl:when test="( @fromdir = 256 )" >CW</xsl:when>
                                <xsl:when test="( @fromdir = 512 )" >CCW</xsl:when>
                                <xsl:when test="( @fromdir = 1024 )" >Left</xsl:when>
                                <xsl:when test="( @fromdir = 2048 )" >Right</xsl:when>
                                <xsl:when test="( @fromdir = 4096 )" >Up</xsl:when>
                                <xsl:when test="( @fromdir = 8192 )" >Down</xsl:when>
                                <xsl:otherwise><xsl:value-of select="@fromdir"/></xsl:otherwise>
                                </xsl:choose></td>
                            <td>from</td>
                            <xsl:for-each select="beansetting">
                                <td>; when <xsl:value-of select="turnout/@systemName"/>
                                <xsl:choose>
                                <xsl:when test="( @setting = 2 )" > is Closed</xsl:when>
                                <xsl:when test="( @setting = 4 )" > is Thrown</xsl:when>
                                <xsl:otherwise> is <xsl:value-of select="@setting"/></xsl:otherwise>
                                </xsl:choose>
                                </td>
                                </xsl:for-each>
                        </tr>
                        </xsl:for-each></table></td>
                    <td><xsl:value-of select="@length"/></td>
                    <td style="text-align:center"><xsl:value-of select="permissive"/></td>
                </tr>
                </xsl:if>
            </xsl:for-each>
            </table>
    </xsl:if>
</xsl:template>



<!-- *************************************************************************************** -->
<!-- Index through ctcdata elements -->
<xsl:template match="layout-config/ctcdata">
    <h2>CTC Data</h2>
    <h3>CTC Properties</h3>
    <table border="1">
        <tr>
            <th>Type</th>
            <th>Value</th>
        </tr>
    <!-- index through individual elements -->
    <xsl:for-each select="./ctcProperties/*">
        <tr><td><xsl:value-of select="local-name()"/></td>
            <td><xsl:value-of select="."/></td>
        </tr>
    </xsl:for-each>
    </table>
    <h3>CTC Other Data</h3>
    <table border="1">
        <tr>
            <th>Type</th>
            <th>Value</th>
        </tr>
    <!-- index through individual elements -->
    <xsl:for-each select="./ctcOtherData/*">
        <tr><td><xsl:value-of select="local-name()"/></td>
            <td><xsl:value-of select="."/></td>
        </tr>
    </xsl:for-each>
    </table>
    <h3>CTC Code Buttons</h3>
    <table border="1">
        <tr>
            <th>Switch<br/>Number</th>
            <th>Signal Etc<br/>Number</th>
            <th>GUI Column<br/>Number</th>
            <th>Code Button<br/>Internal Sensor</th>
            <th>OS Section Occupied<br/>External Sensor</th>
        </tr>
    <!-- index through individual elements -->
       <xsl:for-each select="./ctcCodeButtonData">
        <tr><td style="text-align:center"><xsl:value-of select="./SwitchNumber"/></td>
            <td style="text-align:center"><xsl:value-of select="./SignalEtcNumber"/></td>
            <td style="text-align:center"><xsl:value-of select="./GUIColumnNumber"/></td>
            <td><xsl:value-of select="./CodeButtonInternalSensor"/></td>
            <td><xsl:value-of select="./OSSectionOccupiedExternalSensor"/></td>
        </tr>
    </xsl:for-each>
     </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through warrants elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/warrants">
    <h2>Warrants</h2>
    Settings:
    <!--Haltstart = <xsl:value-of select="nxparams/haltstart"/>-->
    <!--Max.speed = <xsl:value-of select="nxparams/maxspeed"/>-->
    <table border="1">
        <tr><th>System Name</th><th>User Name</th><th>Block Order</th></tr>
        <!-- index through individual warrant elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through audio elements -->
<!-- each one becomes a table           -->
<xsl:template match="layout-config/audio">
    <h2>Audio</h2>
    <table border="1">
        <tr><th>Class</th><th>System Name</th><th>User Name</th><th>Type</th><th>URL</th><th>Comment</th></tr>
        <!-- index through individual audio elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>


<!-- *************************************************************************************** -->
<!-- Index through logixs elements       -->
<!-- each one becomes a separate section -->
<xsl:template match="layout-config/logixs/logix">
<h2>Logix <xsl:value-of select="systemName"/> <!--names as attributes deprecated since 2.9.6-->
<xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
<xsl:if test="( @enabled = 'yes' )"> [Enabled] </xsl:if>
<xsl:if test="( @enabled = 'no'  )"> [NOT Enabled]</xsl:if>
</h2>
<h4><xsl:if test="string-length(comment) !=0" > [<xsl:value-of select="comment"/>]</xsl:if>
</h4>
    <!-- index through individual logix elements -->
        <xsl:call-template name="oneLogix"/>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- template to show a particular logix -->
<xsl:template name="oneLogix">
    <!-- index is at the logix element here -->
    <xsl:for-each select="logixConditional">
        <xsl:call-template name="oneConditional">
                <xsl:with-param name="name" select="@systemName"/>
        </xsl:call-template>
    </xsl:for-each>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- template to show a particular conditional -->
<xsl:template name="oneConditional">
        <xsl:param name="name"/>
    <!-- index through individual conditional elements, looking for match -->
    <xsl:for-each select="/layout-config/conditionals/conditional">
            <xsl:if test='( @systemName = $name )' >
	    <!-- here have found correct conditional -->
            <h3>Conditional <xsl:value-of select="@systemName"/>
            <xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
            </h3>
        <!-- index through individual state variables -->
    <table border="1">
        <tr>
            <th>Operator</th>
            <th>Type</th>
            <th>Negated</th>
            <th>System/User Name</th>
            <th>Num1</th>
            <th>Num2</th>
            <th>Data String</th>
            <th>Trigger Calc</th>
         </tr>
		    <xsl:for-each select="conditionalStateVariable">
		        <xsl:call-template name="conditionalStateVariable"/>
		    </xsl:for-each>
    </table>
    <p/>
        <!-- index through individual actions -->
    <table border="1">
        <tr>
            <th>Change Option</th>
            <th>Action</th>
        </tr>
		    <xsl:for-each select="conditionalAction">
		        <xsl:call-template name="conditionalAction"/>
		    </xsl:for-each>
    </table>
    <p/>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template name="conditionalStateVariable">

<tr>
<!-- decode operator -->
<td>
<xsl:choose>
  <xsl:when test="( @operator = 1 )" >And </xsl:when>
  <xsl:when test="( @operator = 2 )" >Not </xsl:when>
  <xsl:when test="( @operator = 3 )" >And not </xsl:when>
  <xsl:when test="( @operator = 4 )" ></xsl:when>  <!-- None -->
  <xsl:when test="( @operator = 5 )" >Or</xsl:when>
  <xsl:otherwise><xsl:value-of select="@operator"/></xsl:otherwise>
</xsl:choose>
</td>
<!-- decode type -->
<td style="text-align:center">
<xsl:choose>
  <xsl:when test="( @type = 1 )" >Sensor Active</xsl:when>
  <xsl:when test="( @type = 2 )" >Sensor Inactive</xsl:when>
  <xsl:when test="( @type = 3 )" >Turnout Thrown</xsl:when>
  <xsl:when test="( @type = 4 )" >Turnout Closed</xsl:when>
  <xsl:when test="( @type = 5 )" >Conditional True</xsl:when>
  <xsl:when test="( @type = 6 )" >Conditional False</xsl:when>
  <xsl:when test="( @type = 7 )" >Light On</xsl:when>
  <xsl:when test="( @type = 8 )" >Light Off</xsl:when>
  <xsl:when test="( @type = 9 )" >Memory Equal</xsl:when>
  <xsl:when test="( @type = 10 )" >Fast Clock Range"</xsl:when>
  <xsl:when test="( @type = 11 )" >Signal Red</xsl:when>
  <xsl:when test="( @type = 12 )" >Signal Yellow</xsl:when>
  <xsl:when test="( @type = 13 )" >Signal Green</xsl:when>
  <xsl:when test="( @type = 14 )" >SIgnal Dark</xsl:when>
  <xsl:when test="( @type = 15 )" >Signal Flashing Red</xsl:when>
  <xsl:when test="( @type = 16 )" >Signal Flashing Yellow</xsl:when>
  <xsl:when test="( @type = 17 )" >Signal Flashing Green</xsl:when>
  <xsl:when test="( @type = 18 )" >Signal Head Lit</xsl:when>
  <xsl:when test="( @type = 19 )" >Signal Head Held</xsl:when>
  <xsl:when test="( @type = 20 )" >Signal Memory Compare</xsl:when>
  <xsl:when test="( @type = 21 )" >Signal Head Lunar</xsl:when>
  <xsl:when test="( @type = 22 )" >Signal Head Flashing Lunar</xsl:when>
  <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
</xsl:choose>
</td>
<td style="text-align:center">
<xsl:if test='@negated = "yes"'>Yes</xsl:if>
</td>
<td>
<xsl:value-of select="@systemName"/>
</td>
<td>
<xsl:if test='@num1 != 0'>
num1="<xsl:value-of select="@num1"/>"
</xsl:if>
</td>
<td>
<xsl:if test='@num2 != 0'>
num2="<xsl:value-of select="@num2"/>"
</xsl:if>
</td>
<td>
<xsl:if test='@dataString !="N/A" and @dataString !=""'>
value="<xsl:value-of select="@dataString"/>"
</xsl:if>
</td>
<td style="text-align:center">
<xsl:value-of select="@triggersCalc"/>
</td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template name="conditionalAction">
<tr>
<xsl:if test='@type != 1'>
<!-- decode operator -->
<td>
<xsl:choose>
  <xsl:when test="( @option = 1 )" >On change to true: </xsl:when>
  <xsl:when test="( @option = 2 )" >On change to false: </xsl:when>
  <xsl:when test="( @option = 3 )" >On change: </xsl:when>
  <xsl:otherwise>(option="<xsl:value-of select="@option"/>") </xsl:otherwise>
</xsl:choose>
</td>
<!-- decode type -->
<td>
<xsl:choose>
  <xsl:when test="( @type = 1 )" >
    (none)
  </xsl:when>
  <xsl:when test="( @type = 2 )" >
    Set Turnout "<xsl:value-of select="@systemName"/>"
    to
    <xsl:choose>
        <xsl:when test='@data = 1'>
            Unknown
        </xsl:when>
        <xsl:when test='@data = 2'>
            Closed
        </xsl:when>
        <xsl:when test='@data = 4'>
            Thrown
        </xsl:when>
        <xsl:when test='@data = 8'>
            Inconsistent
        </xsl:when>
        <xsl:otherwise>(<xsl:value-of select="@data"/>)</xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="( @type = 3 )" >
    Set Signal "<xsl:value-of select="@systemName"/>"
    to
    <xsl:choose>
        <xsl:when test='@data = 0'>Dark</xsl:when>
        <xsl:when test='@data = 1'>Red</xsl:when>
        <xsl:when test='@data = 2'>Flashing Red</xsl:when>
        <xsl:when test='@data = 4'>Yellow</xsl:when>
        <xsl:when test='@data = 8'>Flashing Yellow</xsl:when>
        <xsl:when test='@data = 16'>Green</xsl:when>
        <xsl:when test='@data = 32'>Flashing Green</xsl:when>
        <xsl:when test='@data = 64'>Lunar</xsl:when>
        <xsl:when test='@data = 128'>Flashing Lunar</xsl:when>
        <xsl:otherwise>(<xsl:value-of select="@data"/>)</xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="( @type = 4 )" >
    Set Signal "<xsl:value-of select="@systemName"/>" Held
  </xsl:when>
  <xsl:when test="( @type = 5 )" >
    Clear Signal "<xsl:value-of select="@systemName"/>" Held
  </xsl:when>
  <xsl:when test="( @type = 6 )" >
    Set Signal "<xsl:value-of select="@systemName"/>" Dark
  </xsl:when>
  <xsl:when test="( @type = 7 )" >
    Set Signal "<xsl:value-of select="@systemName"/>" Lit
  </xsl:when>
  <xsl:when test="( @type = 8 )" >
    Trigger Route "<xsl:value-of select="@systemName"/>"
  </xsl:when>
  <xsl:when test="( @type = 9 )" >
    Set Sensor "<xsl:value-of select="@systemName"/>"
    to
    <xsl:choose>
        <xsl:when test='@data = 1'>
            Unknown
        </xsl:when>
        <xsl:when test='@data = 2'>
            Active
        </xsl:when>
        <xsl:when test='@data = 4'>
            Inactive
        </xsl:when>
        <xsl:when test='@data = 8'>
            Inconsistent
        </xsl:when>
        <xsl:otherwise>(<xsl:value-of select="@data"/>)</xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="( @type = 10 )" >
    Delayed Set Sensor "<xsl:value-of select="@systemName"/>"
    to
    <xsl:choose>
        <xsl:when test='@data = 1'>
            Unknown
        </xsl:when>
        <xsl:when test='@data = 2'>
            Active
        </xsl:when>
        <xsl:when test='@data = 4'>
            Inactive
        </xsl:when>
        <xsl:when test='@data = 8'>
            Inconsistent
        </xsl:when>
        <xsl:otherwise>(<xsl:value-of select="@data"/>)</xsl:otherwise>
    </xsl:choose>
    after <xsl:value-of select="@delay"/> second(s)
  </xsl:when>
  <xsl:when test="( @type = 11 )" >
    Set Light "<xsl:value-of select="@systemName"/>"
    <xsl:choose>
        <xsl:when test='@data = 1'>
            Unknown
        </xsl:when>
        <xsl:when test='@data = 2'>
            Off
        </xsl:when>
        <xsl:when test='@data = 4'>
            On
        </xsl:when>
        <xsl:when test='@data = 8'>
            Inconsistent
        </xsl:when>
        <xsl:otherwise>(<xsl:value-of select="@data"/>)</xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="( @type = 12 )" >
    Set Memory "<xsl:value-of select="@systemName"/>"
    to "<xsl:value-of select="@string"/>"
  </xsl:when>
  <xsl:when test="( @type = 13 )" >Enable Logix "<xsl:value-of select="@systemName"/>"</xsl:when>
  <xsl:when test="( @type = 14 )" >Disable Logix "<xsl:value-of select="@systemName"/>"</xsl:when>
  <xsl:when test="( @type = 15 )" >Play Sound File "<xsl:value-of select="@string"/>"</xsl:when>
  <xsl:when test="( @type = 16 )" >Run Script "<xsl:value-of select="@string"/>"</xsl:when>
  <xsl:when test="( @type = 17 )" >
    Delayed Set Turnout "<xsl:value-of select="@systemName"/>" to
    <xsl:choose>
        <xsl:when test='@data = 1'>
            Unknown
        </xsl:when>
        <xsl:when test='@data = 2'>
            Closed
        </xsl:when>
        <xsl:when test='@data = 4'>
            Thrown
        </xsl:when>
        <xsl:when test='@data = 8'>
            Inconsistent
        </xsl:when>
        <xsl:otherwise>(<xsl:value-of select="@data"/>)</xsl:otherwise>
    </xsl:choose>
    after <xsl:value-of select="@delay"/> millisecond(s)
  </xsl:when>
  <xsl:when test="( @type = 18 )" >
    Turnout "<xsl:value-of select="@systemName"/>"
    <xsl:choose>
        <xsl:when test='@data = 1'>
            set locked
        </xsl:when>
        <xsl:when test='@data = 0'>
            set unlocked
        </xsl:when>
        <xsl:otherwise>
            lock set to unrecognized value: <xsl:value-of select="@data"/>
        </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="( @type = 19 )" >
    Reset Sensor "<xsl:value-of select="@systemName"/>"
    delay to <xsl:value-of select="@delay"/> millisecond(s)
  </xsl:when>
  <xsl:when test="( @type = 20 )" >Cancel timers for Sensor "<xsl:value-of select="@systemName"/>"</xsl:when>
  <xsl:when test="( @type = 21 )" >
    Reset Delayed Turnout "<xsl:value-of select="@systemName"/>"
    delay to <xsl:value-of select="@delay"/> millisecond(s)
  </xsl:when>
  <xsl:when test="( @type = 22 )" >Cancel timers for Turnout "<xsl:value-of select="@systemName"/>"</xsl:when>
  <xsl:when test="( @type = 23 )" >
    Set Fast Clock Time
    to <xsl:value-of select="@data"/> second after midnight
  </xsl:when>
  <xsl:when test="( @type = 24 )" >Start Fast Clock</xsl:when>
  <xsl:when test="( @type = 25 )" >Stop Fast Clock</xsl:when>
  <xsl:when test="( @type = 26 )" >
    Copy Memory "<xsl:value-of select="@systemName"/>"
    contents to Memory "<xsl:value-of select="@string"/>"
  </xsl:when>
  <xsl:when test="( @type = 27 )" >
    Set Light "<xsl:value-of select="@systemName"/>"
    intensity to <xsl:value-of select="@data"/> percent
  </xsl:when>
  <xsl:when test="( @type = 28 )" >
    Set Light "<xsl:value-of select="@systemName"/>"
    transition time to <xsl:value-of select="@data"/> fast seconds
  </xsl:when>
  <xsl:when test="( @type = 29 )" >
    Control audio:
    <xsl:if test='@option != " "'> option= "<xsl:value-of select="@option"/>"</xsl:if>
    <xsl:if test='@systemName != " "'> systemName= "<xsl:value-of select="@systemName"/>"</xsl:if>
    <xsl:if test='@data != " "'> data= "<xsl:value-of select="@data"/>"</xsl:if>
    <xsl:if test='@string != " "'> string= "<xsl:value-of select="@string"/>"</xsl:if>
  </xsl:when>
  <xsl:when test="( @type = 30 )" >
    Execute python
    <xsl:if test='@string != " "'> command: "<xsl:value-of select="@string"/>"
    </xsl:if>
  </xsl:when>
  <xsl:otherwise>
    Unknown type="<xsl:value-of select="@type"/>"
    name="<xsl:value-of select="@systemName"/>"
    <xsl:if test='@data != 0'>
    data="<xsl:value-of select="@data"/>"
    </xsl:if>
    <xsl:if test='@delay != 0'>
    delay="<xsl:value-of select="@delay"/>"
    </xsl:if>
    <xsl:if test='@string != " "'>
    string="<xsl:value-of select="@string"/>"
    </xsl:if>
    (Please report this as an error)
  </xsl:otherwise>
</xsl:choose>
</td>
</xsl:if>
</tr>
</xsl:template>


<!-- *************************************************************************************** -->
<!-- Index through LogixNGs elements       -->
<!-- each one becomes a separate section -->
<xsl:template match="layout-config/LogixNGs/Thread">
<h2>LogixNG Thread <xsl:value-of select="id"/>
<xsl:if test="string-length(name)!=0" > (<xsl:value-of select="name"/>)</xsl:if>
</h2>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="layout-config/LogixNGs/LogixNG">
<h2>LogixNG <xsl:value-of select="systemName"/> <!--names as attributes deprecated since 2.9.6-->
<xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
<xsl:if test="( @enabled = 'yes' )"> [Enabled] </xsl:if>
<xsl:if test="( @enabled = 'no'  )"> [NOT Enabled]</xsl:if>
</h2>
    <xsl:for-each select="ConditionalNGs/systemName">
        <xsl:call-template name="oneConditionalNG">
                <xsl:with-param name="name" select="."/>
        </xsl:call-template>
    </xsl:for-each>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- template to show a particular ConditionalNG -->
<xsl:template name="oneConditionalNG">
        <xsl:param name="name"/>
    <!-- index through individual conditional elements, looking for match -->
    <xsl:for-each select="/layout-config/LogixNGConditionalNGs/ConditionalNG">
		<xsl:if test='( systemName = $name )' >
            <!-- here have found correct conditional -->
            <h3>ConditionalNG
              <xsl:value-of select="systemName"/>
              <xsl:if test="string-length(userName)!=0" > (<xsl:value-of select="userName"/>)</xsl:if>
              <xsl:if test="( @enabled = 'yes' )"> [Enabled] </xsl:if>
              <xsl:if test="( @enabled = 'no'  )"> [NOT Enabled]</xsl:if>
            </h3>
            <table border="1">
              <tr>
                <th>Socket Name</th>
                <th>System Name</th>
                <th>Action Type</th>
                <th>Comment</th>
               </tr>
            <xsl:for-each select="./Socket">
              <tr><td><xsl:value-of select="socketName"/></td>
              <td><xsl:value-of select="systemName"/></td>
              <!-- Find type of action -->
              <xsl:call-template name="oneNGDigitalActionWithComment">
                    <xsl:with-param name="systemname" select="systemName"/>
              </xsl:call-template>
             </tr>
            </xsl:for-each>
            </table>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<!-- ********************************************* -->
<xsl:template name="oneNGDigitalActionWithComment">
        <xsl:param name="systemname"/>
    <xsl:for-each select="/layout-config/LogixNGDigitalActions/*">
		<xsl:if test='( systemName = $systemname )' >
        <!-- found the right one -->
            <td><xsl:value-of select="local-name()"/></td>
            <td><xsl:value-of select="./comment"/></td>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="layout-config/LogixNGs/InitializationTable">
    <h2>LogixNG Initialization Table</h2>
    <xsl:for-each select="./*">
        <xsl:value-of select="local-name()"/>
        <xsl:value-of select="."/>
        <br/>
    </xsl:for-each>

</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="layout-config/LogixNGs/Clipboard">
    <h2>LogixNG Clipboard</h2>
    <table border="1">
        <tr>
            <th>Socket Name</th>
            <th>System Name</th>
            <th>Action Type</th>
            <th>Expression Type</th>
         </tr>
    <!-- index through individual elements -->
        <xsl:for-each select="Many/Socket">
        <tr>
            <td><xsl:value-of select="socketName"/></td>
            <td><xsl:value-of select="systemName"/></td>
            <td><xsl:call-template name="oneNGDigitalAction">
                  <xsl:with-param name="systemname" select="systemName"/>
            </xsl:call-template></td>
            <td><xsl:call-template name="oneNGDigitalExpression">
                  <xsl:with-param name="systemname" select="systemName"/>
            </xsl:call-template></td>
        </tr>
        </xsl:for-each>
    </table>
</xsl:template>

<!-- ********************************************* -->
<xsl:template name="oneNGDigitalAction">
        <xsl:param name="systemname"/>

    <xsl:for-each select="/layout-config/LogixNGDigitalActions/*">
		<xsl:if test='( systemName = $systemname )' >
        <!-- found the right one -->
            <xsl:value-of select="local-name()"/>
        </xsl:if>
    </xsl:for-each>

</xsl:template>

<!-- ********************************************* -->
<xsl:template name="oneNGDigitalExpression">
        <xsl:param name="systemname"/>

    <xsl:for-each select="/layout-config/LogixNGDigitalExpressions/*">
		<xsl:if test='( systemName = $systemname )' >
        <!-- found the right one -->
            <xsl:value-of select="local-name()"/>
        </xsl:if>
    </xsl:for-each>

</xsl:template>

<!-- *************************************************************************************** -->
<!-- template to show ConditionalNG Expressions -->
<xsl:template match="layout-config/LogixNGDigitalExpressions">
    <h2>LogixNG Digital Expressions</h2>
    <table border="1">
        <tr>
            <th>Type</th>
            <th>System Name</th>
            <th>Sensor</th>
            <th>Turnout</th>
            <th>Light</th>
            <th>Memory</th>
            <th>Signal Mast</th>
            <th>Comment</th>
         </tr>
    <!-- index through individual elements -->
    <xsl:for-each select="./*">
        <tr><td><xsl:value-of select="local-name()"/></td>
            <td><xsl:value-of select="systemName"/></td>
            <td><xsl:value-of select="./sensor"/></td>
            <td><xsl:value-of select="./turnout"/></td>
            <td><xsl:value-of select="./light"/></td>
            <td><xsl:value-of select="./memory"/></td>
            <td><xsl:value-of select="./signalMast"/></td>
            <xsl:if test="string-length(comment)!=0" > <td><xsl:value-of select="comment"/></td></xsl:if>
        </tr>

            <!-- DO MORE HERE -->

    </xsl:for-each>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- template to show ConditionalNG Actions -->
<xsl:template match="layout-config/LogixNGDigitalActions">
    <h2>LogixNG Digital Actions</h2>
    <table border="1">
        <tr>
            <th>Type</th>
            <th>System Name</th>
            <th>Sensor</th>
            <th>Turnout</th>
            <th>Light</th>
            <th>If</th>
            <th>Then</th>
            <th>Else</th>
            <th>Comment</th>
         </tr>
    <!-- index through individual elements -->
    <xsl:for-each select="./*">
        <tr><td><xsl:value-of select="local-name()"/></td>
            <td><xsl:value-of select="systemName"/></td>
            <td><xsl:value-of select="./sensor"/></td>
            <td><xsl:value-of select="./turnout"/></td>
            <td><xsl:value-of select="./light"/></td>
            <td><xsl:value-of select="./IfSocket/systemName"/></td>
            <td><xsl:value-of select="./ThenSocket/systemName"/></td>
            <td><xsl:value-of select="./ElseSocket/systemName"/></td>
            <xsl:if test="string-length(comment)!=0" > <td><xsl:value-of select="comment"/></td></xsl:if>
        </tr>

            <!-- DO MORE HERE -->

    </xsl:for-each>
    </table>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="layout-config/paneleditor">
<h2>Panel: <xsl:value-of select="@name"/></h2>
    <table border="1">
    <!-- index through individual panel elements -->
    <tr>
      <th>Item</th><th>Name</th><th>Value/Description</th>
    </tr>
    <xsl:apply-templates/>
    </table>

</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="layout-config/LayoutEditor">
<h2>Layout Panel: <xsl:value-of select="@name"/></h2>

<h3>Track Drawing Options</h3>
<table border="1">
<xsl:for-each select="./layoutTrackDrawingOptions/*">
    <tr>
        <td><xsl:value-of select="name()"/></td>
        <td><xsl:value-of select="."/></td>
    </tr>
</xsl:for-each>
</table>

<h3>Elements</h3>
    <table border="1">
    <!-- index through individual panel elements -->
    <tr>
      <th>Item</th><th>Name</th><th>Value/Description</th>
    </tr>
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- ****** layoutTrackDrawingOptions handled within Layout Editor ************************* -->
<xsl:template match="layoutTrackDrawingOptions">
</xsl:template>

<!-- *************************************************************************************** -->
<!-- new SSL name -->
<xsl:template match="signalelements">
        <xsl:call-template name="signalelements"/>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- SSL element groups -->
<xsl:template name="signalelements">
    <!-- each one becomes a table -->
    <h2>Simple Signal Logic</h2>
        <table border="1">
        <tr><th>Controls Signal</th>
            <th><!-- match to --></th>
            <th>Mode</th>
            <th>Watch Signal</th>
            <th>Turnout</th>
            <th>Sensors</th>
            <th>Options</th>
            <th>Comment</th>
        </tr>
        <!-- index through individual block elements -->
        <xsl:for-each select="block">
            <xsl:sort select="@signal" />
            <xsl:call-template name="signalelement"/>
        </xsl:for-each>
        <!-- index new form, if present -->
        <xsl:for-each select="signalelement">
            <xsl:sort select="@signal" />
            <xsl:call-template name="signalelement"/>
        </xsl:for-each>
        </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- SSL elements -->
<xsl:template name="signalelement">
    <tr>
        <td>
            <xsl:value-of select="@signal"/></td>
        <td>
            <xsl:variable name="matchto" select="@signal"/>
            <xsl:value-of select="../../signalheads/signalhead[@systemName=$matchto]/@userName"/>
            <xsl:value-of select="../../signalheads/signalhead[@userName=$matchto]/@systemName"/>
        </td>
        <td style="text-align:center"><xsl:if test="@mode = '1'" >
                Single<br/></xsl:if>
            <xsl:if test="@mode = '2'" >
                Main<br/></xsl:if>
            <xsl:if test="@mode = '3'" >
                Siding<br/></xsl:if>
            <xsl:if test="@mode = '4'" >
                Facing<br/></xsl:if>
            </td>
        <td><xsl:value-of select="@watchedsignal1"/><br/>
            <xsl:if test="( @watchedsignal1alt != '' )" ><xsl:value-of select="@watchedsignal1alt"/><br/></xsl:if>
            <xsl:if test="@mode = '4'" ><hr/></xsl:if> <!-- facing has two sections -->
            <xsl:if test="( @watchedsignal2 != '' )" ><xsl:value-of select="@watchedsignal2"/><br/></xsl:if>
            <xsl:if test="( @watchedsignal2alt != '' )" ><xsl:value-of select="@watchedsignal2alt"/><br/></xsl:if>
            </td>
        <td><xsl:value-of select="@watchedturnout"/></td>
        <td>
            <xsl:if test="( @watchedsensor1 != '' )" ><xsl:value-of select="@watchedsensor1"/><br/></xsl:if>
            <xsl:if test="( @watchedsensor1alt != '' )" ><xsl:value-of select="@wwatchedsensor1alt"/><br/></xsl:if>
            <xsl:if test="@mode = '4'" ><hr/></xsl:if> <!-- facing has two sections -->
            <xsl:if test="( @watchedsensor2 != '' )" ><xsl:value-of select="@watchedsensor2"/><br/></xsl:if>
            <xsl:if test="( @watchedsensor2alt != '' )" ><xsl:value-of select="@watchedsensor2alt"/><br/></xsl:if>
            <xsl:for-each select="sensorname">
            <xsl:value-of select="."/><br/>
            </xsl:for-each>
        </td>
        <td><xsl:if test="@limitspeed1 = 'true'" >
                Limit Speed Path 1<br/></xsl:if>
            <xsl:if test="@limitspeed2 = 'true'" >
                Limit Speed Path 2<br/></xsl:if>
            <xsl:if test="@useflashyellow = 'true'" >
                Use Flash Yellow<br/></xsl:if>
            <xsl:if test="@distantsignal = 'true'" >
                Distant Signal<br/></xsl:if>
            </td>
         <td><xsl:value-of select="comment"/>
            </td>
    </tr>
</xsl:template>


<!-- *************************************************************************************** -->
<!--Table cell contents for the different types-->
<!-- *************************************************************************************** -->

<!-- *************************************************************************************** -->
<!-- Index through turnout elements -->
<xsl:template match="turnout">
    <tr>
        <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td> <xsl:value-of select="@feedback"/> </td>
        <td style="text-align:center"><xsl:if test="( @inverted = 'true' )" >Yes</xsl:if></td>
        <td style="text-align:center"><xsl:if test="( @locked = 'true' )" >Yes</xsl:if></td>
        <td style="text-align:center"><xsl:if test="( @automate != 'Default' )" >Yes</xsl:if></td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through light elements -->
<xsl:template match="light">
    <tr>
        <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through route elements -->
<xsl:template match="route">
    <tr>
        <td><xsl:value-of select="systemName"/></td>
        <td><xsl:value-of select="userName"/></td>
        <td><xsl:for-each select="routeSensor">
            <xsl:value-of select="@systemName"/>:&#160;&#160;&#160;<xsl:value-of select="@mode"/><br/>
        </xsl:for-each></td>
        <td><xsl:value-of select="@controlTurnout"/>&#160;&#160;&#160;<xsl:value-of select="@controlTurnoutState"/></td>
        <td><xsl:for-each select="routeOutputTurnout">
            <xsl:value-of select="@systemName"/>:&#160;&#160;&#160;<xsl:value-of select="@state"/><br/>
        </xsl:for-each></td>
        <td><xsl:for-each select="routeOutputSensor">
            <xsl:value-of select="@systemName"/>:&#160;&#160;&#160;<xsl:value-of select="@state"/><br/>
        </xsl:for-each></td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through layoutblock elements -->
<xsl:template match="layoutblock">
<tr>
    <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
    <td><xsl:value-of select="userName"/></td>
    <td><xsl:value-of select="@occupancysensor"/></td>
    <td><xsl:value-of select="@memory"/></td>
    <td style="text-align:center"><xsl:choose>
        <xsl:when test="( @occupiedsense = 2 )" >ACTIVE</xsl:when>
        <xsl:when test="( @occupiedsense = 4 )" >INACTIVE</xsl:when>
        <xsl:otherwise><xsl:value-of select="@occupiedsense"/></xsl:otherwise>
        </xsl:choose></td>
    <td style="text-align:center"><xsl:value-of select="@trackcolor"/></td>
    <td style="text-align:center"><xsl:value-of select="@occupiedcolor"/></td>
    <td style="text-align:center"><xsl:value-of select="@extracolor"/></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through oblock elements -->
<xsl:template match="oblock">
    <tr>
        <td><xsl:value-of select="@systemName"/></td> <!--oblock system name still stored as attrib in 2.9.6-->
        <td><xsl:value-of select="userName"/></td> <!-- user name as attributes deprecated since 2.9.6-->
        <td><xsl:for-each select="portal">
            <xsl:value-of select="@systemName"/> (<xsl:value-of select="portalName"/>) from
            <xsl:for-each select="fromBlock">
                <xsl:value-of select="@blockName"/>
            </xsl:for-each>
            to
            <xsl:for-each select="toBlock">
                <xsl:value-of select="@blockName"/>
            </xsl:for-each>
            <br/>
        </xsl:for-each></td>
        <td><xsl:value-of select="@length"/></td>
        <td style="text-align:center"><xsl:value-of select="@curve"/></td>
        <td style="text-align:center"><xsl:value-of select="@permissive"/></td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through signalhead elements -->
<xsl:template match="signalhead">
<tr><td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
  <td><xsl:value-of select="userName"/></td>
  <td><xsl:choose>
    <xsl:when test="( @class = 'jmri.configurexml.VirtualSignalHeadXml' )" >Virtual</xsl:when>
    <xsl:when test="( @class = 'jmri.implementation.configurexml.VirtualSignalHeadXml' )" >Virtual</xsl:when>
    <xsl:when test="( @class = 'jmri.configurexml.TripleTurnoutSignalHeadXml' )" >Triple Output</xsl:when>
    <xsl:when test="( @class = 'jmri.implementation.configurexml.TripleTurnoutSignalHeadXml' )" >Triple Output</xsl:when>
    <xsl:when test="( @class = 'jmri.configurexml.DoubleTurnoutSignalHeadXml' )" >Double Output</xsl:when>
    <xsl:when test="( @class = 'jmri.implementation.configurexml.DoubleTurnoutSignalHeadXml' )" >Double Output</xsl:when>
    <xsl:when test="( @class = 'jmri.jmrix.loconet.configurexml.SE8cSignalHeadXml' )" >SE8c</xsl:when>
    <xsl:when test="( @class = 'jmri.implementation.configurexml.SE8cSignalHeadXml' )" >SE8c</xsl:when>
    <xsl:otherwise>Other</xsl:otherwise>
    </xsl:choose></td>
  <td>
     <xsl:for-each select="turnout">  <!-- older form with "turnout" elements -->
        <xsl:value-of select="@systemName"/><br/>
     </xsl:for-each>
     <xsl:for-each select="turnoutname">  <!-- newer form with "turnoutname" elements -->
        <xsl:value-of select="."/> (<xsl:value-of select="@defines"/>)<br/>
     </xsl:for-each>
  </td>
  <td><xsl:value-of select="comment"/></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="signalgroup">
    <tr>
        <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td><xsl:value-of select="@signalMast"/></td>
        <td>
            <xsl:for-each select="signalHead">
                <xsl:value-of select="@name"/><br/>
            </xsl:for-each>
        </td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through signalmast elements, several classes -->
<xsl:template match="signalmast">
    <tr><td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td style="text-align:center"><xsl:choose>
            <xsl:when test="( @class = 'jmri.implementation.configurexml.SignalHeadSignalMastXml' )" >SH Mast</xsl:when>
            <xsl:when test="( @class = 'jmri.implementation.configurexml.MatrixSignalMastXml' )" >MX Mast</xsl:when>
            <xsl:otherwise>Other</xsl:otherwise>
        </xsl:choose></td>
        <td style="text-align:center">
            <xsl:for-each select="unlit">
                <xsl:value-of select="@allowed"/><br/>
            </xsl:for-each>
        </td>
        <td>
            <xsl:for-each select="disabledAspects">
                <xsl:for-each select="disabledAspect">
                    <xsl:value-of select="."/><br/>
                </xsl:for-each>
            </xsl:for-each>
        </td>
        <td><xsl:value-of select="comment"/></td>
        <td></td>
        <td></td>
    </tr>
</xsl:template>
<!-- *************************************************************************************** -->
<xsl:template match="dccsignalmast">
    <tr><td><xsl:value-of select="systemName"/></td>
        <td><xsl:value-of select="userName"/></td>
        <td style="text-align:center">DCC Mast</td>
        <td style="text-align:center">
            <xsl:for-each select="unlit">
                <xsl:value-of select="@allowed"/><br/>
            </xsl:for-each>
        </td>
        <td>
            <xsl:for-each select="disabledAspects">
                <xsl:for-each select="disabledAspect">
                    <xsl:value-of select="."/><br/>
                </xsl:for-each>
            </xsl:for-each>
        </td>
        <td><xsl:value-of select="comment"/></td>
        <td></td>
        <td style="text-align:right">
            <xsl:for-each select="aspect">
                <xsl:value-of select="@defines"/>:
                <xsl:value-of select="number"/><br/>
            </xsl:for-each>
        </td>
    </tr>
</xsl:template>
<!-- *************************************************************************************** -->
<xsl:template match="turnoutsignalmast">
    <tr><td><xsl:value-of select="systemName"/></td>
        <td><xsl:value-of select="userName"/></td>
        <td style="text-align:center">Turnout<br/>Mast</td>
        <td style="text-align:center">
            <xsl:for-each select="unlit">
                <xsl:value-of select="@allowed"/><br/>
            </xsl:for-each>
        </td>
        <td>
            <xsl:for-each select="disabledAspects">
                <xsl:for-each select="disabledAspect">
                    <xsl:value-of select="."/><br/>
                </xsl:for-each>
            </xsl:for-each>
        </td>
        <td><xsl:value-of select="comment"/></td>
        <td></td>
        <td style="text-align:right">
            <xsl:for-each select="aspect">
                <xsl:if test='(turnout != "")'>
                    <xsl:value-of select="@defines"/>:
                    <xsl:value-of select="turnout"/> =
                    <xsl:value-of select="turnoutstate"/><br/>
                </xsl:if>
            </xsl:for-each>
        </td>
    </tr>
</xsl:template>
<!-- *************************************************************************************** -->
<xsl:template match="virtualsignalmast">
    <tr><td><xsl:value-of select="systemName"/></td>
        <td><xsl:value-of select="userName"/></td>
        <td style="text-align:center">Virtual<br/>Mast</td>
        <td style="text-align:center">
            <xsl:for-each select="unlit">
                <xsl:value-of select="@allowed"/><br/>
            </xsl:for-each>
        </td>
        <td>
            <xsl:for-each select="disabledAspects">
                <xsl:for-each select="disabledAspect">
                    <xsl:value-of select="."/><br/>
                </xsl:for-each>
            </xsl:for-each>
        </td>
        <td><xsl:value-of select="comment"/></td>
        <td></td>
        <td></td>
    </tr>
</xsl:template>
<!-- *************************************************************************************** -->
<xsl:template match="matrixsignalmast">
    <tr><td><xsl:value-of select="systemName"/></td>
        <td><xsl:value-of select="userName"/></td>
        <td style="text-align:center">Matrix Mast</td>
        <td style="text-align:center">
            <xsl:for-each select="unlit">
                <xsl:value-of select="@allowed"/><br/>
            </xsl:for-each>
        </td>
        <td>
            <xsl:for-each select="disabledAspects">
                <xsl:for-each select="disabledAspect">
                    <xsl:value-of select="."/><br/>
                </xsl:for-each>
            </xsl:for-each>
        </td>
        <td><xsl:value-of select="comment"/></td>
        <td>
            <xsl:for-each select="outputs">
                <xsl:for-each select="output">
                    <xsl:value-of select="@matrixCol"/> =
                    <xsl:value-of select="."/><br/>
                </xsl:for-each>
            </xsl:for-each>
        </td>
        <td style="text-align:right">
            <xsl:for-each select="bitStrings">
                <xsl:for-each select="bitString">
                    <xsl:value-of select="@aspect"/> =
                    <xsl:value-of select="."/><br/>
                </xsl:for-each>
            </xsl:for-each>
        </td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="sensor">
<tr><td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
    <td><xsl:value-of select="userName"/></td>
    <td><xsl:if test='(@inverted = "true")'>Yes</xsl:if></td>
    <td><xsl:value-of select="comment"/></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="memory">
<tr>
  <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
  <td><xsl:value-of select="userName"/></td>
  <td><xsl:value-of select="comment"/></td>
  <td><xsl:value-of select="@value"/></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="reporter">
<tr>
  <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
  <td><xsl:value-of select="userName"/></td>
  <td><xsl:value-of select="comment"/></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="logix">
<tr>
  <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
  <td><xsl:value-of select="userName"/></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="LogixNG">
<tr>
  <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
  <td><xsl:value-of select="userName"/></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="block">
<tr>
  <td><xsl:value-of select="@signal"/></td>
  <td><xsl:value-of select="@watchedturnout"/></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="warrant">
    <tr>
        <td><xsl:value-of select="@systemName"/></td> <!--names still stored as attributes in warrants as of 2.9.6 up to 4.6-->
        <td><xsl:value-of select="@userName"/></td>
        <td>
            <xsl:for-each select="blockOrder">
                Path: <xsl:value-of select="block/@pathname"/><br/>
            </xsl:for-each>
        </td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="section">
    <tr>
        <td><xsl:value-of select="@systemName"/></td> <!--names still stored as attributes in warrants as of 4.7.1 -->
        <td><xsl:value-of select="userName"/></td>
        <td>
            <xsl:for-each select="blockentry">
                <xsl:value-of select="@sName"/> (<xsl:value-of select="@order"/>)<br/>
            </xsl:for-each>
        </td>
        <td>
            <xsl:for-each select="entrypoint">
                <xsl:value-of select="@fromblock"/> to <xsl:value-of select="@toblock"/><br/>
            </xsl:for-each>
        </td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- conditionals are not directly displayed -->
<xsl:template match="conditional">
</xsl:template>

<!-- *************************************************************************************** -->
<!-- conditionalNGs are not directly displayed -->
<xsl:template match="ConditionalNG">
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through audio elements, several classes -->
<xsl:template match="audiobuffer">
    <tr><td>Buffer</td>
        <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td><xsl:value-of select="url"/></td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>
<xsl:template match="audiolistener">
    <tr><td>Listener</td>
        <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td><xsl:value-of select="url"/></td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>
<xsl:template match="audiosource">
    <tr><td>Source</td>
        <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td><xsl:value-of select="url"/></td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="signalheadicon">
<tr><td>Signalhead Icon </td><td><xsl:value-of select="@signalhead"/></td></tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="signalmasticon">
<tr><td>Signalmast Icon </td><td><xsl:value-of select="@signalmast"/></td></tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="turnouticon">
<tr><td>Turnout Icon </td><td><xsl:value-of select="@turnout"/></td></tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="sensoricon">
<tr><td>Sensor Icon </td><td><xsl:value-of select="@sensor"/></td></tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="positionablelabel">
<tr><td>Positionable Label
<xsl:choose>
  <xsl:when test="( @text != '' )" >Text </xsl:when>
  <xsl:when test="( icon/@url != '' )" >Icon </xsl:when>
</xsl:choose>
</td>
<td></td><td>
<xsl:choose>
  <xsl:when test="( @text != '' )" >"<xsl:value-of select="@text"/>"</xsl:when>
  <xsl:when test="( icon/@url != '' )" ><xsl:value-of select="icon/@url"/></xsl:when>
</xsl:choose>
</td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="layoutturnout">
<tr><td>Layout Turnout</td>
<td><xsl:value-of select="@ident"/> / <xsl:value-of select="@turnoutname"/></td>
<td>
 (
<xsl:choose>
  <xsl:when test="( @type = 1 )" >RH</xsl:when>
  <xsl:when test="( @type = 2 )" >LH</xsl:when>
  <xsl:when test="( @type = 3 )" >Wye</xsl:when>
  <xsl:when test="( @type = 4 )" >Double XOver</xsl:when>
  <xsl:when test="( @type = 5 )" >RH XOver</xsl:when>
  <xsl:when test="( @type = 6 )" >LH  XOver</xsl:when>
  <xsl:when test="( @type = 7 )" >Single Slip</xsl:when>
  <xsl:when test="( @type = 8 )" >Double Slip</xsl:when>
  <xsl:otherwise>type="<xsl:value-of select="@type"/>"</xsl:otherwise>
</xsl:choose>
 )
<xsl:choose>
  <xsl:when test="( @blockname != '' )" >
  block: "<xsl:value-of select="@blockname"/>"
  </xsl:when>
</xsl:choose>
<xsl:choose>
  <xsl:when test="( @connectaname != '' )" >
  A to "<xsl:value-of select="@connectaname"/>"
  </xsl:when>
</xsl:choose>
<xsl:choose>
  <xsl:when test="( @connectbname != '' )" >
  B to "<xsl:value-of select="@connectbname"/>"
  </xsl:when>
</xsl:choose>
<xsl:choose>
  <xsl:when test="( @connectcname != '' )" >
  C to "<xsl:value-of select="@connectcname"/>"
  </xsl:when>
</xsl:choose>
<xsl:choose>
  <xsl:when test="( @connectdname != '' )" >
  D to "<xsl:value-of select="@connectdname"/>"
  </xsl:when>
</xsl:choose>
</td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="tracksegment">
<tr><td>Track Segment</td>
<td><xsl:value-of select="@ident"/></td>
<td>
<xsl:choose>
  <xsl:when test="( @blockname != '' )" >
  block: "<xsl:value-of select="@blockname"/>"
  </xsl:when>
</xsl:choose>
connects to "<xsl:value-of select="@connect1name"/>" (type=<xsl:value-of select="@type1"/>);
connects to "<xsl:value-of select="@connect2name"/>" (type=<xsl:value-of select="@type2"/>)
</td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="layoutSlip">
<tr><td>Layout Slip</td>
    <td><xsl:value-of select="@ident"/></td>
    <td>
<xsl:choose>
  <xsl:when test="( @blockname != '' )" >
  block: "<xsl:value-of select="@blockname"/>"
  </xsl:when>
</xsl:choose>
<xsl:choose>
  <xsl:when test="( turnout != '' )" >
  turnout: "<xsl:value-of select="turnout"/>"
  </xsl:when>
</xsl:choose>
<xsl:choose>
  <xsl:when test="( turnoutB != '' )" >
  turnoutB: "<xsl:value-of select="turnoutB"/>"
  </xsl:when>
</xsl:choose>
<xsl:for-each select="states/*">
    (
        <xsl:value-of select="name()"/>:<xsl:value-of select="turnout"/>,<xsl:value-of select="turnoutB"/>
    )
</xsl:for-each>
   </td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="memoryicon">
<tr><td>Memory Icon </td><td><xsl:value-of select="@memory"/></td></tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="indicatortrackicon">
<tr><td>Indicator Track Icon</td>
   <td></td>
   <td>
   block="<xsl:value-of select="occupancyblock"/>"
   paths:
      <xsl:for-each select="paths/*"> "<xsl:value-of select="."/>" <xsl:text> </xsl:text></xsl:for-each>
   </td>
</tr>
</xsl:template>

<!-- *************************************************************************************** DUP??? -->
<xsl:template match="indicatorturnouticon">
<tr><td>Indicator Turnout Icon </td>
    <td>
block="<xsl:value-of select="occupancyblock"/>"
turnout="<xsl:value-of select="turnout"/>"
paths:
<xsl:for-each select="paths/*"> "<xsl:value-of select="."/>" <xsl:text> </xsl:text></xsl:for-each>
    </td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="BlockContentsIcon">
<tr><td>Block Contents Icon </td><td><xsl:value-of select="@blockcontents"/></td></tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="PortalIcon">
<tr><td>Portal Icon</td>
    <td><xsl:value-of select="@portalName"/></td>
    <td>
        to Block Name="<xsl:value-of select="@toBlockName"/>"
        from Block Name="<xsl:value-of select="@fromBlockName"/>"
    </td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="positionablepoint">
<tr><td>Positionable Point </td>
    <td><xsl:value-of select="@ident"/></td>
    <td>
connects to "<xsl:value-of select="@connect1name"/>" (type=<xsl:value-of select="@text"/>);
connects to "<xsl:value-of select="@connect2name"/>" (type=<xsl:value-of select="@text"/>)
    </td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="locoicon">
<tr><td>Loco icon </td><td><xsl:value-of select="@text"/>"</td></tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="LightIcon">
<tr><td>Light Icon </td><td><xsl:value-of select="@light"/></td></tr>
</xsl:template>

<!-- *************************************************************************************** -->
<xsl:template match="multisensoricon">
<tr><td>Multisensor Icon</td>
    <td><xsl:for-each select="active">"<xsl:value-of select="@sensor"/>" </xsl:for-each></td>
</tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Display version number in header -->
<xsl:template match="layout-config/jmriversion">
    JMRI version used to create this panel xml file: <xsl:value-of select="major"/>.<xsl:value-of select="minor"/>.<xsl:value-of select="test"/><xsl:value-of select="modifier"/>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- At the bottom, display JMRI load history -->
<xsl:template match="filehistory">
    <!-- title first time -->
    <xsl:for-each select="..">
      <xsl:choose>
        <xsl:when test="(name() != 'operation' )" >
            <h2>History</h2>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>

    <table border="1">
    <tr><th>Date</th><th>Operation</th><th></th></tr>
    <xsl:for-each select="operation">
        <tr>
            <td><xsl:value-of select="date"/></td>
            <td>
                <xsl:choose>
                    <xsl:when test="type = 'app'" >Started JMRI</xsl:when>
                    <xsl:otherwise><xsl:value-of select="type"/></xsl:otherwise>
                </xsl:choose>
            </td>
            <td><xsl:value-of select="filename"/><br/>
                <xsl:apply-templates select="filehistory"/>
                </td>
        </tr>
    </xsl:for-each>
    </table>
</xsl:template>

</xsl:stylesheet>
