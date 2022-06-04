/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Code analysis, test, build and push") {
    startOn {
        gitPush { enabled =true }
        schedule { cron("0 8 * * *") }
    }
    
    container(displayName = "Continuous inspection of code quality and security", image = "sonarsource/sonar-scanner-cli")
    {
        env["SONAR_LOGIN"] = Secrets("sonar_token")
        env["SONAR_HOST_URL"] = Params("sonar_host_url")
        args("-Dsonar.projectKey=a-aziz93_spring-boot-template","-Dsonar.organization=a-aziz93")
    }
    
    container(displayName = "Gradle test and build", image = "openjdk:11") {
        kotlinScript {api->
            api.gradlew("build")
        }
    }
    
    container("Build container and push", image = "trion/jib-cli") {
        resources {
            cpu = 1.cpu
            memory = 64.mb
        }
        shellScript {
            content="jib jar --target=aaziz93.registry.jetbrains.space/p/microservices/containers/spring-boot-template:1.0.\$JB_SPACE_EXECUTION_NUMBER build/libs/spring-boot-template-*.jar"
        }
    }
}