plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("buildLogic") {
            id = "buildLogic"
            implementationClass = "DummyPlugin"
        }
    }
}
