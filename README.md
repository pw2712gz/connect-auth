---

# Connect Auth – OAuth2 Login Starter

A clean, OAuth2-based authentication starter built with Spring Boot 3 and Thymeleaf. This is the second version of my
full-stack auth boilerplate — now with GitHub & Google login, no frontend framework, and a fully server-rendered UI.

---

## What's Inside

### `connect-auth/` – Spring Boot (Monolith)

* OAuth2 Login via GitHub and Google
* Thymeleaf + static CSS UI (`style.css`)
* Secure session-based authentication (no JWT)
* Clean login, dashboard, and logout flow
* Minimal Dockerfile for App Runner or Railway deployment
* MySQL-compatible, RDS & Railway tested
* `.env`-driven config with secrets and DB settings

---

## Live Demo

[connect.ayubyusuf.dev](https://connect.ayubyusuf.dev)

---

## Local Development

```bash
# Run with Maven
./mvnw spring-boot:run

# OR run with Docker
docker build -t connect-auth .
docker run -p 8080:8080 --env-file .env connect-auth
```

---

## Screenshots

<img width="2474" height="1414" alt="home" src="https://github.com/user-attachments/assets/60a7e8a1-5b61-457b-881c-f89b8675d00e" />

<img width="2474" height="1414" alt="dashboard" src="https://github.com/user-attachments/assets/6c969ecf-f8f9-4ded-b5b5-ff32edfa5a75" />

---

## Related Projects

* **Full Stack JWT Auth Starter**
  [github.com/pw2712gz/auth-starter](https://github.com/pw2712gz/auth-starter)
  (Spring Boot + Angular with JWT and refresh token auth)

---

## Purpose

This project is a simple, production-ready OAuth2 starter — ideal for apps that need secure social login without the
complexity of a frontend SPA. Built with clean structure, minimal dependencies, and real-world use in mind.

---

## License

MIT © Ayub Yusuf

---
