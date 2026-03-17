# PetCare Run & Deployment Guide

This guide explains how to set up, run, and deploy the PetCare Android application.

## Prerequisites

1.  **Android Studio**: Latest version installed.
2.  **Supabase Project**: You need a Supabase project for the backend.
3.  **Google Cloud Project**: Required for Google Sign-In integration.

## Supabase Setup

1.  Create a Supabase project at [supabase.com](https://supabase.com).
2.  Run the following SQL in your Supabase SQL Editor to create the necessary tables:

```sql
-- Create pets table
CREATE TABLE pets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    species TEXT NOT NULL,
    breed TEXT NOT NULL,
    gender TEXT NOT NULL,
    weight DOUBLE PRECISION,
    age_months INT,
    behavioral_notes TEXT,
    img_url TEXT,
    microchip_id INT UNIQUE,
    owner_id UUID REFERENCES auth.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create health_records table
CREATE TABLE health_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    date TIMESTAMP WITH TIME ZONE NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create reminders table
CREATE TABLE reminders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    reminder_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    repeat_pattern TEXT,
    owner_id UUID REFERENCES auth.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

3.  Enable Google Auth in Supabase (Authentication -> Providers -> Google).

## Local Configuration

1.  Locate `local.properties.template` in the project root.
2.  Rename it to `local.properties`.
3.  Fill in your Supabase URL, Anon Key, and Google Server Client ID:

```properties
api.key=YOUR_SUPABASE_ANON_KEY
api.url=YOUR_SUPABASE_PROJECT_URL
server.client.id=YOUR_GOOGLE_SERVER_CLIENT_ID
```

## Running the App

1.  Open the project in **Android Studio**.
2.  Wait for Gradle to sync.
3.  Select an emulator or connect a physical device.
4.  Click the **Run** button (green play icon).

## Deployment

To create a release APK:

1.  Go to **Build -> Generate Signed Bundle / APK**.
2.  Select **APK**.
3.  Follow the wizard to create a new keystore and sign the app.
4.  The generated APK will be in `app/release/`.
