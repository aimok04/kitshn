Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

This update **adds** some **minor** improvements, fixes and **adds compatibility** for Tandoor v2 beta _2_, _3_, _4_ and _5_.

1. Added **activity** card and bottom sheet to **recipe details** view (#194).
2. Added **auto ingredient sorting** when **importing** recipes (#186).
3. Added **option** to add a **comment** when creating a **cook log**.
4. Added **merging ingredients** with shared **food**, **unit** and **note** in **recipe details** view (#217).
5. Reimplemented **auto focus** of **food input field** when adding item to **shopping list** on iOS (#141).
6. Checked **compatibility** with Tandoor **version 2.0.0-beta-2**.
7. Checked **compatibility** with Tandoor **version 2.0.0-beta-3**.
8. Checked **compatibility** with Tandoor **version 2.0.0-beta-4**.
9. Checked **compatibility** with Tandoor **version 2.0.0-beta-5**.

## Commits

- feat(commonMain/import): added auto ingredient sorting to RecipeImportCommon.kt (resolved #186)
- feat(commonMain/recipes): added bottom sheet for adding comment to cook log in RouteRecipeCookPageDone
- feat(commonMain/recipes): added "Activity" card and bottom sheet dialog which display cook logs like the web app does (resolved #194)
- feat(commonMain/recipes): merge ingredients with shared unit, food and notes in RecipeDetails.kt (resolved #217)
- feat(commonMain): updated funding banner to api v2
- fix(commonMain/import): added position parameters to SettingsSwitchListItems in RecipeImportCommon.kt
- fix(commonMain/recipes): bug where ingredient amount gets multiplied in RecipeDetails view
- fix(commonMain/recipes): crash when recipe has no cook logs in RecipeActivityPreviewCard.kt
- chore(commonMain/api): marked v2.0.0-beta-2/3/4/5 as compatible
- Revert "fix(commonMain/shopping): disabled auto focus of food input field in ShoppingListEntryCreationDialog (#141)" (resolved #141)
- l10n(Chinese (Simplified Han script)): added translation using Weblate
- l10n(Ukrainian): translated using Weblate
- l10n(French): translated using Weblate
- l10n(Tamil): translated using Weblate
- l10n(Hindi (Latin script)): added translation using Weblate
- l10n(Portuguese (Brazil)): translated using Weblate
- l10n(Russian): translated using Weblate
- l10n(Russian): added translation using Weblate