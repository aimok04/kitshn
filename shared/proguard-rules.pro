# Consumer ProGuard rules for :shared
# Applied automatically to any Android consumer that runs R8/ProGuard.

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class de.kitshn.**$$serializer { *; }
-keepclassmembers class de.kitshn.** { *** Companion; }
-keepclasseswithmembers class de.kitshn.** { kotlinx.serialization.KSerializer serializer(...); }

# ACRA
-keep class org.acra.** { *; }
-dontwarn org.acra.**

# Coil
-dontwarn coil.**
