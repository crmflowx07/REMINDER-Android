# Production signing for Uptodown

The repository intentionally does **not** contain a private signing key.

Create one release keystore once and keep it permanently. Android updates must be signed with the same key.

GitHub repository secrets required by `.github/workflows/build-signed-release.yml`:

- `SIGNING_KEY_BASE64` — Base64 of the complete .jks file
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

On Windows PowerShell, after creating `reminder-release.jks`:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("reminder-release.jks")) | Set-Clipboard
```

Add the four values at:

**Repository → Settings → Secrets and variables → Actions → New repository secret**

Then open **Actions → Build Signed Uptodown APK → Run workflow**.

The resulting artifact named **REMINDER-1.0.0-Uptodown** contains the signed APK for Uptodown submission.

Do not delete the original .jks file. Keep an offline backup in at least two secure locations.
