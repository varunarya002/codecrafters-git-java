# Command dispatch via interface + static registry

To keep command dispatch open for extension and closed for modification, `Command` is an interface implemented by one class per subcommand (`InitCommand`, `CatFileCommand`, `HashObjectCommand`), wired by name in a static `CommandRegistry` map. We deliberately moved away from the `enum`-with-abstract-method style (still used for `CatFileMode`) because an enum is a closed set — adding a subcommand would force editing existing code. Adding a command now means adding a class plus one registration line, never reopening existing command logic.

## Considered Options

- **Enum of commands** (the original shape) — compact, but adding a command requires modifying the enum, violating OCP.
- **`ServiceLoader` auto-discovery** — zero-edit registration, but heavy ceremony (`META-INF/services`, no compile-time safety) unjustified at this size.
- **Interface + explicit static registry** (chosen) — true OCP at the command layer; the single `register(...)` line is a trivial, mechanical touch that never reopens existing behavior.

Flag handling stays each command's private concern: `cat-file` keeps a `CatFileMode` Strategy because its `-p`/`-t`/`-s` flags are competing output modes, while `hash-object` parses its `-w` boolean toggle inline rather than forcing it into a Strategy it doesn't fit.
