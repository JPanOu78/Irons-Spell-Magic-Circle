# Changelog

All notable changes to **Iron's Spell Magic Circle** will be documented in this file.

---

## [1.0.2] - 2026-11-15

### 🎨 Fully Customizable Layer Textures
- **Native File Picker (`📂 Select File...`)**: Players no longer need to type or copy-paste complicated file paths! Clicking `Select File...` opens the native Windows file chooser dialog (`TinyFileDialogs`) to pick any `.png` image directly from their computer.
- **Dedicated Textures Folder (`📁 Open Folder`)**: Opens `.minecraft/config/irons_magic_circle/textures/` in Windows Explorer with one click. Any PNG files dropped into this folder are automatically detected.
- **In-Game Quick Folder Cycler (`🔄 File: ...`)**: If PNG images exist in the textures folder, a convenient cycle button appears in the GUI allowing players to switch between textures with a single click.
- **DynamicTexture GPU Upload (`CustomTextureManager`)**: Chosen external PNG images are automatically loaded and registered into Minecraft's `TextureManager` on the GPU as `DynamicTexture`, rendering seamlessly in both the in-game GUI 3D preview and the world.
- **Custom Texture Overrides for Layers 1..5 (`layer1Texture` to `layer5Texture`)**: Players can also still use custom ResourceLocation paths or `{school}` placeholder if preferred, or leave empty for school defaults.

### ⚡ Per-Layer Size & Rotation Speed Controls
- **Independent Layer Scaling**: Configure individual size scale multipliers for each of the 5 layers.
- **Independent Layer Speeds**: Adjust rotation speed and direction independently per layer (Clockwise / Counter-Clockwise).

### 🎬 Display Phase Dynamics Customization
- **Phase 1: Emergence / Cast-In**:
  - Starting scale factor when layers first materialize
  - Rotation speed multiplier during emergence
  - Duration for layers to fade in and slide inward
  - Stagger delay between stacked layers appearing
  - Slide distance in front of the caster
- **Phase 2: Active / Sustained Casting**:
  - Overall scale multiplier during sustained casting
  - Rotation speed multiplier during active casting
  - Idle breathing pulse intensity
- **Phase 3: Fade-Out / Dissolution**:
  - Target scale as the circle dissolves
  - Rotation speed multiplier during dissolution
  - Total fade-out duration
  - Forward/outward drift distance as it fades
- **Gun Muzzle Blastwave**:
  - Configurable explosive blast expansion when firing spell bullets

### 🖥️ Interactive In-Game Configuration GUI (`MagicCircleConfigScreen`)
- **Live 3D Rotating Preview**: Real-time rendering inside the configuration GUI showcasing the magic circle spinning in 3D with live visual updates for every slider and texture input.
- **School & Layer Switcher**: Test how textures and animations appear on any magic school and across 1 to 5 layers.
- **Cast Simulation (`Simulate Cast`)**: Dedicated button in the preview window to test the entire Emergence -> Active -> Fade-out animation lifecycle directly in GUI.
- **Mod Menu Integration**: Registered `IConfigScreenFactory` to open the GUI directly via `Mods -> Iron's Spell Magic Circle -> Config`.
- **In-Game Keybind (`MagicCircleKeyHandler`)**: Press `O` in-game to open the configuration screen at any time.

### 🔮 Dynamic Addon School Detection & Fallback Tinting
- **Dynamic Registry Scanning (`SchoolVisualHelper`)**: Automatically queries `SchoolRegistry.REGISTRY` for any schools registered by third-party Iron's Spells addons at runtime (Void, Astral, Poison, Sound, etc.).
- **Native Addon Asset Lookup**: Seamlessly loads custom layer textures if provided by the addon or resource pack.
- **Automatic Dynamic Geometric Fallback Tinting**: If an addon school lacks dedicated textures, cleanly tints geometric fallback rings with the addon's targeting color, preventing missing textures or black-and-purple squares.
- **Dynamic GUI School Switcher**: Lists all registered addon schools in the switcher button with real-time 3D rotation preview.

