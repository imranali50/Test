plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")

   }

android {
    namespace = "com.findmykids.tracker.panda"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.findmykids.tracker.panda"
        minSdk = 24
        targetSdk = 34
        versionCode = 7
        versionName = "1.0.7"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true


        buildConfigField("String", "okBrother", "\"abc\"")
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = "PandaBrothers"
            keyPassword = "PandaBrothers"
            storeFile = file("/Users/tagline/Desktop/Company Project/ChildApp/AppData/PandaBrothers")
            storePassword = "PandaBrothers"
        }
        create("release") {
            keyAlias = "PandaBrothers"
            keyPassword = "PandaBrothers"
            storeFile = file("/Users/tagline/Desktop/Company Project/ChildApp/AppData/PandaBrothers")
            storePassword = "PandaBrothers"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {


    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-compiler:2.48.1")

    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // lifecycle
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")

    //sdp font size
    implementation("com.intuit.sdp:sdp-android:1.1.0")

    // Load Image (Glide)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.0")

    //map
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    //Country Code
    implementation("com.hbb20:ccp:2.7.0")

    //Qr Scanner
    implementation("com.budiyev.android:code-scanner:2.1.0")

    //sticker
    implementation("com.vanniktech:emoji-google-compat:0.18.0-SNAPSHOT")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.5")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")

    //Hilt for di
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")

    //Hilt ViewModel extension
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    //for lazy
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // When using the BoM, you don't specify versions in Firebase library dependencies
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")
    //google Login
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")

    //Socket IO
//    implementation("io.socket:socket.io-client:1.0.0")
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    //photoResize
    implementation ("com.github.yalantis:ucrop:2.2.8")

    //filter-balloon
    implementation("com.github.skydoves:balloon:1.6.4")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")


    val room_version = "2.5.2"

    //Room Database
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-runtime:$room_version")

    //shimmerEffect
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

}