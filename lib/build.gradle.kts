@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.deps.guard)
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
