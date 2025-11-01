Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

This release should fix some major issues like *broken reproducibility* which broke updates on *F-Droid* until now.
Also *date pickers* should work again on iOS. 😊

1. Now showing **recipe** of **food**'s in **ingredient lists** (#284).
2. Fixed **crash** when clicking **sign out**.
3. Reactivated **date pickers** on **iOS** (#282).
4. Checked **compatibility** with Tandoor **version 2.3.0** to **version 2.3.3**.

## Commits

- feat(commonMain/recipes): highlight IngredientItem when food has recipe value and open recipe link dialog on click (resolved #284)
- fix(commonMain/settings): close app when signing out
- Revert "fix(commonMain/mealplan): disabled date picker temporarily for iOS because it depends on kotlinx.datetime.Clock (#282)"
- chore(libs): version bumps (resolved #282)
- chore(commonMain/api): marked v2.3.3 as compatible
- chore(commonMain/api): marked v2.3.0 - v2.3.2 as compatible