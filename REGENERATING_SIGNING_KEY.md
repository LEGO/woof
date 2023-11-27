# Regenerating the GPG signing key

The key for signing releases expires every year. 
To sign a new release _after the key expires_, you need to:

1. Generate a new key
2. Publish the public signature to one or more key servers: (e.g. https://keyserver.ubuntu.com/)

## Generating a new key

We use as password protected, encrypted signing key when signing releases for maven central. To refresh the key, you need to:

1. Download the secret key file from your secret manager
2. Note the encryption password from your secret manager
3. Import the key into your local gpg keychain
   4. `gpg --import <path to secret key file>`
   5. Enter the encryption password when prompted
   6. List the secret key ids: `gpg --list-secret-keys --keyid-format LONG`
   7. Note the key ID of the imported key
7. Refresh the key expiration date
   8. `gpg --edit-key <key id>`
   9. `expire`
   10. **IMPORTANT:** When promted, write `1y`. The default is `no-expiration`, **which is not what we want**.
   11. You should be prompted for the pass-phrase again. Enter the same pass-phrase as before.
   10. `save` + enter
11. If done correctly, it should exit the `gpg` interactive prompt

## Update the key in your secret manager

1. Export the public key: `gpg --output maven.public.gpg --armor --export <key id>` 
2. Export the private key: `gpg --output maven.secret.gpg --armor --export-secret-keys <key id>`
   3. You will be prompted for the pass-phrase. Enter the same pass-phrase as before.
4. Rename the existing public and private keys in your secret manager to something like `maven.public.gpg[expired]`. 
   5. You can delete the `n-2`nd iteration of keys, as we are pretty sure we won't need them again at this point.
5. Upload `maven.public.gpg` and `maven.secret.gpg` to your secret manager.
6. **IMPORTANT** Delete `maven.public.gpg` and `maven.secret.gpg` from your local machine.

## Publish the public key to a key server

1. `gpg --keyserver hkp://keyserver.ubuntu.com --send-key <key id>`

Now the downstream clients can verify the signature of the artifacts you sign with this key.

## Update the secret environment variables in the github action

1. Copy the `base64` encoded secret key from your secret manager
   2. `gpg --armor --export-secret-keys $LONG_ID | base64 | pbcopy`
2. Paste the `base64` encoded secret key into the `PGP_SECRET` secret variable in the github action
