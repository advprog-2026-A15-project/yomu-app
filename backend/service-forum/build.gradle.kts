dependencies {
    implementation(project(":shared-lib"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
}