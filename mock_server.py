"""
Zero 앱 테스트용 Mock API 서버
실행: python mock_server.py
포트: 9090

의존성:
  pip install anthropic
환경변수:
  ANTHROPIC_API_KEY=sk-ant-...
"""
from http.server import HTTPServer, BaseHTTPRequestHandler
import json, urllib.parse, threading, os

# ── Claude Vision (캡챠 해결용) ──────────────────────────
try:
    import anthropic
    _claude = anthropic.Anthropic(api_key=os.environ.get("ANTHROPIC_API_KEY", ""))
    print("[Claude] anthropic 패키지 로드 완료")
except ImportError:
    _claude = None
    print("[Claude] anthropic 패키지 없음 → pip install anthropic")

def solve_captcha_claude(image_base64: str, question: str) -> str:
    """영수증 이미지 + 질문 → Claude Vision으로 정답 추출"""
    if not _claude:
        return ""
    try:
        msg = _claude.messages.create(
            model="claude-opus-4-6",
            max_tokens=64,
            messages=[{
                "role": "user",
                "content": [
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": "image/png",
                            "data": image_base64,
                        }
                    },
                    {
                        "type": "text",
                        "text": (
                            "영수증 이미지를 분석하여 다음 질문에 정확히 답하세요.\n"
                            "답변은 요청한 값(숫자, 텍스트)만 간결하게 반환하세요. "
                            "예: '15000' 또는 '스타벅스강남점'\n\n"
                            f"질문: {question}"
                        )
                    }
                ]
            }]
        )
        answer = msg.content[0].text.strip()
        # 숫자만 있어야 하는 경우 쉼표/원화기호 제거
        cleaned = answer.replace(",", "").replace("원", "").replace("₩", "").strip()
        print(f"[Claude] 캡챠 답: '{answer}' → cleaned: '{cleaned}'")
        return cleaned
    except Exception as e:
        print(f"[Claude] 오류: {e}")
        return ""

# ── 테스트 시나리오 ─────────────────────────────────────
SCENARIO = {
    "id": "test_v1",
    "name": "랜딩 → 네이버 검색 → 상품 상세 진입 테스트",
    "version": 1,
    "variables": {},
    "steps": [
        # 1. 랜딩페이지 → m.search.naver.com 리다이렉트
        {"id": "s1", "action": "navigate",
         "url": "{{task.target_url}}", "timeout": 30000},
        # 2. 차단 체크
        {"id": "s2", "action": "checkStatus"},
        # 3. 대기
        {"id": "s3", "action": "delay", "ms": [1500, 2500]},
        # 4. 스크롤 (상품 로드)
        {"id": "s4", "action": "scroll", "distance": 800},
        # 5. target_index 번째 상품 클릭
        {"id": "s5", "action": "clickProduct",
         "index": "{{task.target_index}}", "mid": "{{task.nv_mid}}"},
        # 6. 상품 상세 체류
        {"id": "s6", "action": "dwell",
         "ms": [5000, 8000], "scrollDist": 2000},
        # 7. 완료 보고
        {"id": "s7", "action": "report", "status": "completed"}
    ]
}

# ── 테스트 작업 (1회만 반환) ─────────────────────────────
TEST_TASK = {
    "traffic_id": 9999,
    "slot_id":    1,
    "product_name":  "스포츠카 남성 자동차 키링",
    "nv_mid":        "505249347",
    "short_keyword": "스포츠카 남성",
    "target_url":    "https://www.adpangshopping.co.kr/r/9kzndy",
    "target_index":  2
}

task_claimed = False
task_lock = threading.Lock()


class Handler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        print(f"[{self.command}] {self.path}  →  {args[0] if args else ''}")

    def send_json(self, code, data):
        body = json.dumps(data, ensure_ascii=False).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def read_body(self):
        length = int(self.headers.get("Content-Length", 0))
        return self.rfile.read(length).decode("utf-8") if length else ""

    def do_POST(self):
        path = urllib.parse.urlparse(self.path).path
        raw_body = self.read_body()   # 파싱용으로 보존

        global task_claimed

        if path.endswith("/devices/register"):
            self.send_json(200, {"role": "soldier", "group_id": "test_group"})

        elif path.endswith("/devices/heartbeat"):
            self.send_json(200, {"ok": True})

        elif path.endswith("/traffic/claim-work"):
            with task_lock:
                if not task_claimed:
                    task_claimed = True
                    print("[★] claim-work → 테스트 작업 반환")
                    self.send_json(200, TEST_TASK)
                else:
                    print("[★] claim-work → 작업 없음")
                    self.send_response(204)
                    self.end_headers()

        elif path.endswith("/traffic/complete"):
            print("[★] 작업 완료 수신!")
            self.send_json(200, {"ok": True})

        elif path.endswith("/traffic/fail"):
            print("[!] 작업 실패 수신")
            self.send_json(200, {"ok": True})

        elif path.endswith("/traffic/log"):
            self.send_json(200, {"ok": True})

        elif path.endswith("/captcha/solve"):
            try:
                body = json.loads(raw_body) if raw_body else {}
                image_b64 = body.get("image_base64", "")
                question  = body.get("question", "")
                device_id = body.get("device_id", "?")
                print(f"[★] 캡챠 요청 device={device_id} | 질문: {question[:60]}")
                if image_b64 and question:
                    answer = solve_captcha_claude(image_b64, question)
                else:
                    answer = ""
                confidence = "high" if answer else "low"
                print(f"[★] 캡챠 답: '{answer}' (신뢰도: {confidence})")
                self.send_json(200, {"answer": answer, "confidence": confidence})
            except Exception as e:
                print(f"[!] 캡챠 처리 오류: {e}")
                self.send_json(200, {"answer": "", "confidence": "low"})

        else:
            self.send_json(200, {"ok": True})

    def do_GET(self):
        path = urllib.parse.urlparse(self.path).path

        if "/scenario/active" in path:
            self.send_json(200, {
                "scenarios": [{"id": "test_v1", "version": 1, "weight": 1}]
            })

        elif "/scenario/test_v1" in path:
            self.send_json(200, SCENARIO)

        elif "/script/version" in path:
            self.send_json(200, {"version": 0})

        else:
            self.send_json(200, {"ok": True})


if __name__ == "__main__":
    server = HTTPServer(("0.0.0.0", 9090), Handler)
    print("=" * 50)
    print("  Zero Mock Server 시작")
    print("  http://0.0.0.0:9090")
    print("  테스트 시나리오: test_v1")
    print(f"  target_url: {TEST_TASK['target_url']}")
    print(f"  nv_mid:     {TEST_TASK['nv_mid']}")
    print("=" * 50)
    server.serve_forever()
