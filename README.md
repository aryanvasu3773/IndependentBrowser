# Independent Browser (starter v1)

A dark Android browser starter using the Android System WebView.

## Build
The included GitHub Actions workflow builds a debug APK on every push to `main`, or manually from Actions.

1. Create a GitHub repository and upload/push this project.
2. Open **Actions** → **Build Android APK** → **Run workflow**.
3. Open the completed run and download the `independent-browser-debug` artifact.
4. Extract the artifact and install `app-debug.apk` on the Redmi 9.

## Termux source workflow
Install Git:
```sh
pkg update && pkg install git
```
Clone your repository:
```sh
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
cd YOUR_REPOSITORY
```
After editing files:
```sh
git add .
git commit -m "Update browser"
git push
```
GitHub Actions will build the APK.

## Rename
Change `app/src/main/AndroidManifest.xml` application `android:label` from `My Browser` to your chosen name. Also change the header text `MY BROWSER` in `MainActivity.java`.

## Important limitations in v1
- Uses the Android System WebView already installed on the phone. It is not a standalone Chromium engine.
- Private tabs are a basic no-cache mode, not a guarantee of complete anonymity. Sites, network providers, and Android components may still observe activity.
- Bookmarks, persistent history UI, download management, ad blocking, and AI tools are not implemented yet.
- The first version requests JavaScript because many modern sites require it. Be cautious with unknown sites.
