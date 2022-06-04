
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
    
    container(displayName = "Continuous inspection of code quality and security",image = "sonarsource/sonar-scanner-cli"){
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
    
    docker("Docker build and push") {
        resources {
            cpu = 1.cpu
            memory = 2000.mb
        }
        beforeBuildScript {
            content = "cp -r  $mountDir/share docker"
        }
        build {
            context = "."
            file = "./Dockerfile"
            labels["vendor"] = "aitech"
        }
    
        push("aaziz93.registry.jetbrains.space/p/microservices/containers/spring-boot-template") {
            // use current job run number as a tag - '0.0.run_number'
            tags("1.0.\$JB_SPACE_EXECUTION_NUMBER", "lts")
            // see example on how to use branch name in a tag
        }
    }
}