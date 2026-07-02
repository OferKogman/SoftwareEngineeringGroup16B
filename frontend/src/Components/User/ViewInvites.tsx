import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import { useSession } from "../../GlobalContext/SessionContext";

type InviteDTO = {
  assignerID: string;
  companyId: number;
  companyName: string;
  assignerName: string;
};

export default function ViewInvites() {
  const { sessionToken } = useSession();
  const apiFetch = useApiFetch();
  const navigate = useNavigate();
  const { companyId } = useParams();

  const [invites, setInvites] = useState<InviteDTO[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  function closePopup() {
    setError("");
  }

  async function loadInvites() {
    try {
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

  async function acceptInvite(assignerID: string) {
    try {
      const response = await apiFetch(
        `http://localhost:8080/production-companies/${companyId}/invites/accept`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ assignerID }),
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      await loadInvites();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to accept invite");
    }
  }

  async function rejectInvite(assignerID: string) {
    try {
      const response = await apiFetch(
        `http://localhost:8080/production-companies/${companyId}/invites/reject`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ assignerID }),
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      await loadInvites();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to reject invite");
    }
  }

  if (loading || invites === null) {
    return <div>Loading invites...</div>;
  }

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
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {invites.map((invite) => (
              <tr key={invite.assignerID}>
                <td>{invite.companyName}</td>
                <td>{invite.assignerName}</td>
                <td>
                  <button onClick={() => acceptInvite(invite.assignerID)}>
                    Accept
                  </button>

                  <button onClick={() => rejectInvite(invite.assignerID)}>
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
