# Share wrapping

kitshn can prepend » **x.kitshn.app** « to links when sharing a recipe from within the app. This enables other kitshn users to open shared recipes directly inside the app, since the app can only intercept requests made to the » **x.kitshn.app** « domain.

For the sake of transparency, the » **x.kitshn.app** « website is hosted on GitHub pages. You can view the source code on https://github.com/kshli/kshli.github.io.

---

The share wrapping url can be changed in the [`kitshn.properties`](https://github.com/aimok04/kitshn/blob/main/kitshn.properties) file.
```txt{2}
# default kitshn share wrapper
share.wrapper.url="https://x.kitshn.app/#"
```
You should also change the [`AndroidManifest.xml`](https://github.com/aimok04/kitshn/blob/main/app/src/main/AndroidManifest.xml) when changing the share wrapping url.
```xml{11}
...
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <data android:scheme="http" />
    <data android:scheme="https" />

    <data android:host="x.kitshn.app" />
</intent-filter>
...
```