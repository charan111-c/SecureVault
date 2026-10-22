import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event) => {
    event.preventDefault();

    setError("");
    setLoading(true);

    /*
     * Clear any old authentication/MFA state.
     */
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    localStorage.removeItem("username");
    localStorage.removeItem("mfaEmail");

    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email: email.trim(),
            password: password,
          }),
        }
      );

      /*
       * Try to read the backend response.
       */
      let data = {};

      try {
        data = await response.json();
      } catch {
        data = {};
      }

      /*
       * Login failed.
       */
      if (!response.ok) {
        throw new Error(
          data.message || "Invalid email or password"
        );
      }

      /*
       * =====================================================
       * MFA REQUIRED
       * =====================================================
       */
      if (data.mfaRequired === true) {
        /*
         * Save only the email.
         */
        localStorage.setItem(
          "mfaEmail",
          email.trim()
        );

        /*
         * Make sure an old token cannot be used.
         */
        localStorage.removeItem("token");
        localStorage.removeItem("email");
        localStorage.removeItem("username");

        /*
         * Go to MFA verification page.
         */
        navigate("/verify-mfa", {
          replace: true,
        });

        return;
      }

      /*
       * =====================================================
       * NORMAL LOGIN WITHOUT MFA
       * =====================================================
       */

      if (!data.token) {
        throw new Error(
          "Login successful, but authentication token was not received."
        );
      }

      /*
       * Save JWT.
       */
      localStorage.setItem(
        "token",
        data.token
      );

      /*
       * Save logged-in email.
       */
      localStorage.setItem(
        "email",
        email.trim()
      );

      /*
       * Save username.
       */
      const username =
        data.username ||
        email.trim().split("@")[0] ||
        "User";

      localStorage.setItem(
        "username",
        username
      );

      /*
       * Make sure MFA state is removed.
       */
      localStorage.removeItem("mfaEmail");

      /*
       * Role based navigation.
       *
       * ADMIN  -> Admin Dashboard
       * USER   -> Normal Dashboard
       */
      const role = String(data.role || "")
        .trim()
        .toUpperCase()
        .replace("ROLE_", "");

      if (role === "ADMIN") {
        navigate("/admin-dashboard", {
          replace: true,
        });
      } else {
        navigate("/dashboard", {
          replace: true,
        });
      }
    } catch (err) {
      /*
       * Failed login must not leave stale
       * authentication or MFA information.
       */
      localStorage.removeItem("token");
      localStorage.removeItem("email");
      localStorage.removeItem("username");
      localStorage.removeItem("mfaEmail");

      setError(
        err.message || "Login failed"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=DM+Mono:wght@400;500&family=Manrope:wght@400;500;600;700;800&display=swap');

        :root {
          --login-bg: #060a0f;
          --login-surface: #0b1118;
          --login-surface-2: #0f1720;
          --login-border: #202d38;
          --login-border-hover: #344554;

          --login-white: #f3f7fa;
          --login-text: #d7e0e8;
          --login-muted: #8796a5;
          --login-subtle: #607080;

          --login-blue: #38bdf8;
          --login-blue-dark: #12364a;
          --login-green: #34d399;
          --login-red: #f87171;
        }

        * {
          box-sizing: border-box;
        }

        body {
          margin: 0;
          font-family: "Manrope", sans-serif;
          background: var(--login-bg);
        }

        .login-page {
          min-height: 100vh;
          position: relative;
          overflow: hidden;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 32px 20px;

          background:
            radial-gradient(
              circle at 50% -10%,
              rgba(56, 189, 248, 0.09),
              transparent 32%
            ),
            radial-gradient(
              circle at 100% 100%,
              rgba(56, 189, 248, 0.035),
              transparent 30%
            ),
            #060a0f;
        }

        /* -----------------------------------------
           BACKGROUND GRID
        ----------------------------------------- */

        .login-grid {
          position: absolute;
          inset: 0;
          pointer-events: none;
          opacity: 0.32;

          background-image:
            linear-gradient(
              rgba(148, 163, 184, 0.025) 1px,
              transparent 1px
            ),
            linear-gradient(
              90deg,
              rgba(148, 163, 184, 0.025) 1px,
              transparent 1px
            );

          background-size: 72px 72px;

          mask-image: linear-gradient(
            to bottom,
            black,
            rgba(0,0,0,0.5),
            transparent
          );
        }

        .login-ambient {
          position: absolute;
          width: 480px;
          height: 480px;
          border-radius: 50%;
          border: 1px solid rgba(56, 189, 248, 0.04);
          pointer-events: none;
        }

        .login-ambient.one {
          top: -270px;
          left: 50%;
          transform: translateX(-50%);
        }

        .login-ambient.two {
          width: 700px;
          height: 700px;
          top: -380px;
          left: 50%;
          transform: translateX(-50%);
          border-color: rgba(56, 189, 248, 0.025);
        }

        /* -----------------------------------------
           MAIN CONTAINER
        ----------------------------------------- */

        .login-shell {
          width: min(430px, 100%);
          position: relative;
          z-index: 2;

          animation: loginAppear 650ms ease both;
        }

        /* -----------------------------------------
           TOP BRAND
        ----------------------------------------- */

        .login-brand {
          text-align: center;
          margin-bottom: 28px;
        }

        .login-brand-mark {
          width: 48px;
          height: 48px;
          margin: 0 auto 15px;

          border: 1px solid rgba(56, 189, 248, 0.45);
          border-radius: 11px;

          background:
            linear-gradient(
              145deg,
              rgba(56, 189, 248, 0.10),
              rgba(56, 189, 248, 0.025)
            );

          display: flex;
          align-items: center;
          justify-content: center;

          box-shadow:
            0 0 0 5px rgba(56, 189, 248, 0.025);
        }

        .login-brand-mark-inner {
          width: 17px;
          height: 21px;

          border: 1.6px solid var(--login-blue);
          border-radius: 5px 5px 7px 7px;

          position: relative;
        }

        .login-brand-mark-inner::before {
          content: "";
          position: absolute;

          width: 8px;
          height: 7px;

          border: 1.5px solid var(--login-blue);
          border-bottom: none;

          border-radius: 7px 7px 0 0;

          left: 50%;
          top: -7px;

          transform: translateX(-50%);
        }

        .login-brand-mark-inner::after {
          content: "";

          position: absolute;
          width: 3px;
          height: 5px;

          background: var(--login-blue);
          border-radius: 2px;

          left: 50%;
          top: 7px;

          transform: translateX(-50%);
        }

        .login-brand-name {
          margin: 0;

          color: var(--login-white);

          font-size: 22px;
          font-weight: 800;
          letter-spacing: -0.035em;
        }

        .login-brand-subtitle {
          margin-top: 6px;

          color: var(--login-subtle);

          font-family: "DM Mono", monospace;
          font-size: 9px;
          letter-spacing: 0.13em;
          text-transform: uppercase;
        }

        /* -----------------------------------------
           CARD
        ----------------------------------------- */

        .login-card {
          position: relative;

          background:
            linear-gradient(
              145deg,
              rgba(255,255,255,0.018),
              rgba(255,255,255,0)
            ),
            var(--login-surface);

          border: 1px solid var(--login-border);
          border-radius: 10px;

          padding: 34px;

          box-shadow:
            0 25px 70px rgba(0,0,0,0.38),
            0 0 0 1px rgba(255,255,255,0.008);

          overflow: hidden;
        }

        .login-card::before {
          content: "";

          position: absolute;
          top: 0;
          left: 34px;
          right: 34px;

          height: 1px;

          background:
            linear-gradient(
              90deg,
              transparent,
              rgba(56,189,248,0.5),
              transparent
            );

          opacity: 0.6;
        }

        /* -----------------------------------------
           BACK BUTTON
        ----------------------------------------- */

        .login-back {
          display: inline-flex;
          align-items: center;
          gap: 7px;

          padding: 0;
          margin: 0 0 29px;

          border: none;
          background: transparent;

          color: var(--login-subtle);

          font-size: 11px;
          font-weight: 600;

          cursor: pointer;

          transition: color 180ms ease;
        }

        .login-back:hover {
          color: var(--login-blue);
        }

        .login-back-arrow {
          font-size: 14px;
          line-height: 1;
        }

        /* -----------------------------------------
           CARD HEADING
        ----------------------------------------- */

        .login-heading {
          margin-bottom: 28px;
        }

        .login-kicker {
          margin-bottom: 9px;

          color: var(--login-blue);

          font-family: "DM Mono", monospace;
          font-size: 9px;
          letter-spacing: 0.14em;
          text-transform: uppercase;
        }

        .login-heading h1 {
          margin: 0;

          color: var(--login-white);

          font-size: 27px;
          line-height: 1.2;
          font-weight: 700;

          letter-spacing: -0.035em;
        }

        .login-heading p {
          margin: 9px 0 0;

          color: var(--login-muted);

          font-size: 12px;
          line-height: 1.65;
        }

        /* -----------------------------------------
           FORM
        ----------------------------------------- */

        .login-form {
          display: flex;
          flex-direction: column;
          gap: 19px;
        }

        .login-field {
          display: flex;
          flex-direction: column;
        }

        .login-label-row {
          display: flex;
          align-items: center;
          justify-content: space-between;

          margin-bottom: 8px;
        }

        .login-label {
          color: #c5d0da;

          font-size: 11px;
          font-weight: 700;
        }

        .login-input {
          width: 100%;
          height: 46px;

          padding: 0 14px;

          border-radius: 6px;
          border: 1px solid var(--login-border);

          background: #080e14;

          color: var(--login-white);

          font-family: "Manrope", sans-serif;
          font-size: 12px;

          outline: none;

          transition:
            border-color 180ms ease,
            background 180ms ease,
            box-shadow 180ms ease;
        }

        .login-input::placeholder {
          color: #4e5d6b;
        }

        .login-input:hover {
          border-color: var(--login-border-hover);
        }

        .login-input:focus {
          border-color: rgba(56, 189, 248, 0.7);

          background: #091119;

          box-shadow:
            0 0 0 3px rgba(56, 189, 248, 0.07);
        }

        .forgot-link {
          color: var(--login-blue);

          font-size: 10px;
          font-weight: 600;

          text-decoration: none;

          transition: color 180ms ease;
        }

        .forgot-link:hover {
          color: #7dd3fc;
        }

        /* -----------------------------------------
           ERROR
        ----------------------------------------- */

        .login-error {
          border: 1px solid rgba(248, 113, 113, 0.25);

          background: rgba(248, 113, 113, 0.055);

          border-radius: 6px;

          padding: 11px 12px;

          color: #fca5a5;

          font-size: 11px;
          line-height: 1.5;
        }

        /* -----------------------------------------
           LOGIN BUTTON
        ----------------------------------------- */

        .login-submit {
          width: 100%;
          height: 46px;

          margin-top: 2px;

          border: 1px solid rgba(56, 189, 248, 0.55);
          border-radius: 6px;

          background:
            linear-gradient(
              180deg,
              #16415a,
              #103247
            );

          color: #e7f8ff;

          font-size: 11px;
          font-weight: 800;

          letter-spacing: 0.01em;

          cursor: pointer;

          transition:
            transform 180ms ease,
            background 180ms ease,
            border-color 180ms ease,
            box-shadow 180ms ease;
        }

        .login-submit:hover:not(:disabled) {
          transform: translateY(-1px);

          background:
            linear-gradient(
              180deg,
              #1a4b66,
              #12394f
            );

          border-color: var(--login-blue);

          box-shadow:
            0 8px 24px rgba(56, 189, 248, 0.10);
        }

        .login-submit:active:not(:disabled) {
          transform: translateY(0);
        }

        .login-submit:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        /* -----------------------------------------
           DIVIDER
        ----------------------------------------- */

        .login-divider {
          display: flex;
          align-items: center;
          gap: 12px;

          margin: 25px 0 20px;
        }

        .login-divider-line {
          flex: 1;
          height: 1px;
          background: var(--login-border);
        }

        .login-divider-text {
          color: var(--login-subtle);

          font-family: "DM Mono", monospace;
          font-size: 8px;
          letter-spacing: 0.1em;
          text-transform: uppercase;
        }

        /* -----------------------------------------
           REGISTER
        ----------------------------------------- */

        .login-register {
          text-align: center;

          color: var(--login-muted);

          font-size: 11px;
        }

        .login-register a {
          margin-left: 4px;

          color: #a8dff6;

          font-weight: 700;
          text-decoration: none;

          transition: color 180ms ease;
        }

        .login-register a:hover {
          color: var(--login-blue);
        }

        /* -----------------------------------------
           SECURITY STATUS
        ----------------------------------------- */

        .login-security {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;

          margin-top: 20px;

          color: #667786;

          font-family: "DM Mono", monospace;
          font-size: 8px;

          letter-spacing: 0.08em;
          text-transform: uppercase;
        }

        .login-security-dot {
          width: 5px;
          height: 5px;

          border-radius: 50%;

          background: var(--login-green);

          box-shadow:
            0 0 8px rgba(52,211,153,0.3);
        }

        /* -----------------------------------------
           FOOTER
        ----------------------------------------- */

        .login-footer {
          margin-top: 20px;

          text-align: center;

          color: #465563;

          font-family: "DM Mono", monospace;
          font-size: 8px;

          letter-spacing: 0.04em;
        }

        /* -----------------------------------------
           ANIMATION
        ----------------------------------------- */

        @keyframes loginAppear {
          from {
            opacity: 0;
            transform: translateY(12px);
          }

          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        /* -----------------------------------------
           RESPONSIVE
        ----------------------------------------- */

        @media (max-width: 520px) {
          .login-page {
            padding: 24px 14px;
          }

          .login-card {
            padding: 27px 22px;
          }

          .login-card::before {
            left: 22px;
            right: 22px;
          }

          .login-heading h1 {
            font-size: 25px;
          }
        }

        @media (prefers-reduced-motion: reduce) {
          *,
          *::before,
          *::after {
            animation-duration: 0.01ms !important;
            animation-iteration-count: 1 !important;
            transition-duration: 0.01ms !important;
          }
        }
      `}</style>

      <div className="login-page">

        {/* BACKGROUND */}
        <div className="login-grid" />
        <div className="login-ambient one" />
        <div className="login-ambient two" />

        <div className="login-shell">

          {/* BRAND */}
          <div className="login-brand">

            <div className="login-brand-mark">
              <div className="login-brand-mark-inner" />
            </div>

            <h2 className="login-brand-name">
              SecureVault
            </h2>

            <div className="login-brand-subtitle">
              Credential Security Platform
            </div>

          </div>

          {/* LOGIN CARD */}
          <div className="login-card">

            {/* BACK */}
            <button
              type="button"
              className="login-back"
              onClick={() => navigate("/")}
            >
              <span className="login-back-arrow">
                ←
              </span>

              Back to Home
            </button>

            {/* HEADING */}
            <div className="login-heading">

              <div className="login-kicker">
                Secure authentication
              </div>

              <h1>
                Welcome back
              </h1>

              <p>
                Sign in to access your credentials and
                security controls.
              </p>

            </div>

            {/* FORM */}
            <form
              onSubmit={handleLogin}
              className="login-form"
            >

              {/* EMAIL */}
              <div className="login-field">

                <div className="login-label-row">
                  <label
                    htmlFor="email"
                    className="login-label"
                  >
                    Email address
                  </label>
                </div>

                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(event) =>
                    setEmail(event.target.value)
                  }
                  placeholder="you@example.com"
                  autoComplete="email"
                  required
                  className="login-input"
                />

              </div>

              {/* PASSWORD */}
              <div className="login-field">

                <div className="login-label-row">

                  <label
                    htmlFor="password"
                    className="login-label"
                  >
                    Password
                  </label>

                  <Link
                    to="/forgot-password"
                    className="forgot-link"
                  >
                    Forgot password?
                  </Link>

                </div>

                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(event) =>
                    setPassword(event.target.value)
                  }
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  required
                  className="login-input"
                />

              </div>

              {/* ERROR */}
              {error && (
                <div className="login-error">
                  {error}
                </div>
              )}

              {/* LOGIN */}
              <button
                type="submit"
                disabled={loading}
                className="login-submit"
              >
                {loading
                  ? "Authenticating..."
                  : "Sign in securely"}
              </button>

            </form>

            {/* DIVIDER */}
            <div className="login-divider">

              <div className="login-divider-line" />

              <span className="login-divider-text">
                Secure access
              </span>

              <div className="login-divider-line" />

            </div>

            {/* REGISTER */}
            <div className="login-register">

              Don't have an account?

              <Link to="/register">
                Create account
              </Link>

            </div>

          </div>

          {/* SECURITY STATUS */}
          <div className="login-security">

            <span className="login-security-dot" />

            Secure authentication environment

          </div>

          {/* FOOTER */}
          <div className="login-footer">
            SecureVault • Credential Management System
          </div>

        </div>
      </div>
    </>
  );
}

export default Login;