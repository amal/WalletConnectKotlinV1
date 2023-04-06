@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.deps.guard)
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("WalletConnectKotlinV1")
                description.set("Library to use WalletConnect v1 with JVM/Android")
                val urlPart = "github.com/amal/WalletConnectKotlinV1"
                val urlString = "https://$urlPart"
                url.set(urlString)

                scm {
                    url.set(urlString)
                    val scmUrl = "scm:git:git://$urlPart.git"
                    connection.set(scmUrl)
                    developerConnection.set(scmUrl)
                }
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    api(libs.khex)

    implementation(libs.bouncycastle.bcprov)

    ksp(libs.moshi.codegen)
    implementation(libs.moshi)

    implementation(libs.okhttp)

    testImplementation(libs.assertj.core)
    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencyGuard {
    configuration("compileClasspath")
    configuration("runtimeClasspath")
}
