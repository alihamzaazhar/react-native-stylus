import type {
  StylusBounds,
  StylusDocument,
  StylusLayer,
  StylusPoint,
  StylusShape,
  StylusStroke,
  StylusTransform,
} from "./types";

const DEFAULT_LAYER_ID = "layer-1";

function uniqueId(prefix: string): string {
  return `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;
}

export function createStylusDocument(
  options: Partial<
    Pick<
      StylusDocument,
      "id" | "width" | "height" | "backgroundColor" | "metadata"
    >
  > = {},
): StylusDocument {
  const now = Date.now();
  return {
    version: 1,
    id: options.id ?? uniqueId("document"),
    width: options.width ?? 1080,
    height: options.height ?? 1920,
    backgroundColor: options.backgroundColor ?? "#FFFFFF",
    activeLayerId: DEFAULT_LAYER_ID,
    layers: [
      {
        id: DEFAULT_LAYER_ID,
        name: "Ink",
        visible: true,
        locked: false,
        opacity: 1,
      },
    ],
    strokes: [],
    shapes: [],
    metadata: { createdAt: now, updatedAt: now, ...options.metadata },
  };
}

export function createStylusLayer(name = "Layer"): StylusLayer {
  return {
    id: uniqueId("layer"),
    name,
    visible: true,
    locked: false,
    opacity: 1,
  };
}

export function serializeStylusDocument(document: StylusDocument): string {
  return JSON.stringify({
    ...document,
    metadata: { ...document.metadata, updatedAt: Date.now() },
  });
}

export function parseStylusDocument(value: string): StylusDocument {
  const parsed = JSON.parse(value) as Partial<StylusDocument>;
  if (
    parsed.version !== 1 ||
    !Array.isArray(parsed.layers) ||
    !Array.isArray(parsed.strokes)
  )
    throw new Error("Unsupported or invalid stylus document");
  const fallback = createStylusDocument();
  const layers = parsed.layers.length ? parsed.layers : fallback.layers;
  return {
    ...fallback,
    ...parsed,
    version: 1,
    layers,
    activeLayerId: layers.some((layer) => layer.id === parsed.activeLayerId)
      ? parsed.activeLayerId!
      : layers[0].id,
    strokes: parsed.strokes,
    shapes: Array.isArray(parsed.shapes) ? parsed.shapes : [],
    metadata: { ...fallback.metadata, ...parsed.metadata },
  };
}

export function getStrokeBounds(
  strokes: readonly StylusStroke[],
): StylusBounds | null {
  const points = strokes.flatMap((stroke) => stroke.points);
  if (!points.length) return null;
  let minX = points[0].x;
  let minY = points[0].y;
  let maxX = minX;
  let maxY = minY;
  for (const point of points) {
    minX = Math.min(minX, point.x);
    minY = Math.min(minY, point.y);
    maxX = Math.max(maxX, point.x);
    maxY = Math.max(maxY, point.y);
  }
  return { x: minX, y: minY, width: maxX - minX, height: maxY - minY };
}

export function transformStrokes(
  strokes: readonly StylusStroke[],
  transform: StylusTransform,
): StylusStroke[] {
  const bounds = getStrokeBounds(strokes);
  const originX =
    transform.originX ?? (bounds ? bounds.x + bounds.width / 2 : 0);
  const originY =
    transform.originY ?? (bounds ? bounds.y + bounds.height / 2 : 0);
  const scaleX = transform.scaleX ?? 1;
  const scaleY = transform.scaleY ?? 1;
  const radians = transform.rotation ?? 0;
  const cosine = Math.cos(radians);
  const sine = Math.sin(radians);
  return strokes.map((stroke) => ({
    ...stroke,
    points: stroke.points.map((point) => {
      const x = (point.x - originX) * scaleX;
      const y = (point.y - originY) * scaleY;
      return {
        ...point,
        x: x * cosine - y * sine + originX + (transform.translateX ?? 0),
        y: x * sine + y * cosine + originY + (transform.translateY ?? 0),
        orientation: point.orientation + radians,
      };
    }),
  }));
}

function pointInsidePolygon(
  point: Pick<StylusPoint, "x" | "y">,
  polygon: readonly Pick<StylusPoint, "x" | "y">[],
): boolean {
  let inside = false;
  for (
    let i = 0, previous = polygon.length - 1;
    i < polygon.length;
    previous = i++
  ) {
    const a = polygon[i];
    const b = polygon[previous];
    if (
      a.y > point.y !== b.y > point.y &&
      point.x < ((b.x - a.x) * (point.y - a.y)) / (b.y - a.y) + a.x
    )
      inside = !inside;
  }
  return inside;
}

export function selectStrokesByLasso(
  strokes: readonly StylusStroke[],
  polygon: readonly Pick<StylusPoint, "x" | "y">[],
): string[] {
  if (polygon.length < 3) return [];
  return strokes
    .filter((stroke) =>
      stroke.points.some((point) => pointInsidePolygon(point, polygon)),
    )
    .map((stroke) => stroke.id);
}

export function eraseWholeStrokes(
  strokes: readonly StylusStroke[],
  x: number,
  y: number,
  radius: number,
): StylusStroke[] {
  const squaredRadius = radius * radius;
  return strokes.filter(
    (stroke) =>
      !stroke.points.some(
        (point) => (point.x - x) ** 2 + (point.y - y) ** 2 <= squaredRadius,
      ),
  );
}

export function duplicateStrokes(
  strokes: readonly StylusStroke[],
  offset = 16,
): StylusStroke[] {
  return strokes.map((stroke) => ({
    ...stroke,
    id: uniqueId("stroke"),
    points: stroke.points.map((point) => ({
      ...point,
      x: point.x + offset,
      y: point.y + offset,
    })),
  }));
}

function escapeXml(value: string): string {
  const entities: Record<string, string> = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&apos;",
  };
  return value.replace(/[&<>"']/g, (character) => entities[character]);
}

function shapeSvg(shape: StylusShape): string {
  const common = `stroke="${escapeXml(shape.color)}" stroke-width="${shape.width}" opacity="${shape.opacity}" fill="${escapeXml(shape.fill ?? "none")}"`;
  if (shape.type === "line" || shape.type === "arrow") {
    const marker = shape.type === "arrow" ? ' marker-end="url(#arrow)"' : "";
    return `<line x1="${shape.x}" y1="${shape.y}" x2="${shape.endX}" y2="${shape.endY}" ${common}${marker}/>`;
  }
  const x = Math.min(shape.x, shape.endX);
  const y = Math.min(shape.y, shape.endY);
  const width = Math.abs(shape.endX - shape.x);
  const height = Math.abs(shape.endY - shape.y);
  if (shape.type === "ellipse")
    return `<ellipse cx="${x + width / 2}" cy="${y + height / 2}" rx="${width / 2}" ry="${height / 2}" ${common}/>`;
  return `<rect x="${x}" y="${y}" width="${width}" height="${height}" ${common}/>`;
}

export function exportStylusDocumentToSvg(document: StylusDocument): string {
  const layers = new Map(document.layers.map((layer) => [layer.id, layer]));
  const paths = document.strokes
    .filter(
      (stroke) =>
        layers.get(stroke.layerId ?? document.activeLayerId)?.visible !== false,
    )
    .map((stroke) => {
      const layerOpacity =
        layers.get(stroke.layerId ?? document.activeLayerId)?.opacity ?? 1;
      const data = stroke.points
        .map((point, index) => `${index ? "L" : "M"}${point.x} ${point.y}`)
        .join(" ");
      return `<path d="${data}" fill="none" stroke="${escapeXml(stroke.color)}" stroke-width="${stroke.width}" stroke-linecap="round" stroke-linejoin="round" opacity="${stroke.opacity * layerOpacity}"/>`;
    });
  const shapes = document.shapes
    .filter((shape) => layers.get(shape.layerId)?.visible !== false)
    .map(shapeSvg);
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${document.width}" height="${document.height}" viewBox="0 0 ${document.width} ${document.height}"><defs><marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto"><path d="M0,0 L0,6 L9,3 z" fill="context-stroke"/></marker></defs><rect width="100%" height="100%" fill="${escapeXml(document.backgroundColor)}"/>${paths.join("")}${shapes.join("")}</svg>`;
}
