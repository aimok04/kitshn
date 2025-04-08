Hey ✌️
This is the first version of kitshn **compatible** with the API changes for *
*[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-alpha-1)**. 🥳
New versions of kitshn will **no longer** support instances on **v1.5.***.

This beta is available here on **GitHub**, through *
*[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through *
*[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Although I have tested the app with **Tandoor v2.0.0-alpha-1**, there could still be some minor API
changes that I might have missed, so **please feel free** to comment on #95 or create **a new issue
** if you **encounter any issues**! :)

---

This release also contains some **general improvements**:

1. 🇮🇹 **Italian** has been added on **[Weblate](https://hosted.weblate.org/projects/kitshn)** (by
   @KindaArtsy).
2. Improved **space switching** due to **new API** (#122).
3. Improved **timer detection** with support for **hours** (#167).

## Highlights

- feat(commonMain): improved space switcher with new Tandoor v2 space api
- feat(commonMain): improved timer detection to support hours and multiple languages (resolved #167)
- feat(commonMain): added Ko-Fi link in Settings for android and jvm
- feat(fastlane): added beta deploy lanes for GitHub, Google Play and TestFlight
- feat(commonMain/main): added info dialog for the kitshn beta
- feat(commonMain/onboarding): added Tandoor v1 instance error to OnboardingSignIn
- feat(commonMain): implemented serverSettings api and outdated Tandoor v1 alert
- feat(commonMain/vm): added favorites.init and connectivityCheck to signIn function
- feat(commonMain): adapted importing to Tandoor v2
- feat(commonMain): adapted supermarket and supermarket category to Tandoor v2
- feat(commonMain/api): adapted recipe overview model to Tandoor v2
- feat(commonMain): adapted shopping meal plan data / recipe data to Tandoor v2
- feat(commonMain): adapted recipe model to Tandoor v2
- feat(commonMain/api): adapted mealplan and meal type to Tandoor v2
- feat(commonMain): adapted books to Tandoor v2
- feat(commonMain/shopping): adapted ViewModel to Tandoor v2
- feat(commonMain): adapted shopping to Tandoor v2
- feat(commonMain/shopping): adapted user and user preference api to Tandoor v2
- feat(commonMain/onboarding): unified signIn method for OnboardingSignIn(Browser)
- fix(commonMain/onboarding): remove mention of alpha in OnboardingWelcome
- fix(commonMain/dialog): width issue when opening SpaceSwitchDialog
- fix(commonMain/dialog): crash when creating duplicate keyword in SelectMultipleKeywordsDialog
- fix(commonMain/mealplan): increase fetch date range to cover issue with timestamps
- fix(commonMain/onboarding): NullPointerException in OnboardingSignIn
- fix(commonMain/api): removed unused fun in TandoorUserRoute
- fix(commonMain/shopping): remove all entries in update()
- fix(commonMain): FocusRequester is not initialized issue
- l10n(Italian): translated using Weblate
- l10n(Italian): added translation using Weblate
- l10n(Ukrainian): translated using Weblate
- l10n(German): translated using Weblate