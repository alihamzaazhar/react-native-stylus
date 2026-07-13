import {createStylusDocument, parseStylusDocument, serializeStylusDocument} from './document';
import type {StylusAnnotationDocument, StylusAnnotationTarget, StylusDocumentStorage} from './types';

export function createAnnotationDocument(target: StylusAnnotationTarget): StylusAnnotationDocument {
  return {...createStylusDocument({width: target.width, height: target.height, metadata: {targetType: target.type, targetUri: target.uri, targetPage: target.page ?? 0}}), target};
}

export function serializeAnnotationDocument(document: StylusAnnotationDocument): string {
  return JSON.stringify({...JSON.parse(serializeStylusDocument(document)), target: document.target});
}

export function parseAnnotationDocument(value: string): StylusAnnotationDocument {
  const raw = JSON.parse(value) as {target?: StylusAnnotationTarget};
  if (!raw.target || (raw.target.type !== 'image' && raw.target.type !== 'pdf')) throw new Error('Invalid annotation target');
  return {...parseStylusDocument(value), target: raw.target};
}

export function annotationPointToViewport(document: StylusAnnotationDocument, x: number, y: number, viewportWidth: number, viewportHeight: number) {
  const scale = Math.min(viewportWidth / document.width, viewportHeight / document.height);
  const offsetX = (viewportWidth - document.width * scale) / 2;
  const offsetY = (viewportHeight - document.height * scale) / 2;
  return {x: x * scale + offsetX, y: y * scale + offsetY, scale, offsetX, offsetY};
}

export function viewportPointToAnnotation(document: StylusAnnotationDocument, x: number, y: number, viewportWidth: number, viewportHeight: number) {
  const mapped = annotationPointToViewport(document, 0, 0, viewportWidth, viewportHeight);
  return {x: (x - mapped.offsetX) / mapped.scale, y: (y - mapped.offsetY) / mapped.scale};
}

export class StylusAutosaveController {
  private timer: ReturnType<typeof setTimeout> | undefined;
  private revision = 0;
  constructor(private storage: StylusDocumentStorage, private key: string, private delayMs = 500) {}
  schedule(document: StylusAnnotationDocument): void {
    const revision = ++this.revision;
    if (this.timer) clearTimeout(this.timer);
    this.timer = setTimeout(async () => {
      if (revision === this.revision) await this.storage.save(this.key, serializeAnnotationDocument(document));
    }, this.delayMs);
  }
  async load(): Promise<StylusAnnotationDocument | null> {
    const value = await this.storage.load(this.key);
    return value ? parseAnnotationDocument(value) : null;
  }
  async flush(document: StylusAnnotationDocument): Promise<void> {
    if (this.timer) clearTimeout(this.timer);
    this.timer = undefined;
    this.revision++;
    await this.storage.save(this.key, serializeAnnotationDocument(document));
  }
  cancel(): void { if (this.timer) clearTimeout(this.timer); this.timer = undefined; this.revision++; }
}
