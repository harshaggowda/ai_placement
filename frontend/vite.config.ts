import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev server proxies /api to the API Gateway so the browser only ever talks to one origin,
// mirroring production where the gateway is the single public entry point.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: process.env.VITE_API_GATEWAY_URL ?? "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
