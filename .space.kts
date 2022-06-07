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
   
    container(displayName = "Sonarqube continuous inspection of code quality and security", image = "openjdk:11")
    {
        env["SONAR_TOKEN"] = Secrets("sonar_token")
        kotlinScript { api->
            api.gradlew("sonarqube")
        }
    }
    
    container(displayName = "Gradle test, build and publish to space maven registry", image = "openjdk:11"){
       kotlinScript {api->
           api.gradlew("test","publish")
       }
    }
    
    container("Jib build docker container and publish to space docker registry", image = "openjdk:11") {
        kotlinScript { api->
            api.gradlew("jib","-Djib.to.auth.username=${'$'}JB_SPACE_CLIENT_ID","-Djib.to.auth.password=${'$'}JB_SPACE_CLIENT_TOKEN")
        }
    }
}

fun getArtifactSuffix():String{
    return "${System.getenv("JB_SPACE_API_URL").split(".")[0].replaceBefore("/","").replaceFirst("//","")}-${'$'}JB_SPACE_PROJECT_KEY-${'$'}JB_SPACE_EXECUTION_NUMBER"
}