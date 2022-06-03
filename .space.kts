/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Code analysis, build and run tests") {
    startOn {
        gitPush { enabled=true }
        schedule { cron("0 8 * * *") }
    }
    container(displayName = "Continuous inspection of code quality and security",image = "sonarsource/sonar-scanner-cli"){
        env["SONAR_LOGIN"] = Secrets("sonar_token")
        env["SONAR_HOST_URL"] = Params("sonar_host_url")
        env["SONAR_ORGANIZATION"] = "a-aziz93"
        env["SONAR_PROJECT_KEY"] = "spring-boot-template"
    }
    
    container(displayName = "Gradle build", image = "openjdk:11") {
        kotlinScript { api ->
            // here goes complex logic
            api.gradlew("clean")
            api.gradlew("build")
        }
    }
    
    docker("Docker build and push") {
        // get auth data from secrets and put it to env vars
        env["DOCKERHUB_USER"] = Params("dockerhub_user")
        env["DOCKERHUB_TOKEN"] = Secrets("dockerhub_token")
        
        // put auth data to Docker config
        beforeBuildScript {
            content = """
                B64_AUTH=${'$'}(echo -n ${'$'}DOCKERHUB_USER:${'$'}DOCKERHUB_TOKEN | base64 -w 0)
                echo "{\"auths\":{\"https://index.docker.io/v1/\":{\"auth\":\"${'$'}B64_AUTH\"}}}" > ${'$'}DOCKER_CONFIG/config.json
            """
        }
        
        build {
            labels["vendor"] = "aitech"
        }
        
        //in push, specify repo_name/image_name
        push("aitech/spring-boot-template") {
            tags("1.0.\$JB_SPACE_EXECUTION_NUMBER")
        }
    }
}