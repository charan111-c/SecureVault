
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE = "http://localhost:8080/api/reports";

const REPORT_ENDPOINTS = [
  {
    name: "Security Report",
    key: "security",
    description:
      "Overview of your SecureVault security status, credentials and notifications.",
  },
  {
    name: "Password Health Report",
    key: "password-health",
    description:
      "Analyze the strength and health of your stored passwords.",
  },
  {
    name: "User Activity Report",
    key: "user-activity",
    description:
      "Review your account activity and security-related events.",
  },
  {
    name: "Threat Monitoring Report",
    key: "threat-monitoring",
    description:
      "Review suspicious activity and potential security threats.",
  },
];

function StatCard({ label, value }) {
  return (
    <div className="rounded-xl border border-slate-700 bg-slate-800/60 p-4">
      <p className="text-xs font-medium uppercase tracking-wider text-slate-400">
        {label}
      </p>
      <p className="mt-2 text-2xl font-semibold text-white">
        {value}
      </p>
    </div>
  );
}

function Reports() {
  const navigate = useNavigate();

  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [downloading, setDownloading] = useState("");

  const email = localStorage.getItem("email") || "User";

  const getToken = () => localStorage.getItem("token");

  useEffect(() => {
    const controller = new AbortController();

    async function loadReports() {
      const token = getToken();

      if (!token) {
        navigate("/login", { replace: true });
        return;
      }

      try {
        setLoading(true);
        setError("");

        const results = await Promise.all(
          REPORT_ENDPOINTS.map(async (report) => {
            const response = await fetch(
              `${API_BASE}/${report.key}`,
              {
                method: "GET",
                headers: {
                  Authorization: `Bearer ${token}`,
                  Accept: "application/json",
                },
                signal: controller.signal,
              }
            );

            const data = await response.json().catch(() => null);

            if (!response.ok) {
              throw new Error(
                data?.message || `Failed to load ${report.name}.`
              );
            }

            return {
              ...report,
              data: data && typeof data === "object" ? data : {},
            };
          })
        );

        setReports(results);
      } catch (err) {
        if (err.name !== "AbortError") {
          console.error("Report loading error:", err);
          setError(err.message || "Unable to load reports.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadReports();

    return () => controller.abort();
  }, [navigate]);

  const downloadReport = async (reportKey, format, reportName) => {
    const token = getToken();

    if (!token) {
      navigate("/login", { replace: true });
      return;
    }

    const downloadKey = `${reportKey}-${format}`;

    try {
      setDownloading(downloadKey);
      setError("");

      const response = await fetch(
        `${API_BASE}/${reportKey}/${format}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const data = await response.json().catch(() => null);
        throw new Error(
          data?.message || `Failed to download ${reportName}.`
        );
      }

      const blob = await response.blob();
      const fileUrl = window.URL.createObjectURL(blob);
      const link = document.createElement("a");

      link.href = fileUrl;
      link.download = `${reportKey}-report.${
        format === "pdf" ? "pdf" : "xlsx"
      }`;

      document.body.appendChild(link);
      link.click();
      link.remove();

      window.setTimeout(() => {
        window.URL.revokeObjectURL(fileUrl);
      }, 1000);
    } catch (err) {
      console.error("Report download error:", err);
      setError(
        err.message ||
          `Unable to download ${reportName}. Please try again.`
      );
    } finally {
      setDownloading("");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    navigate("/login", { replace: true });
  };

  return (
    <div className="min-h-screen bg-[#070b14] text-white">
      {/* Navigation */}

      <nav className="sticky top-0 z-50 border-b border-slate-800 bg-slate-950/90 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
          <button
            type="button"
            onClick={() => navigate("/dashboard")}
            className="text-left focus:outline-none focus:ring-2 focus:ring-cyan-500 rounded-lg"
          >
            <p className="text-xl font-bold tracking-tight text-white">
              SecureVault
            </p>
            <p className="mt-1 text-xs text-slate-400">
              Security Reports
            </p>
          </button>

          <div className="flex items-center gap-4">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium text-slate-200">
                {email}
              </p>
              <p className="mt-1 text-xs text-emerald-400">
                Authenticated
              </p>
            </div>

            <button
              type="button"
              onClick={handleLogout}
              className="rounded-xl border border-red-500/30 px-4 py-2.5 text-sm font-semibold text-red-300 transition hover:bg-red-500/10 focus:outline-none focus:ring-2 focus:ring-red-500/40"
            >
              Logout
            </button>
          </div>
        </div>
      </nav>

      {/* Main Content */}

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10 lg:px-8">
        {/* Page Heading */}

        <section className="mb-10">
          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.2em] text-cyan-400">
            Security Center
          </p>

          <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
            Reports &amp; Export
          </h1>

          <p className="mt-3 max-w-3xl text-sm leading-6 text-slate-400 sm:text-base">
            Generate reports from your existing SecureVault data
            and download them as PDF or Excel files.
          </p>
        </section>

        {/* Error Message */}

        {error && (
          <div
            role="alert"
            className="mb-6 rounded-xl border border-red-500/30 bg-red-500/10 p-4"
          >
            <p className="text-sm font-medium text-red-300">
              {error}
            </p>

            <button
              type="button"
              onClick={() => setError("")}
              className="mt-2 text-xs font-semibold text-red-200 underline underline-offset-4"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Loading State */}

        {loading && (
          <section
            className="flex min-h-64 items-center justify-center rounded-2xl border border-slate-800 bg-slate-900/70 p-8"
            aria-live="polite"
          >
            <div className="text-center">
              <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-slate-700 border-t-cyan-400" />

              <p className="mt-4 text-sm font-semibold text-slate-200">
                Loading reports
              </p>

              <p className="mt-1 text-sm text-slate-500">
                Retrieving your latest report information.
              </p>
            </div>
          </section>
        )}

        {/* Report Cards */}

        {!loading && !error && reports.length > 0 && (
          <section>
            <div className="mb-5 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <h2 className="text-lg font-semibold text-slate-100">
                  Available reports
                </h2>
                <p className="mt-1 text-sm text-slate-400">
                  Select a format to export a report.
                </p>
              </div>

              <p className="text-sm text-slate-500">
                {reports.length} reports available
              </p>
            </div>

            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              {reports.map((report) => (
                <article
                  key={report.key}
                  className="rounded-2xl border border-slate-800 bg-slate-900/80 p-5 shadow-lg shadow-black/10 transition duration-200 hover:border-cyan-500/30 sm:p-6"
                >
                  {/* Report Header */}

                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <h2 className="text-lg font-semibold text-white sm:text-xl">
                        {report.name}
                      </h2>

                      <p className="mt-2 text-xs font-medium text-emerald-300">
                        Report available
                      </p>
                    </div>

                    <span className="shrink-0 rounded-full border border-slate-700 bg-slate-800 px-3 py-1 text-xs font-medium text-slate-300">
                      {report.key}
                    </span>
                  </div>

                  {/* Description */}

                  <p className="mt-5 text-sm leading-6 text-slate-400">
                    {report.description}
                  </p>

                  {/* Report Summary */}

                  <div className="mt-5 grid grid-cols-2 gap-3">
                    <StatCard
                      label="Credentials"
                      value={report.data?.totalCredentials ?? 0}
                    />

                    <StatCard
                      label="Notifications"
                      value={report.data?.totalNotifications ?? 0}
                    />
                  </div>

                  {/* Download Buttons */}

                  <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-2">
                    <button
                      type="button"
                      disabled={Boolean(downloading)}
                      onClick={() =>
                        downloadReport(
                          report.key,
                          "pdf",
                          report.name
                        )
                      }
                      className="rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm font-semibold text-red-300 transition hover:bg-red-500/20 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      {downloading === `${report.key}-pdf`
                        ? "Downloading PDF..."
                        : "Download PDF"}
                    </button>

                    <button
                      type="button"
                      disabled={Boolean(downloading)}
                      onClick={() =>
                        downloadReport(
                          report.key,
                          "excel",
                          report.name
                        )
                      }
                      className="rounded-xl border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm font-semibold text-emerald-300 transition hover:bg-emerald-500/20 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      {downloading === `${report.key}-excel`
                        ? "Downloading Excel..."
                        : "Download Excel"}
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </section>
        )}

        {/* Return to Dashboard */}

        {!loading && (
          <div className="mt-10">
            <button
              type="button"
              onClick={() => navigate("/dashboard")}
              className="rounded-xl border border-slate-700 bg-slate-900 px-5 py-3 text-sm font-semibold text-slate-200 transition hover:border-cyan-500/40 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500/40"
            >
              Back to Dashboard
            </button>
          </div>
        )}
      </main>

      {/* Footer */}

      <footer className="mt-10 border-t border-slate-800 bg-slate-950/60">
        <div className="mx-auto max-w-7xl px-4 py-6 text-center sm:px-6 lg:px-8">
          <p className="text-xs text-slate-500">
            SecureVault · Reports &amp; Export
          </p>
        </div>
      </footer>
    </div>
  );
}

export default Reports;
