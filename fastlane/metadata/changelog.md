Hey ✌️
This is the second version of kitshn **compatible** with the API changes for **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-alpha-1)**. 🥳
New versions of kitshn will **no longer** support instances on **v1.5.***.

This beta is available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Although I have tested the app with **Tandoor v2.0.0-alpha-1**, there could still be some minor API
changes that I might have missed, so **please feel free** to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

1. Implemented **fractional servings** for **meal plans** (#168).
2. Added support to display **hours** in duration chips (#173).
3. Implemented **shortcuts** for *meal plan, shopping, shopping mode* and *books* on Android (#164).
4. Fixed **food name** cell width being **too low** in some scenarios (#166).
5. Fixed **ingredients** appearing as **duplicates** when unchecking items in **ingredient allocation dialog** (#174).
6. Fixed **space switching** dialog showing **error message** when logged in with **API token**.
7. Implemented small **UI improvements**.
8. 🇳🇱 **Dutch** has been brought **up to date** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @jstrvr).
9. 🇫🇷 **French** has been brought **up to date** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @letroll).

## Highlights

- feat(commonMain/mealplan): implemented fractional servings (resolved #168)
- feat(commonMain/recipes): added unified formatDuration method and support for displaying hours (resolved #173)
- feat(androidMain/shortcuts): added meal plan, shopping, shopping mode and books shortcuts (resolved #164)
- feat(androidMain/shortcuts): modernized icon of new instance shortcut
- feat(androidMain/acra): modernized icons in AcraCrashReportDialog
- feat(commonMain/navigation): replaced "default" icons and added inactive/active icons
- feat(commonMain/api): added server version breadcrumb
- fix(commonMain/recipes): keep food cell in ingredients table bigger (resolved #166)
- fix(commonMain/recipes): duplicate ingredients when unchecking item in RecipeIngredientAllocationDialog (resolved #174)
- fix(commonMain/strings.xml): removed unused error string
- fix(commonMain/ui): removed isSupported check in SpaceSwitchIconButton
- l10n(French): translated using Weblate
- l10n(Dutch): translated using Weblate
- l10n(Ukrainian): translated using Weblate
- l10n(German): translated using Weblate