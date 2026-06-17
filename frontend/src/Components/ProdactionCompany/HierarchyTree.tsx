import {
  Background,
  Controls,
  ReactFlow,
  type Edge,
  type Node,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useEffect, useState, type ReactNode } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type {
  HierarchyNodeDTO,
  ManagerPermissions,
} from "../../DTOs/ProductionCompanyDTO";
import "./CSS/HierarchyTree.css";

const API_BASE = "http://localhost:8080";

type HierarchyFlowNodeData = {
  label: ReactNode;
  userID: string;
  roleType: string;
  permissions: ManagerPermissions[];
};

type HierarchyFlowNode = Node<HierarchyFlowNodeData>;

type HierarchyFlowEdge = Edge;

function buildReactFlowHierarchy(nodes: HierarchyNodeDTO[]): {
  flowNodes: HierarchyFlowNode[];
} {
  const childrenByParent = new Map<string, HierarchyNodeDTO[]>();
  const nodeById = new Map<string, HierarchyNodeDTO>();
  const roots = nodes.filter((node) => !node.parentID);

  nodes.forEach((node) => {
    nodeById.set(node.userID, node);

    if (node.parentID) {
      const children = childrenByParent.get(node.parentID) ?? [];
      children.push(node);
      childrenByParent.set(node.parentID, children);
    }
  });

  const levels: HierarchyNodeDTO[][] = [];
  const queue: { node: HierarchyNodeDTO; level: number }[] = roots.map(
    (node) => ({
      node,
      level: 0,
    }),
  );

  while (queue.length > 0) {
    const current = queue.shift();

    if (!current) {
      continue;
    }

    if (!levels[current.level]) {
      levels[current.level] = [];
    }

    levels[current.level].push(current.node);

    const children = childrenByParent.get(current.node.userID) ?? [];
    children.forEach((child) => {
      queue.push({
        node: child,
        level: current.level + 1,
      });
    });
  }

  const horizontalGap = 300;
  const verticalGap = 180;
  const flowNodes: HierarchyFlowNode[] = [];

  levels.forEach((levelNodes, levelIndex) => {
    const rowWidth = (levelNodes.length - 1) * horizontalGap;

    levelNodes.forEach((node, nodeIndex) => {
      flowNodes.push({
        id: node.userID,
        className: "hierarchy-flow-node",
        position: {
          x: nodeIndex * horizontalGap - rowWidth / 2,
          y: levelIndex * verticalGap,
        },
        data: {
          label: (
            <>
              <div>{node.userID}</div>
              <div>{node.roleType}</div>
              {node.roleType === "MANAGER" &&
                node.permissions.map((permission) => (
                  <div key={permission}>{permission}</div>
                ))}
            </>
          ),
          userID: node.userID,
          roleType: node.roleType,
          permissions: node.permissions,
        },
      });
    });
  });

  return { flowNodes };
}

export default function HierarchyTree() {
  const { companyId } = useParams();
  const [hierarchy, setHierarchy] = useState<HierarchyNodeDTO[]>([]);
  const [flowNodes, setFlowNodes] = useState<HierarchyFlowNode[]>([]);
  const [flowEdges, setFlowEdges] = useState<HierarchyFlowEdge[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const apiFetch = useApiFetch();

  useEffect(() => {
    async function loadTree() {
      setLoading(true);
      setError("");
      setHierarchy([]);

      if (!companyId) {
        setError("Missing company id");
        setLoading(false);
        return;
      }

      try {
        const response = await apiFetch(
          `${API_BASE}/production-companies/${companyId}/hierarchy-tree`,
          {
            method: "GET",
          },
        );

        const data = await response.json();

        if (!response.ok) {
          throw new Error(
            data?.message || data?.error || "Failed to load Hierarchy",
          );
        }

        setHierarchy(data);

        const { flowNodes: newFlowNodes } = buildReactFlowHierarchy(hierarchy);
        setFlowNodes(newFlowNodes);

        const newFlowEdges: HierarchyFlowEdge[] = hierarchy
          .filter((node) => node.parentID)
          .map((node) => ({
            id: `${node.parentID}-${node.userID}`,
            source: node.parentID,
            target: node.userID,
            type: "smoothstep",
          }));
        setFlowEdges(newFlowEdges);
      } catch (error) {
        setError(
          error instanceof Error ? error.message : "Failed to load Hierarchy",
        );
      } finally {
        setLoading(false);
      }
    }

    void loadTree();
  }, [companyId, apiFetch]);

  return (
    <div>
      {loading ? (
        <div>
          <div>Loading Hierarchy Tree...</div>
        </div>
      ) : error ? (
        <div>
          <div>Could not load hierarchy tree</div>
          <p>{error}</p>
        </div>
      ) : (
        <>
          <div>
            <h2>Hierarchy Tree</h2>
            <div>
              {hierarchy.length === 0 ? (
                <div>No hierarchy data found</div>
              ) : (
                <div style={{ width: "100%", height: "500px" }}>
                  <ReactFlow nodes={flowNodes} edges={flowEdges} fitView>
                    <Background />
                    <Controls />
                  </ReactFlow>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
