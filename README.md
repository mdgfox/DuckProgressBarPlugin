<h1 align="center">Duck Progress Bar</h1>

<p align="center">
  A rubber duck swims along the progress bars of your JetBrains IDE.
</p>

<p align="center">
  <img src="src/main/resources/skins/duck.svg" alt="Duck" width="96">
  <img src="src/main/resources/skins/vampire.svg" alt="Vampire duck" width="96">
  <img src="src/main/resources/skins/pumpkin.svg" alt="Jack-o'-lantern" width="96">
</p>

<p align="center">
  <img alt="IDE 2024.3+" src="https://img.shields.io/badge/IDE-2024.3%2B-blue">
  <img alt="License: MIT" src="https://img.shields.io/badge/license-MIT-green">
</p>

## Features

- **Indeterminate progress** (indexing, sync, analysis): the duck paddles back and forth and turns around at the edges.
- **Determinate progress** (downloads, builds): the duck swims at the end of the filled part of the bar.
- **Four characters** to choose from.
- **Optional tail** behind the duck: bubbles or a wave.
- Works with every theme, light and dark, and scales with HiDPI.

## Characters

| | Character | Type | Notes |
|:---:|---|---|---|
| <img src="src/main/resources/skins/duck.svg" alt="Duck" width="64"> | Duck | SVG | The default rubber duck |
| <img src="src/main/resources/skins/vampire.svg" alt="Vampire duck" width="64"> | Vampire duck | SVG | Cape, collar, fangs and a red eye |
| <img src="src/main/resources/skins/pumpkin.svg" alt="Jack-o'-lantern" width="64"> | Jack-o'-lantern | SVG | Rolls along the bar instead of turning around |
| <img src="src/main/resources/duck.png" alt="Classic duck" width="64"> | Classic duck | PNG | The original duck from the first versions |

## Installation

- **From the IDE:** `Settings | Plugins | Marketplace`, search for **Duck Progress Bar** and install it.
- **From disk:** download the `.zip` from the releases or build it yourself (see [Development](#development)), then use `Settings | Plugins | ⚙ | Install Plugin from Disk...`.

Requires IntelliJ-based IDE version **2024.3 or newer**.

## Usage

Open `Settings | Appearance & Behavior | Duck Progress Bar`.

| Option | Description |
|---|---|
| Replace the progress bar with the duck | Turns the plugin on or off. Switching it off brings back the standard progress bar. |
| Tail | `None`, `Bubbles` or `Wave`. |
| Apply to determinate bar | Also paints the tail on bars that show a percentage. Off by default, the tail is then shown on indeterminate bars only. |
| Character | Duck, Vampire duck, Jack-o'-lantern or Classic duck. |

Changes are applied with the **Apply** button, including for progress bars that are already on screen.

### Previewing

Don't want to wait for a real task? Use `Tools | Duck Progress Bar`:

- **Preview Indeterminate Progress (10 s)**
- **Preview Determinate Progress (10 s)**

Or press `Shift` twice and type `Preview`.

## Development

Requirements: **JDK 25** (the IDE the plugin is built against runs on Java 25). The plugin itself is compiled to Java 21 bytecode, so it also runs on older IDEs.

| Command | What it does |
|---|---|
| `./gradlew runIde` | Starts a sandbox IDE with the plugin |
| `./gradlew buildPlugin` | Builds the distribution zip into `build/distributions/` |

### Project structure

```text
src/main
├── java/io/github/mdgfox/duckprogressbar
│   ├── DuckProgressBarUI.java            # progress bar painting and animation
│   ├── TailPainter.java                  # bubbles and wave
│   ├── Skin.java                         # characters and their icons
│   ├── TailType.java                     # tail variants
│   ├── DuckSettings.java                 # persistent settings
│   ├── DuckSettingsConfigurable.java     # Settings page
│   ├── ProgressBarListener.java          # installs the UI on startup and theme change
│   └── PreviewProgressAction.java        # 10 s preview tasks
└── resources
    ├── META-INF/plugin.xml
    ├── META-INF/pluginIcon.svg
    ├── skins/                            # SVG characters
    └── duck.png, r_duck.png              # classic PNG duck
```

### How it works

The plugin registers its own `ProgressBarUI` in the Swing `UIManager`. It is installed on IDE startup and again after every theme change, because switching the Look and Feel resets the defaults. The duck's position is calculated from the elapsed time, so its speed does not depend on how often the bar is repainted.

## Contributing

Issues and pull requests are welcome. For bigger changes, please open an issue first to discuss what you would like to change.

## License

Released under the [MIT License](LICENSE).

## Acknowledgements

Inspired by the [Nyan Progress Bar](https://plugins.jetbrains.com/plugin/8575-nyan-progress-bar) plugin.
