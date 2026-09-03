#!/usr/bin/env bash
# Generate a self-signed CA, server cert (CN=localhost, SAN=localhost / 127.0.0.1),
# and a client cert + PKCS12 bundle for testing mTLS.
#
# Usage:  ./setup-certs.sh
# Output: ./certs/ca.{crt,key}
#         ./certs/server.{crt,key}
#         ./certs/client.{crt,key}
#         ./certs/client.p12   (password: kitshn)

set -euo pipefail
cd "$(dirname "$0")"

DIR=certs
mkdir -p "$DIR"
cd "$DIR"

P12_PASS=kitshn
DAYS=825

# --- CA ----------------------------------------------------------------------
if [[ ! -f ca.key ]]; then
    openssl req -x509 -newkey rsa:2048 -nodes \
        -keyout ca.key -out ca.crt -days "$DAYS" \
        -subj "/CN=kitshn mTLS test CA"
fi

# --- Server ------------------------------------------------------------------
cat > server.cnf <<'EOF'
[req]
distinguished_name = dn
req_extensions     = v3_req
prompt             = no
[dn]
CN = localhost
[v3_req]
subjectAltName = @alt
extendedKeyUsage = serverAuth
[alt]
DNS.1 = localhost
IP.1  = 127.0.0.1
EOF

openssl req -newkey rsa:2048 -nodes \
    -keyout server.key -out server.csr \
    -config server.cnf
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
    -out server.crt -days "$DAYS" -extensions v3_req -extfile server.cnf
rm -f server.csr server.cnf

# --- Client ------------------------------------------------------------------
openssl req -newkey rsa:2048 -nodes \
    -keyout client.key -out client.csr \
    -subj "/CN=kitshn-test-client"
openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
    -out client.crt -days "$DAYS"
rm -f client.csr

openssl pkcs12 -export \
    -in client.crt -inkey client.key -certfile ca.crt \
    -out client.p12 -name "kitshn-test-client" \
    -passout pass:"$P12_PASS"

chmod 644 *.crt *.p12
chmod 600 *.key

echo
echo "Done. Certs are in ./certs/"
echo "  Server URL : https://localhost:8443"
echo "  Client p12 : ./certs/client.p12  (password: $P12_PASS)"
echo "  CA cert    : ./certs/ca.crt      (trust this in your client)"
