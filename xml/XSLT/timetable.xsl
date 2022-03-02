<?xml version="1.0" encoding="iso-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2022')" />
<xsl:output method="html" encoding="ISO-8859-1"/>


<xsl:template match='timetable-data'>
  <html>
    <head>
      <title>JMRI Timetable</title>

      <style>
        .ctr {text-align: center;}
        .right {text-align: right;}
      </style>

    </head>

    <body>
      <h1>JMRI Timetable</h1>

      <xsl:apply-templates/>

      <p></p>
      <hr/>
      <p/>This page was produced by <a href="http://jmri.org">JMRI</a>.
      <p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.
      <p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
      <p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
    </body>
  </html>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through layout elements -->
<xsl:template match="timetable-data/layouts">
  <h3>Layouts</h3>
  <table border="1">
    <tr>
      <th>Layout Name</th>
      <th>Scale</th>
      <th>Fast Clock</th>
      <th>Throttles</th>
      <th>Metric</th>
    </tr>

    <!-- index through individual layout elements -->
    <xsl:apply-templates/>

  </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Output the layout detail row -->
<xsl:template match="layout">
  <tr>
      <td><xsl:value-of select="layout_name"/></td>
      <td class="ctr"><xsl:value-of select="scale"/></td>
      <td class="ctr"><xsl:value-of select="fast_clock"/></td>
      <td class="ctr"><xsl:value-of select="throttles"/></td>
      <td class="ctr"><xsl:value-of select="metric"/></td>
  </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through train type elements -->
<xsl:template match="timetable-data/train_types">
  <h3>Train Types</h3>
  <table border="1">
    <tr>
      <th>Layout</th>
      <th>Type Name</th>
      <th>Type Color</th>
    </tr>

    <!-- index through individual train_type elements -->
    <xsl:apply-templates/>

  </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Output the train_type detail row -->
<xsl:template match="train_type">
  <tr>
    <td><xsl:call-template name="getLayoutName"/></td>
    <td><xsl:value-of select="type_name"/></td>
    <td class="ctr"><xsl:value-of select="type_color"/></td>
  </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through segment elements -->
<xsl:template match="timetable-data/segments">
  <h3>Segments</h3>
  <table border="1">
    <tr>
      <th>Layout</th>
      <th>Segment Name</th>
    </tr>

    <!-- index through individual segment elements -->
    <xsl:apply-templates/>

  </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Output the segment detail row -->
<xsl:template match="segment">
  <tr>
    <td><xsl:call-template name="getLayoutName"/></td>
    <td><xsl:value-of select="segment_name"/></td>
  </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through station elements -->
<xsl:template match="timetable-data/stations">
  <h3>Stations</h3>
  <table border="1">
    <tr>
      <th>Segment Name</th>
      <th>Station Name</th>
      <th>Distance</th>
      <th>Double Track</th>
      <th>Sidings</th>
      <th>Staging</th>
    </tr>

    <!-- index through individual station elements -->
    <xsl:apply-templates/>

  </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Output the station detail row -->
<!-- Get the segment name from the segment section -->
<xsl:template match="station">
  <xsl:variable name="matchID"><xsl:value-of select="segment_id" /></xsl:variable>

  <tr>
    <td>
      <xsl:for-each select="/timetable-data/segments/segment">
        <xsl:if test="segment_id = $matchID">
          <xsl:value-of select="segment_name"/>
        </xsl:if>
      </xsl:for-each>
    </td>
    <td><xsl:value-of select="station_name"/></td>
    <td class="right"><xsl:value-of select="distance"/></td>
    <td class="ctr"><xsl:value-of select="double_track"/></td>
    <td class="ctr"><xsl:value-of select="sidings"/></td>
    <td class="ctr"><xsl:value-of select="staging"/></td>
  </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through schedule elements -->
<xsl:template match="timetable-data/schedules">
  <h3>Schedules</h3>
  <table border="1">
    <tr>
      <th>Layout Name</th>
      <th>Schedule Name</th>
      <th>Effective Date</th>
      <th>Start Hour</th>
      <th>Duration</th>
    </tr>

    <!-- index through individual schedule elements -->
    <xsl:apply-templates/>

  </table>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Output the schedule detail row -->
<xsl:template match="schedule">
  <tr>
    <td><xsl:call-template name="getLayoutName"/></td>
    <td><xsl:value-of select="schedule_name"/></td>
    <td class="ctr"><xsl:value-of select="eff_date"/></td>
    <td class="ctr"><xsl:value-of select="start_hour"/></td>
    <td class="ctr"><xsl:value-of select="duration"/></td>
  </tr>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Index through train elements -->
<xsl:template match="timetable-data/trains">
  <h3>Trains/Stops</h3>

    <!-- index through individual train elements -->
    <xsl:apply-templates/>

</xsl:template>