### 📐 Responsive Dynamic Layout & Text Overlap Resolution
- **Zero-Overlap Label Positioning**: Dynamic vertical coordinate flow for section headers, completely eliminating text overlapping on top of buttons.
- **Adaptive Screen-Size Scaling**:
  - Automatically calculates available vertical and horizontal space based on window size and Minecraft GUI Scale setting.
  - Dynamically adjusts button and slider heights, margins, and gaps when resized or on high GUI scales, preventing UI clipping.
- **Clamped 3D Preview Frame**:
  - 3D magic circle preview frame scales dynamically between top controls and the bottom simulate button.
  - Preview circle radius is clamped within frame boundaries, preventing large custom scales from spilling outside.
- **Preset List Pagination**: Automatic page controls (`[◀ Prev] [Page X/Y] [Next ▶]`) when preset lists exceed screen height.
- **Responsive Action Bar**: Bottom action buttons dynamically adapt width to prevent overlapping on narrow windows.

### 💾 Preset Save / Load / Delete System (`PresetManager`)
- **Custom Preset Maker & Naming**:
  - Dedicated custom presets interface with text input to name and save configurations as `.json` presets.
  - Quick `💾 Save Preset...` shortcut directly on the bottom action bar across all tabs.
  - Presets list with 1-click Load, Update (overwrite), and Delete controls.
- **5 Standard Built-In Presets Included**:
  1. `Standard Arcane`: Balanced rotation speeds and organic staggered emergence.
  2. `Turbine Overdrive`: Rapid high-speed spinning layers with snappy explosive emergence.
  3. `Eldritch Ritual`: Slow ceremonial rotation with grand scale and lingering dissolve drift.
  4. `Compact Tactical`: Scaled down with subtle opacity for clean combat visibility.
  5. `Supernova Burst`: Expanding burst emergence with intense shader bloom glow and heavy blastwave.
  - Includes a `Customize` button to load standard presets directly into the Custom Maker for tweaking and renaming.
- **File Management & Sharing**:
  - Saved under `.minecraft/config/irons_magic_circle/presets/<name>.json`.
  - Added `📁 Open Presets Folder...` button to open the directory directly in Windows Explorer.

---

## [1.0.1] - 2026-11-08

### ✨ Compatibility & New Features
- **Iron's Arms Spell Bullet Compatibility**: Arcane magic circles now dynamically appear at the firearm's muzzle whenever firing a spell bullet!
- **Instant Full Layer Emergence**: Gun muzzle circles render all active layers (2–5 layers depending on spell rarity) simultaneously at full opacity on tick 0, without any layer stagger or delay.
- **Flash & Smooth Fade-Out**: Gunshot circles flash intensely upon firing, with an energetic blast wave expansion, then smoothly dissolve away.
- **Dynamic Weapon Muzzle Tracking**: Follows real-time weapon barrel length, player handedness (left/right arm), view angles, and automatically centers when scoping/aiming down sights.
- **Compact Muzzle Scaling**: Scaled to fit firearm muzzle barrels naturally.
- **Soft Dependency Architecture**: Mod remains 100% Client-Only (`Dist.CLIENT`) and safely operates standalone even if gun mods are not installed.

### ⚙ Configuration Additions
- Added toggles and sliders for firearm muzzle magic circles, scale, duration, barrel offset, rotation speed, and opacity.

---

## [1.0.0] - 2026-11-04

### ✨ Initial Release
- **100% Client-Side (`Dist.CLIENT`)**: Fully standalone client-side visual addon for *Iron's Spells 'n Spellbooks*.
- **School-Specific 5-Layer System**: Dedicated hand-crafted 5-layer geometric magic circle texture sets for all 9 magic schools (Fire, Ice, Lightning, Holy, Ender, Blood, Evocation, Nature, Eldritch).
- **Staggered Layer Emergence & Slide-In Convergence**: Staggered slide-in animations based on spell rarity (Common to Legendary).
- **Dual-Pass HDR Shader Bloom Glow**: Dual-pass rendering using `entityTranslucentEmissive` and `magicNoCull` for shader packs (Iris/Oculus).
- **Dynamic Player Scale Support**: Magic circle radius and offsets scale proportionally with entity attributes (`player.getScale()`).
- **Client Configuration (`irons_magic_circle-client.toml`)**: Customization options for scale, opacity, rotation speed, fade-out time, and shader bloom intensity.

