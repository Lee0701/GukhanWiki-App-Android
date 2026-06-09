import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").reader())

configure<ApplicationExtension> {
    namespace = "io.github.lee0701.gukhanwiki.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.lee0701.gukhanwiki.android"
        minSdk = 21
        targetSdk = 35
        versionCode = 28
        versionName = "0.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    flavorDimensions += listOf("server")
    productFlavors {
        create("production") {
            manifestPlaceholders["hostName"] = "wiki.xn--9cs231j0ji.xn--p8s937b.net"
            manifestPlaceholders["altHostName"] = "wiki.韓國語.漢字.net"
            dimension = "server"
            buildConfigField("String", "API_PROTOCOL", "\"https\"")
            buildConfigField("String", "API_HOST", "\"wiki.xn--9cs231j0ji.xn--p8s937b.net\"")
            buildConfigField("String", "REST_BASE_PATH", "\"/rest.php/v1/\"")
            buildConfigField("String", "ACTION_BASE_PATH", "\"/api.php/\"")
            buildConfigField("String", "DOC_PATH", "\"/wiki/\"")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    tasks.register("printVersionCode") {
        println(defaultConfig.versionCode)
    }

    tasks.register("printVersionName") {
        println(defaultConfig.versionName)
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
