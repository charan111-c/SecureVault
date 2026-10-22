
import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_URL = "http://localhost:8080/api/devices";

function getBrowserName(userAgent = "") {
  const agent = userAgent.toLowerCase();

  if (agent.includes("edg/")) return "Microsoft Edge";
  if (agent.includes("opr/") || agent.includes("opera")) return "Opera";
  if (agent.includes("chrome/")) return "Google Chrome";
  if (agent.includes("firefox/")) return "Mozilla Firefox";
  if (agent.includes("safari/")) return "Safari";

  return "Unknown browser";
}

function getOperatingSystem(userAgent = "") {
  const agent = userAgent.toLowerCase();

  if (agent.includes("windows")) return "Windows";

  if (
    agent.includes("iphone") ||
    agent.includes("ipad") ||
    agent.includes("ios")
  ) {
    return "iOS";
  }

  if (agent.includes("android")) return "Android";

  if (
    agent.includes("mac os") ||
    agent.includes("macintosh")
  ) {
    return "macOS";
  }

  if (agent.includes("linux")) return "Linux";

  return "Unknown OS";
}

function formatDate(value) {
  if (!value) return "Not available";

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Not available";
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function StatCard({ label, value, description }) {
  return (
    <section className="rounded-2xl border border-slate-800 bg-slate-900/80 p-5 shadow-lg shadow-black/10">
      <p className="text-sm font-medium text-slate-400">
        {label}
      </p>

      <p className="mt-2 text-3xl font-semibold tracking-tight text-white">
        {value}
      </p>

      <p className="mt-1 text-sm text-slate-500">
        {description}
      </p>
    </section>
  );
}

function KnownDevices() {
  const navigate = useNavigate();

  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login", { replace: true });

      return () => controller.abort();
    }

    async function loadDevices() {
      try {
        setLoading(true);
        setError("");

        const response = await fetch(API_URL, {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            Accept: "application/json",
          },
          signal: controller.signal,
        });

        const data = await response.json().catch(() => null);

        if (!response.ok) {
          throw new Error(
            data?.message || "Unable to load known devices."
          );
        }

        setDevices(Array.isArray(data) ? data : []);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError(
            err.message ||
              "Something went wrong while loading devices."
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadDevices();

    return () => controller.abort();
  }, [navigate]);

  const totalLogins = useMemo(
    () =>
      devices.reduce(
        (total, device) =>
          total + Number(device.loginCount || 0),
        0
      ),
    [devices]
  );

  const latestDevice = useMemo(() => {
    if (devices.length === 0) return null;

    return [...devices].sort(
      (a, b) =>
        new Date(b.lastSeen || 0).getTime() -
        new Date(a.lastSeen || 0).getTime()
    )[0];
  }, [devices]);

  return (
    <div className="min-h-screen bg-[#070b14] text-white">
      {/* Header */}

      <header className="border-b border-slate-800 bg-slate-950/80 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl flex-col gap-5 px-4 py-6 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-400">
              SecureVault
              <span className="px-1 text-slate-600">/</span>
              Security Center
            </p>

            <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">
              Known Devices
            </h1>

            <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-400">
              Review the devices that have accessed your account
              and check their recent activity.
            </p>
          </div>

          <button
            type="button"
            onClick={() => navigate("/")}
            className="inline-flex items-center justify-center rounded-xl border border-slate-700 bg-slate-900 px-5 py-3 text-sm font-semibold text-slate-200 transition hover:border-slate-600 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:ring-offset-2 focus:ring-offset-slate-950"
          >
            Back to Home
          </button>
        </div>
      </header>

      {/* Main Content */}

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        {/* Loading State */}

        {loading && (
          <section
            className="flex min-h-72 items-center justify-center rounded-2xl border border-slate-800 bg-slate-900/70 p-8"
            aria-live="polite"
          >
            <div className="text-center">
              <div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-slate-700 border-t-cyan-400" />

              <p className="mt-4 text-sm font-semibold text-slate-200">
                Loading known devices
              </p>

              <p className="mt-1 text-sm text-slate-500">
                Retrieving your device information.
              </p>
            </div>
          </section>
        )}

        {/* Error State */}

        {!loading && error && (
          <section
            role="alert"
            className="rounded-2xl border border-red-500/30 bg-red-500/10 p-6"
          >
            <h2 className="font-semibold text-red-300">
              Unable to load devices
            </h2>

            <p className="mt-2 text-sm text-red-200/80">
              {error}
            </p>

            <button
              type="button"
              onClick={() => window.location.reload()}
              className="mt-4 rounded-xl border border-red-400/30 px-4 py-2.5 text-sm font-semibold text-red-200 transition hover:bg-red-500/10"
            >
              Try Again
            </button>
          </section>
        )}

        {/* Loaded Content */}

        {!loading && !error && (
          <>
            {/* Statistics */}

            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <StatCard
                label="Known devices"
                value={devices.length}
                description="Devices associated with your account"
              />

              <StatCard
                label="Total logins"
                value={totalLogins}
                description="Combined login count across devices"
              />

              <StatCard
                label="Latest activity"
                value={
                  latestDevice
                    ? formatDate(latestDevice.lastSeen)
                    : "—"
                }
                description="Most recent recorded device access"
              />
            </div>

            {/* Empty State */}

            {devices.length === 0 ? (
              <section className="mt-6 rounded-2xl border border-dashed border-slate-700 bg-slate-900/60 px-6 py-14 text-center">
                <h2 className="text-lg font-semibold text-white">
                  No known devices
                </h2>

                <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-slate-400">
                  No device records are available for your account
                  yet. Sign in from a device to see its information
                  here.
                </p>

                <button
                  type="button"
                  onClick={() => navigate("/login")}
                  className="mt-5 rounded-xl bg-cyan-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-400 focus:ring-offset-2 focus:ring-offset-slate-950"
                >
                  Go to Login
                </button>
              </section>
            ) : (
              <>
                {/* Device List Header */}

                <section className="mt-8">
                  <div className="mb-5 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                    <div>
                      <h2 className="text-lg font-semibold text-slate-100">
                        Your devices
                      </h2>

                      <p className="mt-1 text-sm text-slate-400">
                        Review device details and recent access
                        information.
                      </p>
                    </div>

                    <span className="text-sm font-medium text-slate-500">
                      {devices.length}{" "}
                      {devices.length === 1 ? "device" : "devices"}
                    </span>
                  </div>

                  {/* Device Cards */}

                  <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
                    {devices.map((device) => (
                      <article
                        key={device.id}
                        className="overflow-hidden rounded-2xl border border-slate-800 bg-slate-900/80 shadow-lg shadow-black/10 transition duration-200 hover:border-cyan-500/30 hover:shadow-cyan-950/20"
                      >
                        {/* Card Header */}

                        <div className="border-b border-slate-800 bg-gradient-to-br from-slate-900 to-slate-950 p-5">
                          <div className="flex items-start justify-between gap-3">
                            <div>
                              <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                                Device
                              </p>

                              <h3 className="mt-1 text-lg font-semibold text-white">
                                Device #{device.id}
                              </h3>
                            </div>

                            <span className="inline-flex rounded-full border border-emerald-500/20 bg-emerald-500/10 px-3 py-1.5 text-xs font-semibold text-emerald-300">
                              Known
                            </span>
                          </div>

                          <p className="mt-3 text-sm text-slate-400">
                            {getOperatingSystem(device.userAgent)}

                            <span className="px-2 text-slate-600">
                              /
                            </span>

                            {getBrowserName(device.userAgent)}
                          </p>
                        </div>

                        {/* Device Details */}

                        <div className="space-y-5 p-5">
                          <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                              IP address
                            </p>

                            <p className="mt-2 break-all rounded-xl border border-slate-800 bg-slate-950 px-4 py-3 font-mono text-sm text-slate-300">
                              {device.ipAddress || "Not available"}
                            </p>
                          </div>

                          <div className="flex items-center justify-between rounded-xl border border-slate-800 bg-slate-950 px-4 py-3">
                            <div>
                              <p className="text-sm font-medium text-slate-300">
                                Login count
                              </p>

                              <p className="mt-1 text-xs text-slate-500">
                                Recorded sign-ins
                              </p>
                            </div>

                            <p className="text-xl font-semibold tabular-nums text-white">
                              {Number(device.loginCount || 0)}
                            </p>
                          </div>

                          <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                              First seen
                            </p>

                            <p className="mt-2 text-sm text-slate-300">
                              {formatDate(device.firstSeen)}
                            </p>
                          </div>

                          <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                              Last seen
                            </p>

                            <p className="mt-2 text-sm font-medium text-emerald-300">
                              {formatDate(device.lastSeen)}
                            </p>
                          </div>

                          <div>
                            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                              Browser user agent
                            </p>

                            <p className="mt-2 break-words rounded-xl border border-slate-800 bg-slate-950 p-3 text-xs leading-5 text-slate-400">
                              {device.userAgent || "Not available"}
                            </p>
                          </div>
                        </div>

                        {/* Card Footer */}

                        <div className="border-t border-slate-800 bg-slate-950/50 px-5 py-3">
                          <p className="text-xs text-slate-500">
                            Device activity is based on information
                            returned by your account service.
                          </p>
                        </div>
                      </article>
                    ))}
                  </div>
                </section>
              </>
            )}

            {/* Security Guidance */}

            {devices.length > 0 && (
              <aside className="mt-8 rounded-2xl border border-cyan-500/15 bg-cyan-500/[0.04] p-5">
                <h2 className="font-semibold text-slate-200">
                  Security guidance
                </h2>

                <p className="mt-2 text-sm leading-6 text-slate-400">
                  If you see a device or login activity you do not
                  recognize, change your password and review your
                  account security settings.
                </p>
              </aside>
            )}
          </>
        )}
      </main>

      {/* Footer */}

      <footer className="border-t border-slate-800 bg-slate-950/60">
        <div className="mx-auto max-w-7xl px-4 py-5 text-center text-xs text-slate-500 sm:px-6 lg:px-8">
          SecureVault · Known Devices · Security Monitoring
        </div>
      </footer>
    </div>
  );
}

export default KnownDevices;
