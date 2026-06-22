import { useEffect, useState } from "react";
import type { UserDTO } from "../../DTOs/UserDTO";
import { useApiFetch } from "../../apiFetch";

type UsersListProps = {
  users?: UserDTO[] | null;
};

export default function ViewUsers({ users }: UsersListProps) {
  const [error, setError] = useState<string>("");
  const [userDTOList, setUserDTOList] = useState<UserDTO[]>([]);

  const apiFetch = useApiFetch();

  async function handleCancelSubscription(userID: string) {
    try {
      if (users !== undefined && users !== null) {
        setUserDTOList(users);
        return;
      }

      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/removeUser/${userID}`,
        {
          method: "DELETE",
        },
      );
      if (!response.ok) {
        throw new Error(await response.text());
      }
      setUserDTOList((prev) =>
        prev.filter((user) => user.userEmail !== userID),
      );
      setError("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete users.");
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
        setError(err instanceof Error ? err.message : "Failed to load user.");
      }
    }

    void loadUsers();
  }, [users, apiFetch]);

  if (userDTOList.length === 0) {
    return <div>No users found</div>;
  }

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

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
                  onClick={() => {
                    handleCancelSubscription(user.userEmail);
                  }}
                >
                  Cancel Subscription
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
