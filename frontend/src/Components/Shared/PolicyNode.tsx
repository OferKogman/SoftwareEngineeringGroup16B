import {
    Handle,
    Position,
    type Node,
    type NodeProps,
} from "@xyflow/react";

import "@xyflow/react/dist/style.css";
import { useState } from "react";

import type {
    PurchasePolicyDTO,
    PurchasePolicyTypes,
    SimplePolicyTypes,
} from "../../DTOs/PurchasePolicyDTO";

import "./CSS/PolicyNode.css";

export type PolicyPath = ("left" | "right")[];

type CompositeType = "AND" | "OR";

type PolicyNodeData = {
    label: string;
    type: PurchasePolicyTypes | "NONE";
    path: PolicyPath;

    onSwap: (path: PolicyPath) => void;

    onChangeGoal: (
        path: PolicyPath,
        newGoal: number,
    ) => void;

    onReplace: (
        path: PolicyPath,
        newPolicy: PurchasePolicyDTO,
    ) => void;

    onAdd: (
        path: PolicyPath,
        operator: CompositeType,
        newPolicy: PurchasePolicyDTO,
    ) => void;

    onDelete: () => void;
};

export type PolicyNode = Node<PolicyNodeData>;

const simplePolicyTypes: SimplePolicyTypes[] = [
    "MIN_AGE",
    "MAX_AGE",
    "MIN_TICKETS",
    "MAX_TICKETS",
];

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
    }
}

function isValidValue(
    type: SimplePolicyTypes,
    value: number,
): boolean {
    if (!Number.isInteger(value)) {
        return false;
    }

    if (
        type === "MIN_AGE" ||
        type === "MAX_AGE"
    ) {
        return value >= 0;
    }

    return value >= 1;
}

