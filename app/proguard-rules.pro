# Add project specific ProGuard rules here.

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.relateai.app.**$$serializer { *; }
-keepclassmembers class com.relateai.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.relateai.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keepclasseswithmembernames class * {
    @dagger.hilt.* <fields>;
    @dagger.hilt.* <methods>;
}

# Generative AI
-keep class com.google.ai.client.generativeai.** { *; }
-keep class io.grpc.** { *; }
