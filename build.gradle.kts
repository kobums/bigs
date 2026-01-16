plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}

// 프로젝트 전역 설정
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "kr.thedream"
    version = "0.0.1-SNAPSHOT"

    java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
            @Suppress("UnstableApiUsage")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    tasks.withType<JavaCompile>().configureEach { options.release.set(21) }

    dependencies {
        // kotlin
        implementation("org.jetbrains.kotlin:kotlin-reflect")

        // jackson
        implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        // test
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("io.mockk:mockk:1.13.5")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testImplementation("com.appmattus.fixture:fixture:1.2.0")
    }
    // Ktlint 설정
    ktlint {
        version.set("0.45.2")
        debug.set(false)
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

repositories {
    mavenCentral()
}

// bootstrap
project(":modules:bootstrap") {
    subprojects {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    }
}

// application
project(":modules:application") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}

// domain
project(":modules:domain") {
    // POJO
}

// infra
project(":modules:infrastructure") {
    subprojects {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    }
}

// common
project(":modules:common") {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}

// external
project(":modules:external") {
    subprojects {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    }
}
