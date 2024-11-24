# 4TPA

A Paper/Folia TPA plugin.

![A gif demonstrating the /tpaccept command. A player named "Pride_Dad" sending a TPA request to a player named "OoLunar." The request is accepted and Pride_Dad is teleported to OoLunar.](./res/tpa.gif)

![A gif demonstrating the /tpadeny command. A player named "Pride_Dad" sending a TPA request to a player named "OoLunar." The request is denied and Pride_Dad is not teleported to OoLunar.](./res/tpadeny.gif)

# Commands

- `/back` - Teleport to your previous location
- `/tpa <player>` - Request to teleport to a player
- `/tpaccept [player]` - Accept a TPA request from any player or from a specific player
- `/tpacceptall` - Accept all TPA requests
- `/tpahere <player>` - Request a player to teleport to you
- `/tpcancel` - Cancel a TPA request
- `/tpdeny [player]` - Deny a TPA request from any player or from a specific player
- `/tpdenyall` - Deny all TPA requests

## Admin Commands
- `/tpaoffline <player>` - Request to teleport to an offline player
- `/tpreload` - Reload the plugin configuration

# Configuration

```yml
# Which language file to use within plugins/4TPA/lang
lang: en_US

# When a TPA request should auto-cancel
tpa_timeout: 60
```

# Permissions

```yml
fourtpa.back:
  default: true
  description: Allows the player to use the /back command
fourtpa.reload:
  default: op
  description: Allows the player to use the /tpareload command
fourtpa.tpa:
  default: true
  description: Allows the player to use the /tpa command
fourtpa.tpaccept:
  default: true
  description: Allows the player to use the /tpaccept command
fourtpa.tpacceptall:
  default: true
  description: Allows the player to use the /tpacceptall command
fourtpa.tpahere:
  default: true
  description: Allows the player to use the /tpahere command
fourtpa.tpaoffline:
  default: op
  description: Allows the player to use the /tpaoffline command
fourtpa.tpcancel:
  default: true
  description: Allows the player to use the /tpacancel command
fourtpa.tpdeny:
  default: true
  description: Allows the player to use the /tpadeny command
fourtpa.tpdenyall:
  default: true
  description: Allows the player to use the /tpdenyall command
```