pipeline {
    agent {
        docker {
            label 'docker'
            image 'gradle:4.10.2-jdk8-alpine'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh 'gradle --no-daemon clean build'
            }
        }
        stage('Deploy PR') {
            environment {
                BINTRAY = credentials('fint-bintray')
            }
            when { changeRequest() }
            steps {
                sh "gradle --no-daemon -Pversion=0-${BRANCH_NAME}.${BUILD_NUMBER} -PbintrayUser=${BINTRAY_USR} -PbintrayKey=${BINTRAY_PSW} bintrayUpload"
            }
        }
        stage('Deploy Release') {
            environment {
                BINTRAY = credentials('fint-bintray')
            }
            when {
                tag pattern: "v\\d+\\.\\d+\\.\\d+(-\\w+-\\d+)?", comparator: "REGEXP"
            }
            steps {
                script {
                    VERSION = TAG_NAME[1..-1]
                }
                sh "echo Version is ${VERSION}"
                sh "gradle --no-daemon -Pversion=${VERSION} -PbintrayUser=${BINTRAY_USR} -PbintrayKey=${BINTRAY_PSW} bintrayUpload"
            }
        }
    }
}
