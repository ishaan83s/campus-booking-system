import React from "react";
import { NavLink } from "react-router-dom";
import { Badge } from "../ui/Badge";
import { Button } from "../ui/Button";
import { LogOut, Calendar, Clock, User, BarChart3, Users } from "lucide-react";

export function Header({ session, onLogout }) {
  const role = session?.user?.role;

  const links =
    role === "STUDENT"
      ? [
          { to: "/student/book", label: "Book a slot", icon: Calendar },
          { to: "/student/bookings", label: "My bookings", icon: Clock },
          { to: "/student/profile", label: "Profile", icon: User },
        ]
      : role === "PROFESSOR"
      ? [
          { to: "/professor/schedule", label: "My schedule", icon: Calendar },
          { to: "/professor/create-slot", label: "Add a slot", icon: Clock },
        ]
      : [
          { to: "/admin/overview", label: "Overview", icon: BarChart3 },
          { to: "/admin/users", label: "Users", icon: Users },
        ];

  return (
    <header className="app-header">
      <div className="brand">
        CAMPUS<span>OFFICE</span>HOURS
      </div>

      <nav aria-label="Main Navigation">
        {links.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
          >
            <Icon size={16} aria-hidden="true" />
            <span>{label}</span>
          </NavLink>
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
