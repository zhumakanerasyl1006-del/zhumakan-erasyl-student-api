# zhumakan-erasyl-student-api

Автор: **Zhumakan Erasyl**, IITU, группа **IT2-2302**, Student ID **37380**.
Учебный DevOps Midterm: Part 1 — Linux Administration; Part 2 — REST API управления студентами.

## Технологии

Java 21 toolchain, Spring Boot 3.5.7, Gradle 8.14.3, Spring Web, Jakarta Validation,
Spring Data JPA, PostgreSQL, Flyway; JUnit 5, Mockito, Spring Boot Test и H2 для тестов.
Версии зафиксированы; проверить их актуальный статус поддержки в текущем окружении
не удалось из-за отсутствия DNS. Docker и Jenkins пока не настроены.

## Статус проверки

Проверка этапа 3 (2026-10-29): установлены OpenJDK и javac **21.0.12.1**.
`JAVA_HOME` не задан, Java доступна через PATH; JDK находится в
`/usr/lib/jvm/java-21-openjdk-amd64`. Для явной настройки:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
```

В проекте зафиксирована Gradle **8.14.3**, совместимая с Java 21
(запуск Gradle на Java 21 поддерживается с версии 8.5).
Фактически запустить Gradle не удалось: отсутствуют официальный
`gradle/wrapper/gradle-wrapper.jar` и `gradlew.bat`.
`gradlew` имеет права 755, но пока является диагностическим launcher.

В терминале агента DNS для Gradle и Maven Central не работает, а запрос
напрямую к IP завершается `failed to open socket: Operation not permitted`.
Следовательно, доступ к интернету из пользовательской Ubuntu не означает
доступ из ограниченной среды агента. Локального Gradle и кэша зависимостей
в проверенных стандартных каталогах нет. Для продолжения нужен доступ к сети
из терминала выполнения либо заранее предоставленные официальные Gradle
дистрибутив, checksum и зависимости.

`./gradlew --version`, `./gradlew clean build`, `./gradlew test --rerun-tasks`
и `./gradlew bootJar` завершились с кодом 1 из-за отсутствия Wrapper JAR.
Выполнено **0 тестов**, успешных 0, неуспешных 0; тестирование не началось.
Исполняемый JAR не создан, реальные HTTP-запросы и PostgreSQL не проверены.
Все 6 контрольных сумм Part 1 совпали. Исходники и тесты не изменялись.
Полный протокол: `docs/part3-verification.txt`; исторический протокол этапа 2:
`docs/part2-verification.txt`.

## Структура

```text
src/main/java/kz/iitu/zhumakan/studentapi/
  StudentApiApplication.java
  student/      # entity, repository, service, controller
  dto/          # StudentRequest, StudentResponse
  exception/    # 404 и обработчик ошибок
  config/       # опциональный демонстрационный студент
src/main/resources/
  application.yml
  db/migration/V1__create_students.sql
src/test/java/kz/iitu/zhumakan/studentapi/   # Mockito, MVC, H2 integration, seed
src/test/resources/application-test.yml
build.gradle, settings.gradle, gradle.properties
gradlew, gradle/wrapper/gradle-wrapper.properties
scripts/, logs/, backups/                   # сохранённые результаты Part 1
docs/                                     # протокол Part 2 и контрольные суммы
Dockerfile, docker-compose.yml, Jenkinsfile # прежние пустые файлы
```

## Подготовка JDK и восстановление Wrapper

JDK 21 уже установлен. Команды ниже для загрузки — инструкции, они **не были успешно выполнены**
в текущем окружении. Всё скачивается внутри проекта, sudo не используется.
Если JDK 21 уже установлен, задайте `JAVA_HOME` и пропустите его скачивание.

```bash
cd /home/erasyl/zhumakan-erasyl-student-api
mkdir -p .tools/jdk21
curl -fL 'https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse' \
  -o .tools/jdk21.tar.gz
# Продолжать только после успешной загрузки:
tar -xzf .tools/jdk21.tar.gz -C .tools/jdk21 --strip-components=1
export JAVA_HOME="$PWD/.tools/jdk21"
export PATH="$JAVA_HOME/bin:$PATH"
export GRADLE_USER_HOME="$PWD/.gradle"
java -version
javac -version

curl -fL https://services.gradle.org/distributions/gradle-8.14.3-bin.zip \
  -o .tools/gradle-8.14.3-bin.zip
curl -fL https://services.gradle.org/distributions/gradle-8.14.3-bin.zip.sha256 \
  -o .tools/gradle-8.14.3-bin.zip.sha256
printf '%s  %s\n' "$(cat .tools/gradle-8.14.3-bin.zip.sha256)" \
  .tools/gradle-8.14.3-bin.zip | sha256sum -c -
# Распаковывать только после успешной проверки checksum:
python3 -m zipfile -e .tools/gradle-8.14.3-bin.zip .tools
chmod u+x .tools/gradle-8.14.3/bin/gradle
.tools/gradle-8.14.3/bin/gradle wrapper --gradle-version 8.14.3 \
  --distribution-type bin \
  --gradle-distribution-sha256-sum "$(cat .tools/gradle-8.14.3-bin.zip.sha256)"
