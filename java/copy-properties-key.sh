#!/bin/bash
#
# Copy a key and value from one set of properties files to another set,
# including all existing locales for that key
#
# Example:
# ./java/copy-properties-key.sh jmri.jmrit.simpleprog.SimpleProgAction java/src/apps/ActionListBundle SimpleProgStartupAction java/src/jmri/jmrit/simpleprog/Bundle

# The name of the original key in the properties file
source_key=$1
# The path to the original default properties file
source_prp=${2%".properties"}
# The name of the new key
dest_key=$3
# The path to the destination properties file; must be a file name, will
# be created if it does not exist
dest_prp=${4%".properties"}

for source in ${source_prp}*.properties ; do
	#echo "Source: ${source}"
	lang=${source#"${source_prp}"}
	lang=${lang%".properties"}
	#echo "Locale: ${lang}"
	dest="${dest_prp}${lang}.properties"
	#echo "Destination: ${dest}"
	value=$( echo $( grep "^${source_key}" ${source} | cut -d= -f2 ) )
	#echo "Value: ${value}"
        [ -n "$value" ] && echo ${dest_key}=${value} >> ${dest}
done
