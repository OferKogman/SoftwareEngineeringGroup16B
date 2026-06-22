import { useState } from "react";

type AssignMemberData =
  | { role: "OWNER"; callerID: string; targetID: string }
  | {
      role: "MANAGER";
      callerID: string;
      targetID: string;
      permissions: Set<ManagerPermissions>;
    };

type AssignMemberProps = {
  onSubmit: (data: AssignMemberData) => void | Promise<void>;
};

type ManagerPermissions =
  | "EVENT_INVENTORY"
  | "VENUE_CONFIGURATION"
  | "PURCHASE_POLICY"
  | "CUSTOMER_SUPPORT"
  | "VIEW_PURCHASE_HISTORY"
  | "SALES_REPORT";

const MANAGER_PERMISSIONS: { value: ManagerPermissions; label: string }[] = [
  { value: "EVENT_INVENTORY", label: "Event Inventory" },
  { value: "VENUE_CONFIGURATION", label: "Venue Configuration" },
  { value: "PURCHASE_POLICY", label: "Purchase Policy" },
  { value: "CUSTOMER_SUPPORT", label: "Customer Support" },
  { value: "VIEW_PURCHASE_HISTORY", label: "View Purchase History" },
  { value: "SALES_REPORT", label: "Sales Report" },
];

const initialFields = { callerID: "", targetID: "" };

export default function AssignMember({ onSubmit }: AssignMemberProps) {
  const [role, setRole] = useState<"OWNER" | "MANAGER">("OWNER");
  const [fields, setFields] = useState(initialFields);
  const [permissions, setPermissions] = useState<Set<ManagerPermissions>>(
    new Set(),
  );
  const [permissionsError, setPermissionsError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function togglePermission(perm: ManagerPermissions) {
    setPermissions((prev) => {
      const next = new Set(prev);
      if (next.has(perm)) {
        next.delete(perm);
      } else {
        next.add(perm);
      }
      return next;
    });
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPermissionsError("");
    setSubmitError("");

    if (role === "MANAGER" && permissions.size === 0) {
      setPermissionsError("Manager must have at least one permission.");
      return;
    }

    setIsSubmitting(true);
    try {
      const payload: AssignMemberData =
        role === "OWNER"
          ? { role: "OWNER", ...fields }
          : { role: "MANAGER", ...fields, permissions };
      await onSubmit(payload);
      setFields(initialFields);
      setPermissions(new Set());
    } catch (err) {
      setSubmitError(
        err instanceof Error ? err.message : "Failed to assign member.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  const isManager = role === "MANAGER";

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Assign Member</h2>

      {submitError && <p className="form-error">{submitError}</p>}

      <label>
        Role
        <select
          value={role}
          onChange={(e) => {
            setRole(e.target.value as "OWNER" | "MANAGER");
            setPermissionsError("");
          }}
        >
          <option value="OWNER">Owner</option>
          <option value="MANAGER">Manager</option>
        </select>
      </label>

      <label>
        Target User
        <input
          type="text"
          required
          value={fields.targetID}
          onChange={(e) =>
            setFields((f) => ({ ...f, targetID: e.target.value }))
          }
          placeholder="Target User"
        />
      </label>

      {isManager && (
        <fieldset>
          <legend>Permissions</legend>
          {MANAGER_PERMISSIONS.map(({ value, label }) => (
            <label key={value}>
              <input
                type="checkbox"
                checked={permissions.has(value)}
                onChange={() => togglePermission(value)}
              />
              {label}
            </label>
          ))}
        </fieldset>
      )}
      {permissionsError && <p className="form-error">{permissionsError}</p>}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting
          ? "Saving..."
          : `Assign ${role === "OWNER" ? "Owner" : "Manager"}`}
      </button>
    </form>
  );
}
