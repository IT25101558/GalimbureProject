# GalimbureProject

Spring Boot authentication app with registration, login, and a protected dashboard backed by PostgreSQL.

## What Was Added

- `GET /login` shows the custom login page for existing accounts.
- `POST /login` is handled by Spring Security and returns the user to `/dashboard`.
- `GET /register` shows the registration form.
- `POST /register` validates the selected batch, saves the account, and signs them in automatically.
- `GET /dashboard` shows the protected dashboard after login or registration.
- `GET /admin-dashboard` shows the admin dashboard for users with `ADMIN` role.
- `POST /admin-dashboard/batches` creates a batch row and auto-creates two years under it.
- `POST /admin-dashboard/years` creates a year row and auto-creates 12 months with five weeks each.
- `GET /admin-dashboard/paid-status` opens the student payment status page for a selected batch, year, and month.
- `POST /admin-dashboard/paid-status` saves payment status changes for the selected month.
- `GET /admin-dashboard/marks` shows the batch, year, month, and week marks page.
- `POST /admin-dashboard/users/{id}/role` updates a user's role from the admin dashboard.
- `POST /admin-dashboard/marks` stores or updates weekly marks for the selected week.
- Data is saved into the `registered_users` PostgreSQL table.
- Batches are saved into the `batch_table` PostgreSQL table.
- Years are saved into the `yearplan_table` PostgreSQL table.
- Months are saved into the `monthplan_table` PostgreSQL table.
- Week plans are saved into the `weekplan_table` PostgreSQL table.
- Weekly marks are saved into the `student_marks` PostgreSQL table.
- Passwords are stored as BCrypt hashes.
- The app reads the database connection from environment variables.
- New registrations are stored with the `STUDENT` role by default. To use the admin dashboard, at least one database user must have the `ADMIN` role.
- New student registrations include a batch selection so the marks page can filter the correct cohort.

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
batch_year
batch_id
role
created_at
```

Roles:

```text
STUDENT
ADMIN
```

## Batch Table

The batch table is created automatically by Hibernate.

Table name:

```text
batch_table
```

Columns:

```text
id
batch_year
place
batch_date
```

Rules:

- Batch year and place together are unique.
- The admin creates batches from the admin dashboard before the year, month, and week rows are generated.

## Year Plan Table

The year plan table is created automatically by Hibernate.

Table name:

```text
yearplan_table
```

Columns:

```text
id
batch_id
year_value
created_at
updated_at
```

Rules:

- Each batch gets two years by default when it is created.
- Year values are unique within the same batch.

## Month Plan Table

The month plan table is created automatically by Hibernate.

Table name:

```text
monthplan_table
```

Columns:

```text
id
year_plan_id
month_number
paid_status
created_at
updated_at
```

Rules:

- Each year gets 12 months automatically.
- `paid_status` is stored on the month row.
- Month numbers are unique within the same year.

## Student Marks Table

The weekly marks table is created automatically by Hibernate.

Table name:

```text
student_marks
```

Columns:

```text
id
student_id
week_plan_id
mark
created_at
updated_at
```

Rules:

- One student can have only one mark per week plan.
- Admins select a batch, year, month, and week, then create or update only the students assigned to that batch from `marks.html`.
- The student dashboard still shows weekly marks as a line chart.

## Week Plan Table

The week plan table is created automatically by Hibernate.

Table name:

```text
weekplan_table
```

Columns:

```text
id
month_plan_id
week_number
task
week_start_date
week_end_date
created_at
updated_at
```

Rules:

- Week numbers are unique per month, not globally.
- Each month gets 5 weeks automatically.
- The marks editor only shows the week plans created for the selected month.
