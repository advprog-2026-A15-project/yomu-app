dependencies {
    // 1. Import Shared Library (
    implementation(project(":shared-lib"))

    // 2. Web & Validation
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 3. Database
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
}