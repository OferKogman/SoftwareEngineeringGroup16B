import { Handle, Position, type Node, type NodeProps } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useState } from "react";
import type {
  DiscountPolicyDTO,
  DiscountPolicyTypes,
  SimpleDiscountPolicyTypes,
} from "../../DTOs/DiscountPolicyDTO";
import "./CSS/PolicyNode.css";

type DiscountPolicyNodeData = {
  label: string;
  percentage: number;
  type: DiscountPolicyTypes | "NONE";
  path: PolicyPath;
  onSwap: (path: PolicyPath) => void;
  onChangeGoal: (
    path: PolicyPath,
    newGoal: number | string | null,
    percentageValues: number,
  ) => void;
  onReplace: (path: PolicyPath, newPolicy: DiscountPolicyDTO) => void;
  onDelete: () => void;
};

export type PolicyPath = ("left" | "right")[];

export type DiscountPolicyNode = Node<DiscountPolicyNodeData>;

function createSimplePolicy(
  type: SimpleDiscountPolicyTypes,
  value: number | string,
  percentage: number,
): DiscountPolicyDTO {
  switch (type) {
    case "SIMPLE":
      return {
        type,
        percentage,
      };
    case "MIN_DATE":
      return {
        type,
        startDate: String(value),
        percentage,
      };

    case "MAX_DATE":
      return {
        type,
        endDate: String(value),
        percentage,
      };

    case "MIN_TICKETS":
      return {
        type,
        minAmount: Number(value),
        percentage,
      };

    case "MAX_TICKETS":
      return {
        type,
        maxAmount: Number(value),
        percentage,
      };

    default:
      throw new Error("Not simple policy");
  }
}

const simpleDiscountPolicyTypes: DiscountPolicyTypes[] = [
  "SIMPLE",
  "MIN_DATE",
  "MAX_DATE",
  "MIN_TICKETS",
  "MAX_TICKETS",
];