# Повторная генерация стандартных scripts/JAR тем же Gradle:
./gradlew wrapper
./gradlew --version
./gradlew clean build
```

Официальный Wrapper создаст `gradle-wrapper.jar`, стандартные `gradlew` и `gradlew.bat`.
JAR является частью исходников проекта и должен храниться в Git после получения.
`.tools/`, `.gradle/`, `build/` и файлы `.env` исключены из Git.

## PostgreSQL и запуск

Заранее создайте базу `student_api` и отдельного пользователя с правами на эту базу.
Приложение не устанавливает сервер PostgreSQL. Flyway создаёт таблицу; Hibernate
проверяет схему (`ddl-auto=validate`). Настоящие пароли не храните в файлах проекта.

```bash
export DB_URL='jdbc:postgresql://localhost:5432/student_api'
export DB_USERNAME='student_api'
read -rsp 'PostgreSQL password: ' DB_PASSWORD; echo
export DB_PASSWORD
export SEED_DEMO_STUDENT=true
./gradlew bootRun
```

`SEED_DEMO_STUDENT=true` добавляет Zhumakan Erasyl / IT2-2302 / 37380,
если studentId 37380 отсутствует. По умолчанию seed выключен.
В деморежиме запускайте один экземпляр: проверка и вставка seed не атомарны,
одновременный первый запуск нескольких экземпляров может вызвать unique conflict.

После успешной сборки запуск JAR:

```bash
java -jar build/libs/student-api.jar
```

Переменные: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` обязательны;
`SERVER_PORT` по умолчанию 8080; `SEED_DEMO_STUDENT` по умолчанию false.
На следующем этапе Compose сможет передать URL с hostname сервиса PostgreSQL.
Конфигурация Compose сейчас не создавалась.

## API

`id` — внутренний числовой ключ БД, `studentId` — уникальный номер студента строкой.
`name` обязателен, до 120 символов; `groupName` обязателен, до 40;
`studentId` содержит от 1 до 30 цифр. ID в URL должен быть положительным.
PUT полностью заменяет три поля, возвращая тот же внутренний id.

| Метод | Endpoint | Успешный ответ |
|---|---|---|
| POST | `/api/students` | 201, JSON и Location |
| GET | `/api/students` | 200, JSON-массив по id |
| GET | `/api/students/{id}` | 200, JSON |
| PUT | `/api/students/{id}` | 200, JSON |
| DELETE | `/api/students/{id}` | 204, без тела |

Отсутствующий id: 404 для GET/PUT/DELETE; невалидные данные/JSON/id: 400;
нарушение уникальности studentId: 409. Ошибки возвращаются как ProblemDetail JSON,
ошибки полей содержат объект `errors`. Детали SQL клиенту не передаются.

```bash
# Если seed включён, этот POST для 37380 вернёт 409; для демонстрации создания
# запустите без seed на пустой БД либо используйте другой studentId.
curl -i -X POST http://localhost:8080/api/students \
  -H 'Content-Type: application/json' \
  -d '{"name":"Zhumakan Erasyl","groupName":"IT2-2302","studentId":"37380"}'
curl -i http://localhost:8080/api/students
# Подставьте фактический id из ответа POST/GET вместо 1:
curl -i http://localhost:8080/api/students/1
curl -i -X PUT http://localhost:8080/api/students/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Zhumakan Erasyl","groupName":"IT2-2302","studentId":"37380"}'
curl -i -X DELETE http://localhost:8080/api/students/1
curl -i http://localhost:8080/api/students/1
curl -i -X POST http://localhost:8080/api/students \
  -H 'Content-Type: application/json' -d '{}'
```

## Тестирование и защита

После восстановления JDK, Wrapper и доступа к Maven Central:

```bash
./gradlew clean build
./gradlew test --rerun-tasks
./gradlew bootJar
find build/test-results/test -name 'TEST-*.xml' -print
ls -l build/libs/student-api.jar
sha256sum -c docs/part1-sha256.txt
```

Подготовлены 33 тестовых сценария (26 обычных и 7 параметризованных запусков);
это подсчёт исходников, не результат выполнения.
Тесты используют отдельную H2 in-memory БД, PostgreSQL не требуется:
- StudentServiceTest: CRUD и отсутствие id, Mockito.
- StudentControllerTest: HTTP, Location, 400/404/409, JSON и валидация.
- StudentApiIntegrationTest: полный CRUD, Flyway/JPA, уникальность и rollback.
- DemoStudentTest: персонализированный seed в отдельной H2 БД.

HTML-отчёт после успешного запуска: `build/reports/tests/test/index.html`;
машинные результаты: `build/test-results/test/TEST-*.xml`.
H2 не доказывает совместимость с реальным PostgreSQL. Проверка PostgreSQL и
живые HTTP-запросы пока не выполнены. Авторизация и пагинация не реализованы.

## Part 1 — сохранённые результаты

Bash-скрипт `scripts/Zhumakan_Erasyl_system_report.sh` имеет права 744,
выводит сведения о Linux и дописывает их через `tee -a` в
`logs/Zhumakan_Erasyl_system_report.log`. Инструкция запуска:

```bash
bash -n scripts/Zhumakan_Erasyl_system_report.sh
./scripts/Zhumakan_Erasyl_system_report.sh
```

Запуск меняет лог; в Part 2 скрипт не запускался. Скрипт, логи, файл анализа
и backup Part 1 сохранены без изменений; контрольные суммы — `docs/part1-sha256.txt`.
README обновлён для Part 2, поэтому историческая копия
`backups/Zhumakan_Erasyl_README.md` теперь соответствует README этапа 1,
а не текущему README. Это ожидаемо; backup не перезаписывался.
`>` перезаписывает файл, `>>` дописывает в конец.

## CRUD API — Feature Branch

The Student Management API supports the following operations:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/students | Create a student |
| GET | /api/students | List all students |
| GET | /api/students/{id} | Get a student by ID |
| PUT | /api/students/{id} | Update a student |
| DELETE | /api/students/{id} | Delete a student |

The API uses JSON request and response bodies.
Project status: Feature branch version
