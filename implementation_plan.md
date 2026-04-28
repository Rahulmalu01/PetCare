# PetCare App — Live Data API, Doctor Login, Default Data & Improvements

## Background

The PetCare app is a Kotlin/Jetpack Compose Android app using Supabase (Postgrest + Auth). Currently it supports pet owners managing pets, health records, and reminders. The user wants:

1. **A live data API endpoint + JSON format** to feed real-time dog data into the system
2. **Doctor/Vet login** with the ability to view assigned patients (pets)
3. **Default seed data** in the database
4. **General app improvements** — functional polish and missing feature wiring

---

## Proposed Changes

### 1. Supabase Edge Function — Live Data Ingestion API

Since the app uses Supabase as its backend, we'll create a **Supabase Edge Function** (Deno/TypeScript) that exposes a REST endpoint for IoT devices, wearables, or external systems to push live dog data.

We'll also provide the **exact JSON format** and **cURL examples** as documentation.

#### [NEW] `supabase/functions/ingest-pet-data/index.ts`

A Supabase Edge Function that:
- Accepts `POST` requests with an API key in the header
- Validates the JSON payload
- Upserts pet data + inserts a health record into the database
- Returns success/error responses

**API Endpoint:**
```
POST https://<YOUR_SUPABASE_URL>/functions/v1/ingest-pet-data
Headers:
  Authorization: Bearer <SUPABASE_ANON_KEY>
  Content-Type: application/json
  x-api-key: <DEVICE_API_KEY>
```

**JSON Format for Live Dog Data:**
```json
{
  "microchip_id": 123456789,
  "name": "Buddy",
  "species": "Dog",
  "breed": "Labrador Retriever",
  "gender": "Male",
  "weight": 28.5,
  "age_months": 36,
  "behavioral_notes": "Active and playful, slight limping on right front leg",
  "owner_id": "uuid-of-owner",
  "health_data": {
    "type": "VETERINARIAN_VISIT",
    "date": "2026-04-28T10:30:00Z",
    "details": "Routine checkup. Weight stable. Vaccination updated.",
    "temperature": 38.5,
    "heart_rate": 80,
    "respiratory_rate": 20
  }
}
```

**Minimal payload (just vitals update):**
```json
{
  "microchip_id": 123456789,
  "health_data": {
    "type": "WEIGHT_MEASUREMENT",
    "date": "2026-04-28T14:00:00Z",
    "details": "Weight: 28.5kg, Temp: 38.5°C, HR: 80bpm"
  }
}
```

#### [NEW] `supabase/functions/ingest-pet-data/README.md`

Documentation with full API reference, JSON schema, cURL examples, and error codes.

---

### 2. Supabase Schema Updates — Doctors Table & Assignments

#### [NEW] SQL migration: `supabase/migrations/001_doctors_and_seed.sql`

New tables and seed data:

