#!/bin/bash
#
# Remove the Apple provided Java SE 6
#
# Takes an optional argument specifying the root directory of the OS X
# installation. This argument defaults to /

if [ "${EUID}" -ne 0 ] ; then
    sudo ${0}
    exit $?
fi

exit_result=0
system_root=${1:-/}

if [ ! -L "${system_root}/usr/bin/java" ] ; then
    system_root="/Volumes/Macintosh\ HD"
fi
if [ ! -L "${system_root}/usr/bin/java" ] ; then
    echo "Unable to determine OS X installation volume."
    echo "Please specify installation volume when running script."
    exit 2
fi

pkg_volume="${system_root}"

cd "${system_root}"

if [ "${system_root}" == "/" ] ; then
    system_root=
fi


echo "Removing Apple Java SE 6..."

for p in $( pkgutil --volume "${pkg_volume}" --packages | grep com.apple.pkg.Java ) ; do
    echo "Removing files for package ${p}..."
    while read f ; do
        if [ $( pkgutil --volume "${pkg_volume}" --file-info "${f}" | grep ^pkgid | grep -c -v com.apple.pkg.Java ) -eq 0 ] ; then
            if [ -e "${system_root}/${f}" ] ; then
                echo "Removing ${system_root}/${f}..."
                rm -v -r "${system_root}/${f}"
                result=$?
                if [[ $result -ne 0 && -f "${system_root}/${f}" ]] ; then
                    echo "Unable to uninstall Apple Java SE 6."
                    echo "Disable System Integrity Protection and try again."
                    exit 64
                fi
            fi
        else
            packages=$( pkgutil --volume "${pkg_volume}" --file-info "${f}" | grep ^pkgid | grep -v com.apple.pkg.Java )
            echo "Not removing ${system_root}/${f} belonging to ${packages}."
        fi
    done < <( pkgutil --volume "${pkg_volume}" --files $p ) 
done

for p in $( pkgutil --volume "${pkg_volume}" --packages ${system_root} | grep com.apple.pkg.Java ) ; do
    okay_to_delete="yes"
    while read f ; do
        if [[ -e "${system_root}/$f" && $( pkgutil --volume "${pkg_volume}" --file-info "${f}" | grep ^pkgid | grep -c -v com.apple.pkg.Java ) -eq 0 ]] ; then
            echo "${system_root}/${f} still needs to be removed"
            okay_to_delete="no"
            break
        fi
    done < <( pkgutil --volume "${pkg_volume}" --files $p ) 
    if [ "${okay_to_delete}" == "yes" ] ; then
        pkgutil --volume "${pkg_volume}" --forget ${p} 2>/dev/null
        echo "Deleting receipts for ${p}..."
        rm -v ${pkg_volume}/System/Library/Receipts/${p}.*
        echo "Removed package ${p}."
    else
        echo "Unable to remove package ${p}."
        exit_result=1
    fi
done

if [ ${exit_result} -eq 0 ] ; then
    echo "Removed Apple Java SE 6."
fi

if [ $( csrutil status | grep -c "disabled" ) -eq 1 ] ; then
    echo "Enable System Integrity Protection."
fi

exit ${exit_result}
