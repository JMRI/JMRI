#! /bin/sh
#
# Script to create symbolic links for JMRI web site
#
# See http://jmri.org/help/en/html/doc/Technical/WebSite.shtml#link
# 
# First argument is path to a JMRI/JMRI repository to be used.
# Second argument is path to a JMRI/website repository to be used
#
# typical use:
#   ./setSymLinks.sh  ~me/git/JMRI ~me/git/website

ln -sf $2/COPYING .
ln -sf $2/copyright.shtml .
ln -sf $2/Footer .
ln -sf $2/Footer.shtml .
ln -sf $2/Header .
ln -sf $2/Header.shtml .
ln -sf $2/Help.html .
ln -sf $2/SiteCredit.shtml .
ln -sf $2/Sidebar .
ln -sf $2/Sidebar.shtml .
ln -sf $2/Style .
ln -sf $2/Style.shtml .
ln -sf $2/community .
ln -sf $2/contact .
ln -sf $2/css .
ln -sf $2/donations.shtml .
ln -sf $2/download .
ln -sf $2/exclude-list .
ln -sf $2/favicon.ico .
ln -sf $2/hardware .
ln -sf $2/images .
ln -sf $2/include-list .
ln -sf $2/index.shtml .
ln -sf $2/info.txt .
ln -sf $2/install .
ln -sf $2/manual .
ln -sf $2/nbproject .
ln -sf $2/pdf .
ln -sf $2/problem-report.php .
ln -sf $2/releaselist .
ln -sf $2/releasenotes .
ln -sf $2/robots.txt .
ln -sf $2/sitemap .
ln -sf $2/swf .
ln -sf $2/templates .
ln -sf $2/tools .
ln -sf $2/update.pl .

ln -sf $1/web/ .
ln -sf $1/resources .
ln -sf $1/jython .
ln -sf $1/help .
ln -sf $1/xml .
