import React from "react";
import { Badge } from "../ui/Badge";
import { Button } from "../ui/Button";
import { LogOut, Calendar, Clock, User, Shield, BarChart3, Users } from "lucide-react";

export function Header({ session, tab, setTab, onLogout }) {
  const role = session?.user?.role;

  const tabs =
    role === "STUDENT"
      ? [
          { key: "book", label: "Book a slot", icon: Calendar },
          { key: "bookings", label: "My bookings", icon: Clock },
          { key: "profile", label: "Profile", icon: User },
        ]
      : role === "PROFESSOR"
      ? [
          { key: "schedule", label: "My schedule", icon: Calendar },
          { key: "create", label: "Add a slot", icon: Clock },
        ]
      : [
          { key: "overview", label: "Overview", icon: BarChart3 },
          { key: "users", label: "Users", icon: Users },
        ];

  return (
    <header className="app-header">
      <div className="brand">
        CAMPUS<span>OFFICE</span>HOURS
      </div>

      <nav aria-label="Main Navigation">
        {tabs.map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            type="button"
            onClick={() => setTab(key)}
            className={`nav-link ${key === tab ? "active" : ""}`}
            aria-current={key === tab ? "page" : undefined}
          >
            <Icon size={16} aria-hidden="true" />
            <span>{label}</span>
          </button>
        ))}
      </nav>

      <div className="account-area">
        <span className="avatar" aria-hidden="true">
          {session?.user?.fullName?.[0]?.toUpperCase() ?? "U"}
        </span>
        <span className="account-name">{session?.user?.fullName}</span>
        <Badge value={role} />
        <Button
          variant="ghost"
          size="sm"
          onClick={onLogout}
          aria-label="Sign out"
          title="Sign out"
        >
          <LogOut size={16} aria-hidden="true" />
          <span>Sign out</span>
        </Button>
      </div>
    </header>
  );
}
