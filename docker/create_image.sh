DOCKER_USER="usuario_ejemplo"
BACKEND_IMAGE="biblio-app"
UTILITY_IMAGE="biblio-utility"
VERSION="latest"

echo "Construyendo imagen del Backend..."
docker build -t $DOCKER_USER/$BACKEND_IMAGE:$VERSION ../backend

echo "Construyendo imagen del Utility Service..."
docker build -t $DOCKER_USER/$UTILITY_IMAGE:$VERSION ../utility-service

echo "Publicando imágenes en Docker Hub..."
docker push $DOCKER_USER/$BACKEND_IMAGE:$VERSION
docker push $DOCKER_USER/$UTILITY_IMAGE:$VERSION

echo "Proceso finalizado con éxito."