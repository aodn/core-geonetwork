#!groovy​

pipeline {
    agent none

    stages {
        stage('container') {
            agent {
                dockerfile {
                    args '-v ${HOME}/.m2:/home/builder/.m2 -v ${HOME}/.svn:/home/builder/.svn -v ${HOME}/bin:${HOME}/bin'
                    additionalBuildArgs '--build-arg BUILDER_UID=$(id -u)'
                }
            }
            stages {
                stage('submodule') {
                    steps {
                        sh 'git submodule sync'
                        sh 'git submodule update --init --recursive'
                    }
                }
                stage('set_version_build') {
                    when { not { branch "2.10.x-imos" } }
                    steps {
                        sh './bumpversion.sh build'
                    }
                }
                stage('set_version_release') {
                    when { branch "2.10.x-imos" }
                    steps {
                        withCredentials([usernamePassword(credentialsId: env.GIT_CREDENTIALS_ID, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                            sh './bumpversion.sh release'
                        }
                    }
                }
                stage('package') {
                    steps {
                        sh 'mvn -B clean package -Dfindbugs.skip=true -Dmaven.test.skip=true -Dmaven.junit.jvmargs=-Xmx512m'
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/geonetwork.war', fingerprint: true, onlyIfSuccessful: true
                }
            }
        }
    }
}
