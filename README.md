
# RegistryLib

A Java library for dynamic registration to the Minecraft registry, enabling plugins to add registry entries at runtime, and helping players reload server registry without re-join. RegistryLib also provides event hooks so other plugins can listen to dynamic registry operations from other plugins.


## Features

- **Dynamic Registration**: Register items, blocks, or other registry entries at runtime.
- **Registry Reloading**: Reload the registry for players without requiring a disconnect/reconnect.
- **Event System**: Other plugins can subscribe to events and respond to dynamic registry changes.
- **Designed for Minecraft Plugins**: Easily integrated into your plugin ecosystem.

> [!WARNING]
> Please DO NOT use reloading feature with velocity after 1.21, velocity DOES NOT handle reloading correctly!

## Installation

- Download the [latest version](https://github.com/Lumine1909/RegistryLib/releases/latest)
- Install to your server, at `/plugins`
- Load it from Plugman or restart your server

## Usage

### Command
```
/registrylib - admin command
permission: registrylib.admin

/reloadregistry - reload registry
permission: registrylib.reloadregistry
```

### API usage

See [ExamplePlugin](https://github.com/Lumine1909/RegistryLib/tree/main/ExamplePlugin)

## License

This project is licensed under the [Apache License 2.0](LICENSE).