import React, { useState, useEffect, useCallback, Suspense, lazy } from "react";
import { createRoot } from "react-dom/client";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  useNavigate,
  useLocation,
} from "react-router-dom";
import { Toaster, toast } from "sonner";
import { Loader2 } from "lucide-react";
import "./styles/global.css";

import { SESSION_KEY } from "./api/client";
import { getMe } from "./api/auth";

import { Header } from "./components/common/Header";
import { ErrorBoundary } from "./components/common/ErrorBoundary";
import { AuthScreen } from "./features/auth/AuthScreen";

// Lazy-loaded feature modules for code splitting
const StudentBooking = lazy(() =>
  import("./features/student/StudentBooking").then((m) => ({ default: m.StudentBooking }))
);
const StudentBookings = lazy(() =>
  import("./features/student/StudentBookings").then((m) => ({ default: m.StudentBookings }))
);
const StudentProfile = lazy(() =>
  import("./features/student/StudentProfile").then((m) => ({ default: m.StudentProfile }))
);

const ProfessorSchedule = lazy(() =>
  import("./features/professor/ProfessorSchedule").then((m) => ({ default: m.ProfessorSchedule }))
);
const CreateSlot = lazy(() =>
  import("./features/professor/CreateSlot").then((m) => ({ default: m.CreateSlot }))
);

const AdminOverview = lazy(() =>
  import("./features/admin/AdminOverview").then((m) => ({ default: m.AdminOverview }))
);
const AdminUsers = lazy(() =>
  import("./features/admin/AdminUsers").then((m) => ({ default: m.AdminUsers }))
);

function getRoleHome(role) {
  if (role === "STUDENT") return "/student/book";
  if (role === "PROFESSOR") return "/professor/schedule";
  if (role === "ADMIN") return "/admin/overview";
  return "/login";
}

function LoadingFallback() {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "280px",
        gap: "10px",
        color: "var(--text-muted)",
      }}
    >
      <Loader2 className="animate-spin" size={22} aria-hidden="true" />
      <span>Loading…</span>
    </div>
  );
}

function RequireRole({ session, allowedRole, children }) {
  if (!session?.token) {
    return <Navigate to="/login" replace />;
  }
  const userRole = session?.user?.role;
  if (allowedRole && userRole !== allowedRole) {
    return <Navigate to={getRoleHome(userRole)} replace />;
  }
  return children;
}

