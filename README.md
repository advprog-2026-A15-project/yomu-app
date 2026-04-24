## Yomu

Monorepo ini berisi:

- `frontend/`: React + Vite
- `backend/`: Spring Boot + Gradle

### Local run

Frontend:

```bash
cd frontend
npm ci
npm run dev
```

Backend:

```bash
cd backend
./gradlew bootRun
```

`bootRun` default ke profile `local`. Backend akan membuat satu database H2 di folder `backend/.localdb/`, lalu memakai schema terpisah untuk modul `achievements`, `auth`, `clan`, `forum`, dan `learning`.

### Deploy notes

Untuk deployment backend, aktifkan profile `prod` dan isi environment variable berikut:

```bash
SPRING_PROFILES_ACTIVE=prod
PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/yomu
SPRING_DATASOURCE_USERNAME=<username>
SPRING_DATASOURCE_PASSWORD=<password>
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
```

Database production yang dipakai bersifat shared, tetapi harus memiliki schema berikut terlebih dahulu:

- `achievements`
- `auth`
- `clan`
- `forum`
- `learning`

### Arsitektur backend

Backend sekarang diarahkan sebagai modular monolith:

- satu deployment unit
- modul dipisah di package root: `achievements`, `auth`, `clan`, `forum`, `learning`
- satu database shared dipakai bersama, tetapi setiap modul memiliki schema, datasource, transaction manager, dan `JdbcTemplate` sendiri

Persistence modul belum diimplementasikan penuh. Repository dan entity masih placeholder, jadi langkah berikutnya adalah menghubungkan tiap repository modul ke datasource miliknya sendiri.

### CI

Workflow GitHub Actions aktif ada di `.github/workflows/`:

- `frontend-quality.yml`: install, lint, test, build frontend
- `backend-quality.yml`: build backend

### Docs

- `docs/setup-design.md`: baseline setup repository, branching, dan design awal
- `docs/contracts/`: kontrak event publik antar modul
