import { Handle, Position, type Node, type NodeProps } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useState } from "react";
import type {
  PurchasePolicyDTO,
  PurchasePolicyTypes,
  SimplePolicyTypes,
} from "../../DTOs/PurchasePolicyDTO";
import "./CSS/PolicyNode.css";

type PolicyNodeData = {
  label: string;
  type: PurchasePolicyTypes | "NONE";
  path: PolicyPath;
  onSwap: (path: PolicyPath) => void;
  onChangeGoal: (path: PolicyPath, newGoal: number) => void;
  onReplace: (path: PolicyPath, newPolicy: PurchasePolicyDTO) => void;
  onDelete: () => void;
};

export type PolicyPath = ("left" | "right")[];

export type PolicyNode = Node<PolicyNodeData>;

function createSimplePolicy(
  type: SimplePolicyTypes,
  value: number,
): PurchasePolicyDTO {
  switch (type) {
    case "MIN_AGE":
      return {
        type,
        minAge: value,
      };

    case "MAX_AGE":
      return {
        type,
        maxAge: value,
      };

    case "MIN_TICKETS":
      return {
        type,
        minTickets: value,
      };

    case "MAX_TICKETS":
      return {
        type,
        maxTickets: value,
      };

    default:
      throw new Error("Not simple policy");
  }
}

const simplePolicyTypes: PurchasePolicyTypes[] = [
  "MIN_AGE",
  "MAX_AGE",
  "MIN_TICKETS",
  "MAX_TICKETS",
];

export function PolicyNode(props: NodeProps<PolicyNode>) {
  const [showPopup, setShowPopup] = useState(false);
  const [inputValue, setInputValue] = useState("");
  const [action, setAction] = useState<"CHANGE" | "REPLACE" | null>(null);
  const [replaceInputValue, setReplaceInputValue] =
    useState<PurchasePolicyTypes>("MIN_AGE");
  const [replaceValue, setReplaceValue] = useState("");
  const [leftPolicy, setLeftPolicy] = useState<PurchasePolicyDTO | null>(null);
  const [rightPolicy, setRightPolicy] = useState<PurchasePolicyDTO | null>(
    null,
  );

  function renderReplace() {
    if (action !== "REPLACE") return null;

    const isComplex = replaceInputValue === "AND" || replaceInputValue === "OR";

    return (
      <>
        <select
          value={replaceInputValue}
          onChange={(e) => {
            const value = e.target.value as PurchasePolicyTypes;

            setReplaceInputValue(value);

            setLeftPolicy(null);
            setRightPolicy(null);
            setReplaceValue("");
          }}
        >
          {simplePolicyTypes.map((type) => (
            <option key={type} value={type}>
              {type.replace("_", " ")}
            </option>
          ))}
          <option value="AND">AND</option>
          <option value="OR">OR</option>
        </select>

        {!isComplex && (
          <input
            type="number"
            value={replaceValue}
            onChange={(e) => setReplaceValue(e.target.value)}
            placeholder="Value"
          />
        )}

        {isComplex && (
          <>
            <select
              onChange={(e) => {
                const type = e.target.value as SimplePolicyTypes;

                if (type) {
                  setLeftPolicy(createSimplePolicy(type, 1));
                }
              }}
            >
              <option value="">Choose left policy</option>

              {simplePolicyTypes.map((type) => (
                <option key={type} value={type}>
                  {type.replace("_", " ")}
                </option>
              ))}
            </select>

            <select
              onChange={(e) => {
                const type = e.target.value as SimplePolicyTypes;

                if (type) {
                  setRightPolicy(createSimplePolicy(type, 1));
                }
              }}
            >
              <option value="">Choose right policy</option>

              {simplePolicyTypes.map((type) => (
                <option key={type} value={type}>
                  {type.replace("_", " ")}
                </option>
              ))}
            </select>
          </>
        )}

        <button
          disabled={isComplex && (!leftPolicy || !rightPolicy)}
          onClick={() => {
            let newPolicy: PurchasePolicyDTO;

            if (isComplex) {
              if (!leftPolicy || !rightPolicy) {
                return;
              }

              newPolicy = {
                type: replaceInputValue,
                left: leftPolicy,
                right: rightPolicy,
              };
            } else {
              const value = Number(replaceValue);

              if (!value || value <= 0) {
                alert("Enter valid value");
                return;
              }

              newPolicy = createSimplePolicy(replaceInputValue, value);
            }

            props.data.onReplace(props.data.path, newPolicy);

            setLeftPolicy(null);
            setRightPolicy(null);
            setReplaceValue("");
            setAction(null);
            setShowPopup(false);
          }}
        >
          Replace
        </button>
      </>
    );
  }

  function renderActions() {
    return (
      <>
        {props.data.path.length === 0 && props.data.type !== "NONE" && (
          <button
            onClick={() => {
              props.data.onDelete();
              setShowPopup(false);
            }}
          >
            Delete policy
          </button>
        )}

        {(() => {
          switch (props.data.type) {
            case "AND":
            case "OR":
              return (
                <>
                  <button
                    onClick={() => {
                      props.data.onSwap(props.data.path);
                      setShowPopup(false);
                    }}
                  >
                    Change {props.data.type === "AND" ? "to OR" : "to AND"}
                  </button>

                  <button
                    onClick={() => {
                      setLeftPolicy(null);
                      setRightPolicy(null);
                      setReplaceValue("");
                      setAction("REPLACE");
                    }}
                  >
                    Replace policy
                  </button>

                  {renderReplace()}
                </>
              );

            case "MIN_AGE":
            case "MAX_AGE":
            case "MIN_TICKETS":
            case "MAX_TICKETS":
              return (
                <>
                  {action === "CHANGE" ? (
                    <>
                      <input
                        type="number"
                        value={inputValue}
                        onChange={(e) => setInputValue(e.target.value)}
                        placeholder="New value"
                      />

                      <button
                        onClick={() => {
                          const goalVal = Number(inputValue);

                          if (!goalVal || goalVal <= 0) {
                            alert("Please enter a valid value");
                            return;
                          }
                          props.data.onChangeGoal(props.data.path, goalVal);

                          setInputValue("");
                          setAction(null);
                          setShowPopup(false);
                        }}
                      >
                        Change value
                      </button>
                    </>
                  ) : (
                    <button
                      onClick={() => {
                        setAction("CHANGE");
                      }}
                    >
                      Change value
                    </button>
                  )}

                  <button
                    onClick={() => {
                      setLeftPolicy(null);
                      setRightPolicy(null);
                      setReplaceValue("");
                      setAction("REPLACE");
                    }}
                  >
                    Replace policy
                  </button>

                  {renderReplace()}
                </>
              );

            case "NONE":
              return (
                <>
                  {action === "REPLACE" ? (
                    renderReplace()
                  ) : (
                    <button
                      onClick={() => {
                        setAction("REPLACE");
                      }}
                    >
                      Create policy
                    </button>
                  )}
                </>
              );
          }
        })()}
      </>
    );
  }

  return (
    <div className="policy-node-wrapper">
      <div
        className="policy-node"
        onClick={() => setShowPopup((prev) => !prev)}
      >
        <label>{props.data.label}</label>

        <Handle type="target" position={Position.Top} />
        <Handle type="source" position={Position.Bottom} />
      </div>

      {showPopup && (
        <div className="policy-node-popup" onClick={(e) => e.stopPropagation()}>
          {renderActions()}

          <button onClick={() => setShowPopup(false)}>Close</button>
        </div>
      )}
    </div>
  );
}
