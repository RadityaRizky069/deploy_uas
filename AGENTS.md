# AGENTS.md

## Project Facts
- Single-module Spring Boot 3.3.5 app on Java 17; use `./mvnw` for Maven commands.
- Main class is `src/main/java/com/example/TrashformerApplication.java`; it sits in `com.example` so Spring scans `com.example.trashformer`.
- MVC is server-rendered Thymeleaf: controllers live in `src/main/java/com/example/trashformer/controller`, templates in `src/main/resources/templates`, static assets in `src/main/resources/static`.

## Commands
- Run locally: `./mvnw spring-boot:run`.
- Verify build: `./mvnw clean package`.
- Run tests if tests are added: `./mvnw test`; there is currently no `src/test` tree and `pom.xml` has no test starter.
- No CI, lint, formatter, or codegen config exists in this repo; do not invent extra repo-specific checks.

## Runtime
- `src/main/resources/application.properties` expects MySQL/MariaDB at `jdbc:mysql://localhost:3306/trashformer_db` with user `root` and blank password; Hibernate uses `spring.jpa.hibernate.ddl-auto=update`.
- No Flyway/Liquibase is configured; `trashformer_db.sql`, `migrasi_setoran_unified.sql`, and `fix_user_lawas.sql` are manual SQL scripts.
- `migrasi_setoran_unified.sql` renames/drops old setoran tables; do not run it casually.
- Uploads go to `uploads/bukti_pembayaran` and are served from `/files/bukti_pembayaran/**`; avoid deleting local files under `uploads/`.

## Auth And Data
- Passwords are BCrypt encoded by `SecurityConfig.passwordEncoder()` and `UserService`; never store plaintext in `users.password`.
- Roles are enum values `ADMIN`, `PETUGAS`, `WARGA`; route guards map them to `/admin/**`, `/petugas/**`, and `/warga/**`.
- `CustomUserDetailsService` only blocks users when `is_active` is non-null false; NULL legacy values still authenticate.
- `Setoran` is the unified entity/table for both trash and money deposits, split by `jenis_setoran` (`SAMPAH` or `UANG`).
- `Setoran.onCreate()` defaults status fields only for `SAMPAH`; `UANG` deposits intentionally keep those status fields null.
