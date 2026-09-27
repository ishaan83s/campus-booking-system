import React from "react";
import { AlertTriangle, RefreshCw } from "lucide-react";

export class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // Log error internally without exposing stack traces to the user interface
    if (typeof console !== "undefined" && console.error) {
      console.error("ErrorBoundary caught an unhandled render error:", error, errorInfo);
    }
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div
          role="alert"
          style={{
            minHeight: "100vh",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "24px",
            backgroundColor: "var(--bg-app)",
            color: "var(--text-primary)",
            fontFamily: "var(--font-sans)",
          }}
        >
          <div
            style={{
              maxWidth: "460px",
              width: "100%",
              backgroundColor: "var(--bg-surface)",
              border: "1px solid var(--border-subtle)",
              borderRadius: "var(--radius-lg)",
              padding: "32px",
              textAlign: "center",
              boxShadow: "var(--shadow-card)",
            }}
          >
            <div
              style={{
                width: "48px",
                height: "48px",
                borderRadius: "50%",
                backgroundColor: "var(--danger-bg)",
                border: "1px solid var(--danger-border)",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                marginBottom: "16px",
              }}
            >
              <AlertTriangle size={24} color="var(--danger-text)" aria-hidden="true" />
            </div>

            <h1
              style={{
                fontSize: "1.25rem",
                fontWeight: 700,
                marginBottom: "8px",
                letterSpacing: "-0.015em",
              }}
            >
              Something went wrong
            </h1>

            <p
              style={{
                fontSize: "0.875rem",
                color: "var(--text-secondary)",
                lineHeight: 1.5,
                marginBottom: "24px",
              }}
            >
              An unexpected error occurred while displaying this page. You can reload the application to restore your session.
            </p>

            <button
              type="button"
              className="btn btn-primary"
              onClick={this.handleReload}
              style={{
                width: "100%",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                gap: "8px",
              }}
            >
              <RefreshCw size={16} aria-hidden="true" />
              Reload application
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
