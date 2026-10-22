import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function SecurityAlerts() {
    const navigate = useNavigate();

    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const token = localStorage.getItem("token");

        if (!token) {
            navigate("/login", { replace: true });
            return;
        }

        fetch("http://localhost:8080/api/security-events", {
            method: "GET",
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then(async (response) => {
                const data = await response.json();

                if (!response.ok) {
                    throw new Error(
                        data.message || "Failed to load security alerts"
                    );
                }

                return data;
            })
            .then((data) => {
                setEvents(data);
            })
            .catch((err) => {
                setError(err.message);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [navigate]);

    const getEventStyle = (event) => {

        if (
            event.eventType === "ANOMALOUS_LOGIN" ||
            event.loginStatus === "FAILURE"
        ) {
            return "border-red-500/30 bg-red-500/10";
        }

        return "border-green-500/20 bg-green-500/10";
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white">

            <header className="border-b border-slate-800">
                <div className="max-w-7xl mx-auto px-6 py-5 flex justify-between items-center">

                    <div>
                        <h1 className="text-3xl font-bold text-red-400">
                             Security Alerts
                        </h1>

                        <p className="text-slate-400 mt-1">
                            Suspicious activity and security events
                        </p>
                    </div>

                    <button
                        onClick={() => navigate("/")}
                        className="px-5 py-2 rounded-lg border border-slate-700 hover:bg-slate-800"
                    >
                        ← Home
                    </button>

                </div>
            </header>

            <main className="max-w-7xl mx-auto px-6 py-10">

                {loading && (
                    <div className="text-center text-cyan-400">
                        Loading security events...
                    </div>
                )}

                {error && (
                    <div className="rounded-xl border border-red-500/30 bg-red-500/10 p-5 text-red-400">
                        {error}
                    </div>
                )}

                {!loading && !error && events.length === 0 && (
                    <div className="rounded-2xl border border-green-500/20 bg-green-500/10 p-10 text-center">

                        <div className="text-5xl mb-4">
                            🛡️
                        </div>

                        <h2 className="text-2xl font-bold text-green-400">
                            No Security Alerts
                        </h2>

                        <p className="text-slate-400 mt-2">
                            No suspicious security activity has been detected.
                        </p>

                    </div>
                )}

                {!loading && !error && events.length > 0 && (
                    <div className="space-y-5">

                        {events.map((event) => (

                            <div
                                key={event.id}
                                className={`rounded-2xl border p-6 ${getEventStyle(event)}`}
                            >

                                <div className="flex justify-between items-start gap-5">

                                    <div>

                                        <div className="flex items-center gap-3">

                                            <span className="text-3xl">
                                                {event.eventType ===
                                                "ANOMALOUS_LOGIN"
                                                    ? ""
                                                    : event.loginStatus ===
                                                      "FAILURE"
                                                    ? ""
                                                    : ""}
                                            </span>

                                            <div>

                                                <h2 className="text-xl font-bold">
                                                    {event.eventType}
                                                </h2>

                                                <p className="text-sm text-slate-400">
                                                    Login status:{" "}
                                                    {event.loginStatus}
                                                </p>

                                            </div>

                                        </div>

                                    </div>

                                    <span
                                        className={
                                            event.loginStatus === "FAILURE"
                                                ? "text-red-400"
                                                : "text-green-400"
                                        }
                                    >
                                        {event.loginStatus}
                                    </span>

                                </div>

                                <div className="grid gap-4 md:grid-cols-3 mt-6 text-sm">

                                    <div>
                                        <span className="text-slate-500">
                                            Event Time
                                        </span>

                                        <p className="mt-1">
                                            {event.eventTime
                                                ? new Date(
                                                    event.eventTime
                                                ).toLocaleString()
                                                : "Unknown"}
                                        </p>
                                    </div>

                                    <div>
                                        <span className="text-slate-500">
                                            IP Address
                                        </span>

                                        <p className="mt-1">
                                            {event.ipAddress || "Unknown"}
                                        </p>
                                    </div>

                                    <div>
                                        <span className="text-slate-500">
                                            Browser
                                        </span>

                                        <p className="mt-1 break-words">
                                            {event.userAgent || "Unknown"}
                                        </p>
                                    </div>

                                </div>

                            </div>

                        ))}

                    </div>
                )}

            </main>

        </div>
    );
}

export default SecurityAlerts;