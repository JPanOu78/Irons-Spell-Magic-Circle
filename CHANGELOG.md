# Changelog

All notable changes to **Iron's Spell Magic Circle** will be documented in this file.

---

## [1.0.1] - 2026-11-08

### ✨ Compatibility & New Features
- **Iron's Arms Spell Bullet Compatibility**: Arcane magic circles now dynamically appear at the firearm's muzzle whenever firing a spell bullet!
- **Instant Full Layer Emergence**: Gun muzzle circles render all active layers (2–5 layers depending on spell rarity) simultaneously at full opacity on tick 0, without any layer stagger or delay.
- **Flash & Smooth Fade-Out**: Gunshot circles flash intensely upon firing, with an energetic blast wave expansion (+12%), then smoothly dissolve away over configurable duration ticks.
- **Dynamic Weapon Muzzle Tracking**: Follows real-time weapon barrel length (`MUZZLE_OFFSET`), player handedness (left/right arm), view angles, and automatically centers when scoping/aiming down sights.
- **Compact Muzzle Scaling**: Scaled down to fit firearm muzzle barrels (~0.23m base radius) rather than the body-sized 1.4m human circle.
- **Soft Dependency Architecture**: Mod remains 100% Client-Only (`Dist.CLIENT`) and safely operates standalone even if `irons_artifice` or `irons_spell_bullet` is not installed.

### ⚙ Configuration Additions
- Added `enableGunMagicCircles`: Toggle firearm muzzle magic circles on/off (default: `true`).
- Added `gunCircleScale`: Scale multiplier for gun muzzle circles (default: `0.35`, range: `0.1` to `3.0`).
- Added `gunCircleDurationTicks`: Duration in ticks for muzzle circle flash and fade-out (default: `12`, range: `3` to `60`).
- Added `gunCircleForwardOffset`: Positional forward/backward offset along the barrel (default: `0.0`, range: `-1.0` to `2.0`).
- Added `gunCircleRotationSpeed`: Rotation speed multiplier for the high-energy muzzle spin (default: `4.5`, range: `0.0` to `30.0`).
- Added `gunCircleOpacity`: Maximum opacity for gun muzzle circles (default: `0.65`, range: `0.05` to `1.0`).

---

## [1.0.0] - 2026-11-04

### ✨ Initial Release
- **100% Client-Side (`Dist.CLIENT`)**: Fully standalone client-side visual addon for *Iron's Spells 'n Spellbooks*.
- **School-Specific 5-Layer System**: Dedicated hand-crafted 5-layer geometric magic circle texture sets for all 9 magic schools (Fire, Ice, Lightning, Holy, Ender, Blood, Evocation, Nature, Eldritch).
- **Staggered Layer Emergence & Slide-In Convergence**: Staggered slide-in animations based on spell rarity (Common to Legendary).
- **Dual-Pass HDR Shader Bloom Glow**: Dual-pass rendering using `entityTranslucentEmissive` and `magicNoCull` for shader packs (Iris/Oculus).
- **Dynamic Player Scale Support**: Magic circle radius and offsets scale proportionally with entity attributes (`player.getScale()`).
- **Client Configuration (`irons_magic_circle-client.toml`)**: Customization options for scale, opacity, rotation speed, fade-out time, and shader bloom intensity.
