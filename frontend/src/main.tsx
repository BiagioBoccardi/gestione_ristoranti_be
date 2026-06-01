import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { AuthProvider } from "./context/AuthContext";
import { ToastProvider } from "./context/ToastContext";
import ErrorBoundary from "./components/ErrorBoundary";
import App from "./App";
import "./index.css";

createRoot(document.getElementById("root") as HTMLElement).render(
  <StrictMode>
    <ErrorBoundary>
      <AuthProvider>
        <ToastProvider>
          <App />
        </ToastProvider>
      </AuthProvider>
    </ErrorBoundary>
  </StrictMode>
);