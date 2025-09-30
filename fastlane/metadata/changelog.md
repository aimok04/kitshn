Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

Yet another (mostly) bug fix release:

1. Fixed **crash** when opening **meal plan** page on iOS (#282).
   - *This is a very quick fix, which disables all date pickers on iOS.*
2. Fixed **social media import** failing when **sharing** link to kitshn *without* default AI provider.
3. Improved **decimal** to **fraction** conversion (#281).

## Commits

- feat(commonMain/utils): improved formatDecimalToFraction with more accurate fraction matching (resolved #281)
- fix(commonMain/import): don't proceed with autoFetch when there is no default ai provider in social media import
- fix(commonMain/mealplan): disabled date picker temporarily for iOS because it depends on kotlinx.datetime.Clock (#282)