<!-- *************************************************************************************** -->
<!-- Output the train detail row -->
<!-- Get the schedule name from the schedule section -->
<xsl:template match="train">
  <xsl:variable name="schedID"><xsl:value-of select="schedule_id" /></xsl:variable>
  <xsl:variable name="typeID"><xsl:value-of select="type_id" /></xsl:variable>
<!--
  <xsl:variable name="trainID"><xsl:value-of select="train_id" /></xsl:variable>
 -->

  <table border="1">
    <tr>
      <th>Schedule</th>
      <th>Train Name</th>
      <th>Description</th>
      <th>Train Type</th>
      <th>Default Speed</th>
      <th>Start Time</th>
      <th>Throttle</th>
      <th>Notes</th>
    </tr>

    <tr>
      <td>
        <xsl:for-each select="/timetable-data/schedules/schedule">
          <xsl:if test="schedule_id = $schedID">
            <xsl:value-of select="schedule_name"/>
          </xsl:if>
        </xsl:for-each>
      </td>
      <td><xsl:value-of select="train_name"/></td>
      <td><xsl:value-of select="train_desc"/></td>
      <td>
        <xsl:for-each select="/timetable-data/train_types/train_type">
          <xsl:if test="type_id = $typeID">
            <xsl:value-of select="type_name"/>
          </xsl:if>
        </xsl:for-each>
      </td>
      <td class="ctr"><xsl:value-of select="default_speed"/></td>
      <td class="right">
        <xsl:call-template name="convertTime">
          <xsl:with-param name='minutes'><xsl:value-of select="start_time" /></xsl:with-param>
        </xsl:call-template>
      </td>
      <td class="ctr"><xsl:value-of select="throttle"/></td>
      <td><xsl:value-of select="train_notes"/></td>
    </tr>
  </table>

  <!-- Output the stop details for the current train -->
  <xsl:call-template name="trainStops"/>

</xsl:template>

<!-- *************************************************************************************** -->
<!-- Output the stop details for the current train -->
<xsl:template name="trainStops">
  <xsl:variable name="trainID"><xsl:value-of select="train_id" /></xsl:variable>

  <div style="margin-left: 2em; margin-bottom: 1em">
    <table border="1">
      <tr>
        <th>Station</th>
        <th>Duration</th>
        <th>Next Speed</th>
        <th>Arrive Time</th>
        <th>Depart Time</th>
        <th>Staging Track</th>
        <th>Notes</th>
      </tr>

      <xsl:for-each select="/timetable-data/stops/stop">
        <xsl:if test="train_id = $trainID">
          <xsl:variable name="stationID"><xsl:value-of select="station_id" /></xsl:variable>

          <tr>

            <td>
              <xsl:for-each select="/timetable-data/stations/station">
                <xsl:if test="station_id = $stationID">
                  <xsl:value-of select="station_name"/>
                </xsl:if>
              </xsl:for-each>
            </td>

            <td class="right"><xsl:value-of select="duration"/></td>
            <td class="right"><xsl:value-of select="next_speed"/></td>

            <td class="right">
              <xsl:call-template name="convertTime">
                <xsl:with-param name='minutes'><xsl:value-of select="arrive_time" /></xsl:with-param>
              </xsl:call-template>
            </td>

            <td class="right">
              <xsl:call-template name="convertTime">
                <xsl:with-param name='minutes'><xsl:value-of select="depart_time" /></xsl:with-param>
              </xsl:call-template>
            </td>

            <td class="ctr"><xsl:value-of select="staging_track"/></td>
            <td><xsl:value-of select="stop_notes"/></td>
          </tr>
        </xsl:if>
      </xsl:for-each>

    </table>
  </div>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- Some elements displayed specifically, don't show by default -->
<xsl:template match="stops" />
<xsl:template match="stop" />

<!-- *************************************************************************************** -->
<!-- Get layout name :: called from train type, segment and schedule -->
<xsl:template name='getLayoutName'>
  <xsl:variable name="matchID"><xsl:value-of select="layout_id" /></xsl:variable>

  <xsl:for-each select="/timetable-data/layouts/layout">
    <xsl:if test="layout_id = $matchID">
      <xsl:value-of select="layout_name"/>
    </xsl:if>
  </xsl:for-each>
</xsl:template>

<!-- *************************************************************************************** -->
<!-- convert minutes to hh:mm :: called from train and stop -->
<xsl:template name='convertTime'>
  <xsl:param name='minutes' />
  <xsl:variable name='hh'><xsl:value-of select='format-number(floor($minutes div 60), "00")' /></xsl:variable>
  <xsl:variable name='mm'><xsl:value-of select='format-number(($minutes mod 60), "00")' /></xsl:variable>
  <xsl:value-of select='concat($hh, ":", $mm)' />
</xsl:template>

</xsl:stylesheet>
