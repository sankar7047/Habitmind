import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.habitmind"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.habitmind"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Load API keys from properties file
        val properties = Properties()
        val propertiesFile = rootProject.file("api_key.properties")
        val openAiKey = if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
            properties.getProperty("OPENAI_API_KEY")
                ?.trim()
                ?.removeSurrounding("\"")
                ?.replace(Regex("[\\s%]+$"), "") // Remove trailing whitespace and % characters
                ?.ifBlank { null }
        } else null

        val geminiKey = if (propertiesFile.exists()) {
            properties.getProperty("GEMINI_API_KEY")
                ?.trim()
                ?.removeSurrounding("\"")
                ?.replace(Regex("[\\s%]+$"), "") // Remove trailing whitespace and % characters
                ?.ifBlank { null }
        } else null

        if (openAiKey != null) {
            println("Loaded OpenAI API Key: ${openAiKey.take(10)}... (length: ${openAiKey.length})")
        } else {
            println("Warning: No OpenAI API key found in api_key.properties")
        }

        if (geminiKey != null) {
            println("Loaded Gemini API Key: ${geminiKey.take(10)}... (length: ${geminiKey.length})")
        } else {
            println("Warning: No Gemini API key found in api_key.properties")
        }

        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"${openAiKey ?: ""}\""
        )

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${geminiKey ?: ""}\""
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
        isCoreLibraryDesugaringEnabled = true
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
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)
    
    // Gson
    implementation(libs.gson)
    
    // Charts - Using vico for Compose charts
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")

    implementation("androidx.compose.material:material-icons-extended")
    
    // Core library desugaring for Java 8+ APIs on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}