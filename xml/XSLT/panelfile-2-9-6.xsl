<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI panel file into an HTML page              -->
<!-- Used by default when the panel file is displayed in a web browser      -->
<!-- This version corresponds to the 2.9.6 schema update                    -->

<!-- This file is part of JMRI.  Copyright 2007-2011, 2016.                 -->
 
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
      <xsl:param name="JmriCopyrightYear"/>

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
		<title>JMRI panel file</title>
	</head>
	
	<body>
		<h2>JMRI panel file</h2>

                <xsl:apply-templates/>

        <p></p>
<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
<p/>Site hosted by: <br/>
    <a href="http://sourceforge.net/projects/jmri">
    <img src="https://sourceforge.net/sflogo.php?type=13&amp;group_id=26788" border="0" alt="JMRI Model Railroad Interface at SourceForge.net"/></a>
	</body>
</html>

</xsl:template>

<!-- Index through turnouts elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/turnouts">
<h3>Turnouts</h3>
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
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through lights elements -->
<!-- each one becomes a table      -->
<xsl:template match="layout-config/lights">
    <h3>Lights</h3>
    <table border="1">
        <tr>
            <th>System Name</th>
            <th>User Name</th>
            <th>Comment</th>
        </tr>
        <!-- index through individual light elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through signalheads elements -->
<!-- each one becomes a table           -->
<xsl:template match="layout-config/signalheads">
<h3>Signal Heads</h3>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th><th>Type</th><th>Output</th><th>Comment</th></tr>
    <!-- index through individual signalhead elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through signalmasts elements -->
<!-- each one becomes a table           -->
<xsl:template match="layout-config/signalmasts">
    <h3>Signal Masts</h3>
    <table border="1">
      <tr><th>System Name</th><th>User Name</th><th>Type</th><th>CanBeUnlit</th><th>DisabledAspct</th><th>Comment</th></tr>
      <!-- index through individual signal mast elements/classes, see below) -->
        <!--update for new types/elements-->
      <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through signalgroups elements -->
<!-- each one becomes a table            -->
<xsl:template match="layout-config/signalgroups">
    <h3>Signal Groups</h3>
    <table border="1">
        <tr><th>System Name</th><th>User Name</th><th>Master</th><th>Members</th><th>Comment</th></tr>
        <!-- index through individual signalgroup elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through signalmastlogics elements -->
<!-- each one becomes a separate table       -->
<xsl:template match="layout-config/signalmastlogics">
<h3>Signal Mast Logics</h3>
Logic delay: <xsl:value-of select="logicDelay"/> (ms)<br/>
    <!-- index through individual signalmastlogic elements -->
    <xsl:call-template name="oneSML"/>
</xsl:template>

<!-- Index through sensors elements -->
<!-- each one becomes a table       -->
<xsl:template match="layout-config/sensors">
<h3>Sensors</h3>
    Default sensor state: <xsl:value-of select="@defaultInitialState"/>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th><th>Inv?</th><th>Comment</th></tr>
    <!-- index through individual sensor elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through memories elements -->
<!-- each one becomes a table        -->
<xsl:template match="layout-config/memories">
<h3>Memories</h3>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th><th>Comment</th></tr>
    <!-- index through individual memory elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through reporters elements -->
<!-- each one becomes a table         -->
<xsl:template match="layout-config/reporters">
<h3>Reporters</h3>
    <table border="1">
    <tr><th>System Name</th><th>User Name</th><th>Comment</th></tr>
    <!-- index through individual reporter elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through routes elements -->
<!-- each one becomes a table      -->
<xsl:template match="layout-config/routes">
<h3>Routes</h3>
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

<!-- Index through layoutblocks elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/layoutblocks">
    <h3>Layout Blocks</h3>
    <table border="1">
        <tr>
            <th>System Name</th>
            <th>User Name</th>
            <th>Occupancy Sensor</th>
            <th>Memory</th>
        </tr>
        <!-- index through individual turnout elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through oblocks (occupancy blocks) elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/oblocks">
    <h3>Occupancy Blocks</h3>
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

<!-- Index through sections elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/sections">
    <h3>Sections</h3>
    <table border="1">
        <tr><th>System Name</th><th>User Name</th><th>Entry (order)</th><th>Exit</th><th>Comment</th></tr>
        <!-- index through individual section elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through warrants elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/warrants">
    <h3>Warrants</h3>
    Settings:
    <!--Haltstart = <xsl:value-of select="nxparams/haltstart"/>-->
    <!--Max.speed = <xsl:value-of select="nxparams/maxspeed"/>-->
    <table border="1">
        <tr><th>System Name</th><th>User Name</th><th>Block Order</th></tr>
        <!-- index through individual warrant elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through audio elements -->
