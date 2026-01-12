
<img width="1604" height="267" alt="kitshn (for Tandoor)" src="https://github.com/user-attachments/assets/746041d2-1a85-44b4-a03b-aef0c792cc87" />


### Hey ✌️

Happy New Year! 🥳

This is the first minor bug fix release of *kitshn v2*.

1. Fixed issue where _past date's_ labels only included _weekdays_ (#313).
2. Fixed issue where _plurals_ weren't used in _shopping_ (#307).
3. Fixed multiple iOS software keyboard-related issues.
   - Fixed issue where _keyboard_ wasn't closing in _import_ dialog (#280).
   - Fixed issue where _keyboard_ wasn't closing when _clicking_ on recipe in _search_ (#312).
   - Fixed issue where _keyboard_ wasn't closing when _clicking_ on _search_ icon in _search_ text
     fields (#312).

---

## Commits

- fix(commonMain/utils): toHumanReadableDateLabel always only returned weekday for past days (
  resolved #313)
- fix(commonMain/shopping): now showing plural food name when amount > 1 of min one entry (resolved
  #307)
- fix(commonMain/recipes): dirty fix for scrolling issue when recipe has no description
- fix(commonMain/import): added keyboard hiding workaround for iOS
- fix(commonMain/search): disable search field shortly after pressing search button to close
  keyboard on iOS (resolved #312) (#317)
- fix(commonMain/search): disable search field when opening recipe as a workaround to close keyboard
  on iOS (#312)