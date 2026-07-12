import { Background, ReactFlow, type Edge } from "@xyflow/react";
import "@xyflow/react/dist/style.css";

import type {
  AndDTO,
  MaxAgeDTO,
  MaxTicketsDTO,
  MinAgeDTO,
  MinTicketsDTO,
  NullablePurchasePolicyDTO,
  OrDTO,
  PurchasePolicyDTO,
} from "../../DTOs/PurchasePolicyDTO";

import { useState } from "react";
import "./CSS/PurchasePolicyTree.css";
import { PolicyNode, type PolicyPath } from "./PolicyNode";

type Props = {
  policy: NullablePurchasePolicyDTO;
  onSave: (policy: NullablePurchasePolicyDTO) => Promise<void>;
};

type PolicyEdge = Edge;
type CompositeType = "AND" | "OR";

const HORIZONTAL_GAP = 550;
const VERTICAL_GAP = 220;

function isCompositePolicy(
    policy: PurchasePolicyDTO,
): policy is AndDTO | OrDTO {
  return policy.type === "AND" || policy.type === "OR";
}

function getSubtreeWidth(policy: PurchasePolicyDTO): number {
  if (isCompositePolicy(policy)) {
    return getSubtreeWidth(policy.left) + getSubtreeWidth(policy.right);
  }

  return 1;
}

function buildTree(
    policy: PurchasePolicyDTO,
    centerX: number,
    level: number,
    nodes: PolicyNode[],
    edges: PolicyEdge[],
    path: PolicyPath,
    onSwap: (path: PolicyPath) => void,
    onChangeGoal: (path: PolicyPath, newGoal: number) => void,
    onReplace: (path: PolicyPath, newPolicy: PurchasePolicyDTO) => void,
    onAdd: (
        path: PolicyPath,
        operator: CompositeType,
        newPolicy: PurchasePolicyDTO,
    ) => void,
    onDelete: () => void,
): string {
  const id = crypto.randomUUID();
  const y = level * VERTICAL_GAP;

  if (isCompositePolicy(policy)) {
    nodes.push({
      id,
      position: { x: centerX, y },
      className: "hierarchy-flow-node",
      type: "policy",
      data: {
        label: policy.type,
        type: policy.type,
        path,
        onSwap,
        onChangeGoal,
        onReplace,
        onAdd,
        onDelete,
      },
    });

    const leftWidth = getSubtreeWidth(policy.left);
    const rightWidth = getSubtreeWidth(policy.right);

    const leftCenter =
        centerX - (rightWidth * HORIZONTAL_GAP) / 2;

    const rightCenter =
        centerX + (leftWidth * HORIZONTAL_GAP) / 2;

    const leftId = buildTree(
        policy.left,
        leftCenter,
        level + 1,
        nodes,
        edges,
        [...path, "left"],
        onSwap,
        onChangeGoal,
        onReplace,
        onAdd,
        onDelete,
    );

    const rightId = buildTree(
        policy.right,
        rightCenter,
        level + 1,
        nodes,
        edges,
        [...path, "right"],
        onSwap,
        onChangeGoal,
        onReplace,
        onAdd,
        onDelete,
    );

    edges.push({
      id: `${id}-${leftId}`,
      source: id,
      target: leftId,
      type: "smoothstep",
    });

    edges.push({
      id: `${id}-${rightId}`,
      source: id,
      target: rightId,
      type: "smoothstep",
    });

    return id;
  }

  let label: string;

  switch (policy.type) {
    case "MIN_AGE":
      label = `Min Age: ${(policy as MinAgeDTO).minAge}`;
      break;

    case "MAX_AGE":
      label = `Max Age: ${(policy as MaxAgeDTO).maxAge}`;
      break;

    case "MIN_TICKETS":
      label = `Min Tickets: ${
          (policy as MinTicketsDTO).minTickets
      }`;
      break;

    case "MAX_TICKETS":
      label = `Max Tickets: ${
          (policy as MaxTicketsDTO).maxTickets
      }`;
      break;
  }

  nodes.push({
    id,
    position: { x: centerX, y },
    className: "hierarchy-flow-node",
    type: "policy",
    data: {
      label,
      type: policy.type,
      path,
      onSwap,
      onChangeGoal,
      onReplace,
      onAdd,
      onDelete,
    },
  });

  return id;
}

function createFlow(
    policy: NullablePurchasePolicyDTO,
    onSwap: (path: PolicyPath) => void,
    onChangeGoal: (path: PolicyPath, newGoal: number) => void,
    onReplace: (path: PolicyPath, newPolicy: PurchasePolicyDTO) => void,
    onAdd: (
        path: PolicyPath,
        operator: CompositeType,
        newPolicy: PurchasePolicyDTO,
    ) => void,
    onDelete: () => void,
) {
  const nodes: PolicyNode[] = [];
  const edges: PolicyEdge[] = [];

  if (!policy) {
    nodes.push({
      id: crypto.randomUUID(),
      position: { x: 0, y: 0 },
      className: "hierarchy-flow-node",
      type: "policy",
      data: {
        label: "Create a policy",
        type: "NONE",
        path: [],
        onSwap,
        onChangeGoal,
        onReplace,
        onAdd,
        onDelete,
      },
    });

    return { nodes, edges };
  }

  buildTree(
      policy,
      0,
      0,
      nodes,
      edges,
      [],
      onSwap,
      onChangeGoal,
      onReplace,
      onAdd,
      onDelete,
  );

  return { nodes, edges };
}

