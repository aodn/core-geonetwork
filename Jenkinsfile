#!groovy​

pipeline {
    agent none

    stages {
        stage('git_setup') {
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
                    args '-v ${HOME}/.m2:/home/jenkins/.m2 -v ${HOME}/.svn:/home/jenkins/.svn'
                }
            }
            environment {
                HOME = '/home/jenkins'
                JAVA_TOOL_OPTIONS = '-Duser.home=/home/jenkins'
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
