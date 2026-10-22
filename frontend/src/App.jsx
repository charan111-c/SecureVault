import React from "react";
import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";

import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import Vault from "./pages/Vault";
import TeamVault from "./pages/TeamVault";
import LoginActivity from "./pages/LoginActivity";
import SecurityAnalytics from "./pages/SecurityAnalytics";
import KnownDevices from "./pages/KnownDevices";
import SecurityAlerts from "./pages/SecurityAlerts";
import MfaVerify from "./pages/MfaVerify";

// Reports
import Reports from "./pages/Reports";
import AdminDashboard from "./pages/AdminDashboard";


// ==========================================
// PROTECTED ROUTE
// ==========================================

function ProtectedRoute({ children }) {

    const token = localStorage.getItem("token");

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    return children;
}


// ==========================================
// APP
// ==========================================

function App() {

    return (
        <BrowserRouter>

            <Routes>

                {/* ==================================
                    HOME
                ================================== */}

                <Route
                    path="/"
                    element={<Home />}
                />


                {/* ==================================
                    AUTHENTICATION
                ================================== */}

                <Route
                    path="/login"
                    element={<Login />}
                />

                <Route
                    path="/register"
                    element={<Register />}
                />

                <Route
                    path="/forgot-password"
                    element={<ForgotPassword />}
                />

                <Route
                    path="/reset-password"
                    element={<ResetPassword />}
                />


                {/* ==================================
                    DASHBOARD
                ================================== */}

                <Route
                    path="/dashboard"
                    element={
                        <ProtectedRoute>
                            <Dashboard />
                        </ProtectedRoute>
                    }
                />


                {/* ==================================
                    PERSONAL VAULT
                ================================== */}

                <Route
                    path="/vault"
                    element={
                        <ProtectedRoute>
                            <Vault />
                        </ProtectedRoute>
                    }
                />


                {/* ==================================
                    TEAM VAULT
                ================================== */}

                <Route
                    path="/team-vault"
                    element={
                        <ProtectedRoute>
                            <TeamVault />
                        </ProtectedRoute>
                    }
                />


                {/* ==================================
                    LOGIN ACTIVITY
                ================================== */}

                <Route
                    path="/login-activity"
                    element={
                        <ProtectedRoute>
                            <LoginActivity />
                        </ProtectedRoute>
                    }
                />


                {/* ==================================
                    SECURITY ANALYTICS
                ================================== */}

                <Route
                    path="/analytics"
                    element={
                        <ProtectedRoute>
                            <SecurityAnalytics />
                        </ProtectedRoute>
                    }
                />


                {/* ==================================
                    KNOWN DEVICES
                ================================== */}

                <Route
                    path="/known-devices"
                    element={
                        <ProtectedRoute>
                            <KnownDevices />
                        </ProtectedRoute>
                    }
                />


                {/* ==================================
                    SECURITY ALERTS
                ================================== */}

                <Route
                    path="/security-alerts"
                    element={
                        <ProtectedRoute>
                            <SecurityAlerts />
                        </ProtectedRoute>
                    }
                />


                {/* ==================================
                    REPORTS & EXPORT
                ================================== */}

                <Route
                    path="/reports"
                    element={
                        <ProtectedRoute>
                            <Reports />
                        </ProtectedRoute>
                    }
                />


                {/* ==================================
                    MFA VERIFICATION
                ================================== */}

                <Route
                    path="/verify-mfa"
                    element={<MfaVerify />}
                />


                {/* ==================================
                    UNKNOWN ROUTES
                    MUST ALWAYS BE LAST
                ================================== */}

                <Route
                    path="*"
                    element={
                        <Navigate
                            to="/"
                            replace
                        />
                    }
                />
                <Route
    path="/admin-dashboard"
    element={<AdminDashboard />}
/>

            </Routes>

        </BrowserRouter>
    );
}

export default App;