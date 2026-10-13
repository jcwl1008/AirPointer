# AirPointer 1.7 — Calculator-inspired

Approved concept: iOS Calculator palette combined with the user's Apple editorial design specification. This is an Android app, not an Apple product. Orange overrides the supplied website specification's blue accent.

| Token | Value | Role |
| --- | --- | --- |
| CANVAS | #000000 | Full-screen background and system bars |
| SURFACE | #333333 | Keys, pointer pad, local draft |
| SURFACE_ALT | #272729 | Fixed mode dock |
| UTILITY | #A5A5A5 | Shift, backspace, space and mute |
| ACCENT | #FF9F0A | Main actions and selected mode |
| INK | #FFFFFF | Dark-surface text |
| MUTED | #CCCCCC | Secondary dark-surface text |

- One accent, no decorative gradients or UI shadows.
- Android system sans-serif; 600 headlines, 400 body. No proprietary fonts.
- Structural spacing follows an 8px rhythm; typography uses a 17sp baseline.
- Solid full-bleed canvas, 18dp utility rounding and pill primary actions.
- Circular media/microphone buttons; rectangular QWERTY keys preserve typing area.
- Default button press scale 0.95 with a brief transition and haptic feedback. Gesture surfaces keep their dedicated feedback.
- Portrait mode has a fixed mode dock; landscape prioritizes the keyboard and uses the existing compact toolbar.
- Keep text/background contrast >=4.5:1. Orange and light-gray buttons use black text.
- Do not invent PC text, playback state or album information. Drafts are local only.
- Responsive scroll containers preserve access on compact or enlarged-font screens. Physical visual/gesture verification remains pending.
