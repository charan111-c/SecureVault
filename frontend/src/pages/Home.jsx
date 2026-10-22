import React, { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

/* ---------- Small inline icon set ---------- */

const Icon = ({ name, size = 20 }) => {
  const p = {
    width: size,
    height: size,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.6,
    strokeLinecap: "round",
    strokeLinejoin: "round",
    "aria-hidden": true,
  };

  switch (name) {
    case "vault":
      return (
        <svg {...p}>
          <rect x="3" y="4" width="18" height="16" rx="2.5" />
          <circle cx="12" cy="12" r="3.2" />
          <path d="M12 8.8V7M12 17v-1.8M8.8 12H7M17 12h-1.8" />
        </svg>
      );

    case "team":
      return (
        <svg {...p}>
          <circle cx="9" cy="8" r="3.2" />
          <path d="M3 19c0-3.2 2.7-5.5 6-5.5s6 2.3 6 5.5" />
          <circle cx="17.5" cy="9" r="2.5" />
          <path d="M17 14c2.4.2 4 1.9 4 4.5" />
        </svg>
      );

    case "activity":
      return (
        <svg {...p}>
          <path d="M3 12h4l2.5-6 5 12 2.5-6H21" />
        </svg>
      );

    case "analytics":
      return (
        <svg {...p}>
          <path d="M4 20V10M10 20V4M16 20v-7M22 20H2" />
        </svg>
      );

    case "lock":
      return (
        <svg {...p}>
          <rect x="4.5" y="10.5" width="15" height="10" rx="2.2" />
          <path d="M8 10.5V7.5a4 4 0 018 0v3" />
        </svg>
      );

    case "key":
      return (
        <svg {...p}>
          <circle cx="8" cy="15" r="4" />
          <path d="M11 12l9-9M16 7l3 3M14 9l2 2" />
        </svg>
      );

    case "shield":
      return (
        <svg {...p}>
          <path d="M12 3l8 3v6c0 4.5-3.2 7.8-8 9-4.8-1.2-8-4.5-8-9V6l8-3z" />
          <path d="M8.5 12l2.5 2.5L15.5 10" />
        </svg>
      );

    case "arrow":
      return (
        <svg {...p}>
          <path d="M5 12h14M13 6l6 6-6 6" />
        </svg>
      );

    case "plus":
      return (
        <svg {...p}>
          <path d="M12 5v14M5 12h14" />
        </svg>
      );

    case "menu":
      return (
        <svg {...p}>
          <path d="M4 7h16M4 12h16M4 17h16" />
        </svg>
      );

    case "close":
      return (
        <svg {...p}>
          <path d="M6 6l12 12M18 6L6 18" />
        </svg>
      );

    default:
      return null;
  }
};

/* ---------- Scroll reveal wrapper ---------- */

function Reveal({ children, delay = 0, className = "" }) {
  const ref = useRef(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const el = ref.current;

    if (!el) return;

    if (typeof IntersectionObserver === "undefined") {
      setVisible(true);
      return;
    }

    const io = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true);
          io.disconnect();
        }
      },
      { threshold: 0.12 }
    );

    io.observe(el);

    return () => io.disconnect();
  }, []);

  return (
    <div
      ref={ref}
      className={`sv-reveal ${visible ? "is-visible" : ""} ${className}`}
      style={{ transitionDelay: `${delay}ms` }}
    >
      {children}
    </div>
  );
}

/* ---------- Static data ---------- */

const MODULES = [
  {
    icon: "vault",
    title: "Personal Vault",
    description:
      "Store and organize your personal credentials in one protected, searchable workspace.",
    action: "Open Vault",
    path: "/vault",
    accent: "#38BDF8",
  },
  {
    icon: "team",
    title: "Team Vault",
    description:
      "Share credentials with your team using controlled access and clearly defined permissions.",
    action: "Open Team Vault",
    path: "/team-vault",
    accent: "#60A5FA",
  },
  {
    icon: "activity",
    title: "Login Activity",
    description:
      "Review sign-in events and keep full visibility over authentication history.",
    action: "View Activity",
    path: "/login-activity",
    accent: "#34D399",
  },
  {
    icon: "analytics",
    title: "Security Analytics",
    description:
      "Track security patterns and understand the overall health of your account at a glance.",
    action: "View Analytics",
    path: "/analytics",
    accent: "#A5B4FC",
  },
];

const FEATURES = [
  {
    icon: "lock",
    title: "Encrypted Storage",
    description:
      "Credentials are kept inside a centralized, protected storage layer, never exposed in plain view.",
  },
  {
    icon: "key",
    title: "Password Generator",
    description:
      "Create strong, unpredictable passwords in one click to reduce weak-credential risk.",
  },
  {
    icon: "shield",
    title: "Access Control",
    description:
      "Decide exactly who can view or manage shared credentials with structured permissions.",
  },
];

const STEPS = [
  {
    title: "Create your account",
    description: "Sign up and secure your workspace in under a minute.",
  },
  {
    title: "Add your credentials",
    description:
      "Save logins to your personal vault or share them with your team.",
  },
  {
    title: "Stay in control",
    description:
      "Monitor activity and analytics so nothing happens without your knowledge.",
  },
];

