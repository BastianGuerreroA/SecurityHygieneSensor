# ProGuard rules for SecurityHygieneSensor

# Evitar que R8/ProGuard ofusque u optimice las clases de modelo de la API
-keep class com.bastianguerrero.securitysensor.data.model.** { *; }
-keepclassmembers class com.bastianguerrero.securitysensor.data.model.** { *; }

# Proteger anotaciones de Gson y clases de Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Mantener las interfaces de Retrofit
-keep interface com.bastianguerrero.securitysensor.data.network.** { *; }

# Reglas necesarias para OkHttp y Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod

# OkHttp3
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**