#!/bin/bash

# A shell command to create an SSL certificate for use with secure cookies. Not trustworthy so only for use in dev, demos, and testing

# Create an encrypted key, then create the certificate authority from the key
openssl genpkey -algorithm RSA -out ca.key && openssl req -new -x509 -key ca.key -out ca.crt -days 3650 -subj "/C=US/ST=FL/L=Miami/O=Uwate&Co/OU=SDE/CN=localhost"

# Create an encrypted key, then create the certificate signing request (CSR) from the key
openssl genpkey -algorithm RSA -out springboot.key && openssl req -new -key springboot.key -out springboot.csr -subj "/C=US/ST=FL/L=Miami/O=Uwate&Co/OU=SDE/CN=localhost"

# Create certificate from CSR
openssl x509 -req -in springboot.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out springboot.crt -days 365

# Package into a PKCS12 for use in SpringBoot
openssl pkcs12 -export -in springboot.crt -inkey springboot.key -out springboot.p12 -name springboot -CAfile ca.crt -caname rootCA -passout pass:admin

# Move it into src/test/resources/
cp ./springboot.crt ./src/test/resources