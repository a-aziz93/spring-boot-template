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
   /*
    container(displayName = "Sonar continuous inspection of code quality and security", image = "sonarsource/sonar-scanner-cli")
    {
        env["SONAR_LOGIN"] = Secrets("sonar_token")
        env["SONAR_HOST_URL"] = Params("sonar_host_url")
        args("-Dsonar.projectKey=a-aziz93_spring-boot-template","-Dsonar.organization=a-aziz93")
    }*/
    
    container(displayName = "Gradle test, build and publish to space registry", image = "gradle"){
        shellScript {
            content="""
                gradle build publish
                cp -r build $mountDir/share
                echo $(gradle properties -q | grep "^name:" | awk '{print $2}')-$(gradle properties -q | grep "^version:" | awk '{print $2}')>$mountDir/${getArtifactFilePath()}
                """
        }
    }
    
    container("Jib build docker container and publish to space registry", image = "trion/jib-cli") {
        resources {
            cpu = 1.cpu
            memory = 2000.mb
        }
        env["SPACE_DOCKER_REGISTRY_USER"] = Params("space_docker_registry_user")
        env["SPACE_DOCKER_REGISTRY_TOKEN"] = Params("space_docker_registry_token")
        shellScript {
            content = """
                ARTIFACT_NAME=`cat $mountDir/${getArtifactFilePath()}`
                jib jar --to-username=${'$'}SPACE_DOCKER_REGISTRY_USER --to-password=${'$'}SPACE_DOCKER_REGISTRY_TOKEN --target=aaziz93.registry.jetbrains.space/p/microservices/containers/"${'$'}ARTIFACT_NAME" $mountDir/share/build/libs/"${'$'}ARTIFACT_NAME".jar
            """
        }
    }
}

fun getArtifactFilePath():String{
    return "share/artifact-${System.getenv("JB_SPACE_API_URL").split(".")[0].replaceBefore("/","").replaceFirst("//","")}-${'$'}JB_SPACE_PROJECT_KEY-${'$'}JB_SPACE_EXECUTION_NUMBER"
}