-- ============================================================
-- PetCare — Doctors, Assignments, Vitals tables + Seed Data
-- Run this in your Supabase SQL Editor
-- ============================================================

-- 1. Doctors / Veterinarians table
CREATE TABLE IF NOT EXISTS doctors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    full_name TEXT NOT NULL,
    specialization TEXT,
    clinic_name TEXT,
    clinic_location TEXT,
    phone TEXT,
    license_number TEXT UNIQUE,
    rating DOUBLE PRECISION DEFAULT 0.0,
    img_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Pet ↔ Doctor assignments
CREATE TABLE IF NOT EXISTS pet_doctor_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    notes TEXT,
    UNIQUE(pet_id, doctor_id)
);

-- 3. Live vitals log (for IoT / wearable data)
CREATE TABLE IF NOT EXISTS pet_vitals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    temperature DOUBLE PRECISION,
    heart_rate INT,
    respiratory_rate INT,
    weight DOUBLE PRECISION,
    notes TEXT,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. Enable Row Level Security
ALTER TABLE doctors ENABLE ROW LEVEL SECURITY;
ALTER TABLE pet_doctor_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE pet_vitals ENABLE ROW LEVEL SECURITY;

-- Policies: doctors can read their own record; any authenticated user can read doctor list
CREATE POLICY "Anyone can read doctors" ON doctors FOR SELECT USING (true);
CREATE POLICY "Doctor can update own record" ON doctors FOR UPDATE USING (auth.uid() = user_id);

-- Assignments: doctor or pet owner can read
CREATE POLICY "Read own assignments" ON pet_doctor_assignments FOR SELECT USING (
    doctor_id IN (SELECT id FROM doctors WHERE user_id = auth.uid())
    OR pet_id IN (SELECT id FROM pets WHERE owner_id = auth.uid())
);
CREATE POLICY "Insert assignments" ON pet_doctor_assignments FOR INSERT WITH CHECK (true);

-- Vitals: doctor or pet owner can read
CREATE POLICY "Read own vitals" ON pet_vitals FOR SELECT USING (
    pet_id IN (SELECT id FROM pets WHERE owner_id = auth.uid())
    OR pet_id IN (
        SELECT pda.pet_id FROM pet_doctor_assignments pda
        JOIN doctors d ON d.id = pda.doctor_id
        WHERE d.user_id = auth.uid()
    )
);
CREATE POLICY "Insert vitals" ON pet_vitals FOR INSERT WITH CHECK (true);


-- ============================================================
-- SEED DATA
-- ============================================================

-- NOTE: These use fixed UUIDs so they can reference each other.
-- The owner_id values below are placeholder UUIDs. Replace them
-- with real auth.users IDs from your Supabase project, or use
-- the demo user ID from the app.

-- Demo owner ID (matches demoUser in User.kt)
-- 123e4567-e89b-12d3-a456-426614174000

-- ---- PETS ----
INSERT INTO pets (id, name, species, breed, gender, weight, age_months, behavioral_notes, microchip_id, owner_id) VALUES
    ('a1b2c3d4-0001-4000-a000-000000000001', 'Buddy',   'Dog', 'Labrador Retriever',   'Male',   28.5, 36, 'Very friendly and energetic. Loves fetching balls and swimming.', 100000001, '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000002', 'Luna',    'Dog', 'German Shepherd',      'Female', 32.0, 24, 'Protective and loyal. Great with children. Needs daily exercise.', 100000002, '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000003', 'Max',     'Dog', 'Golden Retriever',     'Male',   30.0, 48, 'Calm and obedient. Therapy dog certified. Loves belly rubs.',     100000003, '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000004', 'Bella',   'Dog', 'French Bulldog',       'Female', 11.5, 18, 'Playful and stubborn. Snores loudly. Allergic to chicken.',        100000004, '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000005', 'Charlie', 'Dog', 'Beagle',               'Male',   13.0, 30, 'Curious and food-motivated. Excellent nose. Howls at sirens.',     100000005, '123e4567-e89b-12d3-a456-426614174000')
ON CONFLICT (microchip_id) DO NOTHING;

-- ---- DOCTORS ----
INSERT INTO doctors (id, full_name, specialization, clinic_name, clinic_location, phone, license_number, rating) VALUES
    ('d0c10001-0001-4000-a000-000000000001', 'Dr. Sarah Mitchell',  'General Practice',    'PawCare Veterinary Clinic',   'Helsinki',  '+358 40 1234567', 'VET-FI-2024-001', 4.8),
    ('d0c10001-0001-4000-a000-000000000002', 'Dr. James Anderson',  'Orthopedic Surgery',  'Nordic Pet Hospital',         'Espoo',     '+358 40 2345678', 'VET-FI-2024-002', 4.6),
    ('d0c10001-0001-4000-a000-000000000003', 'Dr. Emily Chen',      'Dermatology',         'Happy Tails Animal Center',   'Vantaa',    '+358 40 3456789', 'VET-FI-2024-003', 4.9)
ON CONFLICT (license_number) DO NOTHING;

-- ---- PET-DOCTOR ASSIGNMENTS ----
INSERT INTO pet_doctor_assignments (pet_id, doctor_id, notes) VALUES
    ('a1b2c3d4-0001-4000-a000-000000000001', 'd0c10001-0001-4000-a000-000000000001', 'Primary vet for Buddy'),
    ('a1b2c3d4-0001-4000-a000-000000000002', 'd0c10001-0001-4000-a000-000000000001', 'Primary vet for Luna'),
    ('a1b2c3d4-0001-4000-a000-000000000003', 'd0c10001-0001-4000-a000-000000000002', 'Orthopedic follow-up for Max'),
    ('a1b2c3d4-0001-4000-a000-000000000004', 'd0c10001-0001-4000-a000-000000000003', 'Allergy management for Bella'),
    ('a1b2c3d4-0001-4000-a000-000000000005', 'd0c10001-0001-4000-a000-000000000001', 'Primary vet for Charlie'),
    ('a1b2c3d4-0001-4000-a000-000000000001', 'd0c10001-0001-4000-a000-000000000002', 'Knee evaluation for Buddy')
