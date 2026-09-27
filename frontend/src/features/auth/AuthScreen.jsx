import React, { useState } from "react";
import { loginUser, registerUser } from "../../api/auth";
import { SESSION_KEY } from "../../api/client";
import { Button } from "../../components/ui/Button";
import { Input } from "../../components/ui/Input";
import { Select } from "../../components/ui/Select";
import { AlertCircle, Calendar, ShieldCheck, Clock } from "lucide-react";

export function AuthScreen({ onSession, notify }) {
  const [mode, setMode] = useState("login");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [login, setLogin] = useState({ email: "", password: "" });
  const [register, setRegister] = useState({
    fullName: "",
    email: "",
    password: "",
    role: "STUDENT",
    rollNo: "",
    yearOfStudy: "",
    department: "",
  });

  const update = (setter, key) => (event) => {
    const val = event.target.value;
    setter((old) => ({ ...old, [key]: val }));
  };

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      if (mode === "register") {
        await registerUser({
          ...register,
          yearOfStudy: register.role === "STUDENT" ? Number(register.yearOfStudy) : null,
          rollNo: register.role === "STUDENT" ? register.rollNo : null,
          department: register.role === "PROFESSOR" ? register.department : null,
        });
        notify?.("Account created successfully. Please sign in.", "success");
        setMode("login");
        setLogin({ email: register.email, password: register.password });
      } else {
        const result = await loginUser(login);
        const session = {
          token: result.accessToken,
          user: result.user,
          expiresIn: result.expiresIn,
        };
        localStorage.setItem(SESSION_KEY, JSON.stringify(session));
        onSession(session);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card-panel">
        <div className="brand" style={{ marginBottom: 24 }}>
          CAMPUS<span>OFFICE</span>HOURS
        </div>

        <p className="eyebrow">Academic Scheduling Portal</p>
        <h1 style={{ fontSize: "2rem", letterSpacing: "-0.03em", marginBottom: 8 }}>
          {mode === "login" ? "Welcome back." : "Create your account."}
        </h1>
        <p className="muted" style={{ marginBottom: 24 }}>
          Connect with faculty, reserve office hour slots, and track waitlists.
        </p>

        <div className="auth-tabs" role="tablist">
          <button
            type="button"
            role="tab"
            aria-selected={mode === "login"}
            className={`auth-tab-btn ${mode === "login" ? "active" : ""}`}
            onClick={() => { setMode("login"); setError(""); }}
          >
            Sign in
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={mode === "register"}
            className={`auth-tab-btn ${mode === "register" ? "active" : ""}`}
            onClick={() => { setMode("register"); setError(""); }}
          >
            Register
          </button>
        </div>

        <form onSubmit={handleSubmit} className="form-stack">
          {error && (
            <div className="form-error" id="auth-error-msg" role="alert">
              <AlertCircle size={16} aria-hidden="true" />
              <span>{error}</span>
            </div>
          )}

          {mode === "register" && (
            <>
              <div className="field">
                <label htmlFor="reg-fullName">Full name</label>
                <Input
                  id="reg-fullName"
                  value={register.fullName}
                  onChange={update(setRegister, "fullName")}
                  required
                  maxLength="150"
                  placeholder="e.g. Eleanor Vance"
                  autoComplete="name"
                />
              </div>

              <div className="field">
                <label htmlFor="reg-role">Role</label>
                <Select
                  id="reg-role"
                  value={register.role}
                  onChange={update(setRegister, "role")}
                >
                  <option value="STUDENT">Student</option>
                  <option value="PROFESSOR">Professor</option>
                </Select>
              </div>
            </>
          )}

          <div className="field">
            <label htmlFor="auth-email">University email</label>
            <Input
              id="auth-email"
              type="email"
              value={mode === "login" ? login.email : register.email}
              onChange={mode === "login" ? update(setLogin, "email") : update(setRegister, "email")}
              required
              placeholder="e.g. student@university.edu"
              autoComplete="email"
              spellCheck={false}
              aria-invalid={Boolean(error)}
              aria-describedby={error ? "auth-error-msg" : undefined}
            />
          </div>

          <div className="field">
            <label htmlFor="auth-password">Password</label>
            <Input
              id="auth-password"
              type="password"
              value={mode === "login" ? login.password : register.password}
              onChange={mode === "login" ? update(setLogin, "password") : update(setRegister, "password")}
              required
              minLength="8"
              placeholder="Minimum 8 characters"
              autoComplete={mode === "login" ? "current-password" : "new-password"}
              aria-invalid={Boolean(error)}
              aria-describedby={error ? "auth-error-msg" : undefined}
            />
          </div>

          {mode === "register" && register.role === "STUDENT" && (
            <div className="two-column" style={{ gap: 12 }}>
              <div className="field">
                <label htmlFor="reg-rollNo">Roll number</label>
                <Input
                  id="reg-rollNo"
                  value={register.rollNo}
                  onChange={update(setRegister, "rollNo")}
                  required
                  maxLength="50"
                  placeholder="CS-2024-01"
                />
              </div>
              <div className="field">
                <label htmlFor="reg-year">Year of study</label>
                <Input
                  id="reg-year"
                  type="number"
                  min="1"
                  max="8"
                  value={register.yearOfStudy}
                  onChange={update(setRegister, "yearOfStudy")}
                  required
                  placeholder="1-8"
                />
              </div>
            </div>
          )}

          {mode === "register" && register.role === "PROFESSOR" && (
            <div className="field">
              <label htmlFor="reg-department">Department</label>
              <Input
                id="reg-department"
                value={register.department}
                onChange={update(setRegister, "department")}
                required
                maxLength="100"
                placeholder="e.g. Computer Science"
              />
            </div>
          )}

          <Button
            type="submit"
            variant="primary"
            size="lg"
            loading={loading}
            className="btn-w100"
            style={{ marginTop: 8 }}
          >
            {mode === "login" ? "Sign in to portal" : "Create account"}
          </Button>
        </form>
      </section>

      <aside className="auth-aside">
        <div>
          <span className="eyebrow" style={{ color: "var(--primary)" }}>RELIABLE & ORDERED</span>
          <h2>
            Office hours,<br />
            <em>simplified.</em>
          </h2>
          <p className="muted" style={{ maxWidth: 360, fontSize: 16 }}>
            Direct booking with faculty members, automated waitlist promotion, and zero scheduling friction.
          </p>
          <div style={{ display: "grid", gap: 16, marginTop: 32 }}>
            <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
              <Calendar size={20} color="var(--primary)" aria-hidden="true" />
              <span style={{ fontSize: 14, color: "var(--text-secondary)" }}>Real-time professor availability</span>
            </div>
            <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
              <Clock size={20} color="var(--warning-text)" aria-hidden="true" />
              <span style={{ fontSize: 14, color: "var(--text-secondary)" }}>Automated queueing & waitlists</span>
            </div>
            <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
              <ShieldCheck size={20} color="var(--success-text)" aria-hidden="true" />
              <span style={{ fontSize: 14, color: "var(--text-secondary)" }}>Concurrency-safe atomic bookings</span>
            </div>
          </div>
        </div>
      </aside>
    </main>
  );
}
