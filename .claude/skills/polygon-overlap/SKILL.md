---
name: polygon-overlap
description: Perform polygon overlap analysis between two feature sets using quadtree encoding, for use cases like administrative division comparison, land-use change detection, and spatial grid data fusion.
---

# Polygon Overlap Analysis Skill

## When to Use

Invoke this skill when the user's intent involves any of the following:

- **Keyword triggers**: 多边形重叠, 多边形求交, 空间交集, 要素对比, 变化检测, 网格数据融合, polygon overlap, spatial intersection, feature comparison, change detection
- **Use cases**:
  - Comparing administrative divisions across time periods
  - Analyzing land-use changes between two datasets
  - Correlating grid data from different sources
  - Querying spatial overlap relationships between feature sets

## What It Does

This skill performs overlap analysis between two polygon feature sets (`polygonSetA` and `polygonSetB`), with the following workflow:

1. **Auto-detect quadtree encoding**: Checks if input codes use quadtree format (digits 0-3 only)
2. **Smart conversion**: Converts non-quadtree geometries to quadtree codes using bounding box coordinates
3. **Overlap analysis**: Finds features from both sets that share the same spatial location
4. **Result aggregation**: Groups results by quadtree grid prefix, returning overlap details

## Source Code Location

All implementation classes are under:
```
modules/gis-platform/gis-vector-title-min-demo/src/main/java/com/dingky/gis/demo/title/skill/
```

| Class | Role |
|---|---|
| `PolygonOverlapSkill.java` | Main skill orchestrator — validates input, converts encoding, runs analysis, builds response |
| `OverlapAnalysisRequest.java` | Request DTO — holds `polygonSetA`, `polygonSetB`, `includeDetails` |
| `OverlapAnalysisResponse.java` | Response DTO — holds `totalOverlapGrids`, `totalOverlapUnits`, `details` |
| `OverlapDetail.java` | Per-grid result — holds `gridKey`, `codes`, `fids`, `overlapPairs` |
| `OverlapPair.java` | A single overlap pair — `fidA`, `fidB`, `codeA`, `codeB`, `representativeCode`, `representativeFid` |
| `PolygonInput.java` | Input model — `fid`, `code`, `bounds` (BoundingBox) |
| `BoundingBox.java` | Bounding box — `minX`, `minY`, `maxX`, `maxY`, with `getCenterX()`/`getCenterY()` |
| `PolygonOverlapFunctionAdapter.java` | JSON adapter for AI function-calling integration |

Core dependencies (outside `skill/`):
```
modules/gis-platform/gis-vector-title-min-demo/src/main/java/com/dingky/gis/demo/title/
├── BasicUnit.java          — Core data model (fid, code, fromA flag)
└── OverlapAnalyzer.java    — Overlap detection algorithm (findOverlappingUnits)
```

## API Contract

### Input: OverlapAnalysisRequest

```json
{
  "polygonSetA": [
    {
      "fid": 1,
      "code": "0123",
      "bounds": { "minX": 100.0, "minY": 30.0, "maxX": 101.0, "maxY": 31.0 }
    }
  ],
  "polygonSetB": [
    {
      "fid": 101,
      "code": "GRID_B001",
      "bounds": { "minX": 100.5, "minY": 30.5, "maxX": 101.5, "maxY": 31.5 }
    }
  ],
  "includeDetails": true
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `polygonSetA` | List\<PolygonInput\> | Yes | First polygon set (e.g., administrative divisions) |
| `polygonSetB` | List\<PolygonInput\> | Yes | Second polygon set (e.g., grid partitions) |
| `includeDetails` | boolean | No | Include per-grid details (default: `true`) |

Each `PolygonInput`:

| Field | Type | Required | Description |
|---|---|---|---|
| `fid` | long | Yes | Unique feature identifier |
| `code` | String | Yes | Encoding (quadtree or arbitrary format) |
| `bounds` | BoundingBox | Yes | Bounding box for quadtree conversion |

Each `BoundingBox`:

| Field | Type | Required | Description |
|---|---|---|---|
| `minX` | double | Yes | Minimum X coordinate |
| `minY` | double | Yes | Minimum Y coordinate |
| `maxX` | double | Yes | Maximum X coordinate |
| `maxY` | double | Yes | Maximum Y coordinate |

### Output: OverlapAnalysisResponse

```json
{
  "totalOverlapGrids": 3,
  "totalOverlapUnits": 12,
  "details": [
    {
      "gridKey": "0123",
      "codes": ["012301", "012302"],
      "fids": [1, 101],
      "overlapPairs": [
        {
          "fidA": 1, "fidB": 101,
          "codeA": "012301", "codeB": "012302",
          "representativeCode": "012302",
          "representativeFid": 101
        }
      ]
    }
  ]
}
```

| Field | Type | Description |
|---|---|---|
| `totalOverlapGrids` | int | Number of grids with overlapping features |
| `totalOverlapUnits` | int | Total number of overlapping units |
| `details` | List\<OverlapDetail\> | Per-grid breakdown |

## Quadtree Encoding Rules

- **Valid characters**: digits 0-3 only (e.g., `"0123"`, `"01230123"`)
- **Separators**: `-` and `/` are stripped before validation
- **Invalid codes** (trigger auto-conversion): letters (`"A001"`), digits 4-9 (`"110000"`), Chinese characters
- **Conversion algorithm**: Recursive 8-level subdivision centered on bounding box center, global range [-180,180] × [-90,90], precision ≈ 0.7°
- **Grouping**: Features are grouped by the first 4 characters of their quadtree code as the grid key

## Usage Example (Java)

```java
PolygonOverlapSkill skill = new PolygonOverlapSkill();

OverlapAnalysisRequest request = new OverlapAnalysisRequest();
request.setPolygonSetA(Arrays.asList(
    new PolygonInput(1, "0123", new BoundingBox(100.0, 30.0, 101.0, 31.0))
));
request.setPolygonSetB(Arrays.asList(
    new PolygonInput(101, "0123", new BoundingBox(100.0, 30.0, 101.0, 31.0))
));

OverlapAnalysisResponse response = skill.execute(request);
System.out.println("Overlap grids: " + response.getTotalOverlapGrids());
```

## Edge Cases & Constraints

| Scenario | Behavior |
|---|---|
| Empty input set | Throws `IllegalArgumentException` |
| Mixed encoding formats | Auto-detects and converts non-quadtree codes |
| Missing bounding box | Falls back to FID-based temporary encoding |
| Very large datasets (>100k features) | Use `LocalParallelProcessor` or `SparkDistributedProcessor` instead |

## Performance

| Data Size | Processing Time | Memory |
|---|---|---|
| < 1,000 features | < 100ms | ~10 MB |
| 1,000–10,000 | 100ms–1s | ~50 MB |
| 10,000–100,000 | 1s–10s | ~200 MB |

## How to Help Users

When this skill is triggered, help the user by:

1. **Writing integration code** — Generate Java code that constructs `OverlapAnalysisRequest` from their data and calls `PolygonOverlapSkill.execute()`
2. **Interpreting results** — Explain the `OverlapAnalysisResponse` structure, including grid keys, overlap pairs, and representative codes
3. **Debugging encoding issues** — Diagnose whether input codes are valid quadtree format and suggest conversion strategies
4. **Scaling up** — For large datasets, recommend `LocalParallelProcessor` (multi-threaded) or `SparkDistributedProcessor` (cluster)
5. **Explaining quadtree** — Educate users on how quadtree encoding represents spatial location and why it enables efficient overlap detection
