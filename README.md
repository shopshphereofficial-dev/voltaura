# VoltAura

Premium charging animations and notification edge-glow for Android.

## Features
**Charging Animation** — when you plug in the charger, an animated overlay appears
with the current battery percentage and live charging stats (watts, amps, volts,
temperature and charge source). Six animation styles: Neon Ring, Liquid Fill,
Particles, Energy Bolts, Pulse, Wave Bars.

**Notification Glow** — a colourful glowing border animates around the screen edges
whenever a notification arrives. Five styles (Solid, Rainbow, Gradient, Pulse,
Aurora), six colours, adjustable thickness and duration.

## How it works
- `service/ChargingOverlayService` — a foreground service that listens for
  power-connected broadcasts and draws the charging animation as an overlay.
- `service/NotificationGlowService` — a NotificationListenerService that draws the
  edge glow overlay when a notification is posted.
- `overlay/ChargingAnimationView` and `overlay/EdgeGlowView` — the custom canvas
  animations.
- `battery/BatteryReader` — reads live charging data from BatteryManager.

## Permissions the app asks for
- **Draw over other apps** (overlay) — to show the animation on top of everything.
- **Notification access** — to detect incoming notifications for the glow.
- **Notifications** (Android 13+) — for the small ongoing service notification.

## Notes
- Charging watts/amps are read from `BatteryManager`. On some devices the reported
  current is an approximation; exact figures may need root. Values shown are the
  device's own reported numbers.
- Some phone makers (Xiaomi, Oppo, Vivo, Samsung) aggressively stop background
  services. If the animation stops working, disable battery optimisation for
  VoltAura in system settings.
- Built automatically by GitHub Actions on every push to `main`.

## Build
Push to `main`, then download the `voltaura-apk` artifact from the workflow run.
