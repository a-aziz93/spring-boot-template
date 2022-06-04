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
   
    container(displayName = "Sonar continuous inspection of code quality and security", image = "sonarsource/sonar-scanner-cli")
    {
        env["SONAR_LOGIN"] = Secrets("sonar_token")
        env["SONAR_HOST_URL"] = Params("sonar_host_url")
        args("-Dsonar.projectKey=a-aziz93_spring-boot-template","-Dsonar.organization=a-aziz93")
    }
    
    container(displayName = "Gradle test and build", image = "openjdk:11") {
        shellScript {
            content = """
                    ./gradlew build
                    cp -r build $mountDir/share
                """
        }
    }
    
    container("Jib build docker container and push to space registry", image = "trion/jib-cli") {
        resources {
            cpu = 1.cpu
            memory = 64.mb
        }
        args("jib","jar","--target=aaziz93.registry.jetbrains.space/p/microservices/containers/spring-boot-template:1.0.0","$mountDir/share/build/libs/spring-boot-template-1.0.0.jar")
    }
}