```sql
-- Doctors/Veterinarians table
CREATE TABLE doctors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id),
    full_name TEXT NOT NULL,
    specialization TEXT,
    clinic_name TEXT,
    clinic_location TEXT,
    phone TEXT,
    license_number TEXT UNIQUE,
    rating DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Pet-Doctor assignments (which doctor treats which pet)
CREATE TABLE pet_doctor_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    notes TEXT,
    UNIQUE(pet_id, doctor_id)
);

-- Live vitals log (for IoT/wearable data)
CREATE TABLE pet_vitals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    temperature DOUBLE PRECISION,
    heart_rate INT,
    respiratory_rate INT,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

**Default seed data** includes:
- 3 demo pet owners (users)
- 5 demo pets (dogs of various breeds)
- 3 demo doctors
- Health records for each pet
- Reminders for each pet
- Pet-doctor assignments

---

### 3. Android App — Doctor Data Model & Repository

#### [NEW] `app/src/main/java/fi/project/petcare/model/data/Doctor.kt`
- `Doctor` data class with Supabase serialization
- `PetDoctorAssignment` data class
- `PetVitals` data class for live vitals

#### [NEW] `app/src/main/java/fi/project/petcare/model/repository/DoctorRepository.kt`
- `getDoctorByUserId()` — check if logged-in user is a doctor
- `getAssignedPets()` — get all pets assigned to this doctor
- `getPetVitals()` — get live vitals for a pet

---

### 4. Android App — Doctor ViewModel

#### [NEW] `app/src/main/java/fi/project/petcare/viewmodel/DoctorViewModel.kt`
- `DoctorUiState` sealed interface (Loading, DoctorAuthenticated, NotADoctor, Error)
- Checks if the authenticated user has a doctor profile
- Loads assigned patients (pets) for the doctor view
- Loads vitals history

---

### 5. Android App — Doctor UI Screens

#### [NEW] `app/src/main/java/fi/project/petcare/ui/screens/DoctorDashboardScreen.kt`
- A doctor-specific dashboard showing:
  - Welcome card with doctor info & specialization
  - List of assigned patients (pets) with owner info
  - Quick stats (total patients, upcoming appointments)

#### [NEW] `app/src/main/java/fi/project/petcare/ui/screens/DoctorPatientDetailScreen.kt`
- Detailed view of a patient (pet) with:
  - Pet info card (name, breed, weight, age)
  - Health records timeline
  - Live vitals chart (temperature, heart rate, respiratory rate)
  - Ability to add health records

---

### 6. Navigation Updates

#### [MODIFY] [PetCareDestinations.kt](file:///d:/Industry_Projects/PetCare/app/src/main/java/fi/project/petcare/ui/nav/PetCareDestinations.kt)
- Add `Screen.DoctorDashboard` and `Screen.DoctorPatientDetail` routes

#### [MODIFY] [AppNavigation.kt](file:///d:/Industry_Projects/PetCare/app/src/main/java/fi/project/petcare/ui/nav/AppNavigation.kt)
- Add composable routes for doctor screens
- Pass DoctorViewModel to doctor screens

#### [MODIFY] [PetCareApp.kt](file:///d:/Industry_Projects/PetCare/app/src/main/java/fi/project/petcare/PetCareApp.kt)
- After authentication, check if user is a doctor → route to DoctorDashboard
- If regular user → route to existing owner dashboard (current behavior)

---

### 7. App Improvements & Functional Polish

#### [MODIFY] [AuthViewModel.kt](file:///d:/Industry_Projects/PetCare/app/src/main/java/fi/project/petcare/viewmodel/AuthViewModel.kt)
- Handle the `TODO()` cases for `SessionSource.AnonymousSignIn`, `External`, `Unknown`, `UserChanged`, `UserIdentitiesChanged` — currently these will crash the app
- Add a demo doctor sign-in option

#### [MODIFY] [HomeScreen.kt](file:///d:/Industry_Projects/PetCare/app/src/main/java/fi/project/petcare/ui/screens/HomeScreen.kt)
- Wire up "See all" buttons for veterinarians and grooming salons
- Use actual doctor data from the database instead of hardcoded lists

#### [MODIFY] [AppNavigation.kt](file:///d:/Industry_Projects/PetCare/app/src/main/java/fi/project/petcare/ui/nav/AppNavigation.kt) (Community tab)
- Replace the placeholder "coming soon" text with a functional reminders list UI

#### [MODIFY] [RUN_DEPLOY.md](file:///d:/Industry_Projects/PetCare/RUN_DEPLOY.md)
- Add the new SQL tables (doctors, pet_doctor_assignments, pet_vitals)
- Add seed data SQL
- Add API documentation for the live data endpoint

---

## Open Questions

> [!IMPORTANT]
> **Supabase Edge Function deployment**: Do you want me to create the Edge Function files locally for you to deploy manually, or do you have the Supabase CLI set up? I'll create the files either way, but wanted to confirm your deployment preference.

> [!IMPORTANT]  
> **Doctor authentication**: Should doctors use the same sign-in flow (email/password) and we differentiate them by checking the `doctors` table after login? Or do you want a separate "Doctor Login" button on the welcome screen? I'm planning the former (single login, role check after auth) as it's cleaner.

> [!IMPORTANT]
> **Live data source**: What system will feed live data? (IoT collar, vet clinic software, manual input?) This doesn't change the API design, but helps me tailor the documentation and JSON fields. I'll include fields for temperature, heart rate, and respiratory rate as common pet vitals.

---

## Verification Plan

### Automated
- Verify all Kotlin files compile without errors via `./gradlew assembleDebug`
- Verify SQL migrations are syntactically correct

### Manual Verification
- Test the API endpoint JSON format with cURL examples (provided in docs)
- Test doctor login flow: sign in → detect doctor role → show doctor dashboard
- Test owner login flow: sign in → show pet owner dashboard (existing)
- Verify default seed data appears correctly in both views
- Test adding health records from the doctor patient detail screen
