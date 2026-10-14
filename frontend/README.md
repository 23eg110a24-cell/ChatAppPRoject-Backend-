# ChatApp Frontend

React (Vite) client for the ChatApp Spring Boot backend: login and registration, channels and
direct messages, live messages, typing indicators and online presence over WebSocket (STOMP).

Backend repository: https://github.com/23eg110a24-cell/ChatAppPRoject-Backend-

## Run locally

1. Start the backend (`mvn spring-boot:run` in `backend/`), which listens on `http://localhost:8080`.
2. In this folder:

```bash
npm install
cp .env.example .env     # on Windows PowerShell: Copy-Item .env.example .env
npm run dev
```

Open http://localhost:5173. The backend seeds three demo accounts on an empty database:
`ada`, `grace` and `linus`, all with the password `password123`.

## Configuration

| Variable | Purpose |
| --- | --- |
| `VITE_API_URL` | Public base URL of the backend, without a trailing slash. Defaults to `http://localhost:8080`. |

The WebSocket URL is derived from it (`http` becomes `ws`, `https` becomes `wss`, path `/ws`).

## Deploy

Any static host works. On Vercel: import this repository, keep the Vite defaults, and add the
environment variable `VITE_API_URL` pointing at the deployed backend. Then add the Vercel URL to
`chatapp.cors.allowed-origins` in the backend's `application.yml` and redeploy the backend.

## Project layout

```
src/
  api.js                 REST client and token storage
  socket.js              STOMP connection
  utils.js               time formatting and room helpers
  App.jsx                session handling (login page or chat)
  components/
    AuthPage.jsx         sign in / register
    Chat.jsx             state, REST loading and WebSocket wiring
    Sidebar.jsx          profile, search, channel and DM lists
    ChatPane.jsx         message list, typing line, composer
    NewChatModal.jsx     people picker and channel browser
```
