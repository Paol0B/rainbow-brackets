# Rainbow Brackets

A JetBrains IDE plugin that colorizes matching brackets, parentheses, braces, and angle brackets
with rainbow colors for improved code readability.

**Author:** Paolo Bertinetti  
**Version:** 1.0.0  
**Compatibility:** JetBrains Platform 2025.1+

## Features

- **6 cyclic rainbow colors** for nested brackets
- **Supported delimiters:** `()`, `{}`, `[]`, `<>`, XML/HTML tags
- **Smart operator detection:** does NOT color comparison operators (`a < b`)
- **All JetBrains languages** supported via universal PSI traversal
- **Theme-aware:** separate color palettes for Light and Dark themes (VS C# style)
- **Selection highlighting:** translucent background highlight when selecting bracketed blocks
- **Configurable:** Settings → Tools → Rainbow Brackets
- **High performance:** cached bracket computation, parallel annotator execution

## Installation

### From Disk
1. Build the plugin: `./gradlew buildPlugin`
2. In your JetBrains IDE: Settings → Plugins → ⚙ → Install Plugin from Disk
3. Select `build/distributions/rainbow-brackets-1.0.0.zip`

### From JetBrains Marketplace
*(Coming soon)*

## Configuration

Navigate to **Settings → Tools → Rainbow Brackets** to:

- Toggle plugin on/off
- Enable/disable specific bracket types (`()`, `[]`, `{}`, `<>`)
- Enable/disable selection block highlighting
- Adjust selection highlight opacity

### Color Customization

Navigate to **Settings → Editor → Color Scheme → Rainbow Brackets** to customize
colors for each nesting level.

## Default Colors

### Light Theme (VS C# style)
| Level | Color  | Hex     |
|-------|--------|---------|
| 1     | Gold   | #D4A017 |
| 2     | Orchid | #DA70D6 |
| 3     | Blue   | #179FFF |
| 4     | Gold   | #D4A017 |
| 5     | Orchid | #DA70D6 |
| 6     | Blue   | #179FFF |

### Dark Theme (VS C# style)
| Level | Color  | Hex     |
|-------|--------|---------|
| 1     | Gold   | #FFD700 |
| 2     | Orchid | #DA70D6 |
| 3     | Blue   | #179FFF |
| 4     | Gold   | #FFD700 |
| 5     | Orchid | #DA70D6 |
| 6     | Blue   | #179FFF |

## Architecture

```
src/main/kotlin/it/bertinetti/rainbowbrackets/
├── core/               # Pure logic (IDE-independent, fully testable)
│   ├── BracketType        # Bracket type enum
│   ├── BracketToken       # Immutable bracket representation
│   ├── BracketLevelCalculator  # Nesting level computation
│   └── BracketContextResolver # Angle bracket disambiguation
├── highlighting/       # Annotator integration
│   ├── RainbowBracketAnnotator   # Element-level annotator
│   ├── RainbowHighlighterService # Cached level computation
│   └── EditorRangeUpdater        # Range highlight management
├── selection/          # Selection block highlighting
│   ├── SelectionBlockHighlighter # Background overlay
│   ├── SelectionContextResolver  # Affected bracket detection
│   └── RainbowSelectionListener  # Event listener
├── colors/             # Theme-aware color management
│   ├── DefaultColorPalettes      # VS C# color definitions
│   ├── RainbowColorProvider      # TextAttributesKey factory
│   ├── ThemeAwareColorResolver   # Theme adapter
│   └── RainbowColorSettingsPage  # Color scheme UI
├── settings/           # Persistent configuration
│   ├── RainbowSettingsState      # PersistentStateComponent
│   ├── RainbowSettingsComponent  # UI form
│   └── RainbowSettingsConfigurable # Settings integration
├── util/               # Shared utilities
│   ├── PsiUtils          # Language-agnostic PSI helpers
│   ├── EditorUtils       # Editor operation helpers
│   └── PerformanceGuard  # Timeout & safety guards
└── diagnostics/        # Performance instrumentation
    └── BenchmarkHooks    # Metrics collection
```

## Development

### Prerequisites
- JDK 21+
- Gradle 8.12 (wrapper included)

### Build
```bash
./gradlew build
```

### Run IDE with Plugin
```bash
./gradlew runIde
```

### Run Tests
```bash
./gradlew test
```

### Package
```bash
./gradlew buildPlugin
```

## Performance

- **No full-file reparse** — element-level annotator with parallel execution
- **Cached computation** — bracket levels cached per file, invalidated on change
- **Filtered listeners** — selection listener is declarative and lazy-loaded
- **Safety guards** — large files (>500K chars) are skipped automatically

### Benchmark Targets
| Scenario        | Target            |
|-----------------|-------------------|
| 1K-line file    | < 5ms             |
| 10K-line file   | < 50ms            |
| 50K-line file   | < 500ms           |
| Selection update| < 2ms             |

