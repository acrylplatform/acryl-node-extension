# acryl-node-extension
Extension for Acryl Node 

## Extension options
- anti-fork protection
- when the node starts, it sends message why the node will not generate blocks:
  - if mining disabled in config
  - a generating balance is less than 100 Acryl
  - the miner account has a smart contract
- notifies if the node mined block and its reward
- notifies about incoming Acryl
- notifies about changes of leased volume.

## How to install:
1. Download `node-extension-0.0.1.jar` to `/usr/share/acryl/lib/`:
    ```
    wget https://github.com/acrylplatform/acryl-node-extension/releases/download/v0.0.1/node-extension-0.0.1.jar -P /usr/share/acryl/lib/
    ```
2. Download official `scalaj-http_2.12-2.4.2.jar` from Maven Central to `/usr/share/acryl/lib/`:
    ```
    wget https://repo1.maven.org/maven2/org/scalaj/scalaj-http_2.12/2.4.2/scalaj-http_2.12-2.4.2.jar -P /usr/share/acryl/lib/
    ```
3. Add to `/etc/acryl/acryl.conf` (or `local.conf`):
    ```
    acryl.extensions = [
        "com.acrylplatform.extensions.Node"
    ]
    node-extension {
        # Local API key
        local-api-key = "acryl"
   
        webhook {
            # SPECIFY YOUR ENDPOINT
            url = "https://example.com/webhook/1234567890"
        }
    }
    ```
4. Restart the node

If node starts successfully, you will receive message about this.

## Notifications
By default the extension writes notifications to the node log file. In addition, you can specify any endpoint of notifications.

For example, you can use Telegram bot https://t.me/bullhorn_bot from https://integram.org/ team (add this bot and read its welcome message).

You can read the full list of properties in the [application.conf](src/main/resources/application.conf).
