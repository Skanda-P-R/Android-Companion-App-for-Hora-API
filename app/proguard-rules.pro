# Retrofit rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, Exceptions

# Moshi rules
-keep class com.squareup.moshi.** { *; }
-keepnames class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# App models - Keep data classes used for JSON serialization
-keep class com.hora.jnana.models.** { *; }
-keep class com.hora.jnana.api.models.** { *; }

# OkHttp rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext$HandlerPost {
    *** run();
}

# AndroidX Lifecycle
-keep class androidx.lifecycle.CompositeGeneratedAdaptersObserver { *; }
-keep class androidx.lifecycle.FullLifecycleObserverAdapter { *; }
-keep class androidx.lifecycle.ReflectiveGenericLifecycleObserver { *; }
-keep class androidx.lifecycle.SingleGeneratedAdapterObserver { *; }
