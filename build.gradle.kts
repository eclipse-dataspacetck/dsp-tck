import org.eclipse.dataspacetck.gradle.plugins.tckgen.TckGeneratorExtension
import org.eclipse.dataspacetck.gradle.tckbuild.extensions.TckBuildExtension

/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */


plugins {
    `java-library`
    `maven-publish`
    signing
    checkstyle
    jacoco
    `jacoco-report-aggregation`
    alias(libs.plugins.docker)
    alias(libs.plugins.nexuspublishing)
    alias(libs.plugins.tck.build) apply false
    alias(libs.plugins.tck.generator) apply false
}

val tckVersion = libs.versions.tck.get()

allprojects {

    apply(plugin = "java-library")
    apply(plugin = "checkstyle")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.eclipse.dataspacetck.build.tck-build")
    apply(plugin = "org.eclipse.dataspacetck.build.tck-generator")

    configure<TckBuildExtension> {
        pom {
            scmConnection = "https://github.com/eclipse-dataspacetck/dsp-tck.git"
            scmUrl = "scm:git:git@github.com:eclipse-dataspacetck/dsp-tck.git"
            projectName = project.name
            description = "DSP Technology Compatibility Kit"
            projectUrl = "https://projects.eclipse.org/projects/technology.dataspacetck"
        }
    }

    configure<TckGeneratorExtension> {
        generatorVersion = tckVersion
    }

    tasks.test {
        useJUnitPlatform()
    }

    dependencies {
        implementation(rootProject.libs.json.api)
        implementation(rootProject.libs.parsson)
        implementation(rootProject.libs.jackson.databind)
        implementation(rootProject.libs.jackson.jsonp)
        implementation(rootProject.libs.titanium)
        implementation(rootProject.libs.okhttp)
        implementation(rootProject.libs.junit.jupiter)
        implementation(rootProject.libs.junit.platform.engine)
        implementation(rootProject.libs.mockito.core)
        implementation(rootProject.libs.awaitility)
        testImplementation(rootProject.libs.assertj)
    }

}

// needed for running the dash tool
tasks.register("allDependencies", DependencyReportTask::class)

// disallow any errors
checkstyle {
    maxErrors = 0
}
