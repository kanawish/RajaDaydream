# Some methods are only called from tests, so make sure the shrinker keeps them.
-keep class com.shopify.android.architecture.** { *; }

-keep class android.support.v4.widget.DrawerLayout { *; }
-keep class android.support.test.espresso.IdlingResource { *; }
-keep class com.google.common.base.Preconditions { *; }

# For Guava:
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# Proguard rules that are applied to your test apk/code.
-ignorewarnings

-keepattributes *Annotation*

-dontnote junit.framework.**
-dontnote junit.runner.**

-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
-dontwarn org.hamcrest.**
-dontwarn com.squareup.javawriter.JavaWriter

-dontwarn org.mockito.**

######
# Custom rules
######

### Keep GSON stuff
-keep class sun.misc.Unsafe { *; }
-dontwarn sun.misc.Unsafe
-keep class com.google.gson.** { *; }

### Keep these for GSON and Jackson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# RetroLambda
-dontwarn java.lang.invoke.*

######
# Following rules from:
# https://github.com/krschultz/android-proguard-snippets
######

######
# OkHttp3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

######
# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

######
# Picasso
-dontwarn com.squareup.okhttp.**


######
## https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-square-retrofit2.pro
# Retrofit 2.X
## https://square.github.io/retrofit/ ##
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

######
# https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-butterknife-7.pro
# ButterKnife 7
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

######
# https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-calligraphy-2.1.0.pro
# Caligraphy
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class uk.co.chrisjenx.calligraphy.* { *; }
-keep class uk.co.chrisjenx.calligraphy.*$* { *; }

######
# https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-joda-time.pro
# Joda Time 2.3
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

######
# Leak Canary
# https://github.com/square/leakcanary/issues/265
# https://github.com/square/leakcanary/issues/394
#-keep class org.eclipse.mat.** { *; }
-dontwarn com.squareup.haha.guava.**
-dontwarn com.squareup.haha.perflib.**
-dontwarn com.squareup.haha.trove.**
-dontwarn com.squareup.leakcanary.**
-keep class com.squareup.haha.** { *; }
-keep class com.squareup.leakcanary.** { *; }

######
# Tango
-dontwarn com.google.atap.tangocloudservice.**

-keep class com.projecttango.** {*;}
-keep interface com.projecttango.** {*;}

-keep class com.google.atap.** {*;}
-keep interface com.google.atap.** {*;}

-keep class com.google.vr.** {*;}
-keep interface com.google.vr.** {*;}

-keep class org.rajawali3d.** {*;}
-keep interface org.rajawali3d.** {*;}

######
# God damn...
-keep class com.kanawish.** {*;}

######
#

