@echo off
if "%1"=="" (
    echo Usage: publish_docker-compose.bat ^<dockerhub_user^>
    exit /b 1
)
set DOCKER_USER=%1

echo Publishing docker_compose.yml as OCI Artifact to DockerHub...
echo {} > empty.json
oras push registry-1.docker.io/%DOCKER_USER%/biblioonline-compose:latest --config empty.json:application/vnd.docker.compose.project docker-compose.yml:application/vnd.docker.compose.file+yaml
del empty.json
echo Done.