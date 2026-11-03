#!/usr/bin/env bash
# Integration test: MCAN bridge relay via --client-for
#
# Topology:
#   Bridge1  NONE:50500          (no serial, TCP server only)
#   Bridge2  NONE:50501  <---->  Bridge1 at 127.0.0.1:50500
#
# Test 1: Frame injected into Bridge1 (50500) arrives at a poke listener on Bridge2 (50501)
# Test 2: Frame injected into Bridge2 (50501) arrives at a poke listener on Bridge1 (50500)
#
# Usage:
#   bash scripts/test-mcan-bridge-relay.sh [BRIDGE_JAR] [POKE_JAR]
#
# Defaults (relative to repo root):
#   BRIDGE_JAR=dist/mcan-tcp-bridge.jar
#   POKE_JAR=dist/marklin-poke.jar

set -euo pipefail

BRIDGE_JAR="${1:-dist/mcan-tcp-bridge.jar}"
POKE_JAR="${2:-dist/marklin-poke.jar}"

# CS2 PING frame (command 0x18, hash 0x0300)
CS2_PING="00 30 03 00 00 00 00 00 00 00 00 00 00"
# CdB ping for WeichenChef Magnet class 0x22 (broadcast, module 0)
CDB_PING="00 40 2A 80 00 00 00 00 00 00 00 00 00"

BRIDGE1_PORT=50500
BRIDGE2_PORT=50501

PASS=0
FAIL=0

log()  { echo "[$(date +%T)] $*"; }
pass() { log "PASS: $*"; ((PASS++)) || true; }
fail() { log "FAIL: $*"; ((FAIL++)) || true; }

cleanup() {
    log "Stopping bridges..."
    kill "$BRIDGE1_PID" "$BRIDGE2_PID" 2>/dev/null || true
    wait "$BRIDGE1_PID" "$BRIDGE2_PID" 2>/dev/null || true
}

# ── Sanity checks ─────────────────────────────────────────────────────────────

for jar in "$BRIDGE_JAR" "$POKE_JAR"; do
    if [[ ! -f "$jar" ]]; then
        echo "ERROR: JAR not found: $jar"
        echo "Build with: ant mcan-tcp-bridge-jar mcan-poke-jar"
        exit 1
    fi
done

# ── Start bridges ─────────────────────────────────────────────────────────────

log "Starting Bridge1 on port $BRIDGE1_PORT (no serial)..."
java -jar "$BRIDGE_JAR" \
    --port "NONE:$BRIDGE1_PORT" \
    > /tmp/bridge1.log 2>&1 &
BRIDGE1_PID=$!

log "Starting Bridge2 on port $BRIDGE2_PORT, upstream to Bridge1:$BRIDGE1_PORT..."
java -jar "$BRIDGE_JAR" \
    --port "NONE:$BRIDGE2_PORT" \
    --client-for "127.0.0.1:$BRIDGE1_PORT" \
    > /tmp/bridge2.log 2>&1 &
BRIDGE2_PID=$!

trap cleanup EXIT

# Wait for both bridges to bind their ports (up to 5s)
log "Waiting for bridges to start..."
for port in $BRIDGE1_PORT $BRIDGE2_PORT; do
    for i in $(seq 1 25); do
        if java -jar "$POKE_JAR" \
                --command "$CS2_PING" \
                --port "$port" \
                --watch 0 \
                > /dev/null 2>&1; then
            break
        fi
        sleep 0.2
        if [[ $i -eq 25 ]]; then
            log "ERROR: Bridge port $port did not come up in 5s"
            cat /tmp/bridge1.log
            cat /tmp/bridge2.log
            exit 1
        fi
    done
done

# Give the --client-for connection a moment to establish
sleep 1

log "Both bridges running."

# ── Test 1: Bridge1 → Bridge2 propagation ─────────────────────────────────────
#
# Inject CS2_PING into Bridge1 (50500).
# A poke listener on Bridge2 (50501) should receive it, because Bridge2 is a
# client of Bridge1: Bridge1 broadcasts received frames to all its TCP clients,
# and Bridge2's RemoteForwarder receives it and calls broadcastToClients() locally.

log ""
log "Test 1: Frame injected at Bridge1 ($BRIDGE1_PORT) should appear at Bridge2 ($BRIDGE2_PORT)"

