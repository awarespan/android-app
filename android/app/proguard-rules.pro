# AWARESPAN application-specific R8 and ProGuard rules
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Preserve all domain models and network DTOs without field or member obfuscation
-keep class com.awarespan.guardian.domain.model.** { *; }
-keepclassmembers class com.awarespan.guardian.domain.model.** { *; }

-keep class com.awarespan.guardian.data.remote.dto.** { *; }
-keepclassmembers class com.awarespan.guardian.data.remote.dto.** { *; }

-keep interface com.awarespan.guardian.data.remote.api.** { *; }

# Preserve Gson SerializedName fields everywhere
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve Room database and entities
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class com.awarespan.guardian.data.local.entity.** { *; }
-keepclassmembers class com.awarespan.guardian.data.local.entity.** { *; }
-keep interface com.awarespan.guardian.data.local.dao.** { *; }
