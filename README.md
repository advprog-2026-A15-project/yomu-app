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

`bootRun` default ke profile `local`. Backend akan membuat database H2 terpisah untuk setiap modul di folder `backend/.localdb/`.

### Deploy notes

Untuk deployment backend, aktifkan profile `prod` dan isi environment variable berikut:

```bash
SPRING_PROFILES_ACTIVE=prod
PORT=8080

YOMU_MODULES_ACHIEVEMENTS_DATASOURCE_URL=jdbc:postgresql://<host>:5432/achievements
YOMU_MODULES_ACHIEVEMENTS_DATASOURCE_USERNAME=<username>
YOMU_MODULES_ACHIEVEMENTS_DATASOURCE_PASSWORD=<password>

YOMU_MODULES_AUTH_DATASOURCE_URL=jdbc:postgresql://<host>:5432/auth
YOMU_MODULES_AUTH_DATASOURCE_USERNAME=<username>
YOMU_MODULES_AUTH_DATASOURCE_PASSWORD=<password>

YOMU_MODULES_CLAN_DATASOURCE_URL=jdbc:postgresql://<host>:5432/clan
YOMU_MODULES_CLAN_DATASOURCE_USERNAME=<username>
YOMU_MODULES_CLAN_DATASOURCE_PASSWORD=<password>

YOMU_MODULES_FORUM_DATASOURCE_URL=jdbc:postgresql://<host>:5432/forum
YOMU_MODULES_FORUM_DATASOURCE_USERNAME=<username>
YOMU_MODULES_FORUM_DATASOURCE_PASSWORD=<password>

YOMU_MODULES_LEARNING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/learning
YOMU_MODULES_LEARNING_DATASOURCE_USERNAME=<username>
YOMU_MODULES_LEARNING_DATASOURCE_PASSWORD=<password>
```

Tanpa konfigurasi database production di atas, aplikasi backend akan gagal start. Itu sengaja, supaya tiap modul tetap punya database sendiri dan environment deploy tidak diam-diam jatuh ke satu database bersama.

### Arsitektur backend

Backend sekarang diarahkan sebagai modular monolith:

- satu deployment unit
- modul dipisah di package root: `achievements`, `auth`, `clan`, `forum`, `learning`
- setiap modul punya datasource, transaction manager, dan `JdbcTemplate` sendiri

Persistence modul belum diimplementasikan penuh. Repository dan entity masih placeholder, jadi langkah berikutnya adalah menghubungkan tiap repository modul ke datasource miliknya sendiri.

### CI

Workflow GitHub Actions aktif ada di `.github/workflows/`:

- `frontend-quality.yml`: install, lint, build frontend
- `backend-quality.yml`: run backend test suite
