plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    application
    id("org.openjfx.javafxplugin").version("0.0.13")
}

group = "edu.austral.ingsis.starships-ui"
version = "1.0.0"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/austral-ingsis/starship-ui")
        credentials {
            username = ""
            password = ""
        }
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("edu.austral.ingsis.starships:starships-ui:1.2.0")
}

javafx {
    version = "18"
    modules = listOf("javafx.graphics")
}

application {
    // Define the main class for the application.
    mainClass.set("edu.austral.ingsis.starships.AppKt")
}
