import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

function MfaVerification() {

    const navigate = useNavigate();

    const [code, setCode] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const email =
        localStorage.getItem("mfaEmail");

    const handleVerify = async (event) => {

        event.preventDefault();

        setError("");

        /*
         * Check email
         */
        if (!email) {

            setError(
                "Email information is missing. Please login again."
            );

            return;
        }

        /*
         * Validate code
         */
        if (code.length !== 6) {

            setError(
                "Please enter a valid 6-digit MFA code."
            );

            return;
        }

        setLoading(true);

        try {

            const response = await fetch(
                "http://localhost:8080/api/auth/verify-mfa",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
                    },

                    body: JSON.stringify({
                        email: email,
                        code: code,
                    }),
                }
            );

            const data =
                await response.json();

            if (!response.ok) {

                throw new Error(
                    data.message ||
                    "MFA verification failed"
                );
            }

            /*
             * JWT returned after successful MFA
             */
            if (!data.token) {

                throw new Error(
                    "MFA verified but authentication token was not received."
                );
            }

            /*
             * Store authentication information
             */
            localStorage.setItem(
                "token",
                data.token
            );

            localStorage.setItem(
                "email",
                email
            );

            /*
             * MFA email is no longer needed
             */
            localStorage.removeItem(
                "mfaEmail"
            );

            /*
             * Go to dashboard
             */
            navigate("/dashboard", {
                replace: true,
            });

        } catch (err) {

            setError(
                err.message ||
                "MFA verification failed"
            );

        } finally {

            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center px-4">

            <div className="w-full max-w-xl">

                <div className="bg-slate-900/90 border border-slate-700/60 rounded-3xl shadow-2xl p-8 sm:p-10">

                    {/* Logo */}
                    <div className="flex justify-center mb-6">

                        <div className="w-28 h-28 rounded-3xl bg-gradient-to-br from-cyan-400 to-blue-600 flex items-center justify-center shadow-xl shadow-cyan-500/20">

                            <span className="text-5xl">
                                🔐
                            </span>

                        </div>

                    </div>

                    {/* Heading */}
                    <div className="text-center mb-10">

                        <h1 className="text-4xl font-bold text-cyan-400">
                            MFA Verification
                        </h1>

                        <p className="text-slate-400 mt-4">
                            Enter the 6-digit verification code to continue.
                        </p>

                    </div>

                    {/* Email indicator */}
                    {email && (
                        <div className="mb-6 text-center">

                            <p className="text-sm text-slate-400">
                                Verifying account
                            </p>

                            <p className="text-cyan-400 font-medium mt-1">
                                {email}
                            </p>

                        </div>
                    )}

                    {/* Form */}
                    <form
                        onSubmit={handleVerify}
                        className="space-y-6"
                    >

                        {/* Code */}
                        <div>

                            <label
                                htmlFor="mfaCode"
                                className="block text-lg font-semibold text-slate-300 mb-3"
                            >
                                MFA Code
                            </label>

                            <input
                                id="mfaCode"
                                type="text"
                                inputMode="numeric"
                                maxLength={6}
                                value={code}
                                onChange={(event) => {

                                    const value =
                                        event.target.value
                                            .replace(/\D/g, "");

                                    setCode(value);

                                }}
                                placeholder="Enter 6-digit code"
                                autoFocus
                                className="w-full h-20 px-6 rounded-2xl bg-slate-950 border-2 border-slate-700 text-white text-3xl tracking-[0.5em] text-center outline-none focus:border-cyan-400 focus:ring-4 focus:ring-cyan-400/10"
                            />

                        </div>

                        {/* Error */}
                        {error && (

                            <div className="rounded-xl border border-red-500/40 bg-red-500/10 px-5 py-4 text-red-400">

                                {error}

                            </div>

                        )}

                        {/* Verify */}
                        <button
                            type="submit"
                            disabled={
                                loading ||
                                code.length !== 6
                            }
                            className="w-full h-16 rounded-2xl bg-gradient-to-r from-cyan-500 to-blue-600 text-white text-lg font-bold shadow-lg shadow-cyan-500/20 transition hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed"
                        >

                            {loading
                                ? "Verifying..."
                                : "Verify MFA"}

                        </button>

                    </form>

                    {/* Back */}
                    <div className="text-center mt-8">

                        <button
                            type="button"
                            onClick={() => {

                                localStorage.removeItem(
                                    "mfaEmail"
                                );

                                navigate("/login");

                            }}
                            className="text-slate-400 hover:text-cyan-400 transition"
                        >
                            ← Back to Login
                        </button>

                    </div>

                </div>

                <p className="text-center text-xs text-slate-600 mt-6">
                    SecureVault • Multi-Factor Authentication
                </p>

            </div>

        </div>
    );
}

export default MfaVerification;