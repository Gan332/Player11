# Default ProGuard rules for Melodex

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Media3
-keep class androidx.media3.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep data model
-keep class com.musicplayer.melodex.data.model.** { *; }

# Keep application
-keep class com.musicplayer.melodex.MelodexApp { *; }
