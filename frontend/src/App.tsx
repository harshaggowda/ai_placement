// Root component — placeholder shell. Routing, pages, and feature components are added under
// src/pages and src/components as the product is built. No business UI in the scaffold.
export default function App() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-2 bg-slate-50 text-slate-800">
      <h1 className="text-3xl font-semibold">CareerOS AI</h1>
      <p className="text-slate-500">Frontend scaffold — React + TypeScript + Vite + Tailwind.</p>
    </main>
  );
}
