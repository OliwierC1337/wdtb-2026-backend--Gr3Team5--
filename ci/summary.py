"""Podsumowanie reguł projektu i testów zespołu w formacie Markdown (zakładka Summary w GitHub Actions)."""
import glob
import html
import sys
import xml.etree.ElementTree as ET


def esc(text):
    return html.escape(text or "", quote=False).replace("|", "\\|")


suites = []
for path in sorted(glob.glob("target/surefire-reports/TEST-*.xml")):
    root = ET.parse(path).getroot()
    cases = root.findall("testcase")
    failed = [c for c in cases if c.find("failure") is not None or c.find("error") is not None]
    skipped = [c for c in cases if c.find("skipped") is not None]
    suites.append((root.get("name", path), len(cases), failed, skipped))

TITLE = sys.argv[1] if len(sys.argv) > 1 else "Reguły projektu i testy zespołu"
print(f"## {TITLE}\n")
if not suites:
    print("Brak raportów testów (kompilacja się nie powiodła?). Sprawdź log kroku „Testy i reguły”.")
    raise SystemExit(0)

print("| Zestaw | Wynik | Przeszło | Nie przeszło | Pominięte (kolejne laboratoria) |")
print("|---|---|---|---|---|")
for name, total, failed, skipped in suites:
    if total == len(skipped):
        status = "⏭"
    else:
        status = "❌" if failed else "✅"
    print(f"| {esc(name)} | {status} | {total - len(failed) - len(skipped)} | {len(failed)} | {len(skipped)} |")

problems = [(name, case) for name, _, failed, _ in suites for case in failed]
if problems:
    print("\n### Do poprawy\n")
    for name, case in problems:
        node = case.find("failure") if case.find("failure") is not None else case.find("error")
        message = (node.get("message") or "").strip().splitlines()
        first = message[0] if message else ""
        print(f"- **{esc(case.get('name'))}** ({esc(name)})  \n  {esc(first[:300])}")
