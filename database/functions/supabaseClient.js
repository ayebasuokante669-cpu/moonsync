const { createClient } = require("@supabase/supabase-js");

// Replace these with your actual Supabase values
const SUPABASE_URL ="https://wpmsfovkfrxmotvisvkp.supabase.co";


const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY);

module.exports = supabase;
