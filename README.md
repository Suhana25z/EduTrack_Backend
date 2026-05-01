# EduTrack Backend

Spring Boot backend for the EduTrack React frontend.

## Included

- Spring Boot starter project with Maven
- MySQL + Spring Data JPA
- JWT authentication with teacher/student roles
- Swagger/OpenAPI at `http://localhost:8080/swagger-ui.html`
- DTOs for requests and responses
- ModelMapper configuration
- Global exception handling
- Seed demo accounts

## Demo credentials

- Teacher login: `Ramesh12345` / `teacher123`
- Student login: `STU001` / `student123`

## Run

1. Start MySQL.
2. Update `src/main/resources/application.properties` if your MySQL username/password differ from `root/root`.
3. Run `mvn spring-boot:run`

## Main endpoints

- `POST /api/auth/register/teacher`
- `POST /api/auth/login`
- `GET /api/users`
- `GET /api/users/me`
- `POST /api/teacher/students`
- `GET /api/teacher/students`
- `PUT /api/teacher/students/{studentId}/marks`
- `DELETE /api/teacher/students/{studentId}/marks`
- `DELETE /api/teacher/students/{studentId}`
- `GET /api/teacher/analytics`
- `GET /api/teacher/reports`
- `POST /api/teacher/attendance`
- `GET /api/student/dashboard`
- `GET /api/student/recommendations`
- `GET /api/student/attendance`
