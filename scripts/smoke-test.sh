#!/usr/bin/env bash
# Kitchen Line API smoke test.
# Runs a full player journey against the app running on localhost:8080.
# Usage: ./scripts/smoke-test.sh

BASE="http://localhost:8080"
JAR=$(mktemp)                              # temporary cookie file, like a browser's
BODY=$(mktemp)                             # last response body
EMAIL="smoke$(date +%s)@example.com"       # new account every run
PASS="PickleBall2026"
FAILED=0

# The CSRF code the server put in the XSRF-TOKEN cookie
token() { awk '/XSRF-TOKEN/ {print $7}' "$JAR"; }

# call METHOD PATH [JSON]  -> prints the HTTP status code
call() {
  local args=(-s -o "$BODY" -w "%{http_code}" -b "$JAR" -c "$JAR" -X "$1" "$BASE$2"
              -H "X-XSRF-TOKEN: $(token)")
  if [ -n "${3:-}" ]; then
    args+=(-H "Content-Type: application/json" -d "$3")
  fi
  curl "${args[@]}"
}

# check "description" expected actual
check() {
  if [ "$2" = "$3" ]; then
    echo "PASS  $1 ($3)"
  else
    echo "FAIL  $1 (expected $2, got $3)  $(cat "$BODY")"
    FAILED=1
  fi
}

PROFILE='{"age":34,"gender":"FEMALE","skill":"INTERMEDIATE","drillsPerWeek":2,"playsPerWeek":3,"workoutsPerWeek":3}'
BAD_PROFILE='{"age":10,"gender":"FEMALE","skill":"INTERMEDIATE","drillsPerWeek":2,"playsPerWeek":3,"workoutsPerWeek":3}'
CREDENTIALS="{\"email\":\"$EMAIL\",\"password\":\"$PASS\"}"

echo "Testing as $EMAIL"
check "Health check"                    200 "$(call GET /api/health)"
check "Sign up"                         201 "$(call POST /api/auth/signup "$CREDENTIALS")"
check "Program before login is blocked" 401 "$(call GET /api/program/current)"
check "Log in"                          200 "$(call POST /api/auth/login "$CREDENTIALS")"
check "No profile yet"                  404 "$(call GET /api/profile)"
check "No program before a profile"     409 "$(call GET /api/program/current)"
check "Invalid profile is rejected"     400 "$(call PUT /api/profile "$BAD_PROFILE")"
check "Save profile"                    200 "$(call PUT /api/profile "$PROFILE")"
check "Load profile"                    200 "$(call GET /api/profile)"
check "Get current program"             200 "$(call GET /api/program/current)"
check "Everyone receives all 4 weeks"   4   "$(grep -o '"instructions"' "$BODY" | wc -l | tr -d ' ')"
check "Complete week 1, day A"          200 "$(call POST /api/program/workouts/1/A)"
check "Progress shows 1 done"           1   "$(grep -o '"completedCount":1' "$BODY" | wc -l | tr -d ' ')"
check "Week 2 is open to everyone"      200 "$(call POST /api/program/workouts/2/A)"
check "Day D doesn't exist"             400 "$(call POST /api/program/workouts/1/D)"
check "Un-check week 1, day A"          200 "$(call DELETE /api/program/workouts/1/A)"
check "Next month needs 75% first"      409 "$(call POST /api/program/next-month)"

# A change WITHOUT the CSRF header must be refused
NO_CSRF=$(curl -s -o "$BODY" -w "%{http_code}" -b "$JAR" -X POST "$BASE/api/program/workouts/1/B")
check "Change without CSRF token"       403 "$NO_CSRF"

check "Log out"                         204 "$(call POST /api/auth/logout)"
check "Program after logout is blocked" 401 "$(call GET /api/program/current)"

rm -f "$JAR" "$BODY"
echo
if [ "$FAILED" = 0 ]; then echo "All checks passed."; else echo "Some checks failed."; fi
exit $FAILED