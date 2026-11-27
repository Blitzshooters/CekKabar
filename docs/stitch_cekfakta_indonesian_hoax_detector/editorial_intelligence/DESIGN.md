---
name: Editorial Intelligence
colors:
  surface: '#f9f9ff'
  surface-dim: '#d2daf0'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f1f3ff'
  surface-container: '#e8edff'
  surface-container-high: '#e0e8ff'
  surface-container-highest: '#dae2f9'
  on-surface: '#131b2c'
  on-surface-variant: '#464554'
  inverse-surface: '#283041'
  inverse-on-surface: '#edf0ff'
  outline: '#777585'
  outline-variant: '#c7c4d6'
  surface-tint: '#4d4ccd'
  primary: '#251fa8'
  on-primary: '#ffffff'
  primary-container: '#3f3dbf'
  on-primary-container: '#bebeff'
  inverse-primary: '#c1c1ff'
  secondary: '#006d38'
  on-secondary: '#ffffff'
  secondary-container: '#8af6a9'
  on-secondary-container: '#00723b'
  tertiary: '#7a0014'
  on-tertiary: '#ffffff'
  tertiary-container: '#a11323'
  on-tertiary-container: '#ffafac'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e2dfff'
  primary-fixed-dim: '#c1c1ff'
  on-primary-fixed: '#0a006b'
  on-primary-fixed-variant: '#3431b5'
  secondary-fixed: '#8df9ac'
  secondary-fixed-dim: '#71dc92'
  on-secondary-fixed: '#00210d'
  on-secondary-fixed-variant: '#005229'
  tertiary-fixed: '#ffdad8'
  tertiary-fixed-dim: '#ffb3b0'
  on-tertiary-fixed: '#410006'
  on-tertiary-fixed-variant: '#92011a'
  background: '#f9f9ff'
  on-background: '#131b2c'
  surface-variant: '#dae2f9'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  title-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes an authoritative yet accessible editorial-technology aesthetic tailored for Indonesian digital journalism and public discourse verification. It merges the rigor of investigative reporting with the approachable precision of modern civic-tech intelligence.

The brand ethos centers on transparency, non-partisan neutrality, and cognitive clarity. The interface avoids sensationalism or alarmism; instead, it empowers users with calm, evidence-backed breakdowns of complex media claims. 

The aesthetic is characterized by:
- Softened Material Design 3 surfaces with warm, off-white substrates.
- Deliberate semantic framing where credibility states (Fact vs. Hoax) are unmistakable yet visually balanced to prevent cognitive fatigue.
- Restrained, gentle ambient lighting and subtle gradient transitions across hero audit modules.
- Generous internal padding and tactile, rounded container envelopes designed specifically for single-handed mobile navigation (360x800dp canvas).

## Colors

The palette uses a deliberate hierarchy prioritizing objective analytical deep indigo, anchored by decisive semantic indicators for verified truth and identified falsehoods.

### Core Functional Colors
- **Primary (`#3F3DBF`)**: Deep Indigo. Represents systemic authority, neural verification, and calm investigation. Applied to primary CTAs, active navigation states, interactive slider tracks, and deep analytical charts.
- **On Primary (`#FFFFFF`)**: High-contrast white for text and iconography set atop Deep Indigo.
- **Primary Container (`#E0E0FF`)**: Light periwinkle tint. Used for tonal buttons, active filter pills, and selected badge states.

### Surface Architecture
- **Surface / Background (`#F8F9FC`)**: Off-white substrate with a faint cool undertone that mitigates eye strain during prolonged reading.
- **Surface Container (`#FFFFFF`)**: Pure white base for cards, analytical metric containers, and modal sheets.
- **Surface Container High (`#EEF1F8`)**: Elevated structural background for secondary metadata wrappers, navigation bars, and table cell headers.
- **Outline / Border (`#DDE2EC`)**: Low-contrast boundary line used to define card limits and input contours without visual fragmentation.

### Content & Typography Contrast
- **On Surface (`#1E232F`)**: Slate Charcoal. Deep, softened black ensuring maximum legibility for headline typography and primary Indonesian editorial copy.
- **On Surface Variant (`#5A6275`)**: Muted slate gray for secondary timestamps, helper copy, source citations, and inactive icon states.

