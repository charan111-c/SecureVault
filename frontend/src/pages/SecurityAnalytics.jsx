import { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function SecurityAnalytics() {
    const [analytics, setAnalytics] = useState(null);
    const [period, setPeriod] = useState("last7days");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");

    const fetchAnalytics = async (selectedPeriod = period) => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                `${API_BASE_URL}/api/analytics?period=${selectedPeriod}`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                }
            );

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    throw new Error(
                        "You are not authorized to view security analytics."
                    );
                }

                throw new Error("Failed to load security analytics.");
            }

            const data = await response.json();

            setAnalytics(data);
        } catch (err) {
            console.error("Analytics error:", err);
            setError(err.message || "Unable to load analytics.");
            setAnalytics(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAnalytics(period);
    }, [period]);

    return (
        <div
            style={{
                minHeight: "100vh",
                background:
                    "linear-gradient(135deg, #020617, #0f172a, #111827)",
                color: "white",
                padding: "40px",
            }}
        >
            {/* HEADER */}

            <div
                style={{
                    maxWidth: "1200px",
                    margin: "0 auto",
                }}
            >
                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        marginBottom: "35px",
                        gap: "20px",
                        flexWrap: "wrap",
                    }}
                >
                    <div>
                        <h1
                            style={{
                                fontSize: "36px",
                                fontWeight: "700",
                                marginBottom: "8px",
                            }}
                        >
                            Security Analytics
                        </h1>

                        <p
                            style={{
                                color: "#94a3b8",
                                fontSize: "16px",
                            }}
                        >
                            Monitor your account security activity
                        </p>
                    </div>

                    {/* PERIOD FILTER */}

                    <select
                        value={period}
                        onChange={(e) => setPeriod(e.target.value)}
                        style={{
                            background: "#1e293b",
                            color: "white",
                            border: "1px solid #334155",
                            borderRadius: "10px",
                            padding: "12px 18px",
                            fontSize: "15px",
                            outline: "none",
                            cursor: "pointer",
                        }}
                    >
                        <option value="today">Today</option>
                        <option value="last7days">Last 7 Days</option>
                    </select>
                </div>

                {/* LOADING */}

                {loading && (
                    <div
                        style={{
                            textAlign: "center",
                            padding: "80px 20px",
                            color: "#94a3b8",
                        }}
                    >
                        <div
                            style={{
                                fontSize: "40px",
                                marginBottom: "15px",
                            }}
                        >
                            
                        </div>

                        <p>Loading security analytics...</p>
                    </div>
                )}

                {/* ERROR */}

                {!loading && error && (
                    <div
                        style={{
                            background: "#450a0a",
                            border: "1px solid #991b1b",
                            borderRadius: "14px",
                            padding: "25px",
                            textAlign: "center",
                        }}
                    >
                        <div
                            style={{
                                fontSize: "32px",
                                marginBottom: "10px",
                            }}
                        >
                            
                        </div>

                        <p
                            style={{
                                color: "#fecaca",
                                marginBottom: "15px",
                            }}
                        >
                            {error}
                        </p>

                        <button
                            onClick={() => fetchAnalytics(period)}
                            style={{
                                background: "#dc2626",
                                color: "white",
                                border: "none",
                                borderRadius: "8px",
                                padding: "10px 18px",
                                cursor: "pointer",
                                fontWeight: "600",
                            }}
                        >
                            Try Again
                        </button>
                    </div>
                )}

                {/* ANALYTICS */}

                {!loading && !error && analytics && (
                    <>
                        {/* METRIC CARDS */}

                        <div
                            style={{
                                display: "grid",
                                gridTemplateColumns:
                                    "repeat(auto-fit, minmax(220px, 1fr))",
                                gap: "20px",
                                marginBottom: "30px",
                            }}
                        >
                            <MetricCard
                                icon=""
                                title="Successful Logins"
                                value={analytics.successfulLogins ?? 0}
                                description="Successful authentication attempts"
                            />

                            <MetricCard
                                icon=""
                                title="Failed Logins"
                                value={analytics.failedLogins ?? 0}
                                description="Failed authentication attempts"
                            />

                            <MetricCard
                                icon=""
                                title="Total Attempts"
                                value={analytics.totalLoginAttempts ?? 0}
                                description="Total login attempts"
                            />

                            <MetricCard
                                icon=""
                                title="Suspicious Activities"
                                value={
                                    analytics.suspiciousActivities ?? 0
                                }
                                description="Detected suspicious activities"
                            />

                            <MetricCard
                                icon=""
                                title="Active Alerts"
                                value={
                                    analytics.activeSecurityAlerts ?? 0
                                }
                                description="Currently active alerts"
                            />
                        </div>

                        {/* PERIOD INFORMATION */}

                        <div
                            style={{
                                background: "#0f172a",
                                border: "1px solid #1e293b",
                                borderRadius: "14px",
                                padding: "20px",
                                marginBottom: "30px",
                            }}
                        >
                            <h2
                                style={{
                                    fontSize: "20px",
                                    marginBottom: "10px",
                                }}
                            >
                                Analytics Period
                            </h2>

                            <p
                                style={{
                                    color: "#94a3b8",
                                }}
                            >
                                {analytics.startDate
                                    ? new Date(
                                          analytics.startDate
                                      ).toLocaleString()
                                    : "N/A"}

                                {" → "}

                                {analytics.endDate
                                    ? new Date(
                                          analytics.endDate
                                      ).toLocaleString()
                                    : "N/A"}
                            </p>
                        </div>

                        {/* RECENT ACTIVITY */}

                        <div
                            style={{
                                background: "#0f172a",
                                border: "1px solid #1e293b",
                                borderRadius: "14px",
                                overflow: "hidden",
                            }}
                        >
                            <div
                                style={{
                                    padding: "22px",
                                    borderBottom:
                                        "1px solid #1e293b",
                                }}
                            >
                                <h2
                                    style={{
                                        fontSize: "22px",
                                        fontWeight: "600",
                                    }}
                                >
                                    Recent Security Activity
                                </h2>

                                <p
                                    style={{
                                        color: "#64748b",
                                        marginTop: "5px",
                                    }}
                                >
                                    Recent login activity for your account
                                </p>
                            </div>

                            {analytics.recentActivity &&
                            analytics.recentActivity.length > 0 ? (
                                <div
                                    style={{
                                        overflowX: "auto",
                                    }}
                                >
                                    <table
                                        style={{
                                            width: "100%",
                                            borderCollapse: "collapse",
                                        }}
                                    >
                                        <thead>
                                            <tr
                                                style={{
                                                    background: "#111827",
                                                }}
                                            >
                                                <th
                                                    style={
                                                        tableHeaderStyle
                                                    }
                                                >
                                                    Status
                                                </th>

                                                <th
                                                    style={
                                                        tableHeaderStyle
                                                    }
                                                >
                                                    Date & Time
                                                </th>

                                                <th
                                                    style={
                                                        tableHeaderStyle
                                                    }
                                                >
                                                    IP Address
                                                </th>

                                                <th
                                                    style={
                                                        tableHeaderStyle
                                                    }
                                                >
                                                    Device / Browser
                                                </th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            {analytics.recentActivity.map(
                                                (activity) => (
                                                    <tr
                                                        key={
                                                            activity.id
                                                        }
                                                        style={{
                                                            borderTop:
                                                                "1px solid #1e293b",
                                                        }}
                                                    >
                                                        <td
                                                            style={
                                                                tableCellStyle
                                                            }
                                                        >
                                                            <span
                                                                style={{
                                                                    display:
                                                                        "inline-block",
                                                                    padding:
                                                                        "6px 10px",
                                                                    borderRadius:
                                                                        "999px",
                                                                    fontSize:
                                                                        "12px",
                                                                    fontWeight:
                                                                        "600",
                                                                    background:
                                                                        activity.loginStatus ===
                                                                        "SUCCESS"
                                                                            ? "#064e3b"
                                                                            : "#450a0a",
                                                                    color:
                                                                        activity.loginStatus ===
                                                                        "SUCCESS"
                                                                            ? "#6ee7b7"
                                                                            : "#fca5a5",
                                                                }}
                                                            >
                                                                {activity.loginStatus}
                                                            </span>
                                                        </td>

                                                        <td
                                                            style={
                                                                tableCellStyle
                                                            }
                                                        >
                                                            {activity.eventTime
                                                                ? new Date(
                                                                      activity.eventTime
                                                                  ).toLocaleString()
                                                                : "N/A"}
                                                        </td>

                                                        <td
                                                            style={
                                                                tableCellStyle
                                                            }
                                                        >
                                                            {activity.ipAddress ||
                                                                "Unavailable"}
                                                        </td>

                                                        <td
                                                            style={{
                                                                ...tableCellStyle,
                                                                maxWidth:
                                                                    "350px",
                                                                wordBreak:
                                                                    "break-word",
                                                            }}
                                                        >
                                                            {activity.userAgent ||
                                                                "Unavailable"}
                                                        </td>
                                                    </tr>
                                                )
                                            )}
                                        </tbody>
                                    </table>
                                </div>
                            ) : (
                                <div
                                    style={{
                                        padding: "70px 20px",
                                        textAlign: "center",
                                        color: "#64748b",
                                    }}
                                >
                                    <div
                                        style={{
                                            fontSize: "40px",
                                            marginBottom: "15px",
                                        }}
                                    >
                                        🔒
                                    </div>

                                    <h3
                                        style={{
                                            color: "#cbd5e1",
                                            marginBottom: "8px",
                                        }}
                                    >
                                        No Security Activity
                                    </h3>

                                    <p>
                                        There are no login events for
                                        the selected period.
                                    </p>
                                </div>
                            )}
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}

function MetricCard({
    icon,
    title,
    value,
    description,
}) {
    return (
        <div
            style={{
                background:
                    "linear-gradient(145deg, #0f172a, #111827)",
                border: "1px solid #1e293b",
                borderRadius: "16px",
                padding: "24px",
                boxShadow:
                    "0 10px 30px rgba(0,0,0,0.2)",
            }}
        >
            <div
                style={{
                    fontSize: "28px",
                    marginBottom: "15px",
                }}
            >
                {icon}
            </div>

            <p
                style={{
                    color: "#94a3b8",
                    fontSize: "14px",
                    marginBottom: "8px",
                }}
            >
                {title}
            </p>

            <h3
                style={{
                    fontSize: "32px",
                    fontWeight: "700",
                    marginBottom: "8px",
                }}
            >
                {value}
            </h3>

            <p
                style={{
                    color: "#64748b",
                    fontSize: "12px",
                }}
            >
                {description}
            </p>
        </div>
    );
}

const tableHeaderStyle = {
    textAlign: "left",
    padding: "15px 18px",
    color: "#94a3b8",
    fontSize: "13px",
    fontWeight: "600",
};

const tableCellStyle = {
    padding: "16px 18px",
    color: "#cbd5e1",
    fontSize: "14px",
    verticalAlign: "top",
};

export default SecurityAnalytics;