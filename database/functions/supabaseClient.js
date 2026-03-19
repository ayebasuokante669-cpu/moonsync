const { createClient } = require("@supabase/supabase-js");

// Replace these with your actual Supabase values
const SUPABASE_URL ="https://wpmsfovkfrxmotvisvkp.supabase.co";
const SUPABASE_SERVICE_KEY = "sb_secret_ELHOwYH6OwAu5G5UZeTt0A_6pnlTOnP";

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_KEY);

module.exports = supabase;
