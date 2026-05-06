@echo off
if "%1"=="" (
    echo Usage: create_image.bat ^<dockerhub_user^>
    exit /b 1
)
set DOCKER_USER=%1

echo Building app-service image...
docker build -t %DOCKER_USER%/biblio-app:latest -f app-service.Dockerfile ..

echo Building utility-service image...
docker build -t %DOCKER_USER%/biblio-utility:latest -f utility-service.Dockerfile ..

echo Done. Images built locally.