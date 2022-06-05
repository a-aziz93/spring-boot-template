import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    `java-library`
    `maven-publish`
    signing
}

group = "${(System.getenv("JB_SPACE_API_URL")?:"org").split(".")[0].replaceBefore("/","").replaceFirst("//","")}.${(System.getenv("JB_SPACE_PROJECT_KEY")?:"example").toLowerCaseAsciiOnly()}"
version = "1.0.${System.getenv("JB_SPACE_EXECUTION_NUMBER")?:0}"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.named<BootJar>("bootJar") {
    layered {
        isEnabled = false
        isIncludeLayerTools = false
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

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("spring-boot-template")
                description.set("Spring Boot microservice template")
                url.set("https://github.com/a-aziz93/spring-boot-template/")
                properties.set(
                    mapOf(
        
                    )
                )
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("a-aziz93")
                        name.set("Atoev Aziz")
                        email.set("a.atoev93@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/a-aziz93/spring-boot-template.git")
                    developerConnection.set("scm:git:ssh://github.com/a-aziz93/spring-boot-template.git")
                    url.set("https://github.com/a-aziz93/spring-boot-template/")
                }
            }
        }
    }
    
    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            val releasesRepoUrl = uri("https://maven.pkg.jetbrains.space/aaziz93/p/microservices/releases")
            val snapshotsRepoUrl = uri("https://maven.pkg.jetbrains.space/aaziz93/p/microservices/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
    
            credentials {
                // Automation has a special account for authentication in Space
                // account credentials are accessible via env vars
                username = System.getenv("JB_SPACE_CLIENT_ID")
                password = System.getenv("JB_SPACE_CLIENT_SECRET")
            }
        }
    }
}
/*
signing {
    sign(publishing.publications["mavenJava"])
    sign(configurations.archives.get())
    
}
*/
tasks.javadoc {
    if (JavaVersion.current().isJava11Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

