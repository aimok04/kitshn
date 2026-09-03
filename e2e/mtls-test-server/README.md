# mTLS test server

This is a small tandoor test setup with a mtls required server.
This uses `nginx` `ssl_verify_client_on` setting with small script providing certificates
Postgres uses tempfs and will reset once stopped.

## Layout

| File | Purpose |
| --- | --- |
| `docker-compose.yml` | Postgres + Tandoor (`vabene1111/recipes`) + nginx listening on `:8443` |
| `nginx.conf` | TLS + mTLS termination, proxies plain HTTP to Tandoor |
| `setup-certs.sh` | Generates `certs/{ca,server,client}.{crt,key}` and `certs/client.p12` |
| `seed-user.sh` | Django script that creates a dev user + space + groups |
| `certs/` | Generated certs |

## Setup

```sh
./setup-certs.sh
docker compose up -d
./seed-user.sh
```

Server is now at <https://localhost:8443>.

## Connecting from kitshn

| Target | URL | Notes | Usage |
| --- | --- | --- | --- |
| Desktop (JVM) | `https://localhost:8443` | The CA from `client.p12` is enough; no host trust install needed. | Simply set server address and a dialog prompt should warn to select the `.p12` cert | 
| Android / adb | `https://localhost:8443` + `adb reverse tcp:8443 tcp:8443` | The server cert SAN is `localhost`/`127.0.0.1` only, so reach it via adb reverse instead of LAN IP (or change the cert). | Please install the certificate as a app certificate. E.g. on Pixel Privacy & Security -> More Privacy And Security -> Encryption & Credentials (Verschlüsselung & Anmeldedaten) -> Install Certificate -> VPN & App-Certificate install the `.p12` |
| IOS | `https://localhost:8443` | TODO. | Should prompt to select a the `.p12` cert 

When prompted for a client certificate inside kitshn, pick
`certs/client.p12` (password `kitshn`). Sign in with `dev` / `dev`.

## Reset

```sh
docker compose down -v
rm -rf certs
```