function swapPolicyAtPath(
    policy: NullablePurchasePolicyDTO,
    path: PolicyPath,
): NullablePurchasePolicyDTO {
  if (!policy) {
    return null;
  }

  if (path.length === 0) {
    if (!isCompositePolicy(policy)) {
      return policy;
    }

    return {
      ...policy,
      type: policy.type === "AND" ? "OR" : "AND",
    };
  }

  if (!isCompositePolicy(policy)) {
    return policy;
  }

  const [head, ...rest] = path;

  return {
    ...policy,
    [head]:
        head === "left"
            ? swapPolicyAtPath(policy.left, rest)
            : swapPolicyAtPath(policy.right, rest),
  };
}

function changeGoalAtPath(
    policy: PurchasePolicyDTO,
    path: PolicyPath,
    newGoal: number,
): PurchasePolicyDTO {
  if (path.length === 0) {
    switch (policy.type) {
      case "MIN_AGE":
        return { ...policy, minAge: newGoal };

      case "MAX_AGE":
        return { ...policy, maxAge: newGoal };

      case "MIN_TICKETS":
        return { ...policy, minTickets: newGoal };

      case "MAX_TICKETS":
        return { ...policy, maxTickets: newGoal };

      default:
        return policy;
    }
  }

  if (!isCompositePolicy(policy)) {
    return policy;
  }

  const [head, ...rest] = path;

  return {
    ...policy,
    [head]:
        head === "left"
            ? changeGoalAtPath(policy.left, rest, newGoal)
            : changeGoalAtPath(policy.right, rest, newGoal),
  };
}

function replacePolicyAtPath(
    policy: PurchasePolicyDTO,
    path: PolicyPath,
    newPolicy: PurchasePolicyDTO,
): PurchasePolicyDTO {
  if (path.length === 0) {
    return newPolicy;
  }

  if (!isCompositePolicy(policy)) {
    return policy;
  }

  const [head, ...rest] = path;

  return {
    ...policy,
    [head]:
        head === "left"
            ? replacePolicyAtPath(
                policy.left,
                rest,
                newPolicy,
            )
            : replacePolicyAtPath(
                policy.right,
                rest,
                newPolicy,
            ),
  };
}

function addPolicyAtPath(
    policy: PurchasePolicyDTO,
    path: PolicyPath,
    operator: CompositeType,
    newPolicy: PurchasePolicyDTO,
): PurchasePolicyDTO {
  if (path.length === 0) {
    return {
      type: operator,
      left: policy,
      right: newPolicy,
    };
  }

  if (!isCompositePolicy(policy)) {
    return policy;
  }

  const [head, ...rest] = path;

  return {
    ...policy,
    [head]:
        head === "left"
            ? addPolicyAtPath(
                policy.left,
                rest,
                operator,
                newPolicy,
            )
            : addPolicyAtPath(
                policy.right,
                rest,
                operator,
                newPolicy,
            ),
  };
}

export default function PurchasePolicyTree({
                                             policy,
                                             onSave,
                                           }: Props) {
  const [currentPolicy, setCurrentPolicy] =
      useState<NullablePurchasePolicyDTO>(policy);

  const nodeTypes = {
    policy: PolicyNode,
  };

  async function handleSave() {
    await onSave(currentPolicy);
  }

  function swapPolicy(path: PolicyPath) {
    setCurrentPolicy((previous) =>
        swapPolicyAtPath(previous, path),
    );
  }

  function changeGoal(
      path: PolicyPath,
      goal: number,
  ) {
    setCurrentPolicy((previous) => {
      if (!previous) {
        return previous;
      }

      return changeGoalAtPath(
          previous,
          path,
          goal,
      );
    });
  }

  function replacePolicy(
      path: PolicyPath,
      newPolicy: PurchasePolicyDTO,
  ) {
    setCurrentPolicy((previous) => {
      if (!previous) {
        return newPolicy;
      }

      return replacePolicyAtPath(
          previous,
          path,
          newPolicy,
      );
    });
  }

  function addPolicy(
      path: PolicyPath,
      operator: CompositeType,
      newPolicy: PurchasePolicyDTO,
  ) {
    setCurrentPolicy((previous) => {
      if (!previous) {
        return newPolicy;
      }

      return addPolicyAtPath(
          previous,
          path,
          operator,
          newPolicy,
      );
    });
  }

  function deletePolicy() {
    setCurrentPolicy(null);
  }

  const { nodes, edges } = createFlow(
      currentPolicy,
      swapPolicy,
      changeGoal,
      replacePolicy,
      addPolicy,
      deletePolicy,
  );

  return (
      <div style={{ width: "100%", height: "600px" }}>
        <ReactFlow
            nodes={nodes}
            edges={edges}
            fitView
            proOptions={{ hideAttribution: true }}
            nodeTypes={nodeTypes}
        >
          <Background />
        </ReactFlow>

        <button
            type="button"
            onClick={handleSave}
        >
          Save Changes
        </button>
      </div>
  );
}