const FAQS = [
  {
    q: "What is the difference between Personal and Team Vault?",
    a: "The Personal Vault is private to you. The Team Vault is for credentials shared with others, with permissions that control who can access what.",
  },
  {
    q: "Can I generate strong passwords inside SecureVault?",
    a: "Yes. The built-in password generator creates strong, hard-to-guess passwords so you don't have to invent them yourself.",
  },
  {
    q: "How do I know if someone accessed my account?",
    a: "Login Activity lists authentication events on your account, so you can quickly spot anything unfamiliar.",
  },
  {
    q: "What does Security Analytics show?",
    a: "It summarizes the overall state of your account and highlights patterns that may need your attention.",
  },
];

/* ---------- Component ---------- */

function Home() {
  const navigate = useNavigate();

  const [token, setToken] = useState(() =>
    localStorage.getItem("token")
  );

  const [username, setUsername] = useState(() =>
    localStorage.getItem("username")
  );

  const [menuOpen, setMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const [openFaq, setOpenFaq] = useState(0);

  // Keep auth state in sync
  useEffect(() => {
    const sync = () => {
      setToken(localStorage.getItem("token"));
      setUsername(localStorage.getItem("username"));
    };

    window.addEventListener("storage", sync);

    return () => window.removeEventListener("storage", sync);
  }, []);

  // Navbar shadow on scroll
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8);

    onScroll();

    window.addEventListener("scroll", onScroll, {
      passive: true,
    });

    return () =>
      window.removeEventListener("scroll", onScroll);
  }, []);

  // Lock body scroll while mobile menu is open
  useEffect(() => {
    document.body.style.overflow = menuOpen ? "hidden" : "";

    return () => {
      document.body.style.overflow = "";
    };
  }, [menuOpen]);

  const goProtected = (path) => {
    setMenuOpen(false);

    if (!localStorage.getItem("token")) {
      navigate("/login", { replace: true });
      return;
    }

    navigate(path);
  };

  const handleLogin = () => {
    setMenuOpen(false);
    navigate("/login");
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("email");

    setToken(null);
    setUsername(null);
    setMenuOpen(false);

    navigate("/", { replace: true });
  };

  const scrollTo = (id) => {
    setMenuOpen(false);

    document
      .getElementById(id)
      ?.scrollIntoView({ behavior: "smooth" });
  };

  const onKeyActivate = (e, fn) => {
    if (e.key === "Enter" || e.key === " ") {
      e.preventDefault();
      fn();
    }
  };

  const initial = (username || "U")
    .charAt(0)
    .toUpperCase();

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=DM+Mono:wght@400;500&family=Manrope:wght@400;500;600;700;800&display=swap');

        :root {
          --sv-bg: #070b10;
          --sv-surface: #0d141c;
          --sv-surface-2: #111b25;
          --sv-border: #1f2c38;
          --sv-border-soft: rgba(148, 163, 184, 0.10);
          --sv-white: #f1f5f9;
          --sv-text: #dbe4ec;
          --sv-muted: #8b9aaa;
          --sv-subtle: #657688;
          --sv-blue: #38bdf8;
          --sv-green: #34d399;
          --sv-amber: #fbbf24;
        }

        * {
          box-sizing: border-box;
        }

        html {
          scroll-behavior: smooth;
        }

        body {
          margin: 0;
          background: var(--sv-bg);
          color: var(--sv-text);
          font-family: "Manrope", system-ui, sans-serif;
          -webkit-font-smoothing: antialiased;
        }

        button {
          font-family: inherit;
        }

        :focus-visible {
          outline: 2px solid var(--sv-blue);
          outline-offset: 3px;
          border-radius: 6px;
        }

        .sv-home {
          min-height: 100vh;
          position: relative;
          overflow-x: hidden;

          background:
            radial-gradient(
              circle at 80% -5%,
              rgba(56, 189, 248, 0.09),
              transparent 34%
            ),
            radial-gradient(
              circle at 5% 40%,
              rgba(96, 165, 250, 0.04),
              transparent 32%
            ),
            var(--sv-bg);
        }

        .sv-grid {
          position: absolute;
          inset: 0 0 auto 0;
          height: 900px;
          pointer-events: none;
          opacity: 0.35;

          background-image:
            linear-gradient(
              rgba(148, 163, 184, 0.04) 1px,
              transparent 1px
            ),
            linear-gradient(
              90deg,
              rgba(148, 163, 184, 0.04) 1px,
              transparent 1px
            );

          background-size: 72px 72px;

          mask-image: linear-gradient(
            to bottom,
            black,
            transparent
          );

          -webkit-mask-image: linear-gradient(
            to bottom,
            black,
            transparent
          );
        }

        .sv-container {
          width: min(1180px, calc(100% - 48px));
          margin: 0 auto;
          position: relative;
          z-index: 2;
        }

        /* ---------- Reveal ---------- */

        .sv-reveal {
          opacity: 0;
          transform: translateY(18px);
          transition:
            opacity 650ms ease,
            transform 650ms ease;
        }

        .sv-reveal.is-visible {
          opacity: 1;
          transform: none;
        }

        /* ---------- Navbar ---------- */

        .sv-nav-wrap {
          position: sticky;
          top: 0;
          z-index: 50;
          border-bottom: 1px solid transparent;

          transition:
            background 200ms ease,
            border-color 200ms ease;
        }

        .sv-nav-wrap.scrolled {
          background: rgba(7, 11, 16, 0.78);
          backdrop-filter: blur(14px);
          -webkit-backdrop-filter: blur(14px);
          border-color: var(--sv-border-soft);
        }

        .sv-navbar {
          height: 72px;

          display: flex;
          align-items: center;
          justify-content: space-between;

          gap: 24px;
        }

        .sv-brand {
          display: flex;
          align-items: center;
          gap: 12px;

          cursor: pointer;

          background: none;
          border: none;
          padding: 0;

          text-align: left;
        }

        .sv-brand-mark {
          width: 34px;
          height: 34px;

          border-radius: 9px;

          display: grid;
          place-items: center;

          color: var(--sv-blue);

          border: 1px solid rgba(56, 189, 248, 0.4);

          background:
            linear-gradient(
              135deg,
              rgba(56, 189, 248, 0.16),
              rgba(56, 189, 248, 0.03)
            );
        }

        .sv-brand-name {
          color: var(--sv-white);

          font-size: 16px;
          font-weight: 800;

          letter-spacing: -0.02em;

          display: block;
          line-height: 1.1;
        }

        .sv-brand-label {
          color: var(--sv-subtle);

          font-family: "DM Mono", monospace;

          font-size: 8.5px;
          letter-spacing: 0.14em;

          text-transform: uppercase;
        }

        .sv-nav-links {
          display: flex;
          align-items: center;
          gap: 6px;
        }

        .sv-nav-link {
          background: none;
          border: none;
          cursor: pointer;

          color: var(--sv-muted);

          font-size: 13px;
          font-weight: 600;

          padding: 8px 14px;

          border-radius: 8px;

          transition:
            color 160ms ease,
            background 160ms ease;
        }

        .sv-nav-link:hover {
          color: var(--sv-white);
          background: rgba(148, 163, 184, 0.08);
        }

        .sv-nav-right {
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .sv-user-chip {
          display: flex;
          align-items: center;
          gap: 10px;

          padding: 5px 12px 5px 5px;

          border: 1px solid var(--sv-border);

          border-radius: 999px;

          background: var(--sv-surface);

          color: var(--sv-text);

          font-size: 12px;
          font-weight: 600;
        }

        .sv-avatar {
          width: 26px;
          height: 26px;

          border-radius: 50%;

          display: grid;
          place-items: center;

          background:
            linear-gradient(
              135deg,
              #38bdf8,
              #2563eb
            );

          color: #04121c;

          font-size: 12px;
          font-weight: 800;
        }

        .sv-btn {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 9px;

          height: 40px;
          padding: 0 18px;

          border-radius: 8px;

          font-size: 13px;
          font-weight: 700;

          cursor: pointer;

          transition:
            transform 160ms ease,
            background 160ms ease,
            border-color 160ms ease,
            color 160ms ease;
        }

        .sv-btn:active {
          transform: translateY(1px);
        }

        .sv-btn-primary {
          border: 1px solid rgba(125, 211, 252, 0.6);

          background:
            linear-gradient(
              180deg,
              #38bdf8,
              #0ea5e9
            );

          color: #04121c;

          box-shadow:
            0 8px 24px
            rgba(14, 165, 233, 0.22);
        }

        .sv-btn-primary:hover {
          background:
            linear-gradient(
              180deg,
              #5ccbfb,
              #1aabee
            );
        }

        .sv-btn-ghost {
          border: 1px solid var(--sv-border);
          background: var(--sv-surface);
          color: var(--sv-text);
        }

        .sv-btn-ghost:hover {
          border-color: #33465a;
          color: var(--sv-white);
        }

        .sv-btn-lg {
          height: 48px;
          padding: 0 24px;
          font-size: 14px;
        }

        .sv-menu-toggle {
          display: none;

          width: 40px;
          height: 40px;

          border: 1px solid var(--sv-border);

          background: var(--sv-surface);

          color: var(--sv-white);

          border-radius: 8px;

          cursor: pointer;

          place-items: center;
        }

        .sv-mobile-menu {
          position: fixed;

          inset: 72px 0 0 0;

          background: rgba(7, 11, 16, 0.97);

          backdrop-filter: blur(10px);

          padding: 24px;

          display: flex;
          flex-direction: column;
          gap: 6px;

          z-index: 40;

          animation:
            svFade 200ms ease both;
        }

        .sv-mobile-menu .sv-nav-link {
          text-align: left;

          font-size: 17px;

          padding: 14px 12px;

          border-bottom:
            1px solid
            var(--sv-border-soft);

          border-radius: 0;
        }

        .sv-mobile-actions {
          margin-top: 18px;

          display: flex;
          flex-direction: column;

          gap: 10px;
        }

        /* ---------- Hero ---------- */

        .sv-hero {
          display: flex;
          align-items: center;
          justify-content: center;

          text-align: center;

          padding: 84px 0 96px;

          min-height: 620px;
        }

        .sv-hero > div {
          width: 100%;
        }

        .sv-hero .sv-reveal {
          display: flex;
          flex-direction: column;
          align-items: center;
        }

        .sv-pill {
          display: inline-flex;
          align-items: center;
          gap: 10px;

          padding: 6px 14px 6px 8px;

          border:
            1px solid
            rgba(56, 189, 248, 0.25);

          background:
            rgba(56, 189, 248, 0.06);

          border-radius: 999px;

          color: #bfe9fb;

          font-size: 12px;
          font-weight: 600;

          margin-bottom: 26px;
        }

        .sv-pill-dot {
          width: 7px;
          height: 7px;

          border-radius: 50%;

          background: var(--sv-green);

          box-shadow:
            0 0 0 4px
            rgba(52, 211, 153, 0.12);
        }

        .sv-hero h1 {
          margin: 0;

          color: var(--sv-white);

          font-size:
            clamp(40px, 5.4vw, 68px);

          line-height: 1.03;

          letter-spacing: -0.05em;

          font-weight: 800;
        }

        .sv-hero h1 span {
          background:
            linear-gradient(
              90deg,
              #7dd3fc,
              #93a8ff
            );

          -webkit-background-clip: text;
          background-clip: text;

          color: transparent;
        }

        .sv-hero-description {
          margin: 24px auto 0;

          max-width: 650px;

          color: var(--sv-muted);

          font-size: 16px;

          line-height: 1.75;
        }

        .sv-hero-actions {
          margin-top: 34px;

          display: flex;
          flex-wrap: wrap;

          justify-content: center;

          gap: 12px;
        }

        .sv-hero-trust {
          margin-top: 38px;

          display: flex;
          flex-wrap: wrap;

          justify-content: center;

          gap: 22px;

          color: var(--sv-subtle);

          font-family: "DM Mono", monospace;

          font-size: 10px;

          letter-spacing: 0.1em;

          text-transform: uppercase;
        }

        .sv-hero-trust span {
          display: inline-flex;

          align-items: center;

          gap: 8px;
        }

        .sv-hero-trust svg {
          color: var(--sv-green);
        }

        /* ---------- Sections ---------- */

        .sv-section {
          padding: 92px 0;
        }

        .sv-section.bordered {
          border-top:
            1px solid
            var(--sv-border-soft);
        }

        .sv-section-head {
          max-width: 640px;
          margin-bottom: 44px;
        }

        .sv-section-head.center {
          margin-left: auto;
          margin-right: auto;

          text-align: center;
        }

        .sv-kicker {
          color: var(--sv-blue);

          font-family: "DM Mono", monospace;

          font-size: 10px;

          letter-spacing: 0.18em;

          text-transform: uppercase;

          margin-bottom: 14px;
        }

        .sv-title {
          margin: 0;

          color: var(--sv-white);

          font-size:
            clamp(28px, 3.4vw, 40px);

          line-height: 1.12;

          letter-spacing: -0.04em;

          font-weight: 800;
        }

        .sv-lead {
          margin: 16px 0 0;

          color: var(--sv-muted);

          font-size: 15px;

          line-height: 1.75;
        }

        /* ---------- Modules ---------- */

        .sv-module-grid {
          display: grid;

          grid-template-columns:
            repeat(2, 1fr);

          gap: 16px;
        }

        .sv-module-card {
          position: relative;

          overflow: hidden;

          min-height: 250px;

          padding: 28px;

          display: flex;
          flex-direction: column;

          text-align: left;

          border:
            1px solid
            var(--sv-border);

          border-radius: 14px;

          background:
            radial-gradient(
              circle at 100% 0%,
              color-mix(
                in srgb,
                var(--accent) 12%,
                transparent
              ),
              transparent 55%
            ),
            var(--sv-surface);

          cursor: pointer;

          transition:
            transform 220ms ease,
            border-color 220ms ease;
        }

        .sv-module-card:hover {
          transform: translateY(-4px);

          border-color:
            color-mix(
              in srgb,
              var(--accent) 45%,
              #1f2c38
            );
        }

        .sv-module-top {
          display: flex;

          align-items: center;

          justify-content: space-between;
        }

        .sv-module-icon {
          width: 44px;
          height: 44px;

          border-radius: 11px;

          display: grid;

          place-items: center;

          color: var(--accent);

          border:
            1px solid
            color-mix(
              in srgb,
              var(--accent) 35%,
              transparent
            );

          background:
            color-mix(
              in srgb,
              var(--accent) 10%,
              transparent
            );
        }

        .sv-lock-badge {
          display: inline-flex;

          align-items: center;

          gap: 6px;

          color: var(--sv-subtle);

          font-family: "DM Mono", monospace;

          font-size: 9px;

          letter-spacing: 0.1em;

          text-transform: uppercase;
        }

        .sv-module-title {
          margin: 40px 0 10px;

          color: var(--sv-white);

          font-size: 21px;

          font-weight: 700;

          letter-spacing: -0.03em;
        }

        .sv-module-description {
          margin: 0;

          max-width: 420px;

          color: var(--sv-muted);

          font-size: 13px;

          line-height: 1.7;
        }

        .sv-module-action {
          margin-top: auto;

          padding-top: 26px;

          display: inline-flex;

          align-items: center;

          gap: 9px;

          color: #c4d3df;

          font-size: 12px;

          font-weight: 700;
        }

        .sv-module-action svg {
          color: var(--accent);

          transition:
            transform 180ms ease;
        }

        .sv-module-card:hover
        .sv-module-action svg {
          transform: translateX(5px);
        }

        /* ---------- Features ---------- */

        .sv-feature-grid {
          display: grid;

          grid-template-columns:
            repeat(3, 1fr);

          gap: 16px;
        }

        .sv-feature {
          padding: 28px;

          border:
            1px solid
            var(--sv-border);

          border-radius: 14px;

          background:
            var(--sv-surface);
        }

        .sv-feature-icon {
          width: 42px;
          height: 42px;

          border-radius: 10px;

          display: grid;

          place-items: center;

          color: var(--sv-blue);

          background:
            rgba(56, 189, 248, 0.08);

          border:
            1px solid
            rgba(56, 189, 248, 0.22);
        }

        .sv-feature h3 {
          margin: 24px 0 10px;

          color: var(--sv-white);

          font-size: 17px;

          font-weight: 700;

          letter-spacing: -0.02em;
        }

        .sv-feature p {
          margin: 0;

          color: var(--sv-muted);

          font-size: 13px;

          line-height: 1.75;
        }

        /* ---------- Steps ---------- */

        .sv-steps {
          display: grid;

          grid-template-columns:
            repeat(3, 1fr);

          gap: 0;

          border:
            1px solid
            var(--sv-border);

          border-radius: 14px;

          background:
            var(--sv-surface);

          overflow: hidden;
        }

        .sv-step {
          padding: 30px;

          border-right:
            1px solid
            var(--sv-border);
        }

        .sv-step:last-child {
          border-right: none;
        }

        .sv-step-num {
          font-family: "DM Mono", monospace;

          font-size: 11px;

          letter-spacing: 0.14em;

          color: var(--sv-blue);
        }

        .sv-step h3 {
          margin: 30px 0 8px;

          color: var(--sv-white);

          font-size: 17px;

          font-weight: 700;
        }

        .sv-step p {
          margin: 0;

          color: var(--sv-muted);

          font-size: 13px;

          line-height: 1.7;
        }

        /* ---------- FAQ ---------- */

        .sv-faq {
          max-width: 780px;

          margin: 0 auto;

          display: grid;

          gap: 10px;
        }

        .sv-faq-item {
          border:
            1px solid
            var(--sv-border);

          border-radius: 12px;

          background:
            var(--sv-surface);

          overflow: hidden;

          transition:
            border-color 200ms ease;
        }

        .sv-faq-item.open {
          border-color: #2c3d4d;
        }

        .sv-faq-q {
          width: 100%;

          display: flex;

          align-items: center;

          justify-content: space-between;

          gap: 16px;

          padding: 18px 22px;

          background: none;

          border: none;

          cursor: pointer;

          color: var(--sv-white);

          font-size: 14px;

          font-weight: 700;

          text-align: left;
        }

        .sv-faq-q svg {
          flex-shrink: 0;

          color: var(--sv-blue);

          transition:
            transform 220ms ease;
        }

        .sv-faq-item.open
        .sv-faq-q svg {
          transform: rotate(45deg);
        }

        .sv-faq-a {
          display: grid;

          grid-template-rows: 0fr;

          transition:
            grid-template-rows 260ms ease;
        }

        .sv-faq-item.open
        .sv-faq-a {
          grid-template-rows: 1fr;
        }

        .sv-faq-a > div {
          overflow: hidden;
        }

        .sv-faq-a p {
          margin: 0;

          padding: 0 22px 20px;

          color: var(--sv-muted);

          font-size: 13px;

          line-height: 1.75;
        }

        /* ---------- CTA ---------- */

        .sv-cta {
          padding: 20px 0 96px;
        }

        .sv-cta-inner {
          position: relative;

          overflow: hidden;

          display: flex;

          align-items: center;

          justify-content: space-between;

          gap: 32px;

          padding: 52px 56px;

          border:
            1px solid
            var(--sv-border);

          border-radius: 18px;

          background:
            radial-gradient(
              circle at 0% 0%,
              rgba(56, 189, 248, 0.14),
              transparent 50%
            ),
            var(--sv-surface);
        }

        .sv-cta-title {
          margin: 0;

          color: var(--sv-white);

          font-size:
            clamp(24px, 3vw, 32px);

          letter-spacing: -0.04em;

          font-weight: 800;
        }

        .sv-cta-description {
          margin: 10px 0 0;

          color: var(--sv-muted);

          font-size: 14px;
        }

        /* ---------- Footer ---------- */

        .sv-footer {
          border-top:
            1px solid
            var(--sv-border-soft);

          padding: 56px 0 30px;
        }

        .sv-footer-grid {
          display: grid;

          grid-template-columns:
            1.6fr 1fr 1fr;

          gap: 40px;
        }

        .sv-footer-about p {
          margin: 16px 0 0;

          max-width: 320px;

          color: var(--sv-muted);

          font-size: 12px;

          line-height: 1.8;
        }

        .sv-footer-col h4 {
          margin: 0 0 16px;

          color: var(--sv-white);

          font-family: "DM Mono", monospace;

          font-size: 10px;

          font-weight: 500;

          letter-spacing: 0.14em;

          text-transform: uppercase;
        }

        .sv-footer-col button {
          display: block;

          margin-bottom: 11px;

          background: none;

          border: none;

          padding: 0;

          cursor: pointer;

          color: var(--sv-muted);

          font-size: 13px;

          text-align: left;

          transition:
            color 160ms ease;
        }

        .sv-footer-col button:hover {
          color: var(--sv-white);
        }

        .sv-footer-bottom {
          margin-top: 44px;

          padding-top: 22px;

          border-top:
            1px solid
            var(--sv-border-soft);

          display: flex;

          justify-content: space-between;

          gap: 12px;

          flex-wrap: wrap;

          color: var(--sv-subtle);

          font-family: "DM Mono", monospace;

          font-size: 10px;

          letter-spacing: 0.04em;
        }

        /* ---------- Animations ---------- */

        @keyframes svFade {
          from {
            opacity: 0;
          }

          to {
            opacity: 1;
          }
        }

        /* ---------- Responsive ---------- */

        @media (max-width: 980px) {
          .sv-hero {
            padding: 64px 0 72px;
          }

          .sv-nav-links,
          .sv-nav-right .sv-desktop-only {
            display: none;
          }

          .sv-menu-toggle {
            display: grid;
          }

          .sv-module-grid,
          .sv-feature-grid,
          .sv-steps {
            grid-template-columns: 1fr;
          }

          .sv-step {
            border-right: none;

            border-bottom:
              1px solid
              var(--sv-border);
          }

          .sv-step:last-child {
            border-bottom: none;
          }

          .sv-footer-grid {
            grid-template-columns: 1fr 1fr;
          }

          .sv-footer-about {
            grid-column: 1 / -1;
          }

          .sv-cta-inner {
            flex-direction: column;

            align-items: flex-start;

            padding: 38px 30px;
          }
        }

        @media (max-width: 520px) {
          .sv-container {
            width: calc(100% - 28px);
          }

          .sv-section {
            padding: 68px 0;
          }

          .sv-hero-actions .sv-btn {
            width: 100%;
          }

          .sv-brand-label {
            display: none;
          }

          .sv-module-card {
            padding: 24px;

            min-height: 230px;
          }
        }

        @media (prefers-reduced-motion: reduce) {
          *,
          *::before,
          *::after {
            animation-duration: 0.01ms !important;

            animation-iteration-count: 1 !important;

            transition-duration: 0.01ms !important;

            scroll-behavior: auto !important;
          }

          .sv-reveal {
            opacity: 1;
            transform: none;
          }
        }
      `}</style>

      <div className="sv-home">
        <div className="sv-grid" />

        {/* NAVBAR */}

        <div
          className={`sv-nav-wrap ${
            scrolled ? "scrolled" : ""
          }`}
        >
          <div className="sv-container">
            <header className="sv-navbar">
              <button
                className="sv-brand"
                onClick={() => navigate("/")}
                aria-label="SecureVault home"
              >
                <div className="sv-brand-mark">
                  <Icon
                    name="shield"
                    size={18}
                  />
                </div>

                <div>
                  <span className="sv-brand-name">
                    SecureVault
                  </span>

                  <span className="sv-brand-label">
                    Credential Security
                  </span>
                </div>
              </button>

              <nav
                className="sv-nav-links"
                aria-label="Primary"
              >
                <button
                  className="sv-nav-link"
                  onClick={() =>
                    scrollTo("platform")
                  }
                >
                  Platform
                </button>

                <button
                  className="sv-nav-link"
                  onClick={() =>
                    scrollTo("security")
                  }
                >
                  Security
                </button>

                <button
                  className="sv-nav-link"
                  onClick={() =>
                    scrollTo("how")
                  }
                >
                  How it works
                </button>

                <button
                  className="sv-nav-link"
                  onClick={() =>
                    scrollTo("faq")
                  }
                >
                  FAQ
                </button>
              </nav>

              <div className="sv-nav-right">
                {token ? (
                  <>
                    <div className="sv-user-chip sv-desktop-only">
                      <span className="sv-avatar">
                        {initial}
                      </span>

                      {username || "Account"}
                    </div>

                    <button
                      className="sv-btn sv-btn-ghost sv-desktop-only"
                      onClick={handleLogout}
                    >
                      Sign out
                    </button>
                  </>
                ) : (
                  <button
                    className="sv-btn sv-btn-primary sv-desktop-only"
                    onClick={handleLogin}
                  >
                    Sign in
                  </button>
                )}

                <button
                  className="sv-menu-toggle"
                  onClick={() =>
                    setMenuOpen((v) => !v)
                  }
                  aria-label={
                    menuOpen
                      ? "Close menu"
                      : "Open menu"
                  }
                  aria-expanded={menuOpen}
                >
                  <Icon
                    name={
                      menuOpen
                        ? "close"
                        : "menu"
                    }
                  />
                </button>
              </div>
            </header>
          </div>
        </div>

        {/* MOBILE MENU */}

        {menuOpen && (
          <div className="sv-mobile-menu">
            <button
              className="sv-nav-link"
              onClick={() =>
                scrollTo("platform")
              }
            >
              Platform
            </button>

            <button
              className="sv-nav-link"
              onClick={() =>
                scrollTo("security")
              }
            >
              Security
            </button>

            <button
              className="sv-nav-link"
              onClick={() =>
                scrollTo("how")
              }
            >
              How it works
            </button>

            <button
              className="sv-nav-link"
              onClick={() =>
                scrollTo("faq")
              }
            >
              FAQ
            </button>

            <div className="sv-mobile-actions">
              {token ? (
                <button
                  className="sv-btn sv-btn-ghost sv-btn-lg"
                  onClick={handleLogout}
                >
                  Sign out
                </button>
              ) : (
                <button
                  className="sv-btn sv-btn-primary sv-btn-lg"
                  onClick={handleLogin}
                >
                  Sign in
                </button>
              )}
            </div>
          </div>
        )}

        <main className="sv-container">

          {/* HERO */}

          <section className="sv-hero">
            <div>
              <Reveal>
                <div className="sv-pill">
                  <span className="sv-pill-dot" />

                  Security systems operational
                </div>

                <h1>
                  Your credentials.
                  <br />

                  <span>
                    Under your control.
                  </span>
                </h1>

                <p className="sv-hero-description">
                  SecureVault brings credential
                  management, controlled team
                  access, and clear visibility into
                  account activity together in one
                  secure platform.
                </p>

                <div className="sv-hero-actions">
                  {token ? (
                    <button
                      className="sv-btn sv-btn-primary sv-btn-lg"
                      onClick={() =>
                        goProtected("/vault")
                      }
                    >
                      Open my vault

                      <Icon
                        name="arrow"
                        size={16}
                      />
                    </button>
                  ) : (
                    <button
                      className="sv-btn sv-btn-primary sv-btn-lg"
                      onClick={handleLogin}
                    >
                      Get started

                      <Icon
                        name="arrow"
                        size={16}
                      />
                    </button>
                  )}

                  <button
                    className="sv-btn sv-btn-ghost sv-btn-lg"
                    onClick={() =>
                      scrollTo("platform")
                    }
                  >
                    Explore platform
                  </button>
                </div>

                <div className="sv-hero-trust">
                  <span>
                    <Icon
                      name="lock"
                      size={13}
                    />

                    Encrypted storage
                  </span>

                  <span>
                    <Icon
                      name="team"
                      size={13}
                    />

                    Team permissions
                  </span>

                  <span>
                    <Icon
                      name="activity"
                      size={13}
                    />

                    Audit visibility
                  </span>
                </div>
              </Reveal>
            </div>
          </section>

          {/* PLATFORM MODULES */}

          <section
            className="sv-section bordered"
            id="platform"
          >
            <Reveal>
              <div className="sv-section-head">
                <div className="sv-kicker">
                  Platform capabilities
                </div>

                <h2 className="sv-title">
                  Security operations,
                  centralized.
                </h2>

                <p className="sv-lead">
                  A structured environment for
                  managing credentials,
                  collaboration, access, and
                  security visibility.
                </p>
              </div>
            </Reveal>

            <div className="sv-module-grid">
              {MODULES.map((m, i) => (
                <Reveal
                  key={m.title}
                  delay={i * 80}
                >
                  <div
                    role="button"
                    tabIndex={0}
                    className="sv-module-card"
                    style={{
                      "--accent": m.accent,
                    }}
                    onClick={() =>
                      goProtected(m.path)
                    }
                    onKeyDown={(e) =>
                      onKeyActivate(
                        e,
                        () =>
                          goProtected(
                            m.path
                          )
                      )
                    }
                    aria-label={`${m.title}: ${m.action}`}
                  >
                    <div className="sv-module-top">
                      <div className="sv-module-icon">
                        <Icon
                          name={m.icon}
                          size={22}
                        />
                      </div>

                      {!token && (
                        <span className="sv-lock-badge">
                          <Icon
                            name="lock"
                            size={12}
                          />

                          Sign in required
                        </span>
                      )}
                    </div>

                    <h3 className="sv-module-title">
                      {m.title}
                    </h3>

                    <p className="sv-module-description">
                      {m.description}
                    </p>

                    <div className="sv-module-action">
                      {m.action}

                      <Icon
                        name="arrow"
                        size={15}
                      />
                    </div>
                  </div>
                </Reveal>
              ))}
            </div>
          </section>

          {/* SECURITY BY DESIGN */}

          <section
            className="sv-section bordered"
            id="security"
          >
            <Reveal>
              <div className="sv-section-head">
                <div className="sv-kicker">
                  Security architecture
                </div>

                <h2 className="sv-title">
                  Security by design.
                </h2>

                <p className="sv-lead">
                  Core controls built to support
                  safer credential handling and
                  accountable access.
                </p>
              </div>
            </Reveal>

            <div className="sv-feature-grid">
              {FEATURES.map((f, i) => (
                <Reveal
                  key={f.title}
                  delay={i * 90}
                >
                  <article className="sv-feature">
                    <div className="sv-feature-icon">
                      <Icon
                        name={f.icon}
                        size={20}
                      />
                    </div>

                    <h3>{f.title}</h3>

                    <p>
                      {f.description}
                    </p>
                  </article>
                </Reveal>
              ))}
            </div>
          </section>

          {/* HOW IT WORKS */}

          <section
            className="sv-section bordered"
            id="how"
          >
            <Reveal>
              <div className="sv-section-head center">
                <div className="sv-kicker">
                  How it works
                </div>

                <h2 className="sv-title">
                  Up and running in three
                  steps.
                </h2>
              </div>
            </Reveal>

            <Reveal delay={100}>
              <div className="sv-steps">
                {STEPS.map((s, i) => (
                  <div
                    className="sv-step"
                    key={s.title}
                  >
                    <div className="sv-step-num">
                      STEP 0{i + 1}
                    </div>

                    <h3>
                      {s.title}
                    </h3>

                    <p>
                      {s.description}
                    </p>
                  </div>
                ))}
              </div>
            </Reveal>
          </section>

          {/* FAQ */}

          <section
            className="sv-section bordered"
            id="faq"
          >
            <Reveal>
              <div className="sv-section-head center">
                <div className="sv-kicker">
                  FAQ
                </div>

                <h2 className="sv-title">
                  Common questions.
                </h2>
              </div>
            </Reveal>

            <Reveal delay={100}>
              <div className="sv-faq">
                {FAQS.map((item, i) => {
                  const open =
                    openFaq === i;

                  return (
                    <div
                      className={`sv-faq-item ${
                        open ? "open" : ""
                      }`}
                      key={item.q}
                    >
                      <button
                        className="sv-faq-q"
                        onClick={() =>
                          setOpenFaq(
                            open ? -1 : i
                          )
                        }
                        aria-expanded={open}
                      >
                        {item.q}

                        <Icon
                          name="plus"
                          size={18}
                        />
                      </button>

                      <div className="sv-faq-a">
                        <div>
                          <p>
                            {item.a}
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </Reveal>
          </section>

          {/* CTA */}

          {!token && (
            <section className="sv-cta">
              <Reveal>
                <div className="sv-cta-inner">
                  <div>
                    <div className="sv-kicker">
                      Secure access
                    </div>

                    <h2 className="sv-cta-title">
                      Ready to access
                      SecureVault?
                    </h2>

                    <p className="sv-cta-description">
                      Sign in to manage
                      credentials and security
                      controls.
                    </p>
                  </div>

                  <button
                    className="sv-btn sv-btn-primary sv-btn-lg"
                    onClick={handleLogin}
                  >
                    Continue to sign in

                    <Icon
                      name="arrow"
                      size={16}
                    />
                  </button>
                </div>
              </Reveal>
            </section>
          )}
        </main>

        {/* FOOTER */}

        <footer className="sv-footer">
          <div className="sv-container">
            <div className="sv-footer-grid">

              <div className="sv-footer-about">
                <div
                  className="sv-brand"
                  style={{
                    cursor: "default",
                  }}
                >
                  <div className="sv-brand-mark">
                    <Icon
                      name="shield"
                      size={18}
                    />
                  </div>

                  <span className="sv-brand-name">
                    SecureVault
                  </span>
                </div>

                <p>
                  Secure credential management
                  for individuals and teams, with
                  controlled access and clear
                  security visibility.
                </p>
              </div>

              <div className="sv-footer-col">
                <h4>Platform</h4>

                <button
                  onClick={() =>
                    goProtected("/vault")
                  }
                >
                  Personal Vault
                </button>

                <button
                  onClick={() =>
                    goProtected("/team-vault")
                  }
                >
                  Team Vault
                </button>

                <button
                  onClick={() =>
                    goProtected(
                      "/login-activity"
                    )
                  }
                >
                  Login Activity
                </button>

                <button
                  onClick={() =>
                    goProtected("/analytics")
                  }
                >
                  Security Analytics
                </button>
              </div>

              <div className="sv-footer-col">
                <h4>Explore</h4>

                <button
                  onClick={() =>
                    scrollTo("security")
                  }
                >
                  Security
                </button>

                <button
                  onClick={() =>
                    scrollTo("how")
                  }
                >
                  How it works
                </button>

                <button
                  onClick={() =>
                    scrollTo("faq")
                  }
                >
                  FAQ
                </button>
              </div>
            </div>

            <div className="sv-footer-bottom">
              <span>
                © {new Date().getFullYear()}{" "}
                SecureVault. All rights reserved.
              </span>

              <span>
                Secure credential management
                platform
              </span>
            </div>
          </div>
        </footer>
      </div>
    </>
  );
}

export default Home;