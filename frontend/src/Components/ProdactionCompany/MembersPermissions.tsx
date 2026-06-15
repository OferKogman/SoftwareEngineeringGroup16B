import { useState } from "react";
import { useParams } from "react-router-dom";
import AssignMember from "./AssignNewOwnerOrManager";
import ChangeManagerPermissions, {
  type ManagerPermissions,
} from "./ChangeManagerPermissions";
import "./CSS/MembersPermissions.css";

type AssignMemberData =
  | { role: "OWNER"; callerID: string; targetID: string }
  | {
      role: "MANAGER";
      callerID: string;
      targetID: string;
      permissions: Set<ManagerPermissions>;
    };

export default function MembersPermissions() {
  const { companyId } = useParams();

  const [removeTargetId, setRemoveTargetId] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function handleAssignMember(data: AssignMemberData) {
    setMessage("");
    setError("");

    if (!companyId) {
      setError("Missing company ID.");
      return;
    }

    console.warn("MOCK assign member", {
      companyId,
      data,
    });

    setMessage(
      `Mock success: ${data.role === "OWNER" ? "owner" : "manager"} invite sent to ${
        data.targetID
      }.`,
    );

    /*
    const endpoint =
      data.role === "OWNER"
        ? `${API_BASE}/production-companies/${companyId}/owners`
        : `${API_BASE}/production-companies/${companyId}/managers`;

    const body =
      data.role === "OWNER"
        ? {
            targetID: data.targetID,
          }
        : {
            targetID: data.targetID,
            permissions: Array.from(data.permissions),
          };

    const response = await fetch(endpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: getAuthToken(),
      },
      body: JSON.stringify(body),
    });

    const result = await response.json();

    if (!response.ok) {
      throw new Error(result.message || "Failed to assign member.");
    }

    setMessage(
      `${data.role === "OWNER" ? "Owner" : "Manager"} invite sent to ${data.targetID}.`
    );
    */
  }

  async function handleChangePermissions(
    targetID: string,
    _companyID: number,
    newPermissions: Set<ManagerPermissions>,
  ) {
    setMessage("");
    setError("");

    if (!companyId) {
      setError("Missing company ID.");
      return;
    }

    console.warn("MOCK change permissions", {
      companyId,
      targetID,
      newPermissions,
    });

    setMessage(`Mock success: permissions updated for ${targetID}.`);

    /*
    const response = await fetch(
      `${API_BASE}/production-companies/${companyId}/managers/permissions`,
      {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: getAuthToken(),
        },
        body: JSON.stringify({
          targetID,
          newPermissions: Array.from(newPermissions),
        }),
      }
    );

    const result = await response.json();

    if (!response.ok) {
      throw new Error(result.message || "Failed to change permissions.");
    }

    setMessage(`Permissions updated for ${targetID}.`);
    */
  }

  async function handleRemoveMember(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setMessage("");
    setError("");

    if (!companyId) {
      setError("Missing company ID.");
      return;
    }

    if (!removeTargetId.trim()) {
      setError("Please enter target user ID.");
      return;
    }

    console.warn("MOCK remove member", {
      companyId,
      removeTargetId,
    });

    setMessage(`Mock success: removed member ${removeTargetId}.`);
    setRemoveTargetId("");

    /*
    const response = await fetch(
      `${API_BASE}/production-companies/${companyId}/members/${removeTargetId}`,
      {
        method: "DELETE",
        headers: {
          Authorization: getAuthToken(),
        },
      }
    );

    const result = await response.json();

    if (!response.ok) {
      throw new Error(result.message || "Failed to remove member.");
    }

    setMessage(`Member ${removeTargetId} removed.`);
    setRemoveTargetId("");
    */
  }

  return (
    <div className="members-page">
      <h2>Members & Permissions</h2>

      {message && <p className="members-success">{message}</p>}
      {error && <p className="members-error">{error}</p>}

      <div className="members-grid">
        <div className="members-card">
          <AssignMember onSubmit={handleAssignMember} />
        </div>

        <div className="members-card">
          <ChangeManagerPermissions onSubmit={handleChangePermissions} />
        </div>

        <form className="members-card" onSubmit={handleRemoveMember}>
          <h2>Remove Member</h2>

          <label>
            Target ID
            <input
              type="text"
              required
              value={removeTargetId}
              onChange={(event) => setRemoveTargetId(event.target.value)}
              placeholder="Target ID"
            />
          </label>

          <button type="submit" className="danger-button">
            Remove Member
          </button>
        </form>
      </div>
    </div>
  );
}