function AppContent({ session, setSession, notify }) {
  const navigate = useNavigate();
  const location = useLocation();
  const role = session?.user?.role;

  const handleLogout = useCallback(() => {
    localStorage.removeItem(SESSION_KEY);
    setSession(null);
    notify("Signed out successfully.", "info");
    navigate("/login", { replace: true });
  }, [navigate, notify, setSession]);

  // Listen for session expiry event
  useEffect(() => {
    const handleExpired = () => {
      localStorage.removeItem(SESSION_KEY);
      setSession(null);
      notify("Your session has expired. Please sign in again.", "error");
      navigate("/login", { replace: true });
    };

    window.addEventListener("ohs:session-expired", handleExpired);
    return () => window.removeEventListener("ohs:session-expired", handleExpired);
  }, [navigate, notify, setSession]);

  const handleAuthSuccess = (newSession) => {
    setSession(newSession);
    const target = getRoleHome(newSession?.user?.role);
    navigate(target, { replace: true });
  };

  const isAuthRoute = location.pathname === "/login" || location.pathname === "/register";

  return (
    <>
      <Toaster position="bottom-right" richColors theme="dark" />

      {session?.token && !isAuthRoute && (
        <Header session={session} onLogout={handleLogout} />
      )}

      <main className={session?.token && !isAuthRoute ? "app-shell" : ""} id="main-content">
        <Suspense fallback={<LoadingFallback />}>
          <Routes>
            {/* Public Auth Routes */}
            <Route
              path="/login"
              element={
                session?.token ? (
                  <Navigate to={getRoleHome(role)} replace />
                ) : (
                  <AuthScreen onSession={handleAuthSuccess} notify={notify} />
                )
              }
            />
            <Route
              path="/register"
              element={
                session?.token ? (
                  <Navigate to={getRoleHome(role)} replace />
                ) : (
                  <AuthScreen onSession={handleAuthSuccess} notify={notify} />
                )
              }
            />

            {/* Root redirect */}
            <Route
              path="/"
              element={
                session?.token ? (
                  <Navigate to={getRoleHome(role)} replace />
                ) : (
                  <Navigate to="/login" replace />
                )
              }
            />

            {/* Student Portal Routes */}
            <Route
              path="/student"
              element={
                <RequireRole session={session} allowedRole="STUDENT">
                  <Navigate to="/student/book" replace />
                </RequireRole>
              }
            />
            <Route
              path="/student/book"
              element={
                <RequireRole session={session} allowedRole="STUDENT">
                  <StudentBooking token={session?.token} notify={notify} />
                </RequireRole>
              }
            />
            <Route
              path="/student/bookings"
              element={
                <RequireRole session={session} allowedRole="STUDENT">
                  <StudentBookings token={session?.token} notify={notify} />
                </RequireRole>
              }
            />
            <Route
              path="/student/profile"
              element={
                <RequireRole session={session} allowedRole="STUDENT">
                  <StudentProfile
                    token={session?.token}
                    user={session?.user}
                    notify={notify}
                  />
                </RequireRole>
              }
            />

            {/* Professor Workspace Routes */}
            <Route
              path="/professor"
              element={
                <RequireRole session={session} allowedRole="PROFESSOR">
                  <Navigate to="/professor/schedule" replace />
                </RequireRole>
              }
            />
            <Route
              path="/professor/schedule"
              element={
                <RequireRole session={session} allowedRole="PROFESSOR">
                  <ProfessorSchedule
                    token={session?.token}
                    notify={notify}
                    onGoToCreate={() => navigate("/professor/create-slot")}
                  />
                </RequireRole>
              }
            />
            <Route
              path="/professor/create-slot"
              element={
                <RequireRole session={session} allowedRole="PROFESSOR">
                  <CreateSlot
                    token={session?.token}
                    notify={notify}
                    onSlotCreated={() => navigate("/professor/schedule")}
                    onCancel={() => navigate("/professor/schedule")}
                  />
                </RequireRole>
              }
            />

            {/* Admin Management Routes */}
            <Route
              path="/admin"
              element={
                <RequireRole session={session} allowedRole="ADMIN">
                  <Navigate to="/admin/overview" replace />
                </RequireRole>
              }
            />
            <Route
              path="/admin/overview"
              element={
                <RequireRole session={session} allowedRole="ADMIN">
                  <AdminOverview token={session?.token} notify={notify} />
                </RequireRole>
              }
            />
            <Route
              path="/admin/users"
              element={
                <RequireRole session={session} allowedRole="ADMIN">
                  <AdminUsers
                    token={session?.token}
                    currentUser={session?.user}
                    notify={notify}
                  />
                </RequireRole>
              }
            />

            {/* Catch-all 404 / Unknown route redirect */}
            <Route
              path="*"
              element={
                session?.token ? (
                  <Navigate to={getRoleHome(role)} replace />
                ) : (
                  <Navigate to="/login" replace />
                )
              }
            />
          </Routes>
        </Suspense>
      </main>
    </>
  );
}

function App() {
  const [session, setSession] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(SESSION_KEY));
    } catch {
      return null;
    }
  });

  // Unified toast notifier using Sonner
  const notify = useCallback((message, kind = "success") => {
    if (kind === "error") {
      toast.error(message);
    } else if (kind === "warning") {
      toast.warning(message);
    } else if (kind === "info") {
      toast.info(message);
    } else {
      toast.success(message);
    }
  }, []);

  // Validate and refresh active user session on startup
  useEffect(() => {
    if (session?.token) {
      getMe(session.token)
        .then((user) => {
          const next = { ...session, user };
          localStorage.setItem(SESSION_KEY, JSON.stringify(next));
          setSession(next);
        })
        .catch((err) => {
          // Only clear stored session on confirmed 401 Unauthorized
          if (err?.status === 401 || err?.message?.includes("session has expired")) {
            localStorage.removeItem(SESSION_KEY);
            setSession(null);
          } else {
            // Network outage or temporary server unavailability: preserve valid session
            notify("Could not verify session with server. Keeping offline session active.", "warning");
          }
        });
    }
  }, []);

  return (
    <BrowserRouter>
      <ErrorBoundary>
        <AppContent session={session} setSession={setSession} notify={notify} />
      </ErrorBoundary>
    </BrowserRouter>
  );
}

const rootElement = document.getElementById("root");
if (rootElement) {
  createRoot(rootElement).render(<App />);
}
