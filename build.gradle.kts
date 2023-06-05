plugins {
    id("application")
}

application {
    mainClass.set("de/cofinpro/editor/Viewer")
}

group = "de.cofinpro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("net.java.dev.jna:jna:5.13.0")

    val lombokVersion = "1.18.26"
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0-M1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
}

tasks.test {
    useJUnitPlatform()
}