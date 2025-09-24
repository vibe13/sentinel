# Sentinel

Sentinel is a Quarkus-based service that generates and verifies [SLSA Provenance](https://slsa.dev) metadata for builds.  
It integrates with **cosign** and the **Rekor transparency log** to produce and verify in-toto attestations.

---

## Features

- Generate [SLSA v1.0 / v1.1 provenance](https://slsa.dev/spec/v1.0/provenance) for PNC builds  
- Sign provenance using [cosign](https://github.com/sigstore/cosign)  
- Verify signatures against the Rekor transparency log  
- Export signed provenance in the `.intoto.jsonl` format  

---

## Usage

### Generate Provenance

See the test classes to understand how to generate a Provenance and use the CosignWrapper to sign and verify the raw signature and bundle.

### Sign Provenance with Cosign

```bash
COSIGN_PASSWORD="" cosign sign-blob   --key cosign.key   --bundle provenance.intoto.jsonl   provenance.json > provenance.sig
```

This produces:
- `provenance.sig`: the detached signature  
- `provenance.intoto.jsonl`: the signed bundle in in-toto JSONL format  

### Verify Provenance with Cosign

```bash
cosign verify-blob   --key cosign.pub   --bundle provenance.intoto.jsonl   --signature provenance.sig   provenance.json
```

If verification succeeds, cosign checks:
1. The detached signature is valid  
2. The Rekor transparency log contains the entry  
3. The `.intoto.jsonl` bundle matches the signed blob  

---

## Development

### Build

```bash
./mvnw clean install
```

### Run Tests

```bash
./mvnw test
```

---

## Configuration

Configuration is managed via `application.yaml`.  
Example:

```yaml
provenance:
  pnc:
    build_log:
      endpoint: https://orch.com/pnc-rest/v2/builds/{id}/logs/build
    alignment_log:
      endpoint: https://orch.com/pnc-rest/v2/builds/{id}/logs/align
    builder:
      components:
        version:
          orch: 3.2.9
          indy: 1.0.0
          rex: 0.1.2
  slsa:
    spec:
      version: "1.1"
      specs:
        "1.1":
          _type: https://in-toto.io/Statement/v1
          predicateType: https://slsa.dev/provenance/v1
  builders:
    pnc:
      buildType: "https://github.com/project-ncl/workflow/v1"
```

---

## License

Apache License 2.0