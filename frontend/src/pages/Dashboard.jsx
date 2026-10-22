import React from "react";
import { useNavigate } from "react-router-dom";

/* =========================================================
   DASHBOARD
========================================================= */

function Dashboard() {
    const navigate = useNavigate();

    /* =====================================================
       LOGOUT
    ===================================================== */

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("email");
        localStorage.removeItem("username");

        navigate("/login", { replace: true });
    };

    /* =====================================================
       LOGGED-IN USER
    ===================================================== */

    const email = localStorage.getItem("email") || "";

    const username =
        localStorage.getItem("username") ||
        email.split("@")[0] ||
        "User";

    /* =====================================================
       MAIN MODULE CARD
    ===================================================== */

    const ModuleCard = ({
        title,
        description,
        action,
        onClick,
        accent,
    }) => {
        return (
            <button
                type="button"
                onClick={onClick}
                className="
                    group relative w-full text-left
                    overflow-hidden
                    rounded-2xl
                    border border-white/[0.07]
                    bg-[#0b1120]/80
                    backdrop-blur-xl
                    p-6
                    transition-all duration-300
                    hover:-translate-y-1
                    hover:border-white/[0.14]
                    hover:bg-[#0e1628]
                    focus:outline-none
                    focus:ring-2
                    focus:ring-white/10
                "
            >
                {/* Very subtle accent */}
                <div
                    className={`
                        absolute left-0 top-0 bottom-0
                        w-[2px]
                        ${accent}
                        opacity-60
                        group-hover:opacity-100
                        transition-opacity duration-300
                    `}
                />

                <div className="relative">

                    <div className="flex items-center justify-between">
                        <h3 className="text-lg font-semibold text-white">
                            {title}
                        </h3>

                        <span
                            className="
                                text-slate-600
                                text-lg
                                group-hover:text-slate-300
                                group-hover:translate-x-1
                                transition-all duration-300
                            "
                        >
                            →
                        </span>
                    </div>

                    <p className="mt-3 text-sm leading-6 text-slate-400 max-w-md">
                        {description}
                    </p>

                    <div
                        className="
                            mt-6
                            text-sm font-medium
                            text-slate-300
                            group-hover:text-white
                            transition-colors duration-300
                        "
                    >
                        {action}
                    </div>
                </div>
            </button>
        );
    };

    /* =====================================================
       STATUS CARD
    ===================================================== */

    const StatusCard = ({
        title,
        value,
        statusColor,
        indicatorColor,
    }) => {
        return (
            <div
                className="
                    rounded-2xl
                    border border-white/[0.07]
                    bg-[#0b1120]/70
                    backdrop-blur-xl
                    px-5 py-5
                    transition-all duration-300
                    hover:border-white/[0.11]
                "
            >
                <div className="flex items-center justify-between">

                    <div>
                        <p className="text-xs uppercase tracking-[0.14em] text-slate-500">
                            {title}
                        </p>

                        <p
                            className={`
                                mt-2
                                text-xl
                                font-semibold
                                ${statusColor}
                            `}
                        >
                            {value}
                        </p>
                    </div>

                    <span className="relative flex h-2.5 w-2.5">
                        <span
                            className={`
                                absolute
                                inline-flex
                                h-full
                                w-full
                                rounded-full
                                ${indicatorColor}
                                opacity-30
                                animate-ping
                            `}
                        />

                        <span
                            className={`
                                relative
                                inline-flex
                                h-2.5
                                w-2.5
                                rounded-full
                                ${indicatorColor}
                            `}
                        />
                    </span>

                </div>
            </div>
        );
    };

    return (
        <div className="min-h-screen bg-[#050912] text-white">

            {/* =================================================
                BACKGROUND
            ================================================= */}

            <div className="fixed inset-0 pointer-events-none overflow-hidden">

                {/* Main subtle grid */}
                <div
                    className="
                        absolute
                        inset-0
                        opacity-[0.025]
                    "
                    style={{
                        backgroundImage: `
                            linear-gradient(
                                rgba(148,163,184,0.8) 1px,
                                transparent 1px
                            ),
                            linear-gradient(
                                90deg,
                                rgba(148,163,184,0.8) 1px,
                                transparent 1px
                            )
                        `,
                        backgroundSize: "80px 80px",
                    }}
                />

                {/* Very subtle top glow */}
                <div
                    className="
                        absolute
                        -top-72
                        left-1/2
                        -translate-x-1/2
                        w-[700px]
                        h-[500px]
                        rounded-full
                        bg-blue-600/[0.035]
                        blur-[140px]
                    "
                />

                {/* Very subtle side glow */}
                <div
                    className="
                        absolute
                        top-[40%]
                        -right-72
                        w-[500px]
                        h-[500px]
                        rounded-full
                        bg-violet-600/[0.025]
                        blur-[140px]
                    "
                />

            </div>

            {/* =================================================
                NAVBAR
            ================================================= */}

            <nav
                className="
                    sticky top-0 z-50
                    border-b border-white/[0.06]
                    bg-[#050912]/90
                    backdrop-blur-xl
                "
            >
                <div
                    className="
                        max-w-7xl
                        mx-auto
                        px-5 sm:px-6
                        py-4
                        flex
                        items-center
                        justify-between
                    "
                >

                    {/* Brand */}

                    <button
                        type="button"
                        onClick={() => navigate("/dashboard")}
                        className="
                            text-left
                            focus:outline-none
                        "
                    >
                        <div className="flex items-center gap-3">

                            <div>
                                <h1
                                    className="
                                        text-lg
                                        font-semibold
                                        tracking-tight
                                        text-white
                                    "
                                >
                                    SecureVault
                                </h1>

                                <p
                                    className="
                                        mt-0.5
                                        text-[10px]
                                        uppercase
                                        tracking-[0.18em]
                                        text-slate-500
                                    "
                                >
                                    Security Platform
                                </p>
                            </div>

                        </div>
                    </button>

                    {/* User */}

                    <div className="flex items-center gap-5">

                        <div className="hidden sm:block text-right">

                            <p className="text-sm font-medium text-slate-200">
                                {username}
                            </p>

                            <div className="mt-1 flex items-center justify-end gap-2">

                                <span
                                    className="
                                        h-1.5
                                        w-1.5
                                        rounded-full
                                        bg-emerald-400
                                    "
                                />

                                <span className="text-[11px] text-slate-500">
                                    Secure session
                                </span>

                            </div>

                        </div>

                        <button
                            type="button"
                            onClick={handleLogout}
                            className="
                                px-4
                                py-2
                                rounded-lg
                                border border-white/[0.08]
                                bg-white/[0.02]
                                text-sm
                                text-slate-400
                                hover:text-white
                                hover:border-red-400/30
                                hover:bg-red-400/[0.05]
                                transition-all duration-200
                            "
                        >
                            Logout
                        </button>

                    </div>

                </div>
            </nav>

            {/* =================================================
                MAIN
            ================================================= */}

            <main
                className="
                    relative
                    z-10
                    max-w-7xl
                    mx-auto
                    px-5 sm:px-6
                    py-12
                "
            >

                {/* =================================================
                    HERO
                ================================================= */}

                <section className="mb-12">

                    <p
                        className="
                            text-[11px]
                            uppercase
                            tracking-[0.2em]
                            text-slate-500
                            mb-4
                        "
                    >
                        Security Center
                    </p>

                    <h1
                        className="
                            text-4xl
                            sm:text-5xl
                            font-semibold
                            tracking-tight
                            text-white
                        "
                    >
                        Welcome back, {username}
                    </h1>

                    <p
                        className="
                            mt-4
                            max-w-2xl
                            text-sm
                            sm:text-base
                            leading-7
                            text-slate-400
                        "
                    >
                        Manage your credentials, monitor account activity,
                        review trusted devices and identify suspicious
                        security events from one secure workspace.
                    </p>

                </section>

                {/* =================================================
                    STATUS
                ================================================= */}

                <section
                    className="
                        grid
                        grid-cols-1
                        md:grid-cols-3
                        gap-4
                        mb-14
                    "
                >

                    <StatusCard
                        title="Account Status"
                        value="Secure"
                        statusColor="text-emerald-400"
                        indicatorColor="bg-emerald-400"
                    />

                    <StatusCard
                        title="Authentication"
                        value="JWT Active"
                        statusColor="text-blue-400"
                        indicatorColor="bg-blue-400"
                    />

                    <StatusCard
                        title="Security Monitoring"
                        value="Active"
                        statusColor="text-violet-400"
                        indicatorColor="bg-violet-400"
                    />

                </section>

                {/* =================================================
                    SECURITY & VAULT
                ================================================= */}

                <section>

                    <div className="mb-6">

                        <p
                            className="
                                text-[11px]
                                uppercase
                                tracking-[0.18em]
                                text-slate-500
                                mb-2
                            "
                        >
                            Workspace
                        </p>

                        <h2
                            className="
                                text-2xl
                                font-semibold
                                tracking-tight
                                text-white
                            "
                        >
                            Security & Vault
                        </h2>

                    </div>

                    <div
                        className="
                            grid
                            grid-cols-1
                            md:grid-cols-2
                            lg:grid-cols-3
                            gap-4
                        "
                    >

                        {/* Personal Vault */}

                        <ModuleCard
                            title="Personal Vault"
                            description="Securely store and manage your personal credentials."
                            action="Open Vault →"
                            onClick={() => navigate("/vault")}
                            accent="bg-blue-400"
                        />

                        {/* Team Vault */}

                        <ModuleCard
                            title="Team Vault"
                            description="Manage shared credentials and team access."
                            action="Open Team Vault →"
                            onClick={() => navigate("/team-vault")}
                            accent="bg-violet-400"
                        />

                        {/* Login Activity */}

                        <ModuleCard
                            title="Login Activity"
                            description="Review successful and failed login attempts."
                            action="View Activity →"
                            onClick={() => navigate("/login-activity")}
                            accent="bg-emerald-400"
                        />

                        {/* Security Analytics */}

                        <ModuleCard
                            title="Security Analytics"
                            description="Analyze security events, risks and suspicious login behavior."
                            action="Open Analytics →"
                            onClick={() => navigate("/analytics")}
                            accent="bg-amber-400"
                        />

                        {/* Known Devices */}

                        <ModuleCard
                            title="Known Devices"
                            description="View devices that have accessed your SecureVault account."
                            action="View Devices →"
                            onClick={() => navigate("/known-devices")}
                            accent="bg-indigo-400"
                        />

                        {/* Security Alerts */}

                        <ModuleCard
                            title="Security Alerts"
                            description="Review suspicious activity and security warnings."
                            action="View Alerts →"
                            onClick={() => navigate("/security-alerts")}
                            accent="bg-rose-400"
                        />

                        {/* Reports */}

                        <ModuleCard
                            title="Reports & Export"
                            description="Generate security, password health, activity and threat reports."
                            action="View Reports →"
                            onClick={() => navigate("/reports")}
                            accent="bg-fuchsia-400"
                        />

                    </div>

                </section>

                {/* =================================================
                    SECURITY MONITORING
                ================================================= */}

                <section className="mt-14">

                    <div className="mb-6">

                        <p
                            className="
                                text-[11px]
                                uppercase
                                tracking-[0.18em]
                                text-slate-500
                                mb-2
                            "
                        >
                            Real-time protection
                        </p>

                        <h2
                            className="
                                text-2xl
                                font-semibold
                                tracking-tight
                            "
                        >
                            Security Monitoring
                        </h2>

                    </div>

                    <div
                        className="
                            rounded-2xl
                            border border-white/[0.07]
                            bg-[#0b1120]/70
                            backdrop-blur-xl
                            p-6
                            sm:p-7
                        "
                    >

                        <div
                            className="
                                flex
                                flex-col
                                lg:flex-row
                                lg:items-center
                                lg:justify-between
                                gap-6
                            "
                        >

                            <div>

                                <div
                                    className="
                                        flex
                                        items-center
                                        gap-3
                                        flex-wrap
                                    "
                                >

                                    <h3
                                        className="
                                            text-lg
                                            font-semibold
                                            text-white
                                        "
                                    >
                                        Threat Monitoring Active
                                    </h3>

                                    <span
                                        className="
                                            px-2
                                            py-1
                                            rounded-md
                                            border border-emerald-400/10
                                            bg-emerald-400/[0.05]
                                            text-[10px]
                                            uppercase
                                            tracking-wider
                                            text-emerald-400
                                        "
                                    >
                                        Active
                                    </span>

                                </div>

                                <p
                                    className="
                                        mt-3
                                        max-w-2xl
                                        text-sm
                                        leading-6
                                        text-slate-400
                                    "
                                >
                                    SecureVault monitors login activity,
                                    device activity and suspicious behavior
                                    to identify potential security threats.
                                </p>

                            </div>

                            <button
                                type="button"
                                onClick={() => navigate("/analytics")}
                                className="
                                    shrink-0
                                    px-5
                                    py-3
                                    rounded-lg
                                    border border-white/[0.08]
                                    bg-white/[0.04]
                                    text-sm
                                    font-medium
                                    text-slate-200
                                    hover:bg-white/[0.07]
                                    hover:border-white/[0.14]
                                    transition-all duration-200
                                "
                            >
                                Open Security Center →
                            </button>

                        </div>

                    </div>

                </section>

                {/* =================================================
                    QUICK ACTIONS
                ================================================= */}

                <section className="mt-14">

                    <div className="mb-6">

                        <p
                            className="
                                text-[11px]
                                uppercase
                                tracking-[0.18em]
                                text-slate-500
                                mb-2
                            "
                        >
                            Shortcuts
                        </p>

                        <h2
                            className="
                                text-2xl
                                font-semibold
                                tracking-tight
                            "
                        >
                            Quick Actions
                        </h2>

                    </div>

                    <div
                        className="
                            flex
                            flex-wrap
                            gap-2
                        "
                    >

                        <button
                            type="button"
                            onClick={() => navigate("/vault")}
                            className="
                                px-4
                                py-2.5
                                rounded-lg
                                border border-white/[0.07]
                                bg-white/[0.025]
                                text-sm
                                text-slate-400
                                hover:text-white
                                hover:bg-white/[0.05]
                                hover:border-white/[0.12]
                                transition-all
                            "
                        >
                            Add Credential
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate("/known-devices")}
                            className="
                                px-4
                                py-2.5
                                rounded-lg
                                border border-white/[0.07]
                                bg-white/[0.025]
                                text-sm
                                text-slate-400
                                hover:text-white
                                hover:bg-white/[0.05]
                                hover:border-white/[0.12]
                                transition-all
                            "
                        >
                            Devices
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate("/security-alerts")}
                            className="
                                px-4
                                py-2.5
                                rounded-lg
                                border border-white/[0.07]
                                bg-white/[0.025]
                                text-sm
                                text-slate-400
                                hover:text-white
                                hover:bg-white/[0.05]
                                hover:border-white/[0.12]
                                transition-all
                            "
                        >
                            Alerts
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate("/login-activity")}
                            className="
                                px-4
                                py-2.5
                                rounded-lg
                                border border-white/[0.07]
                                bg-white/[0.025]
                                text-sm
                                text-slate-400
                                hover:text-white
                                hover:bg-white/[0.05]
                                hover:border-white/[0.12]
                                transition-all
                            "
                        >
                            Login Activity
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate("/reports")}
                            className="
                                px-4
                                py-2.5
                                rounded-lg
                                border border-white/[0.07]
                                bg-white/[0.025]
                                text-sm
                                text-slate-400
                                hover:text-white
                                hover:bg-white/[0.05]
                                hover:border-white/[0.12]
                                transition-all
                            "
                        >
                            Reports
                        </button>

                    </div>

                </section>

            </main>

            {/* =================================================
                FOOTER
            ================================================= */}

            <footer
                className="
                    border-t
                    border-white/[0.06]
                    mt-16
                "
            >
                <div
                    className="
                        max-w-7xl
                        mx-auto
                        px-5 sm:px-6
                        py-7
                        flex
                        flex-col
                        sm:flex-row
                        items-center
                        justify-between
                        gap-3
                    "
                >

                    <p className="text-sm text-slate-500">
                        SecureVault
                    </p>

                    <p className="text-xs text-slate-600">
                        Secure Password Management System
                    </p>

                </div>
            </footer>

        </div>
    );
}

export default Dashboard;