# Start poke listener on Bridge2 (watch 3s, send nothing)
BRIDGE2_OUTPUT=$(java -jar "$POKE_JAR" \
    --command "$CS2_PING" \
    --host 127.0.0.1 \
    --port "$BRIDGE2_PORT" \
    --watch 3 2>&1) || true

# The poke itself sent a CS2_PING to Bridge2  -  we want to see it echoed back,
# but more importantly inject a *different* frame at Bridge1 via a separate
# listener that Bridge2 also sees.
#
# Simpler approach: inject CDB_PING at Bridge1 from a second connection;
# the same poke session on Bridge2 is already listening for 3s.
# Since poke is blocking, we run Bridge1 inject in background during Bridge2's watch.

WATCH_SECS=4

log "  Listening on Bridge2 for ${WATCH_SECS}s while injecting at Bridge1..."

# Background: inject CDB_PING into Bridge1 after 200ms (poke listener will be up)
(
    sleep 0.2
    java -jar "$POKE_JAR" \
        --command "$CDB_PING" \
        --host 127.0.0.1 \
        --port "$BRIDGE1_PORT" \
        --watch 1 \
        > /tmp/poke_inject_b1.log 2>&1
) &
INJECT_PID=$!

BRIDGE2_OUTPUT=$(java -jar "$POKE_JAR" \
    --command "$CS2_PING" \
    --host 127.0.0.1 \
    --port "$BRIDGE2_PORT" \
    --watch "$WATCH_SECS" 2>&1) || true

wait "$INJECT_PID" 2>/dev/null || true

log "  Bridge2 poke output:"
echo "$BRIDGE2_OUTPUT" | sed 's/^/    /'

# CDB_PING bytes: 00 40 2A 80  -  check first 4 bytes as hex in output
if echo "$BRIDGE2_OUTPUT" | grep -qi "00 40 2A 80\|004 02a 080\|00 40 2a 80"; then
    pass "CdB ping injected at Bridge1 appeared at Bridge2 listener"
else
    fail "CdB ping injected at Bridge1 did NOT appear at Bridge2 listener"
    log "  Bridge1 log:"
    cat /tmp/bridge1.log | tail -20 | sed 's/^/    /'
    log "  Bridge2 log:"
    cat /tmp/bridge2.log | tail -20 | sed 's/^/    /'
fi

# ── Test 2: Bridge2 → Bridge1 propagation ─────────────────────────────────────
#
# Inject CDB_PING at Bridge2 (50501).
# Bridge2's ClientHandler calls broadcastToClients() which includes
# RemoteForwarder.send() to Bridge1. Bridge1 receives it and broadcasts
# to its local TCP clients. A poke listener on Bridge1 (50500) should see it.

log ""
log "Test 2: Frame injected at Bridge2 ($BRIDGE2_PORT) should appear at Bridge1 ($BRIDGE1_PORT)"

(
    sleep 0.2
    java -jar "$POKE_JAR" \
        --command "$CDB_PING" \
        --host 127.0.0.1 \
        --port "$BRIDGE2_PORT" \
        --watch 1 \
        > /tmp/poke_inject_b2.log 2>&1
) &
INJECT2_PID=$!

BRIDGE1_OUTPUT=$(java -jar "$POKE_JAR" \
    --command "$CS2_PING" \
    --host 127.0.0.1 \
    --port "$BRIDGE1_PORT" \
    --watch "$WATCH_SECS" 2>&1) || true

wait "$INJECT2_PID" 2>/dev/null || true

log "  Bridge1 poke output:"
echo "$BRIDGE1_OUTPUT" | sed 's/^/    /'

if echo "$BRIDGE1_OUTPUT" | grep -qi "00 40 2a 80"; then
    pass "CdB ping injected at Bridge2 appeared at Bridge1 listener"
else
    fail "CdB ping injected at Bridge2 did NOT appear at Bridge1 listener"
    log "  Bridge1 log:"
    cat /tmp/bridge1.log | tail -20 | sed 's/^/    /'
    log "  Bridge2 log:"
    cat /tmp/bridge2.log | tail -20 | sed 's/^/    /'
fi

# ── Summary ───────────────────────────────────────────────────────────────────

log ""
log "Results: $PASS passed, $FAIL failed"
log "Bridge1 log: /tmp/bridge1.log"
log "Bridge2 log: /tmp/bridge2.log"

if [[ $FAIL -gt 0 ]]; then
    exit 1
fi
