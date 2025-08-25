# Keep the application class
-keep public class * extends android.app.Application {
    public <init>();
}

# Keep the main activity
-keep public class * extends android.app.Activity {
    public <init>();
}

# Keep any classes that are referenced in XML
-keep class * {
    public static <fields>;
    public <init>();
}

# Keep all Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep all classes that are used in reflection
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Obfuscate everything else
# No specific keep rules means everything will be obfuscated