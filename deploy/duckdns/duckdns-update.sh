#!/bin/bash
# Publishes this instance's current public IPv4 to DuckDNS.
#
# This is what makes an Elastic IP unnecessary. A reserved address is billed at
# USD 0.005/hr whether or not it is attached to a running instance — around USD
# 3.65/month, which is more than twice the cost of a stopped instance's disk.
# Without one, AWS hands out a different public address every time the instance
# starts, and this script is what tells DNS where to look.
#
# Nothing downstream cares that the address moved: the TLS certificate is issued
# for the name rather than the address, and the APK is built against the name.
# That indirection is the entire reason the name exists.
#
# Credentials come from /etc/duckdns/config, root-owned 0600 and never in git.
#
# Usage:
#   duckdns-update.sh              publish this instance's public address
#   duckdns-update.sh 127.0.0.1    park the record — see duckdns-park.service

set -euo pipefail

# shellcheck source=/dev/null
source /etc/duckdns/config

# Empty by default, and that is deliberate rather than an oversight: DuckDNS
# then records the source address of this request, which is by definition the
# address the outside world reaches this instance on. Asking the instance for
# its own address would return the private 172.31.x.x it sees on its interface,
# which is not routable and would point the domain at nothing.
target_ip="${1-}"

response=$(curl -fsS --max-time 30 \
  "https://www.duckdns.org/update?domains=${DUCKDNS_DOMAIN}&token=${DUCKDNS_TOKEN}&ip=${target_ip}")

# DuckDNS answers a bare "OK" or "KO", with HTTP 200 in both cases — so curl's
# exit status says only that the request was delivered, not that it worked.
if [ "$response" != "OK" ]; then
  echo "duckdns: update failed (response: ${response:-empty})" >&2
  exit 1
fi

echo "duckdns: ${DUCKDNS_DOMAIN}.duckdns.org -> ${target_ip:-this instance}"
