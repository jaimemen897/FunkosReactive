plugins {
    id("java")
    id("io.freefair.lombok") version "8.3"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")

    implementation("io.projectreactor:reactor-core:3.5.10")
    implementation("org.mybatis:mybatis:3.5.13")
    implementation("com.h2database:h2:2.1.210")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.test {
    useJUnitPlatform()
}