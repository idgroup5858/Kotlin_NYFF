import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Mahalliy sozlamalar (local.properties — git'ga tushmaydi): backend manzili va Google Maps kaliti.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}
// `-Pnyff.apiUrl=...` — vaqtincha boshqa serverga (local.properties ni o'zgartirmasdan)
val apiUrl: String = (providers.gradleProperty("nyff.apiUrl").orNull ?: localProperties.getProperty("nyff.apiUrl", "http://10.0.2.2:3000"))
    .trim().trimEnd('/')
val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY", "").trim()

android {
    namespace = "pr.com.kotlin_nyff"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pr.com.kotlin_nyff"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_URL", "\"$apiUrl\"")
        manifestPlaceholders["mapsApiKey"] = mapsApiKey
        // http (TLS'siz) server faqat manzil http:// bo'lsa ruxsat etiladi
        manifestPlaceholders["usesCleartextTraffic"] = apiUrl.startsWith("http://").toString()
    }

    androidResources {
        // Android 13+ sozlamalaridagi "Ilova tili" ro'yxati values-* papkalaridan yasaladi (res/resources.properties)
        generateLocaleConfig = true
        localeFilters += listOf("uz", "ru", "en")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // kuryer qismi: "yangi buyurtma" xabari faqat ilova fonda bo'lganda
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    // org.json Android'ning o'zida bor — kutubxonadagi nusxasi bilan to'qnashmasin
    implementation(libs.socket.io.client) {
        exclude(group = "org.json", module = "json")
    }

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
