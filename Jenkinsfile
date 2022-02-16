pipeline {
    agent any
    options {
        skipStagesAfterUnstable()
    }
    stages {

        stage('Build'){
            steps {
                // for prod env
                bat "mvn -Dusername=${username} -Dpassword=${password} -Durl=${url} -Dauth_path=${auth_path} clean install -DskipTests"

                // for dev env
                bat "mvn clean install -DskipTests -P dev"
            }
        }
        stage('Deploy'){
            steps {
                   bat "java -cp C:\\WINDOWS\\system32\\config\\systemprofile\\.m2\\repository\\org\\example\\user-management-api\\1.0-SNAPSHOT\\user-management-api-1.0-SNAPSHOT.jar com.neplab.UserManagement"
            }
        }
    }
}