DOCKER_USER="usuario_ejemplo"
COMPOSE_ARTIFACT="biblio-compose"
VERSION="latest"

echo "Publicando Docker Compose como OCI Artifact en Docker Hub..."
docker compose -f ../docker-compose.yml push
