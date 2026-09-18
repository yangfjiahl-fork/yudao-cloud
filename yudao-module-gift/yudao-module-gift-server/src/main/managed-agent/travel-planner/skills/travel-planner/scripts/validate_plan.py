#!/usr/bin/env python3
"""Managed Agent 沙箱内的零依赖旅行计划结构校验器。"""

import datetime as dt
import json
import sys


SLOTS = {"MORNING", "LUNCH", "AFTERNOON", "DINNER", "EVENING", "ACCOMMODATION"}
ACTIVITY_SLOTS = {"MORNING", "AFTERNOON", "EVENING"}


def fail(message: str) -> None:
    raise ValueError(message)


def main(path: str, expected_days: int) -> None:
    with open(path, "r", encoding="utf-8") as stream:
        plan = json.load(stream)
    for key in ("summary", "overview", "daily_itinerary", "transport", "citation_ids"):
        if key not in plan:
            fail(f"missing top-level field: {key}")
    days = plan["daily_itinerary"]
    if not isinstance(days, list) or len(days) != expected_days:
        fail("daily_itinerary length mismatch")
    seen_pois = set()
    for expected_day, day in enumerate(days, start=1):
        if day.get("day") != expected_day:
            fail(f"day index mismatch: expected {expected_day}")
        dt.date.fromisoformat(day["date"])
        slots = day.get("slots")
        if not isinstance(slots, list) or not slots:
            fail(f"day {expected_day} has no slots")
        names = [slot.get("slot") for slot in slots]
        if len(names) != len(set(names)) or any(name not in SLOTS for name in names):
            fail(f"day {expected_day} has duplicate or invalid slot")
        if "ACCOMMODATION" not in names or not ACTIVITY_SLOTS.intersection(names):
            fail(f"day {expected_day} needs activity and accommodation")
        previous_time = None
        for slot in slots:
            for key in ("poiId", "poiName", "longitude", "latitude", "detail"):
                if slot.get(key) in (None, ""):
                    fail(f"day {expected_day} {slot.get('slot')} missing {key}")
            lon, lat = float(slot["longitude"]), float(slot["latitude"])
            if not -180 <= lon <= 180 or not -90 <= lat <= 90:
                fail(f"day {expected_day} has invalid coordinate")
            current_time = slot.get("plannedStartTime")
            if current_time:
                parsed_time = dt.time.fromisoformat(current_time)
                if previous_time and parsed_time < previous_time:
                    fail(f"day {expected_day} slot time is out of order")
                previous_time = parsed_time
            poi_id = slot["poiId"]
            if poi_id in seen_pois and slot.get("slot") != "ACCOMMODATION":
                fail(f"duplicated poiId without accommodation exception: {poi_id}")
            seen_pois.add(poi_id)
    print("OK")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("usage: validate_plan.py PLAN_JSON EXPECTED_DAYS", file=sys.stderr)
        sys.exit(2)
    try:
        main(sys.argv[1], int(sys.argv[2]))
    except (KeyError, TypeError, ValueError, json.JSONDecodeError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
