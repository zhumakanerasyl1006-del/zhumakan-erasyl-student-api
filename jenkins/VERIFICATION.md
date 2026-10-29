# Проверка после исправления — 30 октября 2026

Jenkins: http://localhost:8081/job/Zhumakan_Erasyl_Student_API/5/

Результат сборки №5: **SUCCESS**. Проверен исходный Jenkinsfile из GitHub main,
коммит `6ee8a352ef32001ba40a4e3ea060720cc080d761`. Jenkinsfile менять не потребовалось.

| Этап | Результат |
|---|---|
| Checkout | SUCCESS; main получен с GitHub, lightweight SCM cache восстановлен |
| Build | SUCCESS; `./gradlew clean assemble --no-daemon` |
| Test | SUCCESS; 33 tests, 0 failures, 0 errors, 0 skipped по XML |
| Docker Build | SUCCESS; multi-stage Dockerfile, Java 21 |
| Verify | SUCCESS; `docker image inspect` |

Образ приложения: `zhumakan-erasyl-student-api:jenkins`.
Image ID: `sha256:67df8a23985a12ac838f841026401df7557d85c4878cdc2418e31db472d824d1`.
Пользователь образа приложения: `appuser`.
Verify проверяет наличие образа; Pipeline не выполняет развёртывание.

## Диагностика и исправления

- Jenkins 2.580.1 / Java 21 / Debian 13, существующий controller user: root.
- `docker.io` установлен, но `/usr/bin/docker` отсутствовал: CLI находится в
  отдельном пакете `docker-cli`. Установлены `docker-cli` и `docker-buildx`.
- Docker CLI 26.1.5 успешно работает с Docker Desktop Engine 29.8.2 через
  существующий socket. Менять права socket не потребовалось.
- Workspace принадлежит jenkins, controller запущен как root. Непосредственно
  воспроизведён `fatal: detected dubious ownership`. В образ добавлен точный
  `safe.directory` только для Student API workspace.
- Логи старых сборок: #1 — невозможно создать cache; #2 — выбрана master вместо
  main; #4 — `fatal: not in a git directory` в lightweight cache. Старого cache
  на момент диагностики уже не было, поэтому его прежнее содержимое проверить
  невозможно. Новый cache и checkout успешно проверены сборкой #5.
- Новый Jenkins запущен через `jenkins/compose.yml`, используя прежний external
  volume `jenkins_home`, прежние порты и socket. Старый контейнер сохранён
  остановленным как `jenkins-before-cli-fix-20261030`.
- Одноразовый Groovy startup hook запустил существующее задание через внутренний
  API Jenkins. Hook удалил собственный файл; авторизация и задания не изменены.
  Первый служебный вызов API был исправлен на `scheduleBuild2(int, Action[])`.

## Основные выполненные команды

Повторные чтения логов и polling опущены.

```bash
git status --short
git log -3 --oneline
docker version
docker ps -a
docker inspect jenkins
docker exec jenkins sh -c 'id; command -v docker; dpkg-query -W docker.io docker-cli'
docker exec jenkins dpkg -L docker.io
docker exec jenkins sh -c 'apt-get update && apt-get install -y --no-install-recommends docker-cli'
docker exec jenkins apt-get install -y --no-install-recommends docker-buildx
docker exec jenkins git config --global --add safe.directory /var/jenkins_home/workspace/Zhumakan_Erasyl_Student_API
docker build -t student-jenkins:local -f jenkins/Dockerfile jenkins
docker run --rm --user root -v /var/run/docker.sock:/var/run/docker.sock student-jenkins:local sh -c 'docker version; docker buildx version'
docker compose -f jenkins/compose.yml config --quiet
# Подготовка и копирование одноразового startup hook через docker cp.
# Один диагностический restart Jenkins, затем миграция на проверенный образ:
docker stop jenkins
docker rename jenkins jenkins-before-cli-fix-20261030
docker compose -f jenkins/compose.yml up -d --no-build
docker exec jenkins docker version
docker exec jenkins git -C /var/jenkins_home/workspace/Zhumakan_Erasyl_Student_API rev-parse HEAD
docker image inspect zhumakan-erasyl-student-api:jenkins
# XML тестов скопированы через docker cp в /tmp и посчитаны Python ElementTree.
docker compose -f jenkins/compose.yml ps
docker volume inspect jenkins_home
curl --fail --silent http://localhost:8080/api/students
git check-ignore .env
git ls-files .env
git diff --check
```

## Сохранность

Работающее приложение не перезапускалось, GET `/api/students` возвращает HTTP 200.
На момент проверки список содержит 0 записей; существующие данные не изменялись.
PostgreSQL healthy, volume `zhumakan-erasyl-student-api_postgres_data` сохранён.
Jenkins volume `jenkins_home`, оба существующих задания и история сборок сохранены.
`.env` игнорируется и не отслеживается Git. Пароли и токены не выводились.
Git-коммиты, авторы и история не изменялись; commit/push не выполнялись.

## Для сдачи

Открыть сборку №5 по ссылке выше, сохранить подтверждение SUCCESS и 33 тестов.
Новые файлы `jenkins/` пока не закоммичены. Просмотреть их, затем сохранить
в Git и отправить в GitHub только с разрешения владельца.
