# GalimbureProject

Spring Boot authentication app with registration, login, and a protected dashboard backed by PostgreSQL.

## What Was Added

- `GET /login` shows the custom login page for existing accounts.
- `POST /login` is handled by Spring Security and returns the user to `/dashboard`.
- `GET /register` shows the registration form.
- `POST /register` validates, saves, and signs the new user in automatically.
- `GET /dashboard` shows the protected dashboard after login or registration.
- Data is saved into the `registered_users` PostgreSQL table.
- Passwords are stored as BCrypt hashes.
- The app reads the database connection from environment variables.

## Render Setup

In Render, set this environment variable for the web service:

```text
DATABASE_URL=postgres://username:password@hostname:5432/database_name
```

Use the value from your Render PostgreSQL database connection settings. Do not put the real database password in `application.properties`.

Recommended Render commands:

```bash
bash ./mvnw clean package -DskipTests
java -jar target/GalimbureProject-0.0.1-SNAPSHOT.jar
```

Set this environment variable on the Render Web Service:

```text
DATABASE_URL=your Render PostgreSQL Internal Database URL
```

After deployment, open:

```text
https://your-render-app-url/login
```

## Local Run On Windows

Create a file named `.env` in the project root:

```text
DATABASE_URL=postgres://username:password@hostname:5432/database_name
```

Use the External Database URL from your Render PostgreSQL database when running on your computer.

If Java is not on your `PATH`, set `JAVA_HOME` in the current terminal:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
```

Run the app:

```powershell
.\mvnw.cmd "spring-boot:run"
```

If Maven says `No plugin found for prefix 'spring-boot'`, run the full plugin command:

```powershell
.\mvnw.cmd "org.springframework.boot:spring-boot-maven-plugin:4.1.0:run"
```

Then open:

```text
http://localhost:8080/login
```

## Table

The table is created automatically by Hibernate because `spring.jpa.hibernate.ddl-auto=update` is enabled.

Table name:

```text
registered_users
```

Columns:

```text
id
full_name
email
phone
address
password_hash
created_at
```
