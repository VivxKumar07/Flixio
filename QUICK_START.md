# Quick Start Checklist for Flixio Supabase Setup

## ✅ What I've Fixed For You

1. **Created Fixed SQL File**: `C:\Users\OnePeak\Downloads\Compressed\self-host-main\flixio_supabase_fixed.sql`
   - This won't give "already exists" errors when you re-run it
   - Uses `CREATE TABLE IF NOT EXISTS` and `CREATE OR REPLACE FUNCTION`

2. **Created Setup Guide**: `SUPABASE_SETUP_GUIDE.md`
   - Complete step-by-step instructions

3. **Created Verification Scripts**:
   - `verify_supabase_setup.bat` (for Windows - double-click to run)
   - `verify_supabase_setup.sh` (for Git Bash)

## ⚠️ What YOU Need To Do

### Step 1: Get Your Real Supabase Credentials (5 minutes)

1. Go to: https://supabase.com/dashboard
2. Click on your Flixio project
3. Click **Settings** → **API** (gear icon in sidebar)
4. Copy these values:

   ```
   Project URL: https://xxxxx.supabase.co
   anon/public key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

   ⚠️ The anon key should be **VERY LONG** (~300 characters). If it's short, you copied the wrong key!

### Step 2: Import Fixed SQL to Supabase (3 minutes)

1. Open Supabase Dashboard → **SQL Editor**
2. Click **New Query**
3. Open this file in Notepad: `C:\Users\OnePeak\Downloads\Compressed\self-host-main\flixio_supabase_fixed.sql`
4. Select All (Ctrl+A) → Copy (Ctrl+C)
5. Paste into Supabase SQL Editor
6. Click **Run** (or Ctrl+Enter)
7. Wait for it to complete (should say "Success" with no errors)

### Step 3: Update Your Credentials (2 minutes)

1. Open: `C:\Users\OnePeak\Downloads\Flixio\Flixio\local.properties`
2. Replace these lines with YOUR real values from Step 1:

   ```properties
   NUVIO_SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
   NUVIO_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.YOUR_ACTUAL_LONG_KEY_HERE
   ```

3. Save the file

### Step 4: Rebuild the App (5 minutes)

Open Command Prompt in your project folder and run:

```cmd
gradlew.bat clean
gradlew.bat :composeApp:generateRuntimeConfigs
gradlew.bat assembleFullDebug
```

### Step 5: Verify Setup

Double-click: `verify_supabase_setup.bat`

It should show all ✓ checks passing. If not, follow the instructions it shows.

### Step 6: Test the App

1. Install and run the app on your device
2. Create an account (sign up with email)
3. Create a profile
4. Add a movie to your library
5. Go to Supabase Dashboard → **Table Editor** → **profiles**
6. You should see your profile data! ✅

## 🔍 Current Status

**Problem:** Your app has **placeholder credentials** (`sb_publishable_KolLV18Po604243G6rJbHw_t3wNklmo`)
- This is only 46 characters - NOT a valid Supabase key
- Real keys are JWT tokens: `eyJ...` and are ~300 characters

**Status:**
- ❌ Invalid credentials in local.properties
- ❌ Placeholder config in generated files
- ✅ Fixed SQL file ready to import
- ✅ All documentation and scripts created

## 📋 Verification Checklist

- [ ] Got real credentials from Supabase Dashboard
- [ ] Imported flixio_supabase_fixed.sql successfully
- [ ] Updated local.properties with real credentials
- [ ] Rebuilt the app with gradlew
- [ ] Ran verify_supabase_setup.bat (all checks pass)
- [ ] Tested the app (profile data saves)

## 🆘 If You Need Help

1. **"Authentication server is not configured" error**
   - Your credentials are still placeholders
   - Go back to Step 1 and get the REAL anon key (must start with `eyJ`)

2. **SQL import still fails**
   - Make sure you're using `flixio_supabase_fixed.sql` NOT the original
   - See SUPABASE_SETUP_GUIDE.md "Option B" for dropping tables first

3. **verify_supabase_setup.bat shows errors**
   - Follow the exact steps it tells you
   - Make sure the anon key starts with `eyJ` and is very long

4. **Data still not saving**
   - Check your Supabase project is not paused (free tier)
   - Verify the SQL import completed (check Functions in Supabase Dashboard)
   - Check app logs for specific error messages

---

**Total Time Required: ~20 minutes**

**Once complete, your Flixio app will be fully connected to Supabase and all user data will save properly!**
