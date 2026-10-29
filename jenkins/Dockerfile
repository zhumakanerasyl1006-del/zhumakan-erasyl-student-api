FROM jenkins/jenkins:lts-jdk21

USER root

RUN apt-get update && \
    apt-get install -y --no-install-recommends docker-cli docker-buildx && \
    rm -rf /var/lib/apt/lists/* && \
    docker --version && docker buildx version

# Existing lab controller runs as root; its workspace was created as jenkins.
# Trust only this repository, never all repositories on the controller.
RUN git config --system --add safe.directory /var/jenkins_home/workspace/Zhumakan_Erasyl_Student_API

USER jenkins
