-- V2__seed_roles.sql
-- Seed default roles

INSERT INTO roles (name)
VALUES ('USER'), ('DRIVER'), ('ADMIN')
ON CONFLICT (name) DO NOTHING;
