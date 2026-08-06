# DuckDNS updater

Keeps `<subdomain>.duckdns.org` pointing at this instance's current public IPv4,
so the deployment does not need an Elastic IP.

## Why this exists

An Elastic IP is billed at USD 0.005/hr — about USD 3.65/month — whether or not
it is attached to a running instance. That is more than twice what a stopped
instance costs in EBS, and the instance is expected to spend a good deal of time
stopped. Without a reserved address, AWS assigns a new public IPv4 on every
start; these units publish it.

Nothing downstream is aware the address moves. The TLS certificate is issued for
the name, and the APK is built against the name.

## Files

| File | Installed to | Purpose |
|---|---|---|
| `duckdns-update.sh` | `/usr/local/bin/` | One `curl` to the DuckDNS update API |
| `duckdns.service` | `/etc/systemd/system/` | Runs the script once |
| `duckdns.timer` | `/etc/systemd/system/` | 15s after boot, then every 5 minutes |
| `duckdns-park.service` | `/etc/systemd/system/` | Parks the record at `127.0.0.1` on shutdown |
| `config.example` | `/etc/duckdns/config` | Subdomain and token — **fill in, never commit** |

`duckdns-park.service` is the security half. A stopped instance returns its
public address to AWS, which reassigns it, while the DNS record still points
there — so the name resolves to a stranger's machine until the instance comes
back. Beyond misrouted traffic, Let's Encrypt's HTTP-01 challenge proves control
of whatever a name currently points at, so whoever holds that address could be
issued a valid certificate for this domain. Parking at `127.0.0.1` closes it.

## Install

```bash
sudo install -m 755 duckdns-update.sh /usr/local/bin/
sudo install -m 644 duckdns.service duckdns.timer duckdns-park.service /etc/systemd/system/

sudo install -d -m 700 /etc/duckdns
sudo install -m 600 config.example /etc/duckdns/config
sudo vi /etc/duckdns/config          # subdomain, and the token from duckdns.org

sudo systemctl daemon-reload
sudo systemctl enable --now duckdns.timer duckdns-park.service
```

## Check it

```bash
sudo /usr/local/bin/duckdns-update.sh    # prints where the record now points
systemctl list-timers duckdns.timer      # next run
dig +short <subdomain>.duckdns.org
```

To rehearse the shutdown path without rebooting, `sudo systemctl stop
duckdns-park.service` runs the same `ExecStop`; the record goes to `127.0.0.1`
and the timer puts it back within five minutes.
