import { useState } from "react";

export type ProductionCompanyDTO = {
  productionCompanyID: number;
  name: string;
  rating: number;
  founderID: string;
  members: string[];
  invites: string[];
  childrenByUser: string[];
};

type CreateProductionCompanyProps = {
  onSubmit: (company: ProductionCompanyDTO) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData = {
  name: "",
  founderID: "",
  members: [] as string[],
  invites: [] as string[],
  childrenByUser: [] as string[],
};

type ListFieldProps = {
  label: string;
  value: string;
  onChange: (v: string) => void;
  placeholder: string;
  items: string[];
  onAdd: () => void;
  onRemove: (item: string) => void;
};

function ListField({
  label,
  value,
  onChange,
  placeholder,
  items,
  onAdd,
  onRemove,
}: ListFieldProps) {
  return (
    <label>
      {label}
      <input
        type="text"
        value={value}
        placeholder={placeholder}
        required={items.length === 0}
        pattern=".*\S.*"
        onInvalid={(event) =>
          event.currentTarget.setCustomValidity(
            `${label} cannot be empty — add at least one entry.`,
          )
        }
        onChange={(event) => {
          event.currentTarget.setCustomValidity("");
          onChange(event.target.value);
        }}
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            e.preventDefault();
            onAdd();
          }
        }}
      />
      <button type="button" onClick={onAdd}>
        Add
      </button>
      {items.length > 0 && (
        <div className="tag-list">
          {items.map((item) => (
            <span key={item} className="tag">
              {item}
              <button type="button" onClick={() => onRemove(item)}>
                ×
              </button>
            </span>
          ))}
        </div>
      )}
    </label>
  );
}

export default function CreateProductionCompany({
  onSubmit,
  onCancel,
}: CreateProductionCompanyProps) {
  const [formData, setFormData] = useState(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [memberInput, setMemberInput] = useState("");
  const [inviteInput, setInviteInput] = useState("");
  const [childInput, setChildInput] = useState("");

  function addToList(
    field: "members" | "invites" | "childrenByUser",
    value: string,
    setValue: (v: string) => void,
  ) {
    const trimmed = value.trim();
    if (!trimmed || formData[field].includes(trimmed)) return;
    setFormData((prev) => ({ ...prev, [field]: [...prev[field], trimmed] }));
    setValue("");
  }

  function removeFromList(
    field: "members" | "invites" | "childrenByUser",
    value: string,
  ) {
    setFormData((prev) => ({
      ...prev,
      [field]: prev[field].filter((v) => v !== value),
    }));
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setError("");

    try {
      await onSubmit({
        productionCompanyID: 0,
        name: formData.name.trim(),
        rating: 0,
        founderID: formData.founderID.trim(),
        members: formData.members,
        invites: formData.invites,
        childrenByUser: formData.childrenByUser,
      });
      setFormData(initialFormData);
      setMemberInput("");
      setInviteInput("");
      setChildInput("");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to create production company.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Create Production Company</h2>

      {error && <p className="form-error">{error}</p>}

      <label>
        Company name
        <input
          type="text"
          required
          pattern=".*\S.*"
          value={formData.name}
          placeholder="Company name"
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Company name cannot be empty or whitespace.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            setFormData((prev) => ({ ...prev, name: event.target.value }));
          }}
        />
      </label>



      <ListField
        label="Members"
        value={memberInput}
        onChange={setMemberInput}
        placeholder="Member ID"
        items={formData.members}
        onAdd={() => addToList("members", memberInput, setMemberInput)}
        onRemove={(item) => removeFromList("members", item)}
      />

      <ListField
        label="Children by user"
        value={childInput}
        onChange={setChildInput}
        placeholder="User ID"
        items={formData.childrenByUser}
        onAdd={() => addToList("childrenByUser", childInput, setChildInput)}
        onRemove={(item) => removeFromList("childrenByUser", item)}
      />

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create company"}
        </button>
      </div>
    </form>
  );
}