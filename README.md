# ProxyForward

BungeeCord & Velocity (modern) forwarding for Fabric and NeoForge servers.

## Supported versions

- 1.21.11
- 26.1.2
- 26.2.x

## Installation

1. Download the jar matching your loader and Minecraft version
2. Drop it into your server's `mods/` folder
3. Set `online-mode=false` in `server.properties`
4. Start the server once - a config file is created automatically

## Configuration

Edit `config/proxyforward.toml`. Set `forwarding_mode` to `bungee` or `velocity`.

Alternatively, use environment variables:

| Variable                       | Description                        |
|--------------------------------|------------------------------------|
| `PROXYFORWARD_FORWARDING_MODE` | `bungee` or `velocity`             |
| `PROXYFORWARD_VELOCITY_SECRET` | Secret key for velocity forwarding |

## Build

```sh
./gradlew buildAndCollect
```
