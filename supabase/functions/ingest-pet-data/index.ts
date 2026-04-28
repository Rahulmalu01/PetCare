// Supabase Edge Function — ingest-pet-data
// Accepts live dog/pet data from IoT devices, wearables, or external systems
// POST https://<PROJECT>.supabase.co/functions/v1/ingest-pet-data

import { createClient } from "jsr:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type, x-api-key",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

// Allowed device API keys — set these as Supabase secrets
// supabase secrets set DEVICE_API_KEY=your-secret-key
const ALLOWED_API_KEYS = new Set([
  Deno.env.get("DEVICE_API_KEY") ?? "dev-key-change-in-production",
]);

interface HealthDataPayload {
  type: string;
  date: string;
  details: string;
  temperature?: number;
  heart_rate?: number;
  respiratory_rate?: number;
}

interface IngestPayload {
  microchip_id: number;
  name?: string;
  species?: string;
  breed?: string;
  gender?: string;
  weight?: number;
  age_months?: number;
  behavioral_notes?: string;
  owner_id?: string;
  health_data?: HealthDataPayload;
}

Deno.serve(async (req: Request) => {
  // Handle CORS preflight
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return new Response(
      JSON.stringify({ error: "Method not allowed. Use POST." }),
      { status: 405, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }

  // ── API key validation ──────────────────────────────────────────────────────
  const apiKey = req.headers.get("x-api-key");
  if (!apiKey || !ALLOWED_API_KEYS.has(apiKey)) {
    return new Response(
      JSON.stringify({ error: "Unauthorized. Invalid or missing x-api-key header." }),
      { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }

  // ── Parse body ──────────────────────────────────────────────────────────────
  let payload: IngestPayload;
  try {
    payload = await req.json();
  } catch {
    return new Response(
      JSON.stringify({ error: "Invalid JSON body." }),
      { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }

  if (!payload.microchip_id) {
    return new Response(
      JSON.stringify({ error: "Required field 'microchip_id' is missing." }),
      { status: 422, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }

  // ── Supabase client (service role for server-side writes) ───────────────────
  const supabase = createClient(
    Deno.env.get("SUPABASE_URL") ?? "",
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "",
    { auth: { persistSession: false } }
  );

  const results: Record<string, unknown> = {};

  // ── Step 1: Find existing pet by microchip_id ───────────────────────────────
  const { data: existingPet } = await supabase
    .from("pets")
    .select("id")
    .eq("microchip_id", payload.microchip_id)
    .maybeSingle();

  // ── Step 2: Upsert pet record (only if full data supplied) ──────────────────
  if (payload.name || payload.species) {
    const petData: Record<string, unknown> = {
      microchip_id: payload.microchip_id,
    };
    if (payload.name) petData.name = payload.name;
    if (payload.species) petData.species = payload.species;
    if (payload.breed) petData.breed = payload.breed;
    if (payload.gender) petData.gender = payload.gender;
    if (payload.weight) petData.weight = payload.weight;
    if (payload.age_months) petData.age_months = payload.age_months;
    if (payload.behavioral_notes) petData.behavioral_notes = payload.behavioral_notes;
    if (payload.owner_id) petData.owner_id = payload.owner_id;

    const { error: upsertError } = await supabase
      .from("pets")
      .upsert(petData, { onConflict: "microchip_id" });

    if (upsertError) {
      return new Response(
        JSON.stringify({ error: "Failed to upsert pet.", detail: upsertError.message }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }
    results.pet = "upserted";
  }

  // ── Step 3: Get the pet_id after upsert ────────────────────────────────────
  const { data: pet, error: petFetchError } = await supabase
    .from("pets")
    .select("id")
    .eq("microchip_id", payload.microchip_id)
    .maybeSingle();

  if (petFetchError || !pet) {
    return new Response(
      JSON.stringify({
        error: "Could not find pet with this microchip_id. Register the pet first.",
        microchip_id: payload.microchip_id,
      }),
      { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }

  const petId = pet.id;

  // ── Step 4: Insert health record ────────────────────────────────────────────
  if (payload.health_data) {
    const hd = payload.health_data;
    if (!hd.type || !hd.date || !hd.details) {
      return new Response(
        JSON.stringify({ error: "health_data must include 'type', 'date', and 'details'." }),
        { status: 422, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const { error: hrError } = await supabase.from("health_records").insert({
      pet_id: petId,
      type: hd.type,
      date: hd.date,
      details: hd.details,
    });

    if (hrError) {
      console.error("health_records insert error:", hrError.message);
    } else {
      results.health_record = "inserted";
    }
  }

  // ── Step 5: Insert vitals (if provided in health_data) ─────────────────────
  const hd = payload.health_data;
  if (hd && (hd.temperature || hd.heart_rate || hd.respiratory_rate)) {
    const vitalsData: Record<string, unknown> = { pet_id: petId };
    if (hd.temperature) vitalsData.temperature = hd.temperature;
    if (hd.heart_rate) vitalsData.heart_rate = hd.heart_rate;
    if (hd.respiratory_rate) vitalsData.respiratory_rate = hd.respiratory_rate;
    if (payload.weight) vitalsData.weight = payload.weight;
    vitalsData.recorded_at = hd.date ?? new Date().toISOString();

    const { error: vitalsError } = await supabase
      .from("pet_vitals")
      .insert(vitalsData);

    if (vitalsError) {
      console.error("pet_vitals insert error:", vitalsError.message);
    } else {
      results.vitals = "inserted";
    }
  }

  return new Response(
    JSON.stringify({
      success: true,
      microchip_id: payload.microchip_id,
      pet_id: petId,
      actions: results,
    }),
    { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
  );
});
