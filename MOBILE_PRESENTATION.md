# Mobile Presentation Guide

Follow these steps to present the PetCare app on your local mobile device.

## Option 1: USB Debugging (Recommended)

1.  **Enable Developer Options** on your Android phone:
    - Go to Settings > About Phone.
    - Tap **Build Number** 7 times.
2.  **Enable USB Debugging**:
    - Go to Settings > System > Developer Options.
    - Turn on **USB Debugging**.
3.  **Connect to PC**:
    - Connect your phone to your computer via USB.
    - If prompted on the phone, "Allow USB debugging".
4.  **Run from Android Studio**:
    - In Android Studio, select your physical phone from the device dropdown.
    - Click **Run**. The app will be installed and launched on your phone.

## Option 2: Install via APK

1.  **Build APK**:
    - In Android Studio, go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2.  **Locate APK**:
    - Once finished, a notification will appear. Click **locate** to find `app-debug.apk`.
3.  **Transfer to Phone**:
    - Send this file to your phone (via USB, email, or cloud storage).
4.  **Install**:
    - On your phone, open the APK file.
    - If prompted, allow "Install from unknown sources" for your file manager/browser.
    - Follow the prompts to install PetCare.

## Tips for Presentation

- **Data Sync**: Ensure your phone has internet access so it can connect to Supabase.
- **Login**: Use a Google account to sign in and show the seamless authentication flow.
- **Demo Pets**: Add 1-2 pets before the presentation so the home screen looks populated.
