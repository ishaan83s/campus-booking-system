import "bootstrap/dist/css/bootstrap.min.css";
import "./styles.css";
import { useCallback, useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
const SESSION_KEY = "ohs.session";
const formatDate = (value) => value ? new Intl.DateTimeFormat("en-US", { weekday: "short", month: "short", day: "numeric", year: "numeric" }).format(new Date(`${value.slice(0, 10)}T00:00:00`)) : "-";
const formatTime = (value) => value ? new Intl.DateTimeFormat("en-US", { hour: "numeric", minute: "2-digit" }).format(new Date(`1970-01-01T${value}`)) : "-";

async function api(path, { token, ...options } = {}) {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: { ...(options.body ? { "Content-Type": "application/json" } : {}), ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers },
  });
  if (response.status === 204) return null;
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message ?? `Request failed (${response.status})`);
  return body;
}

function Notice({ notice, onDismiss }) {
  if (!notice) return null;
  return <div className={`notice ${notice.kind}`} role="status"><span>{notice.message}</span><button onClick={onDismiss} aria-label="Dismiss">×</button></div>;
}

function Badge({ value }) { return <span className={`badge-soft badge-${String(value).toLowerCase()}`}>{value}</span>; }

function AuthScreen({ onSession }) {
  const [mode, setMode] = useState("login"); const [error, setError] = useState(""); const [loading, setLoading] = useState(false);
  const [login, setLogin] = useState({ email: "", password: "" });
  const [register, setRegister] = useState({ fullName: "", email: "", password: "", role: "STUDENT", rollNo: "", yearOfStudy: "", department: "" });
  const update = (setter, key) => (event) => setter((old) => ({ ...old, [key]: event.target.value }));
  async function submit(event) {
    event.preventDefault(); setError(""); setLoading(true);
    try {
      if (mode === "register") { await api("/api/auth/register", { method: "POST", body: JSON.stringify({ ...register, yearOfStudy: register.role === "STUDENT" ? Number(register.yearOfStudy) : null, rollNo: register.role === "STUDENT" ? register.rollNo : null, department: register.role === "PROFESSOR" ? register.department : null }) }); setMode("login"); setLogin({ email: register.email, password: register.password }); }
      else { const result = await api("/api/auth/login", { method: "POST", body: JSON.stringify(login) }); const session = { token: result.accessToken, user: result.user, expiresIn: result.expiresIn }; localStorage.setItem(SESSION_KEY, JSON.stringify(session)); onSession(session); }
    } catch (exception) { setError(exception.message); } finally { setLoading(false); }
  }
  return <main className="auth-page"><section className="auth-card"><div className="brand">OFFICE<span>HOURS</span></div><p className="eyebrow">SIMPLE, FOCUSED SCHEDULING</p><h1>{mode === "login" ? "Welcome back." : "Create your account."}</h1><p className="muted">Book time with your faculty without the back-and-forth.</p>
    <div className="auth-tabs"><button className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>Sign in</button><button className={mode === "register" ? "active" : ""} onClick={() => setMode("register")}>Register</button></div>
    <form onSubmit={submit} className="form-stack">
      {mode === "register" && <><Field label="Full name"><input value={register.fullName} onChange={update(setRegister, "fullName")} required maxLength="150" /></Field><Field label="Role"><select value={register.role} onChange={update(setRegister, "role")}><option value="STUDENT">Student</option><option value="PROFESSOR">Professor</option></select></Field></>}
      <Field label="College email"><input type="email" value={mode === "login" ? login.email : register.email} onChange={mode === "login" ? update(setLogin, "email") : update(setRegister, "email")} required /></Field>
      <Field label="Password"><input type="password" value={mode === "login" ? login.password : register.password} onChange={mode === "login" ? update(setLogin, "password") : update(setRegister, "password")} required minLength="8" /></Field>
      {mode === "register" && register.role === "STUDENT" && <><Field label="Roll number"><input value={register.rollNo} onChange={update(setRegister, "rollNo")} required maxLength="50" /></Field><Field label="Year of study"><input type="number" min="1" max="8" value={register.yearOfStudy} onChange={update(setRegister, "yearOfStudy")} required /></Field></>}
      {mode === "register" && register.role === "PROFESSOR" && <Field label="Department"><input value={register.department} onChange={update(setRegister, "department")} required maxLength="100" /></Field>}
      {error && <p className="form-error">{error}</p>}<button className="button button-dark" disabled={loading}>{loading ? "Please wait..." : mode === "login" ? "Sign in" : "Create account"}</button>
    </form></section><aside className="auth-aside"><div><span className="tiny-label">OFFICE HOURS / 01</span><h2>Make time<br /><em>matter.</em></h2><p>One clear view of availability, bookings, and waitlists.</p></div></aside></main>;
}

