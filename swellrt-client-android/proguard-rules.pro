# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/pablojan/Development/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.google.common.collect.BiMap
-keep class com.google.common.collect.HashBiMap
-keep class com.google.common.base.Preconditions
-keep class com.google.common.collect.ForwardingList
-keep class com.google.common.collect.ImmutableList
-keep class com.google.common.annotations.GwtCompatible
-keep class com.google.common.base.Joiner
-keep class com.google.common.collect.Lists
-keep class com.google.common.collect.Maps

-dontwarn org.waveprotocol.box.common.DeltaSequence
