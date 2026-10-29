# zhumakan-erasyl-student-api

- Full name: Zhumakan Erasyl
- Group: IT2-2302
- Student ID: 37380

## Описание проекта

Учебный проект Student API для DevOps Midterm. В Part 1 — Linux Administration
реализован сбор сведений о Linux: дата и время, пользователь, ОС и ядро, CPU,
оперативная память, использование дисков и доступное место.
Результат выводится в терминал и дописывается в персональный лог.
Части 2–4 не реализованы. Dockerfile, docker-compose.yml, Jenkinsfile и
build.gradle — пустые файлы, созданные только для требуемой структуры.

## Запуск

В Ubuntu WSL2 выполните:

```bash
cd /home/erasyl/zhumakan-erasyl-student-api
bash -n scripts/Zhumakan_Erasyl_system_report.sh
./scripts/Zhumakan_Erasyl_system_report.sh
```

Скрипт имеет права `744` и запускается напрямую владельцем. Пути вычисляются
от расположения скрипта, поэтому запуск возможен из другой рабочей директории.
Лог: `logs/Zhumakan_Erasyl_system_report.log`.
Используются Bash и стандартные утилиты Ubuntu: date, id, cat, uname, lscpu,
free, df, tee. Установка пакетов и sudo не требуются.

## Проверка Part 1

```bash
ls -l scripts/Zhumakan_Erasyl_system_report.sh
cmp README.md backups/Zhumakan_Erasyl_README.md && echo 'Backup matches README'
ps aux
pgrep -a -x java || test "$?" -eq 1
tail -n 5 logs/Zhumakan_Erasyl_system_report.log
grep -n 'Zhumakan' logs/Zhumakan_Erasyl_system_report.log > Zhumakan_Erasyl_log_analysis.txt
cat Zhumakan_Erasyl_log_analysis.txt
```

`>` создаёт файл или перезаписывает существующий файл. `>>` создаёт файл или
дописывает в его конец, сохраняя прежнее содержимое. Скрипт использует `tee -a`,
чтобы одновременно показывать отчёт и дописывать его в лог.
Результат grep сохраняется через `>`, чтобы анализ соответствовал текущему логу.

Папка `src/` зарезервирована для исходного кода, `backups/` содержит копию README,
`scripts/` — Bash-скрипт, `logs/` — отчёт и протокол проверки Part 1.
