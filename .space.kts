/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Build Fleet indexes") {
    // ide is an IDE you want Space to build indexes for:
    // for JetBrains Fleet - Ide.Fleet
    // for IntelliJ-based IDEs via Gateway -
    // Ide.Idea, Ide.WebStorm, Ide.RubyMine,
    // Ide.CLion, Ide.GoLand, Ide.PhpStorm,
    // Ide.PyCharm, Ide.Rider
    
    // optional
    startOn {
        // run on schedule every day at 5AM
        schedule { cron("0 5 * * *") }
        // run on every commit...
        gitPush {
            // ...but only to the main branch
            branchFilter {
                +"refs/heads/main"
            }
        }
    }
    
    warmup(ide = Ide.Fleet) {
        // this job will run in a container based on the image
        // specified in the devfile.yaml (if specified) and for ide version (if specified)
        devfile = ".space/devfile.yaml"
        // optional custom script
        scriptLocation = "./dev-env-warmup.sh"
    }
    
    // optional
    git {
        // fetch the entire commit history
        depth = UNLIMITED_DEPTH
        // fetch all branches
        refSpec = "refs/*:refs/*"
    }
}

job("Code analysis, test, build and push") {
    startOn {
        gitPush { enabled =true }
    }
   
    container(displayName = "Sonarqube continuous inspection of code quality and security", image = "openjdk:11")
    {
        env["SONAR_TOKEN"] = Secrets("spring_boot_template_sonar_token")
        kotlinScript { api->
            api.gradlew("sonarqube")
        }
    }
    
    container(displayName = "Gradle test, build and publish to space maven registry", image = "openjdk:11"){
       kotlinScript {api->
           api.gradlew("test","publish")
       }
    }
    
    container("Jib build docker container and publish to space docker registry", image = "gradle") {
        shellScript {
            content="""
                gradle jib -Djib.to.auth.username=${'$'}JB_SPACE_CLIENT_ID -Djib.to.auth.password=${'$'}JB_SPACE_CLIENT_SECRET
                """
        }
    }
}

fun getArtifactSuffix():String{
    return "${System.getenv("JB_SPACE_API_URL").split(".")[0].replaceBefore("/","").replaceFirst("//","")}-${'$'}JB_SPACE_PROJECT_KEY-${'$'}JB_SPACE_EXECUTION_NUMBER"
}