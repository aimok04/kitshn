Hey ✌️
This beta version of kitshn is *only* compatible with *
*[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-beta-1)**.
Available here on **GitHub**, through *
*[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through *
*[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Feel free to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

This update includes a **rather large design update** so feel free to **comment in #95** or **create
an issue** if you have any problems or suggestions! :)

1. Redesigned app with **Material 3 Expressive** (#204).
2. Added more **haptic feedback** (#204).
3. Added **liquid glass** effect icon on **Apple devices**.
4. Now using **default meal type** when creating **new meal plan entry** (#211).
5. Checked **compatibility** with Tandoor **version 2.0.0-alpha-5**.
6. Checked **compatibility** with Tandoor **version 2.0.0-alpha-6**.
7. Checked **compatibility** with Tandoor **version 2.0.0-alpha-7**.
8. Checked **compatibility** with Tandoor **version 2.0.0-beta-1**.

## Commits

- feat(iosApp): added app icon with liquid glass effect
- feat(commonMain/ui): replaced outdated progress indicators (Material3Expressive) (#204)
- feat(commonMain/ui): added haptic feedback to many actions and components (Material3Expressive) (
  #204)
- feat(commonMain/recipes): moved dropdown menu actions into HorizontalFloatingToolbar (
  Material3Expressive) (#204)
- feat(commonMain/recipes): adapted image roundness to screen rounding on Android (
  Material3Expressive) (#204)
- feat(commonMain/ui): added haptic feedback to selectors (meal plan and servings) (
  Material3Expressive) (#204)
- feat(commonMain/mealplan): added animation to date range chip in tool bar (Material3Expressive) (
  #204)
- feat(commonMain/ui): replaced some outlined text fields and cards (Material3Expressive) (#204)
- feat(commonMain/onboarding): replaced Autofill modifier with .semantics contentType
- feat(commonMain/ui): misc changes (Material3Expressive) (#204)
- feat(commonMain/ui): added loading indicator to list route (Material3Expressive) (#204)
- feat(commonMain/ui): redesign IngredientsList and IngredientItem (Material3Expressive) (#204)
- feat(commonMain/ui): redesign recipe views and routes (Material3Expressive) (#204)
- feat(commonMain/ui): redesign onboarding routes (Material3Expressive) (#204)
- feat(commonMain/ui): redesign settings views (Material3Expressive) (#204)
- feat(commonMain/ui): redesign AdaptiveFullscreenDialog (Material3Expressive) (#204)
- feat(commonMain/ui): redesign book views (Material3Expressive) (#204)
- feat(commonMain/ui): redesign shopping view and shopping mode route (Material3Expressive) (#204)
- feat(commonMain/ui): redesign meal plan view (Material3Expressive) (#204)
- feat(commonMain/ui): redesign home view and search (Material3Expressive) (#204)
- feat(commonMain/ui): add new components and classes (Material3Expressive) (#204)
- feat(commonMain/ui): adapt back button design (Material3Expressive) (#204)
- feat(commonMain/mealplan): use "default" meal type by default when creating new meal plan entry (
  resolved #211)
- fix(libs): reverted some version bumps
- fix(iosApp): updated import in SubscriptionView.swift
- fix(commonMain/main): set default sub route navigation animation to fade
- fix(iosMain/main.kt): disable default back gesture
- fix(commonMain/recipes): removed empty nav back stack read call
- fix(commonMain/mealplan): error when trying to move meal plan entry with associated recipe
- fix(commonMain): adapted to new back stack entry arguments api (Material3Expressive) (#204)
- fix(commonMain/ui): adapted funding banner (Material3Expressive) (#204)
- fix(commonMain/ui): show animation of TandoorBetaInfoDialog
- fix(composeApp): adapt to jetbrains compose alpha (#204)
- chore(libs): version bumps
- chore(commonMain/api): marked v2.0.0-alpha-5/6/7 and v2.0.0-beta-1 as compatible
- chore(libs): version bumps (Material3Expressive) (#204)