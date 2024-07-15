# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-keep class com.findmykids.tracker.panda.model.** {*; }
-keep class com.findmykids.tracker.panda.service.EmitterListenerCallback.** {*; }

-keep class org.apache.http.** { *; }

    -keep class org.codehaus.jackson.** { *; }

    -keep class cz.msebera.android.httpclient.** { *; }
    -keep class com.loopj.android.http.** { *; }
    -keep class com.dd.** {*;}

    -keep class com.squareup.okhttp3.** { *; }

    -dontwarn com.squareup.okhttp.**
    -keep class com.squareup.okhttp.* { *; }
    -keep interface com.squareup.okhttp.* { *; }


    -dontwarn okhttp3.internal.platform.*
    -dontwarn okhttp3.**
    -dontwarn okio.**
    -dontwarn javax.annotation.**
    -dontwarn org.conscrypt.**

    -dontwarn org.apache.http.**
    -dontwarn android.net.**
    -keep class org.apache.** {*;}
    -keep class org.apache.http.** { *; }
    -keep class com.bumptech.glide.** { *; }


 -keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response
  -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

#-keep class com.google.android.gms.auth.** {*; }