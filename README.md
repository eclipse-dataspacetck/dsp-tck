# Dataspace TCK

* [1. Overview](#1-overview)
    * [1.1 Modules](#11-modules)
    * [1.2 Test packages](#12--dataspace-protocol-test-modules-and-packages)
* [2. Set up the connector under test](#2-set-up-the-connector-under-test-cut)
    * [2.1 Required Configuration](#21-required-configuration)
    * [2.2 Metadata endpoint](#22-metadata-endpoint)
    * [2.3 Catalog protocol](#23-catalog-protocol)
    * [2.4 Contract negotiation protocol](#24-contract-negotiation-protocol)
    * [2.5 Transfer process protocol](#25-transfer-process-protocol)
* [3 Run the Dataspace Protocol TCK](#3-run-the-dataspace-protocol-tck)
    * [3.1 JUnit](#31-junit)
    * [3.2 TestContainers](#32-testcontainers)
    * [3.3 Docker](#33-docker)
    * [3.4 Command line](#34-command-line)
        * [3.4.1 Test plan generation](#341-test-plan-generation)

# 1. Overview

The Dataspace TCK is a composable and extensible technology compatibility kit (TCK) for dataspace specifications built
on the JUnit Platform.

It also provides a runtime (DSP TCK) for testing connectors using
the [Dataspace Protocol Specification](https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol), ensuring
that they are compliant and interoperable.

## 1.1 Modules

The Dataspace TCK consists of several modules, each serving a specific purpose:

- **boot**: The bootstrap module used for interfacing between the TCK system and its host environment.
- **core**: The core TCK framework and extensibility system.
- **dsp**: Runtime and verification tests for the Dataspace Protocol Specification.

## 1.2  Dataspace protocol test modules and packages

The module `dsp` contains the runtime and verification tests for
the [Dataspace Protocol Specification](https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol).
implemented using the TCK framework.

It includes several test packages, each corresponding to a specific protocol or functionality:

- **dsp-metadata**: Tests for the metadata endpoint in package `org.eclipse.dataspacetck.dsp.verification.metadata`.
- **dsp-catalog**: Tests for the catalog protocol in package `org.eclipse.dataspacetck.dsp.verification.catalog`.
- **dsp-contract-negotiation**: Tests for the contract negotiation protocol in package
  `org.eclipse.dataspacetck.dsp.verification.cn`.
- **dsp-transfer-process**: Tests for the transfer process protocol in package
  `org.eclipse.dataspacetck.dsp.verification.tp`.

To have a complete overview of the available tests and the flows under test, the TCK provides a test plan that can be
generated in Markdown format. [see section 3.4.1](#341-test-plan-generation).

> The current supported version of the Dataspace Protocol Specification is 2025-1.

# 2. Set up the connector under test (CUT)

A base [configuration](#21-required-configuration) is required in order to run the Dataspace Protocol TCK tests against
a connector under test (CUT). The configuration specifies the connector's details, such as
its agent ID, HTTP URL, and other test specific settings.

The CUT must be running and reachable via network before running the TCK tests.

## 2.1 Required Configuration

The following configuration is required to run the Dataspace Protocol TCK against a CUT:

| Property                                                | Description                                                                                                                                | Example                              |
|---------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| `dataspacetck.debug`                                    | Enables debug logging for the TCK.                                                                                                         | `true`                               |
| `dataspacetck.local.connector`                          | Enable the embedded connector, useful for testing and debugging the DSP TCK. It should be disabled when running against a remote connector | `false`                              |
| `dataspacetck.host`                                     | The hostname of the TCK server.                                                                                                            | `0.0.0.0`                            |
| `dataspacetck.port`                                     | The port of the TCK server.                                                                                                                | `8083`                               |
| `dataspacetck.callback.address`                         | The callback address of the TCK. Attached as `callbackAddress` in DSP messages when required to signal the response channel to the CUT     | `http://localhost:8083`              |
| `dataspacetck.dsp.connector.agent.id`                   | The agent ID of the connector under test. This is used to identify the connector in the TCK tests.                                         | `urn:connector:example-connector`    |
| `dataspacetck.dsp.connector.http.url`                   | The dataspace protocol URL of the connector under test. This is used to access the connector's endpoints during the TCK tests.             | `http://localhost:8080/dsp`          |
| `dataspacetck.dsp.connector.http.base.url`              | The base URL of the connector under test. This is used to access the connector's metadata endpoint during the TCK tests.                   | `http://localhost:8080`              |
| `dataspacetck.dsp.connector.http.headers.authorization` | The authorization header that the TCK will attach on every DSP request                                                                     | `{}`                                 |                           
| `dataspacetck.dsp.connector.negotiation.initiate.url`   | The URL for signaling the connector to start a contract negotiation with the DSP TCK connector.                                            | `http://localhost:8080/negotiations` |
| `dataspacetck.dsp.connector.transfer.initiate.url`      | The URL for signaling the connector to start a transfer request with the DSP TCK connector.                                                | `http://localhost:8080/transfers`    |
| `dataspacetck.dsp.default.wait`                         | The default wait time for the TCK to wait for responses from the CUT. This is used to ensure that the TCK does not timeout too early.      | `5000` (5 seconds)                   |

An example configuration file for the TCK is provided in `config/tck/sample.tck.properties`. This file contains
the necessary settings to run the TCK against a connector. The file also contains test-specific configuration
explained in each test section.

## 2.2 Metadata endpoint

This includes tests from the `org.eclipse.dataspacetck.dsp.verification.metadata` package, which verify that the
connector implements
the [metadata endpoint](https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol/HEAD/#exposure-of-dataspace-protocol-versions)
correctly.

The metadata tests are identified with group `MET` followed by the test number, e.g., `MET:01-01`.

## 2.3 Catalog protocol

This includes tests from the `org.eclipse.dataspacetck.dsp.verification.catalog` package, which verify that the
connector implements
the [catalog protocol](https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol/HEAD/#catalog-protocol)
correctly.

The catalog tests are identified with group `CAT` followed by the test number, e.g., `CAT:01-01`.

An additional configuration property available for the catalog tests, allowing
connector implementors to specify a dataset id to be used in each catalog protocol test.
This can be used for seeding the connector in order to behave accordingly with the test requirements.

The format is as follows:

`CAT_<testNumber>_DATASETID=<datasetId>` where `<testNumber>` is the number of the test

An example is `CAT_01_01_DATASETID=CAT0101` where `CAT0101` is the dataset id to be used in the test
and the TCK will use this id to assert that is retrieved correctly from the catalog request.

## 2.4 Contract negotiation protocol

The contract negotiation protocol tests are implemented in the `org.eclipse.dataspacetck.dsp.verification.cn` package,
which verify that the connector implements
the [contract negotiation protocol](https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol/HEAD/#contract-negotiation-protocol)
correctly.

The contract negotiation tests are identified with group `CN` when the CUT is acting as a provider and `CN_C` when the
CUT is acting as consumer. The group is followed by the test number, e.g., `CN:01-01`, `CN_C:01-01`.

Two additional configuration properties are available for the contract negotiation tests:

- `<group>_<testNumber>_DATASETID=<datasetId>` where `<testNumber>` is the number of the test and `<group>` is either
  `CN`
  or `CN_C`.
- `<group>_<testNumber>_OFFERID=<offerId>` where `<testNumber>` is the number of the test and `<group>` is either `CN`
  or `CN_C`.

These properties allow connector implementors to specify a dataset id and an offer id to be used in each contract
negotiation protocol test.

When running the CUT as a consumer, the CUT must expose a special endpoint that the TCK will use
to signal the connector to start a contract negotiation.
The TCK for each `CN_C` test will do a POST request to the configured URL with the following body:

```json
{
  "datasetId": "<datasetId>",
  "offerId": "<offerId>",
  "providerId": "<providerId>",
  "connectorAddress": "<connectorAddress>"
}
```

where `<datasetId>` and `<offer>` can be specified in the configuration, `<providerId>` and `<connectorAddress>` are the
ID and the URL of the TCK connector.

The endpoint can be configured using the property `dataspacetck.dsp.connector.negotiation.initiate.url`

## 2.5 Transfer process protocol

The transfer process protocol tests are implemented in the `org.eclipse.dataspacetck.dsp.verification.tp` package,
which verify that the connector implements
the [transfer process protocol](https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol/HEAD/#transfer-process-protocol)
correctly.

The tests are identified with group `TP` when the CUT is acting as a provider and `TP_C` when the CUT is acting as
consumer. The group is followed by the test number, e.g., `TP:01-01`, `TP_C:01-01`.

Two additional configuration properties are available for the transfer process tests:

- `<group>_<testNumber>_AGREEMENTID=<format>` where `<testNumber>` is the number of the test and `<group>` is either
  `TP`
  or `TP_C`.
- `<group>_<testNumber>_FORMAT=<format>` where `<testNumber>` is the number of the test and `<group>` is either
  `TP` or `TP_C`.

These properties allow connector implementors to specify an agreement id and
a [format](https://eclipse-dataspace-protocol-base.github.io/DataspaceProtocol/HEAD/#transfer-request-message) to be
used in each transfer process protocol test.

When running the CUT as a consumer, the CUT must expose a special endpoint that the TCK will use
to signal the connector to start a transfer process.
The TCK for each `TP_C` test will do a POST request to the configured URL with the following body:

```json
{
  "agreementId": "<agreementId>",
  "format": "<format>",
  "providerId": "<providerId>",
  "connectorAddress": "<connectorAddress>"
}
```

where `<agreementId>` and `<format>` can be specified in the configuration, `<providerId>` and `<connectorAddress>`
are the ID and the URL of the TCK connector.

The endpoint can be configured using the property `dataspacetck.dsp.connector.transfer.initiate.url`.

# 3. Run the Dataspace Protocol TCK

To run the Dataspace Protocol TCK against a connector under test (CUT), you can use various methods depending on your
environment and preferences. The TCK can be run
using JUnit (useful for JVM based languages), TestContainers, Docker, or from the command line.

## 3.1 JUnit

To run the Dataspace Protocol TCK using JUnit, the `TckRuntime` class from the `tck-runtime` module can be used.

Additional modules need to be imported in the classpath in order tu run
specific [tests](#12--dataspace-protocol-test-modules-and-packages)

```java

@Test
void assertDspCompatibility() throws IOException {
    // boostrap CUT runtime

    var runtime = TckRuntime.Builder.newInstance()
            .properties(Map.of()) // Add any additional properties if needed
            .addPackage("org.eclipse.dataspacetck.dsp.verification")
            .launcher(DspSystemLauncher.class) // it could be also configured with the 'dataspacetck.launcher' property
            .monitor(new ConsoleMonitor(true, true))
            .build();

    var summary = runtime.execute();

    assertThat(summary.getFailures())
            .extracting(f -> "- " + f.getTestIdentifier().getDisplayName() + " (" + f.getException() + ")")
            .isEmpty();
    assertThat(summary.getTestsSucceededCount()).isGreaterThan(0);
}
```

The properties accepted are the same as those specified in the example file `config/tck/sample.tck.properties`,
or documented [here](#21-required-configuration). In this example we are adding the package
`org.eclipse.dataspacetck.dsp.verification`
which should run all the tests in the `dsp` module if the following dependencies are present in the classpath:

- `org.eclipse.dataspacetck.dsp:tck-runtime:<VERSION>`: to launch the TCK from JUnit
- `org.eclipse.dataspacetck.dsp:core:<VERSION>`: for the ConsoleMonitor
- `org.eclipse.dataspacetck.dsp:dsp-system:<VERSION>`: for the launcher that runs the DSP TCK test cases
- `org.eclipse.dataspacetck.dsp:dsp-metadata:<VERSION>`: for metadata test cases
- `org.eclipse.dataspacetck.dsp:dsp-catalog:<VERSION>`: for catalog test cases
- `org.eclipse.dataspacetck.dsp:dsp-contract-negotiation:<VERSION>`: for contract negotiation test cases
- `org.eclipse.dataspacetck.dsp:dsp-transfer-process:<VERSION>`: for transfer process test cases
- `org.junit.platform:junit-platform-launcher:<JUNIT_VERSION>`

## 3.3 Docker

The `dsp-tck` module provides a Docker image that can be used to run the TCK in a containerized environment.
Once started the TCK will run immediately the tests against the connector under test (CUT) and
will provide a report of the results. To use the docker method, the CUT should be running before starting the TCK
container. Since communication between the TCK and the CUT is done over HTTP, and it's bidirectional, the CUT
should be reachable from the TCK container.

This can be done by:

- configuring an additional host `host.docker.internal:host-gateway`
- using `host.docker.internal` for all URLs on the host system

```bash
docker pull eclipsedataspacetck/dsp-tck-runtime:latest

docker run --rm --name dsp-tck \
  --add-host "host.docker.internal:host-gateway" \
  -p "8080:8080" \
  --mount type=bind,source=<configFile>,target=/etc/tck/config.properties  \
  eclipsedataspacetck/dsp-tck-runtime:latest  
```

alternative to the `--mount` with a configuration file, the `-e` option in order to pass environment variables to the
container.

## 3.2 TestContainers

If the requirements is to set up a CI environment for continuous integration, the TCK can be run using `TestContainers`.
for better control over the tests execution and reporting.

```java

@Timeout(300)
@Test
void assertDspCompatibility() throws InterruptedException {

    // bootstrap CUT runtime

    try (GenericContainer<?> container = new TckContainer<>("eclipsedataspacetck/dsp-tck-runtime:latest")) {

        // configure a logger
        var monitor = new ConsoleMonitor(">>> TCK Runtime (Docker)", ConsoleMonitor.Level.INFO, true);

        container.addFileSystemBind(resourceConfig("docker.tck.properties"), "/etc/tck/config.properties", BindMode.READ_ONLY, SelinuxContext.SINGLE);
        container.withExtraHost("host.docker.internal", "host-gateway");
        container.start();

        var latch = new CountDownLatch(1);
        var hasFailed = new AtomicBoolean(false);
        container.followOutput(outputFrame -> {
            monitor.info(outputFrame.getUtf8String());
            if (outputFrame.getUtf8String().toLowerCase().contains("there were failing tests")) {
                hasFailed.set(true);
            }
            if (outputFrame.getUtf8String().toLowerCase().contains("test run complete")) {
                latch.countDown();
            }
        });

        assertThat(latch.await(10, TimeUnit.MINUTES)).isTrue();
        assertThat(hasFailed.get()).describedAs("There were failing TCK tests, please check the log output above").isFalse();
    }
}


```

## 3.4 Command line

The Dataspace Protocol TCK does not ship a runnable binary distribution, but it can be built locally using Gradle:

```bash   
git clone https://github.com/eclipse-dataspacetck/dsp-tck
./gradlew shadowJar
```

and once the CUT is running, run the TCK using the command line:

```bash
 java -jar dsp/dsp-tck/build/libs/dsp-tck-runtime.jar -config config/tck/sample.tck.properties
```

### 3.4.1 Test plan generation

The TCK framework allows to generate a test plan that can be used for having a visual representation of the executed
tests in Markdown format.

By executing the following command, a test plan will be generated in the `build/testplan.md` file:

```bash
/gradlew genTestPlan
```

## 4. Filing Challenges

If you believe there is a bug in the TCK or there is an invalid test assertion, please file a
bug [here](https://github.com/eclipse-dataspacetck/dsp-tck/issues).
