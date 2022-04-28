import org.hidetake.gradle.swagger.generator.GenerateSwaggerCode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
	mavenCentral()
	maven("https://plugins.gradle.org/m2/")
}

plugins {
	java
	jacoco
	idea
	id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
	id("org.springframework.boot") version "2.6.6"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
	kotlin("plugin.allopen") version "1.5.31"
	kotlin("plugin.serialization") version "1.5.31"
	id("org.hidetake.swagger.generator") version "2.18.2"
}

group = "com.ajaybhat.blog"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

extra["springCloudAzureVersion"] = "4.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.azure.spring:spring-cloud-azure-starter-data-cosmos")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	swaggerCodegen("org.openapitools:openapi-generator-cli:5.4.0")

}


swaggerSources {
	register("openApiGen") {
		setInputFile(file("$rootDir/src/main/resources/openAPI/openapi3.yaml"))
		code(
			closureOf<GenerateSwaggerCode> {
				language = "kotlin-spring"
				components = kotlin.collections.listOf("apis", "models")
				configFile = file("$rootDir/src/main/resources/openAPI/api-config.json")
				outputDir = file(
					"$buildDir/generated"
				)
			}
		)
	}
}

tasks.named("compileKotlin").configure {
	dependsOn(tasks.named("generateSwaggerCode"))
}

sourceSets {
	val main by getting
	val openApiGen by swaggerSources.getting
	main.java.srcDir("${openApiGen.code.outputDir}/src/main/kotlin")
}

dependencyManagement {
	imports {
		mavenBom("com.azure.spring:spring-cloud-azure-dependencies:${property("springCloudAzureVersion")}")
	}
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

tasks.getByName<Jar>("jar") {
	enabled = false
}
