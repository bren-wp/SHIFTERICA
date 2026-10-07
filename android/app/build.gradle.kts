plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseVersion = rootProject.projectDir.parentFile.resolve("VERSION").readText().trim()
val versionMatch = Regex("""^(\d+)\.(\d+)\.(\d+)$""").matchEntire(releaseVersion)
    ?: error("VERSION mora koristiti semantički format x.y.z")
val releaseVersionCode =
    versionMatch.groupValues[1].toInt() * 10_000 +
    versionMatch.groupValues[2].toInt() * 100 +
    versionMatch.groupValues[3].toInt()

android {
    namespace = "hr.raspored.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "hr.raspored.app"
        minSdk = 26
        targetSdk = 36
        versionCode = releaseVersionCode
        versionName = releaseVersion
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("com.android.billingclient:billing:9.1.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}
