#!/usr/bin/env kotlin
@file:CompilerOptions("-opt-in=kotlin.RequiresOptIn", "-nowarn")

import java.io.File

val filename = args[0]

val result = """
KTLINT_SUMMARY<<EOF
<details>
<summary>retult.xml</summary>

```xml
${File(filename).readText()}
```
EOF
""".trimIndent()


File(System.getenv("GITHUB_ENV")).writeText(
    result
)
println(result)