<!-- each one becomes a table           -->
<xsl:template match="layout-config/audio">
    <h3>Audio</h3>
    <table border="1">
        <tr><th>Class</th><th>System Name</th><th>User Name</th><th>Type</th><th>URL</th><th>Comment</th></tr>
        <!-- index through individual audio elements -->
        <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through logixs elements       -->
<!-- each one becomes a separate section -->
<xsl:template match="layout-config/logixs/logix">
<h3>Logix <xsl:value-of select="systemName"/> <!--names as attributes deprecated since 2.9.6-->
<xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
</h3>
    <!-- index through individual logix elements -->
        <xsl:call-template name="oneLogix"/>
</xsl:template>

<!-- new SSL name -->
<xsl:template match="signalelements">
        <xsl:call-template name="signalelements"/>
</xsl:template>

<!-- SSL element groups -->
<xsl:template name="signalelements">
    <!-- each one becomes a table -->
    <h3>Simple Signal Logic</h3>
        <table border="1">
        <tr><th>Controls Signal</th><th></th>
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
        <td><xsl:if test="@mode = '1'" >
                Single<br/></xsl:if>
            <xsl:if test="@mode = '2'" >
                Main<br/></xsl:if>
            <xsl:if test="@mode = '3'" >
                Siding<br/></xsl:if>
            <xsl:if test="@mode = '4'" >
                Facing<br/></xsl:if>
            </td>
        <td><xsl:value-of select="@watchedsignal1"/><br/>
            <xsl:value-of select="@watchedsignal2"/></td>
        <td><xsl:value-of select="@watchedturnout"/></td>
        <td><xsl:for-each select="sensor">
            <xsl:value-of select="@systemName"/><br/>
            </xsl:for-each></td>
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

<xsl:template match="layout-config/blocks">
    <xsl:if test="@class = 'jmri.jmrit.blockboss.configurexml.BlockBossLogicXml'" >
        <xsl:call-template name="signalelements"/>
    </xsl:if>
    
    <xsl:if test="@class = 'jmri.configurexml.BlockManagerXml'" >
        <!-- Index through blocks elements -->
        <!-- each one becomes a table -->
        <h3>Blocks</h3>
            <table border="1">
            <tr>
                <th>System Name</th>
                <th>User Name</th>
                <th>Sensor</th>
                <th>Paths</th>
            </tr>
            <!-- index through individual block elements -->
            <xsl:for-each select="block">
                <tr><xsl:element name="a"><xsl:attribute name="id">Block-<xsl:value-of select="@systemName"/></xsl:attribute></xsl:element>
                    <td><xsl:value-of select="@systemName"/></td>
                    <td><xsl:value-of select="@userName"/></td>
                    <td><xsl:for-each select="sensor">
                        <xsl:value-of select="@systemName"/><br/>
                        </xsl:for-each></td>
                    <td><table><xsl:for-each select="path">
                        <tr>
                            <td><xsl:choose>
                                <xsl:when test="( @todir = 16 )" >North</xsl:when>
                                <xsl:when test="( @todir = 32 )" >South</xsl:when>
                                <xsl:when test="( @todir = 64 )" >East</xsl:when>
                                <xsl:when test="( @todir = 128 )" >West</xsl:when>
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
                                <xsl:when test="( @fromdir = 128 )" >West</xsl:when>
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
                </tr>
            </xsl:for-each>
            </table>
    </xsl:if>
</xsl:template>


<!--Table cell contents for the different types-->

<!-- Index through turnout elements -->
<xsl:template match="turnout">
    <tr>
        <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td> <xsl:value-of select="@feedback"/> </td>
        <td><xsl:if test="( @inverted = 'true' )" >Yes</xsl:if></td>
        <td><xsl:if test="( @locked = 'true' )" >Yes</xsl:if></td>
        <td><xsl:if test="( @automate != 'Default' )" >Yes</xsl:if></td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>

<!-- Index through light elements -->
<xsl:template match="light">
    <tr>
        <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td><xsl:value-of select="comment"/></td>
    </tr>
</xsl:template>

<!-- Index through route elements -->
<xsl:template match="route">
    <tr>
        <td><xsl:value-of select="@systemName"/></td> <!--names still stored as attributes in routes as of 2.9.6 up to 4.6-->
        <td><xsl:value-of select="@userName"/></td>
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

<!-- Index through layoutblock elements -->
<xsl:template match="layoutblock">
<tr>
    <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
    <td><xsl:value-of select="userName"/></td>
    <td><xsl:value-of select="@occupancysensor"/></td>
    <td><xsl:value-of select="@memory"/></td>