function Field({ label, children }) { return <label className="field"><span>{label}</span>{children}</label>; }

function Header({ session, tab, setTab, onLogout }) {
  const role = session.user.role;
  const tabs = role === "STUDENT" ? [["book", "Book a slot"], ["bookings", "My bookings"], ["profile", "Profile"]] : role === "PROFESSOR" ? [["schedule", "My schedule"], ["create", "Add a slot"]] : [["overview", "Overview"], ["users", "Users"]];
  return <><header className="app-header"><div className="brand">OFFICE<span>HOURS</span></div><nav>{tabs.map(([key, title]) => <button key={key} onClick={() => setTab(key)} className={key === tab ? "active" : ""}>{title}</button>)}</nav><div className="account"><span className="avatar">{session.user.fullName?.[0]}</span><span className="account-name">{session.user.fullName}</span><Badge value={role} /><button className="sign-out" onClick={onLogout}>Sign out</button></div></header></>;
}

function StudentBooking({ token, notify }) {
  const [professors, setProfessors] = useState([]); const [professor, setProfessor] = useState(""); const [slots, setSlots] = useState([]); const [selected, setSelected] = useState(null); const [reason, setReason] = useState(""); const [saving, setSaving] = useState(false);
  useEffect(() => { api("/api/professors?page=0&size=50", { token }).then((page) => { const rows = page.content ?? []; setProfessors(rows); setProfessor(rows[0]?.professorId ? String(rows[0].professorId) : ""); }).catch((e) => notify(e.message, "error")); }, [token, notify]);
  const loadSlots = useCallback(() => { if (!professor) return; api(`/api/professors/${professor}/slots`, { token }).then((data) => setSlots(data.slots ?? [])).catch((e) => notify(e.message, "error")); }, [professor, token, notify]);
  useEffect(() => { loadSlots(); }, [loadSlots]);
  async function book() { if (!selected) return; setSaving(true); try { const result = await api("/api/bookings", { token, method: "POST", body: JSON.stringify({ slotId: selected.slotId }) }); notify(result.bookingId ? "Booking confirmed." : `You are waitlisted at position ${result.position}.`, "success"); setSelected(null); setReason(""); loadSlots(); } catch (e) { notify(e.message, "error"); } finally { setSaving(false); } }
  const chosenProfessor = professors.find((item) => String(item.professorId) === professor);
  return <section className="page-grid"><div><PageHeading eyebrow="STUDENT DASHBOARD" title="Book office hours." subtitle="Choose a professor and select a time that works for you." /><Field label="Professor"><select value={professor} onChange={(e) => setProfessor(e.target.value)}>{professors.map((item) => <option key={item.professorId} value={item.professorId}>{item.fullName} - {item.department}</option>)}</select></Field><div className="slot-list">{slots.map((slot) => <button className={`slot-row ${slot.status !== "OPEN" ? "disabled" : ""} ${selected?.slotId === slot.slotId ? "selected" : ""}`} disabled={slot.status !== "OPEN"} onClick={() => setSelected(slot)} key={slot.slotId}><div><strong>{formatDate(slot.slotDate)}</strong><span>{formatTime(slot.startTime)} - {formatTime(slot.endTime)}</span></div><div><small>{slot.capacity - slot.bookedCount} spaces left</small><Badge value={slot.status} /></div></button>)}{!slots.length && <Empty text="No future slots are available for this professor." />}</div></div>
    <aside className="side-card"><span className="tiny-label">SELECTED TIME</span>{selected ? <><h2>{formatDate(selected.slotDate)}</h2><p className="slot-time">{formatTime(selected.startTime)} - {formatTime(selected.endTime)}</p><p className="muted">with {chosenProfessor?.fullName}</p><Field label="Reason for visit (optional)"><textarea value={reason} onChange={(e) => setReason(e.target.value)} placeholder="What would you like to discuss?" maxLength="500" /></Field><p className="helper">This note stays in your browser; the current backend booking API accepts only a slot ID.</p><button className="button button-dark w-100" onClick={book} disabled={saving}>{saving ? "Booking..." : "Confirm booking"}</button></> : <Empty text="Select an available time to review and confirm it here." />}</aside></section>;
}

