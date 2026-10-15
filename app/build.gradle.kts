import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.wordgame"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.wordgame"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Google AdMob - AJOUTÉ
    implementation("com.google.android.gms:play-services-ads:23.5.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Mise à jour obligatoire (Google Play In-App Updates)
dependencies {
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    // Fragment >= 1.3 requis pour registerForActivityResult (écran de mise à jour)
    implementation("androidx.fragment:fragment-ktx:1.8.5")
}

// Clé API du classement : google-services.json s'il existe, sinon la propriété firebase.apiKey
// (gradle.properties, local.properties ou ~/.gradle/gradle.properties).
val firebaseApiKey: String = run {
    val config = file("google-services.json")
    @Suppress("UNCHECKED_CAST")
    val fromJson = if (config.exists()) runCatching {
        val json = groovy.json.JsonSlurper().parse(config) as Map<String, Any?>
        val client = (json["client"] as List<Map<String, Any?>>).first {
            ((it["client_info"] as Map<String, Any?>)["android_client_info"] as Map<String, Any?>)["package_name"] == "app.wordgame"
        }
        ((client["api_key"] as List<Map<String, Any?>>).first())["current_key"] as String
    }.getOrNull() else null
    val local = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    fromJson ?: (findProperty("firebase.apiKey") as String?) ?: local.getProperty("firebase.apiKey") ?: ""
}
android.defaultConfig {
    buildConfigField("String", "FIREBASE_PROJECT_ID", "\"wordsgame-80893\"")
    buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
}
// Un AAB/APK release sans clé aurait le classement désactivé : on bloque le build.
tasks.matching { it.name.startsWith("pre") && it.name.endsWith("ReleaseBuild") }.configureEach {
    doFirst {
        if (firebaseApiKey.isBlank()) throw GradleException(
            "Clé Firebase absente : ajoutez app/google-services.json ou firebase.apiKey=... dans local.properties, sinon le classement ne fonctionnera pas.")
    }
}
dependencies { coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5") }