</tr>
</xsl:template>

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
        <td><xsl:value-of select="@curve"/></td>
        <td><xsl:value-of select="@permissive"/></td>
    </tr>
</xsl:template>

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

<!-- Index through signalmast elements, several classes -->
<xsl:template match="signalmast">
    <tr><td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
        <td><xsl:value-of select="userName"/></td>
        <td><xsl:choose>
            <xsl:when test="( @class = 'jmri.implementation.configurexml.SignalHeadSignalMastXml' )" >SH Mast</xsl:when>
            <xsl:when test="( @class = 'jmri.implementation.configurexml.MatrixSignalMastXml' )" >MX Mast</xsl:when>
            <xsl:otherwise>Other</xsl:otherwise>
        </xsl:choose></td>
        <td>
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
    </tr>
</xsl:template>
<xsl:template match="dccsignalmast">
    <tr><td><xsl:value-of select="systemName"/></td>
        <td><xsl:value-of select="userName"/></td>
        <td>DCC Mast</td>
        <td>
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
    </tr>
</xsl:template>
<xsl:template match="turnoutsignalmast">
    <tr><td><xsl:value-of select="systemName"/></td>
        <td><xsl:value-of select="userName"/></td>
        <td>TO Mast</td>
        <td>
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
    </tr>
</xsl:template>
<xsl:template match="virtualsignalmast">
    <tr><td><xsl:value-of select="systemName"/></td>
        <td><xsl:value-of select="userName"/></td>
        <td>Virtual</td>
        <td>
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
    </tr>
</xsl:template>

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

<!-- template to show a particular signalmastlogic -->
<xsl:template name="oneSML"> <!--1 table per SML-->
    <!-- index through individual signalmastlogic elements -->
    <xsl:for-each select="signalmastlogic">
        <!--table header-->
        <h4>Source mast: <xsl:value-of select="sourceSignalMast"/></h4>
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
                    <td><xsl:value-of select="enabled"/></td>
                    <td><xsl:value-of select="useLayoutEditor"/></td>
                    <td><xsl:value-of select="comment"/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:for-each>
</xsl:template>

<xsl:template match="sensor">
<tr><td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
    <td><xsl:value-of select="userName"/></td>
    <td><xsl:if test='(@inverted = "true")'>Yes</xsl:if></td>
    <td><xsl:value-of select="comment"/></td>
</tr>
</xsl:template>

<xsl:template match="memory">
<tr>
  <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
  <td><xsl:value-of select="userName"/></td>
  <td><xsl:value-of select="comment"/></td>
</tr>
</xsl:template>

<xsl:template match="reporter">
<tr>
  <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
  <td><xsl:value-of select="userName"/></td>
  <td><xsl:value-of select="comment"/></td>
</tr>
</xsl:template>

<xsl:template match="logix">
<tr>
  <td><xsl:value-of select="systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
  <td><xsl:value-of select="userName"/></td>
</tr>
</xsl:template>

<xsl:template match="block">
<tr>
  <td><xsl:value-of select="@signal"/></td>
  <td><xsl:value-of select="@watchedturnout"/></td>
</tr>
</xsl:template>

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

<xsl:template match="section">
    <tr>
        <td><xsl:value-of select="@systemName"/></td> <!--names as attributes deprecated since 2.9.6-->
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
		<xsl:if test='( @systemName = translate($name,$lcletters,$ucletters) )' >
		<!-- here have found correct conditional -->
            <h4>Conditional <xsl:value-of select="@systemName"/>
            <xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
            </h4>
		    <xsl:for-each select="conditionalStateVariable">
		        <xsl:call-template name="conditionalStateVariable"/>
		    </xsl:for-each>
		    <p/>
		    <xsl:for-each select="conditionalAction">
		        <xsl:call-template name="conditionalAction"/>
		    </xsl:for-each>
        </xsl:if>        
    </xsl:for-each>
</xsl:template>

<xsl:template name="conditionalStateVariable">
<!-- decode operator -->
<xsl:choose>
  <xsl:when test="( @operator = 1 )" >And </xsl:when>
  <xsl:when test="( @operator = 2 )" >Not </xsl:when>
  <xsl:when test="( @operator = 3 )" >And not </xsl:when>
  <xsl:when test="( @operator = 4 )" ></xsl:when>  <!-- None -->
  <xsl:otherwise>(operator="<xsl:value-of select="@operator"/>") </xsl:otherwise>
</xsl:choose>
<!-- decode type -->
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
  <xsl:otherwise>(type="<xsl:value-of select="@type"/>")</xsl:otherwise>
