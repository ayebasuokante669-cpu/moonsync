// testSupabase.js
require('dotenv').config({ path: './.env.local' }); // make sure path is correct
const { createClient } = require('@supabase/supabase-js');

const supabaseUrl = process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_SERVICE_KEY;

if (!supabaseUrl || !supabaseKey) {
  throw new Error('Supabase URL or Key is missing. Check your .env.local');
}

const supabase = createClient(supabaseUrl, supabaseKey);

async function testConnection() {
  try {
    const { data, error } = await supabase.from('users').select('*'); // replace 'users' with a table you have
    if (error) throw error;
    console.log('Supabase connection successful! Data:', data);
  } catch (err) {
    console.error('Supabase connection failed:', err.message);
  }
}

testConnection();
