import urllib.request
import re

icons = [
    "bolt", "star", "home", "work", "sports_esports", "music_note", "camera_alt",
    "phone", "message", "shopping_cart", "fitness_center", "restaurant", "local_hospital",
    "school", "directions_car", "flight", "hotel", "book", "code", "palette", "sports_soccer",
    "luggage", "account_balance", "newspaper", "play_arrow", "map", "email", "calendar_today",
    "access_time", "settings", "lock", "wifi", "bluetooth", "cloud", "download", "upload",
    "share", "search", "folder", "link"
]

results = {}
for icon in icons:
    try:
        url = f"https://fonts.gstatic.com/s/i/materialicons/{icon}/v1/24px.svg"
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response:
            svg = response.read().decode('utf-8')
            paths = re.findall(r'd="([^"]+)"', svg)
            # filter out the bounding box "M0 0h24v24H0z" and similar
            valid_paths = [p for p in paths if not re.match(r'^M0? 0?h24v24H0?z?$', p, re.IGNORECASE)]
            if valid_paths:
                results[icon] = "".join(valid_paths)
            else:
                print(f"No valid path found for {icon}, found: {paths}")
    except Exception as e:
        print(f"Error fetching {icon}: {e}")

print("val ICON_PATHS = mapOf(")
for icon, path in results.items():
    print(f'    "{icon}" to "{path}",')
print(")")
