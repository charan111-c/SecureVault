import { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function TeamVault() {

    // =========================================================

    // TEAMS

    // =========================================================

    const [teams, setTeams] = useState([]);

    const [selectedTeam, setSelectedTeam] = useState(null);

    const [teamName, setTeamName] = useState("");

    // =========================================================

    // MEMBERS

    // =========================================================

    const [members, setMembers] = useState([]);

    const [memberEmail, setMemberEmail] = useState("");

    const [memberRole, setMemberRole] = useState("MEMBER");

    // =========================================================

    // PERSONAL CREDENTIALS

    // =========================================================

    const [credentials, setCredentials] = useState([]);

    // =========================================================

    // TEAM CREDENTIALS

    // =========================================================

    const [teamCredentials, setTeamCredentials] = useState([]);

    const [visiblePasswords, setVisiblePasswords] = useState({});

    // =========================================================

    // TEAM ACCESS

    // =========================================================

    const [selectedCredential, setSelectedCredential] = useState("");

    const [selectedMember, setSelectedMember] = useState("");

    const [permissionLevel, setPermissionLevel] =

        useState("VIEW_ONLY");

    const [expirationDate, setExpirationDate] = useState("");

    const [accessList, setAccessList] = useState([]);

    // =========================================================

    // EDIT STATE

    // =========================================================

    const [editingCredentialKey, setEditingCredentialKey] =

        useState(null);

    const [editForm, setEditForm] = useState({

        title: "",

        username: "",

        password: ""

    });

    const [savingCredential, setSavingCredential] =

        useState(false);

    // =========================================================

    // GENERAL STATE

    // =========================================================

    const [loading, setLoading] = useState(false);

    const [memberLoading, setMemberLoading] = useState(false);

    const [credentialLoading, setCredentialLoading] =

        useState(false);

    const [accessLoading, setAccessLoading] =

        useState(false);

    const [message, setMessage] = useState("");

    const [error, setError] = useState("");

    // =========================================================

    // AUTH

    // =========================================================

    const token = localStorage.getItem("token");

    const authHeaders = {

        Authorization: `Bearer ${token}`,

        "Content-Type": "application/json"

    };

    // =========================================================

    // INITIAL LOAD

    // =========================================================

    useEffect(() => {

        if (!token) {

            setError("You are not logged in.");

            return;

        }

        loadTeams();

        loadCredentials();

        loadMyTeamCredentials();

    }, []);

    // =========================================================

    // LOAD TEAMS

    // =========================================================

    async function loadTeams() {

        try {

            setLoading(true);

            setError("");

            const response = await fetch(

                `${API_BASE_URL}/api/teams`,

                {

                    method: "GET",

                    headers: authHeaders

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to load teams."

                );

            }

            setTeams(

                Array.isArray(data)

                    ? data

                    : []

            );

        } catch (err) {

            setError(err.message);

        } finally {

            setLoading(false);

        }

    }

    // =========================================================

    // LOAD PERSONAL CREDENTIALS

    // =========================================================

    async function loadCredentials() {

        try {

            setCredentialLoading(true);

            const response = await fetch(

                `${API_BASE_URL}/api/vault/credentials`,

                {

                    method: "GET",

                    headers: authHeaders

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to load credentials."

                );

            }

            setCredentials(

                Array.isArray(data)

                    ? data

                    : []

            );

        } catch (err) {

            setError(err.message);

        } finally {

            setCredentialLoading(false);

        }

    }

    // =========================================================

    // LOAD TEAM CREDENTIALS

    // =========================================================

    async function loadMyTeamCredentials() {

        try {

            const response = await fetch(

                `${API_BASE_URL}/api/team-access/my-credentials`,

                {

                    method: "GET",

                    headers: authHeaders

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to load team credentials."

                );

            }

            setTeamCredentials(

                Array.isArray(data)

                    ? data

                    : []

            );

        } catch (err) {

            console.error(

                "Team credential loading error:",

                err

            );

        }

    }

    // =========================================================

    // LOAD MEMBERS

    // =========================================================

    async function loadMembers(teamId) {

        if (!teamId) {

            setMembers([]);

            return;

        }

        try {

            setMemberLoading(true);

            setError("");

            const response = await fetch(

                `${API_BASE_URL}/api/teams/${teamId}/members`,

                {

                    method: "GET",

                    headers: authHeaders

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to load team members."

                );

            }

            setMembers(

                Array.isArray(data)

                    ? data

                    : []

            );

        } catch (err) {

            setMembers([]);

            setError(err.message);

        } finally {

            setMemberLoading(false);

        }

    }

    // =========================================================

    // LOAD ACCESS

    // =========================================================

    async function loadAccess(

        teamId,

        credentialId

    ) {

        if (!teamId || !credentialId) {

            setAccessList([]);

            return;

        }

        try {

            setAccessLoading(true);

            setError("");

            const response = await fetch(

                `${API_BASE_URL}/api/team-access/teams/${teamId}/credentials/${credentialId}/access`,

                {

                    method: "GET",

                    headers: authHeaders

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to load credential access."

                );

            }

            setAccessList(

                Array.isArray(data)

                    ? data

                    : []

            );

        } catch (err) {

            setAccessList([]);

            setError(err.message);

        } finally {

            setAccessLoading(false);

        }

    }

    // =========================================================

    // SELECT TEAM

    // =========================================================

    async function handleSelectTeam(team) {

        setSelectedTeam(team);

        setMembers([]);

        setAccessList([]);

        setSelectedMember("");

        setSelectedCredential("");

        setMessage("");

        setError("");

        await loadMembers(team.id);

    }

    // =========================================================

    // CREATE TEAM

    // =========================================================

    async function createTeam(event) {

        event.preventDefault();

        setMessage("");

        setError("");

        if (!teamName.trim()) {

            setError("Enter a team name.");

            return;

        }

        try {

            const response = await fetch(

                `${API_BASE_URL}/api/teams`,

                {

                    method: "POST",

                    headers: authHeaders,

                    body: JSON.stringify({

                        name: teamName.trim()

                    })

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to create team."

                );

            }

            setTeamName("");

            setMessage(

                "Team created successfully."

            );

            await loadTeams();

        } catch (err) {

            setError(err.message);

        }

    }

    // =========================================================

    // ADD MEMBER

    // =========================================================

    async function addMember(event) {

        event.preventDefault();

        setMessage("");

        setError("");

        if (!selectedTeam) {

            setError("Select a team first.");

            return;

        }

        if (!memberEmail.trim()) {

            setError("Enter the member email.");

            return;

        }

        try {

            const response = await fetch(

                `${API_BASE_URL}/api/teams/${selectedTeam.id}/members`,

                {

                    method: "POST",

                    headers: authHeaders,

                    body: JSON.stringify({

                        email: memberEmail.trim(),

                        role: memberRole

                    })

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to add member."

                );

            }

            setMemberEmail("");

            setMemberRole("MEMBER");

            setMessage(

                "Team member added successfully."

            );

            await loadMembers(

                selectedTeam.id

            );

        } catch (err) {

            setError(err.message);

        }

    }

    // =========================================================

    // REMOVE MEMBER

    // =========================================================

    async function removeMember(userId) {

        if (!selectedTeam) {

            return;

        }

        const confirmed = window.confirm(

            "Are you sure you want to remove this member?"

        );

        if (!confirmed) {

            return;

        }

        try {

            setMessage("");

            setError("");

            const response = await fetch(

                `${API_BASE_URL}/api/teams/${selectedTeam.id}/members/${userId}`,

                {

                    method: "DELETE",

                    headers: authHeaders

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to remove member."

                );

            }

            setMessage(

                "Team member removed successfully."

            );

            await loadMembers(

                selectedTeam.id

            );

        } catch (err) {

            setError(err.message);

        }

    }

    // =========================================================

    // SELECT CREDENTIAL

    // =========================================================

    async function handleCredentialChange(event) {

        const credentialId =

            event.target.value;

        setSelectedCredential(

            credentialId

        );

        setMessage("");

        setError("");

        if (

            selectedTeam &&

            credentialId

        ) {

            await loadAccess(

                selectedTeam.id,

                credentialId

            );

        } else {

            setAccessList([]);

        }

    }

    // =========================================================

    // GRANT ACCESS

    // =========================================================

    async function grantAccess(event) {

        event.preventDefault();

        setMessage("");

        setError("");

        if (!selectedTeam) {

            setError("Select a team.");

            return;

        }

        if (!selectedMember) {

            setError("Select a team member.");

            return;

        }

        if (!selectedCredential) {

            setError("Select a credential.");

            return;

        }

        try {

            setLoading(true);

            const requestBody = {

                userId: Number(selectedMember),

                permissionLevel,

                expirationDate:

                    expirationDate

                        ? expirationDate

                        : null

            };

            const response = await fetch(

                `${API_BASE_URL}/api/team-access/teams/${selectedTeam.id}/credentials/${selectedCredential}`,

                {

                    method: "POST",

                    headers: authHeaders,

                    body: JSON.stringify(

                        requestBody

                    )

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to grant credential access."

                );

            }

            setMessage(

                "Credential access granted successfully."

            );

            setExpirationDate("");

            await loadAccess(

                selectedTeam.id,

                selectedCredential

            );

            await loadMyTeamCredentials();

        } catch (err) {

            setError(err.message);

        } finally {

            setLoading(false);

        }

    }

    // =========================================================

    // REMOVE CREDENTIAL ACCESS

    // =========================================================

    async function removeCredentialAccess(

        userId

    ) {

        if (

            !selectedTeam ||

            !selectedCredential ||

            !userId

        ) {

            return;

        }

        const confirmed = window.confirm(

            "Are you sure you want to remove this user's credential access?"

        );

        if (!confirmed) {

            return;

        }

        try {

            setMessage("");

            setError("");

            const response = await fetch(

                `${API_BASE_URL}/api/team-access/teams/${selectedTeam.id}/credentials/${selectedCredential}/users/${userId}`,

                {

                    method: "DELETE",

                    headers: authHeaders

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to remove credential access."

                );

            }

            setMessage(

                "Credential access removed successfully."

            );

            await loadAccess(

                selectedTeam.id,

                selectedCredential

            );

            await loadMyTeamCredentials();

        } catch (err) {

            setError(err.message);

        }

    }

    // =========================================================

    // PASSWORD SHOW / HIDE

    // =========================================================

    function togglePassword(id) {

        setVisiblePasswords(

            previous => ({

                ...previous,

                [id]: !previous[id]

            })

        );

    }

    // =========================================================

    // PERMISSION

    // =========================================================

    function getPermissionLabel(permission) {

        switch (permission) {

            case "VIEW_ONLY":

                return "View Only";

            case "EDIT_ACCESS":

                return "Edit Access";

            case "FULL_MANAGEMENT":

                return "Full Management";

            default:

                return permission || "Unknown";

        }

    }

    function getPermissionDescription(permission) {

        switch (permission) {

            case "VIEW_ONLY":

                return "Can view the credential but cannot modify it.";

            case "EDIT_ACCESS":

                return "Can view and update the credential.";

            case "FULL_MANAGEMENT":

                return "Can view, update, and manage credential access.";

            default:

                return "";

        }

    }

    function getPermissionClass(permission) {

        switch (permission) {

            case "VIEW_ONLY":

                return "border-blue-500/20 bg-blue-500/10 text-blue-400";

            case "EDIT_ACCESS":

                return "border-yellow-500/20 bg-yellow-500/10 text-yellow-400";

            case "FULL_MANAGEMENT":

                return "border-violet-500/20 bg-violet-500/10 text-violet-400";

            default:

                return "border-slate-700 bg-slate-800 text-slate-400";

        }

    }

    // =========================================================

    // CHECK EDIT PERMISSION

    // =========================================================

    function canEdit(permission) {

        return (

            permission === "EDIT_ACCESS" ||

            permission === "FULL_MANAGEMENT"

        );

    }

    // =========================================================

    // START EDIT

    // =========================================================

    function startEditing(credential) {

        const credentialId =

            credential.credentialId ??

            credential.id;

        setEditingCredentialKey(`${credential.teamId ?? "team"}:${credentialId}`);

        setEditForm({

            title: credential.title || "",

            username: credential.username || "",

            password: credential.password || ""

        });

        setMessage("");

        setError("");

    }

    // =========================================================

    // CANCEL EDIT

    // =========================================================

    function cancelEditing() {

        setEditingCredentialKey(null);

        setEditForm({

            title: "",

            username: "",

            password: ""

        });

        setMessage("");

        setError("");

    }

    // =========================================================

    // UPDATE EDIT FORM

    // =========================================================

    function handleEditChange(event) {

        const {

            name,

            value

        } = event.target;

        setEditForm(

            previous => ({

                ...previous,

                [name]: value

            })

        );

    }

    // =========================================================

    // SAVE TEAM CREDENTIAL

    // =========================================================

    async function saveCredential(

        credential

    ) {

        const credentialId =

            credential.credentialId ??

            credential.id;

        if (!credentialId) {

            setError(

                "Credential ID is missing."

            );

            return;

        }

        if (!canEdit(

            credential.permissionLevel

        )) {

            setError(

                "You do not have permission to edit this credential."

            );

            return;

        }

        try {

            setSavingCredential(true);

            setMessage("");

            setError("");

            const response = await fetch(

                `${API_BASE_URL}/api/team-access/teams/${credential.teamId}/credentials/${credentialId}`,

                {

                    method: "PUT",

                    headers: authHeaders,

                    body: JSON.stringify({

                        title: editForm.title,

                        username: editForm.username,

                        password: editForm.password

                    })

                }

            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(

                    data.message ||

                    "Failed to update team credential."

                );

            }

            setMessage(

                "Team credential updated successfully."

            );

            setEditingCredentialKey(null);

            setEditForm({

                title: "",

                username: "",

                password: ""

            });

            await loadMyTeamCredentials();

            if (

                selectedTeam &&

                selectedCredential

            ) {

                await loadAccess(

                    selectedTeam.id,

                    selectedCredential

                );

            }

        } catch (err) {

            setError(err.message);

        } finally {

            setSavingCredential(false);

        }

    }

    // =========================================================

    // REFRESH

    // =========================================================

    async function refreshEverything() {

        setMessage("");

        setError("");

        await loadTeams();

        await loadCredentials();

        await loadMyTeamCredentials();

        if (selectedTeam) {

            await loadMembers(

                selectedTeam.id

            );

        }

        if (

            selectedTeam &&

            selectedCredential

        ) {

            await loadAccess(

                selectedTeam.id,

                selectedCredential

            );

        }

    }

    // =========================================================

    // RENDER

    // =========================================================

    return (

        <div className="min-h-screen bg-slate-950 px-6 py-10 text-white">

            <div className="mx-auto max-w-7xl">

                {/* =====================================================

                    HEADER

                ====================================================== */}

                <div className="mb-8">

                    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                        <div>

                            <h1 className="text-3xl font-bold">

                                Team Vault

                            </h1>

                            <p className="mt-2 text-slate-400">

                                Manage teams, members, credentials and access from one place.

                            </p>

                        </div>

                        <button

                            type="button"

                            onClick={refreshEverything}

                            className="rounded-lg bg-slate-700 px-5 py-3 font-semibold hover:bg-slate-600"

                        >

                            Refresh Everything

                        </button>

                    </div>

                </div>

                {/* =====================================================

                    ALERTS

                ====================================================== */}

                {message && (

                    <div className="mb-6 rounded-lg border border-green-500/30 bg-green-500/10 p-4 text-green-400">

                        ✓ {message}

                    </div>

                )}

                {error && (

                    <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 p-4 text-red-400">

                        ⚠ {error}

                    </div>

                )}

                {/* =====================================================

                    CREATE / SELECT TEAM

                ====================================================== */}

                <div className="grid gap-6 lg:grid-cols-2">

                    {/* CREATE TEAM */}

                    <div className="rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                        <h2 className="mb-5 text-xl font-semibold">

                            Create Team

                        </h2>

                        <form

                            onSubmit={createTeam}

                            className="space-y-4"

                        >

                            <input

                                type="text"

                                value={teamName}

                                onChange={event =>

                                    setTeamName(

                                        event.target.value

                                    )

                                }

                                placeholder="Enter team name"

                                className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"

                            />

                            <button

                                type="submit"

                                className="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold hover:bg-blue-700"

                            >

                                Create Team

                            </button>

                        </form>

                    </div>

                    {/* SELECT TEAM */}

                    <div className="rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                        <h2 className="mb-5 text-xl font-semibold">

                            Select Team

                        </h2>

                        {loading && teams.length === 0 ? (

                            <p className="text-slate-400">

                                Loading teams...

                            </p>

                        ) : teams.length === 0 ? (

                            <p className="text-slate-400">

                                No teams created yet.

                            </p>

                        ) : (

                            <select

                                value={

                                    selectedTeam?.id || ""

                                }

                                onChange={event => {

                                    const team =

                                        teams.find(

                                            item =>

                                                String(item.id) ===

                                                event.target.value

                                        );

                                    if (team) {

                                        handleSelectTeam(team);

                                    }

                                }}

                                className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"

                            >

                                <option value="">

                                    Select a team

                                </option>

                                {teams.map(team => (

                                    <option

                                        key={team.id}

                                        value={team.id}

                                    >

                                        {team.name}

                                    </option>

                                ))}

                            </select>

                        )}

                    </div>

                </div>

                {/* =====================================================

                    TEAM LIST

                ====================================================== */}

                <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                    <h2 className="mb-5 text-xl font-semibold">

                        Your Teams

                    </h2>

                    {teams.length === 0 ? (

                        <div className="rounded-lg bg-slate-800 p-5 text-slate-400">

                            No teams available.

                        </div>

                    ) : (

                        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">

                            {teams.map(team => (

                                <button

                                    key={team.id}

                                    type="button"

                                    onClick={() =>

                                        handleSelectTeam(team)

                                    }

                                    className={`rounded-lg border p-5 text-left transition ${

                                        selectedTeam?.id === team.id

                                            ? "border-blue-500 bg-blue-500/10"

                                            : "border-slate-700 bg-slate-800 hover:border-slate-500"

                                    }`}

                                >

                                    <h3 className="text-lg font-semibold">

                                        {team.name}

                                    </h3>

                                    <p className="mt-2 text-sm text-slate-400">

                                        Team ID: {team.id}

                                    </p>

                                </button>

                            ))}

                        </div>

                    )}

                </div>

                {/* =====================================================

                    SELECTED TEAM

                ====================================================== */}

                {selectedTeam && (

                    <>

                        {/* =================================================

                            MEMBERS

                        ================================================== */}

                        <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                            <h2 className="mb-6 text-xl font-semibold">

                                Team Members

                            </h2>

                            <form

                                onSubmit={addMember}

                                className="mb-6 grid gap-4 md:grid-cols-3"

                            >

                                <input

                                    type="email"

                                    value={memberEmail}

                                    onChange={event =>

                                        setMemberEmail(

                                            event.target.value

                                        )

                                    }

                                    placeholder="Member email"

                                    className="rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white"

                                />

                                <select

                                    value={memberRole}

                                    onChange={event =>

                                        setMemberRole(

                                            event.target.value

                                        )

                                    }

                                    className="rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white"

                                >

                                    <option value="MEMBER">

                                        MEMBER

                                    </option>

                                    <option value="ADMIN">

                                        ADMIN

                                    </option>

                                </select>

                                <button

                                    type="submit"

                                    className="rounded-lg bg-emerald-600 px-4 py-3 font-semibold hover:bg-emerald-700"

                                >

                                    Add Member

                                </button>

                            </form>

                            {memberLoading ? (

                                <p className="text-slate-400">

                                    Loading members...

                                </p>

                            ) : (

                                <div className="space-y-3">

                                    {members.map(member => (

                                        <div

                                            key={

                                                member.userId ??

                                                member.id

                                            }

                                            className="flex flex-col gap-4 rounded-lg bg-slate-800 p-5 sm:flex-row sm:items-center sm:justify-between"

                                        >

                                            <div>

                                                <p className="font-semibold">

                                                    {member.username}

                                                </p>

                                                <p className="text-sm text-slate-400">

                                                    {member.email}

                                                </p>

                                                <span className="text-xs text-slate-500">

                                                    {member.role}

                                                </span>

                                            </div>

                                            {member.role !== "OWNER" && (

                                                <button

                                                    type="button"

                                                    onClick={() =>

                                                        removeMember(

                                                            member.userId

                                                        )

                                                    }

                                                    className="rounded-lg bg-red-600 px-3 py-2 text-xs font-semibold hover:bg-red-700"

                                                >

                                                    Remove

                                                </button>

                                            )}

                                        </div>

                                    ))}

                                </div>

                            )}

                        </div>

                        {/* =================================================

                            GRANT ACCESS

                        ================================================== */}

                        <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                            <h2 className="mb-6 text-xl font-semibold">

                                Team Credential Access

                            </h2>

                            <form

                                onSubmit={grantAccess}

                                className="grid gap-5 md:grid-cols-2"

                            >

                                <select

                                    value={selectedMember}

                                    onChange={event =>

                                        setSelectedMember(

                                            event.target.value

                                        )

                                    }

                                    className="rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white"

                                >

                                    <option value="">

                                        Select team member

                                    </option>

                                    {members

                                        .filter(

                                            member =>

                                                member.role !==

                                                "OWNER"

                                        )

                                        .map(member => (

                                            <option

                                                key={member.userId}

                                                value={member.userId}

                                            >

                                                {member.username} — {member.email}

                                            </option>

                                        ))}

                                </select>

                                <select

                                    value={selectedCredential}

                                    onChange={

                                        handleCredentialChange

                                    }

                                    className="rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white"

                                >

                                    <option value="">

                                        Select credential

                                    </option>

                                    {credentials.map(

                                        credential => (

                                            <option

                                                key={credential.id}

                                                value={credential.id}

                                            >

                                                {credential.title}

                                            </option>

                                        )

                                    )}

                                </select>

                                <select

                                    value={permissionLevel}

                                    onChange={event =>

                                        setPermissionLevel(

                                            event.target.value

                                        )

                                    }

                                    className="rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white"

                                >

                                    <option value="VIEW_ONLY">

                                        VIEW ONLY

                                    </option>

                                    <option value="EDIT_ACCESS">

                                        EDIT ACCESS

                                    </option>

                                    <option value="FULL_MANAGEMENT">

                                        FULL MANAGEMENT

                                    </option>

                                </select>

                                <input

                                    type="datetime-local"

                                    value={expirationDate}

                                    onChange={event =>

                                        setExpirationDate(

                                            event.target.value

                                        )

                                    }

                                    className="rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white"

                                />

                                <button

                                    type="submit"

                                    disabled={

                                        loading ||

                                        !selectedMember ||

                                        !selectedCredential

                                    }

                                    className="md:col-span-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold hover:bg-blue-700 disabled:opacity-50"

                                >

                                    {loading

                                        ? "Granting Access..."

                                        : "Grant Credential Access"}

                                </button>

                            </form>

                            <p className="mt-3 text-sm text-slate-500">

                                {getPermissionDescription(

                                    permissionLevel

                                )}

                            </p>

                        </div>

                        {/* =================================================

                            CURRENT ACCESS

                        ================================================== */}

                        {selectedCredential && (

                            <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                                <h2 className="mb-6 text-xl font-semibold">

                                    Current Credential Access

                                </h2>

                                {accessLoading ? (

                                    <p className="text-slate-400">

                                        Loading access...

                                    </p>

                                ) : (

                                    <div className="space-y-3">

                                        {accessList.map(access => {

                                            const userId =

                                                access.userId ??

                                                access.sharedUserId;

                                            return (

                                                <div

                                                    key={

                                                        access.accessId ??

                                                        access.id ??

                                                        userId

                                                    }

                                                    className="flex items-center justify-between rounded-lg bg-slate-800 p-5"

                                                >

                                                    <div>

                                                        <p className="font-semibold">

                                                            {

                                                                access.username ??

                                                                access.usernameOfMember ??

                                                                "Team Member"

                                                            }

                                                        </p>

                                                        <p className="text-sm text-slate-400">

                                                            {access.email}

                                                        </p>

                                                    </div>

                                                    <div className="flex items-center gap-3">

                                                        <span

                                                            className={`rounded-full border px-3 py-1 text-xs font-semibold ${getPermissionClass(

                                                                access.permissionLevel

                                                            )}`}

                                                        >

                                                            {

                                                                getPermissionLabel(

                                                                    access.permissionLevel

                                                                )

                                                            }

                                                        </span>

                                                        <button

                                                            type="button"

                                                            onClick={() =>

                                                                removeCredentialAccess(

                                                                    userId

                                                                )

                                                            }

                                                            className="rounded-lg bg-red-600 px-3 py-2 text-xs font-semibold"

                                                        >

                                                            Remove

                                                        </button>

                                                    </div>

                                                </div>

                                            );

                                        })}

                                    </div>

                                )}

                            </div>

                        )}

                    </>

                )}

                {/* =====================================================

                    TEAM CREDENTIALS

                ====================================================== */}

                <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                    <div className="mb-6">

                        <h2 className="text-xl font-semibold">

                            Team Credentials

                        </h2>

                        <p className="mt-1 text-sm text-slate-400">

                            Credentials you can access through team permissions.

                        </p>

                    </div>

                    {teamCredentials.length === 0 ? (

                        <div className="rounded-lg bg-slate-800 p-5 text-slate-400">

                            No team credentials available.

                        </div>

                    ) : (

                        <div className="grid gap-5 md:grid-cols-2">

                            {teamCredentials.map(

                                credential => {

                                    const credentialId =

                                        credential.credentialId ??

                                        credential.id;

                                    const permission =

                                        credential.permissionLevel;

                                    const credentialKey = `${credential.teamId ?? "team"}:${credentialId}`;

                                    const isEditing = editingCredentialKey === credentialKey;

                                    const passwordVisible = visiblePasswords[credentialKey];

                                    const editable =

                                        canEdit(

                                            permission

                                        );

                                    return (

                                        <div

                                            key={credentialKey}

                                            className="rounded-xl border border-slate-700 bg-slate-800 p-5"

                                        >

                                            {/* HEADER */}

                                            <div className="flex items-start justify-between gap-3">

                                                <div>

                                                    <h3 className="text-lg font-semibold">

                                                        {credential.title}

                                                    </h3>

                                                    <p className="mt-1 text-xs text-slate-500">

                                                        Team Credential

                                                    </p>

                                                </div>

                                                <span

                                                    className={`rounded-full border px-3 py-1 text-xs font-semibold ${getPermissionClass(

                                                        permission

                                                    )}`}

                                                >

                                                    {

                                                        getPermissionLabel(

                                                            permission

                                                        )

                                                    }

                                                </span>

                                            </div>

                                            {/* =================================================

                                                EDIT MODE

                                            ================================================== */}

                                            {isEditing ? (

                                                <div className="mt-5 space-y-4">

                                                    <div>

                                                        <label className="mb-2 block text-xs text-slate-400">

                                                            Title

                                                        </label>

                                                        <input

                                                            type="text"

                                                            name="title"

                                                            value={

                                                                editForm.title

                                                            }

                                                            onChange={

                                                                handleEditChange

                                                            }

                                                            className="w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 text-white outline-none focus:border-yellow-500"

                                                        />

                                                    </div>

                                                    <div>

                                                        <label className="mb-2 block text-xs text-slate-400">

                                                            Username

                                                        </label>

                                                        <input

                                                            type="text"

                                                            name="username"

                                                            value={

                                                                editForm.username

                                                            }

                                                            onChange={

                                                                handleEditChange

                                                            }

                                                            className="w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 text-white outline-none focus:border-yellow-500"

                                                        />

                                                    </div>

                                                    <div>

                                                        <label className="mb-2 block text-xs text-slate-400">

                                                            Password

                                                        </label>

                                                        <input

                                                            type="text"

                                                            name="password"

                                                            value={

                                                                editForm.password

                                                            }

                                                            onChange={

                                                                handleEditChange

                                                            }

                                                            className="w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 font-mono text-white outline-none focus:border-yellow-500"

                                                        />

                                                    </div>

                                                    <div className="flex gap-3">

                                                        <button

                                                            type="button"

                                                            disabled={

                                                                savingCredential

                                                            }

                                                            onClick={() =>

                                                                saveCredential(

                                                                    credential

                                                                )

                                                            }

                                                            className="flex-1 rounded-lg bg-emerald-600 px-4 py-3 font-semibold hover:bg-emerald-700 disabled:opacity-50"

                                                        >

                                                            {savingCredential

                                                                ? "Saving..."

                                                                : "Save Changes"}

                                                        </button>

                                                        <button

                                                            type="button"

                                                            onClick={

                                                                cancelEditing

                                                            }

                                                            className="flex-1 rounded-lg bg-slate-700 px-4 py-3 font-semibold hover:bg-slate-600"

                                                        >

                                                            Cancel

                                                        </button>

                                                    </div>

                                                </div>

                                            ) : (

                                                <>

                                                    {/* USERNAME */}

                                                    <div className="mt-4">

                                                        <p className="text-xs text-slate-500">

                                                            Username

                                                        </p>

                                                        <div className="mt-1 rounded-lg bg-slate-900 px-4 py-3 text-sm">

                                                            {

                                                                credential.username

                                                            }

                                                        </div>

                                                    </div>

                                                    {/* PASSWORD */}

                                                    <div className="mt-4">

                                                        <p className="text-xs text-slate-500">

                                                            Password

                                                        </p>

                                                        <div className="mt-1 flex gap-2">

                                                            <div className="flex-1 overflow-hidden rounded-lg bg-slate-900 px-4 py-3 font-mono text-sm">

                                                                {passwordVisible

                                                                    ? credential.password

                                                                    : "••••••••••••"}

                                                            </div>

                                                            <button

                                                                type="button"

                                                                onClick={() =>

                                                                    togglePassword(

                                                                        credentialKey

                                                                    )

                                                                }

                                                                className="rounded-lg bg-slate-700 px-4 py-2 text-sm font-semibold hover:bg-slate-600"

                                                            >

                                                                {passwordVisible

                                                                    ? "Hide"

                                                                    : "Show"}

                                                            </button>

                                                        </div>

                                                    </div>

                                                    {/* PERMISSION DESCRIPTION */}

                                                    <p className="mt-4 text-xs text-slate-500">

                                                        {

                                                            getPermissionDescription(

                                                                permission

                                                            )

                                                        }

                                                    </p>

                                                    {/* EDIT BUTTON */}

                                                    {editable && (

                                                        <button

                                                            type="button"

                                                            onClick={() =>

                                                                startEditing(

                                                                    credential

                                                                )

                                                            }

                                                            className={`mt-5 w-full rounded-lg px-4 py-3 font-semibold ${

                                                                permission ===

                                                                "FULL_MANAGEMENT"

                                                                    ? "bg-violet-600 hover:bg-violet-700"

                                                                    : "bg-yellow-600 hover:bg-yellow-700"

                                                            }`}

                                                        >

                                                            Edit Credential

                                                        </button>

                                                    )}

                                                    {!editable && (

                                                        <div className="mt-5 rounded-lg border border-slate-700 bg-slate-900 p-3 text-center text-xs text-slate-500">

                                                            View only — editing is disabled.

                                                        </div>

                                                    )}

                                                </>

                                            )}

                                        </div>

                                    );

                                }

                            )}

                        </div>

                    )}

                </div>

            </div>

        </div>

    );

}

export default TeamVault;