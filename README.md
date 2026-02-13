# Kalshi Market Stream


## Recommended Reading Order

For architecture diagrams and full system design, start with:

👉 **`docs/ARCHITECTURE.md`**

After reviewing the architecture guide, return here and continue with the setup instructions.

## Overview

This project runs the Kalshi market streaming backend as a background service on an AWS EC2 instance.  
The frontend must be run locally and connected to the EC2 backend.

---

## Backend Deployment (AWS EC2 UBUNTU)

### Step 1 — Clone Repository

Install git if needed and clone:

```bash
sudo apt update
sudo apt install git -y

git clone https://github.com/kamalkumar789/kalshi-market-stream.git
cd kalshi-market-stream
```

---

### Step 2 — Run Setup Script


Run it:

```bash
chmod +x setup.sh
./setup.sh
```

The script will:

- Install Java and dependencies
- Create the database and user
- Build the application JAR
- Configure a system service
- Start the backend in background

---

### Step 3 — Open Ports in AWS Security Group

1. Open AWS Console → EC2  
2. Select your instance 
3. Go to **Security**  
4. Open the attached **Security Group**  
5. Edit inbound rules  

Add:

- HTTP → Port **80**
- Custom TCP → Port **8080**

Source: custom ip (recommended/tested)

---

### Step 4 — View Backend Logs

To confirm the backend is running:

```bash
journalctl -u kalshi-market-stream -f
```

If logs are streaming, the system is running correctly.

---

## Database Details

Created automatically by setup script:

```
DB_NAME = kalshi_db
DB_USER = postgres
DB_PASS = kamal
```

Check database inside EC2:

```bash
psql -U postgres
```

```sql
\c kalshi_db
\dt
\q
```

---

## Frontend Setup (Run Locally)

The frontend (`frontend.html`) is **not deployed** automatically.  
You must run it on your own machine.

Clone the repository locally and use `frontend.html` from the main directory.

---

### Update Backend URL in Frontend

In the frontend HTML file (baseUrl field):

Replace the base URL with your EC2 **Public DNS**:

```js
const baseUrl = "http://<your-ec2-public-ip>:8080";
```

Example format:

```js
const baseUrl = "http://ec2-54-123-45-67.compute.amazonaws.com:8080";
```

Now open the frontend locally.  
It will connect directly to your EC2 backend.

---

## Logs Command (Important)

```bash
journalctl -u kalshi-market-stream -f
```

This is the main command to monitor the running backend.


