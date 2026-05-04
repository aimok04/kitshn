#!/usr/bin/env bash
# Create a Tandoor user "dev" / "dev" with an active space, idempotent.
# Run after `docker compose up -d` and once Tandoor is reachable.

set -euo pipefail
cd "$(dirname "$0")"

docker compose exec -T web_recipes /opt/recipes/venv/bin/python /opt/recipes/manage.py shell <<'PY'
from django.contrib.auth import get_user_model
from django.contrib.auth.models import Group
from cookbook.models import Space, UserSpace

U = get_user_model()
u, _ = U.objects.get_or_create(username="dev", defaults={"email": "dev@example.com"})
u.set_password("dev")
u.is_superuser = True
u.is_staff = True
u.save()

s, _ = Space.objects.get_or_create(name="dev", defaults={"created_by": u})
us, _ = UserSpace.objects.get_or_create(user=u, space=s, defaults={"active": True})
us.active = True
us.save()
for name in ("admin", "user", "guest"):
    try:
        us.groups.add(Group.objects.get(name=name))
    except Group.DoesNotExist:
        pass

print("user 'dev' / 'dev' ready in space 'dev' with admin/user/guest roles")
PY
