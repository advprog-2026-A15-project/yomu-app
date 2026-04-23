# Contracts

Dokumen di folder ini mendefinisikan kontrak antar modul pada modular monolith `yomu`.

Aturan umum:

- Event publik ditempatkan di package modul yang tidak memakai `internal`.
- Kontrak dianggap stabil dan versioning dilakukan secara additive.
- Consumer antar modul hanya boleh bergantung pada field yang terdokumentasi di sini.
- Perubahan breaking harus disertai version baru atau strategi migrasi yang jelas.

## Event Contracts

- `auth.UserRegisteredEvent`
- `learning.LearningCompletedEvent`
- `achievements.AchievementUnlockedEvent`
- `forum.CommentCreatedEvent`

## HTTP API Contracts

- `GET /api/forum/comments`