function StudentBookings({ token, notify }) {
  const [history, setHistory] = useState({ bookings: [], waitlistEntries: [] });
  const refresh = useCallback(() => api("/api/bookings/me", { token }).then(setHistory).catch((e) => notify(e.message, "error")), [token, notify]); useEffect(() => { refresh(); }, [refresh]);
  async function cancel(id) { try { await api(`/api/bookings/${id}`, { token, method: "DELETE" }); notify("Booking cancelled.", "success"); refresh(); } catch (e) { notify(e.message, "error"); } }
  async function leave(id) { try { await api(`/api/bookings/waitlist/${id}`, { token, method: "DELETE" }); notify("Left waitlist.", "success"); refresh(); } catch (e) { notify(e.message, "error"); } }
  return <section><PageHeading eyebrow="STUDENT DASHBOARD" title="Your bookings." subtitle="Review confirmed meetings and waitlist positions." /><div className="two-column"><Card title="Confirmed & history"><Table headings={["Professor", "When", "Status", ""]}>{history.bookings.map((booking) => <tr key={booking.bookingId}><td>{booking.professorName}</td><td>{formatDate(booking.slotDate)}<br /><span className="muted">{formatTime(booking.startTime)}</span></td><td><Badge value={booking.status} /></td><td>{booking.status === "BOOKED" && <button className="link-button danger" onClick={() => cancel(booking.bookingId)}>Cancel</button>}</td></tr>)}</Table>{!history.bookings.length && <Empty text="You have no bookings yet." />}</Card><Card title="Waitlist"><Table headings={["Slot", "Position", "Status", ""]}>{history.waitlistEntries.map((entry) => <tr key={entry.waitlistId}><td>Slot #{entry.slotId}</td><td>{entry.position}</td><td><Badge value={entry.status} /></td><td><button className="link-button danger" onClick={() => leave(entry.waitlistId)}>Leave</button></td></tr>)}</Table>{!history.waitlistEntries.length && <Empty text="You are not waiting for any slots." />}</Card></div></section>;
}

function StudentProfile({ token, notify }) {
  const [profile, setProfile] = useState(null); const [form, setForm] = useState({ rollNo: "", yearOfStudy: "" });
  useEffect(() => { api("/api/students/me", { token }).then((data) => { setProfile(data); setForm({ rollNo: data.rollNo, yearOfStudy: data.yearOfStudy ?? "" }); }).catch((e) => notify(e.message, "error")); }, [token, notify]);
  async function save(event) { event.preventDefault(); try { const result = await api("/api/students/me", { token, method: "PUT", body: JSON.stringify({ rollNo: form.rollNo, yearOfStudy: form.yearOfStudy ? Number(form.yearOfStudy) : null }) }); setProfile(result); notify("Profile updated.", "success"); } catch (e) { notify(e.message, "error"); } }
  return <section className="narrow"><PageHeading eyebrow="STUDENT DASHBOARD" title="Your profile." subtitle="Keep your course details up to date." /><form className="card form-stack" onSubmit={save}>{profile && <p className="profile-name">{profile.fullName}</p>}<Field label="Roll number"><input value={form.rollNo} onChange={(e) => setForm({ ...form, rollNo: e.target.value })} required maxLength="50" /></Field><Field label="Year of study"><input value={form.yearOfStudy} onChange={(e) => setForm({ ...form, yearOfStudy: e.target.value })} type="number" min="1" max="8" /></Field><button className="button button-dark">Save changes</button></form></section>;
}

function ProfessorSchedule({ token, notify }) {
  const [slots, setSlots] = useState([]); const [roster, setRoster] = useState(null); const [editing, setEditing] = useState(null); const refresh = useCallback(() => api("/api/professors/slots/me", { token }).then(setSlots).catch((e) => notify(e.message, "error")), [token, notify]); useEffect(() => { refresh(); }, [refresh]);
  async function cancel(id) { try { await api(`/api/professors/slots/${id}`, { token, method: "DELETE" }); notify("Slot cancelled.", "success"); refresh(); } catch (e) { notify(e.message, "error"); } }
  async function viewRoster(id) { try { setRoster(await api(`/api/professors/slots/${id}/bookings`, { token })); } catch (e) { notify(e.message, "error"); } }
  async function updateSlot(event) { event.preventDefault(); try { await api(`/api/professors/slots/${editing.slotId}`, { token, method: "PUT", body: JSON.stringify({ slotDate: editing.slotDate, startTime: editing.startTime, endTime: editing.endTime, capacity: Number(editing.capacity) }) }); notify("Slot updated.", "success"); setEditing(null); refresh(); } catch (e) { notify(e.message, "error"); } }
  return <section><PageHeading eyebrow="PROFESSOR DASHBOARD" title="Your schedule." subtitle="Manage your upcoming office-hour windows." /><Card title="Upcoming slots"><Table headings={["Date", "Time", "Capacity", "Status", ""]}>{slots.map((slot) => <tr key={slot.slotId}><td>{formatDate(slot.slotDate)}</td><td>{formatTime(slot.startTime)} - {formatTime(slot.endTime)}</td><td>{slot.bookedCount}/{slot.capacity}</td><td><Badge value={slot.status} /></td><td><button className="link-button" onClick={() => viewRoster(slot.slotId)}>Roster</button>{slot.status !== "CANCELLED" && <><button className="link-button ms-3" onClick={() => setEditing({ ...slot })}>Edit</button><button className="link-button danger ms-3" onClick={() => cancel(slot.slotId)}>Cancel</button></>}</td></tr>)}</Table>{!slots.length && <Empty text="No upcoming slots. Add one to start accepting bookings." />}</Card>{editing && <Card title={`Edit slot #${editing.slotId}`}><form className="form-stack" onSubmit={updateSlot}><div className="two-column"><Field label="Date"><input type="date" value={editing.slotDate} onChange={(e) => setEditing({ ...editing, slotDate: e.target.value })} required /></Field><Field label="Capacity"><input type="number" min="1" value={editing.capacity} onChange={(e) => setEditing({ ...editing, capacity: e.target.value })} required /></Field><Field label="Start time"><input type="time" value={editing.startTime} onChange={(e) => setEditing({ ...editing, startTime: e.target.value })} required /></Field><Field label="End time"><input type="time" value={editing.endTime} onChange={(e) => setEditing({ ...editing, endTime: e.target.value })} required /></Field></div><div className="button-row"><button className="button button-dark">Save slot</button><button type="button" className="button" onClick={() => setEditing(null)}>Cancel</button></div></form></Card>}{roster && <Card title={`Roster for slot #${roster.slotId}`}><div className="two-column"><div><h3>Bookings</h3>{roster.bookings.length ? roster.bookings.map((item) => <p className="list-line" key={item.bookingId}>{item.studentName} <span>{item.rollNo}</span></p>) : <Empty text="No bookings." />}</div><div><h3>Waitlist</h3>{roster.waitlist.length ? roster.waitlist.map((item) => <p className="list-line" key={item.waitlistId}>#{item.position} {item.studentName}</p>) : <Empty text="No waitlist entries." />}</div></div></Card>}</section>;
}

function CreateSlot({ token, notify }) {
  const [form, setForm] = useState({ slotDate: "", startTime: "", endTime: "", capacity: 1 }); const update = (key) => (e) => setForm({ ...form, [key]: e.target.value });
  async function submit(e) { e.preventDefault(); try { await api("/api/professors/slots", { token, method: "POST", body: JSON.stringify({ ...form, capacity: Number(form.capacity) }) }); notify("New slot created.", "success"); setForm({ slotDate: "", startTime: "", endTime: "", capacity: 1 }); } catch (error) { notify(error.message, "error"); } }
  return <section className="narrow"><PageHeading eyebrow="PROFESSOR DASHBOARD" title="Add a slot." subtitle="Create one future office-hour time window." /><form className="card form-stack" onSubmit={submit}><Field label="Date"><input type="date" value={form.slotDate} onChange={update("slotDate")} required /></Field><div className="two-column"><Field label="Start time"><input type="time" value={form.startTime} onChange={update("startTime")} required /></Field><Field label="End time"><input type="time" value={form.endTime} onChange={update("endTime")} required /></Field></div><Field label="Capacity"><input type="number" min="1" value={form.capacity} onChange={update("capacity")} required /></Field><button className="button button-dark">Create slot</button></form></section>;
}

function AdminOverview({ token, notify }) {
  const [report, setReport] = useState(null); const [bookingId, setBookingId] = useState(""); useEffect(() => { api("/api/admin/reports", { token }).then(setReport).catch((e) => notify(e.message, "error")); }, [token, notify]);
  const stats = report ? [["Total users", report.totalUsers], ["Professors", report.totalProfessors], ["Students", report.totalStudents], ["Active bookings", report.totalActiveBookings], ["Waitlisted", report.totalWaitlisted], ["Utilization", `${report.slotUtilizationPercent.toFixed(1)}%`]] : [];
  async function forceCancel(event) { event.preventDefault(); try { await api(`/api/admin/bookings/${bookingId}`, { token, method: "DELETE" }); notify("Booking cancelled by admin.", "success"); setBookingId(""); } catch (e) { notify(e.message, "error"); } }
  return <section><PageHeading eyebrow="ADMIN DASHBOARD" title="At a glance." subtitle="A compact view of activity across office hours." /><div className="stat-grid">{stats.map(([label, value]) => <div className="stat-card" key={label}><span>{label}</span><strong>{value}</strong></div>)}</div><section className="card admin-action"><h2>Force-cancel a booking</h2><form className="inline-form" onSubmit={forceCancel}><input aria-label="Booking ID" type="number" min="1" value={bookingId} onChange={(e) => setBookingId(e.target.value)} placeholder="Booking ID" required /><button className="button button-dark">Cancel booking</button></form></section></section>;
}

function AdminUsers({ token, notify }) {
  const [users, setUsers] = useState([]); const refresh = useCallback(() => api("/api/admin/users", { token }).then(setUsers).catch((e) => notify(e.message, "error")), [token, notify]); useEffect(() => { refresh(); }, [refresh]);
  async function setActive(user, action) { try { await api(`/api/admin/users/${user.id}/${action}`, { token, method: "PATCH" }); notify(`User ${action}d.`, "success"); refresh(); } catch (e) { notify(e.message, "error"); } }
  return <section><PageHeading eyebrow="ADMIN DASHBOARD" title="Manage users." subtitle="Activate or deactivate access to the booking system." /><Card title="Users"><Table headings={["Name", "Email", "Role", "Access", ""]}>{users.map((user) => <tr key={user.id}><td>{user.fullName}</td><td>{user.email}</td><td><Badge value={user.role} /></td><td><Badge value={user.isActive ? "ACTIVE" : "INACTIVE"} /></td><td><button className="link-button" onClick={() => setActive(user, user.isActive ? "deactivate" : "activate")}>{user.isActive ? "Deactivate" : "Activate"}</button></td></tr>)}</Table></Card></section>;
}

function PageHeading({ eyebrow, title, subtitle }) { return <div className="page-heading"><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p className="muted">{subtitle}</p></div>; }
function Card({ title, children }) { return <section className="card"><h2>{title}</h2>{children}</section>; }
function Empty({ text }) { return <p className="empty">{text}</p>; }
function Table({ headings, children }) { return <div className="table-responsive"><table><thead><tr>{headings.map((heading) => <th key={heading}>{heading}</th>)}</tr></thead><tbody>{children}</tbody></table></div>; }

function App() {
  const [session, setSession] = useState(() => { try { return JSON.parse(localStorage.getItem(SESSION_KEY)); } catch { return null; } }); const [notice, setNotice] = useState(null);
  const role = session?.user?.role; const defaultTab = role === "STUDENT" ? "book" : role === "PROFESSOR" ? "schedule" : "overview"; const [tab, setTab] = useState(defaultTab);
  const notify = useCallback((message, kind = "success") => setNotice({ message, kind }), []);
  useEffect(() => { if (session?.token) api("/api/auth/me", { token: session.token }).then((user) => { const next = { ...session, user }; localStorage.setItem(SESSION_KEY, JSON.stringify(next)); setSession(next); }).catch(() => { localStorage.removeItem(SESSION_KEY); setSession(null); }); }, []);
  if (!session?.token) return <AuthScreen onSession={setSession} />;
  const logout = () => { localStorage.removeItem(SESSION_KEY); setSession(null); };
  let view = role === "STUDENT" ? (tab === "bookings" ? <StudentBookings token={session.token} notify={notify} /> : tab === "profile" ? <StudentProfile token={session.token} notify={notify} /> : <StudentBooking token={session.token} notify={notify} />) : role === "PROFESSOR" ? (tab === "create" ? <CreateSlot token={session.token} notify={notify} /> : <ProfessorSchedule token={session.token} notify={notify} />) : (tab === "users" ? <AdminUsers token={session.token} notify={notify} /> : <AdminOverview token={session.token} notify={notify} />);
  return <><Header session={session} tab={tab} setTab={setTab} onLogout={logout} /><main className="app-shell"><Notice notice={notice} onDismiss={() => setNotice(null)} />{view}</main></>;
}

createRoot(document.getElementById("root")).render(<App />);
