import { useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";
import { useSession } from "../../GlobalContext/SessionContext";

type InviteDTO = {
  companyId: number;
  companyName: string;
  assignerId: string;
  invitedId: string;
  roleType: "OWNER" | "MANAGER"; // adjust if backend has more values
  managerPermissions: string[];
};

export default function ViewInvites() {
  const { sessionToken } = useSession();
  const apiFetch = useApiFetch();

  const [invites, setInvites] = useState<InviteDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  function closePopup() {
    setError("");
  }

  async function loadInvites() {
    try {
      setLoading(true);
      setError("");

      const response = await apiFetch(
        "http://localhost:8080/api/user/me/company-invites",
        { method: "GET" },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const data: InviteDTO[] = await response.json();
      setInvites(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load invites");
      setInvites([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!sessionToken) {
      setError("Missing session token");
      setInvites([]);
      setLoading(false);
      return;
    }

    void loadInvites();
  }, [sessionToken]);

  async function acceptInvite(assignerId: string, companyId: number) {
    await apiFetch(
      `http://localhost:8080/production-companies/${companyId}/invites/accept`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ assignerID: assignerId }),
      },
    );

    await loadInvites();
  }

  async function rejectInvite(assignerId: string, companyId: number) {
    await apiFetch(
      `http://localhost:8080/production-companies/${companyId}/invites/reject`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ assignerID: assignerId }),
      },
    );

    await loadInvites();
  }

  if (loading) return <div>Loading invites...</div>;

  return (
    <div>
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}>OK</button>
        </div>
      )}

      {invites.length === 0 ? (
        <p>No invites found</p>
      ) : (
        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr>
              <th>Company</th>
              <th>Invited By</th>
              <th>Role</th>
              <th>Details</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {invites.map((invite, idx) => (
              <tr key={`${invite.companyId}-${idx}`}>
                <td>{invite.companyName}</td>
                <td>{invite.assignerId}</td>
                <td>{invite.roleType}</td>

                <td>
                  {invite.roleType === "MANAGER" ? (
                    invite.managerPermissions.length > 0 ? (
                      <ul>
                        {invite.managerPermissions.map((p) => (
                          <li key={p}>{p}</li>
                        ))}
                      </ul>
                    ) : (
                      <span>No permissions</span>
                    )
                  ) : (
                    <span>-</span>
                  )}
                </td>

                <td>
                  <button
                    style={{ marginRight: "10px" }}
                    onClick={() =>
                      acceptInvite(invite.assignerId, invite.companyId)
                    }
                  >
                    Accept
                  </button>

                  <button
                    style={{ marginRight: "10px" }}
                    onClick={() =>
                      rejectInvite(invite.assignerId, invite.companyId)
                    }
                  >
                    Reject
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
