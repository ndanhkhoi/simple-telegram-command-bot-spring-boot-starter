import org.springframework.boot.gradle.plugin.SpringBootPlugin

/*
 * This file was generated by the Gradle "init" task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details take a look at the "Building Java & JVM projects" chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.1/userguide/building_java_projects.html
 */

plugins {
    `java-library`
    `maven-publish`
    id("org.springframework.boot") version "2.7.1" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "com.github.ndanhkhoi"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:6.1.0")
    implementation("org.springframework.boot:spring-boot-starter")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    api("io.projectreactor:reactor-core:3.4.15")
    api("io.projectreactor.addons:reactor-extra:3.4.6")
    api("org.apache.commons:commons-lang3:3.12.0")
    api("commons-io:commons-io:2.11.0")
    api("commons-beanutils:commons-beanutils:1.9.4")
    api("com.google.guava:guava:31.0.1-jre")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configure<PublishingExtension> {
    publications {
        publications.create<MavenPublication>("mavenJava") {
            from(components["java"])
            repositories {
                maven {
                    name = "GitHubPackages"
                    artifactId = "simple-telegram-command-bot-spring-boot-starter"
                    url = uri("https://maven.pkg.github.com/ndanhkhoi/simple-telegram-command-bot-spring-boot-starter")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}

tasks.withType<Jar> {
    archiveBaseName.set("simple-telegram-command-bot-spring-boot-starter")
}

tasks {
    register("fatJar", Jar::class.java) {
        archiveBaseName.set("simple-telegram-command-bot-spring-boot-starter-full-dependencies")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        with(jar.get() as CopySpec)
    }
}
