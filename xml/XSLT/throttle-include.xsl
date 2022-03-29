<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" encoding="UTF-8"/>


  <!-- To be included in throttle-layout-config.xsl and throttle-config.xsl -->

 
  <!-- Display position-->
  <xsl:template name="window">
    <xsl:if test='window/@isVisible = "true"'>(Visible)</xsl:if>
    <xsl:if test='window/@isIconified = "true"'>(Iconified)</xsl:if>
    [XYPosition:  (<xsl:value-of select="window/@x"/>,<xsl:value-of select="window/@y"/>)
     Height/Width:  (<xsl:value-of select="window/@height"/>/<xsl:value-of select="window/@width"/>)]
  </xsl:template>

  <!-- Display control panel subwindow -->
  <xsl:template match="ThrottleFrame/ControlPanel">
    <h4>Control Panel   <xsl:call-template name="window"/></h4>
    <xsl:apply-templates/>
    <table border="border-width:thin" width="50%">
      <tr><th>Attribute</th><th>Value</th></tr>      
      <xsl:for-each select="@*"> 
        <tr>
          <td><xsl:value-of select="name()"/></td>
          <td>
          <xsl:choose>
            <xsl:when test='name() ="displaySpeedSlider"'>              
              <xsl:choose>
                <xsl:when test='. = 0'>0 (Percentage)</xsl:when>
                <xsl:when test='. = 1'>1 (Speed Steps)</xsl:when>
                <xsl:when test='. = 2'>2 (Shunting)</xsl:when>
                <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>        
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
          </td>
        </tr>
       </xsl:for-each>
    </table>
  </xsl:template>


  <!-- Display function panel subwindow -->
  <xsl:template match="ThrottleFrame/FunctionPanel">
    <h4>Function Panel  <xsl:call-template name="window"/></h4>
    <table border="border-width:thin" width="100%">
      <tr>
        <th>Button ID</th>
        <th>Text</th>
        <th>Lockable</th>
        <th>Visible</th>
        <th>fontSize</th>
        <th>Icon</th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="FunctionButton">
    <tr>
     <td style="text-align:center"><xsl:value-of select="@id"/></td>
     <td><xsl:value-of select="@text"/></td>
     <td style="text-align:center"><xsl:value-of select="@isLockable"/></td>
     <td style="text-align:center"><xsl:value-of select="@isVisible"/></td>
     <td style="text-align:center"><xsl:value-of select="@fontSize"/></td>
     <td>ImageSize: <xsl:value-of select="@buttonImageSize"/> 
       <xsl:if test='@iconPath != ""'><br/>"Off" Icon: <xsl:value-of select="@iconPath"/></xsl:if> 
       <xsl:if test='@selectedIconPath != ""'><br/>"On" Icon: <xsl:value-of select="@selectedIconPath" /></xsl:if>
     </td>
   </tr>
  </xsl:template>

  <!-- Display address panel subwindow -->
  <xsl:template match="ThrottleFrame/AddressPanel">
    <h4>Address Panel   <xsl:call-template name="window"/></h4>
    <table border="border-width:thin" width="50%">
      <tr>
        <th>Loco Number</th>
        <th>Attributes</th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="locoaddress">
    <xsl:if test='number > 0'>
      <tr>
        <td>
          <xsl:if test='number > 0'>
            <xsl:value-of select="number"/>
          </xsl:if>
        </td>
        <td>
          <xsl:if test='dcclocoaddress/@number > 0'>DCC </xsl:if>
          <xsl:if test='dcclocoaddress/@longaddress = "yes"'> [Long]</xsl:if>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- Display speed panel subwindow -->
  <xsl:template match="ThrottleFrame/SpeedPanel">
    <h4>Speed Panel     <xsl:call-template name="window"/></h4>
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Display Jynstrument panel subwindow -->
  <xsl:template match="ThrottleFrame/Jynstrument">
    <h4>Panel Jynstrument  <xsl:call-template name="window"/></h4>
    <table border="border-width:thin" width="50%">
      <!-- Future proofing [only one attribute at current time 2022/03/25] -->
      <xsl:for-each select="@*"> 
        <tr>
          <td><xsl:value-of select="name()"/></td>
          <td><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="./*"> 
      <!-- Future proofing ["window" is only element at current time 2022/03/25] -->
       <xsl:if test='name() != "window"'>
        <tr>
          <td><xsl:value-of select="name()"/></td>
          <td><xsl:for-each select="@*">
            <xsl:value-of select="name()"/>: <xsl:value-of select="."/><br/></xsl:for-each>
          </td>
        </tr>
       </xsl:if>
      </xsl:for-each>
    </table>
    <xsl:apply-templates/>
  </xsl:template>

  
  </xsl:stylesheet>
  
