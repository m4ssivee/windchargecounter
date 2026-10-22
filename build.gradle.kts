plugins {
    id("fabric-loom")
    `maven-publish`
}

val mcVersion = stonecutter.current.version
val mcDep = property("mc_dep") as String

version = "${property("mod_version")}+$mcVersion"
group = property("maven_group") as String
base.archivesName = property("archives_base_name") as String

repositories {
    mavenLocal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modImplementation("com.m4ssive:m4lib:${property("m4lib_version")}+$mcVersion")
    modCompileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

loom {
    runConfigs.all { ideConfigGenerated(false) }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mc_dep" to mcDep,
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

tasks.jar {
    from(rootProject.file("LICENSE")) { rename { "${it}_WindChargeCounter" } }
}
