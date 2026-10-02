group = "app.xpatches"

patches {
    about {
        name = "X Patches"
        description = "Patches for X (formerly Twitter), ported from ReVanced, for use with Morphe"
        source = "https://github.com/Internetservice/x-patches"
        author = "Remy"
        contact = "na"
        website = "https://github.com/Internetservice/x-patches"
        license = "GNU General Public License v3.0"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    implementation(libs.morphe.patches.library)

    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
