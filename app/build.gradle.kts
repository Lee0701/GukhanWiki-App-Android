import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").reader())

android {
    namespace = "io.github.lee0701.gukhanwiki.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.lee0701.gukhanwiki.android"
        minSdk = 21
        targetSdk = 34
        versionCode = 25
        versionName = "0.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true

        defaultConfig.resValue("string", "app_version_name", "${versionName}")
    }

    flavorDimensions += listOf("server")
    productFlavors {
        create("development") {
            dimension = "server"
            buildConfigField("String", "API_PROTOCOL", "\"https\"")
            buildConfigField("String", "API_HOST", "\"${properties.getProperty("api.host")}\"")
            buildConfigField("String", "REST_BASE_PATH", "\"${properties.getProperty("api.rest-base-path")}\"")
            buildConfigField("String", "ACTION_BASE_PATH", "\"${properties.getProperty("api.action-base-path")}\"")
            buildConfigField("String", "DOC_PATH", "\"${properties.getProperty("api.doc-path")}\"")
            manifestPlaceholders += mapOf(
                "hostName" to "\"${properties.getProperty("api.host")}",
                "altHostName" to "\"${properties.getProperty("api.host")}",
            )
            resValue("bool", "altHostEnabled", "false")
        }
        create("production") {
            dimension = "server"
            buildConfigField("String", "API_PROTOCOL", "\"https\"")
            buildConfigField("String", "API_HOST", "\"wiki.xn--9cs231j0ji.xn--p8s937b.net\"")
            buildConfigField("String", "REST_BASE_PATH", "\"/rest.php/v1/\"")
            buildConfigField("String", "ACTION_BASE_PATH", "\"/api.php/\"")
            buildConfigField("String", "DOC_PATH", "\"/wiki/\"")
            manifestPlaceholders += mapOf(
                "hostName" to "\"wiki.xn--9cs231j0ji.xn--p8s937b.net\"",
                "altHostName" to "\"wiki.韓國語.漢字.net\"",
            )
            resValue("bool", "altHostEnabled", "true")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.flexbox)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.android.spinkit)
    implementation(libs.play.services.oss.licenses)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.jsoup)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

tasks.register("printVersionCode") {
    println(android.defaultConfig.versionCode)
}

tasks.register("printVersionName") {
    println(android.defaultConfig.versionName)
}
