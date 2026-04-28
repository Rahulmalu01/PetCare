# ingest-pet-data — Supabase Edge Function

Accepts live pet data from IoT devices, wearables, or external clinic software and writes it to the PetCare Supabase database.

---

## Deploy

```bash
# 1. Install Supabase CLI
npm install -g supabase

# 2. Link to your project
supabase link --project-ref <YOUR_PROJECT_REF>

# 3. Set the device API key secret
supabase secrets set DEVICE_API_KEY=your-secret-device-key

# 4. Deploy the function
supabase functions deploy ingest-pet-data
```

---

## Endpoint

```
POST https://<YOUR_PROJECT_REF>.supabase.co/functions/v1/ingest-pet-data
```

### Required Headers

| Header          | Value                              |
|-----------------|------------------------------------|
| `Authorization` | `Bearer <SUPABASE_ANON_KEY>`       |
| `Content-Type`  | `application/json`                 |
| `x-api-key`     | `<DEVICE_API_KEY>` (set as secret) |

---

## JSON Payload Schema

### Full payload (pet registration + health record + vitals)

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
  "owner_id": "uuid-of-owner-in-auth.users",
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

### Minimal payload (vitals update only — pet must already exist)

```json
{
  "microchip_id": 123456789,
  "health_data": {
    "type": "WEIGHT_MEASUREMENT",
    "date": "2026-04-28T14:00:00Z",
    "details": "Weight: 28.5kg, Temp: 38.5°C, HR: 80bpm",
    "temperature": 38.5,
    "heart_rate": 80,
    "respiratory_rate": 20
  }
}
```

### Field reference

| Field                       | Type    | Required | Description                                        |
|-----------------------------|---------|----------|----------------------------------------------------|
| `microchip_id`              | integer | ✅ Yes    | Unique pet identifier                              |
| `name`                      | string  | No       | Pet name (required on first registration)          |
| `species`                   | string  | No       | e.g., `"Dog"`, `"Cat"`                             |
| `breed`                     | string  | No       | Breed name                                         |
| `gender`                    | string  | No       | `"Male"` or `"Female"`                             |
| `weight`                    | number  | No       | Weight in kg                                       |
| `age_months`                | integer | No       | Age in months                                      |
| `behavioral_notes`          | string  | No       | Free text behavioral observations                  |
| `owner_id`                  | string  | No       | UUID of the owner in `auth.users`                  |
| `health_data.type`          | string  | ✅ (if health_data) | One of the `HealthRecordType` enum values |
| `health_data.date`          | string  | ✅ (if health_data) | ISO 8601 timestamp                        |
| `health_data.details`       | string  | ✅ (if health_data) | Free text description                     |
| `health_data.temperature`   | number  | No       | Body temperature in °C                             |
| `health_data.heart_rate`    | integer | No       | Heart rate in BPM                                  |
| `health_data.respiratory_rate` | integer | No    | Respiratory rate per minute                        |

### HealthRecordType values

```
OPERATION | VETERINARIAN_VISIT | MEDICATION | SYMPTOM | ALLERGY | EXERCISE | WEIGHT_MEASUREMENT
```

---

## cURL Examples

### Register a new pet + record a vet visit

```bash
curl -X POST \
  https://<PROJECT_REF>.supabase.co/functions/v1/ingest-pet-data \
  -H "Authorization: Bearer <SUPABASE_ANON_KEY>" \
  -H "Content-Type: application/json" \
  -H "x-api-key: your-secret-device-key" \
  -d '{
    "microchip_id": 100000001,
    "name": "Buddy",
    "species": "Dog",
    "breed": "Labrador Retriever",
    "gender": "Male",
    "weight": 28.5,
    "age_months": 36,
    "owner_id": "123e4567-e89b-12d3-a456-426614174000",
    "health_data": {
      "type": "VETERINARIAN_VISIT",
      "date": "2026-04-28T10:30:00Z",
      "details": "Annual checkup. All vitals normal.",
      "temperature": 38.5,
      "heart_rate": 80,
      "respiratory_rate": 20
    }
  }'
```

### Push a vitals-only update (IoT wearable)

```bash
curl -X POST \
  https://<PROJECT_REF>.supabase.co/functions/v1/ingest-pet-data \
  -H "Authorization: Bearer <SUPABASE_ANON_KEY>" \
  -H "Content-Type: application/json" \
  -H "x-api-key: your-secret-device-key" \
  -d '{
    "microchip_id": 100000001,
    "health_data": {
      "type": "WEIGHT_MEASUREMENT",
      "date": "2026-04-28T14:00:00Z",
      "details": "Auto reading from smart collar",
      "temperature": 38.6,
      "heart_rate": 82,
      "respiratory_rate": 22
    }
  }'
```

---

## Response

### Success `200`
```json
{
  "success": true,
  "microchip_id": 100000001,
  "pet_id": "a1b2c3d4-0001-4000-a000-000000000001",
  "actions": {
    "pet": "upserted",
    "health_record": "inserted",
    "vitals": "inserted"
  }
}
```

### Error codes

| Status | Meaning                                             |
|--------|-----------------------------------------------------|
| 401    | Missing or invalid `x-api-key`                      |
| 404    | No pet found with that `microchip_id`               |
| 422    | Missing required fields (`microchip_id` / health_data fields) |
| 500    | Database write error                                |