### Semantic Fact & Hoax Tokens
- **Semantic Fact (`#2E9E5B`)**: Forest Emerald Green. Conveys authenticity, verified consensus, and high truth-probability scoring. Paired with **Fact Container (`#E7F6EC`)** and **On Fact Container (`#0F5B2F`)**.
- **Semantic Hoax (`#E5484D`)**: Vibrant Coral Red. Denotes detected fabrications, manipulated media, and high-risk flags without aggressive hostility. Paired with **Hoax Container (`#FDECEC`)** and **On Hoax Container (`#821318`)**.

## Typography

The typographic system utilizes **Plus Jakarta Sans** for structural headers, visual metrics, and navigational affordances to deliver a modern, welcoming, yet sharp editorial tone. **Inter** is reserved for continuous body passages and explanatory breakdowns, optimizing micro-legibility for Indonesian grammatical structures, affixes, and complex sentence lengths.

### Implementation Guidelines
- **Metric Presentation**: Use `display-lg` and `display-md` strictly for analytical percentages, confidence scores (e.g., "94.2%"), and primary verdict counts.
- **Card Hierarchy**: Card titles utilize `title-md`. Ensure an optical balance between headline weight and supporting body text to maintain scanning speed across dense factual feeds.
- **Editorial Text**: Indonesian article copy must be set in `body-md` with `lineHeight: 20px` to comfortably accommodate multi-syllable word construction without line collision.
- **Badges and Metadata**: Chip labels, confidence badges, timestamps, and navigation labels use `label-md` and `label-sm` with slight tracking adjustments for crisp legibility at small viewport scales.

## Layout & Spacing

The layout is engineered around a standard 360x800dp baseline Android mobile viewport using a responsive 4-column fluid layout with 16dp margins and 16dp gutters.

### Mobile Grid & Spatial Cadence
- **Canvas Margins**: Set universally to `margin` (16dp / 1rem) on horizontal viewport bounds. Full-bleed backgrounds may ignore bounds, but all card wrappers and input controls strictly snap to this border.
- **Vertical Rhythm**: 
  - Standard card-to-card spacing relies on `space-md` (16dp).
  - Internal card padding scales between `space-md` (16dp) for standard analytics and `space-lg` (24dp) for the hero detection input.
  - Section separation (e.g., distinguishing the verification summary from source lineage) uses `space-xl` (32dp).
- **Touch Geometry**: All tap targets adhere strictly to an absolute minimum of 48x48dp, even when visual icon footprints are 24dp.
- **Bottom Navigation Clearance**: Provide 80dp of content offset padding at the bottom of all scroll containers to ensure floating elements or final content rows clear the fixed bottom navigation bar without overlapping.

## Elevation & Depth

Visual depth is achieved through subtle tonal tiering paired with extra-diffused, ambient drop shadows. Crisp, dark outlines and heavy drops are avoided to preserve editorial lightness and prevent visual clutter.

### Surface Elevation Levels
- **Level 0 (Flat / Canvas)**: `background: #F8F9FC`. Base screen canvas, zero shadow, zero boundary.
- **Level 1 (Card & Content Blocks)**: `background: #FFFFFF`, border: `1px solid #DDE2EC`. Shadow: `0px 2px 8px rgba(30, 35, 47, 0.04)`. Used for standard feeds, history logs, and matrix cards.
- **Level 2 (Interactive Floating Elements & Bottom Nav)**: `background: #FFFFFF`, border: `1px solid rgba(221, 226, 236, 0.6)`. Shadow: `0px 6px 20px rgba(30, 35, 47, 0.08)`. Applied to the fixed bottom navigation bar and active input fields.
- **Level 3 (Modals & Bottom Sheets)**: `background: #FFFFFF`. Shadow: `0px 12px 32px rgba(30, 35, 47, 0.14)`. Applied to deep dive explainability overlays and source fact-check drawer sheets.

### Hero Gradient Treatment
For the primary hero module (Deteksi input & primary verdict result):
- Use an ultra-soft gradient fill: Linear gradient from `#3F3DBF` (Deep Indigo at 100%) to `#5B59D6` at an angle of 135°.
- When placed over light surfaces, apply an ambient glow shadow: `0px 8px 24px rgba(63, 61, 191, 0.20)`.

## Shapes

The design system adopts a balanced roundedness model (`level 2`, 8dp base), scaling progressively to structural surfaces to emphasize friendly accessibility without losing technological precision.

### Application Specs
- **Cards & Data Modules**: 16dp to 24dp border-radius. Hero cards and analytical result blocks use the larger 24dp radius to create inviting, high-impact surfaces. Standard list cards use 16dp.
- **Input Textareas**: 16dp border-radius. Provides a smooth, containerized look for pasted Indonesian text or URL strings.
- **Buttons & Chips**: Fully pill-shaped (999dp) for all chips, badges, and primary action buttons.
- **Navigation Indicators**: 16dp pill-shaped active indicator capsules within the bottom navigation rail.

