plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

