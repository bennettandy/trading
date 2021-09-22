#/bin/bash!

export TAG="latest"
export TAG="europe-west2-docker.pkg.dev/trading-326621/trading/trading:$TAG"

docker build -t $TAG .