export function DiscountPolicyNode(props: NodeProps<DiscountPolicyNode>) {
  const [showPopup, setShowPopup] = useState(false);
  const [inputValue, setInputValue] = useState<string | number>("");
  const [action, setAction] = useState<"CHANGE" | "REPLACE" | null>(null);
  const [replaceInputValue, setReplaceInputValue] =
    useState<DiscountPolicyTypes>("SIMPLE");
  const [replaceValue, setReplaceValue] = useState<string>("");
  const [percentageValue, setPercentageValue] = useState(0);
  const [leftPolicy, setLeftPolicy] = useState<DiscountPolicyDTO | null>(null);
  const [rightPolicy, setRightPolicy] = useState<DiscountPolicyDTO | null>(
    null,
  );

  function renderReplace() {
    if (action !== "REPLACE") return null;

    const isComplex =
      replaceInputValue === "AND" ||
      replaceInputValue === "OR" ||
      replaceInputValue === "SUM" ||
      replaceInputValue === "MAX";

    return (
      <>
        <select
          value={replaceInputValue}
          onChange={(e) => {
            const value = e.target.value as DiscountPolicyTypes;

            setReplaceInputValue(value);
            if (value === "MIN_DATE" || value === "MAX_DATE") {
              setInputValue("");
            } else {
              setInputValue(0);
            }

            setLeftPolicy(null);
            setRightPolicy(null);
            setReplaceValue("");
          }}
        >
          {simpleDiscountPolicyTypes.map((type) => (
            <option key={type} value={type}>
              {type.replace("_", " ")}
            </option>
          ))}
          <option value="AND">AND</option>
          <option value="OR">OR</option>
          <option value="SUM">SUM</option>
          <option value="MAX">MAX</option>
        </select>

        {!isComplex && (
          <>
            {replaceInputValue !== "SIMPLE" && (
              <input
                type={
                  replaceInputValue === "MIN_DATE" ||
                  replaceInputValue === "MAX_DATE"
                    ? "datetime-local"
                    : "number"
                }
                min={
                  replaceInputValue === "MIN_TICKETS" ||
                  replaceInputValue === "MAX_TICKETS"
                    ? 1
                    : undefined
                }
                max={
                  replaceInputValue === "MIN_DATE" ||
                  replaceInputValue === "MAX_DATE"
                    ? "9999-12-31T23:59"
                    : undefined
                }
                value={replaceValue}
                onChange={(e) => setReplaceValue(e.target.value)}
                placeholder="Ticket amount"
              />
            )}
            <div style={{ position: "relative" }}>
              <input
                type="number"
                min={0}
                max={100}
                value={percentageValue}
                onChange={(e) => setPercentageValue(Number(e.target.value))}
                style={{ paddingRight: "20px" }}
              />
              <span
                style={{
                  position: "absolute",
                  right: "8px",
                  top: "50%",
                  transform: "translateY(-50%)",
                }}
              >
                %
              </span>
            </div>
          </>
        )}

        {isComplex && (
          <>
            {(replaceInputValue === "AND" || replaceInputValue === "OR") && (
              <div style={{ position: "relative" }}>
                <input
                  type="number"
                  min={0}
                  max={100}
                  value={percentageValue}
                  onChange={(e) => setPercentageValue(Number(e.target.value))}
                  style={{ paddingRight: "20px" }}
                />
                <span
                  style={{
                    position: "absolute",
                    right: "8px",
                    top: "50%",
                    transform: "translateY(-50%)",
                  }}
                >
                  %
                </span>
              </div>
            )}
            <select
              onChange={(e) => {
                const type = e.target.value as SimpleDiscountPolicyTypes;

                if (type) {
                  setLeftPolicy(createSimplePolicy(type, 1, percentageValue));
                }
              }}
            >
              <option value="">Choose left policy</option>

              {simpleDiscountPolicyTypes.map((type) => (
                <option key={type} value={type}>
                  {type.replace("_", " ")}
                </option>
              ))}
            </select>

            <select
              onChange={(e) => {
                const type = e.target.value as SimpleDiscountPolicyTypes;

                if (type) {
                  setRightPolicy(createSimplePolicy(type, 1, percentageValue));
                }
              }}
            >
              <option value="">Choose right policy</option>

              {simpleDiscountPolicyTypes.map((type) => (
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
            let newPolicy: DiscountPolicyDTO;

            if (isComplex) {
              if (!leftPolicy || !rightPolicy) {
                return;
              }
              if (replaceInputValue === "SUM" || replaceInputValue === "MAX") {
                newPolicy = {
                  type: replaceInputValue,
                  left: leftPolicy,
                  right: rightPolicy,
                };
              } else {
                newPolicy = {
                  type: replaceInputValue,
                  left: leftPolicy,
                  right: rightPolicy,
                  percentage: percentageValue,
                };
              }
            } else {
              if (replaceInputValue === "SIMPLE") {
                newPolicy = {
                  type: "SIMPLE",
                  percentage: percentageValue,
                };
              } else if (
                replaceInputValue === "MIN_DATE" ||
                replaceInputValue === "MAX_DATE"
              ) {
                const value = String(replaceValue);
                if (!value || value.trim() === "") {
                  alert("Enter valid value");
                  return;
                }
                newPolicy = createSimplePolicy(
                  replaceInputValue,
                  value,
                  percentageValue,
                );
              } else {
                const value = Number(replaceValue);

                if (!value || value <= 0) {
                  alert("Enter valid value");
                  return;
                }

                newPolicy = createSimplePolicy(
                  replaceInputValue,
                  value,
                  percentageValue,
                );
              }
            }
            setLeftPolicy(null);
            setRightPolicy(null);
            setReplaceValue("");
            setAction(null);
            setShowPopup(false);

            props.data.onReplace(props.data.path, newPolicy);
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
                  {action === "CHANGE" ? (
                    <>
                      <div style={{ position: "relative" }}>
                        <input
                          type="number"
                          min={0}
                          max={100}
                          value={percentageValue}
                          onChange={(e) =>
                            setPercentageValue(Number(e.target.value))
                          }
                          style={{ paddingRight: "20px" }}
                        />
                        <span
                          style={{
                            position: "absolute",
                            right: "8px",
                            top: "50%",
                            transform: "translateY(-50%)",
                          }}
                        >
                          %
                        </span>
                      </div>

                      <button
                        onClick={() => {
                          if (percentageValue < 0 || percentageValue > 100) {
                            alert("Please enter a valid percentage (0-100)");
                            return;
                          }
                          props.data.onChangeGoal(
                            props.data.path,
                            null,
                            percentageValue,
                          );

                          setPercentageValue(0);
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

            case "SUM":
            case "MAX":
              return (
                <>
                  <button
                    onClick={() => {
                      props.data.onSwap(props.data.path);
                      setShowPopup(false);
                    }}
                  >
                    Change {props.data.type === "SUM" ? "to MAX" : "to SUM"}
                  </button>
                  {renderReplace()}
                </>
              );

            case "SIMPLE":
            case "MIN_DATE":
            case "MAX_DATE":
            case "MIN_TICKETS":
            case "MAX_TICKETS":
              return (
                <>
                  {action === "CHANGE" ? (
                    <>
                      {props.data.type !== "SIMPLE" && (
                        <input
                          type={
                            props.data.type === "MIN_DATE" ||
                            props.data.type === "MAX_DATE"
                              ? "datetime-local"
                              : "number"
                          }
                          min={
                            props.data.type === "MIN_TICKETS" ||
                            props.data.type === "MAX_TICKETS"
                              ? 1
                              : undefined
                          }
                          max={
                            props.data.type === "MIN_DATE" ||
                            props.data.type === "MAX_DATE"
                              ? "9999-12-31T23:59"
                              : undefined
                          }
                          value={inputValue}
                          onChange={(e) => setInputValue(e.target.value)}
                          placeholder="Ticket amount"
                        />
                      )}
                      <div style={{ position: "relative" }}>
                        <input
                          type="number"
                          min={0}
                          max={100}
                          value={percentageValue}
                          onChange={(e) =>
                            setPercentageValue(Number(e.target.value))
                          }
                          style={{ paddingRight: "20px" }}
                        />
                        <span
                          style={{
                            position: "absolute",
                            right: "8px",
                            top: "50%",
                            transform: "translateY(-50%)",
                          }}
                        >
                          %
                        </span>
                      </div>

                      <button
                        onClick={() => {
                          if (percentageValue < 0 || percentageValue > 100) {
                            alert("Please enter a valid percentage (0-100)");
                            return;
                          }
                          if (props.data.type === "SIMPLE") {
                            props.data.onChangeGoal(
                              props.data.path,
                              null,
                              percentageValue,
                            );

                            setPercentageValue(0);
                            setInputValue("");
                            setAction(null);
                            setShowPopup(false);
                          } else if (
                            props.data.type === "MIN_DATE" ||
                            props.data.type === "MAX_DATE"
                          ) {
                            const dateValue = new Date(inputValue as string);
                            if (isNaN(dateValue.getTime())) {
                              alert("Please enter a valid date");
                              return;
                            }
                            props.data.onChangeGoal(
                              props.data.path,
                              dateValue.toISOString(),
                              percentageValue,
                            );

                            setPercentageValue(0);
                            setInputValue("");
                            setAction(null);
                            setShowPopup(false);
                          } else {
                            const goalVal = Number(inputValue);

                            if (!goalVal || goalVal <= 0) {
                              alert("Please enter a valid value");
                              return;
                            }
                            props.data.onChangeGoal(
                              props.data.path,
                              goalVal,
                              percentageValue,
                            );

                            setPercentageValue(0);
                            setInputValue("");
                            setAction(null);
                            setShowPopup(false);
                          }
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
        {props.data.type !== "NONE" &&
          props.data.type !== "SUM" &&
          props.data.type !== "MAX" && (
            <label>
              {" "}
              <br />
              Percentage: {props.data.percentage}%
            </label>
          )}

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
