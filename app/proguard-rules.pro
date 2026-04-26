# SikAi proguard rules
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep,allowobfuscation,allowshrinking class kotlin.Metadata
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Kotlinx Serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses class com.sikai.learn.**$$serializer { *; }
-keepclassmembers class com.sikai.learn.** {
    *** Companion;
}
-keepclasseswithmembers class com.sikai.learn.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Compose
-keep class androidx.compose.runtime.** { *; }
