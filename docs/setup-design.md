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
- Setiap modul diarahkan memiliki datasource sendiri melalui `ModuleDatabaseConfiguration`

## Frontend design

- Frontend memakai React + Vite
- Basic quality gate:
  - lint
  - test
  - build
