"""Refresh public reference data and create local images + an empty-DB MySQL seed.

Requires Python 3 and Pillow. No production credentials or write requests are used.
Run from any directory: python scripts/prepare-local-data.py [--refresh]
"""
import argparse
import hashlib
import json
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone
from pathlib import Path
from urllib.request import Request, urlopen

from PIL import Image, ImageFile, ImageOps

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "frontend/dev-data"
ASSETS = DATA / "assets"
ORIGIN = "https://twiiiins.com"
ENDPOINTS = ["concerts", "projects", "media/music", "media/videos", "media/news",
             "media/photos/groups", "media/equipment", "media/contacts", "media/download-files"]


def read_public(path):
    request = Request(ORIGIN + path, headers={"User-Agent": "TWIIIINS-local-fixtures/1.0"})
    return urlopen(request, timeout=45)


def dimensions(path):
    # Only read enough bytes to decode the size; do not save production photos.
    parser = ImageFile.Parser()
    with read_public(path) as response:
        while not parser.image:
            chunk = response.read(16384)
            if not chunk:
                raise ValueError(f"Cannot read image dimensions: {path}")
            parser.feed(chunk)
        return parser.image.size


def image_paths(value):
    if isinstance(value, dict):
        for child in value.values():
            yield from image_paths(child)
    elif isinstance(value, list):
        for child in value:
            yield from image_paths(child)
    elif isinstance(value, str) and value.startswith("/uploads/image/"):
        yield value


def substitute(value, mapping):
    if isinstance(value, dict):
        return {key: substitute(child, mapping) for key, child in value.items()}
    if isinstance(value, list):
        return [substitute(child, mapping) for child in value]
    return mapping.get(value, value) if isinstance(value, str) else value


def sql_value(value):
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, int):
        return str(value)
    # MySQL hex strings preserve quotes, newlines, Unicode and backslashes in any SQL mode.
    return "CONVERT(X'" + str(value).encode("utf-8").hex() + "' USING utf8mb4)"


def write_sql(fixtures):
    lines = ["-- Local development ONLY. Apply to an EMPTY schema created by Spring Boot.",
             "-- Plain INSERT intentionally fails on existing IDs; do not use on production.",
             "-- Copy frontend/dev-data/assets to your FILE_UPLOAD_DIR/dummy first.",
             "SET NAMES utf8mb4;", "START TRANSACTION;"]

    def insert(table, row, columns):
        names = ", ".join(f"`{column}`" for column in columns.values())
        values = ", ".join(sql_value(row.get(key)) for key in columns)
        lines.append(f"INSERT INTO `{table}` ({names}) VALUES ({values});")

    tables = {
        "concerts": ("concerts", "id date location name startTime ticketInfo fullLocation googleMapUrl collaborationInfo isPast"),
        "projects": ("projects", "id title subtitle premiereDate location coverImageUrl moreInfoUrl director thankYouText urlSlug displayOrder"),
        "media/music": ("music", "id title artist coverUrl linkUrl displayOrder"),
        "media/videos": ("videos", "id title embedUrl displayOrder"),
        "media/news": ("news", "id date title description displayOrder archived source status version"),
        "media/photos/groups": ("photo_groups", "id title displayOrder"),
        "media/equipment": ("equipment", "id name imageUrl displayOrder"),
        "media/contacts": ("contacts", "id name role email displayOrder"),
        "media/download-files": ("download_files", "id name fileUrl displayOrder"),
    }
    import re
    for endpoint, (table, fields) in tables.items():
        columns = {key: re.sub(r"(?<!^)(?=[A-Z])", "_", key).lower() for key in fields.split()}
        for row in fixtures[endpoint]:
            if table == "news":
                row = {**row, "archived": False, "source": "NEWS", "status": "PUBLISHED", "version": 0}
            insert(table, row, columns)
    for project in fixtures["projects"]:
        for order, description in enumerate(project["descriptions"]):
            insert("project_descriptions", {"project_id": project["id"], "description_order": order, "description": description},
                   {key: key for key in ["project_id", "description_order", "description"]})
        for order, image in enumerate(project["imageUrls"]):
            insert("project_images", {"project_id": project["id"], "image_order": order, "image_url": image},
                   {key: key for key in ["project_id", "image_order", "image_url"]})
        for order, review in enumerate(project["reviews"]):
            insert("project_reviews", {"project_id": project["id"], "review_order": order, **review},
                   {key: key for key in ["project_id", "review_order", "text", "source"]})
    for news in fixtures["media/news"]:
        for order, image in enumerate(news["imageUrls"]):
            insert("news_images", {"news_id": news["id"], "image_url": image, "image_order": order},
                   {"news_id": "news_id", "image_url": "image_url", "image_order": "image_order"})
    for group in fixtures["media/photos/groups"]:
        for photo in group["photos"]:
            insert("photos", photo, {"id": "id", "imageUrl": "image_url", "thumbnailUrl": "thumbnail_url",
                                    "altText": "alt_text", "photoGroupId": "photo_group_id"})
    lines.append("COMMIT;")
    (ROOT / "scripts/local-seed.sql").write_text("\n".join(lines) + "\n", encoding="utf-8")


