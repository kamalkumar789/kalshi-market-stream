# Architecture

## Core Terms

### 1) Series
A **Series** is a top-level category on Kalshi that groups related daily events.

In this project, series are configured manually in `application.properties`:

```properties
kalshi.poll.seriesTickers=KXHIGHNY,KXHIGHLAX
```

Meaning:
- `KXHIGHNY` → Highest temperature in New York
- `KXHIGHLAX` → Highest temperature in Los Angeles

Series link example:
https://kalshi.com/markets/kxhighlax/highest-temperature-in-los-angeles/kxhighlax-26feb13

You can update the `kalshi.poll.seriesTickers` value to track different series.

---

### 2) Events
An **Event** is a single-day instance inside a series.

Example:
- Series: `KXHIGHLAX`
- Event: `KXHIGHLAX-26FEB13`

Events are fetched and stored in the database during polling.

---

### 3) Markets
A **Market** belongs to an event and represents one possible outcome or range for that day.

Example:
For event `KXHIGHLAX-26FEB13`, markets may represent ranges like:
- 32–33
- 34–35
- 40–41

Each market has a unique market ticker returned by the Kalshi API.

---

### 4) Market Snapshots
A **Market Snapshot** is a stored record of market values at a specific point in time.

Snapshots are saved repeatedly during polling to track how values change over time.



---

## Database Schema

The system uses four tables:

- `event`
- `market`
- `market_snapshot`
- `market_snapshot_latency`

The first three tables directly map to the core concepts described above  
(Events, Markets, and Market Snapshots).

The schema diagram below shows only the important fields required by the system.  
Not every Kalshi field is stored — only the data needed for polling, tracking, and analysis.

![Database Schema](/docs/images/dbschema.png)

The additional table `market_snapshot_latency` stores latency measurements for markets,
as required by **Question 2 in the task**. This table is used only for tracking polling
latency and performance metrics.

---

## High-Level System Architecture

![System Architecture](/docs/images/systemarc.png)


---

## Concurrency Model (Per-Series Polling)

Each configured **series** is polled independently using a dedicated thread from a thread pool.

Why:
- Series polling should not block each other.
- If one series request is slow (network/API delay), it should not delay polling of other series.
- Running per-series tasks in parallel prevents overlap/waiting where one series must finish before the next starts.

Current setup:
- Two series are configured (`KXHIGHNY`, `KXHIGHLAX`)
- The thread pool is sized to **2 threads**, so both series can poll concurrently (as shown in the diagram).


---

## Project Structure

```
src/main/java/com/kamal/kalshi_market_stream
├── client        → Kalshi API client (WebClient wrapper)
├── scheduler     → Polling scheduler logic
├── services      → Business logic layer
├── controllers   → REST API endpoints
├── repositories  → Database access layer (Spring Data)
├── entities      → JPA database entities
├── dtos          → API data models
├── config        → Thread pool + shared bean configuration
├── utils         → Signals engine and helper utilities
```

The `config` package contains infrastructure configuration such as:

- Thread pool setup for per-series polling
- Shared beans (e.g., WebClient, executors)

The structure follows a standard layered Spring Boot architecture.


---

## External API (Kalshi)

This system pulls market data from the official Kalshi API:

👉 https://docs.kalshi.com/api-reference/events/get-event

The poller continuously calls Kalshi’s **Get Event** endpoint to fetch markets, and its details of a particular event, then stores only the required fields in the local database.

All market snapshots and latency measurements originate from this upstream API.

---

## Backend APIs (This System)

The backend exposes REST APIs for the frontend to read stored market data.

All endpoints are CORS-enabled and designed for local frontend usage.

---

### 1) List Markets by Event

Returns all markets for an event.

```
GET /api/events/{eventTicker}/markets
```

Optional filter:

```
?status=active
```

Example:

```bash
curl "http://localhost:8080/api/events/KXHIGHLAX-26FEB13/markets"
```

Response:

```json
[
  {
    "marketTicker": "...",
    "title": "...",
    "subtitle": "...",
    "status": "active"
  }
]
```

---

### 2) Market Snapshots (Time Range)

Returns historical snapshot points for a market.

```
GET /api/events/{eventTicker}/markets/{marketTicker}/snapshots
```

Query params:

- `status` (default: active)
- `from` (yyyy-MM-dd HH:mm)
- `to` (yyyy-MM-dd HH:mm)
- `limit` (default: 20)

Example:

```bash
curl "http://localhost:8080/api/events/KXHIGHLAX-26FEB13/markets/KXHIGHLAX-26FEB13-40/snapshots?limit=50"
```

Response:

```json
[
  {
    "observedAt": "...",
    "yesBid": 42,
    "noBid": 58,
    "subtitle": "...",
    "status": "active",
    "eventTicker": "..."
  }
]
```

---

### 3) Market Latency Metrics

Returns recent latency measurements for a market.

```
GET /api/markets/{marketTicker}/latencies
```

Optional:

```
?limit=50
```

Example:

```bash
curl "http://localhost:8080/api/markets/KXHIGHLAX-26FEB13-40/latencies"
```

Response:

```json
[
  {
    "snapshotId": 123,
    "exchangeTs": "...",
    "receivedTs": "...",
    "processedTs": "...",
    "networkLatencyMs": 20,
    "processingLatencyMs": 3,
    "endToEndLatencyMs": 23
  }
]
```

---

These APIs are read-only and expose stored polling data for frontend visualization and analysis.

---

## Output / Results

### Real-time vs Frontend
This system does **not** stream real-time logs directly to the frontend.

- Real-time updates are written into:
  - the **database** (snapshots + latency records)
  - the **EC2 service logs** (view using the commands in `README.md`, e.g. `journalctl -u kalshi-market-stream -f`)

The frontend reads data using **HTTP APIs only**, so it is used mainly for:

- **Question 1:** viewing how a market reacted over time for an event  
- **Question 2:** viewing stored latency history and performance metrics

---

### Frontend Output Screenshot

![Frontend Output](images/output-frontend.png)

---

### Backend Logs Screenshot

![Backend Logs](images/output-logs.png)

---

## Signals (Question 3)

The `trend` field shown in backend logs comes from the signal engine.

Signals are implemented in:

`src/main/java/com/kamal/kalshi_market_stream/utils/Signals.java`

This logic corresponds to **Question 3** and calculates direction based on
fast vs slow moving averages:

- `UP`   → short-term average > long-term average
- `DOWN` → short-term average < long-term average
- `FLAT` → insufficient data or equal averages
