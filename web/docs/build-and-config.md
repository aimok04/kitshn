# Build & Config

Building is pretty straight forward. The easiest way is to open the project in [Android Studio](https://developer.android.com/studio).

Consider changing the [`kitshn.properties`](https://github.com/aimok04/kitshn/blob/main/kitshn.properties) when forking. It currently contains contact information, crash reporting server details and the [share wrapping URL](/docs/features/share-wrapping).

```txt
about.github="https://www.github.com/aimok04/kitshn"
about.github.new.issue="https://www.github.com/aimok04/kitshn/issues/new"
about.contact.website="https://kitshn.app"
about.contact.mailto="contact@kitshn.app"

# default kitshn crash receiver running on a cloudflare worker
acra.http.uri="https://acra.kitshn.app/report/"
acra.http.basic.auth.login="PDEogxtl3k4LMCnH"
acra.http.basic.auth.password="zmeLkKkJvsnwdrHA"

# default kitshn share wrapper
share.wrapper.url="https://x.kitshn.app/#"
```