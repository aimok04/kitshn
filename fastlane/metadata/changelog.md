Hey ✌️
This release adds **some changes** from the **beta** that I decided to **implement for v1**:

1. Implemented **fractional servings** for **meal plans** (#168).
2. Added support to display **hours** in duration chips (#173).
3. Implemented **shortcuts** for *meal plan, shopping, shopping mode* and *books* on Android (#164).
4. Fixed **food name** cell width being **too low** in some scenarios (#166).
5. Fixed **ingredients** appearing as **duplicates** when unchecking items in **ingredient allocation dialog** (#174).
6. 🇳🇱 **Dutch** has been brought **up to date** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @jstrvr).
7. 🇫🇷 **French** has been brought **up to date** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by @letroll).

## Highlights

- feat(commonMain/recipes): added unified formatDuration method and support for displaying hours (#173)
- feat(androidMain/shortcuts): added meal plan, shopping, shopping mode and books shortcuts (resolved #164)
- feat(androidMain/shortcuts): modernized icon of new instance shortcut
- feat(androidMain/acra): modernized icons in AcraCrashReportDialog
- feat(commonMain/mealplan): implemented fractional servings (resolved #168)
- fix(commonMain/recipes): keep food cell in ingredients table bigger (#166)
- fix(commonMain/recipes): duplicate ingredients when unchecking item in RecipeIngredientAllocationDialog (#174)
- l10n(French): translated using Weblate
- l10n(Dutch): translated using Weblate
- l10n(German): translated using Weblate