## Components

### Bottom Navigation Bar (Material 3)
- **Container**: 80dp total height. Background: `#FFFFFF` or `Surface Container High (#EEF1F8)`. Top border: 1px `#DDE2EC`.
- **Destinations (5)**: 
  1. *Deteksi* (Icon: Search/Shield Check)
  2. *Analisis* (Icon: Insights/Psychology)
  3. *WordCloud* (Icon: Cloud/Text Formatting)
  4. *Evaluasi* (Icon: Precision Matrix/Tune)
  5. *Riwayat* (Icon: History/Clock)
- **Active State**: Pill-shaped container (`#E0E0FF`) surrounding the active icon, with icon and label tinted `#3F3DBF`.
- **Inactive State**: Icon and label set to `#5A6275`. Labels set in `label-sm`.

### Buttons
- **Primary Filled Button**: Height 48dp minimum, fully pill-shaped. Background `#3F3DBF`, text `#FFFFFF` (`label-lg`). Active press scales down slightly (0.98x) with a deep indigo ripple.
- **Tonal Button**: Height 48dp, pill-shaped. Background `#E0E0FF`, text `#3F3DBF`. Used for secondary actions (e.g., "Salin Analisis", "Lihat Sumber Rujukan").
- **Outlined Button**: Height 48dp, 1px border `#DDE2EC`, text `#1E232F`. Used for low-priority actions (e.g., "Reset Teks").

### Input Fields (Deteksi Teks)
- Multi-line text field with a minimum height of 140dp.
- Card wrapper background `#FFFFFF`, border `1.5px solid #DDE2EC`, rounded at 16dp.
- Focus state shifts border color to `#3F3DBF` with a soft outer ring: `0 0 0 3px rgba(63, 61, 191, 0.12)`.
- Includes sticky trailing utilities: clear input ("Hapus"), paste clipboard ("Tempel Tautan/Teks"), and character counter in `label-sm`.

### Verdict Chips & Badges
- Pill-shaped chips with 8dp horizontal padding and 4dp vertical padding.
- **Status: Hoax**: Background `#FDECEC`, text `#821318`, icon alert-triangle (`#E5484D`).
- **Status: Fakta**: Background `#E7F6EC`, text `#0F5B2F`, icon check-circle (`#2E9E5B`).
- **Status: Netral / Analisis Lanjut**: Background `#EEF1F8`, text `#5A6275`.

### Semi-Circular Gauge (Skor Probabilitas)
- Used on the *Deteksi* results screen to communicate verdict confidence (0% to 100%).
- 180-degree sweep track (12dp stroke width) in `#EEF1F8`.
- Active segment colored dynamically: `#2E9E5B` (Fakta) or `#E5484D` (Hoax).
- Centered readout: Display percentage in `display-lg`, with label "Tingkat Kepastian" in `label-sm` (`#5A6275`).

### Dual-Direction Bar Charts (Sentimen & Bias)
- Displays linguistic feature impact (e.g., "Kosakata Sensasional" vs. "Bukti Faktual").
- Horizontal diverging bars anchored at a central vertical axis.
- Positive/factual contributions render in `#2E9E5B`; negative/hoax indicators extend in `#E5484D`.
- Bars have rounded caps (4dp). Attribute labels in `body-sm` (`#1E232F`).

### Confusion Matrix Heatmap Card (Evaluasi Model)
- Structural grid embedded inside a 20dp rounded surface container.
- 2x2 grid representing *Prediksi Fakta*, *Prediksi Hoax*, *Aktual Fakta*, *Aktual Hoax*.
- Cell fills scale along a monochromatic indigo opacity gradient (from `rgba(63, 61, 191, 0.05)` to `rgba(63, 61, 191, 0.85)`).
- Text within high-density cells automatically switches to `#FFFFFF` for contrast compliance.

### WordCloud Entity Card
- Contained card (20dp radius) featuring token clusters extracted from the analyzed text.
- Word weight represented proportionally by font size (12dp to 22dp) and font weight (`fontWeight: 500` through `800`).
- Token chip background uses tonal tints based on token polarity: suspicious/alarmist words in `#FDECEC`, factual references in `#E7F6EC`, neutral stopwords in `#EEF1F8`.