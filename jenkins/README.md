# Jenkins: Docker Desktop / WSL2

The controller uses the existing external volume `jenkins_home`. The application
Compose project and its PostgreSQL volume are independent and must not be removed.

Debian 13 splits Docker packages: `docker.io` provides the daemon, while
`docker-cli` provides `/usr/bin/docker`. This image installs `docker-cli` and
`docker-buildx`; the engine is Docker Desktop through `/var/run/docker.sock`.

## Start and update

```bash
docker compose -f jenkins/compose.yml build
docker compose -f jenkins/compose.yml up -d
docker exec jenkins docker version
docker exec jenkins docker buildx version
```

This lab configuration preserves the existing root controller user and socket
access. Docker socket access grants control of the host Docker Engine.
The image trusts only the existing Student API workspace, whose files were
originally created by user `jenkins`. Do not use `safe.directory '*'`.

The former standalone container is retained stopped under
`jenkins-before-cli-fix-20261030` for rollback. Never start both controllers
against the same Jenkins home. For rollback, stop the Compose controller first,
then start the old container (its port is still 8081).

## Existing Pipeline job

Open http://localhost:8081/job/Zhumakan_Erasyl_Student_API/ and sign in using the
existing Jenkins account. Configure:

- Definition: Pipeline script from SCM; SCM: Git.
- Repository: https://github.com/zhumakanerasyl1006-del/zhumakan-erasyl-student-api.git
- Branch: `*/main`; Script Path: `Jenkinsfile`; Lightweight checkout enabled.
- Public repository: no GitHub credential is required.

Click **Build Now**, then the new build number and **Console Output**.
Expected stages: Checkout, Build, Test, Docker Build, Verify.
The current Jenkinsfile's Verify stage inspects the built image; it does not deploy.

## Terminal checks

```bash
docker exec jenkins git -C /var/jenkins_home/workspace/Zhumakan_Erasyl_Student_API rev-parse HEAD
docker exec jenkins docker image inspect zhumakan-erasyl-student-api:jenkins --format '{{.Id}}'
docker ps --format 'table {{.Names}}\t{{.Status}}'
curl --fail http://localhost:8080/api/students
```

Historical failures: build #1 could not create the SCM cache; build #2 selected
nonexistent `master`; build #4 failed inside the lightweight SCM cache. After
switching the controller user, the existing workspace also produced Git's
`dubious ownership` error. The cache was already empty at diagnosis; no history
or cache was deleted during this repair. A fresh checkout validates its recovery.
