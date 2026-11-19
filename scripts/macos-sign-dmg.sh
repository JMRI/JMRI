#!/bin/bash
# Sign the JMRI macOS disk image made by diskimage.sh, and notarize it when an App Store Connect API key is set

set -euo pipefail

INPUT=${1:-}
OUTPUT=${2:-}
IDENTITY=${3:-}
KEYCHAIN=${4:-}

if [ -z "$INPUT" ] || [ -z "$OUTPUT" ] || [ -z "$IDENTITY" ]; then
    echo "usage: $0 INPUT.dmg OUTPUT.dmg IDENTITY [KEYCHAIN]" 1>&2
    echo "  IDENTITY is a Developer ID Application identity, or - for an ad-hoc signature (testing only)" 1>&2
    echo "  notarizes when NOTARY_KEY_PATH, NOTARY_KEY_ID and NOTARY_ISSUER are set" 1>&2
    exit 1
fi

notarize=false
if [ -n "${NOTARY_KEY_PATH:-}" ] && [ -n "${NOTARY_KEY_ID:-}" ] && [ -n "${NOTARY_ISSUER:-}" ]; then
    if [ "$IDENTITY" = "-" ]; then
        echo "ad-hoc signed images can't be notarized" 1>&2
        exit 1
    fi
    notarize=true
    notary=(--key "$NOTARY_KEY_PATH" --key-id "$NOTARY_KEY_ID" --issuer "$NOTARY_ISSUER")
fi

dmg_args=(--force --sign "$IDENTITY")
if [ "$IDENTITY" = "-" ]; then
    dmg_args+=(--timestamp=none)
else
    dmg_args+=(--timestamp)
fi
if [ -n "$KEYCHAIN" ]; then
    dmg_args+=(--keychain "$KEYCHAIN")
fi
codesign_args=("${dmg_args[@]}")
if [ "$IDENTITY" != "-" ]; then
    codesign_args+=(--options runtime)
fi

workdir=$(mktemp -d -t jmrisign)
indir="$workdir/in"
outdir="$workdir/out"
mkdir "$indir" "$outdir"

cleanup() {
    hdiutil detach "$indir" -force -quiet 2>/dev/null || true
    rm -rf "$workdir"
}
trap cleanup EXIT

# hdiutil sometimes fails with "Resource busy" on GitHub's Mac runners (actions/runner-images#7522), so retry it
hdi() {
    local attempt
    for attempt in 1 2 3 4; do
        hdiutil "$@" && return 0
        echo "hdiutil $1 failed, attempt $attempt" 1>&2
        sleep 10
    done
    if [ "$1" = detach ]; then
        hdiutil "$@" -force && return 0
    fi
    return 1
}

sign() {
    xattr -cr "$1"
    codesign "${codesign_args[@]}" "$1"
}

# one submission per file, never retried: repeated failures can get the developer account disabled
submit() {
    local result id status rc=0
    echo "notarizing $(basename "$1")"
    result=$(xcrun notarytool submit "$1" "${notary[@]}" --wait --timeout 1h --output-format json) || rc=$?
    echo "$result"
    id=$(echo "$result" | plutil -extract id raw -o - - 2>/dev/null || true)
    status=$(echo "$result" | plutil -extract status raw -o - - 2>/dev/null || true)
    if [ -n "$id" ]; then
        xcrun notarytool log "$id" "${notary[@]}" || true
    fi
    if [ $rc -ne 0 ] || [ "$status" != "Accepted" ]; then
        echo "notarization of $(basename "$1") failed: $status" 1>&2
        return 1
    fi
}

# stapling can lag behind notarization for a bit, so retrying it is fine
staple() {
    local attempt
    for attempt in 1 2 3 4 5; do
        xcrun stapler staple "$1" && return 0
        sleep 30
    done
    return 1
}

# copy the image contents to a folder, sign them there, then build the final image from the folder
hdi attach "$INPUT" -mountpoint "$indir" -nobrowse -readonly
volname=$(diskutil info "$indir" | sed -n 's/^ *Volume Name: *//p')
ditto "$indir/JMRI" "$outdir/JMRI"
hdi detach "$indir"
jmri="$outdir/JMRI"

# native libraries inside jars: pull each one out, sign it, and put it back
count=0
while IFS= read -r -d '' jar; do
    entries=$(zipinfo -1 "$jar" 2>/dev/null | grep -E '\.(dylib|jnilib)$' || true)
    [ -z "$entries" ] && continue
    jartmp="$workdir/jar"
    rm -rf "$jartmp" && mkdir "$jartmp"
    while IFS= read -r entry; do
        unzip -qo "$jar" "$entry" -d "$jartmp"
        echo "sign $(basename "$jar"): $entry"
        sign "$jartmp/$entry"
        (cd "$jartmp" && zip -q "$jar" "$entry")
        count=$((count + 1))
    done <<< "$entries"
done < <(find "$jmri" -name '*.jar' -print0)
echo "signed $count native libraries inside jars"

# native libraries outside jars
while IFS= read -r -d '' lib; do
    echo "sign $lib"
    sign "$lib"
done < <(find "$jmri" -type f \( -name '*.dylib' -o -name '*.jnilib' \) -print0)

# the application bundles
for app in "$jmri"/*.app; do
    echo "sign $app"
    sign "$app"
done

# notarize and staple the apps too, so they still pass Gatekeeper offline once copied out of the image
if [ "$notarize" = true ]; then
    mkdir "$workdir/apps"
    for app in "$jmri"/*.app; do
        ditto "$app" "$workdir/apps/$(basename "$app")"
    done
    ditto -c -k --sequesterRsrc --keepParent "$workdir/apps" "$workdir/apps.zip"
    submit "$workdir/apps.zip"
    for app in "$jmri"/*.app; do
        staple "$app"
    done
fi

ln -s /Applications "$outdir/Applications"
rm -f "$OUTPUT"
hdi create -ov -srcfolder "$outdir" -volname "$volname" -fs HFS+ -format UDZO -imagekey zlib-level=9 "$OUTPUT"
hdiutil verify "$OUTPUT" -quiet
codesign "${dmg_args[@]}" "$OUTPUT"
echo "signed $OUTPUT"

if [ "$notarize" = false ]; then
    echo "no notary key, skipping notarization"
    exit 0
fi
submit "$OUTPUT"
staple "$OUTPUT"
xcrun stapler validate "$OUTPUT"