</xsl:choose>
name="<xsl:value-of select="@systemName"/>"
<xsl:if test='@num1 != 0'>
num1="<xsl:value-of select="@num1"/>"
</xsl:if>
<xsl:if test='@num2 != 0'>
num2="<xsl:value-of select="@num2"/>"
</xsl:if>
<xsl:if test='@dataString !="N/A" and @dataString !=""'>
value="<xsl:value-of select="@dataString"/>"
</xsl:if>
<xsl:if test='@triggersCalc = "no"'>
<b>(Doesn't trigger calculation)</b>
</xsl:if>
<br/>
</xsl:template>

<xsl:template name="conditionalAction">
<xsl:if test='@type != 1'>
<!-- decode operator -->
<xsl:choose>
  <xsl:when test="( @option = 1 )" >On change to true: </xsl:when>
  <xsl:when test="( @option = 2 )" >On change to false: </xsl:when>
  <xsl:when test="( @option = 3 )" >On change: </xsl:when>
  <xsl:otherwise>(option="<xsl:value-of select="@option"/>") </xsl:otherwise>
</xsl:choose>
<!-- decode type -->
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
<br/>
</xsl:if>
</xsl:template>

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

<xsl:template match="paneleditor">
<h3>Panel: <xsl:value-of select="@name"/></h3>

    <!-- index through individual panel elements -->
    <xsl:apply-templates/>

</xsl:template>

<xsl:template match="LayoutEditor">
<h3>Layout Panel: <xsl:value-of select="@name"/></h3>
    <!-- index through individual panel elements -->
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="signalheadicon">
Signalhead Icon "<xsl:value-of select="@signalhead"/>"<br/>
</xsl:template>

<xsl:template match="signalmasticon">
Signalmast Icon "<xsl:value-of select="@signalmast"/>"<br/>
</xsl:template>

<xsl:template match="turnouticon">
Turnout Icon "<xsl:value-of select="@turnout"/>"<br/>
</xsl:template>

<xsl:template match="sensoricon">
Sensor Icon "<xsl:value-of select="@sensor"/>"<br/>
</xsl:template>

<xsl:template match="positionablelabel">
Positionable Label "<xsl:value-of select="@icon"/>"<br/>
</xsl:template>

<xsl:template match="layoutturnout">
Layout Turnout ident="<xsl:value-of select="@ident"/>", 
turnoutname="<xsl:value-of select="@turnoutname"/>",
blockname="<xsl:value-of select="@blockname"/>"<br/>
</xsl:template>

<xsl:template match="tracksegment">
Track segment ident="<xsl:value-of select="@ident"/>": 
connects to "<xsl:value-of select="@connect1name"/>" (type=<xsl:value-of select="@type1"/>);
connects to "<xsl:value-of select="@connect2name"/>" (type=<xsl:value-of select="@type2"/>)
<br/>
</xsl:template>

<xsl:template match="positionablepoint">
Positionable point ident="<xsl:value-of select="@ident"/>"
connects to "<xsl:value-of select="@connect1name"/>" (type=<xsl:value-of select="@text"/>);
connects to "<xsl:value-of select="@connect2name"/>" (type=<xsl:value-of select="@text"/>)
<br/>
</xsl:template>

<xsl:template match="locoicon">
Loco icon "<xsl:value-of select="@text"/>"<br/>
</xsl:template>

<xsl:template match="LightIcon">
Light Icon "<xsl:value-of select="@light"/>"<br/>
</xsl:template>

<xsl:template match="multisensoricon">
Multisensor Icon  
    <xsl:for-each select="active">"<xsl:value-of select="@sensor"/>" </xsl:for-each>
<br/>
</xsl:template>

<!-- Display version number in header -->
<xsl:template match="layout-config/jmriversion">
    JMRI version # <xsl:value-of select="major"/>.<xsl:value-of select="minor"/>.<xsl:value-of select="test"/><xsl:value-of select="modifier"/>
</xsl:template>

<!-- At the bottom, display JMRI load history -->
<xsl:template match="filehistory">
    <h3>History</h3>
    <table border="1">
    <tr><th>Date</th><th>File</th><th>Result</th></tr>
    <xsl:for-each select="operation">
        <tr>
            <td><xsl:value-of select="date"/></td>
            <td><xsl:value-of select="filename"/></td>
            <td>
                <xsl:choose>
                    <xsl:when test="type = 'app'" >Started JMRI</xsl:when>
                    <xsl:otherwise><xsl:value-of select="type"/></xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
    </xsl:for-each>
    </table>
</xsl:template>

</xsl:stylesheet>
