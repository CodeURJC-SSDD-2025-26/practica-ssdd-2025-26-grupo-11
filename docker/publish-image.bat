@echo off
if "%1"=="" (
    echo Usage: publish_image.bat ^<dockerhub_user^>
    exit /b 1
)
set DOCKER_USER=%1

echo Pushing app-service to DockerHub...
docker push %DOCKER_USER%/biblio-app:latest

echo Pushing utility-service to DockerHub...
docker push %DOCKER_USER%/biblio-utility:latest

echo Done.