# Hermetic Keyboard ProGuard Rules

# Keep Room entities
-keep class com.hermetic.keyboard.symbols.data.** { *; }

# Keep symbol model classes (used with Gson)
-keep class com.hermetic.keyboard.symbols.model.** { *; }

# Keep InputMethodService
-keep class com.hermetic.keyboard.ime.HermeticIME { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
