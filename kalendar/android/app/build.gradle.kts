plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseVersion = rootProject.projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()
val match = Regex("""^(\d+)\.(\d+)\.(\d+)$""").matchEntire(releaseVersion)
    ?: error("VERSION must be x.y.z")

android {
    namespace = "hr.raspored.kalendar"
    compileSdk = 36
    defaultConfig {
        applicationId = "hr.raspored.kalendar"
        minSdk = 26
        targetSdk = 36
        versionName = releaseVersion
        versionCode = match.groupValues[1].toInt() * 10_000 +
            match.groupValues[2].toInt() * 100 + match.groupValues[3].toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
