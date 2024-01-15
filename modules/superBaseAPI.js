import { createClient } from "@supabase/supabase-js";

export const superBase = createClient(
    process.env.SUPERBASE_URL,
    process.env.SUPERBASE_KEY
)

