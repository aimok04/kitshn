Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

This release includes some **new features** and fixes:

1. Added **expand** button to shopping list items **containing multiple** entries (#249).
2. Now showing **all individual** entries when viewing **bottom sheet dialog** of shopping list item (#249).
3. Added **dialog** to edit **amounts** of shopping list entries.
4. Added support for **all** ingredient **templates** (#250) ([learn more about templating](https://kitshn.app/docs/features/templating.html)).
5. Added **custom sorting** button when using **traditional** home screen layout (#251).
6. Improved **recipe details** view when viewing **shared recipes**.
7. Fixed **text fields** being **unreadable** while typing when located at the **bottom** of a page (#247).
8. Fixed unable to view **shared recipes** when opening *x.kitshn.app* or *kitshn://* link (#253).
9. Fixed **meal plan** dialog **not updating** when **editing** meal plan (#246).
10. Fixed **source url** not being **saved** when using **social media import** (#248).
11. Checked **compatibility** with Tandoor **version 2.0.2**.
12. 🇺🇦 **Ukrainian** has been **updated** on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by Максим Горпиніч).

## Commits

- feat(commonMain/shopping): show individual entries in ShoppingListEntryDetailsBottomSheet and added button to open it in shopping mode (resolved #249)
- feat(commonMain/shopping): added amount edit dialog to shopping list entries (resolved #232)
- feat(commonMain/recipes): added ingredients.amount, ingredients.unit, ingredients.food, and ingredients.note templates (resolved #250)
- feat(commonMain/home): added sorting option to HomeTraditionalLayout (resolved #251)
- feat(commonMain/recipes): improved details view when viewing shared recipes
- feat(commonMain/import): adapted RecipeFromSource model and fetch functions to v2 api
- feat(commonMain/import): improved social media import script
- feat(web/docs): update templating.md
- fix(commonMain/ui): added .imePadding in AdaptiveFullscreenDialog to make text fields accessible when focused (resolved #247)
- fix(commonMain/recipes): issue with parsing shared recipe link (resolved #253)
- fix(commonMain/mealplan): update details dialog when editing entry (resolved #246)
- fix(commonMain/import): save social media url as source url when importing (resolved #248)
- fix(commonMain/ui): layout issues when AdaptiveFullscreenDialog is fullscreen
- fix(commonMain/ui): removed window insets from AdaptiveFullscreenDialog when not fullscreen and enlarged click to dismiss area (resolved #252)
- chore(commonMain/api): marked v2.0.2 as compatible
- l10n(Ukrainian): translated using Weblate