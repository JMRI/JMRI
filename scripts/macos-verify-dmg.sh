#!/bin/bash
# Check that everything macos-sign-dmg.sh should have signed in a JMRI disk image carries a valid signature

set -euo pipefail

DMG=${1:-}
if [ -z "$DMG" ]; then
    echo "usage: $0 IMAGE.dmg" 1>&2
    exit 1
fi

workdir=$(mktemp -d -t jmriverify)
mnt="$workdir/mnt"
mkdir "$mnt"
cleanup() {
    hdiutil detach "$mnt" -quiet 2>/dev/null || true
    rm -rf "$workdir"
}
trap cleanup EXIT

failures=0
checked=0
check() {
    checked=$((checked + 1))
    if ! codesign --verify --strict "$1" 2>"$workdir/err"; then
        echo "NOT SIGNED: $2" 1>&2
        cat "$workdir/err" 1>&2
        failures=$((failures + 1))
    fi
}

codesign --verify --strict "$DMG" || { echo "NOT SIGNED: $DMG" 1>&2; exit 1; }
dmg_info=$(codesign -dv "$DMG" 2>&1)
adhoc=false
if [[ "$dmg_info" == *"Signature=adhoc"* ]]; then
    adhoc=true
fi

hdiutil attach "$DMG" -mountpoint "$mnt" -nobrowse -readonly -quiet
jmri="$mnt/JMRI"

for app in "$jmri"/*.app; do
    check "$app" "$(basename "$app")"
done

while IFS= read -r -d '' lib; do
    check "$lib" "${lib#"$jmri"/}"
done < <(find "$jmri" -type f \( -name '*.dylib' -o -name '*.jnilib' \) -print0)

while IFS= read -r -d '' jar; do
    entries=$(zipinfo -1 "$jar" 2>/dev/null | grep -E '\.(dylib|jnilib)$' || true)
    [ -z "$entries" ] && continue
    jartmp="$workdir/jar"
    rm -rf "$jartmp" && mkdir "$jartmp"
    while IFS= read -r entry; do
        unzip -qo "$jar" "$entry" -d "$jartmp"
        check "$jartmp/$entry" "$(basename "$jar"): $entry"
    done <<< "$entries"
done < <(find "$jmri" -name '*.jar' -print0)

echo "checked $checked signatures, $failures not valid"
[ $failures -eq 0 ] || exit 1

if [ "$adhoc" = true ]; then
    echo "ad-hoc signature: skipping Developer ID, notarization and Gatekeeper checks"
    exit 0
fi

if [[ "$dmg_info" != *"Authority=Developer ID Application"* ]]; then
    echo "not signed with a Developer ID" 1>&2
    exit 1
fi
for app in "$jmri"/*.app; do
    xcrun stapler validate "$app"
    spctl --assess --type execute -v "$app"
done
xcrun stapler validate "$DMG"
spctl --assess --type open --context context:primary-signature -v "$DMG"
