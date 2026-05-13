import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProps = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use { load(it) }
    }
}

fun appProp(name: String): String {
    val fromLocalProps = localProps.getProperty(name)
    if (!fromLocalProps.isNullOrBlank()) {
        return fromLocalProps
    }
    val fromProjectProp = project.findProperty(name) as String?
    return fromProjectProp.orEmpty()
}

android {
    namespace = "com.example.modprojecttracker"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.modprojecttracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            "\"${appProp("CLOUDINARY_CLOUD_NAME")}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_UPLOAD_PRESET",
            "\"${appProp("CLOUDINARY_UPLOAD_PRESET")}\""
        )
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${appProp("GEMINI_API_KEY")}\""
        )
        buildConfigField(
            "String",
            "GEMINI_MODEL",
            // Model mac dinh hop le cho endpoint v1beta (SDK generativeai 0.9.0)
            "\"${appProp("GEMINI_MODEL").ifBlank { "gemini-2.5-flash" }}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.okhttp)
    implementation(libs.work.runtime)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
