import React from "react";

export function Card({ className = "", children, ...props }) {
  return (
    <section className={`card ${className}`.trim()} {...props}>
      {children}
    </section>
  );
}

export function CardHeader({ className = "", children, ...props }) {
  return (
    <div className={`card-header ${className}`.trim()} {...props}>
      {children}
    </div>
  );
}

export function CardTitle({ className = "", children, ...props }) {
  return (
    <h2 className={`card-title ${className}`.trim()} {...props}>
      {children}
    </h2>
  );
}

export function CardDescription({ className = "", children, ...props }) {
  return (
    <p className={`muted ${className}`.trim()} {...props}>
      {children}
    </p>
  );
}

export function CardContent({ className = "", children, ...props }) {
  return (
    <div className={`card-content ${className}`.trim()} {...props}>
      {children}
    </div>
  );
}

export function CardFooter({ className = "", children, ...props }) {
  return (
    <div className={`button-row ${className}`.trim()} style={{ marginTop: "16px" }} {...props}>
      {children}
    </div>
  );
}