ON CONFLICT (pet_id, doctor_id) DO NOTHING;

-- ---- HEALTH RECORDS ----
INSERT INTO health_records (pet_id, type, date, details) VALUES
    -- Buddy
    ('a1b2c3d4-0001-4000-a000-000000000001', 'VETERINARIAN_VISIT',  '2026-04-15T10:00:00Z', 'Annual checkup. All vitals normal. Weight stable at 28.5kg.'),
    ('a1b2c3d4-0001-4000-a000-000000000001', 'MEDICATION',          '2026-04-15T10:30:00Z', 'Heartworm prevention — Heartgard Plus administered.'),
    ('a1b2c3d4-0001-4000-a000-000000000001', 'WEIGHT_MEASUREMENT',  '2026-04-15T10:05:00Z', 'Weight: 28.5 kg'),
    -- Luna
    ('a1b2c3d4-0001-4000-a000-000000000002', 'VETERINARIAN_VISIT',  '2026-03-20T14:00:00Z', 'Vaccination booster — DHPP. No adverse reactions.'),
    ('a1b2c3d4-0001-4000-a000-000000000002', 'EXERCISE',            '2026-04-10T08:00:00Z', 'Daily 5km run with owner. Excellent stamina.'),
    -- Max
    ('a1b2c3d4-0001-4000-a000-000000000003', 'OPERATION',           '2026-01-10T09:00:00Z', 'Knee ligament repair surgery — successful. 6-week recovery.'),
    ('a1b2c3d4-0001-4000-a000-000000000003', 'VETERINARIAN_VISIT',  '2026-02-21T11:00:00Z', 'Post-surgery follow-up. Healing well. Begin physiotherapy.'),
    -- Bella
    ('a1b2c3d4-0001-4000-a000-000000000004', 'ALLERGY',             '2026-03-05T16:00:00Z', 'Confirmed chicken allergy via elimination diet. Switched to fish-based food.'),
    ('a1b2c3d4-0001-4000-a000-000000000004', 'SYMPTOM',             '2026-04-01T12:00:00Z', 'Mild skin irritation on belly. Prescribed medicated shampoo.'),
    -- Charlie
    ('a1b2c3d4-0001-4000-a000-000000000005', 'VETERINARIAN_VISIT',  '2026-04-20T09:30:00Z', 'Routine checkup. Slightly overweight — recommend reduced treats.'),
    ('a1b2c3d4-0001-4000-a000-000000000005', 'WEIGHT_MEASUREMENT',  '2026-04-20T09:35:00Z', 'Weight: 13.0 kg (target: 12 kg)');

-- ---- REMINDERS ----
INSERT INTO reminders (pet_id, type, title, description, reminder_time, is_enabled, repeat_pattern, owner_id) VALUES
    ('a1b2c3d4-0001-4000-a000-000000000001', 'Medication',  'Heartworm Pill — Buddy',      'Give Heartgard Plus with food',                   '2026-05-15T08:00:00Z', true,  'Monthly',  '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000001', 'Grooming',    'Bath Day — Buddy',             'Monthly bath and nail trim',                       '2026-05-01T10:00:00Z', true,  'Monthly',  '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000002', 'Exercise',    'Morning Run — Luna',            'Daily 5km run in the park',                       '2026-04-29T07:00:00Z', true,  'Daily',    '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000003', 'Medication',  'Physiotherapy — Max',           'Knee exercises as prescribed by Dr. Anderson',    '2026-04-30T15:00:00Z', true,  'Weekly',   '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000004', 'Feeding',     'Special Diet — Bella',          'Fish-based food only. No chicken treats!',        '2026-04-29T08:00:00Z', true,  'Daily',    '123e4567-e89b-12d3-a456-426614174000'),
    ('a1b2c3d4-0001-4000-a000-000000000005', 'Feeding',     'Reduced Treats — Charlie',      'Max 2 treats per day. Monitor weight weekly.',    '2026-04-29T09:00:00Z', true,  'Daily',    '123e4567-e89b-12d3-a456-426614174000');

-- ---- PET VITALS (sample live data) ----
INSERT INTO pet_vitals (pet_id, temperature, heart_rate, respiratory_rate, weight, recorded_at) VALUES
    ('a1b2c3d4-0001-4000-a000-000000000001', 38.5, 80, 20, 28.5, '2026-04-27T08:00:00Z'),
    ('a1b2c3d4-0001-4000-a000-000000000001', 38.6, 82, 22, 28.5, '2026-04-27T14:00:00Z'),
    ('a1b2c3d4-0001-4000-a000-000000000001', 38.4, 78, 19, 28.5, '2026-04-27T20:00:00Z'),
    ('a1b2c3d4-0001-4000-a000-000000000002', 38.7, 85, 24, 32.0, '2026-04-27T08:00:00Z'),
    ('a1b2c3d4-0001-4000-a000-000000000002', 39.0, 90, 28, 32.0, '2026-04-27T14:00:00Z'),
    ('a1b2c3d4-0001-4000-a000-000000000003', 38.3, 75, 18, 30.0, '2026-04-27T10:00:00Z'),
    ('a1b2c3d4-0001-4000-a000-000000000004', 38.8, 88, 22, 11.5, '2026-04-27T09:00:00Z'),
    ('a1b2c3d4-0001-4000-a000-000000000005', 38.6, 82, 20, 13.0, '2026-04-27T11:00:00Z');
