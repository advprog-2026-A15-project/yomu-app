plugins {
    // Inherit plugins from root
}

extra["springCloudVersion"] = "2023.0.1" 

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}