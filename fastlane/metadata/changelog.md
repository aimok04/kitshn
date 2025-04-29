Hey ✌️
This is the fourth version of kitshn **compatible** with the API changes for **[Tandoor v2](https://github.com/TandoorRecipes/recipes/releases/tag/2.0.0-alpha-2)**. 🥳
New versions of kitshn will **no longer** support instances on **v1.5.***.

This beta is available here on **GitHub**, through **[Google Play testing](https://play.google.com/apps/testing/de.kitshn.android)** and through **[TestFlight](https://testflight.apple.com/join/zx1xzSMg)**.

Although I have tested the app with **Tandoor v2.0.0-alpha-2**, there could still be some minor API
changes that I might have missed, so **please feel free** to comment on #95 or create **a new issue** if you **encounter any issues**! :)

---

1. Fixed issue were **meal plan entries** were added to **wrong dates** (#191).
2. Fixed issue with **opening app links** created on **Tandoor v2** due to **changed URL** (#189).
3. Improved **legibility** of shopping list **clearing dialog**.
4. Checked **compatibility** with Tandoor **version 2.0.0-alpha-3**.

## Highlights

- feat(iosApp): added camera usage description in project.pbxproj
- fix(commonMain/shopping): increased entries clear dialog width (resolved #188)
- fix(androidMain/AndroidManifest.xml): removed unnecessary peekaboo READ_MEDIA_IMAGES permission
- fix(commonMain/api): changed date values to include time in UTC time zone (resolved #191)
- fix(commonMain): issue with converting the value of datePickerState to LocalDate (resolved #191)
- fix(androidMain, iosMain): parsing issue with new v2 recipe url in AppLinkHandler (resolved #189)
- chore(commonMain/api): marked v2.0.0-alpha-3 as compatible