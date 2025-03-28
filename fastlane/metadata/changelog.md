Hey ✌️
I decided to release **kitshn v1.0.0** — this will be the **final release** compatible with *
*Tandoor v1** 🥳
I will now focus on working on compatibility with **Tandoor v2** because there have been some API
and
functionality changes.

I'll consider releasing beta builds on **GitHub**, **Google Play** and **TestFlight** when the main
migrations have been done! :)

This week's release just contains some small new features and minor improvements:

1. Implemented support to **select sharing users** in **meal plan** creation/ edit dialog (#148).
2. Implemented support to **select sharing users** in **recipe book** creation/ edit dialog (#154).
3. Added check to **prevent empty steps** when **importing recipes** (#157).
4. Checked **compatibility** with Tandoor **version 1.5.34**.

## Highlights

- feat(commonMain/mealplan): implemented support to select sharing users in creation/edit dialog (
  resolved #148)
- feat(commonMain/recipeBooks): implemented support to select sharing users in creation/edit
  dialog (resolved #154)
- feat(commonMain/import): added check to prevent empty steps (resolved #157)
- feat(androidMain): removed datastore to multiplatform settings migration
- l10n(Ukrainian): translated using Weblate
- l10n(German): translated using Weblate
- chore(commonMain/api): marked v1.5.34 as compatible