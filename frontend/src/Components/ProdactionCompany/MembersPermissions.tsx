import { useState } from "react";
import { useParams } from "react-router-dom";
import "./CSS/MembersPermissions.css";

const API_BASE = "http://localhost:8080";

const ALL_PERMISSIONS = [
  "VIEW_PURCHASE_HISTORY",
  "SALES_REPORT",
  "VENUE_CONFIGURATION",
];

export default function MembersPermissions() {
  const { companyId } = useParams();

  const [ownerTargetId, setOwnerTargetId] = useState("");
  const [managerTargetId, setManagerTargetId] = useState("");
  const [removeTargetId, setRemoveTargetId] = useState("");
  const [permissionTargetId, setPermissionTargetId] = useState("");
  const [selectedPermissions, setSelectedPermissions] = useState<string[]>([]);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  function getAuthToken() {
    return localStorage.getItem("authToken") || "";
  }

  function togglePermission(permission: string) {
    setSelectedPermissions((current) =>
      current.includes(permission)
        ? current.filter((p) => p !== permission)
        : [...current, permission]
    );
  }

  async function assignOwner(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    setError("");

    if (!ownerTargetId.trim()) {
      setError("Please enter target user ID.");
      return;
    }

    console.warn("MOCK assign owner", { companyId, ownerTargetId });
    setMessage(`Mock success: owner invite sent to ${ownerTargetId}.`);

    /*
    const response = await fetch(`${API_BASE}/production-companies/${companyId}/owners`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: getAuthToken(),
      },
      body: JSON.stringify({
        targetID: ownerTargetId,
      }),
    });
    */
  }

  async function assignManager(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    setError("");

    if (!managerTargetId.trim()) {
      setError("Please enter target user ID.");
      return;
    }

    console.warn("MOCK assign manager", {
      companyId,
      managerTargetId,
      selectedPermissions,
    });

    setMessage(`Mock success: manager invite sent to ${managerTargetId}.`);

    /*
    const response = await fetch(`${API_BASE}/production-companies/${companyId}/managers`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: getAuthToken(),
      },
      body: JSON.stringify({
        targetID: managerTargetId,
        permissions: selectedPermissions,
      }),
    });
    */
  }

  async function removeMember(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    setError("");

    if (!removeTargetId.trim()) {
      setError("Please enter target user ID.");
      return;
    }

    console.warn("MOCK remove member", { companyId, removeTargetId });
    setMessage(`Mock success: removed member ${removeTargetId}.`);

    /*
    const response = await fetch(`${API_BASE}/production-companies/${companyId}/members/${removeTargetId}`, {
      method: "DELETE",
      headers: {
        Authorization: getAuthToken(),
      },
    });
    */
  }

  async function changePermissions(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");
    setError("");

    if (!permissionTargetId.trim()) {
      setError("Please enter target user ID.");
      return;
    }

    console.warn("MOCK change permissions", {
      companyId,
      permissionTargetId,
      selectedPermissions,
    });

    setMessage(`Mock success: permissions updated for ${permissionTargetId}.`);

    /*
    const response = await fetch(`${API_BASE}/production-companies/${companyId}/managers/permissions`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: getAuthToken(),
      },
      body: JSON.stringify({
        targetID: permissionTargetId,
        newPermissions: selectedPermissions,
      }),
    });
    */
  }

  return (
    <div className="members-page">
      <h2>Members & Permissions</h2>

      {message && <p className="members-success">{message}</p>}
      {error && <p className="members-error">{error}</p>}

      <div className="members-grid">
        <form className="members-card" onSubmit={assignOwner}>
          <h3>Assign Owner</h3>

          <input
            value={ownerTargetId}
            onChange={(event) => setOwnerTargetId(event.target.value)}
            placeholder="Target user ID"
          />

          <button type="submit">Send Owner Invite</button>
        </form>

        <form className="members-card" onSubmit={assignManager}>
          <h3>Assign Manager</h3>

          <input
            value={managerTargetId}
            onChange={(event) => setManagerTargetId(event.target.value)}
            placeholder="Target user ID"
          />

          <PermissionCheckboxes
            selectedPermissions={selectedPermissions}
            togglePermission={togglePermission}
          />

          <button type="submit">Send Manager Invite</button>
        </form>

        <form className="members-card" onSubmit={removeMember}>
          <h3>Remove Member</h3>

          <input
            value={removeTargetId}
            onChange={(event) => setRemoveTargetId(event.target.value)}
            placeholder="Target user ID"
          />

          <button type="submit" className="danger-button">
            Remove Member
          </button>
        </form>

        <form className="members-card" onSubmit={changePermissions}>
          <h3>Change Manager Permissions</h3>

          <input
            value={permissionTargetId}
            onChange={(event) => setPermissionTargetId(event.target.value)}
            placeholder="Target user ID"
          />

          <PermissionCheckboxes
            selectedPermissions={selectedPermissions}
            togglePermission={togglePermission}
          />

          <button type="submit">Update Permissions</button>
        </form>
      </div>
    </div>
  );
}

type PermissionCheckboxesProps = {
  selectedPermissions: string[];
  togglePermission: (permission: string) => void;
};

function PermissionCheckboxes({
  selectedPermissions,
  togglePermission,
}: PermissionCheckboxesProps) {
  return (
    <div className="permissions-list">
      {ALL_PERMISSIONS.map((permission) => (
        <label key={permission}>
          <input
            type="checkbox"
            checked={selectedPermissions.includes(permission)}
            onChange={() => togglePermission(permission)}
          />
          {permission}
        </label>
      ))}
    </div>
  );
}