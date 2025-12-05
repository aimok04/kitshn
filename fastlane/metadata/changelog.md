Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

New releases will include `.flatpak` bundles for x86_64 and aarch64 architectures.
The flatpak/ jvm releases of kitshn is far from perfect and might be broken. Feel free to report any issues! :)

1. Hide **ingredients list** when recipe has only one step (#294).
2. Replacing **servings text** in import when it is smaller than 6 characters (#296).
3. Checked **compatibility** with Tandoor **version 2.3.3** to **version 2.3.6**.

## Commits

- feat(commonMain/import): replace servingsText when smaller than 6 characters (resolved #296)
- feat(commonMain/dialog): added "ignore" button to version compatibility dialog (#293)
- feat(commonMain/recipes): don't show merged ingredient list when recipe has only one step (resolved #294)
- feat(flatpak): added new flatpak directory with metadata
- feat(build.gradle): added appImage to targetFormats (for non Mac devices)
- fix(flatpak): added .java/.userPrefs as persistent folder
- fix(flatpak): changed socket to x11
- chore(libs): version bumps
- chore(libs): version bumps
- chore(gradle): bump compileSdk and targetSdk to 36
- github(workflows): added "Build flatpak on new version tag" workflow
- github(workflows): added "Build flatpak manually" workflow