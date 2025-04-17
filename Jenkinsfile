import groovy.json.JsonSlurper
import java.security.*

def getFolderName() {
    def array = pwd().split("/")
    return array[array.length - 2];
}
def parseJson(jsonString) {
    def lazyMap = new JsonSlurper().parseText(jsonString)
    def m = [:]
    m.putAll(lazyMap)
    return m
}
def parseJsonArray(jsonString){
    def datas = readJSON text: jsonString
    return datas
}

def parseJsonString(jsonString, key){
    def datas = readJSON text: jsonString
    String Values = writeJSON returnText: true, json: datas[key]
    return Values
}

def parseYaml(jsonString) {
    def datas = readYaml text: jsonString
    String yml = writeYaml returnText: true, data: datas['kubernetes']
    return yml

}

def createYamlFile(data,filename) {
    writeFile file: filename, text: data
}


def getValue(metadata, key){
    def metadMap = parseJson(metadata)
    def value = metadMap['general']['contextPath']
    return value;
}

def agentLabel = "${env.JENKINS_AGENT == null ? "":env.JENKINS_AGENT}"

def pushToCollector(){
  print("Inside pushToCollector...........")
    def job_name = "$env.JOB_NAME"
    def job_base_name = "$env.JOB_BASE_NAME"
    String generalProperties = parseJsonString(env.JENKINS_METADATA,'general')
    generalPresent = parseJsonArray(generalProperties)
    if(generalPresent.tenant != '' &&
    generalPresent.lazsaDomainUri != ''){
      echo "Job folder - $job_name"
      echo "Pipeline Name - $job_base_name"
      echo "Build Number - $currentBuild.number"
      sh """curl -k -X POST '${generalPresent.lazsaDomainUri}/collector/orchestrator/devops/details' -H 'X-TenantID: ${generalPresent.tenant}' -H 'Content-Type: application/json' -d '{\"jobName\" : \"${job_base_name}\", \"projectPath\" : \"${job_name}\", \"agentId\" : \"${generalPresent.agentId}\", \"devopsConfigId\" : \"${generalPresent.devopsSettingId}\", \"agentApiKey\" : \"${generalPresent.agentApiKey}\", \"buildNumber\" : \"${currentBuild.number}\" }' """
    }
}

