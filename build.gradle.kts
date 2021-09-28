import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.spring") version "1.5.21"
}

group = "uk.co.avsoftware"
version = "0.0.3"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux:2.5.4")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.4")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2-native-mt")
	implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
	implementation("com.google.firebase:firebase-admin:8.0.1")

	implementation("com.google.cloud:spring-cloud-gcp-starter-secretmanager:2.0.4")
	implementation ("com.google.api-client:google-api-client:1.32.1")
	implementation ("com.google.http-client:google-http-client:1.40.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.4")
	testImplementation("io.projectreactor:reactor-test:3.4.10")
	testImplementation("io.mockk:mockk:1.12.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
