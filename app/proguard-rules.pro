# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line number information for crash stack traces while hiding original source file names
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Room Database Entities, DAOs, and Database implementations
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.example.data.model.** { *; }
-keep class com.example.data.db.** { *; }

# Keep BroadcastReceiver entry point and ViewModels instantiated via reflection
-keep class com.example.receiver.HabitReminderReceiver { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
-keep class com.example.ui.HabitViewModel { *; }

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
