plugins {
    `kotlin-dsl`
}

group = "com.octopus.edu.trackmate.buildlogic"

gradlePlugin {
    plugins {
        register("architectureGuard") {
            id = "trackmate.architecture-guard"
            implementationClass = "ArchitectureGuardPlugin"
        }
    }
}

dependencies {
    testImplementation(libs.test.junit)
}
