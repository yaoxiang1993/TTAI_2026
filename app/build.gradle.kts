plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

// 依赖版本管理
object Versions {
    // Android核心库版本
    const val coreKtx = "1.12.0"
    const val lifecycleRuntimeKtx = "2.7.0"
    const val appcompat = "1.6.1"
    const val activityKtx = "1.8.2"
    const val gridlayout = "1.1.0"
    
    // UI组件版本
    const val recyclerview = "1.4.0"  // 适合targetSdk 34的稳定版本
    const val constraintlayout = "2.1.4"
    const val fragmentKtx = "1.6.2"
    const val material = "1.11.0"
    const val navigationFragmentKtx = "2.7.6"
    const val navigationUiKtx = "2.7.6"
    const val swiperefreshlayout = "1.1.0"
    
    // ViewModel和LiveData版本
    const val lifecycleViewmodelKtx = "2.7.0"
    
    // Kotlin协程版本
    const val kotlinxCoroutinesAndroid = "1.7.3"
    
    // 网络库版本
    const val retrofit = "2.9.0"
    const val okhttp = "4.12.0"
    const val gson = "2.10.1"

    // 图片加载版本
    const val glide = "4.16.0"
    // 测试依赖版本
    const val junit = "4.13.2"
    const val androidTestExtJunit = "1.1.5"
    const val espressoCore = "3.5.1"
    
    // 工具库版本
    const val desugarJdkLibs = "2.0.4"
    const val mmkv = "1.3.2"
}

object Deps {
    // Android核心库
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntimeKtx}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val activityKtx = "androidx.activity:activity-ktx:${Versions.activityKtx}"
    const val gridlayout = "androidx.gridlayout:gridlayout:${Versions.gridlayout}"
    
    // UI组件
    const val recyclerview = "androidx.recyclerview:recyclerview:${Versions.recyclerview}"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.navigationFragmentKtx}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.navigationUiKtx}"
    const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swiperefreshlayout}"
    
    // ViewModel和LiveData
    const val lifecycleViewmodelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycleViewmodelKtx}"
    
    // Kotlin协程
    const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinxCoroutinesAndroid}"
    
    // 网络库
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitConverterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"

    // 图片加载
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    // 测试依赖
    const val junit = "junit:junit:${Versions.junit}"
    const val androidTestExtJunit = "androidx.test.ext:junit:${Versions.androidTestExtJunit}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
    
    // 工具库
    const val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:${Versions.desugarJdkLibs}"
    const val mmkv = "com.tencent:mmkv:${Versions.mmkv}"
}

android {
    namespace = "com.example.ttai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ttai"
        minSdk = 24
        targetSdk = 35
        versionCode = 15
        versionName = "1.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file("D:\\project\\TTAI0911\\TTAI\\ttai.jks")
            storePassword ="ttai123"
            keyAlias ="key0"
            keyPassword ="ttai123"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // 启用调试版本的优化
            isDebuggable = true
            isMinifyEnabled = false
            // 启用调试版本的增量编译
            isCrunchPngs = false
        }
        // 证书密码 ttai123
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // 启用增量编译
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        // 启用Kotlin编译优化
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }
    
    buildFeatures {
        viewBinding = true
        // 禁用不需要的功能以提升编译速度
        buildConfig = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // 排除不必要的文件
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }
    
    // 启用并行编译
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // 核心库反混淆支持
    coreLibraryDesugaring(Deps.desugarJdkLibs)

    // Android核心库
    implementation(Deps.coreKtx)
    implementation(Deps.lifecycleRuntimeKtx)
    implementation(Deps.appcompat)
    implementation(Deps.activityKtx)
    implementation(Deps.gridlayout)
    
    // UI组件
    implementation(Deps.recyclerview)  // 已更新为1.4.1，适合targetSdk 34
    implementation(Deps.constraintlayout)
    implementation(Deps.fragmentKtx)
    implementation(Deps.material)
    implementation(Deps.navigationFragmentKtx)
    implementation(Deps.navigationUiKtx)
    implementation(Deps.swiperefreshlayout)
    
    // ViewModel和LiveData
    implementation(Deps.lifecycleViewmodelKtx)
    implementation(Deps.lifecycleRuntimeKtx)
    
    // Kotlin协程
    implementation(Deps.kotlinxCoroutinesAndroid)
    
    // 网络库
    implementation(Deps.retrofit)
    implementation(Deps.retrofitConverterGson)
    implementation(Deps.okhttp)
    implementation(Deps.okhttpLoggingInterceptor)
    implementation(Deps.gson)

    // 图片加载
    implementation(Deps.glide)
    // 测试依赖
    testImplementation(Deps.junit)
    androidTestImplementation(Deps.androidTestExtJunit)
    androidTestImplementation(Deps.espressoCore)
    
    // 工具库
    implementation(Deps.mmkv)
    
    // EventBus
    implementation("org.greenrobot:eventbus:3.3.1")
    
    // PhotoPicker - 现代图片选择库
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity:1.8.2")
    
    // PhotoPicker 库
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
}