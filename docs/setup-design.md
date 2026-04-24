# Setup & Design Baseline

Dokumen ini mencatat baseline setup proyek agar tim bisa mengisi modul secara paralel dengan aturan yang konsisten.

## Repository baseline

- Default branch aktif: `main`
- Integration branch aktif: `staging`
- Workflow GitHub Actions ada di `.github/workflows/`
- Frontend dan backend sudah dipisah menjadi aplikasi terpisah

## Branching strategy

- `main`: branch produksi, hanya menerima merge dari pull request yang sudah lolos review dan CI.
- `staging`: branch integrasi, dipakai untuk menggabungkan pekerjaan lintas modul sebelum dinaikkan ke `main`.
- `feat/<module-or-scope>`: branch kerja tim per fitur atau per modul.

## Recommended branch protection

Aturan berikut perlu diaktifkan di GitHub repository settings:

- Protect branch `main`
- Protect branch `staging`
- Require pull request before merging
- Require status checks to pass before merging
- Required checks:
  - `Frontend Quality / quality`
  - `Backend Quality / quality`
- Restrict direct pushes

## Backend design

- Backend memakai modular packages:
  - `achievements`
  - `auth`
  - `clan`
  - `forum`
  - `learning`
- Kontrak event publik antar modul ada di `docs/contracts/`
- Setiap modul diarahkan memiliki datasource sendiri melalui `ModuleDatabaseConfiguration`, namun semuanya mengarah ke satu database shared dengan schema masing-masing modul

## Local/dev security profile

- Konfigurasi keamanan lokal berada di `backend/src/main/java/id/ac/ui/cs/advprog/yomu/config/LocalDevelopmentSecurityConfig.java`
- Konfigurasi ini hanya aktif untuk profile `local` dan `dev` (via `@Profile({"local", "dev"})`)
- Tujuan utamanya mempermudah pengujian lokal (misalnya Postman) lintas modul tanpa hambatan security default
- Selama profile `local/dev` aktif, CSRF protection dan authentication requirement dinonaktifkan untuk mempercepat iterasi development

## Usage guidance

- Development lokal default: aktifkan `spring.profiles.active=local`
- Untuk pengujian manual endpoint `POST/PUT/DELETE`, profile `local` atau `dev` direkomendasikan agar tidak perlu CSRF token dan auth header sementara
- Untuk production/staging, profile harus menggunakan `prod` atau profile non-local agar konfigurasi security sementara tidak aktif

## Production safety

- Konfigurasi `LocalDevelopmentSecurityConfig` tidak boleh digunakan di production
- Pastikan konfigurasi security production (auth/authorization/CSRF) dipisahkan dan hanya aktif di profile production

## Optional cleanup

- File lama `backend/src/main/java/id/ac/ui/cs/advprog/yomu/forum/internal/configuration/TemporarySecurityConfig.java` dapat dihapus jika seluruh tim sudah migrasi ke konfigurasi global
- Dokumentasi detail perilaku lokal tetap dirujuk dari `docs/LOCAL_SECURITY_CONFIG.md`

## Frontend design

- Frontend memakai React + Vite
- Basic quality gate:
  - lint
  - test
  - build
