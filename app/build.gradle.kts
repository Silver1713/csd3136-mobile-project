plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)

    alias(libs.plugins.google.services)


}

android {
    namespace = "com.csd3156.mobileproject.MovieReviewApp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.csd3156.mobileproject.MovieReviewApp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Added this filed to expose TMDB bearer token via BuildConfig.TMDB_API_TOKEN
        buildConfigField(
            "String",
            "TMDB_API_TOKEN",
            "\"eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIwOWM3ZjJiZGFiNzA1NjhhMzc1NDY4YTYxOTQ3NDRiOSIsIm5iZiI6MTUzMTE5MTQzOC4wODYwMDAyLCJzdWIiOiI1YjQ0MjA4ZTBlMGEyNjcwZmMwMjRiZjUiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.p0gtbhEKJ5UFUUwOCDwxi98dzSCwPdLJJa6LyS1CWiM\""
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Set buildConfig to true so gradle generates BuildConfig
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

configurations.all {
    exclude(group = "com.intellij", module = "annotations")
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
    implementation(libs.androidx.glance.preview)
   // implementation(libs.androidx.room.compiler)
   // implementation(libs.androidx.androidx.room.gradle.plugin)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi.kotlin)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.at.favre.bcrypt)

    // Import Firebase BOM
    implementation(platform(libs.firebase.bom))

    // Firebase products
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)


    //implementation("com.github.haifengl:smile-core:5.0.0")




}

dependencies {
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.android.youtube.player.core)

    implementation(libs.androidx.room.runtime)

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp(libs.androidx.room.compiler)

//    // If this project only uses Java source, use the Java annotationProcessor
//    // No additional plugins are necessary
//    annotationProcessor(libs.androidx.room.compiler)

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)
//
//    // optional - RxJava2 support for Room
//    implementation("androidx.room:room-rxjava2:<version>")
//
//    // optional - RxJava3 support for Room
//    implementation("androidx.room:room-rxjava3:<version>")
//
//    // optional - Guava support for Room, including Optional and ListenableFuture
//    implementation("androidx.room:room-guava:<version>")

    // optional - Test helpers
    testImplementation(libs.androidx.room.testing)

    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.exifinterface)
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

}
