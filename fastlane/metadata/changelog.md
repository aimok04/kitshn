Hey ✌️
This beta version of kitshn is *only* compatible with **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

This release includes another *fix* which should make it possible again to use *input fields* on the home page (now also on iOS) 😅

1. Fixed *issue* which *prevented* using **input fields** on the **home screen** (#289).
2. Changed *number* input field's *software* keyboard.

## Commits

- fix(commonMain/home): replaced ExpandedDockedSearchbar with ExpandedFullScreenSearchbar again but its hidden when searchBarState != expanded (resolved #289)
- fix(commonMain/ui): replaced every KeyboardType.Number with KeyboardType.Decimal