def main():
    arguments = argparse.ArgumentParser()
    arguments.add_argument("--refresh", action="store_true", help="Refresh the public production snapshot and image dimensions")
    args = arguments.parse_args()
    DATA.mkdir(parents=True, exist_ok=True)
    ASSETS.mkdir(parents=True, exist_ok=True)
    snapshot_path = DATA / "reference.json"
    if args.refresh or not snapshot_path.exists():
        reference = {}
        for endpoint in ENDPOINTS:
            with read_public("/api/" + endpoint) as response:
                payload = json.load(response)
            if not payload.get("success") or not isinstance(payload.get("data"), list):
                raise ValueError(f"Unexpected response for {endpoint}")
            reference[endpoint] = payload["data"]
        paths = sorted(set(image_paths(reference)))
        with ThreadPoolExecutor(max_workers=6) as pool:
            sizes = dict(zip(paths, pool.map(dimensions, paths)))
        snapshot = {"origin": ORIGIN, "capturedAt": datetime.now(timezone.utc).isoformat(),
                    "data": reference, "imageSizes": sizes}
        snapshot_path.write_text(json.dumps(snapshot, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    else:
        snapshot = json.loads(snapshot_path.read_text(encoding="utf-8"))
    mapping = {}
    manifest = []
    sources = sorted((ROOT / "frontend/src/imgs/About").glob("*.png"))
    album = ROOT / "frontend/src/imgs/music/time.png"
    for path, size in snapshot["imageSizes"].items():
        name = hashlib.sha256(path.encode()).hexdigest()[:16] + ".jpg"
        canonical = path.replace("/image/thumbnails/", "/image/")
        source_index = int(hashlib.sha256(canonical.encode()).hexdigest()[:8], 16)
        source = album if size[0] == size[1] else sources[source_index % len(sources)]
        with Image.open(source) as original:
            resized = ImageOps.fit(ImageOps.exif_transpose(original).convert("RGB"), tuple(size), method=Image.Resampling.LANCZOS)
            resized.save(ASSETS / name, quality=78, optimize=True)
        mapping[path] = "/uploads/dummy/" + name
        manifest.append({"originalUrl": path, "localUrl": mapping[path], "width": size[0], "height": size[1],
                         "source": source.relative_to(ROOT).as_posix()})
    for item in snapshot["data"]["media/download-files"]:
        path = item["fileUrl"]
        name = Path(path).name
        if not (ASSETS / name).exists():
            with read_public(path) as response:
                (ASSETS / name).write_bytes(response.read())
        mapping[path] = "/uploads/dummy/" + name
    fixtures = substitute(snapshot["data"], mapping)
    (DATA / "fixtures.json").write_text(json.dumps(fixtures, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (DATA / "image-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    write_sql(fixtures)
    print(json.dumps({"counts": {key: len(value) for key, value in fixtures.items()},
                      "photos": sum(len(group["photos"]) for group in fixtures["media/photos/groups"]),
                      "images": len(manifest)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
