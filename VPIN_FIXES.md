# CS VPIN Mode — Refactor Notes

## Why this branch exists

PR #15208 received review feedback from icklesteve pointing out that the original
implementation added a Mode column to the Light Table for *all* hardware types, not
just DCC-EX. He pointed to `CbusReporterManager` as the right pattern: override
`getKnownBeanProperties()` so the column only appears when DCC-EX hardware is active.

This branch refactors the original PR branch (`dccex-cs-vpin-mode`) to implement that
pattern, plus fixes several related issues found during the review.

---

## What changed

### Removed
- `HasLightMode.java` — the interface-based approach (added a Mode column for all hardware)
- `MODECOL` and all `HasLightMode` instanceof checks from `LightTableDataModel`
- `setMode()` / `getMode()` / `getValidModes()` / `getValidModeNames()` from `DCCppLight`
- Backward-compat `loadLights()` override in `DCCppLightManagerXml` (PR never merged to
  master; no real-world files use the old `mode` XML attribute)
- `mode` attribute from `lights-4-19-2.xsd`

### Added / changed
- `DCCppLightManager.getKnownBeanProperties()` — returns a `SelectionPropertyDescriptor`
  for the `LightMode` bean property; column only appears when DCC-EX light manager is active
- `DCCppLightManager.DCCPP_LIGHT_MODE_KEY = "LightMode"` — `public static final` key
- `DCCppLight.MODE_NAMES = {"Accessory Decoder", "CS VPIN"}` — `public static final`,
  annotated `@SuppressFBWarnings("MS_PKGPROTECT")` per JMRI convention
- `DCCppLight.setState()` reads the bean property at call time to pick `<z>` vs `<a>`
- `DCCppLightManagerXml` simplified to minimal form; persistence is handled automatically
  by `AbstractNamedBeanManagerConfigXML` via `<properties>` XML elements
- `Turnout.DIRECTPIN` renamed to `Turnout.CS_VPIN` (value unchanged: 512) with improved Javadoc
- `DCCppVpinMode.xml` fixture updated to use `<properties>` element format

### Tests
- `DCCppLightTest` — rewrote all mode tests to use `setProperty`/`getProperty` API
- `DCCppLightManagerXmlTest` — rewrote round-trip test; removed legacy load test
- `LightTableDataModelTest` — column count updated (removed MODECOL)
- `TurnoutOperationManagerTest` / `DCCppTurnoutTest` — `DIRECTPIN` → `CS_VPIN`

---

## Test status

944 tests, 0 failures (DCC-EX suite + LightTableDataModel + configurexml round-trip +
TurnoutOperationManager).

SpotBugs: 0 issues in our changed files. 3 pre-existing bugs in `DCCppSerialPortController`
and `DCCppSystemConnectionMemo` (present in master, unrelated to this PR).

Checkstyle: 0 violations in our changed files.

---

## Discord / PR context

The DCC-EX community had some debate about the *turnout* side of the PR. Summary:

- **Lights**: uncontroversial. `<z>` for GPIO-driven lights/relays is exactly the right tool.
- **Turnouts**: UKBloke's concern is that `<z>` is pin-level control, and only ~1% of
  DCC-EX turnouts are raw GPIO pins. Using CS VPIN mode for a normal DCC-EX turnout would
  be wrong. His concern is framing/description, not that the feature is broken.
- **Ash** suggested updating the PR description to be clearer: this adds a *JMRI table
  output mode*, not a new DCC-EX turnout type. Opt-in, no breaking changes.
- **haba** asked for "reasonable names" — our rename from `DIRECTPIN` to `CS_VPIN` helps here.

**Before pushing to update the PR**, the description should be updated along Ash's lines,
and a PR comment should be added explaining what changed since the last review
(specifically: icklesteve's `getKnownBeanProperties()` suggestion is now implemented).

---

## Next steps

1. **Bench test** on real DCC-EX hardware (see below)
2. **Update PR description** — clarify turnout CS VPIN is an edge case for raw GPIO-driven
   VPINs, not normal DCC-EX turnouts; no breaking changes; opt-in only
3. **Post a PR comment** addressing icklesteve's feedback — explain we switched from
   `HasLightMode` to `getKnownBeanProperties()` + `SelectionPropertyDescriptor`
4. **Push** `dccex-cs-vpin-mode` (merge this fix branch back first)

---

## Bench test plan

Previous bench test (recorded in PR comments) was done against the *old* implementation
(mode attribute in XML). The current implementation uses bean properties. Run through
all of the following with the refactored code:

**Hardware setup (replicate previous):**
- GPIO VPIN for a light: JMRI light in CS VPIN mode
- GPIO VPIN for a turnout: JMRI turnout with CS VPIN feedback mode
- DCC-EX monitor open to watch raw commands

**Test cases:**
1. Light table — default mode is "Accessory Decoder", `<a>` command goes out on toggle
2. Light table — change mode to "CS VPIN", `<z vpin>` / `<z -vpin>` goes out on toggle
3. Turnout table — CS VPIN feedback mode, `<z>` commands on throw/close
4. Save panel file → close PanelPro → reopen → verify both light mode and turnout
   feedback mode survive the round-trip (bean properties in `<properties>` XML)
5. Verify Mode column in Light Table is absent when using a non-DCC-EX connection
   (e.g. LocoNet or just no DCC-EX manager loaded)
6. Verify no regression on standard Accessory Decoder lights (default mode, `<a>` command)
