import React, { useState, useEffect, useCallback } from "react";
import { createRoot } from "react-dom/client";
import { Toaster, toast } from "sonner";
import "./styles/global.css";

import { SESSION_KEY } from "./api/client";
import { getMe } from "./api/auth";

import { Header } from "./components/common/Header";
import { AuthScreen } from "./features/auth/AuthScreen";

import { StudentBooking } from "./features/student/StudentBooking";
import { StudentBookings } from "./features/student/StudentBookings";
import { StudentProfile } from "./features/student/StudentProfile";

import { ProfessorSchedule } from "./features/professor/ProfessorSchedule";
import { CreateSlot } from "./features/professor/CreateSlot";

import { AdminOverview } from "./features/admin/AdminOverview";
import { AdminUsers } from "./features/admin/AdminUsers";

function App() {
  const [session, setSession] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(SESSION_KEY));
    } catch {
      return null;
    }
  });

  const role = session?.user?.role;
  const defaultTab =
    role === "STUDENT" ? "book" : role === "PROFESSOR" ? "schedule" : "overview";
  const [tab, setTab] = useState(defaultTab);

  // Sync tab if role changes
  useEffect(() => {
    if (role === "STUDENT") setTab("book");
    else if (role === "PROFESSOR") setTab("schedule");
    else if (role === "ADMIN") setTab("overview");
  }, [role]);

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

  // Listen for session expiry event
  useEffect(() => {
    const handleExpired = () => {
      localStorage.removeItem(SESSION_KEY);
      setSession(null);
      notify("Your session has expired. Please sign in again.", "error");
    };

    window.addEventListener("ohs:session-expired", handleExpired);
    return () => window.removeEventListener("ohs:session-expired", handleExpired);
  }, [notify]);

  // Validate and refresh active user session on startup
  useEffect(() => {
    if (session?.token) {
      getMe(session.token)
        .then((user) => {
          const next = { ...session, user };
          localStorage.setItem(SESSION_KEY, JSON.stringify(next));
          setSession(next);
        })
        .catch(() => {
          localStorage.removeItem(SESSION_KEY);
          setSession(null);
        });
    }
  }, []);

  if (!session?.token) {
    return (
      <>
        <Toaster position="bottom-right" richColors theme="dark" />
        <AuthScreen onSession={setSession} notify={notify} />
      </>
    );
  }

  const handleLogout = () => {
    localStorage.removeItem(SESSION_KEY);
    setSession(null);
    notify("Signed out successfully.", "info");
  };

  // Determine current active view based on role and tab
  let view = null;
  if (role === "STUDENT") {
    if (tab === "bookings") {
      view = <StudentBookings token={session.token} notify={notify} />;
    } else if (tab === "profile") {
      view = <StudentProfile token={session.token} notify={notify} />;
    } else {
      view = <StudentBooking token={session.token} notify={notify} />;
    }
  } else if (role === "PROFESSOR") {
    if (tab === "create") {
      view = (
        <CreateSlot
          token={session.token}
          notify={notify}
          onSlotCreated={() => setTab("schedule")}
          onCancel={() => setTab("schedule")}
        />
      );
    } else {
      view = (
        <ProfessorSchedule
          token={session.token}
          notify={notify}
          onGoToCreate={() => setTab("create")}
        />
      );
    }
  } else if (role === "ADMIN") {
    if (tab === "users") {
      view = <AdminUsers token={session.token} notify={notify} />;
    } else {
      view = <AdminOverview token={session.token} notify={notify} />;
    }
  }

  return (
    <>
      <Toaster position="bottom-right" richColors theme="dark" />
      <Header
        session={session}
        tab={tab}
        setTab={setTab}
        onLogout={handleLogout}
      />
      <main className="app-shell" id="main-content">
        {view}
      </main>
    </>
  );
}

const rootElement = document.getElementById("root");
if (rootElement) {
  createRoot(rootElement).render(<App />);
}