export function PolicyNode(
    props: NodeProps<PolicyNode>,
) {
    const [showPopup, setShowPopup] =
        useState(false);

    const [error, setError] =
        useState("");

    const [action, setAction] = useState<
        "CHANGE" | "REPLACE" | "ADD" | null
    >(null);

    const [inputValue, setInputValue] =
        useState("");

    const [selectedType, setSelectedType] =
        useState<SimplePolicyTypes>("MIN_AGE");

    const [selectedValue, setSelectedValue] =
        useState("");

    const [operator, setOperator] =
        useState<CompositeType>("AND");

    function resetEditor() {
        setError("");
        setInputValue("");
        setSelectedType("MIN_AGE");
        setSelectedValue("");
        setOperator("AND");
        setAction(null);
    }

    function closeEditor() {
        resetEditor();
        setShowPopup(false);
    }

    function readSelectedPolicy():
        | PurchasePolicyDTO
        | null {

        const value = Number(selectedValue);

        if (!isValidValue(selectedType, value)) {
            setError(
                selectedType === "MIN_AGE" ||
                selectedType === "MAX_AGE"
                    ? "Age must be a non-negative whole number."
                    : "Ticket amount must be a positive whole number.",
            );

            return null;
        }

        return createSimplePolicy(
            selectedType,
            value,
        );
    }

    function renderSimplePolicyInputs() {
        return (
            <>
                <select
                    value={selectedType}
                    onChange={(event) =>
                        setSelectedType(
                            event.target
                                .value as SimplePolicyTypes,
                        )
                    }
                >
                    {simplePolicyTypes.map((type) => (
                        <option
                            key={type}
                            value={type}
                        >
                            {type.replace("_", " ")}
                        </option>
                    ))}
                </select>

                <input
                    type="number"
                    value={selectedValue}
                    onChange={(event) =>
                        setSelectedValue(
                            event.target.value,
                        )
                    }
                    placeholder="Value"
                />
            </>
        );
    }

    function renderReplace() {
        if (action !== "REPLACE") {
            return null;
        }

        return (
            <>
                {renderSimplePolicyInputs()}

                <button
                    type="button"
                    onClick={() => {
                        const newPolicy =
                            readSelectedPolicy();

                        if (!newPolicy) {
                            return;
                        }

                        props.data.onReplace(
                            props.data.path,
                            newPolicy,
                        );

                        closeEditor();
                    }}
                >
                    {props.data.type === "NONE"
                        ? "Create"
                        : "Replace"}
                </button>
            </>
        );
    }

    function renderAdd() {
        if (action !== "ADD") {
            return null;
        }

        return (
            <>
                <select
                    value={operator}
                    onChange={(event) =>
                        setOperator(
                            event.target
                                .value as CompositeType,
                        )
                    }
                >
                    <option value="AND">AND</option>
                    <option value="OR">OR</option>
                </select>

                {renderSimplePolicyInputs()}

                <button
                    type="button"
                    onClick={() => {
                        const newPolicy =
                            readSelectedPolicy();

                        if (!newPolicy) {
                            return;
                        }

                        props.data.onAdd(
                            props.data.path,
                            operator,
                            newPolicy,
                        );

                        closeEditor();
                    }}
                >
                    Add policy
                </button>
            </>
        );
    }

    function renderActions() {
        if (props.data.type === "NONE") {
            return action === "REPLACE" ? (
                renderReplace()
            ) : (
                <button
                    type="button"
                    onClick={() =>
                        setAction("REPLACE")
                    }
                >
                    Create policy
                </button>
            );
        }

        return (
            <>
                {props.data.path.length === 0 && (
                    <button
                        type="button"
                        onClick={() => {
                            props.data.onDelete();
                            closeEditor();
                        }}
                    >
                        Delete policy
                    </button>
                )}

                {(props.data.type === "AND" ||
                    props.data.type === "OR") && (
                    <button
                        type="button"
                        onClick={() => {
                            props.data.onSwap(
                                props.data.path,
                            );

                            closeEditor();
                        }}
                    >
                        Change{" "}
                        {props.data.type === "AND"
                            ? "to OR"
                            : "to AND"}
                    </button>
                )}

                {props.data.type !== "AND" &&
                    props.data.type !== "OR" && (
                        <>
                            {action === "CHANGE" ? (
                                <>
                                    <input
                                        type="number"
                                        value={inputValue}
                                        onChange={(event) =>
                                            setInputValue(
                                                event.target.value,
                                            )
                                        }
                                        placeholder="New value"
                                    />

                                    <button
                                        type="button"
                                        onClick={() => {
                                            const value =
                                                Number(inputValue);

                                            const type =
                                                props.data
                                                    .type as SimplePolicyTypes;

                                            if (
                                                !isValidValue(
                                                    type,
                                                    value,
                                                )
                                            ) {
                                                setError(
                                                    "Enter a valid whole number.",
                                                );

                                                return;
                                            }

                                            props.data.onChangeGoal(
                                                props.data.path,
                                                value,
                                            );

                                            closeEditor();
                                        }}
                                    >
                                        Change value
                                    </button>
                                </>
                            ) : (
                                <button
                                    type="button"
                                    onClick={() =>
                                        setAction("CHANGE")
                                    }
                                >
                                    Change value
                                </button>
                            )}
                        </>
                    )}

                <button
                    type="button"
                    onClick={() => setAction("ADD")}
                >
                    Add policy
                </button>

                <button
                    type="button"
                    onClick={() =>
                        setAction("REPLACE")
                    }
                >
                    Replace policy
                </button>

                {renderAdd()}
                {renderReplace()}
            </>
        );
    }

    return (
        <div className="policy-node-wrapper">
            <div
                className="policy-node"
                onClick={() =>
                    setShowPopup(
                        (previous) => !previous,
                    )
                }
            >
                <label>{props.data.label}</label>

                <Handle
                    type="target"
                    position={Position.Top}
                />

                <Handle
                    type="source"
                    position={Position.Bottom}
                />
            </div>

            {showPopup && (
                <div
                    className="policy-node-popup"
                    onClick={(event) =>
                        event.stopPropagation()
                    }
                >
                    {error && <p>{error}</p>}

                    {renderActions()}

                    <button
                        type="button"
                        onClick={closeEditor}
                    >
                        Close
                    </button>
                </div>
            )}
        </div>
    );
}