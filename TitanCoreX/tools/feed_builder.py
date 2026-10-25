import json, os, pathlib, base64, urllib.request, urllib.parse
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey

request = urllib.request.Request(
 "https://mb-api.abuse.ch/api/v1/",
 data=urllib.parse.urlencode({"query":"get_recent","selector":"100"}).encode(),
 headers={"Auth-Key":os.environ["MALWAREBAZAAR_AUTH_KEY"]}, method="POST")
with urllib.request.urlopen(request, timeout=30) as response:
 result=json.load(response)
if result.get("query_status")!="ok": raise RuntimeError("Threat provider unavailable")
items=[]
seen=set()
folder=pathlib.Path("TitanCoreX/threat-feed")
folder.mkdir(parents=True,exist_ok=True)
existing=folder/"feed.json"
if existing.exists():
 old=json.loads(existing.read_text(encoding="utf-8"))
 for item in old.get("sha256",[]):
  h=str(item.get("hash","")).lower()
  if len(h)==64 and all(ch in "0123456789abcdef" for ch in h) and h not in seen:
   items.append({"hash":h,"description":str(item.get("description","Known malware indicator"))[:140]})
   seen.add(h)
for row in result.get("data",[]):
 h=str(row.get("sha256_hash","")).lower()
 if len(h)==64 and all(ch in "0123456789abcdef" for ch in h) and h not in seen:
  seen.add(h)
  items.append({"hash":h,"description":"MalwareBazaar confirmed sample"})
if not items: raise RuntimeError("No valid threat indicators")
items=items[-200000:]
payload=json.dumps({"schema":1,"sha256":items},separators=(",",":")).encode()
key=Ed25519PrivateKey.from_private_bytes(base64.b64decode(os.environ["TITAN_FEED_SIGNING_KEY_B64"]))
(folder/"feed.json").write_bytes(payload)
(folder/"feed.json.sig").write_bytes(key.sign(payload))
print("Published",len(items),"SHA-256 indicators")
