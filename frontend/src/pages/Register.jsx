import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function Register() {
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleRegister = async (event) => {
        event.preventDefault();

        setError("");
        setLoading(true);

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/register",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        username,
                        email,
                        password,
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Registration failed"
                );
            }

            navigate("/login");
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <style>{`
                @import url('https://fonts.googleapis.com/css2?family=DM+Mono:wght@400;500&family=Manrope:wght@400;500;600;700;800&display=swap');

                :root {
                    --register-bg: #060a0f;
                    --register-surface: #0b1118;
                    --register-border: #202d38;
                    --register-border-hover: #344554;

                    --register-white: #f3f7fa;
                    --register-text: #d7e0e8;
                    --register-muted: #8796a5;
                    --register-subtle: #607080;

                    --register-blue: #38bdf8;
                    --register-blue-dark: #12364a;
                    --register-green: #34d399;
                    --register-red: #f87171;
                }

                * {
                    box-sizing: border-box;
                }

                body {
                    margin: 0;
                    font-family: "Manrope", sans-serif;
                    background: var(--register-bg);
                }

                .register-page {
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

                /* ==============================
                   BACKGROUND
                ============================== */

                .register-grid {
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
                        rgba(0, 0, 0, 0.5),
                        transparent
                    );
                }

                .register-ring {
                    position: absolute;

                    width: 520px;
                    height: 520px;

                    border-radius: 50%;

                    border: 1px solid rgba(56, 189, 248, 0.035);

                    pointer-events: none;
                }

                .register-ring.one {
                    top: -330px;
                    left: 50%;
                    transform: translateX(-50%);
                }

                .register-ring.two {
                    width: 720px;
                    height: 720px;

                    top: -430px;
                    left: 50%;

                    transform: translateX(-50%);

                    border-color: rgba(56, 189, 248, 0.02);
                }

                /* ==============================
                   MAIN
                ============================== */

                .register-shell {
                    width: min(440px, 100%);
                    position: relative;
                    z-index: 2;

                    animation: registerAppear 650ms ease both;
                }

                /* ==============================
                   BRAND
                ============================== */

                .register-brand {
                    text-align: center;
                    margin-bottom: 25px;
                }

                .register-mark {
                    width: 46px;
                    height: 46px;

                    margin: 0 auto 14px;

                    border: 1px solid rgba(56, 189, 248, 0.45);

                    border-radius: 10px;

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

                .register-mark-inner {
                    width: 16px;
                    height: 20px;

                    border: 1.6px solid var(--register-blue);

                    border-radius: 5px 5px 7px 7px;

                    position: relative;
                }

                .register-mark-inner::before {
                    content: "";

                    position: absolute;

                    width: 8px;
                    height: 7px;

                    border: 1.5px solid var(--register-blue);
                    border-bottom: none;

                    border-radius: 7px 7px 0 0;

                    left: 50%;
                    top: -7px;

                    transform: translateX(-50%);
                }

                .register-mark-inner::after {
                    content: "";

                    position: absolute;

                    width: 3px;
                    height: 5px;

                    background: var(--register-blue);

                    border-radius: 2px;

                    left: 50%;
                    top: 7px;

                    transform: translateX(-50%);
                }

                .register-brand-name {
                    margin: 0;

                    color: var(--register-white);

                    font-size: 21px;
                    font-weight: 800;

                    letter-spacing: -0.035em;
                }

                .register-brand-subtitle {
                    margin-top: 5px;

                    color: var(--register-subtle);

                    font-family: "DM Mono", monospace;

                    font-size: 8px;
                    letter-spacing: 0.13em;

                    text-transform: uppercase;
                }

                /* ==============================
                   CARD
                ============================== */

                .register-card {
                    position: relative;

                    background:
                        linear-gradient(
                            145deg,
                            rgba(255, 255, 255, 0.018),
                            rgba(255, 255, 255, 0)
                        ),
                        var(--register-surface);

                    border: 1px solid var(--register-border);

                    border-radius: 10px;

                    padding: 32px 34px;

                    box-shadow:
                        0 25px 70px rgba(0, 0, 0, 0.38),
                        0 0 0 1px rgba(255, 255, 255, 0.008);

                    overflow: hidden;
                }

                .register-card::before {
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
                            rgba(56, 189, 248, 0.5),
                            transparent
                        );

                    opacity: 0.6;
                }

                /* ==============================
                   BACK
                ============================== */

                .register-back {
                    display: inline-flex;
                    align-items: center;

                    gap: 7px;

                    padding: 0;
                    margin: 0 0 25px;

                    border: none;
                    background: transparent;

                    color: var(--register-subtle);

                    font-size: 11px;
                    font-weight: 600;

                    cursor: pointer;

                    transition: color 180ms ease;
                }

                .register-back:hover {
                    color: var(--register-blue);
                }

                .register-back-arrow {
                    font-size: 14px;
                }

                /* ==============================
                   HEADING
                ============================== */

                .register-heading {
                    margin-bottom: 24px;
                }

                .register-kicker {
                    margin-bottom: 8px;

                    color: var(--register-blue);

                    font-family: "DM Mono", monospace;

                    font-size: 8px;
                    letter-spacing: 0.14em;

                    text-transform: uppercase;
                }

                .register-heading h1 {
                    margin: 0;

                    color: var(--register-white);

                    font-size: 27px;
                    line-height: 1.2;

                    font-weight: 700;

                    letter-spacing: -0.035em;
                }

                .register-heading p {
                    margin: 8px 0 0;

                    color: var(--register-muted);

                    font-size: 11px;
                    line-height: 1.65;
                }

                /* ==============================
                   FORM
                ============================== */

                .register-form {
                    display: flex;
                    flex-direction: column;

                    gap: 17px;
                }

                .register-field {
                    display: flex;
                    flex-direction: column;
                }

                .register-label {
                    color: #c5d0da;

                    font-size: 11px;
                    font-weight: 700;

                    margin-bottom: 8px;
                }

                .register-input {
                    width: 100%;
                    height: 45px;

                    padding: 0 14px;

                    border-radius: 6px;

                    border: 1px solid var(--register-border);

                    background: #080e14;

                    color: var(--register-white);

                    font-family: "Manrope", sans-serif;

                    font-size: 12px;

                    outline: none;

                    transition:
                        border-color 180ms ease,
                        background 180ms ease,
                        box-shadow 180ms ease;
                }

                .register-input::placeholder {
                    color: #4e5d6b;
                }

                .register-input:hover {
                    border-color: var(--register-border-hover);
                }

                .register-input:focus {
                    border-color: rgba(56, 189, 248, 0.7);

                    background: #091119;

                    box-shadow:
                        0 0 0 3px rgba(56, 189, 248, 0.07);
                }

                /* ==============================
                   ERROR
                ============================== */

                .register-error {
                    border: 1px solid rgba(248, 113, 113, 0.25);

                    background: rgba(248, 113, 113, 0.055);

                    border-radius: 6px;

                    padding: 10px 12px;

                    color: #fca5a5;

                    font-size: 11px;

                    line-height: 1.5;
                }

                /* ==============================
                   REGISTER BUTTON
                ============================== */

                .register-submit {
                    width: 100%;
                    height: 46px;

                    margin-top: 3px;

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

                .register-submit:hover:not(:disabled) {
                    transform: translateY(-1px);

                    background:
                        linear-gradient(
                            180deg,
                            #1a4b66,
                            #12394f
                        );

                    border-color: var(--register-blue);

                    box-shadow:
                        0 8px 24px rgba(56, 189, 248, 0.10);
                }

                .register-submit:active:not(:disabled) {
                    transform: translateY(0);
                }

                .register-submit:disabled {
                    opacity: 0.5;
                    cursor: not-allowed;
                }

                /* ==============================
                   DIVIDER
                ============================== */

                .register-divider {
                    display: flex;
                    align-items: center;

                    gap: 12px;

                    margin: 22px 0 18px;
                }

                .register-divider-line {
                    flex: 1;

                    height: 1px;

                    background: var(--register-border);
                }

                .register-divider-text {
                    color: var(--register-subtle);

                    font-family: "DM Mono", monospace;

                    font-size: 8px;

                    letter-spacing: 0.1em;

                    text-transform: uppercase;
                }

                /* ==============================
                   LOGIN
                ============================== */

                .register-login {
                    text-align: center;

                    color: var(--register-muted);

                    font-size: 11px;
                }

                .register-login a {
                    margin-left: 4px;

                    color: #a8dff6;

                    font-weight: 700;

                    text-decoration: none;

                    transition: color 180ms ease;
                }

                .register-login a:hover {
                    color: var(--register-blue);
                }

                /* ==============================
                   SECURITY STATUS
                ============================== */

                .register-security {
                    display: flex;
                    align-items: center;
                    justify-content: center;

                    gap: 8px;

                    margin-top: 19px;

                    color: #667786;

                    font-family: "DM Mono", monospace;

                    font-size: 8px;

                    letter-spacing: 0.08em;

                    text-transform: uppercase;
                }

                .register-security-dot {
                    width: 5px;
                    height: 5px;

                    border-radius: 50%;

                    background: var(--register-green);

                    box-shadow:
                        0 0 8px rgba(52, 211, 153, 0.3);
                }

                /* ==============================
                   FOOTER
                ============================== */

                .register-footer {
                    margin-top: 18px;

                    text-align: center;

                    color: #465563;

                    font-family: "DM Mono", monospace;

                    font-size: 8px;

                    letter-spacing: 0.04em;
                }

                /* ==============================
                   ANIMATION
                ============================== */

                @keyframes registerAppear {
                    from {
                        opacity: 0;
                        transform: translateY(12px);
                    }

                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }

                /* ==============================
                   RESPONSIVE
                ============================== */

                @media (max-width: 520px) {
                    .register-page {
                        padding: 24px 14px;
                    }

                    .register-card {
                        padding: 27px 22px;
                    }

                    .register-card::before {
                        left: 22px;
                        right: 22px;
                    }

                    .register-heading h1 {
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

            <div className="register-page">

                {/* Background */}
                <div className="register-grid" />
                <div className="register-ring one" />
                <div className="register-ring two" />

                <div className="register-shell">

                    {/* Brand */}
                    <div className="register-brand">

                        <div className="register-mark">
                            <div className="register-mark-inner" />
                        </div>

                        <h2 className="register-brand-name">
                            SecureVault
                        </h2>

                        <div className="register-brand-subtitle">
                            Credential Security Platform
                        </div>

                    </div>

                    {/* Register Card */}
                    <div className="register-card">

                        {/* Back */}
                        <button
                            type="button"
                            className="register-back"
                            onClick={() => navigate("/")}
                        >
                            <span className="register-back-arrow">
                                ←
                            </span>

                            Back to Home
                        </button>

                        {/* Heading */}
                        <div className="register-heading">

                            <div className="register-kicker">
                                Account registration
                            </div>

                            <h1>
                                Create your account
                            </h1>

                            <p>
                                Set up your SecureVault account
                                to securely manage your credentials.
                            </p>

                        </div>

                        {/* Form */}
                        <form
                            onSubmit={handleRegister}
                            className="register-form"
                        >

                            {/* Username */}
                            <div className="register-field">

                                <label
                                    htmlFor="username"
                                    className="register-label"
                                >
                                    Username
                                </label>

                                <input
                                    id="username"
                                    type="text"
                                    value={username}
                                    onChange={(event) =>
                                        setUsername(
                                            event.target.value
                                        )
                                    }
                                    placeholder="Enter your username"
                                    autoComplete="username"
                                    required
                                    className="register-input"
                                />

                            </div>

                            {/* Email */}
                            <div className="register-field">

                                <label
                                    htmlFor="email"
                                    className="register-label"
                                >
                                    Email address
                                </label>

                                <input
                                    id="email"
                                    type="email"
                                    value={email}
                                    onChange={(event) =>
                                        setEmail(
                                            event.target.value
                                        )
                                    }
                                    placeholder="you@example.com"
                                    autoComplete="email"
                                    required
                                    className="register-input"
                                />

                            </div>

                            {/* Password */}
                            <div className="register-field">

                                <label
                                    htmlFor="password"
                                    className="register-label"
                                >
                                    Password
                                </label>

                                <input
                                    id="password"
                                    type="password"
                                    value={password}
                                    onChange={(event) =>
                                        setPassword(
                                            event.target.value
                                        )
                                    }
                                    placeholder="Create a strong password"
                                    autoComplete="new-password"
                                    required
                                    className="register-input"
                                />

                            </div>

                            {/* Error */}
                            {error && (
                                <div className="register-error">
                                    {error}
                                </div>
                            )}

                            {/* Submit */}
                            <button
                                type="submit"
                                disabled={loading}
                                className="register-submit"
                            >
                                {loading
                                    ? "Creating account..."
                                    : "Create account"}
                            </button>

                        </form>

                        {/* Divider */}
                        <div className="register-divider">

                            <div className="register-divider-line" />

                            <span className="register-divider-text">
                                Secure registration
                            </span>

                            <div className="register-divider-line" />

                        </div>

                        {/* Login */}
                        <div className="register-login">

                            Already have an account?

                            <Link to="/login">
                                Sign in
                            </Link>

                        </div>

                    </div>

                    {/* Security Status */}
                    <div className="register-security">

                        <span className="register-security-dot" />

                        Secure registration environment

                    </div>

                    {/* Footer */}
                    <div className="register-footer">
                        SecureVault • Credential Management System
                    </div>

                </div>
            </div>
        </>
    );
}

export default Register;