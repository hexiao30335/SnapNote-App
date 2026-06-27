# SnapNote ProGuard Rules

# Keep Room entities
-keep class com.snapnote.data.local.entity.* { *; }

# Keep data models
-keep class com.snapnote.data.model.* { *; }

# Keep Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
