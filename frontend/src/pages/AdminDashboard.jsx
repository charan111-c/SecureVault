import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE = "http://localhost:8080/api";

function AdminDashboard() {
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [stats, setStats] = useState({
        totalUsers: 0,
        totalAdmins: 0,
        totalCredentials: 0,
        totalLoginAttempts: 0,
        successfulLogins: 0,
        failedLogins: 0,
        securityAlerts: 0,
        highRiskAlerts: 0,
        knownDevices: 0,
    });

    const [recentEvents, setRecentEvents] = useState([]);

    const email = localStorage.getItem("email") || "Administrator";
    const token = localStorage.getItem("token");

    // =========================================================
    // API HELPER
    // =========================================================

    const apiRequest = async (url) => {
        const response = await fetch(`${API_BASE}${url}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
        });

        if (response.status === 401) {
            throw new Error("Session expired. Please login again.");
        }

        if (response.status === 403) {
            throw new Error(
                "Access denied. Administrator privileges are required."
            );
        }

        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }

        return response.json();
    };

    // =========================================================
    // LOAD ADMIN DATA
    // =========================================================

    useEffect(() => {
        const loadDashboard = async () => {
            if (!token) {
                navigate("/login", { replace: true });
                return;
            }

            try {
                setLoading(true);
                setError("");

                /*
                 * IMPORTANT:
                 * This endpoint should be created in your backend.
                 *
                 * GET /api/admin/dashboard
                 *
                 * The backend should calculate all statistics from
                 * the actual PostgreSQL database.
                 */

                const data = await apiRequest("/admin/dashboard");

                setStats({
                    totalUsers: data.totalUsers ?? 0,
                    totalAdmins: data.totalAdmins ?? 0,
                    totalCredentials:
                        data.totalCredentials ?? 0,
                    totalLoginAttempts:
                        data.totalLoginAttempts ?? 0,
                    successfulLogins:
                        data.successfulLogins ?? 0,
                    failedLogins:
                        data.failedLogins ?? 0,
                    securityAlerts:
                        data.securityAlerts ?? 0,
                    highRiskAlerts:
                        data.highRiskAlerts ?? 0,
                    knownDevices:
                        data.knownDevices ?? 0,
                });

                setRecentEvents(
                    Array.isArray(data.recentEvents)
                        ? data.recentEvents
                        : []
                );
            } catch (err) {
                console.error(
                    "Admin dashboard error:",
                    err
                );

                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        loadDashboard();
    }, [navigate, token]);

    // =========================================================
    // LOGOUT
    // =========================================================

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("email");

        navigate("/login", {
            replace: true,
        });
    };

    // =========================================================
    // LOADING
    // =========================================================

    if (loading) {
        return (
            <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center">

                <div className="text-center">

                    <div className="w-14 h-14 border-4 border-cyan-400 border-t-transparent rounded-full animate-spin mx-auto mb-5" />

                    <h2 className="text-xl font-semibold">
                        Loading Admin Dashboard
                    </h2>

                    <p className="text-slate-400 mt-2">
                        Fetching security information...
                    </p>

                </div>

            </div>
        );
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    return (
        <div className="min-h-screen bg-slate-950 text-white">

            {/* =================================================
                NAVBAR
            ================================================= */}

            <nav className="sticky top-0 z-50 border-b border-slate-800 bg-slate-900/95 backdrop-blur-xl">

                <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">

                    {/* LOGO */}

                    <div
                        className="flex items-center gap-3 cursor-pointer"
                        onClick={() =>
                            navigate("/admin-dashboard")
                        }
                    >

                        <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-cyan-400 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">

                            <span className="text-2xl">
                                🔐
                            </span>

                        </div>

                        <div>

                            <h1 className="text-xl font-bold">
                                SecureVault
                            </h1>

                            <p className="text-xs text-red-400">
                                Administrator Panel
                            </p>

                        </div>

                    </div>

                    {/* ADMIN */}

                    <div className="flex items-center gap-4">

                        <div className="hidden sm:block text-right">

                            <p className="text-sm font-medium">
                                {email}
                            </p>

                            <p className="text-xs text-red-400">
                                ● Administrator
                            </p>

                        </div>

                        <button
                            onClick={handleLogout}
                            className="px-4 py-2 rounded-xl border border-red-500/30 text-red-400 hover:bg-red-500/10 transition"
                        >
                            Logout
                        </button>

                    </div>

                </div>

            </nav>

            {/* =================================================
                MAIN
            ================================================= */}

            <main className="max-w-7xl mx-auto px-6 py-10">

                {/* HEADER */}

                <div className="mb-10">

                    <p className="text-cyan-400 text-sm font-medium mb-2">
                        ADMINISTRATION & SECURITY CENTER
                    </p>

                    <h1 className="text-4xl md:text-5xl font-bold">
                        Admin Dashboard
                    </h1>

                    <p className="text-slate-400 mt-3 max-w-3xl">
                        Monitor users, authentication activity,
                        credentials, devices and security threats
                        across SecureVault.
                    </p>

                </div>

                {/* ERROR */}

                {error && (
                    <div className="mb-8 rounded-2xl border border-red-500/30 bg-red-500/10 p-5">

                        <div className="flex gap-3">

                            <span className="text-xl">
                                ⚠️
                            </span>

                            <div>

                                <h3 className="font-semibold text-red-400">
                                    Unable to load dashboard
                                </h3>

                                <p className="text-sm text-red-300 mt-1">
                                    {error}
                                </p>

                            </div>

                        </div>

                    </div>
                )}

                {/* =================================================
                    MAIN STATISTICS
                ================================================= */}

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">

                    {/* TOTAL USERS */}

                    <StatCard
                        title="Total Users"
                        value={stats.totalUsers}
                        icon="👥"
                        color="blue"
                    />

                    {/* TOTAL CREDENTIALS */}

                    <StatCard
                        title="Total Credentials"
                        value={stats.totalCredentials}
                        icon="🔐"
                        color="cyan"
                    />

                    {/* LOGIN ATTEMPTS */}

                    <StatCard
                        title="Login Attempts"
                        value={stats.totalLoginAttempts}
                        icon="📋"
                        color="indigo"
                    />

                    {/* SECURITY ALERTS */}

                    <StatCard
                        title="Security Alerts"
                        value={stats.securityAlerts}
                        icon="🚨"
                        color="red"
                    />

                </div>

                {/* =================================================
                    SECOND STATISTICS
                ================================================= */}

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-5 mt-5">

                    <MiniStat
                        title="Admins"
                        value={stats.totalAdmins}
                        icon="🛡️"
                    />

                    <MiniStat
                        title="Successful Logins"
                        value={stats.successfulLogins}
                        icon="✅"
                    />

                    <MiniStat
                        title="Failed Logins"
                        value={stats.failedLogins}
                        icon="❌"
                    />

                    <MiniStat
                        title="High Risk"
                        value={stats.highRiskAlerts}
                        icon="🔴"
                    />

                    <MiniStat
                        title="Known Devices"
                        value={stats.knownDevices}
                        icon="💻"
                    />

                </div>

                {/* =================================================
                    SECURITY STATUS
                ================================================= */}

                <section className="mt-10">

                    <h2 className="text-2xl font-bold mb-5">
                        Security Status
                    </h2>

                    <div className="rounded-2xl border border-green-500/20 bg-green-500/5 p-6">

                        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-5">

                            <div className="flex items-center gap-4">

                                <div className="w-14 h-14 rounded-xl bg-green-500/10 flex items-center justify-center text-3xl">
                                    🛡️
                                </div>

                                <div>

                                    <h3 className="text-xl font-semibold">
                                        SecureVault Monitoring Active
                                    </h3>

                                    <p className="text-sm text-slate-400 mt-1">
                                        Authentication, devices,
                                        security alerts and
                                        suspicious activity are
                                        being monitored.
                                    </p>

                                </div>

                            </div>

                            <span className="px-4 py-2 rounded-full bg-green-500/10 text-green-400 text-sm font-medium">
                                ● System Active
                            </span>

                        </div>

                    </div>

                </section>

                {/* =================================================
                    ADMIN MODULES
                ================================================= */}

                <section className="mt-10">

                    <h2 className="text-2xl font-bold mb-5">
                        Administration
                    </h2>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">

                        {/* USER MANAGEMENT */}

                        <AdminCard
                            icon="👥"
                            title="User Management"
                            description="View registered users, roles and account information."
                            color="blue"
                            onClick={() =>
                                navigate("/users")
                            }
                        />

                        {/* LOGIN ACTIVITY */}

                        <AdminCard
                            icon="📋"
                            title="Login Activity"
                            description="Review successful and failed login attempts across the system."
                            color="indigo"
                            onClick={() =>
                                navigate("/login-activity")
                            }
                        />

                        {/* SECURITY ALERTS */}

                        <AdminCard
                            icon="🚨"
                            title="Security Alerts"
                            description="Review suspicious activity, threats and security warnings."
                            color="red"
                            onClick={() =>
                                navigate("/security-alerts")
                            }
                        />

                        {/* KNOWN DEVICES */}

                        <AdminCard
                            icon="💻"
                            title="Known Devices"
                            description="View devices that have accessed SecureVault."
                            color="cyan"
                            onClick={() =>
                                navigate("/known-devices")
                            }
                        />

                        {/* SECURITY ANALYTICS */}

                        <AdminCard
                            icon="📊"
                            title="Security Analytics"
                            description="Analyze login behavior, risks and security trends."
                            color="violet"
                            onClick={() =>
                                navigate("/analytics")
                            }
                        />

                        {/* REPORTS */}

                        <AdminCard
                            icon="📑"
                            title="Reports & Export"
                            description="Generate security and system activity reports."
                            color="emerald"
                            onClick={() =>
                                navigate("/reports")
                            }
                        />

                    </div>

                </section>

                {/* =================================================
                    RECENT SECURITY EVENTS
                ================================================= */}

                <section className="mt-10">

                    <div className="flex items-center justify-between mb-5">

                        <h2 className="text-2xl font-bold">
                            Recent Security Events
                        </h2>

                        <button
                            onClick={() =>
                                navigate("/login-activity")
                            }
                            className="text-cyan-400 hover:text-cyan-300"
                        >
                            View All →
                        </button>

                    </div>

                    <div className="rounded-2xl border border-slate-700 bg-slate-900 overflow-hidden">

                        {recentEvents.length === 0 ? (

                            <div className="p-10 text-center">

                                <div className="text-4xl mb-3">
                                    🔒
                                </div>

                                <p className="text-slate-400">
                                    No recent security events available.
                                </p>

                            </div>

                        ) : (

                            <div className="overflow-x-auto">

                                <table className="w-full">

                                    <thead className="bg-slate-800">

                                        <tr>

                                            <th className="text-left px-6 py-4 text-sm text-slate-400">
                                                User
                                            </th>

                                            <th className="text-left px-6 py-4 text-sm text-slate-400">
                                                Event
                                            </th>

                                            <th className="text-left px-6 py-4 text-sm text-slate-400">
                                                Risk
                                            </th>

                                            <th className="text-left px-6 py-4 text-sm text-slate-400">
                                                Time
                                            </th>

                                        </tr>

                                    </thead>

                                    <tbody>

                                        {recentEvents.map(
                                            (event, index) => (
                                                <tr
                                                    key={
                                                        event.id ??
                                                        index
                                                    }
                                                    className="border-t border-slate-800 hover:bg-slate-800/50"
                                                >

                                                    <td className="px-6 py-4">
                                                        {event.email ||
                                                            event.username ||
                                                            "Unknown"}
                                                    </td>

                                                    <td className="px-6 py-4 text-slate-300">
                                                        {event.event ||
                                                            event.action ||
                                                            event.type ||
                                                            "Security Event"}
                                                    </td>

                                                    <td className="px-6 py-4">

                                                        <RiskBadge
                                                            value={
                                                                event.riskLevel ||
                                                                event.severity ||
                                                                "LOW"
                                                            }
                                                        />

                                                    </td>

                                                    <td className="px-6 py-4 text-sm text-slate-400">
                                                        {formatDate(
                                                            event.createdAt ||
                                                            event.timestamp ||
                                                            event.loginTime
                                                        )}
                                                    </td>

                                                </tr>
                                            )
                                        )}

                                    </tbody>

                                </table>

                            </div>

                        )}

                    </div>

                </section>

            </main>

            {/* FOOTER */}

            <footer className="border-t border-slate-800 mt-16">

                <div className="max-w-7xl mx-auto px-6 py-6 text-center">

                    <p className="text-sm text-slate-500">
                        SecureVault • Administrator Security Center
                    </p>

                </div>

            </footer>

        </div>
    );
}

// =============================================================
// STAT CARD
// =============================================================

function StatCard({
    title,
    value,
    icon,
    color,
}) {
    const colors = {
        blue: "border-blue-500/20 bg-blue-500/5 text-blue-400",
        cyan: "border-cyan-500/20 bg-cyan-500/5 text-cyan-400",
        indigo: "border-indigo-500/20 bg-indigo-500/5 text-indigo-400",
        red: "border-red-500/20 bg-red-500/5 text-red-400",
    };

    return (
        <div
            className={`rounded-2xl border p-6 ${colors[color]}`}
        >

            <div className="flex items-center justify-between">

                <div>

                    <p className="text-sm text-slate-400">
                        {title}
                    </p>

                    <h2 className="text-3xl font-bold mt-2">
                        {value}
                    </h2>

                </div>

                <div className="w-12 h-12 rounded-xl bg-slate-900/50 flex items-center justify-center text-2xl">
                    {icon}
                </div>

            </div>

        </div>
    );
}

// =============================================================
// MINI STAT
// =============================================================

function MiniStat({
    title,
    value,
    icon,
}) {
    return (
        <div className="rounded-2xl border border-slate-700 bg-slate-900 p-5">

            <div className="flex items-center gap-3">

                <span className="text-xl">
                    {icon}
                </span>

                <div>

                    <p className="text-xs text-slate-400">
                        {title}
                    </p>

                    <p className="text-xl font-bold mt-1">
                        {value}
                    </p>

                </div>

            </div>

        </div>
    );
}

// =============================================================
// ADMIN CARD
// =============================================================

function AdminCard({
    icon,
    title,
    description,
    color,
    onClick,
}) {
    const borderColors = {
        blue: "hover:border-blue-400",
        indigo: "hover:border-indigo-400",
        red: "hover:border-red-400",
        cyan: "hover:border-cyan-400",
        violet: "hover:border-violet-400",
        emerald: "hover:border-emerald-400",
    };

    const textColors = {
        blue: "text-blue-400",
        indigo: "text-indigo-400",
        red: "text-red-400",
        cyan: "text-cyan-400",
        violet: "text-violet-400",
        emerald: "text-emerald-400",
    };

    return (
        <div
            onClick={onClick}
            className={`cursor-pointer rounded-2xl border border-slate-700 bg-slate-900 p-6 transition duration-200 hover:bg-slate-800 ${borderColors[color]}`}
        >

            <div className="w-14 h-14 rounded-xl bg-slate-800 flex items-center justify-center text-3xl mb-5">
                {icon}
            </div>

            <h3 className="text-xl font-semibold">
                {title}
            </h3>

            <p className="text-slate-400 text-sm mt-2 min-h-[40px]">
                {description}
            </p>

            <div
                className={`mt-5 font-medium ${textColors[color]}`}
            >
                Open {title} →
            </div>

        </div>
    );
}

// =============================================================
// RISK BADGE
// =============================================================

function RiskBadge({ value }) {
    const risk = String(value).toUpperCase();

    let className =
        "bg-green-500/10 text-green-400";

    if (risk === "MEDIUM") {
        className =
            "bg-yellow-500/10 text-yellow-400";
    }

    if (
        risk === "HIGH" ||
        risk === "CRITICAL"
    ) {
        className =
            "bg-red-500/10 text-red-400";
    }

    return (
        <span
            className={`px-3 py-1 rounded-full text-xs font-medium ${className}`}
        >
            {risk}
        </span>
    );
}

// =============================================================
// DATE
// =============================================================

function formatDate(value) {
    if (!value) {
        return "N/A";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleString();
}

export default AdminDashboard;