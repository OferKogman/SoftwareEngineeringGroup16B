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
          throw new Error("Failed to load users.");
        }

        const usersFromServer: UserDTO[] = await response.json();

        setUserDTOList(usersFromServer);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load users.");
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

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Actions</th>
          </tr>
        </thead>

        <tbody>
          {userDTOList.map((user) => (
            <tr key={user.userEmail}>
              <td>{user.userEmail}</td>

              <td>
                {/* Remove user from platform */}
                <button onClick={() => {}}>Cancel Subscription</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
