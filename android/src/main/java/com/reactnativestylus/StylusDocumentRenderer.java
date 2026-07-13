package com.reactnativestylus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;

final class StylusDocumentRenderer {
  private StylusDocumentRenderer() {}

  static String export(Context context, String json, String outputName, int maxDimension, boolean annotation) throws Exception {
    JSONObject document = new JSONObject(json);
    int sourceWidth = Math.max(1, document.optInt("width", 1080));
    int sourceHeight = Math.max(1, document.optInt("height", 1920));
    float scale = Math.min(1f, Math.max(256, maxDimension) / (float) Math.max(sourceWidth, sourceHeight));
    int width = Math.max(1, Math.round(sourceWidth * scale)), height = Math.max(1, Math.round(sourceHeight * scale));
    Bitmap bitmap = annotation ? annotationBackground(context, document.optJSONObject("target"), width, height) : null;
    if (annotation && bitmap == null) throw new IllegalArgumentException("Unable to open annotation source URI");
    if (bitmap == null) { bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); new Canvas(bitmap).drawColor(parseColor(document.optString("backgroundColor", "#FFFFFF"), Color.WHITE)); }
    Canvas canvas = new Canvas(bitmap); canvas.scale(scale, scale); drawStrokes(canvas, document.optJSONArray("strokes")); drawShapes(canvas, document.optJSONArray("shapes"));
    File directory = new File(context.getCacheDir(), "stylus-exports"); if (!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("Cannot create export directory");
    String safeName = outputName == null ? "stylus-export.png" : outputName.replaceAll("[^a-zA-Z0-9._-]", "_"); if (!safeName.endsWith(".png")) safeName += ".png";
    File output = new File(directory, safeName); try (FileOutputStream stream = new FileOutputStream(output)) { bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); }
    bitmap.recycle(); return output.getAbsolutePath();
  }

  private static Bitmap annotationBackground(Context context, JSONObject target, int width, int height) {
    if (target == null) return null;
    try {
      Uri uri = Uri.parse(target.getString("uri"));
      if ("pdf".equals(target.optString("type"))) {
        try (ParcelFileDescriptor descriptor = context.getContentResolver().openFileDescriptor(uri, "r"); PdfRenderer renderer = new PdfRenderer(descriptor)) {
          int pageIndex = Math.max(0, Math.min(renderer.getPageCount() - 1, target.optInt("page", 0)));
          try (PdfRenderer.Page page = renderer.openPage(pageIndex)) {
            Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); result.eraseColor(Color.WHITE);
            page.render(result, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY); return result;
          }
        }
      }
      try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
        Bitmap source = BitmapFactory.decodeStream(stream); if (source == null) return null;
        Bitmap result = Bitmap.createScaledBitmap(source, width, height, true); if (result != source) source.recycle(); return result;
      }
    } catch (Exception ignored) { return null; }
  }

  private static void drawStrokes(Canvas canvas, JSONArray strokes) throws Exception {
    if (strokes == null) return; Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG); paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND);
    for (int s = 0; s < strokes.length(); s++) {
      JSONObject stroke = strokes.getJSONObject(s); if ("eraser".equals(stroke.optString("tool"))) continue;
      paint.setColor(parseColor(stroke.optString("color", "#000000"), Color.BLACK));
      float opacity = (float) stroke.optDouble("opacity", 1); if ("highlighter".equals(stroke.optString("tool"))) opacity *= 0.45f;
      paint.setAlpha(Math.round(255 * opacity)); float baseWidth = (float) stroke.optDouble("width", 6); JSONArray points = stroke.optJSONArray("points");
      if (points == null) continue; for (int p = 1; p < points.length(); p++) { JSONObject a = points.getJSONObject(p - 1), b = points.getJSONObject(p);
        float pressure = Math.max(0.08f, ((float) a.optDouble("pressure", 1) + (float) b.optDouble("pressure", 1)) / 2f); paint.setStrokeWidth(baseWidth * pressure);
        canvas.drawLine((float) a.getDouble("x"), (float) a.getDouble("y"), (float) b.getDouble("x"), (float) b.getDouble("y"), paint); }
    }
  }

  private static void drawShapes(Canvas canvas, JSONArray shapes) throws Exception {
    if (shapes == null) return; Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); paint.setStyle(Paint.Style.STROKE);
    for (int i = 0; i < shapes.length(); i++) { JSONObject shape = shapes.getJSONObject(i); paint.setColor(parseColor(shape.optString("color"), Color.BLACK)); paint.setAlpha((int) Math.round(255 * shape.optDouble("opacity", 1))); paint.setStrokeWidth((float) shape.optDouble("width", 2));
      float x = (float) shape.optDouble("x"), y = (float) shape.optDouble("y"), endX = (float) shape.optDouble("endX"), endY = (float) shape.optDouble("endY"); String type = shape.optString("type");
      if ("ellipse".equals(type)) canvas.drawOval(Math.min(x,endX), Math.min(y,endY), Math.max(x,endX), Math.max(y,endY), paint);
      else if ("rectangle".equals(type)) canvas.drawRect(Math.min(x,endX), Math.min(y,endY), Math.max(x,endX), Math.max(y,endY), paint);
      else canvas.drawLine(x, y, endX, endY, paint);
    }
  }

  private static int parseColor(String value, int fallback) { try { return Color.parseColor(value); } catch (Exception ignored) { return fallback; } }
}
