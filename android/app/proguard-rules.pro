-keepattributes Signature
-keepattributes *Annotation*
-keep class com.kalenyator.app.data.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
