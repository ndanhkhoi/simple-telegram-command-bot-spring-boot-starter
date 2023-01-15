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
    id("org.springframework.boot") version "2.7.7"  apply false
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("com.github.monosoul.yadegrap") version "1.0.0"
}

group = "com.github.ndanhkhoi"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:6.4.0")
    implementation("org.springframework.boot:spring-boot-starter")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    api("io.projectreactor:reactor-core:3.5.2")
    api("io.projectreactor.addons:reactor-extra:3.5.0")
    api("org.apache.commons:commons-lang3:3.12.0")
    api("commons-io:commons-io:2.11.0")
    api("com.google.guava:guava:31.1-jre")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("org.springframework:spring-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
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

tasks {
    test {
        useJUnitPlatform()
    }

    jar {
        archiveBaseName.set("simple-telegram-command-bot-spring-boot-starter")
    }

    register("fatJar", Jar::class.java) {
        archiveBaseName.set("simple-telegram-command-bot-spring-boot-starter-full-dependencies")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        with(jar.get() as CopySpec)
    }

    val delombok = "delombok"(com.github.monosoul.yadegrap.DelombokTask::class)

    javadoc {
        dependsOn(delombok)
        setSource(delombok)
        isFailOnError = false
        title = "Simple Telegram Command Bot Spring Boot Starter API"
        options.encoding = "UTF-8"
    }
}
