extension {
    name = "extensions/twitter.mpe"
}

android {
    namespace = "app.xpatches.extension.twitter"

    defaultConfig {
        minSdk = 26
    }
}

configurations.configureEach {
    // AGP adds the Kotlin standard library to every project. The extension is plain Java and the
    // X app already ships its own Kotlin runtime, so keep these classes out of the merged DEX.
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    exclude(group = "org.jetbrains", module = "annotations")
}

dependencies {
    // OkHttp is shipped inside the X app, so it only needs to be present at compile time.
    compileOnly(libs.okhttp)
}
