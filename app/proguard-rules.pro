# Keep retrofit + kotlinx serialization metadata
-keepattributes Signature, *Annotation*, InnerClasses
-keep,allowobfuscation,allowshrinking class kotlin.Metadata
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# kotlinx.serialization
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class * extends androidx.hilt.work.HiltWorkerFactory

# Room
-keep class androidx.room.** { *; }

# Coroutines
-dontwarn kotlinx.coroutines.debug.**

# Compose
-keep class androidx.compose.runtime.** { *; }
