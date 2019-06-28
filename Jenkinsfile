#!groovy​

pipeline {
    agent none

    stages {
        stage('clean') {
            agent { label 'master' }
            steps {
                sh 'git clean -fdx'
                sh 'git submodule sync'
                sh 'git submodule update --init --recursive'
            }
        }
        
        stage('container') {
            agent {
                dockerfile {
                    args '-v ${HOME}/.m2:/home/builder/.m2 -v ${HOME}/.svn:/home/builder/.svn'
                    additionalBuildArgs '--build-arg BUILDER_UID=${JENKINS_UID:-9999}'
                }
            }
            environment {
                HOME = '/home/builder'
                JAVA_TOOL_OPTIONS = '-Duser.home=/home/builder'
            }
            stages {
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
