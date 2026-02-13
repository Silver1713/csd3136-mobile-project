plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)


}

android {
    namespace = "com.csd3156.mobileproject.MovieReviewApp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.csd3156.mobileproject.MovieReviewApp"
        minSdk = 24
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
    implementation ("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.navigation.compose)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)





}

dependencies {
    implementation(libs.androidx.material3)
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")


    val room_version = "2.8.4"

    implementation("androidx.room:room-runtime:$room_version")

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp("androidx.room:room-compiler:$room_version")

//    // If this project only uses Java source, use the Java annotationProcessor
//    // No additional plugins are necessary
//    annotationProcessor("androidx.room:room-compiler:$room_version")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
//
//    // optional - RxJava2 support for Room
//    implementation("androidx.room:room-rxjava2:$room_version")
//
//    // optional - RxJava3 support for Room
//    implementation("androidx.room:room-rxjava3:$room_version")
//
//    // optional - Guava support for Room, including Optional and ListenableFuture
//    implementation("androidx.room:room-guava:$room_version")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")
}
