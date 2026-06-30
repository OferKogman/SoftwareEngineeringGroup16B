import { useEffect, useState } from "react";
import type { UserDTO } from "../../DTOs/UserDTO";
import { useApiFetch } from "../../apiFetch";

type UsersListProps = {
  users?: UserDTO[] | null;
};

export default function ViewUsers({ users }: UsersListProps) {
  const [error, setError] = useState<string>("");
  const [message, setMessage] = useState<string>("");
  const [userDTOList, setUserDTOList] = useState<UserDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [cancelingUserID, setCancelingUserID] = useState<string | null>(null);

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

  async function handleCancelSubscription(userID: string) {
    setCancelingUserID(userID);
    setMessage("");
    setError("");

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/removeUser/${userID}`,
        {
          method: "DELETE",
        },
      );
      const responseText = await response.text();
      if (!response.ok) {
        throw new Error(responseText);
      }
      setUserDTOList((prev) =>
        prev.filter((user) => user.userEmail !== userID),
      );
      setMessage(responseText || "Subscription canceled successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setCancelingUserID(null);
    }
  }

  useEffect(() => {
    async function loadUsers() {
      try {
        if (users !== undefined && users !== null) {
          setUserDTOList(users);
          return;
        }

        const response = await apiFetch(
          `http://localhost:8080/api/admin-management/users`,
          {
            method: "GET",
          },
        );
        if (!response.ok) {
          throw new Error(await response.text());
        }

        const usersFromServer: UserDTO[] = await response.json();

        setUserDTOList(usersFromServer);
      } catch (err) {
        setError(err instanceof Error ? err.message : "");
      } finally {
        setIsLoading(false);
      }
    }

    void loadUsers();
  }, [users, apiFetch]);

  if (isLoading) {
    return <p>Loading users...</p>;
  }

  return (
    <div>
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

      {userDTOList.length === 0 && <p>No users found</p>}

      <table
        style={{
          width: "75%",
          tableLayout: "fixed",
          marginLeft: "auto",
          marginRight: "auto",
        }}
      >
        <thead>
          <tr>
            <th style={{ width: "60%" }}>ID</th>
            <th style={{ width: "15%" }}>Actions</th>
          </tr>
        </thead>

        <tbody>
          {userDTOList.map((user) => (
            <tr key={user.userEmail}>
              <td>{user.userEmail}</td>

              <td>
                <button
                  disabled={cancelingUserID === user.userEmail}
                  onClick={() => {
                    void handleCancelSubscription(user.userEmail);
                  }}
                >
                  {cancelingUserID === user.userEmail
                    ? "Canceling..."
                    : "Cancel Subscription"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
