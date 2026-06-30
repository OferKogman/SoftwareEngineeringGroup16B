import { useState } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { ManagerPermissions } from "../../DTOs/ProductionCompanyDTO";
import AssignMember from "./AssignNewOwnerOrManager";
import ChangeManagerPermissions from "./ChangeManagerPermissions";
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
  const [submitError, setSubmitError] = useState("");
  const [isRemoving, setIsRemoving] = useState(false);

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
    setSubmitError("");
  }

  async function handleAssignMember(data: AssignMemberData) {
    setMessage("");
    setError("");
    setSubmitError("");

    if (!companyId) {
      setError("Missing company ID.");
      return;
    }

    const endpoint =
      data.role === "OWNER"
        ? `http://localhost:8080/production-companies/${companyId}/owners`
        : `http://localhost:8080/production-companies/${companyId}/managers`;

    const body =
      data.role === "OWNER"
        ? {
            targetID: data.targetID,
          }
        : {
            targetID: data.targetID,
            permissions: Array.from(data.permissions),
          };

    const response = await apiFetch(endpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }

    setMessage(
      `${data.role === "OWNER" ? "Owner" : "Manager"} invite sent to ${data.targetID}.`,
    );
  }

  async function handleChangePermissions(
    targetID: string,
    _companyID: number,
    newPermissions: Set<ManagerPermissions>,
  ) {
    setMessage("");
    setError("");
    setSubmitError("");

    if (!companyId) {
      setError("Missing company ID.");
      return;
    }

    const response = await apiFetch(
      `http://localhost:8080/production-companies/${companyId}/managers/permissions`,
      {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          targetID,
          newPermissions: Array.from(newPermissions),
        }),
      },
    );

    if (!response.ok) {
      throw new Error(await response.text());
    }

    setMessage(`Permissions updated for ${targetID}.`);
  }

  async function handleRemoveMember(event: React.FormEvent<HTMLFormElement>) {
    try {
      event.preventDefault();
      setIsRemoving(true);

      setMessage("");
      setError("");
      setSubmitError("");

      if (!companyId) {
        setError("Missing company ID.");
        return;
      }

      const response = await apiFetch(
        `http://localhost:8080/production-companies/${companyId}/members/${removeTargetId}`,
        {
          method: "DELETE",
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      setMessage(`Member ${removeTargetId} removed.`);
      setRemoveTargetId("");
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "");
    } finally {
      setIsRemoving(false);
    }
  }

  return (
    <div className="members-page">
      <h2>Members & Permissions</h2>

      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}

      <div className="members-grid">
        <AssignMember onSubmit={handleAssignMember} />
        <ChangeManagerPermissions onSubmit={handleChangePermissions} />

        <form onSubmit={handleRemoveMember}>
          <h2>Remove Member</h2>

          {submitError && (
            <div className="settings-alert">
              <p>{submitError}</p>
              <button onClick={closePopup}> OK </button>
            </div>
          )}

          <label>
            Target User
            <input
              type="email"
              required
              value={removeTargetId}
              onChange={(event) => setRemoveTargetId(event.target.value)}
              placeholder="Target User"
            />
          </label>

          <button type="submit" disabled={isRemoving}>
            {isRemoving ? "Removing..." : "Remove Member"}
          </button>
        </form>
      </div>
    </div>
  );
}
