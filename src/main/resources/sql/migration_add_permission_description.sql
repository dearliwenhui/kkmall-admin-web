-- Migration: Add description column to sys_permission table
-- Date: 2026-03-10
-- Description: Fix SQLSyntaxErrorException - Unknown column 'description' in 'field list'

-- Add description column (if column already exists, this will fail - that's OK)
ALTER TABLE `sys_permission` ADD COLUMN `description` VARCHAR(200) DEFAULT NULL AFTER `path`;