pipeline {
  agent { label agentLabel }
  environment {
    DEFAULT_STAGE_SEQ = "'CodeCheckout','Deploy','UnitTests','publishReports','Destroy'"
    CUSTOM_STAGE_SEQ = "${DYNAMIC_JENKINS_STAGE_SEQUENCE}"
    PROJECT_TEMPLATE_ACTIVE = "${DYNAMIC_JENKINS_STAGE_NEEDED}"
    LIST = "${env.PROJECT_TEMPLATE_ACTIVE == 'true' ? env.CUSTOM_STAGE_SEQ : env.DEFAULT_STAGE_SEQ}"
    BRANCHES = "${env.GIT_BRANCH}"
    COMMIT = "${env.GIT_COMMIT}"
    RELEASE_NAME = "selenium"
    SERVICE_PORT = "${APP_PORT}"
    DOCKERHOST = "${DOCKERHOST_IP}"
    REGISTRY_URL = "${DOCKER_REPO_URL}"
    ACTION = "${ACTION}"
    DEPLOYMENT_TYPE = "${DEPLOYMENT_TYPE == ""? "EC2":DEPLOYMENT_TYPE}"
    KUBE_SECRET = "${KUBE_SECRET}"
    foldername = getFolderName()
    CHROME_BIN = "/usr/bin/google-chrome"
    ARTIFACTORY = "${ARTIFACTORY == ""? "ECR":ARTIFACTORY}"
    ARTIFACTORY_CREDENTIALS = "${ARTIFACTORY_CREDENTIAL_ID}"
    JENKINS_METADATA = "${JENKINS_METADATA}"
    JAVA_MVN_IMAGE_VERSION = "maven:3.8.1-openjdk-17-slim"
    SELENIUM_IMAGE_TAG = "4.31.0-20250414"
    SELENIUM_VIDEO_TAG = "ffmpeg-7.1-20250414"
    KUBECTL_IMAGE_VERSION = "bitnami/kubectl:1.28" //https://hub.docker.com/r/bitnami/kubectl/tags
    HELM_IMAGE_VERSION = "alpine/helm:3.8.1" //https://hub.docker.com/r/alpine/helm/tags
    OC_IMAGE_VERSION = "quay.io/openshift/origin-cli:4.9.0" //https://quay.io/repository/openshift/origin-cli?tab=tags

  }
  stages {
     stage('Running Stages') {
           agent { label agentLabel }
           steps {
             script {
               def listValue = "$env.LIST"
               def list = listValue.split(',')
               print(list)
               echo "projectTemplateActive - $env.PROJECT_TEMPLATE_ACTIVE"
               if (env.CUSTOM_STAGE_SEQ != null) {
                 echo "customStagesSequence - $env.CUSTOM_STAGE_SEQ"
               }
               echo "defaultStagesSequence - $env.DEFAULT_STAGE_SEQ"
               String generalProperties = parseJsonString(env.JENKINS_METADATA,'general')
               metadataVars = parseJsonArray(generalProperties)
               String kubeProperties = parseJsonString(env.JENKINS_METADATA,'kubernetes')
               kubeVars = parseJsonArray(kubeProperties)

               for (int i = 0; i < list.size(); i++) {
                 print(list[i])
                 if (list[i] == "'CodeCheckout'") {
                   print(list[i])
                   stage('Initialisation') {
                     // stage details here
                     TEMP_STAGE_NAME = "$STAGE_NAME"
                     def job_name = "$env.JOB_NAME"
                     print(job_name)
                     def namespace = ''
                     def values = job_name.split('/')
                     if (env.DEPLOYMENT_TYPE == 'KUBERNETES') {
                        if (kubeVars.namespace != null && kubeVars.namespace != '') {
                            namespace = kubeVars.namespace
                        } else {
                            namespace_prefix = values[0].replaceAll("[^a-zA-Z0-9\\-\\_]+", "").toLowerCase().take(50)
                            namespace = "$namespace_prefix-$env.foldername".toLowerCase()
                        }
                    }
                     serviceData = kubeVars.service
                     serviceType = serviceData.type
                     print(serviceType)
                     env.service_type=serviceType
                     service = values[2].replaceAll("[^a-zA-Z0-9\\-\\_]+","").toLowerCase().take(50)
                     print("kube namespace: $namespace")
                     print("service name: $service")
                     env.namespace_name=namespace
                     env.service=service
                     if (env.ARTIFACTORY == "ACR"){
                      def url_string = "$REGISTRY_URL"
                      url = url_string.split('/')
                      env.ACR_LOGIN_URL = url[0]
                      echo "Reg Login url: $ACR_LOGIN_URL"
                   } 
                   }
                 }
                 else if ("${list[i]}" == "'Deploy'" && env.ACTION == 'DEPLOY') {
                  stage('Deploy') {
                   script {
                     TEMP_STAGE_NAME = "$STAGE_NAME"
                      if (env.DEPLOYMENT_TYPE == 'EC2') {
                          sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker pull selenium/hub:$SELENIUM_IMAGE_TAG "'
                          sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker pull selenium/node-chrome:$SELENIUM_IMAGE_TAG "'
                          sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker pull selenium/node-firefox:$SELENIUM_IMAGE_TAG"'
                          sh 'ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker pull selenium/video:$SELENIUM_VIDEO_TAG"'
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker stop video-firefox-${generalPresent.repoName} video-chrome-${generalPresent.repoName} firefox-${generalPresent.repoName} chrome-${generalPresent.repoName} selenium-hub-${generalPresent.repoName} || true && docker rm video-firefox-${generalPresent.repoName} video-chrome-${generalPresent.repoName} firefox-${generalPresent.repoName} chrome-${generalPresent.repoName} selenium-hub-${generalPresent.repoName} || true" """
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker network rm grid-${generalPresent.repoName} || true" """
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker network create grid-${generalPresent.repoName}" """
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker run -d -p 4442-4444:4442-4444 --net grid-${generalPresent.repoName} --name selenium-hub-${generalPresent.repoName} selenium/hub:$SELENIUM_IMAGE_TAG" """
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker run -d --net grid-${generalPresent.repoName} -e SE_EVENT_BUS_HOST=selenium-hub-${generalPresent.repoName} -e SE_VNC_PASSWORD=secret -e SE_RECORD_VIDEO=true --name chrome-${generalPresent.repoName} selenium/node-chrome:$SELENIUM_IMAGE_TAG" """
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker run -d --net grid-${generalPresent.repoName} -e SE_EVENT_BUS_HOST=selenium-hub-${generalPresent.repoName} -e SE_VNC_PASSWORD=secret -e SE_RECORD_VIDEO=true --name firefox-${generalPresent.repoName} selenium/node-firefox:$SELENIUM_IMAGE_TAG" """
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker run -d --net grid-${generalPresent.repoName} --name video-firefox-${generalPresent.repoName} -v /tmp/videos:/videos -e SE_NODE_GRID_URL=http://selenium-hub-${generalPresent.repoName}:4444 -e SE_VIDEO_FILE_NAME=auto -e SE_VIDEO_UPLOAD_ENABLED=true -e DISPLAY_CONTAINER_NAME=firefox-${generalPresent.repoName} selenium/video:$SELENIUM_VIDEO_TAG" """
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker run -d --net grid-${generalPresent.repoName} --name video-chrome-${generalPresent.repoName} -v /tmp/videos:/videos -e SE_NODE_GRID_URL=http://selenium-hub-${generalPresent.repoName}:4444 -e SE_VIDEO_FILE_NAME=auto -e SE_VIDEO_UPLOAD_ENABLED=true -e DISPLAY_CONTAINER_NAME=chrome-${generalPresent.repoName} selenium/video:$SELENIUM_VIDEO_TAG" """
                          env.REMOTE_DRIVER_HOST = "http://$DOCKERHOST:$SERVICE_PORT"

                      }
                      if (env.DEPLOYMENT_TYPE == 'KUBERNETES') {

                          withCredentials([file(credentialsId: "$KUBE_SECRET", variable: 'KUBECONFIG')]) {
                              env.helmReleaseName = "${metadataVars.repoName}"
                              sh '''
                              docker run --rm  --user root -v "$KUBECONFIG":"$KUBECONFIG" -e KUBECONFIG="$KUBECONFIG" $KUBECTL_IMAGE_VERSION create ns "$namespace_name" || true
                              mkdir helm || true
                              mkdir helm-cache || true
                              docker run --rm  --user root -v "$KUBECONFIG":"$KUBECONFIG" -e KUBECONFIG="$KUBECONFIG" -v "$WORKSPACE":/apps -v "$WORKSPACE"/helm:/root/.config/helm -v "$WORKSPACE"/helm-cache:/root/.config/helm-cache $HELM_IMAGE_VERSION repo add docker-selenium https://www.selenium.dev/docker-selenium
                              docker run --rm  --user root -v "$KUBECONFIG":"$KUBECONFIG" -e KUBECONFIG="$KUBECONFIG" -v "$WORKSPACE":/apps -v "$WORKSPACE"/helm:/root/.config/helm -v "$WORKSPACE"/helm-cache:/root/.config/helm-cache $HELM_IMAGE_VERSION repo update
                              docker run --rm  --user root -v "$KUBECONFIG":"$KUBECONFIG" -e KUBECONFIG="$KUBECONFIG" -v "$WORKSPACE":/apps -v "$WORKSPACE"/helm:/root/.config/helm -v "$WORKSPACE"/helm-cache:/root/.config/helm-cache $HELM_IMAGE_VERSION search repo docker-selenium --versions
                              docker run --rm  --user root -v "$KUBECONFIG":"$KUBECONFIG" -e KUBECONFIG="$KUBECONFIG" -v "$WORKSPACE":/apps -v "$WORKSPACE"/helm:/root/.config/helm -v "$WORKSPACE"/helm-cache:/root/.config/helm-cache $HELM_IMAGE_VERSION upgrade --install "$helmReleaseName" docker-selenium/selenium-grid --set isolateComponents=false --set hub.serviceType="$service_type" --set hub.serviceAnnotations."service\\.beta\\.kubernetes\\.io/aws-load-balancer-scheme"="internet-facing" -n "$namespace_name"

                              sleep 20
                              '''
                                script {
                                env.temp_service_name = "${metadataVars.repoName}-selenium-hub".take(63)
                                def url = sh (returnStdout: true, script: '''kubectl get svc -n "$namespace_name" | grep "$temp_service_name" | awk '{print $4}' ''').trim()
                                  if (url != "<pending>") {
                                    env.REMOTE_DRIVER_HOST = "http://$url:4444"
                                    print("##\$@\$ http://$url:4444/ ##\$@\$")
                                  }
                                }
                          }
                      }
                    }
                   }
                  }
                 else if ("${list[i]}" == "'UnitTests'"  && env.ACTION == 'DEPLOY') {
                   stage('Unit Tests') {
                     script{
                       TEMP_STAGE_NAME = "$STAGE_NAME"
                       sh '''
                           sleep 60
                           docker run --rm -v "$WORKSPACE":/usr/src/mymaven -w /usr/src/mymaven $JAVA_MVN_IMAGE_VERSION mvn clean install -DREMOTE_DRIVER_HOST="$REMOTE_DRIVER_HOST"
                           #mvn clean install -DREMOTE_DRIVER_HOST="$REMOTE_DRIVER_HOST"
                           #junit keepLongStdio: true, skipMarkingBuildUnstable: true, testResults: 'target/surefire-reports/*.xml'
                           
                       '''
                       junit allowEmptyResults: true, keepLongStdio: true, skipMarkingBuildUnstable: true, testResults: 'target/surefire-reports/*.xml'
                       publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'target/surefire-reports/', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: 'Surefire reports', useWrapperFileDirectly: true])
                     }
                   }
                 }
                 else if ("${list[i]}" == "'publishReports'"  && env.ACTION == 'DEPLOY') {
                   stage('Publish Reports') {
                     script{
                       junit allowEmptyResults: true, keepLongStdio: true, skipMarkingBuildUnstable: true, testResults: 'target/surefire-reports/*.xml'
                       publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'target/surefire-reports/', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: 'Surefire reports', useWrapperFileDirectly: true])
                     }
                   }
                 }
                 else if ("${list[i]}" == "'Destroy'" && env.ACTION == 'DESTROY') {
                  stage('Destroy') {
                    TEMP_STAGE_NAME = "$STAGE_NAME"
                    if (env.DEPLOYMENT_TYPE == 'EC2') {
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker stop video-firefox-${generalPresent.repoName} video-chrome-${generalPresent.repoName} firefox-${generalPresent.repoName} chrome-${generalPresent.repoName} selenium-hub-${generalPresent.repoName} || true && docker rm video-firefox-${generalPresent.repoName} video-chrome-${generalPresent.repoName} firefox-${generalPresent.repoName} chrome-${generalPresent.repoName} selenium-hub-${generalPresent.repoName} || true" """
                          sh """ssh -o "StrictHostKeyChecking=no" ciuser@$DOCKERHOST "docker network rm grid-${generalPresent.repoName} || true" """
                    }
                    if (env.DEPLOYMENT_TYPE == 'KUBERNETES') {
                      withCredentials([file(credentialsId: "$KUBE_SECRET", variable: 'KUBECONFIG')]) {
                      env.helmReleaseName = "${metadataVars.helmReleaseName}"
                      sh '''
                      helm uninstall "$helmReleaseName" -n "$namespace_name"
                      '''
                      }
                    }
                  }
                 }
               }
             }
           }
     }
  }
  post { 
        failure {
          pushToCollector()
        }
        success {
          pushToCollector()
        }
        aborted {
            pushToCollector()
        }
  }
}
