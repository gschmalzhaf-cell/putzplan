#!/usr/bin/env bash
# Changelog-Regel: Jeder Release-Tag vX.Y.Z bekommt ein automatisch
# generiertes, nutzerlesbares Changelog in die GitHub-Release-Notes.
#
# Aufruf: scripts/changelog.sh <tag>   (z.B. scripts/changelog.sh v1.5.1)
# Commits werden seit dem davorliegenden Tag gruppiert (feat/fix/chore/sonst).
set -euo pipefail

TAG="${1:?usage: changelog.sh <tag>}"
REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_DIR"

if ! git rev-parse -q --verify "refs/tags/${TAG}" >/dev/null; then
  echo "Tag ${TAG} existiert nicht" >&2
  exit 1
fi

# Vorgaenger-Tag im Commit-Verlauf (falls vorhanden)
PREV="$(git describe --tags --abbrev=0 "${TAG}^" 2>/dev/null || true)"
RANGE="${TAG}"
[ -n "$PREV" ] && RANGE="${PREV}..${TAG}"

VERSION="${TAG#v}"
echo "## Putzplan ${VERSION}"
echo
echo "Veröffentlicht: $(date -u '+%d.%m.%Y')"
echo
echo "### Downloads"
echo "- \`putzplan-${VERSION}.apk\` (signierte Release-APK)"
echo

group() {
  local titel="$1" emoji="$2" prefix="$3"
  local zeilen
  zeilen="$(git log --no-merges --format='%s' "$RANGE" | grep -iE "^(${prefix})" | grep -ivE 'version(Name|Code)' | sed -E 's/^[a-z]+(\([^)]*\))?:[[:space:]]*//I' || true)"
  if [ -n "$zeilen" ]; then
    echo "### ${emoji} ${titel}"
    echo "$zeilen" | sed 's/^/- /'
    echo
  fi
}

group "Neue Funktionen" "✨" "feat"
group "Fehlerbehebungen" "🐛" "fix"
group "Wartung & Build" "🔧" "chore|build|deps|ci"
group "Dokumentation" "📝" "docs"

SONST="$(git log --no-merges --format='%s' "$RANGE" | grep -ivE '^(feat|fix|chore|build|deps|ci|docs)(\([^)]*\))?:' | grep -ivE 'version(Name|Code)' || true)"
if [ -n "$SONST" ]; then
  echo "### 📦 Sonstiges"
  echo "$SONST" | sed 's/^/- /'
  echo
fi

if [ -z "$PREV" ]; then
  echo "### 📦 Alle Commits (erster getaggter Stand)"
  git log --no-merges --format='%s' "$TAG" | sed 's/^/- /'
fi
