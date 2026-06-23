# playwright-clj

Idiomatic **Clojure wrapper over [Playwright for Java](https://playwright.dev/java/)** —
drive real browsers (Chromium/Firefox/WebKit) for browser-debugging and E2E of
WASM / ClojureScript apps. Built to test the kotoba office layer in a genuine
browser (real Service Worker, IndexedDB, WebCrypto, WebAuthn passkeys).

## Why

Node-only harnesses can't exercise the browser runtime (secure-context `crypto.subtle`,
Service Workers, `navigator.credentials`). playwright-clj runs a real browser from
Clojure, evaluates JS, captures console/network, screenshots, and speaks CDP — so
WASM + CLJS code can be asserted where it actually runs.

## Install browsers (once)

```bash
clojure -M:install chromium      # or firefox / webkit
```

## Use

```clojure
(require '[playwright-clj.core :as pw])

(pw/with-page [page {:headless true}]      ; launches + closes a browser+page
  (pw/goto page "https://example.com")
  (println (pw/title page))                ; "Example Domain"
  (println (pw/eval-js page "() => 1 + 1"))      ; 2  (marshalled to Clojure)
  (println (pw/eval-js page "x => x.length" "abc")) ; 3
  (pw/click page "#submit")
  (pw/wait-for-fn page "() => window.__ready === true")
  (pw/screenshot page "shot.png"))
```

### API (`playwright-clj.core`)

| fn | purpose |
|---|---|
| `launch` / `close` / `with-browser` / `with-page` | lifecycle |
| `new-context` / `new-page` | contexts & pages |
| `goto` `click` `fill` `type-in` `text` `inner-text` `content` `title` `url` | navigate + interact |
| `wait-for` (selector state) / `wait-for-fn` (JS predicate) | synchronisation |
| `eval-js` | evaluate JS, result → Clojure data (Map→map, List→vec) |
| `capture-console` | collect console messages into an atom |
| `screenshot` | full-page PNG |
| `cdp-session` / `cdp-send` | raw Chrome DevTools Protocol |
| `add-virtual-authenticator` | install a WebAuthn passkey **virtual authenticator** (CDP) |

### Passkeys (WebAuthn) headlessly

```clojure
(pw/with-page [page {:headless true}]
  (pw/goto page "http://localhost:8123/")          ; secure context (localhost)
  (pw/add-virtual-authenticator page)              ; ctap2 internal passkey
  (pw/eval-js page
    "async () => (await navigator.credentials.create({publicKey:{ … }})).id"))
```

## Tests

```bash
clojure -M:test     # browser smoke: eval, DOM, console, WebAssembly, crypto
```

The kotoba office real-browser E2E (sovereign identity, client-side encryption,
CACAO mint, depth-2 delegation, passkey) lives at
`com-junkawasaki/kotoba/crates/kotoba-wasm/web/browser-e2e/` and depends on this
library via `:local/root`.

## Notes

- `crypto.subtle` and Service Workers require a **secure context** — use
  `http://localhost` (treated as secure) or https, not `data:`/`file:` URLs.
- `eval-js` returns JSON-able values; pass DOM/handles back via `window.*` if needed.
- Built against Playwright for Java 1.49; the bundled driver downloads browsers it manages.
